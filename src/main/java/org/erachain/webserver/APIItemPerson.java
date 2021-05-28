package org.erachain.webserver;

import org.apache.commons.net.util.Base64;
import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.statuses.StatusCls;
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
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Path("apiperson")
@Produces(MediaType.APPLICATION_JSON)
public class APIItemPerson {

    @Context
    HttpServletRequest request;

    private DCSet dcSet = DCSet.getInstance();
    private Controller cntrl = Controller.getInstance();

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("GET apiperson/last", "Get last ID");
        help.put("GET apiperson/{key}", "GET by ID");
        help.put("GET apiperson/raw/{key}", "Returns RAW in Base58 of person with the given key.");
        help.put("GET apiperson/find?filter={name_string}&from{keyID}&&offset=0&limit=0desc={descending}", "Get by words in Name. Use patterns from 5 chars in words. Default {descending} - true");
        help.put("Get apiperson/image/{key}", "GET Person Image");
        help.put("Get apiperson/icon/{key}", "GET Person Icon");
        help.put("Get apiperson/listfrom/{start}?page={pageSize}&showperson={showPerson}&desc={descending}", "Gel list from {start} limit by {pageSize}. {ShowPerson} default - true, {descending} - true. If START = -1 list from last");
        help.put("GET apiperson/text/{key", "Get description by ID");

        help.put("GET apiperson/balance/{personKey}/{assetKey}/{position}?side=[]side",
                "Get Asset Key balance in Position [1..5] for Person Key. Balance Side =0 - total debit; =1 - left; =2 - total credit");
        help.put("GET apiperson/status/{personKey}/{statusKey}?history=true",
                "Get Status data for Person Key. JSON ARRAY format: [timeFrom, timeTo, [par1, par2, str1, str2, reference, description], block, txNo]");

