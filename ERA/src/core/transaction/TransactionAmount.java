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
import core.BlockChain;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import datachain.AddressForging;
import datachain.DCSet;
import lang.Lang;
import utils.NumberAsString;

/*

## typeBytes
0 - record type
1 - record version
2 - property 1
3 = property 2

## version 0
// typeBytes[2] = -128 if NO AMOUNT
// typeBytes[3] = -128 if NO DATA

## version 1
if backward - CONFISCATE CREDIT

## version 2

#### PROPERTY 1
typeBytes[2].0 = -128 if NO AMOUNT - check sign
typeBytes[2].1 = 64 if backward (CONFISCATE CREDIT, ...)

#### PROPERTY 2
typeBytes[3].0 = -128 if NO DATA - check sign

*/

public abstract class TransactionAmount extends Transaction {

	protected static final int AMOUNT_LENGTH = 8;
	protected static final int RECIPIENT_LENGTH = Account.ADDRESS_LENGTH;
	protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + RECIPIENT_LENGTH + KEY_LENGTH + AMOUNT_LENGTH;
	protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + RECIPIENT_LENGTH + KEY_LENGTH + AMOUNT_LENGTH;

	protected Account recipient;
	protected BigDecimal amount;
	protected long key = Transaction.FEE_KEY;
	/*
	public static final String NAME_ACTION_TYPE_BACKWARD_PROPERTY = "backward PROPERTY";
	public static final String NAME_ACTION_TYPE_BACKWARD_HOLD = "backward HOLD";
	public static final String NAME_ACTION_TYPE_BACKWARD_CREDIT = "backward CREDIT";
	public static final String NAME_ACTION_TYPE_BACKWARD_SPEND = "backward SPEND";
	*/
	public static final String NAME_ACTION_TYPE_PROPERTY = "PROPERTY";
	public static final String NAME_ACTION_TYPE_HOLD = "HOLD";
	public static final String NAME_CREDIT= "CREDIT";
	public static final String NAME_SPEND = "SPEND";
	
	
	public static final byte BACKWARD_MASK = 64;

	private static final byte[][] VALID_BAL = new byte[][]{
			Base58.decode("5sAJS3HeLQARZJia6Yzh7n18XfDp6msuaw8J5FPA8xZoinW4FtijNru1pcjqGjDqA3aP8HY2MQUxfdvk8GPC5kjh"),
			Base58.decode("3K3QXeohM3V8beSBVKSZauSiREGtDoEqNYWLYHxdCREV7bxqE4v2VfBqSh9492dNG7ZiEcwuhhk6Y5EEt16b6sVe"),
			Base58.decode("5JP71DmsBQAVTQFUHJ1LJXw4qAHHcoBCzXswN9Ez3H5KDzagtqjpWUU2UNofY2JaSC4qAzaC12ER11kbAFWPpukc"),
			Base58.decode("33okYP8EdKkitutgat1PiAnyqJGnnWQHBfV7NyYndk7ZRy6NGogEoQMiuzfwumBTBwZyxchxXj82JaQiQXpFhRcs"),
			};
	private static final Long[] VALID_REF = new Long[]{
			1496474042552L
		};

	private static final byte[][] TRUSTED_FOR_ANONYMOUS_SEND = new byte[][]{
		Base58.decode("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ")
		};

