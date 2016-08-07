package core;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple3;

import ntp.NTP;
import settings.Settings;
import utils.ObserverMessage;
import utils.TransactionFeeComparator;
import at.AT_Block;
import at.AT_Constants;
import at.AT_Controller;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import controller.Controller;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.block.BlockFactory;
import core.crypto.Crypto;
import core.transaction.Transaction;
import database.DBSet;
import lang.Lang;
import settings.Settings;

public class BlockGenerator extends Thread implements Observer
{	
	
	static Logger LOGGER = Logger.getLogger(BlockGenerator.class.getName());
	
	public enum ForgingStatus {
	    
		FORGING_DISABLED(0, Lang.getInstance().translate("Forging disabled") ),
		FORGING_ENABLED(1, Lang.getInstance().translate("Forging enabled")),
		FORGING(2, Lang.getInstance().translate("Forging")),
		FORGING_WAIT(3, Lang.getInstance().translate("Forging awaiting another peer sync"));
		
		private final int statuscode;
		private String name;

		 ForgingStatus(int status, String name) {
			 statuscode = status;
			 this.name = name;
		  }

		public int getStatuscode() {
			return statuscode;
		}

		public String getName() {
			return name;
		}

	    
	}
	
    public ForgingStatus getForgingStatus()
    {
        return forgingStatus;
    }
	
	private Map<PrivateKeyAccount, Block> blocks;
	private Block solvingBlock;
	private List<PrivateKeyAccount> cachedAccounts;
	
	private ForgingStatus forgingStatus = ForgingStatus.FORGING_DISABLED;
	private boolean walletOnceUnlocked = false;
	
	
	public BlockGenerator(boolean withObserve)
	{
		if(Settings.getInstance().isGeneratorKeyCachingEnabled())
		{
			this.cachedAccounts = new ArrayList<PrivateKeyAccount>();
		}
		
		if (withObserve) addObserver();
	}

