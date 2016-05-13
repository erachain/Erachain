package core.transaction;

import static org.junit.Assert.assertEquals;

//import java.nio.charset.StandardCharsets;
import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
// import org.apache.log4j.Logger;
import java.util.HashSet;

import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import com.google.common.primitives.Bytes;

//import ntp.NTP;

//import org.json.simple.JSONObject;

//import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.crypto.Base58;
import core.crypto.Crypto;
//import core.account.Account;
//import core.account.PublicKeyAccount;
//import core.crypto.Crypto;
//import core.item.ItemCls;
import core.item.persons.PersonCls;
import core.item.persons.PersonFactory;
import core.item.statuses.StatusCls;
//import database.ItemMap;
import database.DBSet;

public class GenesisIssuePersonRecord extends GenesisIssue_ItemRecord 
{
	private static final byte TYPE_ID = (byte)GENESIS_ISSUE_PERSON_TRANSACTION;
	private static final String NAME_ID = "GENESIS Issue Person";
	private static final int RECIPIENT_LENGTH = TransactionAmount.RECIPIENT_LENGTH;
	private static final int BASE_LENGTH = Genesis_Record.BASE_LENGTH + RECIPIENT_LENGTH;

	public GenesisIssuePersonRecord(PersonCls item, Account recipient) 
	{
		super(TYPE_ID, NAME_ID, item, recipient);
	}
	
	//GETTERS/SETTERS
	
	//public String getName() { return "Issue Person"; }
	
	//PARSE CONVERT
	
	public static Transaction Parse(byte[] data) throws Exception
	{	

		//CHECK IF WE MATCH BLOCK LENGTH
		if (data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length " + data.length);
		}
		
		// READ TYPE
		int position = SIMPLE_TYPE_LENGTH;
							
		//READ PERSON
		// read without reference
		PersonCls person = PersonFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
		//position += note.getDataLength(false);

		//READ RECIPIENT
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		Account recipient = new Account(Base58.encode(recipientBytes));
		position += RECIPIENT_LENGTH;

		return new GenesisIssuePersonRecord(person, recipient);
		
	}	

	//VALIDATE

	//@Override
	public int isValid(DBSet db, byte[] releaserReference) 
	{
		
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(this.getRecipient() == null || !Crypto.getInstance().isValidAddress(this.getRecipient().getAddress()))
		{
			return INVALID_ADDRESS;
		}
				
		return super.isValid(db, releaserReference);
	}


	//PROCESS/ORPHAN
	
	//@Override
	public void process(DBSet db, boolean asPack) 
	{

		super.process(db, asPack);

		long key = this.getItem().getKey();
		Account recipient = this.getRecipient();

		//UPDATE RECIPIENT
		Tuple3<Long, Integer, byte[]> itemP = new Tuple3<Long, Integer, byte[]>(null, 0, this.signature);
		// SET ALIVE PERSON for DURATION
		db.getPersonStatusMap().addItem(key, StatusCls.ALIVE_KEY, itemP);

		// SET PERSON ADDRESS
		// Integer.MAX_VALUE = 0 - permanent
		Tuple4<Long, Integer, Integer, byte[]> itemA = new Tuple4<Long, Integer, Integer, byte[]>(key, 0, 0, this.signature);
		Tuple3<Integer, Integer, byte[]> itemA1 = new Tuple3<Integer, Integer, byte[]>(0, 0, this.signature);
		db.getAddressPersonMap().addItem(recipient.getAddress(), itemA);
		db.getPersonAddressMap().addItem(key, recipient.getAddress(), itemA1);
		
		//UPDATE REFERENCE OF RECIPIENT
		recipient.setLastReference(this.signature, db);
	}

	//@Override
	public void orphan(DBSet db, boolean asPack) 
	{
		
		long key = this.getItem().getKey();
		Account recipient = this.getRecipient();

		super.orphan(db, asPack);

		// UNDO ALIVE PERSON for DURATION
		db.getPersonStatusMap().removeItem(key, StatusCls.ALIVE_KEY);

		//UPDATE RECIPIENT
		db.getAddressPersonMap().removeItem(recipient.getAddress());
		db.getPersonAddressMap().removeItem(key, recipient.getAddress());

		//UPDATE REFERENCE OF CREATOR
		// not needthis.creator.setLastReference(this.reference, db);		
		//UPDATE REFERENCE OF RECIPIENT
		recipient.removeReference(db);
	}

	//REST
	
	@Override
	public HashSet<Account> getRecipientAccounts()
	{
		HashSet<Account> accounts = new HashSet<>();
		accounts.add(this.getRecipient());
		return accounts;
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(getRecipient().getAddress()))
		{
			return true;
		}
		
		return false;
	}


}
