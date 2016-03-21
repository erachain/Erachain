package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

//import database.BalanceMap;
import database.DBSet;
import qora.account.Account;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
//import qora.crypto.Crypto;
//import utils.Converter;
import qora.crypto.Crypto;



public class RecStatement extends Transaction {

	private static final int REC_ID = STATEMENT_RECORD;
	protected byte[] data;
	protected byte[] encrypted;
	protected byte[] isText;
	
	protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + IS_TEXT_LENGTH + ENCRYPTED_LENGTH + DATA_SIZE_LENGTH ; 
			//1 + TIMESTAMP_LENGTH + REFERENCE_LENGTH +  + CREATOR_LENGTH + SIGNATURE_LENGTH + RECIPIENT_LENGTH + AMOUNT_LENGTH + KEY_LENGTH;

	public RecStatement(PublicKeyAccount creator, byte feePow, byte[] data, byte[] isText, byte[] encrypted, long timestamp, byte[] reference) {
		super(REC_ID, creator, feePow, timestamp, reference);
		this.data = data;
		this.encrypted = encrypted;
		this.isText = isText;
	}
	public RecStatement(PublicKeyAccount creator, byte feePow, byte[] data, byte[] isText, byte[] encrypted, long timestamp, byte[] reference, byte[] signature) {
		this(creator, feePow, data, isText, encrypted, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}

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
		
	public static Transaction Parse(byte[] data) throws Exception
	{
		if (data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length");
		}

		int position = 0;

		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;

		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;

		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;

		//READ FEE POWER
		byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
		byte feePow = feePowBytes[0];
		position += 1;

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
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);

		return new RecStatement(creator, feePow, arbitraryData, isTextByte, encryptedByte, timestamp, reference, signatureBytes);

	}

	@Override
	public byte[] toBytes(boolean withSign) {

		byte[] data = new byte[0];

		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(REC_ID);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);

		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);

		//WRITE REFERENCE
		data = Bytes.concat(data, this.getReference());

		//WRITE FEE POWER
		byte[] feePowBytes = new byte[1];
		feePowBytes[0] = this.feePow;
		data = Bytes.concat(data, feePowBytes);

		//WRITE DATA SIZE
		byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
		data = Bytes.concat(data, dataSizeBytes);

		//WRITE DATA
		data = Bytes.concat(data, this.data);
		
		//WRITE ENCRYPTED
		data = Bytes.concat(data, this.encrypted);
		
		//WRITE ISTEXT
		data = Bytes.concat(data, this.isText);

		//SIGNATURE
		if (withSign) data = Bytes.concat(data, this.signature);

		return data;	
	}

	@Override
	public int getDataLength() {
		return TYPE_LENGTH + BASE_LENGTH + this.data.length;
	}

	//@Override
	public int isValid(DBSet db) {
		

		//CHECK DATA SIZE
		if(data.length > 4000 || data.length < 1)
		{
			return INVALID_DATA_LENGTH;
		}
		
		// TODO new super clss - without validation of amount
		int res = 0; //super.isValid(db);
		if (res > 0 & res != NO_BALANCE) return res;

		return VALIDATE_OK;
	}
	
	@Override
	public List<Account> getInvolvedAccounts() {
		return Arrays.asList(this.creator);
	}

	@Override
	public boolean isInvolved(Account account) {
		String address = account.getAddress();
		
		if(address.equals(creator.getAddress()))
		{
			return true;
		}
		
		return false;
	}

	public int calcBaseFee() {
		return calcCommonFee();
	}
}
