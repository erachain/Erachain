package api;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import controller.Controller;
import core.crypto.Base58;
import core.transaction.Transaction;
import utils.APIUtils;
import utils.Pair;
import utils.StrJSonFine;

@Path("r_send")
@Produces(MediaType.APPLICATION_JSON)
public class R_SendResource {
    
    private static final Logger LOGGER = Logger.getLogger(R_SendResource.class);
    @Context
    HttpServletRequest request;
    
    @GET
    public String help() {
        Map<String, String> help = new LinkedHashMap<String, String>();
        help.put("GET r_send/{creator}/{recipient}?feePow={feePow}&assetKey={assetKey}&amount={amount}&title={title}&message={message}&nottext=true&encrypt=true&password={password}",
                "make and broadcast SEND asset amount and mail");
        help.put("GET r_send/raw/{creator}/{recipient}?feePow={feePow}&assetKey={assetKey}&amount={amount}&title={title}&message={message}&nottext=true&encrypt=true&password={password}",
                "make RAW for SEND asset amount and mail");
        help.put("POST r_send {\"creator\": \"<creator>\", \"recipient\": \"<recipient>\", \"asset\":\"<assetKey>\", \"amount\":\"<amount>\", \"title\": \"<title>\", \"message\": \"<message>\", \"nottext\": <true/false>, \"encrypt\": <true/false>,  \"password\": \"<password>\"}",
                "make and broadcast SEND asset amount and mail");
        help.put("POST r_send/raw {\"creator\": \"<creator>\", \"recipient\": \"<recipient>\", \"asset\":\"<assetKey>\", \"amount\":\"<amount>\", \"title\": \"<title>\", \"message\": \"<message>\", \"nottext\": <true/false>, \"encrypt\": <true/false>,  \"password\": \"<password>\"}",
                "make RAW for SEND asset amount and mail");
        
        return StrJSonFine.convert(help);
    }
    
    /*
     * send and broadcast GET
     * GET r_send/{creator}/{recipient}?feePow={feePow}&assetKey={assetKey}&amount={amount}&title={title}&message={message}&nottext=true&encrypt=true&password={password}"
     * 
     */
    
