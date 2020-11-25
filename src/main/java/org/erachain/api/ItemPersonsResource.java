package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.persons.PersonFactory;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("persons")
@Produces(MediaType.APPLICATION_JSON)
public class ItemPersonsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemPersonsResource.class);

    @Context
    HttpServletRequest request;

    @SuppressWarnings("unchecked")
    @GET
    public String help() {
        Map help = new LinkedHashMap();

        help.put("persons/last", "Get last key");
        help.put("persons/{key}", "Returns information about person with the given key.");
        help.put("persons/raw/{key}", "Returns RAW in Base58 of person with the given key.");
        help.put("persons/images/{key}", "get item Images by key");
        help.put("persons/listfrom/{start}", "get list from KEY");
        help.put("POST persons/issue {\"feePow\": \"<feePow>\", \"creator\": \"<creator>\", \"name\": \"<name>\", \"description\": \"<description>\", \"icon\": \"<iconBase58>\", \"icon64\": \"<iconBase64>\", \"image\": \"<imageBase58>\", \"image64\": \"<imageBase64>\", \"birthday\": \"long\", \"deathday\": \"<long>\", \"gender\": \"<int>\", \"race\": String, \"birthLatitude\": float, \"birthLongitude\": float, \"skinColor\": String, \"eyeColor\": String, \"hairСolor\": String, \"height\": int, \"owner\": Base58-PubKey, \"ownerSignature\": Base58, \"\": ,     \"password\": \"<password>\"}", "issue");
        help.put("POST persons/issueraw/{creator}?feePow=<int>&password=<String> ", "Issue Person by Base58 RAW in POST body");

        return StrJSonFine.convert(help);
    }

    @GET
    @Path("last")
    public String last() {
        return "" + DCSet.getInstance().getItemPersonMap().getLastKey();
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

        if (!DCSet.getInstance().getItemPersonMap().contains(asLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        ItemCls item = Controller.getInstance().getPerson(asLong);
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

        if (!DCSet.getInstance().getItemPersonMap().contains(asLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        ItemCls item = Controller.getInstance().getPerson(asLong);
        byte[] issueBytes = item.toBytes(false, false);
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

        if (!DCSet.getInstance().getItemPersonMap().contains(asLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        return Controller.getInstance().getPerson(asLong).toJsonData().toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("listfrom/{start}")
    public String getList(@PathParam("start") long start,
                          @DefaultValue("20") @QueryParam("page") int page,
                          @DefaultValue("true") @QueryParam("showperson") boolean showPerson,
                          @DefaultValue("true") @QueryParam("desc") boolean descending) {

        JSONObject output = new JSONObject();
        ItemCls.makeJsonLitePage(DCSet.getInstance(), ItemCls.PERSON_TYPE, start, page, output, showPerson, descending);

        return output.toJSONString();
    }

    @POST
    @Path("/issue")
    public String issue(String x) {

        Controller cntr = Controller.getInstance();
        Object result = cntr.issuePerson(request, x);
        if (result instanceof JSONObject) {
            return ((JSONObject) result).toJSONString();
        }

        Pair<Transaction, Integer> resultGood = (Pair<Transaction, Integer>) result;
        if (resultGood.getB() != Transaction.VALIDATE_OK) {
            JSONObject out = new JSONObject();
            Transaction.updateMapByErrorSimple(resultGood.getB(), out);
            return out.toJSONString();
        }

        Transaction transaction = resultGood.getA();
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
        PersonCls item;
        try {
            item = PersonFactory.getInstance().parse(result.c, false);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(
                    e.getMessage());
        }

        Pair<Transaction, Integer> transactionResult = cntr.issuePerson(result.a, result.b, item);
        if (transactionResult.getB() != Transaction.VALIDATE_OK) {
            throw ApiErrorFactory.getInstance().createError(
                    transactionResult.getB());
        }

        Transaction transaction = transactionResult.getA();
        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            JSONObject out = new JSONObject();
            Transaction.updateMapByErrorSimple(validate, out);
            return out.toJSONString();
        }
    }


}
