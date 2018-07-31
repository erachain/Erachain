package controller;

import api.ApiClient;
import api.ApiService;
import at.AT;
import com.google.common.primitives.Longs;
import core.BlockChain;
import core.BlockGenerator;
import core.BlockGenerator.ForgingStatus;
import core.Synchronizer;
import core.TransactionCreator;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.AEScrypto;
import core.crypto.Base32;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import core.item.assets.Trade;
import core.item.imprints.ImprintCls;
import core.item.persons.PersonCls;
import core.item.persons.PersonHuman;
import core.item.polls.PollCls;
import core.item.statuses.StatusCls;
import core.item.templates.TemplateCls;
import core.item.unions.UnionCls;
import core.naming.Name;
import core.naming.NameSale;
import core.payment.Payment;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import core.voting.Poll;
import core.voting.PollOption;
import core.wallet.Wallet;
import database.DBSet;
import datachain.*;
import gui.AboutFrame;
import gui.Gui;
import lang.Lang;
import network.Network;
import network.Peer;
import network.message.*;
import ntp.NTP;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import settings.Settings;
import utils.*;
import webserver.WebService;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;

// 04/01 +-

public class Controller extends Observable {

    public static final char DECIMAL_SEPARATOR = '.';
    public static final char GROUPING_SEPARATOR = '`';
    // IF new abilities is made - new license insert in CHAIN and set this KEY
    public static final long LICENSE_VERS = 107; // versopn of LICENSE
    public static HashMap<String, Tuple2<Integer, Integer>> LICENSE_LANG_REFS = new HashMap<String, Tuple2<Integer, Integer>>() {
        {
            put("en", new Tuple2<Integer, Integer>(148450, 1));
            put("ru", new Tuple2<Integer, Integer>(148450, 1));
        }
    };

    public static TreeMap<String, Tuple2<BigDecimal, String>> COMPU_RATES = new TreeMap();

    public static final String APP_NAME = BlockChain.DEVELOP_USE ? "Erachain-dev" : "Erachain";
    public final static long MIN_MEMORY_TAIL = 50000000;
    // used in controller.Controller.startFromScratchOnDemand() - 0 uses in
    // code!
    // for reset DB if DB PROTOCOL is CHANGED
    public static final String releaseVersion = "3.02.01";
    // TODO ENUM would be better here
    public static final int STATUS_NO_CONNECTIONS = 0;
    public static final int STATUS_SYNCHRONIZING = 1;
    public static final int STATUS_OK = 2;
    private static final Logger LOGGER = Logger.getLogger(Controller.class);
    private static final String version = "4.11.02 alpha";
    private static final String buildTime = "2017-11-19 15:33:33 UTC";
    public static boolean useGui = true;
    private static List<Thread> threads = new ArrayList<Thread>();
    private static long buildTimestamp;
    private static Controller instance;
    public Wallet wallet;
    private boolean processingWalletSynchronize = false;
    private int status;
    private boolean dcSetWithObserver = false;
    private boolean dynamicGUI = false;
    private Network network;
    private ApiService rpcService;
    private WebService webService;
    private BlockChain blockChain;
    private BlockGenerator blockGenerator;
    private Synchronizer synchronizer;
    private TransactionCreator transactionCreator;
    private boolean needSyncWallet = false;
    private Timer timer = new Timer();
    private Timer timerUnconfirmed = new Timer();
    private Timer timerPeerHeightUpdate = new Timer();
    private Random random = new SecureRandom();
    private byte[] foundMyselfID = new byte[128];
    private byte[] messageMagic;
    private long toOfflineTime;
    private Map<Peer, Tuple2<Integer, Long>> peerHWeight;
    private Map<Peer, Pair<String, Long>> peersVersions;
    private HashSet<byte[]> waitWinBufferProcessed = new HashSet<byte[]>();
    private DBSet dbSet; // = DBSet.getInstance();
    private DCSet dcSet; // = DBSet.getInstance();

    // private JSONObject Setting_Json;

    private AboutFrame about_frame;
    private boolean isStopping = false;
    private String info;

    public static String getVersion() {
        return version;
    }

    public static String getBuildDateTimeString() {
        return DateTimeFormat.timestamptoString(getBuildTimestamp(), "yyyy-MM-dd HH:mm:ss z", "UTC");
    }

    public static String getBuildDateString() {
        return DateTimeFormat.timestamptoString(getBuildTimestamp(), "yyyy-MM-dd", "UTC");
    }

    public static long getBuildTimestamp() {
        if (buildTimestamp == 0) {
            Date date = new Date();
            //// URL resource =
            //// getClass().getResource(getClass().getSimpleName() + ".class");
            // URL resource =
            //// Controller.class.getResource(Controller.class.getSimpleName() +
            //// ".class");
            // if (resource != null && resource.getProtocol().equals("file")) {
            File f = null;
            Path p = null;
            BasicFileAttributes attr = null;
            try {
                f = new File(Controller.APP_NAME.toLowerCase() + ".jar");
                p = f.toPath();
                attr = Files.readAttributes(p, BasicFileAttributes.class);
            } catch (Exception e1) {
                try {
                    f = new File(Controller.APP_NAME.toLowerCase() + ".exe");
                    p = f.toPath();
                    attr = Files.readAttributes(p, BasicFileAttributes.class);
                } catch (Exception e2) {
                }
            }

            if (attr != null) {
                buildTimestamp = attr.lastModifiedTime().toMillis();
                return buildTimestamp;
            }

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            try {
                date = formatter.parse(buildTime);
                buildTimestamp = date.getTime();
            } catch (ParseException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return buildTimestamp;
    }

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
            instance.setDCSetWithObserver(Settings.getInstance().isGuiEnabled());
            instance.setDynamicGUI(Settings.getInstance().isGuiDynamic());
        }

        return instance;
    }

    public boolean isProcessingWalletSynchronize() {
        return processingWalletSynchronize;
    }

    public void setProcessingWalletSynchronize(boolean isPocessing) {
        this.processingWalletSynchronize = isPocessing;
    }

    public void setDCSetWithObserver(boolean dcSetWithObserver) {
        this.dcSetWithObserver = dcSetWithObserver;
    }

    public void setDynamicGUI(boolean dynamicGUI) {
        this.dynamicGUI = dynamicGUI;
    }

    public void setDCSet(DCSet db) {
        this.dcSet = db;
    }

    public DBSet getDBSet() {
        return this.dbSet;
    }

    public int getNetworkPort() {
        if (Settings.getInstance().isTestnet()) {
            return BlockChain.TESTNET_PORT;
        } else {
            return BlockChain.MAINNET_PORT;
        }
    }

    public boolean isTestNet() {
        return Settings.getInstance().isTestnet();
    }

    public byte[] getMessageMagic() {
        if (this.messageMagic == null) {
            long longTestNetStamp = Settings.getInstance().getGenesisStamp();
            if (Settings.getInstance().isTestnet()) {
                byte[] seedTestNetStamp = Crypto.getInstance().digest(Longs.toByteArray(longTestNetStamp));
                this.messageMagic = Arrays.copyOfRange(seedTestNetStamp, 0, Message.MAGIC_LENGTH);
            } else {
                this.messageMagic = Message.MAINNET_MAGIC;
            }
        }
        return this.messageMagic;
    }

    public void statusInfo() {
        long timestamp = this.blockChain.getLastBlock(dcSet).getTimestamp(this.dcSet);
        LOGGER.info("STATUS " + this.getStatus() + "\n" + "| Last Block Signature: "
                + Base58.encode(this.blockChain.getLastBlock(dcSet).getSignature()) + "\n" + "| Last Block Height: "
                + this.blockChain.getLastBlock(dcSet).getHeight(this.dcSet) + "\n" + "| Last Block Time: "
                + DateTimeFormat.timestamptoString(timestamp));
    }

    public byte[] getFoundMyselfID() {
        return this.foundMyselfID;
    }

    public Wallet getWallet() {
        return this.wallet;
    }

    public boolean isAllThreadsGood() {
        if (!this.blockGenerator.isAlive()) {
            return false;
        }

        for (Thread thread : this.threads) {
            if (thread.isInterrupted() || !thread.isAlive())
                return false;
        }
        return true;
    }

    public int getWalletSyncHeight() {
        return this.wallet.getSyncHeight();
    }

    public void sendMyHWeightToPeer(Peer peer) {

        // SEND HEIGHT MESSAGE
        peer.sendMessage(MessageFactory.getInstance().createHWeightMessage(this.blockChain.getHWeightFull(dcSet)));
    }

    public TransactionCreator getTransactionCreator() {
        return transactionCreator;
    }

    public Map<Peer, Tuple2<Integer, Long>> getPeerHWeights() {
        return peerHWeight;
    }

    public Tuple2<Integer, Long> getHWeightOfPeer(Peer peer) {
        if (peerHWeight != null && peerHWeight.containsKey(peer)) {
            return peerHWeight.get(peer);
        } else {
            return null;
        }
    }

    public void setWeightOfPeer(Peer peer, Tuple2<Integer, Long> hWeight) {
        if (peerHWeight != null) {
            peerHWeight.put(peer, hWeight);
        }
    }

    public void resetWeightOfPeer(Peer peer) {
        peerHWeight.put(peer, this.blockChain.getHWeightFull(this.dcSet));
    }
    /*
     * public static Controller getInstance(boolean withObserver, boolean
     * dynamicGUI) { if (instance == null) { instance = new Controller();
     * instance.setDCSetWithObserver(withObserver);
     * instance.setDynamicGUI(dynamicGUI); }
     *
     * return instance; }
     *
     */

    public Map<Peer, Pair<String, Long>> getPeersVersions() {
        return peersVersions;
    }

    public Pair<String, Long> getVersionOfPeer(Peer peer) {
        if (peersVersions != null && peersVersions.containsKey(peer)) {
            return peersVersions.get(peer);
        } else {
            return new Pair<String, Long>("", 0l);
        }
    }

    public int getStatus() {
        return this.status;
    }

    public boolean needUpToDate() {
        return this.status != STATUS_OK && this.status != STATUS_NO_CONNECTIONS;
    }

    public boolean isStatusOK() {
        return this.status == STATUS_OK;
    }

    public boolean checkNeedSyncWallet() {
        if (this.wallet == null || this.wallet.database == null)
            return false;

        // CHECK IF WE NEED TO RESYNC
        byte[] lastBlockSignature = this.wallet.database.getLastBlockSignature();
        if (lastBlockSignature == null
                ///// || !findLastBlockOff(lastBlockSignature, block)
                || !Arrays.equals(lastBlockSignature, this.getBlockChain().getLastBlockSignature(dcSet))) {
            return true;
        }

        return false;

    }

    public boolean isNeedSyncWallet() {
        return this.needSyncWallet;
    }

    public void setNeedSyncWallet(boolean needSync) {
        this.needSyncWallet = needSync;
    }

