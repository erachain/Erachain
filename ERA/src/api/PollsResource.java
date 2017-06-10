package api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import utils.APIUtils;
import utils.Pair;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.Crypto;
import core.transaction.CreatePollTransaction;
import core.transaction.Transaction;
import core.voting.Poll;
import core.voting.PollOption;
import gui.MainFrame;
import gui.library.Issue_Confirm_Dialog;
import gui.library.library;
import lang.Lang;

@Path("polls")
@Produces(MediaType.APPLICATION_JSON)
public class PollsResource 
{
	
	
	private static final Logger LOGGER = Logger.getLogger(PollsResource.class);
	
	@Context
	HttpServletRequest request;

	@POST
	@Consumes(MediaType.WILDCARD)
	public String createPoll(String x)
	{
		
		
		try
		{
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String creator = (String) jsonObject.get("creator");
			String name = (String) jsonObject.get("name");
			String description = (String) jsonObject.get("description");
			JSONArray optionsJSON = (JSONArray) jsonObject.get("options");
			String feePowStr = (String) jsonObject.get("feePow");
			
			//PARSE FEE
			int feePow;
			try
			{
				feePow = Integer.parseInt(feePowStr);
			}
			catch(Exception e)
			{
				throw ApiErrorFactory.getInstance().createError(
						Transaction.INVALID_FEE_POWER);
			}	
			
			//PARSE OPTIONS
			List<String> options = new ArrayList<String>();
			try
			{
				for(int i=0; i<optionsJSON.size(); i++)
				{
					String option = (String) optionsJSON.get(i);
					options.add(option);
				}
			}
			catch(Exception e)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
			}
				
			//CHECK CREATOR
			if(!Crypto.getInstance().isValidAddress(creator))
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
			}

