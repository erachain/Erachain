package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
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

public class PaymentTransaction extends TransactionAmount {

	private static final byte TYPE_ID = (byte)Transaction.PAYMENT_TRANSACTION;
	private static final String NAME_ID = "OLD: payment";
	private static final int BASE_LENGTH = TransactionAmount.BASE_LENGTH - KEY_LENGTH;
		
	public PaymentTransaction(byte[] typeBytes, PublicKeyAccount creator, Account recipient, BigDecimal amount, byte feePow, long timestamp, byte[] reference) 
	{
		super(typeBytes, NAME_ID, creator, feePow, recipient, amount, RIGHTS_KEY, timestamp, reference);
	}
	public PaymentTransaction(byte[] typeBytes, PublicKeyAccount creator, Account recipient, BigDecimal amount, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, recipient, amount, feePow, timestamp, reference);		
		this.signature = signature;
		this.calcFee();
	}
	// as pack
	public PaymentTransaction(byte[] typeBytes, PublicKeyAccount creator, Account recipient, BigDecimal amount, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, recipient, amount, (byte)0, 0l, reference);		
		this.signature = signature;
	}
	public PaymentTransaction(PublicKeyAccount creator, Account recipient, BigDecimal amount, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, recipient, amount, feePow, timestamp, reference, signature);
	}
	public PaymentTransaction(PublicKeyAccount creator, Account recipient, BigDecimal amount, byte feePow, long timestamp, byte[] reference) 
	{
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, recipient, amount, feePow, timestamp, reference);		
	}
	
	//GETTERS/SETTERS	
	//public static String getName() { return "OLD: Payment"; }


	//PARSE/CONVERT

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/RECIPIENT/AMOUNT
				
		return transaction;	
	}

	// releaserReference = null - not a pack
	// releaserReference = reference for releaser account - it is as pack
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

		/////		
		//READ RECIPIENT
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		Account recipient = new Account(Base58.encode(recipientBytes));
		position += RECIPIENT_LENGTH;
		
		//READ AMOUNT
		byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		position += AMOUNT_LENGTH;
				
		if (!asPack) {
			return new PaymentTransaction(typeBytes, creator, recipient, amount, feePow, timestamp, reference, signatureBytes);	
		} else {
			return new PaymentTransaction(typeBytes, creator, recipient, amount, reference, signatureBytes);	
		}
	}	
	
	
	@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference) 
	{

		boolean asPack = releaserReference != null;

		byte[] data = new byte[0];

		//WRITE TYPE
		data = Bytes.concat(data, this.typeBytes);

		if (!asPack) {
			//WRITE TIMESTAMP
			byte[] timestampBytes = Longs.toByteArray(this.timestamp);
			timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
			data = Bytes.concat(data, timestampBytes);
		}
		
		if (!asPack) {
			//WRITE REFERENCE
			data = Bytes.concat(data, this.getReference());
		}

		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());

		if (!asPack) {
			//WRITE FEE POWER
			byte[] feePowBytes = new byte[1];
			feePowBytes[0] = this.feePow;
			data = Bytes.concat(data, feePowBytes);
		}

		//SIGNATURE
		if (withSign) data = Bytes.concat(data, this.signature);
		
		//WRITE RECIPIENT
		data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));
		
		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
				
		return data;
	}

	@Override
	public int getDataLength(boolean asPack) 
	{
		if (asPack) {
			return BASE_LENGTH_AS_PACK;
		} else {
			return BASE_LENGTH;
		}
	}
	

	@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.amount);
		assetAmount = addAssetAmount(assetAmount, this.recipient.getAddress(), FEE_KEY, this.amount);
		
		return assetAmount;
	}
	
	public int calcBaseFee() {
		return calcCommonFee();
	}
}