        help.put("GET apiperson/addresses/{key}?full", "Get Person Addresses");
        help.put("GET apiperson/statuses/{key}", "Get Person Statuses");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }

    @GET
    @Path("last")
    public Response last() {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity("" + dcSet.getItemPersonMap().getLastKey())
                .build();

    }

    @GET
    @Path("{key}")
    public Response item(@PathParam("key") long key) {

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();

        ItemCls item = map.get(key);
        if (item == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(item.toJson().toJSONString())
                .build();

    }

    @GET
    @Path("raw/{key}")
    public Response getRAW(@PathParam("key") String key) {
        Long asLong = null;

        try {
            asLong = Long.valueOf(key);
        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ITEM_KEY);
        }

        if (!DCSet.getInstance().getItemPersonMap().contains(asLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        ItemCls item = Controller.getInstance().getPerson(asLong);
        byte[] issueBytes = item.toBytes(Transaction.FOR_NETWORK, false, false);
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(Base58.encode(issueBytes))
                .build();
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

    @Deprecated
    @GET
    @Path("find/{filter_name_string}")
    public static Response findOld(@PathParam("filter_name_string") String filter,
                                   @QueryParam("from") Long fromID,
                                   @QueryParam("offset") int offset,
                                   @QueryParam("limit") int limit) {

        return find(filter, fromID, offset, limit, true);
    }

    @GET
    @Path("find")
    public static Response find(@QueryParam("filter") String filter,
                                @QueryParam("from") Long fromID,
                                @QueryParam("offset") int offset,
                                @QueryParam("limit") int limit,
                                @DefaultValue("true") @QueryParam("desc") boolean descending) {

        if (limit > 100) {
            limit = 100;
        }

        if (filter == null || filter.isEmpty()) {
            return Response.status(501)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("error - empty filter")
                    .build();
        }

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();
        List<ItemCls> list = map.getByFilterAsArray(filter, fromID, offset, limit, descending);

        JSONArray array = new JSONArray();

        if (list != null) {
            for (ItemCls item : list) {
                array.add(item.toJson());
            }
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString())
                .build();

    }

    @Path("image/{key}")
    @GET
    //@Produces({"video/mp4", "image/jpeg, image/gif"})
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

        return APIItems.getImage(map, key, false);

    }

    @Path("icon/{key}")
    @GET
    @Produces({"video/mp4", "image/gif, image/png, image/jpeg"})
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

        return APIItems.getIcon(map, key);

    }

    @GET
    @Path("listfrom/{start}")
    public Response getList(@PathParam("start") long start,
                            @DefaultValue("20") @QueryParam("page") int page,
                            @DefaultValue("true") @QueryParam("showperson") boolean showPerson,
                            @DefaultValue("true") @QueryParam("desc") boolean descending) {

        if (page > 50 || page < 1) {
            page = 50;
        }

        JSONObject output = new JSONObject();
        ItemCls.makeJsonLitePage(DCSet.getInstance(), ItemCls.PERSON_TYPE, start, page, output, showPerson, descending);

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(output.toJSONString())
                .build();
    }

    @GET
    @Path("text/{key}")
    public Response getText(@PathParam("key") Long key) {

        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ITEM_KEY);
        }

        ItemPersonMap map = DCSet.getInstance().getItemPersonMap();

        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        ItemCls item = map.get(key);

        return Response.status(200).header("Content-Type", "text/plain; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(item.getDescription())
                .build();
    }

    @GET
    @Path("addresses/{key}?full")
    public Response getAddresses(@Context UriInfo info, @PathParam("key") Long key) {
        if (DCSet.getInstance().getItemPersonMap().get(key) == null) {
            JSONObject out = new JSONObject();
            out.put("error", "Person not Found");
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(out.toJSONString())
                    .build();

        } else {
            JSONArray out;
            if (API.checkBoolean(info, "full")) {
                out = PersonCls.toJsonAddresses(key);

            } else {
                TreeMap<String, Stack<Fun.Tuple3<Integer, Integer, Integer>>> addresses
                        = DCSet.getInstance().getPersonAddressMap().getItems(new Long(key));

                out = new JSONArray();
                out.addAll(addresses.keySet());

            }
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(out.toJSONString())
                    .build();
        }
    }

    @GET
    @Path("statuses/{key}")
    public Response getStatuses(@PathParam("key") Long key) {
        if (DCSet.getInstance().getItemPersonMap().get(key) == null) {
            JSONObject out = new JSONObject();
            out.put("error", "Person not Found");
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(out.toJSONString())
                    .build();
        } else {
            TreeMap<Long, Stack<Fun.Tuple5<Long, Long, byte[], Integer, Integer>>> statuses = dcSet.getPersonStatusMap().get(key);

            int block;
            int seqNo;
            Transaction tx;
            JSONArray out = new JSONArray();

            for (Fun.Tuple3<Long, StatusCls, Fun.Tuple5<Long, Long, byte[], Integer, Integer>> item : StatusCls.getSortedItems(statuses)) {
                Map statusJSON = new LinkedHashMap();
                StatusCls status = item.b;

                statusJSON.put("key", item.a);
                statusJSON.put("icon", Base64.encodeBase64String(status.getIcon()));

                statusJSON.put("name", status.toString(dcSet, item.c.c));

                statusJSON.put("start", item.c.a);
                statusJSON.put("stop", item.c.b);
                statusJSON.put("period", StatusCls.viewPeriod(item.c.a, item.c.b));

                block = item.c.d;
                seqNo = item.c.e;
                statusJSON.put("seqNo", Transaction.viewDBRef(block, seqNo));

                tx = Transaction.findByHeightSeqNo(dcSet, block, seqNo);
                Account creator = tx.getCreator();
                if (creator != null) {
                    statusJSON.put("creator", creator.getAddress());
                    if (creator.isPerson()) {
                        statusJSON.put("creator_name", creator.getPerson().b.viewName());
                    }
                } else {
                    statusJSON.put("creator", GenesisBlock.CREATOR.getAddress());
                    statusJSON.put("creator_name", "GENESIS");
                }

                out.add(statusJSON);
            }
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(out.toJSONString())
                    .build();
        }
    }

}