    @GET
    // @Consumes(MediaType.WILDCARD)
    @Path("{creator}/{recipient}")
    public String sendGet(@PathParam("creator") String creatorStr, @PathParam("recipient") String recipientStr,
            @QueryParam("feePow") String feePowStr, @QueryParam("assetKey") String assetKeyStr,
            @QueryParam("amount") String amountStr, @QueryParam("title") String title,
            @QueryParam("message") String message, @QueryParam("codebase") int codebase,
            @QueryParam("encrypt") boolean encrypt, @QueryParam("password") String password) {
        
        APIUtils.askAPICallAllowed(password, "GET send\n ", request);
        
        JSONObject out = new JSONObject();
        Controller cntr = Controller.getInstance();
        
        boolean needAmount = false;
        Pair<Integer, Transaction> result = cntr.make_R_Send(creatorStr, null, recipientStr, feePowStr,
                assetKeyStr, true,
                amountStr, needAmount,
                title, message, codebase, encrypt);
        
        Transaction transaction = result.getB();
        if (transaction == null) {
            out.put("error", result.getA());
            out.put("error_message", gui.transaction.OnDealClick.resultMess(result.getA()));
            return out.toJSONString();
        }
        
        int validate = cntr.getTransactionCreator().afterCreate(transaction, false);
        
        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            out.put("error", validate);
            out.put("error_message", gui.transaction.OnDealClick.resultMess(validate));
            return out.toJSONString();
        }
        
    }
    
    /*
     * send and broadcast POST r_send
     * POST r_send {\"creator\": \"<creator>\", \"recipient\": \"<recipient>\", \"asset\":\"<assetKey>\", \"amount\":\"<amount>\", \"title\": \"<title>\", \"message\": \"<message>\", \"nottext\": <true/false>, \"encrypt\": <true/false>,  \"password\": \"<password>\"}"
     * 
     * 
     */
    @POST
    @SuppressWarnings("unchecked")
    @Consumes(MediaType.WILDCARD)
    public String sendPost(String x) {

        JSONObject jsonObject;
        try {
            //READ JSON
            jsonObject = (JSONObject) JSONValue.parse(x);
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            ///LOGGER.info(e);
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }

        String creator = (String)jsonObject.getOrDefault("creator", null);
        String recipient = (String)jsonObject.getOrDefault("recipient", null);
        String feePow = (String)jsonObject.getOrDefault("feePow", null);
        String assetKey = (String)jsonObject.getOrDefault("assetKey", null);
        String amount = (String)jsonObject.getOrDefault("amount", null);
        String title = (String)jsonObject.getOrDefault("title", null);
        String message = (String)jsonObject.getOrDefault("message", null);
        int codebase = (int)jsonObject.getOrDefault("codebase", 0);
        boolean encrypt = (boolean)jsonObject.getOrDefault("encrypt", false);
        String password = (String)jsonObject.getOrDefault("password", null);
        
        return sendGet(
                creator,
                recipient,
                feePow,
                assetKey, amount,
                title, message,
                codebase, encrypt,
                password
                );

    }
    
    /*
     * make and return RAW
     * GET r_send/raw/{creator}/{recipient}?feePow={feePow}&assetKey={assetKey}&amount={amount}&title={title}&message={message}&codebase={codebase}&encrypt=true&rawbase={58/64}&password={password}
     * 
     * GET r_send/raw/79QuhunbPbc3svqsXMC2JHbh8ix6kwNAao/74eD7JSrkXPsz3xxKMhMEDoTF6TqkfEHBt?feePow=3&assetKey=1001&amount=111&title=probe&message=message&codebase=0&encrypt=true&password=1
     * GET r_send/raw/79QuhunbPbc3svqsXMC2JHbh8ix6kwNAao/74eD7JSrkXPsz3xxKMhMEDoTF6TqkfEHBt?title=probe&message=message&rawbase=64&password=1
     */
    @GET
    // @Consumes(MediaType.WILDCARD)
    //@Produces("text/plain")
    @Path("raw/{creator}/{recipient}")
    public String rawSendGet(@PathParam("creator") String creatorStr, @PathParam("recipient") String recipientStr,
            @QueryParam("feePow") String feePowStr,
            @QueryParam("assetKey") String assetKeyStr, @QueryParam("amount") String amountStr,
            @QueryParam("title") String title, @QueryParam("message") String message,
            @QueryParam("codebase") int codebase, @QueryParam("encrypt") boolean encrypt,
            @QueryParam("rawbase") int rawbase,
            @QueryParam("password") String password) {
        
        APIUtils.askAPICallAllowed(password, "GET rawSend\n ", request);
        
        JSONObject out = new JSONObject();
        Controller cntr = Controller.getInstance();
        
        boolean needAmount = false;
        Pair<Integer, Transaction> result = cntr.make_R_Send(creatorStr, null, recipientStr, feePowStr,
                assetKeyStr, true,
                amountStr, needAmount,
                title, message, codebase, encrypt);
        
        Transaction transaction = result.getB();
        if (transaction == null) {
            out.put("error", result.getA());
            out.put("error_message", gui.transaction.OnDealClick.resultMess(result.getA()));
            return out.toJSONString();
        }
        
        String str;
        if (rawbase == 64)
            out.put("raw64", Base64.getEncoder().encodeToString(transaction.toBytes(true, null)));
        else
            out.put("raw", Base58.encode(transaction.toBytes(true, null)));
        
        return out.toJSONString();
        
    }

    /*
     * make and return RAW
     * POST r_send/raw64 {"creator": "<creator>", "recipient": "<recipient>", "asset":"<assetKey>", "amount":"<amount>", "title": "<title>", "message": "<message>", "codebase": <codebase>, "encrypt": <true/false>,  "password": "<password>"}"
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

        String creator = (String)jsonObject.getOrDefault("creator", null);
        String recipient = (String)jsonObject.getOrDefault("recipient", null);
        String feePow = (String)jsonObject.getOrDefault("feePow", null);
        String assetKey = (String)jsonObject.getOrDefault("assetKey", null);
        String amount = (String)jsonObject.getOrDefault("amount", null);
        String title = (String)jsonObject.getOrDefault("title", null);
        String message = (String)jsonObject.getOrDefault("message", null);
        int codebase = (int)jsonObject.getOrDefault("codebase", 0);
        boolean encrypt = (boolean)jsonObject.getOrDefault("encrypt", false);
        int rawbase = (int)jsonObject.getOrDefault("rawbase", 58);
        String password = (String)jsonObject.getOrDefault("password", null);
        
        return rawSendGet(
                creator,
                recipient,
                feePow,
                assetKey, amount,
                title, message,
                codebase, encrypt,
                rawbase,
                password
                );

    }

}
