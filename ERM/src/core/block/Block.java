package core.block;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

import ntp.NTP;
import settings.Settings;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import utils.Converter;
import at.AT_API_Platform_Impl;
import at.AT_Block;
import at.AT_Constants;
import at.AT_Controller;
import at.AT_Exception;
import at.AT_Transaction;
import controller.Controller;
import core.BlockChain;
import core.BlockGenerator;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.transaction.DeployATTransaction;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;
import lang.Lang;


public class Block {

	public static final int GENERATING_MIN_BLOCK_TIME = GenesisBlock.GENERATING_MIN_BLOCK_TIME * 1000;
	
	public static final int VERSION_LENGTH = 4;
	//public static final int TIMESTAMP_LENGTH = 8;
	//public static final int GENERATING_BALANCE_LENGTH = 8;
	public static final int CREATOR_LENGTH = Crypto.HASH_LENGTH;
	public static final int SIGNATURE_LENGTH = Crypto.SIGNATURE_LENGTH;
	public static final int REFERENCE_LENGTH = SIGNATURE_LENGTH;
	public static final int TRANSACTIONS_HASH_LENGTH = Crypto.HASH_LENGTH;
	private static final int TRANSACTIONS_COUNT_LENGTH = 4;
	private static final int TRANSACTION_SIZE_LENGTH = 4;
	public static final int AT_BYTES_LENGTH = 4;
	private static final int BASE_LENGTH = VERSION_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH + TRANSACTIONS_HASH_LENGTH + SIGNATURE_LENGTH + TRANSACTIONS_COUNT_LENGTH;
	//private static final int AT_FEES_LENGTH = 8;
	//private static final int AT_LENGTH = AT_FEES_LENGTH + AT_BYTES_LENGTH;
	private static final int AT_LENGTH = 0 + AT_BYTES_LENGTH;
	public static final int MAX_TRANSACTION_BYTES = GenesisBlock.MAX_BLOCK_BYTES - BASE_LENGTH;

	protected int version;
	protected byte[] reference;
	//protected long timestamp;
	//protected long generatingBalance;
	protected PublicKeyAccount creator;
	protected byte[] signature;

	private List<Transaction> transactions;	
	private int transactionCount;
	private byte[] rawTransactions;

	protected byte[] transactionsHash;

	protected byte[] atBytes;
	//protected Long atFees;

	static Logger LOGGER = Logger.getLogger(Block.class.getName());

	// VERSION 2 AND 3 BLOCKS, WITH AT AND MESSAGE
	public Block(int version, byte[] reference, PublicKeyAccount creator, byte[] transactionsHash, byte[] atBytes)
	{
		this.version = version;
		this.reference = reference;
		//this.timestamp = timestamp;
		this.creator = creator;

		this.transactionsHash = transactionsHash;

		this.transactionCount = 0;
		this.atBytes = atBytes;

	}

	// VERSION 2 AND 3 BLOCKS, WITH AT AND MESSAGE
	public Block(int version, byte[] reference, PublicKeyAccount creator, byte[] signature, byte[] transactionsHash, byte[] atBytes)
	{
		this(version, reference, creator, transactionsHash, atBytes);
		this.signature = signature;

	}

	
	//GETTERS/SETTERS

	public int getVersion()
	{
		return version;
	}

	public byte[] getSignature()
	{
		return this.signature;
	}

	// timestamp by Height
	public long getTimestamp(DBSet db)
	{
		int height = -1;
		if(db.getHeightMap().contains(this.signature)) {
			height = db.getHeightMap().get(this.signature);
		} else if (db.getHeightMap().contains(this.reference)) {
			// get from parent
			height = db.getHeightMap().get(this.reference) + 1;
		}
		
		BlockChain blockChain = Controller.getInstance().getBlockChain();
		GenesisBlock genesisBlock;
		if (blockChain == null) {
			genesisBlock = new GenesisBlock();
		} else {
			genesisBlock = blockChain.getGenesisBlock();
		}

		return (height - 1) * GENERATING_MIN_BLOCK_TIME
				+ genesisBlock.getGenesisTimestamp();
	}

