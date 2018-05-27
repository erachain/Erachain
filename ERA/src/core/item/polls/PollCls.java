package core.item.polls;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.item.ItemCls;
import datachain.DCSet;
import datachain.Issue_ItemMap;
import datachain.Item_Map;
import datachain.VoteOnItemPollMap;
import utils.Pair;

public abstract class PollCls extends ItemCls{

	public static final int POLL = 1;

	protected static final int OPTIONS_SIZE_LENGTH = 4;
	protected static final int BASE_LENGTH = OPTIONS_SIZE_LENGTH;
	public static final int INITIAL_FAVORITES = 0;

	private List<String> options;

	public PollCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, List<String> options)
	{
		super(typeBytes, owner, name, icon, image, description);
		this.options = options;

	}
	public PollCls(int type, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, List<String> options)
	{
		this(new byte[TYPE_LENGTH], owner, name, icon, image, description, options);
		this.typeBytes[0] = (byte)type;
	}

	//GETTERS/SETTERS
	public int getItemTypeInt() { return ItemCls.POLL_TYPE; }
	public String getItemTypeStr() { return "poll"; }
	
	public List<String> getOptions() {
		return this.options;
	}

	// DB
	public Item_Map getDBMap(DCSet dc)
	{
		return dc.getItemPollMap();
	}
	public Issue_ItemMap getDBIssueMap(DCSet dc)
	{
		return dc.getIssuePollMap();
	}

	public boolean hasVotes(DCSet dc)
	{
		return dc.getVoteOnItemPollMap().hasVotes(this.key);
	}

	public BigDecimal getTotalVotes(DCSet dcSet)
	{
		return getTotalVotes(dcSet, 2);
	}

	public BigDecimal getTotalVotes(DCSet dcSet, long assetKey)
	{
		BigDecimal votes = BigDecimal.ZERO;
		VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
		NavigableSet<Tuple3<Long, Integer, byte[]>> optionVoteKeys;
		Account voter;
		
		optionVoteKeys = map.getVotes1(this.key);
		for (Tuple3<Long, Integer, byte[]> key: optionVoteKeys) {
			voter = Account.makeAccountFromShort(key.c);
			votes.add(voter.getBalanceUSE(assetKey));
		}

		return votes;
	}

	public BigDecimal getTotalVotes(DCSet dcSet, long assetKey, int option)
	{
		BigDecimal votes = BigDecimal.ZERO;
		VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
		NavigableSet<Tuple3<Long, Integer, byte[]>> optionVoteKeys;
		Account voter;
		
		optionVoteKeys = map.getVotes1(this.key);
		for (Tuple3<Long, Integer, byte[]> key: optionVoteKeys) {
			if (option != key.b)
				continue;
			
			voter = Account.makeAccountFromShort(key.c);
			votes.add(voter.getBalanceUSE(assetKey));
		}

		return votes;
	}


	public List<Pair<Account, Integer>> getVotes(DCSet dcSet)
	{
		List<Pair<Account, Integer>> votes = new ArrayList<Pair<Account, Integer>>();

		VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
		NavigableSet<Tuple3<Long, Integer, byte[]>> optionVoteKeys;
		Pair<Account, Integer> vote;
		Account voter;
		
		optionVoteKeys = map.getVotes(this.key);
		for (Tuple3<Long, Integer, byte[]> key: optionVoteKeys) {
			voter = Account.makeAccountFromShort(key.c);
			vote = new Pair<Account, Integer>(voter, key.b);
			votes.add(vote);
		}

		return votes;
	}

	public List<Pair<Account, Integer>> getVotes(DCSet dcSet, List<Account> accounts)
	{
		List<Pair<Account, Integer>> votes = new ArrayList<Pair<Account, Integer>>();

		VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
		NavigableSet<Tuple3<Long, Integer, byte[]>> optionVoteKeys;
		Pair<Account, Integer> vote;
		Account voter;
		
		optionVoteKeys = map.getVotes(this.key);
		for (Tuple3<Long, Integer, byte[]> key: optionVoteKeys) {
			for (Account account: accounts) {
				if (account.equals(key.c)) {
					vote = new Pair<Account, Integer>(account, key.b);
					votes.add(vote);
				}
			}
		}

		return votes;
	}

	
	public int getOption(String option)
	{
		
		int i = 0;
		for(String pollOption: this.options)
		{
			if(pollOption.equals(option))
			{
				return i;
			}
			
			i++;
		}

		return -1;
	}

	
	// PARSE
	public byte[] toBytes(boolean includeReference, boolean onlyBody)
	{
		
		byte[] data = super.toBytes(includeReference, onlyBody);
						
		//WRITE OPTIONS SIZE
		byte[] optionsLengthBytes = Ints.toByteArray(this.options.size());
		data = Bytes.concat(data, optionsLengthBytes);

		//WRITE OPTIONS
		for(String option: this.options)
		{
			
			//WRITE NAME SIZE
			byte[] optionBytes = option.getBytes(StandardCharsets.UTF_8);
			data = Bytes.concat(data, new byte[]{(byte)optionBytes.length});

			//WRITE NAME
			data = Bytes.concat(data, optionBytes);
		}
		
		return data;
	}
	
	
	public int getDataLength(boolean includeReference) 
	{
		int length = super.getDataLength(includeReference) + BASE_LENGTH;

		for(String option: this.options)
		{
			length += 1 + option.getBytes(StandardCharsets.UTF_8).length;
		}
		
		return length;

	}
	
	
	//OTHER
		
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		
		JSONObject pollJSON = super.toJson();

		JSONArray jsonOptions = new JSONArray();
		for(String option: this.options)
		{
			jsonOptions.add(option);
		}
		pollJSON.put("options", jsonOptions);
				
		return pollJSON;
	}

}
