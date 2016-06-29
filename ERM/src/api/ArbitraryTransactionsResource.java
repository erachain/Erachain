package api;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import controller.Controller;
import core.account.PrivateKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.payment.Payment;
import core.transaction.Transaction;
import utils.APIUtils;
import utils.Pair;

@Path("arbitrarytransactions")
@Produces(MediaType.APPLICATION_JSON)
public class ArbitraryTransactionsResource 
{
	
	private static final Logger LOGGER = Logger
			.getLogger(ArbitraryTransactionsResource.class);
	@Context
	HttpServletRequest request;
	
	@POST
	@Consumes(MediaType.WILDCARD)
	public String createArbitraryTransaction(String x)
	{
		try
		{
			String password = null;
			APIUtils.askAPICallAllowed(password, "POST arbitrarytransactions\n" + x, request );

			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			int service = ((Long) jsonObject.get("service")).intValue();
			String data = (String) jsonObject.get("data");
			String feePowStr = (String) jsonObject.get("feePow");
			String creator = (String) jsonObject.get("creator");
			
			long lgAsset = 0L;
			if(jsonObject.containsKey("asset")) {
				lgAsset = ((Long) jsonObject.get("asset")).intValue();
			}
			
			AssetCls defaultAsset;

			try {
				defaultAsset = Controller.getInstance().getAsset(new Long(lgAsset));
			} catch (Exception e) {
				throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_ASSET_ID);
			}
			
			List<Payment> payments = MultiPaymentResource.jsonPaymentParser((JSONArray)jsonObject.get("payments"), defaultAsset);
			
			//PARSE DATA
			byte[] dataBytes;
			try
			{
				dataBytes = Base58.decode(data);
			}
			catch(Exception e)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_DATA);
			}
				
			//PARSE FEE
			
			int feePow = 0;
			if(feePowStr != null) {
				try
				{
					feePow = Integer.parseInt(feePowStr);
				}
				catch(Exception e)
				{
					throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_FEE);
				}	
			}
			
			//CHECK ADDRESS
			if(!Crypto.getInstance().isValidAddress(creator))
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}
				
			//CHECK IF WALLET EXISTS
			if(!Controller.getInstance().doesWalletExists())
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
			}
			
			
			
			//CHECK WALLET UNLOCKED
			if(!Controller.getInstance().isWalletUnlocked())
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
			}
				
			//GET ACCOUNT
			PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(creator);				
			if(account == null)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}
				
			//SEND PAYMENT
			Pair<Transaction, Integer> result = Controller.getInstance().createArbitraryTransaction(account, payments, service, dataBytes, feePow);
				
			return checkArbitraryTransaction(result);
		}
		catch(NullPointerException | ClassCastException e)
		{
			//JSON EXCEPTION
			LOGGER.info(e);
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}

	public static String checkArbitraryTransaction(Pair<Transaction, Integer> result) {
		switch(result.getB())
		{
		case Transaction.VALIDATE_OK:
			
			return result.getA().toJson().toJSONString();
			
		case Transaction.NOT_YET_RELEASED:
			
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NOT_YET_RELEASED);			
		
		case Transaction.INVALID_DATA_LENGTH:
			
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_DATA_LENGTH);	

		case Transaction.NOT_ENOUGH_FEE:
				
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NO_BALANCE);
							
		case Transaction.NO_BALANCE:	
				
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_NO_BALANCE);
		
		case Transaction.NEGATIVE_AMOUNT:	
		case Transaction.INVALID_AMOUNT:
			
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_AMOUNT);
			
		default:
			
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_UNKNOWN);	
		}
	}
}
