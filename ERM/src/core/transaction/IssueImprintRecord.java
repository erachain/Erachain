package core.transaction;

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
import core.item.imprints.ImprintCls;
import core.item.imprints.Imprint;
//import database.ItemMap;
import database.DBSet;

// reference - as item.name
public class IssueImprintRecord extends IssueItemRecord 
{
	private static final byte TYPE_ID = (byte)ISSUE_IMPRINT_TRANSACTION;
	private static final String NAME_ID = "Issue Imprint";
	
	public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp, byte[] reference) 
	{
		super(typeBytes, NAME_ID, creator, imprint, feePow, timestamp, reference);	
	}
	public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, imprint, feePow, timestamp, reference);		
		this.signature = signature;
		this.calcFee();
	}
	public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ImprintCls imprint, byte[] signature) 
	{
		this(typeBytes, creator, imprint, (byte)0, 0l, null);		
		this.signature = signature;
	}
	public IssueImprintRecord(PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, imprint, feePow, timestamp, reference, signature);
	}
	public IssueImprintRecord(PublicKeyAccount creator, ImprintCls imprint, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, imprint, (byte)0, 0l, null, signature);
	}
	public IssueImprintRecord(PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp, byte[] reference) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, imprint, feePow, timestamp, reference);
	}
	public IssueImprintRecord(PublicKeyAccount creator, ImprintCls imprint) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, imprint, (byte)0, 0l, null);
	}

	//GETTERS/SETTERS
	//public static String getName() { return "Issue Imprint"; }
	

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
		
		//READ IMPRINT
		// imprint parse without reference - if is = signature
		ImprintCls imprint = Imprint.parse(Arrays.copyOfRange(data, position, data.length), false);
		position += imprint.getDataLength(false);
				
		if (!asPack) {
			return new IssueImprintRecord(typeBytes, creator, imprint, feePow, timestamp, reference, signatureBytes);
		} else {
			return new IssueImprintRecord(typeBytes, creator, imprint, signatureBytes);
		}
	}	
	
	//VALIDATE
	//
	@Override
	public int isValid(DBSet db, byte[] releaserReference) 
	{
		
		//CHECK NAME LENGTH
		ItemCls item = this.getItem();
		int nameLength = item.getName().getBytes().length;
		if(nameLength > 40 || nameLength < 10)
		{
			return INVALID_NAME_LENGTH;
		}
						
		int result = super.isValid(db, releaserReference);
		if (result != Transaction.VALIDATE_OK) return result;
		
		// CHECK reference in DB
		if (item.getDBIssueMap(db).contains(item.getReference())) return Transaction.DUPLICATE_KEY;

		return Transaction.VALIDATE_OK; 		
	
	}

	
	//PROCESS/ORPHAN

	@Override
	public int calcBaseFee() {
		// + name length ^ 2
		return calcCommonFee() + this.getItem().getName().getBytes().length * this.getItem().getName().getBytes().length ;
	}


}
