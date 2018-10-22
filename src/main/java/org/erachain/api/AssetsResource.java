package org.erachain.api;

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

@Path("assets")
@Produces(MediaType.APPLICATION_JSON)
public class AssetsResource {
    /**
     * Get all asset type 1
     *
     * @return ArrayJSON of all asset. request key means key asset and name asset.
     * <h2>Example request</h2>
     * GET assets
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
    public String getAseetsLite() {
        return JSONValue.toJSONString(BlockExplorer.getInstance().jsonQueryAssetsLite());
    }

    /**
     * Get full information by asset
     *
     * @return ArrayJson full information by asset
     * <h2>Example request</h2>
     * GET assets/full
     * <h2>Example response</h2>
     * {
     * "1": {
     * "key": 1,
     * "name": "ERA",
     * "description": "Основная учётная единица, мера собственности и управления данной средой - \"правовая\",
     * \"управляющая\": ERA(ERA). Именно единицы Эра позволяют собирать блоки и получать комиссию с упакованных
     * в них транзакций. Более чем 100 ЭРА, находящихся в пользовании на счету позволяет собирать блоки (форжить)
     * с этого счёта, а более чем 1000 позволяет удостоверять других участников среды. Число единиц
     * 100000 ЭРА дает права создавать новые статусы и другие сущности в среде.",
     * "owner": "73EotEbxvAo39tyugJSyL5nbcuMWs4aUpS",
     * "quantity": "9`999`000",
     * "scale": 8,
     * "assetType": "Digital Asset",
     * "img": "",
     * "icon": "",
     * "operations": 63
     * }
     * }
     */
    @GET
    @Path("/full")
    public String getAssetsFull() {
        return JSONValue.toJSONString(BlockExplorer.getInstance().jsonQueryAssets());
    }

    /**
     * Get lite information asset by key asset
     * @param key is number asset
     * @return JSON object. Single asset
     */
    @GET
    @Path("/{key}")
    public String getAssetLite(@PathParam("key") String key) {
        Long assetAsLong = null;

        // HAS ASSET NUMBERFORMAT
        try {
            assetAsLong = Long.valueOf(key);

        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);

        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemAssetMap().contains(assetAsLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);

        }

        return Controller.getInstance().getAsset(assetAsLong).toJson().toJSONString();
    }

    @GET
    @Path("/{key}/full")
    public String getAsset(@PathParam("key") String key) {
        Long assetAsLong = null;

        // HAS ASSET NUMBERFORMAT
        try {
            assetAsLong = Long.valueOf(key);

        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);

        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemAssetMap().contains(assetAsLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_ASSET_NOT_EXIST);
        }

        return JSONValue.toJSONString(BlockExplorer.getInstance().jsonQueryAsset(assetAsLong));
    }
}