	public void addObserver() {
		new Thread()
		{
			@Override
			public void run() {
				
				//WE HAVE TO WAIT FOR THE WALLET TO ADD THAT LISTENER.
				while(!Controller.getInstance().doesWalletExists() || !Controller.getInstance().doesWalletDatabaseExists())
				{
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
//						does not matter
					}
				}
				
				Controller.getInstance().addWalletListener(BlockGenerator.this);
				syncForgingStatus();
			}
		}.start();
		Controller.getInstance().addObserver(this);
	}
	
	public void addUnconfirmedTransaction(Transaction transaction)
	{
		this.addUnconfirmedTransaction(DBSet.getInstance(), transaction, true);
	}
	public void addUnconfirmedTransaction(DBSet db, Transaction transaction, boolean process) 
	{
		//ADD TO TRANSACTION DATABASE 
		db.getTransactionMap().add(transaction);
	}
	
	public List<Transaction> getUnconfirmedTransactions()
	{
		return new ArrayList<Transaction>(DBSet.getInstance().getTransactionMap().getValues());
	}
	
	private List<PrivateKeyAccount> getKnownAccounts()
	{
		//CHECK IF CACHING ENABLED
		if(Settings.getInstance().isGeneratorKeyCachingEnabled())
		{
			List<PrivateKeyAccount> privateKeyAccounts = Controller.getInstance().getPrivateKeyAccounts();
			
			//IF ACCOUNTS EXISTS
			if(privateKeyAccounts.size() > 0)
			{
				//CACHE ACCOUNTS
				this.cachedAccounts = privateKeyAccounts;
			}
			
			//RETURN CACHED ACCOUNTS
			return this.cachedAccounts;
		}
		else
		{
			//RETURN ACCOUNTS
			return Controller.getInstance().getPrivateKeyAccounts();
		}
	}
	
	private void setForgingStatus(ForgingStatus status)
	{
		if(forgingStatus != status)
		{
			forgingStatus = status;
			Controller.getInstance().forgingStatusChanged(forgingStatus);
		}
	}
	
	
	public void run()
	{
		while(true)
		{
			if(DBSet.getInstance().isStoped()) {
				try 
				{
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) 
				{
					LOGGER.error(e.getMessage(),e);
				}
				continue;
			}
			
			//CHECK IF WE ARE UPTODATE
			if(!Controller.getInstance().isUpToDate() && !Controller.getInstance().isProcessingWalletSynchronize())
			{
				Controller.getInstance().update();
			}
			
			//CHECK IF WE HAVE CONNECTIONS and READY to GENERATE
			syncForgingStatus();
			if(forgingStatus == ForgingStatus.FORGING && Controller.getInstance().isReadyForging())
			{

				if(!Controller.getInstance().doesWalletExists())
					return;
				
				//GET LAST BLOCK
				// TODO lastBlock error
				// TEST DB for last block - some time error rised
				Block lastBlock = Controller.getInstance().getLastBlock();
				if (lastBlock == null) {
					return;
				}
				
				DBSet dbSet = DBSet.getInstance();
				byte[] lastBlockSignature = dbSet.getBlockMap().getLastBlockSignature();
						
				//CHECK IF DIFFERENT FOR CURRENT SOLVING BLOCK
				if(this.solvingBlock == null || !Arrays.equals(this.solvingBlock.getSignature(), lastBlockSignature))
				{
					//SET NEW BLOCK TO SOLVE
					this.solvingBlock = DBSet.getInstance().getBlockMap().getLastBlock();
					
					//RESET BLOCKS
					this.blocks = new HashMap<PrivateKeyAccount, Block>();
				}

				/*
				 * нужно сразу взять транзакции которые бедум в блок класть - чтобы
				 * значть их ХЭШ - 
				 * тоже самое и AT записями поидее
				 * и эти хэши закатываем уже в заголвок блока и подписываем
				 * после чего делать вычисление значения ПОБЕДЫ - она от подписи зависит
				 * если победа случиласть то
				 * далее сами трнзакции кладем в тело блока и закрываем его
				 */
				/*
				 * нет не  так - вычисляеи победное значение и если оно выиграло то
				 * к нему транзакции собираем
				 * и время всегда одинаковое
				 * 
				 */
				
				//GENERATE NEW BLOCKS
				List<Transaction> unconfirmedTransactions = null;
				
				//PREVENT CONCURRENT MODIFY EXCEPTION
				List<PrivateKeyAccount> knownAccounts = this.getKnownAccounts();
				synchronized(knownAccounts)
				{
					// TODO need calculate block.timestamp
					long lastTimestamp = this.solvingBlock.getTimestamp()
							+ GenesisBlock.GENERATING_MIN_BLOCK_TIME * 60 * 1000;
					
					// GET VALID UNCONFIRMED RECORDS for current TIMESTAMP
					unconfirmedTransactions = getUnconfirmedTransactions(dbSet, lastTimestamp);
					// CALCULATE HASH for that transactions
					byte[] unconfirmedTransactionsHash = Block.makeTransactionsHash(unconfirmedTransactions);
					
					for(PrivateKeyAccount account: knownAccounts)
					{
						BigDecimal generatingBalance = account.getGeneratingBalance();
						if(generatingBalance.compareTo(GenesisBlock.MIN_GENERATING_BALANCE_BD) < 0)
							continue;
						
						//CHECK IF BLOCK FROM USER ALREADY EXISTS USE MAP ACCOUNT BLOCK EASY
						if(this.blocks.containsKey(account))
							continue;
						
						//GENERATE NEW BLOCK FOR USER
						// already signed
						this.blocks.put(account, this.generateNextBlock(DBSet.getInstance(), account, generatingBalance, this.solvingBlock, unconfirmedTransactionsHash));
					}
				}
				
				//VALID BLOCK FOUND
				boolean validBlockFound = false;
						
				//CHECK IF BLOCK IS VALID
				for(PrivateKeyAccount account: this.blocks.keySet())
				{
					Block block = this.blocks.get(account);
					
					/*Date date = new Date(block.getTimestamp());
					DateFormat format = DateFormat.getDateTimeInstance();
					LOGGER.info(format.format(date));*/
					
					//CHECK IF BLACK TIMESTAMP IS VALID
					if(block.getTimestamp() <= NTP.getTime() && !validBlockFound)
					{
						//ADD TRANSACTIONS
						//this.addUnconfirmedTransactions(DBSet.getInstance(), block);
						block.setTransactions(unconfirmedTransactions);
						
						//PASS BLOCK TO CONTROLLER
						Controller.getInstance().newBlockGenerated(block);
						
						//BLOCK FOUND
						validBlockFound = true;
					}
				}
				
				//IF NO BLOCK FOUND
				if(!validBlockFound)
				{
					//SLEEP
					try 
					{
						Thread.sleep(1000);
					} 
					catch (InterruptedException e) 
					{
						LOGGER.error(e.getMessage(),e);
					}
				}
			}
			else
			{
				//SLEEP
				try 
				{
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) 
				{
					LOGGER.error(e.getMessage(),e);
				}
			}
		}
	}
	
	/* TODO
	 * try use this code in Block.isValid and here
	 * 
	// getNextBlockGeneratingBalance(dbSet, block)
	// block.generatingBalance

	//CONVERT PROOF HASH TO BIGINT
	BigInteger hashValue = new BigInteger(1, block.getProofHash());
	//CONVERT HASH TO BIGINT
	BigInteger hashValue = new BigInteger(1, hash);
	*/
	public long calculateGeneratingGuesses(DBSet dbSet, PrivateKeyAccount generator, long generatingBalance, Block block, BigInteger hashValue) {
		//CREATE TARGET
		byte[] targetBytes = new byte[Crypto.HASH_LENGTH];
		Arrays.fill(targetBytes, Byte.MAX_VALUE);
		BigInteger target = new BigInteger(1, targetBytes);
	
		//DIVIDE TARGET BY BASE TARGET
		BigInteger baseTarget = BigInteger.valueOf(BlockGenerator.getBaseTarget(generatingBalance));
		target = target.divide(baseTarget);
	
		//MULTIPLY TARGET BY USER BALANCE
		target = target.multiply(generator.getGeneratingBalance(dbSet).toBigInteger());
	
		//MULTIPLE TARGET BY GUESSES
		long guesses = (block.getTimestamp() - block.getParent(dbSet).getTimestamp()) / 1000; // orid /1000
		//BigInteger guesses = hashValue.divide(target).add(BigInteger.ONE);
		//BigInteger lowerTarget = target.multiply(BigInteger.valueOf(guesses-1));
		//return target.multiply(BigInteger.valueOf(guesses));
		return guesses;
		
	}

	public Block generateNextBlock(DBSet dbSet, PrivateKeyAccount account, BigDecimal generatingBalance, Block parentBlock, byte[] transactionsHash)
	{
		Tuple3<Integer, Integer, TreeSet<String>> value = account.getForgingData(dbSet);
		// calculate last generated block and current
		int len = value.a - parentBlock.getHeight(dbSet);
		long win_amount;
		int MAX_LEN = 1000;
		if (len < MAX_LEN ) {
			win_amount = len * len * value.b ;
		} else {
			win_amount = (MAX_LEN * MAX_LEN + len - MAX_LEN) * value.b;			
		}
		
		int version = parentBlock.getNextBlockVersion(dbSet);
		byte[] atBytes;
		if ( version > 1 )
		{
			AT_Block atBlock = AT_Controller.getCurrentBlockATs( AT_Constants.getInstance().MAX_PAYLOAD_FOR_BLOCK(
					parentBlock.getHeight(dbSet)) , parentBlock.getHeight(dbSet) + 1 );
			atBytes = atBlock.getBytesForBlock();
		} else {
			atBytes = new byte[0];
		}

		Long lastTimestamp = this.solvingBlock.getTimestamp()
				+ GenesisBlock.GENERATING_MIN_BLOCK_TIME * 60 * 1000;

		//CREATE NEW BLOCK
		Block newBlock = BlockFactory.getInstance().create(version, parentBlock.getSignature(), lastTimestamp.longValue(), getNextBlockGeneratingBalance(dbSet, parentBlock), account, transactionsHash, atBytes);
		newBlock.sign(account);
		
		return newBlock;

	}
	public Block generateNextBlock_old(DBSet dbSet, PrivateKeyAccount account, BigDecimal generatingBalance, Block parentBlock, byte[] transactionsHash)
	{

		//CALCULATE SIGNATURE
		//byte[] signature = this.calculateSignature(dbSet, block, account);

		//DETERMINE BLOCK VERSION
		int version = parentBlock.getNextBlockVersion(dbSet);

		//CALCULATE HASH
		byte[] hash;
		if (version > 0)
		{
			byte[] data = Bytes.concat(parentBlock.getSignature(), account.getPublicKey());
			hash = Crypto.getInstance().digest(data);
		}
		else
		{
			hash = null;
		}

		//CONVERT HASH TO BIGINT
		BigInteger hashValue = new BigInteger(1, hash);
		
		//CALCULATE ACCOUNT TARGET
		byte[] targetBytes = new byte[Crypto.HASH_LENGTH];
		Arrays.fill(targetBytes, Byte.MAX_VALUE);
		BigInteger target = new BigInteger(1, targetBytes);
								
		//DIVIDE TARGET BY BASE TARGET
		BigInteger baseTarget = BigInteger.valueOf(getBaseTarget(getNextBlockGeneratingBalance(dbSet, parentBlock)));
		target = target.divide(baseTarget);
			
		//MULTIPLY TARGET BY USER BALANCE
		target = target.multiply(generatingBalance.toBigInteger());
		
		//CALCULATE GUESSES
		//long guesses = hashValue.divide(target).longValue() + 1;
		BigInteger guesses = hashValue.divide(target).add(BigInteger.ONE);
		
		//CALCULATE TIMESTAMP
		//long timestamp = block.getTimestamp() + (guesses * 1000);
		BigInteger timestamp = guesses.multiply(BigInteger.valueOf(1000)).add(BigInteger.valueOf(parentBlock.getTimestamp()));
		
		//CHECK IF NOT HIGHER THAN MAX LONG VALUE
		if(timestamp.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1)
		{
			timestamp = BigInteger.valueOf(Long.MAX_VALUE);
		}

		byte[] atBytes;
		if ( version > 1 )
		{
			AT_Block atBlock = AT_Controller.getCurrentBlockATs( AT_Constants.getInstance().MAX_PAYLOAD_FOR_BLOCK(
					parentBlock.getHeight(dbSet)) , parentBlock.getHeight(dbSet) + 1 );
			atBytes = atBlock.getBytesForBlock();
		} else {
			atBytes = new byte[0];
		}

		//CREATE NEW BLOCK
		Block newBlock = BlockFactory.getInstance().create(version, parentBlock.getSignature(), timestamp.longValue(), getNextBlockGeneratingBalance(dbSet, parentBlock), account, transactionsHash, atBytes);
		newBlock.sign(account);
		
		return newBlock;
	}
	
	/*
	// for generate only ??
	public byte[] calculateSignature(DBSet dbSet, Block solvingBlock, PrivateKeyAccount account) 
	{	
		byte[] data = new byte[0];
		
		//WRITE PARENT GENERATOR SIGNATURE
		byte[] generatorSignature = Bytes.ensureCapacity(solvingBlock.getSignature(), Block.SIGNATURE_LENGTH, 0);
		data = Bytes.concat(data, generatorSignature);
		
		//WRITE GENERATING BALANCE
		byte[] baseTargetBytes = Longs.toByteArray(getNextBlockGeneratingBalance(dbSet, solvingBlock));
		baseTargetBytes = Bytes.ensureCapacity(baseTargetBytes, Block.GENERATING_BALANCE_LENGTH, 0);
		data = Bytes.concat(data,baseTargetBytes);
		
		//WRITE GENERATOR
		byte[] generatorBytes = Bytes.ensureCapacity(account.getPublicKey(), Block.CREATOR_LENGTH, 0);
		data = Bytes.concat(data, generatorBytes);
								
		//CALC SIGNATURE OF NEWBLOCKHEADER
		byte[] signature = Crypto.getInstance().sign(account, data);
		
		return signature;
	}
	*/
		
	/*
	public void addUnconfirmedTransactions(DBSet db, Block block)
	{
		long totalBytes = 0;
		boolean transactionProcessed;
			
		//CREATE FORK OF GIVEN DATABASE
		DBSet newBlockDb = db.fork();
					
		//ORDER TRANSACTIONS BY FEE PER BYTE
		List<Transaction> orderedTransactions = new ArrayList<Transaction>(db.getTransactionMap().getValues());
		Collections.sort(orderedTransactions, new TransactionFeeComparator());
		//Collections.sort(orderedTransactions, Collections.reverseOrder());
					
		do
		{
			transactionProcessed = false;
						
			for(Transaction transaction: orderedTransactions)
			{
				//CHECK TRANSACTION TIMESTAMP AND DEADLINE
				if(transaction.getTimestamp() <= block.getTimestamp() && transaction.getDeadline() > block.getTimestamp())
				{
					try{
						//CHECK IF VALID
						if(transaction.isValid(newBlockDb, null) == Transaction.VALIDATE_OK)
						{
							//CHECK IF ENOUGH ROOM
							if(totalBytes + transaction.getDataLength(false) <= Block.MAX_TRANSACTION_BYTES)
							{
								//ADD INTO BLOCK
								block.addTransaction(transaction);
											
								//REMOVE FROM LIST
								orderedTransactions.remove(transaction);
											
								//PROCESS IN NEWBLOCKDB
								transaction.process(newBlockDb, false);
											
								//TRANSACTION PROCESSES
								transactionProcessed = true;
								break;
							}
						}
					}catch(Exception e){
                        LOGGER.error(e.getMessage(),e);
                        //REMOVE FROM LIST
                        orderedTransactions.remove(transaction);
                        transactionProcessed = true;
                        break;                    
					}
				}
						
			}
		}
		while(transactionProcessed == true);
	}
	*/
	
	public static List<Transaction> getUnconfirmedTransactions(DBSet db, long timestamp)
	{
		long totalBytes = 0;
		boolean transactionProcessed;
			
		//CREATE FORK OF GIVEN DATABASE
		DBSet newBlockDb = db.fork();
					
		//ORDER TRANSACTIONS BY FEE PER BYTE
		List<Transaction> orderedTransactions = new ArrayList<Transaction>(db.getTransactionMap().getValues());
		Collections.sort(orderedTransactions, new TransactionFeeComparator());
		//Collections.sort(orderedTransactions, Collections.reverseOrder());
		
		List<Transaction> transactionsList = new ArrayList<Transaction>();
		
		do
		{
			transactionProcessed = false;
						
			for(Transaction transaction: orderedTransactions)
			{
				//CHECK TRANSACTION TIMESTAMP AND DEADLINE
				if(transaction.getTimestamp() <= timestamp && transaction.getDeadline() > timestamp)
				{
					try{
						//CHECK IF VALID
						if(transaction.isValid(newBlockDb, null) == Transaction.VALIDATE_OK)
						{
							//CHECK IF ENOUGH ROOM
							if(totalBytes + transaction.getDataLength(false) <= Block.MAX_TRANSACTION_BYTES)
							{
								////ADD INTO BLOCK
								//block.addTransaction(transaction);
								// TAKE IT
								transactionsList.add(transaction);
											
								//REMOVE FROM LIST
								orderedTransactions.remove(transaction);
											
								//PROCESS IN NEWBLOCKDB
								transaction.process(newBlockDb, false);
											
								//TRANSACTION PROCESSES
								transactionProcessed = true;
								break;
							}
						}
					}catch(Exception e){
                        LOGGER.error(e.getMessage(),e);
                        //REMOVE FROM LIST
                        orderedTransactions.remove(transaction);
                        transactionProcessed = true;
                        break;                    
					}
				}
						
			}
		}
		while(transactionProcessed == true);
		
		return transactionsList;
	}
	
	/*public void addObserver(Observer o)
	{
		o.update(null, new ObserverMessage(ObserverMessage.LIST_TRANSACTION_TYPE, DBSet.getInstance().getTransactionMap().getValues()));
	}*/
	
	public static long getNextBlockGeneratingBalance(DBSet db, Block block)
	{
		int height = block.getHeight(db);
		if(height % GenesisBlock.GENERATING_RETARGET == 0)
		{
			//CALCULATE THE GENERATING TIME FOR LAST 10 BLOCKS
			long generatingTime = block.getTimestamp();
				
			//GET FIRST BLOCK OF TARGET
			Block firstBlock = block;
			for(int i=1; i<GenesisBlock.GENERATING_RETARGET; i++)
			{
				firstBlock = firstBlock.getParent(db);
			}
					
			generatingTime -= firstBlock.getTimestamp();
			
			//CALCULATE EXPECTED FORGING TIME
			long expectedGeneratingTime = getBlockTime(block.getGeneratingBalance()) * GenesisBlock.GENERATING_RETARGET * 1000;
			
			//CALCULATE MULTIPLIER
			double multiplier = (double) expectedGeneratingTime / (double) generatingTime;
			
			//CALCULATE NEW GENERATING BALANCE
			long generatingBalance = (long) (block.getGeneratingBalance() * multiplier);
			
			return minMaxBalance(generatingBalance);
		}
		
		return block.getGeneratingBalance();
	}
		
	public static long getBaseTarget(long generatingBalance)
	{
		generatingBalance = minMaxBalance(generatingBalance);
		
		long baseTarget = generatingBalance * getBlockTime(generatingBalance);
		
		return baseTarget;
	}
	
	public static long getBlockTime(long generatingBalance)
	{
		generatingBalance = minMaxBalance(generatingBalance);
		
		double percentageOfTotal = (double) generatingBalance / GenesisBlock.MAX_GENERATING_BALANCE;
		long actualBlockTime = (long) (GenesisBlock.GENERATING_MIN_BLOCK_TIME
				+ ((GenesisBlock.GENERATING_MAX_BLOCK_TIME
						- GenesisBlock.GENERATING_MIN_BLOCK_TIME) * (1 - percentageOfTotal)));
		
		return actualBlockTime;
	}
	
	private static long minMaxBalance(long generatingBalance)
	{
		if(generatingBalance < GenesisBlock.MIN_GENERATING_BALANCE)
		{
			return GenesisBlock.MIN_GENERATING_BALANCE;
		}
		
		if(generatingBalance > GenesisBlock.MAX_GENERATING_BALANCE)
		{
			return GenesisBlock.MAX_GENERATING_BALANCE;
		}
		
		return generatingBalance;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
	ObserverMessage message = (ObserverMessage) arg1;
		
		if(message.getType() == ObserverMessage.WALLET_STATUS || message.getType() == ObserverMessage.NETWORK_STATUS)
		{
			//WALLET ONCE UNLOCKED? WITHOUT UNLOCKING FORGING DISABLED 
			if(!walletOnceUnlocked &&  message.getType() == ObserverMessage.WALLET_STATUS)
			{
				walletOnceUnlocked = true;
			}
			
			if(walletOnceUnlocked)
			{
				// WALLET UNLOCKED OR GENERATORCACHING TRUE
				syncForgingStatus();
			}
		}
		
	}
	
	public void syncForgingStatus()
	{
		if(!Settings.getInstance().isForgingEnabled() || getKnownAccounts().size() == 0) {
			setForgingStatus(ForgingStatus.FORGING_DISABLED);
			return;
		}
		
		int status = Controller.getInstance().getStatus();
		//CONNECTIONS OKE? -> FORGING
		if(status != Controller.STATUS_OK) {
			setForgingStatus(ForgingStatus.FORGING_ENABLED);
			return;
		}

		if (Controller.getInstance().isReadyForging())
			setForgingStatus(ForgingStatus.FORGING);
		else
			setForgingStatus(ForgingStatus.FORGING_WAIT);
	}
	
}
