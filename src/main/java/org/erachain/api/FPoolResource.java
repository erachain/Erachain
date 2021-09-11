package org.erachain.api;


import org.erachain.controller.Controller;
import org.erachain.controller.FPool;
import org.json.simple.JSONObject;
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

@Path("fpool")
@Produces(MediaType.APPLICATION_JSON)
public class FPoolResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(FPoolResource.class);

    private static final Controller contr = Controller.getInstance();
    @Context
    HttpServletRequest request;

    @GET
    public String getInfo() {
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
    @Path("pending")
    public String getPending() {

        JSONObject out = new JSONObject();

        FPool fpool = contr.fPool;
        if (fpool == null) {
            out.put("status", "off");
            return out.toJSONString();
        }

        Object[][] pending = fpool.getPending();
        for (Object[] block : pending) {
            out.put(block[0], block[1]);
        }

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
