package controller;
// 04/01 +- 
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

// import org.apache.log4j.Logger;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.ws.rs.HEAD;

import org.apache.commons.io.FileUtils;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import com.google.common.primitives.Longs;

import api.ApiClient;
import api.ApiService;
import at.AT;
import core.BlockChain;
import core.BlockGenerator;
import core.Synchronizer;
import core.TransactionCreator;
import core.BlockGenerator.ForgingStatus;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import core.item.assets.Trade;
import core.item.imprints.ImprintCls;
import core.item.notes.NoteCls;
import core.item.persons.PersonCls;
import core.item.persons.PersonHuman;
import core.item.statuses.StatusCls;
import core.item.unions.UnionCls;
import core.naming.Name;
import core.naming.NameSale;
import core.payment.Payment;
import core.transaction.Transaction;
import core.voting.Poll;
import core.voting.PollOption;
import core.wallet.Wallet;
import database.DBSet;
import database.Item_Map;
import database.LocalDataMap;
import database.SortableList;
import gui.Gui;
import lang.Lang;
import network.Network;
import network.Peer;
import network.message.BlockMessage;
import network.message.BlockWinMessage;
import network.message.GetBlockMessage;
import network.message.GetSignaturesMessage;
import network.message.HWeightMessage;
//import network.message.HeightMessage;
import network.message.Message;
import network.message.MessageFactory;
import network.message.TransactionMessage;
import network.message.VersionMessage;
import ntp.NTP;
import settings.Settings;
import utils.DateTimeFormat;
import utils.ObserverMessage;
import utils.Pair;
import utils.SimpleFileVisitorForRecursiveFolderDeletion;
import utils.SysTray;
import utils.UpdateUtil;
import webserver.WebService;

public class Controller extends Observable {

	private static final Logger LOGGER = Logger.getLogger(Controller.class);
	private static final String version = "3.01.01";
	private static final String buildTime = "2017-02-06 09:33:33 UTC";
	private long buildTimestamp;
	
	// used in controller.Controller.startFromScratchOnDemand() - 0 uses in code!
	// for reset DB if DB PROTOCOL is CHANGED
	public static final String releaseVersion = "2.06.01";

//	TODO ENUM would be better here
	public static final int STATUS_NO_CONNECTIONS = 0;
	public static final int STATUS_SYNCHRONIZING = 1;
	public static final int STATUS_OK = 2;

	private boolean processingWalletSynchronize = false; 
	private int status;
	private Network network;
	private ApiService rpcService;
	private WebService webService;
	private BlockChain blockChain;
	private BlockGenerator blockGenerator;
	private Wallet wallet;
	private Synchronizer synchronizer;
	private TransactionCreator transactionCreator;
	private boolean needSync = false;
	private Timer timer = new Timer();
	private Timer timerPeerHeightUpdate = new Timer();
	private Random random = new SecureRandom();
	private byte[] foundMyselfID = new byte[128];
	private byte[] messageMagic;
	private long toOfflineTime; 
	
	private Map<Peer, Tuple2<Integer, Long>> peerHWeight;

	private Map<Peer, Pair<String, Long>> peersVersions;

	private static Controller instance;
	private DBSet dbSet; // = DBSet.getInstance();

	public boolean isProcessingWalletSynchronize() {
		return processingWalletSynchronize;
	}
	
	public void setProcessingWalletSynchronize(boolean isPocessing) {
		this.processingWalletSynchronize = isPocessing;
	}
	
	public static String getVersion() {
		return version;
	}
	public void setDBSet(DBSet db) {
		this.dbSet = db;
	}


	public int getNetworkPort() {
		if(Settings.getInstance().isTestnet()) {
			return BlockChain.TESTNET_PORT;
		} else {
			return BlockChain.MAINNET_PORT;
		}
	}
	public boolean isTestNet() {
		return Settings.getInstance().isTestnet();
	}
	
	public String getBuildDateTimeString(){
		return DateTimeFormat.timestamptoString(this.getBuildTimestamp(), "yyyy-MM-dd HH:mm:ss z", "UTC");
	}
	
	public String getBuildDateString(){
		return DateTimeFormat.timestamptoString(this.getBuildTimestamp(), "yyyy-MM-dd", "UTC");
	}
	
	public long getBuildTimestamp() {
	    if(this.buildTimestamp == 0) {
		    Date date = new Date();
		    URL resource = getClass().getResource(getClass().getSimpleName() + ".class");
		    if (resource != null) {
		        if (true || !resource.getProtocol().equals("file")) {
		        	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		        	try {
						date = (Date)formatter.parse(this.buildTime);
					} catch (ParseException e) {
						LOGGER.error(e.getMessage(),e);
					}
		        }
		    }
		    this.buildTimestamp = date.getTime();
	    }
	    return this.buildTimestamp;
	}
	
	public byte[] getMessageMagic() {
		if(this.messageMagic == null) {
			long longTestNetStamp = Settings.getInstance().getGenesisStamp();
			if(Settings.getInstance().isTestnet()){
				byte[] seedTestNetStamp = Crypto.getInstance().digest(Longs.toByteArray(longTestNetStamp));
				this.messageMagic =  Arrays.copyOfRange(seedTestNetStamp, 0, Message.MAGIC_LENGTH);	
			} else {
				this.messageMagic = Message.MAINNET_MAGIC;
			}
		}
		return this.messageMagic;
	}
	
	public void statusInfo()
	{
		LOGGER.info(
			"STATUS OK\n" 
			+ "| Last Block Signature: " + Base58.encode(this.blockChain.getLastBlock(dbSet).getSignature()) + "\n"
			+ "| Last Block Height: " + this.blockChain.getLastBlock(dbSet).getHeight(this.dbSet) + "\n"
			+ "| Last Block Time: " + DateTimeFormat.timestamptoString(this.blockChain.getLastBlock(dbSet).getTimestamp(this.dbSet)) + "\n"
			+ "| Last Block Found " + DateTimeFormat.timeAgo(this.blockChain.getLastBlock(dbSet).getTimestamp(this.dbSet)) + " ago."
			);
	}
	
	public byte[] getFoundMyselfID() {
		return this.foundMyselfID;
	}
	
	public int getWalletSyncHeight()
	{
		return this.wallet.getSyncHeight();
	}
	
	public Tuple2<Integer, Long> getMyHWeight(boolean withWinBuffer) {
		return this.blockChain.getHWeight(dbSet, withWinBuffer);
	}

	public void sendMyHWeightToPeer (Peer peer) {
	
		// SEND HEIGTH MESSAGE
		peer.sendMessage(MessageFactory.getInstance().createHWeightMessage(
				getMyHWeight(false)));
	}
	
	public TransactionCreator getTransactionCreator() {
		return  transactionCreator;
	}

	public Map<Peer, Tuple2<Integer, Long>> getPeerHWeights() {
		return peerHWeight;
	}

	public Tuple2<Integer, Long> getHWeightOfPeer(Peer peer) {
		if(peerHWeight!=null && peerHWeight.containsKey(peer)){
			return peerHWeight.get(peer);
		}
		else
		{
			return null;
		}
	}
	public void setWeightOfPeer(Peer peer, Tuple2<Integer, Long> hWeight) {
		if(peerHWeight!=null){
			peerHWeight.put(peer, hWeight);
		}
	}
	
	public Map<Peer, Pair<String, Long>> getPeersVersions() {
		return peersVersions;
	}
	
	public Pair<String, Long> getVersionOfPeer(Peer peer) {
		if(peerHWeight!=null && peersVersions.containsKey(peer)){
			return peersVersions.get(peer);
		}
		else
		{
			return new Pair<String, Long>("", 0l); 
			//\u22640.24.0
		}
	}

	public static Controller getInstance() {
		if (instance == null) {
			instance = new Controller();
		}

		return instance;
	}

	public int getStatus() {
		return this.status;
	}

	public void setNeedSync(boolean needSync) {
		this.needSync = needSync;
	}
	
	public boolean isNeedSync() {
		return this.needSync;
	}
	
	public void start() throws Exception {
		
		this.toOfflineTime = NTP.getTime();
		
		this.foundMyselfID = new byte[128];
		this.random.nextBytes(this.foundMyselfID);
		
		// CHECK NETWORK PORT AVAILABLE
		if (!Network.isPortAvailable(Controller.getInstance().getNetworkPort())) {
			throw new Exception(Lang.getInstance().translate("Network port %port% already in use!").
					replace("%port%", String.valueOf(Controller.getInstance().getNetworkPort())));
		}

		// CHECK RPC PORT AVAILABLE
		if (Settings.getInstance().isRpcEnabled()) {
			if (!Network.isPortAvailable(Settings.getInstance().getRpcPort())) {
				throw new Exception(Lang.getInstance().translate("Rpc port %port% already in use!").
						replace("%port%", String.valueOf(Settings.getInstance().getRpcPort())));
			}
		}

		// CHECK WEB PORT AVAILABLE
		if (Settings.getInstance().isWebEnabled()) {
			if (!Network.isPortAvailable(Settings.getInstance().getWebPort())) {	
				LOGGER.error(Lang.getInstance().translate("Web port %port% already in use!").
						replace("%port%", String.valueOf(Settings.getInstance().getWebPort())));
			}
		}

		this.peerHWeight = new LinkedHashMap<Peer, Tuple2<Integer, Long>>(); // LINKED TO
																// PRESERVE
																// ORDER WHEN
																// SYNCHRONIZING
																// (PRIORITIZE
																// SYNCHRONIZING
																// FROM LONGEST
																// CONNECTION
																// ALIVE)
		
		this.peersVersions = new LinkedHashMap<Peer, Pair<String, Long>>();
		
		this.transactionCreator = new TransactionCreator();

		// OPENING DATABASES
		try {
			this.dbSet = DBSet.getInstance();
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(),e);
			LOGGER.error(Lang.getInstance().translate("Error during startup detected trying to restore backup database..."));
			reCreateDB();
		}

//		startFromScratchOnDemand();

