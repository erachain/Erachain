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
        help.put("POST assets/issueraw/{creator}?feePow=<int>&password=<String> ", "Issue Asset by Base58 RAW in POST body");

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

    @POST
    @Path("issueraw/{creator}")
    public String issueRAW(String x, @PathParam("creator") String creator,
                           @DefaultValue("0") @QueryParam("feePow") String feePowStr,
                           @QueryParam("password") String password) {

        Controller cntr = Controller.getInstance();
        Fun.Tuple3<PrivateKeyAccount, Integer, byte[]> result = APIUtils.postIssueRawItem(request, x, creator, feePowStr, password);
        AssetCls item;
        try {
            item = AssetFactory.getInstance().parse(result.c, false);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(
                    e.getMessage());
        }

        Transaction transaction = cntr.issueAsset(result.a, result.b, item);
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
