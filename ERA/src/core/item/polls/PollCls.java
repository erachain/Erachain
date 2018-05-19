package core.item.polls;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.item.ItemCls;
import core.voting.PollOption;
import datachain.DCSet;
import datachain.Issue_ItemMap;
import datachain.Item_Map;
import utils.Pair;

public abstract class PollCls extends ItemCls{

	public static final int POLL = 1;

	protected static final int OPTIONS_SIZE_LENGTH = 4;
	protected static final int BASE_LENGTH = OPTIONS_SIZE_LENGTH;

	private List<PollOption> options;

	public PollCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, List<PollOption> options)
	{
		super(typeBytes, owner, name, icon, image, description);
		this.options = options;

	}
	public PollCls(int type, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, List<PollOption> options)
	{
		this(new byte[TYPE_LENGTH], owner, name, icon, image, description, options);
		this.typeBytes[0] = (byte)type;
	}

	//GETTERS/SETTERS
	public int getItemTypeInt() { return ItemCls.POLL_TYPE; }
	public String getItemTypeStr() { return "poll"; }
	
	public List<PollOption> getOptions() {
		return this.options;
	}

	// DB
	public Item_Map getDBMap(DCSet db)
	{
		return db.getItemPollMap();
	}
	public Issue_ItemMap getDBIssueMap(DCSet db)
	{
		return db.getIssuePollMap();
	}

	public boolean hasVotes()
	{
		for(PollOption option: this.options)
		{
			if(!option.getVoters().isEmpty())
			{
				return true;
			}
		}

		return false;
	}

	public BigDecimal getTotalVotes()
	{
		return getTotalVotes(0);
	}

	public BigDecimal getTotalVotes(long assetKey)
	{
		BigDecimal votes = BigDecimal.ZERO;

		for(PollOption option: this.options)
		{
			votes = votes.add(option.getVotes(assetKey));
		}

		return votes;
	}

	public List<Pair<Account, PollOption>> getVotes()
	{
		List<Pair<Account, PollOption>> votes = new ArrayList<Pair<Account, PollOption>>();

		for(PollOption option: this.options)
		{
			for(Account voter: option.getVoters())
			{
				Pair<Account, PollOption> vote = new Pair<Account, PollOption>(voter, option);
				votes.add(vote);
			}
		}

		return votes;
	}

	public List<Pair<Account, PollOption>> getVotes(List<Account> accounts)
	{
		List<Pair<Account, PollOption>> votes = new ArrayList<Pair<Account, PollOption>>();

		for(PollOption option: this.options)
		{
			for(Account voter: option.getVoters())
			{
				if(accounts.contains(voter))
				{
					Pair<Account, PollOption> vote = new Pair<Account, PollOption>(voter, option);
					votes.add(vote);
				}
			}
		}

		return votes;
	}

	public PollOption getOption(String option)
	{
		for(PollOption pollOption: this.options)
		{
			if(pollOption.getName().equals(option))
			{
				return pollOption;
			}
		}

		return null;
	}

	
	// PARSE
	public byte[] toBytes(boolean includeReference, boolean onlyBody)
	{
		
		byte[] data = super.toBytes(includeReference, onlyBody);
						
		//WRITE OPTIONS SIZE
		byte[] optionsLengthBytes = Ints.toByteArray(this.options.size());
		data = Bytes.concat(data, optionsLengthBytes);

		//WRITE OPTIONS
		for(PollOption option: this.options)
		{
			data = Bytes.concat(data, option.toBytes());
		}
		
		return data;
	}
	
	
	public int getDataLength(boolean includeReference) 
	{
		int length = super.getDataLength(includeReference) + BASE_LENGTH;

		for(PollOption option: this.options)
		{
			length += option.getDataLength();
		}
		
		return length;

	}
	
	
	//OTHER
	
	public int addVoter(Account voter, int optionIndex)
	{
		//CHECK IF WE HAD A PREVIOUS VOTE IN THIS POLL
		int previousOption = -1;
		for(PollOption option: this.options)
		{
			if(option.hasVoter(voter))
			{
				previousOption = this.options.indexOf(option);
			}
		}

		if(previousOption != -1)
		{
			//REMOVE VOTE
			this.options.get(previousOption).removeVoter(voter);
		}

		//ADD NEW VOTE
		this.options.get(optionIndex).addVoter(voter);

		return previousOption;
	}

	public void deleteVoter(Account voter, int optionIndex)
	{
		this.options.get(optionIndex).removeVoter(voter);
	}

	//COPY

	public PollCls copy()
	{
		try
		{
			byte[] bytes = this.toBytes(false, false);
			return PollFactory.getInstance().parse(bytes, false);
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		
		JSONObject pollJSON = super.toJson();

		JSONArray jsonOptions = new JSONArray();
		for(PollOption option: this.options)
		{
			jsonOptions.add(option.toJson());
		}
		pollJSON.put("options", jsonOptions);
				
		return pollJSON;
	}

}
