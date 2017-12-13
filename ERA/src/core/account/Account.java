package core.account;
//04/01 +- 
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import api.ApiErrorFactory;

//import com.google.common.primitives.Bytes;

import at.AT_Transaction;
import lang.Lang;
import controller.Controller;
import core.BlockChain;
//import core.account.PublicKeyAccount;
import core.BlockGenerator;
import core.block.Block;
import core.block.GenesisBlock;
import core.blockexplorer.BlockExplorer.BigDecimalComparator;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.naming.Name;
//import core.item.assets.AssetCls;
import core.transaction.Transaction;
import core.transaction.TransactionAmount;
import datachain.DCSet;
import datachain.ItemAssetBalanceMap;
import datachain.Item_Map;
import datachain.NameMap;
import datachain.OrderMap;
import datachain.ReferenceMap;
import ntp.NTP;
import settings.Settings;
import utils.NameUtils;
import utils.NumberAsString;
import utils.Pair;
import utils.ReverseComparator;
import utils.NameUtils.NameResult;

public class Account {
	
	public static final int ADDRESS_LENGTH = 25;
	//private static final long ERA_KEY = Transaction.RIGHTS_KEY;
	private static final long FEE_KEY = Transaction.FEE_KEY;
	//public static final long ALIVE_KEY = StatusCls.ALIVE_KEY;
	//public static String EMPTY_PUBLICK_ADDRESS = new PublicKeyAccount(new byte[PublicKeyAccount.PUBLIC_KEY_LENGTH]).getAddress();


	protected String address;
	
	//private byte[] lastBlockSignature;
	//private long generatingBalance; //used  for forging balance
	
	protected Account()
	{
		//this.generatingBalance = 0l;
	}
	
	public Account(String address)
	{

		// ///test address
		assert(Base58.decode(address) instanceof byte[] );
		
		this.address = address;
	}
	
	public static Tuple2<Account, String> tryMakeAccount(String address) {
		
		boolean isBase58 = false;
		try
		{
			Base58.decode(address);
			isBase58 = true;
		}
		catch(Exception e)
		{
			if (PublicKeyAccount.isValidPublicKey(address)) {
				// MAY BE IT BASE.32 +
				return new Tuple2<Account, String>(new PublicKeyAccount(address), null);
			}
		}

		if (isBase58) {
			//ORDINARY RECIPIENT
			if(Crypto.getInstance().isValidAddress(address)) {
				return new Tuple2<Account, String>(new Account(address), null);
			} else if (PublicKeyAccount.isValidPublicKey(address)) {
				return new Tuple2<Account, String>(new PublicKeyAccount(address), null);
			} else {
				return new Tuple2<Account, String>(null, "Wrong Address or PublickKey");
			}
		} else {
			//IT IS NAME - resolve ADDRESS
			Pair<Account, NameResult> result = NameUtils.nameToAdress(address);
			
			if(result.getB() == NameResult.OK)
			{
				return new Tuple2<Account, String>(result.getA(), null);
			} else		
			{
				return new Tuple2<Account, String>(null, "The name is not registered");
			}
		}

	}
	public String getAddress()
	{
		return address;
	}
	
	//BALANCE
	public Tuple3<BigDecimal, BigDecimal, BigDecimal> getUnconfirmedBalance(long key)
	{
		return Controller.getInstance().getUnconfirmedBalance(this, key);
	}
	/*
	public BigDecimal getConfirmedBalance()
	{
		return this.getConfirmedBalance(DBSet.getInstance());
	}
	public BigDecimal getConfirmedBalance(DBSet db)
	{
		return db.getAssetBalanceMap().get(getAddress(), Transaction.FEE_KEY);
	}
	*/
	public BigDecimal getBalanceUSE(long key)
	{
		return this.getBalanceUSE(key, DCSet.getInstance());
	}	
	public BigDecimal getBalanceUSE(long key, DCSet db)
	{
		if (key < 0)
			key = -key;
		Tuple5<
		Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,
		Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>> balance = this.getBalance(db, key);
		
		return balance.a.b.add(balance.b.b);
	}

