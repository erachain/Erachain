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

    protected static final BigDecimal M100 = new BigDecimal(100).setScale(0);

    private TradersManager tradersManager;
    private long sleepTimestep;

    protected WalletOrdersTableModel ordersTableModel;

    protected Controller cnt;
    protected DCSet dcSet;
    protected OrderMap ordersMap;
    protected CallRemoteApi caller;
    protected ApiClient apiClient;

    protected Account account;
    protected String address;
    protected String apiURL;
    protected BigDecimal shiftRate = BigDecimal.ONE;
    protected Long haveKey;
    protected Long wantKey;
    protected AssetCls haveAsset;
    protected AssetCls wantAsset;
    protected BigDecimal rate;
    protected BigDecimal limitUP = new BigDecimal(0.01);
    protected BigDecimal limitDown = new BigDecimal(0.01);

    protected static final int STATUS_INCHAIN = 2;
    protected static final int STATUS_UNFCONFIRMED = -1;

    // KEY -> ORDER
    protected TreeMap<BigInteger, Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = new TreeMap<>();

    // AMOUNT + SPREAD
    protected TreeMap<BigDecimal, BigDecimal> scheme;

    // AMOUNT -> Tree Map of (ORDER.Tuple3 + his STATUS)
    protected TreeMap<BigDecimal, TreeMap<BigInteger, Fun.Tuple2<
            Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                    Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>,
            Integer>>> schemeOrders = new TreeMap();

    // AMOUNT -> Tree Set of SIGNATURE
    protected TreeMap<BigDecimal, TreeSet<String>> schemeUnconfirmeds = new TreeMap();

    private boolean run = true;

    public Trader(TradersManager tradersManager, String accountStr, int sleepSec) {

        this.cnt = Controller.getInstance();
        this.dcSet = DCSet.getInstance();
        this.ordersMap = this.dcSet.getOrderMap();
        this.caller = new CallRemoteApi();
        this.apiClient = new ApiClient();


        this.account = new Account(accountStr);
        this.address = accountStr;
        this.tradersManager = tradersManager;
        this.sleepTimestep = sleepSec * 1000;

        this.setName("Thread Trader - " + this.getClass().getName());

        this.start();
    }

    public TreeMap<BigInteger, Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrders() {
        return this.orders;
    }

    protected synchronized void schemeOrdersPut(BigDecimal amount, Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order, Integer status) {
        TreeMap<BigInteger, Tuple2<Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>, Integer>>
                treeMap = schemeOrders.get(amount);
        if (treeMap == null)
            treeMap = new TreeMap();

        treeMap.put(order.a.a, new Tuple2<>(order, status));
        schemeOrders.put(amount, treeMap);
    }
    protected synchronized boolean schemeOrdersRemove(BigDecimal amount, BigInteger orderID, Integer status) {
        TreeMap<BigInteger, Tuple2<Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>, Integer>>
                treeMap = schemeOrders.get(amount);

        boolean removed = treeMap.remove(orderID) == null;
        schemeOrders.put(amount, treeMap);
        return removed;
    }

    protected synchronized void schemeUnconfirmedsPut(BigDecimal amount, String signatire) {
        TreeSet<String> treeSet = schemeUnconfirmeds.get(amount);
        if (treeSet == null)
            treeSet = new TreeSet();

        treeSet.add(signatire);
        schemeUnconfirmeds.put(amount, treeSet);
    }

    protected synchronized boolean schemeUnconfirmedsRemove(BigDecimal amount, String signature) {
        TreeSet<String> treeSet = schemeUnconfirmeds.get(amount);
        boolean removed = treeSet.remove(signature);
        schemeUnconfirmeds.put(amount, treeSet);
        return removed;
    }

    protected synchronized void removeOrderFromAll(BigDecimal amount, BigInteger orderID) {
        orders.remove(orderID);
        schemeUnconfirmedsRemove(amount, Base58.encode(orderID));
        schemeOrdersRemove(amount, orderID, STATUS_UNFCONFIRMED);
    }

    private boolean createOrder(BigDecimal amount) {

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

            amountHave = amount;
            amountWant = amount.multiply(this.rate).multiply(shift)
                    .setScale(wantAsset.getScale(), BigDecimal.ROUND_HALF_UP);
        } else {
            haveKey = this.wantKey;
            wantKey = this.haveKey;

            BigDecimal shift = BigDecimal.ONE.subtract(shiftPercentage.movePointLeft(2));

            amountWant = amount.negate().setScale(haveAsset.getScale(), BigDecimal.ROUND_HALF_UP);
            amountHave = amountWant.multiply(this.rate).multiply(shift)
                    .setScale(wantAsset.getScale(), BigDecimal.ROUND_HALF_UP);
        }

        result = this.apiClient.executeCommand("GET trade/create/" + this.address + "/" + haveKey + "/" + wantKey
                + "/" + amountHave + "/" + amountWant + "?password=" + TradersManager.WALLET_PASSWORD);
        LOGGER.info("CREATE: " + result);

        JSONObject jsonObject = null;
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

        if (jsonObject.containsKey("signature")) {
            schemeUnconfirmedsPut(amount, (String)jsonObject.get("signature"));
            return true;
        }

        return false;

    }

    private boolean cancelOrder(BigDecimal amount, BigInteger orderID) {

        String result;

        result = this.apiClient.executeCommand("GET trade/get/" + Base58.encode(orderID));
        LOGGER.info("GET: " + Base58.encode(orderID) + "\n" + result);

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
        result = this.apiClient.executeCommand("GET trade/cancel/" + this.address + "/" + Base58.encode(orderID)
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

        if (jsonObject == null) {
            LOGGER.info("CANCEL: " + Base58.encode(orderID) + "\n" + result);
            return false;
        }

        if (jsonObject.containsKey("signature")) {
            schemeUnconfirmedsPut(amount, Base58.encode(orderID));

            return true;
        }

        LOGGER.info("CANCEL: " + Base58.encode(orderID) + "\n" + result);

        return false;

    }

    private TreeSet<BigInteger> makeCancelingArray(JSONArray array) {

        TreeSet<BigInteger> cancelingArray = new TreeSet();
        if (array != null && !array.isEmpty()) {
            for (int i=0; i < array.size(); i++) {
                JSONObject transaction = (JSONObject) array.get(i);
                Transaction transactin = dcSet.getTransactionMap().get(Base58.decode((String) transaction.get("signature")));
                transactin.setDC(dcSet, false);
                if (transactin.isValid(null, 0l) != Transaction.VALIDATE_OK) {
                    dcSet.getTransactionMap().delete(Base58.decode((String) transaction.get("signature")));
                    continue;
                }
                if (((Long)transaction.get("type")).intValue() == Transaction.CANCEL_ORDER_TRANSACTION) {
                    cancelingArray.add(new BigInteger(Base58.decode((String) transaction.get("orderID"))));
                }
            }
        }

        return cancelingArray;

    }

    // REMOVE ALL ORDERS
    private void removaAll() {

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
        TreeSet<BigInteger> cancelsIsUnconfirmed = makeCancelingArray(arrayUnconfirmed);
        BigDecimal amount;

        // CHECK MY SELL ORDERS in CAP
        for (Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>
                order: this.ordersMap.getOrdersForAddress(this.address, this.haveKey, this.wantKey)) {

            // IS IT MY ORDER?
            // BY HAVE AMOUNT
            if (scheme.get(order.b.b) != null
                    && !cancelsIsUnconfirmed.contains(order.a.a)) {
                //this.orders.put(order.a.a, order);
                schemeOrdersPut(order.b.b, order, STATUS_INCHAIN);
            }

        }

        // CHECK MY BUY ORDERS in CAP
        for (Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>
                order: this.ordersMap.getOrdersForAddress(this.address, this.wantKey, this.haveKey)) {

            // IS IT MY ORDER?
            // BY WANT AMOUNT
            if (scheme.get(order.c.b.negate()) != null
                    && !cancelsIsUnconfirmed.contains(order.a.a)) {
                //this.orders.put(order.a.a, order);
                schemeOrdersPut(order.c.b.negate(), order, STATUS_INCHAIN);
            }

        }


        // CHECK MY ORDER in UNCONFIRMED
        for (Object json: arrayUnconfirmed) {

            JSONObject transaction = (JSONObject)json;
            if (((Long)transaction.get("type")).intValue() == Transaction.CREATE_ORDER_TRANSACTION) {
                if (((Long)transaction.get("haveKey")).equals(this.haveKey)
                        && ((Long)transaction.get("wantKey")).equals(this.wantKey)) {
                    // IS IT MY ORDER BY HAVE AMOUNT
                    amount = new BigDecimal ((String)transaction.get("amountHave"));
                    if (scheme.get(amount) != null) {
                        schemeUnconfirmedsPut(amount, (String)transaction.get("signature"));
                    }
                } else if (((Long)transaction.get("haveKey")).equals(this.wantKey)
                        && ((Long)transaction.get("wantKey")).equals(this.wantKey)) {
                    // IS IT MY ORDER BY WANT AMOUNT
                    amount = new BigDecimal ((String)transaction.get("amountWant")).negate();
                    if (scheme.get(amount) != null) {
                        schemeUnconfirmedsPut(amount, (String)transaction.get("signature"));
                    }
                }
            }
        }

        // CANCEL ALL MY ORDERS in UNCONFIRMED

        BigInteger orderID;
        for (BigDecimal amountKey: this.schemeOrders.keySet()) {
            TreeMap<BigInteger, Tuple2<Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>, Integer>>
                    schemeItems = this.schemeOrders.get(amountKey);
            for (Tuple2<Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                    Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>, Integer> orderSet: schemeItems.values()) {
                // RESOLVE SYNCHRONIZE REMOVE

                orderID = orderSet.a.a.a;
                result = this.apiClient.executeCommand("GET transactions/signature/" + Base58.encode(orderID));
                JSONObject transaction = null;
                try {
                    //READ JSON
                    transaction = (JSONObject) JSONValue.parse(result);
                } catch (NullPointerException | ClassCastException e) {
                    //JSON EXCEPTION
                    LOGGER.info(e);
                }

                if (transaction == null)
                    continue;

                if (((Long) transaction.get("haveKey")).equals(this.haveKey)) {
                    amount = new BigDecimal((String) transaction.get("amountHave"));
                } else {
                    amount = new BigDecimal((String) transaction.get("amountWant")).negate();
                }
                cancelOrder(amount, orderID);

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    //FAILED TO SLEEP
                }
            }

        }

    }
    private void shiftAll() {

        // REMOVE ALL ORDERS
        removaAll();

        try {
            Thread.sleep(BlockChain.GENERATING_MIN_BLOCK_TIME_MS<<1);
        } catch (Exception e) {
            //FAILED TO SLEEP
        }

        //BigDecimal persent;
        for(BigDecimal amount: this.scheme.keySet()) {
            //persent = this.scheme.get(amount);
            createOrder(amount.setScale(this.haveAsset.getScale(), BigDecimal.ROUND_HALF_UP));
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                //FAILED TO SLEEP
            }

        }
    }

    private boolean process() {

        String callerResult = null;

        TreeMap<Fun.Tuple3<Long, Long, String>, BigDecimal> rates = Rater.getRates();
        BigDecimal newRate = rates.get(new Fun.Tuple3<Long, Long, String>(this.haveKey, this.wantKey, "wex"));
        if (newRate != null) {
            if (this.rate == null) {
                if (newRate == null)
                    return false;

                this.rate = newRate;
                shiftAll();

            } else {
                if (newRate == null || newRate.compareTo(this.rate) == 0)
                    return false;

                BigDecimal diffPerc = newRate.divide(this.rate, 8, BigDecimal.ROUND_HALF_UP)
                        .subtract(BigDecimal.ONE).multiply(Trader.M100);
                if (diffPerc.compareTo(this.limitUP) > 0
                        || diffPerc.abs().compareTo(this.limitDown) > 0) {

                    this.rate = newRate;
                    shiftAll();
                }
            }
        }

        try {
        } catch (Exception e) {
            //FAILED TO SLEEP
            return false;
        }

        return true;
    }

    public void run() {

        while (this.run) {

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                //FAILED TO SLEEP
            }

            if (!this.run) {
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
