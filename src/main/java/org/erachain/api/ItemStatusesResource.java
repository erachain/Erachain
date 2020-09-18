package org.erachain.api;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.blockexplorer.BlockExplorer;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONValue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("statuses")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class ItemStatusesResource {
    /**
     * Get all status type 1
     *
     * @return ArrayJSON of all status. request key means key status and name status.
     * <h2>Example request</h2>
     * GET statuss
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
    public String getStatusesLite() {
        return JSONValue.toJSONString(BlockExplorer.getInstance().jsonQueryStatusesLite());
    }

    /**
     * Get lite information status by key status
     *
     * @param key is number status
     * @return JSON object. Single status
     */
    @GET
    @Path("/{key}")
    public String getStatusLite(@PathParam("key") String key) {
        Long statusAsLong = null;

        // HAS ASSET NUMBERFORMAT
        try {
            statusAsLong = Long.valueOf(key);

        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_STATUS_NOT_EXIST);

        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemStatusMap().contains(statusAsLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_STATUS_NOT_EXIST);

        }

        return Controller.getInstance().getStatus(statusAsLong).toJson().toJSONString();
    }

    @GET
    @Path("/{key}/full")
    public String getStatus(@PathParam("key") String key) {
        Long statusAsLong = null;

        // HAS ASSET NUMBERFORMAT
        try {
            statusAsLong = Long.valueOf(key);

        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_STATUS_NOT_EXIST);

        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemStatusMap().contains(statusAsLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_STATUS_NOT_EXIST);
        }

        return JSONValue.toJSONString(BlockExplorer.getInstance().jsonQueryItemStatus(statusAsLong));
    }

}