	/*
	public BigDecimal getBalance(long key)
	{
		if (key < 0)
			key = -key;
		return this.getBalance(key, DBSet.getInstance());
	}	
	public BigDecimal getBalance(long key, DBSet db)
	{
		int type = 1; // OWN
		if (key < 0) {
			type = 2; // RENT
			key = -key;
		}
		Tuple3<BigDecimal, BigDecimal, BigDecimal> balance = db.getAssetBalanceMap().get(getAddress(), key);
		
		if (type == 1)
			return balance.a;
		else if (type == 2)
			return balance.b;
		else
			return balance.c;
	}
	
	public Integer setConfirmedPersonStatus(long personKey, long statusKey, int end_date, DBSet db)
	{
		return db.getPersonStatusMap().addItem(personKey, statusKey, end_date);
	}
	*/

	// SET
	/*
	public void setConfirmedBalance(BigDecimal amount)
	{
		this.setConfirmedBalance(amount, DBSet.getInstance());
	}
	public void setConfirmedBalance(BigDecimal amount, DBSet db)
	{
		//UPDATE BALANCE IN DB
		db.getAssetBalanceMap().set(getAddress(), Transaction.FEE_KEY, amount);
	}
	//
	public void setBalance(long key, BigDecimal balance)
	{
		this.setBalance(key, balance, DBSet.getInstance());
	}

	// TODO in_OWN in_RENT on_HOLD
	public void setBalance(long key, BigDecimal balance, DBSet db)
	{

		int type = 1;
		if (key < 0) {
			key = -key;
			type = 2;
		}
		
		Tuple3<BigDecimal, BigDecimal, BigDecimal> value = db.getAssetBalanceMap().get(getAddress(), key); 
		//UPDATE BALANCE IN DB
		if (type == 1) {
			value = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(balance, value.b, value.c);
		} else {
			// SET RENT balance
			value = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(value.a, balance, value.c);
		}
		db.getAssetBalanceMap().set(getAddress(), key, value);
	}
	*/

	public Tuple5<
	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,
	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>> getBalance(long key)
	{
		return this.getBalance(DCSet.getInstance(), key);
	}
	
	public Tuple5<
	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,
	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>> getBalance(DCSet db, long key)
	{
		if (key < 0)
			key = -key;
			
		Tuple5<
		Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,
		Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>> balance = db.getAssetBalanceMap().get(getAddress(), key);
		if (BlockChain.DEVELOP_USE) {
			if (key == 1)
				return new Tuple5<
						Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,
						Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>>
				(new Tuple2<BigDecimal, BigDecimal>(balance.a.a, balance.a.b.add(BigDecimal.valueOf(1000))),
						balance.b, balance.c, balance.d, balance.e);
			else if (key == 2)
				return new Tuple5<
						Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,
						Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>>(
								new Tuple2<BigDecimal, BigDecimal>(balance.a.a, balance.a.b.add(BigDecimal.ONE)),
								balance.b, balance.c, balance.d, balance.e);	
		}
		return balance;
		
	}
	
	public Tuple2<BigDecimal, BigDecimal> getBalance(DCSet db, long key, int actionType)
	{
		if (key < 0)
			key = -key;
			
		Tuple5<
		Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,
		Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>> balance = db.getAssetBalanceMap().get(getAddress(), key);
		
		if (actionType == 1) {
			if (BlockChain.DEVELOP_USE) {
				if (key == 1)
					return new Tuple2<BigDecimal, BigDecimal>(balance.a.a, balance.a.b.add(BigDecimal.valueOf(1000)));
				else if (key == 2)
					return new Tuple2<BigDecimal, BigDecimal>(balance.a.a, balance.a.b.add(BigDecimal.ONE));
			}
			
			return balance.a;
		}
		else if (actionType == 2)
			return balance.b;
		else if (actionType == 3)
			return balance.c;
		else if (actionType == 4)
			return balance.d;
		else
			return balance.e;

	}

