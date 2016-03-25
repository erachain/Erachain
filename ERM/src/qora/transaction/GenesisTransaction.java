package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import qora.account.Account;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.BalanceMap;
import database.DBSet;

public class GenesisTransaction extends Transaction {

	private static final byte TYPE_ID = (byte)Transaction.GENESIS_TRANSACTION;
	private static final String NAME_ID = "OLD: Genesis";
	private static final int RECIPIENT_LENGTH = TransactionAmount.RECIPIENT_LENGTH;
	private static final int AMOUNT_LENGTH = TransactionAmount.AMOUNT_LENGTH;

	private static final int BASE_LENGTH = SIMPLE_TYPE_LENGTH + TIMESTAMP_LENGTH + RECIPIENT_LENGTH + AMOUNT_LENGTH;
	
	private Account recipient;
	private BigDecimal amount;
	
	public GenesisTransaction(Account recipient, BigDecimal amount, long timestamp)
	{
		super(TYPE_ID, NAME_ID, timestamp);
		this.recipient = recipient;
		this.amount = amount;
		this.signature = this.generateSignature();
	}

	//GETTERS/SETTERS
	//public static String getName() { return "OLD: Genesis"; }

	public byte[] generateSignature() {
		
		//return generateSignature1(this.recipient, this.amount, this.timestamp);
		byte[] data = this.toBytes( false );

		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);
				
		return digest;

	}
	
	public Account getRecipient()
	{
		return this.recipient;
	}
	
	public BigDecimal getAmount()
	{
		return this.amount;
	}

	//PARSE/CONVERT
	
	public static Transaction Parse(byte[] data) throws Exception{
		
		//CHECK IF WE MATCH BLOCK LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length");
		}
		
		// READ TYPE
		//byte[] typeBytes = Arrays.copyOfRange(data, 0, SIMPLE_TYPE_LENGTH);
		int position = SIMPLE_TYPE_LENGTH;
	
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;
		
		//READ RECIPIENT
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		Account recipient = new Account(Base58.encode(recipientBytes));
		position += RECIPIENT_LENGTH;
		
		//READ AMOUNT
		byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		
		return new GenesisTransaction(recipient, amount, timestamp);	
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() {
		
		//GET BASE
		JSONObject transaction = this.getJsonBase();
		
		//ADD RECIPIENT/AMOUNT
		transaction.put("recipient", this.recipient.getAddress());
		transaction.put("amount", this.amount.toPlainString());
		
		return transaction;	
	}
	
	@Override
	public byte[] toBytes(boolean withSign) 
	{
		
		//WRITE TYPE
		byte[] data = new byte[]{TYPE_ID};
		//byte[] typeBytes = Ints.toByteArray(TYPE_ID);
		//typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		//data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE RECIPIENT
		data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));
		
		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		
		
		if (withSign) data = Bytes.concat(data, amountBytes);
		
		return data;
	}

	@Override
	public int getDataLength() 
	{
		return BASE_LENGTH;
	}

	//VALIDATE
	@Override
	public boolean isSignatureValid()
	{		
		byte[] digest = this.getSignature();
		return Arrays.equals(digest, this.signature);
	}

	@Override
	public int isValid(DBSet db) 
	{	
		//CHECK IF AMOUNT IS POSITIVE
		if(this.amount.compareTo(BigDecimal.ZERO) == -1)
		{
			return NEGATIVE_AMOUNT;
		}
		
		//CHECK IF ADDRESS IS VALID
		if(!Crypto.getInstance().isValidAddress(this.recipient.getAddress()))
		{
			return INVALID_ADDRESS;
		}

		return VALIDATE_OK;
	}
	
	//PROCESS/ORPHAN
	
	@Override
	public void process(DBSet db) {
		
		//UPDATE BALANCE
		this.recipient.setConfirmedBalance(this.amount, db);
		
		//SET AS REFERENCE
		recipient.setLastReference(this.signature, db);
		
	}
	
	@Override
	public void orphan(DBSet db) 
	{
		//UNDO BALANCE
		this.recipient.setConfirmedBalance(BigDecimal.ZERO, db);
		
		//UNDO REFERENCE
		this.recipient.removeReference(db);
	}
	
	//REST
	
	@Override
	public PublicKeyAccount getCreator()
	{
		return null;
	}
	
	@Override
	public List<Account> getInvolvedAccounts()
	{
		return Arrays.asList(this.recipient);
	}

	@Override
	public boolean isInvolved(Account account) 
	{	
		return this.recipient.getAddress().equals(account.getAddress());		
	}

	//@Override
	public BigDecimal viewAmount(Account account) 
	{		
		if(this.recipient.getAddress().equals(account.getAddress()))
		{
			return this.amount;
		}
		
		return BigDecimal.ZERO;
	}

	//@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<String, Map<Long, BigDecimal>>();
		
		assetAmount = addAssetAmount(assetAmount, this.recipient.getAddress(), BalanceMap.QORA_KEY, this.amount);
		
		return assetAmount;
	}
	public int calcBaseFee() {
		return 0;
	}
}
