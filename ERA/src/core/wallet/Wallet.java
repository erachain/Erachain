package core.wallet;
// 09/03

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import at.AT_Transaction;
import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import core.item.assets.Trade;
import core.item.imprints.ImprintCls;
import core.item.notes.NoteCls;
import core.item.persons.PersonCls;
import core.naming.Name;
import core.naming.NameSale;
import core.transaction.ArbitraryTransaction;
import core.transaction.BuyNameTransaction;
import core.transaction.CancelOrderTransaction;
import core.transaction.CancelSellNameTransaction;
import core.transaction.CreateOrderTransaction;
import core.transaction.CreatePollTransaction;
import core.transaction.Issue_ItemRecord;
//import core.transaction.IssueAssetTransaction;
//import core.transaction.IssueImprintRecord;
//import core.transaction.IssueNoteRecord;
//import core.transaction.IssuePersonRecord;
//import core.transaction.IssueUnionRecord;
import core.transaction.R_Send;
import core.transaction.R_SertifyPubKeys;
import core.transaction.RegisterNameTransaction;
import core.transaction.SellNameTransaction;
import core.transaction.Transaction;
import core.transaction.UpdateNameTransaction;
import core.transaction.VoteOnPollTransaction;
import core.voting.Poll;


//import core.wallet.ItemsFavorites;
import core.wallet.AssetsFavorites;
import core.wallet.NotesFavorites;
import core.wallet.PersonsFavorites;

import database.DBSet;
import database.wallet.SecureWalletDatabase;
import database.wallet.WalletDatabase;
import network.message.Message;
import settings.Settings;
import utils.ObserverMessage;
import utils.Pair;

public class Wallet extends Observable implements Observer
{
	public static final int STATUS_UNLOCKED = 1;
	public static final int STATUS_LOCKED = 0;
	
	private static final long RIGHTS_KEY = Transaction.RIGHTS_KEY;
	private static final long FEE_KEY = Transaction.FEE_KEY;
	public WalletDatabase database;
	private SecureWalletDatabase secureDatabase;
	
	private int secondsToUnlock = -1;
	private Timer lockTimer = new Timer();

	private int syncHeight;
	
	AssetsFavorites assetsFavorites; 
	NotesFavorites notesFavorites; 
	PersonsFavorites personsFavorites; 
	
	static Logger LOGGER = Logger.getLogger(Wallet.class.getName());

	//CONSTRUCTORS
	
	public Wallet()
	{
		this.syncHeight = -1;
		//CHECK IF EXISTS
		if(this.exists())
		{
			//OPEN WALLET
			this.database = new WalletDatabase();
			
			//ADD OBSERVER
		    Controller.getInstance().addObserver(this);
		    DBSet.getInstance().getCompletedOrderMap().addObserver(this);
		}
	}
	
	//GETTERS/SETTERS
	

	public int getSyncHeight()
	{
		return this.syncHeight;
	}

	
	public void initiateItemsFavorites()
	{
		if(this.assetsFavorites == null){
			this.assetsFavorites = new AssetsFavorites();
		}
		if(this.notesFavorites == null){
			this.notesFavorites = new NotesFavorites();
		}
		if(this.personsFavorites == null){
			this.personsFavorites = new PersonsFavorites();
		}
	}
	
	public void setSecondsToUnlock(int seconds)
	{
		this.secondsToUnlock = seconds;
	}

	public int getVersion()
	{
		return this.database.getVersion();
	}
	
	public boolean isUnlocked()
	{
		return this.secureDatabase != null;
	}
	
	public List<Account> getAccounts()
	{
		return this.database.getAccountMap().getAccounts();
	}
	public List<PublicKeyAccount> getPublicKeyAccounts()
	{
		return this.database.getAccountMap().getPublicKeyAccounts();
	}
	public List<Tuple2<Account, Long>> getAccountsAssets()
	{
		return this.database.getAccountMap().getAccountsAssets();
	}
	
	public boolean accountExists(String address)
	{
		return this.database.getAccountMap().exists(address);
	}
	
	public Account getAccount(String address)
	{
		return this.database.getAccountMap().getAccount(address);
	}
	
	public boolean isWalletDatabaseExisting()
	{
		return database != null;
	}
	
	//public BigDecimal getUnconfirmedBalance(String address, long key)
	//{
	//	return this.database.getAccountMap().getUnconfirmedBalance(address, key);
	//}
	/*
	public BigDecimal getUnconfirmedBalance(Account account, long key)
	{
		
		return this.database.getAccountMap().getUnconfirmedBalance(account, key);
	}
	*/
	public Tuple3<BigDecimal, BigDecimal, BigDecimal> getUnconfirmedBalance(Account account, long key)
	{
		
		return this.database.getAccountMap().getBalance(account, key);
	}
	
	public List<PrivateKeyAccount> getprivateKeyAccounts()
	{
		if(this.secureDatabase == null)
		{
			return new ArrayList<PrivateKeyAccount>();
		}
		
		return this.secureDatabase.getAccountSeedMap().getPrivateKeyAccounts();
	}
	
	public PrivateKeyAccount getPrivateKeyAccount(String address)
	{
		if(this.secureDatabase == null)
		{
			return null;
		}
		
		return this.secureDatabase.getAccountSeedMap().getPrivateKeyAccount(address);
	}

	public PublicKeyAccount getPublicKeyAccount(String address)
	{
		if(this.database == null)
		{
			return null;
		}
		
		return this.database.getAccountMap().getPublicKeyAccount(address);
	}

	public boolean exists()
	{
		return WalletDatabase.exists();
	}
	
	public List<Pair<Account, Transaction>> getLastTransactions(int limit)
	{
		if(!this.exists())
		{
			new ArrayList<Pair<Account, Transaction>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getTransactionMap().get(accounts, limit);
	}
	
	public List<Transaction> getLastTransactions(Account account, int limit)
	{
		if(!this.exists())
		{
			return new ArrayList<Transaction>();
		}

		return this.database.getTransactionMap().get(account, limit);
	}
	
	public List<Pair<Account, Block>> getLastBlocks()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, Block>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getBlockMap().get(accounts);
	}

	public List<Block> getLastBlocks(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<Block>();
		}

