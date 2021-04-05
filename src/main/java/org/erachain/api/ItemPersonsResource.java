package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
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

    public static Map help = new LinkedHashMap<String, String>() {{
        put("persons/last", "Get last key");
        put("persons/{key}", "Returns information about person with the given key.");
        put("persons/raw/{key}", "Returns RAW in Base58 of person with the given key.");
        put("persons/images/{key}", "get item Images by key");
        put("persons/listfrom/{start}", "get list from KEY");
        put("POST persons/issue {\"linkTo\": \"<SeqNo>\", \"feePow\": int, \"creator\": \"<creator>\", \"name\": \"<name>\", \"description\": \"<description>\", \"icon\": \"<iconBase58>\", \"icon64\": \"<iconBase64>\", \"image\": \"<imageBase58>\", \"image64\": \"<imageBase64>\", \"birthday\": long, \"deathday\": long, \"gender\": int, \"race\": String, \"birthLatitude\": float, \"birthLongitude\": float, \"skinColor\": String, \"eyeColor\": String, \"hairСolor\": String, \"height\": int, \"owner\": Base58-PubKey, \"ownerSignature\": Base58, \"\": , \"password\": \"<password>\"}", "issue");
        put("POST persons/issueraw/{creator} {\"linkTo\":<SeqNo>, \"feePow\":<int>, \"password\":<String>, \"linkTo\":<SeqNo>, \"raw\":RAW-Base58", "Issue Person by Base58 RAW in POST body");

        put("persons/certify/{creator}/{personKey}/{pubkey}?feePow=<int>&linkTo=<SeqNo>&days=<int>&password=<String>", "Certify some public key for Person by it key. Default: pubKey is owner from Person, feePow=0, days=1");
    }};

    @SuppressWarnings("unchecked")
    @GET
    public String help() {
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


    // POST persons/issue {"creator": "7EPhDbpjsaRDFwB2nY8Cvn7XukF58kGdkz", "name": "Generate", "description": "Generate description", "birthday": 946688521000, "gender": 1, "birthLatitude": 0.0, "birthLongitude": 0.0, "height": 180, "password": "1"}
    @POST
    @Path("issue")
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
        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            JSONObject out = new JSONObject();
            Transaction.updateMapByErrorSimple(validate, out);
            return out.toJSONString();
        }
    }

    // POST persons/issueraw/7EPhDbpjsaRDFwB2nY8Cvn7XukF58kGdkz {"password":"1", "raw":"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"}
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
                creator, password, "issue Person");
        PersonCls item;
        try {
            item = PersonFactory.getInstance().parse(Transaction.FOR_NETWORK, resultRaw.b, false);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(
                    e.getMessage());
        }

        Pair<Transaction, Integer> transactionResult = cntr.issuePerson(resultRaw.a, linkTo, feePow, item);
        if (transactionResult.getB() != Transaction.VALIDATE_OK) {
            throw ApiErrorFactory.getInstance().createError(
                    transactionResult.getB());
        }

        Transaction transaction = transactionResult.getA();
        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            JSONObject out = new JSONObject();
            Transaction.updateMapByErrorSimple(validate, out);
            return out.toJSONString();
        }
    }

    // GET persons/issueraw/7EPhDbpjsaRDFwB2nY8Cvn7XukF58kGdkz/123/asdasdasdasd
    @GET
    // @Consumes(MediaType.WILDCARD)
    //@Produces("text/plain")
    @Path("certify/{creator}/{person}/{pubkey}")
    public String certifyPubkey(@PathParam("creator") String creatorStr,
                                @PathParam("person") Long personKey,
                                @PathParam("pubkey") String pubkeyStr,
                                @DefaultValue("1") @QueryParam("days") Integer addDays,
                                @QueryParam("linkTo") String linkToRefStr,
                                @DefaultValue("0") @QueryParam("feePow") int feePow,
                                @QueryParam("password") String password) {

        Controller cntr = Controller.getInstance();

        if (!DCSet.getInstance().getItemPersonMap().contains(personKey)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_PERSON_NOT_EXIST);
        }

        ExLink linkTo;
        if (linkToRefStr == null)
            linkTo = null;
        else {
            Long linkToRef = Transaction.parseDBRef(linkToRefStr);
            if (linkToRef == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR);
            } else {
                linkTo = new ExLinkAppendix(linkToRef);
            }
        }

        PublicKeyAccount pubKey;
        if (pubkeyStr == null) {
            // by Default - from Person
            PersonCls person = cntr.getPerson(personKey);
            pubKey = person.getMaker();
        } else {
            if (!PublicKeyAccount.isValidPublicKey(pubkeyStr)) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_PUBLIC_KEY);
            }
            pubKey = new PublicKeyAccount(pubkeyStr);
        }

        APIUtils.askAPICallAllowed(password, "GET certify\n ", request, true);
        PrivateKeyAccount creator = APIUtils.getPrivateKeyCreator(creatorStr);

        Transaction transaction = cntr.r_CertifyPubKeysPerson(0, creator, linkTo, feePow, personKey, pubKey, addDays);
        Integer result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);

        // CHECK VALIDATE MESSAGE
        if (result == Transaction.VALIDATE_OK) {
            return transaction.toJson().toJSONString();
        }

        return transaction.makeErrorJSON(result).toJSONString();

    }

}
