package org.erachain.api;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("statuses")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class ItemStatusesResource {
    @GET
    public String help() {
        Map help = new LinkedHashMap();

        help.put("statuses/{key}", "get by KEY");
        help.put("statuses/images/{key}", "get item Images by key");
        help.put("statuses/listfrom/{start}", "get list from KEY");

        help.put("POST statuses/issue", "issue");

        return StrJSonFine.convert(help);
    }

    @GET
    @Path("/{key}")
    public String get(@PathParam("key") String key) {
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
        return JSONValue.toJSONString(item.toJson());
    }

    @GET
    @Path("/images/{key}")
    public String getImages(@PathParam("key") String key) {
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

        return Controller.getInstance().getStatus(asLong).toJsonData().toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("listfrom/{start}")
    public String getList(@PathParam("start") long start,
                          @DefaultValue("20") @QueryParam("page") int page,
                          @DefaultValue("true") @QueryParam("showperson") boolean showPerson,
                          @DefaultValue("true") @QueryParam("desc") boolean descending) {

        JSONObject output = new JSONObject();
        ItemCls.makeJsonLitePage(DCSet.getInstance(), ItemCls.STATUS_TYPE, start, page, output, showPerson, descending);

        return output.toJSONString();
    }

}