	public long getGeneratingBalance(DBSet db)
	{
		return this.creator.getGeneratingBalance(db).longValue();
	}
	public long getGeneratingBalance()
	{
		return getGeneratingBalance(DBSet.getInstance());
	}

	public byte[] getReference()
	{
		return this.reference;
	}

	public PublicKeyAccount getCreator()
	{
		return this.creator;
	}

	public BigDecimal getTotalFee()
	{
		BigDecimal fee = BigDecimal.ZERO.setScale(8);

		for(Transaction transaction: this.getTransactions())
		{
			fee = fee.add(transaction.getFee());
		}

		// TODO calculate AT FEE
		// fee = fee.add(BigDecimal.valueOf(this.atFees, 8));

		return fee;
	}

	/*
	public BigDecimal getATfee()
	{
		return BigDecimal.valueOf(this.atFees, 8);
	}
	*/

	public void setTransactionData(int transactionCount, byte[] rawTransactions)
	{
		this.transactionCount = transactionCount;
		this.rawTransactions = rawTransactions;
	}

	public int getTransactionCount() 
	{	
		return this.transactionCount;		
	}

	public synchronized List<Transaction> getTransactions() 
	{
		if(this.transactions == null)
		{
			//LOAD TRANSACTIONS
			this.transactions = new ArrayList<Transaction>();

			try
			{
				int position = 0;
				for(int i=0; i<transactionCount; i++)
				{
					//GET TRANSACTION SIZE
					byte[] transactionLengthBytes = Arrays.copyOfRange(this.rawTransactions, position, position + TRANSACTION_SIZE_LENGTH);
					int transactionLength = Ints.fromByteArray(transactionLengthBytes);
					position += TRANSACTION_SIZE_LENGTH;
					
					//PARSE TRANSACTION
					byte[] transactionBytes = Arrays.copyOfRange(this.rawTransactions, position, position + transactionLength);
					Transaction transaction = TransactionFactory.getInstance().parse(transactionBytes, null);

					//ADD TO TRANSACTIONS
					this.transactions.add(transaction);

					//ADD TO POSITION
					position += transactionLength;
				}
			}
			catch(Exception e)
			{
				//FAILED TO LOAD TRANSACTIONS
			}
		}

		return this.transactions;
	}

	/*
	public void addTransaction(Transaction transaction)
	{
		this.getTransactions().add(transaction);

		this.transactionCount++;
	}
	*/
	public void setTransactions(List<Transaction> transactions)
	{
		if (transactions == null)
			transactions = new ArrayList<Transaction>();
		
		this.transactions = transactions;
		this.transactionCount = transactions.size();
		this.transactionsHash = makeTransactionsHash(transactions);
	}
	public void setATBytes(byte[] atBytes)
	{
		this.atBytes = atBytes;
	}

	public int getTransactionIndex(byte[] signature)
	{

		int i = 0;
		
		for(Transaction transaction: this.getTransactions())
		{
			if(Arrays.equals(transaction.getSignature(), signature))
			{
				return i;
			}
			i++;
		}

		return -1;
	}

	public Transaction getTransaction(byte[] signature)
	{

		for(Transaction transaction: this.getTransactions())
		{
			if(Arrays.equals(transaction.getSignature(), signature))
			{
				return transaction;
			}
		}

		return null;
	}

	public Transaction getTransaction(int index)
	{
		if (index < this.transactions.size())
			return getTransactions().get(index);
		else return null;
	}
	
	public byte[] getBlockATs()
	{
		return this.atBytes;
	}

	public Block getParent(DBSet db)
	{
		return db.getBlockMap().get(this.reference);
	}

	public Block getChild(DBSet db)
	{
		return db.getChildMap().get(this);
	}

	public int getHeight(DBSet db)
	{
		if(db.getHeightMap().contains(this.signature))
			return db.getHeightMap().get(this.signature);
		else
		{
			
			if (this.reference == null)
				// GENESIS BLOCK !!! ???
				return 1;
				
			if(db.getHeightMap().contains(this.reference))
				return db.getHeightMap().get(this.reference) + 1;
			else
				return -1;
		}
	}