	// make TYPE of transactionAmount by signs of KEY and AMOUNT
	public static int actionType(long key, BigDecimal amount) {
		int type;
		int amount_sign = amount.signum();
		if (key > 0) {
			if (amount_sign > 0) {
				// SEND 
				type = 1;
			} else {
				// HOLD in STOCK
				type = 3;
			}
		} else {
			if (amount_sign > 0) {
				// give CREDIT or BORROW CREDIT
				type = 2;
			} else {
				// PRODUCE or SPEND 
				type = 4;				
			}
		}
		
		return type;
		
	}
	
	// change BALANCE - add or subtract amount by KEY + AMOUNT = TYPE
	public Tuple3<BigDecimal, BigDecimal, BigDecimal> changeBalance(DCSet db, boolean subtract, long key, BigDecimal amount, boolean asOrphan) 
	{
		
		int type = actionType(key, amount);
		long absKey;
		if (key > 0) {
			absKey = key;
		} else {
			absKey = -key;
		}

		Tuple5<
		Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,
		Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>> balance = db.getAssetBalanceMap().get(getAddress(), absKey);

		boolean updateIncomed = subtract && asOrphan || !subtract && !asOrphan;
		
		if (type == 1) {
			// OWN + property
			balance = new Tuple5<
					Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,
					Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>>(
							subtract?
								new Tuple2<BigDecimal, BigDecimal>(
										updateIncomed?balance.a.a.subtract(amount):balance.a.a, balance.a.b.subtract(amount)):
								new Tuple2<BigDecimal, BigDecimal>(
										updateIncomed?balance.a.a.add(amount):balance.a.a, balance.a.b.add(amount)),
							balance.b, balance.c, balance.d, balance.e
					);
		} else if (type == 2) {
			// DEBT + CREDIT
			balance = new Tuple5<
					Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,
					Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>>(
							balance.a,
							subtract?
									new Tuple2<BigDecimal, BigDecimal>(
											updateIncomed?balance.b.a.subtract(amount):balance.b.a, balance.b.b.subtract(amount)):
									new Tuple2<BigDecimal, BigDecimal>(
											updateIncomed?balance.b.a.add(amount):balance.b.a, balance.b.b.add(amount)),
							balance.c, balance.d, balance.e
					);
		} else if(type == 3) {
			// HOLD + STOCK
			balance = new Tuple5<
					Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,
					Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>>(
							balance.a, balance.b,
							subtract?
									new Tuple2<BigDecimal, BigDecimal>(
											updateIncomed?balance.c.a.subtract(amount):balance.c.a, balance.c.b.subtract(amount)):
									new Tuple2<BigDecimal, BigDecimal>(
											updateIncomed?balance.c.a.add(amount):balance.c.a, balance.c.b.add(amount)),
							balance.d, balance.e
					);
		} else {
			// TODO - SPEND + PRODUCE
			balance = new Tuple5<
					Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>,
					Tuple2<BigDecimal, BigDecimal>,	Tuple2<BigDecimal, BigDecimal>>(
							balance.a, balance.b, balance.c,
							subtract?
									new Tuple2<BigDecimal, BigDecimal>(
											updateIncomed?balance.d.a.subtract(amount):balance.d.a, balance.d.b.subtract(amount)):
									new Tuple2<BigDecimal, BigDecimal>(
											updateIncomed?balance.d.a.add(amount):balance.d.a, balance.d.b.add(amount)),
							balance.e
					);
		}
		
		db.getAssetBalanceMap().set(getAddress(), absKey, balance);
		return new Tuple3<BigDecimal, BigDecimal, BigDecimal>(balance.a.b, balance.b.b, balance.c.b);
	}	

