package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import ntp.NTP;

import org.json.simple.JSONObject;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.assets.Asset;
import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.BalanceMap;
import database.DBSet;

public class IssueAssetTransaction extends Transaction 
{
	private static final int BASE_LENGTH = 1 + TIMESTAMP_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH + SIGNATURE_LENGTH;

	private Asset asset;
	
	public IssueAssetTransaction(PublicKeyAccount creator, Asset asset, long timestamp, byte[] reference) 
	{
		super(ISSUE_ASSET_TRANSACTION, creator, timestamp, reference);		
		this.asset = asset;
	}
	public IssueAssetTransaction(PublicKeyAccount creator, Asset asset, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(creator, asset, timestamp, reference);		
		this.signature = signature;
		this.asset.setReference(signature);
		this.feePow = feePow;
		this.calcFee();
	}
	public IssueAssetTransaction(PublicKeyAccount creator, Asset asset, byte feePow, long timestamp, byte[] reference) 
	{
		this(creator, asset, timestamp, reference);
		this.feePow = feePow;
		this.calcFee();
	}

	//GETTERS/SETTERS
		
	public Asset getAsset()
	{
		return this.asset;
	}
	
	
	//@Override
	public void sign(PrivateKeyAccount creator)
	{
		/*
		byte[] data = this.toBytes( false );
		if ( data == null ) return;

		this.signature = Crypto.getInstance().sign(creator, data);
		*/
		super.sign(creator);
		this.asset.setReference(this.signature);
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
		
		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;
		
		//READ ASSET
		// asset parse without reference - if is = signature
		Asset asset = Asset.parse(Arrays.copyOfRange(data, position, data.length), false);
		position += asset.getDataLength(false);
		
		//READ FEE POWER
		byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
		byte feePow = feePowBytes[0];
		position += 1;
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new IssueAssetTransaction(creator, asset, feePow, timestamp, reference, signatureBytes);
	}	
	
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
	
	/*
	public void generateSignature() {
		
		//return generateSignature1(this.recipient, this.amount, this.timestamp);
		byte[] data = this.toBytes( false );

		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);
				
		this.signature = digest;		
		this.asset.setReference(digest);

	}
	*/

	@Override
	public byte[] toBytes(boolean withSign) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(ISSUE_ASSET_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());
		
		//WRITE ASSET
		// without reference
		data = Bytes.concat(data, this.asset.toBytes(false));
		
		//WRITE FEE POWER
		byte[] feePowBytes = new byte[1];
		feePowBytes[0] = this.feePow;
		data = Bytes.concat(data, feePowBytes);

		//SIGNATURE
		if (withSign) data = Bytes.concat(data, this.signature);
		
		return data;
	}
	
	@Override
	public int getDataLength()
	{
		// not include asset reference
		return TYPE_LENGTH + BASE_LENGTH + this.asset.getDataLength(false);
	}
	
	//VALIDATE
		
	@Override
	public int isValid(DBSet db) 
	{
		//CHECK IF RELEASED
		if(NTP.getTime() < Transaction.getASSETS_RELEASE())
		{
			return NOT_YET_RELEASED;
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
		
		//CHECK CREATOR
		if(!Crypto.getInstance().isValidAddress(this.asset.getCreator().getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//CHECK IF CREATOR HAS ENOUGH MONEY
		if(this.creator.getConfirmedBalance(FEE_KEY, db).compareTo(this.fee) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
				
		return VALIDATE_OK;
	}
	
	//PROCESS/ORPHAN

	@Override
	public void process(DBSet db)
	{
		//UPDATE CREATOR
		process_fee(db);
								
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.signature, db);
		
		//INSERT INTO DATABASE
		long key = db.getAssetMap().add(this.asset);
		//this.asset.setKey(key);
		
		//ADD ASSETS TO OWNER
		this.asset.getCreator().setConfirmedBalance(key, new BigDecimal(this.asset.getQuantity()).setScale(8), db);
		
		//SET ORPHAN DATA
		db.getIssueAssetMap().set(this, key);

		Logger.getGlobal().info("issue ASSET KEY: " + key);

	}


	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE CREATOR
		orphan_fee(db);
										
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.reference, db);
				
		//DELETE FROM DATABASE
		long key = db.getIssueAssetMap().get(this);
		db.getAssetMap().delete(key);	
		
		//REMOVE ASSETS FROM OWNER
		this.asset.getCreator().setConfirmedBalance(key, BigDecimal.ZERO.setScale(8), db);
		
		//DELETE ORPHAN DATA
		db.getIssueAssetMap().delete(this);
	}


	@Override
	public List<Account> getInvolvedAccounts() 
	{
		List<Account> accounts = new ArrayList<Account>();
		accounts.add(this.creator);
		//accounts.add(this.asset.getCreator());
		return accounts;
	}


	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress())
				//|| address.equals(this.asset.getCreator().getAddress())
				)
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
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), BalanceMap.QORA_KEY, this.fee);
		
		assetAmount = addAssetAmount(assetAmount, this.creator.getAddress(), this.asset.getKey(), new BigDecimal(this.asset.getQuantity()).setScale(8));

		return assetAmount;
	}
	public BigDecimal calcBaseFee() {
		return calcCommonFee().add(Transaction.FEE_PER_BYTE.multiply(new BigDecimal(1000)));
	}
}
