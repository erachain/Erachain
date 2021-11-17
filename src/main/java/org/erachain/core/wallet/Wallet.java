package org.erachain.core.wallet;

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
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.*;
import org.erachain.dapp.DAPP;
import org.erachain.database.DBASet;
import org.erachain.database.wallet.*;
import org.erachain.datachain.BlockMap;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.ObserverWaiter;
import org.erachain.gui.library.FileChooser;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Timer;
import java.util.*;

/**
 * обработка секретных ключей и моих записей, которые относятся к набору моих счетов
 */
public class Wallet extends Observable implements Observer {

	static final boolean CHECK_CHAIN_BROKENS_ON_SYNC_WALLET = false;

	public static final int STATUS_UNLOCKED = 1;
	public static final int STATUS_LOCKED = 0;

	private static final long RIGHTS_KEY = Transaction.RIGHTS_KEY;
	private static final long FEE_KEY = Transaction.FEE_KEY;
	static Logger LOGGER = LoggerFactory.getLogger(Wallet.class.getSimpleName());

	public DWSet dwSet;
	private SecureWalletDatabase secureDatabase;

	AssetsFavorites assetsFavorites;
	TemplatesFavorites templatesFavorites;
	PersonsFavorites personsFavorites;
	private int secondsToUnlock = 100;
	private Timer lockTimer; // = new Timer();
	private int syncHeight;
	public WalletUpdater walletUpdater;
	DCSet dcSet;

	private List<ObserverWaiter> waitingObservers = new ArrayList<>();
	// CONSTRUCTORS

