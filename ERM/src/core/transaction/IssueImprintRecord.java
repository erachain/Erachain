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

import core.BlockChain;
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
// TODO - reference NOT NEED - because it is unique record! - make it as new version protocol
public class IssueImprintRecord extends Issue_ItemRecord 
{
	private static final byte TYPE_ID = (byte)ISSUE_IMPRINT_TRANSACTION;
	private static final String NAME_ID = "Issue Imprint";
	
	//protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK;
	protected static final int BASE_LENGTH = Transaction.BASE_LENGTH - REFERENCE_LENGTH;

	
	public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp) 
	{
		super(typeBytes, NAME_ID, creator, imprint, feePow, timestamp, null);	
	}
	public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp, byte[] signature) 
	{
		super(typeBytes, NAME_ID, creator, imprint, feePow, timestamp, null, signature);
	}
	// asPack
	public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ImprintCls imprint, byte[] signature) 
	{
		super(typeBytes, NAME_ID, creator, imprint, (byte)0, 0l, null, signature);		
	}
	public IssueImprintRecord(PublicKeyAccount creator, ImprintCls imprint, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, imprint, (byte)0, 0l, signature);
	}
	public IssueImprintRecord(PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, imprint, feePow, timestamp, signature);
	}
	public IssueImprintRecord(PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, imprint, feePow, timestamp, null);
	}
	public IssueImprintRecord(PublicKeyAccount creator, ImprintCls imprint) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, imprint, (byte)0, 0l, null);
	}

	//GETTERS/SETTERS
	//public static String getName() { return "Issue Imprint"; }
	
	public boolean hasPublicText() {
		return false;
	}

	@Override
	public boolean isReferenced()
	{
		// reference not used - because all imprint is unique
		return false;
	}			


	//PARSE CONVERT
	
	public static Transaction Parse(byte[] data, Long releaserReference) throws Exception
	{	

		boolean asPack = releaserReference != null;

		//CHECK IF WE MATCH BLOCK LENGTH
		if (data.length < BASE_LENGTH_AS_PACK
				| !asPack & data.length < BASE_LENGTH
)
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

		/*
		byte[] reference = null;
		if (!asPack && typeBytes[1] == 0) {
			// in not PACKED and it is VERSION - 0
			//READ REFERENCE
			reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			position += REFERENCE_LENGTH;
		}
		*/
		
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
			return new IssueImprintRecord(typeBytes, creator, imprint, feePow, timestamp, signatureBytes);
		} else {
			return new IssueImprintRecord(typeBytes, creator, imprint, signatureBytes);
		}
	}	
	
	//VALIDATE
	//
	@Override
	public int isValid(DBSet db, Long releaserReference) 
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
		if (item.getDBIssueMap(db).contains(item.getReference()))
			return Transaction.ITEM_DUPLICATE_KEY;

		return Transaction.VALIDATE_OK; 		
	
	}
	
	@Override
	public int getDataLength(boolean asPack)
	{
		// not include item reference
		if (asPack) {
			return BASE_LENGTH_AS_PACK + this.getItem().getDataLength(false);
		} else {
			return BASE_LENGTH + this.getItem().getDataLength(false);
		}
	}


	//PROCESS/ORPHAN

	@Override
	public int calcBaseFee() {		
		return calcCommonFee() >>3;
	}


}
