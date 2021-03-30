package org.erachain.webserver;


import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemTemplateMap;
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

@Path("apitemplate")
@Produces(MediaType.APPLICATION_JSON)
public class APIItemTemplate {

    @Context
    HttpServletRequest request;

    private DCSet dcSet = DCSet.getInstance();
    private Controller cntrl = Controller.getInstance();

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("GET apitemplate/last", "Get last ID");
        help.put("GET apitemplate/{key}", "GET by ID");
        help.put("GET apitemplate/raw/{key}", "Returns RAW in Base58 of template with the given key.");
        help.put("GET apitemplate/find?filter={name_string}&from{keyID}&&offset=0&limit=0desc={descending}", "Get by words in Name. Use patterns from 5 chars in words. Default {descending} - true");
        help.put("Get apitemplate/image/{key}", "GET Template Image");
        help.put("Get apitemplate/icon/{key}", "GET Template Icon");
        help.put("Get apitemplate/listfrom/{start}?page={pageSize}&showperson={showPerson}&desc={descending}", "Gel list from {start} limit by {pageSize}. {ShowPerson} default - true, {descending} - true. If START = -1 list from last");
        help.put("GET apitemplate/text/{key", "Get description by ID");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }

    @GET
    @Path("last")
    public Response last() {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity("" + dcSet.getItemTemplateMap().getLastKey())
                .build();

    }

    @GET
    @Path("{key}")
    public Response item(@PathParam("key") long key) {

        ItemTemplateMap map = DCSet.getInstance().getItemTemplateMap();

        ItemCls item = map.get(key);
        if (item == null) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_TEMPLATE_NOT_EXIST);
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

        if (!DCSet.getInstance().getItemTemplateMap().contains(asLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_TEMPLATE_NOT_EXIST);
        }

        ItemCls item = Controller.getInstance().getTemplate(asLong);
        byte[] issueBytes = item.toBytes(Transaction.FOR_NETWORK, false, false);
        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(Base58.encode(issueBytes))
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

        ItemTemplateMap map = DCSet.getInstance().getItemTemplateMap();
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
    @Produces({"image/png", "image/jpg"})
    public Response templateImage(@PathParam("key") long key) throws IOException {

        int weight = 0;
        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Error key");
        }

        ItemTemplateMap map = DCSet.getInstance().getItemTemplateMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_TEMPLATE_NOT_EXIST);
        }

        TemplateCls template = (TemplateCls) map.get(key);

        if (template.getImage() != null) {
            // image to byte[] hot scale (param2 =0)
            //	byte[] b = ImagesTools.ImageToByte(new ImageIcon(person.getImage()).getImage(), 0);
            ///return Response.ok(new ByteArrayInputStream(template.getImage())).build();
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ByteArrayInputStream(template.getImage()))
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
    public Response templateIcon(@PathParam("key") long key) throws IOException {

        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Error key");
        }

        ItemTemplateMap map = DCSet.getInstance().getItemTemplateMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_TEMPLATE_NOT_EXIST);
        }

        TemplateCls template = (TemplateCls) map.get(key);

        if (template.getIcon() != null) {
            // image to byte[] hot scale (param2 =0)
            //	byte[] b = ImagesTools.ImageToByte(new ImageIcon(person.getImage()).getImage(), 0);
            //return Response.ok(new ByteArrayInputStream(template.getIcon())).build();
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ByteArrayInputStream(template.getIcon()))
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
        ItemCls.makeJsonLitePage(DCSet.getInstance(), ItemCls.TEMPLATE_TYPE, start, page, output, showPerson, descending);

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

        ItemTemplateMap map = DCSet.getInstance().getItemTemplateMap();

        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_TEMPLATE_NOT_EXIST);
        }

        ItemCls item = map.get(key);

        return Response.status(200).header("Content-Type", "text/plain; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(item.getDescription())
                .build();
    }

}