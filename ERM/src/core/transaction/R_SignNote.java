package core.transaction;

import java.math.BigDecimal;
//import java.math.BigDecimal;
//import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
//import java.util.List;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import org.apache.log4j.Logger;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.item.ItemCls;
//import database.BalanceMap;
import database.DBSet;



public class R_SignNote extends Transaction {

	private static final byte TYPE_ID = (byte) SIGN_NOTE_TRANSACTION;
	private static final String NAME_ID = "Sign Note";

	protected static final byte HAS_NOTE_MASK = (byte)(1 << 7);
	/*
	PROPERTIES:
	[0] - type
	[1] [0] - =1 - has Note 
	[1] bytes [6,7] - signers: 0 - none; 1..3 = 1..3; 4 = LIST -> 1 byte for LIST.len + 3 
		*/
	protected long key; // key for Note
	protected byte[] data;
	protected byte[] isText;
	protected PublicKeyAccount[] signers;
	protected byte[][] signatures; // multi sign
	
	protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + IS_TEXT_LENGTH + DATA_SIZE_LENGTH ; 
	protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + IS_TEXT_LENGTH + DATA_SIZE_LENGTH ; 

	public R_SignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long Note, byte[] data, byte[] isText, long timestamp, Long reference) {
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);
		this.data = data;
		this.isText = isText;
	}
	public R_SignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long Note, byte[] data, byte[] isText, long timestamp, Long reference, byte[] signature) {
		this(typeBytes, creator, feePow, Note, data, isText, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}
	// asPack
	public R_SignNote(byte[] typeBytes, PublicKeyAccount creator, long Note, byte[] data, byte[] isText, Long reference, byte[] signature) {
		this(typeBytes, creator, (byte)0, Note, data, isText, 0l, reference);
		this.signature = signature;
		// not need this.calcFee();
	}
	public R_SignNote(PublicKeyAccount creator, byte feePow, long Note, byte[] data, byte[] isText, long timestamp, Long reference, byte[] signature) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, Note, data, isText, timestamp, reference, signature);
	}
	public R_SignNote(PublicKeyAccount creator, byte feePow, long Note, byte[] data, byte[] isText, long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, Note, data, isText, timestamp, reference);
	}
	public R_SignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long Note, byte[] data,
			byte[] isText, PublicKeyAccount[] signers, byte[][] signatures, long timestamp, Long reference, byte[] signature)
	{
		this(typeBytes, creator, feePow, Note, data, isText, timestamp, reference, signature);
		this.signers = signers;
		this.signatures = signatures;
		this.setTypeBytes();
	}
	// as Pack
	public R_SignNote(byte[] typeBytes, PublicKeyAccount creator, long Note, byte[] data,
			byte[] isText, PublicKeyAccount[] signers, byte[][] signatures, Long reference, byte[] signature)
	{
		this(typeBytes, creator, Note, data, isText, reference, signature);
		this.signers = signers;
		this.signatures = signatures;
		this.setTypeBytes();
	}
	public R_SignNote(byte prop1, byte prop2, byte prop3, PublicKeyAccount creator, byte feePow, long Note, byte[] data, byte[] isText, long timestamp, Long reference)
	{
		this(new byte[]{TYPE_ID, prop1, prop2, prop3}, creator, feePow, Note, data, isText, timestamp, reference);
	}

	//GETTERS/SETTERS
	public static boolean hasNote(byte[] typeBytes) {
		if (typeBytes[1] < 0 ) return true;
		return false;
	}
	protected boolean hasNote() {
		return hasNote(this.typeBytes);
	}
	public static int getSignersLength(byte[] typeBytes) {
		byte mask = ~HAS_NOTE_MASK;
		return typeBytes[1] & mask;
	}
	
	protected void setTypeBytes() {
		byte prop1 = 0;
		if (this.signers == null | this.signers.length == 0) {
			this.typeBytes = new byte[]{TYPE_ID, 0, 0, 0};
		} else {
			int len = this.signers.length; 
			if (len < 4) {
				prop1 = (byte)len;
			} else {
				prop1 = (byte)4;
			}
		}
		// set has NOTE bite
		if (this.key == 0) prop1 = (byte) (HAS_NOTE_MASK | prop1);
			
		this.typeBytes = new byte[]{TYPE_ID, prop1, 0, 0};
	}

	//public static String getName() { return "Statement"; }

	public long getKey() 
	{
		return this.key;
	}
	
	public byte[] getData() 
	{
		return this.data;
	}
	
	public boolean isText()
	{
		return (Arrays.equals(this.isText,new byte[1]))?false:true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();

		//ADD CREATOR/SERVICE/DATA
		if ( this.isText() )
		{
			transaction.put("data", new String(this.data, Charset.forName("UTF-8")));
		}
		else
		{
			transaction.put("data", Base58.encode(this.data));
		}
		transaction.put("isText", this.isText());
		
		return transaction;	
	}
	
	// releaserReference = null - not a pack
	// releaserReference = reference for releaser account - it is as pack
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
		} else {
			reference = releaserReference;
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

		/////		
		long key = 0l;
		if (hasNote(typeBytes)) 
		{
			//READ KEY
			byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
			key = Longs.fromByteArray(keyBytes);	
			position += KEY_LENGTH;
		}

		//READ DATA SIZE
		byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
		int dataSize = Ints.fromByteArray(dataSizeBytes);	
		position += DATA_SIZE_LENGTH;

		//READ DATA
		byte[] arbitraryData = Arrays.copyOfRange(data, position, position + dataSize);
		position += dataSize;
				
		byte[] isTextByte = Arrays.copyOfRange(data, position, position + IS_TEXT_LENGTH);
		position += IS_TEXT_LENGTH;

		int signersLen = getSignersLength(typeBytes);
		PublicKeyAccount[] signers = null;
		byte[][] signatures = null;
		if (signersLen > 0) {
			if (signersLen == 4) {
				//READ ONE BITE for len
				byte[] signersLenBytes = Arrays.copyOfRange(data, position, position + 1);
				signersLen = Byte.toUnsignedInt(signersLenBytes[0]) + 4;
				position ++;
			}
			signers = new PublicKeyAccount[signersLen];
			signatures = new byte[signersLen][];
			for (int i = 0; i < signersLen ; i++) {
				signers[i] = new PublicKeyAccount(Arrays.copyOfRange(data, position, position + CREATOR_LENGTH));
				position += CREATOR_LENGTH;
				signatures[i] = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
				position += SIGNATURE_LENGTH;
			}
		}
		
		if (signersLen == 0) {
			if (!asPack) {
				return new R_SignNote(typeBytes, creator, feePow, key, arbitraryData, isTextByte, timestamp, reference, signatureBytes);
			} else {
				return new R_SignNote(typeBytes, creator, key, arbitraryData, isTextByte, reference, signatureBytes);
			}
		} else {
			if (!asPack) {
				return new R_SignNote(typeBytes, creator, feePow, key, arbitraryData, isTextByte, signers, signatures, timestamp, reference, signatureBytes);
			} else {
				return new R_SignNote(typeBytes, creator, key, arbitraryData, isTextByte, signers, signatures, reference, signatureBytes);
			}
			
		}

	}

	//@Override
	public byte[] toBytes(boolean withSign, Long releaserReference) {

		byte[] data = super.toBytes(withSign, releaserReference);

		//WRITE DATA SIZE
		byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
		data = Bytes.concat(data, dataSizeBytes);

		//WRITE DATA
		data = Bytes.concat(data, this.data);
				
		//WRITE ISTEXT
		data = Bytes.concat(data, this.isText);

		return data;	
	}

	@Override
	public int getDataLength(boolean asPack) {
		if (asPack) {
			return BASE_LENGTH_AS_PACK + this.data.length;
		} else {
			return BASE_LENGTH + this.data.length;
		}
	}

	//@Override
	public int isValid(DBSet db, Long releaserReference) {
		
		//CHECK DATA SIZE
		if(data.length > 4000 || data.length < 1)
		{
			return INVALID_DATA_LENGTH;
		}
	

		int result = super.isValid(db, releaserReference);
		if (result != Transaction.VALIDATE_OK) return result; 
		
		// ITEM EXIST? - for assets transfer not need - amount expect instead
		if (!db.getItemNoteMap().contains(this.key))
			return Transaction.ITEM_DOES_NOT_EXIST;

		return Transaction.VALIDATE_OK;

	}
	
	@Override
	public HashSet<Account> getInvolvedAccounts()
	{
		HashSet<Account> accounts = new HashSet<Account>();
		accounts.add(this.creator);
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
		
		if(address.equals(this.creator.getAddress()))
		{
			return true;
		}
		
		return false;
	}

	public int calcBaseFee() {
		return calcCommonFee();
	}
}
