package database.wallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import org.mapdb.Atomic.Var;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import controller.Controller;
import com.google.common.primitives.UnsignedBytes;

import core.account.Account;
import core.account.PublicKeyAccount;
import utils.ObserverMessage;

// UNCONFIRMED balances for accounts in owner wallet only
public class AccountMap extends Observable {

	private static final String ADDRESS_ASSETS = "address_assets";
	private static final String ADDRESSES = "addresses";
	private static final String ADDRESSES_NO = "addresses_nomer";

	private Map<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> assetsBalanceMap;
	private Set<byte[]> publickKeys;

	private Var<Long> licenseKeyVar;
	private Long licenseKey;
	// nom account from create. if <0 - not show
	private Map<String, Integer> acountsNoMap;

	//private List<Account> accounts;
	//private List<PublicKeyAccount> publickKeys;

	public AccountMap(DWSet dWSet, DB database)
	{
		//this.publickKeys = new ArrayList<PublicKeyAccount>();
		//OPEN MAP
		//this.publickKeys = database.getHashSet(ADDRESSES);
		this.publickKeys = database.createTreeSet(ADDRESSES)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.serializer(BTreeKeySerializer.BASIC)
				.makeOrGet();

		this.assetsBalanceMap = database.getTreeMap(ADDRESS_ASSETS);
		this.acountsNoMap = database.getTreeMap(ADDRESSES_NO);

		// LICENCE SIGNED
		this.licenseKeyVar = database.getAtomicVar("licenseKey");
		this.licenseKey = this.licenseKeyVar.get();

	}

	/*
	private void loadPublickKeys()
	{
		//RESET ACCOUNTS LIST
		this.publickKeys = new ArrayList<PublicKeyAccount>();

		synchronized(this.publickKeys)
		{

			for(Tuple2<String, Long> item: this.assetsBalanceMap.keySet())
			{
				//CREATE ACCOUNT FROM ADDRESS
				//Account account = new Account(item.a);

				//ADD TO LIST
				//this.publickKeys.add(account);
			}
		}
	}
	 */

	public void setLicenseKey(Long key)
	{

		this.licenseKey = key;
		this.licenseKeyVar.set(this.licenseKey);

	}

	public Long getLicenseKey()
	{
		return this.licenseKey;
	}


	public List<Account> getAccounts()
	{

		List<Account> accounts = new ArrayList<Account>();

		synchronized(this.publickKeys)
		{


			for(byte[] publickKey: this.publickKeys)
			{
				accounts.add( new PublicKeyAccount(publickKey));
			}
		}

		return accounts;
	}

	public List<PublicKeyAccount> getPublicKeyAccounts()
	{
		List<PublicKeyAccount> accounts = new ArrayList<PublicKeyAccount>();

		synchronized(this.publickKeys)
		{


			for(byte[] publickKey: this.publickKeys)
			{
				accounts.add( new PublicKeyAccount(publickKey));
			}
		}

		return accounts;
	}

	// collect account + assets in wallet
	public List<Tuple2<Account, Long>> getAccountsAssets()
	{

		List<Tuple2<Account, Long>> account_assets = new ArrayList<Tuple2<Account, Long>>();

		Collection<Tuple2<String, Long>> keys = this.assetsBalanceMap.keySet();

		//for(PublicKeyAccount publickKey: this.publickKeys)
		for (Tuple2<String, Long> key: keys) {
			account_assets.add(new Tuple2<Account, Long>(new Account(key.a), key.b));
		}

		return account_assets;
	}

	// collect address + assets in wallet
	public Set<Tuple2<String, Long>> getAddressessAssets()
	{

		Set<Tuple2<String, Long>> keys = this.assetsBalanceMap.keySet();

		return keys;
	}

	public boolean exists(String address)
	{
		//return this.assetsBalanceMap.containsKey(address);
		for(byte[] publickKey: this.publickKeys)
		{
			PublicKeyAccount account = new PublicKeyAccount(publickKey);
			if (account.getAddress().equals(address)) return true;
		}
		//return this.publickKeys.containsKey(address);
		return false;
	}

	public Account getAccount(String address)
	{

		return getPublicKeyAccount(address);
		/*
		synchronized(this.publickKeys)
		{
			for(PublicKeyAccount publickKey: this.publickKeys)
			{
				if(publickKey.getAddress().equals(address))
				{
					return publickKey;
				}
			}
		}

		return null;
		 */
	}
	public PublicKeyAccount getPublicKeyAccount(String address)
	{

		synchronized(this.publickKeys)
		{
			for(byte[] publickKeyBytes: this.publickKeys)
			{
				PublicKeyAccount publickKey = new PublicKeyAccount(publickKeyBytes);
				if(publickKey.getAddress().equals(address))
				{
					return publickKey;
				}
			}
		}

		return null;
	}

	private Tuple3<BigDecimal, BigDecimal, BigDecimal> getBalanceNull() {
		return new Tuple3<BigDecimal, BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
	}

