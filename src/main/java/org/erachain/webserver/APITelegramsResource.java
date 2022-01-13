package org.erachain.webserver;

import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.lang.Lang;
import org.erachain.network.message.TelegramMessage;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("apitelegrams")
@Produces(MediaType.APPLICATION_JSON)
public class APITelegramsResource {

    static Logger LOGGER = LoggerFactory.getLogger(APITelegramsResource.class.getName());

    @Context
    HttpServletRequest request;

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<String, String>();
        help.put("apitelegrams/getbysignature/{signature}", "Get Telegramm by signature");
        help.put("apitelegrams/incoming/{recipient}/{timestamp}?limit=100",
                Lang.T("Find telegrams for recipent from timestamp"));
        help.put("apitelegrams/timestamp/{timestamp}?filter={filter}&outcomes=true&limit=100",
                "Get messages from timestamp with filter. Filter is title. If outcomes=true get outcomes too");
        help.put("apitelegrams/check/{signature}",
                "Check telegrams contain in node");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }

    /**
     * @param signature is signature message
     * @return telegram
     * @author Ruslan
     */
    @GET
    @Path("getbysignature/{signature}")
    // GET
    // telegrams/getbysignature/6kdJgbiTxtqFt2zQDz9Lb29Z11Fa1TSwfZvjU21j6Cn9umSUEK4jXmNU19Ww4RcXpFyQiJTCaSz6Lc5YKn26hsR
    public Response getTelegramBySignature(@PathParam("signature") String signature) throws Exception {

        // DECODE SIGNATURE
        @SuppressWarnings("unused")
        byte[] signatureBytes;
        try {
            signatureBytes = Base58.decode(signature);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
        }

        // GET TELEGRAM\
        TelegramMessage telegram = Controller.getInstance().getTelegram(signature);

        // CHECK IF TELEGRAM EXISTS
        if (telegram == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.TELEGRAM_DOES_NOT_EXIST);
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(telegram.toJson().toJSONString()).build();
    }

    /**
     * Get telegrams by timestamp
     *
     * @param address   account user
     * @param timestamp value time
     * @param filter    is title message.
     * @return json string all find message by filter
     * @author Ruslan
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    @GET
    @Path("get")
    public Response get(@QueryParam("address") String address, @QueryParam("timestamp") long timestamp,
                        @QueryParam("filter") String filter,
                        @QueryParam("limit") int limit) {

        // CHECK ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        if (ServletUtils.isRemoteRequest(request)) {
            if (limit > 100 || limit <= 0)
                limit = 100;
        }

        JSONArray array = new JSONArray();
        for (TelegramMessage telegram : Controller.getInstance().getTelegramsForRecipient(new Account(address), timestamp, filter, limit)) {
            array.add(telegram.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString()).build();
    }

    @GET
    @Path("incoming/{recipient}/{timestamp}")
    public Response getIncoming(@QueryParam("recipient") String recipient, @QueryParam("timestamp") long timestamp,
                                @QueryParam("limit") int limit) {

        // CHECK ADDRESS
        if (!Crypto.getInstance().isValidAddress(recipient)) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        if (ServletUtils.isRemoteRequest(request)) {
            if (limit > 100 || limit <= 0)
                limit = 100;
        }

        JSONArray array = new JSONArray();
        for (TelegramMessage telegram : Controller.getInstance().getTelegramsForRecipient(new Account(recipient), timestamp, null, limit)) {
            array.add(telegram.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString()).build();
    }


    @GET
    @Path("timestamp/{timestamp}")
    public Response getTelegramsLimited(@PathParam("timestamp") long timestamp,
                                        @QueryParam("address") String address,
                                        @QueryParam("filter") String filter, @QueryParam("outcomes") boolean outcomes,
                                        @QueryParam("limit") int limit) {

        if (ServletUtils.isRemoteRequest(request)) {
            if (limit > 100 || limit <= 0)
                limit = 100;
        }

        JSONArray array = new JSONArray();
        for (TelegramMessage telegram : Controller.getInstance().getTelegramsFromTimestamp(timestamp, address, filter, outcomes, limit)) {
            array.add(telegram.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString()).build();
    }

    /**
     * Check telegrams
     *
     * @param signature is signature transaction Base58 encode
     * @return JSON string contain telegram in node
     *
     * <h2>Example request</h2>
     * http://127.0.0.1:9067/apitelegrams/check/453ryw6jZKmtxRW3TXCyU1RdCdWiiPPiAYha2ZdzbtwXcn9HrukkP2feaGkC76Ww5etbaa9uhG2FcUU42RS62gNx
     *
     * <h2>Example response</h2>
     * <p>
     * if exist
     * <p>
     * {"check":true}
     * <p><p>
     * if not exist
     * <p>
     * {"check":false}
     */
    @GET
    @Path("check/{signature}")
    @SuppressWarnings("unchecked")
    public Response checkSignature(@PathParam("signature") String signature) {

        TelegramMessage telegram = Controller.getInstance().getTelegram(signature);
        JSONObject jsonObject = new JSONObject();

        if (telegram == null)
            jsonObject.put("check", false);
        else
            jsonObject.put("check", true);
        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(jsonObject.toJSONString()).build();

    }
}