package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ntp.NTP;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import database.BalanceMap;
import database.DBSet;

public class TransferAssetTransaction extends TransactionAmount {

	private static final byte TYPE_ID = (byte)SEND_ASSET_TRANSACTION;
	private static final String NAME_ID = "OLD: Send Asset";
	//private static final int BASE_LENGTH = TransactionAmount.BASE_LENGTH; 
	
	
	public TransferAssetTransaction(byte[] typeBytes, PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, byte feePow, long timestamp, byte[] reference) 
	{
		super(typeBytes, NAME_ID, creator, feePow, recipient, amount, key, timestamp, reference);		
	}
	public TransferAssetTransaction(byte[] typeBytes, PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, recipient, key, amount, feePow, timestamp, reference);		
		this.signature = signature;
		this.calcFee();
	}
	public TransferAssetTransaction(byte[] typeBytes, PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, recipient, key, amount, (byte)0, 0l, reference);		
		this.signature = signature;
	}
	public TransferAssetTransaction(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, recipient, key, amount, feePow, timestamp, reference, signature);		
	}
	public TransferAssetTransaction(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, byte[] signature) 
	{
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, recipient, key, amount, (byte)0, 0l, null, signature);		
	}
	public TransferAssetTransaction(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, byte feePow, long timestamp, byte[] reference) 
	{
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, recipient, key, amount, feePow, timestamp, reference);		
	}
	public TransferAssetTransaction(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount) 
	{
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, recipient, key, amount, (byte)0, 0l, null);		
	}
	
	//GETTERS/SETTERS
	//public static String getName() { return "OLD: Send Asset"; }


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
				
		if (!asPack) {
			return new TransferAssetTransaction(typeBytes, creator, recipient, key, amount, feePow, timestamp, reference, signatureBytes);
		} else {
			return new TransferAssetTransaction(typeBytes, creator, recipient, key, amount, reference, signatureBytes);
		}
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		return transaction;	
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
	
	//VALIDATE
	
	//PROCESS/ORPHAN
	

	//REST
	
}
