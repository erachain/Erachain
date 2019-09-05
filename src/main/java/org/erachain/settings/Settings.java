package org.erachain.settings;
// 17/03 Qj1vEeuz7iJADzV2qrxguSFGzamZiYZVUP
// 30/03 ++

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.lang.Lang;
import org.erachain.network.Peer;
import org.erachain.ntp.NTP;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

//import java.util.Arrays;
// import org.slf4j.LoggerFactory;

public class Settings {
    //private static final String[] DEFAULT_PEERS = { };
    public static final String DEFAULT_THEME = "System";
    public static final int DEFAULT_ACCOUNTS = 1;
    //DATA
    public static final String DEFAULT_DATA_DIR = "datachain";
    public static final String DEFAULT_LOCAL_DIR = "datalocal";
    public static final String DEFAULT_DATATEMP_DIR = "datatemp";
    public static final String DEFAULT_WALLET_DIR = "walletKeys";
    private static final String DEFAULT_DATAWALET_DIR = "dataWallet";
    public static final String DEFAULT_BACKUP_DIR = "backup";
    public static final String DEFAULT_TEMP_DIR = "temp";
    public static final String DEFAULT_TELEGRAM_DIR = "telegram";
    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);
    //NETWORK
    private static final int DEFAULT_MIN_CONNECTIONS = 10; // for OWN maked connections
    private static final int DEFAULT_MAX_CONNECTIONS = 100;
    // EACH known PEER may send that whit peers to me - not white peer may be white peer for me
    private static final int DEFAULT_MAX_RECEIVE_PEERS = 100;
    private static final int DEFAULT_MAX_SENT_PEERS = DEFAULT_MAX_RECEIVE_PEERS;
    // BLOCK
    //public static final int BLOCK_MAX_SIGNATURES = 100; // blocks load onetime
    private static final int DEFAULT_CONNECTION_TIMEOUT = 20000;
    private static final boolean DEFAULT_TRYING_CONNECT_TO_BAD_PEERS = true;
    private static final Integer DEFAULT_FONT_SIZE = 11;
    private static final String DEFAULT_FONT_NAME = "Arial";
    //RPC
    private static final String DEFAULT_RPC_ALLOWED = "127.0.0.1"; // localhost = error in accessHandler.setWhite(Settings.getInstance().getRpcAllowed());
    private static final boolean DEFAULT_RPC_ENABLED = false; //
    private static final boolean DEFAULT_BACUP_ENABLED = false;
    private static final boolean DEFAULT_BACKUP_ASK_ENABLED = false;
    //GUI CONSOLE
    private static final boolean DEFAULT_GUI_CONSOLE_ENABLED = true;
    //WEB
    private static final String DEFAULT_WEB_ALLOWED = BlockChain.DEVELOP_USE? ";" : "127.0.0.1";
    private static final boolean DEFAULT_WEB_ENABLED = true;
    // 19 03
    //GUI
    private static final boolean DEFAULT_GUI_ENABLED = true;
    private static final boolean DEFAULT_GUI_DYNAMIC = true;
    private static final boolean DEFAULT_GENERATOR_KEY_CACHING = true;
    private static final boolean DEFAULT_CHECKPOINTING = true;
    private static final boolean DEFAULT_SOUND_RECEIVE_COIN = true;
    private static final boolean DEFAULT_SOUND_MESSAGE = true;
    private static final boolean DEFAULT_SOUND_NEW_TRANSACTION = true;
    //private static final int DEFAULT_MAX_BYTE_PER_FEE = 512;
    private static final boolean ALLOW_FEE_LESS_REQUIRED = false;
    //DATE FORMAT
    private static final String DEFAULT_TIME_ZONE = ""; //"GMT+3";

    //private static final BigDecimal DEFAULT_BIG_FEE = new BigDecimal(1000);
    private static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss z";
    private static final String DEFAULT_BIRTH_TIME_FORMAT = "yyyy-MM-dd HH:mm z";
    private static final boolean DEFAULT_NS_UPDATE = false;
    private static final boolean DEFAULT_FORGING_ENABLED = true;

    /**
     * нельзя тут использовать localhost - только 127....
     * ex: http://127.0.0.1/7pay_in/tools/block_proc/ERA
     */
    private static final String NOTIFY_INCOMING_URL = "http://127.0.0.1:8000/exhange/era/income";
    private static final int NOTIFY_INCOMING_CONFIRMATIONS = 0;
    public static String DEFAULT_LANGUAGE = "en";

    public static final boolean USE_TELEGRAM_STORE = false;
    public static final int TELEGRAM_STORE_PERIOD = 5; // in days



    private static Settings instance;
    List<Peer> cacheInternetPeers;
    long timeLoadInternetPeers;
    private long genesisStamp = -1;
    private JSONObject settingsJSON;
    private JSONObject peersJSON;
    private String userPath = "";
    private InetAddress localAddress;
    private String[] defaultPeers = {};

    private String tmpPath;
    private String getBackUpPath;
    private String getWalletPath;
    private String getDataWalletPath;
    private String dataPath;
    private String telegramDefaultSender;
    private String telegramDefaultReciever;
    private String telegramRatioReciever = null;
    private String getTelegramPath;
    
    private String telegramtPath;

    private Settings() {
        this.localAddress = this.getCurrentIp();
        settingsJSON = read_setting_JSON();


        File file = new File("");
        //TRY READ PEERS.JSON
        try {
            //OPEN FILE
            file = new File(this.getPeersPath());

            //CREATE FILE IF IT DOESNT EXIST
            if (file.exists()) {
                //READ PEERS FILE
                List<String> lines = Files.readLines(file, Charsets.UTF_8);

                String jsonString = "";
                for (String line : lines) {
                    jsonString += line;
                }

                //CREATE JSON OBJECT
                this.peersJSON = (JSONObject) JSONValue.parse(jsonString);
            } else {
                this.peersJSON = new JSONObject();
            }

        } catch (Exception e) {
            LOGGER.info("Error while reading peers.json " + file.getAbsolutePath() + " using default!");
            LOGGER.error(e.getMessage(), e);
            this.peersJSON = new JSONObject();
        }
    }

    public static Settings getInstance() {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        try {
            if (instance == null) {

                instance = new Settings();
            }
        } finally {
            lock.unlock();
        }
        return instance;
    }

    public static void FreeInstance() {
        if (instance != null) {
            instance = null;
        }
    }

    public JSONObject Dump() {
        return (JSONObject) settingsJSON.clone();
    }

    public void setDefaultPeers(String[] peers) {
        this.defaultPeers = peers;
    }

    public String getSettingsPath() {
        return this.userPath + "settings.json";
    }

    public String getGuiSettingPath() {

        return this.userPath + "gui_settings.json";

    }

    public String getPeersPath() {
        return this.userPath + (BlockChain.DEVELOP_USE ? "peers-dev.json" : "peers.json");
    }

    public String getWalletDir() {
        try {
            if (this.getWalletPath.equals(""))
                return this.userPath + DEFAULT_WALLET_DIR;
            return this.getWalletPath;
        } catch (Exception e) {
			return this.userPath + DEFAULT_WALLET_DIR;
		}
    }

    public String getDataWalletDir() {
        try {
            if (this.getDataWalletPath.equals("")) return this.userPath + DEFAULT_DATAWALET_DIR;
            return this.getWalletPath;
        } catch (Exception e) {
            return this.userPath + DEFAULT_DATAWALET_DIR;
        }
    }

    public void setWalletDir(String dir) {
       
			this.getWalletPath = dir;
		
    }
    
    public String getTelegramDir() {
        try {
            if (this.telegramtPath.equals("")) this.telegramtPath =  this.userPath + DEFAULT_TELEGRAM_DIR;
            return this.telegramtPath;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            return this.userPath + DEFAULT_TELEGRAM_DIR;
        }
    }
    
    public void setTelegramDir(String dir) {
       
            this.telegramtPath = dir;
        
    }

    public boolean getTelegramStoreUse() {
        return USE_TELEGRAM_STORE;
    }

    public int getTelegramStorePeriod() {
        return TELEGRAM_STORE_PERIOD;
    }


    public String getTelegramDefaultSender(){
        return telegramDefaultSender;
    }
    
    public void setTelegramDefaultSender(String str){
        telegramDefaultSender = str;
    }
    
   public String getTelegramDefaultReciever(){
       return telegramDefaultReciever;
   }
   public void setTelegramDefaultReciever(String str){
        this.telegramDefaultReciever=str;
    }
    
 public String getTemDir(){
     String path = "";
     try {
         
         if (this.tmpPath.equals("")) {
             path= this.userPath + DEFAULT_TEMP_DIR;
         }
         else path =  this.tmpPath;
     } catch (Exception e) {
         // TODO Auto-generated catch block
         path = this.userPath + DEFAULT_TEMP_DIR;
     }
     // if temp dir not exist make dir
     File tempDir = new File(path);
     if (!tempDir.exists()) {
             tempDir.mkdir();
     }
        
     return path;
    }

    public String getBackUpDir() {
        try {
			if (this.getBackUpPath.equals("")) return this.userPath + DEFAULT_BACKUP_DIR;
			return this.getBackUpPath;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return this.userPath + DEFAULT_BACKUP_DIR;
		}
    }


    public String getDataDir() {
        try {
			if (this.dataPath.equals(""))
			    return this.getUserPath() + DEFAULT_DATA_DIR;
			return this.dataPath;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return this.getUserPath() + DEFAULT_DATA_DIR;
		}
    }

    public String getLocalDir() {
        return this.getUserPath() + DEFAULT_LOCAL_DIR;
    }

    public String getDataTempDir() {
        return this.getUserPath() + DEFAULT_DATATEMP_DIR;
    }

    public String getLangDir() {
        return this.getUserPath() + "languages";
    }

    public String getUserPath() {
        return this.userPath;
    }

    // http://127.0.0.1:8000/ipay3_free/tools/block_proc/ERA
    public String getNotifyIncomingURL() {
        if (this.settingsJSON.containsKey("notify_incoming_url")) {
            return (String) this.settingsJSON.get("notify_incoming_url");
        }
        return NOTIFY_INCOMING_URL;
    }

    public int getNotifyIncomingConfirmations() {
        if (this.settingsJSON.containsKey("notify_incoming_confirmations")) {
            return (int) (long) this.settingsJSON.get("notify_incoming_confirmations");
        }

        return NOTIFY_INCOMING_CONFIRMATIONS;
    }

    public JSONArray getPeersJson() {
        if (this.peersJSON != null && this.peersJSON.containsKey("knownpeers")) {
            return (JSONArray) this.peersJSON.get("knownpeers");
        } else {
            return new JSONArray();
        }

    }

    @SuppressWarnings("unchecked")
    /**
     * полностью доверныые пиры от которых данные не проверяются - ни блоки ни транзакции
     */
    public List<String> getTrustedPeers() {

        try {

            File file = new File(this.userPath
                    + (BlockChain.DEVELOP_USE ? "peers-trusted-dev.json" : "peers-trusted.json"));

            //CREATE FILE IF IT DOESNT EXIST
            if (file.exists()) {
                //READ PEERS FILE
                List<String> lines = Files.readLines(file, Charsets.UTF_8);

                String jsonString = "";
                for (String line : lines) {
                    jsonString += line;
                }

                //CREATE JSON OBJECT
                return new ArrayList<String>((JSONArray) JSONValue.parse(jsonString));
            }
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }

        return new ArrayList<>();
    }

    public List<Peer> getKnownPeers() {
        try {
            boolean loadPeersFromInternet = (
                    Controller.getInstance().getToOfflineTime() != 0L
                            &&
                            NTP.getTime() - Controller.getInstance().getToOfflineTime() > 5 * 60 * 1000
            );

            List<Peer> knownPeers = new ArrayList<>();
            JSONArray peersArray = new JSONArray();

            if (!BlockChain.DEVELOP_USE) {
                try {
                    JSONArray peersArraySettings = (JSONArray) this.settingsJSON.get("knownpeers");

                    if (peersArraySettings != null) {
                        for (Object peer : peersArraySettings) {
                            if (!peersArray.contains(peer)) {
                                peersArray.add(peer);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    LOGGER.info("Error with loading knownpeers from settings.json.");
                }
            }

            try {
                JSONArray peersArrayPeers = (JSONArray) this.peersJSON.get("knownpeers");

                if (peersArrayPeers != null) {
                    for (Object peer : peersArrayPeers) {
                        if (!peersArray.contains(peer)) {
                            peersArray.add(peer);
                        }
                    }
                }

            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
                LOGGER.info("Error with loading knownpeers from peers.json.");
            }

            knownPeers.addAll(this.getPeersFromDefault());

            knownPeers.addAll(getKnownPeersFromJSONArray(peersArray));

            if (!BlockChain.DEVELOP_USE && (knownPeers.isEmpty() || loadPeersFromInternet)) {
                knownPeers.addAll(getKnownPeersFromInternet());
            }

            return knownPeers;

        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
            LOGGER.info("Error in getKnownPeers().");
            return new ArrayList<Peer>();
        }
    }

    public List<Peer> getKnownPeersFromInternet() {
        try {

            if (this.cacheInternetPeers == null) {

                this.cacheInternetPeers = new ArrayList<Peer>();
            }

            if (this.cacheInternetPeers.isEmpty() || NTP.getTime() - this.timeLoadInternetPeers > 24 * 60 * 60 * 1000) {
                this.timeLoadInternetPeers = NTP.getTime();
                URL u = new URL("https://raw.githubusercontent.com/icreator/ERMbase_public/master/peers.json");
                InputStream in = u.openStream();
                String stringInternetSettings = IOUtils.toString(in);
                JSONObject internetSettingsJSON = (JSONObject) JSONValue.parse(stringInternetSettings);
                JSONArray peersArray = (JSONArray) internetSettingsJSON.get("knownpeers");
                if (peersArray != null) {
                    this.cacheInternetPeers = getKnownPeersFromJSONArray(peersArray);
                }
            }

            //logger.info(Lang.getInstance().translate("Peers loaded from Internet : ") + this.cacheInternetPeers.size());

            return this.cacheInternetPeers;

        } catch (Exception e) {
            //RETURN EMPTY LIST

            //logger.debug(e.getMessage(), e);
            LOGGER.info(Lang.getInstance().translate("Peers loaded from Internet with errors : ") + this.cacheInternetPeers.size());

            return this.cacheInternetPeers;
        }
    }

    public List<Peer> getPeersFromDefault() {
        List<Peer> peers = new ArrayList<Peer>();
        for (int i = 0; i < this.defaultPeers.length; i++) {
            try {
                InetAddress address = InetAddress.getByName(this.defaultPeers[i]);

                if (!this.isLocalAddress(address)) {
                    //CREATE PEER
                    Peer peer = new Peer(address);

                    //ADD TO LIST
                    peers.add(peer);
                }
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
                LOGGER.info(this.defaultPeers[i] + " - invalid peer address!");
            }
        }
        return peers;
    }

    public List<Peer> getKnownPeersFromJSONArray(JSONArray peersArray) {
        try {
            //CREATE LIST WITH PEERS
            List<Peer> peers = new ArrayList<>();

            for (int i = 0; i < peersArray.size(); i++) {
                try {
                    InetAddress address = InetAddress.getByName((String) peersArray.get(i));

                    if (!this.isLocalAddress(address)) {
                        //CREATE PEER
                        Peer peer = new Peer(address);

                        //ADD TO LIST
                        peers.add(peer);
                    }
                } catch (Exception e) {
                    LOGGER.debug(e.getMessage(), e);
                    LOGGER.info((String) peersArray.get(i) + " - invalid peer address!");
                }
            }

            //RETURN
            return peers;
        } catch (Exception e) {
            //RETURN EMPTY LIST
            return new ArrayList<Peer>();
        }
    }

    public boolean isTestnet() {
        return this.getGenesisStamp() != BlockChain.DEFAULT_MAINNET_STAMP;
    }

    public long getGenesisStamp() {
        if (this.genesisStamp == -1) {
            if (this.settingsJSON.containsKey("testnetstamp")) {
                if (this.settingsJSON.get("testnetstamp").toString().equals("now") ||
                        ((Long) this.settingsJSON.get("testnetstamp")).longValue() == 0) {
                    this.genesisStamp = System.currentTimeMillis();
                } else {
                    this.genesisStamp = ((Long) this.settingsJSON.get("testnetstamp")).longValue();
                }
            } else {
                this.genesisStamp = BlockChain.DEFAULT_MAINNET_STAMP;
            }
        }

        return this.genesisStamp;
    }

    public void setGenesisStamp(long testNetStamp) {
        this.genesisStamp = testNetStamp;
    }

    public int getMaxConnections() {
        if (this.settingsJSON.containsKey("maxconnections")) {
            return ((Long) this.settingsJSON.get("maxconnections")).intValue();
        }

        return DEFAULT_MAX_CONNECTIONS;
    }

    public int getMaxReceivePeers() {
        if (this.settingsJSON.containsKey("maxreceivepeers")) {
            return ((Long) this.settingsJSON.get("maxreceivepeers")).intValue();
        }

        return DEFAULT_MAX_RECEIVE_PEERS;
    }

    public int getMaxSentPeers() {
        if (this.settingsJSON.containsKey("maxsentpeers")) {
            return ((Long) this.settingsJSON.get("maxsentpeers")).intValue();
        }

        return DEFAULT_MAX_SENT_PEERS;
    }

    public int getMinConnections() {
        if (this.settingsJSON.containsKey("minconnections")) {
            return ((Long) this.settingsJSON.get("minconnections")).intValue();
        }

        return DEFAULT_MIN_CONNECTIONS;
    }

    public int getConnectionTimeout() {
        if (this.settingsJSON.containsKey("connectiontimeout")) {
            return ((Long) this.settingsJSON.get("connectiontimeout")).intValue();
        }

        return DEFAULT_CONNECTION_TIMEOUT;
    }

    public boolean isTryingConnectToBadPeers() {
        if (this.settingsJSON.containsKey("tryingconnecttobadpeers")) {
            return ((Boolean) this.settingsJSON.get("tryingconnecttobadpeers")).booleanValue();
        }

        return DEFAULT_TRYING_CONNECT_TO_BAD_PEERS;
    }

    public int getRpcPort() {
        if (this.settingsJSON.containsKey("rpcport")) {
            return ((Long) this.settingsJSON.get("rpcport")).intValue();
        }

        return BlockChain.DEFAULT_RPC_PORT;
    }

    public String[] getRpcAllowed() {
        try {
            if (this.settingsJSON.containsKey("rpcallowed")) {
                //GET PEERS FROM JSON
                JSONArray allowedArray = (JSONArray) this.settingsJSON.get("rpcallowed");

                //CREATE LIST WITH PEERS
                String[] allowed = new String[allowedArray.size()];
                for (int i = 0; i < allowedArray.size(); i++) {
                    allowed[i] = (String) allowedArray.get(i);
                }

                //RETURN
                return allowed;
            }

            //RETURN
            return DEFAULT_RPC_ALLOWED.split(";");
        } catch (Exception e) {
            //RETURN EMPTY LIST
            return new String[0];
        }
    }

    public boolean isRpcEnabled() {
        if (this.settingsJSON.containsKey("rpcenabled")) {
            return ((Boolean) this.settingsJSON.get("rpcenabled")).booleanValue();
        }

        return DEFAULT_RPC_ENABLED;
    }

    public boolean getbacUpEnabled() {
        if (this.settingsJSON.containsKey("backupenabled")) {
            return ((Boolean) this.settingsJSON.get("backupenabled")).booleanValue();
        }

        return DEFAULT_BACUP_ENABLED;
    }

    public String getCompuRate() {
        if (this.settingsJSON.containsKey("compuRate")) {
            return ((String) this.settingsJSON.get("compuRate")).toString();
        }

        return "100";
    }

    public long getCompuRateAsset() {
        if (this.settingsJSON.containsKey("compuRateAsset")) {
            return  Long.valueOf(this.settingsJSON.get("compuRateAsset").toString());
        }

        return 95L;
    }

    public long getDefaultPairAssetKey() {
        if (this.settingsJSON.containsKey("defaultPairAsset")) {
            return  Long.valueOf(this.settingsJSON.get("defaultPairAsset").toString());
        }

        return 2L;
    }

    public AssetCls getDefaultPairAsset() {
        long key = getDefaultPairAssetKey();

        AssetCls asset = Controller.getInstance().getAsset(key);
        if (asset == null)
            asset = Controller.getInstance().getAsset(2L);

        return asset;
    }


    public boolean getbacUpAskToStart() {
        if (this.settingsJSON.containsKey("backupasktostart")) {
            return ((Boolean) this.settingsJSON.get("backupasktostart")).booleanValue();
        }

        return DEFAULT_BACKUP_ASK_ENABLED;
    }

    public int getWebPort() {
        if (this.settingsJSON.containsKey("webport")) {
            return ((Long) this.settingsJSON.get("webport")).intValue();
        }

        return BlockChain.DEFAULT_WEB_PORT;
    }

    public String getBlockexplorerURL() {
        if (this.settingsJSON.containsKey("explorerURL")) {
            return this.settingsJSON.get("explorerURL").toString();
        }

        return BlockChain.DEFAULT_EXPLORER;
    }

    public boolean isGuiConsoleEnabled() {
        if (this.settingsJSON.containsKey("guiconsoleenabled")) {
            return ((Boolean) this.settingsJSON.get("guiconsoleenabled")).booleanValue();
        }

        return DEFAULT_GUI_CONSOLE_ENABLED;
    }

    public String[] getWebAllowed() {
        try {
            if (this.settingsJSON.containsKey("weballowed")) {
                //GET PEERS FROM JSON
                JSONArray allowedArray = (JSONArray) this.settingsJSON.get("weballowed");

                //CREATE LIST WITH PEERS
                String[] allowed = new String[allowedArray.size()];
                for (int i = 0; i < allowedArray.size(); i++) {
                    allowed[i] = (String) allowedArray.get(i);
                }

                //RETURN
                return allowed;
            }

            //RETURN
            return DEFAULT_WEB_ALLOWED.split(";");
        } catch (Exception e) {
            //RETURN EMPTY LIST
            return new String[0];
        }
    }

    public boolean isWebEnabled() {
        if (this.settingsJSON.containsKey("webenabled")) {
            return ((Boolean) this.settingsJSON.get("webenabled")).booleanValue();
        }

        return DEFAULT_WEB_ENABLED;
    }

    public boolean updateNameStorage() {
        if (this.settingsJSON.containsKey("nsupdate")) {
            return ((Boolean) this.settingsJSON.get("nsupdate")).booleanValue();
        }

        return DEFAULT_NS_UPDATE;
    }

    public boolean isForgingEnabled() {
        try {
            if (this.settingsJSON.containsKey("forging")) {
                return ((Boolean) this.settingsJSON.get("forging")).booleanValue();
            }
        } catch (Exception e) {
            LOGGER.error("Bad Settings.json content for parameter forging " + ExceptionUtils.getStackTrace(e));
        }

        return DEFAULT_FORGING_ENABLED;
    }

    public boolean isGeneratorKeyCachingEnabled() {
        if (this.settingsJSON.containsKey("generatorkeycaching")) {
            return ((Boolean) this.settingsJSON.get("generatorkeycaching")).booleanValue();
        }

        return DEFAULT_GENERATOR_KEY_CACHING;
    }

    public boolean isCheckpointingEnabled() {
        if (this.settingsJSON.containsKey("checkpoint")) {
            return ((Boolean) this.settingsJSON.get("checkpoint")).booleanValue();
        }

        return DEFAULT_CHECKPOINTING;
    }

    public boolean isSoundReceivePaymentEnabled() {
        if (this.settingsJSON.containsKey("soundreceivepayment")) {
            return ((Boolean) this.settingsJSON.get("soundreceivepayment")).booleanValue();
        }

        return DEFAULT_SOUND_RECEIVE_COIN;
    }

    public boolean isSoundReceiveMessageEnabled() {
        if (this.settingsJSON.containsKey("soundreceivemessage")) {
            return ((Boolean) this.settingsJSON.get("soundreceivemessage")).booleanValue();
        }

        return DEFAULT_SOUND_MESSAGE;
    }

    public boolean isSoundNewTransactionEnabled() {
        if (this.settingsJSON.containsKey("soundnewtransaction")) {
            return ((Boolean) this.settingsJSON.get("soundnewtransaction")).booleanValue();
        }

        return DEFAULT_SOUND_NEW_TRANSACTION;
    }
	
	/*
	public int getMaxBytePerFee() 
	{
		if(this.settingsJSON.containsKey("maxbyteperfee"))
		{
			return ((Long) this.settingsJSON.get("maxbyteperfee")).intValue();
		}
		
		return DEFAULT_MAX_BYTE_PER_FEE;
	}
	*/

    public boolean isAllowFeeLessRequired() {
        if (this.settingsJSON.containsKey("allowfeelessrequired")) {
            return ((Boolean) this.settingsJSON.get("allowfeelessrequired")).booleanValue();
        }

        return ALLOW_FEE_LESS_REQUIRED;
    }

	/*
	public BigDecimal getBigFee() 
	{
		return DEFAULT_BIG_FEE;
	}
	*/

    public boolean isGuiEnabled() {

        if (!Controller.getInstance().doesWalletDatabaseExists()) {
            return true;
        }

        if (System.getProperty("nogui") != null) {
            return false;
        }
        if (this.settingsJSON.containsKey("guienabled")) {
            return ((Boolean) this.settingsJSON.get("guienabled")).booleanValue();
        }

        return DEFAULT_GUI_ENABLED;
    }

    public boolean isGuiDynamic() {
        if (this.settingsJSON.containsKey("guidynamic")) {
            return ((Boolean) this.settingsJSON.get("guidynamic")).booleanValue();
        }

        return DEFAULT_GUI_DYNAMIC;
    }

    public String getTimeZone() {
        if (this.settingsJSON.containsKey("timezone")) {
            return (String) this.settingsJSON.get("timezone");
        }

        return DEFAULT_TIME_ZONE;
    }

    public String getTimeFormat() {
        if (this.settingsJSON.containsKey("timeformat")) {
            return (String) this.settingsJSON.get("timeformat");
        }

        return DEFAULT_TIME_FORMAT;
    }

    // birth
    public String getBirthTimeFormat() {
        if (this.settingsJSON.containsKey("birthTimeformat")) {
            return (String) this.settingsJSON.get("birthTimeformat");
        }

        return DEFAULT_BIRTH_TIME_FORMAT;
    }


    public boolean isSysTrayEnabled() {
        if (this.settingsJSON.containsKey("systray")) {
            return ((Boolean) this.settingsJSON.get("systray")).booleanValue();
        }
        return true;
    }

    public boolean isLocalAddress(InetAddress address) {
        try {
            if (this.localAddress == null) {
                return false;
            } else {
                return address.equals(this.localAddress);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public InetAddress getCurrentIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces
                        .nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while (nias.hasMoreElements()) {
                    InetAddress ia = (InetAddress) nias.nextElement();
                    if (!ia.isLinkLocalAddress()
                            && !ia.isLoopbackAddress()
                            && ia instanceof Inet4Address) {
                        return ia;
                    }
                }
            }
        } catch (SocketException e) {
            LOGGER.info("unable to get current IP " + e.getMessage());
        }
        return null;
    }

    public String getLang() {
        if (this.settingsJSON.containsKey("lang")) {
            String langStr = (String)this.settingsJSON.get("lang");
            
            // delete .json
            String[] tokens = langStr.split("\\.");
            if (tokens.length > 1)
        	return tokens[0];
            return langStr;
        }

        return DEFAULT_LANGUAGE;
    }

    public String getLangFileName() {
	return getLang() + ".json";
    }

    public String get_Font() {
        if (this.settingsJSON.containsKey("font_size")) {
            return ((String) this.settingsJSON.get("font_size").toString());
        }

        return DEFAULT_FONT_SIZE.toString();

    }

    public String get_File_Chooser_Paht() {
        if (this.settingsJSON.containsKey("FileChooser_Path")) {
            return ((String) this.settingsJSON.get("FileChooser_Path").toString());
        }

        return getUserPath();

    }

    public int get_File_Chooser_Wight() {
        if (this.settingsJSON.containsKey("FileChooser_Wight")) {
            return new Integer(this.settingsJSON.get("FileChooser_Wight").toString());
        }

        return 0;

    }

    public int get_File_Chooser_Height() {
        if (this.settingsJSON.containsKey("FileChooser_Height")) {
            return new Integer(this.settingsJSON.get("FileChooser_Height").toString());
        }

        return 0;

    }

    public String get_Font_Name() {
        if (this.settingsJSON.containsKey("font_name")) {
            return ((String) this.settingsJSON.get("font_name").toString());
        }

        return DEFAULT_FONT_NAME;
    }

    public String get_Theme() {

        if (this.settingsJSON.containsKey("theme")) {
            return ((String) this.settingsJSON.get("theme").toString());
        }

        return DEFAULT_THEME;
    }

    public String get_LookAndFell() {

        if (this.settingsJSON.containsKey("LookAndFell")) {
            return ((String) this.settingsJSON.get("LookAndFell").toString());
        }

        return DEFAULT_THEME;


    }
    
   

    public String cutPath(String path) {

        //if (!(this.userPath.endsWith("\\")
        if (path.endsWith("/")) {
            path.substring(0, path.length() - 1);
            path += File.separator;
        }

        return path;


    }

    public JSONObject read_setting_JSON() {
        int alreadyPassed = 0;
        File file = new File(this.userPath + "settings.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
                JSONObject json = new JSONObject();
                json.put("!!!ver", "3.0");
                return json;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            while (alreadyPassed < 2) {
                //OPEN FILE
                //READ SETTINS JSON FILE
                List<String> lines = Files.readLines(file, Charsets.UTF_8);

                String jsonString = "";
                for (String line : lines) {

                    //correcting single backslash bug
                    if (line.contains("userpath")) {
                        line = line.replace("\\", File.separator);
                    }

                    jsonString += line;
                }

                //CREATE JSON OBJECT
                this.settingsJSON = (JSONObject) JSONValue.parse(jsonString);
                settingsJSON = settingsJSON == null ? new JSONObject() : settingsJSON;


                alreadyPassed++;

                if (this.settingsJSON.containsKey("userpath")) {
                    this.userPath = (String) this.settingsJSON.get("userpath");
                } else {
                    alreadyPassed++;
                }
                // read Telegrams parameters
                if (this.settingsJSON.containsKey("Telegram_Sender")){ 
                    telegramDefaultSender =(String)this.settingsJSON.get("Telegram_Sender");
                }   
                
                if (this.settingsJSON.containsKey("Telegram_Reciever")){
                    telegramDefaultReciever = (String)this.settingsJSON.get("Telegram_Reciever");
                }
                
                if (this.settingsJSON.containsKey("Telegram_Ratio_Reciever")){
                    telegramRatioReciever = (String)this.settingsJSON.get("Telegram_Ratio_Reciever");
                }
                // read BackUb Path
                if (this.settingsJSON.containsKey("backuppath")) {
                    this.getBackUpPath = (String) this.settingsJSON.get("backuppath");


                    try {
                        if ( false && !(this.getBackUpPath.endsWith("\\") || this.getBackUpPath.endsWith("/") || this.getBackUpPath.endsWith(File.separator))) {
                            this.getBackUpPath += File.separator;
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        this.getBackUpPath = "";
                    }

                } else {
                    this.getBackUpPath = "";

                }

                if (this.settingsJSON.containsKey("walletdir")) {
                    this.getWalletPath = (String) this.settingsJSON.get("walletdir");

                } else {
                    this.getWalletPath = "";
                }

                // set data dir getDataPath
                if (this.settingsJSON.containsKey("datadir")) {
                    this.dataPath = (String) this.settingsJSON.get("datadir");

                    try {
                        if ( false && !(this.dataPath.endsWith("\\") || this.dataPath.endsWith("/") || this.dataPath.endsWith(File.separator))) {
                            this.dataPath += File.separator;
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        this.dataPath = "";
                    }
                } else {
                    this.dataPath = "";
                }

                //CREATE FILE IF IT DOESNT EXIST
                if (!file.exists()) {
                    file.createNewFile();
                }
            }

        } catch (Exception e) {
            LOGGER.info("Error while reading/creating settings.json " + file.getAbsolutePath() + " using default!");
            LOGGER.error(e.getMessage(), e);
            settingsJSON = new JSONObject();
        }

        return settingsJSON;

    }
    
    public JSONObject getJSONObject(){
        return this.settingsJSON;
    }

    public String getTelegramRatioReciever() {
        // TODO Auto-generated method stub
        return this.telegramRatioReciever;
    }
    
    public void setTelegramRatioReciever(String str) {
        // TODO Auto-generated method stub
        this.telegramRatioReciever = str;
    }


}
