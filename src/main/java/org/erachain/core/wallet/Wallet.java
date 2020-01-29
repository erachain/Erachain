package org.erachain.core.wallet;
// 09/03

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.at.ATTransaction;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.naming.Name;
import org.erachain.core.naming.NameSale;
import org.erachain.core.transaction.*;
import org.erachain.core.voting.Poll;
import org.erachain.database.wallet.AccountMap;
import org.erachain.database.wallet.DWSet;
import org.erachain.database.wallet.SecureWalletDatabase;
import org.erachain.datachain.BlockMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.ObserverWaiter;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.erachain.utils.SaveStrToFile;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Timer;
import java.util.*;

/**
 * обработка секртеных ключей и моих записей, которые относятся к набору моих счетов
 */
public class Wallet extends Observable implements Observer {

	static final boolean CHECK_CHAIN_BROKENS_ON_SYNC_WALLET = false;

	public static final int STATUS_UNLOCKED = 1;
	public static final int STATUS_LOCKED = 0;

	private static final long RIGHTS_KEY = Transaction.RIGHTS_KEY;
	private static final long FEE_KEY = Transaction.FEE_KEY;
	static Logger LOGGER = LoggerFactory.getLogger(Wallet.class.getSimpleName());
	public DWSet database;
	AssetsFavorites assetsFavorites;
	TemplatesFavorites templatesFavorites;
	PersonsFavorites personsFavorites;
	private SecureWalletDatabase secureDatabase;
	private int secondsToUnlock = 100;
	private Timer lockTimer; // = new Timer();
	private int syncHeight;
	private WalletUpdater walletUpdater;

	private List<ObserverWaiter> waitingObservers = new ArrayList<>();
	// CONSTRUCTORS

	public Wallet(boolean withObserver, boolean dynamicGUI) {

		//this.syncHeight = ;

		// CHECK IF EXISTS
		if (this.exists()) {
			// OPEN WALLET
			this.database = DWSet.reCreateDB(withObserver, dynamicGUI);

			linkWaitingObservers();

			if (withObserver) {
				// ADD OBSERVER
				// Controller.getInstance().addObserver(this);
				DCSet.getInstance().getTransactionTab().addObserver(this);
				DCSet.getInstance().getBlockMap().addObserver(this);
				// DCSet.getInstance().getCompletedOrderMap().addObserver(this);
			}

			walletUpdater = new WalletUpdater(Controller.getInstance(),
					Controller.getInstance().getBlockChain(), DCSet.getInstance(), this);

        }

        startProcessForSynchronize();

	}

	// GETTERS/SETTERS

	public static byte[] generateAccountSeed(byte[] seed, int nonce) {
		byte[] nonceBytes = Ints.toByteArray(nonce);
		byte[] accountSeed = Bytes.concat(nonceBytes, seed, nonceBytes);
		return Crypto.getInstance().doubleDigest(accountSeed);
	}

	public static byte[] generateAccountSeed(byte[] seed, String nonce) {
		byte[] nonceBytes = nonce.getBytes();
		byte[] accountSeed = Bytes.concat(nonceBytes, seed, nonceBytes);
		return Crypto.getInstance().doubleDigest(accountSeed);
	}

	public int getSyncHeight() {
		return this.syncHeight;
	}

	public void initiateItemsFavorites() {
		if (this.assetsFavorites == null) {
			this.assetsFavorites = new AssetsFavorites();
		}
		if (this.templatesFavorites == null) {
			this.templatesFavorites = new TemplatesFavorites();
		}
		if (this.personsFavorites == null) {
			this.personsFavorites = new PersonsFavorites();
		}
	}

	public void setSecondsToUnlock(int seconds) {
		this.secondsToUnlock = seconds;
	}

	public int getVersion() {
		return this.database.getVersion();
	}

	public boolean isUnlocked() {
		return this.secureDatabase != null;
	}
	public boolean isUnlockedForRPC() {
		// Если раслочено на все время
		return this.secureDatabase != null && this.secondsToUnlock < 0;
	}

	public List<Account> getAccounts() {
		if (this.database == null)
			return new ArrayList<>();

		return this.database.getAccountMap().getAccounts();
	}

	public List<Account> getAccountsAndSetBalancePosition(int position) {
		if (this.database == null)
			return new ArrayList<>();

		List<Account> accounts = this.database.getAccountMap().getAccounts();
		for (Account account: accounts) {
			account.setViewBalancePosition(position);
		}

		return accounts;
	}

	public List<PublicKeyAccount> getPublicKeyAccounts() {
		if (this.database == null)
			return new ArrayList<>();


        AccountMap mapAccs = this.database.getAccountMap();
        synchronized (mapAccs) { // else deadlock in org.erachain.database.wallet.AccountMap.add
            return mapAccs.getPublicKeyAccounts();
        }
	}

	public List<Tuple2<Account, Long>> getAccountsAssets() {
		return this.database.getAccountMap().getAccountsAssets();
	}

	public boolean accountExists(String address) {
		return this.database.getAccountMap().exists(address);
	}

	public Account getAccount(String address) {
		return this.database.getAccountMap().getAccount(address);
	}

	public boolean isWalletDatabaseExisting() {
		return database != null;
	}

	// public BigDecimal getUnconfirmedBalance(String address, long key)
	// {
	// return this.database.getAccountMap().getUnconfirmedBalance(address, key);
	// }
	/*
	 * public BigDecimal getUnconfirmedBalance(Account account, long key) {
	 *
	 * return this.database.getAccountMap().getUnconfirmedBalance(account, key);
	 * }
	 */
	public Tuple3<BigDecimal, BigDecimal, BigDecimal> getUnconfirmedBalance(Account account, long key) {

		return this.database.getAccountMap().getBalance(account, key);
	}

	public List<PrivateKeyAccount> getprivateKeyAccounts() {
		if (this.secureDatabase == null) {
			return new ArrayList<PrivateKeyAccount>();
		}

		return this.secureDatabase.getAccountSeedMap().getPrivateKeyAccounts();
	}

	public PrivateKeyAccount getPrivateKeyAccount(String address) {
		if (this.secureDatabase == null) {
			return null;
		}

		return this.secureDatabase.getAccountSeedMap().getPrivateKeyAccount(address);
	}

	public void addWaitingObserver(ObserverWaiter observer) {
		waitingObservers.add(observer);
	}

	/*
	public PublicKeyAccount getPublicKeyAccount(String address) {
		if (this.database == null) {
			return null;
		}

		return this.database.getAccountMap().getPublicKeyAccount(address);
	}
	*/

	public boolean exists() {
		if (Controller.getInstance().noUseWallet){
			return false;
		}
		String walletDir = Settings.getInstance().getWalletDir();
		return new File(walletDir).exists();
	}

