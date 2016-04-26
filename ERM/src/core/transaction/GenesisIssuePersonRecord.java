package core.transaction;

import static org.junit.Assert.assertEquals;

//import java.nio.charset.StandardCharsets;
import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
// import org.apache.log4j.Logger;

//import ntp.NTP;

//import org.json.simple.JSONObject;

//import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

//import core.account.Account;
//import core.account.PublicKeyAccount;
//import core.crypto.Crypto;
//import core.item.ItemCls;
import core.item.persons.PersonCls;
import core.item.persons.PersonFactory;
//import database.ItemMap;

public class GenesisIssuePersonRecord extends GenesisIssue_ItemRecord 
{
	private static final byte TYPE_ID = (byte)GENESIS_ISSUE_PERSON_TRANSACTION;
	private static final String NAME_ID = "Genesis Issue Person";

	public GenesisIssuePersonRecord(PersonCls item) 
	{
		super(TYPE_ID, NAME_ID, item);
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

		return new GenesisIssuePersonRecord(person);
		
	}	
	
	//VALIDATE

	//PROCESS/ORPHAN


}
