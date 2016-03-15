package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import qora.account.Account;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Crypto;
import qora.naming.NameSale;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.BalanceMap;
import database.DBSet;

public class SellNameTransaction extends Transaction 
{
	private static final int BASE_LENGTH = 1 + TIMESTAMP_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH + SIGNATURE_LENGTH;

	private PublicKeyAccount creator;
	private NameSale nameSale;
	
	public SellNameTransaction(PublicKeyAccount creator, NameSale nameSale, long timestamp, byte[] reference) 
	{
		super(SELL_NAME_TRANSACTION, creator, timestamp, reference);

		this.creator = creator;
		this.nameSale = nameSale;
	}
	public SellNameTransaction(PublicKeyAccount creator, NameSale nameSale, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(creator, nameSale, timestamp, reference);
		this.signature = signature;
		this.feePow = feePow;
		this.calcFee();
	}
	public SellNameTransaction(PublicKeyAccount creator, NameSale nameSale, byte feePow, long timestamp, byte[] reference) 
	{
		this(creator, nameSale, timestamp, reference);
		this.feePow = feePow;
		this.calcFee();
	}
	
	//GETTERS/SETTERS	
	public NameSale getNameSale()
	{
		return this.nameSale;
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
		byte[] registrantBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(registrantBytes);
		position += CREATOR_LENGTH;
		
		//READ NAMESALE
		NameSale nameSale = NameSale.Parse(Arrays.copyOfRange(data, position, data.length));
		position += nameSale.getDataLength();
		
		//READ FEE POWER
		byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
		byte feePow = feePowBytes[0];
		position += 1;
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new SellNameTransaction(creator, nameSale, feePow, timestamp, reference, signatureBytes);
	}	

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
						
		//ADD REGISTRANT/NAME/VALUE
		transaction.put("creator", this.creator.getAddress());
		transaction.put("name", this.nameSale.getKey());
		transaction.put("amount", this.nameSale.getAmount().toPlainString());
						
		return transaction;	
	}

	@Override
	public byte[] toBytes(boolean withSign) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(SELL_NAME_TRANSACTION);
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
		
		//WRITE NAMESALE
		data = Bytes.concat(data, this.nameSale.toBytes());
		
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
		return TYPE_LENGTH + BASE_LENGTH + this.nameSale.getDataLength();
	}
	
	//VALIDATE

	@Override
	public int isValid(DBSet db) 
	{
		//CHECK NAME LENGTH
		int nameLength = this.nameSale.getKey().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > 400 || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK IF NAME EXISTS
		if(!db.getNameMap().contains(this.nameSale.getName(db)))
		{
			return NAME_DOES_NOT_EXIST;
		}
				
		//CHECK CREATOR
		if(!Crypto.getInstance().isValidAddress(this.nameSale.getName(db).getOwner().getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//CHECK IF CREATOR IS CREATOR
		if(!db.getNameMap().get(this.nameSale.getKey()).getOwner().getAddress().equals(this.creator.getAddress()))
		{
			return INVALID_NAME_CREATOR;
		}
		
		//CHECK IF NOT FOR SALE ALREADY
		if(db.getNameExchangeMap().contains(this.nameSale))
		{
			return NAME_ALREADY_FOR_SALE;
		}
		
		//CHECK IF AMOUNT POSITIVE
		if(this.nameSale.getAmount().compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_AMOUNT;
		}
		
		//CHECK IF AMOUNT POSSIBLE
		if(this.nameSale.getAmount().compareTo(BigDecimal.valueOf(10000000000l)) > 0)
		{
			return INVALID_AMOUNT;
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
		process_fee(db);
										
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.signature, db);
				
		//INSERT INTO DATABASE
		db.getNameExchangeMap().add(this.nameSale);
	}

	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE CREATOR
		orphan_fee(db);
										
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.reference, db);
		
		//DELETE FORM DATABASE
		db.getNameExchangeMap().delete(this.nameSale);
		
	}

	@Override
	public List<Account> getInvolvedAccounts()
	{
		List<Account> accounts = new ArrayList<Account>();
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
	public BigDecimal getAmount(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		
		return BigDecimal.ZERO;
	}

	@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		return subAssetAmount(null, this.creator.getAddress(), BalanceMap.QORA_KEY, this.fee);
	}

	public BigDecimal calcBaseFee() {
		return calcCommonFee();
	}
}
