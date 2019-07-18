package org.erachain.webserver;

//import com.google.common.collect.Iterables;
//import com.google.gson.internal.LinkedHashTreeMap;
//import com.sun.org.apache.xpath.internal.operations.Or;
//import javafx.print.Collation;
import org.erachain.api.ApiErrorFactory;
//import com.google.gson.Gson;
import org.erachain.api.TradeResource;
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

        help.put("apiexchange/orders/[have]/[want]?limit=[limit]",
                "Get orders for HaveKey & WantKey, "
                        + "limit is count record. The number of transactions is limited by input param. Max 50, default 20.");
        help.put("apiexchange/trades/[have]/[want]?timestamp=[timestamp]&limit=[limit]",
                "Get trades from timestamp for HaveKey & WantKey, "
                        + "limit is count record. The number of transactions is limited by input param. Max 200, default 50.");
        help.put("apiexchange/volume24/[have]/[want]",
                "Get day volume of trades for HaveKey & WantKey");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }

    @GET
    @Path("orders/{have}/{want}")
    // orders/1/2?imit=4
    public Response getOrders(@PathParam("have") Long have, @PathParam("want") Long want,
                              @DefaultValue("20") @QueryParam("limit") Long limit) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limit > 50)
                limit = 50L;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TradeResource.getOrders(have, want, limit))
                .build();
    }

    @GET
    @Path("trades/{have}/{want}")
    public Response getTradesFromTimestamp(@PathParam("have") Long have, @PathParam("want") Long want,
                                                @DefaultValue("0") @QueryParam("timestamp") Long timestamp,
                                                @DefaultValue("50") @QueryParam("limit") Long limit) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limit > 200)
                limit = 200L;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(TradeResource.getTradesFromTimestamp(have, want, timestamp, limit))
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