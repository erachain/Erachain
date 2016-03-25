package qora.transaction;

//import java.math.BigDecimal;
//import java.math.BigInteger;
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
//import qora.crypto.Crypto;



public class RecStatement extends Transaction {

	private static final byte TYPE_ID = (byte) STATEMENT_RECORD;
	private static final String NAME_ID = "Statement";
	protected byte[] data;
	protected byte[] isText;
	
	protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + IS_TEXT_LENGTH + DATA_SIZE_LENGTH ; 

	public RecStatement(byte[] typeBytes, PublicKeyAccount creator, byte feePow, byte[] data, byte[] isText, long timestamp, byte[] reference) {
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);
		this.data = data;
		this.isText = isText;
	}
	public RecStatement(byte[] typeBytes, PublicKeyAccount creator, byte feePow, byte[] data, byte[] isText, long timestamp, byte[] reference, byte[] signature) {
		this(typeBytes, creator, feePow, data, isText, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}
	public RecStatement(byte prop1, byte prop2, byte prop3, PublicKeyAccount creator, byte feePow, byte[] data, byte[] isText, long timestamp, byte[] reference)
	{
		this(new byte[]{TYPE_ID, prop1, prop2, prop3}, creator, feePow, data, isText, timestamp, reference);
	}

	//GETTERS/SETTERS

	public static byte[] makeProps() {
		return new byte[]{TYPE_ID, 0, 0, 0};
	}
	
	//public static String getName() { return "Statement"; }

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
		
	public static Transaction Parse(byte[] data) throws Exception
	{
		if (data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length");
		}


		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;

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
		position += FEE_POWER_LENGTH;

		//READ DATA SIZE
		byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
		int dataSize = Ints.fromByteArray(dataSizeBytes);	
		position += DATA_SIZE_LENGTH;

		//READ DATA
		byte[] arbitraryData = Arrays.copyOfRange(data, position, position + dataSize);
		position += dataSize;
				
		byte[] isTextByte = Arrays.copyOfRange(data, position, position + IS_TEXT_LENGTH);
		position += IS_TEXT_LENGTH;
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);

		return new RecStatement(typeBytes, creator, feePow, arbitraryData, isTextByte, timestamp, reference, signatureBytes);

	}

	//@Override
	public byte[] toBytes(boolean withSign) {

		byte[] data = super.toBytes(withSign);

		//WRITE DATA SIZE
		byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
		data = Bytes.concat(data, dataSizeBytes);

		//WRITE DATA
		data = Bytes.concat(data, this.data);
				
		//WRITE ISTEXT
		data = Bytes.concat(data, this.isText);

		//SIGNATURE
		if (withSign) data = Bytes.concat(data, this.signature);

		return data;	
	}

	@Override
	public int getDataLength() {
		return BASE_LENGTH + this.data.length;
	}

	//@Override
	public int isValid(DBSet db) {
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}

		//CHECK DATA SIZE
		if(data.length > 4000 || data.length < 10)
		{
			return INVALID_DATA_LENGTH;
		}

		// CHECK FEE
		if(this.creator.getConfirmedBalance(FEE_KEY, db).compareTo(this.fee) == -1)
		{
			return NOT_ENOUGH_FEE;
		}

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
