package database.wallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import com.google.common.primitives.UnsignedBytes;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.item.ItemCls;
import utils.ObserverMessage;

// UNCONFIRMED balances for accounts in owner wallet only
public class AccountMap extends Observable {

	private static final String ADDRESS_ASSETS = "address_assets";
	private static final String ADDRESSES = "addresses";
	
	private Map<Tuple2<String, Long>, BigDecimal> assetsBalanceMap;
	private Set<byte[]> publickKeys;
	
	//private List<Account> accounts;
	//private List<PublicKeyAccount> publickKeys;
	
	public AccountMap(WalletDatabase walletDatabase, DB database) 
	{
		//this.publickKeys = new ArrayList<PublicKeyAccount>();
		//OPEN MAP
		//this.publickKeys = database.getHashSet(ADDRESSES);
		this.publickKeys = database.createTreeSet(ADDRESSES)
	    		.comparator(UnsignedBytes.lexicographicalComparator())
	    		.serializer(BTreeKeySerializer.BASIC)
	    		.makeOrGet();

		this.assetsBalanceMap = database.getTreeMap(ADDRESS_ASSETS);

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

	public List<Account> getAccounts()
	{
		
		List<Account> accounts = new ArrayList<Account>();

		synchronized(this.publickKeys)
		{

			
			for(byte[] publickKey: this.publickKeys)
			{
				accounts.add( (Account) new PublicKeyAccount(publickKey));
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

		return (Account)getPublicKeyAccount(address);
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

	private BigDecimal getUnconfirmedBalance(String address, Long key) 
	{
		
		Tuple2<String, Long> k = new Tuple2<String, Long>(address, key);
		if(this.assetsBalanceMap.containsKey(k))
		{
			return this.assetsBalanceMap.get(k);
		}
		
		return null;
	}
	// IF account+key not found in wallet - take from common confirmed balance map
	public BigDecimal getUnconfirmedBalance(Account account, Long key) 
	{		
		BigDecimal balance = getUnconfirmedBalance(account.getAddress(), key);
		if (balance == null) {
			balance = account.getConfirmedBalance(key);
			this.update(account, key, balance);
		}
		
		return balance;
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
				
				this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));
				
			}
		}
	}
	
	public void update(Account account, long key, BigDecimal unconfirmedBalance) 
	{		
		this.assetsBalanceMap.put(new Tuple2<String, Long>(account.getAddress(), key), unconfirmedBalance);	
		
		this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));
		
	}
	
	// dekete all assets for this account
	public void delete(PublicKeyAccount account)
	{

		// TODO - its work?
		Map<Tuple2<String, Long>, BigDecimal> keys = ((BTreeMap) this.assetsBalanceMap).subMap(
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