	// change BALANCE - add or subtract amount by KEY + AMOUNT = TYPE
	public Tuple3<BigDecimal, BigDecimal, BigDecimal> changeBalance(String address, boolean subtract, long key, BigDecimal amount)
	{

		int type = core.account.Account.actionType(key, amount);
		long absKey;
		if (key > 0) {
			absKey = key;
		} else {
			absKey = -key;
		}

		Tuple2<String, Long> k = new Tuple2<String, Long>(address, absKey);

		if(!this.assetsBalanceMap.containsKey(k))
			return getBalanceNull();

		Tuple3<BigDecimal, BigDecimal, BigDecimal> balance = this.assetsBalanceMap.get(k);

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

		this.assetsBalanceMap.put(k, balance);
		return balance;
	}

	/*
	private BigDecimal getUnconfirmedBalance(String address, Long key)
	{

		int type = 1; // OWN
		if (key < 0) {
			type = 2; // RENT
			key = -key;
		}

		Tuple2<String, Long> k = new Tuple2<String, Long>(address, key);

		if(!this.assetsBalanceMap.containsKey(k))
			return BigDecimal.ZERO;

		Tuple3<BigDecimal, BigDecimal, BigDecimal> value = this.assetsBalanceMap.get(k);
		if (type == 1)
			return value.a;
		else if (type == 2)
			return value.b;
		else
			return value.c;
	}
	public BigDecimal getUnconfirmedBalance(Account account, Long key)
	{
		return getUnconfirmedBalance(account.getAddress(), key);
	}
	 */

	private Tuple3<BigDecimal, BigDecimal, BigDecimal> getBalance(String address, Long key)
	{

		if (key < 0) {
			key = -key;
		}

		Tuple2<String, Long> k = new Tuple2<String, Long>(address, key);

		if(!this.assetsBalanceMap.containsKey(k))
			return getBalanceNull();

		return this.assetsBalanceMap.get(k);
	}
	public Tuple3<BigDecimal, BigDecimal, BigDecimal>  getBalance(Account account, Long key)
	{
		return getBalance(account.getAddress(), key);
	}

	/*
	public void add(PublicKeyAccount account, long key)
	{
		this.assetsBalanceMap.put(new Tuple2<String, Long>(account.getAddress(), key),
				account.getConfirmedBalance(key));
	}
	 */

	// ADD AN PUBLIC KEY ACCOUNT in wallet
	public void add(PublicKeyAccount account)
	{
		/*
		if(this.publickKeys == null)
		{
			this.loadPublickKeys();
		}
		 */

		synchronized(this.publickKeys)
		{
			if(!this.publickKeys.contains(account.getPublicKey()))
			{
				this.publickKeys.add(account.getPublicKey());
				int n = Controller.getInstance().wallet.getAccountNonce();
				acountsNoMap.put(account.getAddress(),n);
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));

			}
		}
	}
	
	public int getAccountNo(String pubkey){
		
		return acountsNoMap.get(pubkey);
	}

	/*
	public void update(Account account, long key, Tuple3<BigDecimal, BigDecimal, BigDecimal> unconfirmedBalance)
	{
		this.assetsBalanceMap.put(new Tuple2<String, Long>(account.getAddress(), key), unconfirmedBalance);

		this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));

	}
	public void update(Account account, long key, BigDecimal unconfirmedBalance)
	{
		int type = 1; // OWN
		if (key < 0) {
			type = 2; // RENT
			key = -key;
		}

		Tuple3<BigDecimal, BigDecimal, BigDecimal> value;

		Tuple2<String, Long> k = new Tuple2<String, Long>(account.getAddress(), key);
		if(!this.assetsBalanceMap.containsKey(k)) {
			value =	new Tuple3<BigDecimal, BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO);
		} else {
			value = this.assetsBalanceMap.get(k);
		}

		if (type == 1)
			value = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(unconfirmedBalance, value.b, value.c);
		else if (type == 2)
			value = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(value.a, unconfirmedBalance, value.c);
		else
			value = new Tuple3<BigDecimal, BigDecimal, BigDecimal>(value.a, value.b, unconfirmedBalance);


		this.assetsBalanceMap.put(k, value);

		this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));

	}
	 */

	// delete all assets for this account
	public void delete(PublicKeyAccount account)
	{

		// TODO - its work?
		Map<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> keys = ((BTreeMap) this.assetsBalanceMap).subMap(
				//BTreeMap keys = ((BTreeMap) this.assetsBalanceMap).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI()));
		/*
		if(this.publickKeys == null)
		{
			this.loadPublickKeys();
		}
		 */

		synchronized(this.publickKeys)
		{
			//DELETE NAMES
			for(Tuple2<String, Long> key: keys.keySet())
			{

				this.assetsBalanceMap.remove(key);
			}

			this.publickKeys.remove(account.getPublicKey());

			this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_ACCOUNT_TYPE, account));
		}
	}


}
