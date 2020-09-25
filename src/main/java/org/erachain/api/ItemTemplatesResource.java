package org.erachain.api;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.blockexplorer.BlockExplorer;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("templates")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class ItemTemplatesResource {
    /**
     * Get all template type 1
     *
     * @return ArrayJSON of all template. request key means key template and name template.
     * <h2>Example request</h2>
     * GET templates
     * <h2>Example response</h2>
     * {
     * "1": "ERA",
     * "2": "COMPU",
     * "3": "АЗЫ",
     * "4": "ВЕДЫ",
     * "5": "►РА",
     * "6": "►RUNEURO",
     * "7": "►ERG",
     * "8": "►LERG",
     * "9": "►A"
     * }
     */
    @GET
    public String getTemplateesLite() {
        return JSONValue.toJSONString(BlockExplorer.getInstance().jsonQueryTemplatesLite());
    }

    /**
     * Get lite information template by key template
     *
     * @param key is number template
     * @return JSON object. Single template
     */
    @GET
    @Path("/{key}")
    public String getTemplateLite(@PathParam("key") String key) {
        Long templateAsLong = null;

        // HAS ASSET NUMBERFORMAT
        try {
            templateAsLong = Long.valueOf(key);

        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_TEMPLATE_NOT_EXIST);

        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemTemplateMap().contains(templateAsLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_TEMPLATE_NOT_EXIST);

        }

        return Controller.getInstance().getTemplate(templateAsLong).toJson().toJSONString();
    }

    @GET
    @Path("/{key}/full")
    public String getTemplate(@PathParam("key") String key) {
        Long templateAsLong = null;

        // HAS ASSET NUMBERFORMAT
        try {
            templateAsLong = Long.valueOf(key);

        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_TEMPLATE_NOT_EXIST);

        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemTemplateMap().contains(templateAsLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_TEMPLATE_NOT_EXIST);
        }

        return JSONValue.toJSONString(BlockExplorer.getInstance().jsonQueryItemTemplate(templateAsLong));
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("listfrom/{start}")
    public String getList(@PathParam("start") long start,
                          @DefaultValue("50") @QueryParam("page") int page) {

        JSONObject output = new JSONObject();
        ItemCls.makeJsonLitePage(DCSet.getInstance(), ItemCls.TEMPLATE_TYPE, start, page, output);

        return output.toJSONString();
    }

}
