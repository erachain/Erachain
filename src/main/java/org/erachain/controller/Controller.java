package org.erachain.controller;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.erachain.api.ApiClient;
import org.erachain.api.ApiErrorFactory;
import org.erachain.api.ApiService;
import org.erachain.at.AT;
import org.erachain.core.*;
import org.erachain.core.BlockGenerator.ForgingStatus;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.crypto.Base32;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.payment.Payment;
import org.erachain.core.telegram.TelegramStore;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.erachain.core.voting.PollOption;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.DLSet;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemMap;
import org.erachain.datachain.TransactionMap;
import org.erachain.datachain.TransactionSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.AboutFrame;
import org.erachain.gui.Gui;
import org.erachain.gui.GuiTimer;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.network.Network;
import org.erachain.network.Peer;
import org.erachain.network.message.*;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.*;
import org.erachain.webserver.Status;
import org.erachain.webserver.WebService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * main class for connection all modules
 */
public class Controller extends Observable {

    public static String version = "5.4.1 demo 03";
    public static String buildTime = "2021-05-05 12:00:00 UTC";

    public static final char DECIMAL_SEPARATOR = '.';
    public static final char GROUPING_SEPARATOR = '`';
    // IF new abilities is made - new license insert in CHAIN and set this KEY
    public static final long LICENSE_VERS = 107; // version of LICENSE
    public static HashMap<String, Long> LICENSE_LANG_REFS;

    public final String APP_NAME;
    public final static long MIN_MEMORY_TAIL = 64 * (1 << 20); // Машина Явы вылетает если меньше 50 МБ

    public static final Integer MUTE_PEER_COUNT = 6;

