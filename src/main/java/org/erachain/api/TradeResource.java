package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.datachain.OrderMap;
import org.erachain.datachain.TransactionFinalMap;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.erachain.gui.transaction.OnDealClick;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.erachain.utils.APIUtils;
import org.erachain.utils.StrJSonFine;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                        + "limit is count record. The number of transactions is limited by input param, default 20.");
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

        if (DCSet.getInstance().getTransactionMap().contains(signature)) {
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

        if (!DCSet.getInstance().getTransactionMap().contains(signature)) {
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
    public String getOrders(@PathParam("have") Long have, @PathParam("want") Long want,
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
            json.put("left", order.getAmountHaveLeft().toPlainString());
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
            json.put("left", order.getAmountWantLeft().toPlainString());
            json.put("price", order.calcPriceReverse().toPlainString());
            arrayWant.add(json);
        }
        result.put("want", arrayWant);

        result.put("haveKey", have);
        result.put("wantKey", want);

        return result.toJSONString();
    }

    @GET
    @Path("getbyaddress/{creator}/{haveKey}/{wantKey}")
    public String cancel(@PathParam("creator") String address,
                         @PathParam("haveKey") Long haveKey, @PathParam("wantKey") Long wantKey) {


        OrderMap ordersMap = DCSet.getInstance().getOrderMap();
        TransactionFinalMap finalMap = DCSet.getInstance().getTransactionFinalMap();
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
}
