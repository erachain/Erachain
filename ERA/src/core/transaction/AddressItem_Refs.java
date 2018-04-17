package core.transaction;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;

import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.item.ItemCls;
import datachain.DCSet;

public abstract class AddressItem_Refs extends Transaction
{

	private ItemCls item;

	public AddressItem_Refs(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte feePow, long timestamp, Long reference)
	{
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);
		this.item = item;
	}

	/*
	public AddressItem_Refs(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte feePow, long timestamp, Long reference, byte[] signature)
	{
		this(typeBytes, NAME_ID, creator, item, feePow, timestamp, reference);
		this.signature = signature;
		if (item.getReference() == null) item.setReference(signature); // set reference
		//item.resolveKey(DBSet.getInstance());
		///// if (timestamp > 1000 ) setDB; // not asPaack
	}
	 */
	public AddressItem_Refs(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte[] signature)
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
	@Override
	public String viewItemName()
	{
		return item.toString();
	}

	//@Override
	@Override
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
		data = Bytes.concat(data, this.item.toBytes(false, false));

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
	@Override
	public int isValid(DCSet db, Long releaserReference)
	{

		//CHECK NAME LENGTH
		int nameLength = this.item.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > ItemCls.MAX_NAME_LENGTH || nameLength < item.getMinNameLen())
		{
			return INVALID_NAME_LENGTH;
		}

		//CHECK DESCRIPTION LENGTH
		int descriptionLength = this.item.getDescription().getBytes(StandardCharsets.UTF_8).length;
		if(descriptionLength > BlockChain.MAX_REC_DATA_BYTES)
		{
			return INVALID_DESCRIPTION_LENGTH;
		}

		return super.isValid(db, releaserReference);

	}

	//PROCESS/ORPHAN

	//@Override
	@Override
	public void process(Block block, boolean asPack)
	{
		//UPDATE CREATOR
		super.process(block, asPack);

		// SET REFERENCE if not setted before (in Imprint it setted)
		if (this.item.getReference() == null) this.item.setReference(this.signature);

		//INSERT INTO DATABASE
		this.item.insertToMap(this.dcSet, 1000l);

	}

	//@Override
	@Override
	public void orphan(boolean asPack)
	{
		//UPDATE CREATOR
		super.orphan(asPack);

		//DELETE FROM DATABASE
		long key = this.item.removeFromMap(this.dcSet);
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

		int add_fee = 0;
		int len = this.getItem().getName().length();
		if (len < 10) {
			add_fee = 3^(10-len) * 100;
		}

		return calcCommonFee() + BlockChain.FEE_PER_BYTE * (500 + add_fee);
	}
}
