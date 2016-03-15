package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

//import com.google.common.primitives.Longs;

import database.BalanceMap;
import database.DBSet;
//import database.DBSet;
import qora.account.Account;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import utils.Converter;



public class JsonTransaction extends Transaction {

	protected byte[] data;
	protected Account recipient;
	protected BigDecimal amount;
	protected long key;
	protected byte[] encrypted;
	protected byte[] isText;
	
	protected static final int BASE_LENGTH = 1 + TIMESTAMP_LENGTH + REFERENCE_LENGTH + IS_TEXT_LENGTH + ENCRYPTED_LENGTH + CREATOR_LENGTH + DATA_SIZE_LENGTH + SIGNATURE_LENGTH + RECIPIENT_LENGTH + AMOUNT_LENGTH + KEY_LENGTH;

	public JsonTransaction(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, byte[] data, byte[] isText, byte[] encrypted, long timestamp, byte[] reference) {
		super(JSON_TRANSACTION, creator, timestamp, reference);

		this.data = data;
		this.recipient = recipient;
		this.key = key;
		this.amount = amount;
		this.encrypted = encrypted;
		this.isText = isText;
	}
	public JsonTransaction(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, byte feePow, byte[] data, byte[] isText, byte[] encrypted, long timestamp, byte[] reference, byte[] signature) {
		this(creator, recipient, key, amount, data, isText, encrypted, timestamp, reference);
		this.signature = signature;
		this.feePow = feePow;
		calcFee();
	}
	public JsonTransaction(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, byte feePow, byte[] data, byte[] isText, byte[] encrypted, long timestamp, byte[] reference) {
		this(creator, recipient, key, amount, data, isText, encrypted, timestamp, reference);
		this.feePow = feePow;
		calcFee();
	}
	
	public byte[] getData() 
	{
		return this.data;
	}

	public Account getRecipient()
	{
		return this.recipient;
	}

	public long getKey()
	{
		return this.key;
	}
	
