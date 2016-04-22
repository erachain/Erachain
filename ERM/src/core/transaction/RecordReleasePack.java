package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//import ntp.NTP;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Crypto;
import core.transaction.Transaction;
import database.ItemAssetBalanceMap;
import database.DBSet;

public class RecordReleasePack extends Transaction {

	private static final byte TYPE_ID = (byte)Transaction.RELEASE_PACK;
	private static final String NAME_ID = "Release pack";
	private static final int PACK_SIZE_LENGTH = 4;
	private static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + PACK_SIZE_LENGTH;
	private static final int BASE_LENGTH = Transaction.BASE_LENGTH + PACK_SIZE_LENGTH;

	private List<Transaction> transactions;
	
	public RecordReleasePack(byte[] typeBytes, PublicKeyAccount creator, List<Transaction> transactions, byte feePow, long timestamp, byte[] reference) 
	{
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);		
		this.transactions = transactions;
	}
	public RecordReleasePack(byte[] typeBytes, PublicKeyAccount creator, List<Transaction> transactions, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, transactions, feePow, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}
	// as pack - calcFee not needed
	public RecordReleasePack(byte[] typeBytes, PublicKeyAccount creator, List<Transaction> transactions, byte[] reference, byte[] signature)
	{
		this(typeBytes, creator, transactions, (byte)0, 0l, reference);
		this.signature = signature;		
	}
	public RecordReleasePack(PublicKeyAccount creator, List<Transaction> transactions, byte feePow, long timestamp, byte[] reference) 
	{
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, transactions, feePow, timestamp, reference);
	}
	// as Pack
	public RecordReleasePack(PublicKeyAccount creator, List<Transaction> transactions, byte[] reference)
	{
		this(creator, transactions, (byte)0, 0l, reference);
	}
	
	//GETTERS/SETTERS

	//public static String getName() { return "Multi Send"; }

	public List<Transaction> getTransactions()
	{
		return this.transactions;
	}
	
	//PARSE/CONVERT

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject json = this.getJsonBase();
		
		//ADD CREATOR/PAYMENTS
		json.put("creator", this.creator.getAddress());
		
		JSONArray transactions = new JSONArray();
		for(Transaction transaction: this.transactions)
		{
			transactions.add(transaction.toJson());
		}
		json.put("transactions", transactions);
				
		return json;	
	}

	public static Transaction Parse(byte[] data, byte[] releaserReference) throws Exception{
		
		boolean asPack = releaserReference != null;
		int data_length = data.length;
		
		//CHECK IF WE MATCH BLOCK LENGTH
		if (data_length < BASE_LENGTH_AS_PACK
				| !asPack & data_length < BASE_LENGTH)
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
		//READ PACK SIZE
		byte[] transactionsLengthBytes = Arrays.copyOfRange(data, position, position + PACK_SIZE_LENGTH);
		int transactionsLength = Ints.fromByteArray(transactionsLengthBytes);
		position += PACK_SIZE_LENGTH;
		
		if(transactionsLength < 1 || transactionsLength > 400)
		{
			throw new Exception("Invalid pack length");
		}
		
		//READ TRANSACTIONS
		TransactionFactory tf_inct = TransactionFactory.getInstance();
		List<Transaction> transactions = new ArrayList<Transaction>();
		for(int i=0; i<transactionsLength; i++)
		{
			Transaction transaction = tf_inct.parse(Arrays.copyOfRange(data, position, data_length), releaserReference);
			transactions.add(transaction);
			
			position += transaction.getDataLength(true);
		}
		
		if (!asPack) {
			return new RecordReleasePack(typeBytes, creator, transactions, feePow, timestamp, reference, signatureBytes);
		} else {
			return new RecordReleasePack(typeBytes, creator, transactions, reference, signatureBytes);
		}
	}	
	
	//@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference) 
	{

		byte[] data = super.toBytes(withSign, null);
			
		//WRITE PAYMENTS SIZE
		int transactionsLength = this.transactions.size();
		byte[] transactionsLengthBytes = Ints.toByteArray(transactionsLength);
		data = Bytes.concat(data, transactionsLengthBytes);
		
		//WRITE PAYMENTS
		for(Transaction transaction: this.transactions)
		{
			data = Bytes.concat(data, transaction.toBytes(withSign, releaserReference));
		}
				
		return data;
	}

	@Override
	public int getDataLength(boolean asPack) 
	{
		int transactionsLength = 0;
		for(Transaction transaction: this.getTransactions())
		{
			transactionsLength += transaction.getDataLength(asPack);
		}
		
		return asPack ? BASE_LENGTH_AS_PACK : BASE_LENGTH + transactionsLength;
	}
	
	//VALIDATE

	//@Override
	public int isValid(DBSet db, byte[] releaserReference) 
	{
		
		//CHECK PAYMENTS SIZE
		if(this.transactions.size() < 1 || this.transactions.size() > 400)
		{
			return INVALID_PAYMENTS_LENGTH;
		}
		
		DBSet fork = db.fork();

		int counter = 0;
		int result = 0;
		//CHECK PAYMENTS
		for(Transaction transaction: this.transactions)
		{	
			
			result = transaction.isValid(fork, releaserReference);
			if (result != Transaction.VALIDATE_OK)
				// transaction counter x100
				return result + counter * 100;
			//PROCESS PAYMENT IN FORK AS PACK
			transaction.process(fork, true);
			counter++;
		}
		
		// IN FORK
		return super.isValid(fork, releaserReference);
		
	}

	//PROCESS/ORPHAN
	
	//@Override
	public void process(DBSet db, boolean asPack) 
	{
		//UPDATE CREATOR
		super.process(db, asPack);
		
		//PROCESS PAYMENTS
		for(Transaction transaction: this.transactions)
		{
			transaction.process(db, true); // as Pack in body			
		}
	}

	//@Override
	public void orphan(DBSet db, boolean asPack) 
	{
		//UPDATE CREATOR
		super.orphan(db, asPack);
								
		//ORPHAN PAYMENTS
		for(Transaction transaction: this.transactions)
		{
			transaction.orphan(db, true); // as Pack in body
		}
	}

	//REST
	
	@Override
	public HashSet<Account> getInvolvedAccounts()
	{
		HashSet<Account> accounts = new HashSet<Account>();
		accounts.add(this.creator);
		accounts.addAll(this.getRecipientAccounts());
		return accounts;
	}
	
	@Override
	public HashSet<Account> getRecipientAccounts()
	{
		HashSet<Account> accounts = new HashSet<>();
		
		for(Transaction transaction: this.transactions)
		{
			accounts.addAll(transaction.getInvolvedAccounts());
		}
		
		return accounts;
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		for(Account involved: this.getInvolvedAccounts())
		{
			if(address.equals(involved.getAddress()))
			{
				return true;
			}
		}
		
		return false;
	}

	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), ItemAssetBalanceMap.FEE_KEY, this.fee);
		
		for(Transaction transaction: this.transactions)
		{
			//assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), transaction.getAsset(), transaction.getAmount());
			//assetAmount = addAssetAmount(assetAmount, transaction.getRecipient().getAddress(), transaction.getAsset(), transaction.getAmount());
		}
		
		return assetAmount;
	}
	public int calcBaseFee() {
		return calcCommonFee();
	}
	
}