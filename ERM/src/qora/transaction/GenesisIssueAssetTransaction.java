package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import ntp.NTP;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
//import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.assets.Asset;
import core.crypto.Crypto;
import database.DBSet;

public class GenesisIssueAssetTransaction extends Transaction 
{
	private static final int RECIPIENT_LENGTH = Account.ADDRESS_LENGTH;
	// + KEY_LENGTH
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH; // + AMOUNT_LENGTH + KEY_LENGTH; // -RECIPIENT_LENGTH

	private Asset asset;
	
	public GenesisIssueAssetTransaction(long timestamp, Asset asset) 
	{
		this.type = GENESIS_ISSUE_ASSET_TRANSACTION;
		this.timestamp = timestamp;
		this.reference = new byte[0];
		this.asset = asset;

		// after set all params!!!
		this.signature = generateSignature(timestamp, asset);
		//this.maker = asset.getMaker();

	}

	//GETTERS/SETTERS
		
	@Override
	public byte[] getSignature() {
		
		return generateSignature(this.timestamp, this.asset);
	}
	public Asset getAsset()
	{
		return this.asset;
	}
	
	//PARSE/CONVERT
	
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
				
		return new GenesisIssueAssetTransaction(timestamp, asset);
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/NAME/DISCRIPTION/QUANTITY/DIVISIBLE
		//transaction.put("creator", this.getAsset().getMaker().getAddress());
		transaction.put("name", this.getAsset().getName());
		transaction.put("description", this.getAsset().getDescription());
		transaction.put("quantity", this.getAsset().getQuantity());
		transaction.put("divisible", this.getAsset().isDivisible());
				
		return transaction;	
	}
	
	@Override
	public int getDataLength()
	{
		return TYPE_LENGTH + BASE_LENGTH + this.asset.getDataLength();
	}
	
	//VALIDATE
		
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
				
		return VALIDATE_OKE;
	}
	
	//PROCESS/ORPHAN

	@Override
	public void process(DBSet db)
	{
		
		Account recipient = this.asset.getMaker();
										
		//UPDATE REFERENCE OF ISSUER
		recipient.setLastReference(this.signature, db);
		
		//INSERT INTO DATABASE
		long key = db.getAssetMap().add(this.asset);
		
		//ADD ASSETS TO OWNER
		recipient.setConfirmedBalance(key, new BigDecimal(this.asset.getQuantity()).setScale(8), db);
		
		//SET ORPHAN DATA
		db.getIssueAssetMap().set(this, key);
	}


	@Override
	public void orphan(DBSet db) 
	{
		Account recipient = this.asset.getMaker();
		
		//UNDO BALANCE
		recipient.setConfirmedBalance(BigDecimal.ZERO, db);
		
		//UNDO REFERENCE
		recipient.removeReference(db);
				
		//DELETE FROM DATABASE
		long key = db.getIssueAssetMap().get(this);
		db.getAssetMap().delete(key);	
		
		//REMOVE ASSETS FROM OWNER
		//recipient.setConfirmedBalance(key, BigDecimal.ZERO.setScale(8), db);
		
		//DELETE ORPHAN DATA
		db.getIssueAssetMap().delete(this);
	}
	
	public static byte[] toByte(long timestamp, Asset asset) 
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
				
		return data;
		
	}

	public byte[] toByte(boolean sign)
	{
		byte[] data = toByte(this.timestamp, this.asset);

		return data;
	}

	private static byte[] generateSignature(long timestamp, Asset asset)
	{
		byte[] data = toByte(timestamp, asset);
		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);

		return digest;
	}

	@Override
	public List<Account> getInvolvedAccounts() 
	{
		return Arrays.asList(this.asset.getMaker());
	}


	@Override
	public boolean isInvolved(Account account) 
	{
		return this.asset.getMaker().getAddress().equals(account.getAddress());		
	}


	@Override
	public BigDecimal getAmount(Account account) 
	{
		if(account.getAddress().equals(this.asset.getMaker().getAddress()))
		{
			return new BigDecimal(this.asset.getQuantity()).setScale(8);
		}
		
		return BigDecimal.ZERO;
	}
	}