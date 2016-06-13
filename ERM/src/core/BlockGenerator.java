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

import org.apache.log4j.Logger;

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
		FORGING(2, Lang.getInstance().translate("Forging"));
		
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
			if(DBSet.getInstance().isStoped())
				continue;
			
			//CHECK IF WE ARE UPTODATE
			if(!Controller.getInstance().isUpToDate() && !Controller.getInstance().isProcessingWalletSynchronize())
			{
				Controller.getInstance().update();
			}
			
			//CHECK IF WE HAVE CONNECTIONS
			if(forgingStatus == ForgingStatus.FORGING)
			{
				//GET LAST BLOCK
				byte[] lastBlockSignature = DBSet.getInstance().getBlockMap().getLastBlockSignature();
						
				//CHECK IF DIFFERENT FOR CURRENT SOLVING BLOCK
				if(this.solvingBlock == null || !Arrays.equals(this.solvingBlock.getSignature(), lastBlockSignature))
				{
					//SET NEW BLOCK TO SOLVE
					this.solvingBlock = DBSet.getInstance().getBlockMap().getLastBlock();
					
					//RESET BLOCKS
					this.blocks = new HashMap<PrivateKeyAccount, Block>();
				}
				
				//GENERATE NEW BLOCKS
				if(Controller.getInstance().doesWalletExists())
				{
					//PREVENT CONCURRENT MODIFY EXCEPTION
					List<PrivateKeyAccount> knownAccounts = this.getKnownAccounts();
					synchronized(knownAccounts)
					{
						for(PrivateKeyAccount account: knownAccounts)
						{
							if(account.getGeneratingBalance().compareTo(GenesisBlock.MIN_GENERATING_BALANCE_BD) >= 0)
							{
								//CHECK IF BLOCK FROM USER ALREADY EXISTS USE MAP ACCOUNT BLOCK EASY
								if(!this.blocks.containsKey(account))
								{	
									//GENERATE NEW BLOCK FOR USER
									this.blocks.put(account, this.generateNextBlock(DBSet.getInstance(), account, this.solvingBlock));
								}
							}
						}
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
						this.addUnconfirmedTransactions(DBSet.getInstance(), block);
						
						//ADD TRANSACTION SIGNATURE
						block.setTransactionsSignature(this.calculateTransactionsSignature(block, account));
						
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
						Thread.sleep(100);
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
					Thread.sleep(100);
				} 
				catch (InterruptedException e) 
				{
					LOGGER.error(e.getMessage(),e);
				}
			}
		}
	}
	
	public Block generateNextBlock(DBSet dbSet, PrivateKeyAccount account, Block block)
	{
		//CHECK IF ACCOUNT HAS BALANCE
		if(account.getGeneratingBalance(dbSet).compareTo(GenesisBlock.MIN_GENERATING_BALANCE_BD) < 0)
		{
			return null;
		}

		//CALCULATE SIGNATURE
		byte[] signature = this.calculateSignature(dbSet, block, account);

		//DETERMINE BLOCK VERSION
		int version = block.getNextBlockVersion(dbSet);

		//CALCULATE HASH
		byte[] hash;
		if (version > 0)
		{
			byte[] data = Bytes.concat(block.getSignature(), account.getPublicKey());
			hash = Crypto.getInstance().digest(data);
		}
		else
		{
			hash = null;
		}
		/*
		if ((version < 3)
		{
			hash = Crypto.getInstance().digest(signature);
		}
		else
		{
			//newSig = sha256(prevSig || pubKey)
			byte[] data = Bytes.concat(block.getSignature(), account.getPublicKey());
			hash = Crypto.getInstance().digest(data);
		}
		*/

		//CONVERT HASH TO BIGINT
		BigInteger hashValue = new BigInteger(1, hash);
		
		//CALCULATE ACCOUNT TARGET
		byte[] targetBytes = new byte[32];
		Arrays.fill(targetBytes, Byte.MAX_VALUE);
		BigInteger target = new BigInteger(1, targetBytes);
								
		//DIVIDE TARGET BY BASE TARGET
		BigInteger baseTarget = BigInteger.valueOf(getBaseTarget(getNextBlockGeneratingBalance(dbSet, block)));
		target = target.divide(baseTarget);
			
		//MULTIPLY TARGET BY USER BALANCE
		target = target.multiply(account.getGeneratingBalance(dbSet).toBigInteger());
		
		//CALCULATE GUESSES
		//long guesses = hashValue.divide(target).longValue() + 1;
		BigInteger guesses = hashValue.divide(target).add(BigInteger.ONE);
		
		//CALCULATE TIMESTAMP
		//long timestamp = block.getTimestamp() + (guesses * 1000);
		BigInteger timestamp = guesses.multiply(BigInteger.valueOf(1000)).add(BigInteger.valueOf(block.getTimestamp()));
		
		//CHECK IF NOT HIGHER THAN MAX LONG VALUE
		if(timestamp.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1)
		{
			timestamp = BigInteger.valueOf(Long.MAX_VALUE);
		}
		
		//CREATE NEW BLOCK
		Block newBlock;
		if ( version > 1 )
		{
			AT_Block atBlock = AT_Controller.getCurrentBlockATs( AT_Constants.getInstance().MAX_PAYLOAD_FOR_BLOCK(
					block.getHeight(dbSet)) , block.getHeight(dbSet) + 1 );
			byte[] atBytes = atBlock.getBytesForBlock();
			newBlock = BlockFactory.getInstance().create(version, block.getSignature(), timestamp.longValue(), getNextBlockGeneratingBalance(dbSet, block), account, signature, atBytes, atBlock.getTotalFees());
		}
		else
		{
			newBlock = BlockFactory.getInstance().create(version, block.getSignature(), timestamp.longValue(), getNextBlockGeneratingBalance(dbSet, block), account, signature);
		}
		return newBlock;
	}
	
	public byte[] calculateSignature(DBSet dbSet, Block solvingBlock, PrivateKeyAccount account) 
	{	
		byte[] data = new byte[0];
		
		//WRITE PARENT GENERATOR SIGNATURE
		byte[] generatorSignature = Bytes.ensureCapacity(solvingBlock.getGeneratorSignature(), Block.GENERATOR_SIGNATURE_LENGTH, 0);
		data = Bytes.concat(data, generatorSignature);
		
		//WRITE GENERATING BALANCE
		byte[] baseTargetBytes = Longs.toByteArray(getNextBlockGeneratingBalance(dbSet, solvingBlock));
		baseTargetBytes = Bytes.ensureCapacity(baseTargetBytes, Block.GENERATING_BALANCE_LENGTH, 0);
		data = Bytes.concat(data,baseTargetBytes);
		
		//WRITE GENERATOR
		byte[] generatorBytes = Bytes.ensureCapacity(account.getPublicKey(), Block.GENERATOR_LENGTH, 0);
		data = Bytes.concat(data, generatorBytes);
								
		//CALC SIGNATURE OF NEWBLOCKHEADER
		byte[] signature = Crypto.getInstance().sign(account, data);
		
		return signature;
	}
	
	public byte[] calculateTransactionsSignature(Block block, PrivateKeyAccount account) 
	{	
		byte[] data = block.getGeneratorSignature();
		
		//WRITE TRANSACTION SIGNATURE
		for(Transaction transaction: block.getTransactions())
		{
			data = Bytes.concat(data, transaction.getSignature());
		}
		
		return Crypto.getInstance().sign(account, data);
	}
	
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
		if(!Settings.getInstance().isForgingEnabled()) {
			setForgingStatus(ForgingStatus.FORGING_DISABLED);
			return;
		}
		
		if(getKnownAccounts().size() > 0)
		{
			//CONNECTIONS OKE? -> FORGING
			if(Controller.getInstance().getStatus() == Controller.STATUS_OK) {
				setForgingStatus(ForgingStatus.FORGING);
			} else {
				setForgingStatus(ForgingStatus.FORGING_ENABLED);
			}
		} else {
			setForgingStatus(ForgingStatus.FORGING_DISABLED);
		}
	}
	
}
