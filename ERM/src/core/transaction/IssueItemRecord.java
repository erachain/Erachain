package core.transaction;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
//import java.util.Map;
// import org.apache.log4j.Logger;

//import ntp.NTP;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Crypto;
import core.item.ItemCls;
//import database.ItemItemMap;
import database.DBSet;

public abstract class IssueItemRecord extends Transaction 
{

	//private static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK;
	//private static final int BASE_LENGTH = Transaction.BASE_LENGTH;

	private ItemCls item;
	
	public IssueItemRecord(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte feePow, long timestamp, byte[] reference) 
	{
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);		
		this.item = item;
	}
	public IssueItemRecord(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(typeBytes, NAME_ID, creator, item, feePow, timestamp, reference);		
		this.signature = signature;
		this.calcFee();
	}
	public IssueItemRecord(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte[] signature) 
	{
		this(typeBytes, NAME_ID, creator, item, (byte)0, 0l, null);		
		this.signature = signature;
	}

	//GETTERS/SETTERS
	//public static String getName() { return "Issue Item"; }

	public ItemCls getItem()
	{
		return this.item;
	}
	
	//@Override
	public void sign(PrivateKeyAccount creator, boolean asPack)
	{
		super.sign(creator, asPack);
		// in IMPRINT reference already setted before sign
		if (this.item.getReference() == null) this.item.setReference(this.signature);
	}

	//PARSE CONVERT
	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/NAME/DISCRIPTION/QUANTITY/DIVISIBLE
		transaction.put("item", this.item.toJson());
				
		return transaction;	
	}
	
	@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference) 
	{
		byte[] data = super.toBytes(withSign, releaserReference);
		
		//WRITE NOTE
		// without reference
		data = Bytes.concat(data, this.item.toBytes(false));
				
		return data;
	}
	
	@Override
	public int getDataLength(boolean asPack)
	{
		// not include item reference
		if (asPack) {
			return BASE_LENGTH_AS_PACK + this.item.getDataLength(false);
		} else {
			return BASE_LENGTH + this.item.getDataLength(false);
		}
	}
	
	//VALIDATE
		
	//@Override
	public int isValid(DBSet db, byte[] releaserReference) 
	{
		
		//CHECK NAME LENGTH
		int nameLength = this.item.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > ItemCls.MAX_NAME_LENGTH || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK DESCRIPTION LENGTH
		int descriptionLength = this.item.getDescription().getBytes(StandardCharsets.UTF_8).length;
		if(descriptionLength > 4000 || descriptionLength < 0)
		{
			return INVALID_DESCRIPTION_LENGTH;
		}
				
		int res = super.isValid(db, releaserReference);
		if (res != Transaction.VALIDATE_OK) return res;
		
		// CHECH MAKER IS PERSON?
		if (!creator.isPerson(db)) return ACCOUNT_NOT_PERSON;
		
		return VALIDATE_OK;
	
	}
	
	//PROCESS/ORPHAN

	//@Override
	public void process(DBSet db, boolean asPack)
	{
		//UPDATE CREATOR
		super.process(db, asPack);
		
		// SET REFERENCE if not setted before (in Imprint it setted)
		if (this.item.getReference() == null) this.item.setReference(this.signature);
		
		//INSERT INTO DATABASE
		long key = this.item.insertToMap(db);

		//LOGGER.info("issue NOTE KEY: " + item.getKey(db));

	}

	//@Override
	public void orphan(DBSet db, boolean asPack) 
	{
		//UPDATE CREATOR
		super.orphan(db, asPack);

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
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress()))
		{
			return true;
		}
		
		return false;
	}

	public int calcBaseFee() {
		return calcCommonFee() + (Transaction.FEE_PER_BYTE * 500);
	}
}
