package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.controller.PairsController;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.transaction.CancelOrderTransaction;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.datachain.*;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.ntp.NTP;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;
import org.erachain.utils.StrJSonFine;
import org.erachain.webserver.API;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.lang.ref.WeakReference;
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
        help.put("GET trade/create/[creator]/[amountAssetKey]/[priceAssetKey]/[haveAmount]/[wantAmount]?feePow=[feePow]&password=[password]",
                "make and broadcast CreateOrder");
        help.put("GET trade/get/[seqNo|signature]",
                "Get Order by seqNo or Signature. For example: 4321-2");
        help.put("GET trade/ordersbook/[have]/[want]?limit=[limit]",
                "Get active orders in orderbook for amountAssetKey & priceAssetKey, "
                        + "limit is count record. The number of orders is limited by input param, default 20.");
        help.put("GET trade/completedordersfrom/[have]/[want]?order=[orderID]&height=[height]&time=[timestamp]&limit=[limit]",
                "Get completed orders for amountAssetKey & priceAssetKey, "
                        + "limit is count record. The number of orders is limited by input param, default 50."
                        + "Use Order ID as Block-seqNo or Long. For example 103506-3 or 928735142671");
        help.put("GET trade/trades/[have]/[want]?timestamp=[timestamp]&limit=[limit]",
                "Get trades for amountAssetKey & priceAssetKey, "
                        + "limit is count record. The number of trades is limited by input param, default 50.");
        help.put("GET trade/tradesfrom/[have]/[want]?order=[orderID]&height=[height]&time=[timestamp]&limit=[limit]",
                "Get trades for amountAssetKey & priceAssetKey, "
                        + "limit is count record. The number of trades is limited by input param, default 50."
                        + "Use Order ID as Block-seqNo or Long. For example 103506-3 or 928735142671");
        help.put("GET trade/tradesfrom/[address]/[have]/[want]?trade=[TradeID}&order=[orderID]&height=[height]&time=[timestamp]&limit=[limit]",
                "Get trades for amountAssetKey & priceAssetKey for creator [address], "
                        + "limit is count record. The number of trades is limited by input param, default 50."
                        + "Use Order ID as Block-seqNo. For example 103506-3. Use TradeID as Initiator_OrderID/Target_OrderID");
        help.put("GET trade/ordersbyaddress/[creator]/[amountAssetKey]/[priceAssetKey]?limit=[limit]",
                "get list of orders in CAP by address for trade pair");

        help.put("GET trade/allordersbyaddress/{address}/{from}?limit=[20]&desc={false}",
                "get list of ALL orders (in CAP and completed) by address from OrderID. "
                        + "Use Order ID as Block-seqNo or Long. For example 103506-3 or 928735142671");
        help.put("GET trade/allordersbyaddress/{address}?from={SeqNo}&limit=[20]&desc={false}",
                "get list of ALL orders (in CAP and completed) by address from OrderID. "
                        + "Use Order ID as Block-seqNo or Long. For example 103506-3 or 928735142671");

        help.put("GET trade/cancel/[creator]/[signature]?password=[password]",
                "Cancel Order");

        help.put("GET trade/updatepairs/[days]",
                "Update pairs stat by trades deep days. May be need after resynchronization");

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
     * @param creatorStr    address in wallet
     * @param haveKey       haveKey
     * @param priceAssetKey priceAssetKey
     * @param haveAmount    haveAmount or head
     * @param wantAmount    wantAmount
     * @param feePower      fee Power
     * @param password      password
     * @return JSON row
     *
     * <h2>Example request</h2>
     * GET create/7GvWSpPr4Jbv683KFB5WtrCCJJa6M36QEP/2/1/1.0/100.0?password=123456789
     * <h2>Example response</h2>
     * {}
     */
    @GET
    @Path("create/{creator}/{haveKey}/{priceAssetKey}/{haveAmount}/{wantAmount}")
    public String sendGet(@PathParam("creator") String creatorStr,
                          @PathParam("haveKey") Long haveKey, @PathParam("priceAssetKey") Long priceAssetKey,
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

        AssetCls wantAsset = cntr.getAsset(priceAssetKey);
        if (wantAsset == null)
            throw ApiErrorFactory.getInstance().createError(Transaction.ITEM_ASSET_NOT_EXIST);

        PrivateKeyAccount privateKeyAccount = cntr.getWalletPrivateKeyAccountByAddress(resultCreator.a.getAddress());
        if (privateKeyAccount == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_WALLET_ADDRESS);
        }

        Transaction transaction = cntr.createOrder(privateKeyAccount, haveAsset, wantAsset,
                haveAmount, //new BigDecimal(haveAmount), // becouse String .setScale(haveAsset.getScale()),
                wantAmount, //new BigDecimal(wantAmount), //.setScale(wantAsset.getScale()),
                feePower.intValue());

        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);

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
    public static String getOrder(@PathParam("signature") String signatureStr) {

        Long orderID;
        try {
            orderID = Transaction.parseDBRef(signatureStr);
        } catch (Exception e) {
            orderID = null;
        }

        if (orderID == null) {
            byte[] signature;
            try {
                signature = Base58.decode(signatureStr);
            } catch (Exception e1) {
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

            orderID = key;

        }

        if (DCSet.getInstance().getOrderMap().contains(orderID)) {
            JSONObject out = DCSet.getInstance().getOrderMap().get(orderID).toJson();
            out.put("active", true);
            return out.toJSONString();
        } else {
            Order order = DCSet.getInstance().getCompletedOrderMap().get(orderID);
            if (order == null)
                throw ApiErrorFactory.getInstance().createError(Transaction.ORDER_DOES_NOT_EXIST);

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


        PrivateKeyAccount privateKeyAccount = cntr.getWalletPrivateKeyAccountByAddress(resultCreator.a.getAddress());
        if (privateKeyAccount == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_WALLET_ADDRESS);
        }

        Transaction transaction = cntr.cancelOrder2(privateKeyAccount, signature, feePower.intValue());

        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);

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
    @Path("ordersbook/{have}/{want}")
    // orders/1/2?imit=4
    public static String getOrdersBook(@PathParam("have") Long have, @PathParam("want") Long want,
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

        AssetCls haveAsset = map.get(have);
        AssetCls wantAsset = map.get(want);

        List<Order> haveOrders = DCSet.getInstance().getOrderMap().getOrders(have, want, limitInt);
        List<Order> wantOrders = DCSet.getInstance().getOrderMap().getOrders(want, have, limitInt);

        JSONObject result = new JSONObject();

        JSONArray arrayHave = new JSONArray();
        for (Order order : haveOrders) {
            JSONObject json = order.toJson();
            json.put("pairAmount", order.getAmountHaveLeft().setScale(haveAsset.getScale()).toPlainString());
            json.put("pairTotal", order.getAmountWantLeft().setScale(wantAsset.getScale()).toPlainString());
            json.put("pairPrice", order.calcLeftPrice().toPlainString());
            arrayHave.add(json);
        }
        result.put("have", arrayHave);

        JSONArray arrayWant = new JSONArray();
        for (Order order : wantOrders) {
            JSONObject json = order.toJson();
            // get REVERSE price and AMOUNT
            json.put("pairAmount", order.getAmountWantLeft().setScale(haveAsset.getScale()).toPlainString());
            json.put("pairTotal", order.getAmountHaveLeft().setScale(wantAsset.getScale()).toPlainString());
            json.put("pairPrice", order.calcLeftPriceReverse().toPlainString());
            arrayWant.add(json);
        }
        result.put("want", arrayWant);

        result.put("amountAssetKey", have);
        result.put("priceAssetKey", want);
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
                                                @DefaultValue("50") @QueryParam("limit") Integer limit) {

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

        List<Trade> listResult = Controller.getInstance().getTradeByTimestamp(have, want, timestamp * 1000, limit);

        JSONArray arrayJSON = new JSONArray();
        for (Trade trade : listResult) {
            arrayJSON.add(trade.toJson(have, false));
        }

        return arrayJSON.toJSONString();
    }

    @GET
    @Path("tradesfrom")
    public static String getTradesFrom(@QueryParam("height") Integer fromHeight,
                                       @QueryParam("trade") String fromTrade,
                                       @QueryParam("order") String fromOrder,
                                       @DefaultValue("0") @QueryParam("time") Long fromTimestamp,
                                       @DefaultValue("50") @QueryParam("limit") Integer limit) {

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();

        List<Trade> listResult;
        if (fromTrade != null) {
            long[] startTradeID = Trade.parseID(fromTrade);
            listResult = Controller.getInstance().getTradesFromTradeID(startTradeID, limit);
        } else if (fromOrder != null) {
            Long startOrderID = Transaction.parseDBRef(fromOrder);
            if (startOrderID == null) {
                startOrderID = Long.parseLong(fromOrder);
            }
            listResult = Controller.getInstance().getTradeByOrderID(startOrderID, limit);

        } else if (fromHeight != null) {
            listResult = Controller.getInstance().getTradeByHeight(fromHeight, limit);
        } else {
            listResult = Controller.getInstance().getTradeByTimestamp(fromTimestamp * 1000, limit);
        }

        JSONArray arrayJSON = new JSONArray();
        for (Trade trade : listResult) {
            arrayJSON.add(trade.toJson(0, true));
        }

        return arrayJSON.toJSONString();
    }

    public static List<Trade> getTradesFrom_1(Long have, Long want,
                                              Integer fromHeight,
                                              String fromOrder,
                                              Long fromTimestamp,
                                              Integer limit) {

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

        List<Trade> listResult;
        if (fromOrder != null) {
            Long startOrderID = Transaction.parseDBRef(fromOrder);
            if (startOrderID == null) {
                startOrderID = Long.parseLong(fromOrder);
            }

            listResult = Controller.getInstance().getTradeByOrderID(have, want, startOrderID, limit);

        } else if (fromHeight != null) {
            listResult = Controller.getInstance().getTradeByHeight(have, want, fromHeight, limit);
        } else {
            listResult = Controller.getInstance().getTradeByTimestamp(have, want, fromTimestamp * 1000, limit);
        }

        return listResult;
    }

    @GET
    @Path("tradesfrom/{have}/{want}")
    public static String getTradesFrom(@PathParam("have") Long have, @PathParam("want") Long want,
                                       @QueryParam("height") Integer fromHeight,
                                       @QueryParam("order") String fromOrder,
                                       @DefaultValue("0") @QueryParam("time") Long fromTimestamp,
                                       @DefaultValue("50") @QueryParam("limit") Integer limit) {

        JSONArray arrayJSON = new JSONArray();
        for (Trade trade : getTradesFrom_1(have, want, fromHeight, fromOrder, fromTimestamp, limit)) {
            arrayJSON.add(trade.toJson(have, true));
        }

        return arrayJSON.toJSONString();
    }

    @GET
    @Path("tradesfrom/{address}/{have}/{want}")
    public static String getTradesAddressFrom(@PathParam("address") String address, @PathParam("have") Long have, @PathParam("want") Long want,
                                              @QueryParam("height") Integer fromHeight,
                                              @QueryParam("order") String fromOrder,
                                              @DefaultValue("0") @QueryParam("time") Long fromTimestamp,
                                              @DefaultValue("50") @QueryParam("limit") Integer limit) {

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

        List<Trade> listResult;
        if (fromOrder != null) {
            Long startOrderID = Transaction.parseDBRef(fromOrder);
            if (startOrderID == null) {
                startOrderID = Long.parseLong(fromOrder);
            }

            listResult = Controller.getInstance().getTradeByOrderID(have, want, startOrderID, limit);

        } else if (fromHeight != null) {
            listResult = Controller.getInstance().getTradeByHeight(have, want, fromHeight, limit);
        } else {
            listResult = Controller.getInstance().getTradeByTimestamp(have, want, fromTimestamp * 1000, limit);
        }

        DCSet dcSet = DCSet.getInstance();
        JSONArray arrayJSON = new JSONArray();
        for (Trade trade : listResult) {
            Order initiator = trade.getInitiatorOrder(dcSet);
            Order target = trade.getTargetOrder(dcSet);
            if (initiator.getCreator().equals(address) || target.getCreator().equals(address)) {
                arrayJSON.add(trade.toJson(have, true));
            }
        }

        return arrayJSON.toJSONString();
    }

    @GET
    @Path("completedordersfrom/{have}/{want}")
    public static String getOrdersFrom(@PathParam("have") Long have, @PathParam("want") Long want,
                                       @QueryParam("height") Integer fromHeight,
                                       @QueryParam("order") String fromOrder,
                                       @DefaultValue("0") @QueryParam("time") Long fromTimestamp,
                                       @DefaultValue("50") @QueryParam("limit") Integer limit) {

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

        List<Order> listResult;
        if (fromOrder != null) {
            Long startOrderID = Transaction.parseDBRef(fromOrder);
            if (startOrderID == null) {
                startOrderID = Long.parseLong(fromOrder);
            }

            listResult = Controller.getInstance().getOrdersByOrderID(have, want, startOrderID, limit);

        } else if (fromHeight != null) {
            listResult = Controller.getInstance().getOrdersByHeight(have, want, fromHeight, limit);
        } else {
            listResult = Controller.getInstance().getOrdersByTimestamp(have, want, fromTimestamp * 1000, limit);
        }

        //AssetCls haveAsset = map.get(have);
        //AssetCls wantAsset = map.get(want);

        JSONArray arrayJSON = new JSONArray();
        for (Order order : listResult) {
            arrayJSON.add(order.toJson());

        }

        return arrayJSON.toJSONString();
    }

    @GET
    @Path("ordersbyaddress/{creator}/{amountAssetKey}/{priceAssetKey}")
    public static String getOrdersByAddress(@PathParam("creator") String address,
                                            @PathParam("amountAssetKey") Long haveKey, @PathParam("priceAssetKey") Long priceAssetKey,
                                            @DefaultValue("50") @QueryParam("limit") Integer limit) {


        OrderMap ordersMap = DCSet.getInstance().getOrderMap();
        TransactionFinalMapImpl finalMap = DCSet.getInstance().getTransactionFinalMap();
        Transaction createOrder;

        JSONArray out = new JSONArray();
        for (Order order : ordersMap.getOrdersForAddress(address, haveKey, priceAssetKey, limit)) {
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

    @GET
    @Path("allordersbyaddress/{address}/{from}")
    public static String getAllOrdersByAddress(@Context UriInfo info,
                                               @PathParam("address") String address,
                                               @PathParam("from") String fromOrder,
                                               @DefaultValue("50") @QueryParam("limit") Integer limit) {

        Long startOrderID = null;
        if (fromOrder != null) {
            startOrderID = Transaction.parseDBRef(fromOrder);
            if (startOrderID == null) {
                startOrderID = Long.parseLong(fromOrder);
            }
        }

        boolean desc = API.checkBoolean(info, "desc");

        TransactionFinalMapImpl finalMap = DCSet.getInstance().getTransactionFinalMap();
        CreateOrderTransaction createOrder;

        List<Long> keys = finalMap.getKeysByAddressAndType(Account.makeShortBytes(address), Transaction.CREATE_ORDER_TRANSACTION, Boolean.TRUE, startOrderID, limit, 0, desc);

        OrderMap ordersMap = DCSet.getInstance().getOrderMap();
        CompletedOrderMap completedOrdersMap = DCSet.getInstance().getCompletedOrderMap();

        Order order;
        JSONArray out = new JSONArray();
        for (Long key : keys) {
            createOrder = (CreateOrderTransaction) finalMap.get(key);

            if ((order = ordersMap.get(key)) != null) { // обновим данные об ордере - fulfilled
                if (false) {
                    // сейчас из Карты уже со статусом берется
                    if (order.isNotTraded()) {
                        order.setStatus(Order.ACTIVE);
                    } else {
                        order.setStatus(Order.FULFILLED);
                    }
                }
            } else if ((order = completedOrdersMap.get(key)) != null) { // обновим данные об ордере - fulfilled
                if (false) {
                    // сейчас из Карты уже со статусом берется
                    if (order.isFulfilled()) {
                        order.setStatus(Order.COMPLETED);
                    } else {
                        order.setStatus(Order.CANCELED);
                    }
                }
            } else {
                order = createOrder.makeOrder();
                Long err = null;
                err++;
            }

            JSONObject orderJson = order.toJson();
            orderJson.put("signature", Base58.encode(createOrder.getSignature()));

            out.add(orderJson);

        }


        return out.toJSONString();
    }

    @GET
    @Path("allordersbyaddress/{address}")
    public static String getAllOrdersByAddress2(@Context UriInfo info,
                                                @PathParam("address") String address,
                                                @QueryParam("from") String fromOrder,
                                                @DefaultValue("50") @QueryParam("limit") Integer limit) {
        return getAllOrdersByAddress(info, address, fromOrder, limit);
    }

    /**
     * В ordersMap.getKeysForAddressFromID неэффективно перебор до Откуда
     *
     * @param address
     * @param fromOrder
     * @param limit
     * @return
     */
    @GET
    @Path("allordersbyaddress_old/{address}/{from}")
    public static String getAllOrdersByAddress_old(@PathParam("address") String address,
                                                   @PathParam("from") String fromOrder,
                                                   @DefaultValue("50") @QueryParam("limit") Integer limit) {

        Long startOrderID = null;
        if (fromOrder != null) {
            startOrderID = Transaction.parseDBRef(fromOrder);
            if (startOrderID == null) {
                startOrderID = Long.parseLong(fromOrder);
            }
        }

        OrderMap ordersMap = DCSet.getInstance().getOrderMap();
        TransactionFinalMapImpl finalMap = DCSet.getInstance().getTransactionFinalMap();
        CreateOrderTransaction createOrder;

        // на самом деле не эффективно
        Set<Long> keys = ordersMap.getKeysForAddressFromID(address, startOrderID, limit);

        Order order;
        JSONArray out = new JSONArray();
        for (Long key : keys) {
            createOrder = (CreateOrderTransaction) finalMap.get(key);

            ///order = ordersMap.get(key);
            order = createOrder.makeOrder();

            JSONObject orderJson = order.toJson();
            orderJson.put("signature", Base58.encode(createOrder.getSignature()));

            out.add(orderJson);

        }

        CompletedOrderMap completedOrdersMap = DCSet.getInstance().getCompletedOrderMap();
        try (IteratorCloseable<Long> completedIterator = completedOrdersMap.getAddressIterator(address, startOrderID)) {

            Long key;
            while (completedIterator.hasNext()) {
                key = completedIterator.next();
                createOrder = (CreateOrderTransaction) finalMap.get(key);

                order = completedOrdersMap.get(key);

                JSONObject orderJson = order.toJson();
                orderJson.put("signature", Base58.encode(createOrder.getSignature()));

                out.add(orderJson);

            }
        } catch (IOException e) {
        }

        return out.toJSONString();
    }

    private static long test1Delay = 0;
    private static float test1probability = 0;
    private static Thread threadTest1;
    private static List<PrivateKeyAccount> test1Creators;

    /**
     * GET trade/test1/0.85/1000
     *
     * @param probability
     * @param delay
     * @param password
     * @return
     */
    @GET
    @Path("test1/{probability}/{delay}")
    public String test1(@PathParam("probability") float probability, @PathParam("delay") long delay, @QueryParam("password") String password) {

        if (!BlockChain.TEST_MODE
                && ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))
        )
            return "not LOCAL && not testnet";

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
        test1Creators = controller.getWalletPrivateKeyAccounts();

        // запомним счетчики для счетов
        HashMap<String, Long> counters = new HashMap<String, Long>();
        for (Account crestor : test1Creators) {
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
                    int rrr = random.nextInt((int) (100.0 / test1probability));
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
                            List<Order> addressOrders = dcSet.getOrderMap().getOrdersForAddress(account.getAddress(), haveStart.getKey(), wantStart.getKey(), 0);
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
                        for (String txSign : orders.keySet()) {

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
                                for (PrivateKeyAccount crestor : test1Creators) {
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

                            Integer result = cnt.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);
                            // CLEAR for HEAP
                            transaction.resetDCSet();

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
                            WeakReference<Transaction> weakRef = new WeakReference<>(new CreateOrderTransaction(creator, have.getKey(), want.getKey(),
                                    haveAmount, wantAmount, (byte) 0, time, 0l));

                            weakRef.get().sign(creator, Transaction.FOR_NETWORK);

                            // карта сбрасывается иногда при очистке, поэтому надо брать свежую всегда
                            cnt.transactionsPool.offerMessage(weakRef.get());
                            cnt.broadcastTransaction(weakRef.get());

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

    /// get trade/testcancelorder/7K8eqCRon1NKxnWn9o7dfbkTkL9zNEKCzR - see issue/1149
    @GET
    @Path("testcancelorder/{address}")
    public String testCancelOrder(@PathParam("address") String address) {

        TransactionFinalMapImpl txMap = DCSet.getInstance().getTransactionFinalMap();
        OrderMap orderMap = DCSet.getInstance().getOrderMap();

        try (IteratorCloseable<Long> iterator = DCSet.getInstance().getTransactionFinalMap()
                .findTransactionsKeys(null, address, null,
                        null, 0, 0, Transaction.CANCEL_ORDER_TRANSACTION,
                        0, false, 0, 0)) {

            String result = "";
            Transaction cancelOrder;
            Long key;
            Order order;
            while (iterator.hasNext()) {
                key = iterator.next();
                cancelOrder = txMap.get(key);
                if (cancelOrder == null) {
                    result += "\n Cancel Order not FOUND: " + Transaction.viewDBRef(key);
                }

                order = orderMap.get(((CancelOrderTransaction) cancelOrder).getOrderID());
                if (order != null) {
                    result += "\n Canceled by " + Transaction.viewDBRef(key)
                            + " Order is ACTIVE! - " + Transaction.viewDBRef(((CancelOrderTransaction) cancelOrder).getOrderID());
                }
            }

            if (!result.isEmpty()) {
                LOGGER.error(result);
                return "see log";
            }
        } catch (IOException e) {
        }

        return "OK";
    }

    /// get trade/updatepairs/days
    @GET
    @Path("updatepairs/{days}")
    public String updatePairs(@PathParam("days") Integer days) {
        PairsController.foundPairs(DCSet.getInstance(), Controller.getInstance().dlSet, days);
        return "OK";
    }

}