package org.erachain.webserver;

//import com.google.common.collect.Iterables;
//import com.google.gson.internal.LinkedHashTreeMap;
//import com.sun.org.apache.xpath.internal.operations.Or;
//import javafx.print.Collation;

import org.erachain.api.ApiErrorFactory;
import org.erachain.api.ItemAssetsResource;
import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.datachain.OrderMapImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.utils.Pair;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

//import com.google.gson.Gson;
//import org.mapdb.Fun;

@Path("apiasset")
@Produces(MediaType.APPLICATION_JSON)
public class APIItemAsset {

    @Context
    HttpServletRequest request;

    private DCSet dcSet = DCSet.getInstance();
    private Controller cntrl = Controller.getInstance();

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("GET apiasset/last", "Get last ID");
        help.put("GET apiasset/{key}", "Get by ID");
        help.put("GET apiasset/raw/{key}", "Returns RAW in Base64 of asset with the given key.");
        help.put("GET apiasset/find?filter={name_string}&ontrade={assetKey}&from{keyID}&&offset=0&limit=0desc={descending}", "Get by words in Name. Use [ontrade] for select only traded assets (each limit*2). }Use patterns from 5 chars in words. Default {descending} - true");
        help.put("Get apiasset/image/{key}?preview", "Get Asset Image. Use 'preview' for see as small video (use it for the tiles list for example). Install `ffmpeg` for preview option, see makePreview.bat for Windows or install ffmpeg on Unix");
        help.put("Get apiasset/icon/{key}", "Get Asset Icon");
        help.put("Get apiasset/listfrom/{start}?page={pageSize}&showperson={showPerson}&desc={descending}", "Gel list from {start} limit by {pageSize}. {ShowPerson} default - true, {descending} - true. If START = -1 list from last");
        help.put("GET apiasset/text/{key", "Get description by ID");

        help.put("GET apiasset/types", "Return array of asset types.");
        help.put("GET apiasset/types/actions", "Return array of asset types and Actions for localize.");

