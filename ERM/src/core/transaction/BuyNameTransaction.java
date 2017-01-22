package core.transaction;

import java.math.BigDecimal;
//import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.Base58;
import core.naming.Name;
import core.naming.NameSale;
import database.ItemAssetBalanceMap;
import database.DBSet;

public class BuyNameTransaction extends Transaction
{
	private static final byte TYPE_ID = (byte)BUY_NAME_TRANSACTION;
	private static final String NAME_ID = "OLD: Buy Name";
	private static final int SELLER_LENGTH = TransactionAmount.RECIPIENT_LENGTH;
	private static final int BASE_LENGTH = Transaction.BASE_LENGTH
			 + SELLER_LENGTH;
	
	private NameSale nameSale;
	private Account seller;
	
	public BuyNameTransaction(byte[] typeBytes, PublicKeyAccount creator, NameSale nameSale, Account seller, byte feePow, long timestamp, Long reference) {
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);
		this.nameSale = nameSale;
		this.seller = seller;
	}
	public BuyNameTransaction(byte[] typeBytes, PublicKeyAccount creator, NameSale nameSale, Account seller, byte feePow, long timestamp, Long reference, byte[] signature) {
		this(typeBytes, creator, nameSale, seller, feePow, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}
	public BuyNameTransaction(PublicKeyAccount creator, NameSale nameSale, Account seller, byte feePow, long timestamp, Long reference, byte[] signature) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, nameSale, seller, feePow, timestamp, reference, signature);
	}
	public BuyNameTransaction(PublicKeyAccount creator, NameSale nameSale, Account seller, byte feePow, long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, nameSale, seller, feePow, timestamp, reference);
	}
	
	//GETTERS/SETTERS
	//public static String getName() { return "OLD: Buy Name";	}

	/*
	public PublicKeyAccount getBuyer()
	{
		return this.creator;
	}
	*/
	
	public NameSale getNameSale()
	{
		return this.nameSale;
	}
	
	public Account getSeller()
	{
		return this.seller;
	}
	
	public boolean hasPublicText() {
		return false;
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
		byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		long reference = Longs.fromByteArray(referenceBytes);	
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
		
		return new BuyNameTransaction(typeBytes, creator, nameSale, seller, feePow, timestamp, reference, signatureBytes);
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
	public byte[] toBytes( boolean withSign, Long releaserReference) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		//byte[] typeBytes = Ints.toByteArray(TYPE_ID);
		//typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, this.typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		byte[] referenceBytes = Longs.toByteArray(this.reference);
		referenceBytes = Bytes.ensureCapacity(referenceBytes, REFERENCE_LENGTH, 0);
		data = Bytes.concat(data, referenceBytes);
		
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
	public int getDataLength(boolean asPack) 
	{
		return BASE_LENGTH + this.nameSale.getDataLength();
	}
	
	//VALIDATE
	//@Override
	public int isValid(DBSet db, Long releaserReference) 
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
			return INVALID_MAKER_ADDRESS;
		}
		
		//CHECK IF CREATOR HAS ENOUGH MONEY
		if(this.creator.getBalance(db, Transaction.FEE_KEY).a.compareTo(this.nameSale.getAmount()) == -1)
		{
			return NO_BALANCE;
		}
				
		//CHECK IF PRICE MATCHES
		NameSale nameSale = db.getNameExchangeMap().getNameSale(this.nameSale.getKey());
		if(!this.nameSale.getAmount().equals(nameSale.getAmount()))
		{
			return INVALID_AMOUNT;
		}
				
		return super.isValid(db, releaserReference);
	}

	//@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.nameSale.getAmount());
		
		assetAmount = addAssetAmount(assetAmount, this.getSeller().getAddress(), FEE_KEY, this.nameSale.getAmount());
		
		return assetAmount;
	}

	//PROCESS/ORPHAN

	//@Override
	public void process(DBSet db, Block block, boolean asPack) 
	{
		//UPDATE CREATOR
		super.process(db, block, asPack);
		//this.creator.setBalance(Transaction.FEE_KEY, this.creator.getBalance(db, Transaction.FEE_KEY).subtract(this.nameSale.getAmount()), db);
		this.creator.changeBalance(db, true, Transaction.FEE_KEY, this.nameSale.getAmount());
		
		//UPDATE SELLER
		Name name = this.nameSale.getName(db);
		//this.seller.setBalance(Transaction.FEE_KEY, this.seller.getBalance(db, Transaction.FEE_KEY).add(this.nameSale.getAmount()), db);
		this.seller.changeBalance(db, false, Transaction.FEE_KEY, this.nameSale.getAmount());
						
		//UPDATE NAME OWNER (NEW OBJECT FOR PREVENTING CACHE ERRORS)
		name = new Name(this.creator, name.getName(), name.getValue());
		db.getNameMap().add(name);
		
		//DELETE NAME SALE FROM DATABASE
		db.getNameExchangeMap().delete(this.nameSale.getKey());
		
	}

	//@Override
	public void orphan(DBSet db, boolean asPack) 
	{
		//UPDATE CREATOR
		super.orphan(db, asPack);
		//this.creator.setBalance(Transaction.FEE_KEY, this.creator.getBalance(db, Transaction.FEE_KEY).add(this.nameSale.getAmount()), db);
		this.creator.changeBalance(db, false, Transaction.FEE_KEY, this.nameSale.getAmount());
		
		//UPDATE SELLER
		//this.seller.setBalance(Transaction.FEE_KEY, this.seller.getBalance(db, Transaction.FEE_KEY).subtract(this.nameSale.getAmount()), db);
		this.seller.changeBalance(db, true, Transaction.FEE_KEY, this.nameSale.getAmount());

		//UPDATE NAME OWNER (NEW OBJECT FOR PREVENTING CACHE ERRORS)
		Name name = this.nameSale.getName(db);
		name = new Name(this.seller, name.getName(), name.getValue());
		db.getNameMap().add(name);
		
		//RESTORE NAMESALE
		db.getNameExchangeMap().add(this.nameSale);
	}

	@Override
	public HashSet<Account> getInvolvedAccounts()
	{
		HashSet<Account> accounts = new HashSet<>();
		
		accounts.add(this.creator);
		accounts.addAll(this.getRecipientAccounts());
		
		return accounts;
	}
	
	@Override
	public HashSet<Account> getRecipientAccounts()
	{
		HashSet<Account> accounts = new HashSet<>();
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
	public BigDecimal getAmount(Account account) 
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
