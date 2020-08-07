package org.erachain.webserver;

import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSetStatusToItem;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemPersonMap;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Path("apiperson")
@Produces(MediaType.APPLICATION_JSON)
public class APIPerson {

    @Context
    HttpServletRequest request;

    private DCSet dcSet = DCSet.getInstance();
    private Controller cntrl = Controller.getInstance();

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("apiperson/balance/{personKey}/{assetKey}/{position}?side=[]side",
                "Get Asset Key balance in Position [1..5] for Person Key. Balance Side =0 - total debit; =1 - left; =2 - total credit");
        help.put("apiperson/status/{personKey}/{statusKey}?history=true",
                "Get Status data for Person Key. JSON ARRAY format: [timeFrom, timeTo, [par1, par2, str1, str2, reference, description], block, txNo]");

        help.put("Get apiperson/image/{key}", "GET Person Image");
        help.put("Get apiperson/icon/{key}", "GET Person Icon");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }


    /**
     * @param personKey
     * @param assetKey
     * @param pos       1..5
     * @return
     */
    @GET
    @Path("balance/{person}/{asset}/{position}")
    public Response getBalance(@PathParam("person") long personKey, @PathParam("asset") long assetKey,
                               @PathParam("position") int pos,
                               @DefaultValue("1") @QueryParam("side") int side) {

        BigDecimal sum = PersonCls.getBalance(personKey, assetKey, pos, side);

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
        if (status == null || status.isEmpty())
            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("status not found").build();


        Fun.Tuple5<Long, Long, byte[], Integer, Integer> last = status.peek();

        JSONArray lastJSON = new JSONArray();
        lastJSON.add(last.a);
        lastJSON.add(last.b);
        lastJSON.add(RSetStatusToItem.unpackDataJSON(last.c));
        lastJSON.add(last.d);
        lastJSON.add(last.e);

        JSONObject out = new JSONObject();
        out.put("last", lastJSON);

        out.put("text", DCSet.getInstance().getItemStatusMap().get(statusKey).toString(DCSet.getInstance(), last.c));

        if (history) {
            JSONArray historyJSON = new JSONArray();
            Iterator<Fun.Tuple5<Long, Long, byte[], Integer, Integer>> iterator = status.iterator();
            iterator.next();

            while (iterator.hasNext()) {
                Fun.Tuple5<Long, Long, byte[], Integer, Integer> item = iterator.next();
                JSONArray historyItemJSON = new JSONArray();

                historyItemJSON.add(item.a);
                historyItemJSON.add(item.b);
                historyItemJSON.add(RSetStatusToItem.unpackDataJSON(last.c));
                historyItemJSON.add(item.d);
                historyItemJSON.add(item.e);

                historyJSON.add(historyItemJSON);
            }
            out.put("history", historyJSON);
        }
        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString()).build();
    }

    @Path("image/{key}")
    @GET
    @Produces({"image/png", "image/jpg"})
    public Response personImage(@PathParam("key") long key) throws IOException {

        int weight = 0;
        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Error key");
        }

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        PersonCls person = (PersonCls) map.get(key);

        if (person.getImage() != null) {
            // image to byte[] hot scale (param2 =0)
            //	byte[] b = Images_Work.ImageToByte(new ImageIcon(person.getImage()).getImage(), 0);
            ///return Response.ok(new ByteArrayInputStream(person.getImage())).build();
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ByteArrayInputStream(person.getImage()))
                    .build();
        }
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity("")
                .build();

    }

    @Path("icon/{key}")
    @GET
    @Produces({"image/png", "image/jpg"})
    public Response personIcon(@PathParam("key") long key) throws IOException {

        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Error key");
        }

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        PersonCls person = (PersonCls) map.get(key);

        if (person.getIcon() != null) {
            // image to byte[] hot scale (param2 =0)
            //	byte[] b = Images_Work.ImageToByte(new ImageIcon(person.getImage()).getImage(), 0);
            //return Response.ok(new ByteArrayInputStream(person.getIcon())).build();
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ByteArrayInputStream(person.getIcon()))
                    .build();
        }
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity("")
                .build();
    }


}