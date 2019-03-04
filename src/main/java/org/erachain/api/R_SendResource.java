package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

@Path("r_send")
@Produces(MediaType.APPLICATION_JSON)
public class R_SendResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(R_SendResource.class);
    @Context
    HttpServletRequest request;

    @GET
    public String help() {
        Map<String, String> help = new LinkedHashMap<String, String>();
        help.put("GET r_send/{creator}/{recipient}?feePow={feePow}&assetKey={assetKey}&amount={amount}&title={title}&message={message}&encoding={encoding}&encrypt=true&password={password}",
                "make and broadcast SEND asset amount and mail");
        help.put("GET r_send/raw/{creator}/{recipient}?feePow={feePow}&assetKey={assetKey}&amount={amount}&title={title}&message={message}&encoding={encoding}&encrypt=true&password={password}",
                "make RAW for SEND asset amount and mail");
        help.put("POST r_send {\"creator\": \"<creator>\", \"recipient\": \"<recipient>\", \"asset\":\"<assetKey>\", \"amount\":\"<amount>\", \"title\": \"<title>\", \"message\": \"<message>\", \"encoding\": <encoding>, \"encrypt\": <true/false>,  \"password\": \"<password>\"}",
                "make and broadcast SEND asset amount and mail");
        help.put("POST r_send/raw {\"creator\": \"<creator>\", \"recipient\": \"<recipient>\", \"asset\":\"<assetKey>\", \"amount\":\"<amount>\", \"title\": \"<title>\", \"message\": \"<message>\", \"encoding\": <encoding>, \"encrypt\": <true/false>,  \"password\": \"<password>\"}",
                "make RAW for SEND asset amount and mail");
        help.put("GET r_send/test1/{delay}?password={password}",
                "Stert test; dekay = 0 - stop");

        return StrJSonFine.convert(help);
    }

    /**
     * send and broadcast GET
     *
     * @param creatorStr   address in wallet
     * @param recipientStr recipient
     * @param feePowStr    fee
     * @param assetKey     assetKey
     * @param amount       amount
     * @param title        title or head
     * @param message      message
     * @param encoding  code if exist is text (not required field)
     * @param encrypt      bool value encrypt (not required field)
     * @param password     password
     * @return JSON row
     *
     * <h2>Example request</h2>
     * GET r_send/7GvWSpPr4Jbv683KFB5WtrCCJJa6M36QEP/79MXsjo9DEaxzu6kSvJUauLhmQrB4WogsH?message={\"msg\":\"123\"}&amp;encrypt=false&amp;password=123456789
     * <h2>Example response</h2>
     * {"type_name":"LETTER", "creator":"7GvWSpPr4Jbv683KFB5WtrCCJJa6M36QEP",
     * "message":"{"msg":"123"}","signature":"Vf8qtG3tiYV6LwvCrrTPf6zq3ikUVNZWfgwkrLU1tckvEQ2Dx8qB1qLEGkX8Wqj4WVKDYZRYfJyGb3dZCTR3asz",
     * "fee":"0.00010624", "publickey":"5sD1mTM2tB8aiQdUzKtXiNtesmJQCvHjXMrcCZWQca37", "type":31,
     * "confirmations":0, "version":0, "record_type":"LETTER", "property2":0, "property1":128, "size":166,
     * "encrypted":false, "recipient":"79MXsjo9DEaxzu6kSvJUauLhmQrB4WogsH", "sub_type_name":"", "isText":true, "timestamp":1529585877655}
     */
    @GET
    // @Consumes(MediaType.WILDCARD)
    @Path("{creator}/{recipient}")
    public String sendGet(@PathParam("creator") String creatorStr, @PathParam("recipient") String recipientStr,
                          @QueryParam("feePow") int feePowStr, @QueryParam("assetKey") long assetKey,
                          @QueryParam("amount") BigDecimal amount, @QueryParam("title") String title,
                          @QueryParam("message") String message,
                          @QueryParam("encoding") int encoding,
                          @QueryParam("encrypt") boolean encrypt, @QueryParam("password") String password) {

        APIUtils.askAPICallAllowed(password, "GET send\n ", request, true);

        JSONObject out = new JSONObject();
        Controller cntr = Controller.getInstance();

        boolean needAmount = false;
        Pair<Integer, Transaction> result = cntr.make_R_Send(creatorStr, null, recipientStr, feePowStr,
                assetKey, true,
                amount, needAmount,
                title, message, encoding, encrypt);

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

    /**
     * send and broadcast POST r_send
     * @param x JSON row with all parameter
     * @return
     * <h2>Example request</h2>
     * POST r_send {"creator":"79WA9ypHx1iyDJn45VUXE5gebHTVrZi2iy","recipient":"79MXsjo9DEaxzu6kSvJUauLhmQrB4WogsH",
     * "feePow":"1","assetKey":"643","amount":"1","title":"123","message":"{"msg":"1223"}",
     * "encoding":"0","encrypt":"false","password":"123456789"}
     *
     * <h2>Example response</h2>
     * {
     *   "type_name":"SEND", "creator":"79WA9ypHx1iyDJn45VUXE5gebHTVrZi2iy", "amount":"1", "message":"{",
     *   "signature":"5QASfQZ8kp8VWdBh9tNJkvdZqNynPki2fMSyaRh3oWPiV8bGw49v66cAjYp1dCC5LKWUirkE9kqWm7kUanNChxsi",
     *   "fee":"0.00035424", "publickey":"krksTcZunJmmnXQtUoNVQhwWAXFfQ4LbCJw3Qg8THo8", "type":31,
     *   "confirmations":0, "version":0, "record_type":"SEND", "property2":0, "action_key":1, "head":"123",
     *   "property1":0, "size":169, "encrypted":false, "action_name":"PROPERTY", "recipient":"79MXsjo9DEaxzu6kSvJUauLhmQrB4WogsH",
     *   "backward":false, "asset":643, "sub_type_name":"PROPERTY", "isText":true, "timestamp":1529586600467 }
     */
    @POST
    @Consumes(MediaType.WILDCARD)
    //@Consumes(MediaType.TEXT_PLAIN)
    //@Produces(MediaType.TEXT_PLAIN)
    //@Path("send")
    @SuppressWarnings("unchecked")
    //@SuppressWarnings("unchecked")
    public String sendPost(String x) {

        JSONObject jsonObject;
        try {
            //READ JSON
            jsonObject = (JSONObject) JSONValue.parse(x);
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            ///LOGGER.error(e.getMessage());
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }

        if (jsonObject == null)
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);

        String creator = (String) jsonObject.getOrDefault("creator", null);
        String recipient = (String) jsonObject.getOrDefault("recipient", null);
        int feePow = Integer.valueOf(jsonObject.getOrDefault("feePow", 0).toString());
        long assetKey = Long.valueOf(jsonObject.getOrDefault("assetKey", 0l).toString());
        BigDecimal amount = new BigDecimal(jsonObject.getOrDefault("amount", 0).toString());
        String title = (String) jsonObject.getOrDefault("title", null);
        String message = (String) jsonObject.getOrDefault("message",null);
        int encoding = Integer.valueOf(jsonObject.getOrDefault("encoding", 0).toString());
        boolean encrypt = Boolean.valueOf((boolean) jsonObject.getOrDefault("encrypt", false));
        String password = (String) jsonObject.getOrDefault("password", null);

        return sendGet(
                creator,
                recipient,
                feePow,
                assetKey, amount,
                title, message,
                encoding, encrypt,
                password
        );

    }

    /*
     * make and return RAW
     * GET r_send/raw/{creator}/{recipient}?feePow={feePow}&assetKey={assetKey}&amount={amount}&title={title}&message={message}&encoding={encoding}&encrypt=true&rawbase={58/64}&password={password}
     *
     * GET r_send/raw/79QuhunbPbc3svqsXMC2JHbh8ix6kwNAao/74eD7JSrkXPsz3xxKMhMEDoTF6TqkfEHBt?feePow=3&assetKey=1001&amount=111&title=probe&message=message&encoding=0&encrypt=true&password=1
     * GET r_send/raw/79QuhunbPbc3svqsXMC2JHbh8ix6kwNAao/74eD7JSrkXPsz3xxKMhMEDoTF6TqkfEHBt?title=probe&message=message&rawbase=64&password=1
     */
    @GET
    // @Consumes(MediaType.WILDCARD)
    //@Produces("text/plain")
    @Path("raw/{creator}/{recipient}")
    public String rawSendGet(@PathParam("creator") String creatorStr, @PathParam("recipient") String recipientStr,
                             @QueryParam("feePow") int feePowStr,
                             @QueryParam("assetKey") long assetKey, @QueryParam("amount") BigDecimal amountStr,
                             @QueryParam("title") String title,
                             @QueryParam("message") String message,
                             @QueryParam("encoding") int encoding, @QueryParam("encrypt") boolean encrypt,
                             @QueryParam("rawbase") int rawbase,
                             @QueryParam("password") String password) {

        APIUtils.askAPICallAllowed(password, "GET rawSend\n ", request, true);

        JSONObject out = new JSONObject();
        Controller cntr = Controller.getInstance();

        boolean needAmount = false;
        Pair<Integer, Transaction> result = cntr.make_R_Send(creatorStr, null, recipientStr, feePowStr,
                assetKey, true,
                amountStr, needAmount,
                title, message, encoding, encrypt);

        Transaction transaction = result.getB();
        if (transaction == null) {
            out.put("error", result.getA());
            out.put("error_message", OnDealClick.resultMess(result.getA()));
            return out.toJSONString();
        }

        String str;
        if (rawbase == 64)
            out.put("raw64", Base64.getEncoder().encodeToString(transaction.toBytes(Transaction.FOR_NETWORK, false)));
        else
            out.put("raw", Base58.encode(transaction.toBytes(Transaction.FOR_NETWORK, false)));

        return out.toJSONString();

    }

    /*
     * make and return RAW
     * POST r_send/raw64 {"creator": "<creator>", "recipient": "<recipient>", "asset":"<assetKey>", "amount":"<amount>", "title": "<title>", "message": "<message>", "encoding": <encoding>, "encrypt": <true/false>,  "password": "<password>"}"
     *
     */
    @POST
    @Consumes(MediaType.WILDCARD)
    //@Consumes(MediaType.APPLICATION_JSON)
    //@Produces(MediaType.APPLICATION_JSON)
    @Path("raw")
    @SuppressWarnings("unchecked")
    public String rawSendPost(String x) {

        JSONObject jsonObject;
        try {
            //READ JSON
            jsonObject = (JSONObject) JSONValue.parseWithException(x);
        } catch (ParseException | NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }

        if (jsonObject == null)
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);

        String creator = (String) jsonObject.getOrDefault("creator", null);
        String recipient = (String) jsonObject.getOrDefault("recipient", null);
        int feePow = Integer.valueOf(jsonObject.getOrDefault("feePow", 0).toString());
        long assetKey = Long.valueOf(jsonObject.getOrDefault("assetKey", 0l).toString());
        BigDecimal amount = new BigDecimal(jsonObject.getOrDefault("amount", 0).toString());
        String title = (String) jsonObject.getOrDefault("title", null);
        String message = (String) jsonObject.getOrDefault("message",null);
        int encoding = Integer.valueOf(jsonObject.getOrDefault("encoding", 0).toString());
        boolean encrypt = Boolean.valueOf((boolean) jsonObject.getOrDefault("encrypt", false));
        int rawbase = Integer.valueOf(jsonObject.getOrDefault("rawbase", 58).toString());
        String password = (String) jsonObject.getOrDefault("password", null);

        return rawSendGet(
                creator,
                recipient,
                feePow,
                assetKey, amount,
                title, message, encoding,
                encrypt,
                rawbase,
                password
        );

    }

    private static long test1Delay = 0;
    private static Thread threadTest1;
    private static List<PrivateKeyAccount> test1Creators;

    @GET
    @Path("test1/{delay}")
    public String test1(@PathParam("delay") long delay, @QueryParam("password") String password) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))
                && !BlockChain.DEVELOP_USE)
            return "not LOCAL && not DEVELOP";

        APIUtils.askAPICallAllowed(password, "GET test1\n ", request, true);

        this.test1Delay = delay;

        if (threadTest1 != null) {
            JSONObject out = new JSONObject();
            if (delay <= 0) {
                threadTest1 = null;
                out.put("status", "STOP");
                LOGGER.info("r_send/test1 STOP");
            } else {
                out.put("delay", delay);
                LOGGER.info("r_send/test1 DELAY UPDATE:" + delay);
            }
            return out.toJSONString();
        }

        // CACHE private keys
        test1Creators = Controller.getInstance().getPrivateKeyAccounts();


        JSONObject out = new JSONObject();

        if (test1Creators.size() <= 1) {
            out.put("error", "too small accounts");

            return out.toJSONString();
        }

        threadTest1 = new Thread(() -> {

            Random random = new Random();
            Controller cnt = Controller.getInstance();

            int counter = 0;

            do {

                try {

                    if (this.test1Delay <= 0) {
                        return;
                    }

                    if (cnt.isOnStopping())
                        return;

                    PrivateKeyAccount creator = test1Creators.get(random.nextInt(test1Creators.size()));
                    Account recipient;
                    do {
                        recipient = test1Creators.get(random.nextInt(test1Creators.size()));
                    } while (recipient.equals(creator));


                    Transaction transaction = cnt.r_Send(creator,
                            0, recipient,
                            2l, null, "Safe-Pay " + ++counter,
                            "TEST TEST TEST".getBytes(Charset.forName("UTF-8")), new byte[]{(byte) 1},
                            new byte[]{(byte) 1});

                    if (cnt.isOnStopping())
                        return;

                    Integer result = cnt.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);
                    // CLEAR for HEAP
                    transaction.setDC(null);


                    // CHECK VALIDATE MESSAGE
                    if (result != Transaction.VALIDATE_OK) {
                        if (result == Transaction.RECEIVER_NOT_PERSONALIZED
                                || result == Transaction.CREATOR_NOT_PERSONALIZED
                                || result == Transaction.NO_BALANCE
                                || result == Transaction.NOT_ENOUGH_FEE
                                || result == Transaction.UNKNOWN_PUBLIC_KEY_FOR_ENCRYPT)
                            continue;

                        // not work in Threads - LOGGER.info("TEST1: " + OnDealClick.resultMess(result));
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                        }
                        continue;
                    }

                    try {
                        Thread.sleep(this.test1Delay);
                    } catch (InterruptedException e) {
                    }

                    if (cnt.isOnStopping())
                        return;

                } catch (Exception e10) {
                    // not see in Thread - LOGGER.error(e10.getMessage(), e10);
                }

            } while (true);
        });

        threadTest1.start();
        threadTest1.setName("R_Send.Test1");

        out.put("delay", test1Delay);
        LOGGER.info("r_send/test1 STARTED for delay: " + test1Delay);

        return out.toJSONString();

    }

}