	/*
	public void setBalance3(long key, Tuple3<BigDecimal, BigDecimal, BigDecimal> balance, DBSet db)
	{
		if (key < 0)
			key = -key;
		
		db.getAssetBalanceMap().set(getAddress(), key, balance);
	}

	public void addBalanceOWN(long key, BigDecimal value, DBSet db)
	{
		Tuple3<BigDecimal, BigDecimal, BigDecimal> balance = this.getBalance3(key, db);
		Tuple3<BigDecimal, BigDecimal, BigDecimal> balance_new = 
				 new Tuple3<BigDecimal, BigDecimal, BigDecimal>(balance.a.add(value), balance.b, balance.c);
		
		this.setBalance3(key, balance_new, db);
	}
	*/

	
	// STATUS
	/*
	public void setConfirmedPersonStatus(long personKey, long statusKey, Integer days)
	{
		this.setConfirmedPersonStatus(personKey, statusKey, days, DBSet.getInstance());
	}
		
	public void setConfirmedPersonStatus(long personKey, long statusKey, Integer days, DBSet db)
	{
		//UPDATE PRIMARY TIME IN DB
		db.getPersonStatusMap().set(personKey, statusKey, days);
	}
	*/

	
	public Tuple3<BigDecimal, BigDecimal, BigDecimal> getConfBalance3(int confirmations, long key)
	{
		return this.getConfBalance3(confirmations, key, DCSet.getInstance());
	}
	public Tuple3<BigDecimal, BigDecimal, BigDecimal> getConfBalance3(int confirmations, long key, DCSet db)
	{
		//CHECK IF UNCONFIRMED BALANCE
		if(confirmations <= 0)
		{
			return this.getUnconfirmedBalance(key);
		}
		
		//IF 1 CONFIRMATION
		if(confirmations == 1)
		{
			Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
					Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
					Tuple2<BigDecimal, BigDecimal>> balance = this.getBalance(db, key); 
			return new Tuple3<BigDecimal, BigDecimal, BigDecimal>(balance.a.b, balance.b.b, balance.c.b);
		}
		
		//GO TO PARENT BLOCK 10
		Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
		Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
		Tuple2<BigDecimal, BigDecimal>> balance = this.getBalance(db, key);
		BigDecimal own = balance.a.b;
		BigDecimal rent = balance.b.b;
		BigDecimal hold = balance.c.b;
		
		Block block = db.getBlockMap().getLastBlock();
		
		for(int i=1; i<confirmations && block != null && block.getVersion()>0; i++)
		{
			for(Transaction transaction: block.getTransactions())
			{
				if(transaction.isInvolved(this))
				{
					if (transaction.getType() == Transaction.HOLD_ASSET_TRANSACTION) {
						
					} else if (transaction.getKey() > 0) {
						own = own.subtract(transaction.getAmount(this));
					} else {
						rent = own.subtract(transaction.getAmount(this));						
					}
					
				}
			}
				
			block = block.getParent(db);
		}

		//RETURN
		return new Tuple3<BigDecimal, BigDecimal, BigDecimal>(own, rent, hold) ;
	}
	