		if (this.dbSet.getBlockMap().isProcessing()) {
			try {
				this.dbSet.close();
			} catch (Throwable e) {
				LOGGER.error(e.getMessage(),e);
			}
			reCreateDB();
		}
		
		//CHECK IF DB NEEDS UPDATE

		if(this.dbSet.getBlockMap().getLastBlockSignature() != null)
		{
			//CHECK IF NAME STORAGE NEEDS UPDATE
			if (this.dbSet.getLocalDataMap().get("nsupdate") == null )
			{
				//FIRST NAME STORAGE UPDATE
				UpdateUtil.repopulateNameStorage( 70000 );
				this.dbSet.getLocalDataMap().set("nsupdate", "1");
			}
			//CREATE TRANSACTIONS FINAL MAP
			if (this.dbSet.getLocalDataMap().get("txfinalmap") == null
					|| !this.dbSet.getLocalDataMap().get("txfinalmap").equals("2"))
			{
				//FIRST NAME STORAGE UPDATE
				UpdateUtil.repopulateTransactionFinalMap(  );
				this.dbSet.getLocalDataMap().set("txfinalmap", "2");
			}
			
			if (this.dbSet.getLocalDataMap().get("blogpostmap") == null ||  !this.dbSet.getLocalDataMap().get("blogpostmap").equals("2"))
			{
				//recreate comment postmap
				UpdateUtil.repopulateCommentPostMap();
				this.dbSet.getLocalDataMap().set("blogpostmap", "2");
			}
		} else {
			this.dbSet.getLocalDataMap().set("nsupdate", "1");
			this.dbSet.getLocalDataMap().set("txfinalmap", "2");
			this.dbSet.getLocalDataMap().set("blogpostmap", "2");
		}
		
		// CREATE SYNCHRONIZOR
		this.synchronizer = new Synchronizer();

		// CREATE BLOCKCHAIN
		this.blockChain = new BlockChain(null);
		
		// START API SERVICE
		if (Settings.getInstance().isRpcEnabled()) {
			this.rpcService = new ApiService();
			this.rpcService.start();
		}

		// START WEB SERVICE
		if (Settings.getInstance().isWebEnabled()) {
			this.webService = new WebService();
			this.webService.start();
		}

		// CREATE WALLET
		this.wallet = new Wallet();

	    if(this.wallet.isWalletDatabaseExisting()){
	    	this.wallet.initiateItemsFavorites();
	    }
	    
		if(Settings.getInstance().isTestnet() && this.wallet.isWalletDatabaseExisting() && this.wallet.getAccounts().size() > 0) {
			this.wallet.synchronize();	
		}
		
		// CREATE BLOCKGENERATOR
		this.blockGenerator = new BlockGenerator(true);
		// START BLOCKGENERATOR
		this.blockGenerator.start();

		// CREATE NETWORK
		this.network = new Network();

