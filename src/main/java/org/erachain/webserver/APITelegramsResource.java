package org.erachain.webserver;

import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
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
        help.put("apitelegrams/get?address={address}&timestamp={timestamp}&filter={filter}",
                "Get messages by filter. Filter is title.");
        help.put("apitelegrams/timestamp/{timestamp}?filter={filter}",
                "Get messages from timestamp with filter. Filter is title.");
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
                .entity(StrJSonFine.convert(telegram.toJson().toJSONString())).build();
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
    @GET
    @Path("get")
    public Response getTelegramsTimestamp(@QueryParam("address") String address, @QueryParam("timestamp") int timestamp, @QueryParam("filter") String filter) {

        // CHECK ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        int limit = 1024 << (BlockChain.HARD_WORK >> 1);
        JSONArray array = new JSONArray();
        for (TelegramMessage telegram : Controller.getInstance().getLastTelegrams(address, timestamp, filter)) {
            if (--limit < 0)
                break;

            array.add(telegram.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(array.toJSONString())).build();
    }

    @GET
    @Path("timestamp/{timestamp}")
    public Response getTelegramsLimited(@PathParam("timestamp") long timestamp,
                                      @QueryParam("filter") String filter) {

        int limit = 1024 << (BlockChain.HARD_WORK>>1);
        JSONArray array = new JSONArray();
        for (TelegramMessage telegram : Controller.getInstance().getLastTelegrams(timestamp, null, filter)) {
            if (--limit < 0)
                break;

            array.add(telegram.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(array.toJSONString())).build();
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
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(jsonObject.toJSONString())).build();

    }
}