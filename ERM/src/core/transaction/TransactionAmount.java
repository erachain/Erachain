package core.transaction;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
//import database.BalanceMap;
import database.DBSet;
import utils.NumberAsString;

public abstract class TransactionAmount extends Transaction {

	protected static final int AMOUNT_LENGTH = 8;
	protected static final int RECIPIENT_LENGTH = Account.ADDRESS_LENGTH;
	protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + RECIPIENT_LENGTH + KEY_LENGTH + AMOUNT_LENGTH;
	protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + RECIPIENT_LENGTH + KEY_LENGTH + AMOUNT_LENGTH;

	protected Account recipient;
	protected BigDecimal amount;
	protected long key = Transaction.FEE_KEY;

	// need for calculate fee
	protected TransactionAmount(byte[] typeBytes, String name, PublicKeyAccount creator, byte feePow, Account recipient, BigDecimal amount, long key, long timestamp, Long reference, byte[] signature)
	{
		super(typeBytes, name, creator, feePow, timestamp, reference, signature);
		this.recipient = recipient;

		if (amount == null || amount.equals(BigDecimal.ZERO)) {
			// set version to 1
			typeBytes[1] = (byte)(typeBytes[1] | (byte)-128);
		} else {
			this.amount = amount;
		}
		
		this.key = key;
	}

	// need for calculate fee by feePow into GUI
	protected TransactionAmount(byte[] typeBytes, String name, PublicKeyAccount creator, byte feePow, Account recipient, BigDecimal amount, long key, long timestamp, Long reference)
	{
		super(typeBytes, name, creator, feePow, timestamp, reference);
		this.recipient = recipient;

		if (amount == null || amount.equals(BigDecimal.ZERO)) {
			// set version to 1
			typeBytes[1] = (byte)(typeBytes[1] | (byte)-128);
		} else {
			this.amount = amount;
		}

		this.key = key;
	}

	//GETTERS/SETTERS	

	//public static String getName() { return "unknown subclass Amount"; }

	public Account getRecipient()
	{
		return this.recipient;
	}

	public long getKey()
	{
		return this.key;
	}
	public long getAssetKey()
	{
		return this.key;
	}

	@Override
	public BigDecimal getAmount() {
		//return this.amount == null? BigDecimal.ZERO: this.amount;
		return this.amount;
	}
	public String getStr()
	{
		return "transAmount";
	}

	// VIEW
	@Override
	public String viewRecipient() {
		return recipient.asPerson();
	}
	
	@Override
	public BigDecimal getAmount(String address) {
		BigDecimal amount = BigDecimal.ZERO.setScale(8);
		
		if (this.amount != null)
		{

			if(address.equals(this.creator.getAddress()))
			{
				//IF SENDER
				amount = amount.subtract(this.amount);
			} else if(address.equals(this.recipient.getAddress()))
			{
				//IF RECIPIENT
				amount = amount.add(this.amount);
			}
		}

		return amount;
	}
	@Override
	public BigDecimal getAmount(Account account) {
		String address = account.getAddress();
		return getAmount(address);
	}
	
