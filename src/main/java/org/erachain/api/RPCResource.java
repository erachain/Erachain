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
        for (String[] helpString : ApiClient.helpStrings) {
            help += helpString[0] + "\n";
            if (helpString.length > 1)
                help += "\t" + helpString[1] + "\n";
            if (helpString.length > 2)
                help += "\t" + helpString[2] + "\n";
            if (helpString.length > 3)
                help += "\t" + helpString[3] + "\n";
        }
        return help;
    }

}
