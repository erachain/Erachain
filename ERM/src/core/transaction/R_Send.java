package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import database.ItemAssetBalanceMap;
import database.DBSet;
import utils.Converter;


// typeBytes[1] (version) = 1 - CONFISCATE CREDIT
// typeBytes[2] = -128 if NO AMOUNT
// typeBytes[3] = -128 if NO DATA

public class R_Send extends TransactionAmount {

	private static final byte TYPE_ID = (byte)Transaction.SEND_ASSET_TRANSACTION;
	private static final String NAME_ID = "Send";

	protected byte[] data;
	protected byte[] encrypted;
	protected byte[] isText;
	
	protected static final int BASE_LENGTH = IS_TEXT_LENGTH + ENCRYPTED_LENGTH + DATA_SIZE_LENGTH;

	public R_Send(byte[] typeBytes, PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference) {
		super(typeBytes, NAME_ID, creator, feePow, recipient, amount, key, timestamp, reference);

		if (data == null || data.length == 0) {
			// set version byte
			typeBytes[3] = (byte)(typeBytes[3] | (byte)-128);
		} else {
			this.data = data;
			this.encrypted = encrypted;
			this.isText = isText;
		}
	}
	public R_Send(byte[] typeBytes, PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference, byte[] signature) {
		this(typeBytes, creator, feePow, recipient, key, amount, data, isText, encrypted, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}
	// as pack
	public R_Send(byte[] typeBytes, PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, byte[] data, byte[] isText, byte[] encrypted, Long reference, byte[] signature) {
		this(typeBytes, creator, (byte)0, recipient, key, amount, data, isText, encrypted, 0l, reference);
		this.signature = signature;
	}
	// FOR CONFISCATE CREDIT
	public R_Send(byte vers, PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, vers, 0, 0}, creator, feePow, recipient, key, amount, data, isText, encrypted, timestamp, reference);
	}
	public R_Send(PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, recipient, key, amount, data, isText, encrypted, timestamp, reference);
	}
	public R_Send(PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference, byte[] signature) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, recipient, key, amount, data, isText, encrypted, timestamp, reference, signature);
	}
	// as pack
	public R_Send(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, byte[] data, byte[] isText, byte[] encrypted, Long reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, (byte)0, recipient, key, amount, data, isText, encrypted, 0l, reference);
	}

	////////////////////////// SHOR -text DATA
	public R_Send(byte[] typeBytes, PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, long timestamp, Long reference) {
		super(typeBytes, NAME_ID, creator, feePow, recipient, amount, key, timestamp, reference);
		typeBytes[3] = (byte)(typeBytes[3] & (byte)128);
	}
	public R_Send(byte[] typeBytes, PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, long timestamp, Long reference, byte[] signature) {
		this(typeBytes, creator, feePow, recipient, key, amount, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}
	// as pack
	public R_Send(byte[] typeBytes, PublicKeyAccount creator, Account recipient, long key, BigDecimal amount) {
		this(typeBytes, creator, (byte)0, recipient, key, amount, 0l, null);
	}
	public R_Send(PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, 0, -128, 0}, creator, feePow, recipient, key, amount, timestamp, reference);
	}
	public R_Send(PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, long timestamp, Long reference, byte[] signature) {
		this(new byte[]{TYPE_ID, 0, -128, 0}, creator, feePow, recipient, key, amount, timestamp, reference, signature);
	}
	// as pack
	public R_Send(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, Long reference) {
		this(new byte[]{TYPE_ID, 0, -128, 0}, creator, (byte)0, recipient, key, amount, 0l, reference);
	}

	
	//GETTERS/SETTERS

	//public static String getName() { return "Send"; }

	public byte[] getData() 
	{
		return this.data;
	}
	
	public byte[] getEncrypted()
	{

		byte[] enc = new byte[1];
		enc[0] = (isEncrypted())?(byte)1:(byte)0;
		return enc;
	}
	
	public boolean isText()
	{
		if (data == null || data.length == 0) return false;
		return (Arrays.equals(this.isText,new byte[1]))?false:true;
	}
	
	public boolean isEncrypted()
	{
		if (data == null || data.length == 0) return false;
		return (Arrays.equals(this.encrypted,new byte[1]))?false:true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();

		if (data != null && data.length > 0) {

			//ADD CREATOR/SERVICE/DATA
			if ( this.isText() && !this.isEncrypted() )
			{
				transaction.put("data", new String(this.data, Charset.forName("UTF-8")));
			}
			else
			{
				transaction.put("data", Base58.encode(this.data));
			}
			transaction.put("encrypted", this.isEncrypted());
			transaction.put("isText", this.isText());
		}
		
		return transaction;	
	}

	//PARSE/CONVERT
	
	public static Transaction Parse(byte[] data, Long releaserReference) throws Exception{

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

		//READ RECIPIENT
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		Account recipient = new Account(Base58.encode(recipientBytes));
		position += RECIPIENT_LENGTH;
		
		long key = 0;
		BigDecimal amount = null;
		if (typeBytes[2] >= 0) {
			// IF here is AMOUNT
			
			//READ KEY
			byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
			key = Longs.fromByteArray(keyBytes);	
			position += KEY_LENGTH;
			
			//READ AMOUNT
			byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
			amount = new BigDecimal(new BigInteger(amountBytes), 8);
			position += AMOUNT_LENGTH;
		}

		// DATA +++
		byte[] arbitraryData = null;
		byte[] encryptedByte = null;
		byte[] isTextByte = null;
		if (typeBytes[3] >= 0) {
			// IF here is DATA

			//READ DATA SIZE
			byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
			int dataSize = Ints.fromByteArray(dataSizeBytes);	
			position += DATA_SIZE_LENGTH;
	
			//READ DATA
			arbitraryData = Arrays.copyOfRange(data, position, position + dataSize);
			position += dataSize;
			
			encryptedByte = Arrays.copyOfRange(data, position, position + ENCRYPTED_LENGTH);
			position += ENCRYPTED_LENGTH;
			
			isTextByte = Arrays.copyOfRange(data, position, position + IS_TEXT_LENGTH);
			position += IS_TEXT_LENGTH;
		}
		
		if (!asPack) {
			return new R_Send(typeBytes, creator, feePow, recipient, key, amount, arbitraryData, isTextByte, encryptedByte, timestamp, reference, signatureBytes);
		} else {
			return new R_Send(typeBytes, creator, recipient, key, amount, arbitraryData, isTextByte, encryptedByte, reference, signatureBytes);
		}

	}

	@Override
	public byte[] toBytes(boolean withSign, Long releaserReference) {

		byte[] data = super.toBytes(withSign, releaserReference);

		if (this.data != null ) {
			//WRITE DATA SIZE
			byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
			data = Bytes.concat(data, dataSizeBytes);
	
			//WRITE DATA
			data = Bytes.concat(data, this.data);
			
			//WRITE ENCRYPTED
			data = Bytes.concat(data, this.encrypted);
			
			//WRITE ISTEXT
			data = Bytes.concat(data, this.isText);
		}

		return data;	
	}

	@Override
	public int getDataLength(boolean asPack) {
		if (this.typeBytes[3] >= 0)
			return super.getDataLength(asPack) + BASE_LENGTH + this.data.length;
		else 
			return super.getDataLength(asPack);
	}

	//@Override
	public int isValid(DBSet db, Long releaserReference) {
		
		if (this.data != null ) {
			//CHECK DATA SIZE
			if(data.length > 4000 || data.length < 1)
			{
				return INVALID_DATA_LENGTH;
			}
		}
			
		return super.isValid(db, releaserReference);
	}

}