	/*
	public void setTransactionsHash(byte[] transactionsHash) 
	{
		this.transactionsHash = transactionsHash;
	}
	*/
	
	public static byte[] makeTransactionsHash(List<Transaction> transactions) 
	{
				
		if (transactions == null || transactions.size() == 0) {
			return new byte[TRANSACTIONS_HASH_LENGTH];
		}
		
		byte[] data = new byte[0];
		
		//MAKE TRANSACTIONS HASH
		for(Transaction transaction: transactions)
		{
			data = Bytes.concat(data, transaction.getSignature());
		}
		
		return Crypto.getInstance().digest(data);

	}
	public void makeTransactionsHash() 
	{
		this.transactionsHash = makeTransactionsHash(this.transactions);
	}

	//PARSE/CONVERT

	public static Block parse(byte[] data) throws Exception
	{
		//CHECK IF WE HAVE MINIMUM BLOCK LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data is less then minimum block length");
		}

		int position = 0;

		//READ VERSION
		byte[] versionBytes = Arrays.copyOfRange(data, position, position + VERSION_LENGTH);
		int version = Ints.fromByteArray(versionBytes);
		position += VERSION_LENGTH;

		/*
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);
		position += TIMESTAMP_LENGTH;
		*/		

		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;

		/*
		//READ GENERATING BALANCE
		byte[] generatingBalanceBytes = Arrays.copyOfRange(data, position, position + GENERATING_BALANCE_LENGTH);
		long generatingBalance = Longs.fromByteArray(generatingBalanceBytes);
		position += GENERATING_BALANCE_LENGTH;
		*/

		//READ GENERATOR
		byte[] generatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount generator = new PublicKeyAccount(generatorBytes);
		position += CREATOR_LENGTH;

		//READ TRANSACTION SIGNATURE
		byte[] transactionsHash =  Arrays.copyOfRange(data, position, position + TRANSACTIONS_HASH_LENGTH);
		position += TRANSACTIONS_HASH_LENGTH;


		//READ GENERATOR SIGNATURE
		byte[] signature =  Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		position += SIGNATURE_LENGTH;
 
		//CREATE BLOCK
		Block block;
		if(version > 1)
		{
			//ADD ATs BYTES
			byte[] atBytesCountBytes = Arrays.copyOfRange(data, position, position + AT_BYTES_LENGTH);
			int atBytesCount = Ints.fromByteArray(atBytesCountBytes);
			position += AT_BYTES_LENGTH;
	
			byte[] atBytes = Arrays.copyOfRange( data , position, position + atBytesCount);
			position += atBytesCount;
	
			//byte[] atFees = Arrays.copyOfRange( data , position , position + 8 );
			//position += 8;
	
			//long atFeesL = Longs.fromByteArray(atFees);

			block = new Block(version, reference, generator, signature, transactionsHash, atBytes); //, atFeesL);
		}
		else
		{
			// GENESIS BLOCK version = 1
			block = new Block(version, reference, generator, signature, transactionsHash, new byte[0]);
		}

		//READ TRANSACTIONS COUNT
		byte[] transactionCountBytes = Arrays.copyOfRange(data, position, position + TRANSACTIONS_COUNT_LENGTH);
		int transactionCount = Ints.fromByteArray(transactionCountBytes);
		position += TRANSACTIONS_COUNT_LENGTH;

		//SET TRANSACTIONDATA
		byte[] rawTransactions = Arrays.copyOfRange(data, position, data.length);
		block.setTransactionData(transactionCount, rawTransactions);

		//SET TRANSACTIONS SIGNATURE
		// transaction only in raw here - block.makeTransactionsHash();

