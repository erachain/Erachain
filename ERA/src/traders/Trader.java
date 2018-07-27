package traders;
// 30/03

import api.ApiClient;
import api.ApiErrorFactory;
import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.crypto.Base58;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import core.transaction.CreateOrderTransaction;
import core.transaction.Transaction;
import datachain.DCSet;
import datachain.OrderMap;
import gui.models.WalletOrdersTableModel;
import org.apache.commons.lang3.BitField;
import org.apache.log4j.Logger;
import org.bouncycastle.jcajce.provider.symmetric.ARC4;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import settings.Settings;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


public abstract class Trader extends Thread {

    private static final Logger LOGGER = Logger.getLogger(Trader.class);

    private static final int INVALID_TIMESTAMP = 7;
    private static final int ORDER_DOES_NOT_EXIST = 36;

    protected static final BigDecimal M100 = new BigDecimal(100).setScale(0);

    private TradersManager tradersManager;
    private long sleepTimestep;

    protected Controller cnt;
    protected DCSet dcSet;
    protected OrderMap ordersMap;
    protected CallRemoteApi caller;
    protected ApiClient apiClient;

    protected boolean cleanAllOnStart;
    protected Account account;
    protected String address;
    protected BigDecimal shiftRate = BigDecimal.ONE;
    protected Long haveKey;
    protected Long wantKey;
    protected AssetCls haveAsset;
    protected AssetCls wantAsset;
    protected BigDecimal rate;

    // in PERCENT
    protected BigDecimal limitUP;
    protected BigDecimal limitDown;

    protected static final int STATUS_INCHAIN = 2;
    protected static final int STATUS_UNFCONFIRMED = -1;

    // KEY -> ORDER
    //protected TreeSet<BigInteger> orders = new TreeSet<>();

    // AMOUNT + SPREAD
    protected HashMap<BigDecimal, BigDecimal> scheme;

    // AMOUNT -> Tree Map of (ORDER.Tuple3 + his STATUS)
    protected HashMap<BigDecimal, HashSet<String>> schemeOrders = new HashMap();

    // AMOUNT -> Tree Set of SIGNATURE
    protected HashMap<BigDecimal, HashSet<String>> unconfirmedsCancel = new HashMap();

    private boolean run = true;

    public Trader(TradersManager tradersManager, String accountStr, int sleepSec,
                  HashMap<BigDecimal, BigDecimal> scheme, Long haveKey, Long wantKey,
                  BigDecimal limitUP, BigDecimal limitDown, // = new BigDecimal("0.1")
            boolean cleanAllOnStart) {

        this.cnt = Controller.getInstance();
        this.dcSet = DCSet.getInstance();
        this.ordersMap = this.dcSet.getOrderMap();
        this.caller = new CallRemoteApi();
        this.apiClient = new ApiClient();

        this.cleanAllOnStart = cleanAllOnStart;
        this.account = new Account(accountStr);
        this.address = accountStr;
        this.tradersManager = tradersManager;
        this.sleepTimestep = sleepSec * 1000;

        this.scheme = scheme;

        this.haveKey = haveKey;
        this.wantKey = wantKey;

        this.haveAsset = dcSet.getItemAssetMap().get(haveKey);
        this.wantAsset = dcSet.getItemAssetMap().get(wantKey);

        this.limitUP = limitUP;
        this.limitDown = limitDown;

        this.setName("Thread Trader - " + this.getClass().getName());

        this.start();
    }

    //public HashSet<BigInteger> getOrders() {
    //    return this.orders;
   // }

    protected synchronized void schemeOrdersPut(BigDecimal amount, String orderID) {
        HashSet<String> set = schemeOrders.get(amount);
        if (set == null)
            set = new HashSet();

        set.add(orderID);
        schemeOrders.put(amount, set);
    }

    protected synchronized boolean schemeOrdersRemove(BigDecimal amount, String orderID) {
        HashSet<String> set = schemeOrders.get(amount);

        if (set == null || set.isEmpty())
            return false;

        boolean removed = set.remove(orderID);
        schemeOrders.put(amount, set);
        return removed;
    }

    /*
    protected synchronized void unconfirmedsCancelPut(BigDecimal amount, String signatire) {
        TreeSet<String> treeSet = unconfirmedsCancel.get(amount);
        if (treeSet == null)
            treeSet = new TreeSet();

        treeSet.add(signatire);
        unconfirmedsCancel.put(amount, treeSet);
    }

    protected synchronized boolean unconfirmedsCancelRemove(BigDecimal amount, String signature) {
        TreeSet<String> treeSet = unconfirmedsCancel.get(amount);
        boolean removed = treeSet.remove(signature);
        unconfirmedsCancel.put(amount, treeSet);
        return removed;
    }
    */

