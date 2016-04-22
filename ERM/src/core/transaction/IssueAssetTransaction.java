package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 import org.apache.log4j.Logger;

import ntp.NTP;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.assets.AssetFactory;
import database.ItemAssetBalanceMap;
import database.DBSet;

public class IssueAssetTransaction extends Transaction 
{
	private static final byte TYPE_ID = (byte)ISSUE_ASSET_TRANSACTION;
	private static final String NAME_ID = "Issue Asset";

	//private static final int BASE_LENGTH = Transaction.BASE_LENGTH;

	private AssetCls asset;
	
	public IssueAssetTransaction(byte[] typeBytes, PublicKeyAccount creator, AssetCls asset, byte feePow, long timestamp, byte[] reference) 
	{
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);		
		this.asset = asset;
	}
	public IssueAssetTransaction(byte[] typeBytes, PublicKeyAccount creator, AssetCls asset, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, asset, feePow, timestamp, reference);		
		this.signature = signature;
		this.calcFee();
	}
	// as pack
	public IssueAssetTransaction(byte[] typeBytes, PublicKeyAccount creator, AssetCls asset, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, asset, (byte)0, 0l, reference);
		this.signature = signature;
	}
	public IssueAssetTransaction(PublicKeyAccount creator, AssetCls asset, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, asset, feePow, timestamp, reference, signature);
	}
	// as pack
	public IssueAssetTransaction(PublicKeyAccount creator, AssetCls asset, byte[] reference, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, asset, (byte)0, 0l, reference, signature);
	}
	public IssueAssetTransaction(PublicKeyAccount creator, AssetCls asset, byte feePow, long timestamp, byte[] reference) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, asset, feePow, timestamp, reference);
	}

	//GETTERS/SETTERS
	//public static String getName() { return "Issue Asset"; }

	public AssetCls getAsset()
	{
		return this.asset;
	}
	
	
	//@Override
	public void sign(PrivateKeyAccount creator, boolean asPack)
	{
		super.sign(creator, asPack);
		this.asset.setReference(this.signature);
	}

	//PARSE CONVERT
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/NAME/DISCRIPTION/QUANTITY/DIVISIBLE
		transaction.put("creator", this.getAsset().getCreator().getAddress());
		transaction.put("name", this.getAsset().getName());
		transaction.put("description", this.getAsset().getDescription());
		transaction.put("quantity", this.getAsset().getQuantity());
		transaction.put("scale", this.getAsset().getScale());
		transaction.put("divisible", this.getAsset().isDivisible());
				
		return transaction;	
	}

	public static Transaction Parse(byte[] data, byte[] releaserReference) throws Exception
	{	
		boolean asPack = releaserReference != null;
		
		//CHECK IF WE MATCH BLOCK LENGTH
		if (data.length < BASE_LENGTH_AS_PACK
				| !asPack & data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length " + data.length);
		}
		
		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;

		long timestamp = 0;
		if (!asPack) {
			//READ TIMESTAMP
			byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
			timestamp = Longs.fromByteArray(timestampBytes);	
			position += TIMESTAMP_LENGTH;
		}

		byte[] reference;
		if (!asPack) {
			//READ REFERENCE
			reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			position += REFERENCE_LENGTH;
		} else {
			reference = releaserReference;
		}
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;
		
		byte feePow = 0;
		if (!asPack) {
			//READ FEE POWER
			byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
			feePow = feePowBytes[0];
			position += 1;
		}
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		position += SIGNATURE_LENGTH;

		/////		
		
		//READ ASSET
		// asset parse without reference - if is = signature
		AssetCls asset = AssetFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
		position += asset.getDataLength(false);

		if (!asPack) {
			return new IssueAssetTransaction(typeBytes, creator, asset, feePow, timestamp, reference, signatureBytes);
		} else {
			return new IssueAssetTransaction(typeBytes, creator, asset, reference, signatureBytes);
		}
	}	
	
	
	//@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference) 
	{

		byte[] data = super.toBytes(withSign, releaserReference);
		
		//WRITE ASSET
		// without reference
		data = Bytes.concat(data, this.asset.toBytes(false));
				
		return data;
	}
	
	@Override
	public int getDataLength(boolean asPack)
	{
		// not include asset reference
		if (asPack) {
			return BASE_LENGTH_AS_PACK + this.asset.getDataLength(false);
		} else {
			return BASE_LENGTH + this.asset.getDataLength(false);
		}
	}
	
	//VALIDATE
		
	//@Override
	public int isValid(DBSet db, byte[] releaserReference) 
	{
		
		//CHECK NAME LENGTH
		int nameLength = this.asset.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > AssetCls.MAX_NAME_LENGTH || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK DESCRIPTION LENGTH
		int descriptionLength = this.asset.getDescription().getBytes(StandardCharsets.UTF_8).length;
		if(descriptionLength > 4000 || descriptionLength < 1)
		{
			return INVALID_DESCRIPTION_LENGTH;
		}
		
		//CHECK QUANTITY
		long maxQuantity = this.asset.isDivisible() ? 10000000000L : 1000000000000000000L;
		if(this.asset.getQuantity() < 1 || this.asset.getQuantity() > maxQuantity)
		{
			return INVALID_QUANTITY;
		}
		
		return super.isValid(db, releaserReference);

	}
	
	//PROCESS/ORPHAN

	//@Override
	public void process(DBSet db, boolean asPack)
	{
		//UPDATE CREATOR
		super.process(db, asPack);
								
		this.asset.setReference(this.signature);
		
		//INSERT INTO DATABASE
		long key = db.getItemAssetMap().add(this.asset);
		//this.asset.setKey(key);
		
		//ADD ASSETS TO OWNER
		//this.asset.getCreator().setConfirmedBalance(key, new BigDecimal(this.asset.getQuantity()).setScale(8), db);
		this.creator.setConfirmedBalance(key, new BigDecimal(this.asset.getQuantity()).setScale(8), db);
		
		//SET ORPHAN DATA
		db.getIssueAssetMap().set(this.signature, key);

		//LOGGER.info("issue ASSET KEY: " + asset.getKey(db));

	}

	//@Override
	public void orphan(DBSet db, boolean asPack) 
	{
		//UPDATE CREATOR
		super.orphan(db, asPack);
				
		//DELETE FROM DATABASE
		long key = db.getIssueAssetMap().get(this);
		db.getItemAssetMap().delete(key);	
		
		//REMOVE ASSETS FROM OWNER
		//this.asset.getCreator().setConfirmedBalance(key, BigDecimal.ZERO.setScale(8), db);
		this.creator.setConfirmedBalance(key, BigDecimal.ZERO.setScale(8), db);
		
		//DELETE ORPHAN DATA
		db.getIssueAssetMap().delete(this);
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


	//@Override
	public BigDecimal viewAmount(Account account) 
	{
		/* 
		if(account.getAddress().equals(this.creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		*/
		
		return BigDecimal.ZERO;
	}

	//@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), ItemAssetBalanceMap.FEE_KEY, this.fee);
		
		assetAmount = addAssetAmount(assetAmount, this.creator.getAddress(), this.asset.getKey(), new BigDecimal(this.asset.getQuantity()).setScale(8));

		return assetAmount;
	}
	public int calcBaseFee() {
		return calcCommonFee() + (Transaction.FEE_PER_BYTE * 1000);
	}
}