		return block;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJson()
	{
		JSONObject block = new JSONObject();

		block.put("version", this.version);
		block.put("reference", Base58.encode(this.reference));
		block.put("timestamp", this.getTimestamp(DBSet.getInstance()));
		//block.put("generatingBalance", this.generatingBalance);
		block.put("winValue", this.getWinValue(DBSet.getInstance()));
		block.put("creator", this.creator.getAddress());
		block.put("creatorGeneratingBalance", this.creator.getGeneratingBalance());
		block.put("fee", this.getTotalFee().toPlainString());
		block.put("transactionsHash", Base58.encode(this.transactionsHash));
		block.put("signature", Base58.encode(this.signature));
		block.put("signature",  Base58.encode(this.getSignature()));
		block.put("height", this.getHeight(DBSet.getInstance()));

		//CREATE TRANSACTIONS
		JSONArray transactionsArray = new JSONArray();

		for(Transaction transaction: this.getTransactions())
		{
			transactionsArray.add(transaction.toJson());
		}

		//ADD TRANSACTIONS TO BLOCK
		block.put("transactions", transactionsArray);

		//ADD AT BYTES
		if ( atBytes != null )
		{
			block.put("blockATs", Converter.toHex( atBytes ));
			//block.put("atFees", this.atFees);
		}

		//RETURN
		return block;
	}

	public byte[] toBytes(boolean withSign)
	{
		byte[] data = new byte[0];

		//WRITE VERSION
		byte[] versionBytes = Ints.toByteArray(this.version);
		versionBytes = Bytes.ensureCapacity(versionBytes, VERSION_LENGTH, 0);
		data = Bytes.concat(data, versionBytes);

		/*
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		*/

		//WRITE REFERENCE
		byte[] referenceBytes = Bytes.ensureCapacity(this.reference, REFERENCE_LENGTH, 0);
		data = Bytes.concat(data, referenceBytes);

		/*
		//WRITE GENERATING BALANCE
		byte[] baseTargetBytes = Longs.toByteArray(this.generatingBalance);
		baseTargetBytes = Bytes.ensureCapacity(baseTargetBytes, GENERATING_BALANCE_LENGTH, 0);
		data = Bytes.concat(data,baseTargetBytes);
		*/

		//WRITE GENERATOR
		byte[] generatorBytes = Bytes.ensureCapacity(this.creator.getPublicKey(), CREATOR_LENGTH, 0);
		data = Bytes.concat(data, generatorBytes);

		//WRITE TRANSACTIONS HASH
		data = Bytes.concat(data, this.transactionsHash);

		if (!withSign) {
			// make HEAD data for signature
			return data;
		}

		//WRITE GENERATOR SIGNATURE
		data = Bytes.concat(data, this.signature);

		//ADD ATs BYTES
		if(this.version > 1)
		{
			if (atBytes!=null)
			{
				byte[] atBytesCount = Ints.toByteArray( atBytes.length );
				data = Bytes.concat(data, atBytesCount);

				data = Bytes.concat(data, atBytes);

				//byte[] atByteFees = Longs.toByteArray(atFees);
				//data = Bytes.concat(data,atByteFees);
			}
			else
			{
				byte[] atBytesCount = Ints.toByteArray( 0 );
				data = Bytes.concat(data, atBytesCount);
				
				//byte[] atByteFees = Longs.toByteArray(0L);
				//data = Bytes.concat(data,atByteFees);
			}
		}

		//WRITE TRANSACTION COUNT
		byte[] transactionCountBytes = Ints.toByteArray(this.getTransactionCount());
		transactionCountBytes = Bytes.ensureCapacity(transactionCountBytes, TRANSACTIONS_COUNT_LENGTH, 0);
		data = Bytes.concat(data, transactionCountBytes);

		for(Transaction transaction: this.getTransactions())
		{
			//WRITE TRANSACTION LENGTH
			int transactionLength = transaction.getDataLength(false);
			byte[] transactionLengthBytes = Ints.toByteArray(transactionLength);
			transactionLengthBytes = Bytes.ensureCapacity(transactionLengthBytes, TRANSACTION_SIZE_LENGTH, 0);
			data = Bytes.concat(data, transactionLengthBytes);

			//WRITE TRANSACTION
			data = Bytes.concat(data, transaction.toBytes(true, null));
		}

		return data;
	}

	public void sign(PrivateKeyAccount account) 
	{	
		byte[] data = toBytes(false); // without SIGNATURE
		this.signature = Crypto.getInstance().sign(account, data);
	}

