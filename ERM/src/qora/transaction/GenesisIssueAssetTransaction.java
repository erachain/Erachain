package qora.transaction;

import java.math.BigDecimal;
//import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

//import ntp.NTP;

import org.json.simple.JSONObject;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.assets.Asset;
import qora.assets.AssetFactory;
import qora.crypto.Base58;
import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.AssetMap;
//import database.BalanceMap;
import database.DBSet;

public class GenesisIssueAssetTransaction extends Transaction 
{
	
	private static final int TYPE_ID = GENESIS_ISSUE_ASSET_TRANSACTION;
	private static final String NAME_ID = "Genesis Issue Asset";
	private static final int BASE_LENGTH = CREATOR_LENGTH + TIMESTAMP_LENGTH;
	private Asset asset;
	
	public GenesisIssueAssetTransaction(PublicKeyAccount creator, Asset asset, long timestamp) 
	{
		super(TYPE_ID, NAME_ID, timestamp);

		this.creator = creator;
		this.asset = asset;
		this.generateSignature();

	}

	//GETTERS/SETTERS
	//public static String getName() { return "Genesis Issue Asset"; }
	
	public void generateSignature() {
		
		//return generateSignature1(this.recipient, this.amount, this.timestamp);
		byte[] data = this.toBytes( false );

		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);
				
		this.signature = digest;		
		this.asset.setReference(digest);

	}
		
	public Asset getAsset()
	{
		return this.asset;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/NAME/DISCRIPTION/QUANTITY/DIVISIBLE
		transaction.put("creator", this.creator.getAddress());
		transaction.put("name", this.getAsset().getName());
		transaction.put("description", this.getAsset().getDescription());
		transaction.put("quantity", this.getAsset().getQuantity());
		transaction.put("divisible", this.getAsset().isDivisible());
				
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
		
		int position = 0;
		
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;
						
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;

		//READ ASSET
		// read without reference
		Asset asset = AssetFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
		//position += asset.getDataLength(false);
						
		return new GenesisIssueAssetTransaction(creator, asset, timestamp);
	}	
	
	
	@Override
	public byte[] toBytes(boolean withSign) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(TYPE_ID);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
				
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());

		//WRITE ASSET
		// without reference
		data = Bytes.concat(data, this.asset.toBytes(false));
				
		return data;
	}
	
	@Override
	public int getDataLength()
	{
		// not include asset REFERENCE
		return TYPE_LENGTH + BASE_LENGTH + this.asset.getDataLength(false);
	}
	
	//VALIDATE
	
	public boolean isSignatureValid()
	{
		return true;
	}
	
	@Override
	public int isValid(DBSet db) 
	{
		
		//CHECK IF ADDRESS IS VALID
		if(!Crypto.getInstance().isValidAddress(this.creator.getAddress()))
		{
			return INVALID_ADDRESS;
		}

		//CHECK NAME LENGTH
		int nameLength = this.asset.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > 400 || nameLength < 1)
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
		
		return VALIDATE_OK;
	}
	
	//PROCESS/ORPHAN

	@Override
	public void process(DBSet db)
	{
		
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.signature, db);

		//INSERT INTO DATABASE
		AssetMap assetMap = db.getAssetMap();
		int mapSize = assetMap.size();
		//Logger.getGlobal().info("GENESIS MAP SIZE: " + assetMap.size());
		long key = 0l;
		if (mapSize == 0) {
			// initial map set
			assetMap.set(0l, this.asset);
		} else {
			key = assetMap.add(this.asset);
			//this.asset.setKey(key);
		}
		db.getIssueAssetMap().set(this.signature, key); // need to SET but not ADD !

		//ADD ASSETS TO OWNER
		this.creator.setConfirmedBalance(key, new BigDecimal(this.asset.getQuantity()).setScale(8), db);

		//Logger.getGlobal().info("GENESIS ASSET KEY: " + key);

	}


	@Override
	public void orphan(DBSet db) 
	{
														
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.reference, db);
				
		//DELETE FROM DATABASE
		long assetKey = db.getIssueAssetMap().get(this);
		db.getAssetMap().delete(assetKey);	
		
		//REMOVE ASSETS FROM OWNER
		this.creator.setConfirmedBalance(assetKey, BigDecimal.ZERO.setScale(8), db);
		
		//DELETE ORPHAN DATA
		db.getIssueAssetMap().delete(this);

		//Logger.getGlobal().info("GENESIS ORpHAN ASSET KEY: " + assetKey);

	}


	@Override
	public List<Account> getInvolvedAccounts() 
	{
		List<Account> accounts = new ArrayList<Account>();
		accounts.add(this.asset.getCreator());
		return accounts;
	}


	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.asset.getCreator().getAddress()))
		{
			return true;
		}
		
		return false;
	}


	//@Override
	public BigDecimal viewAmount(Account account) 
	{		
		return BigDecimal.ZERO;
	}
	
	//@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
				
		assetAmount = addAssetAmount(assetAmount, this.creator.getAddress(), this.asset.getKey(), new BigDecimal(this.asset.getQuantity()).setScale(8));

		return assetAmount;
	}
	public int calcBaseFee() {
		return 0;
	}
}
