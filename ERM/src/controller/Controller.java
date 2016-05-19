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
// import org.apache.log4j.Logger;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.io.FileUtils;
import org.mapdb.Fun.Tuple2;

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
import network.message.GetBlockMessage;
import network.message.GetSignaturesMessage;
import network.message.HeightMessage;
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
	private String version = "2.14.01";
	private String buildTime = "2016-05-18 12:12:12 UTC";
	private long buildTimestamp;
	
	public static final String releaseVersion = "2.05.0";

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
	
	private Map<Peer, Integer> peerHeight;

	private Map<Peer, Pair<String, Long>> peersVersions;

	private static Controller instance;

	public boolean isProcessingWalletSynchronize() {
		return processingWalletSynchronize;
	}
	
	public void setProcessingWalletSynchronize(boolean isPocessing) {
		this.processingWalletSynchronize = isPocessing;
	}
	
	public String getVersion() {
		return version;
	}

	public int getNetworkPort() {
		if(Settings.getInstance().isTestnet()) {
			return Network.TESTNET_PORT;
		} else {
			return Network.MAINNET_PORT;
		}
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
		        if (!resource.getProtocol().equals("file")) {
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
			+ "| Last Block Signature: " + Base58.encode(this.blockChain.getLastBlock().getSignature()) + "\n"
			+ "| Last Block Height: " + this.blockChain.getLastBlock().getHeight() + "\n"
			+ "| Last Block Time: " + DateTimeFormat.timestamptoString(this.blockChain.getLastBlock().getTimestamp()) + "\n"
			+ "| Last Block Found " + DateTimeFormat.timeAgo(this.blockChain.getLastBlock().getTimestamp()) + " ago."
			);
	}
	
	public byte[] getFoundMyselfID() {
		return this.foundMyselfID;
	}
	
	public int getWalletSyncHeight()
	{
		return this.wallet.getSyncHeight();
	}
	
	public void getSendMyHeightToPeer (Peer peer) {
	
		// GET HEIGHT
		int height = this.blockChain.getHeight();
				
		// SEND HEIGTH MESSAGE
		peer.sendMessage(MessageFactory.getInstance().createHeightMessage(
				height));
	}
	
	public Map<Peer, Integer> getPeerHeights() {
		return peerHeight;
	}
	
	public Integer getHeightOfPeer(Peer peer) {
		if(peerHeight!=null && peerHeight.containsKey(peer)){
			return peerHeight.get(peer);
		}
		else
		{
			return 0;
		}
	}
	
	public Map<Peer, Pair<String, Long>> getPeersVersions() {
		return peersVersions;
	}
	
	public Pair<String, Long> getVersionOfPeer(Peer peer) {
		if(peerHeight!=null && peersVersions.containsKey(peer)){
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

		this.peerHeight = new LinkedHashMap<Peer, Integer>(); // LINKED TO
																// PRESERVE
																// ORDER WHEN
																// SYNCHRONIZING
																// (PRIORITIZE
																// SYNCHRONIZING
																// FROM LONGEST
																// CONNECTION
																// ALIVE)
		
		this.peersVersions = new LinkedHashMap<Peer, Pair<String, Long>>();
		
		this.status = STATUS_NO_CONNECTIONS;
		this.transactionCreator = new TransactionCreator();

		// OPENING DATABASES
		try {
			DBSet.getInstance();
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(),e);
			LOGGER.error(Lang.getInstance().translate("Error during startup detected trying to restore backup database..."));
			reCreateDB();
		}

//		startFromScratchOnDemand();

		if (DBSet.getInstance().getBlockMap().isProcessing()) {
			try {
				DBSet.getInstance().close();
			} catch (Throwable e) {
				LOGGER.error(e.getMessage(),e);
			}
			reCreateDB();
		}
		
		//CHECK IF DB NEEDS UPDATE

		if(DBSet.getInstance().getBlockMap().getLastBlockSignature() != null)
		{
			//CHECK IF NAME STORAGE NEEDS UPDATE
			if (DBSet.getInstance().getLocalDataMap().get("nsupdate") == null )
			{
				//FIRST NAME STORAGE UPDATE
				UpdateUtil.repopulateNameStorage( 70000 );
				DBSet.getInstance().getLocalDataMap().set("nsupdate", "1");
			}
			//CREATE TRANSACTIONS FINAL MAP
			if (DBSet.getInstance().getLocalDataMap().get("txfinalmap") == null
					|| !DBSet.getInstance().getLocalDataMap().get("txfinalmap").equals("2"))
			{
				//FIRST NAME STORAGE UPDATE
				UpdateUtil.repopulateTransactionFinalMap(  );
				DBSet.getInstance().getLocalDataMap().set("txfinalmap", "2");
			}
			
			if (DBSet.getInstance().getLocalDataMap().get("blogpostmap") == null ||  !DBSet.getInstance().getLocalDataMap().get("blogpostmap").equals("2"))
			{
				//recreate comment postmap
				UpdateUtil.repopulateCommentPostMap();
				DBSet.getInstance().getLocalDataMap().set("blogpostmap", "2");
			}
		} else {
			DBSet.getInstance().getLocalDataMap().set("nsupdate", "1");
			DBSet.getInstance().getLocalDataMap().set("txfinalmap", "2");
			DBSet.getInstance().getLocalDataMap().set("blogpostmap", "2");
		}
		
		// CREATE SYNCHRONIZOR
		this.synchronizer = new Synchronizer();

		// CREATE BLOCKCHAIN
		this.blockChain = new BlockChain();
		
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
		this.blockGenerator = new BlockGenerator();
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
	        				Controller.getInstance().getSendMyHeightToPeer(peer);
	        			}
	        		}
	        	}
	        }
		};
		
		this.timerPeerHeightUpdate.schedule(action, 5*60*1000, 5*60*1000);

		// REGISTER DATABASE OBSERVER
		this.addObserver(DBSet.getInstance().getTransactionMap());
		this.addObserver(DBSet.getInstance());
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

		if (DBSet.getInstance().getBlockMap().isProcessing()) {
			throw new Exception(
					Lang.getInstance().translate("The application was not closed correctly! Delete the folder ")
							+ dataDir.getAbsolutePath()
							+ Lang.getInstance().translate(" and start the application again."));
		}
	}

	public void startFromScratchOnDemand() throws IOException {
		String dataVersion = DBSet.getInstance().getLocalDataMap()
				.get(LocalDataMap.LOCAL_DATA_VERSION_KEY);

		if (dataVersion == null || !dataVersion.equals(releaseVersion)) {
			File dataDir = new File(Settings.getInstance().getDataDir());
			File dataBak = getDataBakDir(dataDir);
			DBSet.getInstance().close();

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

			DBSet.getInstance()
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
		DBSet.getInstance().getBlockMap().addObserver(o);

		// ADD OBSERVER TO BLOCKGENERATOR
		// this.blockGenerator.addObserver(o);
		DBSet.getInstance().getTransactionMap().addObserver(o);

		// ADD OBSERVER TO NAMESALES
		DBSet.getInstance().getNameExchangeMap().addObserver(o);

		// ADD OBSERVER TO POLLS
		DBSet.getInstance().getPollMap().addObserver(o);

		// ADD OBSERVER TO ASSETS
		DBSet.getInstance().getItemAssetMap().addObserver(o);

		// ADD OBSERVER TO IMPRINTS
		DBSet.getInstance().getItemImprintMap().addObserver(o);

		// ADD OBSERVER TO NOTES
		DBSet.getInstance().getItemNoteMap().addObserver(o);

		// ADD OBSERVER TO PERSONS
		DBSet.getInstance().getItemPersonMap().addObserver(o);

		// ADD OBSERVER TO STATUSES
		DBSet.getInstance().getItemStatusMap().addObserver(o);

		// ADD OBSERVER TO UNIONS
		DBSet.getInstance().getItemUnionMap().addObserver(o);
		
		// ADD OBSERVER TO ORDERS
		DBSet.getInstance().getOrderMap().addObserver(o);

		// ADD OBSERVER TO TRADES
		DBSet.getInstance().getTradeMap().addObserver(o);

		// ADD OBSERVER TO BALANCES
		DBSet.getInstance().getAssetBalanceMap().addObserver(o);

		// ADD OBSERVER TO ATMAP
		DBSet.getInstance().getATMap().addObserver(o);

		// ADD OBSERVER TO ATTRANSACTION MAP
		DBSet.getInstance().getATTransactionMap().addObserver(o);

		// ADD OBSERVER TO CONTROLLER
		super.addObserver(o);
		o.update(this, new ObserverMessage(ObserverMessage.NETWORK_STATUS,
				this.status));
	}

	@Override
	public void deleteObserver(Observer o) {
		DBSet.getInstance().getBlockMap().deleteObserver(o);

		super.deleteObserver(o);
	}

	public void deleteWalletObserver(Observer o) {
		this.wallet.deleteObserver(o);
	}

	private boolean isStopping = false;

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
			DBSet.getInstance().close();

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
		if (!DBSet.getInstance().getBlockMap().isProcessing()
				&& Settings.getInstance().isCheckpointingEnabled()) {
			DBSet.getInstance().close();

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
		return this.network.getActiveConnections();
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

		if(DBSet.getInstance().isStoped())
			return;
		
		// GET HEIGHT
		int height = this.blockChain.getHeight();

		//if(NTP.getTime() >= Transaction.getPOWFIX_RELEASE())
		if (true)
		{
			// SEND FOUNDMYSELF MESSAGE
			peer.sendMessage( MessageFactory.getInstance().createFindMyselfMessage( 
				Controller.getInstance().getFoundMyselfID() 
				));

			// SEND VERSION MESSAGE
			peer.sendMessage( MessageFactory.getInstance().createVersionMessage( 
				Controller.getInstance().getVersion(),
				this.getBuildTimestamp() ));
		}
		
		// SEND HEIGTH MESSAGE
		peer.sendMessage(MessageFactory.getInstance().createHeightMessage(
				height));
		
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

	public void onDisconnect(Peer peer) {
		synchronized (this.peerHeight) {
			
			this.peerHeight.remove(peer);
			
			this.peersVersions.remove(peer);
			
			if (this.peerHeight.size() == 0) {
				
				if(this.getToOfflineTime() == 0L) {
					//SET START OFFLINE TIME
					this.setToOfflineTime(NTP.getTime());
				}
				
				// UPDATE STATUS
				this.status = STATUS_NO_CONNECTIONS;

				
				// NOTIFY
				this.setChanged();
				this.notifyObservers(new ObserverMessage(
						ObserverMessage.NETWORK_STATUS, this.status));
			}
		}
	}

	public void onError(Peer peer) {
		this.onDisconnect(peer);
	}

	// SYNCHRONIZED DO NOT PROCESSS MESSAGES SIMULTANEOUSLY
	public void onMessage(Message message) {
		Message response;
		Block block;

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

			case Message.HEIGHT_TYPE:

				HeightMessage heightMessage = (HeightMessage) message;

				// ADD TO LIST
				synchronized (this.peerHeight) {
					this.peerHeight.put(heightMessage.getSender(),
							heightMessage.getHeight());
				}

				break;

			case Message.GET_SIGNATURES_TYPE:

				GetSignaturesMessage getHeadersMessage = (GetSignaturesMessage) message;

				// ASK SIGNATURES FROM BLOCKCHAIN
				List<byte[]> headers = this.blockChain
						.getSignatures(getHeadersMessage.getParent());

				// CREATE RESPONSE WITH SAME ID
				response = MessageFactory.getInstance().createHeadersMessage(
						headers);
				response.setId(message.getId());

				// SEND RESPONSE BACK WITH SAME ID
				message.getSender().sendMessage(response);

				break;

			case Message.GET_BLOCK_TYPE:

				GetBlockMessage getBlockMessage = (GetBlockMessage) message;

				// ASK BLOCK FROM BLOCKCHAIN
				block = this.blockChain
						.getBlock(getBlockMessage.getSignature());

				// CREATE RESPONSE WITH SAME ID
				response = MessageFactory.getInstance().createBlockMessage(
						block);
				response.setId(message.getId());

				// SEND RESPONSE BACK WITH SAME ID
				message.getSender().sendMessage(response);

				break;

			case Message.BLOCK_TYPE:

				BlockMessage blockMessage = (BlockMessage) message;

				// ASK BLOCK FROM BLOCKCHAIN
				block = blockMessage.getBlock();

				boolean isNewBlockValid = this.blockChain.isNewBlockValid(block);
				
				if(isNewBlockValid)	{
					synchronized (this.peerHeight) {
						this.peerHeight.put(message.getSender(),
								blockMessage.getHeight());
					}
				}
					
				if(this.isProcessingWalletSynchronize()) {
					
					break;
				}
				
				// CHECK IF VALID
				if (isNewBlockValid
						&& this.synchronizer.process(block)) {
					LOGGER.info(Lang.getInstance().translate("received new valid block"));

					// PROCESS
					// this.synchronizer.process(block);

					// BROADCAST
					List<Peer> excludes = new ArrayList<Peer>();
					excludes.add(message.getSender());
					this.network.broadcast(message, excludes);

					// UPDATE ALL PEER HEIGHTS TO OUR HEIGHT
					/*
					 * synchronized(this.peerHeight) { for(Peer peer:
					 * this.peerHeight.keySet()) { this.peerHeight.put(peer,
					 * this.blockChain.getHeight()); } }
					 */
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
					this.network.onError(message.getSender(), Lang.getInstance().translate("invalid transaction signature"));

					return;
				}

				// CHECK IF TRANSACTION HAS MINIMUM FEE AND MINIMUM FEE PER BYTE
				// AND UNCONFIRMED
				// TODO fee
				// transaction.calcFee();
				if (!DBSet.getInstance().getTransactionParentMap()
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

	public void addActivePeersObserver(Observer o) {
		this.network.addObserver(o);
	}

	public void removeActivePeersObserver(Observer o) {
		this.network.deleteObserver(o);
	}

	private void broadcastBlock(Block newBlock) {

		// CREATE MESSAGE
		Message message = MessageFactory.getInstance().createBlockMessage(
				newBlock);

		// BROADCAST MESSAGE
		List<Peer> excludes = new ArrayList<Peer>();
		this.network.broadcast(message, excludes);
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
		if (this.peerHeight.size() == 0) {
			return true;
		}

		int maxPeerHeight = this.getMaxPeerHeight();
		int chainHeight = this.blockChain.getHeight();
		return maxPeerHeight <= chainHeight;
	}
	
	public boolean isNSUpToDate() {
		return !Settings.getInstance().updateNameStorage();
	}

	public void update() {
		// UPDATE STATUS
		this.status = STATUS_SYNCHRONIZING;

		// NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.NETWORK_STATUS, this.status));
		
		Peer peer = null;
		try {
			// WHILE NOT UPTODATE
			while (!this.isUpToDate()) {
				// START UPDATE FROM HIGHEST HEIGHT PEER
				peer = this.getMaxHeightPeer();

				// SYNCHRONIZE FROM PEER
				this.synchronizer.synchronize(peer);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);

			if (peer != null) {
				// DISHONEST PEER
				this.network.onError(peer, e.getMessage());
			}
		}

		if (this.peerHeight.size() == 0) {
			// UPDATE STATUS
			this.status = STATUS_NO_CONNECTIONS;

			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(
					ObserverMessage.NETWORK_STATUS, this.status));
		} else {
			// UPDATE STATUS
			this.status = STATUS_OK;

			// NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(
					ObserverMessage.NETWORK_STATUS, this.status));
			
			Controller.getInstance().statusInfo();
		}
	}

	private Peer getMaxHeightPeer() {
		Peer highestPeer = null;
		int height = 0;

		try {
			synchronized (this.peerHeight) {
				for (Peer peer : this.peerHeight.keySet()) {
					if (highestPeer == null && peer != null) {
						highestPeer = peer;
					} else {
						// IF HEIGHT IS BIGGER
						if (height < this.peerHeight.get(peer)) {
							highestPeer = peer;
							height = this.peerHeight.get(peer);
						}

						// IF HEIGHT IS SAME
						if (height == this.peerHeight.get(peer)) {
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

	public int getMaxPeerHeight() {
		int height = 0;

		try {
			synchronized (this.peerHeight) {
				for (Peer peer : this.peerHeight.keySet()) {
					if (height < this.peerHeight.get(peer)) {
						height = this.peerHeight.get(peer);
					}
				}
			}
		} catch (Exception e) {
			// PEER REMOVED WHILE ITERATING
		}

		return height;
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
			LOGGER.info(Lang.getInstance().translate("Wallet needs to synchronize!"));
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
	public BigDecimal getUnconfirmedBalance(Account account, long key) {
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

		return getTransaction(signature, DBSet.getInstance());
	}
	
	public Transaction getTransaction(byte[] signature, DBSet database) {
		
		// CHECK IF IN BLOCK
		Block block = database.getTransactionParentMap()
				.getParent(signature);
		if (block != null) {
			return block.getTransaction(signature);
		}
		
		// CHECK IF IN TRANSACTION DATABASE
		return database.getTransactionMap().get(signature);
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
		return DBSet.getInstance().getNameExchangeMap().getNameSales();
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
				return DBSet.getInstance().getItemAssetMap();
			case ItemCls.IMPRINT_TYPE:
				return DBSet.getInstance().getItemImprintMap();
			case ItemCls.NOTE_TYPE:
				return DBSet.getInstance().getItemNoteMap();
			case ItemCls.PERSON_TYPE:
				return DBSet.getInstance().getItemPersonMap();
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
		return DBSet.getInstance().getPollMap().getValues();
	}

	public Collection<ItemCls> getAllItems(int type) {
		return getItemMap(type).getValues();
	}

	/*
	public Collection<ItemCls> getAllAssets() {
		return DBSet.getInstance().getAssetMap().getValues();
	}

	public Collection<ItemCls> getAllNotes() {
		return DBSet.getInstance().getNoteMap().getValues();
	}
	public Collection<ItemCls> getAllPersons() {
		return DBSet.getInstance().getPersonMap().getValues();
	}
	*/

	public void onDatabaseCommit() {
		this.wallet.commit();
	}

	public ForgingStatus getForgingStatus() {
		return this.blockGenerator.getForgingStatus();
	}

	// BLOCKCHAIN

	public int getHeight() {
		// need for TESTs
		return this.blockChain != null? this.blockChain.getHeight(): -1;
	}

	public Block getLastBlock() {
		return this.blockChain.getLastBlock();
	}
	
	public byte[] getWalletLastBlockSign() {
		return this.wallet.getLastBlockSignature();
	}
	
	public Block getBlock(byte[] header) {
		return this.blockChain.getBlock(header);
	}

	public Pair<Block, List<Transaction>> scanTransactions(Block block,
			int blockLimit, int transactionLimit, int type, int service,
			Account account) {
		return this.blockChain.scanTransactions(block, blockLimit,
				transactionLimit, type, service, account);

	}

	public long getNextBlockGeneratingBalance() {
		return BlockGenerator.getNextBlockGeneratingBalance(
				DBSet.getInstance(), DBSet.getInstance().getBlockMap()
						.getLastBlock());
	}

	public long getNextBlockGeneratingBalance(Block parent) {
		return BlockGenerator.getNextBlockGeneratingBalance(
				DBSet.getInstance(), parent);
	}

	// FORGE

	public void newBlockGenerated(Block newBlock) {

		this.synchronizer.process(newBlock);

		// BROADCAST
		this.broadcastBlock(newBlock);
	}

	public List<Transaction> getUnconfirmedTransactions() {
		return this.blockGenerator.getUnconfirmedTransactions();
	}

	// BALANCES

	public SortableList<Tuple2<String, Long>, BigDecimal> getBalances(long key) {
		return DBSet.getInstance().getAssetBalanceMap().getBalancesSortableList(key);
	}

	public SortableList<Tuple2<String, Long>, BigDecimal> getBalances(
			Account account) {
		return DBSet.getInstance().getAssetBalanceMap()
				.getBalancesSortableList(account);
	}

	// NAMES

	public Name getName(String nameName) {
		return DBSet.getInstance().getNameMap().get(nameName);
	}

	public NameSale getNameSale(String nameName) {
		return DBSet.getInstance().getNameExchangeMap().getNameSale(nameName);
	}

	// POLLS

	public Poll getPoll(String name) {
		return DBSet.getInstance().getPollMap().get(name);
	}

	// ASSETS

	/*
	public ItemCls getERMAsset() {
		return DBSet.getInstance().getAssetMap().get(0l);
	}
	*/
	public AssetCls getAsset(long key) {
		return (AssetCls) DBSet.getInstance().getItemAssetMap().get(key);
	}
	public PersonCls getPerson(long key) {
		return (PersonCls) DBSet.getInstance().getItemPersonMap().get(key);
	}

	public SortableList<BigInteger, Order> getOrders(AssetCls have, AssetCls want) {
		return this.getOrders(have, want, false);
	}

	public SortableList<BigInteger, Order> getOrders(AssetCls have, AssetCls want, boolean filter) {
		return DBSet.getInstance().getOrderMap()
				.getOrdersSortableList(have.getKey(), want.getKey(), filter);
	}
	
	public SortableList<Tuple2<BigInteger, BigInteger>, Trade> getTrades(
			AssetCls have, AssetCls want) {
		return DBSet.getInstance().getTradeMap()
				.getTradesSortableList(have.getKey(), want.getKey());
	}

	public SortableList<Tuple2<BigInteger, BigInteger>, Trade> getTrades(
			Order order) {
		return DBSet.getInstance().getTradeMap().getTrades(order);
	}

	// IMPRINTS
	public ImprintCls getItemImprint(long key) {
		return (ImprintCls)DBSet.getInstance().getItemImprintMap().get(key);
	}

	// NOTES
	public NoteCls getItemNote(long key) {
		return (NoteCls)DBSet.getInstance().getItemNoteMap().get(key);
	}

	// PERSONS
	public PersonCls getItemPerson(long key) {
		return (PersonCls)DBSet.getInstance().getItemPersonMap().get(key);
	}

	// STATUSES
	public StatusCls getItemStatus(long key) {
		return (StatusCls)DBSet.getInstance().getItemStatusMap().get(key);
	}
	// UNIONS
	public UnionCls getItemUnion(long key) {
		return (UnionCls)DBSet.getInstance().getItemUnionMap().get(key);
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
		return this.getItem(DBSet.getInstance(), type, key);
	}
		

	// ATs

	public SortableList<String, AT> getAcctATs(String type, boolean initiators) {
		return DBSet.getInstance().getATMap().getAcctATs(type, initiators);
	}

	// TRANSACTIONS

	public void onTransactionCreate(Transaction transaction) {
		// ADD TO UNCONFIRMED TRANSACTIONS
		this.blockGenerator.addUnconfirmedTransaction(transaction);

		// NOTIFY OBSERVERS
		this.setChanged();
		this.notifyObservers(new ObserverMessage(
				ObserverMessage.LIST_TRANSACTION_TYPE, DBSet.getInstance()
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
	
	public Pair<Transaction, Integer> issueAsset(PrivateKeyAccount creator,
			String name, String description, long quantity, byte scale, boolean divisible,
			int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createIssueAssetTransaction(creator,
					name, description, quantity, scale, divisible, feePow);
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

	public Pair<Transaction, Integer> issuePerson(PrivateKeyAccount creator, String fullName, int feePow,
			long birthday, long deathday,
			byte gender, String race, float birthLatitude, float birthLongitude,
			String skinColor, String eyeColor, String hairСolor, int height, String description) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createIssuePersonTransaction(creator, fullName, feePow, birthday, deathday,
					gender, race, birthLatitude, birthLongitude,
					skinColor, eyeColor, hairСolor, height, description);
		}
	}

	public Pair<Transaction, Integer> issueStatus(PrivateKeyAccount creator,
			String name, String description, int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createIssueStatusTransaction(creator,
					name, description, feePow);
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
			AssetCls have, AssetCls want, BigDecimal amount, BigDecimal price,
			int feePow) {
		// CREATE ONLY ONE TRANSACTION AT A TIME
		synchronized (this.transactionCreator) {
			return this.transactionCreator.createOrderTransaction(creator,
					have, want, amount, price, feePow);
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
					key, amount, null, null, null);
		}
	}
	public Pair<Transaction, Integer> r_Send(PrivateKeyAccount sender,
			int feePow, Account recipient, long key,BigDecimal amount,
			byte[] isText, byte[] message, byte[] encryptMessage) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.r_Send(sender, recipient,
					key, amount, feePow, message, isText, encryptMessage);
		}
	}

	public Pair<Transaction, Integer> recordNote(boolean asPack, PrivateKeyAccount sender,
			int feePow,	long key, byte[] message, byte[] isText) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.recordNote(asPack, sender, feePow, key, isText, message);
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

	public Pair<Transaction, Integer> r_SetStatusToItem(int version, boolean asPack, PrivateKeyAccount creator,
			int feePow, long key,
			ItemCls item, Long beg_date, Long end_date) {
		synchronized (this.transactionCreator) {
			return this.transactionCreator.r_SetStatusToItem( version, asPack,
					creator, feePow, key,
					item, beg_date, end_date);
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
	
	public Block getBlockByHeight(int parseInt) {
		byte[] b = DBSet.getInstance().getHeightMap().getBlockByHeight(parseInt);
		return DBSet.getInstance().getBlockMap().get(b);
	}

	public PublicKeyAccount getPublicKeyByAddress1(String address) {
		if(this.doesWalletExists()) {
			return this.wallet.getPublicKeyAccount(address);
		} else {
			return null;
		}
	}

	public byte[] getPublicKeyByAddress(String address) {

		
		if (!Crypto.getInstance().isValidAddress(address)) {
			return null;
		}

		// CHECK ACCOUNT IN OWN WALLET
		Account account = Controller.getInstance().getAccountByAddress(address);
		if (account != null) {
			if (Controller.getInstance().isWalletUnlocked()) {
				return Controller.getInstance()
						.getPrivateKeyAccountByAddress(address).getPublicKey();
			}
		}

		if (!DBSet.getInstance().getReferenceMap().contains(address)) {
			return null;
		}

		Transaction transaction = Controller.getInstance().getTransaction(
				DBSet.getInstance().getReferenceMap().get(address));

		if (transaction == null) {
			return null;
		}

		return transaction.getCreator().getPublicKey();
	}
}