		// CLOSE ON UNEXPECTED SHUTDOWN
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				stopAll();
			}
		});
		
		
		//TIMER TO SEND HEIGHT TO NETWORK EVERY 5 MIN 
		
		this.timerPeerHeightUpdate.cancel();
		this.timerPeerHeightUpdate = new Timer();
		
		TimerTask action = new TimerTask() {
	        public void run() {
	        	if(Controller.getInstance().getStatus() == STATUS_OK)
	        	{
	        		if(Controller.getInstance().getActivePeers().size() > 0)
	        		{
	        			Peer peer = Controller.getInstance().getActivePeers().get(
	        				random.nextInt( Controller.getInstance().getActivePeers().size() )
	        				);
	        			if(peer != null){
	        				Controller.getInstance().sendMyHWeightToPeer(peer);
	        			}
	        		}
	        	}
	        }
		};
		
		this.timerPeerHeightUpdate.schedule(action, 
				Block.GENERATING_MIN_BLOCK_TIME>>4, Block.GENERATING_MIN_BLOCK_TIME>>2);

		if( Settings.getInstance().isTestnet()) 
			this.status = STATUS_OK;
		
		// REGISTER DATABASE OBSERVER
		this.addObserver(this.dbSet.getTransactionMap());
		this.addObserver(this.dbSet);
	}

	// need for TESTS
	public void initBlockChain(DBSet dbSet) {
		this.blockChain = new BlockChain(dbSet);
	}

	/*
	public void replaseAssetsFavorites() {
		if(this.wallet != null) {
			this.wallet.replaseAssetFavorite();
		}
	}
	public void replaseNotesFavorites() {
		if(this.wallet != null) {
			this.wallet.replaseNoteFavorite();
		}
	}
	public void replasePersonsFavorites() {
		if(this.wallet != null) {
			this.wallet.replasePersonFavorite();
		}
	}
	*/
	
	public void replaseFavoriteItems(int type) {
		if(this.wallet != null) {
			this.wallet.replaseFavoriteItems(type);
		}
	}
	
	public void reCreateDB() throws IOException, Exception {
		reCreateDB(true);
	}
	
	public void reCreateDB(boolean useDataBak) throws IOException, Exception {
		
		File dataDir = new File(Settings.getInstance().getDataDir());
		if (dataDir.exists()) {
			// delete data folder
			java.nio.file.Files.walkFileTree(dataDir.toPath(),
					new SimpleFileVisitorForRecursiveFolderDeletion());
			File dataBak = getDataBakDir(dataDir);
			if (useDataBak && dataBak.exists()
					&& Settings.getInstance().isCheckpointingEnabled()) {
				FileUtils.copyDirectory(dataBak, dataDir);
				LOGGER.error(Lang.getInstance().translate("restoring backup database"));
				try {
					DBSet.reCreateDatabase();
				} catch (IOError e) {
					LOGGER.error(e.getMessage(),e);
					//backupdb is buggy too starting from scratch
					if(dataDir.exists())
					{
						java.nio.file.Files.walkFileTree(dataDir.toPath(),
								new SimpleFileVisitorForRecursiveFolderDeletion());
					}
					if(dataBak.exists())
					{
						java.nio.file.Files.walkFileTree(dataBak.toPath(),
								new SimpleFileVisitorForRecursiveFolderDeletion());
					} 
					DBSet.reCreateDatabase();
				}
				
			} else {
				DBSet.reCreateDatabase();
			}

		}

		if (this.dbSet.getBlockMap().isProcessing()) {
			throw new Exception(
					Lang.getInstance().translate("The application was not closed correctly! Delete the folder ")
							+ dataDir.getAbsolutePath()
							+ Lang.getInstance().translate(" and start the application again."));
		}
	}

	public void startFromScratchOnDemand() throws IOException {
		String dataVersion = this.dbSet.getLocalDataMap()
				.get(LocalDataMap.LOCAL_DATA_VERSION_KEY);

		if (dataVersion == null || !dataVersion.equals(releaseVersion)) {
			File dataDir = new File(Settings.getInstance().getDataDir());
			File dataBak = getDataBakDir(dataDir);
			this.dbSet.close();

			if (dataDir.exists()) {
				// delete data folder
				java.nio.file.Files.walkFileTree(dataDir.toPath(),
						new SimpleFileVisitorForRecursiveFolderDeletion());

			}

			if (dataBak.exists()) {
				// delete data folder
				java.nio.file.Files.walkFileTree(dataBak.toPath(),
						new SimpleFileVisitorForRecursiveFolderDeletion());
			}
			DBSet.reCreateDatabase();

			this.dbSet
					.getLocalDataMap()
					.set(LocalDataMap.LOCAL_DATA_VERSION_KEY,
							Controller.releaseVersion);

		}
	}

	private File getDataBakDir(File dataDir) {
		return new File(dataDir.getParent(), "dataBak");
	}

	public void rpcServiceRestart() {
		this.rpcService.stop();

		// START API SERVICE
		if (Settings.getInstance().isRpcEnabled()) {
			this.rpcService = new ApiService();
			this.rpcService.start();
		}
	}

	public void webServiceRestart() {
		this.webService.stop();

		// START API SERVICE
		if (Settings.getInstance().isWebEnabled()) {
			this.webService = new WebService();
			this.webService.start();
		}
	}

	@Override
	public void addObserver(Observer o) {
		// ADD OBSERVER TO SYNCHRONIZER
		// this.synchronizer.addObserver(o);
		this.dbSet.getBlockMap().addObserver(o);

		// ADD OBSERVER TO BLOCKGENERATOR
		// this.blockGenerator.addObserver(o);
		this.dbSet.getTransactionMap().addObserver(o);

		// ADD OBSERVER TO NAMESALES
		this.dbSet.getNameExchangeMap().addObserver(o);

		// ADD OBSERVER TO POLLS
		this.dbSet.getPollMap().addObserver(o);

		// ADD OBSERVER TO ASSETS
		this.dbSet.getItemAssetMap().addObserver(o);

		// ADD OBSERVER TO IMPRINTS
		this.dbSet.getItemImprintMap().addObserver(o);

		// ADD OBSERVER TO NOTES
		this.dbSet.getItemNoteMap().addObserver(o);

		// ADD OBSERVER TO PERSONS
		this.dbSet.getItemPersonMap().addObserver(o);

		// ADD OBSERVER TO STATUSES
		this.dbSet.getItemStatusMap().addObserver(o);

		// ADD OBSERVER TO UNIONS
		this.dbSet.getItemUnionMap().addObserver(o);
		
		// ADD OBSERVER TO ORDERS
		this.dbSet.getOrderMap().addObserver(o);

		// ADD OBSERVER TO TRADES
		this.dbSet.getTradeMap().addObserver(o);

		// ADD OBSERVER TO BALANCES
		this.dbSet.getAssetBalanceMap().addObserver(o);

		// ADD OBSERVER TO ATMAP
		this.dbSet.getATMap().addObserver(o);

		// ADD OBSERVER TO ATTRANSACTION MAP
		this.dbSet.getATTransactionMap().addObserver(o);

		// ADD OBSERVER TO CONTROLLER
		super.addObserver(o);
		o.update(this, new ObserverMessage(ObserverMessage.NETWORK_STATUS,
				this.status));
	}

	@Override
	public void deleteObserver(Observer o) {
		this.dbSet.getBlockMap().deleteObserver(o);

		super.deleteObserver(o);
	}

	public void deleteWalletObserver(Observer o) {
		this.wallet.deleteObserver(o);
	}

	private boolean isStopping = false;

	public boolean isOnStopping() {
		return this.isStopping;
	}

	public void stopAll() {
		// PREVENT MULTIPLE CALLS
		if (!this.isStopping) {
			this.isStopping = true;

			// STOP MESSAGE PROCESSOR
			LOGGER.info(Lang.getInstance().translate("Stopping message processor"));
			this.network.stop();

			// STOP BLOCK PROCESSOR
			LOGGER.info(Lang.getInstance().translate("Stopping block processor"));
			this.synchronizer.stop();

			// CLOSE DATABABASE
			LOGGER.info(Lang.getInstance().translate("Closing database"));
			this.dbSet.close();

			// CLOSE WALLET
			LOGGER.info(Lang.getInstance().translate("Closing wallet"));
			this.wallet.close();

			createDataCheckpoint();

			LOGGER.info(Lang.getInstance().translate("Closed."));
			// FORCE CLOSE
			System.exit(0);
		}
	}

	private void createDataCheckpoint() {
		if (!this.dbSet.getBlockMap().isProcessing()
				&& Settings.getInstance().isCheckpointingEnabled()) {
			this.dbSet.close();

			File dataDir = new File(Settings.getInstance().getDataDir());

			File dataBak = getDataBakDir(dataDir);

			if (dataDir.exists()) {
				if (dataBak.exists()) {
					try {
						Files.walkFileTree(
								dataBak.toPath(),
								new SimpleFileVisitorForRecursiveFolderDeletion());
					} catch (IOException e) {
						LOGGER.error(e.getMessage(),e);
					}
				}
				try {
					FileUtils.copyDirectory(dataDir, dataBak);
				} catch (IOException e) {
					LOGGER.error(e.getMessage(),e);
				}

			}

		}

	}

	// NETWORK

	public List<Peer> getActivePeers() {
		// GET ACTIVE PEERS
		return this.network.getActivePeers(false);
	}

	public void walletSyncStatusUpdate(int height) {
		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.WALLET_SYNC_STATUS, height));
	}
	
	public void blockchainSyncStatusUpdate(int height) {
		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.BLOCKCHAIN_SYNC_STATUS, height));
	}
		
	public long getToOfflineTime() {
		return this.toOfflineTime;
	}
	
	public void setToOfflineTime(long time) {
		this.toOfflineTime = time;
	}
		
	public void onConnect(Peer peer) {

		if(this.dbSet.isStoped())
			return;
		
		// SEND FOUNDMYSELF MESSAGE
		peer.sendMessage( MessageFactory.getInstance().createFindMyselfMessage( 
			Controller.getInstance().getFoundMyselfID() 
			));

		// SEND VERSION MESSAGE
		peer.sendMessage( MessageFactory.getInstance().createVersionMessage( 
			Controller.getInstance().getVersion(),
			this.getBuildTimestamp() ));
		
		// GET HEIGHT
		Tuple2<Integer, Long> HWeight = this.blockChain.getHWeight(dbSet, false);
		// SEND HEIGTH MESSAGE
		peer.sendMessage(MessageFactory.getInstance().createHWeightMessage(
				HWeight));

		// GET CURRENT WIN BLOCK
		Block winBlock = this.blockChain.getWaitWinBuffer();
		if (winBlock != null) {
			// SEND  MESSAGE
			peer.sendMessage(MessageFactory.getInstance().createWinBlockMessage(winBlock));
		}

		if (this.status == STATUS_NO_CONNECTIONS) {
			// UPDATE STATUS
			this.status = STATUS_OK;

			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(
					ObserverMessage.NETWORK_STATUS, this.status));
			
			this.actionAfterConnect();
		}
	}

	public void actionAfterConnect() 
	{
		this.timer.cancel();
		this.timer = new Timer();

		TimerTask action = new TimerTask() {
	        public void run() {
	        	
	        	if(Controller.getInstance().getStatus() == STATUS_OK)
		        {
	    			Controller.getInstance().statusInfo();

		        	Controller.getInstance().setToOfflineTime(0L);
		        	
			       	if(Controller.getInstance().isNeedSync() && !Controller.getInstance().isProcessingWalletSynchronize())
			       	{
			       		Controller.getInstance().synchronizeWallet();
			       	}
		        }
	        }
		};
			
		this.timer.schedule(action, Settings.getInstance().getConnectionTimeout());
	}
	
	
	public void forgingStatusChanged(ForgingStatus status) {
		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.FORGING_STATUS, status));
	}

	// used from NETWORK
	public void afterDisconnect(Peer peer) {
		synchronized (this.peerHWeight) {
			
			this.peerHWeight.remove(peer);
			
			this.peersVersions.remove(peer);
			
			if (this.peerHWeight.size() == 0) {
				
				if(this.getToOfflineTime() == 0L) {
					//SET START OFFLINE TIME
					this.setToOfflineTime(NTP.getTime());
				}
				
				// UPDATE STATUS
				if (isTestNet())
					this.status = STATUS_OK;
				else
					this.status = STATUS_NO_CONNECTIONS;

				
				// NOTIFY
				this.setChanged();
				this.notifyObservers(new ObserverMessage(
						ObserverMessage.NETWORK_STATUS, this.status));
			}
		}
	}

	public List<byte[]> getNextHeaders(byte[] signature) {
		return this.blockChain.getSignatures(dbSet, signature);
	}

	// SYNCHRONIZED DO NOT PROCESSS MESSAGES SIMULTANEOUSLY
	public void onMessage(Message message) {
		Message response;
		Block newBlock;


		synchronized (this) {
			switch (message.getType()) {
			case Message.PING_TYPE:

				// CREATE PING
				response = MessageFactory.getInstance().createPingMessage();

				// SET ID
				response.setId(message.getId());

				// SEND BACK TO SENDER
				message.getSender().sendMessage(response);

				break;

			/*
			case Message.HEIGHT_TYPE:

				HeightMessage heightMessage = (HeightMessage) message;

				// ADD TO LIST
				synchronized (this.peerHWeight) {
					this.peerHWeight.put(heightMessage.getSender(),
							heightMessage.getHeight());
				}

				break;
				*/

			case Message.HWEIGHT_TYPE:

				HWeightMessage hWeightMessage = (HWeightMessage) message;

				// ADD TO LIST
				synchronized (this.peerHWeight) {
					this.peerHWeight.put(hWeightMessage.getSender(),
							hWeightMessage.getHWeight());
				}

				break;

			case Message.GET_SIGNATURES_TYPE:

				GetSignaturesMessage getHeadersMessage = (GetSignaturesMessage) message;

				// ASK SIGNATURES FROM BLOCKCHAIN
				List<byte[]> headers = getNextHeaders(getHeadersMessage.getParent());

				/*
				 * for core.Synchronizer.synchronize(DBSet, Block, List<Block>)
				LOGGER.error("controller.Controller.onMessage(Message).GET_SIGNATURES_TYPE ->"
						+ Base58.encode(getHeadersMessage.getParent()));


				LOGGER.error("this.blockChain.getSignatures.size() -> "
						+ headers.size());
				if (headers.size() > 0) {
				LOGGER.error("this.blockChain.getSignatures.get(0) -> "
						+ Base58.encode( headers.get(0) ));
				LOGGER.error("this.blockChain.getSignatures.get(headers.size()-1) -> "
						+ Base58.encode( headers.get(headers.size()-1) ));
				}
				*/
				
				// CREATE RESPONSE WITH SAME ID
				response = MessageFactory.getInstance().createHeadersMessage(
						headers);
				response.setId(message.getId());

				// SEND RESPONSE BACK WITH SAME ID
				message.getSender().sendMessage(response);

				break;

			case Message.GET_BLOCK_TYPE:

				GetBlockMessage getBlockMessage = (GetBlockMessage) message;

				/*
				LOGGER.error("controller.Controller.onMessage(Message).GET_BLOCK_TYPE ->.getSignature()"
						+ " form PEER: " + getBlockMessage.getSender().toString()
						+ " sign: " + Base58.encode(getBlockMessage.getSignature()));
				*/

				// ASK BLOCK FROM BLOCKCHAIN
				newBlock = this.blockChain
						.getBlock(dbSet, getBlockMessage.getSignature());

				// CREATE RESPONSE WITH SAME ID
				response = MessageFactory.getInstance().createBlockMessage(
						newBlock);
				response.setId(message.getId());

				// SEND RESPONSE BACK WITH SAME ID
				message.getSender().sendMessage(response);

				if (newBlock == null) {
					String mess = "Block NOT FOUND for sign:" + getBlockMessage.getSignature();
					banPeerOnError(message.getSender(), mess);
				}
				
				break;

			case Message.WIN_BLOCK_TYPE:

				if (this.status != STATUS_OK
					|| this.isProcessingWalletSynchronize()) {
					break;
				}
				
				BlockWinMessage blockWinMessage = (BlockWinMessage) message;

				// ASK BLOCK FROM BLOCKCHAIN
				newBlock = blockWinMessage.getBlock();
				LOGGER.debug("mess from " + blockWinMessage.getSender().getAddress());
				LOGGER.debug(" received new WIN Block " + newBlock.toString(dbSet));

				
				Block lastBlock = this.blockChain.getLastBlock(dbSet);
				if (newBlock.getReference().equals(lastBlock.getReference())) {
					// NEW winBlock CONCURENT for LAST BLOCK found!!
					LOGGER.debug("  ** it is concurent for LAST BLOCK ???");
					if (newBlock.calcWinValue(dbSet) > lastBlock.calcWinValue(dbSet)) {
						LOGGER.debug("   ++ concurent is OK ++");
						int isNewWinBlockValid = this.blockChain.isNewBlockValid(dbSet, newBlock);
						if (isNewWinBlockValid == 0
								|| isNewWinBlockValid == 4) {
							// STOP FORGING
							ForgingStatus tempStatus = this.blockGenerator.getForgingStatus();
							this.blockGenerator.setForgingStatus(ForgingStatus.FORGING_WAIT);
							// ORPHAN
							lastBlock.orphan(dbSet);
							this.blockChain.clearWaitWinBuffer();
							// set NEW win Block
							this.blockChain.setWaitWinBuffer(dbSet, newBlock);
							
							List<Peer> excludes = new ArrayList<Peer>();
							excludes.add(message.getSender());
							this.network.broadcast(message, excludes);
							// FORGING RESTORE
							this.blockGenerator.setForgingStatus(tempStatus);
						}
						
					}
					return;
				}
				int isNewWinBlockValid = this.blockChain.isNewBlockValid(dbSet, newBlock);
				
				// CHECK IF VALID
				if(isNewWinBlockValid == 0)	{
					if (blockChain.setWaitWinBuffer(dbSet, newBlock)) {
						// IF IT WIN
						/*
						LOGGER.info(Lang.getInstance().translate("received new valid WIN Block")
								+ " for Height: " + this.getMyHeight());
								*/
		
						// BROADCAST
						List<Peer> excludes = new ArrayList<Peer>();
						excludes.add(message.getSender());
						this.network.broadcast(message, excludes);
						return;
					}
				} else {
					if (isNewWinBlockValid == 4) {
						// update peers WH
						Peer peer = blockWinMessage.getSender();
						int peerHeight = blockWinMessage.getHeight();
						updatePeerHeight(peer, peerHeight);
						return;
					}
					LOGGER.debug("controller.Controller.onMessage BLOCK_TYPE -> WIN block not valid "
							+ " for Height: " + this.getMyHeight()
							+ ", code: " + isNewWinBlockValid
							+ ", " + newBlock.toString(dbSet));
				}

				break;

			case Message.BLOCK_TYPE:
				

				// ALL IINCOMED BLOCKS ignored now!!!
				if (true || this.status != STATUS_OK
						|| this.isProcessingWalletSynchronize()) {
					break;
				}

				BlockMessage blockMessage = (BlockMessage) message;

				// ASK BLOCK FROM BLOCKCHAIN
				newBlock = blockMessage.getBlock();
				LOGGER.debug("mess from " + blockMessage.getSender().getAddress());
				LOGGER.debug(" received new chain Block " + newBlock.toString(dbSet));

				/*
				synchronized (this.peerHWeight) {
					Tuple2<Integer, Long> peerHM = this.peerHWeight.get(message.getSender());
					long hmVal;
					if (peerHM == null || peerHM.b == null) {
						hmVal = 0;
					} else {
						hmVal = peerHM.b;
					}
						
					int peerHeight =  blockWinMessage.getHeight();
					if (peerHeight < 0) {
						peerHeight = newBlock.getHeightByParent(dbSet);
					}
					this.peerHWeight.put(message.getSender(),							
							new Tuple2<Integer, Long>(peerHeight,
									hmVal + newBlock.calcWinValueTargeted(dbSet)));
					
				}
				*/

				int isNewBlockValid = this.blockChain.isNewBlockValid(dbSet, newBlock);
				if (isNewBlockValid == 4) {
					// fork branch! disconnect!
					//// NOT !!! this.onDisconnect(message.getSender());
					return;
				} else if (isNewBlockValid != 0) {
					return;
				}
				
				// may be it block need in WIN battle with MY winBlock?
				Block waitWinBlock = this.blockChain.getWaitWinBuffer();
				if (waitWinBlock != null
						// alreday checced && waitWinBlock.getHeightByParent(dbSet) == newBlock.getHeightByParent(dbSet)
						) {
					// same candidate for win
					if (this.blockChain.setWaitWinBuffer(dbSet, newBlock)) {
						// need to BROADCAST
						this.broadcastWinBlock(newBlock, null);
					}
					return;
				}	
				
				// CHECK IF VALID
				if (this.synchronizer.process(dbSet, newBlock)) {
						
					/*
					synchronized (this.peerHWeight) {
						Tuple2<Integer, Long> peerHM = this.peerHWeight.get(message.getSender());
						long hmVal;
						if (peerHM == null || peerHM.b == null) {
							hmVal = 0;
						} else {
							hmVal = peerHM.b;
						}
							
						this.peerHWeight.put(message.getSender(),
								new Tuple2<Integer, Long>(blockMessage.getHeight(),
										hmVal + newBlock.calcWinValueTargeted(dbSet)));
						
					}
					*/

					/*
					LOGGER.info(Lang.getInstance().translate("received new valid block"));
					*/

					// BROADCAST
					List<Peer> excludes = new ArrayList<Peer>();
					excludes.add(message.getSender());
					this.network.broadcast(message, excludes);

				} else {
					banPeerOnError(message.getSender(), "Block (" + newBlock.toString(dbSet) + ") is Invalid");
				}
				
				break;

			case Message.TRANSACTION_TYPE:

				TransactionMessage transactionMessage = (TransactionMessage) message;

				// GET TRANSACTION
				Transaction transaction = transactionMessage.getTransaction();

				// CHECK IF SIGNATURE IS VALID OR GENESIS TRANSACTION
				if (transaction.getCreator() != null 
						& !transaction.isSignatureValid()) {
					// DISHONEST PEER
					banPeerOnError(message.getSender(), "invalid transaction signature");

					return;
				}

				// CHECK IF TRANSACTION HAS MINIMUM FEE AND MINIMUM FEE PER BYTE
				// AND UNCONFIRMED
				// TODO fee
				// transaction.calcFee();
				if (!this.dbSet.getTransactionRef_BlockRef_Map()
								.contains(transaction.getSignature())) {
					// ADD TO UNCONFIRMED TRANSACTIONS
					this.blockGenerator.addUnconfirmedTransaction(transaction);

					// NOTIFY OBSERVERS
					// this.setChanged();
					// this.notifyObservers(new
					// ObserverMessage(ObserverMessage.LIST_TRANSACTION_TYPE,
					// DatabaseSet.getInstance().getTransactionsDatabase().getTransactions()));

					this.setChanged();
					this.notifyObservers(new ObserverMessage(
							ObserverMessage.ADD_TRANSACTION_TYPE, transaction));

					// BROADCAST
					List<Peer> excludes = new ArrayList<Peer>();
					excludes.add(message.getSender());
					this.network.broadcast(message, excludes);
				}

				break;
				
			case Message.VERSION_TYPE:

				VersionMessage versionMessage = (VersionMessage) message;

				// ADD TO LIST
				synchronized (this.peersVersions) {
					this.peersVersions.put(versionMessage.getSender(),
							new Pair<String, Long>(versionMessage.getStrVersion(), versionMessage.getBuildDateTime()) );
				}

				break;
			}
		}
	}

	public void banPeerOnError(Peer peer, String mess) {
		this.network.tryDisconnect(peer, 30, "ban PeerOnError - " + mess);
	}

	public void addActivePeersObserver(Observer o) {
		this.network.addObserver(o);
	}

	public void removeActivePeersObserver(Observer o) {
		this.network.deleteObserver(o);
	}

	public void broadcastWinBlock(Block newBlock, List<Peer> excludes) {

		LOGGER.info("broadcast winBlock " + newBlock.toString(this.dbSet));

		// CREATE MESSAGE
		Message message = MessageFactory.getInstance().createWinBlockMessage(newBlock);
		
		// BROADCAST MESSAGE		
		this.network.broadcast(message, excludes);
		
	}
	public void broadcastHWeight(List<Peer> excludes) {

		//LOGGER.info("broadcast winBlock " + newBlock.toString(this.dbSet));

		// CREATE MESSAGE
 		// GET HEIGHT
		Tuple2<Integer, Long> HWeight = this.blockChain.getHWeight(dbSet, false);
		Message messageHW = MessageFactory.getInstance().createHWeightMessage(HWeight);
		
		// BROADCAST MESSAGE		
		this.network.broadcast(messageHW, excludes);
		
	}
	
	public void broadcastBlock(Block newBlock, List<Peer> excludes) {

		LOGGER.info("broadcast chainBlock: " + newBlock.toString(this.dbSet));

		// CREATE MESSAGE
		Message message = MessageFactory.getInstance().createBlockMessage(newBlock);
		
		// BROADCAST MESSAGE		
		this.network.broadcast(message, excludes);

		/*
		// TODO HWeight
 		// GET HEIGHT
		Tuple2<Integer, Long> HWeight = this.blockChain.getHWeight(dbSet, false);
		// SEND HEIGTH MESSAGE
		peer.sendMessage(MessageFactory.getInstance().createHWeightMessage(
				HWeight));

		Message messageHW = MessageFactory.getInstance().createHWeightMessage(
				new Tuple2<Integer, Long>(newBlock.getHeightByParent(dbSet), newBlock.calcWinValue(dbSet)));
		
		// BROADCAST MESSAGE		
		this.network.broadcast(messageHW, excludes);
		*/
	}

	private void broadcastTransaction(Transaction transaction) {

		if (Controller.getInstance().getStatus() == Controller.STATUS_OK) {
			// CREATE MESSAGE
			Message message = MessageFactory.getInstance()
					.createTransactionMessage(transaction);

			// BROADCAST MESSAGE
			List<Peer> excludes = new ArrayList<Peer>();
			this.network.broadcast(message, excludes);
		}
	}

	// SYNCHRONIZE

	public boolean isUpToDate() {
		
		if (isTestNet())
			return true;
		
		if (this.peerHWeight.size() == 0) {
			return false;
		}

		Tuple3<Integer, Long, Peer> maxHW = this.getMaxPeerHWeight();
		if (maxHW.c == null)
			return true;
		
		Tuple2<Integer, Long> thisHW = this.blockChain.getHWeight(dbSet, false);
		if (maxHW.a > thisHW.a ) {
			return false;
		} else if (maxHW.a < thisHW.a)
			return true;
		
		long maxPeerWeight = maxHW.b;
		long chainWeight = thisHW.b;
		if (maxPeerWeight > chainWeight) {
			// SAME last block?
			int pickTarget = BlockChain.BASE_TARGET >>2;
			if (true || (maxPeerWeight - chainWeight < pickTarget)) {
				byte[] lastBlockSignature = dbSet.getBlockMap().getLastBlockSignature();

				try {
					Block maxBlock = core.Synchronizer.getBlock(lastBlockSignature, maxHW.c);
					if (maxBlock != null) {
						// SAME LAST BLOCK
						//this.blockChain.getHWeight(dbSet, false);
						dbSet.getBlockSignsMap().setFullWeight(maxPeerWeight);
						return true;
					}
				} catch (Exception e) {
					// error on peer - disconnect!
					this.network.tryDisconnect(maxHW.c, 0, e.getMessage());
				}
			}
		}
		//LOGGER.info("Controller.isUpToDate getMaxPeerHWeight:" + maxPeerWeight + "<=" + chainWeight);

		return maxPeerWeight <= chainWeight;
	}
	
	public boolean isReadyForging() {
		
		/*
		if (this.peerHWeight.size() == 0) {
			return false;
		}

		if (true) {
			int maxPeerHeight = this.getMaxPeerHWeight().a;
			int chainHeight = this.blockChain.getHWeight(dbSet, false).a;
			int diff = chainHeight - maxPeerHeight;
			return diff >= 0;
		} else {
			long maxPeerWeight = this.getMaxPeerHWeight().b;
			long chainWeight = this.blockChain.getHWeight(dbSet, false).b;
			long diff = chainWeight - maxPeerWeight;
			return diff >= 0 && diff < 999;
		}
		*/
		
		return true;
	}
	
	public boolean isNSUpToDate() {
		return !Settings.getInstance().updateNameStorage();
	}

	public void update() {
		// UPDATE STATUS
		
		if (this.status == STATUS_NO_CONNECTIONS)
			return;
		
		this.status = STATUS_SYNCHRONIZING;

		// NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.NETWORK_STATUS, this.status));
		
		//DBSet dbSet = DBSet.getInstance();

		Peer peer = null;
		//Block lastBlock = getLastBlock();
		//int lastTrueBlockHeight = this.getMyHeight() - Settings.BLOCK_MAX_SIGNATURES;
		int checkPointHeight = this.getBlockChain().getCheckPoint(dbSet);
		
		try {
			// WHILE NOT UPTODATE
			do {
				// START UPDATE FROM HIGHEST HEIGHT PEER
				Tuple3<Integer, Long, Peer> peerHW = this.getMaxPeerHWeight();				
				if (peerHW != null) {
					peer = peerHW.c;
					if (peer != null) {
						LOGGER.info("Controller.update from MaxHeightPeer:" + peer.getAddress().getHostAddress()
								+ " WH: " + getHWeightOfPeer(peer));

						// SYNCHRONIZE FROM PEER
						this.synchronizer.synchronize(dbSet, checkPointHeight, peer);						
					}
				}
			} while (!this.isUpToDate());
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);

			/*
			if (peer != null && peer.isUsed()) {
				// DISHONEST PEER
				this.network.tryDisconnect(peer, 2 * BlockChain.GENERATING_MIN_BLOCK_TIME / 60, e.getMessage());
			}
			*/
		}

		if (this.peerHWeight.size() == 0
				|| peer == null) {
			// UPDATE STATUS
			this.status = STATUS_NO_CONNECTIONS;
		} else if (!this.isUpToDate()) {
			// UPDATE STATUS
			this.status = STATUS_SYNCHRONIZING;
		} else {
			this.status = STATUS_OK;
		}

		// NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.NETWORK_STATUS, this.status));
		
		this.statusInfo();

	}

	/*
	private Peer getMaxWeightPeer() {
		Peer highestPeer = null;
		
		// NOT USE GenesisBlocks
		long weight = BlockChain.BASE_TARGET + BlockChain.BASE_TARGET>>1;

		try {
			synchronized (this.peerHWeight) {
				for (Peer peer : this.peerHWeight.keySet()) {
					if (highestPeer == null && peer != null) {
						highestPeer = peer;
					} else {
						// IF HEIGHT IS BIGGER
						if (weight < this.peerHWeight.get(peer).b) {
							highestPeer = peer;
							weight = this.peerHWeight.get(peer).b;
						} else if (weight == this.peerHWeight.get(peer).b) {
							// IF HEIGHT IS SAME
							// CHECK IF PING OF PEER IS BETTER
							if (peer.getPing() < highestPeer.getPing()) {
								highestPeer = peer;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// PEER REMOVED WHILE ITERATING
		}

		return highestPeer;
	}
	*/

	public Tuple3<Integer, Long, Peer> getMaxPeerHWeight() {
		
		Tuple2<Integer, Long> myHWeight = this.getMyHWeight(false);
		int height = myHWeight.a;
		long weight = myHWeight.b;
		Peer maxPeer = null;

		try {
			synchronized (this.peerHWeight) {
				for (Peer peer : this.peerHWeight.keySet()) {
					Tuple2<Integer, Long> whPeer = this.peerHWeight.get(peer);
					if (height < whPeer.a
							&& weight < whPeer.b) {
						height = whPeer.a;
						weight = whPeer.b;
						maxPeer = peer;
					}
				}
			}
		} catch (Exception e) {
			// PEER REMOVED WHILE ITERATING
		}

		return new Tuple3<Integer, Long, Peer>(height, weight, maxPeer);
	}

	public void updatePeerHeight(Peer peer, int peerHeight) {
		Tuple2<Integer, Long> hWeightMy = this.getMyHWeight(false);
		if (peerHeight > hWeightMy.a) {
			hWeightMy = new Tuple2<Integer, Long>(peerHeight, hWeightMy.b + 10000l);
		} else {
			hWeightMy = new Tuple2<Integer, Long>(peerHeight, hWeightMy.b - 10000l);							
		}
		this.peerHWeight.put(peer, hWeightMy);
		blockchainSyncStatusUpdate(this.getMyHeight());
	}
	public void updateMyWeight(long weight) {
		dbSet.getBlockSignsMap().setFullWeight(weight);
		blockchainSyncStatusUpdate(this.getMyHeight());
	}

	// WALLET

	public boolean doesWalletExists() {
		// CHECK IF WALLET EXISTS
		return this.wallet != null && this.wallet.exists();
	}

	public boolean doesWalletDatabaseExists() {
		return wallet != null && this.wallet.isWalletDatabaseExisting();
	}

	public boolean createWallet(byte[] seed, String password, int amount) {
		// IF NEW WALLET CREADED
		return this.wallet.create(seed, password, amount, false);
	}
	
	public boolean recoverWallet(byte[] seed, String password, int amount) {
		if(this.wallet.create(seed, password, amount, false))
		{
			LOGGER.info("Wallet needs to synchronize!");
			this.actionAfterConnect();
			this.setNeedSync(true);

			return true;
		}
		else
			return false;
	}

	public List<Account> getAccounts() {

		return this.wallet.getAccounts();
	}
	
	public boolean isAddressIsMine(String address)
	{
		if(!this.doesWalletExists())
			return false;

		List<Account> accounts = this.wallet.getAccounts();
		for (Account account: accounts) {
			if (account.getAddress().equals(address))
				return true;
		}
		return false;
	}
	
	public List<PublicKeyAccount> getPublicKeyAccounts() {

		return this.wallet.getPublicKeyAccounts();
	}

	public List<PrivateKeyAccount> getPrivateKeyAccounts() {

		return this.wallet.getprivateKeyAccounts();
	}

	public String generateNewAccount() {
		return this.wallet.generateNewAccount();
	}

	public PrivateKeyAccount getPrivateKeyAccountByAddress(String address) {
		if(this.doesWalletExists()) {
			return this.wallet.getPrivateKeyAccount(address);
		} else {
			return null;
		}
	}

	public Account getAccountByAddress(String address) {
		if(this.doesWalletExists()) {
			return this.wallet.getAccount(address);
		} else {
			return null;
		}
	}

	//public BigDecimal getUnconfirmedBalance(String address, long key) {
	//	return this.wallet.getUnconfirmedBalance(address, key);
	//}
	public Tuple3<BigDecimal, BigDecimal, BigDecimal> getUnconfirmedBalance(Account account, long key) {
		return this.wallet.getUnconfirmedBalance(account, key);
	}

	public void addWalletListener(Observer o) {
		this.wallet.addObserver(o);
	}

	public String importAccountSeed(byte[] accountSeed) {
		return this.wallet.importAccountSeed(accountSeed);
	}

	public byte[] exportAccountSeed(String address) {
		return this.wallet.exportAccountSeed(address);
	}

	public byte[] exportSeed() {
		return this.wallet.exportSeed();
	}

	public boolean deleteAccount(PrivateKeyAccount account) {
		return this.wallet.deleteAccount(account);
	}

	public void synchronizeWallet() {
		this.wallet.synchronize();
	}

	public boolean isWalletUnlocked() {
		return this.wallet.isUnlocked();
	}

	public int checkAPICallAllowed(String json, HttpServletRequest request)
			throws Exception {
		int result = 0;

		if (request != null) {
			Enumeration<String> headers = request
					.getHeaders(ApiClient.APICALLKEY);
			String uuid = null;
			if (headers.hasMoreElements()) {
				uuid = headers.nextElement();
				if (ApiClient.isAllowedDebugWindowCall(uuid)) {
					return ApiClient.SELF_CALL;
				}
			}
		}

		if (!GraphicsEnvironment.isHeadless() &&  (Settings.getInstance().isGuiEnabled() || Settings.getInstance().isSysTrayEnabled()) ) {
			Gui gui = Gui.getInstance();
			SysTray.getInstance().sendMessage(Lang.getInstance().translate("INCOMING API CALL"),
					Lang.getInstance().translate("An API call needs authorization!"), MessageType.WARNING);
			Object[] options = { Lang.getInstance().translate("Yes"), Lang.getInstance().translate("No") };

			 StringBuilder sb = new StringBuilder(Lang.getInstance().translate("Permission Request: "));
	            sb.append(Lang.getInstance().translate("Do you want to authorize the following API call?\n\n")
						+ json);
	            JTextArea jta = new JTextArea(sb.toString());
	            jta.setLineWrap(true);
	            jta.setEditable(false);
	            JScrollPane jsp = new JScrollPane(jta){
	                /**
					 *
					 */
					private static final long serialVersionUID = 1L;

					@Override
	                public Dimension getPreferredSize() {
	                    return new Dimension(480, 200);
	                }
	            };

			gui.bringtoFront();
			
			result = JOptionPane
					.showOptionDialog(gui,
							jsp, Lang.getInstance().translate("INCOMING API CALL"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[1]);
		}

		return result;
	}

	public boolean lockWallet() {
		return this.wallet.lock();
	}

	public boolean unlockWallet(String password) {
		return this.wallet.unlock(password);
	}

	public void setSecondsToUnlock(int seconds) {
		this.wallet.setSecondsToUnlock(seconds);
	}

	public List<Pair<Account, Transaction>> getLastTransactions(int limit) {
		return this.wallet.getLastTransactions(limit);
	}

	public Transaction getTransaction(byte[] signature) {

		return getTransaction(signature, this.dbSet);
	}
	
	// by account addres + timestamp get signature
	public byte[] getSignatureByAddrTime(DBSet dbSet, String address, Long timestamp) {

		return dbSet.getAddressTime_SignatureMap().get(address, timestamp);
	}
	
	public Transaction getTransaction(byte[] signature, DBSet database) {
		
		// CHECK IF IN BLOCK
		Block block = database.getTransactionRef_BlockRef_Map()
				.getParent(signature);
		if (block != null) {
			return block.getTransaction(signature);
		}
		
		// CHECK IF IN TRANSACTION DATABASE
		if (database.getTransactionMap().contains(signature)) {
		return database.getTransactionMap().get(signature);
		}
		return null;
	}

	public List<Transaction> getLastTransactions(Account account, int limit) {
		return this.wallet.getLastTransactions(account, limit);
	}

	public List<Pair<Account, Block>> getLastBlocks() {
		return this.wallet.getLastBlocks();
	}

	public List<Block> getLastBlocks(Account account) {
		return this.wallet.getLastBlocks(account);
	}

	public List<Pair<Account, Name>> getNames() {
		return this.wallet.getNames();
	}

	public List<Name> getNamesAsList() {
		List<Pair<Account, Name>> names = this.wallet.getNames();
		List<Name> result = new ArrayList<>();
		for (Pair<Account, Name> pair : names) {
			result.add(pair.getB());
		}

		return result;

	}

	public List<String> getNamesAsListAsString() {
		List<Name> namesAsList = getNamesAsList();
		List<String> results = new ArrayList<String>();
		for (Name name : namesAsList) {
			results.add(name.getName());
		}
		return results;

	}

	public List<Name> getNames(Account account) {
		return this.wallet.getNames(account);
	}

	public List<Pair<Account, NameSale>> getNameSales() {
		return this.wallet.getNameSales();
	}

	public List<NameSale> getNameSales(Account account) {
		return this.wallet.getNameSales(account);
	}

	public List<NameSale> getAllNameSales() {
		return this.dbSet.getNameExchangeMap().getNameSales();
	}

	public List<Pair<Account, Poll>> getPolls() {
		return this.wallet.getPolls();
	}

	public List<Poll> getPolls(Account account) {
		return this.wallet.getPolls(account);
	}

	/*
	public void addAssetFavorite(AssetCls asset) {
		this.wallet.addAssetFavorite(asset);
	}
	*/
	public void addItemFavorite(ItemCls item) {
		this.wallet.addItemFavorite(item);
	}

	/*
	public void removeAssetFavorite(AssetCls asset) {
		this.wallet.removeAssetFavorite(asset);
	}
	public void removeNoteFavorite(NoteCls note) {
		this.wallet.removeNoteFavorite(note);
	}
	public void removePersonFavorite(PersonCls person) {
		this.wallet.removePersonFavorite(person);
	}
	*/
	
	public Item_Map getItemMap(int type) {
		switch(type)
			{
			case ItemCls.ASSET_TYPE:
				return this.dbSet.getItemAssetMap();
			case ItemCls.IMPRINT_TYPE:
				return this.dbSet.getItemImprintMap();
			case ItemCls.NOTE_TYPE:
				return this.dbSet.getItemNoteMap();
			case ItemCls.PERSON_TYPE:
				return this.dbSet.getItemPersonMap();
		}
		return null;
	}

	public void removeItemFavorite(ItemCls item) {
		this.wallet.removeItemFavorite(item);
	}

	/*
	public boolean isAssetFavorite(AssetCls asset) {
		return this.wallet.isAssetFavorite(asset);
	}
	
	public boolean isNoteFavorite(NoteCls note) {
		return this.wallet.isNoteFavorite(note);
	}
	public boolean isPersonFavorite(PersonCls person) {
		return this.wallet.isPersonFavorite(person);
	}
	*/
	public boolean isItemFavorite(ItemCls item) {
		return this.wallet.isItemFavorite(item);
	}

	public Collection<Poll> getAllPolls() {
		return this.dbSet.getPollMap().getValues();
	}

	public Collection<ItemCls> getAllItems(int type) {
		return getItemMap(type).getValues();
	}

	/*
	public Collection<ItemCls> getAllAssets() {
		return this.dbSet.getAssetMap().getValues();
	}

	public Collection<ItemCls> getAllNotes() {
		return this.dbSet.getNoteMap().getValues();
	}
	public Collection<ItemCls> getAllPersons() {
		return this.dbSet.getPersonMap().getValues();
	}
	*/

	public void onDatabaseCommit() {
		this.wallet.commit();
	}

	public ForgingStatus getForgingStatus() {
		return this.blockGenerator.getForgingStatus();
	}

	// BLOCKCHAIN

	public BlockChain getBlockChain() {
		return this.blockChain;
	}


	public int getMyHeight() {
		// need for TESTs
		return this.blockChain != null? this.blockChain.getHWeight(dbSet, false).a: -1;
	}

	public Block getLastBlock() {
		return this.blockChain.getLastBlock(dbSet);
	}
	
	public byte[] getWalletLastBlockSign() {
		return this.wallet.getLastBlockSignature();
	}
	
	public Block getBlock(byte[] header) {
		return this.blockChain.getBlock(dbSet, header);
	}

	public Pair<Block, List<Transaction>> scanTransactions(Block block,
			int blockLimit, int transactionLimit, int type, int service,
			Account account) {
		return this.blockChain.scanTransactions(dbSet, block, blockLimit,
				transactionLimit, type, service, account);

	}

	public long getNextBlockGeneratingBalance() {
		Block block = this.dbSet.getBlockMap().getLastBlock();
		return block.getGeneratingBalance(dbSet);
	}


	// FORGE

	/*
	public boolean newBlockGenerated(Block newBlock) {
		
		Tuple2<Boolean, Block> result = this.blockChain.setWaitWinBuffer(dbSet, newBlock); 
		if ( result.a ) {
			// need to BROADCAST
			this.broadcastBlock(result.b);
		}
		
		return result.a;
	}
	*/

	// FLUSH BLOCK from win Buffer - ti MAP and NERWORK
	public boolean flushNewBlockGenerated() {

		Block newBlock = this.blockChain.popWaitWinBuffer();
		if (newBlock == null)
			return false;
		
		// if last block is changed by core.Synchronizer.process(DBSet, Block)
		// clear this win block
		if (!Arrays.equals(dbSet.getBlockMap().getLastBlockSignature(), newBlock.getReference())) {
			return false;
		}
				
		// IC
		if(isProcessingWalletSynchronize())
		{
			// IC
			return false;
		}

		boolean isValid = this.synchronizer.process(this.dbSet, newBlock);
		if (isValid) {
			LOGGER.info("flush chainBlock: "
					+ newBlock.toString(this.dbSet));

			///LOGGER.info("and broadcast it");
			
		}
		
		return isValid;
	}

	
	public List<Transaction> getUnconfirmedTransactions() {
		return this.blockGenerator.getUnconfirmedTransactions();
	}

	// BALANCES

	public SortableList<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> getBalances(long key) {
		return this.dbSet.getAssetBalanceMap().getBalancesSortableList(key);
	}

	public SortableList<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> getBalances(
			Account account) {
		return this.dbSet.getAssetBalanceMap()
				.getBalancesSortableList(account);
	}

	// NAMES

	public Name getName(String nameName) {
		return this.dbSet.getNameMap().get(nameName);
	}

	public NameSale getNameSale(String nameName) {
		return this.dbSet.getNameExchangeMap().getNameSale(nameName);
	}

	// POLLS

	public Poll getPoll(String name) {
		return this.dbSet.getPollMap().get(name);
	}

	// ASSETS

	public AssetCls getAsset(long key) {
		return (AssetCls) this.dbSet.getItemAssetMap().get(key);
	}
	public PersonCls getPerson(long key) {
		return (PersonCls) this.dbSet.getItemPersonMap().get(key);
	}
	public NoteCls getNote(long key) {
		return (NoteCls) this.dbSet.getItemNoteMap().get(key);
	}

	public SortableList<BigInteger, Order> getOrders(AssetCls have, AssetCls want) {
		return this.getOrders(have, want, false);
	}

	public SortableList<BigInteger, Order> getOrders(AssetCls have, AssetCls want, boolean filter) {
		return this.dbSet.getOrderMap()
				.getOrdersSortableList(have.getKey(), want.getKey(), filter);
	}
	
	public SortableList<Tuple2<BigInteger, BigInteger>, Trade> getTrades(
			AssetCls have, AssetCls want) {
		return this.dbSet.getTradeMap()
				.getTradesSortableList(have.getKey(), want.getKey());
	}

	public SortableList<Tuple2<BigInteger, BigInteger>, Trade> getTrades(
			Order order) {
		return this.dbSet.getTradeMap().getTrades(order);
	}

	// IMPRINTS
	public ImprintCls getItemImprint(long key) {
		return (ImprintCls)this.dbSet.getItemImprintMap().get(key);
	}

	// NOTES
	public NoteCls getItemNote(long key) {
		return (NoteCls)this.dbSet.getItemNoteMap().get(key);
	}

	// PERSONS
	public PersonCls getItemPerson(long key) {
		return (PersonCls)this.dbSet.getItemPersonMap().get(key);
	}

	// STATUSES
	public StatusCls getItemStatus(long key) {
		return (StatusCls)this.dbSet.getItemStatusMap().get(key);
	}
	// UNIONS
	public UnionCls getItemUnion(long key) {
		return (UnionCls)this.dbSet.getItemUnionMap().get(key);
	}

	// ALL ITEMS
	public ItemCls getItem(DBSet db, int type, long key) {
		
		switch(type)
			{
			case ItemCls.ASSET_TYPE: {
				return db.getItemAssetMap().get(key);
			}
			case ItemCls.IMPRINT_TYPE: {
				return db.getItemImprintMap().get(key);
			}
			case ItemCls.NOTE_TYPE: {
				return db.getItemNoteMap().get(key);
			}
			case ItemCls.PERSON_TYPE: {
				return db.getItemPersonMap().get(key);
			}
			case ItemCls.STATUS_TYPE: {
				return db.getItemStatusMap().get(key);	
			}
			case ItemCls.UNION_TYPE: {
				return db.getItemUnionMap().get(key);
			}
		}
		return null;
	}
	public ItemCls getItem(int type, long key) {
		return this.getItem(this.dbSet, type, key);
	}
		

	// ATs

	public SortableList<String, AT> getAcctATs(String type, boolean initiators) {
		return this.dbSet.getATMap().getAcctATs(type, initiators);
	}

	// TRANSACTIONS

	public void onTransactionCreate(Transaction transaction) {
		// ADD TO UNCONFIRMED TRANSACTIONS
		this.blockGenerator.addUnconfirmedTransaction(transaction);

		// NOTIFY OBSERVERS
		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.LIST_TRANSACTION_TYPE, this.dbSet
						.getTransactionMap().getValues()));

		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.ADD_TRANSACTION_TYPE, transaction));

		// BROADCAST
		this.broadcastTransaction(transaction);
	}

	public Pair<Transaction, Integer> registerName(
			PrivateKeyAccount registrant, Account owner, String name,
			String value,int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createNameRegistration(registrant,
					new Name(owner, name, value), feePow);
		}
	}


	public Pair<Transaction, Integer> updateName(PrivateKeyAccount owner,
			Account newOwner, String name, String value, int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createNameUpdate(owner, new Name(
					newOwner, name, value), feePow);
		}
	}

	public Pair<Transaction, Integer> sellName(PrivateKeyAccount owner,
			String name, BigDecimal amount,int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createNameSale(owner, new NameSale(
					name, amount), feePow);
		}
	}

	public Pair<Transaction, Integer> cancelSellName(PrivateKeyAccount owner,
			NameSale nameSale,int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createCancelNameSale(owner,
					nameSale, feePow);
		}
	}

	public Pair<Transaction, Integer> BuyName(PrivateKeyAccount buyer,
			NameSale nameSale,int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createNamePurchase(buyer, nameSale,
					feePow);
		}
	}

	public Pair<Transaction, Integer> createPoll(PrivateKeyAccount creator,
			String name, String description, List<String> options,
			int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			// CREATE POLL OPTIONS
			List<PollOption> pollOptions = new ArrayList<PollOption>();
			for (String option : options) {
				pollOptions.add(new PollOption(option));
			}

			// CREATE POLL
			Poll poll = new Poll(creator, name, description, pollOptions);

			return this.transactionCreator.createPollCreation(creator, poll,
					feePow);
		}
	}

	public Pair<Transaction, Integer> createPollVote(PrivateKeyAccount creator,
			Poll poll, PollOption option,int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			// GET OPTION INDEX
			int optionIndex = poll.getOptions().indexOf(option);

			return this.transactionCreator.createPollVote(creator,
					poll.getName(), optionIndex, feePow);
		}
	}

	public Pair<Transaction, Integer> createArbitraryTransaction(
			PrivateKeyAccount creator, List<Payment> payments, int service, byte[] data,int feePow) {
		
		if(payments == null) {
			payments = new ArrayList<Payment>();
		}
		
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createArbitraryTransaction(creator, payments,
					service, data, feePow);
		}
	}

	public Pair<Transaction, Integer> createTransactionFromRaw(
			byte[] rawData) {
		
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createTransactionFromRaw(rawData);
		}
	}
	
	public Transaction issueAsset(PrivateKeyAccount creator,
			String name, String description, boolean movable, long quantity, byte scale, boolean divisible,
			int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createIssueAssetTransaction(creator,
					name, description, movable, quantity, scale, divisible, feePow);
		}
	}

	public Pair<Transaction, Integer> issueImprint(PrivateKeyAccount creator,
			String name, String description, int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createIssueImprintTransaction(creator,
					name, description, feePow);
		}
	}

	public Pair<Transaction, Integer> issueNote(PrivateKeyAccount creator,
			String name, String description, int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createIssueNoteTransaction(creator,
					name, description, feePow);
		}
	}

	public Pair<Transaction, Integer> issuePerson(
			boolean forIssue,
			PrivateKeyAccount creator, String fullName, int feePow,
			long birthday, long deathday,
			byte gender, String race, float birthLatitude, float birthLongitude,
			String skinColor, String eyeColor, String hairСolor, int height,
			byte[] icon, byte[] image, String description,
			PublicKeyAccount owner, byte[] ownerSignature) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createIssuePersonTransaction(
					forIssue,
					creator, fullName, feePow, birthday, deathday,
					gender, race, birthLatitude, birthLongitude,
					skinColor, eyeColor, hairСolor, height,
					icon, image, description,
					owner, ownerSignature);
		}
	}
	public Pair<Transaction, Integer> issuePersonHuman(
			PrivateKeyAccount creator, int feePow, PersonHuman human) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createIssuePersonHumanTransaction(
					creator, feePow, human);
		}
	}

	public Pair<Transaction, Integer> issueStatus(PrivateKeyAccount creator,
			String name, String description, boolean unique, int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createIssueStatusTransaction(creator,
					name, description, unique, feePow);
		}
	}

	public Pair<Transaction, Integer> issueUnion(PrivateKeyAccount creator,
			String name, long birthday, long parent, String description, int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createIssueUnionTransaction(creator,
					name, birthday, parent, description, feePow);
		}
	}

	public Pair<Transaction, Integer> createOrder(PrivateKeyAccount creator,
			AssetCls have, AssetCls want, BigDecimal amountHave, BigDecimal amountWant,
			int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createOrderTransaction(creator,
					have, want, amountHave, amountWant, feePow);
		}
	}

	public Pair<Transaction, Integer> cancelOrder(PrivateKeyAccount creator,
			Order order,int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createCancelOrderTransaction(
					creator, order, feePow);
		}
	}

	public Pair<Transaction, Integer> deployAT(PrivateKeyAccount creator,
			String name, String description, String type, String tags,
			byte[] creationBytes, BigDecimal quantity,int feePow) {

		synchronized (this.transactionCreator) {
			return this.transactionCreator.deployATTransaction(creator, name,
					description, type, tags, creationBytes, quantity, feePow);
		}
	}

	public Pair<Transaction, Integer> sendMultiPayment(
			PrivateKeyAccount sender, List<Payment> payments,int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.sendMultiPayment(sender, payments,
					feePow);
		}
	}

	public Pair<Transaction, Integer> r_Send(PrivateKeyAccount sender,
			int feePow, Account recipient, long key, BigDecimal amount) {
		synchronized (this.transactionCreator) {
			return this.r_Send(sender, feePow, recipient,
					key, amount, "", null, null, null);
		}
	}
	public Pair<Transaction, Integer> r_Send(PrivateKeyAccount sender,
			int feePow, Account recipient, long key,BigDecimal amount,
			String head, byte[] isText, byte[] message, byte[] encryptMessage) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.r_Send(sender, recipient,
					key, amount, feePow, head, message, isText, encryptMessage);
		}
	}
	public Pair<Transaction, Integer> r_Send(byte version, PrivateKeyAccount sender,
			int feePow, Account recipient, long key,BigDecimal amount,
			String head, byte[] isText, byte[] message, byte[] encryptMessage) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.r_Send(version, sender, recipient,
					key, amount, feePow, head, message, isText, encryptMessage);
		}
	}

	public Pair<Transaction, Integer> signNote(boolean asPack, PrivateKeyAccount sender,
			int feePow,	long key, byte[] message, byte[] isText, byte[] encrypted) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.signNote(asPack, sender, feePow, key, message, isText, encrypted);
		}
	}

	public Pair<Transaction, Integer> r_SertifyPerson(int version, boolean asPack, PrivateKeyAccount creator,
			int feePow, long key,
			List<PublicKeyAccount> userAccounts, int add_day) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.r_SertifyPerson( version, asPack,
					creator, feePow, key,
					userAccounts, add_day);
		}
	}

	public Pair<Transaction, Integer> r_Vouch(int version, boolean asPack,
			PrivateKeyAccount creator, int feePow,
			int height, int seq) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.r_Vouch( version, asPack,
					creator, feePow,
					height, seq);
		}
	}

	public Pair<Transaction, Integer> r_Hashes(PrivateKeyAccount sender,
			int feePow, String url, String data, String hashes) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.r_Hashes(sender, feePow,
					url, data, hashes);
		}
	}

	public Pair<Transaction, Integer> r_Hashes(PrivateKeyAccount sender,
			int feePow, String url, String data, String[] hashes) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.r_Hashes(sender, feePow,
					url, data, hashes);
		}
	}

	/*
	// ver 1
	public Pair<Transaction, Integer> r_SetStatusToItem(int version, boolean asPack, PrivateKeyAccount creator,
			int feePow, long key,
			ItemCls item, Long beg_date, Long end_date,
			int value_1, int value_2, byte[] data, long refParent
			) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.r_SetStatusToItem( 0, asPack,
					creator, feePow, key,
					item, beg_date, end_date,
					value_1, value_2, data, refParent
					);
		}
	}
	*/
	// ver2 
	public Pair<Transaction, Integer> r_SetStatusToItem(int version, boolean asPack, PrivateKeyAccount creator,
			int feePow, long key,
			ItemCls item, Long beg_date, Long end_date,
			long value_1, long value_2, byte[] data_1, byte[] data_2, long refParent, byte[] description
			) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.r_SetStatusToItem( 1, asPack,
					creator, feePow, key,
					item, beg_date, end_date,
					value_1, value_2, data_1, data_2, refParent, description
					);
		}
	}

	/*
	public Pair<Transaction, Integer> sendJson(PrivateKeyAccount sender,
			Account recipient, long key, BigDecimal amount,int feePow,
			byte[] isText, byte[] message, byte[] encryptMessage) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createJson(sender, recipient,
					key, amount, feePow, message, isText, encryptMessage);
		}
	}
	public Pair<Transaction, Integer> sendAccounting(PrivateKeyAccount sender,
			Account recipient, long key, BigDecimal amount,int feePow,
			byte[] isText, byte[] message, byte[] encryptMessage) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createAccounting(sender, recipient,
					key, amount, feePow, message, isText, encryptMessage);
		}

	}
	*/
	
	public Block getBlockByHeight(DBSet db, int parseInt) {
		byte[] b = db.getBlockHeightsMap().get((long)parseInt);
		return db.getBlockMap().get(b);
	}
	public Block getBlockByHeight(int parseInt) {
		return getBlockByHeight(this.dbSet, parseInt);
	}


	public byte[] getPublicKeyByAddress(String address) {

		
		if (!Crypto.getInstance().isValidAddress(address)) {
			return null;
		}

		// CHECK ACCOUNT IN OWN WALLET
		Controller cntr = Controller.getInstance();
		Account account = cntr.getAccountByAddress(address);
		if (account != null) {
			if (cntr.isWalletUnlocked()) {
				return cntr.getPrivateKeyAccountByAddress(address).getPublicKey();
			}
		}

		DBSet db = this.dbSet;
		// get last transaction from this address
		byte[] sign = db.getAddressTime_SignatureMap().get(address);
		if (sign == null) {
			return null;
		}

		/*
		long lastReference = db.getReferenceMap().get(address);
		byte[] sign = getSignatureByAddrTime(db, address, lastReference);
		if (sign == null)
			return null;
			*/
				
		Transaction transaction = cntr.getTransaction(sign);
		if (transaction == null) {
			return null;
		}

		return transaction.getCreator().getPublicKey();
	}
}
