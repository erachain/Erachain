package org.erachain.api;

import com.google.gson.Gson;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.transaction.IssuePollRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.voting.Poll;
import org.erachain.core.voting.PollOption;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Path("polls")
@Produces(MediaType.APPLICATION_JSON)
public class ItemPollsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemPollsResource.class);

    @Context
    HttpServletRequest request;

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
    public String createPoll(String poll) {

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
            IssuePollRecord issue_voiting = (IssuePollRecord) controller.issuePoll(account, name, description, options, null, null, feePow);

            //VALIDATE AND PROCESS
            int validate = controller.getTransactionCreator().afterCreate(issue_voiting, Transaction.FOR_NETWORK);
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
    @Path("create")
    public Response createPollGet(@QueryParam("poll") String poll) {

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
            Controller.getInstance().issuePoll(account, name, description, options, null, null, feePow);

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
    @Path("/vote/{name}")
    @Consumes(MediaType.WILDCARD)
    public String createPollVote(String x, @PathParam("name") String name) {

        String password = null;
        APIUtils.askAPICallAllowed(password, "POST polls/vote/" + name + "\n" + x, request, true);

        try {
            //READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            String voter = (String) jsonObject.get("voter");
            String option = (String) jsonObject.get("option");
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
            Poll poll = Controller.getInstance().getPoll(name);
            if (poll == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.POLL_NOT_EXISTS);
            }

            //GET OPTION
            PollOption pollOption = poll.getOption(option);
            if (pollOption == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.POLL_OPTION_NOT_EXISTS);
            }

            //CREATE POLL
            Pair<Transaction, Integer> result = Controller.getInstance().createPollVote(account, poll, pollOption, feePow);

            if (result.getB() == Transaction.VALIDATE_OK)
                return result.getA().toJson().toJSONString();
            else
                throw ApiErrorFactory.getInstance().createError(result.getB());

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
    @Path("vote/{voter}")
    public Response createPollVoteGet(@PathParam("voter") String voter, @QueryParam("feePow") Integer feePow,
                                      @QueryParam("poll") long pollKey, @QueryParam("option") int option) {

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
        int result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

        if (result == Transaction.VALIDATE_OK) {

            return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(transaction.toJson().toJSONString()).build();
        } else
            throw ApiErrorFactory.getInstance().createError(result);

    }

    @SuppressWarnings("unchecked")
    @GET
    public String getPolls() {

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        Collection<ItemCls> polls = Controller.getInstance().getAllItems(ItemCls.POLL_TYPE);
        JSONArray array = new JSONArray();

        for (ItemCls poll : polls) {
            array.add(((PollCls) poll).toJson());
        }

        return array.toJSONString();
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


    @GET
    @Path("/{name}")
    public String getPoll(@PathParam("name") String name) {
        Poll poll = Controller.getInstance().getPoll(name);

        //CHECK IF NAME EXISTS
        if (poll == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.POLL_NOT_EXISTS);
        }

        return poll.toJson().toJSONString();
    }

}
