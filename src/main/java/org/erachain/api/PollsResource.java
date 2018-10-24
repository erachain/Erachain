package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.IssuePollRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.voting.Poll;
import org.erachain.core.voting.PollOption;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Path("polls")
@Produces(MediaType.APPLICATION_JSON)
public class PollsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollsResource.class);

    @Context
    HttpServletRequest request;

    @POST
    @Consumes(MediaType.WILDCARD)
    public String createPoll(String x) {
        try {
            //READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
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

            String password = null;
            APIUtils.askAPICallAllowed(password, "POST polls " + x, request);
            Controller controller = new Controller().getInstance();
            //CHECK IF WALLET EXISTS
            if (!controller.doesWalletExists()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
            }

            //CHECK WALLET UNLOCKED
            if (!controller.isWalletUnlocked()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
            }

            //GET ACCOUNT
            PrivateKeyAccount account = controller.getPrivateKeyAccountByAddress(creator);
            if (account == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.CREATOR_NOT_OWNER);
            }

            //CREATE POLL
            IssuePollRecord issue_voiting = (IssuePollRecord) controller.createPoll_old(account, name, description, options, feePow);

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

    @POST
    @Path("/vote/{name}")
    @Consumes(MediaType.WILDCARD)
    public String createPollVote(String x, @PathParam("name") String name) {
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

            String password = null;
            APIUtils.askAPICallAllowed(password, "POST polls/vote/" + name + "\n" + x, request);

            //CHECK IF WALLET EXISTS
            if (!Controller.getInstance().doesWalletExists()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
            }

            //CHECK WALLET UNLOCKED
            if (!Controller.getInstance().isWalletUnlocked()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
            }

            //GET ACCOUNT
            PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(voter);
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

    @SuppressWarnings("unchecked")
    @GET
    public String getPolls() {
        String password = null;
        APIUtils.askAPICallAllowed(password, "GET polls", request);

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        List<Pair<Account, Poll>> polls = Controller.getInstance().getPolls();
        JSONArray array = new JSONArray();

        for (Pair<Account, Poll> poll : polls) {
            array.add(poll.getB().toJson());
        }

        return array.toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/address/{address}")
    public String getPolls(@PathParam("address") String address) {
        String password = null;
        APIUtils.askAPICallAllowed(password, "GET polls/address/" + address, request);

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        //CHECK ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        //CHECK ACCOUNT IN WALLET
        Account account = Controller.getInstance().getAccountByAddress(address);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
        }

        JSONArray array = new JSONArray();
        for (Poll poll : Controller.getInstance().getPolls(account)) {
            array.add(poll.toJson());
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

    @SuppressWarnings("unchecked")
    @GET
    @Path("/network")
    public String getAllPolls() {
        Collection<Poll> polls = Controller.getInstance().getAllPolls();
        JSONArray array = new JSONArray();

        for (Poll poll : polls) {
            array.add(poll.getName());
        }

        return array.toJSONString();
    }
}
