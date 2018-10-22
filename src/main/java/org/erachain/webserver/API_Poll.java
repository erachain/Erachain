package org.erachain.webserver;

import org.erachain.api.ApiErrorFactory;
import com.google.gson.Gson;
import org.erachain.controller.Controller;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.voting.Poll;
import org.erachain.core.voting.PollOption;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.erachain.utils.Pair;
import org.erachain.utils.StrJSonFine;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Poll class (Create, vote by poll, get poll, )
 */
@Path("apipoll")
@Produces(MediaType.APPLICATION_JSON)
public class API_Poll {

    @Context
    HttpServletRequest request;

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("apipoll/CreatePoll?poll={poll}", "Create new poll.");
        help.put("apipoll/createPollVote?data={data}&name={name}", "Vote to poll.");
        help.put("apipoll/allPoll", "Get all poll.");
        help.put("apipoll/getPoll", "Get single poll by name.");
        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
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
    @Path("CreatePoll")
    public Response createPoll(@QueryParam("poll") String poll) {
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
            PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(creator);
            if (account == null)
                throw ApiErrorFactory.getInstance().createError(Transaction.CREATOR_NOT_OWNER);

            //CREATE POLL
            Controller.getInstance().createPoll_old(account, name, description, options, feePow);

        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);

        }
        result.toJson("ok");
        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(result).build();
    }

    /**
     * Vote to poll
     *
     * @param data data vote
     * @param name is name poll
     * @return polling creation status
     */
    @GET
    @Path("vote")
    public Response createPollVote(@QueryParam("data") String data, @QueryParam("name") String name) {
        try {
            //READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(data);
            String voter = (String) jsonObject.get("voter");
            String option = (String) jsonObject.get("option");
            String feePowStr = (String) jsonObject.get("feePow");

            //PARSE FEE
            int feePow;
            try {
                feePow = Integer.parseInt(feePowStr);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_FEE_POWER);
            }

            //CHECK VOTERa
            if (!Crypto.getInstance().isValidAddress(voter))
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);

            //CHECK IF WALLET EXISTS
            if (!Controller.getInstance().doesWalletExists())
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);

            //GET ACCOUNT
            PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(voter);
            if (account == null)
                throw ApiErrorFactory.getInstance().createError(Transaction.CREATOR_NOT_OWNER);

            //GET POLL
            Poll poll = Controller.getInstance().getPoll(name);
            if (poll == null)
                throw ApiErrorFactory.getInstance().createError(Transaction.POLL_NOT_EXISTS);

            //GET OPTION
            PollOption pollOption = poll.getOption(option);
            if (pollOption == null)
                throw ApiErrorFactory.getInstance().createError(Transaction.POLL_OPTION_NOT_EXISTS);

            //CREATE POLL
            Pair<Transaction, Integer> result = Controller.getInstance().createPollVote(account, poll, pollOption, feePow);

            if (result.getB() == Transaction.VALIDATE_OK) {

                return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(result.getA().toJson().toJSONString()).build();
            } else
                throw ApiErrorFactory.getInstance().createError(result.getB());

        } catch (NullPointerException | ClassCastException e) {
            //JSON EXCEPTION
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }
    }

    /**
     * Get all poll
     *
     * @return all poll in JSON
     */
    @GET
    @Path("allPoll")
    public Response getAllPolls() {
        Collection<Poll> polls = Controller.getInstance().getAllPolls();
        JSONArray array = new JSONArray();

        for (Poll poll : polls) {
            array.add(poll.getName());
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(array.toJSONString()).build();
    }

    /**
     * Find poll by name
     *
     * @param name is name poll
     * @return poll in JSON
     */
    @GET
    @Path("getPoll")
    public Response getPoll(@QueryParam("name") String name) {
        Poll poll = Controller.getInstance().getPoll(name);

        //CHECK IF NAME EXISTS
        if (poll == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.POLL_NOT_EXISTS);
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(poll.toJson()).build();
    }
}