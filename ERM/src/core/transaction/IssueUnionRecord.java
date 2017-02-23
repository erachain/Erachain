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

import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.unions.UnionCls;
import core.item.unions.UnionFactory;
//import database.ItemMap;
import database.DBSet;

public class IssueUnionRecord extends Issue_ItemRecord 
{
	private static final byte TYPE_ID = (byte)ISSUE_UNION_TRANSACTION;
	private static final String NAME_ID = "Issue Union";
	
	public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp, Long reference) 
	{
		super(typeBytes, NAME_ID, creator, union, feePow, timestamp, reference);		
	}
	public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp, Long reference, byte[] signature) 
	{
		super(typeBytes, NAME_ID, creator, union, feePow, timestamp, reference, signature);		
	}
	public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, UnionCls union, byte[] signature) 
	{
		super(typeBytes, NAME_ID, creator, union, (byte)0, 0l, null, signature);		
	}
	public IssueUnionRecord(PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp, Long reference, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, union, feePow, timestamp, reference, signature);
	}
	public IssueUnionRecord(PublicKeyAccount creator, UnionCls union, byte[] signature) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, union, (byte)0, 0l, null, signature);
	}
	public IssueUnionRecord(PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp, Long reference) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, union, feePow, timestamp, reference);
	}
	public IssueUnionRecord(PublicKeyAccount creator, UnionCls union) 
	{
		this(new byte[]{TYPE_ID,0,0,0}, creator, union, (byte)0, 0l, null);
	}

	//GETTERS/SETTERS
	//public static String getName() { return "Issue Union"; }
	
	// NOT GENESIS ISSUE STRT FRON NUM
	protected long getStartKey() {
		return 1000l;
	}

	//@Override
	public int isValid(DBSet db, Long releaserReference) {	

		int result = super.isValid(db, releaserReference);
		if (result != Transaction.VALIDATE_OK) return result; 
		
		BigDecimal balERM = this.creator.getBalanceUSE(RIGHTS_KEY, db);
		if ( balERM.compareTo(BlockChain.MAJOR_ERM_BALANCE_BD)<0 )
		{
			return Transaction.NOT_ENOUGH_RIGHTS;
		}

		return Transaction.VALIDATE_OK;
	}

	//PARSE CONVERT
	
	public static Transaction Parse(byte[] data, Long releaserReference) throws Exception
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

		Long reference = null;
		if (!asPack) {
			//READ REFERENCE
			byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			reference = Longs.fromByteArray(referenceBytes);	
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
		
		//READ UNION
		// union parse without reference - if is = signature
		UnionCls union = UnionFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
		position += union.getDataLength(false);
				
		if (!asPack) {
			return new IssueUnionRecord(typeBytes, creator, union, feePow, timestamp, reference, signatureBytes);
		} else {
			return new IssueUnionRecord(typeBytes, creator, union, signatureBytes);
		}
	}	
		
	//PROCESS/ORPHAN

	/*
	@Override
	public int calcBaseFee() {
		return 10 * (calcCommonFee() + BlockChain.FEE_PER_BYTE * 1000);
	}
	*/

}
