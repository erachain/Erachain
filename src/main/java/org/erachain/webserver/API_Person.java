package org.erachain.webserver;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.R_SetStatusToItem;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.datachain.KKPersonStatusMap;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;

@Path("apiperson")
@Produces(MediaType.APPLICATION_JSON)
public class API_Person {

    @Context
    HttpServletRequest request;

    private DCSet dcSet = DCSet.getInstance();
    private Controller cntrl = Controller.getInstance();

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("apiperson/balance/{personKey}/{assetKey}/{position}",
                "Get Asset Key balance in Position [1..5] for Person Key.");
        help.put("apiperson/status/{personKey}/{statusKey}?history=true",
                "Get Status data for Person Key. JSON ARRAY format: [timeFrom, timeTo, [par1, par2, str1, str2, reference, description], block, txNo]");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }


    /**
     * @param personKey
     * @param assetKey
     * @param pos 1..5
     * @return
     */
    @GET
    @Path("balance/{person}/{asset}/{position}")
    public Response getBalance(@PathParam("person") long personKey, @PathParam("asset") long assetKey,
                               @PathParam("position") int pos) {

        Set<String> addresses = DCSet.getInstance().getPersonAddressMap().getItems(personKey).keySet();

        Controller cont = Controller.getInstance();
        ItemAssetBalanceMap map = DCSet.getInstance().getAssetBalanceMap();
        BigDecimal sum = BigDecimal.ZERO;
        switch (pos) {
            case 1:
                for (String address : addresses) {
                    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balances = map.get(address, assetKey);
                    sum = sum.add(balances.a.b);
                }
                break;
            case 2:
                for (String address : addresses) {
                    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balances = map.get(address, assetKey);
                    sum = sum.add(balances.b.b);
                }
                break;
            case 3:
                for (String address : addresses) {
                    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balances = map.get(address, assetKey);
                    sum = sum.add(balances.c.b);
                }
                break;
            case 4:
                for (String address : addresses) {
                    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balances = map.get(address, assetKey);
                    sum = sum.add(balances.d.b);
                }
                break;
            case 5:
                for (String address : addresses) {
                    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balances = map.get(address, assetKey);
                    sum = sum.add(balances.e.b);
                }
                break;
            default:
                return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                        .header("Access-Control-Allow-Origin", "*")
                        .entity("position not found").build();

        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(sum.toPlainString()).build();
    }


    /**
     * Get Status for Person
     *                 block = value.b.d;
     *                 recNo = value.b.e;
     *                 record = Transaction.findByHeightSeqNo(dcSet, block, recNo);
     *                 return record == null ? null : record.viewTimestamp();
     * @param personKey
     * @param statusKey
     * @param history true - get history of changes
     * @return
     */
    @GET
    @Path("status/{person}/{status}")
    // http://127.0.0.1:9067/apiperson/status/1/11?history=true
    public Response getStatus(@PathParam("person") long personKey, @PathParam("status") long statusKey,
                              @QueryParam("history") boolean history) {

        KKPersonStatusMap map = DCSet.getInstance().getPersonStatusMap();
        TreeMap<Long, Stack<Fun.Tuple5<Long, Long, byte[], Integer, Integer>>> statuses = map.get(personKey);
        if (statuses == null)
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("person not found").build();

        Stack<Fun.Tuple5<Long, Long, byte[], Integer, Integer>> status = statuses.get(statusKey);
        if (status == null)
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("status not found").build();


        Fun.Tuple5<Long, Long, byte[], Integer, Integer> last = status.peek();

        JSONArray lastJSON = new JSONArray();
        lastJSON.add(last.a);
        lastJSON.add(last.b);
        lastJSON.add(R_SetStatusToItem.unpackDataJSON(last.c));
        lastJSON.add(last.d);
        lastJSON.add(last.e);

        JSONObject out = new JSONObject();
        out.put("last", lastJSON);

        if (history) {
            JSONArray historyJSON = new JSONArray();
            Iterator<Fun.Tuple5<Long, Long, byte[], Integer, Integer>> iterator = status.iterator();
            iterator.next();

            while (iterator.hasNext()) {
                Fun.Tuple5<Long, Long, byte[], Integer, Integer> item = iterator.next();
                historyJSON.add(item.a);
                historyJSON.add(item.b);
                lastJSON.add(R_SetStatusToItem.unpackDataJSON(last.c));
                historyJSON.add(item.d);
                historyJSON.add(item.e);
            }
            out.put("history", historyJSON);
        }
        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString()).build();
    }


}