	public Wallet(DCSet dcSet, boolean withObserver, boolean dynamicGUI) {

		this.dcSet = dcSet;
		//this.syncHeight = ;

		// CHECK IF EXISTS
		if (this.walletKeysExists()) {
			// OPEN WALLET
			this.dwSet = DWSet.reCreateDB(dcSet, withObserver, dynamicGUI);

			linkWaitingObservers(withObserver);

			walletUpdater = new WalletUpdater(Controller.getInstance(), this);

			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_DB_OPEN, this));

		}

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
		return this.dwSet.getVersion();
	}

	public boolean isUnlocked() {
		return this.secureDatabase != null;
	}
	public boolean isUnlockedForRPC() {
		// Если раслочено на все время
		return this.secureDatabase != null && this.secondsToUnlock < 0;
	}

	public List<Account> getAccounts() {
		if (this.dwSet == null)
			return new ArrayList<>();

		return this.dwSet.getAccountMap().getAccounts();
	}

	public List<Account> getAccountsAndSetBalancePosition(int position) {
		if (this.dwSet == null)
			return new ArrayList<>();

		List<Account> accounts = this.dwSet.getAccountMap().getAccounts();
		for (Account account : accounts) {
			account.setViewBalancePosition(position);
		}

		return accounts;
	}

	public List<PublicKeyAccount> getPublicKeyAccounts() {
		if (this.dwSet == null)
			return new ArrayList<>();


		AccountMap mapAccs = this.dwSet.getAccountMap();
		//synchronized (mapAccs) { // else deadlock in org.erachain.database.wallet.AccountMap.add
		return mapAccs.getPublicKeyAccounts();
		//}
	}

	public List<Tuple2<Account, Long>> getAccountsAssets() {
		return this.dwSet.getAccountMap().getAccountsAssets();
	}

	public boolean accountExists(String address) {
		return this.dwSet.getAccountMap().exists(address);
	}

	public boolean accountExists(Account account) {
		return this.dwSet.getAccountMap().exists(account);
	}

	public Account getAccount(String address) {
		return this.dwSet.getAccountMap().getAccount(address);
	}

	public boolean isWalletDatabaseExisting() {
		return dwSet != null;
	}

	public Tuple3<BigDecimal, BigDecimal, BigDecimal> getUnconfirmedBalance(Account account, long key) {

		return this.dwSet.getAccountMap().getBalance(account, key);
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

	public PrivateKeyAccount getPrivateKeyAccount(Account account) {
		if (this.secureDatabase == null) {
			return null;
		}

		return this.secureDatabase.getAccountSeedMap().getPrivateKeyAccount(account);
	}

	public void addWaitingObserver(ObserverWaiter observer) {
		waitingObservers.add(observer);
	}

	public void removeWaitingObserver(ObserverWaiter observer) {
		waitingObservers.remove(observer);
	}

	public static boolean walletKeysExists() {
		if (Controller.getInstance().noUseWallet || Settings.SECURE_WALLET_FILE == null) {
			return false;
		}
		return Settings.SECURE_WALLET_FILE.exists();
	}

	public List<Pair<Account, Transaction>> getLastTransactions(int limit) {
		if (!this.walletKeysExists()) {
			new ArrayList<Pair<Account, Transaction>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.dwSet.getTransactionMap().get(accounts, limit);
	}

	public Iterator<Tuple2<Long, Integer>> getTransactionsIteratorByType(int type, boolean descending) {
		if (!this.walletKeysExists()) {
			return null;
		}

		return this.dwSet.getTransactionMap().getTypeIterator((byte) type, descending);

	}

	public Transaction getTransaction(Tuple2<Long, Integer> key) {
		if (!this.walletKeysExists()) {
			return null;
		}

		return this.dwSet.getTransactionMap().get(key);

	}

	public List<Transaction> getLastTransactions(Account account, int limit) {
		if (!this.walletKeysExists()) {
			return new ArrayList<Transaction>();
		}

		return this.dwSet.getTransactionMap().get(account, limit);
	}

	public List<Pair<Account, Block.BlockHead>> getLastBlocks(int limit) {
		if (!this.walletKeysExists()) {
			return new ArrayList<Pair<Account, Block.BlockHead>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.dwSet.getBlocksHeadMap().get(accounts, limit);
	}

	public List<Block.BlockHead> getLastBlocks(Account account, int limit) {
		if (!this.walletKeysExists()) {
			return new ArrayList<Block.BlockHead>();
		}

		return this.dwSet.getBlocksHeadMap().get(account, limit);
	}

	public void addAddressFavorite(String address, String pubKey, String name, String description) {
		if (!this.walletKeysExists()) {
			return;
		}

		this.dwSet.addAddressFavorite(address, pubKey, name, description);
	}

	public void addItemFavorite(ItemCls item) {
		if (!this.walletKeysExists()) {
			return;
		}

		this.dwSet.addItemFavorite(item);
	}

	public void removeItemFavorite(ItemCls item) {
		if (!this.walletKeysExists()) {
			return;
		}

		this.dwSet.deleteItemFavorite(item);
	}

	public boolean isItemFavorite(ItemCls item) {
		if (!walletKeysExists()) {
			return false;
		}
		return dwSet.isItemFavorite(item);
	}

	public void addDocumentFavorite(Transaction transaction) {
		if (!this.walletKeysExists()) {
			return;
		}

		this.dwSet.addDocumentToFavorite(transaction);
	}

	public void removeDocumentFavorite(Transaction transaction) {
		if (!this.walletKeysExists()) {
			return;
		}

		this.dwSet.removeDocumentFromFavorite(transaction);
	}

	public boolean isDocumentFavorite(Transaction transaction) {
		if (!this.walletKeysExists()) {
			return false;
		}

		return this.dwSet.isDocumentFavorite(transaction);
	}

	public void addTransactionFavorite(Transaction transaction) {
		if (!this.walletKeysExists()) {
			return;
		}

		this.dwSet.addTransactionToFavorite(transaction);
	}

	public void removeTransactionFavorite(Transaction transaction) {
		if (!this.walletKeysExists()) {
			return;
		}

		this.dwSet.removeTransactionFromFavorite(transaction);
	}

	public boolean isTransactionFavorite(Transaction transaction) {
		if (!this.walletKeysExists()) {
			return false;
		}

		return this.dwSet.isTransactionFavorite(transaction);
	}

	private String saveFavoritesLong(FavoriteItemMap map, String name) throws IOException {

		String out = "";

		Object key;
		try (IteratorCloseable iterator = map.getIterator()) {
			JSONArray jsonArray = new JSONArray();
			while (iterator.hasNext()) {
				jsonArray.add(iterator.next());
			}
			out += "\"" + name + "\":" + jsonArray.toJSONString() + ",";
		} catch (IOException e) {
			throw e;
		}

		return out;
	}

	public String saveFavorites() {

		String out = "{";

		Object key;
		FavoriteAccountsMap accountsMap = dwSet.getFavoriteAccountsMap();
		try (IteratorCloseable iterator = accountsMap.getIterator()) {
			JSONObject accountsJson = new JSONObject();
			JSONArray jsonRow;
			while (iterator.hasNext()) {
				key = iterator.next();
				Tuple3<String, String, String> value = accountsMap.get((String) key);
				jsonRow = new JSONArray();
				jsonRow.add(value.a);
				jsonRow.add(value.b);
				jsonRow.add(value.c);
				accountsJson.put(key, jsonRow);
			}
			out += "\"accounts\":" + accountsJson.toJSONString() + ",";
		} catch (IOException e) {
			return "ERROR: on getFavoriteAccountsMap" + e.getMessage();
		}

		try {
			out += saveFavoritesLong(dwSet.getAssetFavoritesSet(), "assets");
		} catch (IOException e) {
			return "assets " + e;
		}
		try {
			out += saveFavoritesLong(dwSet.getDocumentFavoritesSet(), "documents");
		} catch (IOException e) {
			return "documents " + e;
		}
		try {
			out += saveFavoritesLong(dwSet.getImprintFavoritesSet(), "imprints");
		} catch (IOException e) {
			return "imprints " + e;
		}
		try {
			out += saveFavoritesLong(dwSet.getPersonFavoritesSet(), "persons");
		} catch (IOException e) {
			return "persons " + e;
		}
		try {
			out += saveFavoritesLong(dwSet.getPollFavoritesSet(), "polls");
		} catch (IOException e) {
			return "polls " + e;
		}
		try {
			out += saveFavoritesLong(dwSet.getStatusFavoritesSet(), "statuses");
		} catch (IOException e) {
			return "statuses " + e;
		}
		try {
			out += saveFavoritesLong(dwSet.getTemplateFavoritesSet(), "templates");
		} catch (IOException e) {
			return "templates " + e;
		}
		try {
			out += saveFavoritesLong(dwSet.getUnionFavoritesSet(), "unions");
		} catch (IOException e) {
			return "unions " + e;
		}
		try {
			out += saveFavoritesLong(dwSet.getTransactionFavoritesSet(), "txs");
		} catch (IOException e) {
			return "txs " + e;
		}

		out += "}";
		Library.saveToFile(null, out, "favorites", "JSON", "json");

		return null;
	}

	private String loadFavoritesLong(JSONObject json, FavoriteItemMap map, String name) {

		try {
			JSONArray rows = (JSONArray) json.get(name);
			for (Object key : rows) {
				if (((Long) key) <= 0)
					return name + " wrong value: " + key;

				map.add((Long) key);
			}
		} catch (Exception e) {
			return name + " - " + e;
		}

		return null;
	}

	public String loadFavorites() {

		FileChooser chooser = new FileChooser();
		chooser.setDialogTitle(Lang.T("Open File"));
		chooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Favorites", "json");
		chooser.setFileFilter(filter);

		String res = "";
		if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			String pp = chooser.getSelectedFile().getPath();
			File ff = new File(pp);

			try {
				BufferedReader in = new BufferedReader(new FileReader(ff));
				String str;
				while ((str = in.readLine()) != null) {
					res += (str);
				}
				in.close();
			} catch (IOException e) {
				return e.getMessage();
			}

			JSONObject json = null;
			try {
				json = (JSONObject) JSONValue.parseWithException(res);
			} catch (Exception e) {
				String error = e.getMessage();
				return e.toString();
			}

			JSONObject rows = (JSONObject) json.get("accounts");
			for (Object key : rows.keySet()) {
				JSONArray row = (JSONArray) rows.get(key);
				dwSet.addAddressFavorite((String) key, (String) row.get(0), (String) row.get(1), (String) row.get(2));
			}

			String result = loadFavoritesLong(json, dwSet.getAssetFavoritesSet(), "assets");
			if (result != null)
				return result;
			result = loadFavoritesLong(json, dwSet.getPersonFavoritesSet(), "persons");
			if (result != null)
				return result;
			result = loadFavoritesLong(json, dwSet.getDocumentFavoritesSet(), "documents");
			if (result != null)
				return result;
			result = loadFavoritesLong(json, dwSet.getImprintFavoritesSet(), "imprints");
			if (result != null)
				return result;
			result = loadFavoritesLong(json, dwSet.getPersonFavoritesSet(), "persons");
			if (result != null)
				return result;
			result = loadFavoritesLong(json, dwSet.getPollFavoritesSet(), "polls");
			if (result != null)
				return result;
			result = loadFavoritesLong(json, dwSet.getStatusFavoritesSet(), "statuses");
			if (result != null)
				return result;
			result = loadFavoritesLong(json, dwSet.getTemplateFavoritesSet(), "templates");
			if (result != null)
				return result;
			result = loadFavoritesLong(json, dwSet.getUnionFavoritesSet(), "unions");
			if (result != null)
				return result;
			result = loadFavoritesLong(json, dwSet.getTransactionFavoritesSet(), "txs");
			if (result != null)
				return result;

		}

		dwSet.hardFlush();


		return null;
	}


	// CREATE
	public synchronized boolean create(byte[] seed, String password, int depth, boolean synchronize, String path,
									   boolean withObserver, boolean dynamicGUI) {
		String oldPath = Settings.getInstance().getWalletKeysPath();
		// set wallet dir
		Settings.getInstance().setWalletKeysPath(path);

		if (this.dwSet != null) {
			this.dwSet.close();
		}
		// OPEN WALLET
		this.dwSet = DWSet.reCreateDB(dcSet, withObserver, dynamicGUI);

		if (this.secureDatabase != null) {
			// CLOSE secured WALLET
			lock();
		}

		// OPEN SECURE WALLET
		SecureWalletDatabase secureDatabase = new SecureWalletDatabase(password);

		// CREATE
		boolean res = this.create(secureDatabase, seed, depth, synchronize, withObserver);
		if (res) {
			// save wallet dir
			Settings.getInstance().updateSettingsValue();
		} else {
			Settings.getInstance().setWalletKeysPath(oldPath);
		}

		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_DB_OPEN, this));

		return res;
	}

	public synchronized boolean create(SecureWalletDatabase secureDatabase, byte[] seed, int depth,
									   boolean synchronize, boolean withObserver) {

		// CREATE SECURE WALLET
		this.secureDatabase = secureDatabase;

		// SET LICENSE KEY
		this.setLicenseKey(Controller.LICENSE_VERS);

		// ADD SEED
		try {
			this.secureDatabase.setSeed(seed);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}

		// CREATE ACCOUNTS
		for (int i = 1; i <= depth; i++) {
			this.generateNewAccount();
		}

		linkWaitingObservers(withObserver);

		// SCAN TRANSACTIONS
		if (synchronize) {
			this.synchronizeFull();
		}

		// COMMIT
		this.commit();

		walletUpdater = new WalletUpdater(Controller.getInstance(), this);

		return true;
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
		int nonce = this.secureDatabase.getNonce();

		// GENERATE ACCOUNT SEED for next NONCE
		byte[] accountSeed = generateAccountSeed(seed, nonce);
		PrivateKeyAccount account = new PrivateKeyAccount(accountSeed);

		// CHECK IF ACCOUNT ALREADY EXISTS
		if (!this.accountExists(account)) {

			// ADD TO DATABASE
			this.secureDatabase.addPrivateKey(account);
			this.dwSet.getAccountMap().add(account, nonce + 1);

			LOGGER.info("Added account #" + (nonce + 1));

			this.commit();

			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));

		}

		return account.getAddress();
	}

	/**
	 * Clear and load account from WalletKeys secret database
	 */
	public void updateAccountsFromSecretKeys() {

		if (dwSet == null)
			return;

		int number = 0;
		/// deadlock org.erachain.database.wallet.AccountMap
		AccountMap mapAccs = dwSet.getAccountMap();
		synchronized (mapAccs) {
			mapAccs.clear();
			for (PrivateKeyAccount privateAccount : getprivateKeyAccounts()) {
				mapAccs.add(privateAccount, ++number);
			}
		}

		dwSet.hardFlush();

	}

	// SYNCRHONIZE

	// UPDATE all accounts for all assets unconfirmed balance
	public void update_account_assets() {
		List<Tuple2<Account, Long>> accounts_assets = this.getAccountsAssets();

		for (Tuple2<Account, Long> account_asset : accounts_assets) {
			this.dwSet.getAccountMap().changeBalance(account_asset.a.getAddress(), false, account_asset.b,
					BigDecimal.ZERO, false, false);
		}

	}

	/**
	 * нужно для запрета вызова уже работающего процесса синхронизации
	 */
	public boolean synchronizeBodyUsed;

	public void synchronizeBody(boolean reset) {

		synchronizeBodyUsed = true;

		Block blockStart;
		int height;

		if (reset) {
			LOGGER.info("   >>>>  try to Reset maps");

			walletUpdater.lastBlocks.clear();

			// SAVE transactions file
			this.dwSet.hardFlush();
			this.dwSet.clearCache();

			// RESET MAPS
			try {
				dwSet.clear(false);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);

				// видимо цепочку другую взяли и в ней таких сущностей нет и падает на создании меток
				// поставим версию невалидную чтобы база пересоздалась сама
				DBASet.setVersion(this.dwSet.database, 1);
				this.dwSet.hardFlush();

				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_DB_CLOSED, true));

				this.dwSet.close();
				dwSet = DWSet.reCreateDB(dcSet, dwSet.isWithObserver(), dwSet.isDynamicGUI());

				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_DB_OPEN, this));

			}

			LOGGER.info("   >>>>  Maps was Resetting");

			// REPROCESS BLOCKS
			blockStart = new GenesisBlock();
			this.dwSet.setLastBlockSignature(blockStart.getReference());

		} else {

			byte[] lastSignature = this.dwSet.getLastBlockSignature();

			if (lastSignature == null) {
				LOGGER.debug("   >>>>  WALLET SYNCHRONIZE cancel by lastSignature = null");
				Controller.getInstance().walletSyncStatusUpdate(0);
				// выходим и потом пересинхронизируемся с начала
				return;
			}

			blockStart = dcSet.getBlockSignsMap().getBlock(lastSignature);

			if (blockStart == null) {
				LOGGER.debug("   >>>>  WALLET SYNCHRONIZE cancel by blockStart = null");
				Controller.getInstance().walletSyncStatusUpdate(0);
				// выходим и потом пересинхронизируемся с начала
				return;
			}
		}

		// SAVE transactions file
		this.dwSet.hardFlush();
		this.dwSet.clearCache();

		if (Controller.getInstance().isOnStopping())
			return;

		height = blockStart.getHeight();
		int steepHeight = dcSet.getBlockMap().size() / 100;
		int lastHeight = 0;

		long timePoint = System.currentTimeMillis();
		BlockMap blockMap = dcSet.getBlockMap();

		LOGGER.info("   >>>>  WALLET SYNCHRONIZE from: " + height);

		try {
			if (getAccounts() != null && !getAccounts().isEmpty()) {
				do {

					Block block = blockMap.getAndProcess(height);

					if (block == null) {
						break;
					}

					try {
						this.processBlock(block);
					} catch (java.lang.OutOfMemoryError e) {
						LOGGER.error(e.getMessage(), e);
						// внутрення ошибка - выходим для лога
						Controller.getInstance().stopAndExit(644);
						return;
					} finally {
						block.close();
					}

					if (System.currentTimeMillis() - timePoint > 10000
							|| steepHeight < height - lastHeight) {

						timePoint = System.currentTimeMillis();
						lastHeight = height;

						this.syncHeight = height;

						//logger.debug("try Commit");
						this.dwSet.commit();

						if (Controller.getInstance().isOnStopping())
							return;

						// обязательно нужно чтобы память освобождать
						// и если объект был изменен (с тем же ключем у него удалили поле внутри - чтобы это не выдавлось
						// при новом запросе - иначе изменения прилетают в другие потоки и ошибку вызываю
						// БЕЗ очистки КЭША HEAP забивается под завязку
						dcSet.clearCache();
						this.dwSet.clearCache();

						// не нужно - Ява сама норм делает вызов очистки
						//System.gc();

						Controller.getInstance().walletSyncStatusUpdate(height);

					}

					height++;

				} while (synchronizeBodyUsed
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

			// тут возможно цепочка синхронизировалась или начала синхронизироваться и КОММИТ вызовет ошибку
			//  java.io.IOException: Запрошенную операцию нельзя выполнить для файла с открытой пользователем сопоставленной секцией
			this.dwSet.hardFlush();

			// обязательно нужно чтобы память освобождать
			// и если объект был изменен (с тем же ключем у него удалили поле внутри - чтобы это не выдавлось
			// при новом запросе - иначе изменения прилетают в другие потоки и ошибку вызывают
			// вдобавое отчищает полностью память - много свободной памяти получаем
			dcSet.clearCache();

			this.dwSet.clearCache();

			System.gc();

			Controller.getInstance().walletSyncStatusUpdate(height);

			// RESET UNCONFIRMED BALANCE for accounts + assets
			LOGGER.info("Resetted balances");
			update_account_assets();
			Controller.getInstance().walletSyncStatusUpdate(0);

			LOGGER.info("Update Orders");
			this.dwSet.getOrderMap().updateLefts();

			LOGGER.info(" >>>>>>>>>>>>>>> *** Synchronizing wallet DONE on: " + height);

			synchronizeBodyUsed = false;

		}

	}

	public void synchronizeFull() {
		walletUpdater.setGoSynchronize(true);
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

	public boolean unlock(SecureWalletDatabase secureDatabase) {

		synchronized (secureDatabase) {
			if (this.secureDatabase != null) {
				this.secureDatabase.close();
				this.secureDatabase = null;
			}

			this.secureDatabase = secureDatabase;
		}

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

		synchronized (secureDatabase) {
			// CLOSE
			if (this.secureDatabase != null) {
				this.secureDatabase.close();
				this.secureDatabase = null;
			}
		}

		if (Controller.getInstance().useGui) {
			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_STATUS, STATUS_LOCKED));
		}

		this.secondsToUnlock = 100;
		if (this.lockTimer != null)
			this.lockTimer.cancel();

		// LOCK SUCCESSFUL
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
		if (!this.accountExists(account)) {
			// ADD TO DATABASE
			this.dwSet.getAccountMap().add(account, this.secureDatabase.addPrivateKey(account));

			// SAVE TO DISK
			this.dwSet.hardFlush();

			// SYNCHRONIZE
			this.synchronizeFull();

			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));

			// RETURN
			return account.getAddress();
		}

		return "";
	}

	public Tuple3<String, Integer, String> importPrivateKey(byte[] privateKey64) {
		// CHECK IF WALLET IS OPEN
		if (!this.isUnlocked()) {
			return new Tuple3<>(null, -1, "Wallet is locked");
		}

		// CHECK LENGTH
		if (privateKey64.length != Crypto.SIGNATURE_LENGTH) {
			return new Tuple3<>(null, -1, "Wrong length != 64");
		}

		// CREATE ACCOUNT
		PrivateKeyAccount account = new PrivateKeyAccount(privateKey64);

		// CHECK IF ACCOUNT ALREADY EXISTS
		if (this.accountExists(account))
			return new Tuple3<>(null, 0, "Already exist");

		// ADD TO DATABASE
		this.dwSet.getAccountMap().add(account, this.secureDatabase.addPrivateKey(account));

		// SAVE TO DISK
		this.dwSet.hardFlush();

		// SYNCHRONIZE
		this.synchronizeFull();

		// NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));

		// RETURN
		return new Tuple3<>(account.getAddress(), null, null);

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

	public void linkWaitingObservers(boolean withObserver) {
		// добавим теперь раз кошелек открылся все ожидающие связи на наблюдения
		for (ObserverWaiter observer : waitingObservers) {
			observer.addObservers();
		}
		waitingObservers.clear();

		if (withObserver) {
			// ADD OBSERVERs

			// REGISTER ON ORDERS - foe BELLs on incomed TRADES
			this.dwSet.getOrderMap().addObserver(this);
			this.dwSet.getTelegramsMap().addObserver(this);

		}

	}

	@Override
	public void addObserver(Observer o) {

		super.addObserver(o);

		if (false && Controller.getInstance().doesWalletDatabaseExists()) {

			// REGISTER ON ACCOUNTS
			this.dwSet.getAccountMap().addObserver(o);

			// REGISTER ON TRANSACTIONS
			this.dwSet.getTransactionMap().addObserver(o);

			// REGISTER ON BLOCKS
			this.dwSet.getBlocksHeadMap().addObserver(o);

			// REGISTER ON ASSETS
			this.dwSet.getAssetMap().addObserver(o);

			// REGISTER ON IMPRINTS
			this.dwSet.getImprintMap().addObserver(o);

			// REGISTER ON TEMPLATES
			this.dwSet.getTemplateMap().addObserver(o);

			// REGISTER ON PERSONS
			this.dwSet.getPersonMap().addObserver(o);

			// REGISTER ON STATUS
			this.dwSet.getStatusMap().addObserver(o);

			// REGISTER ON UNION
			this.dwSet.getUnionMap().addObserver(o);

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
			this.dwSet.getAssetFavoritesSet().addObserver(o);

			// REGISTER ON PLATE FAVORITES
			this.dwSet.getTemplateFavoritesSet().addObserver(o);

			// REGISTER ON PERSON FAVORITES
			this.dwSet.getPersonFavoritesSet().addObserver(o);

			// REGISTER ON STATUS FAVORITES
			this.dwSet.getStatusFavoritesSet().addObserver(o);

			// REGISTER ON UNION FAVORITES
			this.dwSet.getUnionFavoritesSet().addObserver(o);

		}
	}

	private void deal_transaction(Account account, Transaction transaction, boolean asOrphan) {

		transaction.setDC(dcSet, true);
		// UPDATE UNCONFIRMED BALANCE for ASSET
		// TODO: fee doubled?
		long absKey = transaction.getAbsKey();
		String address = account.getAddress();


		BigDecimal fee = transaction.getFee(account);
		boolean isBackward = false;
		boolean isDirect = false;
		if (absKey > 0) {
			// ASSET TRANSFERED + FEE
			BigDecimal amount = transaction.getAmount(account);
			if (transaction instanceof RSend) {
				RSend rSend = (RSend) transaction;
				isBackward = rSend.isBackward();
				isDirect = rSend.getAsset().isSelfManaged();
			}

			if (fee.compareTo(BigDecimal.ZERO) != 0) {
				if (absKey == FEE_KEY) {
					amount = amount.subtract(fee);
				}
			}
			this.dwSet.getAccountMap().changeBalance(address, !asOrphan, transaction.getKey(), amount, isBackward, isDirect);
		} else {
			// ONLY FEE
			if (fee.compareTo(BigDecimal.ZERO) != 0) {
				this.dwSet.getAccountMap().changeBalance(address, !asOrphan, FEE_KEY, fee, isBackward, false);
			}
		}

	}

	private static final Account[] acctArrayCLS = new Account[]{};

	public Account[] getInvolvedAccounts(Transaction transaction) {

		// CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return null;
		}

		List<Account> involved = new ArrayList<>();

		// FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		synchronized (accounts) {
			for (Account account : accounts) {
				// CHECK IF INVOLVED
                if (transaction.isInvolved(account)) {
                    // ADD TO ACCOUNT TRANSACTIONS
                    involved.add(account);
                }
            }
        }
        return involved.toArray(acctArrayCLS);

    }

    public Account getInvolvedAccount(Transaction transaction) {

        // CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return null;
		}

        // FOR ALL ACCOUNTS
        List<Account> accounts = this.getAccounts();
		//synchronized (accounts) {
            for (Account account : accounts) {
                // CHECK IF INVOLVED
                if (transaction.isInvolved(account)) {
                    // ADD TO ACCOUNT TRANSACTIONS
                    return account;
                }
            }
		//}
        return null;

    }

    private static final Integer[] intArrayCLS = new Integer[]{};

    public Integer[] getInvolvedAccountHashes(Transaction transaction) {

        // CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return null;
		}

		List<Integer> involved = new ArrayList<>();

		// FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		synchronized (accounts) {
			for (Account account : accounts) {
				// CHECK IF INVOLVED
				if (transaction.isInvolved(account)) {
					// ADD TO ACCOUNT TRANSACTIONS
					involved.add(account.hashCode());
				}
			}
		}
		return involved.toArray(intArrayCLS);

	}


	private void updateFavorites(Transaction transaction, boolean asOrphan) {

		Object[][] itemKeys = transaction.getItemsKeys();
		if (itemKeys == null || transaction.getAbsKey() == 0) {
			// если созданная мной транзакция то она ЕЩЕ не обросла мясом и тут все Сущности пустые и ключей не будет
			return;
		}

		long key;
		FavoriteItemMap map;
		for (Object[] itemKey : itemKeys) {
			map = this.dwSet.getItemFavoritesSet((int) itemKey[0]);
			key = (Long) itemKey[1];
			if (key <= 0)
				continue;

			if (asOrphan) {
				map.delete(key);
			} else {
				if (!map.contains(key)) {
					map.add(key);
				}
			}
		}

		DAPP dapp = transaction.getSmartContract();
		if (dapp != null) {
			for (Object[] itemKey : dapp.getItemsKeys()) {
				map = this.dwSet.getItemFavoritesSet((int) itemKey[0]);
				key = (Long) itemKey[1];
				if (key <= 0)
					continue;

				if (asOrphan) {
					map.delete(key);
				} else {
					if (!map.contains(key)) {
						map.add(key);
					}
				}
			}
		}
	}

	/**
	 * Тут просто неподтвержденную прибавим ко всем учавствующим счетам и все
	 *
	 * @param transaction
	 */
	public void insertUnconfirmedTransaction(Transaction transaction) {
		// CHECK IF WALLET IS OPEN
		if (!this.isWalletDatabaseExisting()) {
			return;
		}

		// FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		for (Account account : accounts) {
			// CHECK IF INVOLVED
			if (transaction.isInvolved(account)) {
				// ADD TO ACCOUNT TRANSACTIONS
				this.dwSet.getTransactionMap().set(account, transaction);

			}
		}
	}

	/**
	 * Внимание! Здесь нельзя делать выход если один раз счет совпал - так как иначе не правильно обработаются балансы
	 * и Входящая / Исходящая транзакции, например по АПИ. Поэтому если в одном кошельке
	 * несколько счетов к которым эта транзакция подходит - то она добавится в кошелек длля каждого из них
	 * Это правильно и так и должно быть!
	 *
	 * @param transaction
	 */
	public boolean processTransaction(Transaction transaction) {
		// CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return false;
		}

		/// сейчас блок с мясом приходит - transaction.updateFromStateDB();

		// FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		boolean isInvolved = false;
		for (Account account : accounts) {
			// CHECK IF INVOLVED
			if (transaction.isInvolved(account)) {
				isInvolved = true;
				// ADD TO ACCOUNT TRANSACTIONS
				this.dwSet.getTransactionMap().set(account, transaction);
				// UPDATE BALANCE
				deal_transaction(account, transaction, false);
			}
		}

		if (!isInvolved)
			return false;


		// ADD SENDER to FAVORITES
		updateFavorites(transaction, false);

		PublicKeyAccount creator = transaction.getCreator();
		if (creator != null && !accountExists(creator) && !this.dwSet.getAccountMap().exists(creator)) {
			String title = transaction.getTitle();
			Tuple2<Integer, PersonCls> personItem = creator.getPerson();
			if (personItem != null && personItem.b != null)
				title = personItem.b.getName() + " - " + title;

			String description = "";
			if (transaction instanceof RSend) {
				RSend rSend = (RSend) transaction;
				if (!rSend.isEncrypted() && rSend.isText())
					description = rSend.viewData();
			} else if (transaction instanceof RSignNote) {
				RSignNote rNote = (RSignNote) transaction;
				if (!rNote.isEncrypted() && rNote.isText())
					description = rNote.getMessage();
			}
			addAddressFavorite(creator.getAddress(), creator.getBase58(),
					title == null || title.isEmpty() ? "" : title, description);
		}

		// CHECK IF ITEM ISSUE
		else if (transaction instanceof IssueItemRecord) {
			this.processItemIssue((IssueItemRecord) transaction);
		}

		// CHECK IF SERTIFY PErSON
		else if (transaction instanceof RCertifyPubKeys) {
			this.processCertifyPerson((RCertifyPubKeys) transaction);
		}

		// CHECK IF ORDER CREATION
		if (transaction instanceof CreateOrderTransaction) {
			this.processOrderCreation((CreateOrderTransaction) transaction);
		}

		// CHECK IF ORDER CHANGE
		else if (transaction instanceof ChangeOrderTransaction) {
			this.processOrderChanging((ChangeOrderTransaction) transaction);
		}

		return true;
	}

	private void processATTransaction(Tuple2<Tuple2<Integer, Integer>, ATTransaction> atTx) {
		// CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return;
		}

		// FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		synchronized (accounts) {
			for (Account account : accounts) {
				// CHECK IF INVOLVED
				if (atTx.b.getRecipient() == account.getAddress()) {
					this.dwSet.getAccountMap().changeBalance(account.getAddress(), false, atTx.b.getKey(),
							BigDecimal.valueOf(atTx.b.getAmount()), false, false);

				}
			}
		}
	}

	private void orphanTransaction(Transaction transaction) {
		// CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return;
		}

		/// FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		boolean isInvolved = false;

		for (Account account : accounts) {
			// CHECK IF INVOLVED
			if (transaction.isInvolved(account)) {
				isInvolved = true;

				// 1. DELETE FROM ACCOUNT TRANSACTIONS - с нарощенным мясом
				this.dwSet.getTransactionMap().delete(account, transaction);

				// 2. а теперь сбросим все и сохраним без ссылки на блок
				transaction.resetSeqNo();

				// 3. добавим заново в кошелек уже как неподтвержденную
				this.dwSet.getTransactionMap().put(account, transaction);

			}
		}

		if (!isInvolved)
			return;

		// ADD SENDER to FAVORITES
		updateFavorites(transaction, true);

	}

	private void orphanATTransaction(Tuple2<Tuple2<Integer, Integer>, ATTransaction> atTx) {
		// CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return;
		}

		// FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		synchronized (accounts) {
			for (Account account : accounts) {
				// CHECK IF INVOLVED
				if (atTx.b.getRecipient().equalsIgnoreCase(account.getAddress())) {
					this.dwSet.getAccountMap().changeBalance(account.getAddress(), true, atTx.b.getKey(),
							BigDecimal.valueOf(atTx.b.getAmount()), false, false);
				}
			}
		}
	}

	// TODO: our woier
	public boolean checkNeedSyncWallet(byte[] signatureORreference) {

		// CHECK IF WE NEED TO RESYNC
		byte[] lastBlockSignature = this.dwSet.getLastBlockSignature();
		if (lastBlockSignature == null
				|| !Arrays.equals(lastBlockSignature, signatureORreference)) {
			return true;
		}

		return false;

	}

	public void feeProcess(Long blockFee, Account blockGenerator, boolean asOrphan) {

		this.dwSet.getAccountMap().changeBalance(blockGenerator.getAddress(), asOrphan, FEE_KEY,
				new BigDecimal(blockFee).movePointLeft(BlockChain.FEE_SCALE), false, false);

	}

	private long processBlockLogged = 0;

	void processTransaction(int height, int seqNo, Transaction transaction) {
		// TODO нужно сделать при закрытии базы чтобы ожидала окончания проходя всего блока тут - пока ОТКАТ

		if (transaction.isWiped()) {
			return;
		}

		if (transaction.noDCSet())
			transaction.setDC(dcSet, Transaction.FOR_NETWORK, height, seqNo, true);

		if (!processTransaction(transaction))
			return;

		Controller.getInstance().playWalletEvent(transaction);

	}

	void processBlock(Block block) {
		// CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return;
		}

		long start = System.currentTimeMillis();

		// SET AS LAST BLOCK
		this.dwSet.setLastBlockSignature(block.blockHead.signature);

		Account blockGenerator = block.blockHead.creator;

		int height = block.blockHead.heightBlock;

		// очередь последних блоков
		walletUpdater.lastBlocks.put(height, block);
		if (walletUpdater.lastBlocks.size() > 100) {
			walletUpdater.lastBlocks.remove(walletUpdater.lastBlocks.firstKey());
		}

		// CHECK IF WE ARE GENERATOR
		if (this.accountExists(blockGenerator)) {
			// ADD BLOCK
			this.dwSet.getBlocksHeadMap().add(block.blockHead);

			// KEEP TRACK OF UNCONFIRMED BALANCE
			// PROCESS FEE
			feeProcess(block.blockHead.totalFee, blockGenerator, false);

			Controller.getInstance().playWalletEvent(block);

		}

		// CHECK TRANSACTIONS
		int seqNo = 0;
		for (Transaction transaction : block.getTransactions()) {

			++seqNo;

			// TODO нужно сделать при закрытии базы чтобы ожидала окончания проходя всего блока тут - пока ОТКАТ

			processTransaction(height, seqNo, transaction);

		}

        if (block.blockHead.transactionsCount > 0
				&& start - processBlockLogged > 30000) {
			long tickets = System.currentTimeMillis() - start;
			if (tickets > 3) {
				processBlockLogged = start;
				LOGGER.debug("WALLET [" + block.blockHead.heightBlock + "] processing time: " + tickets
						+ " ms, TXs = " + block.blockHead.transactionsCount + ", TPS:"
						+ 1000 * block.blockHead.transactionsCount / tickets);
			}
		}

	}

	void orphanBlock(Block block) {
		// CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return;
		}

		// ORPHAN ALL TRANSACTIONS IN DB BACK TO FRONT
		if (block == null)
			return;

		int height = block.heightBlock;

		walletUpdater.lastBlocks.remove(height);

		List<Transaction> transactions = block.getTransactions();
		int seqNo;
		for (int i = block.blockHead.transactionsCount - 1; i >= 0; i--) {

			seqNo = i + 1;

			Transaction transaction = transactions.get(i);
			if (transaction.isWiped()) {
				continue;
			}

			if (transaction.noDCSet())
				transaction.setDC(dcSet, Transaction.FOR_NETWORK, block.blockHead.heightBlock, seqNo, true);

			// CHECK IF PAYMENT
			if (transaction instanceof IssueItemRecord) {
				this.orphanItemIssue((IssueItemRecord) transaction);
			}

			// CHECK IF SERTIFY PErSON
			else if (transaction instanceof RCertifyPubKeys) {
				this.orphanSertifyPerson((RCertifyPubKeys) transaction, height);
			}

			this.orphanTransaction(transaction);

		}

		Account blockGenerator = block.blockHead.creator;

		// CHECK IF WE ARE GENERATOR
		if (this.accountExists(blockGenerator)) {
			// DELETE BLOCK
			this.dwSet.getBlocksHeadMap().delete(block.blockHead);

			// SET AS LAST BLOCK

			// KEEP TRACK OF UNCONFIRMED BALANCE
			feeProcess(block.blockHead.totalFee, blockGenerator, true);

		}

		// SET AS LAST BLOCK
		this.dwSet.setLastBlockSignature(block.blockHead.reference); // .reference


    }

	private void processItemIssue(IssueItemRecord issueItem) {
		// CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return;
		}

		// CHECK IF WE ARE OWNER
		ItemCls item = issueItem.getItem();
		if (item == null)
			return;

		if (issueItem.getKey() <= 0)
			return;

		if (issueItem instanceof IssueAssetSeriesTransaction) {
			IssueAssetSeriesTransaction issueSeries = (IssueAssetSeriesTransaction) issueItem;
			int total = issueSeries.getTotal();
			long copyKey = issueSeries.getKey() - total + 1;
			while (total-- > 0) {
				item = dcSet.getItemAssetMap().get(copyKey++);
				// ADD ASSET
				this.dwSet.putItem(item);
				// ADD to FAVORITES
				///this.dwSet.addItemFavorite(item);
			}
		} else {
			// ADD to MY ITEMs
			this.dwSet.putItem(item);
		}
	}

	private void orphanItemIssue(IssueItemRecord issueItem) {
		// CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return;
		}

		// CHECK IF WE ARE OWNER
		ItemCls item = issueItem.getItem();

		if (issueItem instanceof IssueAssetSeriesTransaction) {
			IssueAssetSeriesTransaction issueSeries = (IssueAssetSeriesTransaction) issueItem;
			int total = issueSeries.getTotal();
			long copyKey = issueSeries.getKey() - total + 1;
			while (total-- > 0) {
				item = dcSet.getItemAssetMap().get(copyKey++);
				// ADD ASSET
				this.dwSet.deleteItem(item);
				// ADD to FAVORITES
				///this.dwSet.deleteItemFavorite(item);
			}
		} else {

			// DELETE ASSET
			this.dwSet.deleteItem(item);

		}

	}

	private void processCertifyPerson(RCertifyPubKeys certifyPubKeys) {
		// CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return;
		}

		// CHECK IF WE ARE OWNER
		Account creator = certifyPubKeys.getCreator();
		if (creator == null)
			return;

		addOwnerInFavorites(certifyPubKeys);

		boolean personalized = false;
		TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> personalisedData = dcSet.getPersonAddressMap().getItems(certifyPubKeys.getKey());
		if (personalisedData == null || personalisedData.isEmpty()) {
			personalized = true;
		}

		if (!personalized) {
			// IT IS NOT VOUCHED PERSON

			// FIND person
			ItemCls person = dcSet.getItemPersonMap().get(certifyPubKeys.getKey());
			if (person != null) {
				// FIND issue record
				Transaction transPersonIssue = dcSet.getTransactionFinalMap().get(person.getReference());

				// ISSUE NEW COMPU in chain
				BigDecimal issued_FEE_BD = BlockChain.BONUS_FOR_PERSON(transPersonIssue.getBlockHeight());

				// GIFTs
				if (this.accountExists(transPersonIssue.getCreator())) {
					this.dwSet.getAccountMap().changeBalance(transPersonIssue.getCreator().getAddress(),
							false, FEE_KEY, issued_FEE_BD, false, false);
				}

				// GIFTs
				if (this.accountExists(creator)) {
					this.dwSet.getAccountMap().changeBalance(creator.getAddress(), false, FEE_KEY, issued_FEE_BD, false, false);
				}

				PublicKeyAccount pkAccount = certifyPubKeys.getCertifiedPublicKeys().get(0);
				if (this.accountExists(pkAccount)) {
					this.dwSet.getAccountMap().changeBalance(pkAccount.getAddress(), false, FEE_KEY, issued_FEE_BD, false, false);
				}
			}
		}
	}

	private void addOwnerInFavorites(RCertifyPubKeys certifyPubKeys) {
		List<PublicKeyAccount> certifiedPublicKeys = certifyPubKeys.getCertifiedPublicKeys();

		for (PublicKeyAccount key : certifiedPublicKeys) {
			if (this.accountExists(key)) {
				long personKey = certifyPubKeys.getKey();
				this.dwSet.getPersonFavoritesSet().add(personKey);
				break;
			}
		}
	}

	private void orphanSertifyPerson(RCertifyPubKeys certifyPubKeys, int height) {
		// CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return;
		}

		Account creator = certifyPubKeys.getCreator();
		if (creator == null)
			return;

		// GIFTs

		boolean personalized = false;
		TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> personalisedData = dcSet.getPersonAddressMap().getItems(certifyPubKeys.getKey());
		if (personalisedData == null || personalisedData.isEmpty()) {
			personalized = true;
		}

		if (!personalized) {
			// IT IS NOT VOUCHED PERSON

			// FIND person
			ItemCls person = dcSet.getItemPersonMap().get(certifyPubKeys.getKey());
			if (person == null)
				return;

			// FIND issue record
			Transaction transPersonIssue = dcSet.getTransactionFinalMap().get(person.getReference());

			// ISSUE NEW COMPU in chain
			BigDecimal issued_FEE_BD = BlockChain.BONUS_FOR_PERSON(height);

			// GIFTs
			if (this.accountExists(transPersonIssue.getCreator())) {
				this.dwSet.getAccountMap().changeBalance(transPersonIssue.getCreator().getAddress(),
						true, FEE_KEY, issued_FEE_BD, false, false);
			}

			// GIFTs
			if (this.accountExists(creator)) {
				this.dwSet.getAccountMap().changeBalance(creator.getAddress(), true, FEE_KEY, issued_FEE_BD, false, false);
			}

			PublicKeyAccount pkAccount = certifyPubKeys.getCertifiedPublicKeys().get(0);
			if (this.accountExists(creator)) {
				this.dwSet.getAccountMap().changeBalance(pkAccount.getAddress(), true, FEE_KEY, issued_FEE_BD, false, false);
			}
		}
	}

	private void processOrderCreation(CreateOrderTransaction orderCreation) {
		// CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return;
		}

		if (orderCreation.getOrderId() == null)
			return;

		this.dwSet.getOrderMap().add(Order.getOrder(dcSet, orderCreation.getOrderId()));

	}

	private void processOrderChanging(ChangeOrderTransaction orderChanging) {
		// CHECK IF WALLET IS OPEN
		if (!this.walletKeysExists()) {
			return;
		}

		this.dwSet.getOrderMap().add(Order.getOrder(dcSet, orderChanging.getOrderId()));
		this.dwSet.getOrderMap().add(Order.getOrder(dcSet, orderChanging.getDBRef()));

	}

	/////////////////////////////////////////
	long notifySysTrayRecord;

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {
		if (Controller.getInstance().noUseWallet || Controller.getInstance().noDataWallet
				|| synchronizeBodyUsed)
			return;

		try {
			if (this.dwSet == null)
				return;

			ObserverMessage message = (ObserverMessage) arg;
			int type = message.getType();

			if (type == ObserverMessage.WALLET_ADD_TELEGRAM_TYPE) {
				Controller.getInstance().playWalletEvent(message.getValue());

			} else if (type == ObserverMessage.WALLET_ADD_ORDER_TYPE) {
				Controller.getInstance().playWalletEvent(message.getValue());

			} else if (false && type == ObserverMessage.ADD_UNC_TRANSACTION_TYPE) {

				// прилетающие неподтвержденные тоже проверяем и если это относится к нам
				// то закатываем себе в кошелек.
				// потом они при переподтверждении обновятся
				// но если нет то останутся висеть и пользователь сам их должен удалить
				// это как раз сигнал что такая не подтвердилась трнзакция

				Pair<Long, Transaction> item = (Pair<Long, Transaction>) message.getValue();
				Transaction transaction = item.getB();

				if (false) {
					/// блокирует внесение блоков через вызов события!
					List<Account> accounts = this.getAccounts();
					synchronized (accounts) {
						for (Account account : accounts) {
							// CHECK IF INVOLVED
							if (transaction.isInvolved(account)) {
								// ADD TO ACCOUNT TRANSACTIONS
								if (!this.dwSet.getTransactionMap().set(account, transaction)) {
									// UPDATE UNCONFIRMED BALANCE for ASSET
								}
							}
						}
					}
				}

			} else if (false && type == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE) {
				if (Controller.getInstance().useGui
						&& System.currentTimeMillis() - notifySysTrayRecord > 1000) {
					notifySysTrayRecord = System.currentTimeMillis();
					Pair<Tuple2<String, String>, Transaction> item = (Pair<Tuple2<String, String>, Transaction>) message.getValue();
					Transaction transaction = item.getB();
					Library.notifySysTrayRecord(transaction);
				}

			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    }

	// CLOSE

	public void close() {

		try {
			walletUpdater.halt();
		} catch (Exception e) {
		}

		if (this.lockTimer != null)
			this.lockTimer.cancel();

		if (this.dwSet != null) {
			this.dwSet.close();
		}

		if (this.secureDatabase != null) {
			this.secureDatabase.close();
		}
	}

	public void commit() {
		if (this.dwSet != null) {
			this.dwSet.hardFlush();
		}

		if (this.secureDatabase != null) {
			this.secureDatabase.commit();
		}

	}

	public byte[] getLastBlockSignature() {
		return this.dwSet.getLastBlockSignature();
	}

	public long getLicenseKey() {
		if (this.dwSet == null || this.dwSet.getLicenseKey() == null) {
			return 2L;
		}

		return this.dwSet.getLicenseKey();
	}

	public void setLicenseKey(long key) {
		this.dwSet.setLicenseKey(key);
	}

	/**
	 * @param withObserver
	 * @param dynamicGUI
	 * @return 1 - OK, > 1- error
	 */
}
