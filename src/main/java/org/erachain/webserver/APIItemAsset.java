package org.erachain.webserver;

//import com.google.common.collect.Iterables;
//import com.google.gson.internal.LinkedHashTreeMap;
//import com.sun.org.apache.xpath.internal.operations.Or;
//import javafx.print.Collation;

import org.erachain.api.ApiErrorFactory;
import org.erachain.api.ItemAssetsResource;
import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
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
        help.put("GET apiasset/raw/{key}", "Returns RAW in Base58 of asset with the given key.");
        help.put("GET apiasset/find?filter={name_string}&offset=0&limit=0", "Get by words in Name. Use patterns from 5 chars in words");
        help.put("Get apiasset/image/{key}", "Get Asset Image");
        help.put("Get apiasset/icon/{key}", "Get Asset Icon");
        help.put("Get apiasset/listfrom/{start}?page={pageSize}&showperson={showPerson}&desc={descending}", "Gel list from {start} limit by {pageSize}. {ShowPerson} default - true, {descending} - true. If START = -1 list from last");
        help.put("GET apiasset/text/{key", "Get description by ID");

        help.put("GET apiasset/types", "Return array of asset types.");
        help.put("GET apiasset/types/actions", "Return array of asset types and Actions for localize.");

        help.put("GET apiasset/balances/[assetKey]?position=POS&offset=OFFSET&limit=LIMIT",
                "Get balances for assetKey sorted by Own Amount. Balance positions: 1 - Own, 2 - Credit, 3 - Hold, 4 - Spend, 5 - Other. Default: POS=1. Balance A - total debit. Balance B - final amount.");


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
        byte[] issueBytes = item.toBytes(false, false);
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(Base58.encode(issueBytes))
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
                                   @QueryParam("from") Long fromID,
                                   @QueryParam("offset") int offset,
                                   @QueryParam("limit") int limit) {

        return find(filter, fromID, offset, limit);
    }

    @GET
    @Path("find")
    public static Response find(@QueryParam("filter") String filter,
                                @QueryParam("from") Long fromID,
                                @QueryParam("offset") int offset,
                                @QueryParam("limit") int limit) {

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

        ItemAssetMap map = DCSet.getInstance().getItemAssetMap();
        List<ItemCls> list = map.getByFilterAsArray(filter, fromID, offset, limit, true);

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
    @Produces({"image/png", "image/jpg"})
    public Response assetImage(@PathParam("key") long key) throws IOException {

        int weight = 0;
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

        AssetCls asset = (AssetCls) map.get(key);

        if (asset.getImage() != null) {
            // image to byte[] hot scale (param2 =0)
            //	byte[] b = Images_Work.ImageToByte(new ImageIcon(person.getImage()).getImage(), 0);
            ///return Response.ok(new ByteArrayInputStream(asset.getImage())).build();
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ByteArrayInputStream(asset.getImage()))
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

        AssetCls asset = (AssetCls) map.get(key);

        if (asset.getIcon() != null) {
            // image to byte[] hot scale (param2 =0)
            //	byte[] b = Images_Work.ImageToByte(new ImageIcon(person.getImage()).getImage(), 0);
            //return Response.ok(new ByteArrayInputStream(asset.getIcon())).build();
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ByteArrayInputStream(asset.getIcon()))
                    .build();
        }
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity("")
                .build();
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