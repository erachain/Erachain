package api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import controller.Controller;
import lang.Lang;
import settings.Settings;
import utils.APIUtils;
import utils.Chain;

@Path("util")
@Produces(MediaType.APPLICATION_JSON)
public class UtilResource 
{
	@Context
	HttpServletRequest request;

	@GET 
	@Path("/hw_test")
	public String hw_Test() 
	{ 
		return String.valueOf(Chain.hw_Test(false));
	}

}
