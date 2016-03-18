package qora.transaction;

//import java.util.logging.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
//import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.BalanceMap;
//import database.BalanceMapHKey;
import database.DBSet;
import qora.account.Account;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;
//import utils.Converter;


public class AccountingTransactionHKey2 extends TransactionAmount {
	
	protected static final int HASH_KEY_LENGTH = 1;
	protected static final int BASE_LENGTH = AccountingTransaction.BASE_LENGTH - KEY_LENGTH + HASH_KEY_LENGTH;

	protected byte[] data;

	protected Account recipient;
	protected BigDecimal amount;
	protected byte[] hkey;
	protected byte[] encrypted;
	protected byte[] isText;
	
	public AccountingTransactionHKey2(PublicKeyAccount creator, Account recipient, BigDecimal amount, byte[] hkey, byte[] data, byte[] isText, byte[] encrypted, long timestamp, byte[] reference) {

		super(ACCOUNTING_TRANSACTION, creator, recipient, amount, 0l, timestamp, reference);

		this.hkey = hkey;

		this.data = data;
		this.encrypted = encrypted;
		this.isText = isText;

	}
	public AccountingTransactionHKey2(PublicKeyAccount creator, Account recipient, BigDecimal amount, byte[] hkey, byte feePow, byte[] data, byte[] isText, byte[] encrypted, long timestamp, byte[] reference, byte[] signature) {

		this(creator, recipient, amount, hkey, data, isText, encrypted, timestamp, reference);
		this.feePow = feePow;
		this.signature = signature;
		this.calcFee();

	}
	public AccountingTransactionHKey2(PublicKeyAccount creator, Account recipient, BigDecimal amount, byte[] hkey, byte feePow, byte[] data, byte[] isText, byte[] encrypted, long timestamp, byte[] reference) {

		this(creator, recipient, amount, hkey, data, isText, encrypted, timestamp, reference);
		this.feePow = feePow;
		this.calcFee();

	}
	
	/*
	public Account getSender()
	{
		return this.creator;
	}
	*/

	public byte[] getData() 
	{
		return this.data;
	}

	public Account getRecipient()
	{
		return this.recipient;
	}

	public byte[] getHKey()
	{
		return this.hkey;
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
		transaction.put("hkey", this.hkey);
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
	public int getDataLength() {
		return TYPE_LENGTH + BASE_LENGTH + this.hkey.length + this.data.length;
	}

	//@Override
	public byte[] toBytes(boolean withSign) {

		byte[] data = new byte[0];

		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(ACCOUNTING_TRANSACTION);
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

		//WRITE HKEY SIZE
		byte[] hkeySizeBytes = Ints.toByteArray(this.hkey.length);
		//Integer ii = this.hkey.length;
		//ii.byteValue()
		//Logger.getGlobal().info("byte " + hkeySizeBytes[3]);
		data = Bytes.concat(data, new byte[hkeySizeBytes[3]]);

		//WRITE HKEY
		data = Bytes.concat(data, this.hkey);
		
		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte [] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);

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

		//READ RECIPIENT
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		Account recipient = new Account(Base58.encode(recipientBytes));
		position += RECIPIENT_LENGTH;

		//READ HKEY SIZE
		byte[] hkeySizeBytes = Arrays.copyOfRange(data, position, position + 1);
		int hkeySize = Byte.toUnsignedInt(hkeySizeBytes[0]);	
		position += 1;

		//READ HKEY
		byte[] hkey = Arrays.copyOfRange(data, position, position + hkeySize);
		position += hkeySize;
		
		//READ AMOUNT
		byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		position += AMOUNT_LENGTH;

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

		return new AccountingTransactionHKey2(creator, recipient, amount, hkey, feePow, arbitraryData, isTextByte, encryptedByte, timestamp, reference, signatureBytes);

	}

	@Override
	public int isValid(DBSet db) {
		
		int res = super.isValid(db);
		if (res > 0) return res;

		//CHECK DATA SIZE
		if(data.length > 4000 || data.length < 1)
		{
			return INVALID_DATA_LENGTH;
		}
	
		return VALIDATE_OK;
	}

	@Override
	public void process(DBSet db) {
		//UPDATE SENDER
		process_fee(db);
		//this.creator.setConfirmedBalance(this.hkey, this.creator.getConfirmedBalance(this.hkey, db).subtract(this.amount), db);
						
		//UPDATE RECIPIENT
		//this.recipient.setConfirmedBalance(this.hkey, this.recipient.getConfirmedBalance(this.hkey, db).add(this.amount), db);
		
		//UPDATE REFERENCE OF SENDER
		this.creator.setLastReference(this.signature, db);
		
	}

	@Override
	public void orphan(DBSet db) {
		//UPDATE SENDER
		orphan_fee(db);
		//this.creator.setConfirmedBalance(this.hkey, this.creator.getConfirmedBalance(this.hkey, db).add(this.amount), db);
						
		//UPDATE RECIPIENT
		//this.recipient.setConfirmedBalance(this.hkey, this.recipient.getConfirmedBalance(this.hkey, db).subtract(this.amount), db);
		
		//UPDATE REFERENCE OF SENDER
		this.creator.setLastReference(this.reference, db);
		
	}

	@Override
	public BigDecimal viewAmount(Account account) {
		
		return this.amount;
	}
	

	@Override
	// TODO hkey
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		return subAssetAmount(null, this.creator.getAddress(), FEE_KEY, this.fee);
	}

	public BigDecimal calcBaseFee() {
		return calcCommonFee();
	}

}