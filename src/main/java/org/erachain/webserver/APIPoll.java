package org.erachain.webserver;

import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Poll class (Create, vote by poll, get poll, )
 */
@Path("apipoll")
@Produces(MediaType.APPLICATION_JSON)
public class APIPoll {

    @Context
    HttpServletRequest request;

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("apipoll/allPoll", "Get all poll.");
        help.put("apipoll/getPoll", "Get single poll by name.");
        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }

    /**
     * Get all poll
     *
     * @return all poll in JSON
     */
    @GET
    @Path("allPoll")
    public Response getAllPolls() {
        Collection<ItemCls> polls = Controller.getInstance().getAllItems(ItemCls.POLL_TYPE);
        JSONArray array = new JSONArray();

        for (ItemCls poll : polls) {
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
        List<ItemCls> listPolls = DCSet.getInstance().getItemPollMap().findByName(name, false);

        //CHECK IF NAME EXISTS
        if (listPolls == null || listPolls.isEmpty()) {
            throw ApiErrorFactory.getInstance().createError(Transaction.POLL_NOT_EXISTS);
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(listPolls.get(0).toJson()).build();
    }
}