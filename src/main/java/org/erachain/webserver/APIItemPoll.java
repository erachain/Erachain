package org.erachain.webserver;

import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemPollMap;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Poll class (Create, vote by poll, get poll, )
 */
@Path("apipoll")
@Produces(MediaType.APPLICATION_JSON)
public class APIItemPoll {

    @Context
    HttpServletRequest request;

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("apipoll/allPoll", "Get all poll.");
        help.put("apipoll/getPoll", "Get single poll by name.");

        help.put("Get apipoll/image/{key}", "GET Poll Image");
        help.put("Get apipoll/icon/{key}", "GET Poll Icon");

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
        List<ItemCls> listPolls = DCSet.getInstance().getItemPollMap().getByFilterAsArray(name, 0, 100);

        //CHECK IF NAME EXISTS
        if (listPolls == null || listPolls.isEmpty()) {
            throw ApiErrorFactory.getInstance().createError(Transaction.POLL_NOT_EXISTS);
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(listPolls.get(0).toJson().toJSONString()).build();
    }

    @Path("image/{key}")
    @GET
    @Produces({"image/png", "image/jpg"})
    public Response pollImage(@PathParam("key") long key) throws IOException {

        int weight = 0;
        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Error key");
        }

        ItemPollMap map = DCSet.getInstance().getItemPollMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_POLL_NOT_EXIST);
        }

        PollCls poll = (PollCls) map.get(key);

        if (poll.getImage() != null) {
            // image to byte[] hot scale (param2 =0)
            //	byte[] b = Images_Work.ImageToByte(new ImageIcon(person.getImage()).getImage(), 0);
            ///return Response.ok(new ByteArrayInputStream(poll.getImage())).build();
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ByteArrayInputStream(poll.getImage()))
                    .build();
        }
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity("")
                .build();

    }

    @Path("icon/{key}")
    @GET
    @Produces({"image/png", "image/jpg"})
    public Response pollIcon(@PathParam("key") long key) throws IOException {

        if (key <= 0) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Error key");
        }

        ItemPollMap map = DCSet.getInstance().getItemPollMap();
        // DOES EXIST
        if (!map.contains(key)) {
            throw ApiErrorFactory.getInstance().createError(
                    //ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    Transaction.ITEM_POLL_NOT_EXIST);
        }

        PollCls poll = (PollCls) map.get(key);

        if (poll.getIcon() != null) {
            // image to byte[] hot scale (param2 =0)
            //	byte[] b = Images_Work.ImageToByte(new ImageIcon(person.getImage()).getImage(), 0);
            //return Response.ok(new ByteArrayInputStream(poll.getIcon())).build();
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ByteArrayInputStream(poll.getIcon()))
                    .build();
        }
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity("")
                .build();
    }

}