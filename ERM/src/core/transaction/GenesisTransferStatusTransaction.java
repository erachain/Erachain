package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ntp.NTP;

import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Base58;
import core.crypto.Crypto;
import database.PersonStatusMap;
import database.DBSet;

public class GenesisTransferStatusTransaction extends Transaction {

	private static final byte TYPE_ID = (byte)Transaction.GENESIS_ASSIGN_STATUS_TRANSACTION;
	private static final String NAME_ID = "Genesis Assign Status";
	//private static final int RECIPIENT_LENGTH = TransactionAmount.RECIPIENT_LENGTH;
	private static final int RECIPIENT_LENGTH = TransactionAmount.RECIPIENT_LENGTH;
	private static final int BASE_LENGTH = SIMPLE_TYPE_LENGTH + TIMESTAMP_LENGTH + CREATOR_LENGTH + RECIPIENT_LENGTH + KEY_LENGTH;

	private Account recipient;
	private long key;
	
	public GenesisTransferStatusTransaction(PublicKeyAccount creator, Account recipient, long key, long timestamp) 
	{
		super(TYPE_ID, NAME_ID, timestamp);
		this.creator = creator;
		this.recipient = recipient;
		this.key = key;
		this.generateSignature();
	}
	
	//GETTERS/SETTERS
	//public static String getName() { return NAME; }

	public void generateSignature() {
		
		//return generateSignature1(this.recipient, this.amount, this.timestamp);
		byte[] data = this.toBytes( false, null );

		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);
				
		this.signature = digest;		

	}

	public Account getRecipient()
	{
		return this.recipient;
	}
		
	public long getKey()
	{
		return this.key;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/RECIPIENT/AMOUNT/STATUS
		transaction.put("creator", this.creator.getAddress());
		transaction.put("recipient", this.recipient.getAddress());
		transaction.put("status", this.key);
				
		return transaction;	
	}

	//PARSE/CONVERT
	
	public static Transaction Parse(byte[] data) throws Exception{
		
		//CHECK IF WE MATCH BLOCK LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length: " + data.length + " in " + NAME_ID);
		}
		
		// READ TYPE
		//byte[] typeBytes = Arrays.copyOfRange(data, 0, SIMPLE_TYPE_LENGTH);
		int position = SIMPLE_TYPE_LENGTH;
	
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;
		
		/*
		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;
		*/
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;
		
		//READ RECIPIENT
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		Account recipient = new Account(Base58.encode(recipientBytes));
		position += RECIPIENT_LENGTH;
		
		//READ KEY
		byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
		long key = Longs.fromByteArray(keyBytes);	
		position += KEY_LENGTH;
						
		return new GenesisTransferStatusTransaction(creator, recipient, key, timestamp);	
	}	
	
	@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference)
	{
		//byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] data = new byte[]{TYPE_ID};
		//byte[] typeBytes = Ints.toByteArray(TYPE_ID);
		//typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		//data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		/*
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		*/
		
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());
		
		//WRITE RECIPIENT
		data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));
		
		//WRITE KEY
		byte[] keyBytes = Longs.toByteArray(this.key);
		keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
		data = Bytes.concat(data, keyBytes);
						
		return data;
	}

	@Override
	public int getDataLength(boolean asPack) 
	{
		return BASE_LENGTH;
	}


	//VALIDATE
	@Override
	public boolean isSignatureValid()
	{
		
		byte[] digest = this.getSignature();
		return Arrays.equals(digest, this.signature);
	}

	@Override
	public int isValid(DBSet db, byte[] releaserReference) 
	{
		
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.recipient.getAddress()))
		{
			return INVALID_ADDRESS;
		}

		return VALIDATE_OK;
	}

	//PROCESS/ORPHAN
	
	@Override
	public void process(DBSet db, boolean asPack) 
	{
		//UPDATE CREATOR
						
		//UPDATE RECIPIENT
		//this.recipient.setConfirmedStatus(this.key, 0L, db);
		
		//UPDATE REFERENCE OF CREATOR
		// not need this.creator.setLastReference(this.signature, db);
		//UPDATE REFERENCE OF RECIPIENT
		this.recipient.setLastReference(this.signature, db);
	}

	@Override
	public void orphan(DBSet db, boolean asPack) 
	{
		//UPDATE CREATOR
		
		//UPDATE RECIPIENT
		//this.recipient.setConfirmedStatus(this.key, -1L, db);
		
		//UPDATE REFERENCE OF CREATOR
		// not needthis.creator.setLastReference(this.reference, db);		
		//UPDATE REFERENCE OF RECIPIENT
		this.recipient.removeReference(db);
	}

	//REST
	
	@Override
	public HashSet<Account> getInvolvedAccounts()
	{
		HashSet<Account> accounts = new HashSet<Account>();
		accounts.add(this.creator);
		accounts.addAll(this.getRecipientAccounts());
		return accounts;
	}

	@Override
	public HashSet<Account> getRecipientAccounts() {
		HashSet<Account> accounts = new HashSet<Account>();
		accounts.add(this.recipient);
		return accounts;
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(creator.getAddress()) || address.equals(recipient.getAddress()))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public Long viewTime(Account account) 
	{
		return 0L;
	}

	//@Override
	public Map<String, Map<Long, Long>> getStatusTime() 
	{
		Map<String, Map<Long, Long>> statusTime = new LinkedHashMap<>();
						
		return statusTime;
	}
	public int calcBaseFee() {
		return 0;
	}
}
