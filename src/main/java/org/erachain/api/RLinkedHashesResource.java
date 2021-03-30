package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.transaction.Transaction;
import org.erachain.utils.APIUtils;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * linked hashes
 */
@Path("r_linkedhashes")
@Produces(MediaType.APPLICATION_JSON)
public class RLinkedHashesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RLinkedHashesResource.class);

    @Context
    HttpServletRequest request;

    //@Consumes(MediaType.WILDCARD)

    @GET
    public String help() {
        Map<String, String> help = new LinkedHashMap<String, String>();
        help.put("GET r_linkedhashes/{creator}?linkTo=Seq-No&url=<String>&message=<String>&hashes<Strind[,- ]>&feePow<int>&password=<String",
                "Make and broadcast Linked Hashes transaction");
        help.put("POST r_linkedhashes/make {\"linkTo\": \"<SeqNo>\", \"creator\": \"<creator>\", \"url\": \"<String>\", \"message\":\"<String>\", \"hashes\":\"<Strind[,- ]>\", \"feePow\": \"<int>\", \"password\": \"<password>\"}",
                "Make and broadcast Linked Hashes transaction");

        return StrJSonFine.convert(help);
    }

    private String make(PrivateKeyAccount creator, String hashes,
                        String exLinkRef, Long feePow,
                        String url,
                        String message) {

        Controller cntr = Controller.getInstance();

        ExLink exLink;
        if (exLinkRef == null) {
            exLink = null;
        } else {
            Long linkTo = Transaction.parseDBRef(exLinkRef);
            if (linkTo != null)
                exLink = new ExLinkAppendix(linkTo);
            else
                exLink = null;
        }

        Transaction transaction = cntr.r_Hashes(creator, exLink, feePow.intValue(),
                url, message, hashes);

        if (transaction == null) {
            JSONObject out = new JSONObject();
            out.put("error", "unknown");
            return out.toJSONString();
        }

        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            return transaction.makeErrorJSON(validate).toJSONString();
        }

    }

    @GET
    @Path("make/{creator}/{hashes}")
    public String makeGet(@PathParam("creator") String creatorStr, @PathParam("hashes") String hashes,
                          @QueryParam("linkTo") String exLinkRef, @DefaultValue("0") @QueryParam("feePow") Long feePow,
                          @QueryParam("url") String url,
                          @QueryParam("message") String message,
                          @QueryParam("password") String password) {

        // READ CREATOR
        Fun.Tuple2<Account, String> resultCreator = Account.tryMakeAccount(creatorStr);
        if (resultCreator.b != null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_MAKER_ADDRESS, resultCreator.b);
        }

        APIUtils.askAPICallAllowed(password, "GET r_linkedhashes\n ", request, true);

        Controller cntr = Controller.getInstance();

        PrivateKeyAccount privateKeyAccount = cntr.getWalletPrivateKeyAccountByAddress(resultCreator.a.getAddress());
        if (privateKeyAccount == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_WALLET_ADDRESS);
        }

        return make(privateKeyAccount, hashes, exLinkRef, feePow, url, message);

    }

    @POST
    public String makePost(String x) {
        // READ JSON
        Tuple3<JSONObject, PrivateKeyAccount, Integer> resultRequet = APIUtils.postPars(request, x);

        JSONObject jsonObject = resultRequet.a;
        PrivateKeyAccount maker = resultRequet.b;
        int feePow = resultRequet.c;

        String url = (String) jsonObject.get("url");
        String message = (String) jsonObject.get("message");
        String hashes = (String) jsonObject.get("hashes");
        String linkTo = (String) jsonObject.get("linkTo");

        return make(maker, hashes, linkTo, (long) feePow, url, message);

    }
}
