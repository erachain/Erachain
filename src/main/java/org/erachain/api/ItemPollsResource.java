package org.erachain.api;

import com.google.gson.Gson;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.item.polls.PollFactory;
import org.erachain.core.transaction.IssuePollRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.utils.APIUtils;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("polls")
@Produces(MediaType.APPLICATION_JSON)
public class ItemPollsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemPollsResource.class);

    @Context
    HttpServletRequest request;

    @GET
    public String help() {
        Map help = new LinkedHashMap();

        help.put("polls/last", "Get last key");
        help.put("polls/{key}", "Returns information about poll with the given key.");
        help.put("polls/raw/{key}", "Returns RAW in Base58 of poll with the given key.");
        help.put("polls/images/{key}", "get item Images by key");
        help.put("polls/listfrom/{start}", "get list from KEY");
        help.put("GET polls/issue {\"creator\":\"<creatorAddress>\", \"linkTo\":\"<SeqNo>\",  \"name\":\"<name>\", \"description\":\"<description>\", \"options\": [<optionOne>, <optionTwo>], \"feePow\":\"<feePow>\"}", "issue");
        help.put("POST polls/issue {\"creator\":\"<creatorAddress>\", \"linkTo\":\"<SeqNo>\", \"name\":\"<name>\", \"description\":\"<description>\", \"options\": [<optionOne>, <optionTwo>], \"feePow\":\"<feePow>\"}", "Used to create a new poll. Returns the transaction in JSON when successful.");
        help.put("POST polls/issueraw/{creator} {\"linkTo\":<SeqNo>, \"feePow\":<int>, \"password\":<String>, \"linkTo\":<SeqNo>, \"raw\":RAW-Base58", "Issue Poll by Base58 RAW in POST body");

        help.put("polls/vote/{key}/{option}/{voter}?feePow=feePow", "Used to vote on a poll with the given KEY. Returns the transaction in JSON when successful.");
        help.put("POST polls/vote/{key} {\"voter\":\"<voterAddress>\", \"option\": \"<optionOne>\", \"feePow\":\"<feePow>\"}", "Used to vote on a poll with the given KEY. Returns the transaction in JSON when successful.");

        help.put("polls/address/{address}", "Returns an array of all the polls owned by a specific address in your wallet.");

        return StrJSonFine.convert(help);
    }

    @GET
    @Path("last")
    public String last() {
        return "" + DCSet.getInstance().getItemPollMap().getLastKey();
    }

    /**
     * Get lite information poll by key poll
     *
     * @param key is number poll
     * @return JSON object. Single poll
     */
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

        if (!DCSet.getInstance().getItemPollMap().contains(asLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_POLL_NOT_EXIST);

        }

        return Controller.getInstance().getPoll(asLong).toJson().toJSONString();
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

        if (!DCSet.getInstance().getItemPollMap().contains(asLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_POLL_NOT_EXIST);
        }

        ItemCls item = Controller.getInstance().getPoll(asLong);
        byte[] issueBytes = item.toBytes(Transaction.FOR_NETWORK, false, false);
        return Base58.encode(issueBytes);
    }

    /**
     *
     */
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

        if (!DCSet.getInstance().getItemPollMap().contains(asLong)) {
            throw ApiErrorFactory.getInstance().createError(
                    Transaction.ITEM_POLL_NOT_EXIST);

        }

        return Controller.getInstance().getPoll(asLong).toJsonData().toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("listfrom/{start}")
    public String getList(@PathParam("start") long start,
                          @DefaultValue("20") @QueryParam("page") int page,
                          @DefaultValue("true") @QueryParam("showperson") boolean showPerson,
                          @DefaultValue("true") @QueryParam("desc") boolean descending) {

        JSONObject output = new JSONObject();
        ItemCls.makeJsonLitePage(DCSet.getInstance(), ItemCls.POLL_TYPE, start, page, output, showPerson, descending);

        return output.toJSONString();
    }

    /**
     * Create new poll.
     * <br>
     * <h3>example request:</h3>
     * {"creator":"", "name":"", "description":"", "options":["1","2","3"], "feePow":""}
     *
     * @param poll is poll in json format
     * @return record trades
     * <h3>  example param poll : {"creator":"", "name":"", "description":"", "options":["1","2","3"], "feePow":""}</h3>
     */

    @POST
    @Consumes(MediaType.WILDCARD)
    @Path("issue")
    public String issuePoll(String poll) {

        String password = null;
        APIUtils.askAPICallAllowed(password, "POST polls " + poll, request, true);

        try {
            //READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(poll);
            String creator = (String) jsonObject.get("creator");
            String name = (String) jsonObject.get("name");
            String description = (String) jsonObject.get("description");
            JSONArray optionsJSON = (JSONArray) jsonObject.get("options");
            String feePowStr = (String) jsonObject.get("feePow");

            String linkToRefStr = jsonObject.get("linkTo").toString();
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

            //PARSE FEE
            int feePow;
            try {
                feePow = Integer.parseInt(feePowStr);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_FEE_POWER);
            }

            //PARSE OPTIONS
            List<String> options = new ArrayList<String>();
            try {
                for (int i = 0; i < optionsJSON.size(); i++) {
                    String option = (String) optionsJSON.get(i);
                    options.add(option);
                }
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }

            //CHECK CREATOR
            if (!Crypto.getInstance().isValidAddress(creator)) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
            }

            Controller controller = Controller.getInstance();
            //CHECK IF WALLET EXISTS
            if (!controller.doesWalletExists()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
            }

            //CHECK WALLET UNLOCKED
            if (!controller.isWalletUnlocked()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
            }

            //GET ACCOUNT
            PrivateKeyAccount account = controller.getWalletPrivateKeyAccountByAddress(creator);
            if (account == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.CREATOR_NOT_OWNER);
            }

            //CREATE POLL
            IssuePollRecord issue_voiting = (IssuePollRecord) controller.issuePoll(null, account, linkTo, name, description, options, null, null, feePow);

            //VALIDATE AND PROCESS
            int validate = controller.getTransactionCreator().afterCreate(issue_voiting, Transaction.FOR_NETWORK, false, false);
            if (validate == Transaction.VALIDATE_OK)
                return "ok";
        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            LOGGER.error(e.getMessage());
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }
        return "ok";
    }

    /**
     * Create new poll.
     * <br>
     * <h3>example request:</h3>
     * ApiPoll/CreatePoll?poll={"creator":"", "name":"", "description":"", "options":["1","2","3"], "feePow":""}
     *
     * @param poll is poll in json format
     * @return record trades
     * @author Ruslan
     * <h3>  example param poll : {"creator":"", "name":"", "description":"", "options":["1","2","3"], "feePow":""}</h3>
     */

    @GET
    @Path("issue")
    public Response issuePollGet(@QueryParam("poll") String poll) {

        String password = null;
        APIUtils.askAPICallAllowed(password, "GET polls " + poll, request, true);

        Gson result = new Gson();
        try {
            //READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(poll);
            String creator = (String) jsonObject.get("creator");
            String name = (String) jsonObject.get("name");
            String description = (String) jsonObject.get("description");
            JSONArray optionsJSON = (JSONArray) jsonObject.get("options");
            String feePowStr = (String) jsonObject.get("feePow");

            String linkToRefStr = jsonObject.get("linkTo").toString();
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

            //PARSE FEE
            int feePow;
            try {
                feePow = Integer.parseInt(feePowStr);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_FEE_POWER);
            }

            //PARSE OPTIONS
            List<String> options = new ArrayList<String>();
            try {
                for (int i = 0; i < optionsJSON.size(); i++) {
                    String option = (String) optionsJSON.get(i);
                    options.add(option);
                }
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
            }

            //CHECK CREATOR
            if (!Crypto.getInstance().isValidAddress(creator))
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);

            //CHECK IF WALLET EXISTS
            if (!Controller.getInstance().doesWalletExists())
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);

            //GET ACCOUNT
            PrivateKeyAccount account = Controller.getInstance().getWalletPrivateKeyAccountByAddress(creator);
            if (account == null)
                throw ApiErrorFactory.getInstance().createError(Transaction.CREATOR_NOT_OWNER);

            //CREATE POLL
            Controller.getInstance().issuePoll(null, account, linkTo, name, description, options, null, null, feePow);

        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);

        }
        result.toJson("ok");
        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(result).build();
    }

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
                creator, password, "issue Poll");

        PollCls item;
        try {
            item = PollFactory.getInstance().parse(Transaction.FOR_NETWORK, resultRaw.b, false);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(
                    e.getMessage());
        }

        Transaction transaction = cntr.issuePoll(resultRaw.a, linkTo, feePow, item);
        int validate = cntr.getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);

        if (validate == Transaction.VALIDATE_OK)
            return transaction.toJson().toJSONString();
        else {
            JSONObject out = new JSONObject();
            Transaction.updateMapByErrorSimple(validate, out);
            return out.toJSONString();
        }
    }

    @POST
    @Path("/vote/{key}")
    @Consumes(MediaType.WILDCARD)
    public Response createPollVote(String x, @PathParam("key") Long key) {

        String password = null;
        APIUtils.askAPICallAllowed(password, "POST polls/vote/" + key + "\n" + x, request, true);

        try {
            //READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            String voter = (String) jsonObject.get("voter");
            Integer option = (Integer) jsonObject.get("option");
            String feePowStr = (String) jsonObject.get("feePow");

            //PARSE FEE
            int feePow;
            try {
                feePow = Integer.parseInt(feePowStr);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(
                        Transaction.INVALID_FEE_POWER);
            }

            //CHECK VOTER
            if (!Crypto.getInstance().isValidAddress(voter)) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
            }

            //CHECK IF WALLET EXISTS
            if (!Controller.getInstance().doesWalletExists()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
            }

            //CHECK WALLET UNLOCKED
            if (!Controller.getInstance().isWalletUnlocked()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
            }

            //GET ACCOUNT
            PrivateKeyAccount account = Controller.getInstance().getWalletPrivateKeyAccountByAddress(voter);
            if (account == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.CREATOR_NOT_OWNER);
            }

            //GET POLL
            PollCls poll = Controller.getInstance().getPoll(key);
            if (poll == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.POLL_NOT_EXISTS);
            }

            //GET OPTION
            String pollOption = poll.getOptions().get(option);
            if (pollOption == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.POLL_OPTION_NOT_EXISTS);
            }

            //CREATE POLL
            Transaction transaction = Controller.getInstance().createItemPollVote(account, key, option, feePow);

            int result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);

            if (result == Transaction.VALIDATE_OK) {

                return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(transaction.toJson().toJSONString()).build();
            } else
                throw ApiErrorFactory.getInstance().createError(result);

        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            LOGGER.error(e.getMessage());
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }
    }

    /**
     * @param voter
     * @param feePow
     * @param pollKey
     * @param option
     * @return
     */
    @GET
    @Path("vote/{key}/{option}/{voter}")
    public Response createPollVoteGet(@PathParam("key") long pollKey, @PathParam("option") int option,
                                      @PathParam("voter") String voter,
                                      @QueryParam("feePow") Integer feePow
    ) {

        String password = null;
        APIUtils.askAPICallAllowed(password, "GET polls/vote/" + pollKey + "\n", request, true);

        //CHECK VOTERa
        if (!Crypto.getInstance().isValidAddress(voter))
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists())
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);

        //GET ACCOUNT
        PrivateKeyAccount account = Controller.getInstance().getWalletPrivateKeyAccountByAddress(voter);
        if (account == null)
            throw ApiErrorFactory.getInstance().createError(Transaction.CREATOR_NOT_OWNER);

        //GET POLL
        PollCls poll = Controller.getInstance().getPoll(pollKey);
        if (poll == null)
            throw ApiErrorFactory.getInstance().createError(Transaction.POLL_NOT_EXISTS);

        //GET OPTION
        String pollOption = poll.getOptions().get(option);
        if (pollOption == null)
            throw ApiErrorFactory.getInstance().createError(Transaction.POLL_OPTION_NOT_EXISTS);

        //CREATE POLL
        Transaction transaction = Controller.getInstance().createItemPollVote(account, pollKey, option, feePow);
        int result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK, false, false);

        if (result == Transaction.VALIDATE_OK) {

            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(transaction.toJson().toJSONString()).build();
        } else
            throw ApiErrorFactory.getInstance().createError(result);

    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/address/{address}")
    public String getPolls(@PathParam("address") String address) {

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        //CHECK ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        //CHECK ACCOUNT IN WALLET
        Account account = Controller.getInstance().getWalletAccountByAddress(address);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
        }

        JSONArray array = new JSONArray();
        for (ItemCls poll : Controller.getInstance().getAllItems(ItemCls.POLL_TYPE, account)) {
            array.add(((PollCls) poll).toJson());
        }

        return array.toJSONString();
    }

}
