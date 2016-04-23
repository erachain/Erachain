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
import core.item.ItemCls;
import core.item.ItemFactory;
import database.Item_Map;
//import database.BalanceMap;
import database.DBSet;

public class GenesisIssueItem_Record extends Transaction 
{
	
	protected static final int BASE_LENGTH = SIMPLE_TYPE_LENGTH + CREATOR_LENGTH + TIMESTAMP_LENGTH;

	private ItemCls item;
	
	public GenesisIssueItem_Record(byte type, String NAME_ID, PublicKeyAccount creator, ItemCls item, long timestamp) 
	{
		super(type, NAME_ID, timestamp);

		this.creator = creator;
		this.item = item;
		this.generateSignature();

	}

	//GETTERS/SETTERS
	
	public void generateSignature() {
		
		//return generateSignature1(this.recipient, this.amount, this.timestamp);
		byte[] data = this.toBytes( false, null );

		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);
				
		this.signature = digest;		
		this.item.setReference(digest);

	}
		
	public ItemCls getItem()
	{
		return this.item;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/NAME/DISCRIPTION/QUANTITY/DIVISIBLE
		transaction.put("creator", this.creator.getAddress());
		transaction.put(this.item.getItemType(), this.item.toJson());
				
		return transaction;	
	}

	//PARSE CONVERT
	//public abstract Transaction Parse(byte[] data);
	
	@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference) 
	{
		
		//WRITE TYPE in typeBytes[0]
		byte[] data = new byte[]{this.typeBytes[0]};
		//byte[] typeBytes = Ints.toByteArray(TYPE_ID);
		//typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		//data = Bytes.concat(data, TYPE_ID);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
				
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());

		//WRITE ITEM
		// without reference
		data = Bytes.concat(data, this.item.toBytes(false));
				
		return data;
	}
	
	@Override
	public int getDataLength(boolean asPack)
	{
		// not include item REFERENCE
		return BASE_LENGTH + this.item.getDataLength(false);
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
		int nameLength = this.item.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > ItemCls.MAX_NAME_LENGTH || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK DESCRIPTION LENGTH
		int descriptionLength = this.item.getDescription().getBytes(StandardCharsets.UTF_8).length;
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
		long key = this.item.insertToMap(db);

	}


	@Override
	public void orphan(DBSet db, boolean asPack) 
	{
														
		//UPDATE REFERENCE OF CREATOR
		// for not genesis - this.creator.setLastReference(this.reference, db);
		this.creator.removeReference(db);
				
		//DELETE FROM DATABASE
		long key = this.item.removeFromMap(db);

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
