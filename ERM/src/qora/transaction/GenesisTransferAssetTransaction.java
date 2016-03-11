package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ntp.NTP;

import org.json.simple.JSONObject;

import qora.account.Account;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.BalanceMap;
import database.DBSet;

public class GenesisTransferAssetTransaction extends Transaction {

	
	private static final int BASE_LENGTH = TIMESTAMP_LENGTH + CREATOR_LENGTH + RECIPIENT_LENGTH + KEY_LENGTH + AMOUNT_LENGTH;

	private Account recipient;
	private BigDecimal amount;
	private long key;
	
	public GenesisTransferAssetTransaction(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, long timestamp) 
	{
		super(GENESIS_TRANSFER_ASSET_TRANSACTION, creator, timestamp, null);		
		this.recipient = recipient;
		this.amount = amount;
		this.key = key;
		this.signature = this.getSignature(); //  setLastReference not change in PROCCESS
	}
	
	//GETTERS/SETTERS
	@Override
	public byte[] getSignature() {
		
		//return generateSignature1(this.recipient, this.amount, this.timestamp);
		byte[] data = this.toBytes( false );

		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);
				
		return digest;

	}

	public Account getRecipient()
	{
		return this.recipient;
	}
	
	public BigDecimal getAmount() 
	{
		return this.amount;
	}
	
	public long getKey()
	{
		return this.key;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/RECIPIENT/AMOUNT/ASSET
		transaction.put("creator", this.creator.getAddress());
		transaction.put("recipient", this.recipient.getAddress());
		transaction.put("asset", this.key);
		transaction.put("amount", this.amount.toPlainString());
				
		return transaction;	
	}

	//PARSE/CONVERT
	
	public static Transaction Parse(byte[] data) throws Exception{
		
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
		
		/*
		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;
		*/
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;
		
		//READ RECIPIENT
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		Account recipient = new Account(Base58.encode(recipientBytes));
		position += RECIPIENT_LENGTH;
		
		//READ KEY
		byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
		long key = Longs.fromByteArray(keyBytes);	
		position += KEY_LENGTH;
		
		//READ AMOUNT
		byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		position += AMOUNT_LENGTH;
				
		return new GenesisTransferAssetTransaction(creator, recipient, key, amount, timestamp);	
	}	
	
	@Override
	public byte[] toBytes(boolean withSign)
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(GENESIS_TRANSFER_ASSET_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		/*
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		*/
		
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());
		
		//WRITE RECIPIENT
		data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));
		
		//WRITE KEY
		byte[] keyBytes = Longs.toByteArray(this.key);
		keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
		data = Bytes.concat(data, keyBytes);
		
		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
				
		return data;
	}

	@Override
	public int getDataLength() 
	{
		return TYPE_LENGTH + BASE_LENGTH;
	}


	//VALIDATE
	public boolean isSignatureValid()
	{
		
		byte[] digest = this.getSignature();
		return Arrays.equals(digest, this.signature);
	}

	@Override
	public int isValid(DBSet db) 
	{
		
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.recipient.getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//REMOVE FEE
		DBSet fork = db.fork();

		//CHECK IF CREATOR HAS ENOUGH ASSET BALANCE
		if(this.creator.getConfirmedBalance(this.key, fork).compareTo(this.amount) == -1)
		{
			return NO_BALANCE;
		}
				
		//CHECK IF AMOUNT IS DIVISIBLE
		if(!db.getAssetMap().get(this.key).isDivisible())
		{
			//CHECK IF AMOUNT DOES NOT HAVE ANY DECIMALS
			if(this.getAmount().stripTrailingZeros().scale() > 0)
			{
				//AMOUNT HAS DECIMALS
				return INVALID_AMOUNT;
			}
		}
		
		/*
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
		*/
		
		//CHECK IF AMOUNT IS POSITIVE
		if(this.amount.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_AMOUNT;
		}
				
		return VALIDATE_OK;
	}

	//PROCESS/ORPHAN
	
	@Override
	public void process(DBSet db) 
	{
		//UPDATE CREATOR
		this.creator.setConfirmedBalance(this.key, this.creator.getConfirmedBalance(this.key, db).subtract(this.amount), db);
						
		//UPDATE RECIPIENT
		this.recipient.setConfirmedBalance(this.key, this.recipient.getConfirmedBalance(this.key, db).add(this.amount), db);
		
		/* icreator - not need change references for genesis block
		*/
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.signature, db);
		
		//UPDATE REFERENCE OF RECIPIENT
		if(this.key == BalanceMap.QORA_KEY)
		{
			if(Arrays.equals(this.recipient.getLastReference(db), new byte[0]))
			{
				this.recipient.setLastReference(this.signature, db);
			}
		}
	}

	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE CREATOR
		this.creator.setConfirmedBalance(this.key, this.creator.getConfirmedBalance(this.key, db).add(this.amount), db);
						
		//UPDATE RECIPIENT
		this.recipient.setConfirmedBalance(this.key, this.recipient.getConfirmedBalance(this.key, db).subtract(this.amount), db);
		
		/* icreator - not need change references for genesis block
		*/
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.reference, db);
		
		//UPDATE REFERENCE OF RECIPIENT
		if(this.key == BalanceMap.QORA_KEY)
		{
			if(Arrays.equals(this.recipient.getLastReference(db), this.signature))
			{
				this.recipient.removeReference(db);
			}	
		}
	}

	//REST
	
	@Override
	public List<Account> getInvolvedAccounts()
	{
		return Arrays.asList(this.creator, this.recipient);
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(creator.getAddress()) || address.equals(recipient.getAddress()))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public BigDecimal getAmount(Account account) 
	{
		BigDecimal amount = BigDecimal.ZERO.setScale(8);
		String address = account.getAddress();
		
		//IF CREATOR
		if(address.equals(this.creator.getAddress()))
		{
			amount = amount.subtract(this.fee);
		}

		//IF QORA ASSET
		if(this.key == BalanceMap.QORA_KEY)
		{
			//IF CREATOR
			if(address.equals(this.creator.getAddress()))
			{
				amount = amount.subtract(this.amount);
			}
			
			//IF RECIPIENT
			if(address.equals(this.recipient.getAddress()))
			{
				amount = amount.add(this.amount);
			}
		}
		
		return amount;
	}

	@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), BalanceMap.QORA_KEY, this.fee);
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), this.key, this.amount);
		assetAmount = addAssetAmount(assetAmount, this.recipient.getAddress(), this.key, this.amount);
		
		return assetAmount;
	}
}
