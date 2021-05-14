package org.erachain.webserver;

//import com.google.common.collect.Iterables;
//import com.google.gson.internal.LinkedHashTreeMap;
//import com.sun.org.apache.xpath.internal.operations.Or;
//import javafx.print.Collation;

import org.erachain.api.ApiErrorFactory;
import org.erachain.api.TradeResource;
import org.erachain.controller.Controller;
import org.erachain.controller.PairsController;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.item.assets.TradePair;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.database.PairMapImpl;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//import com.google.gson.Gson;
//import org.mapdb.Fun;

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

        help.put("GET apiexchange/spot/list",
                "Pairs list");
        help.put("GET apiexchange/spot/pairs",
                "Pairs values24");
        help.put("GET apiexchange/spot/pairs/{pairs_list}",
                "Pairs values24. Pairs list separated by comma for example: BTC_USD,ERA_USD,GOLD_USD");
        help.put("GET apiexchange/spot/ordersbook/[pair]?depth=[depth]",
                "Get active orders in orderbook for pair as [price, volume]."
                        + " The number of orders is limited by depth, default 50, max = 500");
        help.put("GET apiexchange/spot/trades/[pair]?fromOrder=[orderID]&limit=[limit]",
                "Get trades for pair, "
                        + "limit is count record. The number of trades is limited by input param, default 50."
                        + "Use fromOrder (initial order ID) as Block-seqNo or Long. For example 103506-3 or 928735142671");

        help.put("GET apiexchange/order/[seqNo|signature]",
                "Get Order by seqNo or Signature. For example: 4321-2");
        help.put("GET apiexchange/v2/pair/{baseAssetKey}/{quoteAssetKey}",
                "Get Pair info fot baseAssetKey / quoteAssetKey");
        help.put("GET apiexchange/pair/{have}/{want}",
                "Get Pair info fot Have / Want");
        help.put("GET apiexchange/ordersbook/[have]/[want]?limit=[limit]",
                "Get active orders in orderbook for amountAssetKey & priceAssetKey, "
                        + "limit is count record. The number of orders is limited by input param, default 20.");
        help.put("GET apiexchange/ordersbyaddress/[address]/{amountAssetKey}/{priceAssetKey}?limit=[limit]",
                "Get active orders in orderbook for creator address and asset pair, "
                        + "limit is count record. The number of orders is limited by input param, default 20.");
        help.put("GET apiexchange/completedordersfrom/[have]/[want]?order=[orderID]&height=[height]&time=[timestamp]&limit=[limit]",
                "Get completed orders for amountAssetKey & priceAssetKey, "
                        + "limit is count record. The number of orders is limited by input param, default 50."
                        + "Use Order ID as Block-seqNo or Long. For example 103506-3 or 928735142671");

        help.put("GET apiexchange/allordersbyaddress/{address}/{from}?limit=[limit]",
                "get list of ALL orders (in CAP and completed) by address from OrderID. "
                        + "Use Order ID as Block-seqNo or Long. For example 103506-3 or 928735142671");
        help.put("GET apiexchange/allordersbyaddress/{address}?from={SeqNo}&limit=[20]&desc={false}",
                "get list of ALL orders (in CAP and completed) by address from OrderID. "
                        + "Use Order ID as Block-seqNo or Long. For example 103506-3 or 928735142671");

        help.put("GET apiexchange/trades/[amountAssetKey]/[priceAssetKey]?timestamp=[timestamp]&limit=[limit]",
                "Get trades from timestamp for amountAssetKey & priceAssetKey, "
                        + "limit is count record. The number of transactions is limited by input param. Max 200, default 50.");
        help.put("GET apiexchange/tradesfrom?trade=[TradeID]&order=[orderID]&height=[height]&time=[timestamp]&limit=[limit]",
                "Get trades for amountAssetKey & priceAssetKey, "
                        + "limit is count record. The number of trades is limited by input param, default 50."
                        + "Use Order ID as Block-seqNo. For example 103506-3. Use TradeID as Initiator_OrderID/Target_OrderID");
        help.put("GET apiexchange/tradesfrom/[amountAssetKey]/[priceAssetKey]?order=[orderID]&height=[height]&time=[timestamp]&limit=[limit]",
                "Get trades for amountAssetKey & priceAssetKey, "
                        + "limit is count record. The number of trades is limited by input param, default 50."
                        + "Use Order ID as Block-seqNo or Long. For example 103506-3 or 928735142671");
        help.put("GET apiexchange/tradesfrom/[address]/[amountAssetKey]/[priceAssetKey]?order=[orderID]&height=[height]&time=[timestamp]&limit=[limit]",
                "Get trades for amountAssetKey & priceAssetKey for creator [address], "
                        + "limit is count record. The number of trades is limited by input param, default 50."
                        + "Use Order ID as Block-seqNo or Long. For example 103506-3 or 928735142671");
        help.put("GET apiexchange/volume24/[amountAssetKey]/[priceAssetKey]",
                "Get day volume of trades for amountAssetKey & priceAssetKey");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }

    @GET
    @Path("ordersbook/{have}/{want}")
    public Response getOrdersBook(@PathParam("have") Long have, @PathParam("want") Long want,
                                  @DefaultValue("20") @QueryParam("limit") Long limit) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limit > 50 || limit <= 0)
                limit = 50L;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TradeResource.getOrdersBook(have, want, limit))
                .build();
    }

    @GET
    @Path("ordersbyaddress/{creator}/{amountAssetKey}/{priceAssetKey}")
    public Response getOrdersByAddress(@PathParam("creator") String address,
                                       @PathParam("amountAssetKey") Long haveKey, @PathParam("priceAssetKey") Long priceAssetKey,
                                       @DefaultValue("50") @QueryParam("limit") Integer limit) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limit > 50 || limit <= 0)
                limit = 50;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TradeResource.getOrdersByAddress(address, haveKey, priceAssetKey, limit))
                .build();
    }


    @GET
    @Path("allordersbyaddress/{address}/{from}")
    // TODO нужно сделать тесты на проверку потерянных ордеров - есть трнзакция создания а его нету ни в одной таблице
    public Response getAllOrdersByAddress(@Context UriInfo info,
                                          @PathParam("address") String address,
                                          @PathParam("from") String fromOrder,
                                          @DefaultValue("20") @QueryParam("limit") Integer limit) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limit > 50 || limit <= 0)
                limit = 50;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TradeResource.getAllOrdersByAddress(info, address, fromOrder, limit))
                .build();
    }

    @GET
    @Path("allordersbyaddress/{address}")
    public Response getAllOrdersByAddress2(@Context UriInfo info,
                                           @PathParam("address") String address,
                                           @QueryParam("from") String fromOrder,
                                           @DefaultValue("50") @QueryParam("limit") Integer limit) {
        return getAllOrdersByAddress(info, address, fromOrder, limit);
    }

    @GET
    @Path("completedordersfrom/{have}/{want}")
    public Response getOrdersFrom(@PathParam("have") Long have, @PathParam("want") Long want,
                                  @QueryParam("height") Integer fromHeight,
                                  @QueryParam("order") String fromOrder,
                                  @DefaultValue("0") @QueryParam("time") Long fromTimestamp,
                                  @DefaultValue("50") @QueryParam("limit") Integer limit) {

        int limitInt = limit.intValue();
        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limitInt > 200 || limitInt <= 0)
                limitInt = 200;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TradeResource.getOrdersFrom(have, want, fromHeight, fromOrder, fromTimestamp, limitInt))
                .build();
    }

    @GET
    @Path("trades/{have}/{want}")
    public Response getTradesFromTimestamp(@PathParam("have") Long have, @PathParam("want") Long want,
                                                @DefaultValue("0") @QueryParam("timestamp") Long timestamp,
                                                @DefaultValue("50") @QueryParam("limit") Long limit) {

        int limitInt = limit.intValue();
        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limitInt > 200 || limitInt <= 0)
                limitInt = 200;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TradeResource.getTradesFromTimestamp(have, want, timestamp, limitInt))
                .build();
    }

    @GET
    @Path("tradesfrom")
    public Response getTradesFrom(@QueryParam("height") Integer fromHeight,
                                  @QueryParam("trade") String fromTrade,
                                  @QueryParam("order") String fromOrder,
                                  @DefaultValue("0") @QueryParam("time") Long fromTimestamp,
                                  @DefaultValue("50") @QueryParam("limit") Integer limit) {

        int limitInt = limit.intValue();
        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limitInt > 200 || limitInt <= 0)
                limitInt = 200;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TradeResource.getTradesFrom(fromHeight, fromTrade, fromOrder, fromTimestamp, limitInt))
                .build();
    }

    @GET
    @Path("tradesfrom/{have}/{want}")
    public Response getTradesFrom(@PathParam("have") Long have, @PathParam("want") Long want,
                                  @QueryParam("height") Integer fromHeight,
                                  @QueryParam("order") String fromOrder,
                                  @DefaultValue("0") @QueryParam("time") Long fromTimestamp,
                                  @DefaultValue("50") @QueryParam("limit") Integer limit) {

        int limitInt = limit.intValue();
        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limitInt > 200 || limitInt <= 0)
                limitInt = 200;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TradeResource.getTradesFrom(have, want, fromHeight, fromOrder, fromTimestamp, limitInt))
                .build();
    }

    @GET
    @Path("tradesfrom/{address}/{have}/{want}")
    public Response getTradesAddressFrom(@PathParam("address") String address, @PathParam("have") Long have, @PathParam("want") Long want,
                                         @QueryParam("height") Integer fromHeight,
                                         @QueryParam("order") String fromOrder,
                                         @DefaultValue("0") @QueryParam("time") Long fromTimestamp,
                                         @DefaultValue("50") @QueryParam("limit") Integer limit) {

        int limitInt = limit.intValue();
        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limitInt > 200 || limitInt <= 0)
                limitInt = 200;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TradeResource.getTradesAddressFrom(address, have, want, fromHeight, fromOrder, fromTimestamp, limitInt))
                .build();
    }

    @GET
    @Path("order/{signature}")
    public Response getOrder(@PathParam("signature") String seqNo) {

        return Response.status(200).header("Content-Type", "text/html; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TradeResource.getOrder(seqNo))
                .build();
    }

    @GET
    @Path("pair/{have}/{want}")
    // apiexchange/pair?have=1&want=2
    public Response getPair(@PathParam("have") Long have, @PathParam("want") Long want) {

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

        JSONObject out = new JSONObject();
        out.put("vol24", dcSet.getTradeMap().getVolume24(have, want).toPlainString());

        Order haveOrder = DCSet.getInstance().getOrderMap().getHaveWanFirst(have, want);
        if (haveOrder != null) {
            out.put("havePrice", haveOrder.calcLeftPrice().toPlainString());
        }
        Order wantOrder = DCSet.getInstance().getOrderMap().getHaveWanFirst(want, have);
        if (wantOrder != null) {
            out.put("wantPrice", wantOrder.calcLeftPriceReverse().toPlainString());
        }

        Trade trade = dcSet.getTradeMap().getLastTrade(have, want);
        if (trade == null) {
            out.put("last", "--");
        } else {
            if (trade.getHaveKey() == want) {
                out.put("lastPrice", trade.calcPrice().toPlainString());
                out.put("lastAmount", trade.getAmountHave().toPlainString());
                out.put("lastDir", "buy");
            } else {
                out.put("lastPrice", trade.calcPriceRevers().toPlainString());
                out.put("lastAmount", trade.getAmountWant().toPlainString());
                out.put("lastDir", "sell");
            }
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

    @GET
    @Path("v2/pair/{baseAssetKey}/{quoteAssetKey}")
    // apiexchange/v2/pair/1/2
    public Response getPair2(@PathParam("baseAssetKey") Long baseAssetKey, @PathParam("quoteAssetKey") Long quoteAssetKey) {

        ItemAssetMap map = this.dcSet.getItemAssetMap();
        // DOES ASSETID EXIST
        if (baseAssetKey == null || !map.contains(baseAssetKey)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }
        if (quoteAssetKey == null || !map.contains(quoteAssetKey)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        PairMapImpl mapPairs = Controller.getInstance().dlSet.getPairMap();
        AssetCls asset1 = map.get(quoteAssetKey);
        AssetCls asset2 = map.get(baseAssetKey);

        TradePair tradePair = PairsController.reCalcAndUpdate(asset1, asset2, mapPairs, 30);

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(tradePair.toJson().toJSONString())
                .build();
    }

    @GET
    @Path("volume24/{have}/{want}")
    // apiexchange/get?have=1&want=2&timestamp=3&limit=4
    public Response getVolume24(@PathParam("have") Long have, @PathParam("want") Long want) {

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


    @GET
    @Path("spot/list")
    // apiexchange/spot/list
    public Response spotList() {

        cntrl.pairsController.updateList();

        return Response.status(200).header("Content-Type", "text/html; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(cntrl.pairsController.spotPairsList.toJSONString())
                .build();
    }

    @GET
    @Path("spot/pairs")
    // apiexchange/spot/pairs
    public Response spotPairs() {

        cntrl.pairsController.updateList();

        return Response.status(200).header("Content-Type", "text/html; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(cntrl.pairsController.spotPairsJson.toJSONString())
                .build();
    }

    @GET
    @Path("spot/pairs/{pairs}")
    // apiexchange/spot/pair/BTC_USD
    public Response spotPair(@PathParam("pairs") String pairs) {

        cntrl.pairsController.updateList();

        JSONArray result = new JSONArray();
        for (String pair : pairs.split(",")) {
            result.add(cntrl.pairsController.spotPairsJson.get(pair));
        }

        return Response.status(200).header("Content-Type", "text/html; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(result.toJSONString())
                .build();
    }

    // TODO кешировать ответ по набору параметров
    String getSpotOrdersBook; // разные параметры же на входе - поэтому несколько уровней и все - чтобы кешировать ответ

    @GET
    @Path("spot/ordersbook/{pair}")
    public Response getSpotOrdersBook(@PathParam("pair") String pair,
                                      @DefaultValue("50") @QueryParam("depth") Long limit) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limit > 500 || limit <= 0)
                limit = 500L;
        }

        int limitInt = (int) (long) limit;

        cntrl.pairsController.updateList();
        JSONArray array = (JSONArray) cntrl.pairsController.spotPairsList.get(pair);
        if (array == null) {
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("Ticker not found")
                    .build();
        }

        Long have = (Long) array.get(1);
        Long want = (Long) array.get(2);

        List<Order> haveOrders = DCSet.getInstance().getOrderMap().getOrders(have, want, limitInt);
        List<Order> wantOrders = DCSet.getInstance().getOrderMap().getOrders(want, have, limitInt);

        JSONObject result = new JSONObject();

        JSONArray arrayHave = new JSONArray();
        for (Order order : haveOrders) {
            JSONArray json = new JSONArray();
            json.add(order.calcLeftPrice().toPlainString());
            json.add(order.getAmountHaveLeft().toPlainString());
            arrayHave.add(json);
        }
        result.put("bids", arrayHave);

        JSONArray arrayWant = new JSONArray();
        for (Order order : wantOrders) {
            JSONArray json = new JSONArray();
            // get REVERSE price and AMOUNT
            json.add(order.calcLeftPriceReverse().toPlainString());
            json.add(order.getAmountWantLeft().toPlainString());
            arrayWant.add(json);
        }
        result.put("asks", arrayWant);

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(result.toJSONString())
                .build();
    }

    @GET
    @Path("spot/trades/{pair}")
    public Response getSpotTrades(@PathParam("pair") String pair,
                                  @QueryParam("fromOrder") String fromOrder,
                                  @DefaultValue("100") @QueryParam("limit") Integer limit) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limit > 200 || limit <= 0)
                limit = 200;
        }

        cntrl.pairsController.updateList();
        JSONArray array = (JSONArray) cntrl.pairsController.spotPairsList.get(pair);
        if (array == null) {
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("Ticker not found")
                    .build();
        }

        Long have = (Long) array.get(1);
        Long want = (Long) array.get(2);

        long startOrder = 0;
        Long startLong = Transaction.parseDBRef(fromOrder);
        if (startLong != null)
            startOrder = startLong;

        JSONArray arrayJSON = new JSONArray();
        for (Trade trade : dcSet.getTradeMap().getTradesByOrderID(have, want, startOrder, 0, limit)) {
            JSONObject json = new JSONObject();
            json.put("initial_order_id", Transaction.viewDBRef(trade.getInitiator()));
            json.put("target_order_id", Transaction.viewDBRef(trade.getTarget()));
            json.put("timestamp", trade.getTimestamp());

            boolean reversed = trade.getAmountHave().equals(want);

            json.put("price", reversed ? trade.calcPrice() : trade.calcPriceRevers());
            json.put("base_volume", reversed ? trade.getAmountHave() : trade.getAmountWant());
            json.put("quote_volume", reversed ? trade.getAmountWant() : trade.getAmountHave());

            arrayJSON.add(json);
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(arrayJSON.toJSONString())
                .build();
    }

}