			String password = null;
			APIUtils.askAPICallAllowed(password, "POST polls " + x, request);

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
				throw ApiErrorFactory.getInstance().createError(Transaction.CREATOR_NOT_OWNER);
			}
				
			//CREATE POLL
			CreatePollTransaction issue_voiting = (CreatePollTransaction) Controller.getInstance().createPoll(account, name, description, options, feePow);
				
			Poll poll = issue_voiting.getPoll();

			//Issue_Asset_Confirm_Dialog cont = new Issue_Asset_Confirm_Dialog(issueAssetTransaction);
			 String text = "<HTML><body>";
			 	text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"  + Lang.getInstance().translate("Issue Asset") + "<br><br><br>";
			    text += Lang.getInstance().translate("Creator") +":&nbsp;"  + issue_voiting.getCreator() +"<br>";
			    text += Lang.getInstance().translate("Name") +":&nbsp;"+ poll.getName() +"<br>";
			   text += "<br>"+Lang.getInstance().translate("Description")+":<br>"+ library.to_HTML(poll.getDescription())+"<br>";
			    text += "<br>"+ Lang.getInstance().translate("Options")+":<br>";
			    
			   
				
			    String Status_text = "<HTML>"+ Lang.getInstance().translate("Size")+":&nbsp;"+ issue_voiting.viewSize(true)+" Bytes, ";
			    Status_text += "<b>" +Lang.getInstance().translate("Fee")+":&nbsp;"+ issue_voiting.getFee().toString()+" COMPU</b><br></body></HTML>";
			    
			
		//	    UIManager.put("OptionPane.cancelButtonText", "Отмена");
		//	    UIManager.put("OptionPane.okButtonText", "Готово");
			
		//	int s = JOptionPane.showConfirmDialog(MainFrame.getInstance(), text, Lang.getInstance().translate("Issue Asset"),  JOptionPane.YES_NO_OPTION);
			
			Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(MainFrame.getInstance(), true,text, 600, 400,Status_text, Lang.getInstance().translate("Confirmation Transaction"));
			dd.setLocationRelativeTo(null);
			dd.setVisible(true);
			
		//	JOptionPane.OK_OPTION
			if (dd.isConfirm){ //s!= JOptionPane.OK_OPTION)	{
				
				
				
				
			
			
					
			//VALIDATE AND PROCESS
			int result = Controller.getInstance().getTransactionCreator().afterCreate(issue_voiting, false);
			
			
			
			
			
			
			if (result == Transaction.VALIDATE_OK)
				return result +"";
			else
				throw ApiErrorFactory.getInstance().createError(result);
			}
		}
		catch(NullPointerException | ClassCastException e)
		{
			//JSON EXCEPTION
			LOGGER.info(e);
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
		return "ok";
	}
	
	@POST
	@Path("/vote/{name}")
	@Consumes(MediaType.WILDCARD)
	public String createPollVote(String x, @PathParam("name") String name)
	{
		try
		{
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String voter = (String) jsonObject.get("voter");
			String option = (String) jsonObject.get("option");
			String feePowStr = (String) jsonObject.get("feePow");
			
			//PARSE FEE
			int feePow;
			try
			{
				feePow = Integer.parseInt(feePowStr);
			}
			catch(Exception e)
			{
				throw ApiErrorFactory.getInstance().createError(
						Transaction.INVALID_FEE_POWER);
			}	
			
			//CHECK VOTER
			if(!Crypto.getInstance().isValidAddress(voter))
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
			}

			String password = null;
			APIUtils.askAPICallAllowed(password, "POST polls/vote/" + name + "\n"+x, request);

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
			PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(voter);				
			if(account == null)
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.CREATOR_NOT_OWNER);
			}
			
			//GET POLL
			Poll poll = Controller.getInstance().getPoll(name);
			if(poll == null)
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.POLL_NOT_EXISTS);
			}
			
			//GET OPTION
			PollOption pollOption = poll.getOption(option);
			if(pollOption == null)
			{
				throw ApiErrorFactory.getInstance().createError(Transaction.POLL_OPTION_NOT_EXISTS);
			}
				
			//CREATE POLL
			Pair<Transaction, Integer> result = Controller.getInstance().createPollVote(account, poll, pollOption, feePow);
				
			if (result.getB() == Transaction.VALIDATE_OK)
				return result.getA().toJson().toJSONString();
			else
				throw ApiErrorFactory.getInstance().createError(result.getB());
			
		}
		catch(NullPointerException | ClassCastException e)
		{
			//JSON EXCEPTION
			LOGGER.info(e);
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}
	
	@SuppressWarnings("unchecked")
	@GET
	public String getPolls()
	{
		String password = null;
		APIUtils.askAPICallAllowed(password, "GET polls", request);

		//CHECK IF WALLET EXISTS
		if(!Controller.getInstance().doesWalletExists())
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}
		
		List<Pair<Account, Poll>> polls = Controller.getInstance().getPolls();
		JSONArray array = new JSONArray();
		
		for(Pair<Account, Poll> poll: polls)
		{
			array.add(poll.getB().toJson());
		}
		
		return array.toJSONString();
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/address/{address}")	
	public String getPolls(@PathParam("address") String address)
	{
		String password = null;
		APIUtils.askAPICallAllowed(password, "GET polls/address/" + address, request);

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
		for(Poll poll: Controller.getInstance().getPolls(account))
		{
			array.add(poll.toJson());
		}
		
		return array.toJSONString();
	}
	
	@GET
	@Path("/{name}")	
	public String getPoll(@PathParam("name") String name)
	{	
		Poll poll = Controller.getInstance().getPoll(name);
				
		//CHECK IF NAME EXISTS
		if(poll == null)
		{
			throw ApiErrorFactory.getInstance().createError(Transaction.POLL_NOT_EXISTS);
		}
		
		return poll.toJson().toJSONString();
	}

	@SuppressWarnings("unchecked")
	@Path("/network")	
	@GET
	public String getAllPolls()
	{
		Collection<Poll> polls = Controller.getInstance().getAllPolls();
		JSONArray array = new JSONArray();
		
		for(Poll poll: polls)
		{
			array.add(poll.getName());
		}
		
		return array.toJSONString();
	}
	
}
