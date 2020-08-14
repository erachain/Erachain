package org.erachain.api;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.blockexplorer.BlockExplorer;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;

@Path("assets")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j

public class ItemAssetsResource {

    @Context
    HttpServletRequest request;

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
    public String getAssetsLite() {
        return JSONValue.toJSONString(BlockExplorer.getInstance().jsonQueryAssetsLite());
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

        return JSONValue.toJSONString(BlockExplorer.getInstance().jsonQueryItemAsset(assetAsLong));
    }

    /**
     * Sorted Array
     *
     * @param assetKey
     * @param offset
     * @param position
     * @param limit
     * @return
     */
    @GET
    @Path("balances/{key}")
    public static String getBalances(@PathParam("key") Long assetKey, @DefaultValue("0") @QueryParam("offset") Integer offset,
                                     @DefaultValue("1") @QueryParam("position") Integer position,
                                     @DefaultValue("50") @QueryParam("limit") Integer limit) {

        ItemAssetBalanceMap map = DCSet.getInstance().getAssetBalanceMap();
        byte[] key;
        Crypto crypto = Crypto.getInstance();
        Fun.Tuple2<BigDecimal, BigDecimal> balance;

        JSONArray out = new JSONArray();
        int counter = limit;
        try (IteratorCloseable<byte[]> iterator = map.getIteratorByAsset(assetKey)) {
            while (iterator.hasNext()) {
                key = iterator.next();
                if (offset > 0) {
                    offset--;
                    continue;
                }

                try {
                    balance = Account.getBalanceInPosition(map.get(key), position);

                    // пустые не берем
                    if (balance.a.signum() == 0 && balance.b.signum() == 0)
                        continue;

                    JSONArray bal = new JSONArray();
                    bal.add(crypto.getAddressFromShort(ItemAssetBalanceMap.getShortAccountFromKey(key)));
                    bal.add(balance.a.toPlainString());
                    bal.add(balance.b.toPlainString());
                    out.add(bal);

                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    logger.error("Wrong key raw: " + Base58.encode(key));
                }

                if (limit > 0 && --counter <= 0) {
                    break;
                }

            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return out.toJSONString();
    }

    @POST
    @Path("/issue")
    public String issueAsset(String x) {

        JSONObject jsonObject;
        try {
            //READ JSON
            jsonObject = (JSONObject) JSONValue.parse(x);
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            ///logger.error(e.getMessage());
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }

        if (jsonObject == null)
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);

        String creatorStr = (String) jsonObject.getOrDefault("creator", null);
        String name = (String) jsonObject.getOrDefault("name", null);
        String description = (String) jsonObject.getOrDefault("description", null);
        String iconStr = (String) jsonObject.getOrDefault("icon64", null);
        byte[] icon = Base64.getDecoder().decode(iconStr);
        String imageStr = (String) jsonObject.getOrDefault("image", null);

        int feePow = Integer.valueOf(jsonObject.getOrDefault("feePow", 0).toString());
        long assetKey = Long.valueOf(jsonObject.getOrDefault("assetKey", 0l).toString());
        BigDecimal amount = new BigDecimal(jsonObject.getOrDefault("amount", 0).toString());
        String title = (String) jsonObject.getOrDefault("title", null);
        int encoding = Integer.valueOf(jsonObject.getOrDefault("encoding", 0).toString());
        boolean encrypt = Boolean.valueOf((boolean) jsonObject.getOrDefault("encrypt", false));
        String password = (String) jsonObject.getOrDefault("password", null);

        APIUtils.askAPICallAllowed(password, "GET send\n ", request, true);

        JSONObject out = new JSONObject();
        Controller cntr = Controller.getInstance();

        Account creator = null;
        if (creatorStr == null) {
            out.put("error", Transaction.INVALID_CREATOR);
            out.put("error_message", OnDealClick.resultMess(Transaction.INVALID_CREATOR));
            return out.toJSONString();
        } else {
            Fun.Tuple2<Account, String> resultCreator = Account.tryMakeAccount(creatorStr);
            if (resultCreator.a == null) {
                out.put("error", Transaction.INVALID_CREATOR);
                out.put("error_message", resultCreator.b);
                return out.toJSONString();
            }
            creator = resultCreator.a;
        }

        boolean needAmount = false;
        Pair<Integer, Transaction> result = cntr.issueAsset(creator, name, description, feePowStr,
                assetKey, true,
                amount, needAmount,
                title, message, encoding, encrypt, 0);

        Transaction transaction = result.getB();
        if (transaction == null) {
            out.put("error", result.getA());
            out.put("error_message", OnDealClick.resultMess(result.getA()));
            return out.toJSONString();
        }

        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            out.put("error", validate);
            out.put("error_message", OnDealClick.resultMess(validate));
            return out.toJSONString();
        }
    }

}
