package org.erachain.api;


import org.erachain.controller.Controller;
import org.erachain.controller.FPool;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.TreeMap;

@Path("fpool")
@Produces(MediaType.APPLICATION_JSON)
public class FPoolResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(FPoolResource.class);

    private static final Controller contr = Controller.getInstance();
    @Context
    HttpServletRequest request;

    @GET
    public static String getInfo() {
        JSONObject out = new JSONObject();

        FPool fpool = contr.fPool;
        if (fpool == null) {
            out.put("status", "off");
            return out.toJSONString();
        }

        out.put("status", "on");
        out.put("tax", fpool.getTax().movePointRight(2));
        out.put("address", fpool.getAddress());
        out.put("pendingPeriod", fpool.getPendingPeriod());

        return out.toJSONString();
    }

    @GET
    @Path("pending/blocks")
    public String getPendingBlocks() {

        JSONObject out = new JSONObject();

        FPool fpool = contr.fPool;
        if (fpool == null) {
            out.put("status", "off");
            return out.toJSONString();
        }

        Object[][] pending = fpool.getPendingBlocks();
        for (Object[] block : pending) {
            out.put(block[0], block[1]);
        }

        return out.toJSONString();
    }

    @GET
    @Path("pending/withdraws")
    public String getPendingWithdraws() {

        JSONObject out = new JSONObject();

        FPool fpool = contr.fPool;
        if (fpool == null) {
            out.put("status", "off");
            return out.toJSONString();
        }

        TreeMap<Fun.Tuple2<Long, String>, BigDecimal> pending = fpool.getPendingWithdraws();
        for (Fun.Tuple2<Long, String> key : pending.keySet()) {
            if (!out.containsKey(key.a)) {
                out.put(key.a, new JSONObject());
            }

            ((JSONObject) out.get(key.a)).put(key.b, pending.get(key));
        }

        return out.toJSONString();
    }

    @GET
    @Path("withdraw/all")
    public String getWithdrawALL() {

        JSONObject out = new JSONObject();

        FPool fpool = contr.fPool;
        if (fpool == null) {
            out.put("status", "off");
            return out.toJSONString();
        }

        TreeMap<Fun.Tuple2<Long, String>, BigDecimal> pending = fpool.getPendingWithdraws();
        for (Fun.Tuple2<Long, String> key : pending.keySet()) {
            if (!out.containsKey(key.a)) {
                out.put(key.a, new JSONObject());
            }

            ((JSONObject) out.get(key.a)).put(key.b, pending.get(key));
        }

        do {

        } while (fpool.withdraw(true));

        return out.toJSONString();
    }

    @GET
    @Path("settax/{tax}")
    public String setTax(@PathParam("tax") BigDecimal tax) {
        FPool fpool = contr.fPool;
        if (fpool == null) {
            JSONObject out = new JSONObject();
            out.put("status", "off");
            return out.toJSONString();
        }

        fpool.setTax(tax.movePointLeft(2));

        return getInfo();
    }

}