	public List<Pair<Account, Transaction>> getLastTransactions(int limit) {
		if (!this.exists()) {
			new ArrayList<Pair<Account, Transaction>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getTransactionMap().get(accounts, limit);
	}

	public List<Transaction> getLastTransactions(Account account, int limit) {
		if (!this.exists()) {
			return new ArrayList<Transaction>();
		}

		return this.database.getTransactionMap().get(account, limit);
	}

	public List<Pair<Account, Block.BlockHead>> getLastBlocks(int limit) {
		if (!this.exists()) {
			return new ArrayList<Pair<Account, Block.BlockHead>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getBlocksHeadMap().get(accounts, limit);
	}

	public List<Block.BlockHead> getLastBlocks(Account account, int limit) {
		if (!this.exists()) {
			return new ArrayList<Block.BlockHead>();
		}

		return this.database.getBlocksHeadMap().get(account, limit);
	}

	public List<Pair<Account, Name>> getNames() {
		if (!this.exists()) {
			return new ArrayList<Pair<Account, Name>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getNameMap().get(accounts);
	}

	public List<Name> getNames(Account account) {
		if (!this.exists()) {
			return new ArrayList<Name>();
		}

		return this.database.getNameMap().get(account);
	}

	public List<Pair<Account, NameSale>> getNameSales() {
		if (!this.exists()) {
			return new ArrayList<Pair<Account, NameSale>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getNameSaleMap().get(accounts);
	}

	public List<NameSale> getNameSales(Account account) {
		if (!this.exists()) {
			return new ArrayList<NameSale>();
		}

		return this.database.getNameSaleMap().get(account);
	}

    @Deprecated
    public List<Pair<Account, Poll>> getPolls() {
        if (!this.exists()) {
            return new ArrayList<Pair<Account, Poll>>();
        }

        List<Account> accounts = this.getAccounts();
        return this.database.getPollMap_old().get(accounts);
    }

    @Deprecated
    public List<Poll> getPolls(Account account) {
        if (!this.exists()) {
            return new ArrayList<Poll>();
        }

        return this.database.getPollMap_old().get(account);
    }

	// тут нужно понять где это используется
	public void replaseFavoriteItems(int type) {
		if (!this.exists()) {
			return;
		}

		switch (type) {
			case ItemCls.ASSET_TYPE:
				if (this.assetsFavorites != null) {
					this.database.getAssetFavoritesSet().replace(this.assetsFavorites.getKeys());
				}
			case ItemCls.TEMPLATE_TYPE:
				if (this.templatesFavorites != null) {
					this.database.getTemplateFavoritesSet().replace(this.templatesFavorites.getKeys());
				}
			case ItemCls.PERSON_TYPE:
				if (this.personsFavorites != null) {
					this.database.getPersonFavoritesSet().replace(this.personsFavorites.getKeys());
				}
		}
	}

	public void addItemFavorite(ItemCls item) {
		if (!this.exists()) {
			return;
		}

		this.database.addItemToFavorite(item);
	}

	public void removeItemFavorite(ItemCls item) {
		if (!this.exists()) {
			return;
		}

		this.database.removeItemFromFavorite(item);
	}

	public boolean isItemFavorite(ItemCls item) {
		if (!exists()) {
			return false;
		}
		return database.isItemFavorite(item);
	}

	public void addTransactionFavorite(Transaction transaction) {
		if (!this.exists()) {
			return;
		}

		this.database.addTransactionToFavorite(transaction);
	}

	public void removeTransactionFavorite(Transaction transaction) {
		if (!this.exists()) {
			return;
		}

		this.database.removeTransactionFromFavorite(transaction);
	}

	public boolean isTransactionFavorite(Transaction transaction) {
		if (!this.exists()) {
			return false;
		}

		return this.database.isTransactionFavorite(transaction);
	}

	// CREATE
	public boolean create(byte[] seed, String password, int depth, boolean synchronize, String path,
						  boolean withObserver, boolean dynamicGUI) {
		String oldPath = Settings.getInstance().getWalletDir();
		// set wallet dir
		Settings.getInstance().setWalletDir(path);
		// OPEN WALLET
		DWSet database = DWSet.reCreateDB(withObserver, dynamicGUI);

		if (this.secureDatabase != null) {
			// CLOSE secured WALLET
			lock();
		}

		// OPEN SECURE WALLET
		SecureWalletDatabase secureDatabase = new SecureWalletDatabase(password);

		// CREATE
		boolean res = this.create(database, secureDatabase, seed, depth, synchronize);
		if (res) {
			// save wallet dir

			JSONObject settingsLangJSON = new JSONObject();
			settingsLangJSON.putAll(Settings.getInstance().read_setting_JSON());
			Settings.getInstance().setWalletDir(path);
			settingsLangJSON.put("walletdir", Settings.getInstance().getWalletDir());
			try {
				SaveStrToFile.saveJsonFine(Settings.getInstance().getSettingsPath(), settingsLangJSON);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			Settings.getInstance().setWalletDir(oldPath);
		}
		return res;
	}

	public boolean create(DWSet database, SecureWalletDatabase secureDatabase, byte[] seed, int depth,
						  boolean synchronize) {
		// CREATE WALLET
		this.database = database;

		// CREATE SECURE WALLET
		this.secureDatabase = secureDatabase;

		// ADD VERSION
		this.database.setVersion(1);

		// SET LICENSE KEY
		this.setLicenseKey(Controller.LICENSE_VERS);

		// ADD SEED
		try {
			this.secureDatabase.setSeed(seed);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}

		// ADD NONCE
		this.secureDatabase.setNonce(0);

		// CREATE ACCOUNTS
		for (int i = 1; i <= depth; i++) {
			this.generateNewAccount();
		}

		linkWaitingObservers();

		// SCAN TRANSACTIONS
		if (synchronize) {
			this.synchronize(true);
		}

		// COMMIT
		this.commit();

		// ADD OBSERVER
		Controller.getInstance().addObserver(this);
		////DCSet.getInstance().getCompletedOrderMap().addObserver(this);

		// SOME
		// Account initAccount = this.getAccounts().get(0);
		// initAccount.setConfirmedBalance(Transaction.AssetCls.DILE_KEY,
		// BigDecimal.valueOf(0.00001));

		return true;
	}

    private Timer timerSynchronize;

    public void startProcessForSynchronize() {

        if (this.timerSynchronize == null) {
            this.timerSynchronize = new Timer("Wallet synchronize check");

            TimerTask action = new TimerTask() {
                @Override
                public void run() {

                    if (Controller.getInstance().isStatusWaiting()) {

                        if (Controller.getInstance().isNeedSyncWallet()
                                && !Controller.getInstance().isProcessingWalletSynchronize()) {

                            Controller.getInstance().synchronizeWallet();
                        }
                    }
                }
            };

            this.timerSynchronize.schedule(action, 30000, 60000);
        }

    }

	public int getAccountNonce() {
		return this.secureDatabase.getNonce();
	}

	@SuppressWarnings("unchecked")
	public String generateNewAccount() {
		// CHECK IF WALLET IS OPEN
		if (!this.isUnlocked()) {
			return "";
		}

		// READ SEED
		byte[] seed = this.secureDatabase.getSeed();

		// READ NONCE
		int nonce = this.secureDatabase.getAndIncrementNonce();

		// GENERATE ACCOUNT SEED
		byte[] accountSeed = generateAccountSeed(seed, nonce);
		PrivateKeyAccount account = new PrivateKeyAccount(accountSeed);
		JSONObject ob = new JSONObject();
		// CHECK IF ACCOUNT ALREADY EXISTS
		if (!this.accountExists(account.getAddress())) {
			// ADD TO DATABASE
			this.secureDatabase.getAccountSeedMap().add(account);
			this.database.getAccountMap().add(account, -1);
			// set name
			ob.put("description", Lang.getInstance().translate("Created by default Account") + " " + (nonce + 1));
			this.database.getAccountsPropertisMap().put(account.getAddress(), new Tuple2<String, String>(
					Lang.getInstance().translate("My Account") + " " + (nonce + 1), StrJSonFine.convert(ob)));
			LOGGER.info("Added account #" + nonce);

			this.commit();

			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));

		}

		return account.getAddress();
	}

	// DELETE

	public boolean deleteAccount(PrivateKeyAccount account) {
		// CHECK IF WALLET IS OPEN
		if (!this.isUnlocked()) {
			return false;
		}

		// DELETE FROM DATABASE
		this.database.delete(account);
		this.secureDatabase.delete(account);

		// SAVE TO DISK
		this.database.hardFlush();

		// NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_ACCOUNT_TYPE, account));

		// RETURN
		return true;
	}

	// SYNCRHONIZE

	// UPDATE all accounts for all assets unconfirmed balance
	public void update_account_assets() {
		List<Tuple2<Account, Long>> accounts_assets = this.getAccountsAssets();

        for (Tuple2<Account, Long> account_asset : accounts_assets) {
            this.database.getAccountMap().changeBalance(account_asset.a.getAddress(), false, account_asset.b,
                    BigDecimal.ZERO);
        }

	}


    public static boolean synchronizeStatus;

    /**
     * нужно для запрета вызова уже работающего процесса синхронизации
     * @return
     */
    public synchronized boolean synchronizeStatusCheck() {
        if (synchronizeStatus)
            return true;
        synchronizeStatus = true;
        return false;
    }

    public boolean synchronizeBodyStop;
	public void synchronizeBody(boolean reset) {
	    if (!synchronizeBodyStop || synchronizeStatusCheck())
	        return;

		DCSet dcSet = DCSet.getInstance();

		Block blockStart;
		int height;
        synchronizeBodyStop = false;

		if (reset) {
			LOGGER.info("   >>>>  try to Reset maps");

            // SAVE transactions file
            this.database.clearCache();
            this.database.hardFlush();

            // RESET MAPS
			this.database.getTransactionMap().clear();
			this.database.getBlocksHeadMap().clear();
			this.database.getNameMap().clear();
			this.database.getNameSaleMap().clear();
            this.database.getPollMap_old().clear();
			this.database.getAssetMap().clear();
			this.database.getImprintMap().clear();
			this.database.getTemplateMap().clear();
			this.database.getPersonMap().clear();
			this.database.getStatusMap().clear();
			this.database.getUnionMap().clear();
			this.database.getOrderMap().clear();

            LOGGER.info("   >>>>  Maps was Resetted");

			// REPROCESS BLOCKS
            blockStart = new GenesisBlock();
			this.database.setLastBlockSignature(blockStart.getReference());
			height = 1;

        } else {

            byte[] lastSignature = this.database.getLastBlockSignature();
            blockStart = dcSet.getBlockSignsMap().getBlock(lastSignature);

            if (blockStart == null) {
                // выходим и потом пересинхронизируемся с начала
                return;
            }
        }

        // SAVE transactions file
        this.database.clearCache();
        this.database.hardFlush();

		if (Controller.getInstance().isOnStopping())
			return;

		height = blockStart.getHeight();
		int steepHeight = dcSet.getBlockMap().size() / 100;
		int lastHeight = 0;

		long timePoint = System.currentTimeMillis();
		BlockMap blockMap = dcSet.getBlockMap();

        this.database.clearCache();

        try {
        	if (getAccounts() != null && !getAccounts().isEmpty()) {
				do {

					Block block = blockMap.getAndProcess(height);

					if (block == null) {
						break;
					}

					try {
						this.processBlock(dcSet, block);
					} catch (java.lang.OutOfMemoryError e) {
						LOGGER.error(e.getMessage(), e);
						Controller.getInstance().stopAll(44);
						return;
					}

					if (System.currentTimeMillis() - timePoint > 10000
							|| steepHeight < height - lastHeight) {

						timePoint = System.currentTimeMillis();
						lastHeight = height;

						this.syncHeight = height;

						//logger.debug("try Commit");
						this.database.commit();

                        if (Controller.getInstance().isOnStopping())
                            return;

						// обязательно нужно чтобы память освобождать
						// и если объект был изменен (с тем же ключем у него удалили поле внутри - чтобы это не выдавлось
						// при новом запросе - иначе изменения прилетают в другие потоки и ошибку вызываю
						// БЕЗ очистки КЭША HEAP забивается под завязку
						dcSet.clearCache();
						this.database.clearCache();

						// не нужно - Ява сама норм делает вызов очистки
						//System.gc();

						Controller.getInstance().walletSyncStatusUpdate(height);

					}

					height++;

				} while (!synchronizeBodyStop
						&& !Controller.getInstance().isOnStopping()
						&& !Controller.getInstance().needUpToDate()
						&& Controller.getInstance().isStatusWaiting());
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);

		} finally {

			if (Controller.getInstance().isOnStopping())
				return;

            this.syncHeight = height;

			// обязательно нужно чтобы память освобождать
			// и если объект был изменен (с тем же ключем у него удалили поле внутри - чтобы это не выдавлось
			// при новом запросе - иначе изменения прилетают в другие потоки и ошибку вызывают
            // вдобавое отчищает полностью память - много свободной памяти получаем
			dcSet.clearCache();

            // тут возможно цепочка синхронизировалась или начала синхронизироваться и КОММИТ вызовет ошибку
            //  java.io.IOException: Запрошенную операцию нельзя выполнить для файла с открытой пользователем сопоставленной секцией
			this.database.hardFlush();

            this.database.clearCache();

            System.gc();

            synchronizeStatus = false;

            Controller.getInstance().walletSyncStatusUpdate(height);
            Controller.getInstance().setProcessingWalletSynchronize(false);

        }

		// RESET UNCONFIRMED BALANCE for accounts + assets
		LOGGER.info("Resetted balances");
		update_account_assets();
        Controller.getInstance().walletSyncStatusUpdate(0);

		LOGGER.info("Update Orders");
		this.database.getOrderMap().updateLefts();

		// NOW IF NOT SYNCHRONIZED SET STATUS
		// CHECK IF WE ARE UPTODATE
		if (false && !Controller.getInstance().checkStatus(0)) {
			// NOTIFY
			Controller.getInstance().notifyObservers(
					new ObserverMessage(ObserverMessage.NETWORK_STATUS, Controller.STATUS_SYNCHRONIZING));
		}

        LOGGER.info(" >>>>>>>>>>>>>>> *** Synchronizing wallet DONE");

	}

    // asynchronous RUN from BlockGenerator
    public void synchronize(boolean reset) {
        if (!reset && Controller.getInstance().isProcessingWalletSynchronize()
                || Controller.getInstance().isOnStopping()
			|| Controller.getInstance().noDataWallet || Controller.getInstance().noUseWallet) {
            return;
        }

        // here ICREATOR
        Controller.getInstance().setProcessingWalletSynchronize(true);
        Controller.getInstance().setNeedSyncWallet(false);

        Controller.getInstance().walletSyncStatusUpdate(-1);

        LOGGER.info(" >>>>>>>>>>>>>>> *** Synchronizing wallet...");

        DCSet dcSet = DCSet.getInstance();

        ///////////////////////////////////// IS CHAIN VALID
        if (CHECK_CHAIN_BROKENS_ON_SYNC_WALLET) {
            LOGGER.info("TEST CHAIN .... ");
            for (int i = 1; i <= dcSet.getBlockMap().size(); i++) {
                Block block = dcSet.getBlockMap().getAndProcess(i);
                if (block.getHeight() != i) {
                    Long error = null;
                    ++error;
                }
                if (block.blockHead.heightBlock != i) {
                    Long error = null;
                    ++error;
                }
                Block.BlockHead head = dcSet.getBlocksHeadsMap().get(i);
                if (head.heightBlock != i) {
                    Long error = null;
                    ++error;
                }
                if (i > 1) {
                    byte[] reference = block.getReference();
                    Block parent = dcSet.getBlockSignsMap().getBlock(reference);
                    if (parent == null) {
                        Long error = null;
                        ++error;
                    }
                    if (parent.getHeight() != i - 1) {
                        Long error = null;
                        ++error;
                    }
                    parent = dcSet.getBlockMap().getAndProcess(i - 1);
                    if (!Arrays.equals(parent.getSignature(), reference)) {
                        Long error = null;
                        ++error;
                    }
                }
                byte[] signature = block.getSignature();
                int signHeight = dcSet.getBlockSignsMap().get(signature);
                if (signHeight != i) {
                    Long error = null;
                    ++error;
                }
            }
        }

        Block block;

        if (reset) {

            // полная пересборка кошелька

            // break current synchronization if exists
            synchronizeBodyStop = true;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
            synchronizeBody(reset);
            return;

        } else {

            byte[] lastSignature = this.database.getLastBlockSignature();
            if (lastSignature == null) {
                synchronize(true);
                return;
            }

            block = dcSet.getBlockSignsMap().getBlock(lastSignature);
            if (block == null) {
                synchronize(true);
                return;
            }
        }

        // break current synchronization if exists
        synchronizeBodyStop = true;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            return;
        }
        // запустим догоняние
        synchronizeBody(false);

    }

    // UNLOCK
	public boolean unlock(String password) {

		if (Controller.getInstance().noUseWallet)
			return false;

		if (this.secureDatabase != null) {
			// CLOSE secure WALLET
			lock();
		}

		// TRY TO UNLOCK
		try {
			SecureWalletDatabase secureDatabase = new SecureWalletDatabase(password);
			return this.unlock(secureDatabase);
		} catch (Exception e) {
			return false;
		}

	}

	/*
	// UNLOCK ONCE
	public boolean unlockOnce(String password) {

		this.secondsToUnlock = -1;
		return unlock(password);
	}
	*/

	public boolean unlock(SecureWalletDatabase secureDatabase) {
		this.secureDatabase = secureDatabase;

		if (Controller.getInstance().useGui) {
			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_STATUS, STATUS_UNLOCKED));
		} else {
			// CACHE ACCOUNTS
			Controller.getInstance().BlockGeneratorCacheAccounts();
		}

		if (this.secondsToUnlock > 0) {

			if (this.lockTimer != null)
				this.lockTimer.cancel();

			this.lockTimer = new Timer("Wallet Locker");

			TimerTask action = new TimerTask() {
				@Override
				public void run() {
					lock();
				}
			};

			this.lockTimer.schedule(action, this.secondsToUnlock * 1000);
		} else {
			if (this.lockTimer != null)
				this.lockTimer.cancel();
		}
		return true;
	}

