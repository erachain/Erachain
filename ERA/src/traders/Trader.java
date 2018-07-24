package traders;
// 30/03

import api.ApiClient;
import api.ApiErrorFactory;
import controller.Controller;
import core.account.Account;
import core.crypto.Base58;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import core.transaction.CreateOrderTransaction;
import datachain.DCSet;
import gui.models.WalletOrdersTableModel;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import settings.Settings;
import test.SettingTests;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public abstract class Trader extends Thread {

    private static final Logger LOGGER = Logger.getLogger(Trader.class);

    protected static final BigDecimal M100 = new BigDecimal(100).setScale(0);

    protected static TreeMap<BigInteger, Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = new TreeMap<>();

    private TradersManager tradersManager;
    private long sleepTimestep;

    protected WalletOrdersTableModel ordersTableModel;

    protected Controller cnt;
    protected DCSet dcSet;
    protected CallRemoteApi caller;

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

    // AMOUNT + SPREAD
    protected TreeMap<BigDecimal, BigDecimal> scheme;
    // AMOUNT + ORDER SIGNATUTE
    protected TreeMap<BigDecimal, byte[]> schemeOrders;

    private boolean run = true;

    public Trader(TradersManager tradersManager, String accountStr, int sleepSec) {

        this.cnt = Controller.getInstance();
        this.dcSet = DCSet.getInstance();
        this.caller = new CallRemoteApi();

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

    protected synchronized void schemeOrdersPut(BigDecimal amount, byte[] signature) {
        schemeOrders.put(amount, signature);
    }

    protected synchronized void removeOrders(BigInteger orderID) {
        orders.remove(orderID);
    }

    private boolean createOrder(BigDecimal amount) {

        ApiClient ApiClient = new ApiClient();
        String result;
        //String result = ApiClient.executeCommand("POST wallet/unlock " + TradersManager.WALLET_PASSWORD);

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
            amountWant = amount.divide(this.rate.multiply(shift), wantAsset.getScale(), BigDecimal.ROUND_HALF_UP);
        } else {
            haveKey = this.wantKey;
            wantKey = this.haveKey;

            BigDecimal shift = BigDecimal.ONE.subtract(shiftPercentage.movePointLeft(2));

            amountWant = amount.negate();
            amountHave = amountWant.multiply(this.rate).multiply(shift)
                    .setScale(wantAsset.getScale(), BigDecimal.ROUND_HALF_UP);
        }

        result = ApiClient.executeCommand("GET trade/create/" + this.address + "/" + haveKey + "/" + wantKey
                + "/" + amountHave + "/" + amountWant + "?password=" + TradersManager.WALLET_PASSWORD);
        LOGGER.info(result);

        JSONObject jsonObject = null;
        try {
            //READ JSON
            jsonObject = (JSONObject) JSONValue.parse(result);
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            LOGGER.info(e);
            //throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        } finally {
            ApiClient.executeCommand("GET wallet/lock");
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

        ApiClient ApiClient = new ApiClient();
        String result = ApiClient.executeCommand("POST wallet/unlock " + TradersManager.WALLET_PASSWORD);

        //String resultAddresses = new ApiClient().executeCommand("GET addresses");
        //String[] parse = (resultAddresses.replace("\r\n", "").split(","));
        //String address = parse[1].replace("[", "").replace("]", "").trim().replace("\"", "");


        result = ApiClient.executeCommand("GET trade/cancel/" + orderID);
        LOGGER.info("CANCEL " + orderID + " result:\n" + result);

        JSONObject jsonObject = null;
        try {
            //READ JSON
            jsonObject = (JSONObject) JSONValue.parse(result);
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            LOGGER.info(e);
            //throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        } finally {
            ApiClient.executeCommand("GET wallet/lock");
        }

        if (jsonObject != null && !jsonObject.containsKey("error")) {
            return true;
        }

        return false;

    }

    private void shiftAll() {

        // REMOVE ALL ORDERS
        for (BigInteger orderID: this.orders.keySet()) {

            Fun.Tuple3 orderInChain = this.dcSet.getOrderMap().get(orderID);
            if (orderInChain == null)
                continue;

            if (false && !cancelOrder(orderID))
                break;

            removeOrders(orderID);

        }

        //BigDecimal persent;
        for(BigDecimal amount: this.scheme.keySet()) {
            //persent = this.scheme.get(amount);
            createOrder(amount);
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

        // CHECJ MY ORDERS from WALLET
        this.ordersTableModel = new WalletOrdersTableModel();
        for (int i=0; i < this.ordersTableModel.getRowCount(); i++) {
            Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = this.ordersTableModel.getOrder(i);
            Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> orderInChain = this.dcSet.getOrderMap().get(order.a.a);
            if (orderInChain == null)
                continue;

            if (order.b.a.equals(this.haveKey) && order.c.a.equals(this.wantKey)
                || order.b.a.equals(this.wantKey) && order.c.a.equals(this.haveKey)) {
                CreateOrderTransaction createTx = (CreateOrderTransaction) cnt.getTransaction(orderInChain.a.a.toByteArray());
                if (createTx.getCreator().equals(this.account) && createTx.)
                    this.orders.put(orderInChain.a.a, orderInChain);
            }

        }

        while (this.run) {

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                //FAILED TO SLEEP
            }

            try {

                this.process();

                //SLEEP
                try {
                    Thread.sleep(sleepTimestep);
                } catch (InterruptedException e) {
                    //FAILED TO SLEEP
                }

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

    }

    public void close() {
        this.run = false;
    }
}
