package webserver;

import api.ApiErrorFactory;
import com.google.gson.Gson;
import controller.Controller;
import core.blockexplorer.BlockExplorer;
import core.crypto.Base58;
import core.item.assets.Order;
import core.item.assets.Trade;
import core.transaction.Transaction;
import datachain.DCSet;
import datachain.ItemAssetMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import utils.StrJSonFine;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("apitrade")
@Produces(MediaType.APPLICATION_JSON)
public class API_Trade {

    @Context
    HttpServletRequest request;

    private DCSet dcSet = DCSet.getInstance();
    private Controller cntrl = Controller.getInstance();

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("apitrade/get?have={have}&want={want}&timestamp={timestamp}&limit={limit}",
                "Get data by trade. Have= Want=, "
                        + "limit is count record. The number of transactions is limited by input param.");
        help.put("apitrade/orders?have={have}&want={want}&limit={limit}",
                "Get Orders.");
        help.put("apitrade/trades?have={have}&want={want}&limit={limit}",
                "Get trades.");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
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
    @Path("get")
    // apitrade/get?have=1&want=2&timestamp=3&limit=4
    public Response getTradeByAccount(@QueryParam("have") Long have, @QueryParam("want") Long want,
                                      @QueryParam("timestamp") Long timestamp, @DefaultValue("20") @QueryParam("limit") Long limit) {

        //Long haveKey = Long.parseLong(have);
        //Long wantKey = Long.parseLong(want);
        List<Trade> listRusult = cntrl.getTradeByTimestmp(have, want, timestamp);

        List<Trade> tradeList = listRusult.subList(0, limit.intValue());

        Gson gs = new Gson();
        String result = gs.toJson(tradeList);

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(result).build();
    }

    @GET
    @Path("orders")
    // apitrade/orders?have=1&want=2&limit=20
    public Response orders(@QueryParam("have") Long have, @QueryParam("want") Long want,
                           @DefaultValue("20") @QueryParam("limit") Long limit) {

        ItemAssetMap map = this.dcSet.getItemAssetMap();
        // DOES ASSETID EXIST
        if (!map.contains(have)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }
        if (!map.contains(want)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        Map output = new LinkedHashMap();

        List<Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Fun.Tuple2<Long, BigDecimal>>> ordersHave = dcSet
                .getOrderMap().getOrdersForTradeWithFork(have, want, false);
        List<Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Fun.Tuple2<Long, BigDecimal>>> ordersWant = dcSet
                .getOrderMap().getOrdersForTradeWithFork(want, have, true);

        Map sellsJSON = new LinkedHashMap();
        Map buysJSON = new LinkedHashMap();

        BigDecimal sumAmount = BigDecimal.ZERO;

        BigDecimal sumSellingAmount = BigDecimal.ZERO;

        BigDecimal vol;
        int counter = 0;
        // show SELLs in BACK order
        for (int i = ordersHave.size() - 1; i >= 0; i--) {

            Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Fun.Tuple2<Long, BigDecimal>> order = ordersHave.get(i);
            Map sellJSON = new LinkedHashMap();

            sellJSON.put("price", order.a.e);
            vol = order.b.b.subtract(order.b.c);
            sellJSON.put("amount", vol); // getAmountHaveLeft
            sumAmount = sumAmount.add(vol);

            sellJSON.put("sellingPrice", Order.calcPrice(order.c.b, order.b.b));

            BigDecimal sellingAmount = Order.calcAmountWantLeft(order);

            sellJSON.put("sellingAmount", sellingAmount);

            sumSellingAmount = sumSellingAmount.add(sellingAmount);

            sellsJSON.put(Base58.encode(order.a.a), sellJSON);

            if(counter++ > limit) break;

        }

        output.put("sells", sellsJSON);

        output.put("sellsSumAmount", sumAmount);
        output.put("sellsSumTotal", sumSellingAmount);

        sumAmount = BigDecimal.ZERO;

        BigDecimal sumBuyingAmount = BigDecimal.ZERO;

        counter = 0;
        for (int i = ordersWant.size() - 1; i >= 0; i--) {

            Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Fun.Tuple2<Long, BigDecimal>> order = ordersWant.get(i);

            Map buyJSON = new LinkedHashMap();

            buyJSON.put("price", order.a.e);
            vol = order.b.b.subtract(order.b.c);
            buyJSON.put("amount", vol);

            sumAmount = sumAmount.add(vol);

            buyJSON.put("buyingPrice", Order.calcPrice(order.c.b, order.b.b));

            BigDecimal buyingAmount = Order.calcAmountWantLeft(order);

            buyJSON.put("buyingAmount", buyingAmount);

            sumBuyingAmount = sumBuyingAmount.add(buyingAmount);

            buysJSON.put(Base58.encode(order.a.a), buyJSON);

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

    @GET
    @Path("trades")
    // apitrade/trades?have=1&want=2&limit=20
    public Response trades(@QueryParam("have") Long have, @QueryParam("want") Long want,
                           @DefaultValue("100") @QueryParam("limit") Long limit) {

        ItemAssetMap map = this.dcSet.getItemAssetMap();
        // DOES ASSETID EXIST
        if (!map.contains(have)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }
        if (!map.contains(want)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        Map output = new LinkedHashMap();

        Map tradesJSON = new LinkedHashMap();

        List<Fun.Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trades = dcSet.getTradeMap().getTrades(have,
                want);

        output.put("tradesCount", trades.size());

        BigDecimal tradeWantAmount = BigDecimal.ZERO;
        BigDecimal tradeHaveAmount = BigDecimal.ZERO;

        int i = 0;
        for (Fun.Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> trade : trades) {
            if (i++ > limit) break;

            Map tradeJSON = new LinkedHashMap();

            Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Fun.Tuple2<Long, BigDecimal>> orderInitiator = Order
                    .getOrder(dcSet, trade.a);

            Fun.Tuple3<Fun.Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Fun.Tuple3<Long, BigDecimal, BigDecimal>, Fun.Tuple2<Long, BigDecimal>> orderTarget = Order
                    .getOrder(dcSet, trade.b);

            tradeJSON.put("amountHave", trade.c);
            tradeJSON.put("amountWant", trade.d);

            tradeJSON.put("realPrice", Order.calcPrice(trade.c, trade.d));
            tradeJSON.put("realReversePrice", Order.calcPrice(trade.d, trade.c));

            tradeJSON.put("initiatorTxSignature", Base58.encode(orderInitiator.a.a));

            tradeJSON.put("initiatorCreator", orderInitiator.a.b);
            tradeJSON.put("initiatorAmount", orderInitiator.b.b);
            if (orderInitiator.b.a == have) {
                tradeJSON.put("type", "sell");
                tradeWantAmount = tradeWantAmount.add(trade.c);
                tradeHaveAmount = tradeHaveAmount.add(trade.d);

            } else {
                tradeJSON.put("type", "buy");
                tradeHaveAmount = tradeHaveAmount.add(trade.c);
                tradeWantAmount = tradeWantAmount.add(trade.d);
            }
            tradeJSON.put("targetTxSignature", Base58.encode(orderTarget.a.a));
            tradeJSON.put("targetCreator", orderTarget.a.b);
            tradeJSON.put("targetAmount", orderTarget.b.b);

            tradeJSON.put("timestamp", trade.e);
            tradeJSON.put("dateTime", BlockExplorer.timestampToStr(trade.e));

            tradesJSON.put(i, tradeJSON);
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