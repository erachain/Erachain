package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ntp.NTP;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.statuses.StatusCls;
import database.ItemAssetBalanceMap;
import database.DBSet;

public class GenesisCertifyPersonRecord extends Genesis_Record {

	private static final byte TYPE_ID = (byte)Transaction.GENESIS_CERTIFY_PERSON_TRANSACTION;
	private static final String NAME_ID = "GENESIS Certify Person";
	private static final int RECIPIENT_LENGTH = TransactionAmount.RECIPIENT_LENGTH;
	private static final int BASE_LENGTH = Genesis_Record.BASE_LENGTH + RECIPIENT_LENGTH + KEY_LENGTH;

	private Account recipient;
	private long key;
	
	public GenesisCertifyPersonRecord(Account recipient, long key) 
	{
		super(TYPE_ID, NAME_ID);
		this.recipient = recipient;
		this.key = key;
		this.generateSignature();
	}
	
	//GETTERS/SETTERS
	//public static String getName() { return NAME; }

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
		JSONObject transaction = super.toJson();
				
		//ADD CREATOR/RECIPIENT/AMOUNT/ASSET
		transaction.put("recipient", this.recipient.getAddress());
		transaction.put("person", this.key);
				
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
					
		//READ RECIPIENT
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		Account recipient = new Account(Base58.encode(recipientBytes));
		position += RECIPIENT_LENGTH;
		
		//READ KEY
		byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
		long key = Longs.fromByteArray(keyBytes);	
		position += KEY_LENGTH;
						
		return new GenesisCertifyPersonRecord(recipient, key);	
	}	
	
	//@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference)
	{
		byte[] data = super.toBytes(withSign, releaserReference);
				
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
	public int isValid(DBSet db, byte[] releaserReference) 
	{
		
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.recipient.getAddress()))
		{
			return INVALID_ADDRESS;
		}

		if ( !db.getItemPersonMap().contains(this.key) )
		{
			return Transaction.ITEM_PERSON_NOT_EXIST;
		}
				
		return VALIDATE_OK;
	}

	//PROCESS/ORPHAN
	
	@Override
	public void process(DBSet db, boolean asPack) 
	{

		//UPDATE RECIPIENT
		Tuple3<Long, Integer, byte[]> itemP = new Tuple3<Long, Integer, byte[]>(null, 0, this.signature);

		// SET ALIVE PERSON for DURATION permanent
		db.getPersonStatusMap().addItem(this.key, StatusCls.ALIVE_KEY, itemP);

		// SET PERSON ADDRESS - end date as timestamp
		Tuple4<Long, Integer, Integer, byte[]> itemA = new Tuple4<Long, Integer, Integer, byte[]>(this.key, Integer.MAX_VALUE, 0, this.signature);
		Tuple3<Integer, Integer, byte[]> itemA1 = new Tuple3<Integer, Integer, byte[]>(0, 0, this.signature);
		db.getAddressPersonMap().addItem(this.recipient.getAddress(), itemA);
		db.getPersonAddressMap().addItem(this.key, this.recipient.getAddress(), itemA1);
		
		//UPDATE REFERENCE OF RECIPIENT
		this.recipient.setLastReference(this.signature, db);
	}

	@Override
	public void orphan(DBSet db, boolean asPack) 
	{
								
		// UNDO ALIVE PERSON for DURATION
		db.getPersonStatusMap().removeItem(this.key, StatusCls.ALIVE_KEY);

		//UPDATE RECIPIENT
		db.getAddressPersonMap().removeItem(this.recipient.getAddress());
		db.getPersonAddressMap().removeItem(this.key, this.recipient.getAddress());

		//UPDATE REFERENCE OF CREATOR
		// not needthis.creator.setLastReference(this.reference, db);		
		//UPDATE REFERENCE OF RECIPIENT
		this.recipient.removeReference(db);
	}

	//REST
	
	@Override
	public HashSet<Account> getRecipientAccounts()
	{
		HashSet<Account> accounts = new HashSet<>();
		accounts.add(this.recipient);
		return accounts;
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(recipient.getAddress()))
		{
			return true;
		}
		
		return false;
	}

}