	public int getDataLength()
	{

		int length = BASE_LENGTH;

		if(this.version >= 2)
		{
			length += AT_LENGTH;
			if (this.atBytes!=null)
			{
				length+=atBytes.length;
			}
		}

		for(Transaction transaction: this.getTransactions())
		{
			length += TRANSACTION_SIZE_LENGTH + transaction.getDataLength(false);
		}

		return length;
	}

	public byte[] getProofHash()
	{
		if(this.version < 3)
		{
			return Crypto.getInstance().digest(this.signature);
		}
		else
		{
			//newSig = sha256(prevSig || pubKey)
			byte[] data = Bytes.concat(this.reference, creator.getPublicKey());

			return Crypto.getInstance().digest(data);
		}
	}
	
	public long getWinValue(DBSet dbSet)
	{
		return this.creator.calcWinValue(dbSet, this.getHeight(dbSet));
	}


	//VALIDATE

	public boolean isSignatureValid()
	{
		//VALIDATE BLOCK SIGNATURE
		byte[] data = this.toBytes(false);

		if(!Crypto.getInstance().verify(this.creator.getPublicKey(), this.signature, data))
		{
			LOGGER.error("Block signature not valid "
					+ this.toString());
			return false;
		}

		return true;
	}

	// canonical definition of block version release schedule
	public int getNextBlockVersion(DBSet db)
	{

		return 3;
		
		/*
		int height = getHeight(db);

		if(height < Transaction.getAT_BLOCK_HEIGHT_RELEASE())
		{
			return 1;
		}
		else if(getTimestamp() < Transaction.getPOWFIX_RELEASE())
		{
			return 2;
		}
		else
		{
			return 3;
		}
		*/
	}

