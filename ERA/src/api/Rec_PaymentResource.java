package api;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import utils.APIUtils;

@Path("rec_payment")
@Produces(MediaType.APPLICATION_JSON)
public class Rec_PaymentResource 
{
	
	private static final Logger LOGGER = Logger
			.getLogger(Rec_PaymentResource.class);
	@Context
	HttpServletRequest request;
	
	@GET
	//@Consumes(MediaType.WILDCARD)
	@Path("{feePow}/{sender}/{assetKey}/{amount}/{recipient}")
	public String createPaymentGet(
			@PathParam("feePow") String feePow,
			@PathParam("sender") String sender,
			@PathParam("assetKey") String assetKey,
			@PathParam("amount") String amount,
			@PathParam("recipient") String recipient,
			@QueryParam("password") String password
			)
	{
		try
		{

			// it check below APIUtils.askAPICallAllowed(password, "GET payment\n ", request);
		
			return APIUtils.processPayment(password, sender,  feePow, recipient, assetKey, amount, "", request );
		}
		catch(NullPointerException| ClassCastException e)
		{
			//JSON EXCEPTION
			LOGGER.info(e);
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}

	}
	@POST
	//@GET
	@Consumes(MediaType.WILDCARD)
	public String createPayment(String x)
	{

		try
		{
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String assetKey = "" + jsonObject.get("assetKey");
			String feePow = "" + jsonObject.get("feePow");
			String amount = "" + jsonObject.get("amount");
			String sender = (String) jsonObject.get("sender");
			String recipient = (String) jsonObject.get("recipient");
			String password = (String) jsonObject.get("password");

			password = null;
			APIUtils.askAPICallAllowed(password, "POST payment\n " + x, request);
		
			return APIUtils.processPayment(password, sender,  feePow, recipient, assetKey, amount, x, request );
		}
		catch(NullPointerException| ClassCastException e)
		{
			//JSON EXCEPTION
			LOGGER.info(e);
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}

	}

}
