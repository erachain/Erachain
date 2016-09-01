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

public class IssueAssetTransaction extends Issue_ItemRecord 
{
	private static final byte TYPE_ID = (byte)ISSUE_ASSET_TRANSACTION;
	private static final String NAME_ID = "Issue Asset";

	//private static final int BASE_LENGTH = Transaction.BASE_LENGTH;

	//private AssetCls asset;
	
	public IssueAssetTransaction(byte[] typeBytes, PublicKeyAccount creator, AssetCls asset, byte feePow, long timestamp, Long reference) 
	{
		super(typeBytes, NAME_ID, creator, asset, feePow, timestamp, reference);		
		//this.asset = asset;
	}
	public IssueAssetTransaction(byte[] typeBytes, PublicKeyAccount creator, AssetCls asset, byte feePow, long timestamp, Long reference, byte[] signature) 
	{
		super(typeBytes, NAME_ID, creator, asset, feePow, timestamp, reference, signature);		
	}
	// as pack
	public IssueAssetTransaction(byte[] typeBytes, PublicKeyAccount creator, AssetCls asset, byte[] signature) 
	{
		super(typeBytes, NAME_ID, creator, asset, (byte)0, 0l, null, signature);		
	}
	public IssueAssetTransaction(PublicKeyAccount creator, AssetCls asset, byte feePow, long timestamp, Long reference, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, asset, feePow, timestamp, reference, signature);
	}
	// as pack
	public IssueAssetTransaction(PublicKeyAccount creator, AssetCls asset, Long reference, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, asset, (byte)0, 0l, reference, signature);
	}
	public IssueAssetTransaction(PublicKeyAccount creator, AssetCls asset, byte feePow, long timestamp, Long reference) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, asset, feePow, timestamp, reference);
	}

	//GETTERS/SETTERS
	//public static String getName() { return "Issue Asset"; }

	public long getAssetKey(DBSet db)
	{
		return this.getItem().getKey(db);
	}

	@Override
	public BigDecimal getAmount() {
		return new BigDecimal(((AssetCls)this.getItem()).getQuantity());
	}

	@Override
	public BigDecimal getAmount(String address) {
		
		if(address.equals(this.creator.getAddress()))
		{
			return this.getAmount();
		}

		return BigDecimal.ZERO.setScale(8);
	}

	/*
	@Override
	public BigDecimal getAmount(Account account) {
		String address = account.getAddress();
		return getAmount(address);
	}
	*/

	public String viewAmount(String address) {
		return this.getAmount().toString();
	}

	//PARSE CONVERT
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = super.toJson();
		AssetCls asset = (AssetCls)this.getItem(); 
		//ADD CREATOR/NAME/DISCRIPTION/QUANTITY/DIVISIBLE
		transaction.put("quantity", asset.getQuantity());
		transaction.put("scale", asset.getScale());
		transaction.put("divisible", asset.isDivisible());
				
		return transaction;	
	}

	public static Transaction Parse(byte[] data, Long releaserReference) throws Exception
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

		Long reference = null;
		if (!asPack) {
			//READ REFERENCE
			byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			reference = Longs.fromByteArray(referenceBytes);	
			position += REFERENCE_LENGTH;
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
			return new IssueAssetTransaction(typeBytes, creator, asset, signatureBytes);
		}
	}	
	
	
	/*
	//@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference) 
	{

		byte[] data = super.toBytes(withSign, releaserReference);
		
		//WRITE ASSET
		// without reference
		data = Bytes.concat(data, this.asset.toBytes(false));
				
		return data;
	}
	*/
	
	/*
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
	*/
	
	//VALIDATE
		
	//@Override
	public int isValid(DBSet db, Long releaserReference) 
	{
		
		int result = super.isValid(db, releaserReference);
		if(result != Transaction.VALIDATE_OK) return result;
		
		//CHECK QUANTITY
		AssetCls asset = (AssetCls)this.getItem();
		long maxQuantity = asset.isDivisible() ? 10000000000L : 1000000000000000000L;
		long quantity = asset.getQuantity();
		//if(quantity > maxQuantity || quantity < 0 && quantity != -1 && quantity != -2 )
		if(quantity > maxQuantity || quantity < 0 )
		{
			return INVALID_QUANTITY;
		}
		
		return Transaction.VALIDATE_OK;
	}
	
	//PROCESS/ORPHAN

	//@Override
	public void process(DBSet db, boolean asPack)
	{
		//UPDATE CREATOR
		super.process(db, asPack);
										
		//ADD ASSETS TO OWNER
		this.creator.setBalance(this.getItem().getKey(db),
				new BigDecimal(((AssetCls)this.getItem()).getQuantity()).setScale(8), db);
		

	}

	//@Override
	public void orphan(DBSet db, boolean asPack) 
	{
		//UPDATE CREATOR
		super.orphan(db, asPack);

		//REMOVE ASSETS FROM OWNER
		this.creator.setBalance(this.getItem().getKey(db), BigDecimal.ZERO.setScale(8), db);
	}

	/*
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
	*/

	//@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);

		AssetCls asset = (AssetCls)this.getItem();
		assetAmount = addAssetAmount(assetAmount, this.creator.getAddress(), asset.getKey(DBSet.getInstance()),
				new BigDecimal(asset.getQuantity()).setScale(8));

		return assetAmount;
	}
	
	@Override
	public int calcBaseFee() {
		return 10 * (calcCommonFee() + (Transaction.FEE_PER_BYTE * 1000));
	}
}