    protected boolean createOrder(BigDecimal amount) {

        String result;
        BigDecimal shiftPercentage = this.scheme.get(amount);

        long haveKey;
        long wantKey;

        BigDecimal amountHave;
        BigDecimal amountWant;

        if (amount.signum() > 0) {
            haveKey = this.haveKey;
            wantKey = this.wantKey;

            BigDecimal shift = BigDecimal.ONE.add(shiftPercentage.movePointLeft(2));

            amountHave = amount.stripTrailingZeros();
            amountWant = amountHave.multiply(this.rate).multiply(shift).stripTrailingZeros();

            // NEED SCALE for VALIDATE
            if (amountWant.scale() > this.wantAsset.getScale()) {
                amountWant = amountWant.setScale(wantAsset.getScale(), BigDecimal.ROUND_HALF_UP);
            }

        } else {
            haveKey = this.wantKey;
            wantKey = this.haveKey;

            BigDecimal shift = BigDecimal.ONE.subtract(shiftPercentage.movePointLeft(2));

            amountWant = amount.negate().stripTrailingZeros();
            amountHave = amountWant.multiply(this.rate).multiply(shift).stripTrailingZeros();

            // NEED SCALE for VALIDATE
            if (amountHave.scale() > this.wantAsset.getScale()) {
                amountHave = amountHave.setScale(wantAsset.getScale(), BigDecimal.ROUND_HALF_UP);
            }
        }

        LOGGER.info("TRY CREATE " + amountHave.toString() + " : " + amountWant.toString());

        JSONObject jsonObject = null;
        // TRY MAKE ORDER in LOOP
        do {

            result = this.apiClient.executeCommand("GET trade/create/" + this.address + "/" + haveKey + "/" + wantKey
                    + "/" + amountHave + "/" + amountWant + "?password=" + TradersManager.WALLET_PASSWORD);

            try {
                //READ JSON
                jsonObject = (JSONObject) JSONValue.parse(result);
            } catch (NullPointerException | ClassCastException e) {
                //JSON EXCEPTION
                LOGGER.info(e);
            } finally {
                this.apiClient.executeCommand("GET wallet/lock");
            }

            if (jsonObject == null)
                return false;

            if (jsonObject.containsKey("signature"))
                break;

            int error = ((Long)jsonObject.get("error")).intValue();
            if (error == INVALID_TIMESTAMP) {
                // INVALIT TIMESTAMP
                LOGGER.info("CREATE - TRY ANEW");
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    //FAILED TO SLEEP
                }
                continue;
            }

            LOGGER.info("CREATE: " + result);
            return false;

        } while (true);

