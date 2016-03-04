package qora.transaction;

import java.math.BigDecimal;
//import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import ntp.NTP;

import org.json.simple.JSONObject;

import qora.account.Account;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.assets.Asset;
import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;

public class GenesisIssueAssetTransaction extends Transaction 
{
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH;

	private Asset asset;
	
	public GenesisIssueAssetTransaction(Asset asset, long timestamp) 
	{
		super(GENESIS_ISSUE_ASSET_TRANSACTION, BigDecimal.ZERO, timestamp, new byte[]{}, generateSignature(asset, timestamp));
		
		this.asset = asset;
	}

	//GETTERS/SETTERS
	
	public PublicKeyAccount getIssuer()
	{
		return null;
	}
	
	public Asset getAsset()
	{
		return this.asset;
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
						
		//READ ASSET
		Asset asset = Asset.parse(Arrays.copyOfRange(data, position, data.length));
		position += asset.getDataLength();
						
		return new GenesisIssueAssetTransaction(asset, timestamp);
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/NAME/DISCRIPTION/QUANTITY/DIVISIBLE
		transaction.put("name", this.getAsset().getName());
		transaction.put("description", this.getAsset().getDescription());
		transaction.put("quantity", this.getAsset().getQuantity());
		transaction.put("divisible", this.getAsset().isDivisible());
				
		return transaction;	
	}
	
	@Override
	public byte[] toBytes() 
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
				
		//WRITE ASSET
		data = Bytes.concat(data , this.asset.toBytes(true));
				
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
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(GENESIS_ISSUE_ASSET_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE ASSET
		data = Bytes.concat(data , this.asset.toBytes(false));
						
		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);
				
		//CHECK IF EQUAL
		return Arrays.equals(digest, this.signature);
	}
	
	@Override
	public int isValid(DBSet db) 
	{
		
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
		
		//ADD ASSETS TO OWNER
		this.asset.getOwner().setConfirmedBalance(key, new BigDecimal(this.asset.getQuantity()).setScale(8), db);
		
		//SET ORPHAN DATA
		db.getIssueAssetMap().set(this, key);
	}


	@Override
	public void orphan(DBSet db) 
	{
				
		//DELETE FROM DATABASE
		long key = db.getIssueAssetMap().get(this);
		db.getAssetMap().delete(key);	
		
		//REMOVE ASSETS FROM OWNER
		this.asset.getOwner().setConfirmedBalance(key, BigDecimal.ZERO.setScale(8), db);
		
		//DELETE ORPHAN DATA
		db.getIssueAssetMap().delete(this);
	}

	@Override
	public PublicKeyAccount getCreator() 
	{
		return null;
	}


	@Override
	public List<Account> getInvolvedAccounts() 
	{
		List<Account> accounts = new ArrayList<Account>();
		accounts.add(this.asset.getOwner());
		return accounts;
	}


	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.asset.getOwner().getAddress()))
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
}