    public void start() throws Exception {

        this.toOfflineTime = NTP.getTime();
        if (Controller.useGui)
            about_frame = AboutFrame.getInstance();
        this.foundMyselfID = new byte[128];
        this.random.nextBytes(this.foundMyselfID);

        this.peerHWeight = new LinkedHashMap<Peer, Tuple2<Integer, Long>>();
        // LINKED TO PRESERVE ORDER WHEN SYNCHRONIZING (PRIORITIZE SYNCHRONIZING
        // FROM LONGEST CONNECTION ALIVE)

        this.peersVersions = new LinkedHashMap<Peer, Pair<String, Long>>();

        // CHECK NETWORK PORT AVAILABLE
        if (!Network.isPortAvailable(Controller.getInstance().getNetworkPort())) {
            throw new Exception(Lang.getInstance().translate("Network port %port% already in use!").replace("%port%",
                    String.valueOf(Controller.getInstance().getNetworkPort())));
        }

        // CHECK RPC PORT AVAILABLE
        if (Settings.getInstance().isRpcEnabled()) {
            if (!Network.isPortAvailable(Settings.getInstance().getRpcPort())) {
                throw new Exception(Lang.getInstance().translate("Rpc port %port% already in use!").replace("%port%",
                        String.valueOf(Settings.getInstance().getRpcPort())));
            }
        }

        // CHECK WEB PORT AVAILABLE
        if (Settings.getInstance().isWebEnabled()) {
            if (!Network.isPortAvailable(Settings.getInstance().getWebPort())) {
                LOGGER.error(Lang.getInstance().translate("Web port %port% already in use!").replace("%port%",
                        String.valueOf(Settings.getInstance().getWebPort())));
            }
        }

        this.transactionCreator = new TransactionCreator();

        // Setting_Json = new JSONObject();
        // Setting_Json = Settings.getInstance().read_setting_JSON();

        int error = 0;
        // OPEN DATABASE

        try {
            if (Controller.useGui)
                about_frame.set_console_Text(Lang.getInstance().translate("Open DataLocale"));
            this.dbSet = DBSet.getinstanse();
            if (Controller.useGui)
                about_frame.set_console_Text(Lang.getInstance().translate("DataLocale OK"));
            LOGGER.info("DataLocale OK");
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            // e1.printStackTrace();
            reCreateDB();
            LOGGER.error("Error during startup detected trying to restore backup DataLocale...");
        }

        // OPENING DATABASES
        try {
            if (Controller.useGui)
                about_frame.set_console_Text(Lang.getInstance().translate("Open DataChain"));
            this.dcSet = DCSet.getInstance(this.dcSetWithObserver, this.dynamicGUI);
            if (Controller.useGui)
                about_frame.set_console_Text(Lang.getInstance().translate("DataChain OK"));
            LOGGER.info("DataChain OK");
        } catch (Throwable e) {
            // Error open DB
            error = 1;
            // LOGGER.error(e.getMessage(),e);
            LOGGER.error("Error during startup detected trying to restore backup DataChain...");
            reCreateDC();
        }
        if (error == 0 && Controller.useGui && Settings.getInstance().getbacUpEnabled()) {

            if (Settings.getInstance().getbacUpAskToStart()) {
                // ask dialog
                int n = JOptionPane.showConfirmDialog(null, Lang.getInstance().translate("BackUp Database?"),
                        Lang.getInstance().translate("Confirmation"), JOptionPane.OK_CANCEL_OPTION);
                if (n == JOptionPane.OK_OPTION) {
                    about_frame.set_console_Text(Lang.getInstance().translate("BackUp datachain"));
                    // delete & copy files in BackUp dir
                    createDataCheckpoint();
                }
            } else {
                about_frame.set_console_Text(Lang.getInstance().translate("BackUp datachain"));
                // delete & copy files in BackUp dir
                createDataCheckpoint();
            }
        }
        if (this.dcSet.getBlockMap().isProcessing()) {
            try {
                this.dcSet.close();
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }
            reCreateDC();
        }

        if (Controller.useGui)
            about_frame.set_console_Text(Lang.getInstance().translate("Datachain Ok"));
        // createDataCheckpoint();

        // CHECK IF DB NEEDS UPDATE
        /*
         * try { if(this.dcSet.getBlockMap().getLastBlockSignature() != null) {
         * //CHECK IF NAME STORAGE NEEDS UPDATE if
         * (this.dcSet.getLocalDataMap().get("nsupdate") == null ) { //FIRST
         * NAME STORAGE UPDATE UpdateUtil.repopulateNameStorage( 70000 );
         * this.dcSet.getLocalDataMap().set("nsupdate", "1"); } //CREATE
         * TRANSACTIONS FINAL MAP if
         * (this.dcSet.getLocalDataMap().get("txfinalmap") == null ||
         * !this.dcSet.getLocalDataMap().get("txfinalmap").equals("2")) {
         * //FIRST NAME STORAGE UPDATE UpdateUtil.repopulateTransactionFinalMap(
         * ); this.dcSet.getLocalDataMap().set("txfinalmap", "2"); }
         *
         * if (this.dcSet.getLocalDataMap().get("blogpostmap") == null ||
         * !this.dcSet.getLocalDataMap().get("blogpostmap").equals("2")) {
         * //recreate comment postmap UpdateUtil.repopulateCommentPostMap();
         * this.dcSet.getLocalDataMap().set("blogpostmap", "2"); } } else {
         * this.dcSet.getLocalDataMap().set("nsupdate", "1");
         * this.dcSet.getLocalDataMap().set("txfinalmap", "2");
         * this.dcSet.getLocalDataMap().set("blogpostmap", "2"); } } catch
         * (Exception e12) { createDataCheckpoint(); //
         * Setting_Json.put("DB_OPEN", "Open BAD - try reCreate"); }
         */

        // CREATE SYNCHRONIZOR
        this.synchronizer = new Synchronizer();

        // CREATE BLOCKCHAIN
        this.blockChain = new BlockChain(dcSet);

        // START API SERVICE
        if (Settings.getInstance().isRpcEnabled()) {
            if (Controller.useGui)
                about_frame.set_console_Text(Lang.getInstance().translate("Start API Service"));
            this.rpcService = new ApiService();
            this.rpcService.start();
        }

        // START WEB SERVICE
        if (Settings.getInstance().isWebEnabled()) {
            if (Controller.useGui)
                about_frame.set_console_Text(Lang.getInstance().translate("Start WEB Service"));
            this.webService = new WebService();
            this.webService.start();
        }

        // CREATE WALLET
        if (Controller.useGui)
            about_frame.set_console_Text(Lang.getInstance().translate("Open Wallet"));
        this.wallet = new Wallet();

        if (this.wallet.isWalletDatabaseExisting()) {
            this.wallet.initiateItemsFavorites();
        }
        if (Controller.useGui)
            about_frame.set_console_Text(Lang.getInstance().translate("Wallet OK"));

        if (Settings.getInstance().isTestnet() && this.wallet.isWalletDatabaseExisting()
                && !this.wallet.getAccounts().isEmpty()) {
            this.wallet.synchronize(true);
        }

        // CREATE BLOCKGENERATOR
        this.blockGenerator = new BlockGenerator(true);
        // START UPDATES and BLOCK BLOCKGENERATOR
        this.blockGenerator.start();

        // CREATE NETWORK
        this.network = new Network();

        // CLOSE ON UNEXPECTED SHUTDOWN
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopAll(0);
            }
        });

        if (false) {
            // TIMER TO SEND HEIGHT TO NETWORK EVERY 5 MIN

            this.timerPeerHeightUpdate.cancel();
            this.timerPeerHeightUpdate = new Timer();

            TimerTask action = new TimerTask() {
                @Override
                public void run() {

                    if (Controller.getInstance().getStatus() == STATUS_OK) {
                        List<Peer> peers = Controller.getInstance().getActivePeers();
                        int peersCounter = peers.size();
                        if (peersCounter > 0) {
                            Peer peer = peers.get(random.nextInt(peers.size()));
                            if (peer != null) {
                                Controller.getInstance().sendMyHWeightToPeer(peer);
                            }
                        }
                    }
                }
            };

            this.timerPeerHeightUpdate.schedule(action, BlockChain.GENERATING_MIN_BLOCK_TIME_MS >> 4,
                    BlockChain.GENERATING_MIN_BLOCK_TIME_MS >> 2);
        }

        if (Settings.getInstance().isTestnet())
            this.status = STATUS_OK;

        // REGISTER DATABASE OBSERVER
        // this.addObserver(this.dbSet.getPeerMap());
        this.addObserver(this.dcSet);

        // start memory viewer
        MemoryViewer mamoryViewer = new MemoryViewer(this);
        mamoryViewer.start();


        this.COMPU_RATES.put("ru", new Tuple2<BigDecimal, String>
                (new BigDecimal(245 * 62).setScale(2), "\u20BD"));
        this.COMPU_RATES.put("en", new Tuple2<BigDecimal, String>
                (new BigDecimal(245).setScale(2), "$"));
    }

    // need for TESTS
    public void initBlockChain(DCSet dcSet) {
        try {
            this.blockChain = new BlockChain(dcSet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void replaseFavoriteItems(int type) {
        this.wallet.replaseFavoriteItems(type);
    }

    public DCSet reCreateDC() throws IOException, Exception {
        File dataChain = new File(Settings.getInstance().getDataDir());
        File dataChainBackUp = new File(Settings.getInstance().getBackUpDir() + File.separator
                + Settings.getInstance().DEFAULT_DATA_DIR + File.separator);
        // del datachain
        if (dataChain.exists()) {
            try {
                Files.walkFileTree(dataChain.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        // copy Back dir to DataChain
        if (dataChainBackUp.exists()) {

            try {
                FileUtils.copyDirectory(dataChainBackUp, dataChain);
                LOGGER.info("Restore BackUp/DataChain to DataChain is Ok");
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }

        }

        DCSet.reCreateDatabase(this.dcSetWithObserver, this.dynamicGUI);
        this.dcSet = DCSet.getInstance();
        return this.dcSet;
    }

    // recreate DB locate
    public DBSet reCreateDB() throws IOException, Exception {

        File dataLocal = new File(Settings.getInstance().getLocalDir());
        File dataLocalBackUp = new File(Settings.getInstance().getBackUpDir() + File.separator
                + Settings.getInstance().DEFAULT_LOCAL_DIR + File.separator);

        // del DataLocal
        if (dataLocal.exists()) {
            try {
                Files.walkFileTree(dataLocal.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        // copy Loc dir to Back
        if (dataLocalBackUp.exists()) {

            try {
                FileUtils.copyDirectory(dataLocalBackUp, dataLocal);
                LOGGER.info("Restore BackUp/DataLocal to DataLocal is Ok");
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }

        }
        DBSet.reCreateDatabase();
        this.dbSet = DBSet.getinstanse();
        return this.dbSet;
    }

    public void startFromScratchOnDemand() throws IOException, Exception {
        String dataVersion = this.dcSet.getLocalDataMap().get(LocalDataMap.LOCAL_DATA_VERSION_KEY);

        if (dataVersion == null || !dataVersion.equals(releaseVersion)) {
            File dataDir = new File(Settings.getInstance().getDataDir());
            File dataBak = getDataBakDir(dataDir);
            this.dcSet.close();

            if (dataDir.exists()) {
                // delete data folder
                java.nio.file.Files.walkFileTree(dataDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());

            }

            if (dataBak.exists()) {
                // delete data folder
                java.nio.file.Files.walkFileTree(dataBak.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
            }
            DCSet.reCreateDatabase(this.dcSetWithObserver, this.dynamicGUI);

            this.dcSet.getLocalDataMap().set(LocalDataMap.LOCAL_DATA_VERSION_KEY, Controller.releaseVersion);

        }
    }

    private File getDataBakDir(File dataDir) {
        return new File(dataDir.getParent(), Settings.getInstance().getDataDir() + "Bak");
    }

    public void rpcServiceRestart() {
        if (this.rpcService != null)
            this.rpcService.stop();

        // START API SERVICE
        if (Settings.getInstance().isRpcEnabled()) {
            this.rpcService = new ApiService();
            this.rpcService.start();
        }
    }

    public void webServiceRestart() {

        if (this.webService != null)
            this.webService.stop();

        // START API SERVICE
        if (Settings.getInstance().isWebEnabled()) {
            this.webService = new WebService();
            this.webService.start();
        }
    }

    @Override
    public void addObserver(Observer o) {

        this.dcSet.getBlockMap().addObserver(o);
        this.dcSet.getTransactionMap().addObserver(o);
        // this.dcSet.getTransactionFinalMap().addObserver(o);

        if (this.dcSetWithObserver) {
            // ADD OBSERVER TO SYNCHRONIZER
            // this.synchronizer.addObserver(o);

            // ADD OBSERVER TO BLOCKGENERATOR
            // this.blockGenerator.addObserver(o);

            // ADD OBSERVER TO NAMESALES
            this.dcSet.getNameExchangeMap().addObserver(o);

            // ADD OBSERVER TO POLLS
            //this.dcSet.getPollMap().addObserver(o);

            // ADD OBSERVER TO ASSETS
            this.dcSet.getItemAssetMap().addObserver(o);

            // ADD OBSERVER TO IMPRINTS
            this.dcSet.getItemImprintMap().addObserver(o);

            // ADD OBSERVER TO TEMPLATES
            this.dcSet.getItemTemplateMap().addObserver(o);

            // ADD OBSERVER TO PERSONS
            this.dcSet.getItemPersonMap().addObserver(o);

            // ADD OBSERVER TO STATUSES
            this.dcSet.getItemStatusMap().addObserver(o);

            // ADD OBSERVER TO UNIONS
            this.dcSet.getItemUnionMap().addObserver(o);

            // ADD OBSERVER TO ORDERS
            this.dcSet.getOrderMap().addObserver(o);

            // ADD OBSERVER TO TRADES
            this.dcSet.getTradeMap().addObserver(o);

            // ADD OBSERVER TO BALANCES
            this.dcSet.getAssetBalanceMap().addObserver(o);

            // ADD OBSERVER TO ATMAP
            this.dcSet.getATMap().addObserver(o);

            // ADD OBSERVER TO ATTRANSACTION MAP
            this.dcSet.getATTransactionMap().addObserver(o);
        }

        // ADD OBSERVER TO CONTROLLER
        super.addObserver(o);
        o.update(this, new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));
    }

    @Override
    public void deleteObserver(Observer o) {
        this.dcSet.getBlockMap().deleteObserver(o);

        super.deleteObserver(o);
    }

    public void deleteWalletObserver(Observer o) {
        this.wallet.deleteObserver(o);
    }

    public boolean isOnStopping() {
        return this.isStopping;
    }

    public void stopAll(Integer par) {
        // PREVENT MULTIPLE CALLS
        if (this.isStopping)
            return;
        this.isStopping = true;
        // delete temp Dir
        LOGGER.info("Delete files from TEMP dir");
        for (File file : new File(Settings.getInstance().getTemDir()).listFiles())
            if (file.isFile()) file.delete();

        // STOP MESSAGE PROCESSOR
        LOGGER.info("Stopping message processor");
        this.network.stop();

        // STOP BLOCK PROCESSOR
        LOGGER.info("Stopping block processor");
        // boolean a = this.blockGenerator.isAlive() ;
        // this.blockGenerator.interrupt();
        // a = this.blockGenerator.isAlive() ;
        // while(this.blockGenerator.isAlive()){
        // };
        this.synchronizer.stop();

        // CLOSE DATABABASE
        LOGGER.info("Closing database");
        this.dcSet.close();

        // CLOSE WALLET
        LOGGER.info("Closing wallet");
        this.wallet.close();

        LOGGER.info("Closed.");
        // FORCE CLOSE
        LOGGER.info("EXIT parameter:" + par);
        System.exit(par);
        // bat
        // if %errorlevel% neq 0 exit /b %errorlevel%

    }

    private void createDataCheckpoint() {
        if (!this.dcSet.getBlockMap().isProcessing()) {
            // && Settings.getInstance().isCheckpointingEnabled()) {
            // this.dcSet.close();

            File dataDir = new File(Settings.getInstance().getDataDir());
            File dataLoc = new File(Settings.getInstance().getLocalDir());

            File dataBakDC = new File(Settings.getInstance().getBackUpDir() + File.separator
                    + Settings.getInstance().DEFAULT_DATA_DIR + File.separator);
            File dataBakLoc = new File(Settings.getInstance().getBackUpDir() + File.separator
                    + Settings.getInstance().DEFAULT_LOCAL_DIR + File.separator);
            // copy Data dir to Back
            if (dataDir.exists()) {
                if (dataBakDC.exists()) {
                    try {
                        Files.walkFileTree(dataBakDC.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                try {
                    FileUtils.copyDirectory(dataDir, dataBakDC);
                    LOGGER.info("Copy DataChain to BackUp");
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }

            }
            // copy Loc dir to Back
            if (dataLoc.exists()) {
                if (dataBakLoc.exists()) {
                    try {
                        Files.walkFileTree(dataBakLoc.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                try {
                    FileUtils.copyDirectory(dataLoc, dataBakLoc);
                    LOGGER.info("Copy DataLocal to BackUp");
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }

            }

        }

    }

    // NETWORK

    public List<Peer> getActivePeers() {
        // GET ACTIVE PEERS
        return this.network.getActivePeers(false);
    }

    public void pingAllPeers(boolean onlySynchronized) {
        // PINF ALL ACTIVE PEERS
        if (this.network != null)
            this.network.pingAllPeers(null, onlySynchronized);
    }

    public int getActivePeersCounter() {
        // GET ACTIVE PEERS
        return this.network.getActivePeersCounter(false);
    }

    public void walletSyncStatusUpdate(int height) {
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_SYNC_STATUS, height));
    }

    public void blockchainSyncStatusUpdate(int height) {
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.BLOCKCHAIN_SYNC_STATUS, height));
    }

    public long getToOfflineTime() {
        return this.toOfflineTime;
    }

    public void setToOfflineTime(long time) {
        this.toOfflineTime = time;
    }

    public void notifyObserveUpdatePeer(Peer peer) {
        this.network.notifyObserveUpdatePeer(peer);
    }

    public void broadcastUnconfirmedToPeer(Peer peer) {

        // LOGGER.info(peer.getAddress() + " sended UNCONFIRMED ++++ START ");

        byte[] peerByte = peer.getAddress().getAddress();

        if (this.isStopping)
            return;

        TransactionMap map = this.dcSet.getTransactionMap();
        Iterator<byte[]> iterator = map.getIterator(0, false);
        Transaction transaction;
        Message message;
        long ping = 0;
        int counter = 0;
        ///////// big maxCounter freeze network and make bans on response
        ///////// headers and blocks
        int stepCount = 128; // datachain.TransactionMap.MAX_MAP_SIZE>>2;
        long dTime = this.blockChain.getTimestamp(this.dcSet);

        while (iterator.hasNext() && stepCount > 4) {

            if (this.isStopping) {
                return;
            }

            transaction = map.get(iterator.next());
            if (transaction == null)
                continue;

            if (transaction.getDeadline() < dTime) {
                map.delete(transaction);
                continue;
            }

            // LOGGER.error(" time " + transaction.viewTimestamp());

            if (!map.needBroadcasting(transaction, peerByte))
                continue;

            message = MessageFactory.getInstance().createTransactionMessage(transaction);

            try {
                if (peer.sendMessage(message)) {
                    counter++;
                    map.addBroadcastedPeer(transaction, peerByte);
                } else {
                    break;
                }
            } catch (Exception e) {
                if (this.isStopping) {
                    return;
                }
                LOGGER.error(e.getMessage(), e);
            }

            if (counter % stepCount == 0) {

                peer.tryPing(10000);
                this.network.notifyObserveUpdatePeer(peer);

                ping = peer.getPing();
                if (ping < 0 || ping > 500) {

                    stepCount >>= 1;

                    if (ping < 0) {
                        stepCount >>= 1;
                        try {
                            Thread.sleep(10000);
                        } catch (Exception e) {
                        }
                    } else if (ping > 5000) {
                        stepCount >>= 1;
                    }

                    // LOGGER.debug(peer.getAddress() + " stepCount down " +
                    // stepCount);

                } else if (ping < 100) {
                    stepCount <<= 1;
                    // LOGGER.debug(peer.getAddress() + " stepCount UP " +
                    // stepCount + " for PING: " + ping);
                }

            }

        }

        peer.tryPing(10000);
        this.network.notifyObserveUpdatePeer(peer);

        // LOGGER.info(peer.getAddress() + " sended UNCONFIRMED counter: " +
        // counter);

    }

    public void onConnect(Peer peer) {

        if (this.isStopping)
            return;

        // SEND FOUNDMYSELF MESSAGE
        if (!peer.sendMessage(
                MessageFactory.getInstance().createFindMyselfMessage(Controller.getInstance().getFoundMyselfID())))
            return;

        try {
            Thread.sleep(300);
        } catch (Exception e) {
        }

        // SEND VERSION MESSAGE
        if (!peer.sendMessage(
                MessageFactory.getInstance().createVersionMessage(Controller.getVersion(), getBuildTimestamp())))
            return;

        // CHECK GENESIS BLOCK on CONNECT
        Message mess = MessageFactory.getInstance()
                .createGetHeadersMessage(this.blockChain.getGenesisBlock().getSignature());
        SignaturesMessage response = (SignaturesMessage) peer.getResponse(mess, 20000);
        if (response == null)
            ; // MAY BE IT HARD BUSY
        else if (response.getSignatures().isEmpty()) {
            // NO
            this.network.tryDisconnect(peer, Synchronizer.BAN_BLOCK_TIMES << 2, "wrong GENESIS BLOCK");
            return;
        }

        /*
         * // SEND GENESIS BLOCK MESSAGE
         * ////peer.sendMessage(MessageFactory.getInstance().
         * createGetBlockMessage(genesisBlockSign)); //SEND MESSAGE TO PEER
         * Message mess =
         * MessageFactory.getInstance().createGetBlockMessage(genesisBlockSign);
         * BlockMessage response = (BlockMessage) peer.getResponse(mess);
         *
         * //CHECK IF WE GOT RESPONSE if(response == null) { //ERROR //error =
         * true; return; // WRONG GENESIS BLOCK }
         *
         * Block block = response.getBlock(); //CHECK BLOCK SIGNATURE if(block
         * == null || !(block instanceof GenesisBlock)) { //error = true;
         * return; // WRONG GENESIS BLOCK }
         *
         * // TODO CHECK GENESIS BLOCK on CONNECT Message mess =
         * MessageFactory.getInstance().createGetHeadersMessage(genesisBlockSign
         * ); GetSignaturesMessage response = (GetSignaturesMessage)
         * peer.getResponse(mess);
         *
         * //CHECK IF WE GOT RESPONSE if(response == null) { //ERROR //error =
         * true; return; // WRONG GENESIS BLOCK }
         *
         * byte[] header = response.getParent(); if (header == null) { return;
         * // WRONG GENESIS BLOCK }
         */

        // GET CURRENT WIN BLOCK
        Block winBlock = this.blockChain.getWaitWinBuffer();
        if (winBlock != null) {
            // SEND MESSAGE
            if (!peer.sendMessage(MessageFactory.getInstance().createWinBlockMessage(winBlock)))
                return;
        }

        if (this.isStopping)
            return;

        this.actionAfterConnect();

        if (this.status == STATUS_NO_CONNECTIONS) {
            // UPDATE STATUS
            this.status = STATUS_SYNCHRONIZING;

            // NOTIFY
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));

        }

        // BROADCAST UNCONFIRMED TRANSACTIONS to PEER
        this.broadcastUnconfirmedToPeer(peer);

        /*
         * // GET HEIGHT Tuple2<Integer, Long> HWeight =
         * this.blockChain.getHWeightFull(dcSet); // SEND HEIGHT MESSAGE if
         * (!peer.sendMessage(MessageFactory.getInstance().createHWeightMessage(
         * HWeight))) return;
         *
         * //peer.setNeedPing(); peer.tryPing(30000);
         * this.network.notifyObserveUpdatePeer(peer);
         */

    }

    public void actionAfterConnect() {

        ///////// UNCONFIRMED MAP CLEAR
        this.timerUnconfirmed.cancel();
        this.timerUnconfirmed = new Timer();

        TimerTask actionUnconfirmed = new TimerTask() {
            @Override
            public void run() {

                // LOGGER.debug("timerUnconfirmed ---------------- ");
                Controller.getInstance().clearUnconfirmedRecords();

            }
        };

        this.timerUnconfirmed.schedule(actionUnconfirmed, BlockChain.GENERATING_MIN_BLOCK_TIME_MS << 1);

        if (// BlockChain.HARD_WORK ||
                !this.doesWalletExists() || !this.useGui)
            return;

        this.timer.cancel();
        this.timer = new Timer();

        TimerTask action = new TimerTask() {
            @Override
            public void run() {

                // LOGGER.error("actionAfterConnect --->>>>>> ");

                if (Controller.getInstance().getStatus() == STATUS_OK) {
                    // Controller.getInstance().statusInfo();

                    Controller.getInstance().setToOfflineTime(0L);

                    if ((Controller.getInstance().isNeedSyncWallet() || checkNeedSyncWallet())
                            && !Controller.getInstance().isProcessingWalletSynchronize()) {
                        // LOGGER.error("actionAfterConnect --->>>>>>
                        // synchronizeWallet");
                        Controller.getInstance().synchronizeWallet();
                    }
                }
            }
        };

        this.timer.schedule(action, BlockChain.GENERATING_MIN_BLOCK_TIME_MS >> 1);

    }

    public void forgingStatusChanged(ForgingStatus status) {
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.FORGING_STATUS, status));
    }

    // used from NETWORK
    public void afterDisconnect(Peer peer) {
        synchronized (this.peerHWeight) {

            this.peerHWeight.remove(peer);

            this.peersVersions.remove(peer);

            if (this.peerHWeight.isEmpty()) {

                if (this.getToOfflineTime() == 0L) {
                    // SET START OFFLINE TIME
                    this.setToOfflineTime(NTP.getTime());
                }

                // UPDATE STATUS
                if (isTestNet())
                    this.status = STATUS_OK;
                else
                    this.status = STATUS_NO_CONNECTIONS;

                // NOTIFY
                this.setChanged();
                this.notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));
            }
        }
    }

    public List<byte[]> getNextHeaders(byte[] signature) {
        return this.blockChain.getSignatures(dcSet, signature);
    }

    public void setOrphanTo(int height) {
        this.blockChain.clearWaitWinBuffer();
        this.blockGenerator.setOrphanTo(height);
    }

    public void clearWaitWinBufferProcessed() {
        waitWinBufferProcessed = new HashSet<byte[]>();
    }

    // SYNCHRONIZED DO NOT PROCESSS MESSAGES SIMULTANEOUSLY
    public void onMessage(Message message) {
        Message response;
        Block newBlock;

        if (this.isStopping)
            return;

        //// synchronized (this) {
        if (true) {
            switch (message.getType()) {

                /*
                 * case Message.HEIGHT_TYPE:
                 *
                 * HeightMessage heightMessage = (HeightMessage) message;
                 *
                 * // ADD TO LIST synchronized (this.peerHWeight) {
                 * this.peerHWeight.put(heightMessage.getSender(),
                 * heightMessage.getHeight()); }
                 *
                 * break;
                 */

                case Message.HWEIGHT_TYPE:

                    HWeightMessage hWeightMessage = (HWeightMessage) message;

                    // TEST TIMESTAMP of PEER
                    Tuple2<Integer, Long> hW = hWeightMessage.getHWeight();
                    if (this.getBlockChain().getTimestamp(hW.a) - 2 * BlockChain.GENERATING_MIN_BLOCK_TIME_MS > NTP
                            .getTime()) {
                        // IT PEER from FUTURE
                        this.banPeerOnError(hWeightMessage.getSender(), "peer from FUTURE");
                        return;
                    }

                    // ADD TO LIST
                    synchronized (this.peerHWeight) {
                        this.peerHWeight.put(hWeightMessage.getSender(), hWeightMessage.getHWeight());
                    }

                    // this.checkStatusAndObserve(0);

                    break;

                case Message.GET_SIGNATURES_TYPE:

                    GetSignaturesMessage getHeadersMessage = (GetSignaturesMessage) message;

                    // ASK SIGNATURES FROM BLOCKCHAIN
                    long time1 = System.currentTimeMillis();
                    List<byte[]> headers = getNextHeaders(getHeadersMessage.getParent());
                    LOGGER.debug(message.getSender().getAddress() + " getNextHeaders time: "
                            + (System.currentTimeMillis() - time1) + " for headers: " + headers.size()
                            + " from Height: " + (headers.isEmpty() ? "-1"
                            : this.blockChain.getBlock(dcSet, headers.get(0)) == null ? "CHECK"
                            : this.blockChain.getBlock(dcSet, headers.get(0)).getHeight(dcSet)));

                    /*
                     * LOGGER.error(message.getId() +
                     * " controller.Controller.onMessage(Message).GET_SIGNATURES_TYPE ->"
                     * + Base58.encode(getHeadersMessage.getParent()));
                     *
                     * if (!headers.isEmpty()) {
                     * LOGGER.error("this.blockChain.getSignatures.get(0) -> " +
                     * Base58.encode( headers.get(0) )); LOGGER.
                     * error("this.blockChain.getSignatures.get(headers.size()-1) -> "
                     * + Base58.encode( headers.get(headers.size()-1) )); } else
                     * { LOGGER.
                     * error("controller.Controller.onMessage(Message).GET_SIGNATURES_TYPE -> NOT FOUND!"
                     * ); }
                     */

                    // CREATE RESPONSE WITH SAME ID
                    response = MessageFactory.getInstance().createHeadersMessage(headers);
                    response.setId(message.getId());

                    // SEND RESPONSE BACK WITH SAME ID
                    message.getSender().sendMessage(response);

                    break;

                case Message.GET_BLOCK_TYPE:

                    GetBlockMessage getBlockMessage = (GetBlockMessage) message;

                    /*
                     * LOGGER.
                     * error("controller.Controller.onMessage(Message).GET_BLOCK_TYPE ->.getSignature()"
                     * + " form PEER: " + getBlockMessage.getSender().toString()
                     * + " sign: " +
                     * Base58.encode(getBlockMessage.getSignature()));
                     */

                    // ASK BLOCK FROM BLOCKCHAIN
                    newBlock = this.blockChain.getBlock(dcSet, getBlockMessage.getSignature());

                    // CREATE RESPONSE WITH SAME ID
                    response = MessageFactory.getInstance().createBlockMessage(newBlock);
                    response.setId(message.getId());

                    // SEND RESPONSE BACK WITH SAME ID
                    message.getSender().sendMessage(response);

                    if (newBlock == null) {
                        String mess = "Block NOT FOUND for sign:" + getBlockMessage.getSignature();
                        banPeerOnError(message.getSender(), mess);
                    }

                    break;

                case Message.WIN_BLOCK_TYPE:

                    if (this.status != STATUS_OK) {
                        break;
                    }

                    BlockWinMessage blockWinMessage = (BlockWinMessage) message;

                    // ASK BLOCK FROM BLOCKCHAIN
                    newBlock = blockWinMessage.getBlock();

                    // if already it block in process
                    byte[] key = newBlock.getSignature();

                    synchronized (this.waitWinBufferProcessed) {
                        if (!waitWinBufferProcessed.add(key)
                                || Arrays.equals(dcSet.getBlockMap().getLastBlockSignature(), key))
                            return;
                    }

                    info = " received new WIN Block from " + blockWinMessage.getSender().getAddress() + " "
                            + newBlock.toString(dcSet);
                    LOGGER.debug(info);

                    if (this.status == STATUS_SYNCHRONIZING) {
                        // SET for FUTURE without CHECK
                        blockChain.clearWaitWinBuffer();
                        blockChain.setWaitWinBuffer(dcSet, newBlock);
                        break;
                    }

                    int isNewWinBlockValid = this.blockChain.isNewBlockValid(dcSet, newBlock, message.getSender());
                    if (isNewWinBlockValid < 0) {
                        if (isNewWinBlockValid < -10) {
                            info = "newBlock (" + newBlock.toString(dcSet) + ") is Invalid";
                            banPeerOnError(message.getSender(), info);
                            return;
                        } else if (isNewWinBlockValid == -4) {
                            // reference to PARENT last block >>> weak...
                            // already - BROADCASTED to him
                            return;
                        } else {
                            /// NOT BAN - WATCH it
                            if (this.network.getActivePeersCounter(false) > Settings.getInstance().getMaxConnections()
                                    - 3) {
                                this.network.tryDisconnect(message.getSender(), 0, "");
                            }
                            return;
                        }
                    }

                    if (isNewWinBlockValid == 3) {
                        return;
                    } else if (isNewWinBlockValid == 0) {
                        // CHECK IF VALID
                        if (!newBlock.isValid(dcSet, false)) {
                            info = "Block (" + newBlock.toString(dcSet) + ") is Invalid";
                            banPeerOnError(message.getSender(), info);
                            return;
                        }

                        ///// newBlock.calcWinValue(dcSet);

                        if (blockChain.setWaitWinBuffer(dcSet, newBlock)) {
                            // IF IT WIN
                            /*
                             * LOGGER.info(Lang.getInstance().
                             * translate("received new valid WIN Block") +
                             * " for Height: " + this.getMyHeight());
                             */

                            // BROADCAST
                            List<Peer> excludes = new ArrayList<Peer>();
                            excludes.add(message.getSender());
                            this.network.asyncBroadcast(message, excludes, false);
                        } else {
                            // SEND IF my WIN BLOCK as RESPONCE
                            if (blockChain.compareNewWin(dcSet, newBlock) < 0) {
                                Message messageBestWin = MessageFactory.getInstance()
                                        .createWinBlockMessage(blockChain.getWaitWinBuffer());
                                message.getSender().sendMessage(messageBestWin);
                            }
                        }
                        return;

                    } else if (false // при больших нагрузках увеличивает развал
                            // сети
                            && isNewWinBlockValid == 4) {
                        // NEW BLOCK is CONURENT for last BLOCK - try WIN it
                        // STOP FORGING
                        LOGGER.debug("   ++ block CONCURENT to LAST BLOCK in CHAIN ++");
                        ForgingStatus tempStatus = this.blockGenerator.getForgingStatus();
                        this.blockGenerator.setForgingStatus(ForgingStatus.FORGING_WAIT);
                        try {
                            Block lastBlock = this.getLastBlock();
                            this.blockChain.clearWaitWinBuffer();
                            this.synchronizer.pipeProcessOrOrphan(this.dcSet, lastBlock, true, false);
                            if (!newBlock.isValid(dcSet, false)) {
                                info = "Block (" + newBlock.toString(dcSet) + ") is Invalid";
                                banPeerOnError(message.getSender(), info);
                                this.blockGenerator.setForgingStatus(tempStatus);
                                return;
                            }
                            this.synchronizer.pipeProcessOrOrphan(this.dcSet, newBlock, false, false);
                            List<Peer> excludes = new ArrayList<Peer>();
                            excludes.add(message.getSender());
                            this.network.asyncBroadcast(message, excludes, false);
                        } catch (Exception e) {
                            if (this.isOnStopping()) {
                                return;
                            } else {
                                LOGGER.error(e.getMessage(), e);
                            }
                        }
                        // FORGING RESTORE
                        this.blockGenerator.setForgingStatus(tempStatus);
                        return;
                    } else if (false // при больших нагрузках увеличивает развал
                            // сети
                            && isNewWinBlockValid == 5) {
                        // STOP FORGING
                        LOGGER.debug("   ++ block to FUTURE ++");
                        ForgingStatus tempStatus = this.blockGenerator.getForgingStatus();
                        this.blockGenerator.setForgingStatus(ForgingStatus.FORGING_WAIT);
                        try {
                            if (this.flushNewBlockGenerated()) {
                                if (!newBlock.isValid(dcSet, false)) {
                                    info = "Block (" + newBlock.toString(dcSet) + ") is Invalid";
                                    banPeerOnError(message.getSender(), info);
                                    this.blockGenerator.setForgingStatus(tempStatus);
                                    return;
                                }
                                this.blockChain.setWaitWinBuffer(dcSet, newBlock);
                                List<Peer> excludes = new ArrayList<Peer>();
                                excludes.add(message.getSender());
                                this.network.asyncBroadcast(message, excludes, false);
                            }
                        } catch (Exception e) {
                            if (this.isOnStopping()) {
                                return;
                            } else {
                                LOGGER.error(e.getMessage(), e);
                            }
                        }
                        // FORGING RESTORE
                        this.blockGenerator.setForgingStatus(tempStatus);
                        return;
                    } else {
                        LOGGER.debug("controller.Controller.onMessage BLOCK_TYPE -> WIN block not valid "
                                + " for Height: " + this.getMyHeight() + ", code: " + isNewWinBlockValid + ", "
                                + newBlock.toString(dcSet));
                    }

                    break;

                case Message.TRANSACTION_TYPE:

                    TransactionMessage transactionMessage = (TransactionMessage) message;

                    // GET TRANSACTION
                    Transaction transaction = transactionMessage.getTransaction();

                    // CHECK IF SIGNATURE IS VALID OR GENESIS TRANSACTION
                    if (transaction.getCreator() != null & !transaction.isSignatureValid(DCSet.getInstance())) {
                        // DISHONEST PEER
                        banPeerOnError(message.getSender(), "invalid transaction signature");

                        return;
                    }

                    // AND UNCONFIRMED
                    // TODO fee
                    // transaction.calcFee();
                    byte[] signature = transaction.getSignature();
                    if (this.dcSet.getTransactionMap().contains(signature)
                            || this.dcSet.getTransactionFinalMapSigns().contains(signature) || this.isStopping)
                        return;

                    // ADD TO UNCONFIRMED TRANSACTIONS
                    this.dcSet.getTransactionMap().add(transaction);

                    // BROADCAST
                    List<Peer> excludes = new ArrayList<Peer>();
                    excludes.add(message.getSender());
                    this.network.broadcast(message, excludes, false);

                    return;

                case Message.VERSION_TYPE:

                    VersionMessage versionMessage = (VersionMessage) message;

                    // ADD TO LIST
                    synchronized (this.peersVersions) {
                        this.peersVersions.put(versionMessage.getSender(), new Pair<String, Long>(
                                versionMessage.getStrVersion(), versionMessage.getBuildDateTime()));
                    }

                    break;

                default:

                    Tuple2<Integer, Long> HWeight = Controller.getInstance().getBlockChain().getHWeightFull(dcSet);
                    if (HWeight == null)
                        HWeight = new Tuple2<Integer, Long>(-1, -1L);

                    // TODO - for OLD versions
                    // CREATE PING
                    response = MessageFactory.getInstance().createHWeightMessage(HWeight);
                    // CREATE RESPONSE WITH SAME ID
                    response.setId(message.getId());

                    // SEND BACK TO SENDER
                    boolean result11 = message.getSender().sendMessage(response);
                    if (!result11) {
                        LOGGER.debug("error on response GET_HWEIGHT_TYPE to " + message.getSender().getAddress());
                    }

            }
        }
    }

    public void banPeerOnError(Peer peer, String mess) {
        if (Settings.getInstance().getMaxConnections() - this.network.getActivePeersCounter(false) < 1) {
            // BAN if ALL connection USED
            this.network.tryDisconnect(peer, Synchronizer.BAN_BLOCK_TIMES >> 2, "ban PeerOnError - " + mess);
        }
    }

    public void addActivePeersObserver(Observer o) {
        this.network.addObserver(o);
    }

    public void removeActivePeersObserver(Observer o) {
        this.network.deleteObserver(o);
    }

    public void broadcastWinBlock(Block newBlock, List<Peer> excludes) {

        LOGGER.info("broadcast winBlock " + newBlock.toString(this.dcSet) + " size:" + newBlock.getTransactionCount());

        // CREATE MESSAGE
        Message message = MessageFactory.getInstance().createWinBlockMessage(newBlock);

        if (this.isOnStopping())
            return;

        // BROADCAST MESSAGE
        this.network.asyncBroadcastWinBlock(message, excludes, false);

        LOGGER.info("broadcasted!");

    }

    public void broadcastHWeightFull(List<Peer> excludes) {

        // LOGGER.info("broadcast winBlock " + newBlock.toString(this.dcSet));

        // CREATE MESSAGE
        // GET HEIGHT
        Tuple2<Integer, Long> HWeight = this.blockChain.getHWeightFull(dcSet);
        if (HWeight == null)
            return;

        Message messageHW = MessageFactory.getInstance().createHWeightMessage(HWeight);

        // BROADCAST MESSAGE
        this.network.asyncBroadcast(messageHW, excludes, false);

    }

    public void broadcastTransaction(Transaction transaction) {

        // CREATE MESSAGE
        Message message = MessageFactory.getInstance().createTransactionMessage(transaction);

        // BROADCAST MESSAGE
        List<Peer> excludes = new ArrayList<Peer>();
        this.network.broadcast(message, excludes, false);
    }

    public void broadcastTelegram(Transaction transaction, boolean store) {

        // CREATE MESSAGE
        Message telegram = MessageFactory.getInstance().createTelegramMessage(transaction);

        if (store) {
            this.network.addTelegram((TelegramMessage) telegram);
        }

        // BROADCAST MESSAGE
        List<Peer> excludes = new ArrayList<Peer>();
        this.network.asyncBroadcast(telegram, excludes, false);
    }

    // SYNCHRONIZE

    public void orphanInPipe(Block block) throws Exception {
        this.synchronizer.pipeProcessOrOrphan(this.dcSet, block, true, true);
    }

    public boolean checkStatus(int shift) {
        if (isTestNet()) {
            this.status = STATUS_OK;
            return true;
        }

        if (this.peerHWeight.isEmpty()) {
            this.status = STATUS_NO_CONNECTIONS;
            return true;
        }

        if (isStopping)
            return true;

        Tuple2<Integer, Long> thisHW = this.blockChain.getHWeightFull(dcSet);
        if (thisHW == null) {
            this.status = STATUS_OK;
            return true;
        }

        // withWinBuffer
        Tuple3<Integer, Long, Peer> maxHW = this.getMaxPeerHWeight(shift);
        if (maxHW.c == null) {
            this.status = STATUS_OK;
            return true;
        }

        if (maxHW.a > thisHW.a + shift) {
            this.status = STATUS_SYNCHRONIZING;
            return false;
            // } else if (maxHW.a < thisHW.a) {
        } else {
            this.status = STATUS_OK;
            return true;
        }

        /*
         * long maxPeerWeight = maxHW.b; long chainWeight = thisHW.b; if
         * (maxPeerWeight > chainWeight) { // SAME last block? int pickTarget =
         * BlockChain.BASE_TARGET >>2; if (true || (maxPeerWeight - chainWeight
         * < pickTarget)) { byte[] lastBlockSignature =
         * dcSet.getBlockMap().getLastBlockSignature();
         *
         * Block maxBlock = null; try { maxBlock =
         * core.Synchronizer.getBlock(lastBlockSignature, maxHW.c, true); }
         * catch (Exception e) { // error on peer - disconnect! this.status =
         * STATUS_SYNCHRONIZING; this.network.tryDisconnect(maxHW.c, 0,
         * "checkStatus - core.Synchronizer.getBlock - " + e.getMessage());
         * return false; } if (maxBlock != null) { // SAME LAST BLOCK
         * //this.blockChain.getHWeight(dcSet, false);
         * dcSet.getBlockSignsMap().setFullWeight(maxPeerWeight); this.status =
         * STATUS_OK; return true; } } }
         * //LOGGER.info("Controller.isUpToDate getMaxPeerHWeight:" +
         * maxPeerWeight + "<=" + chainWeight);
         *
         * boolean result = maxPeerWeight <= chainWeight; if (result) {
         * this.status = STATUS_OK; return true; }
         *
         * this.status = STATUS_SYNCHRONIZING; return false;
         */

    }

    public int checkStatusAndObserve(int shift) {

        int statusOld = this.status;
        checkStatus(shift);
        if (statusOld != this.status) {
            // NOTIFY
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));

        }

        return this.status;
    }

    public boolean isReadyForging() {

        /*
         * if (this.peerHWeight.isEmpty()) { return false; }
         *
         * if (true) { int maxPeerHeight = this.getMaxPeerHWeight().a; int
         * chainHeight = this.blockChain.getHWeight(dcSet, false).a; int diff =
         * chainHeight - maxPeerHeight; return diff >= 0; } else { long
         * maxPeerWeight = this.getMaxPeerHWeight().b; long chainWeight =
         * this.blockChain.getHWeight(dcSet, false).b; long diff = chainWeight -
         * maxPeerWeight; return diff >= 0 && diff < 999; }
         */

        return true;
    }

    // https://127.0.0.1/7pay_in/tools/block_proc/ERA
    public void NotifyIncoming(List<Transaction> transactions) {

        List<Account> accounts = this.wallet.getAccounts();
        List<Integer> seqs = new ArrayList<Integer>();

        int seq = 0;
        for (Transaction transaction : transactions) {
            // FOR ALL ACCOUNTS
            synchronized (accounts) {
                for (Account account : accounts) {
                    // CHECK IF INVOLVED
                    if (!account.equals(transaction.getCreator()) && transaction.isInvolved(account)) {
                        seqs.add(++seq);
                        break;
                    }
                }
            }
        }

        if (seqs.isEmpty())
            return;

        // SEE -
        // http://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
        String url_string = Settings.getInstance().getNotifyIncomingURL();
        try {

            // CREATE CONNECTION
            URL url = new URL(url_string);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // EXECUTE
            int res = connection.getResponseCode();
        } catch (Exception e) {
            // return -1;
        }

    }

    public boolean isNSUpToDate() {
        return !Settings.getInstance().updateNameStorage();
    }

    public void update(int shift) {
        // UPDATE STATUS

        if (this.status == STATUS_NO_CONNECTIONS) {
            return;
        }

        /// this.status = STATUS_SYNCHRONIZING;

        // DBSet dcSet = DBSet.getInstance();

        Peer peer = null;
        // Block lastBlock = getLastBlock();
        // int lastTrueBlockHeight = this.getMyHeight() -
        // Settings.BLOCK_MAX_SIGNATURES;
        int checkPointHeight = BlockChain.getCheckPoint(dcSet);

        boolean isUpToDate;
        // WHILE NOT UPTODATE
        do {

            // NOTIFY
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));

            // START UPDATE FROM HIGHEST HEIGHT PEER
            // withWinBuffer = true
            Tuple3<Integer, Long, Peer> peerHW = this.getMaxPeerHWeight(shift);
            if (peerHW != null) {
                peer = peerHW.c;
                if (peer != null) {
                    info = "update from MaxHeightPeer:" + peer.getAddress().getHostAddress() + " WH: "
                            + getHWeightOfPeer(peer);
                    LOGGER.info(info);
                    if (Controller.useGui && about_frame.isVisible())
                        about_frame.set_console_Text(info);
                    try {
                        // SYNCHRONIZE FROM PEER
                        if (!this.isOnStopping())
                            this.synchronizer.synchronize(dcSet, checkPointHeight, peer, peerHW.a);
                        if (this.isOnStopping())
                            return;
                    } catch (Exception e) {
                        if (this.isOnStopping()) {
                            // on closing this.dcSet.rollback();
                            return;
                        } else if (peer.isBanned()) {
                            ;
                        } else {
                            LOGGER.error(e.getMessage(), e);
                            return;
                        }
                    }
                }

                blockchainSyncStatusUpdate(getMyHeight());
            }

            isUpToDate = checkStatus(shift);
            this.checkStatusAndObserve(shift);

        } while (!this.isStopping && !isUpToDate);

        if (this.peerHWeight.isEmpty() || peer == null) {
            // UPDATE STATUS
            this.status = STATUS_NO_CONNECTIONS;
            // } else if (!this.isUpToDate()) {
            ////// this.s/tatus = STATUS_SYNCHRONIZING;
            // UPDATE RENEW
            /// update();
        } else {
            this.status = STATUS_OK;
            this.pingAllPeers(false);
        }

        // send to ALL my HW
        //// broadcastHWeight(null);
        if (this.isStopping)
            return;

        // NOTIFY
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));

        this.statusInfo();

    }

    /*
     * private Peer getMaxWeightPeer() { Peer highestPeer = null;
     *
     * // NOT USE GenesisBlocks long weight = BlockChain.BASE_TARGET +
     * BlockChain.BASE_TARGET>>1;
     *
     * try { synchronized (this.peerHWeight) { for (Peer peer :
     * this.peerHWeight.keySet()) { if (highestPeer == null && peer != null) {
     * highestPeer = peer; } else { // IF HEIGHT IS BIGGER if (weight <
     * this.peerHWeight.get(peer).b) { highestPeer = peer; weight =
     * this.peerHWeight.get(peer).b; } else if (weight ==
     * this.peerHWeight.get(peer).b) { // IF HEIGHT IS SAME // CHECK IF PING OF
     * PEER IS BETTER if (peer.getPing() < highestPeer.getPing()) { highestPeer
     * = peer; } } } } } } catch (Exception e) { // PEER REMOVED WHILE ITERATING
     * }
     *
     * return highestPeer; }
     */

    public Tuple3<Integer, Long, Peer> getMaxPeerHWeight(int shift) {

        if (this.isStopping || this.dcSet.isStoped())
            return null;

        Tuple2<Integer, Long> myHWeight = this.getBlockChain().getHWeightFull(dcSet);
        int height = myHWeight.a + shift;
        long weight = myHWeight.b;
        Peer maxPeer = null;

        try {
            synchronized (this.peerHWeight) {
                for (Peer peer : this.peerHWeight.keySet()) {
                    Tuple2<Integer, Long> whPeer = this.peerHWeight.get(peer);
                    if (height < whPeer.a || (maxPeer != null && height == whPeer.a && weight < whPeer.b)) {
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
        Tuple2<Integer, Long> hWeightMy = this.blockChain.getHWeightFull(dcSet);
        if (peerHeight > hWeightMy.a) {
            hWeightMy = new Tuple2<Integer, Long>(peerHeight, hWeightMy.b + 10000l);
        } else {
            hWeightMy = new Tuple2<Integer, Long>(peerHeight, hWeightMy.b - 10000l);
        }
        this.peerHWeight.put(peer, hWeightMy);
        //// blockchainSyncStatusUpdate(this.getMyHeight());
    }

    // WALLET

    public boolean doesWalletExists() {
        // CHECK IF WALLET EXISTS
        return this.wallet != null && this.wallet.exists();
    }

    public boolean doesWalletDatabaseExists() {
        return wallet != null && this.wallet.isWalletDatabaseExisting();
    }

    // use license KEY
    public boolean createWallet(long licenseKey, byte[] seed, String password, int amount, String path) {
        // IF NEW WALLET CREADED
        if (this.wallet.create(seed, password, amount, false, path)) {
            this.setWalletLicense(licenseKey);
            return true;
        } else
            return false;
    }

    public boolean recoverWallet(byte[] seed, String password, int amount, String path) {
        if (this.wallet.create(seed, password, amount, false, path)) {
            LOGGER.info("Wallet needs to synchronize!");
            this.actionAfterConnect();
            this.setNeedSyncWallet(true);

            return true;
        } else
            return false;
    }

    public long getWalletLicense() {
        if (this.doesWalletExists()) {
            return this.wallet.getLicenseKey();
        } else {
            return 2l;
        }
    }

    public void setWalletLicense(long key) {
        if (this.doesWalletExists()) {
            this.wallet.setLicenseKey(key);
        }
    }

    public List<Account> getAccounts() {

        return this.wallet.getAccounts();
    }

    public boolean isAddressIsMine(String address) {
        if (!this.doesWalletExists())
            return false;

        List<Account> accounts = this.wallet.getAccounts();
        for (Account account : accounts) {
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

    public String generateNewAccountWithSynch() {
        String ss = this.wallet.generateNewAccount();
        this.wallet.synchronize(true);
        return ss;
    }

    public PrivateKeyAccount getPrivateKeyAccountByAddress(String address) {
        if (this.doesWalletExists()) {
            return this.wallet.getPrivateKeyAccount(address);
        } else {
            return null;
        }
    }

    public byte[] decrypt(PublicKeyAccount creator, Account recipient, byte[] data) {

        Account account = this.getAccountByAddress(creator.getAddress());

        byte[] privateKey = null;
        byte[] publicKey = null;

        // IF SENDER ANOTHER
        if (account == null) {
            PrivateKeyAccount accountRecipient = this.getPrivateKeyAccountByAddress(recipient.getAddress());
            privateKey = accountRecipient.getPrivateKey();

            publicKey = creator.getPublicKey();
        }
        // IF SENDER ME
        else {
            PrivateKeyAccount accountRecipient = this.getPrivateKeyAccountByAddress(account.getAddress());
            privateKey = accountRecipient.getPrivateKey();

            publicKey = this.getPublicKeyByAddress(recipient.getAddress());
        }

        try {
            return AEScrypto.dataDecrypt(data, privateKey, publicKey);
        } catch (InvalidCipherTextException e1) {
            return null;
        }

    }

    /**
     * Get account in wallet by address
     *
     * @param address is a address in wallet
     * @return object Account
     */
    public Account getAccountByAddress(String address) {
        if (this.doesWalletExists()) {
            return this.wallet.getAccount(address);
        } else {
            return null;
        }
    }

    public boolean isMyAccountByAddress(String address) {
        if (this.doesWalletExists()) {
            return this.wallet.accountExists(address);
        }
        return false;
    }

    // public BigDecimal getUnconfirmedBalance(String address, long key) {
    // return this.wallet.getUnconfirmedBalance(address, key);
    // }
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
        this.wallet.synchronize(false);
    }

    public void clearUnconfirmedRecords() {
        this.blockChain.clearUnconfirmedRecords(this, this.dcSet);
    }

    /**
     * Check if wallet is unlocked
     *
     * @return bool value wallet unlock or not
     */
    public boolean isWalletUnlocked() {
        return this.wallet.isUnlocked();
    }

    public int checkAPICallAllowed(String json, HttpServletRequest request) throws Exception {
        int result = 0;

        if (request != null) {
            Enumeration<String> headers = request.getHeaders(ApiClient.APICALLKEY);
            String uuid = null;
            if (headers.hasMoreElements()) {
                uuid = headers.nextElement();
                if (ApiClient.isAllowedDebugWindowCall(uuid)) {

                    //Gui.getInstance().bringtoFront();

                    return ApiClient.SELF_CALL;
                }
            }
        }

        if (!GraphicsEnvironment.isHeadless()
                && (Settings.getInstance().isGuiEnabled() || Settings.getInstance().isSysTrayEnabled())) {
            SysTray.getInstance().sendMessage(Lang.getInstance().translate("INCOMING API CALL"),
                    Lang.getInstance().translate("An API call needs authorization!"), MessageType.WARNING);
            Object[] options = {Lang.getInstance().translate("Yes"), Lang.getInstance().translate("No")};

            StringBuilder sb = new StringBuilder(Lang.getInstance().translate("Permission Request: "));
            sb.append(Lang.getInstance().translate("Do you want to authorize the following API call?\n\n") + json
                    + " \n" + request.getRequestURL());
            JTextArea jta = new JTextArea(sb.toString());
            jta.setLineWrap(true);
            jta.setEditable(false);
            JScrollPane jsp = new JScrollPane(jta) {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(480, 200);
                }
            };

            Gui gui = Gui.getInstance();
            gui.bringtoFront();

            result = JOptionPane.showOptionDialog(gui, jsp, Lang.getInstance().translate("INCOMING API CALL"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        }

        return result;
    }

    public boolean lockWallet() {
        return this.wallet.lock();
    }

    public boolean unlockWallet(String password) {
        return this.wallet.unlock(password);
    }

    public boolean unlockOnceWallet(String password) {
        return this.wallet.unlockOnce(password);
    }

    public void setSecondsToUnlock(int seconds) {
        this.wallet.setSecondsToUnlock(seconds);
    }

    public List<Pair<Account, Transaction>> getLastTransactions(int limit) {
        return this.wallet.getLastTransactions(limit);
    }

    public Transaction getTransaction(byte[] signature) {

        return getTransaction(signature, this.dcSet);
    }

    // by account addres + timestamp get signature
    public byte[] getSignatureByAddrTime(DCSet dcSet, String address, Long timestamp) {

        return dcSet.getAddressTime_SignatureMap().get(address, timestamp);
    }

    public Transaction getTransaction(byte[] signature, DCSet database) {

        // CHECK IF IN BLOCK
        Tuple2<Integer, Integer> tuple_Tx = database.getTransactionFinalMapSigns().get(signature);
        if (tuple_Tx != null) {
            return database.getTransactionFinalMap().get(tuple_Tx);
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

    public List<Pair<Account, Block>> getLastBlocks(int limit) {
        return this.wallet.getLastBlocks(limit);
    }

    public List<Block> getLastBlocks(Account account, int limit) {
        return this.wallet.getLastBlocks(account, limit);
    }

    public List<TelegramMessage> getLastTelegrams(Account account, long timestamp, String filter) {
        return this.network.getTelegramsForAddress(account.getAddress(), timestamp, filter);
    }

    public List<TelegramMessage> deleteTelegram(List<TelegramMessage> SignTelegram) {
        return this.network.deleteTelegram(SignTelegram);
    }

    public List<TelegramMessage> getLastTelegrams(String address, long timestamp, String filter) {
        return this.network.getTelegramsForAddress(address, timestamp, filter);
    }

    public List<TelegramMessage> getLastTelegrams(long timestamp, String filter) {
        return this.network.getTelegramsFromTimestamp(timestamp, filter);
    }

    public TelegramMessage getTelegram(byte[] signature) {
        return this.network.getTelegram(signature);
    }

    /**
     * Get telegram by signature
     *
     * @param signature is a signature
     * @return
     */
    public TelegramMessage getTelegram(String signature) {
        return this.network.getTelegram(signature);
    }
    // public TelegramMessage getTelegram(String signature) {
    // return this.network.getTelegram(signature);
    // }

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
        return this.dcSet.getNameExchangeMap().getNameSales();
    }

    public List<Pair<Account, Poll>> getPolls() {
        return this.wallet.getPolls();
    }

    public List<Poll> getPolls(Account account) {
        return this.wallet.getPolls(account);
    }

    /*
     * public void addAssetFavorite(AssetCls asset) {
     * this.wallet.addAssetFavorite(asset); }
     */
    public void addItemFavorite(ItemCls item) {
        this.wallet.addItemFavorite(item);
    }

    public Item_Map getItemMap(int type) {
        switch (type) {
            case ItemCls.ASSET_TYPE:
                return this.dcSet.getItemAssetMap();
            case ItemCls.IMPRINT_TYPE:
                return this.dcSet.getItemImprintMap();
            case ItemCls.TEMPLATE_TYPE:
                return this.dcSet.getItemTemplateMap();
            case ItemCls.PERSON_TYPE:
                return this.dcSet.getItemPersonMap();
        }
        return null;
    }

    public void removeItemFavorite(ItemCls item) {
        this.wallet.removeItemFavorite(item);
    }

    public boolean isItemFavorite(ItemCls item) {
        return this.wallet.isItemFavorite(item);
    }

    public Collection<Poll> getAllPolls() {
        return this.dcSet.getPollMap().getValuesAll();
    }

    public Collection<ItemCls> getAllItems(int type) {
        return getItemMap(type).getValuesAll();
    }

    public void onDatabaseCommit() {
        this.wallet.commit();
    }

    public void startBlockGenerator() {
        this.blockGenerator.start();
    }

    public ForgingStatus getForgingStatus() {
        return this.blockGenerator.getForgingStatus();
    }

    public void setForgingStatus(ForgingStatus status) {
        this.blockGenerator.setForgingStatus(status);
    }

    // BLOCKCHAIN

    public BlockChain getBlockChain() {
        return this.blockChain;
    }

    public int getMyHeight() {
        // need for TESTs
        if (this.isOnStopping())
            return -1;

        return dcSet.getBlocksHeadsMap().size();
    }

    public Block getLastBlock() {
        Block block = this.blockChain.getLastBlock(dcSet);
        if (block == null)
            return this.blockChain.getGenesisBlock();
        return block;
    }

    public byte[] getWalletLastBlockSign() {
        return this.wallet.getLastBlockSignature();
    }

    public Block getBlock(byte[] header) {
        return this.blockChain.getBlock(dcSet, header);
    }

    public Pair<Block, List<Transaction>> scanTransactions(Block block, int blockLimit, int transactionLimit, int type,
                                                           int service, Account account) {
        return this.blockChain.scanTransactions(dcSet, block, blockLimit, transactionLimit, type, service, account);

    }

    public long getNextBlockGeneratingBalance() {
        Block block = this.dcSet.getBlockMap().last();
        return block.getForgingValue();
    }

    // FORGE

    /*
     * public boolean newBlockGenerated(Block newBlock) {
     *
     * Tuple2<Boolean, Block> result = this.blockChain.setWaitWinBuffer(dcSet,
     * newBlock); if ( result.a ) { // need to BROADCAST
     * this.broadcastBlock(result.b); }
     *
     * return result.a; }
     */

    // FLUSH BLOCK from win Buffer - to MAP and NERWORK
    public boolean flushNewBlockGenerated() throws Exception {

        Block newBlock = this.blockChain.popWaitWinBuffer();
        if (newBlock == null)
            return false;

        // if last block is changed by core.Synchronizer.process(DBSet, Block)
        // clear this win block
        if (!Arrays.equals(dcSet.getBlockMap().getLastBlockSignature(), newBlock.getReference())) {
            return false;
        }

        LOGGER.debug("+++ flushNewBlockGenerated TRY flush chainBlock: " + newBlock.toString(this.dcSet));

        boolean isValid = false;
        if (isMyAccountByAddress(newBlock.getCreator().getAddress())) {
            isValid = true; // GENERATE by ME
        } else {
            isValid = newBlock.isSignatureValid() && newBlock.isValid(this.dcSet, false);
            LOGGER.debug("+++ flushNewBlockGenerated Validated records: " + newBlock.getTransactionCount());
        }

        if (!isValid)
            return false;

        try {
            this.synchronizer.pipeProcessOrOrphan(this.dcSet, newBlock, false, true);
            this.clearWaitWinBufferProcessed();

        } catch (Exception e) {
            if (this.isOnStopping()) {
                throw new Exception("on stoping");
            } else {
                LOGGER.error(e.getMessage(), e);
            }
        }

        LOGGER.debug("+++ flushNewBlockGenerated OK");

        /// LOGGER.info("and broadcast it");

        // broadcast my HW
        broadcastHWeightFull(null);

        return true;
    }

    public List<Transaction> getUnconfirmedTransactions(int from, int count, boolean descending) {
        return this.dcSet.getTransactionMap().getTransactions(from, count, descending);

    }

    // BALANCES

    public SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> getBalances(
            long key) {
        return this.dcSet.getAssetBalanceMap().getBalancesSortableList(key);
    }

    public SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> getBalances(
            Account account) {

        return this.dcSet.getAssetBalanceMap().getBalancesSortableList(account);
    }

    // NAMES

    public Name getName(String nameName) {
        return this.dcSet.getNameMap().get(nameName);
    }

    public NameSale getNameSale(String nameName) {
        return this.dcSet.getNameExchangeMap().getNameSale(nameName);
    }

    // POLLS

    public Poll getPoll(String name) {
        return this.dcSet.getPollMap().get(name);
    }

    // ASSETS

    public AssetCls getAsset(long key) {
        return (AssetCls) this.dcSet.getItemAssetMap().get(key);
    }

    public PersonCls getPerson(long key) {
        return (PersonCls) this.dcSet.getItemPersonMap().get(key);
    }

    public PollCls getPoll(long key) {
        return (PollCls) this.dcSet.getItemPollMap().get(key);
    }

    public ImprintCls getImprint(long key) {
        return (ImprintCls) this.dcSet.getItemImprintMap().get(key);
    }

    public StatusCls getStatus(long key) {
        return (StatusCls) this.dcSet.getItemStatusMap().get(key);
    }

    public TemplateCls getTemplate(long key) {
        return (TemplateCls) this.dcSet.getItemTemplateMap().get(key);
    }

    /*
     * public SortableList<BigInteger, Order> getOrders(AssetCls have, AssetCls
     * want) { return this.getOrders(have, want, true); }
     */

    public SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrders(
            AssetCls have, AssetCls want) {
        return this.dcSet.getOrderMap().getOrdersSortableList(have.getKey(this.dcSet), want.getKey(this.dcSet));
    }

    public SortableList<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> getTrades(
            AssetCls have, AssetCls want) {
        return this.dcSet.getTradeMap().getTradesSortableList(have.getKey(this.dcSet), want.getKey(this.dcSet));
    }

    public List<Trade> getTradeByTimestmp(long have, long want, long timestamp) {
        return Trade.getTradeByTimestmp(this.dcSet, have, want, timestamp);
    }

    // IMPRINTS
    public ImprintCls getItemImprint(long key) {
        return (ImprintCls) this.dcSet.getItemImprintMap().get(key);
    }

    // TEMPLATES
    public TemplateCls getItemTemplate(long key) {
        return (TemplateCls) this.dcSet.getItemTemplateMap().get(key);
    }

    // PERSONS
    public PersonCls getItemPerson(long key) {
        return (PersonCls) this.dcSet.getItemPersonMap().get(key);
    }

    // STATUSES
    public StatusCls getItemStatus(long key) {
        return (StatusCls) this.dcSet.getItemStatusMap().get(key);
    }

    // UNIONS
    public UnionCls getItemUnion(long key) {
        return (UnionCls) this.dcSet.getItemUnionMap().get(key);
    }

    // ALL ITEMS
    public ItemCls getItem(DCSet db, int type, long key) {

        switch (type) {
            case ItemCls.ASSET_TYPE: {
                return db.getItemAssetMap().get(key);
            }
            case ItemCls.IMPRINT_TYPE: {
                return db.getItemImprintMap().get(key);
            }
            case ItemCls.TEMPLATE_TYPE: {
                return db.getItemTemplateMap().get(key);
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
        return this.getItem(this.dcSet, type, key);
    }

    // ATs

    public SortableList<String, AT> getAcctATs(String type, boolean initiators) {
        return this.dcSet.getATMap().getAcctATs(type, initiators);
    }

    // TRANSACTIONS

    public void onTransactionCreate(Transaction transaction) {
        // ADD TO UNCONFIRMED TRANSACTIONS
        this.dcSet.getTransactionMap().add(transaction);

        // NOTIFY OBSERVERS - AUTO in database.wallet.TransactionMap
        if (false) {
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_LIST_TRANSACTION_TYPE,
                    this.dcSet.getTransactionMap().getValuesAll()));

            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_ADD_TRANSACTION_TYPE, transaction));
        }

        // BROADCAST
        this.broadcastTransaction(transaction);
    }

    public Pair<Transaction, Integer> registerName(PrivateKeyAccount registrant, Account owner, String name,
                                                   String value, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createNameRegistration(registrant, new Name(owner, name, value), feePow);
        }
    }

    public Pair<Transaction, Integer> updateName(PrivateKeyAccount owner, Account newOwner, String name, String value,
                                                 int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createNameUpdate(owner, new Name(newOwner, name, value), feePow);
        }
    }

    public Pair<Transaction, Integer> sellName(PrivateKeyAccount owner, String name, BigDecimal amount, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createNameSale(owner, new NameSale(name, amount), feePow);
        }
    }

    public Pair<Transaction, Integer> cancelSellName(PrivateKeyAccount owner, NameSale nameSale, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createCancelNameSale(owner, nameSale, feePow);
        }
    }

    public Pair<Transaction, Integer> BuyName(PrivateKeyAccount buyer, NameSale nameSale, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createNamePurchase(buyer, nameSale, feePow);
        }
    }

    public Transaction issuePoll(PrivateKeyAccount creator, String name, byte[] icon, byte[] image, String description,
                                 List<String> options, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            // CREATE POLL
            PollCls poll = new core.item.polls.Poll(creator, name, icon, image, description, options);

            return this.transactionCreator.createIssuePollRecord(creator, poll, feePow);
        }
    }

    public Transaction createPoll_old(PrivateKeyAccount creator, String name, String description, List<String> options,
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

            return this.transactionCreator.createPollCreation(creator, poll, feePow);
        }
    }

    public Transaction createItemPollVote(PrivateKeyAccount creator, long pollKey, int optionIndex, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            // GET OPTION INDEX
            // int optionIndex = poll.getOptions().indexOf(option);

            return this.transactionCreator.createItemPollVote(creator, pollKey, optionIndex, feePow);
        }
    }

    public Pair<Transaction, Integer> createPollVote(PrivateKeyAccount creator, Poll poll, PollOption option,
                                                     int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            // GET OPTION INDEX
            int optionIndex = poll.getOptions().indexOf(option);

            return this.transactionCreator.createPollVote(creator, poll.getName(), optionIndex, feePow);
        }
    }

    public Pair<Transaction, Integer> createArbitraryTransaction(PrivateKeyAccount creator, List<Payment> payments,
                                                                 int service, byte[] data, int feePow) {

        if (payments == null) {
            payments = new ArrayList<Payment>();
        }

        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createArbitraryTransaction(creator, payments, service, data, feePow);
        }
    }

    /*
     * public Pair<Transaction, Integer> createTransactionFromRaw( byte[]
     * rawData) {
     *
     * synchronized (this.transactionCreator) { return
     * this.transactionCreator.createTransactionFromRaw(rawData); } }
     */

    public Pair<Transaction, Integer> lightCreateTransactionFromRaw(byte[] rawData) {

        // CREATE TRANSACTION FROM RAW
        Transaction transaction;
        try {
            transaction = TransactionFactory.getInstance().parse(rawData, null);
        } catch (Exception e) {
            return new Pair<Transaction, Integer>(null, Transaction.INVALID_RAW_DATA);
        }

        // CHECK IF RECORD VALID
        if (!transaction.isSignatureValid(DCSet.getInstance()))
            return new Pair<Transaction, Integer>(null, Transaction.INVALID_SIGNATURE);

        // CHECK FOR UPDATES
        int valid = this.transactionCreator.afterCreateRaw(transaction, false, 0l);
        if (valid != Transaction.VALIDATE_OK)
            return new Pair<Transaction, Integer>(null, valid);

        return new Pair<Transaction, Integer>(transaction, valid);

    }

    public Transaction issueAsset(PrivateKeyAccount creator, String name, String description, byte[] icon, byte[] image,
                                  boolean movable, int scale, int asset_type, long quantity, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueAssetTransaction(creator, name, description, icon, image, scale,
                    asset_type, quantity, feePow);
        }
    }

    public Pair<Transaction, Integer> issueImprint(PrivateKeyAccount creator, String name, String description,
                                                   byte[] icon, byte[] image, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueImprintTransaction(creator, name, description, icon, image,
                    feePow);
        }
    }

    public Transaction issueImprint1(PrivateKeyAccount creator, String name, String description, byte[] icon,
                                     byte[] image, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueImprintTransaction1(creator, name, description, icon, image,
                    feePow);
        }
    }

    public Transaction issueTemplate(PrivateKeyAccount creator, String name, String description, byte[] icon,
                                     byte[] image, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueTemplateTransaction(creator, name, description, icon, image,
                    feePow);
        }
    }

    public Pair<Transaction, Integer> issuePerson(boolean forIssue, PrivateKeyAccount creator, String fullName,
                                                  int feePow, long birthday, long deathday, byte gender, String race, float birthLatitude,
                                                  float birthLongitude, String skinColor, String eyeColor, String hairСolor, int height, byte[] icon,
                                                  byte[] image, String description, PublicKeyAccount owner, byte[] ownerSignature) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssuePersonTransaction(forIssue, creator, fullName, feePow, birthday,
                    deathday, gender, race, birthLatitude, birthLongitude, skinColor, eyeColor, hairСolor, height, icon,
                    image, description, owner, ownerSignature);
        }
    }

    public Pair<Transaction, Integer> issuePersonHuman(PrivateKeyAccount creator, int feePow, PersonHuman human) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssuePersonHumanTransaction(creator, feePow, human);
        }
    }

    public Transaction issuePoll(PrivateKeyAccount creator, String name, String description, List<String> options,
                                 byte[] icon, byte[] image, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssuePollTransaction(creator, name, description, icon, image, options,
                    feePow);
        }
    }

    public Transaction issueStatus(PrivateKeyAccount creator, String name, String description, boolean unique,
                                   byte[] icon, byte[] image, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueStatusTransaction(creator, name, description, icon, image, unique,
                    feePow);
        }
    }

    public Transaction issueUnion(PrivateKeyAccount creator, String name, long birthday, long parent,
                                  String description, byte[] icon, byte[] image, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueUnionTransaction(creator, name, birthday, parent, description,
                    icon, image, feePow);
        }
    }

    public Transaction createOrder(PrivateKeyAccount creator, AssetCls have, AssetCls want, BigDecimal amountHave,
                                   BigDecimal amountWant, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createOrderTransaction(creator, have, want, amountHave, amountWant, feePow);
        }
    }

    public Pair<Transaction, Integer> cancelOrder(PrivateKeyAccount creator, Order order, int feePow) {
        return cancelOrder(creator, order.getId(), feePow);
    }

    public Pair<Transaction, Integer> cancelOrder(PrivateKeyAccount creator, BigInteger orderID, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createCancelOrderTransaction(creator, orderID, feePow);
        }
    }

    public Pair<Transaction, Integer> deployAT(PrivateKeyAccount creator, String name, String description, String type,
                                               String tags, byte[] creationBytes, BigDecimal quantity, int feePow) {

        synchronized (this.transactionCreator) {
            return this.transactionCreator.deployATTransaction(creator, name, description, type, tags, creationBytes,
                    quantity, feePow);
        }
    }

    public Pair<Transaction, Integer> sendMultiPayment(PrivateKeyAccount sender, List<Payment> payments, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.sendMultiPayment(sender, payments, feePow);
        }
    }

    public Pair<Integer, Transaction> make_R_Send(String creatorStr, Account creator, String recipientStr,
            String feePowStr, String assetKeyStr, boolean checkAsset, String amountStr, boolean needAmount,
            String title, String message, int messagecode, boolean encrypt) {
        
        Controller cnt = Controller.getInstance();

        // READ CREATOR
        if (creatorStr != null && creator == null) {
            Tuple2<Account, String> resultCreator = Account.tryMakeAccount(creatorStr);
            if (resultCreator.b != null) {
                return new Pair<Integer, Transaction>(Transaction.INVALID_CREATOR, null);
            }
            creator = resultCreator.a;
        }

        // READ RECIPIENT
        Tuple2<Account, String> resultRecipient = Account.tryMakeAccount(recipientStr);
        if (resultRecipient.b != null) {
            return new Pair<Integer, Transaction>(Transaction.INVALID_ADDRESS, null);
        }
        Account recipient = resultRecipient.a;

        // creator != recipient
        if (creator.equals(recipient)) {
            return new Pair<Integer, Transaction>(Transaction.INVALID_RECEIVER, null);
        }

        // READ FEE
        int feePow = 0;
        if (feePowStr != null) {
            try {
                feePow = Integer.parseInt(feePowStr);
            } catch (Exception e) {
                return new Pair<Integer, Transaction>(Transaction.INVALID_FEE_POWER, null);
            }
        }

        // READ AMOUNT
        BigDecimal amount = null;
        if (amountStr != null) {
            try {
                amount = new BigDecimal(amountStr);
            } catch (Exception e) {
                return new Pair<Integer, Transaction>(Transaction.INVALID_AMOUNT, null);
            }

        }

        if (needAmount && (amount == null || amount.signum() == 0)) {
            return new Pair<Integer, Transaction>(Transaction.INVALID_AMOUNT_IS_NULL, null);
        }

        long assetKey = 0;
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) == 0)
                amount = null;
            else {
                // PARSE asset Key
                if (assetKeyStr == null) {
                    assetKey = 2;
                } else {
                    try {
                        assetKey = new Long(assetKeyStr);
                    } catch (Exception e) {
                        return new Pair<Integer, Transaction>(Transaction.INVALID_ITEM_KEY, null);
                    }

                    if (checkAsset) {
                        AssetCls asset;
                        if (assetKey > 0)
                            asset = cnt.getAsset(assetKey);
                        else
                            asset = cnt.getAsset(-assetKey);

                        if (asset == null)
                            return new Pair<Integer, Transaction>(Transaction.ITEM_ASSET_NOT_EXIST, null);
                    }
                }
            }
        }

        if (title == null)
            title = "";
        else if (title.getBytes(StandardCharsets.UTF_8).length > 256) {
            return new Pair<Integer, Transaction>(Transaction.INVALID_TITLE_LENGTH, null);
        }

        byte[] messageBytes = null;

        if (message != null && message.length() > 0) {
            if (messagecode == 0) {
                messageBytes = message.getBytes(Charset.forName("UTF-8"));
            } else {
                try {
                    if (messagecode == 16) {
                        messageBytes = Converter.parseHexString(message);
                    } else if (messagecode == 32) {
                        messageBytes = Base32.decode(message);
                    } else if (messagecode == 58) {
                        messageBytes = Base58.decode(message);
                    } else if (messagecode == 64) {
                        messageBytes = Base64.getDecoder().decode(message);
                    }
                } catch (Exception e) {
                    return new Pair<Integer, Transaction>(Transaction.INVALID_MESSAGE_FORMAT, null);
                }
            }
        }

        // if no TEXT - set null
        if (messageBytes != null && messageBytes.length == 0)
            messageBytes = null;

        PrivateKeyAccount privateKeyAccount = cnt.getPrivateKeyAccountByAddress(creator.getAddress());
        if (privateKeyAccount == null) {
            return new Pair<Integer, Transaction>(Transaction.INVALID_WALLET_ADDRESS, null);
        }
        
        byte[] encrypted = (encrypt) ? new byte[] { 1 } : new byte[] { 0 };
        byte[] isTextByte = (messagecode == 0) ? new byte[] { 1 } : new byte[] { 0 };
        
        if (messageBytes != null) {
            if (messageBytes.length > BlockChain.MAX_REC_DATA_BYTES) {
                return new Pair<Integer, Transaction>(Transaction.INVALID_MESSAGE_LENGTH, null);
            }

            if (encrypt) {
                // sender
                byte[] privateKey = privateKeyAccount.getPrivateKey();

                // recipient
                byte[] publicKey = cnt.getPublicKeyByAddress(recipient.getAddress());
                if (publicKey == null) {
                    return new Pair<Integer, Transaction>(Transaction.UNKNOWN_PUBLIC_KEY_FOR_ENCRYPT, null);
                }

                messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
            }
        }

        // CREATE R_Send
        return new Pair<Integer, Transaction>(Transaction.VALIDATE_OK, this.r_Send(privateKeyAccount, feePow, recipient,
                assetKey, amount, title, messageBytes, isTextByte, encrypted));

    }

    public Transaction r_Send(PrivateKeyAccount sender, int feePow, Account recipient, long key, BigDecimal amount) {
        return this.r_Send(sender, feePow, recipient, key, amount, "", null, null, null);
    }

    public Transaction r_Send(PrivateKeyAccount sender, int feePow,
                              Account recipient, long key, BigDecimal amount, String head, byte[] isText, byte[] message,
                              byte[] encryptMessage) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_Send(sender, recipient, key, amount, feePow, head, message, isText,
                    encryptMessage);
        }
    }

    public Transaction r_Send(byte version, byte property1, byte property2,
                              PrivateKeyAccount sender, int feePow,
                              Account recipient, long key, BigDecimal amount, String head, byte[] isText, byte[] message,
                              byte[] encryptMessage) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_Send(version, property1, property2, sender, recipient, key, amount, feePow,
                    head, message, isText, encryptMessage);
        }
    }

    public Transaction r_SignNote(byte version, byte property1, byte property2, boolean asPack,
                                  PrivateKeyAccount sender, int feePow, long key, byte[] message, byte[] isText, byte[] encrypted) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_SignNote(version, property1, property2, asPack, sender, feePow, key,
                    message, isText, encrypted);
        }
    }

    public Transaction r_SertifyPerson(int version, boolean asPack, PrivateKeyAccount creator, int feePow, long key,
                                       List<PublicKeyAccount> userAccounts, int add_day) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_SertifyPerson(version, asPack, creator, feePow, key, userAccounts,
                    add_day);
        }
    }

    public Transaction r_Vouch(int version, boolean asPack, PrivateKeyAccount creator, int feePow, int height,
                               int seq) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_Vouch(version, asPack, creator, feePow, height, seq);
        }
    }

    public Pair<Transaction, Integer> r_Hashes(PrivateKeyAccount sender, int feePow, String url, String data,
                                               String hashes) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_Hashes(sender, feePow, url, data, hashes);
        }
    }

    public Pair<Transaction, Integer> r_Hashes(PrivateKeyAccount sender, int feePow, String url, String data,
                                               String[] hashes) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_Hashes(sender, feePow, url, data, hashes);
        }
    }

    /*
     * // ver 1 public Pair<Transaction, Integer> r_SetStatusToItem(int version,
     * boolean asPack, PrivateKeyAccount creator, int feePow, long key, ItemCls
     * item, Long beg_date, Long end_date, int value_1, int value_2, byte[]
     * data, long refParent ) { synchronized (this.transactionCreator) { return
     * this.transactionCreator.r_SetStatusToItem( 0, asPack, creator, feePow,
     * key, item, beg_date, end_date, value_1, value_2, data, refParent ); } }
     */
    // ver2
    public Transaction r_SetStatusToItem(int version, boolean asPack, PrivateKeyAccount creator, int feePow, long key,
                                         ItemCls item, Long beg_date, Long end_date, long value_1, long value_2, byte[] data_1, byte[] data_2,
                                         long refParent, byte[] description) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_SetStatusToItem(1, asPack, creator, feePow, key, item, beg_date, end_date,
                    value_1, value_2, data_1, data_2, refParent, description);
        }
    }

    /*
     * public Pair<Transaction, Integer> sendJson(PrivateKeyAccount sender,
     * Account recipient, long key, BigDecimal amount,int feePow, byte[] isText,
     * byte[] message, byte[] encryptMessage) { synchronized
     * (this.transactionCreator) { return
     * this.transactionCreator.createJson(sender, recipient, key, amount,
     * feePow, message, isText, encryptMessage); } } public Pair<Transaction,
     * Integer> sendAccounting(PrivateKeyAccount sender, Account recipient, long
     * key, BigDecimal amount,int feePow, byte[] isText, byte[] message, byte[]
     * encryptMessage) { synchronized (this.transactionCreator) { return
     * this.transactionCreator.createAccounting(sender, recipient, key, amount,
     * feePow, message, isText, encryptMessage); }
     *
     * }
     */

    public Block getBlockByHeight(DCSet db, int parseInt) {
        return db.getBlockMap().getWithMind(parseInt);
    }

    public Block getBlockByHeight(int parseInt) {
        return getBlockByHeight(this.dcSet, parseInt);
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

        // DCSet db = this.dcSet;
        // get last transaction from this address
        byte[] sign = dcSet.getAddressTime_SignatureMap().get(address);
        if (sign == null) {
            return null;
        }

        /*
         * long lastReference = db.getReferenceMap().get(address); byte[] sign =
         * getSignatureByAddrTime(db, address, lastReference); if (sign == null)
         * return null;
         */

        Transaction transaction = cntr.getTransaction(sign);
        if (transaction == null) {
            return null;
        }

        if (transaction.getCreator().equals(address))
            return transaction.getCreator().getPublicKey();
        else {
            List<PublicKeyAccount> pKeys = transaction.getPublicKeys();
            if (pKeys != null) {
                for (PublicKeyAccount pKey : pKeys) {
                    if (pKey.equals(address)) {
                        return pKey.getPublicKey();
                    }
                }
            }
        }

        return null;
    }
}
