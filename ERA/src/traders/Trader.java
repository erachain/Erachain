package traders;
// 30/03

import api.ApiClient;
import controller.Controller;
import core.account.Account;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import core.transaction.CreateOrderTransaction;
import datachain.DCSet;
import gui.models.WalletOrdersTableModel;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
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

    protected static TreeMap<BigInteger, Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = new TreeMap<>();

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

    private boolean run = true;


    public Trader(TradersManager tradersManager, String accountStr, int sleepSec, TreeMap<BigDecimal, BigDecimal> scheme,
                  Long haveKey, Long wantKey) {

        this.cnt = Controller.getInstance();
        this.dcSet = DCSet.getInstance();
        this.caller = new CallRemoteApi();

        this.account = new Account(accountStr);
        this.address = accountStr;
        this.tradersManager = tradersManager;
        this.sleepTimestep = sleepSec * 1000;
        this.scheme = scheme;

        this.haveKey = haveKey;
        this.wantKey = wantKey;

        this.haveAsset = dcSet.getItemAssetMap().get(haveKey);
        this.wantAsset = dcSet.getItemAssetMap().get(wantKey);

        this.setName("Thread Trader - " + this.getClass().getName());

        this.start();
    }

    protected abstract void parse(String result);

    public TreeMap<BigInteger, Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrders() {
        return this.orders;
    }

    private void createOrder(BigDecimal amount) {

        ApiClient ApiClient = new ApiClient();
        ApiClient.executeCommand("POST wallet/unlock " + TradersManager.WALLET_PASSWORD);

        //String resultAddresses = new ApiClient().executeCommand("GET addresses");
        //String[] parse = (resultAddresses.replace("\r\n", "").split(","));
        //String address = parse[1].replace("[", "").replace("]", "").trim().replace("\"", "");

        BigDecimal shiftPercentage = this.scheme.get(amount);
        BigDecimal price = this.rate.multiply(BigDecimal.ONE.add(shiftPercentage.movePointLeft(2)));
        BigDecimal total = amount.multiply(price).setScale(wantAsset.getScale());

        String result = ApiClient.executeCommand("GET trade/create" + this.address + "/" + this.haveKey + "/" + this.wantKey
                amount + "/" + total);
        LOGGER.info(result);


        ApiClient.executeCommand("GET wallet/lock");

    }

    private void cancelOrder(BigInteger orderID) {

        ApiClient ApiClient = new ApiClient();
        ApiClient.executeCommand("POST wallet/unlock " + TradersManager.WALLET_PASSWORD);

        //String resultAddresses = new ApiClient().executeCommand("GET addresses");
        //String[] parse = (resultAddresses.replace("\r\n", "").split(","));
        //String address = parse[1].replace("[", "").replace("]", "").trim().replace("\"", "");


        String result = ApiClient.executeCommand("GET trade/cancel/" + orderID);
        LOGGER.info("CANCEL " + orderID + " result:\n" + result);


        ApiClient.executeCommand("GET wallet/lock");

    }

    private void shiftAll() {

        // REMOVE ALL ORDERS
        for (BigInteger orderID: this.orders.keySet()) {

            this.orders.remove(orderID);

            Fun.Tuple3 orderInChain = this.dcSet.getOrderMap().get(orderID);
            if (orderInChain == null)
                continue;

            cancelOrder(orderID);

        }
    }

    private boolean process() {

        String callerResult = null;

        TreeMap<Fun.Tuple3<Long, Long, String>, BigDecimal> rates = Rater.getRates();
        BigDecimal newRate = rates.get(new Fun.Tuple3<Long, Long, String>(this.want, this.have, "wex"));
        if (newRate != null) {
            if (this.rate == null || !newRate.equals(this.rate)) {
                BigDecimal diffPerc = newRate.divide(this.rate, 8, BigDecimal.ROUND_HALF_UP)
                        .subtract(BigDecimal.ONE).multiply(Trader.M100);
                if (diffPerc.compareTo(this.limitUP) > 0
                        || diffPerc.abs().compareTo(this.limitDown) > 0) {
                    shiftAll();
                }
            }
        }

        try {
            callerResult = caller.ResponseValueAPI(this.apiURL, "GET", "");
            this.parse(callerResult);
        } catch (Exception e) {
            //FAILED TO SLEEP
            return false;
        }

        return true;
    }

    public void run() {

        while(!cnt.doesWalletExists()) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }

        // CHECJ MY ORDERS from WALLET
        this.ordersTableModel = new WalletOrdersTableModel();
        for (int i=0; i < this.ordersTableModel.getRowCount(); i++) {
            Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = this.ordersTableModel.getOrder(i);
            Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> orderInChain = this.dcSet.getOrderMap().get(order.a.a);
            if (orderInChain == null)
                continue;

            CreateOrderTransaction createTx = (CreateOrderTransaction) cnt.getTransaction(orderInChain.a.a.toByteArray());
            if (createTx.getCreator().equals(this.account))
                this.orders.put(orderInChain.a.a, orderInChain);

        }


        int sleepTimeFull = Settings.getInstance().getPingInterval();

        while (this.run) {

            try {
                this.process();
                Thread.sleep(1000);
            } catch (Exception e) {
                //FAILED TO SLEEP
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
