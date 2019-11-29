package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.datachain.OrderMap;
import org.erachain.datachain.TransactionFinalMapImpl;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.ntp.NTP;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Path("trade")
@Produces(MediaType.APPLICATION_JSON)
public class TradeResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TradeResource.class);
    @Context
    HttpServletRequest request;

    @GET
    public String help() {

        Map<String, String> help = new LinkedHashMap<String, String>();
        help.put("GET trade/rater/[start/stop]",
                "Start Rater: 1 - start, 0 - stop");
        help.put("GET trade/create/[creator]/[haveKey]/[wantKey]/[haveAmount]/[wantAmount]?feePow=[feePow]&password=[password]",
                "make and broadcast CreateOrder");
        help.put("GET trade/get/[signature]",
                "Get Order");
        help.put("GET trade/orders/[have]/[want]?limit=[limit]",
                "Get tradeorders for HaveKey & WantKey, "
                        + "limit is count record. The number of orders is limited by input param, default 20.");
        help.put("GET trade/trades/[have]/[want]?timestamp=[timestamp]&limit=[limit]",
                "Get trades for HaveKey & WantKey, "
                        + "limit is count record. The number of trades is limited by input param, default 50.");
        help.put("GET trade/getbyaddress/[creator]/[haveKey]/[wantKey]",
                "get list of orders in CAP by address");
        help.put("GET trade/cancel/[creator]/[signature]?password=[password]",
                "Cancel Order");

        return StrJSonFine.convert(help);
    }

    @GET
    @Path("rater/{status}")
    public String rater(@PathParam("status") Long status) {


        return "+";
    }

    /**
     * send and broadcast GET
     *
     * @param creatorStr address in wallet
     * @param haveKey    haveKey
     * @param wantKey    wantKey
     * @param haveAmount haveAmount or head
     * @param wantAmount wantAmount
     * @param feePower   fee Power
     * @param password   password
     * @return JSON row
     *
     * <h2>Example request</h2>
     * GET create/7GvWSpPr4Jbv683KFB5WtrCCJJa6M36QEP/2/1/1.0/100.0?password=123456789
     * <h2>Example response</h2>
     * {}
     */
    @GET
    @Path("create/{creator}/{haveKey}/{wantKey}/{haveAmount}/{wantAmount}")
    public String sendGet(@PathParam("creator") String creatorStr,
                          @PathParam("haveKey") Long haveKey, @PathParam("wantKey") Long wantKey,
                          /// STRING for AMOUNT!!! SCALE is GOOD
                          @PathParam("haveAmount") BigDecimal haveAmount, @PathParam("wantAmount") BigDecimal wantAmount,
                          @DefaultValue("0") @QueryParam("feePow") Long feePower, @QueryParam("password") String password) {

        APIUtils.askAPICallAllowed(password, "GET create Order\n ", request, true);

        Controller cntr = Controller.getInstance();

        // READ CREATOR
        Fun.Tuple2<Account, String> resultCreator = Account.tryMakeAccount(creatorStr);
        if (resultCreator.b != null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_MAKER_ADDRESS);
        }

        AssetCls haveAsset = cntr.getAsset(haveKey);
        if (haveAsset == null)
            throw ApiErrorFactory.getInstance().createError(Transaction.ITEM_ASSET_NOT_EXIST);

        AssetCls wantAsset = cntr.getAsset(wantKey);
        if (wantAsset == null)
            throw ApiErrorFactory.getInstance().createError(Transaction.ITEM_ASSET_NOT_EXIST);

        PrivateKeyAccount privateKeyAccount = cntr.getPrivateKeyAccountByAddress(resultCreator.a.getAddress());
        if (privateKeyAccount == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_WALLET_ADDRESS);
        }

        Transaction transaction = cntr.createOrder(privateKeyAccount, haveAsset, wantAsset,
                haveAmount, //new BigDecimal(haveAmount), // becouse String .setScale(haveAsset.getScale()),
                wantAmount, //new BigDecimal(wantAmount), //.setScale(wantAsset.getScale()),
                feePower.intValue());

        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            JSONObject out = new JSONObject();
            out.put("error", validate);
            out.put("error_message", OnDealClick.resultMess(validate));
            return out.toJSONString();
        }

    }

    @GET
    @Path("get/{signature}")
    public String get(@PathParam("signature") String signatureStr) {

        byte[] signature;
        try {
            signature = Base58.decode(signatureStr);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
        }

        if (DCSet.getInstance().getTransactionTab().contains(signature)) {
            JSONObject out = new JSONObject();
            out.put("unconfirmed", true);
            return out.toJSONString();
        }

        Long key = DCSet.getInstance().getTransactionFinalMapSigns().get(signature);
        if (key == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.ORDER_DOES_NOT_EXIST);
        }

        Long orderID = key;
        if (DCSet.getInstance().getOrderMap().contains(orderID)) {
            JSONObject out = DCSet.getInstance().getOrderMap().get(orderID).toJson();
            out.put("active", true);
            return out.toJSONString();
        } else {
            Order order = DCSet.getInstance().getCompletedOrderMap().get(orderID);
            JSONObject out = order.toJson();
            if (order.isFulfilled()) {
                out.put("completed", true);
            } else {
                out.put("canceled", true);
            }
            return out.toJSONString();
        }

    }

    /**
     * send and broadcast GET
     *
     * @param creatorStr   address in wallet
     * @param signatureStr signature
     * @param feePower     fee Power
     * @param password     password
     * @return JSON row
     *
     * <h2>Example request</h2>
     * GET cancelbyid/7GvWSpPr4Jbv683KFB5WtrCCJJa6M36QEP/1234567898765432134567899876545678?password=123456789
     * <h2>Example response</h2>
     * {}
     */
    @GET
    @Path("cancel/{creator}/{signature}")
    public String cancel(@PathParam("creator") String creatorStr,
                         @PathParam("signature") String signatureStr,
                         @DefaultValue("0") @QueryParam("feePow") Long feePower, @QueryParam("password") String password) {

        APIUtils.askAPICallAllowed(password, "GET create Order\n ", request, true);

        byte[] signature;
        try {
            signature = Base58.decode(signatureStr);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
        }

        // READ CREATOR
        Fun.Tuple2<Account, String> resultCreator = Account.tryMakeAccount(creatorStr);
        if (resultCreator.b != null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_MAKER_ADDRESS);
        }

        Controller cntr = Controller.getInstance();

        if (!DCSet.getInstance().getTransactionTab().contains(signature)) {
            // ЕСЛИ нет его в неподтвержденных то пытаемся найти в действующих
            Long key = DCSet.getInstance().getTransactionFinalMapSigns().get(signature);
            if (key == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.ORDER_DOES_NOT_EXIST);
            }

            Long orderID = key;
            if (!DCSet.getInstance().getOrderMap().contains(orderID)) {
                throw ApiErrorFactory.getInstance().createError(Transaction.ORDER_DOES_NOT_EXIST);
            }
        }


        PrivateKeyAccount privateKeyAccount = cntr.getPrivateKeyAccountByAddress(resultCreator.a.getAddress());
        if (privateKeyAccount == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_WALLET_ADDRESS);
        }

        Transaction transaction = cntr.cancelOrder2(privateKeyAccount, signature, feePower.intValue());

        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            JSONObject out = new JSONObject();
            out.put("error", validate);
            out.put("error_message", OnDealClick.resultMess(validate));
            return out.toJSONString();
        }

    }

    @GET
    @Path("orders/{have}/{want}")
    // orders/1/2?imit=4
    public static String getOrders(@PathParam("have") Long have, @PathParam("want") Long want,
                                   @DefaultValue("20") @QueryParam("limit") Long limit) {

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();

        // DOES ASSETID EXIST
        if (have == null || !map.contains(have)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }
        if (want == null || !map.contains(want)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        int limitInt = limit.intValue();

        List<Order> haveOrders = DCSet.getInstance().getOrderMap().getOrders(have, want, limitInt);
        List<Order> wantOrders = DCSet.getInstance().getOrderMap().getOrders(want, have, limitInt);

        JSONObject result = new JSONObject();

        JSONArray arrayHave = new JSONArray();
        for (Order order: haveOrders) {
            JSONObject json = new JSONObject();
            json.put("id", order.getId());
            json.put("seqNo", Transaction.viewDBRef(order.getId()));
            json.put("creator", order.getCreator().getAddress());
            json.put("amount", order.getAmountHaveLeft().toPlainString());
            json.put("total", order.getAmountWantLeft().toPlainString());
            json.put("price", order.getPrice().toPlainString());
            arrayHave.add(json);
        }
        result.put("have", arrayHave);

        JSONArray arrayWant = new JSONArray();
        for (Order order: wantOrders) {
            JSONObject json = new JSONObject();
            json.put("id", order.getId());
            json.put("seqNo", Transaction.viewDBRef(order.getId()));
            json.put("creator", order.getCreator().getAddress());
            // get REVERSE price and AMOUNT
            json.put("amount", order.getAmountWantLeft().toPlainString());
            json.put("total", order.getAmountHaveLeft().toPlainString());
            json.put("price", order.calcPriceReverse().toPlainString());
            arrayWant.add(json);
        }
        result.put("want", arrayWant);

        result.put("haveKey", have);
        result.put("wantKey", want);
        result.put("limited", limitInt);

        return result.toJSONString();
    }

    /**
     * Get trades by timestamp. The number of transactions is limited by input
     * param.
     *
     * @param have      is account
     * @param want      is account two
     * @param timestamp value time
     * @param limit     count out record
     * @return record trades
     * @author Ruslan
     */

    @GET
    @Path("trades/{have}/{want}")
    // /trades/1/2?timestamp=3&limit=4
    public static String getTradesFromTimestamp(@PathParam("have") Long have, @PathParam("want") Long want,
                                                @DefaultValue("0") @QueryParam("timestamp") Long timestamp,
                                                @DefaultValue("50") @QueryParam("limit") Long limit) {

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        // DOES ASSETID EXIST
        if (have == null || !map.contains(have)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }
        if (want == null || !map.contains(want)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        int limitInt = limit.intValue();
        List<Trade> listResult = Controller.getInstance().getTradeByTimestmp(have, want, timestamp * 1000, limitInt);

        JSONArray arrayJSON = new JSONArray();
        for (Trade trade: listResult) {
            arrayJSON.add(trade.toJson(have));
        }

        return arrayJSON.toJSONString();
    }

    @GET
    @Path("getbyaddress/{creator}/{haveKey}/{wantKey}")
    public String cancel(@PathParam("creator") String address,
                         @PathParam("haveKey") Long haveKey, @PathParam("wantKey") Long wantKey) {


        OrderMap ordersMap = DCSet.getInstance().getOrderMap();
        TransactionFinalMapImpl finalMap = DCSet.getInstance().getTransactionFinalMap();
        Transaction createOrder;

        JSONArray out = new JSONArray();
        for( Order order: ordersMap.getOrdersForAddress(address, haveKey, wantKey)) {
            JSONObject orderJson = order.toJson();
            Long key = order.getId();
            createOrder = finalMap.get(key);
            if (createOrder == null)
                continue;

            orderJson.put("signature", Base58.encode(createOrder.getSignature()));

            out.add(orderJson);

        }

        return out.toJSONString();
    }

    private static long test1Delay = 0;
    private static float test1probability = 0;
    private static Thread threadTest1;
    private static List<PrivateKeyAccount> test1Creators;

    /**
     * GET trade/test1/0.85/1000
     * @param probability
     * @param delay
     * @param password
     * @return
     */
    @GET
    @Path("test1/{probability}/{delay}")
    public String test1(@PathParam("probability") float probability, @PathParam("delay") long delay, @QueryParam("password") String password) {

        if (!BlockChain.DEVELOP_USE
                && ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))
        )
            return "not LOCAL && not DEVELOP";

        APIUtils.askAPICallAllowed(password, "GET trade/test1\n ", request, true);

        this.test1Delay = delay;
        this.test1probability = probability;

        if (threadTest1 != null) {
            JSONObject out = new JSONObject();
            if (delay <= 0) {
                threadTest1 = null;
                out.put("status", "STOP");
                LOGGER.info("trade/test1 STOP");
            } else {
                out.put("delay", delay);
                LOGGER.info("trade/test1 DELAY UPDATE:" + delay);
            }
            return out.toJSONString();
        }

        Controller controller = Controller.getInstance();

        // CACHE private keys
        test1Creators = controller.getPrivateKeyAccounts();

        // запомним счетчики для счетов
        HashMap<String, Long> counters = new HashMap<String, Long>();
        for (Account crestor: test1Creators) {
            counters.put(crestor.getAddress(), 0L);
        }

        JSONObject out = new JSONObject();

        if (test1Creators.size() <= 1) {
            out.put("error", "too small accounts");

            return out.toJSONString();
        }

        threadTest1 = new Thread(() -> {

            DCSet dcSet = DCSet.getInstance();

            Random random = new Random();
            Controller cnt = controller;

            AssetCls haveStart = controller.getAsset(1L);
            AssetCls wantStart = controller.getAsset(2L);

            BigDecimal rateStart = new BigDecimal("0.0005");
            BigDecimal rateStartRev = BigDecimal.ONE.divide(rateStart, 8, RoundingMode.HALF_DOWN);

            BigDecimal amounHaveStart = new BigDecimal("0.1");
            BigDecimal amounWantStart = amounHaveStart.multiply(rateStart);

            Transaction transaction;
            HashMap<String, String> orders = new HashMap<>();

            do {

                if (this.test1Delay <= 0) {
                    return;
                }

                if (cnt.isOnStopping())
                    return;

                // если есть вероятногсть по если не влазим в нее то просто ожидание и пропуск ходя
                if (test1probability < 1 && test1probability > 0) {
                    int rrr = random.nextInt((int) (100.0 / test1probability) );
                    if (rrr > 100) {
                        try {
                            Thread.sleep(this.test1Delay);
                        } catch (InterruptedException e) {
                            break;
                        }

                        continue;
                    }
                }

                String address;
                long counter;

                try {

                    // если отключены непротокольные индексы то не найдем ничего для счета этого
                    if (controller.onlyProtocolIndexing) {

                    } else {


                        // check all created orders
                        for (PrivateKeyAccount account : test1Creators) {
                            List<Order> addressOrders = dcSet.getOrderMap().getOrdersForAddress(account.getAddress(), haveStart.getKey(), wantStart.getKey());
                            for (Order order : addressOrders) {
                                Transaction createTx = dcSet.getTransactionFinalMap().get(order.getId());
                                if (createTx != null) {
                                    // add as my orders
                                    DCSet forkCreator = cnt.getTransactionCreator().getFork();
                                    if (forkCreator != null && !order.isActive(forkCreator)) {
                                        // если заказ уже был отменен но в неподтвержденных отмена лежит
                                        continue;
                                    }

                                    orders.put(createTx.viewSignature(), order.getCreator().getAddress());
                                }
                            }
                        }
                    }

                    if (orders.size() / test1Creators.size() >= 5) {
                        // если уже много ордеров на один счет то попробуем удалить какие-то
                        for (String txSign: orders.keySet()) {

                            Transaction orderCreateTx = dcSet.getTransactionFinalMap().get(Base58.decode(txSign));
                            if (orderCreateTx == null) {
                                // еше не подтвердилась
                                continue;
                            }

                            if (dcSet.getOrderMap().contains(orderCreateTx.getDBRef())) {
                                // создаем отмену ордера
                                address = orderCreateTx.getCreator().getAddress();
                                counter = counters.get(address);

                                PrivateKeyAccount privateKey = null;
                                for (PrivateKeyAccount crestor: test1Creators) {
                                    if (crestor.equals(orderCreateTx.getCreator())) {
                                        privateKey = crestor;
                                        break;
                                    }
                                }

                                if (privateKey == null)
                                    continue;

                                Pair<Transaction, Integer> cancelResult = cnt.cancelOrder(privateKey, orderCreateTx.getSignature(), 0);
                                int result = cancelResult.getB();

                                if (result == Transaction.VALIDATE_OK) {
                                    counters.put(address, counter - 1);
                                    orders.remove(txSign);
                                    break;

                                } else {
                                    // not work in Threads - logger.info("TEST1: " + OnDealClick.resultMess(result));
                                    try {
                                        Thread.sleep(10000);
                                    } catch (InterruptedException e) {
                                        break;
                                    }
                                    continue;
                                }
                            } else {
                                // уже сыгранный
                                orders.remove(txSign);
                            }

                        }

                        continue;

                    } else {
                        PrivateKeyAccount creator = test1Creators.get(random.nextInt(test1Creators.size()));
                        Account recipient;
                        do {
                            recipient = test1Creators.get(random.nextInt(test1Creators.size()));
                        } while (recipient.equals(creator));

                        AssetCls have = null;
                        BigDecimal haveAmount = null;
                        AssetCls want = null;
                        BigDecimal wantAmount = null;

                        if (random.nextBoolean()) {
                            have = haveStart;
                            haveAmount = amounHaveStart;
                            want = wantStart;
                            wantAmount = amounWantStart.multiply(new BigDecimal((1050.0 - random.nextInt(100)) / 1000.0))
                                    .setScale(wantStart.getScale(), RoundingMode.HALF_DOWN);
                        } else {
                            have = wantStart;
                            haveAmount = amounWantStart.multiply(new BigDecimal((1050.0 - random.nextInt(100)) / 1000.0))
                                    .setScale(wantStart.getScale(), RoundingMode.HALF_DOWN);
                            want = haveStart;
                            wantAmount = amounHaveStart;
                        }

                        address = creator.getAddress();
                        counter = counters.get(address);

                        if (cnt.isOnStopping())
                            return;

                        if (false) {
                            transaction = cnt.createOrder(creator,
                                    have, want, haveAmount, wantAmount, 0);

                            Integer result = cnt.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);
                            // CLEAR for HEAP
                            transaction.setDC(null);

                            // CHECK VALIDATE MESSAGE
                            if (result == Transaction.VALIDATE_OK) {
                                orders.put(transaction.viewSignature(), address);
                                counters.put(address, counter + 1);

                            } else {
                                if (result == Transaction.NO_BALANCE
                                        || result == Transaction.NOT_ENOUGH_FEE
                                ) {

                                    try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException e) {
                                        break;
                                    }

                                    continue;
                                }

                                // not work in Threads - logger.info("TEST1: " + OnDealClick.resultMess(result));
                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException e) {
                                    break;
                                }
                                continue;
                            }
                        } else {

                            long time = NTP.getTime();

                            //CREATE ORDER TRANSACTION
                            transaction = new CreateOrderTransaction(creator, have.getKey(), want.getKey(),
                                    haveAmount, wantAmount, (byte) 0, time, 0l);

                            transaction.sign(creator, Transaction.FOR_NETWORK);

                            // карта сбрасывается иногда при очистке, поэтому надо брать свежую всегда
                            cnt.transactionsPool.offerMessage(transaction);
                            cnt.broadcastTransaction(transaction);

                        }

                    }

                    try {
                        Thread.sleep(this.test1Delay);
                    } catch (InterruptedException e) {
                    }

                    if (cnt.isOnStopping())
                        return;

                } catch (Exception e10) {
                    // not see in Thread - logger.error(e10.getMessage(), e10);
                    String error = e10.getMessage();
                    error += "";
                } catch (Throwable e10) {
                }

            } while (true);
        });

        threadTest1.setName("Trade.Test1");
        threadTest1.start();

        out.put("delay", test1Delay);
        LOGGER.info("trade/test1 STARTED for delay: " + test1Delay);

        return out.toJSONString();

    }

}