	public BigDecimal getAmount()
	{
		return this.amount;
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
		transaction.put("creator", this.creator.getAddress());
		transaction.put("recipient", this.recipient.getAddress());
		transaction.put("asset", this.key);
		transaction.put("amount", this.amount.toPlainString());
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
	
	@Override
	public List<Account> getInvolvedAccounts() {
		return Arrays.asList(this.creator, this.recipient);
	}

	@Override
	public boolean isInvolved(Account account) {
		String address = account.getAddress();
		
		if(address.equals(creator.getAddress()) || address.equals(recipient.getAddress()))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public BigDecimal getAmount(Account account) {
		BigDecimal amount = BigDecimal.ZERO.setScale(8);
		String address = account.getAddress();
		
		//IF SENDER
		if(address.equals(this.creator.getAddress()))
		{
			amount = amount.subtract(this.fee);
		}

		//IF QORA ASSET
		if(this.key == BalanceMap.QORA_KEY)
		{
			//IF SENDER
			if(address.equals(this.creator.getAddress()))
			{
				amount = amount.subtract(this.amount);
			}
			
			//IF RECIPIENT
			if(address.equals(this.recipient.getAddress()))
			{
				amount = amount.add(this.amount);
			}
		}
		
		return amount;
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

		//READ SENDER
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
				
		//READ FEE POWER
		byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
		byte feePow = feePowBytes[0];
		position += 1;

		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);

		return new JsonTransaction(creator, recipient, key, amount, feePow, arbitraryData, isTextByte, encryptedByte, timestamp, reference, signatureBytes);

	}

	@Override
	public byte[] toBytes(boolean withSign) {

		byte[] data = new byte[0];

		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(JSON_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);

		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);

		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);

		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());

		//WRITE RECIPIENT
		data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));

		//WRITE KEY
		byte[] keyBytes = Longs.toByteArray(this.key);
		keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
		data = Bytes.concat(data, keyBytes);
		
		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);

		//WRITE DATA SIZE
		byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
		data = Bytes.concat(data, dataSizeBytes);

		//WRITE DATA
		data = Bytes.concat(data, this.data);
		
		//WRITE ENCRYPTED
		data = Bytes.concat(data, this.encrypted);
		
		//WRITE ISTEXT
		data = Bytes.concat(data, this.isText);

		//WRITE FEE POWER
		byte[] feePowBytes = new byte[1];
		feePowBytes[0] = this.feePow;
		data = Bytes.concat(data, feePowBytes);

		//SIGNATURE
		if (withSign) data = Bytes.concat(data, this.signature);

		return data;	
	}

	@Override
	public int getDataLength() {
		return TYPE_LENGTH + BASE_LENGTH + this.data.length;
	}


	@Override
	public int isValid(DBSet db) {
		//CHECK IF RELEASED
		if( db.getBlockMap().getLastBlock().getHeight(db) < Transaction.getMESSAGE_BLOCK_HEIGHT_RELEASE())
		{
			return NOT_YET_RELEASED;
		}
		
		if( this.getTimestamp() < Transaction.getPOWFIX_RELEASE())
		{
			return NOT_YET_RELEASED;
		}
		
		//CHECK DATA SIZE
		if(data.length > 4000 || data.length < 1)
		{
			return INVALID_DATA_LENGTH;
		}
	
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.recipient.getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//REMOVE FEE
		DBSet fork = db.fork();
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(fork).subtract(this.fee), fork);

		//CHECK IF SENDER HAS ENOUGH ASSET BALANCE
		if(this.creator.getConfirmedBalance(this.key, fork).compareTo(this.amount) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF SENDER HAS ENOUGH QORA BALANCE
		if(this.creator.getConfirmedBalance(fork).compareTo(BigDecimal.ZERO) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
		
		//CHECK IF AMOUNT IS POSITIVE. 
		//NOW IN V3 MAY BE ZERO
		if(this.amount.compareTo(BigDecimal.ZERO) < 0)
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

	@Override
	public void process(DBSet db) {
		//UPDATE SENDER
		process_fee(db);
		this.creator.setConfirmedBalance(this.key, this.creator.getConfirmedBalance(this.key, db).subtract(this.amount), db);
						
		//UPDATE RECIPIENT
		this.recipient.setConfirmedBalance(this.key, this.recipient.getConfirmedBalance(this.key, db).add(this.amount), db);
		
		//UPDATE REFERENCE OF SENDER
		this.creator.setLastReference(this.signature, db);
		
		//UPDATE REFERENCE OF RECIPIENT
		if(this.key == BalanceMap.QORA_KEY)
		{
			if(Arrays.equals(this.recipient.getLastReference(db), new byte[0]))
			{
				this.recipient.setLastReference(this.signature, db);
			}
		}
	}

	@Override
	public void orphan(DBSet db) {
		//UPDATE SENDER
		orphan_fee(db);
		this.creator.setConfirmedBalance(this.key, this.creator.getConfirmedBalance(this.key, db).add(this.amount), db);
						
		//UPDATE RECIPIENT
		this.recipient.setConfirmedBalance(this.key, this.recipient.getConfirmedBalance(this.key, db).subtract(this.amount), db);
		
		//UPDATE REFERENCE OF SENDER
		this.creator.setLastReference(this.reference, db);
		
		//UPDATE REFERENCE OF RECIPIENT
		if(this.key == BalanceMap.QORA_KEY)
		{
			if(Arrays.equals(this.recipient.getLastReference(db), this.signature))
			{
				this.recipient.removeReference(db);
			}	
		}
	}
	
	@Override
	// TODO hkey
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		return subAssetAmount(null, this.creator.getAddress(), BalanceMap.QORA_KEY, this.fee);
	}

	public BigDecimal calcBaseFee() {
		return calcCommonFee();
	}
}