        help.put("GET apiasset/balances/[assetKey]?position=POS&offset=OFFSET&limit=LIMIT",
                "Get balances for assetKey sorted by Own Amount. Balance positions: 0 - all positions, 1 - Own, 2 - Credit, 3 - Hold, 4 - Spend, 5 - Pledge (on DEX). Default: POS=1. Balance A - total debit. Balance B - final amount.");


        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }

    @GET
    @Path("last")
    public Response last() {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity("" + dcSet.getItemAssetMap().getLastKey())
                .build();

    }

    @GET
    @Path("{key}")
    public Response item(@PathParam("key") long key) {

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();

        ItemCls item = map.get(key);
        if (item == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
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

        if (!DCSet.getInstance().getItemAssetMap().contains(asLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        ItemCls item = Controller.getInstance().getAsset(asLong);
        byte[] issueBytes = item.toBytes(Transaction.FOR_NETWORK, false, false);
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(Base64.getEncoder().encodeToString(issueBytes))
                .build();

    }

    static String getAssetTypesCACHE = null;
    @GET
    @Path("types")
    public String getAssetTypes() {
        if (getAssetTypesCACHE != null)
            return getAssetTypesCACHE;
        return (getAssetTypesCACHE = AssetCls.typesJson().toJSONString());
    }

    static String getAssetTypesLangCACHE = null;
    @GET
    @Path("types/actions")
    public String getAssetTypesLang() {
        if (getAssetTypesLangCACHE != null)
            return getAssetTypesLangCACHE;
        return (getAssetTypesLangCACHE = AssetCls.AssetTypesActionsJson().toJSONString());
    }

    @Deprecated
    @GET
    @Path("find/{filter_name_string}")
    public static Response findOld(@PathParam("filter_name_string") String filter,
                                   @QueryParam("ontrade") Long onTradeKey,
                                   @QueryParam("from") Long fromID,
                                   @QueryParam("offset") int offset,
                                   @QueryParam("limit") int limit) {

        return find(filter, onTradeKey, fromID, offset, limit, true);
    }

    @GET
    @Path("find")
    public static Response find(@QueryParam("filter") String filter,
                                @QueryParam("ontrade") Long onTradeKey,
                                @QueryParam("from") Long fromID,
                                @QueryParam("offset") int offset,
                                @QueryParam("limit") int limit,
                                @DefaultValue("true") @QueryParam("desc") boolean descending) {

        if (limit <= 0 || limit > 25) {
            limit = 25;
        }

        if (filter == null || filter.isEmpty()) {
            return Response.status(501)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("error - empty filter")
                    .build();
        }

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        OrderMapImpl ordersMap = DCSet.getInstance().getOrderMap();

        IteratorCloseable<Long> iterator = null;
        Pair<String, IteratorCloseable<Long>> resultKeys;

        JSONArray array = new JSONArray();

        try {

            resultKeys = map.getKeysIteratorByFilterAsArray(filter, fromID, offset, descending);
            if (resultKeys.getA() == null) {

                iterator = resultKeys.getB();

                int count = 0;
                int countSkippedOnTrade = 0;
                Long key;

                while (iterator.hasNext()) {

                    key = iterator.next();

                    if (onTradeKey != null) {
                        Order bidLastOrder = ordersMap.getHaveWanFirst(key, onTradeKey);
                        Order askLastOrder = ordersMap.getHaveWanFirst(onTradeKey, key);
                        if (bidLastOrder == null && askLastOrder == null) {
                            if (countSkippedOnTrade++ > limit * 2)
                                // for stop overload
                                countSkippedOnTrade = 0;
                            else
                                continue;
                        }
                    }

                    ItemCls item = map.get(key);
                    array.add(item.toJson());

                    if (++count >= limit)
                        break;

                }
            }
        } finally {
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (IOException e) {
                }
            }
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString())
                .build();

    }

    private Response getImage(long key, boolean preview) {

        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    "Error key");
        }

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        return APIItems.getImage(request, map, key, preview);
    }

    @Path("image/{key}")
    @GET
    //@Produces({"video/mp4", "image/gif, image/png, image/jpeg"})
    public Response assetImage(@Context UriInfo info, @PathParam("key") long key) {

        boolean preview = API.checkBoolean(info, "preview");
        return getImage(key, preview);

    }

    @Path("imagepre/{key}")
    @GET
    public Response assetImagePre(@Context UriInfo info, @PathParam("key") long key) {
        return getImage(key, true);
    }

    @Path("image/{key}.mp4")
    @GET
    public Response assetImageMP4(@Context UriInfo info, @PathParam("key") long key) {
        boolean preview = API.checkBoolean(info, "preview");
        return getImage(key, preview);
    }

    @Path("imagepre/{key}.mp4")
    @GET
    //@Produces({"video/mp4", "image/gif, image/png, image/jpeg"})
    public Response assetImagePreMP4(@Context UriInfo info, @PathParam("key") long key) {
        return getImage(key, true);
    }

    @Path("icon/{key}")
    @GET
    //@Produces({"video/mp4", "image/gif, image/png, image/jpeg"})
    public Response assetIcon(@PathParam("key") long key) throws IOException {

        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Error key");
        }

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
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
        ItemCls.makeJsonLitePage(DCSet.getInstance(), ItemCls.ASSET_TYPE, start, page, output, showPerson, descending);

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(output.toJSONString())
                .build();
    }

    @GET
    @Path("balances/{key}")
    public Response getBalances(@PathParam("key") Long assetKey, @DefaultValue("0") @QueryParam("offset") Integer offset,
                                @DefaultValue("1") @QueryParam("position") Integer position,
                                @DefaultValue("50") @QueryParam("limit") Integer limit) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limit > 200)
                limit = 200;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(ItemAssetsResource.getBalances(assetKey, offset, position, limit))
                .build();
    }

    @GET
    @Path("text/{key}")
    public Response getText(@PathParam("key") Long key) {

        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ITEM_KEY);
        }

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        ItemCls item = map.get(key);

        return Response.status(200).header("Content-Type", "text/plain; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(item.getDescription())
                .build();
    }

}