		return this.database.getBlockMap().get(account);
	}
		
	public List<Pair<Account, Name>> getNames()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, Name>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getNameMap().get(accounts);
	}
	
	public List<Name> getNames(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<Name>();
		}

		return this.database.getNameMap().get(account);
	}
	
	public List<Pair<Account, NameSale>> getNameSales()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, NameSale>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getNameSaleMap().get(accounts);
	}
	
	public List<NameSale> getNameSales(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<NameSale>();
		}

		return this.database.getNameSaleMap().get(account);
	}
	
	public List<Pair<Account, Poll>> getPolls()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, Poll>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getPollMap().get(accounts);
	}
	
	public List<Poll> getPolls(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<Poll>();
		}

		return this.database.getPollMap().get(account);
	}
	

	public void addItemFavorite(ItemCls item)
	{
		if(!this.exists())
		{
			return;
		}
		
		this.database.addItemToFavorite(item);
	}

	/*
	public void replaseAssetFavorite()
	{
		if(!this.exists())
		{
			return;
		}
		
		if(this.assetsFavorites != null) {
			this.database.getAssetFavoritesSet().replace(this.assetsFavorites.getKeys());	
		}
	}
	public void replaseNoteFavorite()
	{
		if(!this.exists())
		{
			return;
		}
		
		if(this.notesFavorites != null) {
			this.database.getNoteFavoritesSet().replace(this.notesFavorites.getKeys());	
		}
	}
	public void replasePersonFavorite()
	{
		if(!this.exists())
		{
			return;
		}
		
		if(this.personsFavorites != null) {
			this.database.getPersonFavoritesSet().replace(this.personsFavorites.getKeys());	
		}
	}
	*/
	// тут нужно понять где это используется
	public void replaseFavoriteItems(int type)
	{
		if(!this.exists())
		{
			return;
		}
		
		switch (type) {
			case ItemCls.ASSET_TYPE:
				if(this.assetsFavorites != null) {
					this.database.getAssetFavoritesSet().replace(this.assetsFavorites.getKeys());
				}
			case ItemCls.NOTE_TYPE:
				if(this.notesFavorites != null) {
					this.database.getNoteFavoritesSet().replace(this.notesFavorites.getKeys());
				}
			case ItemCls.PERSON_TYPE:
				if(this.personsFavorites != null) {
					this.database.getPersonFavoritesSet().replace(this.personsFavorites.getKeys());
				}
		}
	}
	
	/*
	public void removeAssetFavorite(AssetCls asset)
	{
		if(!this.exists())
		{
			return;
		}
		
		this.database.getAssetFavoritesSet().delete(asset.getKey());
	}
	public void removeNoteFavorite(NoteCls note)
	{
		if(!this.exists())
		{
			return;
		}
		
		this.database.getNoteFavoritesSet().delete(note.getKey());
	}
	*/
	public void removeItemFavorite(ItemCls item)
	{
		if(!this.exists())
		{
			return;
		}
		
		this.database.removeItemFromFavorite(item);
	}
	
	/*
	public boolean isAssetFavorite(AssetCls asset)
	{
		if(!this.exists())
		{
			return false;
		}
		
		return this.database.getAssetFavoritesSet().contains(asset.getKey());
	}
	public boolean isNoteFavorite(NoteCls note)
	{
		if(!this.exists())
		{
			return false;
		}
		
		return this.database.getNoteFavoritesSet().contains(note.getKey());
	}
	*/
	public boolean isItemFavorite(ItemCls item)
	{
		if(!this.exists())
		{
			return false;
		}
		
		return this.database.isItemFavorite(item);
	}

	//CREATE
	
	public boolean create(byte[] seed, String password, int depth, boolean synchronize)
	{
		//OPEN WALLET
		WalletDatabase database = new WalletDatabase();
		
	    //OPEN SECURE WALLET
		SecureWalletDatabase secureDatabase = new SecureWalletDatabase(password);
	    
	    //CREATE
	    return this.create(database, secureDatabase, seed, depth, synchronize);
	}
	
	public boolean create(WalletDatabase database, SecureWalletDatabase secureDatabase, byte[] seed, int depth, boolean synchronize)
	{
		//CREATE WALLET
		this.database = database;
	    
	    //CREATE SECURE WALLET
	    this.secureDatabase = secureDatabase;
	    
	    //ADD VERSION
	    this.database.setVersion(1);
	    
	    //SET LICENSE KEY
	    this.setLicenseKey(Controller.LICENSE_KEY);

	    //ADD SEED
	    this.secureDatabase.setSeed(seed);
	    
	    //ADD NONCE
	    this.secureDatabase.setNonce(0);
	    
	    //CREATE ACCOUNTS
	    for(int i=1; i<=depth; i++)
	    {
	    	this.generateNewAccount();
	    }
	    
	    //SCAN TRANSACTIONS
	    if(synchronize)
	    {
	    	this.synchronize();
	    }
	    
	    //COMMIT
	    this.commit();
	    
	    //ADD OBSERVER
	    Controller.getInstance().addObserver(this);
	    DBSet.getInstance().getCompletedOrderMap().addObserver(this);
	    
	    this.initiateItemsFavorites();
	    
	    // SOME 
	    //Account initAccount = this.getAccounts().get(0);
	    //initAccount.setConfirmedBalance(Transaction.AssetCls.DILE_KEY, BigDecimal.valueOf(0.00001).setScale(8));

	    
	    return true;
	}
	
	public String generateNewAccount() 
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return "";
		}
	    
	    //READ SEED
	    byte[] seed = this.secureDatabase.getSeed();
	    
	    //READ NONCE
	    int nonce = this.secureDatabase.getAndIncrementNonce();
	    
	    //GENERATE ACCOUNT SEED
	    byte[] accountSeed = generateAccountSeed(seed, nonce);
	    PrivateKeyAccount account = new PrivateKeyAccount(accountSeed);
	    
	    //CHECK IF ACCOUNT ALREADY EXISTS
	    if(!this.accountExists(account.getAddress()))
	    {	    
	    	//ADD TO DATABASE
		    this.secureDatabase.getAccountSeedMap().add(account);
		    this.database.getAccountMap().add(account);
		    LOGGER.info("Added account #" + nonce);
		    
		    this.secureDatabase.commit();
		    this.database.commit();
		    
		    //NOTIFY
		    this.setChanged();
		    this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));
	    }
	    
	    return account.getAddress();
	}
	
	public static byte[] generateAccountSeed(byte[] seed, int nonce) 
	{		
		byte[] nonceBytes = Ints.toByteArray(nonce);
		byte[] accountSeed = Bytes.concat(nonceBytes, seed, nonceBytes);
		return Crypto.getInstance().doubleDigest(accountSeed);		
	}
	
	//DELETE
	
	public boolean deleteAccount(PrivateKeyAccount account)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return false;
		}
		
		//DELETE FROM DATABASE
		this.database.delete(account);
		this.secureDatabase.delete(account);
		
		// SAVE TO DISK
	    this.database.commit();
		this.secureDatabase.commit();
	    
		//NOTIFY
	    this.setChanged();
	    this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_ACCOUNT_TYPE, account));
	    
	    //RETURN
	    return true;
	}
	
	//SYNCRHONIZE

	// UPDATE all accounts for all assets unconfirmed balance
	public void update_account_assets()
	{
		List<Tuple2<Account, Long>> accounts_assets = this.getAccountsAssets();

		synchronized(accounts_assets)
		{
			for(Tuple2<Account, Long> account_asset: accounts_assets)
			{
				this.database.getAccountMap().changeBalance(
						account_asset.a.getAddress(), false, account_asset.b, BigDecimal.ZERO);
			}
		}

	}
	
	public void synchronize()
	{
		if(Controller.getInstance().isProcessingWalletSynchronize()) {
			return;
		}

		// here ICREATOR
		Controller.getInstance().setNeedSync(false);
		Controller.getInstance().setProcessingWalletSynchronize(true);

		LOGGER.info("Resetted maps");
				
		//RESET MAPS
		this.database.getTransactionMap().reset();
		this.database.getBlockMap().reset();
		this.database.getNameMap().reset();
		this.database.getNameSaleMap().reset();
		this.database.getPollMap().reset();
		this.database.getAssetMap().reset();
		this.database.getImprintMap().reset();
		this.database.getNoteMap().reset();
		this.database.getPersonMap().reset();
		this.database.getStatusMap().reset();
		this.database.getUnionMap().reset();
		this.database.getOrderMap().reset();
		
		//REPROCESS BLOCKS
		Block block = new GenesisBlock();
		//this.database.setLastBlockSignature(new byte[]{1,1,1,1,1,1,1,1});
		this.database.setLastBlockSignature(block.getSignature());
		DBSet dbSet = DBSet.getInstance();
		int height = 1;
		
		try{
			this.syncHeight = 1;
			do
			{
				//UPDATE
				this.update(this, new ObserverMessage(ObserverMessage.ADD_BLOCK_TYPE, block));
				height = block.getHeight(dbSet);
				
				if(height % (5 * BlockChain.MAX_ORPHAN) == 0) 
				{
					this.syncHeight = height;
					
					Controller.getInstance().walletSyncStatusUpdate(this.syncHeight);
					
					LOGGER.info("Synchronize wallet: " + this.syncHeight);
					this.database.commit();
				}
				
				//LOAD NEXT
				block = block.getChild(dbSet);
			}
			while(block != null);
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(), e);

		} finally {
			
			Controller.getInstance().setProcessingWalletSynchronize(false);
			this.database.commit();
			// icreator this.syncHeight = -1;
			this.syncHeight = height;
			Controller.getInstance().walletSyncStatusUpdate(height);
		}
		
		
		//RESET UNCONFIRMED BALANCE for accounts + assets
		LOGGER.info("Resetted balances");
		update_account_assets();
		

		/// ic Controller.getInstance().walletSyncStatusUpdate(-1);
		////Controller.getInstance().walletSyncStatusUpdate(this.syncHeight);
		
		
		//NOW IF NOT SYNCHRONIZED SET STATUS
		//CHECK IF WE ARE UPTODATE
		if(!Controller.getInstance().isUpToDate())
		{
			// NOTIFY
			Controller.getInstance().notifyObservers(new ObserverMessage(
					ObserverMessage.NETWORK_STATUS, Controller.STATUS_SYNCHRONIZING));
		}
		
		//SET LAST BLOCK
		
		/*//SCAN TRANSACTIONS
		Map<Account, List<Transaction>> transactions;
		synchronized(accounts)
		{
			transactions = Controller.getInstance().scanTransactions(accounts);
		}
		
		//DELETE TRANSACTIONS
		this.database.getTransactionMap().deleteAll(accounts);
		
		//ADD TRANSACTIONS
		this.database.getTransactionMap().addAll(transactions);
	    	
		//TODO SCAN UNCONFIRMED TRANSACTIONS    
	    	    
	    //SCAN BLOCKS
	    Map<Account, List<Block>> blocks;
	    synchronized(accounts)
		{
	    	blocks = Controller.getInstance().scanBlocks(accounts);
		}
	    
	    //DELETE BLOCKS
	  	this.database.getBlockMap().deleteAll(accounts);
	  	
	  	//ADD BLOCKS
	  	this.database.getBlockMap().addAll(blocks);
	    
	    //SCAN NAMES
	    Map<Account, List<Name>> names;
	    synchronized(accounts)
		{
	    	names = Controller.getInstance().scanNames(accounts);
		}
	    
	    //DELETE NAMES
	  	this.database.getNameMap().deleteAll(accounts);
	  	
	  	//ADD NAMES
	  	this.database.getNameMap().addAll(names);
	  	
	  	//TODO SCAN UNCONFIRMED NAMES
	    
	  	//SCAN NAMESALES
	    Map<Account, List<NameSale>> nameSales;
	    synchronized(accounts)
		{
	    	nameSales = Controller.getInstance().scanNameSales(accounts);
		}
	    
	    //DELETE NAMESALES
	  	this.database.getNameSaleMap().deleteAll(accounts);
	  	
	  	//ADD NAMES
	  	this.database.getNameSaleMap().addAll(nameSales);
	  	
	  	//SCAN POLLS
	  	Map<Account, List<Poll>> polls;
	  	synchronized(accounts)
	  	{
	  		polls = Controller.getInstance().scanPolls(accounts);
	  	}
	  	
	  	//DELETE POLLS
	  	this.database.getPollMap().deleteAll(accounts);
	  	
	  	//ADD POLLS
	  	this.database.getPollMap().addAll(polls);
	  	
	  	//SCAN ASSETS
		Map<Account, List<Asset>> assets;
	  	synchronized(accounts)
	  	{
	  		assets = Controller.getInstance().scanAssets(accounts);
	  	}
	  	
	  	//DELETE ASSETS
	  	this.database.getAssetMap().deleteAll(accounts);
	  	
	  	//ADD ASSETS
	  	this.database.getAssetMap().addAll(assets);

	  	//SCAN ORDERS
	  	Map<Account, List<Order>> orders;
	  	synchronized(accounts)
	  	{
	  		orders = Controller.getInstance().scanOrders(accounts);
	  	}
	  	
	  	//DELETE ASSETS
	  	this.database.getOrderMap().deleteAll(accounts);
	  	
	  	//ADD ASSETS
	  	this.database.getOrderMap().addAll(orders);

	  	//SCAN NOTES --------
	  	 * notes!
		Map<Account, List<NoteCls>> notes;
	  	synchronized(accounts)
	  	{
	  		assets = Controller.getInstance().scanAssets(accounts);
	  	}
	  	
	  	//DELETE NOTES
	  	this.database.getAssetMap().deleteAll(accounts);
	  	
	  	//ADD ASSETS
	  	this.database.getAssetMap().addAll(assets);

	  	//SCAN ORDERS
	  	Map<Account, List<Order>> orders;
	  	synchronized(accounts)
	  	{
	  		orders = Controller.getInstance().scanOrders(accounts);
	  	}
	  	
	  	//DELETE ASSETS
	  	this.database.getOrderMap().deleteAll(accounts);
	  	
	  	//ADD ASSETS ---- notes
	  	this.database.getOrderMap().addAll(orders);

	  	//SET LAST BLOCK
	  	this.database.setLastBlockSignature(Controller.getInstance().getLastBlock().getSignature());*/
	}
	
	//UNLOCK
	
	public boolean unlock(String password)
	{
		if(this.isUnlocked())
		{
			return true;
		}
		
		//TRY TO UNLOCK
		try
		{
			SecureWalletDatabase secureDatabase = new SecureWalletDatabase(password);
			return this.unlock(secureDatabase);
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	public boolean unlock(SecureWalletDatabase secureDatabase)
	{
		this.secureDatabase = secureDatabase;
		
		//NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_STATUS, STATUS_UNLOCKED));
		
		if(this.secondsToUnlock > 0)
		{
			this.lockTimer.cancel(); 
			this.lockTimer = new Timer();
			
			TimerTask action = new TimerTask() {
		        public void run() {
		            lock();
		        }
		    };
		    
		    this.lockTimer.schedule(action, this.secondsToUnlock*1000);
		}
		return true;
	}
	
	public boolean lock()
	{
		if(!this.isUnlocked())
		{
			return true;
		}
		
		//CLOSE
		this.secureDatabase.close();
		this.secureDatabase = null;
		
		//NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_STATUS, STATUS_LOCKED));
		
		this.secondsToUnlock = -1;
		this.lockTimer.cancel(); 
		
		//LOCK SUCCESSFULL
		return true;
	}

	//IMPORT/EXPORT
	
	public String importAccountSeed(byte[] accountSeed)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return "";
		}
		
		//CHECK LENGTH
		if(accountSeed.length != Crypto.HASH_LENGTH)
		{
			return "";
		}
		
		//CREATE ACCOUNT
		PrivateKeyAccount account = new PrivateKeyAccount(accountSeed);
		
		//CHECK IF ACCOUNT ALREADY EXISTS
	    if(!this.accountExists(account.getAddress()))
	    {	
	    	//ADD TO DATABASE
		    this.secureDatabase.getAccountSeedMap().add(account);
		    this.database.getAccountMap().add(account);
		    
		    // SAVE TO DISK
		    this.secureDatabase.commit();
		    this.database.commit();
			
		    //SYNCHRONIZE
		    this.synchronize();
		    
		    //NOTIFY
		    this.setChanged();
		    this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));
		    
		    //RETURN
		    return account.getAddress();
	    }
	    
	    return "";
	}

	public byte[] exportAccountSeed(String address)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return null;
		}
		
		PrivateKeyAccount account = this.getPrivateKeyAccount(address);
		
		if(account == null)
		{
			return null;
		}
		
		return account.getSeed();	
	}
	
	public byte[] exportSeed()
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return null;
		}
		
		return this.secureDatabase.getSeed();
	}
	

	//OBSERVER
	
	@Override
	public void addObserver(Observer o)
	{
		super.addObserver(o);
		
		//REGISTER ON ACCOUNTS
		this.database.getAccountMap().addObserver(o);
		
		//REGISTER ON TRANSACTIONS
		this.database.getTransactionMap().addObserver(o);
		
		//REGISTER ON BLOCKS
		this.database.getBlockMap().addObserver(o);
		
		//REGISTER ON NAMES
		this.database.getNameMap().addObserver(o);
		
		//REGISTER ON NAME SALES
		this.database.getNameSaleMap().addObserver(o);
		
		//REGISTER ON POLLS
		this.database.getPollMap().addObserver(o);
		
		//REGISTER ON ASSETS
		this.database.getAssetMap().addObserver(o);

		//REGISTER ON IMPRINTS
		this.database.getImprintMap().addObserver(o);

		//REGISTER ON NOTES
		this.database.getNoteMap().addObserver(o);

		//REGISTER ON PERSONS
		this.database.getPersonMap().addObserver(o);

		//REGISTER ON STATUS
		this.database.getStatusMap().addObserver(o);

		//REGISTER ON UNION
		this.database.getUnionMap().addObserver(o);

		//REGISTER ON ORDERS
		this.database.getOrderMap().addObserver(o);
		
		//REGISTER ON ASSET FAVORITES
		this.database.getAssetFavoritesSet().addObserver(o);
		
		//REGISTER ON NOTE FAVORITES
		this.database.getNoteFavoritesSet().addObserver(o);

		//REGISTER ON PERSON FAVORITES
		this.database.getPersonFavoritesSet().addObserver(o);

		//REGISTER ON STATUS FAVORITES
		this.database.getStatusFavoritesSet().addObserver(o);

		//REGISTER ON UNION FAVORITES
		this.database.getUnionFavoritesSet().addObserver(o);

		//SEND STATUS
		int status = STATUS_LOCKED;
		if(this.isUnlocked())
		{
			status = STATUS_UNLOCKED;
		}
		
		o.update(this, new ObserverMessage(ObserverMessage.WALLET_STATUS, status));
	}

	private void deal_transaction(Account account, Transaction transaction, boolean asOrphan)
	{
		//UPDATE UNCONFIRMED BALANCE for ASSET
		// TODO: fee doubled?
		long key = transaction.getAssetKey();
		long absKey = key<0?-key:key;
		String address = account.getAddress();
		
		BigDecimal fee = transaction.getFee(account);
		if (absKey != 0)
		{
			// ASSET TRANSFERED + FEE
			BigDecimal amount = transaction.getAmount(account);

			if (fee.compareTo(BigDecimal.ZERO) != 0)
			{
				if (absKey == FEE_KEY)
				{
					amount = amount.subtract(fee);
				}
			}
			this.database.getAccountMap().changeBalance(address, !asOrphan, key, amount);
		} else {
			// ONLY FEE
			if (fee.compareTo(BigDecimal.ZERO) != 0)
			{
				this.database.getAccountMap().changeBalance(address, !asOrphan, FEE_KEY, fee);
			}
		}

	}
	private void processTransaction(Transaction transaction)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();	
		synchronized(accounts)
		{		
			for(Account account: accounts)
			{
				//CHECK IF INVOLVED
				if(transaction.isInvolved(account))
				{
					//ADD TO ACCOUNT TRANSACTIONS
					if(!this.database.getTransactionMap().add(account, transaction))
					{					
						//UPDATE UNCONFIRMED BALANCE for ASSET
						deal_transaction(account, transaction, false);
					}
				}
			}
		}
	}
	
	private void processATTransaction( Tuple2< Tuple2< Integer, Integer >, AT_Transaction > atTx )
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();	
		synchronized(accounts)
		{		
			for(Account account: accounts)
			{
				//CHECK IF INVOLVED
				//if(atTx.b.getRecipient().equalsIgnoreCase( account.getAddress() ))
				if(atTx.b.getRecipient() == account.getAddress() )
				{				
						this.database.getAccountMap().changeBalance(
								account.getAddress(), false, atTx.b.getKey(), BigDecimal.valueOf(atTx.b.getAmount(),8));
					
				}
			}
		}
	}
	
	private void orphanTransaction(Transaction transaction)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		///FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		
		synchronized(accounts)
		{		
			for(Account account: accounts)
			{
				//CHECK IF INVOLVED
				if(transaction.isInvolved(account))
				{
					//DELETE FROM ACCOUNT TRANSACTIONS
					this.database.getTransactionMap().delete(account, transaction);
					
					//UPDATE UNCONFIRMED BALANCE
					deal_transaction(account, transaction, true);
				}
			}
		}
	}
	
	private void orphanATTransaction(Tuple2<Tuple2<Integer,Integer>, AT_Transaction> atTx)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();	
		synchronized(accounts)
		{		
			for(Account account: accounts)
			{
				//CHECK IF INVOLVED
				if(atTx.b.getRecipient().equalsIgnoreCase( account.getAddress() ))
				{				
					this.database.getAccountMap().changeBalance(
							account.getAddress(), true, atTx.b.getKey(), BigDecimal.valueOf(atTx.b.getAmount(),8));
				}
			}
		}
	}

	private void processBlock(Block block)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE NEED TO RESYNC
		byte[] lastBlockSignature = this.database.getLastBlockSignature();
		if(lastBlockSignature == null || !Arrays.equals(lastBlockSignature, block.getReference()))
		{
			LOGGER.info("Wallet not synchronized with current blockchain: synchronizing wallet.");
			this.synchronize();
		}
		
		//SET AS LAST BLOCK
		this.database.setLastBlockSignature(block.getSignature());

		Account blockGenerator = block.getCreator();
		String blockGeneratorStr = blockGenerator.getAddress(); 

		//CHECK IF WE ARE GENERATOR
		if(this.accountExists(blockGeneratorStr))
		{
			//ADD BLOCK
			this.database.getBlockMap().add(block);
				
			//KEEP TRACK OF UNCONFIRMED BALANCE
			//PROCESS FEE
			BigDecimal blockFee = block.getTotalFeeForProcess();
			BigDecimal blockTotalFee = block.getTotalFee(DBSet.getInstance());

			if (blockFee.compareTo(blockTotalFee) < 0) {
				
				// find rich account
				String rich = Account.getRich(Transaction.FEE_KEY);
				if (!rich.equals(blockGeneratorStr)) {

					blockFee = blockTotalFee;
					
					if(this.accountExists(rich)) {

						BigDecimal bonus_fee = blockTotalFee.subtract(blockFee);
						Account richAccount = new Account(rich);	
						this.database.getAccountMap().changeBalance(
								richAccount.getAddress(), true, FEE_KEY, bonus_fee.divide(new BigDecimal(2)));
					}
				}
			}
			
			this.database.getAccountMap().changeBalance(
					blockGenerator.getAddress(), false, FEE_KEY, blockFee);
		}
	}

	private void orphanBlock(Block block)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		Account blockGenerator = block.getCreator();
		String blockGeneratorStr = blockGenerator.getAddress(); 

		//CHECK IF WE ARE GENERATOR
		if(this.accountExists(blockGeneratorStr))
		{
			//DELETE BLOCK
			this.database.getBlockMap().delete(block);
			
			//KEEP TRACK OF UNCONFIRMED BALANCE
			BigDecimal blockFee = block.getTotalFeeForProcess();
			BigDecimal blockTotalFee = block.getTotalFee(DBSet.getInstance());

			if (blockFee.compareTo(blockTotalFee) < 0) {
				
				// find rich account
				String rich = Account.getRich(Transaction.FEE_KEY);
				if (!rich.equals(blockGeneratorStr)) {

					blockFee = blockTotalFee;
					
					if(this.accountExists(rich)) {

						BigDecimal bonus_fee = blockTotalFee.subtract(blockFee);
						Account richAccount = new Account(rich);
						this.database.getAccountMap().changeBalance(
								richAccount.getAddress(), false, FEE_KEY, bonus_fee.divide(new BigDecimal(2)));
					}
				}
			}

			this.database.getAccountMap().changeBalance(
					blockGenerator.getAddress(), true, FEE_KEY, blockFee);
		}
	}
	
	private void processNameRegistration(RegisterNameTransaction nameRegistration)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(nameRegistration.getName().getOwner().getAddress()))
		{
			//ADD NAME
			this.database.getNameMap().add(nameRegistration.getName());
		}
	}
	
	private void orphanNameRegistration(RegisterNameTransaction nameRegistration)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(nameRegistration.getName().getOwner().getAddress()))
		{
			//DELETE NAME
			this.database.getNameMap().delete(nameRegistration.getName());
		}
	}
	
	private void processPollCreation(CreatePollTransaction pollCreation)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(pollCreation.getPoll().getCreator().getAddress()))
		{
			//ADD POLL
			this.database.getPollMap().add(pollCreation.getPoll());
		}
	}
	
	private void orphanPollCreation(CreatePollTransaction pollCreation)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(pollCreation.getPoll().getCreator().getAddress()))
		{
			//DELETE POLL
			this.database.getPollMap().delete(pollCreation.getPoll());
		}
	}

	private void processPollVote(VoteOnPollTransaction pollVote)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		Poll poll = DBSet.getInstance().getPollMap().get(pollVote.getPoll());
		if(this.accountExists(poll.getCreator().getAddress()))
		{
			//UPDATE POLL
			this.database.getPollMap().add(poll);
		}
	}
	
	private void orphanPollVote(VoteOnPollTransaction pollVote)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//CHECK IF WE ARE OWNER
		Poll poll = DBSet.getInstance().getPollMap().get(pollVote.getPoll());
		if(this.accountExists(poll.getCreator().getAddress()))
		{
			//UPDATE POLL
			this.database.getPollMap().add(poll);
		}
	}	
	
	private void processNameUpdate(UpdateNameTransaction nameUpdate)
	{
		//CHECK IF WE ARE OWNER
		if(this.accountExists(nameUpdate.getCreator().getAddress()))
		{
			//CHECK IF OWNER CHANGED
			if(!nameUpdate.getCreator().getAddress().equals(nameUpdate.getName().getOwner().getAddress()))
			{
				//DELETE PREVIOUS NAME
				Name name = DBSet.getInstance().getUpdateNameMap().get(nameUpdate);
				this.database.getNameMap().delete(nameUpdate.getCreator(), name);
			}
		}
		
		//CHECK IF WE ARE NEW OWNER
		if(this.accountExists(nameUpdate.getName().getOwner().getAddress()))
		{
			//ADD NAME
			this.database.getNameMap().add(nameUpdate.getName());
		}
	}
	
	private void orphanNameUpdate(UpdateNameTransaction nameUpdate)
	{
		//CHECK IF WE WERE OWNER
		if(this.accountExists(nameUpdate.getCreator().getAddress()))
		{
			//CHECK IF OWNER WAS CHANGED
			if(!nameUpdate.getCreator().getAddress().equals(nameUpdate.getName().getOwner().getAddress()))
			{
				//ADD PREVIOUS  NAME
				Name name = DBSet.getInstance().getNameMap().get(nameUpdate.getName().getName());
				this.database.getNameMap().add(name);

			}
		}
		
		//CHECK IF WE WERE NEW OWNER
		if(this.accountExists(nameUpdate.getName().getOwner().getAddress()))
		{
			//ADD NAME
			this.database.getNameMap().delete(nameUpdate.getName());
		}
	}
	
	private void processNameSale(SellNameTransaction nameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(nameSaleTransaction.getNameSale().getName().getOwner().getAddress()))
		{
			//ADD TO DATABASE
			this.database.getNameSaleMap().add(nameSaleTransaction.getNameSale());
		}
	}
	
	private void orphanNameSale(SellNameTransaction nameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(nameSaleTransaction.getNameSale().getName().getOwner().getAddress()))
		{
			//REMOVE FROM DATABASE
			this.database.getNameSaleMap().delete(nameSaleTransaction.getNameSale());
		}
	}

	private void processCancelNameSale(CancelSellNameTransaction cancelNameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(cancelNameSaleTransaction.getCreator().getAddress()))
		{
			//REMOVE FROM DATABASE
			BigDecimal amount = DBSet.getInstance().getCancelSellNameMap().get(cancelNameSaleTransaction);
			NameSale nameSale = new NameSale(cancelNameSaleTransaction.getName(), amount);
			this.database.getNameSaleMap().delete(nameSale);
		}
	}
	
	private void orphanCancelNameSale(CancelSellNameTransaction cancelNameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(cancelNameSaleTransaction.getCreator().getAddress()))
		{
			//ADD TO DATABASE
			NameSale nameSale = DBSet.getInstance().getNameExchangeMap().getNameSale(cancelNameSaleTransaction.getName());
			this.database.getNameSaleMap().add(nameSale);
		}
	}
	
	private void processNamePurchase(BuyNameTransaction namePurchase)
	{
		//CHECK IF WE ARE BUYER
		if(this.accountExists(namePurchase.getCreator().getAddress()))
		{
			//ADD NAME
			Name name = DBSet.getInstance().getNameMap().get(namePurchase.getNameSale().getKey());
			this.database.getNameMap().add(name);
		}
		
		//CHECK IF WE ARE SELLER
		Account seller = namePurchase.getSeller();
		if(this.accountExists(seller.getAddress()))
		{
			//DELETE NAMESALE
			this.database.getNameSaleMap().delete(seller, namePurchase.getNameSale());
			
			//DELETE NAME
			Name name = DBSet.getInstance().getNameMap().get(namePurchase.getNameSale().getKey());
			name.setOwner(seller);
			this.database.getNameMap().delete(seller, name);
		}
	}
	
	private void orphanNamePurchase(BuyNameTransaction namePurchase)
	{
		//CHECK IF WE WERE BUYER
		if(this.accountExists(namePurchase.getCreator().getAddress()))
		{
			//DELETE NAME
			Name name = namePurchase.getNameSale().getName();
			name.setOwner(namePurchase.getCreator());
			this.database.getNameMap().delete(namePurchase.getCreator(), name);
		}
		
		//CHECK IF WE WERE SELLER
		Account seller = namePurchase.getSeller();
		if(this.accountExists(seller.getAddress()))
		{
			//ADD NAMESALE
			this.database.getNameSaleMap().add(namePurchase.getNameSale());
			
			//ADD NAME
			Name name = namePurchase.getNameSale().getName();
			this.database.getNameMap().add(name);
		}
	}
	
	private void processItemIssue(Issue_ItemRecord issueItem)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		ItemCls item = issueItem.getItem();
		//item.resolveKey(DBSet.getInstance());
		Account creator = item.getOwner();
		if (creator == null) return;
		
		if(this.accountExists(creator.getAddress()))
		{
			//ADD ASSET
			this.database.getItemMap(item).add(creator.getAddress(), issueItem.getSignature(), item);
		}
	}
	
	private void orphanItemIssue(Issue_ItemRecord issueItem)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		ItemCls item = issueItem.getItem();
		Account creator = item.getOwner();
		if (creator == null) return;
		
		if(this.accountExists(creator.getAddress()))
		{
			//DELETE ASSET
			this.database.getItemMap(item).delete(creator.getAddress(), issueItem.getSignature());
		}
	}

	private void processSertifyPerson(R_SertifyPubKeys sertifyPubKeys)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		Account creator = sertifyPubKeys.getCreator();
		if (creator == null) return;
		
		DBSet db = DBSet.getInstance();
		boolean personalized = false;
		for (Account pkAccount: sertifyPubKeys.getSertifiedPublicKeys()) {
			if (pkAccount.getPersonDuration(db) != null) {
				personalized = true;
				break;
			}
		}

		if (!personalized) {
			// IT IS NOT VOUCHED PERSON

			// FIND person
			ItemCls person = db.getItemPersonMap().get(sertifyPubKeys.getKey());
			if (person != null) {
				// FIND issue record
				Transaction transPersonIssue = db.getTransactionFinalMap().get(db.getTransactionFinalMapSigns()
						.get(person.getReference()));
				// GET FEE from that record
				transPersonIssue.calcFee(); // RECALC FEE if from DB
				BigDecimal issueFEE_BD = transPersonIssue.getFee();
				//long issueFEE = transPersonIssue.getFeeLong() + BlockChain.GIFTED_COMPU_AMOUNT;
				//BigDecimal issueFEE_BD = BigDecimal.valueOf(issueFEE, BlockChain.FEE_SCALE);
				BigDecimal issueGIFT_FEE_BD = BigDecimal.valueOf(BlockChain.GIFTED_COMPU_AMOUNT, BlockChain.FEE_SCALE);
	
				// GIFTs
				if(this.accountExists(creator.getAddress()))
				{
					this.database.getAccountMap().changeBalance(
							creator.getAddress(), false, FEE_KEY, issueGIFT_FEE_BD);
				}
				
				PublicKeyAccount pkAccount = sertifyPubKeys.getSertifiedPublicKeys().get(0);
				if(this.accountExists(pkAccount.getAddress())) 
				{
					this.database.getAccountMap().changeBalance(
							pkAccount.getAddress(), false, FEE_KEY, issueFEE_BD);
				}
			}
		}
	}
	
	private void orphanSertifyPerson(R_SertifyPubKeys sertifyPubKeys)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		Account creator = sertifyPubKeys.getCreator();
		if (creator == null) return;

		// GIFTs
		DBSet db = DBSet.getInstance();
		boolean personalized = false;
		for (Account pkAccount: sertifyPubKeys.getSertifiedPublicKeys()) {
			if (pkAccount.getPersonDuration(db) != null) {
				personalized = true;
				break;
			}
		}

		if (!personalized) {
			// IT IS NOT VOUCHED PERSON

			// FIND person
			ItemCls person = db.getItemPersonMap().get(sertifyPubKeys.getKey());
			// FIND issue record
			Transaction transPersonIssue = db.getTransactionFinalMap().get(db.getTransactionFinalMapSigns()
					.get(person.getReference()));
			// GET FEE from that record
			transPersonIssue.calcFee(); // RECALC FEE if from DB
			BigDecimal issueFEE_BD = transPersonIssue.getFee();
			//long issueFEE = transPersonIssue.getFeeLong() + BlockChain.GIFTED_COMPU_AMOUNT;
			//BigDecimal issueFEE_BD = BigDecimal.valueOf(issueFEE, BlockChain.FEE_SCALE);
			BigDecimal issueGIFT_FEE_BD = BigDecimal.valueOf(BlockChain.GIFTED_COMPU_AMOUNT, BlockChain.FEE_SCALE);

			// GIFTs
			if(this.accountExists(creator.getAddress()))
			{
				this.database.getAccountMap().changeBalance(
						creator.getAddress(), true, FEE_KEY, issueGIFT_FEE_BD);
			}
			
			PublicKeyAccount pkAccount = sertifyPubKeys.getSertifiedPublicKeys().get(0);
			if(this.accountExists(creator.getAddress())) 
			{
				this.database.getAccountMap().changeBalance(
						pkAccount.getAddress(), true, FEE_KEY, issueFEE_BD);
			}
		}
	}
	
	private void processOrderCreation(CreateOrderTransaction orderCreation)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//ADD ORDER
		//Order = orderCreation.getOrder();
		//this.addOrder(Trade.getOrder(orderCreation.getOrder().getId(), DBSet.getInstance()));
		this.addOrder(orderCreation.getOrder());
		
	}
	
	private void addOrder(Order order)
	{
		//CHECK IF WE ARE CREATOR
		if(this.accountExists(order.getCreator().getAddress()))
		{
			//ADD ORDER
			// reload order
			Order orderReloaded = Trade.getOrder(order.getId(), DBSet.getInstance());
			this.database.getOrderMap().add(orderReloaded == null?
					order:
					orderReloaded);
		}
	}
	
	private void orphanOrderCreation(CreateOrderTransaction orderCreation)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE CREATOR
		if(this.accountExists(orderCreation.getOrder().getCreator().getAddress()))
		{
			//DELETE ORDER
			//this.database.getOrderMap().delete(orderCreation.getOrder());
		}
	}
	
	private void processOrderCancel(CancelOrderTransaction orderCancel)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//CHECK IF WE ARE CREATOR
		if(this.accountExists(orderCancel.getCreator().getAddress()))
		{
			//DELETE ORDER
			this.database.getOrderMap().delete(new Tuple2<String, BigInteger>(orderCancel.getCreator().getAddress(), orderCancel.getOrder()));
		}
	}
	
	private void orphanOrderCancel(CancelOrderTransaction orderCancel)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//CHECK IF WE ARE CREATOR
		if(this.accountExists(orderCancel.getCreator().getAddress()))
		{
			//DELETE ORDER
			Order order = DBSet.getInstance().getOrderMap().get(orderCancel.getOrder());
			this.database.getOrderMap().add(order);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) 
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE)
		{
			Block block = (Block) message.getValue();
				
			//CHECK BLOCK
			this.processBlock(block);
				
			//CHECK TRANSACTIONS
			for(Transaction transaction: block.getTransactions())
			{
				this.processTransaction(transaction);
				
				//SKIP PAYMENT TRANSACTIONS
				if (transaction instanceof R_Send)
				{
					continue;
				}
				
				//CHECK IF NAME REGISTRATION
				else if(transaction instanceof RegisterNameTransaction)
				{
					this.processNameRegistration((RegisterNameTransaction) transaction);
				}
				
				//CHECK IF NAME UPDATE
				else if(transaction instanceof UpdateNameTransaction)
				{
					this.processNameUpdate((UpdateNameTransaction) transaction);
				}
				
				//CHECK IF NAME SALE
				else if(transaction instanceof SellNameTransaction)
				{
					this.processNameSale((SellNameTransaction) transaction);
				}
				
				//CHECK IF NAME SALE
				else if(transaction instanceof CancelSellNameTransaction)
				{
					this.processCancelNameSale((CancelSellNameTransaction) transaction);
				}
				
				//CHECK IF NAME PURCHASE
				else if(transaction instanceof BuyNameTransaction)
				{
					this.processNamePurchase((BuyNameTransaction) transaction);
				}
				
				//CHECK IF POLL CREATION
				else if(transaction instanceof CreatePollTransaction)
				{
					this.processPollCreation((CreatePollTransaction) transaction);
				}
				
				//CHECK IF POLL VOTE
				else if(transaction instanceof VoteOnPollTransaction)
				{
					this.processPollVote((VoteOnPollTransaction) transaction);
				}
				
				//CHECK IF ITEM ISSUE
				else if(transaction instanceof Issue_ItemRecord)
				{
					this.processItemIssue((Issue_ItemRecord) transaction);
				}
				
				//CHECK IF SERTIFY PErSON
				else if(transaction instanceof R_SertifyPubKeys)
				{
					this.processSertifyPerson((R_SertifyPubKeys) transaction);
				}
				
				//CHECK IF ORDER CREATION
				if(transaction instanceof CreateOrderTransaction)
				{
					this.processOrderCreation((CreateOrderTransaction) transaction);
				}
				
				//CHECK IF ORDER CANCEL
				else if(transaction instanceof CancelOrderTransaction)
				{
					this.processOrderCancel((CancelOrderTransaction) transaction);
				}
			}
		}
		
		else if(message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE)
		{	
			Transaction transaction = (Transaction) message.getValue();
				
			this.processTransaction(transaction);
			
			//CHECK IF PAYMENT
			if (transaction instanceof R_Send)
			{
				return;
			}
			
			//CHECK IF NAME REGISTRATION
			else if(transaction instanceof RegisterNameTransaction)
			{
				this.processNameRegistration((RegisterNameTransaction) transaction);
			}
			
			//CHECK IF POLL CREATION
			else if(transaction instanceof CreatePollTransaction)
			{
				this.processPollCreation((CreatePollTransaction) transaction);
			}
			
			//CHECK IF ITEM ISSUE
			else if(transaction instanceof Issue_ItemRecord)
			{
				this.processItemIssue((Issue_ItemRecord) transaction);
			}

			//CHECK IF SERTIFY PErSON
			else if(transaction instanceof R_SertifyPubKeys)
			{
				this.processSertifyPerson((R_SertifyPubKeys) transaction);
			}

			//CHECK IF ORDER CREATION
			else if(transaction instanceof CreateOrderTransaction)
			{
				this.processOrderCreation((CreateOrderTransaction) transaction);
			}
		}
		
		else if(message.getType() == ObserverMessage.REMOVE_BLOCK_TYPE)
		{
			Block block = (Block) message.getValue();
				
			//CHECK BLOCK
			this.orphanBlock(block);
				
			//CHECK TRANSACTIONS
			for(Transaction transaction: block.getTransactions())
			{
				this.orphanTransaction(transaction);
				
				//CHECK IF PAYMENT
				if (transaction instanceof R_Send)
				{
					continue;
				}

				//CHECK IF NAME REGISTRATION
				else if(transaction instanceof RegisterNameTransaction)
				{
					this.orphanNameRegistration((RegisterNameTransaction) transaction);
				}
				
				//CHECK IF NAME UPDATE
				else if(transaction instanceof UpdateNameTransaction)
				{
					this.orphanNameUpdate((UpdateNameTransaction) transaction);
				}
				
				//CHECK IF NAME SALE
				else if(transaction instanceof SellNameTransaction)
				{
					this.orphanNameSale((SellNameTransaction) transaction);
				}
				
				//CHECK IF CANCEL NAME SALE
				else if(transaction instanceof CancelSellNameTransaction)
				{
					this.orphanCancelNameSale((CancelSellNameTransaction) transaction);
				}
				
				//CHECK IF CANCEL NAME SALE
				else if(transaction instanceof BuyNameTransaction)
				{
					this.orphanNamePurchase((BuyNameTransaction) transaction);
				}
				
				//CHECK IF POLL CREATION
				else if(transaction instanceof CreatePollTransaction)
				{
					this.orphanPollCreation((CreatePollTransaction) transaction);
				}
				
				//CHECK IF POLL VOTE
				else if(transaction instanceof VoteOnPollTransaction)
				{
					this.orphanPollVote((VoteOnPollTransaction) transaction);
				}
				
				//CHECK IF ITEM ISSUE
				else if(transaction instanceof Issue_ItemRecord)
				{
					this.orphanItemIssue((Issue_ItemRecord) transaction);
				}
				
				//CHECK IF SERTIFY PErSON
				else if(transaction instanceof R_SertifyPubKeys)
				{
					this.orphanSertifyPerson((R_SertifyPubKeys) transaction);
				}

				//CHECK IF ORDER CREATION
				else if(transaction instanceof CreateOrderTransaction)
				{
					this.orphanOrderCreation((CreateOrderTransaction) transaction);
				}
				
				//CHECK IF ORDER CANCEL
				else if(transaction instanceof CancelOrderTransaction)
				{
					this.orphanOrderCancel((CancelOrderTransaction) transaction);
				}
			}
		}
		
		else if(message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
		{	
			Transaction transaction = (Transaction) message.getValue();
				
			this.orphanTransaction(transaction);
				
			//CHECK IF PAYMENT
			if (transaction instanceof R_Send)
			{
				return;
			}
			//CHECK IF NAME REGISTRATION
			else if(transaction instanceof RegisterNameTransaction)
			{
				this.orphanNameRegistration((RegisterNameTransaction) transaction);
			}
			
			//CHECK IF POLL CREATION
			else if(transaction instanceof CreatePollTransaction)
			{
				this.orphanPollCreation((CreatePollTransaction) transaction);
			}
			
			//CHECK IF ITEM ISSUE
			else if(transaction instanceof Issue_ItemRecord)
			{
				this.orphanItemIssue((Issue_ItemRecord) transaction);
			}
			
			//CHECK IF SERTIFY PErSON
			else if(transaction instanceof R_SertifyPubKeys)
			{
				this.orphanSertifyPerson((R_SertifyPubKeys) transaction);
			}

			//CHECK IF ORDER CREATION
			else if(transaction instanceof CreateOrderTransaction)
			{
				this.orphanOrderCreation((CreateOrderTransaction) transaction);
			}
		}
		
		else if (message.getType() == ObserverMessage.ADD_AT_TX_TYPE)
		{
			this.processATTransaction( (Tuple2<Tuple2<Integer, Integer>, AT_Transaction>) message.getValue() );
		}
		
		else if (message.getType() == ObserverMessage.REMOVE_AT_TX)
		{
			this.orphanATTransaction( (Tuple2<Tuple2<Integer, Integer>, AT_Transaction>) message.getValue() );
		}
		
		//ADD ORDER
		else if(message.getType() == ObserverMessage.ADD_ORDER_TYPE || message.getType() == ObserverMessage.REMOVE_ORDER_TYPE)
		{
			this.addOrder((Order) message.getValue());
		}
	}

	//CLOSE
	
	public void close()
	{
		if(this.database != null)
		{
			this.database.close();
		}
		
		if(this.secureDatabase != null)
		{
			this.secureDatabase.close();
		}
	}

	public void commit() 
	{
		if(this.database != null)
		{
			this.database.commit();
		}
		
		if(this.secureDatabase != null)
		{
			this.secureDatabase.commit();
		}
		
	}	
	
	public byte[] getLastBlockSignature()
	{
		return this.database.getLastBlockSignature();
	}
	
	public long getLicenseKey()
	{
		if (this.database == null || this.database.getAccountMap().getLicenseKey() == null) {
			return 2l;
		}
		
		return this.database.getAccountMap().getLicenseKey();
	}

	public void setLicenseKey(long key)
	{
		this.database.getAccountMap().setLicenseKey(key);
	}
}
