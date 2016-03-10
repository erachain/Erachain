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
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.assets.Asset;
import qora.crypto.Base58;
import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

//import database.BalanceMap;
import database.DBSet;

public class GenesisIssueAssetTransaction extends Transaction 
{
	private static final int BASE_LENGTH = RECIPIENT_LENGTH + TIMESTAMP_LENGTH;

	private PublicKeyAccount recipient;
	private Asset asset;
	
	public GenesisIssueAssetTransaction(PublicKeyAccount recipient, Asset asset, long timestamp) 
	{
		// new reference = byte[]{}
		super(GENESIS_ISSUE_ASSET_TRANSACTION, timestamp);

		this.recipient = recipient;
		this.asset = asset;
		this.signature = generateSignature(asset, timestamp);

	}

	//GETTERS/SETTERS
	@Override
	public byte[] getSignature() {
		
		return this.signature;
	}
	
	public Account getRecipient()
	{
		return this.recipient;
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
		transaction.put("recipient", this.recipient.getAddress());
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
						
		//READ RECIPIENT
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		PublicKeyAccount recipient = new PublicKeyAccount(recipientBytes);
		position += RECIPIENT_LENGTH;

		//READ ASSET
		Asset asset = Asset.parse(Arrays.copyOfRange(data, position, data.length));
		//position += asset.getDataLength();
						
		return new GenesisIssueAssetTransaction(recipient, asset, timestamp);
	}	
	
	
	@Override
	public byte[] toBytes(boolean withSign) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(GENESIS_ISSUE_ASSET_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
				
		//WRITE RECIPIENT
		data = Bytes.concat(data, this.recipient.getPublicKey());

		//WRITE ASSET
		data = Bytes.concat(data, this.asset.toBytes(true));
				
		return data;
	}
	
	@Override
	public int getDataLength()
	{
		return TYPE_LENGTH + BASE_LENGTH + this.asset.getDataLength();
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
		if(!Crypto.getInstance().isValidAddress(this.recipient.getAddress()))
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
		
		//INSERT INTO DATABASE
		long key = db.getAssetMap().add(this.asset);
		
		//UPDATE REFERENCE OF CREATOR
		this.recipient.setLastReference(this.signature, db);

		//ADD ASSETS TO OWNER
		this.recipient.setConfirmedBalance(key, new BigDecimal(this.asset.getQuantity()).setScale(8), db);
		
		//SET ORPHAN DATA
		db.getIssueAssetMap().set(this, key);
    	//db.getAssetMap().set(key, this.asset);

	}


	@Override
	public void orphan(DBSet db) 
	{
														
		//UPDATE REFERENCE OF CREATOR
		this.recipient.setLastReference(this.reference, db);
				
		//DELETE FROM DATABASE
		long key = db.getIssueAssetMap().get(this);
		db.getAssetMap().delete(key);	
		
		//REMOVE ASSETS FROM OWNER
		this.recipient.setConfirmedBalance(key, BigDecimal.ZERO.setScale(8), db);
		
		//DELETE ORPHAN DATA
		db.getIssueAssetMap().delete(this);

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


	@Override
	public BigDecimal getAmount(Account account) 
	{		
		return BigDecimal.ZERO;
	}

	public byte[] generateSignature1(Asset asset, long timestamp) 
	{
		byte[] data = this.toBytes(false);
				
		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);
				
		return digest;
	}
	public static byte[] generateSignature(Asset asset, long timestamp) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(GENESIS_ISSUE_ASSET_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
						
		//WRITE ASSET
		data = Bytes.concat(data , asset.toBytes(false));
				
		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);
				
		return digest;
	}
	
	@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
				
		assetAmount = addAssetAmount(assetAmount, this.creator.getAddress(), this.asset.getKey(), new BigDecimal(this.asset.getQuantity()).setScale(8));

		return assetAmount;
	}
}
