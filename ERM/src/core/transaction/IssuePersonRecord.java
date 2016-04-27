package core.transaction;

import java.math.BigDecimal;
import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
//import java.util.Map;
// import org.apache.log4j.Logger;

//import ntp.NTP;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.persons.PersonCls;
import core.item.persons.PersonFactory;
import core.transaction.Transaction;
//import database.ItemMap;
import database.DBSet;

public class IssuePersonRecord extends Issue_ItemRecord 
{
	private static final byte TYPE_ID = (byte)ISSUE_PERSON_TRANSACTION;
	private static final String NAME_ID = "Issue Person";
	
	
	public IssuePersonRecord(byte[] typeBytes, PublicKeyAccount creator, PersonCls person, byte feePow, long timestamp, byte[] reference) 
	{
		super(typeBytes, NAME_ID, creator, person, feePow, timestamp, reference);		
	}
	public IssuePersonRecord(byte[] typeBytes, PublicKeyAccount creator, PersonCls person, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, person, feePow, timestamp, reference);		
		this.signature = signature;
		this.calcFee();
	}
	public IssuePersonRecord(byte[] typeBytes, PublicKeyAccount creator, PersonCls person, byte[] signature) 
	{
		this(typeBytes, creator, person, (byte)0, 0l, null);		
		this.signature = signature;
	}
	public IssuePersonRecord(PublicKeyAccount creator, PersonCls person, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, person, feePow, timestamp, reference, signature);
	}
	public IssuePersonRecord(PublicKeyAccount creator, PersonCls person, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, person, (byte)0, 0l, null, signature);
	}
	public IssuePersonRecord(PublicKeyAccount creator, PersonCls person, byte feePow, long timestamp, byte[] reference) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, person, feePow, timestamp, reference);
	}
	public IssuePersonRecord(PublicKeyAccount creator, PersonCls person) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, person, (byte)0, 0l, null);
	}

	//GETTERS/SETTERS
	
	//public String getName() { return "Issue Person"; }
	
	//@Override
	public int isValid(DBSet db, byte[] releaserReference) 
	{
						
		int res = super.isValid(db, releaserReference);
		if (res != Transaction.VALIDATE_OK) return res;
		
		PersonCls person = (PersonCls) this.getItem();
		if (person.getBirthLatitude() > 180 || person.getBirthLatitude() < -180) return Transaction.ITEM_PERSON_LATITUDE_ERROR;
		if (person.getBirthLongitude() > 90 || person.getBirthLongitude() < -90) return Transaction.ITEM_PERSON_LONGITUDE_ERROR;
		if (person.getRace().length() <1 || person.getRace().length() > 125) return Transaction.ITEM_PERSON_RACE_ERROR;
		if (person.getGender() < 0 || person.getGender() > 10) return Transaction.ITEM_PERSON_GENDER_ERROR;
		if (person.getSkinColor().length() <1 || person.getSkinColor().length() >255) return Transaction.ITEM_PERSON_SKIN_COLOR_ERROR;
		if (person.getEyeColor().length() <1 || person.getEyeColor().length() >255) return Transaction.ITEM_PERSON_EYE_COLOR_ERROR;
		if (person.getHairСolor().length() <1 || person.getHairСolor().length() >255) return Transaction.ITEM_PERSON_HAIR_COLOR_ERROR;
		if (person.getHeight() < 10 || person.getHeight() > 255) return Transaction.ITEM_PERSON_HEIGHT_ERROR;
		
		// CHECH MAKER IS PERSON?
		if (!this.creator.isPerson(db)
				// OR RIGHTS_KEY ENOUGHT
				&& this.creator.getConfirmedBalance(Transaction.RIGHTS_KEY, db)
						.compareTo(new BigDecimal(1000)) < 0)
			
			return Transaction.ACCOUNT_NOT_PERSONALIZED;
		
		return VALIDATE_OK;
	
	}

	//PARSE CONVERT
	
	public static Transaction Parse(byte[] data, byte[] releaserReference) throws Exception
	{	

		boolean asPack = releaserReference != null;

		//CHECK IF WE MATCH BLOCK LENGTH
		if (data.length < BASE_LENGTH_AS_PACK
				| !asPack & data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length " + data.length);
		}
		
		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;

		long timestamp = 0;
		if (!asPack) {
			//READ TIMESTAMP
			byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
			timestamp = Longs.fromByteArray(timestampBytes);	
			position += TIMESTAMP_LENGTH;
		}

		byte[] reference = null;
		if (!asPack) {
			//READ REFERENCE
			reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			position += REFERENCE_LENGTH;
		}
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;
		
		byte feePow = 0;
		if (!asPack) {
			//READ FEE POWER
			byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
			feePow = feePowBytes[0];
			position += 1;
		}
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		position += SIGNATURE_LENGTH;
		
		//READ PERSON
		// person parse without reference - if is = signature
		PersonCls person = PersonFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
		position += person.getDataLength(false);
				
		if (!asPack) {
			return new IssuePersonRecord(typeBytes, creator, person, feePow, timestamp, reference, signatureBytes);
		} else {
			return new IssuePersonRecord(typeBytes, creator, person, signatureBytes);
		}
	}	
	
	//VALIDATE
		
	
	//PROCESS/ORPHAN


}
