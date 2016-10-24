package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import database.AddressForging;
//import database.BalanceMap;
import database.DBSet;
import lang.Lang;
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

	public String viewSendType() {
		int amo_sign = this.amount.compareTo(BigDecimal.ZERO);
		
		if (this.key < 0) {
			return "DEBT";
		} else {
			if (amo_sign < 0) {
				return "HOLD";
			} else { 
				return "PAY";
			}
		}
		// return "SPEND";
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
			byte[] amountBytes = Longs.toByteArray(this.amount.unscaledValue().longValue());
			amountBytes = Bytes.ensureCapacity(amountBytes, AMOUNT_LENGTH, 0);
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

		/*
		//CHECK IF AMOUNT IS POSITIVE
		if(this.amount != null && this.amount.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_AMOUNT;
		}
		*/

		//CHECK IF AMOUNT IS DIVISIBLE
		long absKey = this.key;
		if (absKey < 0)
			absKey = -absKey;
		
		AssetCls asset = (AssetCls)db.getItemAssetMap().get(absKey);
		if (asset == null) {
			return ASSET_DOES_NOT_EXIST;
		}
				
		if (this.amount != null) {
			int amount_sign = this.amount.signum();
			if (amount_sign != 0) {

				if(!asset.isDivisible())
				{
					//CHECK IF AMOUNT DOES NOT HAVE ANY DECIMALS
					if(this.amount.stripTrailingZeros().scale() > 0)
					{
						//AMOUNT HAS DECIMALS
						return AMOUNT_DIVISIBLE;
					}
				}

				Tuple3<BigDecimal, BigDecimal, BigDecimal> balance = this.creator.getBalance3(absKey, db);
				BigDecimal balanceUSE = balance.a.add(balance.b);

				if (this.key > 0) {
					if (amount_sign < 0) {
						// HOLD transfer
						// here amount is negative
						if (!asset.isMovable()) {
							return NOT_MOVABLE_ASSET;						
						}
						
						if (amount.abs().compareTo(balance.c) > 0) {
							// If the holder does not have enough hold balance
							return NO_HOLD_BALANCE;
						}
						if(this.creator.getBalance(FEE_KEY, db).compareTo( this.fee ) < 0)
						{
							return NOT_ENOUGH_FEE;
						}
					} else {
						// common SEND
						if (absKey != FEE_KEY) {
							// CHECK FEE
							if(this.creator.getBalance(FEE_KEY, db).compareTo(this.fee) < 0)
							{
								return NOT_ENOUGH_FEE;
							}
				
							// if asset is unlimited and me is creator of this asset 
							boolean unLimited = 
									absKey > AssetCls.DEAL_KEY // not genesis assets!
									&& asset.getQuantity().equals(0l)
									&& asset.getCreator().getAddress().equals(this.creator.getAddress());
				
							//CHECK IF CREATOR HAS ENOUGH ASSET BALANCE
							if(!unLimited && balanceUSE.compareTo(this.amount) < 0)
							{
								return NO_BALANCE;
							}
						} else {
							if(balanceUSE.compareTo( this.amount.add(this.fee) ) < 0)
							{
								return NO_BALANCE;
							}
						}
					}
				} else {
					// DEBTs
					if (amount_sign < 0) {
						// confiscate DEBT
						Tuple3<BigDecimal, BigDecimal, BigDecimal> debtorBalance = this.recipient.getBalance3(absKey, db);
						//BigDecimal balanceUSE = balance.a.add(balance.b);
						if (this.amount.abs().compareTo(debtorBalance.b) > 0) {
							// here amount is negative
							return NO_DEBT_BALANCE;
						}
					} else {
						// give DEBT
						if (balanceUSE.compareTo(amount) < 0)
						{
							return NO_BALANCE;
						}
					}
					
					// test FEE
					if(this.creator.getBalanceUSE(FEE_KEY, db)
							.compareTo( this.fee ) < 0)
					{
						return NOT_ENOUGH_FEE;
					}
				}
			} else {
				if(this.creator.getBalanceUSE(FEE_KEY, db)
						.compareTo( this.fee ) < 0)
				{
					return NOT_ENOUGH_FEE;
				}
			}
		}

		return VALIDATE_OK;
	}		

	public void process(DBSet db, Block block, boolean asPack) {

		super.process(db, block, asPack);
		
		if (this.amount == null)
			return;

		int amount_sign = this.amount.compareTo(BigDecimal.ZERO);
		if (amount_sign == 0)
			return;
						
		long absKey = getAbsKey();

		Tuple3<BigDecimal, BigDecimal, BigDecimal> creatorBalance = this.creator.getBalance3(absKey, db);
		Tuple3<BigDecimal, BigDecimal, BigDecimal> recipientBalance = this.recipient.getBalance3(absKey, db);

		if (this.key > 0) {
			if (amount_sign > 0) {
				//UPDATE SENDER
				this.creator.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
						creatorBalance.a.subtract(this.amount), creatorBalance.b, creatorBalance.c),
						db);
				//UPDATE RECIPIENT
				this.recipient.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
						recipientBalance.a.add(this.amount), recipientBalance.b, recipientBalance.c),
						db);
				
				AssetCls asset = (AssetCls)db.getItemAssetMap().get(absKey);
				if (asset.isMovable()) {
					// MOVABLE
					//UPDATE SENDER
					this.creator.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
							creatorBalance.a, creatorBalance.b, creatorBalance.c.add(this.amount)),
							db);
					//UPDATE RECIPIENT
					this.recipient.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
							recipientBalance.a, recipientBalance.b, recipientBalance.c.subtract(this.amount)),
							db);
				}
			} else {
				// HOLD transfer
				// here amount is negative
				//UPDATE SENDER
				this.creator.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
						creatorBalance.a, creatorBalance.b, creatorBalance.c.add(this.amount)),
						db);
				//UPDATE RECIPIENT
				this.recipient.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
						recipientBalance.a, recipientBalance.b, recipientBalance.c.subtract(this.amount)),
						db);
			}
		} else {
			/////// DEBTs
			// give DEBT
			// or
			// confiscate DEBT
			this.creator.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
					creatorBalance.a, creatorBalance.b.subtract(this.amount), creatorBalance.c),
					db);
			//UPDATE RECIPIENT
			this.recipient.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
					recipientBalance.a, recipientBalance.b.add(this.amount), recipientBalance.c),
					db);
		}
		
		if (!asPack) {

			//UPDATE REFERENCE OF RECIPIENT - for first accept FEE need
			if(absKey == FEE_KEY)
			{
				if(this.recipient.getLastReference(db) == null)
				{
					this.recipient.setLastReference(this.timestamp, db);
				}
			}
		}

		if (absKey == Transaction.RIGHTS_KEY
				&& this.recipient.getLastForgingData(db) == -1) {
			// update last forging block if it not exist
			// if exist - it not need - incomes will be negate from forging balance

			// it is stil unconfirmed!!!  Block block = this.getParent(db);

			// get height by LAST block in CHAIN + 2 - skip incoming BLOCK 
			int blockHeight = Controller.getInstance().getBlockChain().getHeight(db) + 2;
			this.recipient.setLastForgingData(db, blockHeight);
		}
	}

	public void orphan(DBSet db, boolean asPack) {

		super.orphan(db, asPack);
		
		if (this.amount == null)
			return;

		int amount_sign = this.amount.compareTo(BigDecimal.ZERO);
		if (amount_sign == 0)
			return;
						
		long absKey = getAbsKey();

		Tuple3<BigDecimal, BigDecimal, BigDecimal> creatorBalance = this.creator.getBalance3(absKey, db);
		Tuple3<BigDecimal, BigDecimal, BigDecimal> recipientBalance = this.recipient.getBalance3(absKey, db);

		if (this.key > 0) {
			if (amount_sign > 0) {
				//UPDATE SENDER
				this.creator.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
						creatorBalance.a.add(this.amount), creatorBalance.b, creatorBalance.c),
						db);
				//UPDATE RECIPIENT
				this.recipient.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
						recipientBalance.a.subtract(this.amount), recipientBalance.b, recipientBalance.c),
						db);
				
				AssetCls asset = (AssetCls)db.getItemAssetMap().get(absKey);
				if (asset.isMovable()) {
					// MOVABLE
					//UPDATE SENDER
					this.creator.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
							creatorBalance.a, creatorBalance.b, creatorBalance.c.subtract(this.amount)),
							db);
					//UPDATE RECIPIENT
					this.recipient.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
							recipientBalance.a, recipientBalance.b, recipientBalance.c.add(this.amount)),
							db);
				}
			} else {
				// HOLD transfer
				// here amount is negative
				//UPDATE SENDER
				this.creator.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
						creatorBalance.a, creatorBalance.b, creatorBalance.c.subtract(this.amount)),
						db);
				//UPDATE RECIPIENT
				this.recipient.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
						recipientBalance.a, recipientBalance.b, recipientBalance.c.add(this.amount)),
						db);
			}
		} else {
			/////// DEBTs
			// give DEBT
			// or
			// confiscate DEBT
			this.creator.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
					creatorBalance.a, creatorBalance.b.add(this.amount), creatorBalance.c),
					db);
			//UPDATE RECIPIENT
			this.recipient.setBalance3(absKey, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
					recipientBalance.a, recipientBalance.b.subtract(this.amount), recipientBalance.c),
					db);
		}
			
		if (!asPack) {
			
			//UPDATE REFERENCE OF RECIPIENT
			if(absKey == FEE_KEY)
			{
				if( this.recipient.getLastReference(db).equals(this.timestamp) )
				{
					this.recipient.removeReference(db);
				}	
			}
		}
	
		if (absKey == Transaction.RIGHTS_KEY) {
			
			// Parent BLOCK is still in MAP!
			int blockHeight = Controller.getInstance().getBlockChain().getHeight(db);
			if (this.recipient.getForgingData(db, blockHeight) == -1 ) {
				// if it is first payment ERMO - reset last forging BLOCK
				//this.recipient.delForgingData(db, blockHeight);
				this.recipient.setLastForgingData(db, -1);
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
