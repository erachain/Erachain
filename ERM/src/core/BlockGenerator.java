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
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;
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
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
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
	
	//private Map<PrivateKeyAccount, Block> blocks;
	private Map<PrivateKeyAccount, Long> winners;
	private PrivateKeyAccount acc_winner;
	private Block solvingBlock;
	private int solvingBlockHeight;
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

		Controller ctrl = Controller.getInstance();

		if(!ctrl.doesWalletExists())
			return;

		DBSet dbSet = DBSet.getInstance();
		boolean isGenesisStart = false;

		int wait_interval_run = 2000;
		int wait_interval_flush = 20000;
		int wait_interval = wait_interval_run;
		while(true)
		{
			
			// try solve and flush new block from Win Buffer			
			Block waitWin = ctrl.getBlockChain().getWaitWinBuffer();
			if (waitWin != null
					&& waitWin.getTimestamp(dbSet) + wait_interval_flush < NTP.getTime()) {
				ctrl.flushNewBlockGenerated();
				
				wait_interval = Block.GENERATING_MIN_BLOCK_TIME
						- wait_interval_flush - wait_interval_run;
				continue;
			}

			try 
			{
				Thread.sleep(wait_interval);
			} 
			catch (InterruptedException e) 
			{
				LOGGER.error(e.getMessage(),e);
			}

			if(dbSet.isStoped()) {
				continue;
			}

			//CHECK IF WE ARE UP TO DATE
			if(!ctrl.isUpToDate() && !ctrl.isProcessingWalletSynchronize())
			{
				ctrl.update();
			}
			
			//CHECK IF WE HAVE CONNECTIONS and READY to GENERATE
			syncForgingStatus();
			if(forgingStatus == ForgingStatus.FORGING && ctrl.isReadyForging())
			{
				
				//GET LAST BLOCK
				// TODO lastBlock error
				// TEST DB for last block - some time error raised
				Block lastBlock = ctrl.getLastBlock();
				if (lastBlock == null) {
					return;
				}
				
				byte[] lastBlockSignature = dbSet.getBlockMap().getLastBlockSignature();
						
				//CHECK IF DIFFERENT FOR CURRENT SOLVING BLOCK
				if(this.solvingBlock == null
						||  ctrl.getBlockChain().getWaitWinBuffer() == null
						|| !Arrays.equals(this.solvingBlock.getSignature(), lastBlockSignature)
						)
				{
					
					//SET NEW BLOCK TO SOLVE
					this.solvingBlock = dbSet.getBlockMap().getLastBlock();
					this.solvingBlockHeight = this.solvingBlock.getHeight(dbSet);
					
					//RESET BLOCKS
					//this.blocks = new HashMap<PrivateKeyAccount, Block>();
					this.winners = new HashMap<PrivateKeyAccount, Long>();
					
					this.acc_winner = null;

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
					byte[] unconfirmedTransactionsHash = null;
					long max_winned_value = 0;
					long winned_value;
	
					// TODO if empty TRNSACTIONS - longer time
					long newTimestamp = this.solvingBlock.getTimestamp(dbSet)
							+ Block.GENERATING_MIN_BLOCK_TIME;
					
					
					if (newTimestamp + 3000 > NTP.getTime()) {
						wait_interval = wait_interval_run;
						continue;
					} else if (newTimestamp + 2* Block.GENERATING_MIN_BLOCK_TIME < NTP.getTime()
							&& isGenesisStart) {
						// QUICK MAKE CHAIN
						wait_interval = 200;
					}

					//PREVENT CONCURRENT MODIFY EXCEPTION
					List<PrivateKeyAccount> knownAccounts = this.getKnownAccounts();
					synchronized(knownAccounts)
					{

						int height = Controller.getInstance().getMyHeight();
						
						for(PrivateKeyAccount account: knownAccounts)
						{
							/*
							//CHECK IF BLOCK FROM USER ALREADY EXISTS USE MAP ACCOUNT BLOCK EASY
							if(this.winners.containsKey(account))
								continue;
								*/

							long generatingBalance = account.getGeneratingBalance();
							if(generatingBalance < GenesisBlock.MIN_GENERATING_BALANCE)
								continue;
							
							//GENERATE NEW BLOCK FOR USER
							// already signed
							winned_value = account.calcWinValue(dbSet, height);
							if (winned_value > max_winned_value) {
								//this.winners.put(account, winned_value);
								acc_winner = account;
								max_winned_value = winned_value;
								
								if (account.getAddress().equals("7EpDngzSLXrqnRBJ5x9YKTU395VEpsz5Mz")) {
									isGenesisStart = true;
								}
							}
						}
					}
					
					if(acc_winner != null)
					{
						
						LOGGER.error("core.BlockGenerator.run() - selected account: "
								+ max_winned_value + " " + acc_winner.getAddress());

						// GET VALID UNCONFIRMED RECORDS for current TIMESTAMP
						unconfirmedTransactions = getUnconfirmedTransactions(dbSet, newTimestamp);
						// CALCULATE HASH for that transactions
						unconfirmedTransactionsHash = Block.makeTransactionsHash(unconfirmedTransactions);
	
						//ADD TRANSACTIONS
						//this.addUnconfirmedTransactions(dbSet, block);
						Block block = generateNextBlock(dbSet, acc_winner, 
								this.solvingBlock, unconfirmedTransactionsHash);
						block.setTransactions(unconfirmedTransactions);
						
						//PASS BLOCK TO CONTROLLER
						///ctrl.newBlockGenerated(block);
						if (ctrl.getBlockChain().setWaitWinBuffer(block)) {
							// need to BROADCAST
							ctrl.broadcastBlock(block, null);
						}
						
					}
				}
			}
		}
	}
	
	public static Block generateNextBlock(DBSet dbSet, PrivateKeyAccount account,
			Block parentBlock, byte[] transactionsHash)
	{
		
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

		//CREATE NEW BLOCK
		Block newBlock = BlockFactory.getInstance().create(version, parentBlock.getSignature(), account,
				transactionsHash, atBytes);
		newBlock.sign(account);
		
		return newBlock;

	}
	
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
							if(totalBytes + transaction.getDataLength(false) <= GenesisBlock.MAX_TRANSACTION_BYTES)
							{
								
								totalBytes += transaction.getDataLength(false);

								////ADD INTO LIST
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
	
	/*
	public static long getNextBlockGeneratingBalance_old(DBSet db, Block block)
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
	*/
	/*
	public static long getBlockTime_old(long generatingBalance)
	{
		generatingBalance = minMaxBalance(generatingBalance);
		
		double percentageOfTotal = (double) generatingBalance / GenesisBlock.MAX_GENERATING_BALANCE;
		long actualBlockTime = (long) (GenesisBlock.GENERATING_MIN_BLOCK_TIME
				+ ((GenesisBlock.GENERATING_MAX_BLOCK_TIME
						- GenesisBlock.GENERATING_MIN_BLOCK_TIME) * (1 - percentageOfTotal)));
		
		return actualBlockTime;
	}
	*/
	
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