        this.schemeOrdersPut(amount, (String)jsonObject.get("signature"));
        return true;

    }

    protected boolean cancelOrder(String orderID) {

        String result;

        result = this.apiClient.executeCommand("GET trade/get/" + orderID);
        //LOGGER.info("GET: " + Base58.encode(orderID) + "\n" + result);

        JSONObject jsonObject = null;
        try {
            //READ JSON
            jsonObject = (JSONObject) JSONValue.parse(result);
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            LOGGER.info(e);
            //throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }
        if (!jsonObject.containsKey("ID")) {
            return false;
        }

        jsonObject = null;

        do {
            result = this.apiClient.executeCommand("GET trade/cancel/" + this.address + "/" + orderID
                    + "?password=" + TradersManager.WALLET_PASSWORD);

            try {
                //READ JSON
                jsonObject = (JSONObject) JSONValue.parse(result);
            } catch (NullPointerException | ClassCastException e) {
                //JSON EXCEPTION
                LOGGER.info(e);
                //throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            } finally {
                this.apiClient.executeCommand("GET wallet/lock");
            }

            if (jsonObject == null)
                return false;

            if (jsonObject.containsKey("signature"))
                break;

            int error = ((Long) jsonObject.get("error")).intValue();
            if (error == INVALID_TIMESTAMP) {
                // INVALIT TIMESTAMP
                LOGGER.info("CANCEL - TRY ANEW");
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    //FAILED TO SLEEP
                }
                continue;
            } else if (error == ORDER_DOES_NOT_EXIST) {
                // ORDER not EXIST - as DELETED
                return true;
            }

            LOGGER.info("CANCEL: " + orderID + "\n" + result);
            return false;

        } while(true);

        return true;

    }

    protected HashSet<String> makeCancelingArray(JSONArray array) {

        HashSet<String> cancelingArray = new HashSet();
        if (array == null || array.isEmpty())
            return cancelingArray;

        for (int i=0; i < array.size(); i++) {
            JSONObject transactionJSON = (JSONObject) array.get(i);
            Transaction transaction = dcSet.getTransactionMap().get(Base58.decode((String) transactionJSON.get("signature")));
            if (transaction == null)
                continue;
            transaction.setDC(dcSet, false);
            if (transaction.isValid(null, 0l) != Transaction.VALIDATE_OK) {
                dcSet.getTransactionMap().delete(transaction.getSignature());
                continue;
            }
            if (transaction.getType() == Transaction.CANCEL_ORDER_TRANSACTION) {
                cancelingArray.add((String) transactionJSON.get("orderID"));
            }
        }

        return cancelingArray;

    }

    // REMOVE ALL ORDERS
    protected void removaAll() {

        String result = this.apiClient.executeCommand("GET transactions/unconfirmedof/" + this.address);

        JSONArray arrayUnconfirmed = null;
        try {
            //READ JSON
            arrayUnconfirmed = (JSONArray) JSONValue.parse(result);
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            LOGGER.info(e);
        }

        // GET CANCELS in UNCONFIRMEDs
        HashSet<String> cancelsIsUnconfirmed = makeCancelingArray(arrayUnconfirmed);
        BigDecimal amount;
        String orderID;

        // CHECK MY ORDERs in UNCONFIRMED
        for (Object json: arrayUnconfirmed) {

            JSONObject transaction = (JSONObject)json;
            if (((Long)transaction.get("type")).intValue() == Transaction.CREATE_ORDER_TRANSACTION) {
                if (((Long)transaction.get("haveKey")).equals(this.haveKey)
                        && ((Long)transaction.get("wantKey")).equals(this.wantKey)
                    || ((Long)transaction.get("haveKey")).equals(this.wantKey)
                        && ((Long)transaction.get("wantKey")).equals(this.wantKey)) {

                    orderID = (String)transaction.get("signature");
                    // IF not aldeady CANCEL in WAITING
                    if (cancelsIsUnconfirmed.contains(orderID))
                        continue;

                    cancelOrder(orderID);

                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        //FAILED TO SLEEP
                    }
                }
            }
        }

        // CHECK MY SELL ORDERS in CAP
        for (Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>
                order: this.ordersMap.getOrdersForAddress(this.address, this.haveKey, this.wantKey)) {

            orderID = Base58.encode(order.a.a);
            if (cancelsIsUnconfirmed.contains(orderID))
                continue;

            if (this.scheme.containsKey(orderID)) {
                schemeOrdersRemove(order.b.b, orderID);
            }
            cancelOrder( orderID);

            try {
                Thread.sleep(100);
            } catch (Exception e) {
                //FAILED TO SLEEP
            }

        }

        // CHECK MY BUY ORDERS in CAP
        for (Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>
                order: this.ordersMap.getOrdersForAddress(this.address, this.wantKey, this.haveKey)) {

            orderID = Base58.encode(order.a.a);
            if (cancelsIsUnconfirmed.contains(orderID))
                continue;

            if (this.scheme.containsKey(order.c.b.negate())) {
                schemeOrdersRemove(order.c.b.negate(), orderID);
            }
            cancelOrder(orderID);

            try {
                Thread.sleep(100);
            } catch (Exception e) {
                //FAILED TO SLEEP
            }

        }

        try {
            Thread.sleep(BlockChain.GENERATING_MIN_BLOCK_TIME_MS<<1);
        } catch (Exception e) {
            //FAILED TO SLEEP
        }

    }

    // REMOVE ALL ORDERS
    protected boolean cleanSchemeOrders() {

        boolean cleaned = false;

        if (this.schemeOrders == null || this.schemeOrders.isEmpty())
            return cleaned;

        // CANCEL ALL MY ORDERS in UNCONFIRMED

        BigDecimal amount;
        String result;
        JSONObject transaction = null;
        for (BigDecimal amountKey: this.schemeOrders.keySet()) {

            HashSet<String> schemeItems = this.schemeOrders.get(amountKey);

            if (schemeItems == null || schemeItems.isEmpty())
                continue;

            for (String orderID: schemeItems) {

                // IF that TRANSACTION exist in CHAIN or queue
                result = this.apiClient.executeCommand("GET transactions/signature/" + orderID);
                try {
                    //READ JSON
                    transaction = (JSONObject) JSONValue.parse(result);
                } catch (NullPointerException | ClassCastException e) {
                    //JSON EXCEPTION
                    LOGGER.info(e);
                }

                if (transaction == null || !transaction.containsKey("signature"))
                    continue;

                cleaned = cancelOrder(orderID);

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    //FAILED TO SLEEP
                }
            }

            // CLEAR map
            schemeItems.clear();
            this.schemeOrders.put(amountKey, schemeItems);
        }

        // CLEAR cancels
        unconfirmedsCancel.clear();
        return cleaned;
    }



    protected abstract boolean process();

    public void run() {

        if (cleanAllOnStart) {
            removaAll();
        }

        Controller cntr = Controller.getInstance();

        while (this.run) {

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                //FAILED TO SLEEP
            }

            if (!cntr.isStatusOK() ||
                    !this.run) {
                continue;
            }

            try {

                this.process();

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            //SLEEP
            try {
                Thread.sleep(sleepTimestep);
            } catch (InterruptedException e) {
                //FAILED TO SLEEP
            }

        }

    }

    public void close() {
        this.run = false;
    }
}
