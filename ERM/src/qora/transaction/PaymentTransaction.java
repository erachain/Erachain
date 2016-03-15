package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import qora.account.Account;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.BalanceMap;
import database.DBSet;

public class PaymentTransaction extends Transaction {

	private static final int BASE_LENGTH = 1 + TIMESTAMP_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH + RECIPIENT_LENGTH + AMOUNT_LENGTH + SIGNATURE_LENGTH;

	private BigDecimal amount;
	private Account recipient;
		
	public PaymentTransaction(PublicKeyAccount creator, Account recipient, BigDecimal amount, long timestamp, byte[] reference) 
	{
		super(PAYMENT_TRANSACTION, creator, timestamp, reference);
		
		this.recipient = recipient;
		this.amount = amount;
	}
	public PaymentTransaction(PublicKeyAccount creator, Account recipient, BigDecimal amount, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(creator, recipient, amount, timestamp, reference);		
		this.signature = signature;
		this.feePow = feePow;
		this.calcFee();
	}
	public PaymentTransaction(PublicKeyAccount creator, Account recipient, BigDecimal amount, byte feePow, long timestamp, byte[] reference) 
	{
		this(creator, recipient, amount, timestamp, reference);
		this.feePow = feePow;
		this.calcFee();
	}
	
	//GETTERS/SETTERS	
	public BigDecimal getAmount() 
	{
		return this.amount;
	}
	public Account getRecipient() 
	{
		return this.recipient;
	}
	
	//PARSE/CONVERT
	
	public static Transaction Parse(byte[] data) throws Exception{
		
		//CHECK IF WE MATCH BLOCK LENGTH
		if(data.length < BASE_LENGTH)
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
		
		//READ RECIPIENT
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		Account recipient = new Account(Base58.encode(recipientBytes));
		position += RECIPIENT_LENGTH;
		
		//READ AMOUNT
		byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		position += AMOUNT_LENGTH;
		
		//READ FEE POWER
		byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
		byte feePow = feePowBytes[0];
		position += 1;
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new PaymentTransaction(creator, recipient, amount, feePow, timestamp, reference, signatureBytes);	
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD CREATOR/RECIPIENT/AMOUNT
		transaction.put("creator", this.creator.getAddress());
		transaction.put("recipient", this.recipient.getAddress());
		transaction.put("amount", this.amount.toPlainString());
				
		return transaction;	
	}
	
	@Override
	public byte[] toBytes(boolean withSign) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(PAYMENT_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE CREATOR
		data = Bytes.concat(data , this.creator.getPublicKey());
		
		//WRITE RECIPIENT
		data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));
		
		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		//WRITE FEE POWER
		byte[] feePowBytes = new byte[1];
		feePowBytes[0] = this.feePow;
		data = Bytes.concat(data, feePowBytes);

		//SIGNATURE
		if (withSign) data = Bytes.concat(data, this.signature);
		
		return data;
	}

	@Override
	public int getDataLength() 
	{
		return TYPE_LENGTH + BASE_LENGTH;
	}
	
	//VALIDATE	
	@Override
	public int isValid(DBSet db) 
	{
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.recipient.getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//CHECK IF CREATOR HAS ENOUGH MONEY
		if(this.creator.getBalance(1, db).compareTo(this.amount.add(this.fee)) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
		
		//CHECK IF AMOUNT IS POSITIVE
		if(this.amount.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_AMOUNT;
		}
		
		//CHECK IF FEE IS POSITIVE
		if(this.fee.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_FEE;
		}
		
		return VALIDATE_OK;
	}

	//PROCESS/ORPHAN
	
	@Override
	public void process(DBSet db) 
	{
		//UPDATE CREATOR
		process_fee(db);
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).subtract(this.amount), db);
						
		//UPDATE RECIPIENT
		this.recipient.setConfirmedBalance(this.recipient.getConfirmedBalance(db).add(this.amount), db);
		
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.signature, db);
		
		//UPDATE REFERENCE OF RECIPIENT
		if(Arrays.equals(this.recipient.getLastReference(db), new byte[0]))
		{
			this.recipient.setLastReference(this.signature, db);
		}		
	}

	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE CREATOR
		orphan_fee(db);
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).add(this.amount), db);
						
		//UPDATE RECIPIENT
		this.recipient.setConfirmedBalance(this.recipient.getConfirmedBalance(db).subtract(this.amount), db);
		
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.reference, db);
		
		///UPDATE REFERENCE OF RECIPIENT
		if(Arrays.equals(this.recipient.getLastReference(db), this.signature))
		{
			this.recipient.removeReference(db);
		}	
	}

	//REST
		
	@Override
	public List<Account> getInvolvedAccounts()
	{
		return Arrays.asList(this.creator, this.recipient);
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(creator.getAddress()) || address.equals(recipient.getAddress()))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public BigDecimal getAmount(Account account) 
	{
		String address = account.getAddress();
		
		//CHECK OF BOTH CREATOR AND RECIPIENT
		if(address.equals(creator.getAddress()) && address.equals(recipient.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		
		//CHECK IF ONLY CREATOR
		if(address.equals(creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.amount).subtract(this.fee);
		}
		
		//CHECK IF ONLY RECIPIENT
		if(address.equals(recipient.getAddress()))
		{
			return this.amount;
		}
		
		return BigDecimal.ZERO;
	}

	@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), BalanceMap.QORA_KEY, this.fee);
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), BalanceMap.QORA_KEY, this.amount);
		assetAmount = addAssetAmount(assetAmount, this.recipient.getAddress(), BalanceMap.QORA_KEY, this.amount);
		
		return assetAmount;
	}
	
	public BigDecimal calcBaseFee() {
		return calcCommonFee();
	}
}
