package qora.transaction;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
//import java.util.Map;
//import java.util.logging.Logger;

//import ntp.NTP;

import org.json.simple.JSONObject;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.notes.NoteCls;
import qora.notes.NoteFactory;
import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import database.NoteMap;
import database.DBSet;

public class IssueNoteTransaction extends Transaction 
{
	private static final byte TYPE_ID = (byte)ISSUE_NOTE_TRANSACTION;
	private static final String NAME_ID = "Issue Note";

	private static final int BASE_LENGTH = Transaction.BASE_LENGTH;

	private NoteCls note;
	
	public IssueNoteTransaction(byte[] typeBytes, PublicKeyAccount creator, NoteCls note, byte feePow, long timestamp, byte[] reference) 
	{
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);		
		this.note = note;
	}
	public IssueNoteTransaction(byte[] typeBytes, PublicKeyAccount creator, NoteCls note, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, note, feePow, timestamp, reference);		
		this.signature = signature;
		this.calcFee();
	}
	public IssueNoteTransaction(PublicKeyAccount creator, NoteCls note, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, note, feePow, timestamp, reference, signature);
	}
	public IssueNoteTransaction(PublicKeyAccount creator, NoteCls note, byte feePow, long timestamp, byte[] reference) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, note, feePow, timestamp, reference);
	}

	//GETTERS/SETTERS
	//public static String getName() { return "Issue Note"; }

	public NoteCls getNote()
	{
		return this.note;
	}
	
	
	//@Override
	public void sign(PrivateKeyAccount creator)
	{
		super.sign(creator);
		this.note.setReference(this.signature);
	}

	//PARSE CONVERT
	
	public static Transaction Parse(byte[] data) throws Exception
	{	
		//CHECK IF WE MATCH BLOCK LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length");
		}
		
		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;
	
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;
		
		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;
		
		//READ NOTE
		// note parse without reference - if is = signature
		NoteCls note = NoteFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
		position += note.getDataLength(false);
		
		//READ FEE POWER
		byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
		byte feePow = feePowBytes[0];
		position += 1;
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new IssueNoteTransaction(typeBytes, creator, note, feePow, timestamp, reference, signatureBytes);
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/NAME/DISCRIPTION/QUANTITY/DIVISIBLE
		transaction.put("creator", this.creator.getAddress());
		transaction.put("name", this.note.getName());
		transaction.put("description", this.note.getDescription());
				
		return transaction;	
	}
	
	@Override
	public byte[] toBytes(boolean withSign) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		data = Bytes.concat(data, this.typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());
		
		//WRITE NOTE
		// without reference
		data = Bytes.concat(data, this.note.toBytes(false));
		
		//WRITE FEE POWER
		byte[] feePowBytes = new byte[1];
		feePowBytes[0] = this.feePow;
		data = Bytes.concat(data, feePowBytes);

		//SIGNATURE
		if (withSign) data = Bytes.concat(data, this.signature);
		
		return data;
	}
	
	@Override
	public int getDataLength()
	{
		// not include note reference
		return BASE_LENGTH + this.note.getDataLength(false);
	}
	
	//VALIDATE
		
	@Override
	public int isValid(DBSet db) 
	{
		
		//CHECK NAME LENGTH
		int nameLength = this.note.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > 256 || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK DESCRIPTION LENGTH
		int descriptionLength = this.note.getDescription().getBytes(StandardCharsets.UTF_8).length;
		if(descriptionLength > 4000 || descriptionLength < 1)
		{
			return INVALID_DESCRIPTION_LENGTH;
		}
				
		//CHECK CREATOR
		if(!Crypto.getInstance().isValidAddress(this.note.getCreator().getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//CHECK IF CREATOR HAS ENOUGH MONEY
		if(this.creator.getConfirmedBalance(FEE_KEY, db).compareTo(this.fee) == -1)
		{
			return NOT_ENOUGH_FEE;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
				
		return VALIDATE_OK;
	}
	
	//PROCESS/ORPHAN

	//@Override
	public void process(DBSet db)
	{
		//UPDATE CREATOR
		super.process(db);
								
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.signature, db);
		this.note.setReference(this.signature);
		
		//INSERT INTO DATABASE
		NoteMap noteMap = db.getNoteMap();
		int mapSize = noteMap.size();
		//Logger.getGlobal().info("GENESIS MAP SIZE: " + assetMap.size());
		long key = 0l;
		if (mapSize == 0) {
			// initial map set
			noteMap.set(0l, this.note);
		} else {
			key = noteMap.add(this.note);
			//this.asset.setKey(key);
		}
				
		//SET ORPHAN DATA
		db.getIssueNoteMap().set(this.signature, key);

		//Logger.getGlobal().info("issue NOTE KEY: " + note.getKey(db));

	}

	//@Override
	public void orphan(DBSet db) 
	{
		//UPDATE CREATOR
		super.orphan(db);
										
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.reference, db);
				
		//DELETE FROM DATABASE
		long key = db.getIssueNoteMap().get(this);
		db.getNoteMap().delete(key);	
				
		//DELETE ORPHAN DATA
		db.getIssueNoteMap().delete(this);
	}

	@Override
	public List<Account> getInvolvedAccounts() {
		return Arrays.asList(this.creator);
	}

	@Override
	public boolean isInvolved(Account account) {
		String address = account.getAddress();
		
		if(address.equals(creator.getAddress()))
		{
			return true;
		}
		
		return false;
	}

	public int calcBaseFee() {
		return calcCommonFee() + (Transaction.FEE_PER_BYTE * 500);
	}
}