	/*
	private void updateGeneratingBalance(DBSet db)
	{
		//CHECK IF WE NEED TO RECALCULATE
		if(this.lastBlockSignature == null)
		{
			this.lastBlockSignature = db.getBlockMap().getLastBlockSignature();
			calculateGeneratingBalance(db);
		}
		else
		{
			//CHECK IF WE NEED TO RECALCULATE
			if(!Arrays.equals(this.lastBlockSignature, db.getBlockMap().getLastBlockSignature()))
			{
				this.lastBlockSignature = db.getBlockMap().getLastBlockSignature();
				calculateGeneratingBalance(db);
			}
		}
	}

	// take current balance
	public void calculateGeneratingBalance(DBSet db)
	{
		long balance = this.getConfirmedBalance(ERA_KEY, db).setScale(0).longValue();
		this.generatingBalance = balance;
	}

	// balance FOR generation
	public void calculateGeneratingBalance_old(DBSet db)
	{
		//CONFIRMED BALANCE + ALL NEGATIVE AMOUNTS IN LAST 9 BLOCKS - for ERA_KEY only
		BigDecimal balance = this.getConfirmedBalance(ERA_KEY, db);
		
		Block block = db.getBlockMap().getLastBlock();
		
		int penalty_koeff = 1000000;
		int balance_penalty = penalty_koeff;
		
		// icreator X 10
		// not resolve first 100 blocks
		for(int i=1; i<GenesisBlock.GENERATING_RETARGET * 10 && block != null && block.getHeight(db) > 100; i++)
		{
			for(Transaction transaction: block.getTransactions())
			{
				if(transaction.isInvolved(this) & transaction instanceof TransactionAmount)
				{
					TransactionAmount ta = (TransactionAmount)transaction;
					
					if(ta.getKey() == ERA_KEY & transaction.getAmount(this).compareTo(BigDecimal.ZERO) == 1)
					{
						balance = balance.subtract(transaction.getAmount(this));
					}
				}
			}
			LinkedHashMap<Tuple2<Integer,Integer>,AT_Transaction> atTxs = db.getATTransactionMap().getATTransactions(block.getHeight(db));
			Iterator<AT_Transaction> iter = atTxs.values().iterator(); 
			while ( iter.hasNext() )
			{
				AT_Transaction key = iter.next();
				if ( key.getRecipient().equals( this.getAddress() ) )
				{
					balance = balance.subtract( BigDecimal.valueOf(key.getAmount(), 8) );
				}
			}
			
			// icreator X 0.9 for each block generated
			if (balance_penalty > 0.1 * penalty_koeff && block.getCreator().getAddress().equals(this.address)) {
				balance_penalty *= Settings.GENERATE_CONTINUOUS_PENALTY * 0.001;
			} else {
				// reset
				balance_penalty = penalty_koeff;
			}
			block = block.getParent(db);
		}
		
		//DO NOT GO BELOW 0
		if(balance.compareTo(BigDecimal.ZERO) == -1)
		{
			balance = BigDecimal.ZERO.setScale(8);
		}

		// use penalty
		this.generatingBalance = balance.multiply(new BigDecimal(balance_penalty / penalty_koeff));
		
	}
	*/

	//REFERENCE
	
	public Long getLastTimestamp()
	{
		return this.getLastTimestamp(DCSet.getInstance());
	}
	
	public Long getLastTimestamp(DCSet dcSet)
	{
		return dcSet.getReferenceMap().getLast(this.getAddress());
	}

	/*
	public void setLastReference(Long timestamp)
	{
		this.setLastReference(timestamp, DBSet.getInstance());
	}
	*/
	
	public void setLastTimestamp(Long timestamp, DCSet dcSet)
	{
		byte[] key = Base58.decode(this.getAddress());
		ReferenceMap map = dcSet.getReferenceMap();

		// GET CURRENT REFERENCE
		Long reference = map.get(key);
		
		// MAKE KEY for this TIMESTAMP
		byte[] keyTimestamp = Bytes.concat(key, Longs.toByteArray(timestamp));
		
		// set NEW LAST TIMESTAMP as REFERENCE
		map.set(keyTimestamp, reference);

		// SET NEW REFERENCE
		map.set(key, timestamp);
	}
		
	public void removeLastTimestamp(DCSet dcSet) 
	{
		byte[] key = Base58.decode(this.getAddress());
		ReferenceMap map = dcSet.getReferenceMap();
				
		// GET LAST TIMESTAMP
		Long timestamp = map.get(key);

		// MAKE KEY for this TIMESTAMP
		byte[] keyTimestamp = Bytes.concat(key, Longs.toByteArray(timestamp));

		// GET REFERENCE
		Long reference = map.get(keyTimestamp);
		
		// DELETE TIMESTAMP - REFERENCE
		map.delete(keyTimestamp);
		// SET OLD REFERENCE
		map.set(key, reference);
	}
	
	//TOSTRING
	public String personChar(Tuple2<Integer, PersonCls> personRes)
	{
		if (personRes == null) return "";
		
		PersonCls person = personRes.b;
		if (person.getDeathday()/10 > person.getBirthday()/10)
			return "="; //"☗";
		
		int key = personRes.a;
		if (key == -1) return "-"; //"☺";
		else if (key == 1) return "+"; //"♥"; //"☺"; //"☑"; 9829
		else return "";
		
	}

