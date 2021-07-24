package org.erachain.webserver;

import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemStatusMap;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("apistatus")
@Produces(MediaType.APPLICATION_JSON)
public class APIItemStatus {

    @Context
    HttpServletRequest request;

    private DCSet dcSet = DCSet.getInstance();
    private Controller cntrl = Controller.getInstance();

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("GET apistatus/last", "Get last ID");
        help.put("GET apistatus/{key}", "GET by ID");
        help.put("GET apistatus/raw/{key}", "Returns RAW in Base64 of status with the given key.");
        help.put("GET apistatus/find?filter={name_string}&from{keyID}&&offset=0&limit=0desc={descending}", "Get by words in Name. Use patterns from 5 chars in words. Default {descending} - true");
        help.put("Get apistatus/image/{key}", "GET Status Image");
        help.put("Get apistatus/icon/{key}", "GET Status Icon");
        help.put("Get apistatus/listfrom/{start}?page={pageSize}&showperson={showPerson}&desc={descending}", "Gel list from {start} limit by {pageSize}. {ShowPerson} default - true, {descending} - true. If START = -1 list from last");
        help.put("GET apistatus/text/{key", "Get description by ID");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }

    @GET
    @Path("last")
    public Response last() {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity("" + dcSet.getItemStatusMap().getLastKey())
                .build();

    }

    @GET
    @Path("{key}")
    public Response item(@PathParam("key") long key) {

        ItemStatusMap map = DCSet.getInstance().getItemStatusMap();

        ItemCls item = map.get(key);
        if (item == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_STATUS_NOT_EXIST);
        }
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(item.toJson())
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

        if (!DCSet.getInstance().getItemStatusMap().contains(asLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_STATUS_NOT_EXIST);
        }

        ItemCls item = Controller.getInstance().getStatus(asLong);
        byte[] issueBytes = item.toBytes(Transaction.FOR_NETWORK, false, false);
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(Base64.getEncoder().encodeToString(issueBytes))
                .build();
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

        ItemStatusMap map = DCSet.getInstance().getItemStatusMap();
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
    //@Produces({"video/mp4", "image/gif, image/png, image/jpeg"})
    public Response statusImage(@PathParam("key") long key) throws IOException {

        int weight = 0;
        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Error key");
        }

        ItemStatusMap map = DCSet.getInstance().getItemStatusMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_STATUS_NOT_EXIST);
        }

        return APIItems.getImage(request, map, key, false);

    }

    @Path("icon/{key}")
    @GET
    //@Produces({"video/mp4", "image/gif, image/png, image/jpeg"})
    public Response statusIcon(@PathParam("key") long key) throws IOException {

        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Error key");
        }

        ItemStatusMap map = DCSet.getInstance().getItemStatusMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_STATUS_NOT_EXIST);
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
        ItemCls.makeJsonLitePage(DCSet.getInstance(), ItemCls.STATUS_TYPE, start, page, output, showPerson, descending);

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

        ItemStatusMap map = DCSet.getInstance().getItemStatusMap();

        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_STATUS_NOT_EXIST);
        }

        ItemCls item = map.get(key);

        return Response.status(200).header("Content-Type", "text/plain; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(item.getDescription())
                .build();
    }

}