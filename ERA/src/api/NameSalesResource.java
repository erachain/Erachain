package api;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.Crypto;
import core.naming.Name;
import core.naming.NameSale;
import core.transaction.Transaction;
import utils.APIUtils;
import utils.Pair;

@Path("namesales")
@Produces(MediaType.APPLICATION_JSON)
public class NameSalesResource
{
	@Context
	HttpServletRequest request;

	@SuppressWarnings("unchecked")
	@GET
	public String getNameSales()
	{
		String password = null;
		APIUtils.askAPICallAllowed(password, "GET namesales", request);

		//CHECK IF WALLET EXISTS
		if(!Controller.getInstance().doesWalletExists())
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}

		List<Pair<Account, NameSale>> nameSales = Controller.getInstance().getNameSales();
		JSONArray array = new JSONArray();

		for(Pair<Account, NameSale> nameSale: nameSales)
		{
			array.add(nameSale.getB().toJson());
		}

		return array.toJSONString();
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/address/{address}")
	public String getNameSales(@PathParam("address") String address)
	{
		String password = null;
		APIUtils.askAPICallAllowed(password, "GET namesales/address/" + address, request);

		//CHECK IF WALLET EXISTS
		if(!Controller.getInstance().doesWalletExists())
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}

		//CHECK ADDRESS
		if(!Crypto.getInstance().isValidAddress(address))
		{
			throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
		}

		//CHECK ACCOUNT IN WALLET
		Account account = Controller.getInstance().getAccountByAddress(address);
		if(account == null)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
		}

		JSONArray array = new JSONArray();
		for(NameSale nameSale: Controller.getInstance().getNameSales(account))
		{
			array.add(nameSale.toJson());
		}

		return array.toJSONString();
	}

	@GET
	@Path("/{name}")
	public static String getNameSale(@PathParam("name") String nameName)
	{
		NameSale nameSale = Controller.getInstance().getNameSale(nameName);

		//CHECK IF NAME SALE EXISTS
		if(nameSale == null)
		{
			throw ApiErrorFactory.getInstance().createError(Transaction.NAME_DOES_NOT_EXIST);
		}

		return nameSale.toJson().toJSONString();
	}

	@SuppressWarnings("unchecked")
	@Path("/network")
	@GET
	public String getAllNameSales()
	{
		List<NameSale> nameSales = Controller.getInstance().getAllNameSales();
		JSONArray array = new JSONArray();

		for(NameSale nameSale: nameSales)
		{
			array.add(nameSale.getKey());
		}

		return array.toJSONString();
	}

	@POST
	@Path("/{name}")
	@Consumes(MediaType.WILDCARD)
	public String createNameSale(String x, @PathParam("name") String nameName)
	{
		try
		{
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String amount = (String) jsonObject.get("amount");
			String feePowStr = (String) jsonObject.get("feePow");

			//PARSE AMOUNT
			BigDecimal bdAmount;
			try
			{
				bdAmount = new BigDecimal(amount);
				bdAmount = bdAmount.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
			}
			catch(Exception e)
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_AMOUNT);
			}


			//PARSE FEE
			int feePow=0;
			try
			{
				feePow = Integer.parseInt(feePowStr);
			}
			catch(Exception e)
			{
			}

			String password = null;
			APIUtils.askAPICallAllowed(password, "POST namesales/" + nameName + "\n"+x, request);

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

			//GET NAME
			Name name = Controller.getInstance().getName(nameName);
			if(name == null)
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.NAME_DOES_NOT_EXIST);
			}

			//GET OWNER
			PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(name.getOwner().getAddress());
			if(account == null)
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.CREATOR_NOT_OWNER);
			}

			//CREATE NAME SALE
			Pair<Transaction, Integer> result = Controller.getInstance().sellName(account, nameName, bdAmount, feePow);

			if (result.getB() == Transaction.VALIDATE_OK)
				return result.getA().toJson().toJSONString();
			else
				throw ApiErrorFactory.getInstance().createError(result.getB());

		}
		catch(NullPointerException e)
		{
			//JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
		catch(ClassCastException e)
		{
			//JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}

	@DELETE
	@Path("/{name}/{fee}")
	@Consumes(MediaType.WILDCARD)
	public String cancelNameSale(@PathParam("name") String nameName, @PathParam("feePow") String feePowStr)
	{
		try
		{
			//PARSE FEE
			int feePow = 0;
			try
			{
				feePow = Integer.parseInt(feePowStr);
			}
			catch(Exception e)
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_FEE_POWER);
			}

			NameSale nameSale = Controller.getInstance().getNameSale(nameName);

			String password = null;
			APIUtils.askAPICallAllowed(password, "DELETE namesales/"+nameName+"/"+ feePow, request );

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

			//CHECK IF NAME SALE EXISTS
			if(nameSale == null)
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.NAME_DOES_NOT_EXIST);
			}

			//GET OWNER
			PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(nameSale.getName().getOwner().getAddress());
			if(account == null)
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.CREATOR_NOT_OWNER);
			}

			//CREATE NAME SALE
			Pair<Transaction, Integer> result = Controller.getInstance().cancelSellName(account, nameSale, feePow);

			if (result.getB() == Transaction.VALIDATE_OK)
				return result.getA().toJson().toJSONString();
			else
				throw ApiErrorFactory.getInstance().createError(result.getB());

		}
		catch(NullPointerException e)
		{
			//JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
		catch(ClassCastException e)
		{
			//JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}

	@POST
	@Path("/buy/{name}")
	@Consumes(MediaType.WILDCARD)
	public String createNamePurchase(String x, @PathParam("name") String nameName)
	{
		try
		{
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String buyer = (String) jsonObject.get("buyer");
			String feePowStr = (String) jsonObject.get("feePow");

			//PARSE FEE
			int feePow;
			try
			{
				feePow = Integer.parseInt(feePowStr);
			}
			catch(Exception e)
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_FEE_POWER);
			}

			String password = null;
			APIUtils.askAPICallAllowed(password, "POST namesales/buy/" + nameName + "\n" + x, request);

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

			NameSale nameSale = Controller.getInstance().getNameSale(nameName);

			//CHECK IF NAME SALE EXISTS
			if(nameSale == null)
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.NAME_DOES_NOT_EXIST);
			}

			//CHECK ADDRESS
			if(!Crypto.getInstance().isValidAddress(buyer))
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
			}

			//GET BUYER
			PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(buyer);
			if(account == null)
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.CREATOR_NOT_OWNER);
			}

			//CREATE NAME SALE
			Pair<Transaction, Integer> result = Controller.getInstance().BuyName(account, nameSale, feePow);

			if (result.getB() == Transaction.VALIDATE_OK)
				return result.getA().toJson().toJSONString();
			else
				throw ApiErrorFactory.getInstance().createError(result.getB());

		}
		catch(NullPointerException e)
		{
			//JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
		catch(ClassCastException e)
		{
			//JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}
}
