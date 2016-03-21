package qora.transaction;

import java.math.BigDecimal;
//import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import qora.account.Account;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
//import qora.crypto.Crypto;
import qora.naming.Name;
import qora.naming.NameSale;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.BalanceMap;
import database.DBSet;

public class BuyNameTransaction extends Transaction
{
	private static final int SELLER_LENGTH = TransactionAmount.RECIPIENT_LENGTH;
	private static final int BASE_LENGTH = 1 + TIMESTAMP_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH + SELLER_LENGTH + SIGNATURE_LENGTH;
	
	private NameSale nameSale;
	private Account seller;
	
	public BuyNameTransaction(PublicKeyAccount creator, NameSale nameSale, Account seller, byte feePow, long timestamp, byte[] reference) {
		super(BUY_NAME_TRANSACTION, creator, feePow, timestamp, reference);
		this.nameSale = nameSale;
		this.seller = seller;
	}
	public BuyNameTransaction(PublicKeyAccount creator, NameSale nameSale, Account seller, byte feePow, long timestamp, byte[] reference, byte[] signature) {
		this(creator, nameSale, seller, feePow, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}
	
	//GETTERS/SETTERS
	
	public PublicKeyAccount getBuyer()
	{
		return this.creator;
	}
	
	public NameSale getNameSale()
	{
		return this.nameSale;
	}
	
	public Account getSeller()
	{
		return this.seller;
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
		
		//READ NAMESALE
		NameSale nameSale = NameSale.Parse(Arrays.copyOfRange(data, position, data.length));
		position += nameSale.getDataLength();
		
		//READ SELLER
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + SELLER_LENGTH);
		Account seller = new Account(Base58.encode(recipientBytes));
		position += SELLER_LENGTH;
		
		//READ FEE POWER
		byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
		byte feePow = feePowBytes[0];
		position += 1;
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new BuyNameTransaction(creator, nameSale, seller, feePow, timestamp, reference, signatureBytes);
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
		transaction.put("seller", this.seller.getAddress());
								
		return transaction;	
	}

	@Override
	public byte[] toBytes( boolean withSign) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(BUY_NAME_TRANSACTION);
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
		
		//WRITE NAME SALE
		data = Bytes.concat(data, this.nameSale.toBytes());
		
		//WRITE SELLER
		data = Bytes.concat(data, Base58.decode(this.seller.getAddress()));
		
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
		Name name = this.nameSale.getName(db);
		if(name == null)
		{
			return NAME_DOES_NOT_EXIST;
		}
				
		//CHECK IF CREATOR IS OWNER
		if(name.getOwner().getAddress().equals(this.creator.getAddress()))
		{
			return BUYER_ALREADY_OWNER;
		}
		
		//CHECK IF NAME FOR SALE ALREADY
		if(!db.getNameExchangeMap().contains(this.nameSale.getKey()))
		{
			return NAME_NOT_FOR_SALE;
		}
		
		//CHECK IF SELLER IS SELLER
		if(!name.getOwner().getAddress().equals(this.seller.getAddress()))
		{
			return INVALID_SELLER;
		}
		
		//CHECK IF CREATOR HAS ENOUGH MONEY
		if(this.creator.getBalance(1, db).compareTo(this.nameSale.getAmount()) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF SENDER HAS ENOUGH FEE BALANCE
		if(this.creator.getConfirmedBalance(FEE_KEY, db).compareTo(this.fee) == -1)
		{
			return NOT_ENOUGH_FEE;
		}
		
		//CHECK IF PRICE MATCHES
		NameSale nameSale = db.getNameExchangeMap().getNameSale(this.nameSale.getKey());
		if(!this.nameSale.getAmount().equals(nameSale.getAmount()))
		{
			return INVALID_AMOUNT;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
		
		return VALIDATE_OK;
	}

	//@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), BalanceMap.QORA_KEY, this.nameSale.getAmount());
		
		assetAmount = addAssetAmount(assetAmount, this.getSeller().getAddress(), BalanceMap.QORA_KEY, this.nameSale.getAmount());
		
		return assetAmount;
	}

	//PROCESS/ORPHAN

	//@Override
	public void process(DBSet db) 
	{
		//UPDATE CREATOR
		super.process(db);
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).subtract(this.nameSale.getAmount()), db);
		
		//UPDATE SELLER
		Name name = this.nameSale.getName(db);
		this.seller.setConfirmedBalance(this.seller.getConfirmedBalance(db).add(this.nameSale.getAmount()), db);
		
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.signature, db);
				
		//UPDATE NAME OWNER (NEW OBJECT FOR PREVENTING CACHE ERRORS)
		name = new Name(this.creator, name.getName(), name.getValue());
		db.getNameMap().add(name);
		
		//DELETE NAME SALE FROM DATABASE
		db.getNameExchangeMap().delete(this.nameSale.getKey());
		
	}

	//@Override
	public void orphan(DBSet db) 
	{
		//UPDATE CREATOR
		super.orphan(db);
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).add(this.nameSale.getAmount()), db);
		
		//UPDATE SELLER
		this.seller.setConfirmedBalance(this.seller.getConfirmedBalance(db).subtract(this.nameSale.getAmount()), db);
												
		//UPDATE REFERENCE OF OWNER
		this.creator.setLastReference(this.reference, db);
				
		//UPDATE NAME OWNER (NEW OBJECT FOR PREVENTING CACHE ERRORS)
		Name name = this.nameSale.getName(db);
		name = new Name(this.seller, name.getName(), name.getValue());
		db.getNameMap().add(name);
		
		//RESTORE NAMESALE
		db.getNameExchangeMap().add(this.nameSale);
	}

	@Override
	public List<Account> getInvolvedAccounts()
	{
		List<Account> accounts = new ArrayList<Account>();
		
		accounts.add(this.creator);
		accounts.add(this.getSeller());
		
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
		
		if(address.equals(this.getSeller().getAddress()))
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
			return BigDecimal.ZERO.setScale(8).subtract(this.nameSale.getAmount());
		}
		
		if(address.equals(this.getSeller().getAddress()))
		{
			return this.nameSale.getAmount();
		}
		
		return BigDecimal.ZERO.setScale(8);
	}
	public int calcBaseFee() {
		return calcCommonFee();
	}
}
