package core.transaction;

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

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.statuses.StatusCls;
import core.item.statuses.StatusFactory;
import database.ItemStatusMap;
//import database.BalanceMap;
import database.DBSet;

public class GenesisIssueStatusTransaction extends Transaction 
{
	
	private static final byte TYPE_ID = (byte)GENESIS_ISSUE_STATUS_TRANSACTION;
	private static final String NAME_ID = "Genesis Issue Status";
	private static final int BASE_LENGTH = SIMPLE_TYPE_LENGTH + CREATOR_LENGTH + TIMESTAMP_LENGTH;
	private StatusCls status;
	
	public GenesisIssueStatusTransaction(PublicKeyAccount creator, StatusCls status, long timestamp) 
	{
		super(TYPE_ID, NAME_ID, timestamp);

		this.creator = creator;
		this.status = status;
		this.generateSignature();

	}

	//GETTERS/SETTERS
	//public static String getName() { return "Genesis Issue Status"; }
	
	public void generateSignature() {
		
		//return generateSignature1(this.recipient, this.amount, this.timestamp);
		byte[] data = this.toBytes( false, null );

		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);
				
		this.signature = digest;		
		this.status.setReference(digest);

	}
		
	public StatusCls getStatus()
	{
		return this.status;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/NAME/DISCRIPTION/QUANTITY/DIVISIBLE
		transaction.put("creator", this.creator.getAddress());
		transaction.put("name", this.status.getName());
		transaction.put("description", this.status.getDescription());
				
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

		//READ STATUS
		// read without reference
		StatusCls status = StatusFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
		//position += status.getDataLength(false);
						
		return new GenesisIssueStatusTransaction(creator, status, timestamp);
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

		//WRITE STATUS
		// without reference
		data = Bytes.concat(data, this.status.toBytes(false));
				
		return data;
	}
	
	@Override
	public int getDataLength(boolean asPack)
	{
		// not include status REFERENCE
		return BASE_LENGTH + this.status.getDataLength(false);
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
		int nameLength = this.status.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > StatusCls.MAX_NAME_LENGTH || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK DESCRIPTION LENGTH
		int descriptionLength = this.status.getDescription().getBytes(StandardCharsets.UTF_8).length;
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
		ItemStatusMap statusMap = db.getItemStatusMap();
		int mapSize = statusMap.size();
		//LOGGER.info("GENESIS MAP SIZE: " + statusMap.size());
		long key = 0l;
		if (mapSize == 0) {
			// initial map set
			statusMap.set(0l, this.status);
		} else {
			key = statusMap.add(this.status);
			//this.status.setKey(key);
		}
		db.getIssueStatusMap().set(this.signature, key); // need to SET but not ADD !

	}


	@Override
	public void orphan(DBSet db, boolean asPack) 
	{
														
		//UPDATE REFERENCE OF CREATOR
		// for not genesis - this.creator.setLastReference(this.reference, db);
		this.creator.removeReference(db);
				
		//DELETE FROM DATABASE
		long statusKey = db.getIssueStatusMap().get(this);
		db.getItemStatusMap().delete(statusKey);	
				
		//DELETE ORPHAN DATA
		db.getIssueStatusMap().delete(this);

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
