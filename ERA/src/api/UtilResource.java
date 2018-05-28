package api;

import utils.Chain;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("util")
@Produces(MediaType.APPLICATION_JSON)
public class UtilResource {
    @Context
    HttpServletRequest request;

    @GET
    @Path("/hw_test")
    public String hw_Test() {
        return String.valueOf(Chain.hw_Test());
    }

}
