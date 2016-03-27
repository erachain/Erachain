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
import qora.account.PrivateKeyAccount;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import utils.Converter;


public class PersonalizeRecord extends RecStatement {

	private static final byte TYPE_ID = (byte)Transaction.PERSONALIZE_RECORD;
	private static final String NAME_ID = "Personalize";
	private static final int APPLICANT_LENGTH = Transaction.CREATOR_LENGTH;
	private static final int DURATION_LENGTH = 2;
	private static final BigDecimal MIN_ERM_BALANCE = BigDecimal.valueOf(1000).setScale(8);
	private static final BigDecimal MIN_VOTE_BALANCE = BigDecimal.valueOf(10).setScale(8);
	
	protected PublicKeyAccount applicant;
	protected byte[] applicantSignature;
	protected Integer duration; // duration in days 
	protected byte[] data;
	
	protected static final int BASE_LENGTH = RecStatement.BASE_LENGTH - IS_TEXT_LENGTH
			+ APPLICANT_LENGTH + DURATION_LENGTH + SIGNATURE_LENGTH;

	public PersonalizeRecord(byte[] typeBytes, PublicKeyAccount creator, byte feePow, PublicKeyAccount applicant, byte[] data, int duration, long timestamp, byte[] reference) {
		super(typeBytes, creator, feePow, data, new byte[]{1}, timestamp, reference);

		this.NAME = NAME_ID;
		this.applicant = applicant;
		this.duration = duration; 
		this.data = data;
		this.feePow = feePow;
	}
	public PersonalizeRecord(byte[] typeBytes, PublicKeyAccount creator, byte feePow, PublicKeyAccount applicant, byte[] data, int duration, long timestamp, byte[] reference, byte[] signature, byte[] applicantSignature) {
		this(typeBytes, creator, feePow, applicant, data, duration, timestamp, reference);
		this.signature = signature;
		this.applicantSignature = applicantSignature;
		this.calcFee();
	}
	public PersonalizeRecord(PublicKeyAccount creator, byte feePow, PublicKeyAccount applicant, byte[] data, int duration, long timestamp, byte[] reference, byte[] signature, byte[] applicantSignature) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, applicant, data, duration, timestamp, reference, signature, applicantSignature);
	}
	public PersonalizeRecord(PublicKeyAccount creator, byte feePow, PublicKeyAccount applicant, byte[] data, int duration, long timestamp, byte[] reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, applicant, data, duration, timestamp, reference);
	}
	
	//GETTERS/SETTERS

	//public static String getName() { return "Send"; }

	public PublicKeyAccount getApplicant() 
	{
		return this.applicant;
	}
	public byte[] getApplicantSignature() 
	{
		return this.applicantSignature;
	}
	public int getDuration() 
	{
		return this.duration;
	}
			
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();

		//ADD CREATOR/SERVICE/DATA
		transaction.put("data", new String(this.data, Charset.forName("UTF-8")));
		transaction.put("applicant", this.applicant.getAddress());
		transaction.put("duration", this.duration);
		
		return transaction;	
	}

	public void signApplicant(PrivateKeyAccount applicant)
	{
		byte[] data = this.toBytes( false );
		if ( data == null ) return;

		this.applicantSignature = Crypto.getInstance().sign(applicant, data);
		this.calcFee(); // need for recal!
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

		//READ APPLICANT
		byte[] applicantBytes = Arrays.copyOfRange(data, position, position + APPLICANT_LENGTH);
		PublicKeyAccount applicant = new PublicKeyAccount(applicantBytes);
		position += APPLICANT_LENGTH;
		
		//READ DATA SIZE
		byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
		int dataSize = Ints.fromByteArray(dataSizeBytes);	
		position += DATA_SIZE_LENGTH;

		//READ DATA
		byte[] arbitraryData = Arrays.copyOfRange(data, position, position + dataSize);
		position += dataSize;

		// READ DURATION
		int duration = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + DURATION_LENGTH));
		position += DURATION_LENGTH;
				
		//READ FEE POWER
		byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
		byte feePow = feePowBytes[0];
		position += 1;

		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		position += SIGNATURE_LENGTH;

		//READ APPLICANT SIGNATURE
		byte[] applSignatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		//position += SIGNATURE_LENGTH;

		return new PersonalizeRecord(typeBytes, creator, feePow, applicant, arbitraryData, duration, timestamp, reference, signatureBytes, applSignatureBytes);

	}

	@Override
	public byte[] toBytes(boolean withSign) {

		byte[] data = new byte[0];

		//WRITE TYPE
		data = Bytes.concat(data, this.typeBytes);

		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);

		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);

		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());

		//WRITE APPLICANT
		data = Bytes.concat(data, this.applicant.getPublicKey());

		//WRITE DATA SIZE
		byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
		data = Bytes.concat(data, dataSizeBytes);

		//WRITE DATA
		data = Bytes.concat(data, this.data);

		//WRITE DURATION
		data = Bytes.concat(data, Ints.toByteArray(this.duration));

		//WRITE FEE POWER
		byte[] feePowBytes = new byte[1];
		feePowBytes[0] = this.feePow;
		data = Bytes.concat(data, feePowBytes);

		//SIGNATURE
		if (withSign) {
			data = Bytes.concat(data, this.signature);
			data = Bytes.concat(data, this.applicantSignature);
		}

		return data;	
	}

	//VALIDATE

	@Override
	public boolean isSignatureValid() {

		if ( this.signature == null | this.signature.length != 64 | this.signature == new byte[64]
			| this.applicantSignature == null | this.applicantSignature.length != 64 | this.applicantSignature == new byte[64]) return false;
		
		byte[] data = this.toBytes( false );
		if ( data == null ) return false;

		Crypto crypto = Crypto.getInstance();
		return crypto.verify(creator.getPublicKey(), signature, data)
				& crypto.verify(this.applicant.getPublicKey(), this.applicantSignature, data);
	}

	public int isValid(DBSet db) {
		
		//CHECK DATA SIZE
		if(duration < 100 | duration > 777)
		{
			return INVALID_DURATION;
		}
	
		/* VALID in all cases!
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.applicant.getAddress()))
		{
			return INVALID_ADDRESS;
		}
		*/

		int res = super.isValid(db);
		if (res > 0) return res;
		
		BigDecimal balERM = this.creator.getConfirmedBalance(0l, db);
		if ( balERM.compareTo(MIN_ERM_BALANCE)<0 )
		{
			return Transaction.INVALID_AMOUNT;
		}
		BigDecimal balVOTE = this.creator.getConfirmedBalance(0l, db);
		if ( balVOTE.compareTo(MIN_VOTE_BALANCE)<0 )
		{
			return Transaction.INVALID_AMOUNT;
		}

		
		return VALIDATE_OK;
	}

}