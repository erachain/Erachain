package org.erachain.webserver;

//import com.google.common.collect.Iterables;
//import com.google.gson.internal.LinkedHashTreeMap;
//import com.sun.org.apache.xpath.internal.operations.Or;
//import javafx.print.Collation;
import org.erachain.api.ApiErrorFactory;
//import com.google.gson.Gson;
import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import org.mapdb.Fun;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;

@Path("apiexchange")
@Produces(MediaType.APPLICATION_JSON)
public class APIExchange {

    @Context
    HttpServletRequest request;

    private DCSet dcSet = DCSet.getInstance();
    private Controller cntrl = Controller.getInstance();

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("apiexchange/orders?have={have}&want={want}&limit={limit}",
                "Get orders for HaveKey & WantKey, "
                        + "limit is count record. The number of transactions is limited by input param. Max 50, default 20.");
        help.put("apiexchange/trades?have={have}&want={want}&timestamp={timestamp}&limit={limit}",
                "Get trades from timestamp for HaveKey & WantKey, "
                        + "limit is count record. The number of transactions is limited by input param. Max 200, default 50.");
        help.put("apiexchange/volume24?have={have}&want={want}",
                "Get day volume of trades for HaveKey & WantKey");
        help.put("apiexchange/ordersfull?have={have}&want={want}&limit={limit}",
                "Get Orders. Only for local requests");
        help.put("apiexchange/tradesfull?have={have}&want={want}&limit={limit}",
                "Get trades. Only for local requests");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }

    @GET
    @Path("orders")
    // apiexchange/get?have=1&want=2&&limit=4
    public Response getOrders(@QueryParam("have") Long have, @QueryParam("want") Long want,
                              @DefaultValue("20") @QueryParam("limit") Long limit) {

        ItemAssetMap map = this.dcSet.getItemAssetMap();

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
        if (limitInt > 50)
            limitInt = 50;


        List<Order> haveOrders = dcSet.getOrderMap().getOrdersHave(have, limitInt);
        List<Order> wantOrders = dcSet.getOrderMap().getOrdersWant(want, limitInt);

        JSONObject result = new JSONObject();

        // тут ошибка конвертации если пользовать StrJSonFine или gs
        //  java.lang.NumberFormatException: For input string: "587341072695297-1"
        // .entity(StrJSonFine.convert(output)) или gs.toJson(listResult)
        ////////// ТАК у нас у Ордера есть toString - и Сборщики почемуто берут это а не .toJson
        // поэтому делаем вручную
        JSONArray arrayHave = new JSONArray();
        for (Order order: haveOrders) {
            arrayHave.add(order.toJson());
        }
        result.put("have", arrayHave);

        JSONArray arrayWant = new JSONArray();
        for (Order order: wantOrders) {
            arrayWant.add(order.toJson());
        }
        result.put("want", arrayWant);

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(result.toJSONString())
                .build();
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
    @Path("trades")
    // apiexchange/get?have=1&want=2&timestamp=3&limit=4
    public Response getTradesFromTimestamp(@QueryParam("have") Long have, @QueryParam("want") Long want,
                               @DefaultValue("0") @QueryParam("timestamp") Long timestamp,
                               @DefaultValue("50") @QueryParam("limit") Long limit) {

        ItemAssetMap map = this.dcSet.getItemAssetMap();
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
        if (limitInt > 200)
            limitInt = 200;

        List<Trade> listResult = cntrl.getTradeByTimestmp(have, want, timestamp, limitInt);

        JSONArray arrayJSON = new JSONArray();
        for (Trade trade: listResult) {
            arrayJSON.add(trade.toJson(have));
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(arrayJSON.toJSONString())
                .build();
    }

    @GET
    @Path("volume24")
    // apiexchange/get?have=1&want=2&timestamp=3&limit=4
    public Response getVolume24(@QueryParam("have") Long have, @QueryParam("want") Long want) {

        ItemAssetMap map = this.dcSet.getItemAssetMap();
        // DOES ASSETID EXIST
        if (have == null || !map.contains(have)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }
        if (want == null || !map.contains(want)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        return Response.status(200).header("Content-Type", "text/html; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity("\"" + dcSet.getTradeMap().getVolume24(have, want).toPlainString() + "\"")
                .build();
    }

    /**
     * Get orders. The number of items in SEL and BUY is limited by LIMIT
     * param.
     *
     * @param have      is HaveKey
     * @param want      is WantKey
     * @param limit     count out record, default = 20
     * @return orders for SELL and BUY
     * @author Icreator
     */

    @GET
    @Path("ordersfull")
    // apiexchange/orders?have=1&want=2&limit=20
    public Response ordersFull(@QueryParam("have") Long have, @QueryParam("want") Long want,
                           @DefaultValue("20") @QueryParam("limit") Long limit) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("Not LOCAL request. Access denied.")
                    .build();
        }

        ItemAssetMap map = this.dcSet.getItemAssetMap();
        // DOES ASSETID EXIST
        if (have == null || !map.contains(have)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }
        if (want == null || !map.contains(want)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        Map output = new LinkedHashMap();

        List<Order> ordersHave = dcSet.getOrderMap().getOrdersForTradeWithFork(have, want, false);
        List<Order> ordersWant = dcSet.getOrderMap().getOrdersForTradeWithFork(want, have, true);

        Map sellsJSON = new LinkedHashMap();
        Map buysJSON = new LinkedHashMap();

        BigDecimal sumAmount = BigDecimal.ZERO;

        BigDecimal sumSellingAmount = BigDecimal.ZERO;

        BigDecimal vol;
        int counter = 0;
        // show SELLs in BACK order
        for (int i = ordersHave.size() - 1; i >= 0; i--) {

            Order order = ordersHave.get(i);
            Map sellJSON = new LinkedHashMap();

            sellJSON.put("price", order.getPrice());
            vol = order.getAmountHaveLeft();
            sellJSON.put("amount", vol);
            sumAmount = sumAmount.add(vol);

            sellJSON.put("sellingPrice", order.calcPriceReverse());

            BigDecimal sellingAmount = order.getAmountWantLeft();

            sellJSON.put("sellingAmount", sellingAmount);

            sumSellingAmount = sumSellingAmount.add(sellingAmount);

            sellsJSON.put(order.getId().toString(), sellJSON);

            if(counter++ > limit) break;

        }

        output.put("sells", sellsJSON);

        output.put("sellsSumAmount", sumAmount);
        output.put("sellsSumTotal", sumSellingAmount);

        sumAmount = BigDecimal.ZERO;

        BigDecimal sumBuyingAmount = BigDecimal.ZERO;

        counter = 0;
        for (int i = ordersWant.size() - 1; i >= 0; i--) {

            Order order = ordersWant.get(i);

            Map buyJSON = new LinkedHashMap();

            buyJSON.put("price", order.getPrice());
            vol = order.getAmountHaveLeft();
            buyJSON.put("amount", vol);

            sumAmount = sumAmount.add(vol);

            buyJSON.put("buyingPrice", order.calcPriceReverse());

            BigDecimal buyingAmount = order.getAmountWantLeft();

            buyJSON.put("buyingAmount", buyingAmount);

            sumBuyingAmount = sumBuyingAmount.add(buyingAmount);

            buysJSON.put(order.getId(), buyJSON);
            //buysJSON.put(Base58.encode(order.a.a, 64), buyJSON);

            if(counter++ > limit) break;

        }
        output.put("buys", buysJSON);

        output.put("buysSumAmount", sumBuyingAmount);
        output.put("buysSumTotal", sumAmount);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(output))
                //.entity(output.toJSONString())
                .build();

    }

    /**
     * Get trades. The number of items in SEL and BUY is limited by LIMIT
     * param.
     *
     * @param have      is HaveKey
     * @param want      is WantKey
     * @param limit     count out record, default = 100
     * @return trades
     * @author Icreator
     */

    @GET
    @Path("tradesfull")
    // apiexchange/trades?have=1&want=2&limit=20
    public Response tradesFull(@QueryParam("have") Long have, @QueryParam("want") Long want,
                           @DefaultValue("20") @QueryParam("limit") Long limit) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("Not LOCAL request. Access denied.")
                    .build();
        }

        ItemAssetMap map = this.dcSet.getItemAssetMap();
        // DOES ASSETID EXIST
        if (have == null || !map.contains(have)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }
        if (want == null || !map.contains(want)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        Map output = new LinkedHashMap();

        //Map tradesJSON = new LinkedHashMap();
        List tradesJSON = new ArrayList();

        int limitInt = limit.intValue();
        List<Trade> trades = dcSet.getTradeMap().getTrades(have,
                want, 0, limitInt);

        output.put("tradesCount", trades.size());

        BigDecimal tradeWantAmount = BigDecimal.ZERO;
        BigDecimal tradeHaveAmount = BigDecimal.ZERO;

        TransactionFinalMap finalMap = dcSet.getTransactionFinalMap();
        Transaction createOrder;

        int i = 0;
        for (Trade trade : trades) {
            if (i++ > limitInt) break;

            Map tradeJSON = new LinkedHashMap();

            Order orderInitiator = Order.getOrder(dcSet, trade.getInitiator());

            Order orderTarget = Order.getOrder(dcSet, trade.getTarget());

            tradeJSON.put("amountHave", trade.getAmountHave());
            tradeJSON.put("amountWant", trade.getAmountWant());

            tradeJSON.put("realPrice", trade.calcPrice());
            tradeJSON.put("realReversePrice", trade.calcPriceRevers());

            createOrder = finalMap.get(orderInitiator.getId());
            tradeJSON.put("initiatorTxSignature", Base58.encode(createOrder.getSignature()));
            tradeJSON.put("initiatorId", orderInitiator.getId());

            tradeJSON.put("initiatorCreator", orderInitiator.getCreator().getAddress());
            tradeJSON.put("initiatorAmount", orderInitiator.getAmountHave());
            if (orderInitiator.getHaveAssetKey() == have) {
                tradeJSON.put("type", "sell");
                tradeWantAmount = tradeWantAmount.add(trade.getAmountHave());
                tradeHaveAmount = tradeHaveAmount.add(trade.getAmountWant());

            } else {
                tradeJSON.put("type", "buy");
                tradeHaveAmount = tradeHaveAmount.add(trade.getAmountHave());
                tradeWantAmount = tradeWantAmount.add(trade.getAmountWant());
            }

            createOrder = finalMap.get(orderInitiator.getId());
            tradeJSON.put("targetTxSignature", Base58.encode(createOrder.getSignature()));
            tradeJSON.put("targetId", orderTarget.getId());
            tradeJSON.put("targetCreator", orderTarget.getCreator().getAddress());
            tradeJSON.put("targetAmount", orderTarget.getAmountHave());

            tradeJSON.put("timestamp", trade.getTimestamp());

            tradesJSON.add(tradeJSON);
        }
        output.put("trades", tradesJSON);

        output.put("tradeWantAmount", tradeWantAmount);
        output.put("tradeHaveAmount", tradeHaveAmount);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(StrJSonFine.convert(output))
                //.entity(output.toJSONString())
                .build();

    }

}