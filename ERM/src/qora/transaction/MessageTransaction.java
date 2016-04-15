package qora.transaction;

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

import database.BalanceMap;
import database.DBSet;
import qora.account.Account;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import utils.Converter;



public class MessageTransaction extends TransactionAmount {

	private static final byte TYPE_ID = (byte)Transaction.MESSAGE_TRANSACTION;
	private static final String NAME_ID = "Send";

	protected byte[] data;
	protected byte[] encrypted;
	protected byte[] isText;
	
	protected static final int BASE_LENGTH_AS_PACK = TransactionAmount.BASE_LENGTH_AS_PACK + IS_TEXT_LENGTH + ENCRYPTED_LENGTH + DATA_SIZE_LENGTH;
	protected static final int BASE_LENGTH = TransactionAmount.BASE_LENGTH + IS_TEXT_LENGTH + ENCRYPTED_LENGTH + DATA_SIZE_LENGTH;

	public MessageTransaction(byte[] typeBytes, PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, byte[] data, byte[] isText, byte[] encrypted, long timestamp, byte[] reference) {
		super(typeBytes, NAME_ID, creator, feePow, recipient, amount, key, timestamp, reference);

		this.data = data;
		this.encrypted = encrypted;
		this.isText = isText;
		this.feePow = feePow;
	}
	public MessageTransaction(byte[] typeBytes, PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, byte[] data, byte[] isText, byte[] encrypted, long timestamp, byte[] reference, byte[] signature) {
		this(typeBytes, creator, feePow, recipient, key, amount, data, isText, encrypted, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}
	// as pack
	public MessageTransaction(byte[] typeBytes, PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, byte[] data, byte[] isText, byte[] encrypted, byte[] reference, byte[] signature) {
		this(typeBytes, creator, (byte)0, recipient, key, amount, data, isText, encrypted, 0l, reference);
		this.signature = signature;
	}
	public MessageTransaction(PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, byte[] data, byte[] isText, byte[] encrypted, long timestamp, byte[] reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, recipient, key, amount, data, isText, encrypted, timestamp, reference);
	}
	// as pack
	public MessageTransaction(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, byte[] data, byte[] isText, byte[] encrypted, byte[] reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, (byte)0, recipient, key, amount, data, isText, encrypted, 0l, reference);
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
		return (Arrays.equals(this.isText,new byte[1]))?false:true;
	}
	
	public boolean isEncrypted()
	{
		return (Arrays.equals(this.encrypted,new byte[1]))?false:true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();

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
		
		return transaction;	
	}

	//PARSE/CONVERT
	
	public static Transaction Parse(byte[] data, byte[] releaserReference) throws Exception{

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

		byte[] reference;
		if (!asPack) {
			//READ REFERENCE
			reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
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
		
		//READ KEY
		byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
		long key = Longs.fromByteArray(keyBytes);	
		position += KEY_LENGTH;
		
		//READ AMOUNT
		byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		position += AMOUNT_LENGTH;
				
		//READ DATA SIZE
		byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
		int dataSize = Ints.fromByteArray(dataSizeBytes);	
		position += DATA_SIZE_LENGTH;

		//READ DATA
		byte[] arbitraryData = Arrays.copyOfRange(data, position, position + dataSize);
		position += dataSize;
		
		byte[] encryptedByte = Arrays.copyOfRange(data, position, position + ENCRYPTED_LENGTH);
		position += ENCRYPTED_LENGTH;
		
		byte[] isTextByte = Arrays.copyOfRange(data, position, position + IS_TEXT_LENGTH);
		position += IS_TEXT_LENGTH;
		
		if (!asPack) {
			return new MessageTransaction(typeBytes, creator, feePow, recipient, key, amount, arbitraryData, isTextByte, encryptedByte, timestamp, reference, signatureBytes);
		} else {
			return new MessageTransaction(typeBytes, creator, recipient, key, amount, arbitraryData, isTextByte, encryptedByte, reference, signatureBytes);
		}

	}

	@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference) {

		byte[] data = super.toBytes(withSign, releaserReference);

		//WRITE DATA SIZE
		byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
		data = Bytes.concat(data, dataSizeBytes);

		//WRITE DATA
		data = Bytes.concat(data, this.data);
		
		//WRITE ENCRYPTED
		data = Bytes.concat(data, this.encrypted);
		
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
	public int isValid(DBSet db, byte[] releaserReference) {
		
		//CHECK DATA SIZE
		if(data.length > 4000 || data.length < 1)
		{
			return INVALID_DATA_LENGTH;
		}
			
		return super.isValid(db, releaserReference);
	}

}