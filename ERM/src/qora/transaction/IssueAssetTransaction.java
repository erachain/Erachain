package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ntp.NTP;

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

public class IssueAssetTransaction extends Transaction 
{
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH;

	private Asset asset;
	
	public IssueAssetTransaction(PublicKeyAccount creator, Asset asset, long timestamp, byte[] reference) 
	{
		super(ISSUE_ASSET_TRANSACTION, creator, timestamp, reference);
		
		this.asset = asset;
	}
	public IssueAssetTransaction(PublicKeyAccount creator, Asset asset, BigDecimal fee, long timestamp, byte[] reference, byte[] signature) 
	{
		this(creator, asset, timestamp, reference);
		
		this.signature = signature;
		this.fee = fee;
	}
	public IssueAssetTransaction(PublicKeyAccount creator, Asset asset, int feePow, long timestamp, byte[] reference) 
	{
		this(creator, asset, timestamp, reference);
		this.calcFee();
	}

	//GETTERS/SETTERS
		
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
		
		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;
		
		//READ ASSET
		Asset asset = Asset.parse(Arrays.copyOfRange(data, position, data.length));
		position += asset.getDataLength();
		
		//READ FEE
		byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
		BigDecimal fee = new BigDecimal(new BigInteger(feeBytes), 8);
		position += FEE_LENGTH;		
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new IssueAssetTransaction(creator, asset, fee, timestamp, reference, signatureBytes);
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
		data = Bytes.concat(data , this.asset.toBytes(true));
		
		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		byte[] fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);

		//SIGNATURE
		if (withSign) data = Bytes.concat(data, this.signature);
		
		return data;
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
		if(this.creator.getBalance(1, db).compareTo(this.fee) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
		
		//CHECK IF FEE IS POSITIVE
		if(this.fee.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_FEE;
		}
		
		return VALIDATE_OK;
	}
	
	//PROCESS/ORPHAN

	@Override
	public void process(DBSet db)
	{
		//UPDATE CREATOR
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).subtract(this.fee), db);
								
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.signature, db);
		
		//INSERT INTO DATABASE
		long key = db.getAssetMap().add(this.asset);
		
		//ADD ASSETS TO OWNER
		this.asset.getCreator().setConfirmedBalance(key, new BigDecimal(this.asset.getQuantity()).setScale(8), db);
		
		//SET ORPHAN DATA
		db.getIssueAssetMap().set(this, key);
	}


	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE CREATOR
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).add(this.fee), db);
										
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
		accounts.add(this.asset.getCreator());
		return accounts;
	}


	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress()) || address.equals(this.asset.getCreator().getAddress()))
		{
			return true;
		}
		
		return false;
	}


	@Override
	public BigDecimal getAmount(Account account) 
	{
		if(account.getAddress().equals(this.creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		
		return BigDecimal.ZERO;
	}
}
