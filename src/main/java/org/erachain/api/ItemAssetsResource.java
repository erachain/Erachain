package org.erachain.api;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetFactory;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.utils.APIUtils;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("assets")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j

public class ItemAssetsResource {

    @Context
    HttpServletRequest request;

    @GET
    public String help() {
        Map help = new LinkedHashMap();

        help.put("assets/last", "Get last key");
        help.put("assets/{key}", "Returns information about asset with the given key.");
        help.put("assets/raw/{key}", "Returns RAW in Base58 of asset with the given key.");
        help.put("assets/images/{key}", "get item images by KEY");
        help.put("assets/listfrom/{start}", "get list from KEY");
        help.put("POST assets/issue {\"feePow\": \"<feePow>\", \"creator\": \"<creator>\", \"name\": \"<name>\", \"description\": \"<description>\", \"icon\": \"<iconBase58>\", \"icon64\": \"<iconBase64>\", \"image\": \"<imageBase58>\", \"image64\": \"<imageBase64>\", \"scale\": \"<scale>\", \"assetType\": \"<assetType>\", \"quantity\": \"<quantity>\", \"password\": \"<password>\"}", "Issue Asset");

        help.put("assets/types", "get types");
        help.put("assets/balances/{key}", "get balances for key");

        return StrJSonFine.convert(help);
    }

    @GET
    @Path("last")
    public String last() {
        return "" + DCSet.getInstance().getItemAssetMap().getLastKey();
    }

    /**
     * Get lite information asset by key asset
     *
     * @param key is number asset
     * @return JSON object. Single asset
     */
    @GET
    @Path("{key}")
    public String get(@PathParam("key") String key) {
        Long asLong = null;

        // HAS ASSET NUMBERFORMAT
        try {
            asLong = Long.valueOf(key);

        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ITEM_KEY);

        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemAssetMap().contains(asLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);

        }

        return Controller.getInstance().getAsset(asLong).toJson().toJSONString();
    }

    @GET
    @Path("raw/{key}")
    public String getRAW(@PathParam("key") String key) {
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
        return Base58.encode(issueBytes);
    }

    /**
     *
     */
    @GET
    @Path("images/{key}")
    public String getImages(@PathParam("key") String key) {
        Long asLong = null;

        // HAS ASSET NUMBERFORMAT
        try {
            asLong = Long.valueOf(key);

        } catch (NumberFormatException e) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.INVALID_ITEM_KEY);

        }

        // DOES ASSETID EXIST
        if (!DCSet.getInstance().getItemAssetMap().contains(asLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_ASSET_NOT_EXIST);

        }

        return Controller.getInstance().getAsset(asLong).toJsonData().toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("listfrom/{start}")
    public String getList(@PathParam("start") long start,
                          @DefaultValue("20") @QueryParam("page") int page,
                          @DefaultValue("true") @QueryParam("showperson") boolean showPerson,
                          @DefaultValue("true") @QueryParam("desc") boolean descending) {

        JSONObject output = new JSONObject();
        ItemCls.makeJsonLitePage(DCSet.getInstance(), ItemCls.ASSET_TYPE, start, page, output, showPerson, descending);

        return output.toJSONString();
    }

    @POST
    @Path("issue")
    public String issue(String x) {

        Controller cntr = Controller.getInstance();
        Object result = cntr.issueAsset(request, x);
        if (result instanceof JSONObject) {
            return ((JSONObject) result).toJSONString();
        }

        Transaction transaction = (Transaction) result;
        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            JSONObject out = new JSONObject();
            Transaction.updateMapByErrorSimple(validate, out);
            return out.toJSONString();
        }
    }


    @GET
    @Path("issueraw/{creator}")
    public String issueRAW(String x, @PathParam("creator") String creator,
                           @QueryParam("feePow") String feePowStr,
                           @QueryParam("password") String password) {

        Controller cntr = Controller.getInstance();

        x = "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";
        Fun.Tuple3<PrivateKeyAccount, Integer, byte[]> result = APIUtils.postIssueRawItem(request, x, creator, feePowStr, password);
        AssetCls item;
        try {
            item = AssetFactory.getInstance().parse(result.c, false);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(
                    e.getMessage());
        }

        Transaction transaction = cntr.issueAsset(result.a, item, result.b);
        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            JSONObject out = new JSONObject();
            Transaction.updateMapByErrorSimple(validate, out);
            return out.toJSONString();
        }
    }

    @GET
    @Path("types")
    public String getAssetTypes() {
        return AssetCls.typesJson().toJSONString();
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

}
