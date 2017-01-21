package core.transaction;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
//import java.util.Map;
// import org.apache.log4j.Logger;

//import ntp.NTP;

import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple4;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.Crypto;
import core.item.ItemCls;
//import database.ItemItemMap;
import database.DBSet;

public abstract class Issue_ItemRecord extends Transaction 
{

	//private static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK;
	//private static final int BASE_LENGTH = Transaction.BASE_LENGTH;

	private ItemCls item;
	
	public Issue_ItemRecord(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte feePow, long timestamp, Long reference) 
	{
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);
		this.item = item;
	}
	public Issue_ItemRecord(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte feePow, long timestamp, Long reference, byte[] signature) 
	{
		this(typeBytes, NAME_ID, creator, item, feePow, timestamp, reference);		
		this.signature = signature;
		if (item.getReference() == null) item.setReference(signature); // set reference
		//item.resolveKey(DBSet.getInstance());
		if (timestamp > 1000 ) this.calcFee(); // not asPaack
	}
	public Issue_ItemRecord(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte[] signature) 
	{
		this(typeBytes, NAME_ID, creator, item, (byte)0, 0l, null);		
		this.signature = signature;
		if (this.item.getReference() == null) this.item.setReference(signature);
		//item.resolveKey(DBSet.getInstance());
	}

	//GETTERS/SETTERS
	//public static String getName() { return "Issue Item"; }

	public ItemCls getItem()
	{
		return this.item;
	}
	public String viewItemName()
	{
		return item.toString();
	}
	
	@Override
	public boolean hasPublicText() {
		return true;	
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
	public byte[] toBytes(boolean withSign, Long releaserReference) 
	{
		byte[] data = super.toBytes(withSign, releaserReference);
		
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
	public int isValid(DBSet db, Long releaserReference) 
	{
		
		//CHECK NAME LENGTH
		int nameLength = this.item.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > ItemCls.MAX_NAME_LENGTH || nameLength < 12)
		{
			return INVALID_NAME_LENGTH;
		}

		//CHECK ICON LENGTH
		int iconLength = this.item.getIcon().length;
		if(iconLength > ItemCls.MAX_ICON_LENGTH)
		{
			return INVALID_ICON_LENGTH;
		}

		//CHECK IMAGE LENGTH
		int imageLength = this.item.getImage().length;
		if(imageLength > ItemCls.MAX_IMAGE_LENGTH)
		{
			return INVALID_IMAGE_LENGTH;
		}

		//CHECK DESCRIPTION LENGTH
		int descriptionLength = this.item.getDescription().getBytes(StandardCharsets.UTF_8).length;
		if(descriptionLength > 4000)
		{
			return INVALID_DESCRIPTION_LENGTH;
		}
				
		long count = this.item.getDBMap(db).getSize();
		if (count < 10) {
			// FIRST Persons only by ME
			if (this.creator.equals(BlockChain.GENESIS_ADMIN)) {
				return VALIDATE_OK;
			} else {
				return Transaction.ACCOUNT_NOT_PERSONALIZED;
			}
		}

		return super.isValid(db, releaserReference);
	
	}
	
	//PROCESS/ORPHAN
	//@Override
	public void process(DBSet db, Block block, boolean asPack)
	{
		//UPDATE CREATOR
		super.process(db, block, asPack);
		
		// SET REFERENCE if not setted before (in Imprint it setted)
		if (this.item.getReference() == null)
			this.item.setReference(this.signature);
		
		//INSERT INTO DATABASE
		this.item.insertToMap(db);
				
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

	@Override
	public int calcBaseFee() {		
		return calcCommonFee() + BlockChain.FEE_PER_BYTE * 128 * BlockChain.ISSUE_MULT_FEE;
	}
}