	@Override
	public String toString()
	{
		Tuple2<Integer, PersonCls> personRes = this.getPerson();
		String personStr;
		String addressStr;
		if (personRes == null) {
			personStr = "";
			addressStr = this.getAddress();
		}
		else {
			personStr = personChar(personRes) + personRes.b.getShort();
			addressStr = this.getAddress().substring(0, 8);
		}
		return " {" + NumberAsString.getInstance().numberAsString(this.getBalanceUSE(FEE_KEY)) + "}"
				+ " " + addressStr + " " + personStr;
	}
		
	public String toString(long key)
	{
		Tuple2<Integer, PersonCls> personRes = this.getPerson();
		String personStr;
		String addressStr;
		if (personRes == null) {
			personStr = "";
			addressStr = GenesisBlock.CREATOR.equals(this)?"GENESIS":this.getAddress();
		}
		else {
			personStr = personChar(personRes) + personRes.b.getShort();
			addressStr = this.getAddress().substring(0, 8);
		}
		
		boolean statusBad = Controller.getInstance().getStatus() != Controller.STATUS_OK;

		return (statusBad?"??? ":"")
				+ NumberAsString.getInstance().numberAsString(this.getBalanceUSE(key))
				+ " {" + NumberAsString.getInstance().numberAsString(this.getBalanceUSE(FEE_KEY)) + "}"
				+ " " + addressStr + " " + personStr;
	}
	
	//////////
	public String viewPerson() {
		Tuple2<Integer, PersonCls> personRes = this.getPerson();
		if (personRes == null) {
			if (this.getAddress()!= null) {
				return this.getAddress();
				
			}else {return "";}
		} else {
			String personStr = personChar(personRes) + personRes.b.toString();
			return personStr;
		}
		
	}
	
	public String getPersonAsString()
	{
		Tuple2<Integer, PersonCls> personRes = this.getPerson();
		if (personRes == null) {
			return GenesisBlock.CREATOR.equals(this)?"GENESIS":this.getAddress();
		}
		else {
			String personStr = personChar(personRes) + personRes.b.getShort();
			String addressStr = this.getAddress().substring(1, 6);
			return addressStr + ": " + personStr;
		}
	}

	public String getPersonAsString_01(boolean shrt)
	{
		Tuple2<Integer, PersonCls> personRes = this.getPerson();
		if (personRes == null) {
			return "";
		}
		else {
			return shrt? personRes.b.getShort(): personRes.b.getName();
		}
	}

