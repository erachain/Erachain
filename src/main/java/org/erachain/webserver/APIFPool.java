package org.erachain.webserver;

import org.erachain.api.ApiErrorFactory;
import org.erachain.api.FPoolResource;
import org.erachain.controller.Controller;
import org.erachain.controller.FPool;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;


@Path("apifpool")
@Produces(MediaType.APPLICATION_JSON)

public class APIFPool {

    @Context
    HttpServletRequest request;

    private DCSet dcSet = DCSet.getInstance();
    private Controller cntrl = Controller.getInstance();

    @GET
    public Response Default() {
        Map<String, String> help = new LinkedHashMap<>();

        help.put("GET apifpool/info", "Get fpoolinfo");
        help.put("GET apifpool/balance/{address}", "Get balances for address");
        help.put("GET apifpool/pending/blocks", "Get pending blocks");


        return Response.status(200).header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*").entity(StrJSonFine.convert(help)).build();
    }

    @GET
    @Path("info")
    public Response info() {

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(FPoolResource.getInfo())
                .build();

    }

    @GET
    @Path("balance/{address}")
    public Response balance(@PathParam("address") String address) {

        Fun.Tuple2<Account, String> resultAcc = Account.tryMakeAccount(address);
        if (resultAcc.a == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        JSONObject out = new JSONObject();
        FPool fpool = Controller.getInstance().fPool;

        if (fpool == null) {
            out.put("status", "off");
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(out.toJSONString())
                    .build();
        }

        out.put("balances", fpool.getAddressBalances(address));

        JSONObject pendingBlocks = new JSONObject();
        for (Object[] block : fpool.getPendingBlocks()) {
            pendingBlocks.put(block[0], block[1]);
        }
        out.put("pending", pendingBlocks);

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();

    }

    @GET
    @Path("pending/blocks")
    public Response getPendingBlocks() {

        JSONObject out = new JSONObject();

        FPool fpool = cntrl.fPool;
        if (fpool == null) {
            out.put("status", "off");
            return Response.status(200)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(out.toJSONString())
                    .build();
        }

        for (Object[] block : fpool.getPendingBlocks()) {
            out.put(block[0], block[1]);
        }

        return Response.status(200)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Access-Control-Allow-Origin", "*")
                .entity(out.toJSONString())
                .build();
    }

}