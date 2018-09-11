package webserver;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;

import api.ApiErrorFactory;
import controller.Controller;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.transaction.Transaction;
import network.message.TelegramMessage;
import utils.StrJSonFine;

@Path("apitelegrams")
@Produces(MediaType.APPLICATION_JSON)
public class API_TelegramsResource {

    static Logger LOGGER = Logger.getLogger(API_TelegramsResource.class.getName());
    private static int count_step;
    private static boolean run_test = false;
    private static Thread thread;
    private static int sleep;
    private static BigDecimal amm;
    private static byte[] mes;
    protected String tt;
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

        JSONArray array = new JSONArray();
        for (TelegramMessage telegram : Controller.getInstance().getLastTelegrams(address, timestamp, filter)) {
            array.add(telegram.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(array.toJSONString())).build();
    }

    @GET
    @Path("timestamp/{timestamp}")
    public Response getTelegramsLimited(@PathParam("timestamp") long timestamp,
                                      @QueryParam("filter") String filter) {

        // CREATE JSON OBJECT
        JSONArray array = new JSONArray();

        for (TelegramMessage telegram : Controller.getInstance().getLastTelegrams(timestamp, null, filter)) {
            array.add(telegram.toJson());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(array.toJSONString())).build();
    }

}