package org.erachain.api;
// 30/03

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class RPCResource {
    @Context
    HttpServletRequest request;

    @SuppressWarnings("unchecked")
    @GET
    public String getHelp() {
        String help = "";
        for (String[] strings : ApiClient.helpStrings) {
            help += strings[0] + "\n\t" + strings[1] + "\n\n";
        }
        return help;
    }

}