	public boolean isValid(DBSet db)
	{
		
		//CHECK IF PARENT EXISTS
		if(this.reference == null || this.getParent(db) == null)
		{
			LOGGER.error("*** Block[" + this.getHeight(db) + "].reference invalid");
			return false;
		}

		/*
		 * OLD TIME
		//if (false) {
		if (!noTime) {
			//CHECK IF TIMESTAMP IS VALID -500 MS ERROR MARGIN TIME
			if(true & (this.timestamp - 500 > NTP.getTime()
					|| this.timestamp < this.getParent(db).timestamp))
			{
				LOGGER.error("*** Block[" + this.getHeight(db) + "].timestamp invalid");
				return false;
			}
	
			//CHECK IF TIMESTAMP REST SAME AS PARENT TIMESTAMP REST
			if(this.timestamp % 1000 != this.getParent(db).timestamp % 1000)
			{
				LOGGER.error("*** Block[" + this.getHeight(db) + "].timestamp % 1000 invalid");
				return false;
			}
		}
		 */
		if(this.getTimestamp(db) - 3000 > NTP.getTime()) {
			LOGGER.error("*** Block[" + this.getHeight(db) + ":" + Base58.encode(this.signature) + "].timestamp invalid >NTP.getTime()");
			return false;			
		}
		if(this.getTimestamp(db) - this.getParent(db).getTimestamp(db) != GENERATING_MIN_BLOCK_TIME) {
				LOGGER.error("*** Block[" + this.getHeight(db) + ":" + Base58.encode(this.signature) + "].timestamp invalid != GENERATING_MIN_BLOCK_TIME");
				return false;			
			}

		/*
		//CHECK IF GENERATING BALANCE IS CORRECT
		if(this.generatingBalance != BlockGenerator.getNextBlockGeneratingBalance(db, this.getParent(db)))
		{
			LOGGER.error("*** Block[" + this.getHeight(db) + "].generatingBalance invalid");
			return false;
		}
		*/

		//CHECK IF VERSION IS CORRECT
		if(this.version != this.getParent(db).getNextBlockVersion(db))
		{
			LOGGER.error("*** Block[" + this.getHeight(db) + "].version invalid");
			return false;
		}
		if(this.version < 2 && (this.atBytes.length > 0)) // || this.atFees != 0))
		{
			LOGGER.error("*** Block[" + this.getHeight(db) + "].version AT invalid");
			return false;
		}

		/*
		//CREATE TARGET
		byte[] targetBytes = new byte[SIGNATURE_LENGTH];
		Arrays.fill(targetBytes, Byte.MAX_VALUE);
		BigInteger target = new BigInteger(1, targetBytes);

		//DIVIDE TARGET BY BASE TARGET
		BigInteger baseTarget = BigInteger.valueOf(BlockGenerator.getBaseTarget(this.generatingBalance));
		target = target.divide(baseTarget);

		//MULTIPLY TARGET BY USER BALANCE
		target = target.multiply(this.creator.getGeneratingBalance(db).toBigInteger());

		//MULTIPLE TARGET BY GUESSES
		long guesses = (this.timestamp - this.getParent(db).getTimestamp()) / 1000; // orid /1000
		//BigInteger lowerTarget = target.multiply(BigInteger.valueOf(guesses-1)); // orig -1
		BigInteger lowerTarget = target.multiply(BigInteger.valueOf(guesses-1));
		target = target.multiply(BigInteger.valueOf(guesses));

		//CONVERT PROOF HASH TO BIGINT
		BigInteger hashValue = new BigInteger(1, getProofHash());

		//CHECK IF HASH LOWER THEN TARGET (blockchain total hash - "chain length")
		if(hashValue.compareTo(target) >= 0)
		{
			LOGGER.error("*** Block[" + this.getHeight(db)
					+ "].target is invalid!. " + "guesses: " + guesses
					+ "\nhash >= target:\n" + hashValue.toString() + "\n" + target.toString());
			return false;
		}

		//CHECK IF FIRST BLOCK OF USER	
		if(hashValue.compareTo(lowerTarget) < 0)
		{
			LOGGER.error("*** Block[" + this.getHeight(db)
				+ "].lowerTarget invalid!. " + "guesses: " + guesses
				+ "\nhash < lower:\n" + hashValue.toString() + "\n" + lowerTarget.toString());
			return false;
		}
		*/

		if ( this.atBytes != null && this.atBytes.length > 0 )
		{
			try
			{

				AT_Block atBlock = AT_Controller.validateATs( this.getBlockATs() , db.getBlockMap().getLastBlock().getHeight(db)+1 , db);
				//this.atFees = atBlock.getTotalFees();
			}
			catch(NoSuchAlgorithmException | AT_Exception e)
			{
				LOGGER.error(e.getMessage(),e);
				return false;
			}
		}

		//CHECK TRANSACTIONS
		if (this.transactions == null || this.transactions.size() == 0) {
			// empty transactions
		} else {
			DBSet fork = db.fork();
			byte[] transactionsSignatures = new byte[0];
			
			long timestampEnd = this.getTimestamp(db);
			// because time filter used by parent block timestamp on core.BlockGenerator.run()
			long timestampBeg = this.getParent(fork).getTimestamp(fork);

			for(Transaction transaction: this.getTransactions())
			{
				//CHECK IF NOT GENESISTRANSACTION
				if(transaction.getCreator() == null)
					 // ALL GENESIS transaction
					return false;
				
				if(!transaction.isSignatureValid()) {
					// 
					LOGGER.error("*** Block[" + this.getHeight(fork)
					+ "].Tx[" + this.getTransactionSeq(transaction.getSignature()) + " : "
					+ transaction.viewFullTypeName() + "]"
					+ "invalid code: " + transaction.isValid(fork, null));
					return false;
				}
	
	
				//CHECK IF VALID
				if ( transaction instanceof DeployATTransaction)
				{
					Integer min = 0;
					if ( db.getBlockMap().getParentList() != null )
					{
						min = AT_API_Platform_Impl.getForkHeight(db);
					}
	
					DeployATTransaction atTx = (DeployATTransaction)transaction;
					if ( atTx.isValid(fork, min) != Transaction.VALIDATE_OK )
					{
						LOGGER.error("*** Block[" + this.getHeight(fork) + "].atTx invalid");
						return false;
					}
				}
				else if(transaction.isValid(fork, null) != Transaction.VALIDATE_OK)
				{
					LOGGER.error("*** Block[" + this.getHeight(fork)
						+ "].Tx[" + this.getTransactionSeq(transaction.getSignature()) + " : "
						+ transaction.viewFullTypeName() + "]"
						+ "invalid code: " + transaction.isValid(fork, null));
					return false;
				}
	
				//CHECK TIMESTAMP AND DEADLINE
				if(transaction.getTimestamp() > timestampEnd || transaction.getDeadline() <= timestampBeg)
				{
					LOGGER.error("*** Block[" + this.getHeight(fork) + "].TX.timestamp invalid");
					return false;
				}
	
				//PROCESS TRANSACTION IN MEMORYDB TO MAKE SURE OTHER TRANSACTIONS VALIDATE PROPERLY
				transaction.process(fork, false);
				
				transactionsSignatures = Bytes.concat(transactionsSignatures, transaction.getSignature());
			}
			
			transactionsSignatures = Crypto.getInstance().digest(transactionsSignatures);
			if (!Arrays.equals(this.transactionsHash, transactionsSignatures)) {
				LOGGER.error("*** Block[" + this.getHeight(fork) + "].digest(transactionsSignatures) invalid");
				return false;
			}
		}

		//BLOCK IS VALID
		return true;
	}