	public static String getDetails(String toValue, AssetCls asset) {

		String out = "";
		
		if(toValue.isEmpty())
		{
			return out;
		}

		boolean statusBad = Controller.getInstance().getStatus() != Controller.STATUS_OK;
		
		Account account = null;
		
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(toValue))
		{
			Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(toValue);
					
			if(nameToAdress.getB() == NameResult.OK)
			{
				account = nameToAdress.getA();
				return (statusBad?"??? ":"") + account.toString(asset.getKey());
			}
			else
			{
				return (statusBad?"??? ":"") + nameToAdress.getB().getShortStatusMessage();
			}
		} else
		{
			account = new Account(toValue);
			
			
			if(account.getBalanceUSE(asset.getKey()).compareTo(BigDecimal.ZERO) == 0
					&& account.getBalanceUSE(Transaction.FEE_KEY).compareTo(BigDecimal.ZERO) == 0)
			{
				return Lang.getInstance().translate("Warning!") + " "
						+ (statusBad?"???":"") + account.toString(asset.getKey());
			} else {
				return (statusBad?"???":"") + account.toString(asset.getKey());
			}
		}
		
	}
	@Override
	public int hashCode()
	{
		return this.getAddress().hashCode();
	}
	
	//EQUALS
	@Override
	public boolean equals(Object b)
	{
		if(b instanceof Account) {
			return this.getAddress().equals(((Account) b).getAddress());
		} else if (b instanceof String) {
			return this.getAddress().equals((String) b);			
		}
		
		return false;	
	}

	// personKey, days, block, reference
	public static Tuple4<Long, Integer, Integer, Integer> getPersonDuration(DCSet db, String address) {
		return db.getAddressPersonMap().getItem(address);				
	}
	public Tuple4<Long, Integer, Integer, Integer> getPersonDuration(DCSet db) {
		return getPersonDuration(db, this.address);
	}
	
	
	public boolean isPerson(DCSet dcSet, int forHeight) {
		
		// IF DURATION ADDRESS to PERSON IS ENDED
		Tuple4<Long, Integer, Integer, Integer> addressDuration = this.getPersonDuration(dcSet);
		if (addressDuration == null) return false;

		// TEST TIME and EXPIRE TIME		
		long current_time = Controller.getInstance().getBlockChain().getTimestamp(forHeight);
		
		// TEST TIME and EXPIRE TIME for PERSONALIZE address
		int days = addressDuration.b;
		if (days < 0 ) return false;
		if (days * (long)86400000 < current_time ) return false;

		// IF PERSON ALIVE
		Long personKey = addressDuration.a;
		// TODO by deth day if 
		/*
		//Tuple5<Long, Long, byte[], Integer, Integer> personDuration = db.getPersonStatusMap().getItem(personKey, ALIVE_KEY);
		// TEST TIME and EXPIRE TIME for ALIVE person
		Long end_date = personDuration.b;
		if (end_date == null ) return true; // permanent active
		if (end_date < current_time + 86400000 ) return false; // - 1 day
		*/
		
		return true;
		
	}
	public boolean isPerson() {
		return isPerson(DCSet.getInstance(), Controller.getInstance().getMyHeight());
	}

	
	public Tuple2<Integer, PersonCls> getPerson(DCSet dcSet, int forHeight) {
		
		// IF DURATION ADDRESS to PERSON IS ENDED
		Tuple4<Long, Integer, Integer, Integer> addressDuration = this.getPersonDuration(dcSet);
		if (addressDuration == null) return null;

		// TEST TIME and EXPIRE TIME		
		long current_time = Controller.getInstance().getBlockChain().getTimestamp(forHeight);
		
		// get person
		Long personKey = addressDuration.a;
		PersonCls person = (PersonCls)Controller.getInstance().getItem(dcSet, ItemCls.PERSON_TYPE, personKey);
		
		// TEST ADDRESS is ACTIVE?
		int days = addressDuration.b;
		// TODO x 1000 ?
		if (days < 0 || days * (long)86400000 < current_time )
			return new Tuple2<Integer, PersonCls>(-1, person);

		// IF PERSON is ALIVE
		// TODO by DEATH day
		/*
		Tuple5<Long, Long, byte[], Integer, Integer> personDuration = db.getPersonStatusMap().getItem(personKey, ALIVE_KEY);
		// TEST TIME and EXPIRE TIME for ALIVE person
		if (personDuration == null)
			return new Tuple2<Integer, PersonCls>(-2, person);
		Long end_date = personDuration.b;
		if (end_date == null )
			// permanent active
			return new Tuple2<Integer, PersonCls>(0, person);
		else if (end_date < current_time + 86400000 )
			// ALIVE expired
			return new Tuple2<Integer, PersonCls>(-1, person);
		*/
		
		return new Tuple2<Integer, PersonCls>(1, person);
		
	}
	public Tuple2<Integer, PersonCls> getPerson() {
		return getPerson(DCSet.getInstance(), Controller.getInstance().getMyHeight());
	}
	
	// previous forging block
	public Integer getForgingData(DCSet db, int height) {
		return db.getAddressForging().get(this.address, height);
	}
	/*
	public void setForgingData(DBSet db, int height, int prevHeight) {
		db.getAddressForging().set(this.address, height, prevHeight);
	}
	*/
	public void setForgingData(DCSet db, int height) {
		db.getAddressForging().set(this.address, height);
	}
	public void delForgingData(DCSet db, int height) {
		db.getAddressForging().delete(this.address, height);
	}
	public Integer getLastForgingData(DCSet db) {
		return db.getAddressForging().getLast(this.address);
	}
	/*
	public void setLastForgingData(DBSet db, int prevHeight) {
		db.getAddressForging().setLast(this.address, prevHeight);
	}
	*/
	
	// calc WIN_VALUE for ACCOUNT in HEIGHT
	public long calcWinValue(DCSet dcSet, BlockChain bchain, List<Block> lastBlocksForTarget, int height, long target) {
		
		int generatingBalance = Block.calcGeneratingBalance(dcSet, this, height);
		
		if(!Controller.getInstance().isTestNet() && generatingBalance < BlockChain.MIN_GENERATING_BALANCE)
			return 0l;
		
		// test repeated win account
		if (!Controller.getInstance().isTestNet()) {
			int repeated = Block.isSoRapidly(height, this, lastBlocksForTarget);
			if (repeated > 0) {
				return -repeated;
			}
		}
		
		// TEST STRONG of win Value
		int previousForgingHeight = Block.getPreviousForgingHeightForCalcWin(dcSet, this, height);
		if (previousForgingHeight < 1)
			return 0l;

		long winned_value = Block.calcWinValue(previousForgingHeight, height, generatingBalance);

		int base = BlockChain.getMinTarget(height);
		int targetedWinValue = Block.calcWinValueTargeted2(winned_value, target); 
		if (!Controller.getInstance().isTestNet() && base > targetedWinValue) {
			return -targetedWinValue;
		}

		return winned_value;
		
	}
	
	public static Map<String, BigDecimal> getKeyBalancesWithForks(DCSet dcSet, long key, Map<String, BigDecimal> values) {
		ItemAssetBalanceMap map = dcSet.getAssetBalanceMap();
		Iterator<Tuple2<String, Long>> iterator = map.getIterator(0, true);
		Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
			Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
			Tuple2<BigDecimal, BigDecimal>> ballance;

		Tuple2<String, Long> iteratorKey;
		while (iterator.hasNext()) {
			iteratorKey = iterator.next();
			if(iteratorKey.b == key)
			{
				ballance =  map.get(iteratorKey);
				values.put(iteratorKey.a, ballance.a.b);
			}
		}
		
		DCSet dcParent = dcSet.getParent();
		if (dcParent != null) {
			values = getKeyBalancesWithForks(dcParent, key, values);
		}

		return values;

	}

	public static Map<String, BigDecimal> getKeyOrdersWithForks(DCSet dcSet, long key, Map<String, BigDecimal> values) {

		OrderMap map = dcSet.getOrderMap();
		Iterator<BigInteger> iterator = map.getIterator(0, true);
		Order order;
		while (iterator.hasNext()) {
			order =  map.get(iterator.next());
			if(order.getHave() == key)
			{
				String address = order.getCreator().address;
				values.put(address, values.get(address).add(order.getAmountHave()));
			}
		}
		
		DCSet dcParent = dcSet.getParent();
		if (dcParent != null) {
			values = getKeyOrdersWithForks(dcParent, key, values);
		}

		return values;

	}

	// top balance + orders values
	public static String getRichWithForks(DCSet dcSet, long key) {
		
		Map<String, BigDecimal> values = new TreeMap<String, BigDecimal>();

		values = getKeyBalancesWithForks(dcSet, key, values);

		// add ORDER values
		values = getKeyOrdersWithForks(dcSet, key, values);
		
		// search richest address
		String rich = null;
		BigDecimal maxValue = BigDecimal.ZERO;
		for (Map.Entry<String, BigDecimal> entry : values.entrySet()) {
		    BigDecimal value = entry.getValue();
			if(value.compareTo(maxValue) > 0)
			{
				maxValue = value;
				rich = entry.getKey();
			}
		}

		return rich;

	}

	
}
