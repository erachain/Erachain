package core.transaction;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
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
import core.item.statuses.StatusCls;
import core.item.statuses.StatusFactory;
//import database.ItemMap;
import database.DBSet;

public class IssueStatusRecord extends Issue_ItemRecord 
{
	private static final byte TYPE_ID = (byte)ISSUE_STATUS_TRANSACTION;
	private static final String NAME_ID = "Issue Status";
	private static final BigDecimal MIN_ERM_BALANCE = BigDecimal.valueOf(10000).setScale(8);

	public IssueStatusRecord(byte[] typeBytes, PublicKeyAccount creator, StatusCls status, byte feePow, long timestamp, byte[] reference) 
	{
		super(typeBytes, NAME_ID, creator, status, feePow, timestamp, reference);		
	}
	public IssueStatusRecord(byte[] typeBytes, PublicKeyAccount creator, StatusCls status, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, status, feePow, timestamp, reference);		
		this.signature = signature;
		this.calcFee();
	}
	public IssueStatusRecord(byte[] typeBytes, PublicKeyAccount creator, StatusCls status, byte[] signature) 
	{
		this(typeBytes, creator, status, (byte)0, 0l, null);		
		this.signature = signature;
	}
	public IssueStatusRecord(PublicKeyAccount creator, StatusCls status, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, status, feePow, timestamp, reference, signature);
	}
	public IssueStatusRecord(PublicKeyAccount creator, StatusCls status, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, status, (byte)0, 0l, null, signature);
	}
	public IssueStatusRecord(PublicKeyAccount creator, StatusCls status, byte feePow, long timestamp, byte[] reference) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, status, feePow, timestamp, reference);
	}
	public IssueStatusRecord(PublicKeyAccount creator, StatusCls status) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, status, (byte)0, 0l, null);
	}

	//GETTERS/SETTERS
	//public static String getName() { return "Issue Status"; }
	

	//@Override
	public int isValid(DBSet db, byte[] releaserReference) {	

		int result = super.isValid(db, releaserReference);
		if (result != Transaction.VALIDATE_OK) return result; 
		
		BigDecimal balERM = this.creator.getConfirmedBalance(RIGHTS_KEY, db);
		if ( balERM.compareTo(MIN_ERM_BALANCE)<0 )
		{
			return Transaction.NOT_ENOUGH_RIGHTS;
		}

		if ( !this.creator.isPerson(db) )
		{
			return Transaction.ACCOUNT_NOT_PERSONALIZED;
		}

		return Transaction.VALIDATE_OK;
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
		
		//READ STATUS
		// status parse without reference - if is = signature
		StatusCls status = StatusFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
		position += status.getDataLength(false);
				
		if (!asPack) {
			return new IssueStatusRecord(typeBytes, creator, status, feePow, timestamp, reference, signatureBytes);
		} else {
			return new IssueStatusRecord(typeBytes, creator, status, signatureBytes);
		}
	}	
	
	//VALIDATE
		
	
	//PROCESS/ORPHAN


}