	//PROCESS/ORPHAN

	public void process(DBSet dbSet)
	{	
		//PROCESS TRANSACTIONS
		for(Transaction transaction: this.getTransactions())
		{
			//PROCESS
			transaction.process(dbSet, false);

			//SET PARENT
			dbSet.getTransactionRef_BlockRef_Map().set(transaction, this);

			//REMOVE FROM UNCONFIRMED DATABASE
			dbSet.getTransactionMap().delete(transaction);
		}

		//DELETE CONFIRMED TRANSACTIONS FROM UNCONFIRMED TRANSACTIONS LIST
		List<Transaction> unconfirmedTransactions = new ArrayList<Transaction>(dbSet.getTransactionMap().getValues());
		for(Transaction transaction: unconfirmedTransactions)
		{
			if(dbSet.getTransactionRef_BlockRef_Map().contains(transaction.getSignature()))
			{
				dbSet.getTransactionMap().delete(transaction);
			}
		}

		//PROCESS FEE
		BigDecimal blockFee = this.getTotalFee();
		if(blockFee.compareTo(BigDecimal.ZERO) == 1)
		{
			//UPDATE GENERATOR BALANCE WITH FEE
			this.creator.setConfirmedBalance(Transaction.FEE_KEY, this.creator.getConfirmedBalance(Transaction.FEE_KEY, dbSet).add(blockFee), dbSet);
		}
		
		Block parent = this.getParent(dbSet);
		int height = 1;
		if(parent != null)
		{
			//SET AS CHILD OF PARENT
			dbSet.getChildMap().set(parent, this);		

			//SET BLOCK HEIGHT
			height = parent.getHeight(dbSet) + 1;
			dbSet.getHeightMap().set(this, height);
		}
		else
		{
			//IF NO PARENT HEIGHT IS 1
			dbSet.getHeightMap().set(this, 1);
		}

		if (this instanceof GenesisBlock ) {
		} else {
			// PROCESS FORGING DATA
			Integer prevHeight = this.creator.getLastForgingData(dbSet);
			this.creator.setForgingData(dbSet, height, prevHeight);
			this.creator.setLastForgingData(dbSet, height);
		}

		//PROCESS TRANSACTIONS
		int seq = 1;
		for(Transaction transaction: this.getTransactions())
		{
			dbSet.getTransactionFinalMap().add( height, seq, transaction);
			seq++;
		}

		if(height % Settings.BLOCK_MAX_SIGNATURES == 0) 
		{
			Controller.getInstance().blockchainSyncStatusUpdate(height);
		}
		
		//ADD TO DB
		dbSet.getBlockMap().add(this);

		//UPDATE LAST BLOCK
		dbSet.getBlockMap().setLastBlock(this);	
	}

