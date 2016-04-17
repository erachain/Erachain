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
//import database.BalanceMap;
import database.DBSet;

public abstract class TransactionAmount extends Transaction {

	protected static final int AMOUNT_LENGTH = 8;
	protected static final int RECIPIENT_LENGTH = Account.ADDRESS_LENGTH;
	protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + RECIPIENT_LENGTH + KEY_LENGTH + AMOUNT_LENGTH;
	protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + RECIPIENT_LENGTH + KEY_LENGTH + AMOUNT_LENGTH;

	protected Account recipient;
	protected BigDecimal amount;
	protected long key;

	// need for calculate fee
	protected TransactionAmount(byte[] typeBytes, String name, PublicKeyAccount creator, byte feePow, Account recipient, BigDecimal amount, long key, long timestamp, byte[] reference, byte[] signature)
	{
		super(typeBytes, name, creator, feePow, timestamp, reference, signature);
		this.recipient = recipient;
		this.amount = amount;
		this.key = key;
	}

	// need for calculate fee by feePow into GUI
	protected TransactionAmount(byte[] typeBytes, String name, PublicKeyAccount creator, byte feePow, Account recipient, BigDecimal amount, long key, long timestamp, byte[] reference)
	{
		super(typeBytes, name, creator, feePow, timestamp, reference);
		this.recipient = recipient;
		this.amount = amount;
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
	public BigDecimal getAmount() {
		return this.amount;
	}
	public String getStr()
	{
		return "transAmount";
	}

	@Override
	public BigDecimal viewAmount() {
		return this.amount;
	}
	
	@Override
	public BigDecimal viewAmount(Account account) {
		BigDecimal amount = BigDecimal.ZERO.setScale(8);
		String address = account.getAddress();
		
		//IF SENDER
		if(address.equals(this.creator.getAddress()))
		{
			amount = amount.subtract(this.amount);
			/*
			if(this.key == FEE_KEY)
			{
				amount = amount.subtract(this.fee);
			}
			*/
		} else if(address.equals(this.recipient.getAddress()))
		{
			//IF RECIPIENT
			amount = amount.add(this.amount);
		}

		return amount;
	}
	
	//PARSE/CONVERT
	//@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference)
	{
		
		byte[] data = super.toBytes(withSign, releaserReference);
				
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

	@SuppressWarnings("unchecked")
	protected JSONObject getJsonBase()
	{
		JSONObject transaction = super.getJsonBase();
		
		transaction.put("recipient", this.recipient.getAddress());
		transaction.put("asset", this.key);
		transaction.put("amount", this.amount.toPlainString());
		
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

	@Override // - fee + balance - calculate here
	public int isValid(DBSet db, byte[] releaserReference) {

		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.recipient.getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(releaserReference==null ? this.creator.getLastReference(db) : releaserReference, this.reference))
		{
			return INVALID_REFERENCE;
		}

		//CHECK IF AMOUNT IS POSITIVE
		if(this.amount.compareTo(BigDecimal.ZERO) <= 0)
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

		//CHECK IF CREATOR HAS ENOUGH ASSET BALANCE
		if(this.creator.getConfirmedBalance(this.key, fork).compareTo(this.amount) == -1)
		{
			return NO_BALANCE;
		}
		*/

		if (this.key != OIL_KEY) {
			// CHECK FEE
			if(this.creator.getConfirmedBalance(OIL_KEY, db).compareTo(this.fee) == -1)
			{
				return NOT_ENOUGH_FEE;
			}
			//CHECK IF CREATOR HAS ENOUGH ASSET BALANCE
			if(this.creator.getConfirmedBalance(this.key, db).compareTo(this.amount) == -1)
			{
				return NO_BALANCE;
			}
		} else {
			if(this.creator.getConfirmedBalance(OIL_KEY, db)
					.compareTo( this.amount.add(this.fee) ) == -1)
			{
				return NO_BALANCE;
			}
		}

		return VALIDATE_OK;
	}		

	public void process(DBSet db, boolean asPack) {

		//UPDATE SENDER
		super.process(db, asPack);

		this.creator.setConfirmedBalance(this.key, this.creator.getConfirmedBalance(this.key, db).subtract(this.amount), db);
						
		//UPDATE RECIPIENT
		this.recipient.setConfirmedBalance(this.key, this.recipient.getConfirmedBalance(this.key, db).add(this.amount), db);
		
		if (!asPack) {

			//UPDATE REFERENCE OF RECIPIENT - for first accept OIL need
			if(this.key == OIL_KEY)
			{
				if(Arrays.equals(this.recipient.getLastReference(db), new byte[0]))
				{
					this.recipient.setLastReference(this.signature, db);
				}
			}
		}

	}

	public void orphan(DBSet db, boolean asPack) {
		//UPDATE SENDER
		super.orphan(db, asPack);
		
		this.creator.setConfirmedBalance(this.key, this.creator.getConfirmedBalance(this.key, db).add(this.amount), db);
						
		//UPDATE RECIPIENT
		this.recipient.setConfirmedBalance(this.key, this.recipient.getConfirmedBalance(this.key, db).subtract(this.amount), db);
		
		if (!asPack) {
			
			//UPDATE REFERENCE OF RECIPIENT
			if(this.key == OIL_KEY)
			{
				if(Arrays.equals(this.recipient.getLastReference(db), this.signature))
				{
					this.recipient.removeReference(db);
				}	
			}
		}
	}

	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), OIL_KEY, this.fee);
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), this.key, this.amount);
		assetAmount = addAssetAmount(assetAmount, this.recipient.getAddress(), this.key, this.amount);
		
		return assetAmount;
	}

	//public abstract Map<String, Map<Long, BigDecimal>> getAssetAmount();
	
	public int calcBaseFee() {
		return calcCommonFee();
	}

}