	@Override
	public String viewAmount(Account account) {
		String address = account.getAddress();
		return NumberAsString.getInstance().numberAsString(getAmount(address));
	}
	@Override
	public String viewAmount(String address) {
		return NumberAsString.getInstance().numberAsString(getAmount(address));
	}

	
	//PARSE/CONVERT
	//@Override
	public byte[] toBytes(boolean withSign, Long releaserReference)
	{
		
		byte[] data = super.toBytes(withSign, releaserReference);
				
		//WRITE RECIPIENT
		data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));
		
		if ( this.amount != null ) {
			//WRITE KEY
			byte[] keyBytes = Longs.toByteArray(this.key);
			keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
			data = Bytes.concat(data, keyBytes);
			
			//WRITE AMOUNT
			byte[] amountBytes = this.amount.unscaledValue().toByteArray();
			byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
			amountBytes = Bytes.concat(fill, amountBytes);
			data = Bytes.concat(data, amountBytes);
		}
				
		return data;
	}

	@SuppressWarnings("unchecked")
	protected JSONObject getJsonBase()
	{
		JSONObject transaction = super.getJsonBase();
		
		transaction.put("recipient", this.recipient.getAddress());
		if (this.amount != null) {
			transaction.put("asset", this.key);
			transaction.put("amount", this.amount.toPlainString());
		}
		
		return transaction;
	}
	
	@Override
	public HashSet<Account> getInvolvedAccounts()
	{
		HashSet<Account> accounts = new HashSet<Account>();
		accounts.add(this.creator);
		accounts.addAll(this.getRecipientAccounts());
		return accounts;
	}

	@Override
	public HashSet<Account> getRecipientAccounts() {
		HashSet<Account> accounts = new HashSet<Account>();
		accounts.add(this.recipient);
		return accounts;
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

	public int getDataLength(boolean asPack) {
		// IF VERSION 1 (amount = null)
		return (asPack?BASE_LENGTH_AS_PACK:BASE_LENGTH) 
				- (this.typeBytes[1]<0?(KEY_LENGTH + AMOUNT_LENGTH):0);
	}

	@Override // - fee + balance - calculate here
	public int isValid(DBSet db, Long releaserReference) {

		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.recipient.getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//CHECK IF REFERENCE IS OK
		Long reference = releaserReference==null ? this.creator.getLastReference(db) : releaserReference;
		if (reference.compareTo(this.reference) != 0)
			return INVALID_REFERENCE;
		if (reference.compareTo(this.timestamp) >= 0)
			return INVALID_TIMESTAMP;

		//CHECK IF AMOUNT IS POSITIVE
		if(this.amount != null && this.amount.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_AMOUNT;
		}

		/* very SLOW - db.fork() !!
		//REMOVE FEE
		DBSet fork = db.fork();
		calcFee();
		this.creator.setConfirmedBalance(FEE_KEY, this.creator.getConfirmedBalance(FEE_KEY, fork).subtract(this.fee), fork);
		
		//CHECK IF CREATOR HAS ENOUGH FEE BALANCE
		if(this.creator.getConfirmedBalance(FEE_KEY, fork).compareTo(BigDecimal.ZERO) == -1)
		{
			return NOT_ENOUGH_FEE;
		}
		

		//CHECK IF CREATOR HAS ENOUGH ASSET BALANCE
		if(this.creator.getConfirmedBalance(this.key, fork).compareTo(this.amount) == -1)
		{
			return NO_BALANCE;
		}
		*/

		//CHECK IF AMOUNT IS DIVISIBLE
		AssetCls asset = (AssetCls)db.getItemAssetMap().get(this.key);
		if (asset == null) {
			return ASSET_DOES_NOT_EXIST;
		}
		
		if(!asset.isDivisible())
		{
			//CHECK IF AMOUNT DOES NOT HAVE ANY DECIMALS
			if(this.amount.stripTrailingZeros().scale() > 0)
			{
				//AMOUNT HAS DECIMALS
				return AMOUNT_DIVISIBLE;
			}
		}
		

		if (this.key != FEE_KEY || this.amount == null) {
			// CHECK FEE
			if(this.creator.getConfirmedBalance(FEE_KEY, db).compareTo(this.fee) == -1)
			{
				return NOT_ENOUGH_FEE;
			}

			// if asset is unlimited and me is creator of this asset 
			boolean unLimited = asset.getQuantity().equals(0l)
					&& asset.getCreator().getAddress().equals(this.creator.getAddress());

			//CHECK IF CREATOR HAS ENOUGH ASSET BALANCE
			if(!unLimited && this.amount != null && this.creator.getConfirmedBalance(this.key, db).compareTo(this.amount) == -1)
			{
				return NO_BALANCE;
			}
		} else {
			if(this.creator.getConfirmedBalance(FEE_KEY, db)
					.compareTo( this.amount.add(this.fee) ) == -1)
			{
				return NO_BALANCE;
			}
		}

		return VALIDATE_OK;
	}		

	public void process(DBSet db, boolean asPack) {

		super.process(db, asPack);
						
		if (this.amount != null) {
			//UPDATE SENDER
			this.creator.setConfirmedBalance(this.key, this.creator.getConfirmedBalance(this.key, db).subtract(this.amount), db);
			//UPDATE RECIPIENT
			this.recipient.setConfirmedBalance(this.key, this.recipient.getConfirmedBalance(this.key, db).add(this.amount), db);
			
			if (!asPack) {
	
				//UPDATE REFERENCE OF RECIPIENT - for first accept FEE need
				if(this.key == FEE_KEY)
				{
					if(this.recipient.getLastReference(db) == null)
					{
						this.recipient.setLastReference(this.timestamp, db);
					}
				}
			}
		}
	}

	public void orphan(DBSet db, boolean asPack) {

		super.orphan(db, asPack);
		
		if (this.amount != null) {
			//UPDATE SENDER
			this.creator.setConfirmedBalance(this.key, this.creator.getConfirmedBalance(this.key, db).add(this.amount), db);
			
			//UPDATE RECIPIENT
			this.recipient.setConfirmedBalance(this.key, this.recipient.getConfirmedBalance(this.key, db).subtract(this.amount), db);
			
			if (!asPack) {
				
				//UPDATE REFERENCE OF RECIPIENT
				if(this.key == FEE_KEY)
				{
					if( this.recipient.getLastReference(db).equals(this.timestamp) )
					{
						this.recipient.removeReference(db);
					}	
				}
			}
		}
	}

	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
		
		if (this.amount != null) {

			assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);
			
			assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), this.key, this.amount);
			assetAmount = addAssetAmount(assetAmount, this.recipient.getAddress(), this.key, this.amount);
		}
		
		return assetAmount;
	}

	//public abstract Map<String, Map<Long, BigDecimal>> getAssetAmount();
	
}