	// need for calculate fee
	protected TransactionAmount(byte[] typeBytes, String name, PublicKeyAccount creator, byte feePow, Account recipient, BigDecimal amount, long key, long timestamp, Long reference, byte[] signature)
	{
		super(typeBytes, name, creator, feePow, timestamp, reference, signature);
		this.recipient = recipient;

		if (amount == null || amount.equals(BigDecimal.ZERO)) {
			// set version to 1
			typeBytes[2] = (byte)(typeBytes[2] | (byte)-128);
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
			typeBytes[2] = (byte)(typeBytes[2] | (byte)-128);
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
		return recipient.getPersonAsString();
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
	
	public int getActionType() {
		int type = core.account.Account.actionType(this.key, this.amount);
		return type * (isBackward()?-1:1);
	}
	
	// BACKWARD AMOUNT
	public boolean isBackward() {
		return typeBytes[1] == 1
			|| typeBytes[1] > 1 && (typeBytes[2] & BACKWARD_MASK) > 0;
	}

	/* ************** VIEW
	 */
	
	@Override
	public String viewTypeName() {
		if (this.amount == null
				|| this.amount.signum() == 0) 
			return "LETTER";

		if (this.isBackward()) {
			return "backward";
		} else {
			return "SEND";			
		}
	}
	@Override
	public String viewSubTypeName() {
		return viewActionType();
	}
	@Override
	public String viewAmount() {
		if (this.amount == null)
			return "";
		
		if (this.amount.signum() < 0) {
			return this.amount.negate().toPlainString();
		} else {
			return this.amount.toPlainString();
		}
	}
	@Override
	public String viewAmount(Account account) {
		if (this.amount == null)
			return "";
		String address = account.getAddress();
		return NumberAsString.getInstance().numberAsString(getAmount(address));
	}
	@Override
	public String viewAmount(String address) {
		if (this.amount == null)
			return "";
		return NumberAsString.getInstance().numberAsString(getAmount(address));
	}

	private String viewActionType() {
		
		if (this.amount == null
				|| this.amount.signum() == 0) 
			return "";
		
		int amo_sign = this.amount.signum();
		
		if (this.key > 0) {
			if (amo_sign > 0) {
				return NAME_ACTION_TYPE_PROPERTY;
			} else { 
				return NAME_ACTION_TYPE_HOLD;
			}
		} else {
			if (amo_sign > 0) {
				return NAME_CREDIT;
			} else { 
				return NAME_SPEND;
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
			transaction.put("asset", this.getAbsKey());
			transaction.put("amount", this.viewAmount());
			//transaction.put("action_type", this.viewActionType());
			transaction.put("action_key", this.getActionType());
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
				- (this.typeBytes[2]<0?(KEY_LENGTH + AMOUNT_LENGTH):0);
	}

	@Override // - fee + balance - calculate here
	public int isValid(DCSet dcSet, Long releaserReference) {

		int height = this.getBlockHeightByParentOrLast(dcSet);
		boolean wrong = true;

		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.recipient.getAddress()))
		{
			if (height < 120000) {
				wrong = true;
				for ( byte[] valid_address: Transaction.VALID_ADDRESSES) {
					if (Arrays.equals(this.recipient.getBytes(), valid_address)) {
						wrong = false;
						break;
					}
				}
				
				if (wrong) {
					return INVALID_ADDRESS;
				}
			}
		}
		
		//CHECK IF REFERENCE IS OK
		Long reference = releaserReference==null ? this.creator.getLastTimestamp(dcSet) : releaserReference;
		if (reference.compareTo(this.timestamp) >= 0)
			return INVALID_TIMESTAMP;

		boolean isPerson = this.creator.isPerson(dcSet, height);

		//CHECK IF AMOUNT AND ASSET
		if (this.amount != null) {
			long absKey = this.key;
			if (absKey < 0)
				absKey = -absKey;

			AssetCls asset = (AssetCls)dcSet.getItemAssetMap().get(absKey);
			if (asset == null) {
				return ITEM_ASSET_NOT_EXIST;
			}

			//CHECK IF AMOUNT IS DIVISIBLE
			int amount_sign = this.amount.signum();
			
			// BACKWARD - CONFISCATE
			boolean confiscate_credit = typeBytes[1] == 1 
					|| typeBytes[1] > 1 && (typeBytes[2] & BACKWARD_MASK) > 0;
			
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

				int actionType = Account.actionType(key, amount);
				
				if (actionType == 3) {
					// HOLD GOODS
					if (!asset.isMovable()) {
						return NOT_MOVABLE_ASSET;						
					}					
					BigDecimal balance1 = this.creator.getBalance(dcSet, absKey, actionType).b;
					if (amount.compareTo(balance1) > 0) {
						return NO_HOLD_BALANCE;
					}
				
				} else if (actionType == 2) {
					// CREDIT - BORROW
					if (absKey == FEE_KEY) {
						return NOT_DEBT_ASSET;		
					}
					
					// 75hXUtuRoKGCyhzps7LenhWnNtj9BeAF12 -> 7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7
					if (confiscate_credit) {
						// BACKWARD - BORROW - CONFISCATE CREDIT
						Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(
								this.creator.getAddress(), absKey,
								this.recipient.getAddress()); 
						BigDecimal creditAmount = dcSet.getCredit_AddressesMap().get(creditKey);
						if (creditAmount.compareTo(amount) < 0) {
							// NOT ENOUGHT DEBT from recipient to creator
							return NO_DEBT_BALANCE;
						}
						
						/*
						BigDecimal balance1 = this.creator.getBalanceUSE(absKey, db);
						if (balance1.compareTo(amount) < 0) {
							// OWN + (-CREDIT)) = max amount that can be used for new credit
							return NO_BALANCE;
						}
						*/
					} else {
						// CREDIT - GIVE CREDIT
						// OR RETURN CREDIT
						Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(
								this.recipient.getAddress(), absKey,
								this.creator.getAddress());
						// TRY RETURN
						BigDecimal creditAmount = dcSet.getCredit_AddressesMap().get(creditKey);
						if (creditAmount.compareTo(amount) < 0) {
							BigDecimal leftAmount = amount.subtract(creditAmount);
							BigDecimal balanceOwn = this.creator.getBalance(dcSet, absKey, 1).b; // OWN balance
							// NOT ENOUGHT DEBT from recipient to creator
							// TRY CREDITN OWN
							if (balanceOwn.compareTo(leftAmount) < 0) {
								// NOT ENOUGHT DEBT from recipient to creator
								return NO_BALANCE;
							}
						}
					}

				} else if (actionType == 1) {
					
					// SPEND ASSET
					
					if (absKey == RIGHTS_KEY && !BlockChain.DEVELOP_USE
							) {
						
						//byte[] ss = this.creator.getAddress();
						if (height > Transaction.FREEZE_FROM
								&& BlockChain.FOUNDATION_ADDRESSES.contains(this.creator.getAddress())) {
							// LOCK PAYMENTS
							wrong = true;
							for ( String address: Transaction.TRUE_ADDRESSES) {
								if (this.recipient.equals(address)
										// || this.creator.equals(address)
										) {
									wrong = false;
									break;
								}
							}
							
							if (wrong) {
								//int balance = this.creator.getBalance(dcSet, absKey, 1).b.intValue();								
								//if (balance > 3000)
									return INVALID_CREATOR;
							}

						}
						
					}
					
					// if asset is unlimited and me is creator of this asset 
					boolean unLimited = 
							absKey > AssetCls.REAL_KEY // not genesis assets!
							&& asset.getQuantity(dcSet).equals(0l)
							&& asset.getOwner().getAddress().equals(this.creator.getAddress());
		
					//CHECK IF CREATOR HAS ENOUGH ASSET BALANCE
					if (unLimited) {
						// not make RETURN - check validate next
						//
					} else if (absKey == FEE_KEY) {
						if(this.creator.getBalance(dcSet, FEE_KEY, 1).b.compareTo( this.amount.add(this.fee) ) < 0
								&& height < 120000) {
							wrong = true;
							for ( byte[] valid_item: VALID_BAL) {
								if (Arrays.equals(this.signature, valid_item)) {
									wrong = false;
									break;
								}
							}
							
							if (wrong)
								return NO_BALANCE;
						}
						
					} else {
						if(this.creator.getBalance(dcSet, FEE_KEY, 1).b.compareTo( this.fee ) < 0) {
							return NOT_ENOUGH_FEE;
						}
						BigDecimal forSale = this.creator.getForSale(dcSet, absKey);
						
						if (amount.compareTo(forSale) > 0) {
							// TODO: delete wrong check in new CHAIN
							// SOME PAYMENTs is WRONG
							wrong = true;
							for ( byte[] valid_item: VALID_BAL) {
								if (Arrays.equals(this.signature, valid_item)) {
									wrong = false;
									break;
								}
							}
							
							if (wrong)
								return NO_BALANCE;
						}
						
					}
				} else {
					// PRODUCE - SPEND
					// TRY FEE
					if(this.creator.getBalance(dcSet, FEE_KEY, 1).b.compareTo( this.fee ) < 0) {
						return NOT_ENOUGH_FEE;
					}
					BigDecimal balance1 = this.creator.getBalance(dcSet, absKey, actionType).b;
					if (amount.compareTo(balance1) > 0) {
						return NO_BALANCE;
					}
				}
				
				// IF send from PERSON to ANONIMOUSE
				// TODO: PERSON RULE 1
				if (BlockChain.PERSON_SEND_PROTECT && actionType != 2 && isPerson && absKey != FEE_KEY && !this.recipient.isPerson(dcSet, height)) {
					for (byte[] trusted_address: TRUSTED_FOR_ANONYMOUS_SEND) {
						if (Arrays.equals(this.recipient.getBytes(), trusted_address)) {
							wrong = false;
							break;
						}
					}
					
					if (wrong) {
						return RECEIVER_NOT_PERSONALIZED;
					}
				}
			}

		} else {
			// TODO first records is BAD already ((
			//CHECK IF CREATOR HAS ENOUGH FEE MONEY
			if(this.creator.getBalance(dcSet, FEE_KEY).a.b.compareTo(this.fee) < 0)
			{
				return NOT_ENOUGH_FEE;
			}				
	
		}
		
		// PUBLICK TEXT only from PERSONS
		if (this.hasPublicText() && !isPerson) {
			if (BlockChain.DEVELOP_USE) {	
				boolean good = false;
				for ( String admin: BlockChain.GENESIS_ADMINS) {
					if (this.creator.equals(admin)) {
						good = true;
						break;
					}
				}
				if (!good) {
					return CREATOR_NOT_PERSONALIZED;					
				}
			} else if (Base58.encode(this.getSignature()).equals("1ENwbUNQ7Ene43xWgN7BmNzuoNmFvBxBGjVot3nCRH4fiiL9FaJ6Fxqqt9E4zhDgJADTuqtgrSThp3pqWravkfg")) {
				;				
			} else {
				return CREATOR_NOT_PERSONALIZED;
			}
		}
		
		return VALIDATE_OK;
	}		

	public void process(DCSet db, Block block, boolean asPack) {

		super.process(db, block, asPack);
		
		if (this.amount == null)
			return;

		int amount_sign = this.amount.compareTo(BigDecimal.ZERO);
		if (amount_sign == 0)
			return;
						
		long absKey = getAbsKey();

		// BACKWARD - CONFISCATE
		boolean confiscate_credit = typeBytes[1] == 1
				|| typeBytes[1] > 1 && (typeBytes[2] & BACKWARD_MASK) > 0;

		//UPDATE SENDER
		this.creator.changeBalance(db, !confiscate_credit, key, this.amount, false);
		//UPDATE RECIPIENT
		this.recipient.changeBalance(db, confiscate_credit, key, this.amount, false);

		int actionType = Account.actionType(key, amount);
		if (actionType == 2) {
			if (confiscate_credit) {
				// BORROW
				Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(
						this.creator.getAddress(), absKey,
						this.recipient.getAddress()); 
				db.getCredit_AddressesMap().sub(creditKey, this.amount);
			} else {
				// CREDIR or RETURN CREDIT
				Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(
						this.recipient.getAddress(), absKey,
						this.creator.getAddress()); 
				BigDecimal creditAmount = db.getCredit_AddressesMap().get(creditKey);
				if (creditAmount.compareTo(amount) >= 0) {
					// ALL CREDIT RETURN
					db.getCredit_AddressesMap().sub(creditKey, this.amount);
				} else {
					// GET CREDIT for left AMOUNT
					BigDecimal leftAmount = amount.subtract(creditAmount);
					Tuple3<String, Long, String> leftCreditKey = new Tuple3<String, Long, String>(
							this.creator.getAddress(), absKey,
							this.recipient.getAddress()); // REVERSE 
					db.getCredit_AddressesMap().add(leftCreditKey, leftAmount);
				}
			}
		}
		
		if (absKey == Transaction.RIGHTS_KEY
				&& this.recipient.getLastForgingData(db) == -1
				&& this.amount.compareTo(BlockChain.MIN_GENERATING_BALANCE_BD) >= 0) {
			// TODO - если сначала прислать 12 а потом дослать 200000
			// то будет все время выдавать недостаточное чило моне для форжинга
			// так как все доначисления буду вычиаться из самого первого
			// так как ттут нет добавки
			
			
			// update last forging block if it not exist
			// if exist - it not need - incomes will be negate from forging balance

			// it is stil unconfirmed!!!  Block block = this.getParent(db);

			// get height by LAST block in CHAIN + 2 - skip incoming BLOCK
			int blockHeight;
			if (block == null) {
				blockHeight = Controller.getInstance().getBlockChain().getHeight(db) + 1;
			} else {
				blockHeight = block.getHeightByParent(db);
			}
			this.recipient.setForgingData(db, blockHeight);
		}
	}

	public void orphan(DCSet db, boolean asPack) {

		super.orphan(db, asPack);
		
		if (this.amount == null)
			return;

		int amount_sign = this.amount.compareTo(BigDecimal.ZERO);
		if (amount_sign == 0)
			return;
						
		long absKey = getAbsKey();

		// BACKWARD - CONFISCATE
		boolean confiscate_credit = typeBytes[1] == 1
				|| typeBytes[1] > 1 && (typeBytes[2] & BACKWARD_MASK) > 0;
		
		//UPDATE SENDER
		this.creator.changeBalance(db, confiscate_credit, key, this.amount, true);
		//UPDATE RECIPIENT
		this.recipient.changeBalance(db, !confiscate_credit, key, this.amount, true);

		int actionType = Account.actionType(key, amount);
		if (actionType == 2) {
			if (confiscate_credit) {
				// BORROW
				Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(
						this.creator.getAddress(), absKey,
						this.recipient.getAddress()); 
				db.getCredit_AddressesMap().add(creditKey, this.amount);
			} else {
				// in BACK order - RETURN CREDIT << CREDIT
				// GET CREDIT for left AMOUNT
				Tuple3<String, Long, String> leftCreditKey = new Tuple3<String, Long, String>(
						this.creator.getAddress(), absKey,
						this.recipient.getAddress()); // REVERSE 
				BigDecimal leftAmount = db.getCredit_AddressesMap().get(leftCreditKey);
				if (leftAmount.compareTo(amount) < 0) {
					db.getCredit_AddressesMap().sub(leftCreditKey, leftAmount);
					// CREDIR or RETURN CREDIT
					Tuple3<String, Long, String> creditKey = new Tuple3<String, Long, String>(
							this.recipient.getAddress(), absKey,
							this.creator.getAddress()); 
					db.getCredit_AddressesMap().add(creditKey, amount.subtract(leftAmount));
				} else {
					// ONLY RETURN CREDIT
					db.getCredit_AddressesMap().add(leftCreditKey, amount);					
				}

			}
		}
	
		if (absKey == Transaction.RIGHTS_KEY) {			
			// Parent BLOCK is still in MAP!
			int blockHeight;
			if (block == null) {
				blockHeight = Controller.getInstance().getBlockChain().getHeight(db);
			} else {
				blockHeight = block.getHeightByParent(db);
			}
			int lastForgingHeight = this.recipient.getLastForgingData(db);
			if (lastForgingHeight != -1 && lastForgingHeight == blockHeight) {
				int prevForgingHeight = this.recipient.getForgingData(db, blockHeight);
				if (prevForgingHeight == -1 ) {
					// if it is first payment ERM - reset last forging BLOCK
					this.recipient.delForgingData(db, blockHeight);
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
