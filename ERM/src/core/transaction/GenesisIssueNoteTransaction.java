package qora.transaction;

import java.math.BigDecimal;
//import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 import org.apache.log4j.Logger;

//import ntp.NTP;

import org.json.simple.JSONObject;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.item.notes.NoteCls;
import qora.item.notes.NoteFactory;
import qora.crypto.Base58;
import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.ItemNoteMap;
//import database.BalanceMap;
import database.DBSet;

public class GenesisIssueNoteTransaction extends Transaction 
{
	
	private static final byte TYPE_ID = (byte)GENESIS_ISSUE_NOTE_TRANSACTION;
	private static final String NAME_ID = "Genesis Issue Note";
	private static final int BASE_LENGTH = SIMPLE_TYPE_LENGTH + CREATOR_LENGTH + TIMESTAMP_LENGTH;
	private NoteCls note;
	
	public GenesisIssueNoteTransaction(PublicKeyAccount creator, NoteCls note, long timestamp) 
	{
		super(TYPE_ID, NAME_ID, timestamp);

		this.creator = creator;
		this.note = note;
		this.generateSignature();

	}

	//GETTERS/SETTERS
	//public static String getName() { return "Genesis Issue Note"; }
	
	public void generateSignature() {
		
		//return generateSignature1(this.recipient, this.amount, this.timestamp);
		byte[] data = this.toBytes( false, null );

		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);
				
		this.signature = digest;		
		this.note.setReference(digest);

	}
		
	public NoteCls getNote()
	{
		return this.note;
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

	//PARSE CONVERT
	public static Transaction Parse(byte[] data) throws Exception
	{	
		//CHECK IF WE MATCH BLOCK LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length");
		}
		
		// READ TYPE
		//byte[] typeBytes = Arrays.copyOfRange(data, 0, SIMPLE_TYPE_LENGTH);
		int position = SIMPLE_TYPE_LENGTH;
	
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;
						
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;

		//READ NOTE
		// read without reference
		NoteCls note = NoteFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
		//position += note.getDataLength(false);
						
		return new GenesisIssueNoteTransaction(creator, note, timestamp);
	}	
	
	
	@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference) 
	{
		
		//WRITE TYPE
		byte[] data = new byte[]{TYPE_ID};
		//byte[] typeBytes = Ints.toByteArray(TYPE_ID);
		//typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		//data = Bytes.concat(data, TYPE_ID);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
				
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());

		//WRITE NOTE
		// without reference
		data = Bytes.concat(data, this.note.toBytes(false));
				
		return data;
	}
	
	@Override
	public int getDataLength(boolean asPack)
	{
		// not include note REFERENCE
		return BASE_LENGTH + this.note.getDataLength(false);
	}
	
	//VALIDATE
	
	public boolean isSignatureValid()
	{
		return Arrays.equals(this.signature, this.getSignature());
		//return true;
	}
	
	@Override
	public int isValid(DBSet db, byte[] releaserReference) 
	{
		
		//CHECK IF ADDRESS IS VALID
		if(!Crypto.getInstance().isValidAddress(this.creator.getAddress()))
		{
			return INVALID_ADDRESS;
		}

		//CHECK NAME LENGTH
		int nameLength = this.note.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > NoteCls.MAX_NAME_LENGTH || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK DESCRIPTION LENGTH
		int descriptionLength = this.note.getDescription().getBytes(StandardCharsets.UTF_8).length;
		if(descriptionLength > 4000 || descriptionLength < 1)
		{
			return INVALID_DESCRIPTION_LENGTH;
		}
				
		return VALIDATE_OK;
	}
	
	//PROCESS/ORPHAN

	@Override
	public void process(DBSet db, boolean asPack)
	{
		
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.signature, db);

		//INSERT INTO DATABASE
		ItemNoteMap noteMap = db.getNoteMap();
		int mapSize = noteMap.size();
		//LOGGER.info("GENESIS MAP SIZE: " + noteMap.size());
		long key = 0l;
		if (mapSize == 0) {
			// initial map set
			noteMap.set(0l, this.note);
		} else {
			key = noteMap.add(this.note);
			//this.note.setKey(key);
		}
		db.getIssueNoteMap().set(this.signature, key); // need to SET but not ADD !

	}


	@Override
	public void orphan(DBSet db, boolean asPack) 
	{
														
		//UPDATE REFERENCE OF CREATOR
		// for not genesis - this.creator.setLastReference(this.reference, db);
		this.creator.removeReference(db);
				
		//DELETE FROM DATABASE
		long noteKey = db.getIssueNoteMap().get(this);
		db.getNoteMap().delete(noteKey);	
				
		//DELETE ORPHAN DATA
		db.getIssueNoteMap().delete(this);

	}

	@Override
	public HashSet<Account> getInvolvedAccounts()
	{
		return this.getRecipientAccounts();
	}

	@Override
	public HashSet<Account> getRecipientAccounts()
	{
		HashSet<Account> accounts = new HashSet<>();
		accounts.add(this.creator);
		return accounts;
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{	
		return this.creator.getAddress().equals(account.getAddress());		
	}

	public int calcBaseFee() {
		return 0;
	}
}
