package org.erachain.api;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.ntp.NTP;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Path("record")
@Produces(MediaType.APPLICATION_JSON)
public class RecResource {

    private static final Logger LOGGER = LoggerFactory            .getLogger(RecResource.class);
    @Context
    HttpServletRequest request;
    @Context
    private UriInfo uriInfo;

    /*@GET here defines, this method will process HTTP GET requests. */

    ///////////////////////////
    public static String toBytes(int record_type, int version, int property1, int property2, int feePow, long timestamp, String creator, long reference,
                                 MultivaluedMap<String, String> queryParameters) // throws JSONException
    {

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(queryParameters);
        } catch (NullPointerException | ClassCastException e) {
            // JSON EXCEPTION
            //logger.error(e.getMessage());
            //return Response.status(500).entity(ApiErrorFactory.getInstance().createError(
            //		ApiErrorFactory.ERROR_JSON)).build();
            return APIUtils.errorMess(ApiErrorFactory.ERROR_JSON, ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON).toString());
        }

        PublicKeyAccount creatorPK;
        if (!PublicKeyAccount.isValidPublicKey(creator)) {
            return APIUtils.errorMess(Transaction.INVALID_CREATOR,
                    ApiErrorFactory.getInstance().createErrorJSON(Transaction.INVALID_CREATOR).toJSONString());
        }
        creatorPK = new PublicKeyAccount(Base58.decode(creator));

        int step = 0;

        if (reference == 0) {
            reference = Controller.getInstance().getTransactionCreator().getReference(creatorPK);
        }


        try {

            Transaction record = null;

            switch (record_type) {
                case Transaction.SIGN_NOTE_TRANSACTION:

                    //PARSE PAYMENT TRANSACTION
                    //return RSignNote.Parse(data, releaserReference);
                    break;

                case Transaction.CREATE_POLL_TRANSACTION:

                    //PARSE CREATE POLL TRANSACTION
                    //return CreatePollTransaction.Parse(data);
                    break;

                case Transaction.VOTE_ON_POLL_TRANSACTION:

                    //PARSE CREATE POLL VOTE
                    //return VoteOnPollTransaction.Parse(data, releaserReference);
                    break;

                case Transaction.ARBITRARY_TRANSACTION:

                    //PARSE ARBITRARY TRANSACTION
                    //return ArbitraryTransaction.Parse(data);
                    break;

                case Transaction.CREATE_ORDER_TRANSACTION:

                    //PARSE ORDER CREATION TRANSACTION
                    //return CreateOrderTransaction.Parse(data, releaserReference);
                    break;

                case Transaction.CANCEL_ORDER_TRANSACTION:

                    //PARSE ORDER CANCEL
                    //return CancelOrderTransaction.Parse(data, releaserReference);
                    break;

                case Transaction.CHANGE_ORDER_TRANSACTION:

                    //PARSE ORDER CREATION TRANSACTION
                    //return CreateOrderTransaction.Parse(data, releaserReference);
                    break;

                case Transaction.MULTI_PAYMENT_TRANSACTION:

                    //PARSE MULTI PAYMENT
                    //return MultiPaymentTransaction.Parse(data, releaserReference);
                    break;

                case Transaction.DEPLOY_AT_TRANSACTION:
                    //return DeployATTransaction.Parse(data);
                    break;

                case Transaction.SEND_ASSET_TRANSACTION:

                    ExLink exLink = null;
                    Account recipient = null;
                    long key = 0;
                    BigDecimal amount = null;
                    String head = null;
                    byte[] data = null;
                    byte[] isText = null;
                    byte[] encryptMessage = null;
                    try {
                        step++;
                        if (!jsonObject.containsKey("recipient"))
                            return ApiErrorFactory.getInstance().createErrorJSON(Transaction.INVALID_ADDRESS).toJSONString();
                        String recipientStr = ((List<String>) jsonObject.get("recipient")).get(0);
                        Tuple2<Account, String> recipientRes = Account.tryMakeAccount(recipientStr);
                        if (recipientRes.b != null) {
                            return APIUtils.errorMess(Transaction.INVALID_ADDRESS,
                                    ApiErrorFactory.getInstance().createErrorJSON(recipientRes.b).toJSONString());
                        }
                        recipient = recipientRes.a;

                        step++;
                        if (jsonObject.containsKey("key"))
                            key = Long.parseLong(((List<String>) jsonObject.get("key")).get(0));

                        step++;
                        if (jsonObject.containsKey("amount"))
                            amount = new BigDecimal(((List<String>) jsonObject.get("amount")).get(0));

                        step++;
                        if (jsonObject.containsKey("head"))
                            head = ((List<String>) jsonObject.get("head")).get(0);

                        step++;
                        if (jsonObject.containsKey("message"))
                            data = Base58.decode(((List<String>) jsonObject.get("message")).get(0));

                        step++;
                        if (jsonObject.containsKey("isText")
                                && Integer.parseInt(((List<String>) jsonObject.get("isText")).get(0)) == 0)
                            isText = new byte[]{0};
                        else
                            isText = new byte[]{1};

                        step++;
                        if (jsonObject.containsKey("encryptMessage")
                                && Integer.parseInt(((List<String>) jsonObject.get("encryptMessage")).get(0)) == 0)
                            encryptMessage = new byte[]{0};
                        else
                            encryptMessage = new byte[]{1};

                        step++;
                        if (jsonObject.containsKey("exlink"))
                            exLink = ExLink.parse((JSONObject) jsonObject.get("exlink"));

                    } catch (Exception e1) {
                        //logger.info(e1);
                        return APIUtils.errorMess(-step, e1.toString() + " on step: " + step);
                    }
                    record = new RSend((byte) version, (byte) property1, (byte) property2,
                            creatorPK,
                            exLink, (byte) feePow, recipient, key, amount, head,
                            data, isText, encryptMessage, timestamp, reference);

                    break;

                case Transaction.HASHES_RECORD:

                    // PARSE ACCOUNTING TRANSACTION V3
                    //return RHashes.Parse(data, releaserReference);
                    break;

                case Transaction.SIGN_TRANSACTION:

                    //PARSE CERTIFY PERSON TRANSACTION
                    //return RVouch.Parse(data, releaserReference);
                    break;

                case Transaction.SET_STATUS_TO_ITEM_TRANSACTION:

                    //PARSE CERTIFY PERSON TRANSACTION
                    //return RSetStatusToItem.Parse(data, releaserReference);
                    break;

                case Transaction.SET_UNION_TO_ITEM_TRANSACTION:

                    //PARSE CERTIFY PERSON TRANSACTION
                    //return RSetUnionToItem.Parse(data, releaserReference);
                    break;

                case Transaction.CERTIFY_PUB_KEYS_TRANSACTION:

                    //PARSE CERTIFY PERSON TRANSACTION
                    //return RCertifyPubKeys.Parse(data, releaserReference);
                    break;

                case Transaction.ISSUE_ASSET_TRANSACTION:

                    //PARSE ISSUE ASSET TRANSACTION
                    //return IssueAssetTransaction.Parse(data, releaserReference);
                    break;

                case Transaction.ISSUE_IMPRINT_TRANSACTION:

                    //PARSE ISSUE IMPRINT TRANSACTION
                    //return IssueImprintRecord.Parse(data, releaserReference);
                    break;

                case Transaction.ISSUE_TEMPLATE_TRANSACTION:

                    //PARSE ISSUE PLATE TRANSACTION
                    //return IssueTemplateRecord.Parse(data, releaserReference);
                    break;

                case Transaction.ISSUE_PERSON_TRANSACTION:

                    //PARSE ISSUE PERSON TRANSACTION
                    //return IssuePersonRecord.Parse(data, releaserReference);
                    break;

                case Transaction.ISSUE_POLL_TRANSACTION:

                    //PARSE ISSUE POLL TRANSACTION
                    //return IssuePollRecord.Parse(data, releaserReference);
                    break;

                case Transaction.ISSUE_STATUS_TRANSACTION:

                    //PARSE ISSUE PLATE TRANSACTION
                    //return IssueStatusRecord.Parse(data, releaserReference);
                    break;

                case Transaction.ISSUE_UNION_TRANSACTION:

                    //PARSE ISSUE PLATE TRANSACTION
                    //return IssueUnionRecord.Parse(data, releaserReference);
                    break;

                case Transaction.GENESIS_SEND_ASSET_TRANSACTION:

                    //PARSE TRANSFER ASSET TRANSACTION
                    //return GenesisTransferAssetTransaction.Parse(data);
                    break;

                case Transaction.GENESIS_ISSUE_PERSON_TRANSACTION:

                    //PARSE ISSUE PERSON TRANSACTION
                    //return GenesisIssuePersonRecord.Parse(data);
                    break;

                case Transaction.GENESIS_ISSUE_TEMPLATE_TRANSACTION:

                    //PARSE ISSUE PLATE TRANSACTION
                    //return GenesisIssueTemplateRecord.Parse(data);
                    break;

                case Transaction.GENESIS_ISSUE_STATUS_TRANSACTION:

                    //PARSE ISSUE STATUS TRANSACTION
                    //return GenesisIssueStatusRecord.Parse(data);
                    break;

                case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:

                    //PARSE GENESIS TRANSACTION
                    //return GenesisIssueAssetTransaction.Parse(data);
                    break;

                default:
                    return APIUtils.errorMess(Transaction.INVALID_TRANSACTION_TYPE, "Invalid transaction type: " + record_type);

            }

            // all test a not valid for main test
            // all other network must be invalid here!
            int port = BlockChain.NETWORK_PORT;
            return Base58.encode(Bytes.concat(record.toBytes(Transaction.FOR_NETWORK, false),
                    Ints.toByteArray(port)));

        } catch (Exception e) {
            //logger.error(e.getMessage());
            return APIUtils.errorMess(-step, e.toString() + " on step: " + step);
        }

    }

    @GET
    public String getRecord() {
        String text = "";
        JSONArray help = new JSONArray();
        JSONObject item;

        text = "SEND_ASSET_TRANSACTION: ";
        text += "type: " + Transaction.SEND_ASSET_TRANSACTION;
        text += ", recipient: Account in Base58";
        text += ", key: AssetKey - long";
        text += ", amount: BigDecimal";
        text += ", head: String UTF-8";
        text += ", data: String in Base58";
        text += ", isText: =0 - not as a TEXT";
        text += ", encryptMessage: =0 - not encrypt";
        item = new JSONObject();
        item.put(Transaction.SEND_ASSET_TRANSACTION, text);


        help.add(item);

        return help.toJSONString();
    }

    // short data without Signature
    @GET
    @Path("/parsetest/")
    // for test raw without Signature
    // http://127.0.0.1:9068/record/parsetest?data=3RKre8zCEarLNq4CQ6njRmvjGURz7KFWhec3H9H3tebEeKQEGDTsvAFizKnFpJAGDAoRQCKH9pygBQsrWfbxwgfcuEAKbARh5p6Yk2ZvfJDReFzBJbUSUwUgtxsKm2ZXHR
    //
    public String parseShort() // throws JSONException
    {

        if (uriInfo == null) {
            return APIUtils.errorMess(ApiErrorFactory.ERROR_JSON, ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON).toString());
        }

        // see http://ru.tmsoftstudio.com/file/page/web-services-java/javax_ws_rs_core.html
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        //CREATE TRANSACTION FROM RAW
        Transaction transaction;
        try {
            if (!queryParameters.containsKey("data"))
                return APIUtils.errorMess(-1, "Parameter [data] not found");

            String dataStr = queryParameters.get("data").get(0);
            byte[] data = Base58.decode(dataStr);
            int cut = 53;
            byte[] dataTail = Arrays.copyOfRange(data, cut, data.length);
            data = Bytes.concat(Arrays.copyOfRange(data, 0, cut), new byte[64], dataTail);
            transaction = TransactionFactory.getInstance().parse(data, Transaction.FOR_NETWORK);
            JSONObject json = transaction.toJson();
            json.put("raw", Base58.encode(data));
            return json.toJSONString();
        } catch (Exception e) {
            return APIUtils.errorMess(-1, e.toString());
        }

    }

    @GET
    @Path("/parse/")
    // http://127.0.0.1:9068/record/parse?data=DPDnFCNvPk4kLMQcyEp8wTmzT53vcFpVPVhBA8VuHDH6ekAWJAEgZvtjtKGcXwsAKyNs5k2aCpziAmqEDjTigbnDjMeXRfbUDUJNmEJHwB2uPdboSszwsy3fckANUgPV8Ep9CN1fdTdq3QfYE7bbpeYWS2rsTNHb3a7nEV6jg2XJguavqhNSzVeyM6UrRtbiVciMvHFayUAMrE4L3CPjZjPEf
    //
    public String parse() // throws JSONException
    {

        if (uriInfo == null) {
            return APIUtils.errorMess(ApiErrorFactory.ERROR_JSON, ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON).toString());
        }

        // see http://ru.tmsoftstudio.com/file/page/web-services-java/javax_ws_rs_core.html
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        //CREATE TRANSACTION FROM RAW
        Transaction transaction;
        try {
            if (!queryParameters.containsKey("data"))
                return APIUtils.errorMess(-1, "Parameter [data] not found");

            String dataStr = queryParameters.get("data").get(0);
            transaction = TransactionFactory.getInstance().parse(Base58.decode(dataStr), Transaction.FOR_NETWORK);
        } catch (Exception e) {
            return APIUtils.errorMess(-1, e.toString());
        }

        return transaction.toJson().toJSONString();
    }

    @GET
    @Path("/getraw/{type}/{creator}")
    //@Consumes(MediaType.APPLICATION_JSON)
    //@Produces("application/json")
    //
    // get record/getraw/31/5mgpEGqUGpfme4W2tHJmG7Ew21Te2zNY7Ju3e9JfUmRF?feePow=2&timestamp=123123243&version=3&recipient=7JS4ywtcqrcVpRyBxfqyToS2XBDeVrdqZL&amount=123.0000123&key=12
    // http://127.0.0.1:9068/record/getraw/31/5mgpEGqUGpfme4W2tHJmG7Ew21Te2zNY7Ju3e9JfUmRF?feePow=2&recipient=77QnJnSbS9EeGBa2LPZFZKVwjPwzeAxjmy&amount=123.0000123&key=1
    //
    public String getRaw(@PathParam("type") int record_type,
                         @PathParam("creator") String creator) // throws JSONException
    {

        if (uriInfo == null) {
            return APIUtils.errorMess(ApiErrorFactory.ERROR_JSON, ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON).toString());
        }

        // see http://ru.tmsoftstudio.com/file/page/web-services-java/javax_ws_rs_core.html
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(queryParameters);
        } catch (NullPointerException | ClassCastException e) {
            // JSON EXCEPTION
            //logger.error(e.getMessage());
            return APIUtils.errorMess(ApiErrorFactory.ERROR_JSON, ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON).toString());
        }

        int feePow;
        int version;
        long timestamp;
        long reference;

        int step = 0;
        try {
            feePow = jsonObject.containsKey("feePow") ? Integer.parseInt(((List<String>) jsonObject.get("feePow")).get(0)) : 0;

            step++;
            version = jsonObject.containsKey("version") ? Integer.parseInt(((List<String>) jsonObject.get("version")).get(0)) : 0;

            step++;
            timestamp = jsonObject.containsKey("timestamp") ? Long.parseLong(((List<String>) jsonObject.get("timestamp")).get(0)) : NTP.getTime();

            step++;
            reference = jsonObject.containsKey("reference") ? Long.parseLong(((List<String>) jsonObject.get("reference")).get(0)) : 0l;
        } catch (Exception e1) {
            //logger.info(e1);
            return APIUtils.errorMess(-step, e1.toString() + " on step: " + step);
        }


        return toBytes(record_type, version, 0, 0, feePow, timestamp, creator, reference, queryParameters);

    }

    @GET
    @Path("/getraw/{type}/{version}/{creator}/{timestamp}/{feePow}")
    public String getRaw(@PathParam("type") int record_type,
                         @PathParam("version") int version,
                         @PathParam("creator") String creator,
                         @PathParam("timestamp") long timestamp,
                         @PathParam("feePow") int feePow,
                         @PathParam("reference") long reference
    ) // throws JSONException
    {

        if (uriInfo == null) {
            return APIUtils.errorMess(ApiErrorFactory.ERROR_JSON, ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON).toString());
        }

        // see http://ru.tmsoftstudio.com/file/page/web-services-java/javax_ws_rs_core.html
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        return toBytes(record_type, version, 0, 0, feePow, timestamp, creator, reference, queryParameters);

    }

    @GET
    @Path("/getraw/{type}/{version}/{property1}/{property2}/{creator}/{timestamp}/{feePow}")
    public String getRaw(@PathParam("type") int record_type,
                         @PathParam("version") int version,
                         @PathParam("property1") int property1,
                         @PathParam("property2") int property2,
                         @PathParam("creator") String creator,
                         @PathParam("timestamp") long timestamp,
                         @PathParam("feePow") int feePow,
                         @PathParam("reference") long reference
    ) // throws JSONException
    {

        if (uriInfo == null) {
            return APIUtils.errorMess(ApiErrorFactory.ERROR_JSON, ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_JSON).toString());
        }

        // see http://ru.tmsoftstudio.com/file/page/web-services-java/javax_ws_rs_core.html
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        return toBytes(record_type, version, property1, property2, feePow, timestamp, creator, reference, queryParameters);

    }

    @POST
    @Path("/broadcast")
    public String broadcastFromRaw(String rawDataBase58) {
        int step = 1;

        try {
            byte[] transactionBytes = Base58.decode(rawDataBase58);

            step++;
            Pair<Transaction, Integer> result = Controller.getInstance().lightCreateTransactionFromRaw(transactionBytes);
            if (result.getB() == Transaction.VALIDATE_OK) {
                return "+";
            } else {
                return APIUtils.errorMess(result.getB(), OnDealClick.resultMess(result.getB()), result.getA());
            }

        } catch (Exception e) {
            //logger.error(e.getMessage());
            return APIUtils.errorMess(-1, e.toString() + " on step: " + step);
        }
    }

}