	public boolean lock() {
		if (!this.isUnlocked()) {
			return true;
		}

		// CLOSE
		if (this.secureDatabase != null) {
			this.secureDatabase.close();
			this.secureDatabase = null;
		}

		if (Controller.getInstance().useGui) {
			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_STATUS, STATUS_LOCKED));
		}

		this.secondsToUnlock = 100;
		if (this.lockTimer != null)
			this.lockTimer.cancel();

		// LOCK SUCCESSFULL
		return true;
	}

	// IMPORT/EXPORT
	public String importAccountSeed(byte[] accountSeed) {
		// CHECK IF WALLET IS OPEN
		if (!this.isUnlocked()) {
			return "Wallet is locked";
		}

		// CHECK LENGTH
		if (accountSeed.length != Crypto.HASH_LENGTH) {
			return "Wrong length != 32";
		}

		// CREATE ACCOUNT
		PrivateKeyAccount account = new PrivateKeyAccount(accountSeed);

		// CHECK IF ACCOUNT ALREADY EXISTS
		if (!this.accountExists(account.getAddress())) {
			// ADD TO DATABASE
			this.secureDatabase.getAccountSeedMap().add(account);
			this.database.getAccountMap().add(account, -1);

			// SAVE TO DISK
			this.database.hardFlush();

			// SYNCHRONIZE
			this.synchronize(true);

			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));

			// RETURN
			return account.getAddress();
		}

		return "";
	}

	public String importPrivateKey(byte[] privateKey64) {
		// CHECK IF WALLET IS OPEN
		if (!this.isUnlocked()) {
			return "Wallet is locked";
		}

		// CHECK LENGTH
		if (privateKey64.length != Crypto.SIGNATURE_LENGTH) {
			return "Wrong length != 64";
		}

		// CREATE ACCOUNT
		PrivateKeyAccount account = new PrivateKeyAccount(privateKey64);

		// CHECK IF ACCOUNT ALREADY EXISTS
		if (!this.accountExists(account.getAddress())) {
			// ADD TO DATABASE
			this.secureDatabase.getAccountSeedMap().add(account);
			this.database.getAccountMap().add(account, -1);

			// SAVE TO DISK
			this.database.hardFlush();

			// SYNCHRONIZE
			this.synchronize(true);

			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));

			// RETURN
			return account.getAddress();
		}

		return "";
	}

	public byte[] exportAccountSeed(String address) {
		// CHECK IF WALLET IS OPEN
		if (!this.isUnlocked()) {
			return null;
		}

		PrivateKeyAccount account = this.getPrivateKeyAccount(address);

		if (account == null) {
			return null;
		}

		return account.getSeed();
	}

	public byte[] exportSeed() {
		// CHECK IF WALLET IS OPEN
		if (!this.isUnlocked()) {
			return null;
		}

		return this.secureDatabase.getSeed();
	}

	// OBSERVER

	public void linkWaitingObservers() {
		// добавим теперь раз кошелек открылся все ожидающие связи на наблюдения
		for (ObserverWaiter observer : waitingObservers) {
			observer.addObservers();
		}
		waitingObservers.clear();
	}

	@Override
	public void addObserver(Observer o) {

		super.addObserver(o);

		if (false && Controller.getInstance().doesWalletDatabaseExists()) {

			// REGISTER ON ACCOUNTS
			this.database.getAccountMap().addObserver(o);

			// REGISTER ON TRANSACTIONS
			this.database.getTransactionMap().addObserver(o);

			// REGISTER ON BLOCKS
			this.database.getBlocksHeadMap().addObserver(o);

			// REGISTER ON NAMES
			this.database.getNameMap().addObserver(o);

			// REGISTER ON NAME SALES
			this.database.getNameSaleMap().addObserver(o);

			// REGISTER ON POLLS
            this.database.getPollMap_old().addObserver(o);

			// REGISTER ON ASSETS
			this.database.getAssetMap().addObserver(o);

			// REGISTER ON IMPRINTS
			this.database.getImprintMap().addObserver(o);

			// REGISTER ON TEMPLATES
			this.database.getTemplateMap().addObserver(o);

			// REGISTER ON PERSONS
			this.database.getPersonMap().addObserver(o);

			// REGISTER ON STATUS
			this.database.getStatusMap().addObserver(o);

			// REGISTER ON UNION
			this.database.getUnionMap().addObserver(o);

			// REGISTER ON ORDERS
			this.database.getOrderMap().addObserver(o);

		}

		// SEND STATUS
		int status = STATUS_LOCKED;
		if (this.isUnlocked()) {
			status = STATUS_UNLOCKED;
		}

		o.update(this, new ObserverMessage(ObserverMessage.WALLET_STATUS, status));
	}

	public void addFavoritesObserver(Observer o) {

		super.addObserver(o);

		if (Controller.getInstance().doesWalletDatabaseExists()) {

			// REGISTER ON ASSET FAVORITES
			this.database.getAssetFavoritesSet().addObserver(o);

			// REGISTER ON PLATE FAVORITES
			this.database.getTemplateFavoritesSet().addObserver(o);

			// REGISTER ON PERSON FAVORITES
			this.database.getPersonFavoritesSet().addObserver(o);

			// REGISTER ON STATUS FAVORITES
			this.database.getStatusFavoritesSet().addObserver(o);

			// REGISTER ON UNION FAVORITES
			this.database.getUnionFavoritesSet().addObserver(o);

		}
	}

	private void deal_transaction(Account account, Transaction transaction, boolean asOrphan) {
		// UPDATE UNCONFIRMED BALANCE for ASSET
		// TODO: fee doubled?
		long key = transaction.getAssetKey();
		long absKey = key < 0 ? -key : key;
		String address = account.getAddress();

		if (!asOrphan && transaction instanceof RSend) {
			// ADD to FAVORITES
			if (!this.database.getAssetFavoritesSet().contains(transaction.getAbsKey()))
				this.database.getAssetFavoritesSet().add(transaction.getAbsKey());

		}

		BigDecimal fee = transaction.getFee(account);
		if (absKey != 0) {
			// ASSET TRANSFERED + FEE
			BigDecimal amount = transaction.getAmount(account);

			if (fee.compareTo(BigDecimal.ZERO) != 0) {
				if (absKey == FEE_KEY) {
					amount = amount.subtract(fee);
				}
			}
			this.database.getAccountMap().changeBalance(address, !asOrphan, key, amount);
		} else {
			// ONLY FEE
			if (fee.compareTo(BigDecimal.ZERO) != 0) {
				this.database.getAccountMap().changeBalance(address, !asOrphan, FEE_KEY, fee);
			}
		}

	}

	private void processTransaction(Transaction transaction) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		// FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		synchronized (accounts) {
			for (Account account : accounts) {
				// CHECK IF INVOLVED
				if (transaction.isInvolved(account)) {
					// ADD TO ACCOUNT TRANSACTIONS
					if (!this.database.getTransactionMap().add(account, transaction)) {
						// UPDATE UNCONFIRMED BALANCE for ASSET
						deal_transaction(account, transaction, false);
					}
				}
			}
		}
	}

	private void processATTransaction(Tuple2<Tuple2<Integer, Integer>, ATTransaction> atTx) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		// FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		synchronized (accounts) {
			for (Account account : accounts) {
				// CHECK IF INVOLVED
				// if(atTx.b.getRecipient().equalsIgnoreCase(
				// account.getAddress() ))
				if (atTx.b.getRecipient() == account.getAddress()) {
					this.database.getAccountMap().changeBalance(account.getAddress(), false, atTx.b.getKey(),
							BigDecimal.valueOf(atTx.b.getAmount()));

				}
			}
		}
	}

	private void orphanTransaction(Transaction transaction) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		/// FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		DCSet dcSet = DCSet.getInstance();

		synchronized (accounts) {
			for (Account account : accounts) {
				// CHECK IF INVOLVED
				if (transaction.isInvolved(account)) {
					// DELETE FROM ACCOUNT TRANSACTIONS
					this.database.getTransactionMap().delete(account, transaction);

					// UPDATE UNCONFIRMED BALANCE
					deal_transaction(account, transaction, true);
				}
			}
		}
	}

	private void orphanATTransaction(Tuple2<Tuple2<Integer, Integer>, ATTransaction> atTx) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		// FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		synchronized (accounts) {
			for (Account account : accounts) {
				// CHECK IF INVOLVED
				if (atTx.b.getRecipient().equalsIgnoreCase(account.getAddress())) {
					this.database.getAccountMap().changeBalance(account.getAddress(), true, atTx.b.getKey(),
							BigDecimal.valueOf(atTx.b.getAmount()));
				}
			}
		}
	}

	// TODO: our woier
	public boolean checkNeedSyncWallet(byte[] signatureORreference) {

		// CHECK IF WE NEED TO RESYNC
		byte[] lastBlockSignature = this.database.getLastBlockSignature();
		if (lastBlockSignature == null
				|| !Arrays.equals(lastBlockSignature, signatureORreference)) {
			Controller.getInstance().setNeedSyncWallet(true);
			return true;
		}

		return false;

	}

	public void feeProcess(DCSet dcSet, Long blockFee, Account blockGenerator, boolean asOrphan) {

		/*
        BigDecimal bonusFee; // = block.getBonusFee();
        BigDecimal blockTotalFee; // = block.getTotalFee(dcSet);
        BigDecimal emittedFee;

        if (BlockChain.ROBINHOOD_USE) {
            // find rich account
            String rich = Account.getRichWithForks(dcSet, Transaction.FEE_KEY);

            if (!rich.equals(blockGenerator.getAddress())) {
                emittedFee = bonusFee.divide(new BigDecimal(2));

                if (this.accountExists(rich)) {
                    Account richAccount = new Account(rich);
                    this.database.getAccountMap().changeBalance(richAccount.getAddress(), !asOrphan, FEE_KEY, bonusFee);
                }
            } else {
                emittedFee = BigDecimal.ZERO;
            }

        } else {
            emittedFee = bonusFee;
        }

        */
		this.database.getAccountMap().changeBalance(blockGenerator.getAddress(), asOrphan, FEE_KEY,
				new BigDecimal(blockFee).movePointLeft(BlockChain.AMOUNT_DEDAULT_SCALE));

	}

	private long processBlockLogged = 0;

	void processBlock(DCSet dcSet, Block block) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		long start = System.currentTimeMillis();

		// SET AS LAST BLOCK
        this.database.setLastBlockSignature(block.blockHead.signature);

        Account blockGenerator = block.blockHead.creator;
		String blockGeneratorStr = blockGenerator.getAddress();

        int height = block.blockHead.heightBlock;

		// CHECK IF WE ARE GENERATOR
		if (this.accountExists(blockGeneratorStr)) {
			// ADD BLOCK
            this.database.getBlocksHeadMap().add(block.blockHead);

			// KEEP TRACK OF UNCONFIRMED BALANCE
			// PROCESS FEE
            feeProcess(dcSet, block.blockHead.totalFee, blockGenerator, false);

		}

		// CHECK TRANSACTIONS
		int seqNo = 0;
		for (Transaction transaction : block.getTransactions()) {

            // TODO нужно сделать при закрытии базы чтобы ожидала окончания проходя всего блока тут - пока ОТКАТ


			if (transaction.isWiped()) {
				continue;
			}

			if (transaction.noDCSet())
                transaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo);

			this.processTransaction(transaction);

			// SKIP PAYMENT TRANSACTIONS
			if (transaction instanceof RSend) {
				continue;
			}

			// CHECK IF ITEM ISSUE
			else if (transaction instanceof IssueItemRecord) {
				this.processItemIssue((IssueItemRecord) transaction);
			}

			// CHECK IF SERTIFY PErSON
			else if (transaction instanceof RSertifyPubKeys) {
				this.processSertifyPerson((RSertifyPubKeys) transaction, height);
			}

			// CHECK IF ORDER CREATION
			if (transaction instanceof CreateOrderTransaction) {
				this.processOrderCreation((CreateOrderTransaction) transaction);
			}

			// CHECK IF ORDER CANCEL
			else if (transaction instanceof CancelOrderTransaction) {
				this.processOrderCancel((CancelOrderTransaction) transaction);
			}

		}

        if (block.blockHead.transactionsCount > 0
				&& start - processBlockLogged > 30000) {
			long tickets = System.currentTimeMillis() - start;
			processBlockLogged = start;
			LOGGER.debug("WALLET [" + block.blockHead.heightBlock + "] processing time: " + tickets * 0.001
					+ " TXs = " + block.blockHead.transactionsCount + " millsec/record:"
					+ tickets / (block.blockHead.transactionsCount + 1));
		}

    }

	void orphanBlock(DCSet dcSet, Block block) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		// ORPHAN ALL TRANSACTIONS IN DB BACK TO FRONT
		if (block == null)
			return;

		int height = block.heightBlock;
		List<Transaction> transactions = block.getTransactions();
		int seqNo;
        for (int i = block.blockHead.transactionsCount - 1; i >= 0; i--) {

			seqNo = i + 1;

			Transaction transaction = transactions.get(i);
			if (transaction.isWiped()) {
				continue;
			}

            if (transaction.noDCSet())
                transaction.setDC(dcSet, Transaction.FOR_NETWORK, block.blockHead.heightBlock, seqNo);

			this.orphanTransaction(transaction);

			// CHECK IF PAYMENT
			if (transaction instanceof RSend) {
				continue;
			}
			// CHECK IF ITEM ISSUE
			else if (transaction instanceof IssueItemRecord) {
				this.orphanItemIssue((IssueItemRecord) transaction);
			}

			// CHECK IF SERTIFY PErSON
			else if (transaction instanceof RSertifyPubKeys) {
				this.orphanSertifyPerson((RSertifyPubKeys) transaction, height);
			}

			// CHECK IF ORDER CREATION
			else if (transaction instanceof CreateOrderTransaction) {
				this.orphanOrderCreation((CreateOrderTransaction) transaction);
			}

			// CHECK IF ORDER CANCEL
			else if (transaction instanceof CancelOrderTransaction) {
				this.orphanOrderCancel((CancelOrderTransaction) transaction);
			}

		}

        Account blockGenerator = block.blockHead.creator;
		String blockGeneratorStr = blockGenerator.getAddress();

		// CHECK IF WE ARE GENERATOR
		if (this.accountExists(blockGeneratorStr)) {
			// DELETE BLOCK
            this.database.getBlocksHeadMap().delete(block.blockHead);

			// SET AS LAST BLOCK
			// this.database.setLastBlockSignature(block.getReference());

			// KEEP TRACK OF UNCONFIRMED BALANCE
            feeProcess(dcSet, block.blockHead.totalFee, blockGenerator, true);

		}

		// SET AS LAST BLOCK
        this.database.setLastBlockSignature(block.blockHead.reference); // .reference

        // long tickets = System.currentTimeMillis() - start;
		// logger.info("WALLET [" + block.getHeightByParent(DCSet.getInstance())
		// + "] orphaning time: " + tickets*0.001
		// + " TXs = " + block.getTransactionCount() + " millsec/record:"
		// + tickets/(block.getTransactionCount()+1) );

    }

	private void processItemIssue(IssueItemRecord issueItem) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		// CHECK IF WE ARE OWNER
		ItemCls item = issueItem.getItem();

		if (this.accountExists(issueItem.getCreator().getAddress())) {
			// ADD ASSET
			this.database.getItemMap(item).add(
					issueItem.getCreator(), issueItem.getDBRef(), item);
			// ADD to FAVORITES
			this.database.getItemFavoritesSet(item).add(item.getKey());

			return;

		}

		// item.resolveKey(DLSet.getInstance());
		Account owner = item.getOwner();

		if (owner != null && this.accountExists(owner.getAddress())) {

			// ADD ASSET
			this.database.getItemMap(item).add(
					owner, issueItem.getDBRef(), item);

			// ADD to FAVORITES
			this.database.getItemFavoritesSet(item).add(item.getKey());
		}
	}

	private void orphanItemIssue(IssueItemRecord issueItem) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		// CHECK IF WE ARE OWNER
		ItemCls item = issueItem.getItem();
		Account creator = item.getOwner();
		if (creator == null)
			return;

		if (this.accountExists(creator.getAddress())) {
			// DELETE ASSET
			this.database.getItemMap(item).delete(creator, issueItem.getDBRef());
		}
	}

	private void processSertifyPerson(RSertifyPubKeys sertifyPubKeys, int height) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		// CHECK IF WE ARE OWNER
		Account creator = sertifyPubKeys.getCreator();
		if (creator == null)
			return;

        addOwnerInFavorites(sertifyPubKeys);

        DCSet db = DCSet.getInstance();

		boolean personalized = false;
		TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> personalisedData = db.getPersonAddressMap().getItems(sertifyPubKeys.getKey());
		if (personalisedData == null || personalisedData.isEmpty()) {
			personalized = true;
		}

		if (!personalized) {
			// IT IS NOT VOUCHED PERSON

			// FIND person
			ItemCls person = db.getItemPersonMap().get(sertifyPubKeys.getKey());
			if (person != null) {
				// FIND issue record
				Transaction transPersonIssue = db.getTransactionFinalMap().get(person.getReference());
				///// GET FEE from that record
				///transPersonIssue.setDC(db, Transaction.FOR_NETWORK); // RECALC FEE if from DB

				// ISSUE NEW COMPU in chain
				BigDecimal issued_FEE_BD = BlockChain.BONUS_FOR_PERSON(height);

				// GIFTs
				if (this.accountExists(transPersonIssue.getCreator().getAddress())) {
					this.database.getAccountMap().changeBalance(transPersonIssue.getCreator().getAddress(),
							false, FEE_KEY, issued_FEE_BD);
				}

				// GIFTs
				if (this.accountExists(creator.getAddress())) {
					this.database.getAccountMap().changeBalance(creator.getAddress(), false, FEE_KEY, issued_FEE_BD);
				}

				PublicKeyAccount pkAccount = sertifyPubKeys.getSertifiedPublicKeys().get(0);
				if (this.accountExists(pkAccount.getAddress())) {
					this.database.getAccountMap().changeBalance(pkAccount.getAddress(), false, FEE_KEY, issued_FEE_BD);
				}
			}
		}
	}

    private void addOwnerInFavorites(RSertifyPubKeys sertifyPubKeys) {
        List<PublicKeyAccount> sertifiedPublicKeys = sertifyPubKeys.getSertifiedPublicKeys();

        for (PublicKeyAccount key : sertifiedPublicKeys) {
            if (this.accountExists(key.getAddress())) {
                long personKey = sertifyPubKeys.getKey();
                this.database.getPersonFavoritesSet().add(personKey);
                break;
            }
        }
    }

    private void orphanSertifyPerson(RSertifyPubKeys sertifyPubKeys, int height) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		Account creator = sertifyPubKeys.getCreator();
		if (creator == null)
			return;

		// GIFTs
		DCSet db = DCSet.getInstance();

		boolean personalized = false;
		TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> personalisedData = db.getPersonAddressMap().getItems(sertifyPubKeys.getKey());
		if (personalisedData == null || personalisedData.isEmpty()) {
			personalized = true;
		}

		if (!personalized) {
			// IT IS NOT VOUCHED PERSON

			// FIND person
			ItemCls person = db.getItemPersonMap().get(sertifyPubKeys.getKey());
			if (person == null)
				return;

			// FIND issue record
			Transaction transPersonIssue = db.getTransactionFinalMap().get(person.getReference());
			//// GET FEE from that record
			///transPersonIssue.setDC(db, Transaction.FOR_NETWORK); // RECALC FEE if from DB

			// ISSUE NEW COMPU in chain
			BigDecimal issued_FEE_BD = BlockChain.BONUS_FOR_PERSON(height);

			// GIFTs
			if (this.accountExists(transPersonIssue.getCreator().getAddress())) {
				this.database.getAccountMap().changeBalance(transPersonIssue.getCreator().getAddress(),
						true, FEE_KEY, issued_FEE_BD);
			}

			// GIFTs
			if (this.accountExists(creator.getAddress())) {
				this.database.getAccountMap().changeBalance(creator.getAddress(), true, FEE_KEY, issued_FEE_BD);
			}

			PublicKeyAccount pkAccount = sertifyPubKeys.getSertifiedPublicKeys().get(0);
			if (this.accountExists(creator.getAddress())) {
				this.database.getAccountMap().changeBalance(pkAccount.getAddress(), true, FEE_KEY, issued_FEE_BD);
			}
		}
	}

	private void processOrderCreation(CreateOrderTransaction orderCreation) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		if(orderCreation.getOrderId() == null)
			return;

		this.addOrder(orderCreation);

	}

	private void addOrder(CreateOrderTransaction orderCreation) {
		// CHECK IF WE ARE CREATOR
		if (this.accountExists(orderCreation.getCreator().getAddress())) {

			// ADD ORDER
			Order orderNew = orderCreation.makeOrder();
			this.database.getOrderMap().add(orderNew);

			// TRADES for TARGETs
			//trades
		}
	}

	private void orphanOrderCreation(CreateOrderTransaction orderCreation) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		if(orderCreation.getOrderId() == null)
			return;

		// CHECK IF WE ARE CREATOR
		if (this.accountExists(orderCreation.getCreator().getAddress())) {
			// DELETE ORDER
			if (false) {
				// order STATUS is ORPHANED
				this.database.getOrderMap().delete(new Tuple2<String, Long>(orderCreation.getCreator().getAddress(),
						Transaction.makeDBRef(orderCreation.getHeightSeqNo())));
			}
		}
	}

	private void processOrderCancel(CancelOrderTransaction orderCancel) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		if (orderCancel.getOrderID() == null)
			return;

		// CHECK IF WE ARE CREATOR
		if (this.accountExists(orderCancel.getCreator().getAddress())) {
			if (false) {
				// DELETE ORDER
				this.database.getOrderMap().delete(new Tuple2<String, Long>(orderCancel.getCreator().getAddress(),
						Transaction.makeDBRef(orderCancel.getHeightSeqNo())));
			}
		}
	}

	private void orphanOrderCancel(CancelOrderTransaction orderCancel) {
		// CHECK IF WALLET IS OPEN
		if (!this.exists()) {
			return;
		}

		if (orderCancel.getOrderID() == null)
			return;

		// CHECK IF WE ARE CREATOR
		if (this.accountExists(orderCancel.getCreator().getAddress())) {
			if (false) {
				// DELETE ORDER
				Order order = DCSet.getInstance().getOrderMap().get(orderCancel.getOrderID());
				this.database.getOrderMap().add(order);
			}
		}
	}

	@SuppressWarnings("unchecked")
    @Override
    public void update(Observable o, Object arg) {
    	if (Controller.getInstance().noUseWallet || Controller.getInstance().noDataWallet)
    		return;

        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
        }
    }

    @SuppressWarnings("unchecked")
    // synchronized нужно чтобы не было конкуренции при this.database.commit();
    public synchronized void syncUpdate(Observable o, Object arg) {

        if (this.database == null)
			return;

        ObserverMessage message = (ObserverMessage) arg;
        int type = message.getType();

        if (type == ObserverMessage.ADD_UNC_TRANSACTION_TYPE) {

            // прилетающие неподтвержденные тоже проверяем и если это относится к нам
            // то закатываем себе в кошелек.
            // потом они при переподтверждении обновятся
            // но если нет то останутся висеть и пользователь сам их должен удалить
            // это как раз сигнал что такая не подтвердилась трнзакция

            Pair<Long, Transaction> item = (Pair<Long, Transaction>) message.getValue();
            Transaction transaction = item.getB();

            List<Account> accounts = this.getAccounts();
            synchronized (accounts) {
                for (Account account : accounts) {
                    // CHECK IF INVOLVED
                    if (transaction.isInvolved(account)) {
                        // ADD TO ACCOUNT TRANSACTIONS
                        if (!this.database.getTransactionMap().add(account, transaction)) {
                            // UPDATE UNCONFIRMED BALANCE for ASSET
                        }
                    }
                }
            }

            return;

        } else if (type == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE) {
            if (Controller.getInstance().useGui) {
                Pair<Tuple2<String, String>, Transaction> item = (Pair<Tuple2<String, String>, Transaction>) message.getValue();
                Transaction transaction = item.getB();
                Library.notifySysTrayRecord(transaction);
            }

            return;

        } else if (type == ObserverMessage.ADD_ORDER_TYPE
				|| type == ObserverMessage.ADD_COMPL_ORDER_TYPE) {
            // UPDATE FULFILLED
            Order order = (Order) message.getValue();
            if (!this.accountExists(order.getCreator().getAddress()))
                return;

            Tuple2<String, Long> key = new Tuple2<String, Long>(order.getCreator().getAddress(), order.getId());
            if (this.database.getOrderMap().contains(key)) {
                this.database.getOrderMap().set(key, order);
            }

            return;

        }

        //////////// PROCESS BLOCKS ////////////
		DCSet dcSet = DCSet.getInstance();

        if (this.synchronizeStatus) {
            // идет синхронизация кошелька уже - не обрабатываем блоки тут
            return;
        }

        if (type == ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE)
        {

            Block block = (Block) message.getValue();

            // TODO сделать фактори которая синхронно по оереди с синхронизацией это будет разруливать
            // CHECK IF WE NEED TO RESYNC
            // BY SIGNATURE !!!!
            if (checkNeedSyncWallet(block.getSignature())) {
                return;
            }

            // CHECK BLOCK
			this.orphanBlock(dcSet, block);

            //this.database.clearCache();
            //this.database.commit();
        } else if (type == ObserverMessage.CHAIN_ADD_BLOCK_TYPE) {

            Block block = (Block) message.getValue();

			// TODO сделать фактори которая синхронно по оереди с синхронизацией это будет разруливать

			// CHECK IF WE NEED TO RESYNC
			// BY REFERENCE !!!!
            if (checkNeedSyncWallet(block.getReference())) {
				return;
			}

			// CHECK BLOCK
			this.processBlock(DCSet.getInstance(), block);

        }

    }

	// CLOSE

	public void close() {

        if (this.timerSynchronize != null)
            this.timerSynchronize.cancel();

        if (this.lockTimer != null)
            this.lockTimer.cancel();

        if (this.database != null) {
			this.database.close();
		}

		if (this.secureDatabase != null) {
			this.secureDatabase.close();
		}
	}

	public void commit() {
		if (this.database != null) {
			this.database.hardFlush();
		}

		if (this.secureDatabase != null) {
			this.secureDatabase.commit();
		}

	}

	public byte[] getLastBlockSignature() {
		return this.database.getLastBlockSignature();
	}

	public long getLicenseKey() {
		if (this.database == null || this.database.getLicenseKey() == null) {
			return 2l;
		}

		return this.database.getLicenseKey();
	}

	public void setLicenseKey(long key) {
		this.database.setLicenseKey(key);
	}

	public Integer loadFromDir(boolean withObserver, boolean dynamicGUI) {
		// return 1 - is ok
		// if > 1 - error
		String pathOld = Settings.getInstance().getWalletDir();
		JFileChooser fileopen = new JFileChooser();
		fileopen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		String path = Settings.getInstance().getWalletDir();
		File ff = new File(path);
		if (!ff.exists())
			path = ".." + File.separator;
		fileopen.setCurrentDirectory(new File(path));
		int ret = fileopen.showDialog(null, Lang.getInstance().translate("Open Wallet Dir"));
		if (ret == JFileChooser.APPROVE_OPTION) {
			String dir = fileopen.getSelectedFile().toString();

			// set wallet dir
			Settings.getInstance().setWalletDir(dir);
			// open wallet
			Controller.getInstance().wallet = new Wallet(withObserver, dynamicGUI);
			// not wallet return 0;
			if (!Controller.getInstance().wallet.exists()) return 2;
			// accounts
			List<Account> aa = Controller.getInstance().wallet.getAccounts();
			if (Controller.getInstance().wallet.getAccounts().size() < 1) return 5;
			if (Controller.getInstance().wallet.isWalletDatabaseExisting()) {
				Controller.getInstance().wallet.initiateItemsFavorites();
				// save path from setting json
				JSONObject settingsLangJSON = new JSONObject();
				settingsLangJSON.putAll(Settings.getInstance().read_setting_JSON());
				Settings.getInstance().setWalletDir(dir);
				settingsLangJSON.put("walletdir", Settings.getInstance().getWalletDir());
				try {
					SaveStrToFile.saveJsonFine(Settings.getInstance().getSettingsPath(), settingsLangJSON);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// is ok
				return 1;

			}


		}
		//is abort
		return 3;
	}
}
