package org.erachain.webserver;

//import com.google.common.collect.Iterables;
//import com.google.gson.internal.LinkedHashTreeMap;
//import com.sun.org.apache.xpath.internal.operations.Or;
//import javafx.print.Collation;

import org.erachain.api.AssetsResource;
import org.erachain.controller.Controller;
import org.erachain.core.web.ServletUtils;
import org.erachain.datachain.DCSet;
import org.erachain.utils.StrJSonFine;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

//import com.google.gson.Gson;
//import org.mapdb.Fun;

@Path("apiasset")
@Produces(MediaType.APPLICATION_JSON)
public class APIAsset {

    @Context
    HttpServletRequest request;

    private DCSet dcSet = DCSet.getInstance();
    private Controller cntrl = Controller.getInstance();

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("GET apiasset/balances/[assetKey]?position=POS&offset=OFFSET&limit=LIMIT",
                "Get balances by assetKey. Balance positions: 1 - Owner, 2 - Credit, 3 - Hold, 4 - Spend, 5 - Other. Default: POS=1. Balance A - total debit. Balance B - final result.");

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }

    @GET
    @Path("balances/{key}")
    public Response getBalances(@PathParam("key") Long assetKey, @DefaultValue("0") @QueryParam("offset") Integer offset,
                                @DefaultValue("1") @QueryParam("position") Integer position,
                                @DefaultValue("50") @QueryParam("limit") Integer limit) {

        if (ServletUtils.isRemoteRequest(request, ServletUtils.getRemoteAddress(request))) {
            if (limit > 200)
                limit = 200;
        }

        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(AssetsResource.getBalances(assetKey, offset, position, limit))
                .build();
    }

}