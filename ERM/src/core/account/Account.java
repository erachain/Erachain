package core.account;
//04/01 +- 
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;

import api.ApiErrorFactory;

//import com.google.common.primitives.Bytes;

import at.AT_Transaction;
import database.Item_Map;
import database.DBSet;
import database.NameMap;
import controller.Controller;
import core.BlockChain;
//import core.account.PublicKeyAccount;
import core.BlockGenerator;
import core.block.Block;
import core.block.GenesisBlock;
import core.blockexplorer.BlockExplorer.BigDecimalComparator;
import core.crypto.Base58;
import core.item.ItemCls;
import core.item.assets.Order;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.naming.Name;
//import core.item.assets.AssetCls;
import core.transaction.Transaction;
import core.transaction.TransactionAmount;
import ntp.NTP;
import settings.Settings;
import utils.NameUtils;
import utils.NumberAsString;
import utils.Pair;
import utils.ReverseComparator;
import utils.NameUtils.NameResult;

public class Account {
	
	public static final int ADDRESS_LENGTH = 25;
	private static final long ERM_KEY = Transaction.RIGHTS_KEY;
	private static final long FEE_KEY = Transaction.FEE_KEY;
	//public static final long ALIVE_KEY = StatusCls.ALIVE_KEY;
	public static String EMPTY_PUBLICK_ADDRESS = new PublicKeyAccount(new byte[PublicKeyAccount.PUBLIC_KEY_LENGTH]).getAddress();


	protected String address;
	
	//private byte[] lastBlockSignature;
	//private long generatingBalance; //used  for forging balance
	
	protected Account()
	{
		//this.generatingBalance = 0l;
	}
	
