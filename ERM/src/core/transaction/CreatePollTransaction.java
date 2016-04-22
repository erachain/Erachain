package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ntp.NTP;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Crypto;
import core.voting.Poll;
import core.voting.PollOption;
import database.ItemAssetBalanceMap;
import database.DBSet;

public class CreatePollTransaction extends Transaction 
{
	private static final int TYPE_ID = Transaction.CREATE_POLL_TRANSACTION;
	private static final String NAME_ID = "Create Poll";
	private static final int BASE_LENGTH = Transaction.BASE_LENGTH;

	private PublicKeyAccount creator;
	private Poll poll;
	
	public CreatePollTransaction(byte[] typeBytes, PublicKeyAccount creator, Poll poll, byte feePow, long timestamp, byte[] reference) 
	{
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);
		
		this.creator = creator;
		this.poll = poll;
	}
	public CreatePollTransaction(byte[] typeBytes, PublicKeyAccount creator, Poll poll, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, poll, feePow, timestamp, reference);
		
		this.signature = signature;
		this.calcFee();
	}
	public CreatePollTransaction(PublicKeyAccount creator, Poll poll, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, poll, feePow, timestamp, reference, signature);
	}
	public CreatePollTransaction(PublicKeyAccount creator, Poll poll, byte feePow, long timestamp, byte[] reference) 
	{
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, poll, feePow, timestamp, reference);
	}

	//GETTERS/SETTERS
	//public static String getName() { return "Create Poll"; }

	public Poll getPoll()
	{
		return this.poll;
	}

	//PARSE CONVERT
	
	public static Transaction Parse(byte[] data) throws Exception
	{	
		//CHECK IF WE MATCH BLOCK LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length");
		}
		
		
		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;

		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;
		
		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;
		
		//READ POLL
		Poll poll = Poll.parse(Arrays.copyOfRange(data, position, data.length));
		position += poll.getDataLength();
		
		//READ FEE POWER
		byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
		byte feePow = feePowBytes[0];
		position += 1;
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new CreatePollTransaction(typeBytes, creator, poll, feePow, timestamp, reference, signatureBytes);
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/NAME/DESCRIPTION/OPTIONS
		transaction.put("creator", this.creator.getAddress());
		transaction.put("name", this.poll.getName());
		transaction.put("description", this.poll.getDescription());
		
		JSONArray options = new JSONArray();
		for(PollOption option: this.poll.getOptions())
		{
			options.add(option.getName());
		}
		
		transaction.put("options", options);
				
		return transaction;	
	}
	
	@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		data = Bytes.concat(data, this.typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());
		
		//WRITE POLL
		data = Bytes.concat(data , this.poll.toBytes());
		
		//WRITE FEE POWER
		byte[] feePowBytes = new byte[1];
		feePowBytes[0] = this.feePow;
		data = Bytes.concat(data, feePowBytes);

		//SIGNATURE
		if (withSign) data = Bytes.concat(data, this.signature);
		
		return data;
	}
	
	@Override
	public int getDataLength(boolean asPack)
	{
		return BASE_LENGTH + this.poll.getDataLength();
	}
	
	//VALIDATE
	
	public boolean isSignatureValid()
	{
		byte[] data = this.toBytes( false, null );
		if ( data == null ) return false;
				
		return Crypto.getInstance().verify(this.creator.getPublicKey(), this.signature, data);
	}
	
	@Override
	public int isValid(DBSet db, byte[] releaserReference) 
	{
		/*
		//CHECK IF RELEASED
		if(NTP.getTime() < Transaction.getVOTING_RELEASE())
		{
			return NOT_YET_RELEASED;
		}
		*/
		
		//CHECK POLL NAME LENGTH
		int nameLength = this.poll.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > 400 || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK POLL NAME LOWERCASE
		if(!this.poll.getName().equals(this.poll.getName().toLowerCase()))
		{
			return NAME_NOT_LOWER_CASE;
		}
		
		//CHECK POLL DESCRIPTION LENGTH
		int descriptionLength = this.poll.getDescription().getBytes(StandardCharsets.UTF_8).length;
		if(descriptionLength > 4000 || descriptionLength < 1)
		{
			return INVALID_DESCRIPTION_LENGTH;
		}
		
		//CHECK POLL DOES NOT EXIST ALREADY
		if(db.getPollMap().contains(this.poll))
		{
			return POLL_ALREADY_CREATED;
		}
		
		//CHECK IF POLL DOES NOT CONTAIN ANY VOTERS
		if(this.poll.hasVotes())
		{
			return POLL_ALREADY_HAS_VOTES;
		}
		
		//CHECK POLL CREATOR VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.poll.getCreator().getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//CHECK OPTIONS LENGTH
		int optionsLength = poll.getOptions().size();
		if(optionsLength > 100 || optionsLength < 1)
		{
			return INVALID_OPTIONS_LENGTH;
		}
		
		//CHECK OPTIONS
		List<String> options = new ArrayList<String>();
		for(PollOption option: this.poll.getOptions())
		{
			//CHECK OPTION LENGTH
			int optionLength = option.getName().getBytes(StandardCharsets.UTF_8).length;
			if(optionLength > 400 || optionLength < 1)
			{
				return INVALID_OPTION_LENGTH;
			}
			
			//CHECK OPTION UNIQUE
			if(options.contains(option.getName()))
			{
				return DUPLICATE_OPTION;
			}
			
			options.add(option.getName());
		}
		
		//CHECK IF SENDER HAS ENOUGH FEE BALANCE
		if(this.creator.getConfirmedBalance(FEE_KEY, db).compareTo(this.fee) == -1)
		{
			return NOT_ENOUGH_FEE;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
		
		
		return VALIDATE_OK;
	}
	
	//PROCESS/ORPHAN

	//@Override
	public void process(DBSet db, boolean asPack)
	{
		//UPDATE CREATOR
		super.process(db, asPack);
		
		//INSERT INTO DATABASE
		db.getPollMap().add(this.poll);
	}


	//@Override
	public void orphan(DBSet db, boolean asPack) 
	{
		//UPDATE CREATOR
		super.orphan(db, asPack);
				
		//DELETE FROM DATABASE
		db.getPollMap().delete(this.poll);		
	}


	@Override
	public HashSet<Account> getInvolvedAccounts() 
	{
		HashSet<Account> accounts = new HashSet<>();
		accounts.add(this.creator);
		accounts.add(this.poll.getCreator());
		return accounts;
	}

	@Override
	public HashSet<Account> getRecipientAccounts()
	{
		return new HashSet<>();
	}

	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress()) || address.equals(this.poll.getCreator().getAddress()))
		{
			return true;
		}
		
		return false;
	}

	//@Override
	public BigDecimal viewAmount(Account account) 
	{
		if(account.getAddress().equals(this.creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		
		return BigDecimal.ZERO;
	}
	
	//@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		return subAssetAmount(null, this.creator.getAddress(), ItemAssetBalanceMap.FEE_KEY, this.fee);
	}

	public int calcBaseFee() {
		return calcCommonFee();
	}
}
