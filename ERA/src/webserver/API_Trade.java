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
import datachain.TransactionFinalMap;
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

            sellJSON.put("sellingPrice", Order.calcPrice(order.getAmountWant(), order.getAmountHave()));

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
            vol = order.getAmountHaveLeft()
            buyJSON.put("amount", vol);

            sumAmount = sumAmount.add(vol);

            buyJSON.put("buyingPrice", Order.calcPrice(order.getAmountWant(), order.getAmountHave()));

            BigDecimal buyingAmount = order.getAmountWantLeft();

            buyJSON.put("buyingAmount", buyingAmount);

            sumBuyingAmount = sumBuyingAmount.add(buyingAmount);

            buysJSON.put(order.getId(), buyJSON);

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

        List<Fun.Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> trades = dcSet.getTradeMap().getTrades(have,
                want);

        output.put("tradesCount", trades.size());

        BigDecimal tradeWantAmount = BigDecimal.ZERO;
        BigDecimal tradeHaveAmount = BigDecimal.ZERO;

        TransactionFinalMap finalMap = DCSet.getInstance().getTransactionFinalMap();
        Transaction createOrder;

        int i = 0;
        for (Fun.Tuple5<Long, Long, BigDecimal, BigDecimal, Long> trade : trades) {
            if (i++ > limit) break;

            Map tradeJSON = new LinkedHashMap();

            Order orderInitiator = Order.getOrder(dcSet, trade.a);

            Order orderTarget = Order.getOrder(dcSet, trade.b);

            tradeJSON.put("amountHave", trade.c);
            tradeJSON.put("amountWant", trade.d);

            tradeJSON.put("realPrice", Order.calcPrice(trade.c, trade.d));
            tradeJSON.put("realReversePrice", Order.calcPrice(trade.d, trade.c));

            createOrder = finalMap.get(orderInitiator.getId());
            tradeJSON.put("initiatorTxSignature", Base58.encode(createOrder.getSignature()));

            tradeJSON.put("initiatorCreator", orderInitiator.getCreator().getAddress());
            tradeJSON.put("initiatorAmount", orderInitiator.getAmountHave());
            if (orderInitiator.getHave() == have) {
                tradeJSON.put("type", "sell");
                tradeWantAmount = tradeWantAmount.add(trade.c);
                tradeHaveAmount = tradeHaveAmount.add(trade.d);

            } else {
                tradeJSON.put("type", "buy");
                tradeHaveAmount = tradeHaveAmount.add(trade.c);
                tradeWantAmount = tradeWantAmount.add(trade.d);
            }

            createOrder = finalMap.get(orderInitiator.getId());
            tradeJSON.put("targetTxSignature", Base58.encode(createOrder.getSignature()));
            tradeJSON.put("targetCreator", orderTarget.getCreator().getAddress());
            tradeJSON.put("targetAmount", orderTarget.getAmountHave());

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