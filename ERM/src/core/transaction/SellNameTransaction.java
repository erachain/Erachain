package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Crypto;
import core.naming.NameSale;
import database.ItemAssetBalanceMap;
import database.DBSet;

public class SellNameTransaction extends Transaction 
{
	private static final byte TYPE_ID = (byte)SELL_NAME_TRANSACTION;
	private static final String NAME_ID = "OLD: Sale Name";
	private static final int BASE_LENGTH = TransactionAmount.BASE_LENGTH;

	private PublicKeyAccount creator;
	private NameSale nameSale;
	
	public SellNameTransaction(byte[] typeBytes, PublicKeyAccount creator, NameSale nameSale, byte feePow, long timestamp, byte[] reference) 
	{
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);

		this.creator = creator;
		this.nameSale = nameSale;
	}
	public SellNameTransaction(byte[] typeBytes, PublicKeyAccount creator, NameSale nameSale, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, nameSale, feePow, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}
	public SellNameTransaction(PublicKeyAccount creator, NameSale nameSale, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, nameSale, feePow, timestamp, reference, signature);
	}
	public SellNameTransaction(PublicKeyAccount creator, NameSale nameSale, byte feePow, long timestamp, byte[] reference) 
	{
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, nameSale, feePow, timestamp, reference);
	}
	
	//GETTERS/SETTERS	

	//public static String getName() { return "OLD: Sell Name"; }

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
		

		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;

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
		
		return new SellNameTransaction(typeBytes, creator, nameSale, feePow, timestamp, reference, signatureBytes);
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
	public byte[] toBytes(boolean withSign, byte[] releaserReference) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		data = Bytes.concat(data, this.typeBytes);
		
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
	public int getDataLength(boolean asPack) 
	{
		return BASE_LENGTH + this.nameSale.getDataLength();
	}
	
	//VALIDATE

	@Override
	public int isValid(DBSet db, byte[] releaserReference) 
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
				
		//CHECK IF SENDER HAS ENOUGH FEE BALANCE
		if(this.creator.getConfirmedBalance(FEE_KEY, db).compareTo(this.fee) == -1)
		{
			return NOT_ENOUGH_FEE;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
				
		return VALIDATE_OK;
	}
	
	//PROCESS/ORPHAN

	//@Override
	public void process(DBSet db, boolean asPack)
	{
		//UPDATE CREATOR
		super.process(db, asPack);
										
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.signature, db);
				
		//INSERT INTO DATABASE
		db.getNameExchangeMap().add(this.nameSale);
	}

	//@Override
	public void orphan(DBSet db, boolean asPack) 
	{
		//UPDATE CREATOR
		super.orphan(db, asPack);
										
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.reference, db);
		
		//DELETE FORM DATABASE
		db.getNameExchangeMap().delete(this.nameSale);
		
	}

	@Override
	public HashSet<Account> getInvolvedAccounts()
	{
		HashSet<Account> accounts = new HashSet<Account>();
		accounts.add(this.creator);
		return accounts;
	}

	@Override
	public HashSet<Account> getRecipientAccounts() {
		return new HashSet<Account>();
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
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		
		return BigDecimal.ZERO;
	}

	//@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		return subAssetAmount(null, this.creator.getAddress(), FEE_KEY, this.fee);
	}

	public int calcBaseFee() {
		return calcCommonFee();
	}
}