	public Account(String address)
	{

		// test address
		assert(Base58.decode(address) instanceof byte[] );

		this.address = address;
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
		return this.getBalanceUSE(key, DBSet.getInstance());
	}	
	public BigDecimal getBalanceUSE(long key, DBSet db)
	{
		if (key < 0)
			key = -key;
		Tuple3<BigDecimal, BigDecimal, BigDecimal> balance = db.getAssetBalanceMap().get(getAddress(), key);
		return balance.a.add(balance.b);
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

	public Tuple3<BigDecimal, BigDecimal, BigDecimal> getBalance(long key)
	{
		return this.getBalance(DBSet.getInstance(), key);
	}
	public Tuple3<BigDecimal, BigDecimal, BigDecimal> getBalance(DBSet db, long key)
	{
		if (key < 0)
			key = -key;
			
		return db.getAssetBalanceMap().get(getAddress(), key);
	}
	
	public BigDecimal getBalance(DBSet db, long key, int actionType)
	{
		if (key < 0)
			key = -key;
			
		Tuple3<BigDecimal, BigDecimal, BigDecimal> balance = db.getAssetBalanceMap().get(getAddress(), key);
		if (actionType == 1)
			return balance.a;
		else if (actionType == 2)
			return balance.b;
		else if (actionType == 3)
			return balance.c;
		else
			return balance.a;

	}

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
	public Tuple3<BigDecimal, BigDecimal, BigDecimal> changeBalance(DBSet db, boolean subtract, long key, BigDecimal amount) 
	{
		
		int type = actionType(key, amount);
		long absKey;
		if (key > 0) {
			absKey = key;
		} else {
			absKey = -key;
		}

		Tuple3<BigDecimal, BigDecimal, BigDecimal> balance = db.getAssetBalanceMap().get(getAddress(), absKey);

		if (type == 1) {
			// OWN + property
			balance = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
					subtract?balance.a.subtract(amount):balance.a.add(amount),
					balance.b, balance.c
					);
		} else if (type == 2) {
			// DEBT + CREDIT
			balance = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
					balance.a,
					subtract?balance.b.subtract(amount):balance.b.add(amount),
					balance.c
					);
		} else if(type == 3) {
			// HOLD + STOCK
			balance = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
					balance.a, balance.b,
					subtract?balance.c.subtract(amount):balance.c.add(amount)
					);
		} else {
			// TODO - SPEND + PRODUCE
			balance = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
					balance.a, balance.b,
					subtract?balance.c.subtract(amount):balance.c.add(amount)
					);
		}
		
		db.getAssetBalanceMap().set(getAddress(), absKey, balance);
		return balance;
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
		return this.getConfBalance3(confirmations, key, DBSet.getInstance());
	}
	public Tuple3<BigDecimal, BigDecimal, BigDecimal> getConfBalance3(int confirmations, long key, DBSet db)
	{
		//CHECK IF UNCONFIRMED BALANCE
		if(confirmations <= 0)
		{
			return this.getUnconfirmedBalance(key);
		}
		
		//IF 1 CONFIRMATION
		if(confirmations == 1)
		{
			return this.getBalance(db, key);
		}
		
		//GO TO PARENT BLOCK 10
		Tuple3<BigDecimal, BigDecimal, BigDecimal> balance = this.getBalance(db, key);
		BigDecimal own = balance.a;
		BigDecimal rent = balance.b;
		BigDecimal hold = balance.c;
		
		Block block = db.getBlockMap().getLastBlock();
		
		for(int i=1; i<confirmations && block != null && block instanceof Block; i++)
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
		long balance = this.getConfirmedBalance(ERM_KEY, db).setScale(0).longValue();
		this.generatingBalance = balance;
	}

	// balance FOR generation
	public void calculateGeneratingBalance_old(DBSet db)
	{
		//CONFIRMED BALANCE + ALL NEGATIVE AMOUNTS IN LAST 9 BLOCKS - for ERM_KEY only
		BigDecimal balance = this.getConfirmedBalance(ERM_KEY, db);
		
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
					
					if(ta.getKey() == ERM_KEY & transaction.getAmount(this).compareTo(BigDecimal.ZERO) == 1)
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
	
	public Long getLastReference()
	{
		return this.getLastReference(DBSet.getInstance());
	}
	
	public Long getLastReference(DBSet db)
	{
		return db.getReferenceMap().get(this.getAddress());
	}
	
	public void setLastReference(Long timestamp)
	{
		this.setLastReference(timestamp, DBSet.getInstance());
	}
	
	public void setLastReference(Long timestamp, DBSet db)
	{
		db.getReferenceMap().set(this.getAddress(), timestamp);
	}
	
	public void removeReference() 
	{
		this.removeReference(DBSet.getInstance());
	}
	
	public void removeReference(DBSet db) 
	{
		db.getReferenceMap().delete(this.getAddress());
	}
	
	//TOSTRING
	
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
			personStr = personRes.b.getShort();
			addressStr = this.getAddress().substring(0, 8);
			if (personRes.a == -2) personStr = "[-]" + personStr;
			else if (personRes.a == -1) personStr = "[?]" + personStr;
			else if (personRes.a == 0) personStr = "[++]" + personStr;
			else if (personRes.a == 1) personStr = "[+]" + personStr;
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
			addressStr = this.getAddress();
		}
		else {
			personStr = personRes.b.getShort();
			addressStr = this.getAddress().substring(0, 8);
			if (personRes.a == -2) personStr = "[-]" + personStr;
			else if (personRes.a == -1) personStr = "[?]" + personStr;
			else if (personRes.a == 0) personStr = "[++]" + personStr;
			else if (personRes.a == 1) personStr = "[+]" + personStr;
		}
		return NumberAsString.getInstance().numberAsString(this.getBalanceUSE(key))
				+ " {" + NumberAsString.getInstance().numberAsString(this.getBalanceUSE(FEE_KEY)) + "}"
				+ " " + addressStr + " " + personStr;
	}
	
	//////////
	public String viewPerson() {
		Tuple2<Integer, PersonCls> personRes = this.getPerson();
		if (personRes == null) {
			return "";
		} else {
			String personStr = personRes.b.toString();
			if (personRes.a == -2) personStr = "[-]" + personStr;
			else if (personRes.a == -1) personStr = "[?]" + personStr;
			//else if (personRes.a == 0) personStr = "[+]" + personStr; // default is permanent ACTIVE
			else if (personRes.a == 1) personStr = "[+]" + personStr;
			return personStr;
		}
		
	}
	
	public String getPersonAsString()
	{
		Tuple2<Integer, PersonCls> personRes = this.getPerson();
		if (personRes == null) {
			return this.getAddress();
		}
		else {
			String personStr = personRes.b.getShort();
			String addressStr = this.getAddress().substring(1, 6);
			if (personRes.a == -2) personStr = "[-]" + personStr;
			else if (personRes.a == -1) personStr = "[?]" + personStr;
			//else if (personRes.a == 0) personStr = "[+]" + personStr; // default is permanent ACTIVE
			else if (personRes.a == 1) personStr = "[+]" + personStr;
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

	@Override
	public int hashCode()
	{
		return this.getAddress().hashCode();
	}
	
	//EQUALS
	@Override
	public boolean equals(Object b)
	{
		if(b instanceof Account)
		{
			return this.getAddress().equals(((Account) b).getAddress());
		} else if (b instanceof String) {
			return this.getAddress().equals((String) b);			
		}
		
		return false;	
	}

	// personKey, days, block, reference
	public static Tuple4<Long, Integer, Integer, Integer> getPersonDuration(DBSet db, String address) {
		return db.getAddressPersonMap().getItem(address);				
	}
	public Tuple4<Long, Integer, Integer, Integer> getPersonDuration(DBSet db) {
		return getPersonDuration(db, this.address);
	}
	
	public boolean isPerson(DBSet db) {
		
		// IF DURATION ADDRESS to PERSON IS ENDED
		Tuple4<Long, Integer, Integer, Integer> addressDuration = this.getPersonDuration(db);
		if (addressDuration == null) return false;
		// TEST TIME and EXPIRE TIME
		long current_time = NTP.getTime();
		
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
	public Tuple2<Integer, PersonCls> getPerson(DBSet db) {
		
		// IF DURATION ADDRESS to PERSON IS ENDED
		Tuple4<Long, Integer, Integer, Integer> addressDuration = this.getPersonDuration(db);
		if (addressDuration == null) return null;
		// TEST TIME and EXPIRE TIME
		long current_time = NTP.getTime();
		
		// get person
		Long personKey = addressDuration.a;
		PersonCls person = (PersonCls)Controller.getInstance().getItem(db, ItemCls.PERSON_TYPE, personKey);
		
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
		return getPerson(DBSet.getInstance());
	}
	
	// previous forging block
	public Integer getForgingData(DBSet db, int height) {
		return db.getAddressForging().get(this.address, height);
	}
	public void setForgingData(DBSet db, int height, int prevHeight) {
		db.getAddressForging().set(this.address, height, prevHeight);
	}
	public void setForgingData(DBSet db, int height) {
		int previousForgingHeight = this.getForgingData(db, height);
		db.getAddressForging().set(this.address, height, previousForgingHeight);
	}
	public void delForgingData(DBSet db, int height) {
		db.getAddressForging().delete(this.address, height);
	}
	public Integer getLastForgingData(DBSet db) {
		return db.getAddressForging().getLast(this.address);
	}
	/*
	public void setLastForgingData(DBSet db, int prevHeight) {
		db.getAddressForging().setLast(this.address, prevHeight);
	}
	*/
	
	// calc WIN_VALUE for ACCOUNT in HEIGHT
	public long calcWinValue(DBSet dbSet, BlockChain bchain, List<Block> lastBlocksForTarget, int height, long target) {
		
		int generatingBalance = Block.calcGeneratingBalance(dbSet, this, height);
		
		if(generatingBalance < BlockChain.MIN_GENERATING_BALANCE)
			return 0l;
		
		// test repeated win account
		if (height < 100 && lastBlocksForTarget != null && !lastBlocksForTarget.isEmpty()) {
			// NEED CHECK ONLY ON START
			int i = 0;
			for (Block testBlock: lastBlocksForTarget) {
				i++;
				if (testBlock.getCreator().equals(this)) {
					return 0l;
				} else if ( i > bchain.REPEAT_WIN)
					break;
			}
		}

		/*
		// if new block in DB - get next height
		if (this.getForgingData(dbSet, height) != -1) {
			height++;
		}
		*/
		
		long winned_value = Block.calcWinValue(dbSet, this, height, generatingBalance);
		
		// not use small values
		if (!bchain.isGoodWinForTarget(height, winned_value, target)) {
			return 0l;
		}

		return winned_value;
		
	}
	
	// top balance + orders values
	public static String getRich(long key) {
		
		Map<String, BigDecimal> values = new TreeMap<String, BigDecimal>();

		Collection<Tuple2<String, Long>> addrs = DBSet.getInstance().getAssetBalanceMap().getKeys();
		
		for (Tuple2<String, Long> addr : addrs) {
			if(addr.b == key)
			{
				Tuple3<BigDecimal, BigDecimal, BigDecimal> ball =  DBSet.getInstance().getAssetBalanceMap().get(addr);
				values.put(addr.a, ball.a);
			}
		}

		// add ORDER values
		Collection<Order> orders = DBSet.getInstance().getOrderMap().getValues();

		for (Order order : orders) {
			if(order.getHave() == key)
			{
				String address = order.getCreator().address;
				values.put(address, values.get(address).add(order.getAmountHave()));
			}
		}

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
