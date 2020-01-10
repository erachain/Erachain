package org.erachain.webserver;

//import com.google.common.collect.Iterables;
//import com.google.gson.internal.LinkedHashTreeMap;
//import com.sun.org.apache.xpath.internal.operations.Or;
//import javafx.print.Collation;

import org.erachain.api.ApiErrorFactory;
import org.erachain.api.TradeResource;
import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.utils.StrJSonFine;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
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

        help.put("GET apiexchange/order/[seqNo|signature]",
                "Get Order by seqNo or Signature. For example: 4321-2");
        help.put("GET apiexchange/ordersbook/[have]/[want]?limit=[limit]",
                "Get active orders in orderbook for amountAssetKey & priceAssetKey, "
                        + "limit is count record. The number of orders is limited by input param, default 20.");
        help.put("GET apiexchange/ordersbyaddress/[address]?limit=[limit]",
                "Get active orders in orderbook for creator address, "
                        + "limit is count record. The number of orders is limited by input param, default 20.");
        help.put("GET apiexchange/completedordersfrom/[have]/[want]?order=[orderID]&height=[height]&time=[timestamp]&limit=[limit]",
                "Get completed orders for amountAssetKey & priceAssetKey, "
                        + "limit is count record. The number of orders is limited by input param, default 50."
                        + "Use Order ID as Block-seqNo or Long. For example 103506-3 or 928735142671");

        help.put("GET apiexchange/allordersbyaddress/{address}/{from}?limit=[limit]",
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
    // orders/1/2?imit=4
    public Response getOrdersBook(@PathParam("have") Long have, @PathParam("want") Long want,
                                  @DefaultValue("20") @QueryParam("limit") Long limit) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limit > 50)
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
            if (limit > 50)
                limit = 50;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TradeResource.getOrdersByAddress(address, haveKey, priceAssetKey, limit))
                .build();
    }


    @GET
    @Path("allordersbyaddress/{address}/{from}")
    // orders/1/2?imit=4
    // TODO нужно сделать тесты на проверку потерянных ордеров - есть трнзакция создания а его нету ни в одной таблице
    public Response getAllOrdersByAddress(@PathParam("address") String address,
                                          @PathParam("from") String fromOrder,
                                          @DefaultValue("20") @QueryParam("limit") Integer limit) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limit > 50)
                limit = 50;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TradeResource.getAllOrdersByAddress(address, fromOrder, limit))
                .build();
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
            if (limitInt > 200)
                limitInt = 200;
            else if (limitInt < 0)
                limitInt = 0;
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
            if (limitInt > 200)
                limitInt = 200;
            else if (limitInt < 0)
                limitInt = 0;
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
            if (limitInt > 200)
                limitInt = 200;
            else if (limitInt < 0)
                limitInt = 0;
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
            if (limitInt > 200)
                limitInt = 200;
            else if (limitInt < 0)
                limitInt = 0;
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
            if (limitInt > 200)
                limitInt = 200;
            else if (limitInt < 0)
                limitInt = 0;
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

}