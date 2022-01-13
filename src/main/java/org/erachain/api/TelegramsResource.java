package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.crypto.Base32;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.datachain.DCSet;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.network.message.TelegramMessage;
import org.erachain.ntp.NTP;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Converter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

@Path("telegrams")
@Produces(MediaType.APPLICATION_JSON)
public class TelegramsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramsResource.class);

    @Context
    HttpServletRequest request;

    @GET
    public String getTelegrams(
            @QueryParam("filter") String filter,
            @QueryParam("outcomes") boolean outcomes,
            @QueryParam("decrypt") boolean decrypt,
            @QueryParam("limit") int limit,
            @QueryParam("password") String password) {
        return this.getTelegramsLimited(NTP.getTime() - 6000000, null, filter, decrypt, outcomes, limit, password);
    }

    @GET
    @Path("address/{address}")
    public String getTelegramsTwo(@PathParam("address") String address,
                                  @QueryParam("timestamp") long timestamp,
                                  @QueryParam("filter") String filter,
                                  @QueryParam("limit") int limit,
                                  @QueryParam("outcomes") boolean outcomes,
                                  @QueryParam("decrypt") boolean decrypt,
                                  @QueryParam("password") String password) {
        return this.getTelegramsTimestamp(address, timestamp, filter, decrypt, limit, outcomes, password);
    }

    private JSONObject decrypt(TelegramMessage telegram, JSONObject item) {

        Transaction transaction = telegram.getTransaction();
        if (transaction instanceof RSend) {

            RSend r_Send = (RSend) transaction;
            if (r_Send.isEncrypted()) {

                byte[] dataMess = Controller.getInstance().decrypt(r_Send.getCreator(), r_Send.getRecipient(), r_Send.getData());

                String message;

                if (dataMess != null) {
                    if (r_Send.isText()) {
                        try {
                            message = new String(dataMess, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            message = "error UTF-8";
                        }
                    } else {
                        message = Base58.encode(dataMess);
                    }
                } else {
                    message = "decode error";
                }

                JSONObject transactionJson = (JSONObject) item.get("transaction");
                transactionJson.put("message", message);

                item.put("transaction", transactionJson);

            }

        }

        return item;
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("timestamp/{timestamp}")
    public String getTelegramsLimited(@PathParam("timestamp") long timestamp,
                                      @QueryParam("recipient") String recipient,
                                      @QueryParam("filter") String filter,
                                      @QueryParam("outcomes") boolean outcomes,
                                      @QueryParam("decrypt") boolean decrypt,
                                      @QueryParam("limit") int limit,
                                      @QueryParam("password") String password) {

        // CREATE JSON OBJECT
        JSONArray array = new JSONArray();
        Controller controller = Controller.getInstance();
        JSONObject item;

        if (decrypt)
            APIUtils.askAPICallAllowed(password, "GET telegrams decrypt", request, true);

        for (TelegramMessage telegram : controller.getTelegramsFromTimestamp(timestamp, recipient, filter, outcomes, limit)) {

            item = telegram.toJson();
            if (decrypt) {
                decrypt(telegram, item);
            }

            array.add(item);
        }

        return array.toJSONString();
    }

    /**
     * @param address   recipient
     * @param timestamp the value more than which will be searched
     * @param filter    is title in telegram
     * @param outcomes    if set True - use outcomes too
     * @return Array all telegram by recipient in format JSON
     *
     * <h2>Example request</h2>
     * GET telegrams/address/" + recipient + "/timestamp/1
     * <h2>Example response</h2>
     * [{
     * "transaction":{
     * "type_name":"SEND",
     * "creator":"79WA9ypHx1iyDJn45VUXE5gebHTVrZi2iy",
     * "amount":"0.01",
     * "signature":"2SgAAsV83V7HxGN8rwhBgjVrfUZmvHc6qYNK5uodRQ5sz7mX8kinct9q3hphA43gSDXoXYcYbPS1amdNAbBKCmHg",
     * "fee":"0.00023168",
     * "publickey":"krksTcZunJmmnXQtUoNVQhwWAXFfQ4LbCJw3Qg8THo8",
     * "type":31,
     * "confirmations":0,
     * "version":0,
     * "record_type":"SEND",
     * "property2":128,
     * "action_key":1,
     * "    // _description - Описание заголовка, если нет то null":"NPL",
     * "message:"MESSAGE",
     * "property1":0,
     * "size":162,
     * "action_name":"PROPERTY",
     * "recipient":"7Dpv5Gi8HjCBgtDN1P1niuPJQCBQ5H8Zob",
     * "backward":false,
     * "assetKey":643,
     * "sub_type_name":"PROPERTY",
     * "timestamp":1529583735448
     * }}]
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("address/{address}/timestamp/{timestamp}")
    public String getTelegramsTimestamp(@PathParam("address") String address, @PathParam("timestamp") long timestamp,
                                        @QueryParam("filter") String filter,
                                        @QueryParam("decrypt") boolean decrypt,
                                        @QueryParam("limit") int limit,
                                        @QueryParam("outcomes") boolean outcomes,
                                        @QueryParam("password") String password) {

        Tuple2<Account, String> account = Account.tryMakeAccount(address);
        if (account.a == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        if (decrypt)
            APIUtils.askAPICallAllowed(password, "GET telegrams decrypt", request, true);

        JSONArray array = new JSONArray();
        JSONObject item;
        Transaction transaction;
        for (TelegramMessage telegram : Controller.getInstance().getTelegramsFromTimestamp(timestamp, account.a.getAddress(), filter, outcomes, limit)) {

            item = telegram.toJson();

            if (decrypt) {
                decrypt(telegram, item);
            }

            array.add(item);
        }

        return array.toJSONString();
    }

    @GET
    @Path("get/{signature}")
    // GET telegrams/get/6kdJgbiTxtqFt2zQDz9Lb29Z11Fa1TSwfZvjU21j6Cn9umSUEK4jXmNU19Ww4RcXpFyQiJTCaSz6Lc5YKn26hsR
    public String getTelegramBySignature(@PathParam("signature") String signature,
                                         @QueryParam("decrypt") boolean decrypt,
                                         @QueryParam("password") String password) {

        if (decrypt)
            APIUtils.askAPICallAllowed(password, "GET telegrams decrypt", request, true);

        // DECODE SIGNATURE
        byte[] signatureBytes;
        try {
            signatureBytes = Base58.decode(signature);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
        }

        // GET TELEGRAM
        TelegramMessage telegram = Controller.getInstance().getTelegram(signature);

        // CHECK IF TELEGRAM EXISTS
        if (telegram == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.TELEGRAM_DOES_NOT_EXIST);
        }

        JSONObject item = telegram.toJson();

        if (decrypt) {
            decrypt(telegram, item);
        }

        return item.toJSONString();
    }

    /**
     * Send telegram. not in block chain
     *
     * @param sender_in    address in wallet
     * @param recipient_in recipient
     * @param feePow       feePow
     * @param assetKey     assetKey
     * @param amount       amount
     * @param title        title
     * @param message      message
     * @param encoding     code if exist is text (not required field)
     * @param encrypt      bool value isEncrypt
     * @param password     password
     * @return return signature telegram
     *
     * <h2>Example request</h2>
     * GET telegrams/send/79WA9ypHx1iyDJn45VUXE5gebHTVrZi2iy/7C5HJALxTbAhzyhwVZeDCsGqVnSwcdEtqu/0/2/0.0001/title1/message1/true/false/1
     * <h2>Example response</h2>
     * {
     * "signature":"2vcTBHyCSUD7Qh968S1hr9mQpRgSswpCinCMeMX26XaUq58MDSCah3q9ntavhezqUGe7doR4hz4ZuPbc1QS2XzNg"
     * }
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("send/{sender}/{recipient}/{feePow}/{assetKey}/{amount}/{title}/{message}/{encoding}/{encrypt}/{password}")
    public String send(@PathParam("sender") String sender_in, @PathParam("recipient") String recipient_in,
                       @PathParam("feePow") int feePow, @PathParam("assetKey") long assetKey, @PathParam("amount") BigDecimal amount,
                       @PathParam("title") String title, @PathParam("message") String message,
                       @PathParam("encoding") int encoding,
                       @PathParam("encrypt") boolean encrypt,
                       @PathParam("password") String password) {

        APIUtils.askAPICallAllowed(password, "GET telegrams/send", request, true);

        JSONObject out = new JSONObject();
        Controller cntr = Controller.getInstance();

        // READ SENDER
        Account sender;
        try {
            sender = new Account(sender_in);
            if (sender.getAddress() == null)
                throw new Exception("");
        } catch (Exception e1) {
            Transaction.updateMapByErrorSimple(Transaction.INVALID_CREATOR, out);
            return out.toJSONString();
        }

        // READ RECIPIENT
        Account recipient;
        try {
            recipient = new Account(recipient_in);
            if (recipient.getAddress() == null)
                throw new Exception("");
        } catch (Exception e1) {
            Transaction.updateMapByErrorSimple(Transaction.INVALID_ADDRESS, out);
            return out.toJSONString();
        }

        byte[] messageBytes = null;

        if (message != null && message.length() > 0) {
            if (encoding == 0) {
                messageBytes = message.getBytes(StandardCharsets.UTF_8);
            } else {
                try {
                    if (encoding == 16) {
                        messageBytes = Converter.parseHexString(message);
                    } else if (encoding == 32) {
                        messageBytes = Base32.decode(message);
                    } else if (encoding == 58) {
                        messageBytes = Base58.decode(message);
                    } else if (encoding == 64) {
                        messageBytes = Base64.getDecoder().decode(message);
                    }
                } catch (Exception e) {
                    Transaction.updateMapByErrorSimple(Transaction.INVALID_MESSAGE_FORMAT, out);
                    return out.toJSONString();
                }
            }
        }

        // if no TEXT - set null
        if (messageBytes != null && messageBytes.length == 0)
            messageBytes = null;

        byte[] encrypted;
        byte[] isTextByte;

        if (messageBytes == null) {
            encrypted = new byte[]{0};
            isTextByte = new byte[]{0};
        } else {
            encrypted = encrypt ? new byte[]{1} : new byte[]{0};
            isTextByte = (encoding == 0) ? new byte[]{1} : new byte[]{0};
        }

        // title
        if (title != null && title.getBytes(StandardCharsets.UTF_8).length > Transaction.MAX_TITLE_BYTES_LENGTH) {
            Transaction.updateMapByErrorSimple(Transaction.INVALID_TITLE_LENGTH_MAX, out);
            return out.toJSONString();
        }

        // CREATE TX MESSAGE
        Transaction transaction;
        PrivateKeyAccount account = cntr.getWalletPrivateKeyAccountByAddress(sender.getAddress());
        if (account == null) {
            Transaction.updateMapByErrorSimple(ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS, out);
            return out.toJSONString();
        }

        if (encrypt && messageBytes != null) {
            //recipient
            byte[] publicKey = Controller.getInstance().getPublicKeyByAddress(recipient.getAddress());
            if (publicKey == null) {
                Transaction.updateMapByErrorSimple(Transaction.UNKNOWN_PUBLIC_KEY_FOR_ENCRYPT, out);
                return out.toJSONString();
            }

            //sender
            byte[] privateKey = account.getPrivateKey();
            messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
        }

        try {
            transaction = cntr.r_Send(
                    account, null, null, feePow, recipient, assetKey, amount,
                    title, messageBytes, isTextByte, encrypted, 0);
        } catch (Exception e) {
            Transaction.updateMapByErrorSimple(Transaction.INVALID_RETURN, e.getMessage(), out);
            return out.toJSONString();
        }

        int result = Controller.getInstance().broadcastTelegram(transaction, true);
        if (result == 0) {
            out.put("status", "ok");
        } else {
            Transaction.updateMapByErrorSimple(result, out);
        }
        out.put("signature", Base58.encode(transaction.getSignature()));
        return out.toJSONString();
    }

    // GET telegrams/send/7NH4wjxVy1y8kqBPtArA4UsevPMdgJS2Dk/7C5HJALxTbAhzyhwVZeDCsGqVnSwcdEtqu?
    // 2/0.0001/title/message/0/false?password=1
    @SuppressWarnings("unchecked")
    @GET
    @Path("send/{sender}/{recipient}")
    public String sendQuery(@PathParam("sender") String sender, @PathParam("recipient") String recipient,
                            @QueryParam("feePow") int feePow, @QueryParam("assetKey") long assetKey, @QueryParam("amount") BigDecimal amount,
                            @QueryParam("title") String title, @QueryParam("message") String message,
                            @QueryParam("encoding") int encoding, @QueryParam("encrypt") boolean encrypt,
                            @QueryParam("password") String password) {

        return send(sender, recipient, feePow, assetKey, amount, title, message, encoding, encrypt, password);

    }

    // "POST telegrams/send {\"sender\": \"<sender>\", \"recipient\": \"<recipient>\", \"feePow\":<feePow>, \"assetKey\": <assetKey>, \"amount\": <amount>, \"title\": \"<title>\", \"message\": \"<message>\", \"encoding\": 0, \"encrypt\": <true/false>, \"password\": \"<password>\"}",
    // POST telegrams/send {"sender": "78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5", "recipient": "7C5HJALxTbAhzyhwVZeDCsGqVnSwcdEtqu", "feePow":0, "asset": 2, "amount": 0.0001, "title": "title", "message": "<message>", "encoding": 0, "encrypt": false, "password": "122"}

    /**
     * Send telegram. not in block chain
     *
     * @param x JSON data row
     * @return signature telegram
     * <h2>Example request</h2>
     * if using Node console
     * use this:
     * POST telegrams/send {"sender":"79WA9ypHx1iyDJn45VUXE5gebHTVrZi2iy","recipient":"7Dpv5Gi8HjCBgtDN1P1niuPJQCBQ5H8Zob",
     * "feePow":0,"assetKey":643,"amount":0.01,"title":"NPL","encoding":0,"encrypt":true,"password":"123456789"}
     * if using command line
     * use this:
     * curl -d {\"sender\":\"7CzxxwH7u9aQtx5iNHskLQjyJvybyKg8rF\",\"recipient\":\"7Dpv5Gi8HjCBgtDN1P1niuPJQCBQ5H8Zob\",\"title\":\"title\",\"message\":\"message\",\"encrypt\":true,\"password\":\"123456789\"} -X POST http://127.0.0.1:9068/telegrams/send
     *
     * <h2>Example response</h2>
     * {
     * "signature":"FC3vHuUoPhYArc8L4DbgshH4mu54EaFZdGJ8Mh48FozDb5oSZazNVucyiyTYpFAHZNALUVYn5DCATMMNvtJTPhf"
     * }
     */
    @SuppressWarnings("unchecked")
    @POST
    @Path("send")
    public String sendPost(String x) {

        JSONObject jsonObject;
        try {
            // READ JSON
            jsonObject = (JSONObject) JSONValue.parse(x);
//        } catch (NullPointerException | ClassCastException e) {
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }

        if (jsonObject == null)
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);

        return send((String) jsonObject.getOrDefault("sender", null),
                (String) jsonObject.getOrDefault("recipient", null),
                Integer.valueOf(jsonObject.getOrDefault("feePow", 0).toString()),
                Long.valueOf(jsonObject.getOrDefault("assetKey", 0).toString()),
                new BigDecimal(jsonObject.getOrDefault("amount", 0).toString()),
                (String) jsonObject.getOrDefault("title", null),
                String.valueOf(jsonObject.getOrDefault("message", null)),
                Integer.valueOf(jsonObject.getOrDefault("encoding", 0).toString()),
                Boolean.valueOf(jsonObject.getOrDefault("encrypt", false).toString()),
                (String) jsonObject.getOrDefault("password", null));
    }

    // GET telegrams/datadecrypt/GerrwwEJ9Ja8gZnzLrx8zdU53b7jhQjeUfVKoUAp1StCDSFP9wuyyqYSkoUhXNa8ysoTdUuFHvwiCbwarKhhBg5?password=123456789
    @GET
    //@Produces("text/plain")
    @Path("datadecrypt/{signature}")
    public String dataDecrypt(@PathParam("signature") String signature, @QueryParam("password") String password) {

        byte[] signatureBytes;
        try {
            signatureBytes = Base58.decode(signature);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
        }

        // GET TELEGRAM
        TelegramMessage telegram = Controller.getInstance().getTelegram(signature);

        // CHECK IF TELEGRAM EXISTS
        if (telegram == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.TRANSACTION_DOES_NOT_EXIST);
        }

        RSend r_Send = (RSend) telegram.getTransaction();
        byte[] r_data = r_Send.getData();
        if (r_data == null || r_data.length == 0)
            return null;

        APIUtils.askAPICallAllowed(password, "POST decrypt telegram data\n " + signature, request, true);

        byte[] message = Controller.getInstance().decrypt(r_Send.getCreator(), r_Send.getRecipient(), r_data);
        if (message == null) {
            return "wrong decryption";
        }

        if (r_Send.isText()) {
            try {
                String str = new String(message, "UTF-8");
                return str;
            } catch (UnsupportedEncodingException e) {
                return "error UTF-8";
            }
        } else {
            String str = Base58.encode(message);
            return str;
        }
    }

    /**
     * Remove telegram by signature. signature not deleted if signature not contains in node.
     * Remove telegram if this node creator
     * <h2>Example request</h2>
     * POST telegrams/delete {"list": ["5HUqfaaY2uFgdmDM7XNky31rkdcUCPTzhHXeanBviSvyDfhgYnH4a64Aje3L53Jxmyb3CcouRiBeUF4HZNc7yySy"]}
     *
     * <h2>Example response</h2>
     * if all telegram delete return empty JSON string "[]"
     * if some telegram not delete return signature not remove telegram
     * ["5HUqfaaY2uFgdmDM7XNky31rkdcUCPTzhHXeanBviSvyDfhgYnH4a64Aje3L53Jxmyb3CcouRiBeUF4HZNc7yySy",...]
     *
     * @param value JSON string not delete telegram
     * @return
     */
    @POST
    @Path("delete")
    public String deleteTelegram(String value, @QueryParam("password") String password) {

        //APIUtils.askAPICallAllowed(password, "Delete telegrams request", request, true);

        JSONObject jsonObject;
        try {
            JSONParser jsonParser = new JSONParser();
            jsonObject = (JSONObject) jsonParser.parse(value);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }

        JSONArray arraySign = (JSONArray) (jsonObject.get("list"));
        JSONArray arrayNotDelete = new JSONArray();
        JSONObject out = new JSONObject();
        List<String> deleteList = new ArrayList<>();
        Controller controller = Controller.getInstance();

        String signature;
        for (Object obj : arraySign) {

            signature = obj.toString();
            TelegramMessage telegramMessage = controller.getTelegram(signature);
            if (telegramMessage == null)
                arrayNotDelete.add(signature);

            deleteList.add(signature);
        }

        try {
            controller.deleteTelegram(deleteList);
            out.put("signature", arrayNotDelete);
            return out.toJSONString();
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(e.getMessage());
        }
    }

    /**
     * Remove telegram to this timestamp then address and title
     * Remove telegram if this node creator
     * <h2>Example request</h2>
     * GET telegrams/deleteToTimestamp/12345678900?address=79WA9ypHx1iyDJn45VUXE5gebHTVrZi2iy&title=head
     *
     *
     * <h2>Example response</h2>
     * return counter of deletions
     *
     * @param timestamp  timestamp (long)
     * @param address    recipient address (String)
     * @param title      title (String)
     *
     *
     * @return count int
     */
    @GET
    @Path("deleteToTimestamp/{timestamp}")
    public String deleteTelegramToTimestamp(@PathParam("timestamp") long timestamp,
                               @QueryParam("address") String address,
                               @QueryParam("title") String title) {

        Controller controller = Controller.getInstance();



        return "" + controller.deleteTelegramsToTimestamp(timestamp, address, title);

    }

    /**
     * Remove telegram by address then timestamp and title
     * Remove telegram if this node creator
     * <h2>Example request</h2>
     * GET telegrams/delete/79WA9ypHx1iyDJn45VUXE5gebHTVrZi2iy?title=123&timestamp=123456789
     *
     *
     * <h2>Example response</h2>
     * return counter of deletions
     *
     * @param address    recipient address (String)
     * @param timestamp  timestamp (long)
     * @param title      title (String)
     *
     *
     * @return count int
     */
    @GET
    @Path("deleteForRecipient/{address}")
    public String deleteTelegramForRecipient(@PathParam("address") String address,
                                           @QueryParam("timestamp") long timestamp,
                                           @QueryParam("title") String title) {

        Controller controller = Controller.getInstance();

        return "" + controller.deleteTelegramsForRecipient(address, timestamp, title);

    }


    @GET
    @Path("info")
    public Response infoTelegrams() {
        JSONObject jsonObject = new JSONObject();
        Controller controller = Controller.getInstance();

        Integer count = controller.TelegramInfo();
        jsonObject.put("count", count);

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(jsonObject.toJSONString())
                .build();
    }

    private static long test1Delay = 0;
    private static Thread threadTest1;
    private static List<PrivateKeyAccount> test1Creators;

    @GET
    @Path("test1/{delay}")
    public String test1(@PathParam("delay") long delay, @QueryParam("password") String password) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))
                && !BlockChain.TEST_MODE)
            return "not LOCAL && not testnet";

        APIUtils.askAPICallAllowed(password, "GET telegrams/test1\n ", request, true);

        this.test1Delay = delay;

        if (threadTest1 != null) {
            JSONObject out = new JSONObject();
            if (delay <= 0) {
                threadTest1 = null;
                out.put("status", "STOP");
                LOGGER.info("telegrams/test1 STOP");
            } else {
                out.put("delay", delay);
                LOGGER.info("telegrams/test1 DELAY UPDATE:" + delay);
            }
            return out.toJSONString();
        }

        // CACHE private keys
        test1Creators = Controller.getInstance().getWalletPrivateKeyAccounts();


        JSONObject out = new JSONObject();

        if (test1Creators.size() <= 1) {
            out.put("error", "too small accounts");

            return out.toJSONString();
        }

        threadTest1 = new Thread(() -> {

            Random random = new Random();
            Controller cnt = Controller.getInstance();
            DCSet dcSet = DCSet.getInstance();
            //List<Peer> excludes = new ArrayList<Peer>();

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

                    // MAKE TELEGRAM
                    Transaction transaction = new RSend(creator, null, null, (byte) 0, recipient, 0, null,
                            "TEST 1", "TEST TEST TEST".getBytes(StandardCharsets.UTF_8), new byte[]{(byte) 1},
                            new byte[]{(byte) 1},
                            NTP.getTime(), 0L);
                    transaction.sign(creator, Transaction.FOR_NETWORK);

                    if (cnt.isOnStopping())
                        return;

                    // CREATE MESSAGE
                    Message telegram = MessageFactory.getInstance().createTelegramMessage(transaction);
                    cnt.network.telegramer.offerMessage(telegram);

                    try {
                        Thread.sleep(this.test1Delay);
                    } catch (InterruptedException e) {
                        return;
                    }

                    if (cnt.isOnStopping())
                        return;

                } catch (Exception e10) {
                    // not see in Thread - logger.error(e10.getMessage(), e10);
                }

            } while (true);
        });

        threadTest1.start();
        threadTest1.setName("Telegrams.Test1");

        out.put("delay", test1Delay);
        LOGGER.info("telegrams/test1 STARTED for delay: " + test1Delay);

        return out.toJSONString();

    }

}
