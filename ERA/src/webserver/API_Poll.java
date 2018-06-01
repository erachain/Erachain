package webserver;

import api.ApiErrorFactory;
import com.google.gson.Gson;
import controller.Controller;
import core.account.PrivateKeyAccount;
import core.crypto.Crypto;
import core.transaction.CreatePollTransaction;
import core.transaction.Transaction;
import core.voting.Poll;
import core.voting.PollOption;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import utils.APIUtils;
import utils.Pair;
import utils.StrJSonFine;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("apipoll")
@Produces(MediaType.APPLICATION_JSON)
public class API_Poll {

    @Context
    HttpServletRequest request;

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("apipoll/CreatePoll?poll={poll}", "Create new poll.");
        help.put("apipoll/CreatePoll?poll={poll}", "Create new poll.");
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
     * @apiNote example param poll : {"creator":"", "name":"", "description":"", "options":["1","2","3"], "feePow":""}
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
            CreatePollTransaction issue_voiting =
                (CreatePollTransaction) Controller.getInstance().createPoll_old(account, name, description, options, feePow);


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
     * Vote to
     * @param data data vote
     * @param name
     * @return
     */
    @GET
    @Path("vote")
    @Consumes(MediaType.WILDCARD)
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
}