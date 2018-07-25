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
import datachain.DCSet;
import gui.models.WalletOrdersTableModel;
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

    private boolean run = true;

    public Trader(TradersManager tradersManager, String accountStr, int sleepSec) {

        this.cnt = Controller.getInstance();
        this.dcSet = DCSet.getInstance();
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
        treeMap.put(order.a.a, new Tuple2<>(order, status));
        schemeOrders.put(amount, treeMap);
    }

    protected synchronized boolean removeOrder(BigInteger orderID) {
        return null != orders.remove(orderID);
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

            schemeOrdersPut(amount, Base58.decode((String)jsonObject.get("signature")));
            return true;
        }

        return false;

    }

    private boolean cancelOrder(BigInteger orderID) {

        String result;

        result = this.apiClient.executeCommand("GET trade/cancel/" + this.address + "/" + Base58.encode(orderID)
                + "?password=" + TradersManager.WALLET_PASSWORD);
        LOGGER.info("CANCEL: " + result);

        Fun.Tuple3 orderInChain = this.dcSet.getOrderMap().get(this.orders.firstKey());


        result = this.apiClient.executeCommand("GET trade/cancel/" + this.address + "/" + Base58.encode(orderID)
                + "?password=" + TradersManager.WALLET_PASSWORD);
        LOGGER.info("CANCEL: " + result);

        JSONObject jsonObject = null;
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

        if (jsonObject != null && !jsonObject.containsKey("error")) {
            return true;
        }

        return false;

    }

    private boolean notInCancelingArray(BigInteger orderID, JSONArray array) {
        String signatureOrder = Base58.encode(orderID);
        if (array != null) {
            for (int i=0; i < array.size(); i++) {
                JSONObject item = (JSONObject) array.get(i);
                if (item.containsKey("orderID"))
                    if (item.get("orderID").equals(signatureOrder))
                        return false;
            }
        }

        return true;

    }

    private void shiftAll() {

        // REMOVE ALL ORDERS

        // CHECK MY SELL ORDERS in CAP
        for (Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>
                order: dcSet.getOrderMap().getOrdersSForAddress(this.address, this.haveKey, this.wantKey)) {

            // IS IT MY ORDER?
            // BY HAVE AMOUNT
            if (scheme.get(order.b.b) != null)
                this.orders.put(order.a.a, order);

        }

        // CHECK MY BUY ORDERS in CAP
        for (Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>
                order: dcSet.getOrderMap().getOrdersSForAddress(this.address, this.wantKey, this.haveKey)) {

            // IS IT MY ORDER?
            // BY WANT AMOUNT
            if (scheme.get(order.c.b.negate()) != null)
                this.orders.put(order.a.a, order);
            this.orders.put(order.a.a, order);

        }

        String result = this.apiClient.executeCommand("GET transactions/unconfirmedof/" + this.address);

        JSONArray arrayUnconfirmed = null;
        try {
            //READ JSON
            arrayUnconfirmed = (JSONArray) JSONValue.parse(result);
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            LOGGER.info(e);
        }

        // CANCEL ALL MY ORDERS
        BigInteger orderID;
        while (!this.orders.keySet().isEmpty()) {
            // RESOLVE SYNCHRONIZE REMOVE
            orderID = this.orders;
            Fun.Tuple3 orderInChain = this.dcSet.getOrderMap().get(orderID);
            if (orderInChain != null
                    && notInCancelingArray(orderID, arrayUnconfirmed)) {
                cancelOrder(orderID);
            }

            removeOrder(orderID);

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                //FAILED TO SLEEP
            }

        }

        try {
            Thread.sleep(BlockChain.GENERATING_MIN_BLOCK_TIME_MS);
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
