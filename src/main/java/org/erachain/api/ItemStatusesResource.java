package org.erachain.api;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.statuses.StatusFactory;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.utils.APIUtils;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("statuses")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class ItemStatusesResource {

    @Context
    HttpServletRequest request;

    @GET
    public String help() {
        Map help = new LinkedHashMap();

        help.put("statuses/last", "Get last key");
        help.put("statuses/{key}", "Returns information about status with the given key.");
        help.put("statuses/raw/{key}", "Returns RAW in Base58 of status with the given key.");
        help.put("statuses/images/{key}", "get item Images by key");
        help.put("statuses/listfrom/{start}", "get list from KEY");
        help.put("POST statuses/issueraw/{creator}?linkTo=<SeqNo>&feePow=<int>&password=<String> ", "Issue Status by Base58 RAW in POST body");
        help.put("POST statuses/issueraw/{creator} {\"linkTo\":<SeqNo>, \"feePow\":<int>, \"password\":<String>, \"linkTo\":<SeqNo>, \"raw\":RAW-Base58", "Issue Statuse by Base58 RAW in POST body");

        //help.put("POST statuses/issue", "issue");

        return StrJSonFine.convert(help);
    }

    @GET
    @Path("last")
    public String last() {
        return "" + DCSet.getInstance().getItemStatusMap().getLastKey();
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
    @Path("raw/{key}")
    public String getRAW(@PathParam("key") String key) {
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
        byte[] issueBytes = item.toBytes(Transaction.FOR_NETWORK, false, false);
        return Base58.encode(issueBytes);
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

    @POST
    @Path("issueraw/{creator}")
    public String issueRAW(String x, @PathParam("creator") String creatorStr) {

        Controller cntr = Controller.getInstance();
        Object result = Transaction.decodeJson(creatorStr, x);
        if (result instanceof JSONObject) {
            return result.toString();
        }

        Fun.Tuple5<Account, Integer, ExLink, String, JSONObject> resultHead = (Fun.Tuple5<Account, Integer, ExLink, String, JSONObject>) result;
        Account creator = resultHead.a;
        int feePow = resultHead.b;
        ExLink linkTo = resultHead.c;
        String password = resultHead.d;
        JSONObject jsonObject = resultHead.e;

        Fun.Tuple2<PrivateKeyAccount, byte[]> resultRaw = APIUtils.postIssueRawItem(request, jsonObject.get("raw").toString(),
                creator, password, "issue Status");

        StatusCls item;
        try {
            item = StatusFactory.getInstance().parse(Transaction.FOR_NETWORK, resultRaw.b, false);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(
                    e.getMessage());
        }

        Transaction transaction = cntr.issueStatus(resultRaw.a, linkTo, feePow, item);
        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            JSONObject out = new JSONObject();
            Transaction.updateMapByErrorSimple(validate, out);
            return out.toJSONString();
        }
    }

}
