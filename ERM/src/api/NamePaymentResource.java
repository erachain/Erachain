package api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import core.account.Account;
import utils.APIUtils;
import utils.NameUtils;
import utils.NameUtils.NameResult;
import utils.Pair;

@Path("namepayment")
@Produces(MediaType.APPLICATION_JSON)
public class NamePaymentResource {

	
	private static final Logger LOGGER = Logger
			.getLogger(NamePaymentResource.class);
	
	@Context
	HttpServletRequest request;
	
	@POST
	@Consumes(MediaType.WILDCARD)
	public String namePayment(String x)
	{
		try
		{				
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String assetKey = (String) jsonObject.get("assetKey");
			String amount = (String) jsonObject.get("amount");
			String feePow = (String) jsonObject.get("feePow");
			String sender = (String) jsonObject.get("sender");
			String nameName = (String) jsonObject.get("recipient");	
			
			Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(nameName);
			if(nameToAdress.getB() == NameResult.OK)
			{
				String recipient = nameToAdress.getA().getAddress();
				return APIUtils.processPayment(sender, feePow, recipient, assetKey, amount,  x, request);
			}else
			{
				return APIUtils.processPayment(sender, feePow, nameName, assetKey, amount,  x, request);
			}
		}
		catch(NullPointerException | ClassCastException e)
		{
			//JSON EXCEPTION
			LOGGER.info(e);
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
			
			
	}
	
	
}
