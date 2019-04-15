package org.erachain.controller;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.erachain.api.ApiClient;
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
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.naming.Name;
import org.erachain.core.naming.NameSale;
import org.erachain.core.payment.Payment;
import org.erachain.core.telegram.TelegramStore;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.erachain.core.voting.PollOption;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.DLSet;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemMap;
import org.erachain.datachain.LocalDataMap;
import org.erachain.datachain.TransactionMap;
import org.erachain.gui.AboutFrame;
import org.erachain.gui.Gui;
import org.erachain.gui.GuiTimer;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.lang.Lang;
import org.erachain.network.Network;
import org.erachain.network.Peer;
import org.erachain.network.message.*;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.traders.TradersManager;
import org.erachain.utils.*;
import org.erachain.webserver.Status;
import org.erachain.webserver.WebService;
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
import java.io.IOException;
import java.math.BigDecimal;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

// 04/01 +-

/**
 * main class for connection all modules
 */
public class Controller extends Observable {

    public static String version = "4.11.12 beta RC";
    public static String buildTime = "2019-04-05 13:33:33 UTC";

    public static final char DECIMAL_SEPARATOR = '.';
    public static final char GROUPING_SEPARATOR = '`';
    // IF new abilities is made - new license insert in CHAIN and set this KEY
    public static final long LICENSE_VERS = 107; // versopn of LICENSE
    public static HashMap<String, Long> LICENSE_LANG_REFS = BlockChain.DEVELOP_USE ?
            new HashMap<String, Long>(3, 1) {
                {
                    put("en", Transaction.makeDBRef(148450, 1));
                    put("ru", Transaction.makeDBRef(191502, 1));
                }
            } :
            new HashMap<String, Long>(3, 1) {
                {
                    put("en", Transaction.makeDBRef(159719, 1));
                    put("ru", Transaction.makeDBRef(159727, 1));
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
    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
    public boolean useGui = true;
    private List<Thread> threads = new ArrayList<Thread>();
    public static long buildTimestamp;
    private static Controller instance;
    public Wallet wallet;
    public TelegramStore telegramStore;
    private boolean processingWalletSynchronize = false;
    private int status;
    private boolean dcSetWithObserver = false;
    private boolean dynamicGUI = false;
    public Network network;
    private TradersManager tradersManager;
    private ApiService rpcService;
    private WebService webService;
    public TransactionsPool transactionsPool;
    public WinBlockSelector winBlockSelector;
    public BlocksRequest blockRequester;
    private BlockChain blockChain;
    private BlockGenerator blockGenerator;
    private Synchronizer synchronizer;
    private TransactionCreator transactionCreator;
    private boolean needSyncWallet = false;
    private Timer connectTimer;
    private Random random = new SecureRandom();
    private byte[] foundMyselfID = new byte[128];
    private byte[] messageMagic;
    private long toOfflineTime;
    private ConcurrentHashMap<Peer, Tuple2<Integer, Long>> peerHWeight;
    private ConcurrentHashMap<Peer, Pair<String, Long>> peersVersions;
    private DLSet dlSet; // = DLSet.getInstance();
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

    public static String getVersion() {
        return version;
    }

    public static String getBuildDateTimeString() {
        return DateTimeFormat.timestamptoString(buildTimestamp, "yyyy-MM-dd HH:mm:ss z", "UTC");
    }

    public static String getBuildDateString() {
        return DateTimeFormat.timestamptoString(buildTimestamp, "yyyy-MM-dd", "UTC");
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

    public boolean isDynamicGUI() {
        return this.dynamicGUI;
    }

    public void setDCSet(DCSet db) {
        this.dcSet = db;
    }

    public DLSet getDBSet() {
        return this.dlSet;
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


    public void sendMyHWeightToPeer(Peer peer) {

        // SEND HEIGHT MESSAGE
        peer.offerMessage(MessageFactory.getInstance().createHWeightMessage(this.blockChain.getHWeightFull(dcSet)));
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
        } else {
            peerHWeight.remove(peer);
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
        }
        return new Pair<String, Long>("", 0L);
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

    public void checkNeedSyncWallet() {
        if (this.wallet == null || this.wallet.database == null)
            return;

        // CHECK IF WE NEED TO RESYNC
        byte[] lastBlockSignature = this.wallet.database.getLastBlockSignature();
        if (lastBlockSignature == null
                ///// || !findLastBlockOff(lastBlockSignature, block)
                || !Arrays.equals(lastBlockSignature, this.getBlockChain().getLastBlockSignature(dcSet))) {
            this.needSyncWallet = true;
        }
    }

    public boolean isNeedSyncWallet() {
        return this.needSyncWallet;
    }

    public void setNeedSyncWallet(boolean needSync) {
        this.needSyncWallet = needSync;
    }

    private void openDataBaseFile(String name, String path, org.erachain.database.IDB dbSet) {

        boolean error = false;
        boolean backUped = false;

        try {
            LOGGER.info("Open " + name);
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Open") + " " + name));

            //// должен быть метод
            ///// DLSet.open();
            /// this.DLSet = DLSet.getinstanse();

            LOGGER.info(name + " OK");
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("OK")));

        } catch (Throwable e) {

            LOGGER.error("Error during startup detected trying to restore backup " + name);

            error = true;

            try {
                // пытаемся восстановисть

                /// у объекта должен быть этот метод восстанорвления
                // DLSet.restoreBuckUp();

            } catch (Throwable e1) {

                LOGGER.error("Error during backup, tru recreate " + name);
                backUped = true;

                try {
                    // пытаемся пересоздать
                    //// у объекта должен быть такой метод пересоздания
                    // DLSet.reCreateDB();

                } catch (Throwable e2) {

                    // не смогли пересоздать выход!
                    stopAll(-3);
                }

            }
        }

        if (!error && !backUped && Settings.getInstance().getbacUpEnabled()) {
            // если нет ошибок и не было восстановления и нужно делать копии то сделаем

            if (useGui && Settings.getInstance().getbacUpAskToStart()) {
                // ask dialog
                int n = JOptionPane.showConfirmDialog(null, Lang.getInstance().translate("BackUp Database?"),
                        Lang.getInstance().translate("Confirmation"), JOptionPane.OK_CANCEL_OPTION);
                if (n == JOptionPane.OK_OPTION) {
                    this.setChanged();
                    this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("BackUp datachain")));
                    // delete & copy files in BackUp dir

                    //// у объекта должен быть этот метод сохранения DLSet.createDataCheckpoint();
                }
            } else {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("BackUp datachain")));
                // delete & copy files in BackUp dir
                //// у объекта должен быть этот метод сохранения DLSet.createDataCheckpoint();
            }
        }

    }

    public void start() throws Exception {

        this.toOfflineTime = NTP.getTime();
        this.foundMyselfID = new byte[128];
        this.random.nextBytes(this.foundMyselfID);

        this.peerHWeight = new ConcurrentHashMap<Peer, Tuple2<Integer, Long>>(20, 1);
        // LINKED TO PRESERVE ORDER WHEN SYNCHRONIZING (PRIORITIZE SYNCHRONIZING
        // FROM LONGEST CONNECTION ALIVE)

        this.peersVersions = new ConcurrentHashMap<Peer, Pair<String, Long>>(20, 1);

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

        /////////openDataBaseFile("DataLocale", "/datalocal", DCSet);

        try {
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Open DataLocale")));
            LOGGER.info("Try Open DataLocal");
            this.dlSet = DLSet.reCreateDB();
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("DataLocale OK")));
            LOGGER.info("DataLocale OK");
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            // e1.printStackTrace();
            reCreateDB();
            LOGGER.error("Error during startup detected trying to recreate DataLocale...");
        }

        // OPENING DATABASES
        try {
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Try Open DataChain")));
            LOGGER.info("Try Open DataChain");
            this.dcSet = DCSet.getInstance(this.dcSetWithObserver, this.dynamicGUI);
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("DataChain OK")));
            LOGGER.info("DataChain OK");
        } catch (Throwable e) {
            // Error open DB
            error = 1;
            LOGGER.error(e.getMessage(), e);
            LOGGER.error("Error during startup detected trying to restore backup DataChain...");
            try {
                reCreateDC();
            } catch (Throwable e1) {
                stopAll(5);
            }
        }


        if (error == 0 && useGui && Settings.getInstance().getbacUpEnabled()) {

            if (Settings.getInstance().getbacUpAskToStart()) {
                // ask dialog
                int n = JOptionPane.showConfirmDialog(null, Lang.getInstance().translate("BackUp Database?"),
                        Lang.getInstance().translate("Confirmation"), JOptionPane.OK_CANCEL_OPTION);
                if (n == JOptionPane.OK_OPTION) {
                    this.setChanged();
                    this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("BackUp datachain")));
                    // delete & copy files in BackUp dir
                    createDataCheckpoint();
                }
            } else {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("BackUp datachain")));
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
                reCreateDC();
            } catch (Throwable e) {
                stopAll(5);
            }
        }

        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Datachain Ok")));
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

        // CREATE SYNCHRONIZOR
        this.synchronizer = new Synchronizer();

        // CREATE BLOCKCHAIN
        this.blockChain = new BlockChain(dcSet);

        // CREATE TRANSACTIONS POOL
        this.transactionsPool = new TransactionsPool(this, blockChain, dcSet);

        // CREATE WinBlock SELECTOR
        this.winBlockSelector = new WinBlockSelector(this, blockChain, dcSet);

        // CREATE Block REQUESTER
        this.blockRequester = new BlocksRequest(this, blockChain, dcSet);

        // START API SERVICE
        if (Settings.getInstance().isRpcEnabled()) {
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Start API Service")));
            LOGGER.info(Lang.getInstance().translate("Start API Service"));
            this.rpcService = new ApiService();
            this.rpcServiceRestart();
        }

        // START WEB SERVICE
        if (Settings.getInstance().isWebEnabled()) {
            this.setChanged();
            this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Start WEB Service")));
            LOGGER.info(Lang.getInstance().translate("Start WEB Service"));
            this.webService = new WebService();
            this.webService.start();
        }

        // CREATE WALLET
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Open Wallet")));
        this.wallet = new Wallet(this.dcSetWithObserver, this.dynamicGUI);

        if (this.seedCommand != null && this.seedCommand.length > 1) {
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
                        path = Settings.getInstance().getWalletDir();
                    }

                    boolean res = recoverWallet(seed,
                            this.seedCommand.length > 2 ? this.seedCommand[2] : "1",
                            accsNum, path);
                    this.seedCommand = null;
                }
            }

        }

        guiTimer = new GuiTimer();

        if (this.wallet.isWalletDatabaseExisting()) {
            this.wallet.initiateItemsFavorites();
        }
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Wallet OK")));

        if (Settings.getInstance().isTestnet() && this.wallet.isWalletDatabaseExisting()
                && !this.wallet.getAccounts().isEmpty()) {
            this.wallet.synchronize(true);
        }
        // create telegtam

        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Open Telegram")));
        this.telegramStore = TelegramStore.getInstanse(this.dcSetWithObserver, this.dynamicGUI);


        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Telegram OK")));

        // CREATE BLOCKGENERATOR
        this.blockGenerator = new BlockGenerator(this.dcSet, this.blockChain, true);
        // START UPDATES and BLOCK BLOCKGENERATOR
        this.blockGenerator.start();

        // CREATE NETWORK
        this.network = new Network(this);

        // CLOSE ON UNEXPECTED SHUTDOWN
        Runtime.getRuntime().addShutdownHook(new Thread(null, null, "ShutdownHook") {
            @Override
            public void run() {
                // -999999 - not use System.exit() - if freeze exit
                stopAll(-999999);
                //Runtime.getRuntime().removeShutdownHook(currentThread());
            }
        });

        if (Settings.getInstance().isTestnet())
            this.status = STATUS_OK;

        // REGISTER DATABASE OBSERVER
        // this.addObserver(this.DLSet.getPeerMap());
        this.addObserver(this.dcSet);

        // start memory viewer
        MemoryViewer mamoryViewer = new MemoryViewer(this);
        mamoryViewer.start();

        // CREATE NETWORK
        this.tradersManager = new TradersManager();

        updateCompuRaes();
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
        return this.wallet.loadFromDir(this.dcSetWithObserver, this.dynamicGUI);
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

        DCSet.reCreateDB(this.dcSetWithObserver, this.dynamicGUI);
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
                LOGGER.error(e.getMessage(), e);
            }
        }

        this.dlSet = DLSet.reCreateDB();
        return this.dlSet;
    }

    private void createDataCheckpoint() {
        if (!this.dcSet.getBlockMap().isProcessing()) {
            // && Settings.getInstance().isCheckpointingEnabled()) {
            // this.dcSet.close();

            File dataDir = new File(Settings.getInstance().getDataDir());

            File dataBakDC = new File(Settings.getInstance().getBackUpDir() + File.separator
                    + Settings.getInstance().DEFAULT_DATA_DIR + File.separator);
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

    /**
     * я так понял - это отслеживание версии базы данных - и если она новая то все удаляем и заново закачиваем
     *
     * @throws IOException
     * @throws Exception
     */
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
            DCSet.reCreateDB(this.dcSetWithObserver, this.dynamicGUI);

            this.dcSet.getLocalDataMap().set(LocalDataMap.LOCAL_DATA_VERSION_KEY, Controller.releaseVersion);

        }
    }

    private File getDataBakDir(File dataDir) {
        return new File(dataDir.getParent(), Settings.getInstance().getDataDir() + "Bak");
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

        // START API SERVICE
        if (Settings.getInstance().isWebEnabled()) {
            this.webService = new WebService();
            this.webService.start();
        }
    }

    public boolean isOnStopping() {
        return this.isStopping;
    }

    public void stopAll(Integer par) {
        // PREVENT MULTIPLE CALLS
        if (this.isStopping)
            return;
        this.isStopping = true;

        if (this.connectTimer != null)
            this.connectTimer.cancel();

        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Closing")));
        // STOP MESSAGE PROCESSOR
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Stopping message processor")));

        LOGGER.info("Stopping message processor");
        this.network.stop();


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
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Delete files from TEMP dir")));
        LOGGER.info("Delete files from TEMP dir");
        for (File file : new File(Settings.getInstance().getTemDir()).listFiles())
            if (file.isFile()) file.delete();

        // STOP TRANSACTIONS POOL
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Stopping Transactions Pool")));
        LOGGER.info("Stopping Transactions Pool");
        this.transactionsPool.halt();

        // STOP WIN BLOCK SELECTOR
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Stopping WinBlock Selector")));
        LOGGER.info("Stopping WinBlock Selector");
        this.winBlockSelector.halt();

        // STOP BLOCK REQUESTER
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Stopping Block Requester")));
        LOGGER.info("Stopping Block Requester");
        this.blockRequester.halt();

        // STOP BLOCK PROCESSOR
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Stopping block synchronizer")));
        LOGGER.info("Stopping block synchronizer");
        this.synchronizer.stop();

        // WAITING STOP MAIN PROCESS
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Waiting stopping processors")));
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
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("DCSet is busy...")));
        LOGGER.info("DCSet is busy...");

        i = 0;
        while (i++ < 10 && dcSet.isBusy()) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                break;
            }
        }

        // CLOSE DATABABASE
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Closing database")));
        LOGGER.info("Closing database");
        this.dcSet.close();

        // CLOSE WALLET
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Closing wallet")));
        LOGGER.info("Closing wallet");
        this.wallet.close();

        // CLOSE LOCAL
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Closing Local database")));
        LOGGER.info("Closing Local database");
        this.dlSet.close();

        // CLOSE telegram
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate("Closing telegram")));
        LOGGER.info("Closing telegram");
        this.telegramStore.close();

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

        TransactionMap map = this.dcSet.getTransactionMap();
        Iterator<Long> iterator = map.getIterator(0, false);
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

        // logger.info(peer + " sended UNCONFIRMED counter: " +
        // counter);

        //NOTIFY OBSERVERS
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_PEER_TYPE, peer));

        return peer.isUsed();

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
        if (!peer.directSendMessage(
                MessageFactory.getInstance().createVersionMessage(Controller.getVersion(), buildTimestamp))) {
            peer.ban(network.banForActivePeersCounter(), "connection - break on Version send");
            return;
        }

        // TODO в новой версии 4.11.9 включить это обратно
        if (false) {
            // CHECK GENESIS BLOCK on CONNECT
            Message mess = MessageFactory.getInstance()
                    .createGetHeadersMessage(this.blockChain.getGenesisBlock().getSignature());
            SignaturesMessage response = (SignaturesMessage) peer.getResponse(mess, 10000); // AWAIT!

            if (this.isStopping)
                return;
            if (response == null) {
                peer.ban(network.banForActivePeersCounter(), "connection - break on POINTs get");
                return;
            } else if (response.getSignatures().isEmpty()) {
                // NO
                peer.ban(Synchronizer.BAN_BLOCK_TIMES << 2, "connection - wrong GENESIS BLOCK");
                return;
            }
        }

        if (this.isStopping)
            return; // MAY BE IT HARD BUSY

        // GET CURRENT WIN BLOCK
        Block winBlock = this.blockChain.getWaitWinBuffer();
        if (winBlock != null) {
            // SEND MESSAGE
            peer.sendWinBlock((BlockWinMessage) MessageFactory.getInstance().createWinBlockMessage(winBlock));
        }

        if (this.status == STATUS_NO_CONNECTIONS) {
            // UPDATE STATUS
            this.status = STATUS_SYNCHRONIZING;

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
                if (this.getBlockChain().getTimestamp(hW.a) - 2 * BlockChain.GENERATING_MIN_BLOCK_TIME_MS > NTP
                        .getTime()) {
                    // IT PEER from FUTURE
                    this.banPeerOnError(hWeightMessage.getSender(), "peer from FUTURE");
                    return;
                }

                // ADD TO LIST
                this.peerHWeight.put(hWeightMessage.getSender(), hWeightMessage.getHWeight());

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

                // ADD TO LIST
                this.peersVersions.put(versionMessage.getSender(), new Pair<String, Long>(
                        versionMessage.getStrVersion(), versionMessage.getBuildDateTime()));

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
                message.getSender().offerMessage(response);

        }

    }

    public void banPeerOnError(Peer peer, String mess) {
        peer.ban("ban PeerOnError - " + mess);
    }

    public void banPeerOnError(Peer peer, String mess, int minutes) {
        peer.ban(minutes, "ban PeerOnError - " + mess);
    }

    public void addActivePeersObserver(Observer o) {
        this.network.addObserver(o);
        this.guiTimer.addObserver(o);
    }

    public void removeActivePeersObserver(Observer o) {
        this.guiTimer.deleteObserver(o);
        this.network.deleteObserver(o);
    }

    public void broadcastWinBlock(Block newBlock) {

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

    public boolean broadcastTelegram(Transaction transaction, boolean store) {

        // CREATE MESSAGE
        Message telegram = MessageFactory.getInstance().createTelegramMessage(transaction);
        boolean notAdded = this.network.addTelegram((TelegramMessage) telegram);

        if (!store || !notAdded) {
            // BROADCAST MESSAGE
            this.network.broadcast(telegram, false);
            // save DB
            Controller.getInstance().wallet.database.getTelegramsMap().add(transaction.viewSignature(), transaction);
        }

        return !notAdded;

    }

    // SYNCHRONIZE

    public void orphanInPipe(Block block) throws Exception {
        this.synchronizer.pipeProcessOrOrphan(this.dcSet, block, true, false);
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
        Tuple3<Integer, Long, Peer> maxHW = this.getMaxPeerHWeight(shift, false);
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
            LOGGER.info("NotifyIncoming " + url_string + ": " + res);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    public boolean isNSUpToDate() {
        return !Settings.getInstance().updateNameStorage();
    }


    public synchronized void checkNewBetterPeer(Peer currentBetterPeer) throws Exception {

        if (!newPeerConnected)
            return;

        newPeerConnected = false;

        Tuple3<Integer, Long, Peer> betterPeerHW = this.getMaxPeerHWeight(0, true);
        if (betterPeerHW != null) {
            Tuple2<Integer, Long> currentHW = getHWeightOfPeer(currentBetterPeer);
            if (currentHW != null && (currentHW.a > betterPeerHW.a || currentHW.b >= betterPeerHW.b
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

    public void update(int shift) {
        // UPDATE STATUS

        if (this.status == STATUS_NO_CONNECTIONS) {
            return;
        }

        /// this.status = STATUS_SYNCHRONIZING;

        // DLSet dcSet = DLSet.getInstance();

        Peer peer = null;
        // Block lastBlock = getLastBlock();
        // int lastTrueBlockHeight = this.getMyHeight() -
        // Settings.BLOCK_MAX_SIGNATURES;
        int checkPointHeight = BlockChain.getCheckPoint(dcSet);

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
            Tuple3<Integer, Long, Peer> peerHW = this.getMaxPeerHWeight(shift, true);
            if (peerHW != null && peerHW.a > myHWeight.a) {
                peer = peerHW.c;
                if (peer != null) {
                    info = "update from MaxHeightPeer:" + peer + " WH: "
                            + getHWeightOfPeer(peer);
                    LOGGER.info(info);
                    this.setChanged();
                    this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_ABOUT_TYPE, Lang.getInstance().translate(info)));
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
                            peer.ban(e.getMessage());
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
            if (this.isStopping) return;

            // если в момент синхронизации прилетал победный блок
            // то его вынем и поновой вставим со всеми проверками
            Block winBlockUnchecked = this.blockChain.popWaitWinBuffer();
            if (winBlockUnchecked != null)
                this.blockChain.setWaitWinBuffer(this.dcSet, winBlockUnchecked, null);

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

    public Tuple3<Integer, Long, Peer> getMaxPeerHWeight(int shift, boolean useWeight) {

        if (this.isStopping || this.dcSet.isStoped())
            return null;

        Tuple2<Integer, Long> myHWeight = this.getBlockChain().getHWeightFull(dcSet);
        int height = myHWeight.a + shift;
        long weight = myHWeight.b;
        Peer maxPeer = null;

        try {
            for (Peer peer : this.peerHWeight.keySet()) {
                if (peer.getPing() < 0) {
                    // не использовать пиры которые не в быстром коннекте
                    // - так как иначе они заморозят синхронизацию совсем
                    // да и не понятно как с них данные получать
                    continue;
                }
                Tuple2<Integer, Long> whPeer = this.peerHWeight.get(peer);
                if (height < whPeer.a
                        || (useWeight && height == whPeer.a && weight < whPeer.b)) {
                    height = whPeer.a;
                    weight = whPeer.b;
                    maxPeer = peer;
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

            LOGGER.info("Wallet needs to synchronize!");
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

    public void clearUnconfirmedRecords(boolean cutDeadTime) {
        this.blockChain.clearUnconfirmedRecords(this.dcSet, cutDeadTime);

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

            //gui = Gui.getInstance();
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
        this.wallet.setSecondsToUnlock(3);
        return this.wallet.unlock(password);
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

        // CHECK IF IN TRANSACTION DATABASE
        if (database.getTransactionMap().contains(signature)) {
            return database.getTransactionMap().get(signature);
        }
        // CHECK IF IN BLOCK
        Long tuple_Tx = database.getTransactionFinalMapSigns().get(signature);
        if (tuple_Tx != null) {
            return database.getTransactionFinalMap().get(tuple_Tx);
        }
        return null;
    }

    public List<Transaction> getLastTransactions(Account account, int limit) {
        return this.wallet.getLastTransactions(account, limit);
    }

    public List<Pair<Account, Block.BlockHead>> getLastBlocks(int limit) {
        return this.wallet.getLastBlocks(limit);
    }

    public List<Block.BlockHead> getLastBlocks(Account account, int limit) {
        return this.wallet.getLastBlocks(account, limit);
    }

    public List<TelegramMessage> getLastTelegrams(Account account, long timestamp, String filter) {
        return this.network.getTelegramsForAddress(account.getAddress(), timestamp, filter);
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

    public List<TelegramMessage> getLastTelegrams(String address, long timestamp, String filter) {
        return this.network.getTelegramsForAddress(address, timestamp, filter);
    }

    public List<TelegramMessage> getLastTelegrams(long timestamp, String recipient, String filter) {
        return this.network.getTelegramsFromTimestamp(timestamp, recipient, filter);
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

    @Deprecated
    public List<Name> getNames(Account account) {
        return this.wallet.getNames(account);
    }

    @Deprecated
    public List<Pair<Account, NameSale>> getNameSales() {
        return this.wallet.getNameSales();
    }

    @Deprecated
    public List<NameSale> getNameSales(Account account) {
        return this.wallet.getNameSales(account);
    }

    @Deprecated
    public List<NameSale> getAllNameSales() {
        return this.dcSet.getNameExchangeMap().getNameSales();
    }

    @Deprecated
    public List<Pair<Account, org.erachain.core.voting.Poll>> getPolls() {
        return this.wallet.getPolls();
    }

    public List<org.erachain.core.voting.Poll> getPolls(Account account) {
        return this.wallet.getPolls(account);
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
        }
        return null;
    }

    public void addItemFavorite(ItemCls item) {
        this.wallet.addItemFavorite(item);
    }

    public void removeItemFavorite(ItemCls item) {
        this.wallet.removeItemFavorite(item);
    }

    public boolean isItemFavorite(ItemCls item) { return this.wallet.isItemFavorite(item); }

    public void addTransactionFavorite(Transaction transaction) {
        this.wallet.addTransactionFavorite(transaction);
    }

    public void removeTransactionFavorite(Transaction transaction) {
        this.wallet.removeTransactionFavorite(transaction);
    }

    public boolean isTransactionFavorite(Transaction transaction) {
        return this.wallet.isTransactionFavorite(transaction);
    }


    public Collection<org.erachain.core.voting.Poll> getAllPolls() {
        return this.dcSet.getPollMap().getValues();
    }

    public Collection<ItemCls> getAllItems(int type) {
        return getItemMap(type).getValues();
    }

    public Collection<ItemCls> getAllItems(int type, Account account) {
        return getItemMap(type).getValues();
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

    public byte[] getWalletLastBlockSign() {
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

        // if last block is changed by core.Synchronizer.process(DLSet, Block)
        // clear this win block
        if (!Arrays.equals(dcSet.getBlockMap().getLastBlockSignature(), newBlock.getReference())) {
            return false;
        }

        if (!newBlock.isValidated())
            // это может случиться при добавлении в момент синхронизации - тогда до расчета Победы не доходит
            // или прри добавлении моего сгнерированного блока т.к. он не проверился?
            if (!newBlock.isValid(dcSet, false))
                // тогда проверим заново полностью
                return false;

        LOGGER.info("+++ flushNewBlockGenerated TRY flush chainBlock: " + newBlock.toString());

        try {
            this.synchronizer.pipeProcessOrOrphan(this.dcSet, newBlock, false, true);
            this.network.clearHandledWinBlockMessages();

        } catch (Exception e) {
            if (this.isOnStopping()) {
                throw new Exception("on stoping");
            } else {
                LOGGER.error(e.getMessage(), e);
                return false;
            }
        }

        LOGGER.info("+++ flushNewBlockGenerated OK");

        /// logger.info("and broadcast it");

        // broadcast my HW
        broadcastHWeightFull();

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

    public List<Transaction> getUnconfirmedTransactionsByAddressFast100(String address) {
        return this.dcSet.getTransactionMap().getTransactionsByAddressFast100(address);
    }

    // NAMES

    public Name getName(String nameName) {
        return this.dcSet.getNameMap().get(nameName);
    }

    public NameSale getNameSale(String nameName) {
        return this.dcSet.getNameExchangeMap().getNameSale(nameName);
    }

    // POLLS

    public org.erachain.core.voting.Poll getPoll(String name) {
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

    public SortableList<Long, Order> getOrders(
            AssetCls have, AssetCls want, boolean reverse) {
        return this.dcSet.getOrderMap().getOrdersSortableList(have.getKey(this.dcSet), want.getKey(this.dcSet), reverse);
    }

    public List<Order> getOrders(Long have, Long want) {

        return dcSet.getOrderMap().getOrdersForTradeWithFork(have, want, false);
    }


    public SortableList<Tuple2<Long, Long>, Trade> getTrades(
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
            org.erachain.core.voting.Poll poll = new org.erachain.core.voting.Poll(creator, name, description, pollOptions);

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

    public Pair<Transaction, Integer> lightCreateTransactionFromRaw(byte[] rawData) {

        // CREATE TRANSACTION FROM RAW
        Transaction transaction;
        try {
            transaction = TransactionFactory.getInstance().parse(rawData, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            return new Pair<Transaction, Integer>(null, Transaction.INVALID_RAW_DATA);
        }

        // CHECK IF RECORD VALID
        if (!transaction.isSignatureValid(DCSet.getInstance()))
            return new Pair<Transaction, Integer>(null, Transaction.INVALID_SIGNATURE);

        // CHECK FOR UPDATES
        int valid = this.transactionCreator.afterCreateRaw(transaction, Transaction.FOR_NETWORK, 0l);
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
        Transaction orderCreate = this.dcSet.getTransactionFinalMap().get(order.getId());
        return cancelOrder(creator, orderCreate.getSignature(), feePow);
    }

    public Pair<Transaction, Integer> cancelOrder(PrivateKeyAccount creator, byte[] orderID, int feePow) {
        // CREATE ONLY ONE TRANSACTION AT A TIME
        synchronized (this.transactionCreator) {
            return this.transactionCreator.createCancelOrderTransaction(creator, orderID, feePow);
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
                                                  int feePow, long assetKey, boolean checkAsset, BigDecimal amount, boolean needAmount,
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
        return new Pair<Integer, Transaction>(Transaction.VALIDATE_OK, this.r_Send(privateKeyAccount, feePow, recipient,
                assetKey, amount, title, messageBytes, isTextByte, encrypted));

    }

    public Transaction r_Send(PrivateKeyAccount sender, int feePow, Account recipient, long key, BigDecimal amount) {
        return this.r_Send(sender, feePow, recipient, key, amount, "", null, null, null);
    }

    public Transaction r_Send(PrivateKeyAccount sender, int feePow,
                              Account recipient, long key, BigDecimal amount, String title, byte[] message, byte[] isText,
                              byte[] encryptMessage) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_Send(sender, recipient, key, amount, feePow, title, message, isText,
                    encryptMessage);
        }
    }

    public Transaction r_Send(byte version, byte property1, byte property2,
                              PrivateKeyAccount sender, int feePow,
                              Account recipient, long key, BigDecimal amount, String title, byte[] message, byte[] isText,
                              byte[] encryptMessage) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_Send(version, property1, property2, sender, recipient, key, amount, feePow,
                    title, message, isText, encryptMessage);
        }
    }

    public Transaction r_SignNote(byte version, byte property1, byte property2, int asDeal,
                                  PrivateKeyAccount sender, int feePow, long key, byte[] message, byte[] isText, byte[] encrypted) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_SignNote(version, property1, property2, asDeal, sender, feePow, key,
                    message, isText, encrypted);
        }
    }

    public Transaction r_SertifyPerson(int version, int asDeal, PrivateKeyAccount creator, int feePow, long key,
                                       List<PublicKeyAccount> userAccounts, int add_day) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_SertifyPerson(version, asDeal, creator, feePow, key, userAccounts,
                    add_day);
        }
    }

    public Transaction r_Vouch(int version, int asDeal, PrivateKeyAccount creator, int feePow, int height,
                               int seq) {
        synchronized (this.transactionCreator) {
            return this.transactionCreator.r_Vouch(version, asDeal, creator, feePow, height, seq);
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

    public void updateCompuRaes() {
        BigDecimal rate = new BigDecimal(Settings.getInstance().getCompuRate()).setScale(2);
        //this.COMPU_RATES.put("ru", new Tuple2<BigDecimal, String>(rate, "$"));
        this.COMPU_RATES.put("en", new Tuple2<BigDecimal, String>(rate, "$"));
    }

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

    public void addSingleObserver(Observer o) {
        super.addObserver(o);
    }

    public void deleteSingleObserver(Observer o) {
        super.deleteObserver(o);
    }

    public void addWalletObserver(Observer o) {
        this.wallet.addObserver(o);
        this.guiTimer.addObserver(o); // обработка repaintGUI
    }

    public void deleteWalletObserver(Observer o) {
        this.guiTimer.deleteObserver(o); // нужно для перерисовки раз в 2 сек
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

        for (String arg : args) {
            if (arg.equals("-cli")) {
                cli = true;
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

            if (arg.equals("-backup")) {
                // backUP data
                backUP = true;
                continue;
            }

            if (arg.equals("-nogui")) {
                useGui = false;
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
            if (arg.equals("-testnet")) {
                Settings.getInstance().setGenesisStamp(System.currentTimeMillis());
                continue;
            }
            if (arg.startsWith("-testnet=") && arg.length() > 9) {
                try {
                    long testnetstamp = Long.parseLong(arg.substring(9));

                    if (testnetstamp == 0) {
                        testnetstamp = System.currentTimeMillis();
                    }

                    Settings.getInstance().setGenesisStamp(testnetstamp);
                } catch (Exception e) {
                    Settings.getInstance().setGenesisStamp(BlockChain.DEFAULT_MAINNET_STAMP);
                }
            }
        }

        if (noCalculated)
            LOGGER.info("-not store calculated TXs");

        if (onlyProtocolIndexing)
            LOGGER.info("-only protocol indexing");

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
                    throw new Exception(Lang.getInstance().translate("Both gui and rpc cannot be disabled!"));
                }

                LOGGER.info(Lang.getInstance().translate("Starting %app%")
                        .replace("%app%", Lang.getInstance().translate(Controller.APP_NAME)));
                LOGGER.info(version + Lang.getInstance().translate(" build ")
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
                        LOGGER.error(Lang.getInstance().translate("GUI ERROR - at Start"), e1);
                    }
                }

                if (Controller.getInstance().doesWalletExists()) {
                    Controller.getInstance().wallet.initiateItemsFavorites();
                }


            } catch (Exception e) {

                LOGGER.error(e.getMessage(), e);
                // show error dialog
                if (useGui) {
                    if (Settings.getInstance().isGuiEnabled()) {
                        IssueConfirmDialog dd = new IssueConfirmDialog(null, true, null,
                                Lang.getInstance().translate("STARTUP ERROR") + ": " + e.getMessage(), 600, 400, Lang.getInstance().translate(" "));
                        dd.jButton1.setVisible(false);
                        dd.jButton2.setText(Lang.getInstance().translate("Cancel"));
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

                //ERROR STARTING
                LOGGER.error(Lang.getInstance().translate("STARTUP ERROR") + ": " + e.getMessage());

                if (Gui.isGuiStarted()) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), Lang.getInstance().translate("Startup Error"), JOptionPane.ERROR_MESSAGE);

                }


                if (about_frame != null) {
                    about_frame.setVisible(false);
                    about_frame.dispose();
                }
                //FORCE SHUTDOWN
                System.exit(0);
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