	public void orphan(DBSet dbSet)
	{
		int height = this.getHeight(dbSet);
		
		//ORPHAN AT TRANSACTIONS
		LinkedHashMap< Tuple2<Integer, Integer> , AT_Transaction > atTxs = dbSet.getATTransactionMap().getATTransactions(height);

		Iterator<AT_Transaction> iter = atTxs.values().iterator();

		while ( iter.hasNext() )
		{
			AT_Transaction key = iter.next();
			Long amount  = key.getAmount();
			if (key.getRecipientId() != null && !Arrays.equals(key.getRecipientId(), new byte[ AT_Constants.AT_ID_SIZE ]) && !key.getRecipient().equalsIgnoreCase("1") )
			{
				Account recipient = new Account( key.getRecipient() );
				recipient.setConfirmedBalance(Transaction.FEE_KEY,  recipient.getConfirmedBalance(Transaction.FEE_KEY,  dbSet ).subtract( BigDecimal.valueOf( amount, 8 ) ) , dbSet );
				if ( recipient.getLastReference(dbSet) != null)
				{
					recipient.removeReference(dbSet);
				}
			}
			Account sender = new Account( key.getSender() );
			sender.setConfirmedBalance(Transaction.FEE_KEY,  sender.getConfirmedBalance(Transaction.FEE_KEY,  dbSet ).add( BigDecimal.valueOf( amount, 8 ) ) , dbSet );

		}

		//ORPHAN TRANSACTIONS
		this.orphanTransactions(this.getTransactions(), dbSet);

		//REMOVE FEE
		BigDecimal blockFee = this.getTotalFee();
		if(blockFee.compareTo(BigDecimal.ZERO) == 1)
		{
			//UPDATE GENERATOR BALANCE WITH FEE
			this.creator.setConfirmedBalance(Transaction.FEE_KEY, this.creator.getConfirmedBalance(Transaction.FEE_KEY, dbSet).subtract(blockFee), dbSet);
		}

		//DELETE AT TRANSACTIONS FROM DB
		dbSet.getATTransactionMap().delete(height);
		
		//DELETE TRANSACTIONS FROM FINAL MAP
		dbSet.getTransactionFinalMap().delete(height);

		dbSet.getHeightMap().delete(this.signature);
		// delete CHILDS - for proper making HEADERS in Controller.onMessage
		dbSet.getChildMap().delete(this.signature);

		// LAST FORGING BLOCK
		if (this instanceof GenesisBlock ) {
		} else {
			// ORPHAN FORGING DATA
			Integer prevHeight = this.creator.getForgingData(dbSet, height);
			this.creator.delForgingData(dbSet, height);
			this.creator.setLastForgingData(dbSet, prevHeight);
		}

		//DELETE BLOCK FROM DB
		dbSet.getBlockMap().delete(this);

		if (this.getParent(dbSet) != null) {
			//SET PARENT AS LAST BLOCK
			dbSet.getBlockMap().setLastBlock(this.getParent(dbSet));
		}
				
		for(Transaction transaction: this.getTransactions())
		{
			//ADD ORPHANED TRANASCTIONS BACK TO DATABASE
			dbSet.getTransactionMap().add(transaction);

			//DELETE ORPHANED TRANASCTIONS FROM PARENT DATABASE
			dbSet.getTransactionRef_BlockRef_Map().delete(transaction.getSignature());
		}
	}

	private void orphanTransactions(List<Transaction> transactions, DBSet db)
	{
		//ORPHAN ALL TRANSACTIONS IN DB BACK TO FRONT
		for(int i=transactions.size() -1; i>=0; i--)
		{
			Transaction transaction = transactions.get(i);
			transaction.orphan(db, false);
		}
	}

	public int getTransactionSeq(byte[] signature)
	{
		int seq = 1;
		for(Transaction transaction: this.getTransactions())
		{
			if(Arrays.equals(transaction.getSignature(), signature))
			{
				return seq;
			}
			seq ++;
		}

		return -1;
	}
	
	@Override 
	public boolean equals(Object otherObject)
	{
		if(otherObject instanceof Block)
		{
			Block otherBlock = (Block) otherObject;
			
			return Arrays.equals(this.getSignature(), otherBlock.getSignature());
		}
		
		return false;
	}
}