    // TODO ENUM would be better here
    public static final int STATUS_NO_CONNECTIONS = 0;
    public static final int STATUS_SYNCHRONIZING = 1;
    public static final int STATUS_OK = 2;
    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class.getSimpleName());

    public static int HARD_WORK = 0;
    public static String CACHE_DC = "hard";
    public boolean useGui = true;
    public boolean useNet = true;


    private List<Thread> threads = new ArrayList<Thread>();
    public static long buildTimestamp;
    private static Controller instance;
    public PairsController pairsController;
    private Wallet wallet;
    public TelegramStore telegramStore;
    private int status;
    private boolean dcSetWithObserver = false;
    private boolean dynamicGUI = false;
    public Network network;
    private ApiService rpcService;
    private WebService webService;
    public TransactionsPool transactionsPool;
    public WinBlockSelector winBlockSelector;
    public BlocksRequest blockRequester;
    public BlockChain blockChain;
    private BlockGenerator blockGenerator;
    public Synchronizer synchronizer;
    private TransactionCreator transactionCreator;
    private Timer connectTimer;
    private Random random = new SecureRandom();
    private byte[] foundMyselfID = new byte[128];
    private byte[] messageMagic;
    private long toOfflineTime;
    //private ConcurrentHashMap<Peer, Tuple2<Integer, Long>> peerHWeight = new ConcurrentHashMap<Peer, Tuple2<Integer, Long>>(20, 1);
    //private ConcurrentHashMap<Peer, Integer> peerHWeightMute = new ConcurrentHashMap<Peer, Integer>(20, 1);

    public DLSet dlSet; // = DLSet.getInstance();
    private DCSet dcSet; // = DLSet.getInstance();
    public Gui gui;
    public GuiTimer guiTimer;

    // private JSONObject Setting_Json;

    public AboutFrame about_frame = null;
    private boolean isStopping = false;
    private String info;

    public long unconfigmedMessageTimingAverage;
    public static final int BLOCK_AS_TX_COUNT = 4;
    public long transactionMessageTimingAverage;
    private long transactionMakeTimingAverage;

    public boolean backUP = false;
    public String[] seedCommand;
    public boolean noCalculated;
    public boolean noUseWallet;
    public boolean noDataWallet;
    public boolean onlyProtocolIndexing;
    public boolean compactDConStart;
    public boolean inMemoryDC;

    /**
     * see org.erachain.datachain.DCSet#BLOCKS_MAP
     */
    public int databaseSystem = -1;

    Controller() {
        instance.LICENSE_LANG_REFS = BlockChain.TEST_MODE ?
                new HashMap<String, Long>(3, 1) {
                    {
                        put("en", Transaction.makeDBRef(0, 1));
                        put("ru", Transaction.makeDBRef(0, 1));
                    }
                } :
                new HashMap<String, Long>(3, 1) {
                    {
                        put("en", Transaction.makeDBRef(159719, 1));
                        put("ru", Transaction.makeDBRef(159727, 1));
                    }
                };

        if (Settings.getInstance().isCloneNet()) {
            APP_NAME = "Erachain-" + Settings.getInstance().APP_NAME;
        } else {
            APP_NAME = "Erachain";
        }

    }

    public static String getVersion(boolean withTimestamp) {

        String dbs;
        switch (getInstance().databaseSystem) {
            case DCSet.DBS_ROCK_DB:
                dbs = "R";
                break;
            case DCSet.DBS_MAP_DB:
                dbs = "M";
                break;
            case DCSet.DBS_FAST:
                dbs = "f";
                break;
            default:
                dbs = "M";

        }

        if (withTimestamp)
            return version + (BlockChain.DEMO_MODE ? " DEMO Net"
                    : BlockChain.TEST_MODE ? " Test Net: " + Settings.getInstance().getGenesisStamp()
                    : "")
                    + " (" + dbs + ")";

        return version + " (" + dbs + ")";

    }

    public String getApplicationName(boolean withVersion) {
        return APP_NAME + " " + (withVersion ? getVersion(true) :
                BlockChain.DEMO_MODE ? "DEMO Net" : BlockChain.TEST_MODE ? "Test Net"
                        : "");
    }

    public static String getBuildDateTimeString() {
        return DateTimeFormat.timestamptoString(buildTimestamp, "yyyy-MM-dd HH:mm:ss z", "UTC");
    }

    public static String getBuildDateString() {
        return DateTimeFormat.timestamptoString(buildTimestamp, "yyyy-MM-dd", "UTC");
    }

    public long getBuildTimestamp() {
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
                f = new File(APP_NAME.toLowerCase() + ".jar");
                p = f.toPath();
                attr = Files.readAttributes(p, BasicFileAttributes.class);
            } catch (Exception e1) {
                try {
                    f = new File(APP_NAME.toLowerCase() + ".exe");
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

    public synchronized static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
            instance.setDCSetWithObserver(Settings.getInstance().isGuiEnabled());
            instance.setDynamicGUI(Settings.getInstance().isGuiDynamic());
        }

        return instance;
    }

    public void setDCSetWithObserver(boolean dcSetWithObserver) {
        this.dcSetWithObserver = dcSetWithObserver;
    }

    public void setDynamicGUI(boolean dynamicGUI) {
        this.dynamicGUI = dynamicGUI;
    }

    public boolean isDynamicGUI() {
        return this.dynamicGUI;
    }

    public void setDCSet(DCSet db) {
        this.dcSet = db;
    }

    public DLSet getDLSet() {
        return this.dlSet;
    }
    public DCSet getDCSet() {
        return this.dcSet;
    }

    public byte[] getMessageMagic() {
        if (this.messageMagic == null) {
            long longTestNetStamp = Settings.getInstance().getGenesisStamp();
            if (!BlockChain.DEMO_MODE && BlockChain.TEST_MODE || BlockChain.CLONE_MODE) {
                byte[] seedTestNetStamp = Crypto.getInstance().digest(Longs.toByteArray(longTestNetStamp));
                this.messageMagic = Arrays.copyOfRange(seedTestNetStamp, 0, Message.MAGIC_LENGTH);
            } else if (BlockChain.CLONE_MODE) {
                byte[] seedTestNetStamp = blockChain.getGenesisBlock().getSignature();
                this.messageMagic = Arrays.copyOfRange(seedTestNetStamp, 0, Message.MAGIC_LENGTH);
            } else {
                this.messageMagic = Message.MAINNET_MAGIC;
            }
        }
        return this.messageMagic;
    }

    public void statusInfo() {
        long timestamp = this.blockChain.getLastBlock(dcSet).getTimestamp();
        LOGGER.info("STATUS " + this.getStatus() + "\n" + "| Last Block Signature: "
                + Base58.encode(this.blockChain.getLastBlock(dcSet).getSignature()) + "\n" + "| Last Block Height: "
                + this.blockChain.getLastBlock(dcSet).getHeight() + "\n" + "| Last Block Time: "
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

    /**
     * Среднее время обработки неподтвержденной транзакции при ее прилете из сети
     *
     * @return
     */
    public long getUnconfigmedMessageTimingAverage() {
        return unconfigmedMessageTimingAverage;
    }

    /**
     * Среднее время обработки транзакции при создании нашего блока. Блок считается как одна транзакция
     *
     * @return
     */
    public long getTransactionMakeTimingAverage() {
        return transactionMakeTimingAverage;
    }

    public void setTransactionMakeTimingAverage(long transactionMakeTimingAverage) {
        this.transactionMakeTimingAverage = transactionMakeTimingAverage;
    }

    /**
     *
     * @return
     */
    public JSONObject getBenchmarks() {

        JSONObject jsonObj = new JSONObject();
        Controller cnt = Controller.getInstance();
        long timing;

        if (network != null) {
            jsonObj.put("missedTelegrams", cnt.getInstance().network.missedTelegrams.get());
            jsonObj.put("activePeersCounter", cnt.getInstance().network.getActivePeersCounter(false, false));
            jsonObj.put("missedWinBlocks", cnt.getInstance().network.missedWinBlocks.get());
            jsonObj.put("missedMessages", cnt.getInstance().network.missedMessages.get());
            jsonObj.put("missedSendes", cnt.getInstance().network.missedSendes.get());

            timing = cnt.getInstance().network.telegramer.messageTimingAverage;

            if (timing > 0) {
                timing = 1000000000L / timing;
            } else {
                timing = 0;
            }
            jsonObj.put("msgTimingAvrg", timing);
        } else {
            jsonObj.put("network", "shutdown");
        }

        jsonObj.put("missedTransactions", cnt.getInstance().transactionsPool.missedTransactions);

        timing = cnt.getInstance().getUnconfigmedMessageTimingAverage();
        if (timing > 0) {
            timing = 1000000L / timing;
        } else {
            timing = 0;
        }
        jsonObj.put("unconfMsgTimingAvrg", timing);

        timing = cnt.getInstance().getBlockChain().transactionWinnedTimingAverage;
        if (timing > 0) {
            timing = 1000000L / timing;
        } else {
            timing = 0;
        }
        jsonObj.put("transactionWinnedTimingAvrg", timing);

        timing = cnt.getInstance().getTransactionMakeTimingAverage();
        if (timing > 0) {
            timing = 1000000L / timing;
        } else {
            timing = 0;
        }
        jsonObj.put("transactionMakeTimingAvrg", timing);

        timing = cnt.getInstance().getBlockChain().transactionValidateTimingAverage;
        if (timing > 0) {
            timing = 1000000L / timing;
        } else {
            timing = 0;
        }
        jsonObj.put("transactionValidateTimingAvrg", timing);

        timing = cnt.getInstance().getBlockChain().transactionProcessTimingAverage;
        if (timing > 0) {
            timing = 1000000L / timing;
        } else {
            timing = 0;
        }
        jsonObj.put("transactionProcessTimingAvrg", timing);

        return jsonObj;
    }


    public void sendMyHWeightToPeer(Peer peer) {

        // SEND HEIGHT MESSAGE
        peer.offerMessage(MessageFactory.getInstance().createHWeightMessage(this.blockChain.getHWeightFull(dcSet)));
    }

    public TransactionCreator getTransactionCreator() {
        return transactionCreator;
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

    public boolean isStatusWaiting() {
        return this.status != STATUS_SYNCHRONIZING;
    }

    public boolean isStatusSynchronizing() {
        return this.status == STATUS_SYNCHRONIZING;
    }

    private void openDataBaseFile(String name, String path, org.erachain.database.IDB dbSet) {

        boolean error = false;
        boolean backUped = false;

        try {
            LOGGER.info("Open " + name);
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Open") + " " + name));

            //// должен быть метод
            ///// DLSet.open();
            /// this.DLSet = DLSet.getinstanse();

            LOGGER.info(name + " OK");
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("OK")));

        } catch (Throwable e) {

            LOGGER.error("Error during startup detected trying to restore backup " + name);
            LOGGER.error(e.getMessage(), e);

            error = true;

            try {
                // пытаемся восстановисть

                /// у объекта должен быть этот метод восстанорвления
                // DLSet.restoreBuckUp();

            } catch (Throwable e1) {

                LOGGER.error("Error during backup, tru recreate " + name);
                LOGGER.error(e1.getMessage(), e1);
                backUped = true;

                try {
                    // пытаемся пересоздать
                    //// у объекта должен быть такой метод пересоздания
                    // DLSet.reCreateDB();

                } catch (Throwable e2) {

                    LOGGER.error("Error during backup, tru recreate " + name);
                    LOGGER.error(e2.getMessage(), e2);
                    // не смогли пересоздать выход!
                    stopAll(-3);
                }

            }
        }

        if (!error && !backUped && Settings.getInstance().getbacUpEnabled()) {
            // если нет ошибок и не было восстановления и нужно делать копии то сделаем

            if (useGui && Settings.getInstance().getbacUpAskToStart()) {
                // ask dialog
                int n = JOptionPane.showConfirmDialog(null, Lang.T("BackUp Database?"),
                        Lang.T("Confirmation"), JOptionPane.OK_CANCEL_OPTION);
                if (n == JOptionPane.OK_OPTION) {
                    this.setChanged();
                    this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("BackUp datachain")));
                    // delete & copy files in BackUp dir

                    //// у объекта должен быть этот метод сохранения DLSet.createDataCheckpoint();
                }
            } else {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("BackUp datachain")));
                // delete & copy files in BackUp dir
                //// у объекта должен быть этот метод сохранения DLSet.createDataCheckpoint();
            }
        }

    }

    public void start() throws Exception {

        this.toOfflineTime = NTP.getTime();
        this.foundMyselfID = new byte[128];
        this.random.nextBytes(this.foundMyselfID);

        // CHECK NETWORK PORT AVAILABLE
        if (BlockChain.TEST_DB == 0 && !Network.isPortAvailable(BlockChain.NETWORK_PORT)) {
            throw new Exception(Lang.T("Network port %port% already in use!").replace("%port%",
                    String.valueOf(BlockChain.NETWORK_PORT)));
        }

        // CHECK RPC PORT AVAILABLE
        if (Settings.getInstance().isRpcEnabled()) {
            if (!Network.isPortAvailable(Settings.getInstance().getRpcPort())) {
                throw new Exception(Lang.T("Rpc port %port% already in use!").replace("%port%",
                        String.valueOf(Settings.getInstance().getRpcPort())));
            }
        }

        // CHECK WEB PORT AVAILABLE
        if (Settings.getInstance().isWebEnabled()) {
            if (!Network.isPortAvailable(Settings.getInstance().getWebPort())) {
                LOGGER.error(Lang.T("Web port %port% already in use!").replace("%port%",
                        String.valueOf(Settings.getInstance().getWebPort())));
            }
        }

        this.transactionCreator = new TransactionCreator();

        // Setting_Json = new JSONObject();
        // Setting_Json = Settings.getInstance().read_setting_JSON();

        int error = 0;
        // OPEN DATABASE

        /////////openDataBaseFile("DataLocale", "/datalocal", DCSet);

        try {
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Open DataLocale")));
            LOGGER.info("Try Open DataLocal");
            this.dlSet = DLSet.reCreateDB();
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("DataLocale OK")));
            LOGGER.info("DataLocale OK");
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            // TODO Auto-generated catch block
            try {
                this.dlSet.close();
            } catch (Exception e2) {
                
            }
            reCreateDB();
            LOGGER.error("Error during startup detected trying to recreate DataLocale...");
        }

        try {
            // удалим все в папке Temp
            File tempDir = new File(Settings.getInstance().getDataTempDir());
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
            ////LOGGER.error(e.getMessage(), e);
        }

        // OPENING DATABASES
        try {
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, "Try Open DataChain"));
            LOGGER.info("Try Open DataChain");
            if (Settings.simpleTestNet) {
                // -testnet
                reCreateDC(inMemoryDC);
            } else {
                this.dcSet = DCSet.getInstance(this.dcSetWithObserver, this.dynamicGUI, inMemoryDC);
            }
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, "DataChain OK"));
            LOGGER.info("DataChain OK - " + Settings.getInstance().getDataChainPath());
        } catch (Throwable e) {
            // Error open DB
            error = 1;
            LOGGER.error("Error during startup detected trying to restore backup DataChain...");
            LOGGER.error(e.getMessage(), e);
            try {
                reCreateDC(inMemoryDC);
            } catch (Throwable e1) {
                LOGGER.error(e1.getMessage(), e1);
                stopAll(5);
            }
        }


        if (error == 0 && useGui && Settings.getInstance().getbacUpEnabled()) {

            if (Settings.getInstance().getbacUpAskToStart()) {
                // ask dialog
                int n = JOptionPane.showConfirmDialog(null, Lang.T("BackUp Database?"),
                        Lang.T("Confirmation"), JOptionPane.OK_CANCEL_OPTION);
                if (n == JOptionPane.OK_OPTION) {
                    this.setChanged();
                    this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("BackUp datachain")));
                    // delete & copy files in BackUp dir
                    createDataCheckpoint();
                }
            } else {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("BackUp datachain")));
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
            try {
                reCreateDC(inMemoryDC);
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
                stopAll(6);
            }
        }

        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Datachain Ok")));
        // createDataCheckpoint();

        // CHECK IF DB NEEDS UPDATE
        /*
         * try { if(this.dcSet.getBlocksHeadMap().getLastBlockSignature() != null) {
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
         * Setting_Json.put("DB_OPEN", "Open BAD - try reCreateDB"); }
         */

        // CREATE BLOCKCHAIN
        this.blockChain = new BlockChain(dcSet);

        // CREATE TRANSACTIONS POOL
        this.transactionsPool = new TransactionsPool(this, blockChain, dcSet);

        // CREATE WinBlock SELECTOR
        this.winBlockSelector = new WinBlockSelector(this, blockChain, dcSet);

        // CREATE SYNCHRONIZOR
        this.synchronizer = new Synchronizer(this);

        // CREATE Block REQUESTER
        this.blockRequester = new BlocksRequest(this, blockChain, dcSet);

        // START API SERVICE
        if (Settings.getInstance().isRpcEnabled()) {
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, "Start API Service"));
            LOGGER.info(Lang.T("Start API Service"));
            this.rpcService = new ApiService();
            this.rpcServiceRestart();
        }

        // START WEB SERVICE
        if (Settings.getInstance().isWebEnabled()) {
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, "Start WEB Service"));
            LOGGER.info(Lang.T("Start WEB Service"));
            this.webService = WebService.getInstance();
            this.webService.start();
        }

        pairsController = new PairsController();

        // CREATE WALLET
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, "Try Open Wallet"));
        this.wallet = new Wallet(dcSet, this.dcSetWithObserver, this.dynamicGUI);

        boolean walletKeysRecovered = false;
        if (this.seedCommand != null && this.seedCommand.length > 1 && !Wallet.walletKeysExists()) {
            /// 0 - Accounts number, 1 - seed, 2 - password, [3 - path]
            byte[] seed;
            if (this.seedCommand[1].length() < 30) {
                seed = new byte[32];
                this.random.nextBytes(seed);
            } else {
                try {
                    seed = Base58.decode(this.seedCommand[1]);
                } catch (Exception e) {
                    seed = null;
                }
            }

            if (seed != null) {

                int accsNum;
                try {
                    accsNum = Ints.tryParse(this.seedCommand[0]);
                } catch (Exception e) {
                    accsNum = 0;
                }

                if (accsNum > 0) {

                    String path;
                    if (this.seedCommand.length > 3) {
                        path = this.seedCommand[3];
                    } else {
                        path = Settings.getInstance().getWalletKeysPath();
                    }

                    walletKeysRecovered = recoverWallet(seed,
                            this.seedCommand.length > 2 ? this.seedCommand[2] : "1",
                            accsNum, path);
                    this.seedCommand = null;
                }
            }

        }

        if (!walletKeysRecovered) {
        }

        if (BlockChain.TEST_DB == 0) {
            guiTimer = new GuiTimer();

            if (this.wallet.isWalletDatabaseExisting()) {
                this.wallet.initiateItemsFavorites();
            }
            this.setChanged();
            String mess = "Wallet OK" + " " + Settings.getInstance().getDataWalletPath();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, mess));
            LOGGER.info(mess);

            // create telegtam

            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, "Open Telegram"));
            this.telegramStore = TelegramStore.getInstanse(this.dcSetWithObserver, this.dynamicGUI);


            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Telegram OK")));

        }

        // CREATE BLOCKGENERATOR
        this.blockGenerator = new BlockGenerator(this.dcSet, this.blockChain, true);
        // START UPDATES and BLOCK BLOCKGENERATOR
        this.blockGenerator.start();

        // CREATE NETWORK
        if (BlockChain.TEST_DB == 0) {
            this.network = new Network(this);
        }

        // CLOSE ON UNEXPECTED SHUTDOWN
        Runtime.getRuntime().addShutdownHook(new Thread(null, null, "ShutdownHook") {
            @Override
            public void run() {
                // -999999 - not use System.exit() - if freeze exit
                stopAll(-999999);
                //Runtime.getRuntime().removeShutdownHook(currentThread());
            }
        });

        if (!useNet)
            this.status = STATUS_OK;

        // start memory viewer
        MemoryViewer mamoryViewer = new MemoryViewer(this);
        mamoryViewer.start();

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

    public int loadWalletFromDir() {
        JFileChooser fileopen = new JFileChooser();
        fileopen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String pathOld = Settings.getInstance().getWalletKeysPath();
        File ff = new File(pathOld);
        if (!ff.exists())
            pathOld = "." + File.separator;
        fileopen.setCurrentDirectory(new File(pathOld));
        int ret = fileopen.showDialog(null, Lang.T("Open Wallet Dir"));
        if (ret != JFileChooser.APPROVE_OPTION) {
            //is abort
            return 3;

        }

        String selectedDir = fileopen.getSelectedFile().toString();

        // set wallet dir
        Settings.getInstance().setWalletKeysPath(selectedDir);

        // open wallet
        if (Controller.getInstance().wallet == null) {
            Controller.getInstance().wallet = new Wallet(dcSet, dcSetWithObserver, dynamicGUI);
        }

        // not wallet return 0;
        if (!Controller.getInstance().wallet.walletKeysExists()) {
            Settings.getInstance().setWalletKeysPath(pathOld);
            return 2;
        }

        if (!Controller.getInstance().isWalletUnlocked()) {
            // ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(null);
            if (!Controller.getInstance().unlockWallet(password)) {
                // WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.T("Invalid password"),
                        Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
                return 5;
            }
        }

        // LOAD accounts
        Controller.getInstance().wallet.updateAccountsFromSecretKeys();

        if (Controller.getInstance().wallet.isWalletDatabaseExisting()) {
            Controller.getInstance().wallet.initiateItemsFavorites();
            // save path from setting json
            Settings.getInstance().updateSettingsValue();
            // is ok
            return 1;
        } else {
            Settings.getInstance().setWalletKeysPath(pathOld);
            return 3;
        }
    }

    public void replaseFavoriteItems(int type) {
        this.wallet.replaseFavoriteItems(type);
    }

    public DCSet reCreateDC(boolean inMemory) throws IOException, Exception {

        if (inMemory) {
            DCSet.reCreateDBinMEmory(this.dcSetWithObserver, this.dynamicGUI);
        } else {
            File dataChain = new File(Settings.getInstance().getDataChainPath());
            File dataChainBackUp = new File(Settings.getInstance().getBackUpPath()
                    + Settings.getInstance().getDataChainPath() + File.separator);
            // del datachain
            if (dataChain.exists()) {
                try {
                    Files.walkFileTree(dataChain.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
                } catch (IOException e) {
                    //LOGGER.error(e.getMessage(), e);
                }
            }
            // copy Back dir to DataChain
            if (false && dataChainBackUp.exists()) {

                try {
                    FileUtils.copyDirectory(dataChainBackUp, dataChain);
                    LOGGER.info("Restore BackUp/DataChain to DataChain is Ok");
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }

            }

            DCSet.reCreateDB(this.dcSetWithObserver, this.dynamicGUI);

        }
        this.dcSet = DCSet.getInstance();
        return this.dcSet;
    }


    // recreate DB locate
    public DLSet reCreateDB() throws IOException, Exception {

        File dataLocal = new File(Settings.getInstance().getLocalDir());

        // del DataLocal
        if (dataLocal.exists()) {
            try {
                Files.walkFileTree(dataLocal.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
            } catch (IOException e) {
                //LOGGER.error(e.getMessage(), e);
            }
        }

        this.dlSet = DLSet.reCreateDB();
        return this.dlSet;
    }

    private void createDataCheckpoint() {
        if (!this.dcSet.getBlockMap().isProcessing()) {
            // && Settings.getInstance().isCheckpointingEnabled()) {
            // this.dcSet.close();

            File dataDir = new File(Settings.getInstance().getDataChainPath());

            File dataBakDC = new File(Settings.getInstance().getBackUpPath()
                    + Settings.getInstance().getDataChainPath() + File.separator);
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
        }

    }

    private File getDataBakDir(File dataDir) {
        return new File(dataDir.getParent(), Settings.getInstance().getDataChainPath() + "Bak");
    }

    public ApiService getRPCService() {
        if (this.rpcService == null) {
            this.rpcService = new ApiService();
        }
        return rpcService;
    }

    public void rpcServiceRestart() {
        getRPCService();
        this.rpcService.stop();

        // START API SERVICE
        if (Settings.getInstance().isRpcEnabled()) {
            this.rpcService.start();
        }
    }

    public void webServiceRestart() {

        if (this.webService != null)
            this.webService.stop();
        while( !this.webService.isStoped()){}
        this.webService = null;

        // START API SERVICE
        WebService.getInstance().clearInstance();
        if (Settings.getInstance().isWebEnabled()) {
            this.webService = WebService.getInstance();
            this.webService.start();
        }
    }

    public boolean isOnStopping() {
        return this.isStopping;
    }

    public void stopAll(int par) {
        // PREVENT MULTIPLE CALLS
        if (this.isStopping)
            return;
        this.isStopping = true;

        if (this.connectTimer != null)
            this.connectTimer.cancel();

        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Closing")));
        // STOP MESSAGE PROCESSOR
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Stopping message processor")));

        if (this.network != null) {
            LOGGER.info("Stopping message processor");
            this.network.stop();
        }


        if (this.webService != null) {
            LOGGER.info("Stopping WEB server");
            this.webService.stop();
        }

        if (this.rpcService != null) {
            LOGGER.info("Stopping RPC server");
            this.rpcService.stop();
        }

        // delete temp Dir
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Delete files from TEMP dir")));
        LOGGER.info("Delete files from TEMP dir");
        try {
            File tempDir = new File(Settings.getInstance().getDataTempDir());
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Exception e) {
        }

        // STOP TRANSACTIONS POOL
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Stopping Transactions Pool")));
        LOGGER.info("Stopping Transactions Pool");
        this.transactionsPool.halt();

        // STOP WIN BLOCK SELECTOR
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Stopping WinBlock Selector")));
        LOGGER.info("Stopping WinBlock Selector");
        this.winBlockSelector.halt();

        // STOP BLOCK REQUESTER
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Stopping Block Requester")));
        LOGGER.info("Stopping Block Requester");
        this.blockRequester.halt();

        // STOP BLOCK PROCESSOR
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Stopping block synchronizer")));
        LOGGER.info("Stopping block synchronizer");
        this.synchronizer.stop();

        // WAITING STOP MAIN PROCESS
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Waiting stopping processors")));
        LOGGER.info("Waiting stopping processors");

        int i = 0;
        while (i++ < 10 && blockGenerator.getStatus() > 0) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }

        if (dcSet.isBusy())
            this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("DCSet is busy...")));
        LOGGER.info("DCSet is busy...");

        i = 0;
        while (i++ < 10 && dcSet.isBusy()) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                break;
            }
        }

        if (this.wallet != null) {
            // CLOSE WALLET
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Closing wallet")));
            LOGGER.info("Closing wallet");
            this.wallet.close();
        }

        // CLOSE LOCAL
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Closing Local database")));
        LOGGER.info("Closing Local database");
        this.dlSet.close();

        if (telegramStore != null) {
            // CLOSE telegram
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Closing telegram")));
            LOGGER.info("Closing telegram");
            this.telegramStore.close();
        }

        // CLOSE DATABABASE
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T("Closing database")));
        LOGGER.info("Closing database");
        this.dcSet.close();

        LOGGER.info("Closed.");
        // FORCE CLOSE
        if (par != -999999) {
            LOGGER.info("EXIT parameter:" + par);
            System.exit(par);
            //System.
            // bat
            // if %errorlevel% neq 0 exit /b %errorlevel%
        } else {
            LOGGER.info("EXIT parameter:" + 0);
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
            this.network.pingAllPeers(onlySynchronized);
    }

    public int getActivePeersCounter() {
        // GET ACTIVE PEERS
        return this.network.getActivePeersCounter(false, false);
    }

    public void walletSyncStatusUpdate(int height) {
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_SYNC_STATUS, height));
    }

    public void blockchainSyncStatusUpdate(int height) {
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.BLOCKCHAIN_SYNC_STATUS, height));
    }

    public void playWalletEvent(Object object) {
        if (gui == null || gui.walletNotifyTimer == null)
            return;
        gui.walletNotifyTimer.playEvent(object);
    }

    /**
     * Total time in disconnect
     *
     * @return
     */
    public long getToOfflineTime() {
        return this.toOfflineTime;
    }

    public void setToOfflineTime(long time) {
        this.toOfflineTime = time;
    }

    public void notifyObserveUpdatePeer(Peer peer) {
        this.network.notifyObserveUpdatePeer(peer);
    }

    public boolean broadcastUnconfirmedToPeer(Peer peer) {

        // logger.info(peer + " sended UNCONFIRMED ++++ START ");

        byte[] peerByte = peer.getAddress().getAddress();

        if (this.isStopping)
            return false;

        TransactionMap map = this.dcSet.getTransactionTab();
        if (map.isClosed())
            return false;

        try {
            try (IteratorCloseable<Long> iterator = map.getIndexIterator(TransactionSuit.TIMESTAMP_INDEX, false)) {
                long ping = 0;
                int counter = 0;
                ///////// big maxCounter freeze network and make bans on response
                ///////// headers andblocks
                int stepCount = 64; // datachain.TransactionMap.MAX_MAP_SIZE>>2;
                long dTime = this.blockChain.getTimestamp(this.dcSet);
                boolean pinged = false;
                long timePoint;

                while (iterator.hasNext() && stepCount > 2 && peer.isUsed()) {

                    counter++;

                    if (this.isStopping) {
                        return false;
                    }

                    if (map.isClosed())
                        return false;
                    Transaction transaction = map.get(iterator.next());
                    if (transaction == null)
                        continue;

                    // logger.error(" time " + transaction.viewTimestamp());

                    if (counter > BlockChain.ON_CONNECT_SEND_UNCONFIRMED_UNTIL
                        // дело в том что при коннекте новому узлу надо все же
                        // передавать все так как он может собрать пустой блок
                        /////&& !map.needBroadcasting(transaction, peerByte)
                    )
                        break;

                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                    }

                    Message message = MessageFactory.getInstance().createTransactionMessage(transaction);

                    try {
                        // воспользуемся тут прямой пересылкой - так как нам надо именно ждать всю обработку
                        if (peer.directSendMessage(message)) {

                            if (peer.getPing() > 300) {
                                this.network.notifyObserveUpdatePeer(peer);
                                LOGGER.debug(" bad ping " + peer.getPing() + "ms for:" + counter);

                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                }
                            }

                        } else {
                            return false;
                        }
                    } catch (Exception e) {
                        if (this.isStopping) {
                            return false;
                        }
                        LOGGER.error(e.getMessage(), e);
                    }

                    if (counter % stepCount == 0) {

                        pinged = true;
                        //peer.tryPing();
                        //this.network.notifyObserveUpdatePeer(peer);
                        ping = peer.getPing();

                        if (ping < 0 || ping > 300) {

                            stepCount >>= 1;

                            try {
                                Thread.sleep(2000);
                            } catch (Exception e) {
                            }

                            LOGGER.debug(peer + " stepCount down " + stepCount);

                        } else if (ping < 100) {
                            stepCount <<= 1;
                            LOGGER.debug(peer + " stepCount UP " + stepCount + " for PING: " + ping);
                        }

                    }
                }
            }

            // logger.info(peer + " sended UNCONFIRMED counter: " +
            // counter);

        } catch (java.lang.Throwable e) {
            if (e instanceof java.lang.IllegalAccessError) {
                // налетели на закрытую таблицу
            } else {
                LOGGER.error(e.getMessage(), e);
            }

        }

        //NOTIFY OBSERVERS
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_PEER_TYPE, peer));

        return peer.isUsed();

    }

    public Synchronizer getSynchronizer() {
        return synchronizer;
    }

    /**
     * при установке коннекта нельзя сразу пинговать - это тормозит и толку ноль - пинги не проходят
     * а вот после уже передачи неподтвержденных трнзакций - можно пингануть - тогда вроде норм все проходит
     *
     * @param peer
     */
    public void onConnect(Peer peer) {

        if (this.isStopping) {
            return;
        }

        // SEND FOUNDMYSELF MESSAGE
        if (!peer.directSendMessage(
                MessageFactory.getInstance().createFindMyselfMessage(Controller.getInstance().getFoundMyselfID()))) {
            peer.ban(network.banForActivePeersCounter(), "connection - break on MY ID send");
            return;
        }

        // SEND VERSION MESSAGE
        JSONObject peerInfo = new JSONObject();
        peerInfo.put("v", Controller.getVersion(true));
        Tuple2<Integer, Long> myHWeight = this.getBlockChain().getHWeightFull(dcSet);
        peerInfo.put("h", myHWeight.a);
        peerInfo.put("w", myHWeight.b);
        JSONObject info = new JSONObject();
        if (Settings.getInstance().isWebEnabled() && Settings.getInstance().getWebAllowed().length == 0) {
            // разрешено всем - передадим его
            info.put("port", Settings.getInstance().getWebPort());
            info.put("scheme", Settings.getInstance().isWebUseSSL() ? "https" : "http");

        }
        peerInfo.put("i", info);

        // CheckPointSign
        peerInfo.put("cps", Base58.encode(blockChain.getMyHardCheckPointSign()));
        peerInfo.put("cph", blockChain.getMyHardCheckPointHeight());

        if (!peer.directSendMessage(
                MessageFactory.getInstance().createVersionMessage(peerInfo.toString(), buildTimestamp))) {
            peer.ban(network.banForActivePeersCounter(), "connection - break on Version send");
            return;
        }

        if (false && BlockChain.DEMO_MODE) {
            try {
                synchronizer.checkBadBlock(peer);
            } catch (Exception e) {
                if (!peer.isBanned()) {
                    peer.ban(Synchronizer.BAN_BLOCK_TIMES >> 2, "lose connection: " + e.getMessage());
                }
                return;
            }
        }

        if (this.isStopping)
            return; // MAY BE IT HARD BUSY

        { // ограничим действие переменной winBlock
            // GET CURRENT WIN BLOCK
            Block winBlock = this.blockChain.getWaitWinBuffer();
            if (winBlock != null) {
                // SEND MESSAGE
                peer.sendWinBlock((BlockWinMessage) MessageFactory.getInstance().createWinBlockMessage(winBlock));
            }
        }

        if (this.status == STATUS_NO_CONNECTIONS) {
            // UPDATE STATUS
            int myHeight = getMyHeight();
            if (blockChain.getTimestamp(myHeight)
                    + (BlockChain.GENERATING_MIN_BLOCK_TIME_MS(myHeight) >> 1)
                    < NTP.getTime()) {
                // мы не во воремени - надо синхронизироваться
                this.status = STATUS_SYNCHRONIZING;
                LOGGER.debug("status = STATUS_SYNCHRONIZING by " + peer);
            } else {
                // время не ушло вперед - можно не синронизироваться
                this.status = STATUS_OK;
            }

            // NOTIFY
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));

        }

        // BROADCAST UNCONFIRMED TRANSACTIONS to PEER
        if (!this.broadcastUnconfirmedToPeer(peer)) {
            peer.ban(network.banForActivePeersCounter(), "broken on SEND UNCONFIRMEDs");
            return;
        }

        this.actionAfterConnect();

    }

    public boolean newPeerConnected;

    /**
     * учет времени полного длисконекта ноды
     */
    public void actionAfterConnect() {

        newPeerConnected = true;

        if (this.connectTimer == null) {
            this.connectTimer = new Timer("Action after connect");

            TimerTask action = new TimerTask() {
                @Override
                public void run() {

                    if (Controller.getInstance().getStatus() == STATUS_OK) {

                        Controller.getInstance().setToOfflineTime(0L);

                    }
                }
            };

            this.connectTimer.schedule(action, 30000, 30000);
        }

    }

    public void forgingStatusChanged(BlockGenerator.ForgingStatus status) {
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.FORGING_STATUS, status));
    }

    private long statusObserveTime;

    // used from NETWORK
    public void afterDisconnect(Peer peer) {

        if (network.noActivePeers(false)) {

            if (this.getToOfflineTime() == 0L) {
                // SET START OFFLINE TIME
                this.setToOfflineTime(NTP.getTime());
            }

            // UPDATE STATUS
            if (!useNet)
                this.status = STATUS_OK;
            else
                this.status = STATUS_NO_CONNECTIONS;

            if (System.currentTimeMillis() - statusObserveTime > 2000) {
                // чтобы не генерилось часто
                statusObserveTime = System.currentTimeMillis();
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

    // SYNCHRONIZED DO NOT PROCESSS MESSAGES SIMULTANEOUSLY
    public void onMessage(Message message) {
        Message response;
        Block newBlock;

        if (this.isStopping)
            return;

        long timeCheck = System.currentTimeMillis();

        switch (message.getType()) {

            case Message.HWEIGHT_TYPE:

                HWeightMessage hWeightMessage = (HWeightMessage) message;

                // TEST TIMESTAMP of PEER
                Tuple2<Integer, Long> hW = hWeightMessage.getHWeight();
                // TODO
                String errorMess = this.getBlockChain().blockFromFuture(hW.a - 2);
                if (errorMess != null) {
                    // IT PEER from FUTURE
                    hWeightMessage.getSender().ban(errorMess);
                    return;
                }

                // ADD TO LIST
                hWeightMessage.getSender().setHWeight(hWeightMessage.getHWeight());

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
                        : this.dcSet.getBlockSignsMap().get(headers.get(0)) == null ? "CHECK"
                        : this.dcSet.getBlockSignsMap().get(headers.get(0))));

                /*
                 * logger.error(message.getId() +
                 * " controller.Controller.onMessage(Message).GET_SIGNATURES_TYPE ->"
                 * + Base58.encode(getHeadersMessage.getParent()));
                 *
                 * if (!headers.isEmpty()) {
                 * logger.error("this.blockChain.getSignatures.get(0) -> " +
                 * Base58.encode( headers.get(0) )); logger.
                 * error("this.blockChain.getSignatures.get(headers.size()-1) -> "
                 * + Base58.encode( headers.get(headers.size()-1) )); } else
                 * { logger.
                 * error("controller.Controller.onMessage(Message).GET_SIGNATURES_TYPE -> NOT FOUND!"
                 * ); }
                 */

                // CREATE RESPONSE WITH SAME ID
                response = MessageFactory.getInstance().createHeadersMessage(headers);
                response.setId(message.getId());

                // SEND RESPONSE BACK WITH SAME ID
                message.getSender().offerMessage(response);

                timeCheck = System.currentTimeMillis() - timeCheck;
                if (timeCheck > 10) {
                    LOGGER.debug(this + " : " + message + " solved by period: " + timeCheck);
                }

                break;

            case Message.VERSION_TYPE:

                VersionMessage versionMessage = (VersionMessage) message;
                Peer peer = versionMessage.getSender();
                peer.setBuildTime(versionMessage.getBuildDateTime());

                // ADD TO LIST
                String infoStr = versionMessage.getStrVersion();
                JSONObject peerIhfo = (JSONObject) JSONValue.parse(infoStr);
                Integer peerHeight = Integer.parseInt(peerIhfo.get("h").toString());
                Long peerWeight = (Long) peerIhfo.get("w");
                peer.setHWeight(new Tuple2<>(peerHeight, peerWeight));
                peer.setVersion((String) peerIhfo.get("v"));
                peer.setNodeInfo((JSONObject) peerIhfo.get("i"));

                if (!blockChain.validateHardCheckPointPeerSign((Long) peerIhfo.get("cph"), (String) peerIhfo.get("cps"))) {
                    peer.ban(10, "WRONG HARD CHECKPOINT!");
                    return;
                }

                break;

            default:

                LOGGER.debug(" UNKNOWN: " + message.viewPref(false) + message);

                Tuple2<Integer, Long> HWeight = Controller.getInstance().getBlockChain().getHWeightFull(dcSet);
                if (HWeight == null)
                    HWeight = new Tuple2<Integer, Long>(-1, -1L);

                // TODO - for OLD versions
                // CREATE PING
                response = MessageFactory.getInstance().createHWeightMessage(HWeight);
                // CREATE RESPONSE WITH SAME ID
                response.setId(message.getId());

                // SEND BACK TO SENDER
                message.getSender().offerMessage(response);

        }

    }

    public void addActivePeersObserver(Observer o) {
        if (this.network != null)
            this.network.addObserver(o);
        if (this.guiTimer != null)
            this.guiTimer.addObserver(o);
    }

    public void removeActivePeersObserver(Observer o) {
        if (this.network != null)
            this.guiTimer.deleteObserver(o);
        if (this.guiTimer != null)
            this.network.deleteObserver(o);
    }

    public void broadcastWinBlock(Block newBlock) {

        if (network == null)
            return;

        LOGGER.info("broadcast winBlock " + newBlock.toString() + " size:" + newBlock.getTransactionCount());

        // CREATE MESSAGE
        BlockWinMessage blockWinMessage = (BlockWinMessage) MessageFactory.getInstance().createWinBlockMessage(newBlock);

        if (this.isOnStopping())
            return;

        // BROADCAST MESSAGE
        this.network.broadcastWinBlock(blockWinMessage, false);

        LOGGER.info("broadcasted!");

    }

    public void broadcastHWeightFull() {

        // logger.info("broadcast winBlock " + newBlock.toString(this.dcSet));

        // CREATE MESSAGE
        // GET HEIGHT
        Tuple2<Integer, Long> HWeight = this.blockChain.getHWeightFull(dcSet);
        if (HWeight == null)
            return;

        Message messageHW = MessageFactory.getInstance().createHWeightMessage(HWeight);

        // BROADCAST MESSAGE
        this.network.broadcast(messageHW, false);

    }

    public void broadcastTransaction(Transaction transaction) {

        // CREATE MESSAGE
        Message message = MessageFactory.getInstance().createTransactionMessage(transaction);

        // BROADCAST MESSAGE
        this.network.broadcast(message, false);
    }

    public int broadcastTelegram(Transaction transaction, boolean store) {

        // CREATE MESSAGE
        Message telegram = MessageFactory.getInstance().createTelegramMessage(transaction);
        int notAdded = this.network.addTelegram((TelegramMessage) telegram);

        if (!store || notAdded == 0) {
            // BROADCAST MESSAGE
            this.network.broadcast(telegram, false);
            // save DB
            if (wallet != null && wallet.dwSet != null) {
                wallet.dwSet.getTelegramsMap().add(transaction.viewSignature(), transaction);
            }
        }

        return notAdded;

    }

    // SYNCHRONIZE

    public void orphanInPipe(Block block) throws Exception {
        this.synchronizer.pipeProcessOrOrphan(this.dcSet, block, true, false, false);
    }

    public boolean checkStatus(int shift) {
        if (!useNet) {
            this.status = STATUS_OK;
            return true;
        }

        if (network != null && network.noActivePeers(false)) {
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
        Tuple3<Integer, Long, Peer> maxHW = this.getMaxPeerHWeight(shift, false, false);
        if (maxHW.c == null) {
            this.status = STATUS_OK;
            return true;
        }

        if (maxHW.a > thisHW.a + shift) {
            this.status = STATUS_SYNCHRONIZING;
            LOGGER.debug("status = STATUS_SYNCHRONIZING by check maxHW: " + maxHW.a + " - shift: " + shift);
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
         * dcSet.getBlocksHeadMap().getLastBlockSignature();
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
         * //logger.info("Controller.isUpToDate getMaxPeerHWeight:" +
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
            new Thread(() -> {
                setChanged();
                notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));
            }).start();
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

    private int skipNotify = 0;
    private long skipNotifyTime = 0L;
    // https://127.0.0.1/7pay_in/tools/block_proc/ERA
    public void NotifyWalletIncoming(List<Transaction> transactions) {

        if (!doesWalletExists())
            return;

        List<Account> accounts = this.wallet.getAccounts();
        List<Integer> seqs = new ArrayList<Integer>();

        int seq = 0;
        for (Transaction transaction : transactions) {

            transaction.setDC(dcSet);

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

        // Если моих транзакций нету
        if (seqs.isEmpty()
                // раз в 100 блоков уведомлять что обновиться  надо
                && (++skipNotify < 10
                || System.currentTimeMillis() - skipNotifyTime < 200000L
                || isStatusSynchronizing()))
            return;

        skipNotify = 0;
        skipNotifyTime = System.currentTimeMillis();

        // SEE -
        // http://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
        String url_string = Settings.getInstance().getNotifyIncomingURL();
        try {

            // CREATE CONNECTION
            URL url = new URL(url_string);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // EXECUTE
            int res = connection.getResponseCode();
            LOGGER.info("NotifyIncoming " + url_string + ": " + res);

        } catch (Exception e) {
            LOGGER.error("try NotifyIncoming: " + url_string);
            LOGGER.error(e.getMessage(), e);
        }

    }

    public boolean isNSUpToDate() {
        return !Settings.getInstance().updateNameStorage();
    }

    public Account[] getInvolvedAccounts(Transaction transaction) {
        if (!doesWalletExists())
            return null;

        return wallet.getInvolvedAccounts(transaction);
    }

    public Account getInvolvedAccount(Transaction transaction) {
        if (!doesWalletExists())
            return null;

        return wallet.getInvolvedAccount(transaction);
    }

    /**
     * вызывается только из синхронизатора в момент синхронизации цепочки.
     * Поэтому можно сипользовать внутренню переменную
     *
     * @param currentBetterPeer
     * @throws Exception
     */
    public synchronized void checkNewBetterPeer(Peer currentBetterPeer) throws Exception {

        if (!newPeerConnected)
            return;

        newPeerConnected = false;

        // нам не важно отличие в последнем блоке тут - главное чтобы цепочка была длиньше?
        //blockGenerator.checkWeightPeers();
        Tuple3<Integer, Long, Peer> betterPeerHW = this.getMaxPeerHWeight(0, false, true);
        if (betterPeerHW != null) {
            Tuple2<Integer, Long> currentHW = currentBetterPeer.getHWeight(true);
            if (currentHW != null && (currentHW.a >= betterPeerHW.a
                    || currentBetterPeer.equals(betterPeerHW.c))) {
                // новый пир не лучше - продолжим синхронизацию не прерываясь
                return;
            }
        } else {
            // пиров нет вообще - прекратим синхронизацию
            throw new Exception("peer is unconnected");
        }

        if (this.blockGenerator.checkWeightPeers()) {
            throw new Exception("New Better Peer is found " + betterPeerHW.c);
        }

    }

    public Block checkNewPeerUpdates(Peer peer) {
        if (this.blockChain.isEmptyWaitWinBuffer()) {
            // если победный блок не подошел - значит он устарел и там скорее уже цепочка двинулась еще
            byte[] lastSignature = getLastBlockSignature();
            Message mess = MessageFactory.getInstance()
                    .createGetHeadersMessage(lastSignature);
            SignaturesMessage resp = (SignaturesMessage) peer.getResponse(mess, 3000); // AWAIT!
            if (resp != null) {
                List<byte[]> signatures = resp.getSignatures();
                if (!signatures.isEmpty()) {
                    // а теперь еще проверим - может там уже новый блок в цепочке последним добавлен?
                    Message message = MessageFactory.getInstance().createGetBlockMessage(signatures.get((0)));
                    //SEND MESSAGE TO PEER
                    BlockMessage response = (BlockMessage) peer.getResponse(message, 10000);
                    //CHECK IF WE GOT RESPONSE
                    if (response != null) {
                        Block block = response.getBlock();
                        if (Arrays.equals(block.getReference(), lastSignature)) {
                            LOGGER.debug("ADD update new block");
                            this.blockChain.setWaitWinBuffer(this.dcSet, block,
                                    peer // тут ПИР забаним если не прошел так как заголовок то сошелся
                            );
                            return this.blockChain.popWaitWinBuffer();
                        }
                    }
                }
            }
        }
        return null;
    }

    public void requestLastBlock() {
        // TODO тут сделать стандартный пустой блок для запроса или новую команду сетевую
        Block block = getBlockByHeight(2);
        if (block != null) {
            broadcastWinBlock(block);
        }
    }

    public Peer update(int shift) {
        // UPDATE STATUS

        if (this.status == STATUS_NO_CONNECTIONS) {
            return null;
        }

        /// this.status = STATUS_SYNCHRONIZING;

        // DLSet dcSet = DLSet.getInstance();

        Peer peer = null;
        // Block lastBlock = getLastBlock();
        // int lastTrueBlockHeight = this.getMyHeight() -
        // Settings.BLOCK_MAX_SIGNATURES;
        int checkPointHeight = BlockChain.getCheckPoint(dcSet, true);

        Tuple2<Integer, Long> myHWeight = this.getBlockChain().getHWeightFull(dcSet);

        boolean isUpToDate;
        // WHILE NOT UPTODATE
        do {

            // NOTIFY
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));

            // START UPDATE FROM HIGHEST HEIGHT PEER
            // withWinBuffer = true
            // тут поиск длаем с учетом СИЛЫ
            // но если найдено с такой же высотой как у нас то игнорируем
            Tuple3<Integer, Long, Peer> peerHW;
            Tuple2<Integer, Long> peerHWdata = null;
            if (blockGenerator.betterPeer == null) {
                peerHW = this.getMaxPeerHWeight(shift, false, true);
            } else {
                // берем пир который нашли в генераторе при осмотре более сильных цепочек
                // иначе тут будет взято опять значение накрученное самим пировм ипереданое нам
                // так как тут не подвергаются исследованию точность, как это делается в checkWeightPeers
                peerHWdata = blockGenerator.betterPeer.getHWeight(true);
                if (peerHWdata == null) {
                    // почемуто там пусто - уже произошла обработка что этот пир как мы оказался и его удалили
                    peerHW = this.getMaxPeerHWeight(shift, false, true);
                    LOGGER.info(info);
                } else {
                    peerHW = new Tuple3<Integer, Long, Peer>(peerHWdata.a, peerHWdata.b, blockGenerator.betterPeer);
                }
                blockGenerator.betterPeer = null;
            }

            if (peerHW != null && peerHW.a > myHWeight.a
                    || peerHWdata != null) {
                peer = peerHW.c;
                if (peer != null) {
                    info = "update from MaxHeightPeer:" + peer + " WH: "
                            + peer.getHWeight(true);
                    LOGGER.info(info);
                    this.setChanged();
                    this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.T(info)));
                    try {
                        // SYNCHRONIZE FROM PEER
                        if (!this.isOnStopping())
                            this.synchronizer.synchronize(dcSet, checkPointHeight, peer, peerHW.a, null);
                        if (this.isOnStopping())
                            return null;
                    } catch (Exception e) {
                        if (this.isOnStopping()) {
                            // on closing this.blocchain.rollback(dcSet);
                            return null;
                        } else if (peer.isBanned()) {
                            ;
                        } else if (blockGenerator.betterPeer != null) {
                            // найден новый лучший ПИР
                            isUpToDate = false;
                            continue;
                        } else {
                            LOGGER.error(e.getMessage(), e);
                            peer.ban(e.getMessage());
                            return null;
                        }
                    }

                    // сохранимся - хотя может и заря - раньше то работало и так
                    if (true) {
                        dcSet.flush(0, true, false);
                    }

                }

                blockchainSyncStatusUpdate(getMyHeight());
            }

            isUpToDate = checkStatus(shift);
            this.checkStatusAndObserve(shift);

        } while (!this.isStopping && !isUpToDate);

        if (network.noActivePeers(false) || peer == null) {
            // UPDATE STATUS
            this.status = STATUS_NO_CONNECTIONS;
            // } else if (!this.isUpToDate()) {
            ////// this.s/tatus = STATUS_SYNCHRONIZING;
            // UPDATE RENEW
            /// update();
        } else {
            this.status = STATUS_OK;
            this.pingAllPeers(false);
            if (this.isStopping) return null;

            // если в момент синхронизации прилетал победный блок
            // то его вынем и поновой вставим со всеми проверками
            Block winBlockUnchecked = this.blockChain.popWaitWinBuffer();
            if (winBlockUnchecked != null) {
                if (!this.blockChain.setWaitWinBuffer(this.dcSet, winBlockUnchecked,
                        null // если блок не верный - не баним ПИР может просто он отстал
                )) {
                    // если все же он не подошел или не было победного то вышлем всеим запрос на "Порделитесль последним блоком"
                    LOGGER.info("requestLastBlock");
                    requestLastBlock();
                }
            }
        }

        // send to ALL my HW
        //// broadcastHWeight(null);
        if (this.isStopping)
            return null;

        // NOTIFY
        this.setChanged();
        try {
            this.notifyObservers(new ObserverMessage(ObserverMessage.NETWORK_STATUS, this.status));
        } catch (ClassCastException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        PairsController.foundPairs(dcSet, dlSet, 10);

        this.statusInfo();
        return peer;

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

    public Tuple3<Integer, Long, Peer> getMaxPeerHWeight(int shift, boolean useWeight, boolean excludeMute) {

        if (this.isStopping || this.dcSet.isStoped() || this.network == null)
            return new Tuple3<Integer, Long, Peer>(0, 0L, null);

        Tuple2<Integer, Long> myHWeight = this.getBlockChain().getHWeightFull(dcSet);
        int height = myHWeight.a + shift;
        long weight = myHWeight.b;
        Peer maxPeer = null;

        long maxHeight = blockChain.getHeightOnTimestampMS(NTP.getTime());

        try {
            for (Peer peer : network.getActivePeers(false)) {
                if (peer.getPing() < 0) {
                    // не использовать пиры которые не в быстром коннекте
                    // - так как иначе они заморозят синхронизацию совсем
                    // да и не понятно как с них данные получать
                    continue;
                }
                if (excludeMute) {
                    int muteCount = peer.getMute();
                    if (muteCount > 0) {
                        ///// и не использовать те кому мы заткнули - они данные по Силе блока завышенные дают
                        continue;
                    }
                }
                Tuple2<Integer, Long> whPeer = peer.getHWeight(true);
                if (maxHeight < whPeer.a) {
                    // Этот пир дает цепочку из будущего - не берем его
                    peer.ban(5, "FROM FUTURE: " + whPeer);
                    continue;
                }

                if (height < whPeer.a
                        || useWeight && weight < whPeer.b) {
                    height = whPeer.a;
                    weight = whPeer.b;
                    maxPeer = peer;
                }
            }
        } catch (Exception e) {
            // PEER REMOVED WHILE ITERATING
            LOGGER.error(e.getMessage(), e);
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
        peer.setHWeight(hWeightMy);
        //// blockchainSyncStatusUpdate(this.getMyHeight());
    }

    // WALLET

    public boolean doesWalletExists() {
        // CHECK IF WALLET EXISTS
        return !noUseWallet && this.wallet != null && this.wallet.walletKeysExists();
    }

    public boolean doesWalletDatabaseExists() {
        return !noUseWallet && wallet != null && this.wallet.isWalletDatabaseExisting();
    }

    // use license KEY
    public boolean createWallet(long licenseKey, byte[] seed, String password, int amount, String path) {

        if (noUseWallet)
            return true;

        // IF NEW WALLET CREADED
        if (this.wallet.create(seed, password, amount, false, path,
                this.dcSetWithObserver, this.dynamicGUI)) {
            this.setWalletLicense(licenseKey);
            return true;
        } else
            return false;
    }

    public boolean recoverWallet(byte[] seed, String password, int amount, String path) {

        if (noUseWallet)
            return true;

        if (this.wallet.create(seed, password, amount, false, path,
                this.dcSetWithObserver, this.dynamicGUI)) {

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

    public List<Account> getWalletAccounts() {

        return this.wallet.getAccounts();
    }

    public List<Account> getWalletAccountsAndSetBalancePosition(int position) {

        return this.wallet.getAccountsAndSetBalancePosition(position);
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

    public List<PublicKeyAccount> getWalletPublicKeyAccounts() {
        return this.wallet.getPublicKeyAccounts();
    }

    public List<PrivateKeyAccount> getWalletPrivateKeyAccounts() {
        return this.wallet.getprivateKeyAccounts();
    }

    public String generateNewWalletAccount() {
        return this.wallet.generateNewAccount();
    }

    public PrivateKeyAccount getWalletPrivateKeyAccountByAddress(String address) {
        if (this.doesWalletExists()) {
            return this.wallet.getPrivateKeyAccount(address);
        } else {
            return null;
        }
    }

    public PrivateKeyAccount getWalletPrivateKeyAccountByAddress(Account account) {
        if (this.doesWalletExists()) {
            return this.wallet.getPrivateKeyAccount(account);
        } else {
            return null;
        }
    }

    public byte[] decrypt(PublicKeyAccount creator, Account recipient, byte[] data) {

        Account account = this.getWalletAccountByAddress(creator.getAddress());

        byte[] privateKey = null;
        byte[] publicKey = null;

        // IF SENDER ANOTHER
        if (account == null) {
            PrivateKeyAccount accountRecipient = this.getWalletPrivateKeyAccountByAddress(recipient.getAddress());
            privateKey = accountRecipient.getPrivateKey();

            publicKey = creator.getPublicKey();
        }
        // IF SENDER ME
        else {
            PrivateKeyAccount accountRecipient = this.getWalletPrivateKeyAccountByAddress(account.getAddress());
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
    public Account getWalletAccountByAddress(String address) {
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

    public boolean isMyAccountByAddress(Account address) {
        if (this.doesWalletExists()) {
            return this.wallet.accountExists(address);
        }
        return false;
    }

    public Tuple3<BigDecimal, BigDecimal, BigDecimal> getWalletUnconfirmedBalance(Account account, long key) {
        return this.wallet.getUnconfirmedBalance(account, key);
    }

    public String importAccountSeed(byte[] accountSeed) {
        return this.wallet.importAccountSeed(accountSeed);
    }

    public Tuple3<String, Integer, String> importPrivateKey(byte[] privateKey) {
        if (privateKey.length > 34) {
            // 64 bytes - from mobile
            return this.wallet.importPrivateKey(privateKey);
        } else {
            // as account pair SEED - 32 bytes
            return new Tuple3<>(this.wallet.importAccountSeed(privateKey), null, null);
        }
    }

    public byte[] exportAccountSeed(String address) {
        return this.wallet.exportAccountSeed(address);
    }

    public byte[] exportSeed() {
        return this.wallet.exportSeed();
    }

    /**
     * Check if wallet is unlocked
     *
     * @return bool value wallet unlock or not
     */
    public boolean isWalletUnlocked() {
        return this.wallet.isUnlocked();
    }

    public boolean isWalletUnlockedForRPC() {
        return this.wallet.isUnlockedForRPC();
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
            SysTray.getInstance().sendMessage(Lang.T("INCOMING API CALL"),
                    Lang.T("An API call needs authorization!"), MessageType.WARNING);
            Object[] options = {Lang.T("Yes"), Lang.T("No")};

            StringBuilder sb = new StringBuilder(Lang.T("Permission Request: "));
            sb.append(Lang.T("Do you want to authorize the following API call?\n\n") + json
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

            //gui = Gui.getInstance();
            gui.bringtoFront();

            result = JOptionPane.showOptionDialog(gui, jsp, Lang.T("INCOMING API CALL"),
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
        this.wallet.setSecondsToUnlock(3);
        return this.wallet.unlock(password);
    }

    public void setSecondsToUnlock(int seconds) {
        this.wallet.setSecondsToUnlock(seconds);
    }

    public List<Pair<Account, Transaction>> getLastWalletTransactions(int limit) {
        return this.wallet.getLastTransactions(limit);
    }

    public Transaction getTransaction(byte[] signature) {

        return getTransaction(signature, this.dcSet);
    }

    public Transaction getTransaction(Long dbREF) {

        return getTransaction(dbREF, this.dcSet);
    }

    // by account addres + timestamp get signature
    public long[] getSignatureByAddrTime(DCSet dcSet, String address, Long timestamp) {

        //return dcSet.getAddressTime_SignatureMap().get(address, timestamp);
        return dcSet.getReferenceMap().get(Account.makeShortBytes(address));
    }

    public Transaction getTransaction(byte[] signature, DCSet database) {

        // CHECK IF IN TRANSACTION DATABASE
        if (database.getTransactionTab().contains(signature)) {
            return database.getTransactionTab().get(signature);
        }
        // CHECK IF IN BLOCK
        Long tuple_Tx = database.getTransactionFinalMapSigns().get(signature);
        if (tuple_Tx != null) {
            return database.getTransactionFinalMap().get(tuple_Tx);
        }
        return null;
    }

    public Transaction getTransaction(long refDB, DCSet database) {
        return database.getTransactionFinalMap().get(refDB);
    }

    public Transaction getTransaction(String refDB) {
        return dcSet.getTransactionFinalMap().getRecord(refDB);
    }

    public List<Transaction> getLastWalletTransactions(Account account, int limit) {
        return this.wallet.getLastTransactions(account, limit);
    }

    public List<Pair<Account, Block.BlockHead>> getLastWalletBlocks(int limit) {
        return this.wallet.getLastBlocks(limit);
    }

    public List<Block.BlockHead> getLastWalletBlocks(Account account, int limit) {
        return this.wallet.getLastBlocks(account, limit);
    }

    public List<String> deleteTelegram(List<String> telegramSignatures) {
        return this.network.deleteTelegram(telegramSignatures);
    }

    public long deleteTelegramsToTimestamp(long timestamp, String recipient, String title) {
        return this.network.deleteTelegramsToTimestamp(timestamp, recipient, title);
    }

    public long deleteTelegramsForRecipient(String recipient, long timestamp, String title) {
        return this.network.deleteTelegramsForRecipient(recipient, timestamp, title);
    }

    public List<TelegramMessage> getLastIncomeTelegrams(Account account, long timestamp, String filter) {
        return this.network.getTelegramsForAddress(account.getAddress(), timestamp, filter);
    }

    public List<TelegramMessage> getLastTelegrams(long timestamp, String recipient, String filter, boolean outcomes) {
        return this.network.getTelegramsFromTimestamp(timestamp, recipient, filter, outcomes);
    }

    public TelegramMessage getTelegram(byte[] signature) {
        return this.network.getTelegram(signature);
    }


    public Integer TelegramInfo() {
        return this.network.TelegramInfo();
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

    public ItemMap getItemMap(int type) {
        switch (type) {
            case ItemCls.ASSET_TYPE:
                return this.dcSet.getItemAssetMap();
            case ItemCls.IMPRINT_TYPE:
                return this.dcSet.getItemImprintMap();
            case ItemCls.TEMPLATE_TYPE:
                return this.dcSet.getItemTemplateMap();
            case ItemCls.PERSON_TYPE:
                return this.dcSet.getItemPersonMap();
            case ItemCls.POLL_TYPE:
                return this.dcSet.getItemPollMap();
        }
        return null;
    }

    public void addAddressFavorite(String address, String pubKey, String name, String description) {
        this.wallet.addAddressFavorite(address, pubKey, name, description);
    }

    public void addTelegramToWallet(Transaction transaction, String signatureKey) {
        if (wallet == null || wallet.dwSet == null) {
            return;
        }

        HashSet<Account> recipients = transaction.getRecipientAccounts();
        PublicKeyAccount creator = transaction.getCreator();
        String creator58 = creator.getAddress();
        String creatorPubKey58 = creator.getBase58();
        for (Account recipient : recipients) {
            if (wallet.accountExists(recipient)) {
                wallet.dwSet.getTelegramsMap().add(signatureKey, transaction);
                if (!wallet.dwSet.getFavoriteAccountsMap().contains(creator58)) {
                    String title = transaction.getTitle();
                    String description = "";
                    if (transaction instanceof RSend) {
                        RSend rsend = ((RSend) transaction);
                        if (rsend.isText()) {
                            byte[] data = rsend.getData();
                            if (data != null && data.length > 0) {
                                if (rsend.isEncrypted()) {
                                    data = decrypt(creator, rsend.getRecipient(), data);
                                }
                                if (data != null && data.length > 0) {
                                    try {
                                        description = new String(data, "UTF-8");
                                    } catch (UnsupportedEncodingException e) {
                                    }
                                }
                            }
                        }
                    }
                    addAddressFavorite(creator58, creatorPubKey58,
                            title == null || title.isEmpty() ? "telegram" : title, description);
                    break;
                }
            }
        }
    }

    public void addItemFavorite(ItemCls item) {
        this.wallet.addItemFavorite(item);
    }

    public void removeItemFavorite(ItemCls item) {
        this.wallet.removeItemFavorite(item);
    }

    public boolean isItemFavorite(ItemCls item) {
        return this.wallet.isItemFavorite(item);
    }

    public void addTransactionFavorite(Transaction transaction) {
        this.wallet.addTransactionFavorite(transaction);
    }

    public void removeTransactionFavorite(Transaction transaction) {
        this.wallet.removeTransactionFavorite(transaction);
    }

    public boolean isTransactionFavorite(Transaction transaction) {
        return this.wallet.isTransactionFavorite(transaction);
    }

    public boolean isDocumentFavorite(Transaction transaction) {
        return this.wallet.isDocumentFavorite(transaction);
    }

    public void addDocumentFavorite(Transaction transaction) {
        this.wallet.addDocumentFavorite(transaction);
    }

    public void removeDocumentFavorite(Transaction transaction) {
        this.wallet.removeDocumentFavorite(transaction);
    }

    public Collection<ItemCls> getAllItems(int type) {
        return getItemMap(type).values();
    }

    public Collection<ItemCls> getAllItems(int type, Account account) {
        return getItemMap(type).values();
    }

    public BlockGenerator getBlockGenerator() {
        return this.blockGenerator;
    }

    public void BlockGeneratorCacheAccounts() {
        this.blockGenerator.cacheKnownAccounts();
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

    public byte[] getLastBlockSignature() {
        byte[] signature = this.blockChain.getLastBlockSignature(dcSet);
        if (signature == null)
            return this.blockChain.getGenesisBlock().getSignature();
        return signature;
    }

    public byte[] getLastWalletBlockSign() {
        return this.wallet.getLastBlockSignature();
    }

    public Block getBlock(byte[] header) {
        return this.blockChain.getBlock(dcSet, header);
    }

    public Block.BlockHead getBlockHead(int height) {
        return this.dcSet.getBlocksHeadsMap().get(height);
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

        try {
            // if last block is changed by core.Synchronizer.process(DLSet, Block)
            // clear this win block
            if (!Arrays.equals(dcSet.getBlockMap().getLastBlockSignature(), newBlock.getReference())) {
                // see finalliy newBlock.close();
                return false;
            }

            LOGGER.info("+++ flushNewBlockGenerated TRY flush chainBlock: " + newBlock.toString());

            if (!newBlock.isValidated()) {
                // это может случиться при добавлении в момент синхронизации - тогда до расчета Победы не доходит
                // или при добавлении моего сгнерированного блока т.к. он не проверился?

                // создаем в памяти базу - так как она на 1 блок только нужна - а значит много памяти не возьмет
                DCSet forked = dcSet.fork(DCSet.makeDBinMemory(), "flushNewBlockGenerated");
                // в процессингом сразу делаем - чтобы потом изменения из форка залить сразу в цепочку

                if (newBlock.isValid(forked,
                        onlyProtocolIndexing // если вторичные индексы нужны то нельзя быстрый просчет - иначе вторичные при сиве из форка не создадутся
                ) > 0) {
                    // очищаем занятые транзакциями ресурсы
                    // - see finalliy newBlock.close();

                    // освобождаем память от базы данных - так как мы ее к блоку не привязали
                    forked.close();
                    return false;
                }

                // если вторичные индексы нужны то нельзя быстрый просчет - иначе вторичные при сиве из форка не создадутся
                if (onlyProtocolIndexing) {
                    // запоним что в этой базе проверку сделали с Процессингом чтобы потом быстро слить в основную базу
                    newBlock.setValidatedForkDB(forked);
                } else {
                    // освобождаем память от базы данных - так как мы ее к блоку не привязали
                    forked.close();
                }
            }

            try {
                this.synchronizer.pipeProcessOrOrphan(this.dcSet, newBlock, false, true, false);

            } catch (Exception e) {
                if (this.isOnStopping()) {
                    throw new Exception("on stoping");
                } else {
                    LOGGER.error(e.getMessage(), e);
                    return false;
                }
            }
            if (network != null) {
                this.network.clearHandledWinBlockMessages();
            }
        } finally {
            newBlock.close();
        }

        LOGGER.info("+++ flushNewBlockGenerated OK");

        /// logger.info("and broadcast it");

        if (network != null) {
            // broadcast my HW
            broadcastHWeightFull();
        }
        return true;

    }

    public List<Transaction> getUnconfirmedTransactions(int count, boolean descending) {
        return this.dcSet.getTransactionTab().getTransactions(count, descending);

    }

    // BALANCES

    public List<Tuple2<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>>
    getBalances(
            long key) {
        return this.dcSet.getAssetBalanceMap().getBalancesList(key);
    }

    public List<Tuple2<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>>
    getBalances(
            Account account) {

        return this.dcSet.getAssetBalanceMap().getBalancesList(account);
    }

    public List<Transaction> getUnconfirmedTransactionsByAddressFast100(String address) {
        return this.dcSet.getTransactionTab().getTransactionsByAddressFast100(address);
    }

    // ASSETS

    public AssetCls getAsset(long key) {
        return this.dcSet.getItemAssetMap().get(key);
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

    public List<Order> getOrders(Long have, Long want) {

        return dcSet.getOrderMap().getOrdersForTrade(have, want, false);
    }

    public List<Order> getOrdersByTimestamp(long have, long want, long timestamp, int limit) {
        return dcSet.getCompletedOrderMap().getOrdersByTimestamp(have, want, timestamp, 0, limit);
    }

    public List<Order> getOrdersByOrderID(long have, long want, long orderID, int limit) {
        return dcSet.getCompletedOrderMap().getOrdersByOrderID(have, want, orderID, 0, limit);
    }

    public List<Order> getOrdersByHeight(long have, long want, int height, int limit) {
        // так как там обратный отсчет
        return dcSet.getCompletedOrderMap().getOrdersByHeight(have, want, height, 0, limit);
    }

    public List<Trade> getTradeByTimestamp(long timestamp, int limit) {
        return dcSet.getTradeMap().getTradesByTimestamp(timestamp, 0, limit);
    }

    public List<Trade> getTradeByTimestamp(long have, long want, long timestamp, int limit) {
        return dcSet.getTradeMap().getTradesByTimestamp(have, want, timestamp, 0, limit);
    }

    public List<Trade> getTradesFromTradeID(long[] tradeID, int limit) {
        return dcSet.getTradeMap().getTradesFromTradeID(tradeID, limit);
    }

    /**
     * @param fromOrderID
     * @param toOrderID   0 - ALL to end
     * @param limit
     * @param useCancel
     * @return
     */
    public List<Trade> getTradeFromToOrderID(long fromOrderID, long toOrderID, int limit, boolean useCancel) {
        return dcSet.getTradeMap().getTradesFromToOrderID(fromOrderID, toOrderID, limit, useCancel);
    }

    /**
     * @param have
     * @param want
     * @param fromOrderID
     * @param toOrderID   0 - ALL to and
     * @param limit
     * @param useCancel
     * @return
     */
    public List<Trade> getTradeFromToOrderID(long have, long want, long fromOrderID, long toOrderID, int limit, boolean useCancel) {
        return dcSet.getTradeMap().getTradesFromToOrderID(have, want, fromOrderID, toOrderID, limit, useCancel);
    }

    /**
     * @param fromHeight
     * @param toHeight   0 - ALL to and
     * @param limit
     * @return
     */
    public List<Trade> getTradeFromToHeight(int fromHeight, int toHeight, int limit) {
        // так как там обратный отсчет
        return dcSet.getTradeMap().getTradesFromToHeight(fromHeight, toHeight, limit);
    }

    /**
     * @param have
     * @param want
     * @param fromHeight
     * @param toHeight   0 - ALL to and
     * @param limit
     * @return
     */
    public List<Trade> getTradeFromToHeight(long have, long want, int fromHeight, int toHeight, int limit) {
        // так как там обратный отсчет
        return dcSet.getTradeMap().getTradesFromToHeight(have, want, fromHeight, toHeight, limit);
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
            case ItemCls.ASSET_TYPE:
                return db.getItemAssetMap().get(key);
            case ItemCls.IMPRINT_TYPE:
                return db.getItemImprintMap().get(key);
            case ItemCls.TEMPLATE_TYPE:
                return db.getItemTemplateMap().get(key);
            case ItemCls.PERSON_TYPE:
                return db.getItemPersonMap().get(key);
            case ItemCls.POLL_TYPE:
                return db.getItemPollMap().get(key);
            case ItemCls.STATUS_TYPE:
                return db.getItemStatusMap().get(key);
            case ItemCls.UNION_TYPE:
                return db.getItemUnionMap().get(key);
        }
        return null;
    }

    public ItemCls getItem(int type, long key) {
        return this.getItem(this.dcSet, type, key);
    }

    // ATs

    public List<AT> getAcctATs(String type, boolean initiators) {
        return this.dcSet.getATMap().getAcctATs(type, initiators);
    }

    // TRANSACTIONS

    public void onTransactionCreate(Transaction transaction) {

        // CLEAR ALL LOCAL DATA
        transaction = transaction.copy();

        // ADD TO UNCONFIRMED TRANSACTIONS
        this.transactionsPool.offerMessage(transaction);

        // BROADCAST
        this.broadcastTransaction(transaction);

        // ADD TO WALLET TRANSACTIONS
        if (doesWalletExists() && HARD_WORK < 4) {
            wallet.processTransaction(transaction);
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

    public Pair<Transaction, Integer> createPollVote(PrivateKeyAccount creator, org.erachain.core.voting.Poll poll, PollOption option,
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

    public Fun.Tuple3<Transaction, Integer, String> parseAndCheck(String rawDataStr, boolean base64, boolean andCheck) {
        byte[] transactionBytes;

        int step = 1;
        try {
            if (base64) {
                transactionBytes = Base64.getDecoder().decode(rawDataStr);
            } else {
                step++;
                transactionBytes = Base58.decode(rawDataStr);
            }
        } catch (Exception e) {
            return new Fun.Tuple3<>(null, -1, "Base" + (step == 2 ? "58" : "64") + " decode: " + e.getMessage());
        }

        if (andCheck)
            return Controller.getInstance().lightCreateTransactionFromRaw(transactionBytes, true);

        try {
            Transaction transaction = TransactionFactory.getInstance().parse(transactionBytes, Transaction.FOR_NETWORK);
            return new Tuple3<Transaction, Integer, String>(transaction, null, null);
        } catch (Exception e) {
            return new Tuple3<Transaction, Integer, String>(null, Transaction.INVALID_RAW_DATA, e.getMessage());
        }

    }

    public Tuple3<Transaction, Integer, String> lightCreateTransactionFromRaw(byte[] rawData, boolean notRelease) {

        // CREATE TRANSACTION FROM RAW
        Transaction transaction;
        try {
            transaction = TransactionFactory.getInstance().parse(rawData, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            return new Tuple3<Transaction, Integer, String>(null, Transaction.INVALID_RAW_DATA, e.getMessage());
        }

        // CHECK IF RECORD VALID
        if (!transaction.isSignatureValid(DCSet.getInstance()))
            return new Tuple3<Transaction, Integer, String>(transaction, Transaction.INVALID_SIGNATURE, null);

        // CHECK FOR UPDATES
        int valid = this.transactionCreator.afterCreateRaw(transaction, Transaction.FOR_NETWORK, 0L, notRelease);
        if (valid == Transaction.VALIDATE_OK)
            return new Tuple3<Transaction, Integer, String>(transaction, valid, null);

        return new Tuple3<Transaction, Integer, String>(transaction, valid, transaction.errorValue);

    }

    public Tuple3<Transaction, Integer, String> checkTransaction(Transaction transaction) {

        // CHECK IF RECORD VALID
        if (!transaction.isSignatureValid(DCSet.getInstance()))
            return new Tuple3<Transaction, Integer, String>(transaction, Transaction.INVALID_SIGNATURE, null);

        // CHECK FOR UPDATES
        int valid = this.transactionCreator.afterCreateRaw(transaction, Transaction.FOR_NETWORK, 0l, false);
        if (valid != Transaction.VALIDATE_OK)
            return new Tuple3<Transaction, Integer, String>(transaction, valid, transaction.errorValue);

        return new Tuple3<Transaction, Integer, String>(transaction, valid, null);

    }

    public Object issueAsset(HttpServletRequest request, String x) {

        Object result = Transaction.decodeJson(null, x);
        if (result instanceof JSONObject) {
            return result;
        }

        Fun.Tuple5<Account, Integer, ExLink, String, JSONObject> resultHead = (Fun.Tuple5<Account, Integer, ExLink, String, JSONObject>) result;
        Account creator = resultHead.a;
        int feePow = resultHead.b;
        ExLink linkTo = resultHead.c;
        String password = resultHead.d;
        JSONObject jsonObject = resultHead.e;

        if (jsonObject == null) {
            int error = ApiErrorFactory.ERROR_JSON;
            return new Fun.Tuple2<>(error, OnDealClick.resultMess(error));
        }

        String name = (String) jsonObject.get("name");
        String description = (String) jsonObject.get("description");

        byte[] icon;
        String icon64 = (String) jsonObject.get("icon64");
        if (icon64 == null) {
            String icon58 = (String) jsonObject.get("icon");
            if (icon58 == null)
                icon = null;
            else
                icon = Base58.decode(icon58);
        } else {
            icon = java.util.Base64.getDecoder().decode(icon64);
        }

        byte[] image;
        String image64 = (String) jsonObject.get("image64");
        if (image64 == null) {
            String image58 = (String) jsonObject.get("image");
            if (image58 == null)
                image = null;
            else
                image = Base58.decode(image58);
        } else {
            image = java.util.Base64.getDecoder().decode(image64);
        }

        Integer scale = null;
        Integer assetType = null;
        Long quantity = null;
        int error;
        String errorName = null;
        try {
            errorName = "scale: -8...24";
            scale = (Integer) jsonObject.getOrDefault("scale", 0);
            errorName = "assetType: int";
            assetType = (Integer) jsonObject.getOrDefault("assetType", 0);
            errorName = "quantity: long";
            quantity = (Long) jsonObject.getOrDefault("quantity", 0L);
        } catch (Exception e) {
            error = ApiErrorFactory.ERROR_JSON;
            JSONObject out = new JSONObject();
            out.put("error", error);
            out.put("error_message", errorName);
        }

        APIUtils.askAPICallAllowed(password, "POST issue Asset " + name, request, true);
        PrivateKeyAccount creatorPrivate = getWalletPrivateKeyAccountByAddress(creator);

        int profitTaxMin = 0;
        int profitTaxMax = 0;
        int profitFee = 0;
        int loanInterest = 0;

        return issueAsset(null, creatorPrivate, linkTo, profitTaxMin, profitTaxMax, profitFee, loanInterest,
                name, description, icon, image, scale,
                assetType, quantity, feePow);

    }

    public Transaction issueAsset(PrivateKeyAccount creator, ExLink linkTo, int feePow, AssetCls asset) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueAssetTransaction(creator, linkTo, asset, feePow);
        }
    }

    //public Transaction issueAsset(PrivateKeyAccount creator, String name, String description, byte[] icon, byte[] image,
    //                              int scale, int assetType, long quantity, int feePow) {
    public Transaction issueAsset(byte[] appData, PrivateKeyAccount creator, ExLink linkTo, int profitTaxMin, int profitTaxMax,
                                  int profitFee, long loanInterest,
                                  String name, String description, byte[] icon, byte[] image,
                                  int scale, int asset_type, long quantity, int feePow) {

        AssetCls asset = new AssetVenture(appData, creator, name, icon, image, description, asset_type, scale, quantity);

        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueAssetTransaction(creator, linkTo, asset, feePow);
        }
    }

    public Transaction issueImprint1(byte[] itemAppData, PrivateKeyAccount creator, ExLink exLink, String name, String description, byte[] icon,
                                     byte[] image, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueImprintTransaction1(itemAppData, creator, exLink, name, description, icon, image,
                    feePow);
        }
    }

    public Pair<Transaction, Integer> issuePersonHuman(boolean forIssue, byte[] itemAppData, PrivateKeyAccount creator, ExLink linkTo, String fullName,
                                                       int feePow, long birthday, long deathday, byte gender, String race, float birthLatitude,
                                                       float birthLongitude, String skinColor, String eyeColor, String hairСolor, int height, byte[] icon,
                                                       byte[] image, String description, PublicKeyAccount owner, byte[] ownerSignature) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssuePersonHumanTransaction(forIssue, itemAppData, creator, linkTo, fullName, feePow, birthday,
                    deathday, gender, race, birthLatitude, birthLongitude, skinColor, eyeColor, hairСolor, height, icon,
                    image, description, owner, ownerSignature);
        }
    }

    public Object issuePersonHuman(HttpServletRequest request, String x) {

        JSONObject out = new JSONObject();

        Object result = Transaction.decodeJson(null, x);
        if (result instanceof JSONObject) {
            return result;
        }

        int error;
        // creator, feePow, linkTo, password, jsonObject
        Fun.Tuple5<Account, Integer, ExLink, String, JSONObject> transactionResult = (Fun.Tuple5<Account, Integer, ExLink, String, JSONObject>) result;
        Account creator = transactionResult.a;
        int feePow = transactionResult.b;
        ExLink linkTo = transactionResult.c;
        String password = transactionResult.d;
        JSONObject jsonObject = transactionResult.e;

        if (jsonObject == null) {
            Transaction.updateMapByErrorSimple(ApiErrorFactory.ERROR_JSON, out);
            return out;
        }

        String name = (String) jsonObject.get("name");
        String description = (String) jsonObject.get("description");

        byte[] icon;
        String icon64 = (String) jsonObject.get("icon64");
        if (icon64 == null) {
            String icon58 = (String) jsonObject.get("icon");
            if (icon58 == null)
                icon = null;
            else
                icon = Base58.decode(icon58);
        } else {
            icon = java.util.Base64.getDecoder().decode(icon64);
        }

        byte[] image;
        String image64 = (String) jsonObject.get("image64");
        if (image64 == null) {
            String image58 = (String) jsonObject.get("image");
            if (image58 == null)
                image = null;
            else
                image = Base58.decode(image58);
        } else {
            image = java.util.Base64.getDecoder().decode(image64);
        }

        long birthday = 0;
        long deathday = 0;
        byte gender = 2;
        String race = null;
        float birthLatitude = 0.0f;
        float birthLongitude = 0.0f;
        String skinColor = null;
        String eyeColor = null;
        String hairСolor = null;
        int height = 170;
        String owner58 = null;
        PublicKeyAccount owner = null;
        String ownerSignature58 = null;
        byte[] ownerSignature = null;

        APIUtils.askAPICallAllowed(password, "POST issue Person " + name, request, true);
        PrivateKeyAccount creatorPrivate = getWalletPrivateKeyAccountByAddress(creator);
        if (creatorPrivate == null) {
            Transaction.updateMapByErrorSimple(Transaction.INVALID_CREATOR, out);
            return out;
        }

        String errorName = null;
        try {
            errorName = "birthday";
            birthday = (long) (Long) jsonObject.getOrDefault("birthday", 0L);
            errorName = "deathday";
            Long deathdayLong = (Long) jsonObject.get("deathday");
            if (deathdayLong == null) {
                deathday = birthday - 1;
            } else {
                birthday = deathdayLong;
            }

            errorName = "gender - man:0, wimen:1, none:2";
            gender = (byte) (int) (long) (Long) jsonObject.get("gender");

            errorName = "birthLatitude: float";
            birthLatitude = (float) (double) (Double) jsonObject.getOrDefault("birthLatitude", 0.0f);
            errorName = "birthLongitude: float";
            birthLongitude = (float) (double) (Double) jsonObject.getOrDefault("birthLongitude", 0.0f);

            errorName = "height: 10..250";
            height = (int) (long) (Long) jsonObject.get("height");

            race = (String) jsonObject.get("race");
            skinColor = (String) jsonObject.get("skinColor");
            eyeColor = (String) jsonObject.get("eyeColor");
            hairСolor = (String) jsonObject.get("hairСolor");

            owner58 = (String) jsonObject.get("owner");
            if (owner58 == null) {
                owner = new PublicKeyAccount(creatorPrivate.getPublicKey());
            } else {
                errorName = "owner: Base58";
                owner = new PublicKeyAccount(owner58);

                ownerSignature58 = (String) jsonObject.get("ownerSignature");
                errorName = "ownerSignature: Base58";
                ownerSignature = Base58.decode(ownerSignature58);
            }

        } catch (Exception e) {
            error = ApiErrorFactory.ERROR_JSON;
            Transaction.updateMapByErrorValue(ApiErrorFactory.ERROR_JSON, errorName, out);
            return out;
        }

        PersonHuman person = new PersonHuman(null, owner, name, birthday, deathday, gender,
                race, birthLatitude, birthLongitude,
                skinColor, eyeColor, hairСolor, height, icon, image, description,
                ownerSignature);

        return issuePersonHuman(creatorPrivate, linkTo, feePow, person);

    }

    public Pair<Transaction, Integer> issuePersonHuman(PrivateKeyAccount creator, ExLink linkTo, int feePow, PersonCls person) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssuePersonHumanTransaction(creator, linkTo, feePow, person);
        }
    }

    public Transaction issuePerson(PrivateKeyAccount creator, ExLink linkTo, int feePow, PersonCls person) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssuePersonTransaction(creator, linkTo, feePow, person);
        }
    }

    public Transaction issuePoll(PrivateKeyAccount creator, ExLink linkTo, int feePow, PollCls poll) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssuePollTransaction(creator, linkTo, feePow, poll);
        }
    }

    public Transaction issuePoll(byte[] itemAppData, PrivateKeyAccount creator, ExLink linkTo, String name, String description, List<String> options,
                                 byte[] icon, byte[] image, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssuePollTransaction(itemAppData, creator, linkTo, name, description, icon, image, options,
                    feePow);
        }
    }

    public Transaction issueStatus(PrivateKeyAccount creator, ExLink linkTo, int feePow, StatusCls status) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueStatusTransaction(creator, linkTo, feePow, status);
        }
    }

    public Transaction issueStatus(byte[] itemAppData, PrivateKeyAccount creator, ExLink linkTo, String name, String description, boolean unique,
                                   byte[] icon, byte[] image, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueStatusTransaction(itemAppData, creator, linkTo, name, description, icon, image, unique,
                    feePow);
        }
    }

    public Transaction issueTemplate(PrivateKeyAccount creator, ExLink linkTo, int feePow, TemplateCls template) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueTemplateTransaction(creator, linkTo, feePow, template);
        }
    }

    public Transaction issueTemplate(byte[] itemAppData, PrivateKeyAccount creator, ExLink linkTo, String name, String description, byte[] icon,
                                     byte[] image, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueTemplateTransaction(itemAppData, creator, linkTo, name, description, icon, image,
                    feePow);
        }
    }

    public Transaction issueUnion(byte[] itemAppData, PrivateKeyAccount creator, ExLink linkTo, String name, long birthday, long parent,
                                  String description, byte[] icon, byte[] image, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createIssueUnionTransaction(itemAppData, creator, linkTo, name, birthday, parent, description,
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
        Transaction orderCreate = this.dcSet.getTransactionFinalMap().get(order.getId());
        return cancelOrder(creator, orderCreate.getSignature(), feePow);
    }

    public Pair<Transaction, Integer> cancelOrder(PrivateKeyAccount creator, byte[] orderID, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createCancelOrderTransaction(creator, orderID, feePow);
        }
    }

    public Transaction cancelOrder1(PrivateKeyAccount creator, Order order, int feePow) {
        Transaction orderCreate = this.dcSet.getTransactionFinalMap().get(order.getId());
        return cancelOrder1(creator, orderCreate.getSignature(), feePow);
    }

    public Transaction cancelOrder1(PrivateKeyAccount creator, byte[] orderID, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createCancelOrderTransaction1(creator, orderID, feePow);
        }
    }

    public Transaction cancelOrder2(PrivateKeyAccount creator, Long orderID, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createCancelOrderTransaction2(creator, orderID, feePow);
        }
    }

    public Transaction cancelOrder2(PrivateKeyAccount creator, byte[] orderID, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createCancelOrderTransaction2(creator, orderID, feePow);
        }
    }

    public Transaction changeOrder(PrivateKeyAccount creator, int feePow, Order order, BigDecimal wantAmount) {
        Transaction orderCreate = this.dcSet.getTransactionFinalMap().get(order.getId());
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createChangeOrderTransaction(creator, feePow, orderCreate.getSignature(), wantAmount);
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

    public Pair<Integer, Transaction> make_R_Send(String creatorStr, Account creator, ExLink linkTo, String recipientStr,
                                                  int feePow, long assetKey, boolean checkAsset, BigDecimal amount, boolean needAmount,
                                                  String title, String message, int messagecode, boolean encrypt, long timestamp) {

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

        if (needAmount && (amount == null || amount.signum() == 0)) {
            return new Pair<Integer, Transaction>(Transaction.INVALID_AMOUNT_IS_NULL, null);
        }

        //long assetKey = 0;
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) == 0)
                amount = null;
            else {
                // PARSE asset Key
                if (assetKey == 0) {
                    assetKey = 2;
                } else {

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
            return new Pair<Integer, Transaction>(Transaction.INVALID_TITLE_LENGTH_MAX, null);
        }

        byte[] messageBytes = null;

        if (message != null && message.length() > 0) {
            if (messagecode == 0) {
                messageBytes = message.getBytes(StandardCharsets.UTF_8);
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

        PrivateKeyAccount privateKeyAccount = cnt.getWalletPrivateKeyAccountByAddress(creator.getAddress());
        if (privateKeyAccount == null) {
            return new Pair<Integer, Transaction>(Transaction.INVALID_WALLET_ADDRESS, null);
        }

        byte[] encrypted = (encrypt) ? new byte[]{1} : new byte[]{0};
        byte[] isTextByte = (messagecode == 0) ? new byte[]{1} : new byte[]{0};

        if (messageBytes != null) {
            if (messageBytes.length > BlockChain.MAX_REC_DATA_BYTES) {
                return new Pair<Integer, Transaction>(Transaction.INVALID_MESSAGE_LENGTH, null);
            }

            if (encrypt) {
                // recipient
                byte[] publicKey = cnt.getPublicKeyByAddress(recipient.getAddress());
                if (publicKey == null) {
                    return new Pair<Integer, Transaction>(Transaction.UNKNOWN_PUBLIC_KEY_FOR_ENCRYPT, null);
                }

                // sender
                byte[] privateKey = privateKeyAccount.getPrivateKey();

                messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
            }
        }

        // CREATE RSend
        return new Pair<Integer, Transaction>(Transaction.VALIDATE_OK, this.r_Send(privateKeyAccount, linkTo, feePow, recipient,
                assetKey, amount, title, messageBytes, isTextByte, encrypted, timestamp));

    }

    public Transaction r_Send(PrivateKeyAccount sender, ExLink linkTo, int feePow,
                              Account recipient, long key, BigDecimal amount, String title, byte[] message, byte[] isText,
                              byte[] encryptMessage, long timestamp) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_Send(sender, linkTo, recipient, key, amount, feePow, title, message, isText,
                    encryptMessage, timestamp);
        }
    }

    public Transaction r_Send(byte version, byte property1, byte property2,
                              PrivateKeyAccount sender, ExLink linkTo, int feePow,
                              Account recipient, long key, BigDecimal amount, String title, byte[] message, byte[] isText,
                              byte[] encryptMessage) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_Send(version, property1, property2, sender, recipient, key, amount, linkTo, feePow,
                    title, message, isText, encryptMessage);
        }
    }

    public Transaction r_SignNote(byte version, byte property1, byte property2,
                                  PrivateKeyAccount sender, int feePow, long key, byte[] message) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_SignNote(version, property1, property2, sender, feePow, key,
                    message);
        }
    }

    public Transaction r_CertifyPubKeysPerson(int version, PrivateKeyAccount creator, ExLink linkTo,
                                              int feePow, long key, PublicKeyAccount publicKey, int add_day) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_CertifyPubKeysPerson(version, creator, linkTo, feePow, key, publicKey,
                    add_day);
        }
    }

    public Transaction r_CertifyPubKeysPerson(int version, int forDeal, PrivateKeyAccount creator, int feePow, long key,
                                              List<PublicKeyAccount> userAccounts, int add_day) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_CertifyPubKeysPerson(version, forDeal, creator, feePow, key, userAccounts,
                    add_day);
        }
    }

    public Transaction r_Vouch(int version, int forDeal, PrivateKeyAccount creator, int feePow, int height,
                               int seq) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_Vouch(version, forDeal, creator, feePow, height, seq);
        }
    }

    public Transaction r_Hashes(PrivateKeyAccount sender, ExLink exLink, int feePow, String url, String data,
                                String hashes) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_Hashes(sender, exLink, feePow, url, data, hashes);
        }
    }

    public Transaction r_Hashes(PrivateKeyAccount sender, ExLink exLink, int feePow, String url, String data,
                                String[] hashes) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_Hashes(sender, exLink, feePow, url, data, hashes);
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
        return db.getBlockMap().getAndProcess(parseInt);
    }

    public Block getBlockByHeight(int parseInt) {
        return getBlockByHeight(this.dcSet, parseInt);
    }

    public byte[] getPublicKey(Account account) {

        // CHECK ACCOUNT IN OWN WALLET
        if (isMyAccountByAddress(account)) {
            if (isWalletUnlocked()) {
                return getWalletPrivateKeyAccountByAddress(account.getAddress()).getPublicKey();
            }
        }

        long[] makerLastTimestamp = account.getLastTimestamp(dcSet);
        if (makerLastTimestamp == null) {
            return null;
        }

        Transaction transaction = getTransaction(makerLastTimestamp[1]);
        if (transaction != null) {

            if (transaction.getCreator().equals(account))
                return transaction.getCreator().getPublicKey();
            else {
                List<PublicKeyAccount> pKeys = transaction.getPublicKeys();
                if (pKeys != null) {
                    for (PublicKeyAccount pKey : pKeys) {
                        if (pKey.equals(account)) {
                            return pKey.getPublicKey();
                        }
                    }
                }
            }
        }

        return null;
    }

    public byte[] getPublicKeyByAddress(String address) {

        if (!Crypto.getInstance().isValidAddress(address)) {
            return null;
        }

        if (wallet != null && wallet.dwSet != null) {
            Tuple3<String, String, String> favorite = wallet.dwSet.getFavoriteAccountsMap().get(address);
            if (favorite != null && favorite.a != null) {
                return Base58.decode(favorite.a);
            }

            // CHECK ACCOUNT IN OWN WALLET
            Account account = getWalletAccountByAddress(address);
            if (account != null) {
                if (isWalletUnlocked()) {
                    return getWalletPrivateKeyAccountByAddress(address).getPublicKey();
                }
            }
        }

        long[] makerLastTimestamp = dcSet.getReferenceMap().get(Account.makeShortBytes(address));
        if (makerLastTimestamp != null) {

            Transaction transaction = getTransaction(makerLastTimestamp[1]);
            if (transaction == null) {
                return null;
            }

            if (transaction.getCreator() != null // если последняя это ГЕНЕСИЗ транзакция - такое тоже бывает
                    // например дали вдолг на счет. А потом по счету определяем публичный ключ - она не найдет
                    && transaction.getCreator().equals(address))
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
        }

        return null;
    }

    @Override
    public void addObserver(Observer o) {

        this.dcSet.getBlockMap().addObserver(o);
        this.dcSet.getTransactionTab().addObserver(o);
        this.dcSet.getTransactionFinalMap().addObserver(o);

        if (this.dcSetWithObserver) {
            // ADD OBSERVER TO SYNCHRONIZER
            // this.synchronizer.addObserver(o);

            // ADD OBSERVER TO BLOCKGENERATOR
            // this.blockGenerator.addObserver(o);

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

    public void addSingleObserver(Observer o) {
        super.addObserver(o);
    }

    public void deleteSingleObserver(Observer o) {
        super.deleteObserver(o);
    }

    public void addWalletObserver(Observer o) {
        if (this.wallet != null)
            this.wallet.addObserver(o);
        if (this.guiTimer != null)
            this.guiTimer.addObserver(o); // обработка repaintGUI
    }

    public void deleteWalletObserver(Observer o) {
        if (this.guiTimer != null)
            this.guiTimer.deleteObserver(o); // нужно для перерисовки раз в 2 сек
        if (this.wallet != null)
            this.wallet.deleteObserver(o);
    }

    public void addWalletFavoritesObserver(Observer o) {
        this.wallet.addFavoritesObserver(o);
        this.guiTimer.addObserver(o); // обработка repaintGUI
    }

    public void deleteWalletFavoritesObserver(Observer o) {
        this.guiTimer.deleteObserver(o); // нужно для перерисовки раз в 2 сек
        this.wallet.deleteObserver(o);
    }

    public void startApplication(String args[]) {
        boolean cli = false;

        // get GRADLE bild time
        getManifestInfo();

        if (buildTimestamp == 0)
            // get local file time
            getBuildTimestamp();

        String pass = null;

        // init BlockChain then
        String log4JPropertyFile = "resources/log4j" + (BlockChain.TEST_MODE ? "-test" : "") + ".properties";
        Properties p = new Properties();

        try {
            p.load(new FileInputStream(log4JPropertyFile));
            PropertyConfigurator.configure(p);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        // default
        databaseSystem = DCSet.DBS_MAP_DB;

        for (String arg : args) {

            if (arg.equals("-cli")) {
                cli = true;
                useGui = false;
                continue;
            }

            if (arg.toLowerCase().equals("-nocalculated")) {
                noCalculated = true;
                continue;
            }

            if (arg.toLowerCase().equals("-nousewallet")) {
                noUseWallet = true;
                continue;
            }

            if (arg.toLowerCase().equals("-nodatawallet")) {
                noDataWallet = true;
                continue;
            }

            if (arg.toLowerCase().equals("-opi")) {
                onlyProtocolIndexing = true;
                continue;
            }

            if (arg.toLowerCase().equals("-compactdc")) {
                compactDConStart = true;
                continue;
            }

            if (arg.toLowerCase().equals("-inmemory")) {
                inMemoryDC = true;
                continue;
            }

            if (arg.equals("-backup")) {
                // backUP data
                backUP = true;
                continue;
            }

            if (arg.equals("-nogui")) {
                useGui = false;
                continue;
            }

            if (arg.startsWith("-hardwork=") && arg.length() > 10) {
                try {
                    int hartWork = Integer.parseInt(arg.substring(10));

                    if (hartWork > 12) {
                        hartWork = 12;
                    }
                    if (hartWork > 0) {
                        HARD_WORK = hartWork;
                    }
                } catch (Exception e) {
                }
                continue;
            }

            if (arg.startsWith("-cache=") && arg.length() > 7) {
                CACHE_DC = arg.substring(7).toLowerCase();
                LOGGER.info("-cache set to [" + CACHE_DC + "]");
                continue;
            }

            if (arg.startsWith("-dbschain=") && arg.length() > 10) {
                try {
                    String dbsChain = arg.substring(10).toLowerCase();

                    if (dbsChain.equals("rocksdb")) {
                        databaseSystem = DCSet.DBS_ROCK_DB;
                    } else if (dbsChain.equals("mapdb")) {
                        databaseSystem = DCSet.DBS_MAP_DB;
                    } else if (dbsChain.equals("fast")) {
                        databaseSystem = DCSet.DBS_FAST;
                    }

                } catch (Exception e) {
                }
                continue;
            }

            if (arg.startsWith("-datachainpath=")) {
                try {
                    String datachainPath = arg.substring(15);
                    Settings.getInstance().setDataChainPath(datachainPath);
                    LOGGER.info("-datachain path = " + datachainPath);
                } catch (Exception e) {
                }
                continue;
            }

            // TESTS
            if (BlockChain.TEST_DB > 0) {
                useGui = false;
                onlyProtocolIndexing = true;
                noUseWallet = true;
                noCalculated = true;
                HARD_WORK = 6;
                continue;
            }

            if (arg.startsWith("-seed=") && arg.length() > 6) {
                seedCommand = arg.substring(6).split(":");
                continue;
            }
            if (arg.startsWith("-pass=") && arg.length() > 6) {
                pass = arg.substring(6);
                continue;
            }
            if (arg.startsWith("-peers=") && arg.length() > 7) {
                Settings.getInstance().setDefaultPeers(arg.substring(7).split(","));
                continue;
            }
            if (arg.equals("-nonet")) {
                useNet = false;
                continue;
            }

            if (arg.startsWith("-web=") && arg.length() > 5) {
                String value = arg.substring(5).toLowerCase();
                if (value.equals("on")) {
                    Settings.getInstance().updateJson("webenabled", true);
                } else if (value.equals("off")) {
                    Settings.getInstance().updateJson("webenabled", false);
                }
                continue;
            }
            // * - all
            if (arg.startsWith("-weballowed=") && arg.length() > 12) {
                JSONArray list = new JSONArray();
                if (!arg.substring(12).equals("*")) {
                    String[] array = arg.substring(12).split(",");
                    for (String ip : array) {
                        list.add(ip);
                    }
                }
                Settings.getInstance().updateJson("weballowed", list);
                continue;
            }
            if (arg.startsWith("-webport=") && arg.length() > 9) {
                Long value = new Long(arg.substring(9));
                Settings.getInstance().updateJson("webport", value);
                continue;
            }
            if (arg.startsWith("-rpc=") && arg.length() > 5) {
                String value = arg.substring(5).toLowerCase();
                if (value.equals("on")) {
                    Settings.getInstance().updateJson("rpcenabled", true);
                } else if (value.equals("off")) {
                    Settings.getInstance().updateJson("rpcenabled", false);
                }
                continue;
            }
            // * - all
            if (arg.startsWith("-rpcallowed=") && arg.length() > 12) {
                JSONArray list = new JSONArray();
                if (!arg.substring(12).equals("*")) {
                    String[] array = arg.substring(12).split(",");
                    for (String ip : array) {
                        list.add(ip);
                    }
                }
                Settings.getInstance().updateJson("rpcallowed", list);
                continue;
            }
            if (arg.startsWith("-rpcport=") && arg.length() > 9) {
                Long value = new Long(arg.substring(9));
                Settings.getInstance().updateJson("rpcport", value);
                continue;
            }

        }

        if (Settings.genesisStamp <= 0) {
            if (Settings.genesisStamp < 0) {
                useNet = false;
            }
            Settings.genesisStamp = NTP.getTime() - (BlockChain.GENERATING_MIN_BLOCK_TIME_MS(1) << 1);
        }

        // default DataBase System
        if (databaseSystem < 0) {
            databaseSystem = DCSet.DBS_MAP_DB;
        }

        if (noCalculated)
            LOGGER.info("-not store calculated TXs");

        if (onlyProtocolIndexing)
            LOGGER.info("-only protocol indexing");

        if (compactDConStart)
            LOGGER.info("-compact DataChain on start");

        if (HARD_WORK > 0)
            LOGGER.info("-hard work = " + HARD_WORK);

        if (inMemoryDC)
            LOGGER.info("-in Memory DC");

        if (noDataWallet)
            LOGGER.info("-no data wallet");

        if (noUseWallet)
            LOGGER.info("-no use wallet");

        if (useGui) {

            this.about_frame = AboutFrame.getInstance();
            this.addSingleObserver(about_frame);
            this.about_frame.setUserClose(false);
            this.about_frame.setModal(false);
            this.about_frame.setVisible(true);
        }
        if (!cli) {
            try {

                //ONE MUST BE ENABLED
                if (!Settings.getInstance().isGuiEnabled() && !Settings.getInstance().isRpcEnabled()) {
                    throw new Exception(Lang.T("Both gui and rpc cannot be disabled!"));
                }

                LOGGER.info(Lang.T("Starting %app%")
                        .replace("%app%", getApplicationName(false)));
                LOGGER.info(getVersion(true) + Lang.T(" build ")
                        + buildTime);

                this.setChanged();
                this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, info));


                String licenseFile = "Erachain Licence Agreement (genesis).txt";
                File f = new File(licenseFile);
                if (!f.exists()) {

                    LOGGER.error("License file not found: " + licenseFile);

                    //FORCE SHUTDOWN
                    System.exit(3);

                }


                //STARTING NETWORK/BLOCKCHAIN/RPC

                Controller.getInstance().start();

                //unlock wallet

                if (pass != null && doesWalletDatabaseExists()) {
                    if (unlockWallet(pass))
                        lockWallet();
                }

                Status.getinstance();

                if (!useGui) {
                    LOGGER.info("-nogui used");
                } else {

                    try {
                        Thread.sleep(100);

                        //START GUI

                        gui = Gui.getInstance();

                        if (gui != null && Settings.getInstance().isSysTrayEnabled()) {

                            SysTray.getInstance().createTrayIcon();
                            about_frame.setVisible(false);
                        }
                    } catch (Exception e1) {
                        if (about_frame != null) {
                            about_frame.setVisible(false);
                            about_frame.dispose();
                        }
                        LOGGER.error(Lang.T("GUI ERROR - at Start"), e1);
                    }
                }

                if (Controller.getInstance().doesWalletExists()) {
                    Controller.getInstance().wallet.initiateItemsFavorites();
                }


            } catch (Exception e) {

                // show error dialog
                String errorMsg = e.toString() + e.getMessage();
                LOGGER.error(errorMsg, e);
                if (useGui) {
                    if (Settings.getInstance().isGuiEnabled()) {
                        IssueConfirmDialog dd = new IssueConfirmDialog(null, true, null,
                                Lang.T("STARTUP ERROR") + ": "
                                        + errorMsg, 600, 400, Lang.T(" "));
                        dd.jButtonRAW.setVisible(false);
                        dd.jButtonFREE.setVisible(false);
                        dd.jButtonGO.setText(Lang.T("Cancel"));
                        dd.setLocationRelativeTo(null);
                        dd.setVisible(true);
                    }
                }

                //USE SYSTEM STYLE
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e2) {
                    LOGGER.error(e2.getMessage(), e2);
                }

                if (Gui.isGuiStarted()) {
                    JOptionPane.showMessageDialog(null, errorMsg, Lang.T("Startup Error"), JOptionPane.ERROR_MESSAGE);

                }


                if (about_frame != null) {
                    about_frame.setVisible(false);
                    about_frame.dispose();
                }
                //FORCE SHUTDOWN
                System.exit(3);
            }
        } else {
            Scanner scanner = new Scanner(System.in);
            ApiClient client = new ApiClient();

            while (true) {

                System.out.print("[COMMAND] ");
                String command = scanner.nextLine();

                if (command.equals("quit")) {

                    if (about_frame != null) {
                        about_frame.setVisible(false);
                        about_frame.dispose();
                    }
                    scanner.close();
                    System.exit(0);
                }

                String result = client.executeCommand(command);
                LOGGER.info("[RESULT] " + result);
            }
        }

    }

    public static void getManifestInfo() {
        String impTitle = "Gradle Build: ERA";

        try {
            Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                try {
                    Manifest manifest = new Manifest(resources.nextElement().openStream());
                    Attributes attributes = manifest.getMainAttributes();
                    String implementationTitle = attributes.getValue("Implementation-Title");
                    if (implementationTitle != null && implementationTitle.equals(impTitle)) {
                        version = attributes.getValue("Implementation-Version");
                        buildTime = attributes.getValue("Build-Time");

                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                        try {
                            Date date = formatter.parse(buildTime);
                            buildTimestamp = date.getTime();
                        } catch (ParseException e) {
                            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
                            try {
                                Date date = formatter.parse(buildTime);
                                buildTimestamp = date.getTime();
                            } catch (ParseException e1) {
                                LOGGER.error(e.getMessage(), e1);
                            }
                        }
                    }
                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
        }
    }
}
