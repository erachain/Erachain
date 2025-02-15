package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.datachain.TransactionFinalMapImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.ntp.NTP;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("r_send")
@Produces(MediaType.APPLICATION_JSON)
public class RSendResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSendResource.class);
    @Context
    HttpServletRequest request;

    @GET
    public String help() {
        Map<String, String> help = new LinkedHashMap<String, String>();
        help.put("GET r_send/{creator}/{recipient}?feePow={feePow}&assetKey={assetKey}&amount={amount}&title={title}&message={message}&encoding={encoding}&encrypt=true&password={password}",
                "make and broadcast SEND asset amount and mail");
        help.put("POST r_send/{creator}/{recipient} {feePowfeePow}&assetKey={assetKey}&amount={amount}&title={title}&encoding={encoding}&encrypt=true&password={password} (message)",
                "make and broadcast SEND asset amount and mail in body");
        help.put("GET r_send/raw/{creator}/{recipient}?linkTo=<SeqNo>&feePow={feePow}&assetKey={assetKey}&amount={amount}&title={title}&message={message}&encoding={encoding}&encrypt=true&password={password}",
                "make RAW for SEND asset amount and mail");
        help.put("POST r_send {\"linkTo\": \"<SeqNo>\", \"creator\": \"<creator>\", \"recipient\": \"<recipient>\", \"asset\":\"<assetKey>\", \"amount\":\"<amount>\", \"title\": \"<title>\", \"message\": \"<message>\", \"encoding\": <encoding>, \"encrypt\": <true/false>,  \"password\": \"<password>\"}",
                "make and broadcast SEND asset amount and mail");
        help.put("POST r_send/raw {\"linkTo\": \"<SeqNo>\", \"creator\": \"<creator>\", \"recipient\": \"<recipient>\", \"asset\":\"<assetKey>\", \"amount\":\"<amount>\", \"title\": \"<title>\", \"message\": \"<message>\", \"encoding\": <encoding>, \"encrypt\": <true/false>,  \"password\": \"<password>\"}",
                "make RAW for SEND asset amount and mail");
        help.put("GET r_send/test1/{probability}/{delay}?password={password}",
                "Start test send messages. Delay in ms. delay = 0 - stop");
        help.put("GET r_send/test2/{probability}/{delay}?password={password}",
                "Start test send COMPU. Delay in ms. delay = 0 - stop");
        help.put("GET multisend/{fromAddress}/{assetKey}/{forAssetKey}?position=1&amount=0&test=true&feePow=0&activeafter=[date]&activebefore=[date]&greatequal=[amount]&koeff=1&title=&onlyperson=false&selfpay=false&password=",
                "Muli-send from Address [fromAddress] the asset [assetKey] by filter: Who has positive balance by asset [forAssetKey] where "
                        + " position - balance position for test, amount and koeff: sensed AMOUNT = amount + koeff * BALANCE, test - set false for real send or true for statistics, activeafter and activebefore - check activity for address in format: [timestamp_in_sec | YYYY-MM-DD HH:MM],"
                        + " greatequal=0 - if set balance in position must be great or equal this amount, activeTypeTX=0 - if set test activity on this type transactions,"
                        + "selfPay=true - if set pay to self address too. Default = true"
                        + " title=, onlyperson - get only personalized addresses, password=");
        //

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
                          @QueryParam("linkTo") String exLinkObj, @QueryParam("feePow") int feePowStr, @QueryParam("assetKey") long assetKey,
                          @QueryParam("amount") BigDecimal amount, @QueryParam("title") String title,
                          @QueryParam("message") String message,
                          @QueryParam("encoding") int encoding,
                          @QueryParam("encrypt") boolean encrypt, @QueryParam("password") String password) {

        APIUtils.askAPICallAllowed(password, "GET send\n ", request, true);

        JSONObject out = new JSONObject();
        Controller cntr = Controller.getInstance();

        ExLink exLink = ExLinkAppendix.of(exLinkObj);

        boolean needAmount = false;
        Pair<Integer, Transaction> result = cntr.make_R_Send(creatorStr, null, exLink, null, recipientStr, feePowStr,
                assetKey, true,
                amount, needAmount,
                title, message, encoding, encrypt, 0);

        Transaction transaction = result.getB();
        if (transaction == null) {
            out.put("error", result.getA());
            out.put("error_message", OnDealClick.resultMess(result.getA()));
            return out.toJSONString();
        }

        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            out.put("error", validate);
            out.put("error_message", OnDealClick.resultMess(validate));
            return out.toJSONString();
        }

    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("{creator}/{recipient}")
    public String sendPost(@PathParam("creator") String creatorStr, @PathParam("recipient") String recipientStr,
                           @QueryParam("linkTo") String exLinkRef, @QueryParam("feePow") int feePowStr, @QueryParam("assetKey") long assetKey,
                           @QueryParam("amount") BigDecimal amount, @QueryParam("title") String title,
                           String message,
                           @QueryParam("encoding") int encoding,
                           @QueryParam("encrypt") boolean encrypt, @QueryParam("password") String password) {

        return sendGet(creatorStr, recipientStr, exLinkRef, feePowStr, assetKey, amount, title, message, encoding, encrypt, password);

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
            ///logger.error(e.getMessage());
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }

        if (jsonObject == null)
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);

        String creator = (String) jsonObject.getOrDefault("creator", null);
        String recipient = (String) jsonObject.getOrDefault("recipient", null);
        String linkToRefObj = (String) jsonObject.get("linkTo");

        int feePow = Integer.valueOf(jsonObject.getOrDefault("feePow", 0).toString());
        long assetKey = Long.valueOf(jsonObject.getOrDefault("assetKey", 0l).toString());
        BigDecimal amount = new BigDecimal(jsonObject.getOrDefault("amount", 0).toString());
        String title = (String) jsonObject.getOrDefault("title", null);
        String message = (String) jsonObject.getOrDefault("message", null);
        int encoding = Integer.valueOf(jsonObject.getOrDefault("encoding", 0).toString());
        boolean encrypt = Boolean.valueOf((boolean) jsonObject.getOrDefault("encrypt", false));
        String password = (String) jsonObject.getOrDefault("password", null);

        return sendGet(
                creator,
                recipient,
                linkToRefObj,
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
                             @QueryParam("linkTo") Long exLinkRef, @QueryParam("feePow") int feePowStr,
                             @QueryParam("assetKey") long assetKey, @QueryParam("amount") BigDecimal amountStr,
                             @QueryParam("title") String title,
                             @QueryParam("message") String message,
                             @QueryParam("encoding") int encoding, @QueryParam("encrypt") boolean encrypt,
                             @QueryParam("rawbase") int rawbase,
                             @QueryParam("password") String password) {

        APIUtils.askAPICallAllowed(password, "GET rawSend\n ", request, true);

        JSONObject out = new JSONObject();
        Controller cntr = Controller.getInstance();

        ExLink exLink;
        if (exLinkRef == null) {
            exLink = null;
        } else {
            exLink = new ExLinkAppendix(exLinkRef);
        }

        boolean needAmount = false;
        Pair<Integer, Transaction> result = cntr.make_R_Send(creatorStr, null, exLink, null, recipientStr, feePowStr,
                assetKey, true,
                amountStr, needAmount,
                title, message, encoding, encrypt, 0);

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
        Object linkToRefObj = jsonObject.get("linkTo");
        Long linkToRef;
        if (linkToRefObj == null)
            linkToRef = null;
        else {
            linkToRef = Transaction.parseDBRef(linkToRefObj);
            if (linkToRef == null) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR);
            }
        }
        int feePow = Integer.valueOf(jsonObject.getOrDefault("feePow", 0).toString());
        long assetKey = Long.valueOf(jsonObject.getOrDefault("assetKey", 0l).toString());
        BigDecimal amount = new BigDecimal(jsonObject.getOrDefault("amount", 0).toString());
        String title = (String) jsonObject.getOrDefault("title", null);
        String message = (String) jsonObject.getOrDefault("message", null);
        int encoding = Integer.valueOf(jsonObject.getOrDefault("encoding", 0).toString());
        boolean encrypt = Boolean.valueOf((boolean) jsonObject.getOrDefault("encrypt", false));
        int rawbase = Integer.valueOf(jsonObject.getOrDefault("rawbase", 58).toString());
        String password = (String) jsonObject.getOrDefault("password", null);

        return rawSendGet(
                creator,
                recipient,
                linkToRef,
                feePow,
                assetKey, amount,
                title, message, encoding,
                encrypt,
                rawbase,
                password
        );

    }

    private static long test1Delay = 0;
    private static float test1probability = 0;
    private static Thread threadTest1;
    private static List<PrivateKeyAccount> test1Creators;

    /**
     * GET r_send/test1/0.85/1000
     * @param probability - с вероятностью. 1 = каждый раз
     * @param delay
     * @param password
     * @return
     */
    @GET
    @Path("test1/{probability}/{delay}")
    public String test1(@PathParam("probability") float probability, @PathParam("delay") long delay, @QueryParam("password") String password) {

        if (!BlockChain.TEST_MODE
                && ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))
        )
            return "not LOCAL && not testnet";

        APIUtils.askAPICallAllowed(password, "GET test1\n ", request, true);

        this.test1Delay = delay;
        this.test1probability = probability;

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
        test1Creators = Controller.getInstance().getWalletPrivateKeyAccounts();

        // запомним счетчики для счетов
        HashMap<String, Long> counters = new HashMap<String, Long>();
        for (Account crestor: test1Creators) {
            counters.put(crestor.getAddress(), 0L);
        }

        JSONObject out = new JSONObject();

        if (test1Creators.size() <= 1) {
            out.put("error", "too small accounts");

            return out.toJSONString();
        }

        threadTest1 = new Thread(() -> {

            Random random = new Random();
            Controller cnt = Controller.getInstance();

            do {

                if (this.test1Delay <= 0) {
                    return;
                }

                if (cnt.isOnStopping())
                    return;

                // если есть вероятногсть по если не влазим в нее то просто ожидание и пропуск ходя
                if (test1probability < 1 && test1probability > 0) {
                    int rrr = random.nextInt((int) (100.0 / test1probability) );
                    if (rrr > 100) {
                        try {
                            Thread.sleep(this.test1Delay);
                        } catch (InterruptedException e) {
                            break;
                        }

                        continue;
                    }
                }

                try {

                    PrivateKeyAccount creator = test1Creators.get(random.nextInt(test1Creators.size()));
                    Account recipient;
                    do {
                        recipient = test1Creators.get(random.nextInt(test1Creators.size()));
                    } while (recipient.equals(creator));


                    String address = creator.getAddress();
                    long counter = counters.get(address);

                    WeakReference<RSend> weakRef = new WeakReference<>(new RSend(creator, null, null, (byte) 0, recipient, 2l, null,
                            "LoadTest_" + address.substring(1, 5) + " " + counter,
                            (address + counter + "TEST TEST TEST").getBytes(StandardCharsets.UTF_8), new byte[]{(byte) 1},
                            new byte[]{(byte) 1}, NTP.getTime(), 0l));

                    weakRef.get().sign(creator, Transaction.FOR_NETWORK);

                    // карта сбрасывается иногда при очистке, поэтому надо брать свежую всегда
                    cnt.transactionsPool.offerMessage(weakRef.get());
                    cnt.broadcastTransaction(weakRef.get());

                    try {
                        Thread.sleep(this.test1Delay);
                    } catch (InterruptedException e) {
                        break;
                    }

                    if (cnt.isOnStopping())
                        return;

                } catch (Exception e10) {
                    // not worked LOGGER.error(e10.getMessage(), e10);
                } catch (Throwable e10) {
                    // not worked LOGGER.error(e10.getMessage(), e10);
                }

            } while (true);
        });

        threadTest1.setName("RSend.Test1");
        threadTest1.start();

        out.put("delay", test1Delay);
        LOGGER.info("r_send/test1 STARTED for delay: " + test1Delay);

        return out.toJSONString();

    }

    private static long test2Delay = 0;
    private static float test2probability = 0;
    private static Thread threadTest2;
    private static List<PrivateKeyAccount> test2Creators;

    /**
     * GET r_send/test2/0.85/1000
     * @param probability - с вероятностью. 1 = каждый раз
     * @param delay
     * @param password
     * @return
     */
    @GET
    @Path("test2/{probability}/{delay}")
    public String test2(@PathParam("probability") float probability, @PathParam("delay") long delay, @QueryParam("password") String password) {

        if (!BlockChain.TEST_MODE
                && ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))
        )
            return "not LOCAL && not testnet";

        APIUtils.askAPICallAllowed(password, "GET test2\n ", request, true);

        this.test2Delay = delay;
        this.test2probability = probability;

        if (threadTest2 != null) {
            JSONObject out = new JSONObject();
            if (delay <= 0) {
                threadTest2 = null;
                out.put("status", "STOP");
                LOGGER.info("r_send/test2 STOP");
            } else {
                out.put("delay", delay);
                LOGGER.info("r_send/test2 DELAY UPDATE:" + delay);
            }
            return out.toJSONString();
        }

        // CACHE private keys
        test2Creators = Controller.getInstance().getWalletPrivateKeyAccounts();

        // запомним счетчики для счетов
        HashMap<String, Long> counters = new HashMap<String, Long>();
        for (Account crestor: test2Creators) {
            counters.put(crestor.getAddress(), 0L);
        }

        JSONObject out = new JSONObject();

        if (test2Creators.size() <= 1) {
            out.put("error", "too small accounts");

            return out.toJSONString();
        }

        threadTest2 = new Thread(() -> {

            Random random = new Random();
            Controller cnt = Controller.getInstance();
            BigDecimal amount = new BigDecimal("0.00000001");
            byte[] isText = new byte[]{1};
            byte[] encryptMessage = new byte[]{0};

            do {

                if (this.test2Delay <= 0) {
                    return;
                }

                if (cnt.isOnStopping())
                    return;

                // если есть вероятногсть по если не влазим в нее то просто ожидание и пропуск ходя
                if (test2probability < 1 && test2probability > 0) {
                    int rrr = random.nextInt((int) (100.0 / test2probability) );
                    if (rrr > 100) {
                        try {
                            Thread.sleep(this.test2Delay);
                        } catch (InterruptedException e) {
                            break;
                        }

                        continue;
                    }
                }

                try {

                    PrivateKeyAccount creator = test2Creators.get(random.nextInt(test2Creators.size()));
                    Account recipient;
                    do {
                        recipient = test2Creators.get(random.nextInt(test2Creators.size()));
                    } while (recipient.equals(creator));


                    if (cnt.isOnStopping())
                        return;

                    String address = creator.getAddress();
                    long counter = counters.get(address);

                    if (false) {
                        // ERA - она еще форжинговые балансы изменяет - поэтому КОМПУ лучше всего
                        Transaction transaction = cnt.r_Send(creator,
                                null, null, 0, recipient,
                                2L, amount, "LoadTestSend_" + address.substring(1, 5) + " " + counter,
                                (address + counter + "TEST SEND ERA").getBytes(StandardCharsets.UTF_8), encryptMessage,
                                new byte[]{(byte) 1}, 0);

                        Integer result = cnt.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);
                        // CLEAR for HEAP
                        transaction.resetDCSet();

                        // CHECK VALIDATE MESSAGE
                        if (result == Transaction.VALIDATE_OK) {

                            counters.put(address, counter + 1);

                        } else {
                            if (result == Transaction.RECEIVER_NOT_PERSONALIZED
                                    || result == Transaction.CREATOR_NOT_PERSONALIZED
                                    || result == Transaction.NO_BALANCE
                                    || result == Transaction.NOT_ENOUGH_FEE
                                    || result == Transaction.UNKNOWN_PUBLIC_KEY_FOR_ENCRYPT) {

                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                    break;
                                }

                                continue;
                            }

                            // not work in Threads - logger.info("test2: " + OnDealClick.resultMess(result));
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                break;
                            }
                            continue;
                        }

                    } else {

                        WeakReference<RSend> weakRef = new WeakReference<>(new RSend(creator, null, null, (byte) 0, recipient, 2L,
                                amount, "TEST" + counter, null, isText, encryptMessage, NTP.getTime(), 0L));

                        weakRef.get().sign(creator, Transaction.FOR_NETWORK);

                        // карта сбрасывается иногда при очистке, поэтому надо брать свежую всегда
                        cnt.transactionsPool.offerMessage(weakRef.get());
                        cnt.broadcastTransaction(weakRef.get());

                    }


                    try {
                        Thread.sleep(this.test2Delay);
                    } catch (InterruptedException e) {
                        break;
                    }

                    if (cnt.isOnStopping())
                        return;

                } catch (Exception e10) {
                    // not worked LOGGER.error(e10.getMessage(), e10);
                } catch (Throwable e10) {
                    // not worked LOGGER.error(e10.getMessage(), e10);
                }

            } while (true);
        });

        threadTest2.setName("RSend.test2");
        threadTest2.start();

        out.put("delay", test2Delay);
        LOGGER.info("r_send/test2 STARTED for delay: " + test2Delay);

        return out.toJSONString();

    }

    /**
     * Multi send scrip for send asset for many addresses or persons filtered by some parameters.
     * This command will run as test for calculate FEE and total AMOUNT by default. For run real send set parameter `test=false`.
     * Unlock wallet.
     * <br>
     * GET r_send/multisend/7LSN788zgesVYwvMhaUbaJ11oRGjWYagNA/1036/2?amount=0.001&title=probe-multi&onlyperson=true&activeafter=1577712486&password=123
     * GET r_send/multisend/7LSN788zgesVYwvMhaUbaJ11oRGjWYagNA/1069/1036?amount=0.001&title=probe-multi&onlyperson=true&activeafter=2018-01-01 00:00&activebefore=2019-01-01 00:00&greatequal=0&activetypetx=24&password=1
     * GET r_send/multisend/7A94JWgdnNPZtbmbphhpMQdseHpKCxbrZ1/1/2?amount=0.001&title=probe-multi&onlyperson=true&gender=0&password=1
     * get r_send/multisend/78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5/1072/2?amount=1&title=С 8 Марта!&onlyperson=true&gender=1&password=123&test=false
     * GET r_send/multisend/7LSN788zgesVYwvMhaUbaJ11oRGjWYagNA/1/2?amount=100&title=probe-multi&onlyperson=true&activeafter=2019-09-11 00:00&greatequal=0&password=1
     *
     * @param fromAddress     my address in Wallet
     * @param assetKey        asset Key that send
     * @param forAssetKey     asset key of holders test
     * @param amount          absolute amount to send
     * @param onlyPerson      Default: false. Use only person accounts
     * @param gender          Filter by gender. -1 = all, 0 - man, 1 - woman. Default: -1.
     * @param position        test balance position. 1 - Own, 2 - Credit, 3 - Hold, 4 - Spend, 5 - Other
     * @param greatEqual      test balance is great or equal
     * @param selfPay         if set - pay to self address too. Default = true
     * @param test            default - true. test=false - real send
     * @param feePow
     * @param activeAfterStr  timestamp after that is filter - yyyy-MM-dd hh:mm or timestamp(sec)
     * @param activeBeforeStr timestamp before that is filter - yyyy-MM-dd hh:mm or timestamp(sec) activetypetx
     * @param activeTypeTX    if set - test only that type transactions
     * @param koeff           koefficient for amount in balance position of forAssetKey
     * @param title
     * @param password
     * @return
     */
    @GET
    @Path("multisend/{fromAddress}/{assetKey}/{forAssetKey}")
    public String multiSend(@PathParam("fromAddress") String fromAddress, @PathParam("assetKey") long assetKey, @PathParam("forAssetKey") long forAssetKey,
                            @DefaultValue("1") @QueryParam("position") Integer position,
                            @DefaultValue("0") @QueryParam("greatequal") BigDecimal greatEqual, // больше или равно чем
                            @DefaultValue("0") @QueryParam("amount") BigDecimal amount,
                            @DefaultValue("true") @QueryParam("test") Boolean test,
                            @DefaultValue("true") @QueryParam("selfpay") Boolean selfPay,
                            @QueryParam("linkTo") Long exLinkRef,
                            @DefaultValue("0") @QueryParam("feePow") Integer feePow,
                            @DefaultValue("-1") @QueryParam("gender") Byte gender,
                            @DefaultValue("0") @QueryParam("activeafter") String activeAfterStr,
                            @DefaultValue("0") @QueryParam("activebefore") String activeBeforeStr,
                            @DefaultValue("0") @QueryParam("activetypetx") int activeTypeTX, // активность по заданному типу транзакции
                            @DefaultValue("0") @QueryParam("koeff") BigDecimal koeff,
                            @QueryParam("title") String title,
                            @DefaultValue("false") @QueryParam("onlyperson") Boolean onlyPerson,
                            @QueryParam("password") String password) {

        if (!test && !BlockChain.TEST_MODE
                && ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))
        )
            return "not LOCAL && not testnet";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Long activeAfter;
        try {
            Date parsedDate = dateFormat.parse(activeAfterStr);
            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
            activeAfter = timestamp.getTime();
        } catch(Exception e) {
            try {
                activeAfter = Long.parseLong(activeAfterStr) * 1000L;
            } catch (Exception e1) {
                activeAfter = null;
            }
        }

        Long activeBefore;
        try {
            Date parsedDate = dateFormat.parse(activeBeforeStr);
            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
            activeBefore = timestamp.getTime();
        } catch(Exception e) {
            try {
                activeBefore = Long.parseLong(activeBeforeStr) * 1000L;
            } catch (Exception e1) {
                activeBefore = null;
            }
        }

        Controller cntr = Controller.getInstance();
        BlockChain chain = cntr.getBlockChain();

        // преобразуем в seqNo
        Long fromSeqNo = null;
        if (activeAfter != null && activeAfter > 0) {
            fromSeqNo = Transaction.makeDBRef(chain.getHeightOnTimestampMS(activeAfter), 0);
            //LOGGER.debug("fromSeqNo:" + Transaction.viewDBRef(fromSeqNo) + " - " + new Date(activeAfter));
        }
        Long toSeqNo = null;
        if (activeBefore != null && activeBefore > 0) {
            toSeqNo = Transaction.makeDBRef(chain.getHeightOnTimestampMS(activeBefore), 0);
            //LOGGER.debug("toSeqNo:" + Transaction.viewDBRef(toSeqNo) + " - " + new Date(activeBefore));
        }

        if (!test) {
            // так как тут может очень долго работать то откроем на долго
            APIUtils.askAPICallAllowed(password, "GET multisend\n ", request, false);
        }
        try {

            JSONObject out = new JSONObject();
            JSONArray outResult = new JSONArray();

            Fun.Tuple2<Account, String> accResult = Account.tryMakeAccount(fromAddress);
            if (accResult.b != null) {
                throw ApiErrorFactory.getInstance().createError(
                        //ApiErrorFactory.ERROR_INVALID_ADDRESS);
                        Transaction.INVALID_ADDRESS);
            }

            Account accountFrom = accResult.a;

            BigDecimal totalSendAmount = BigDecimal.ZERO;

            DCSet dcSet = DCSet.getInstance();
            ItemAssetBalanceMap balancesMap = dcSet.getAssetBalanceMap();
            TransactionFinalMapImpl txMap = dcSet.getTransactionFinalMap();

            byte[] key;
            Fun.Tuple2<BigDecimal, BigDecimal> balance;

            int count = 0;
            BigDecimal totalFee = BigDecimal.ZERO;

            Fun.Tuple4<Long, Integer, Integer, Integer> addressDuration;
            Long myPersonKey = null;
            if (onlyPerson && !selfPay) {
                addressDuration = dcSet.getAddressPersonMap().getItem(accountFrom.getShortAddressBytes());
                if (addressDuration != null) {
                    myPersonKey = addressDuration.a;
                }
            } else {
                myPersonKey = null;
            }

            HashSet<Long> usedPersons = new HashSet<>();
            boolean needAmount = true;
            long timestampThis = NTP.getTime() - 10000L;
            PersonCls person;

            try (IteratorCloseable<byte[]> iterator = balancesMap.getIteratorByAsset(forAssetKey)) {
                while (iterator.hasNext()) {
                    key = iterator.next();

                    try {

                        balance = Account.getBalanceForPosition(balancesMap.get(key), position);

                        // только тем у кого положительный баланс и больше чем задано
                        if (balance.b.compareTo(greatEqual) < 0)
                            continue;

                        byte[] recipentShort = ItemAssetBalanceMap.getShortAccountFromKey(key);

                        if (onlyPerson) {
                            // так как тут сортировка по убыванию значит первым встретится тот счет на котром больше всего актива
                            // - он и будет выбран куда 1 раз пошлем актив свой
                            addressDuration = dcSet.getAddressPersonMap().getItem(recipentShort);
                            if (addressDuration == null)
                                continue;
                            if (usedPersons.contains(addressDuration.a))
                                continue;

                            if (!selfPay && myPersonKey != null && myPersonKey.equals(addressDuration.a)) {
                                // сами себе не платим?
                                continue;
                            }

                            person = (PersonCls) dcSet.getItemPersonMap().get(addressDuration.a);

                            if (gender >= 0) {
                                if (person.getGender() != gender) {
                                    continue;
                                }
                            }
                        } else {

                            if (!selfPay && fromAddress.equals(recipentShort)) {
                                // сами себе не платим?
                                continue;
                            }

                            addressDuration = null;
                            person = null;
                        }

                        /// если задано то проверим - входит ли в в диаппазон
                        // - собранные блоки учитываем? да - иначе долго будет делать поиск
                        if (fromSeqNo != null || toSeqNo != null) {
                            // на счете должна быть активность в заданном диаппазоне для данного типа
                            if (!txMap.isCreatorWasActive(recipentShort, fromSeqNo, activeTypeTX, toSeqNo))
                                continue;
                        }

                        JSONArray resultOne = new JSONArray();

                        BigDecimal sendAmount;
                        if (amount.signum() > 0) {
                            sendAmount = amount;
                        } else {
                            sendAmount = BigDecimal.ZERO;
                        }

                        if (koeff.signum() > 0) {
                            sendAmount = sendAmount.add(balance.b.multiply(koeff));
                        }

                        String recipientStr = Crypto.getInstance().getAddressFromShort(recipentShort);
                        resultOne.add(recipientStr);
                        resultOne.add(sendAmount.toPlainString());
                        if (person != null) {
                            resultOne.add(person.toString());
                        }

                        ExLink exLink;
                        if (exLinkRef == null) {
                            exLink = null;
                        } else {
                            exLink = new ExLinkAppendix(exLinkRef);
                        }

                        Pair<Integer, Transaction> result = cntr.make_R_Send(null, accountFrom, exLink, null, recipientStr, feePow,
                                assetKey, true,
                                sendAmount, needAmount,
                                title, null, 0, false, timestampThis++);

                        Transaction transaction = result.getB();
                        if (transaction == null) {
                            resultOne.add(OnDealClick.resultMess(result.getA()));

                            if (test) {
                                // просчитаем тоже даже если ошибка
                                totalSendAmount = totalSendAmount.add(sendAmount);
                                ///totalFee = totalFee.add(transaction.getFee());
                                count++;
                                if (onlyPerson) {
                                    // учтем что такой персоне давали
                                    usedPersons.add(addressDuration.a);
                                }
                            }

                        } else {

                            WeakReference<Transaction> weakRef = new WeakReference<>(transaction);
                            int validate = cntr.getTransactionCreator().afterCreate(weakRef.get(),
                                    // если проба то не шлем в реальности
                                    test ? Transaction.FOR_PACK : Transaction.FOR_NETWORK, false, false);

                            if (validate != Transaction.VALIDATE_OK) {
                                resultOne.add(OnDealClick.resultMess(validate));

                            } else {
                                // УСПЕХ! учтем все
                                totalSendAmount = totalSendAmount.add(weakRef.get().getAmount());
                                totalFee = totalFee.add(weakRef.get().getFee());
                                count++;
                                if (onlyPerson) {
                                    // учтем что такой персоне давали
                                    usedPersons.add(addressDuration.a);
                                }
                            }
                        }

                        transaction = null;
                        outResult.add(resultOne);

                    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                        LOGGER.error("Wrong key raw: " + Base58.encode(key));
                    }
                }

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            out.put("_results", outResult);
            out.put("asset", assetKey);
            out.put("count", count);
            out.put("totalFee", totalFee.toPlainString());
            out.put("totalSendAmount", totalSendAmount.toPlainString());

            if (test)
                out.put("status", "TEST");

            return out.toJSONString();

        } finally {
            Controller.getInstance().lockWallet();
        }
    }

}
