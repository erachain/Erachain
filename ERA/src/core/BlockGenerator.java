package core;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;
import ntp.NTP;
import settings.Settings;
import utils.ObserverMessage;
import utils.TransactionTimestampComparator;
import at.AT_Block;
import at.AT_Constants;
import at.AT_Controller;

import controller.Controller;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.block.BlockFactory;
import core.transaction.Transaction;
import datachain.DCSet;
import lang.Lang;

public class BlockGenerator extends Thread implements Observer
{	
	
	static Logger LOGGER = Logger.getLogger(BlockGenerator.class.getName());
	
	private static final int MAX_BLOCK_SIZE = BlockChain.HARD_WORK?20000:1000;
	private static final int MAX_BLOCK_SIZE_BYTE = 
			BlockChain.HARD_WORK?BlockChain.MAX_BLOCK_BYTES:BlockChain.MAX_BLOCK_BYTES>>2;

	static final int wait_interval_flush = BlockChain.GENERATING_MIN_BLOCK_TIME_MS>>2;
	private PrivateKeyAccount acc_winner;
	private List<Block> lastBlocksForTarget;
	private byte[] solvingReference;
	
	private List<PrivateKeyAccount> cachedAccounts;
	
	private ForgingStatus forgingStatus = ForgingStatus.FORGING_DISABLED;
	private boolean walletOnceUnlocked = false;
	private static int status;

	
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

    public int getStatus()
    {
        return status;
    }	

    public static String viewStatus()
    {
    	
		switch (status) {
		case 1:
			return "1:FLUSH, WAIT";
		case 2:
			return "2:FLUSH, TRY";
		case 3:
			return "3:UPDATE";
		case 4:
			return "4:FORGING";
		case 5:
			return "5:FORGING BY ACCOUNTS";
		case 6:
			return "6:MAKING NEW BLOCK";
		case 7:
			return "7:BROADCAST, WAIT";
		case 8:
			return "8:BROADCAST, TRY";
		default:
			return "unknown";
		}
    }	

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
						Thread.sleep(500);
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
		this.addUnconfirmedTransaction(DCSet.getInstance(), transaction);
	}
	public void addUnconfirmedTransaction(DCSet db, Transaction transaction) 
	{
		//ADD TO TRANSACTION DATABASE 
		db.getTransactionMap().add(transaction);
	}
	
	public List<Transaction> getUnconfirmedTransactions()
	{
		return new ArrayList<Transaction>(DCSet.getInstance().getTransactionMap().getValues());
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
	
	public void setForgingStatus(ForgingStatus status)
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
		BlockChain bchain = ctrl.getBlockChain();
		DCSet dcSet = DCSet.getInstance();

		while(!ctrl.isOnStopping())
		{
			
			status = 0;

			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
			}
			
			if (ctrl.isOnStopping())
				return;
			
			////////////////////////////  FLUSH NEW BLOCK /////////////////////////

			// try solve and flush new block from Win Buffer		
			Block waitWin = bchain.getWaitWinBuffer();
			while (waitWin != null) {
				
				status = 1;
				
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
				}

				if (ctrl.isOnStopping())
					return;
				
				// REFRESH
				waitWin = bchain.getWaitWinBuffer();
				//long diffTimeWinBlock =  NTP.getTime() - wait_interval_flush - waitWin.getTimestamp(dcSet);
				//if (diffTimeWinBlock < 0 )
				//	continue;
				if(waitWin.getTimestamp(dcSet) + BlockChain.WIN_BLOCK_BROADCAST_WAIT > NTP.getTime()) {
					continue;
				}
				
				// FLUSH WINER to DB MAP
				LOGGER.info("FLUSH WINER to DB MAP");

				try {
					
					status = 2;
					
					ctrl.flushNewBlockGenerated();
					
					if (ctrl.isOnStopping())
						return;
						
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (ctrl.isOnStopping())
						return;
					LOGGER.error(e.getMessage(), e);
				}
				break;
			}
			
			////////////////////////// UPDATE ////////////////////
			//CHECK IF WE ARE NOT UP TO DATE
			ctrl.checkStatus(ctrl.getStatus());
			if(ctrl.needUpToDate())
			{
				status = 3;

				if (ctrl.isOnStopping())
					return;
				
				bchain.clearWaitWinBuffer();
				
				ctrl.update();
				
				try {
					Thread.sleep(10000);
				}
				catch (InterruptedException e) {
				}
				
				if (ctrl.isOnStopping())
					return;

				continue;
			}
			
			if (ctrl.isOnStopping()) {
				return;
			}

			// NO WALLET - loop
			if(!ctrl.doesWalletExists())
				continue;

			//CHECK IF WE HAVE CONNECTIONS and READY to GENERATE
			syncForgingStatus();
			if (forgingStatus == ForgingStatus.FORGING // FORGING enabled
					&& (this.solvingReference == null // AND GENERATING NOT MAKED
						|| !Arrays.equals(this.solvingReference, dcSet.getBlockMap().getLastBlockSignature()))
					)
			{
				
				/////////////////////////////// TRY FORGING ////////////////////////

				if(dcSet.isStoped()) {
					return;
				}

				//SET NEW BLOCK TO SOLVE
				this.solvingReference = dcSet.getBlockMap().getLastBlockSignature();
				Block solvingBlock = dcSet.getBlockMap().get(this.solvingReference);
				
				//set max block
				if (BlockChain.BLOCK_COUNT >0 && solvingBlock.getHeight(dcSet) > BlockChain.BLOCK_COUNT ) return;

				if(dcSet.isStoped()) {
					return;
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

				status = 4;
				
				//GENERATE NEW BLOCKS
				this.lastBlocksForTarget = bchain.getLastBlocksForTarget(dcSet);				
				this.acc_winner = null;
				
				List<Transaction> unconfirmedTransactions = null;
				byte[] unconfirmedTransactionsHash = null;
				long max_winned_value = 0;
				long winned_value;				
				int height = bchain.getHeight(dcSet) + 1;
				long target = bchain.getTarget(dcSet);

				//PREVENT CONCURRENT MODIFY EXCEPTION
				List<PrivateKeyAccount> knownAccounts = this.getKnownAccounts();
				synchronized(knownAccounts)
				{
					
					status = 5;
					
					for(PrivateKeyAccount account: knownAccounts)
					{
						
						winned_value = account.calcWinValue(dcSet, bchain, this.lastBlocksForTarget, height, target);
						if(winned_value < 1l)
							continue;
						
						if (winned_value > max_winned_value) {
							//this.winners.put(account, winned_value);
							acc_winner = account;
							max_winned_value = winned_value;
							
						}
					}
				}
				
				if(acc_winner == null)
					continue;
				
				if (ctrl.isOnStopping())
					return;

				status = 6;

				int wait_new_block_broadcast = (int)(solvingBlock.getTimestamp(dcSet) + BlockChain.GENERATING_MIN_BLOCK_TIME_MS + wait_interval_flush - NTP.getTime());
				
				if (wait_new_block_broadcast + BlockChain.GENERATING_MIN_BLOCK_TIME_MS < 0) {
					wait_new_block_broadcast = -1;
				} else {
					// WAIT
					if (target + (target>>1) < max_winned_value) {
						wait_new_block_broadcast -= wait_interval_flush>>1 + wait_interval_flush>>3;
					} else if (target + (target>>2) < max_winned_value) {
						wait_new_block_broadcast -= wait_interval_flush>>2;
					} else if (target + (target>>3) < max_winned_value) {
						wait_new_block_broadcast -= wait_interval_flush>>3;
					} else if (target + (target>>4) < max_winned_value) {
						wait_new_block_broadcast -= wait_interval_flush>>4;
					} else if (target < max_winned_value) {
						wait_new_block_broadcast += wait_interval_flush>>4;
					} else {
						wait_new_block_broadcast += (wait_interval_flush>>4)
								+ (long)wait_interval_flush * target / max_winned_value;
					}
				}	
				
				if (wait_new_block_broadcast > 0) {
					LOGGER.info("@@@@@@@@ wait for new winner and BROADCAST: " + wait_new_block_broadcast/1000);
					// SLEEP and WATCH break
					long wait_steep = wait_new_block_broadcast / 500;
					long maxTime = solvingBlock.getTimestamp(dcSet) + (BlockChain.GENERATING_MIN_BLOCK_TIME_MS<<1);
					boolean newWinner = false;
					do {
						try
						{
							Thread.sleep(500);
						}
						catch (InterruptedException e) 
						{
						}
						
						waitWin = bchain.getWaitWinBuffer();
						if (waitWin != null && waitWin.calcWinValue(dcSet) > max_winned_value) {
							// NEW WINNER received
							newWinner = true;
							break;
						}

						if (ctrl.isOnStopping())
							return;
						
					} while (wait_steep-- > 0 && NTP.getTime() < maxTime);

					if (newWinner)
					{
						LOGGER.info("NEW WINER RECEIVED - drop my block");
						continue;
					}
				}

				
				// MAKING NEW BLOCK
				status = 7;

				// GET VALID UNCONFIRMED RECORDS for current TIMESTAMP
				long newTimestamp = BlockChain.GENERATING_MIN_BLOCK_TIME_MS + bchain.getTimestamp(dcSet);
				LOGGER.info("GENERATE my BLOCK after timePOINT: " + (NTP.getTime() - newTimestamp)/1000);

				unconfirmedTransactions = getUnconfirmedTransactions(dcSet, newTimestamp);
				// CALCULATE HASH for that transactions
				byte[] winnerPubKey = acc_winner.getPublicKey();
				byte[] atBytes = null;
				unconfirmedTransactionsHash = Block.makeTransactionsHash(winnerPubKey, unconfirmedTransactions, atBytes);

				//ADD TRANSACTIONS
				//this.addUnconfirmedTransactions(dcSet, block);
				Block generatedBlock = generateNextBlock(dcSet, acc_winner, 
						solvingBlock, unconfirmedTransactionsHash);
				generatedBlock.setTransactions(unconfirmedTransactions);
				
				//PASS BLOCK TO CONTROLLER
				///ctrl.newBlockGenerated(block);
				LOGGER.info("bchain.setWaitWinBuffer, size: " + generatedBlock.getTransactionCount());
				if (bchain.setWaitWinBuffer(dcSet, generatedBlock)) {

					// need to BROADCAST

					// REFRESH
					waitWin = bchain.getWaitWinBuffer();
					if (waitWin != null)
					{
						long wait_winned_value = waitWin.calcWinValue(dcSet);
						if (wait_winned_value > max_winned_value) {
							// block in buffer is more good
							LOGGER.info("wait_winned_value > max_winned_value (new): " + wait_winned_value + ">" + max_winned_value);
							continue;
						}
					}
					
					status = 8;
					
					long timeBlock = generatedBlock.getTimestamp(dcSet) + BlockChain.WIN_BLOCK_BROADCAST_WAIT;
					while(timeBlock > NTP.getTime()) {
						try
						{
							Thread.sleep(500);
						}
						catch (InterruptedException e) 
						{
						}
					}

					ctrl.broadcastWinBlock(generatedBlock, null);
	
				}
			}
		}
	}
	
	public static Block generateNextBlock(DCSet dcSet, PrivateKeyAccount account,
			Block parentBlock, byte[] transactionsHash)
	{
		
		int version = parentBlock.getNextBlockVersion(dcSet);
		byte[] atBytes;
		if ( version > 1 )
		{
			AT_Block atBlock = AT_Controller.getCurrentBlockATs( AT_Constants.getInstance().MAX_PAYLOAD_FOR_BLOCK(
					parentBlock.getHeight(dcSet)) , parentBlock.getHeight(dcSet) + 1 );
			atBytes = atBlock.getBytesForBlock();
		} else {
			atBytes = new byte[0];
		}

		//CREATE NEW BLOCK
		Block newBlock = BlockFactory.getInstance().create(version, parentBlock.getSignature(), account,
				transactionsHash, atBytes);
		// SET GENERATING BALANCE here
		newBlock.setCalcGeneratingBalance(dcSet);
		newBlock.sign(account);
		
		return newBlock;

	}
	
	public static List<Transaction> getUnconfirmedTransactions(DCSet db, long timestamp)
	{
		
		long timrans1 = System.currentTimeMillis();
					
		//CREATE FORK OF GIVEN DATABASE
		DCSet newBlockDb = db.fork();
		Controller ctrl = Controller.getInstance();
					
		//ORDER TRANSACTIONS BY FEE PER BYTE
		DCSet dcSet = DCSet.getInstance();
		
		long start = System.currentTimeMillis();
		LOGGER.error("get orderedTransactions");
		List<Transaction> orderedTransactions = new ArrayList<Transaction>(dcSet.getTransactionMap().getValues());
		long tickets = System.currentTimeMillis() - start;
		LOGGER.error(" time " + tickets);

		// TODO make SORT by FEE to!
		// toBYTE / FEE + TIMESTAMP !!
		////Collections.sort(orderedTransactions, new TransactionFeeComparator());
		// sort by TIMESTAMP
		Collections.sort(orderedTransactions, new TransactionTimestampComparator());
		long tickets2 = System.currentTimeMillis() - start - tickets;
		LOGGER.error("sort time " + tickets2);
		
		//Collections.sort(orderedTransactions, Collections.reverseOrder());
		
		List<Transaction> transactionsList = new ArrayList<Transaction>();

		boolean transactionProcessed;
		long totalBytes = 0;
		int count = 0;

		do
		{
			transactionProcessed = false;
						
			for(Transaction transaction: orderedTransactions)
			{
								
				if (ctrl.isOnStopping()) {
					return null;
				}

				try{

					//CHECK TRANSACTION TIMESTAMP AND DEADLINE
					if(transaction.getTimestamp() > timestamp || transaction.getDeadline() < timestamp) {
						// OFF TIME
						// REMOVE FROM LIST
						transactionProcessed = true;
						orderedTransactions.remove(transaction);
						break;
					}
					
					//CHECK IF VALID
					if(!transaction.isSignatureValid()) {
						// INVALID TRANSACTION
						// REMOVE FROM LIST
						transactionProcessed = true;
						orderedTransactions.remove(transaction);
						break;
					}
						
					transaction.setDB(newBlockDb, false);
					
					if (transaction.isValid(newBlockDb, null) != Transaction.VALIDATE_OK) {
						// INVALID TRANSACTION
						// REMOVE FROM LIST
						transactionProcessed = true;
						orderedTransactions.remove(transaction);
						break;
					}
														
					//CHECK IF ENOUGH ROOM
					totalBytes += transaction.getDataLength(false);

					if(totalBytes > MAX_BLOCK_SIZE_BYTE
							|| ++count> MAX_BLOCK_SIZE)
						break;
					
					////ADD INTO LIST
					transactionsList.add(transaction);
								
					//REMOVE FROM LIST
					orderedTransactions.remove(transaction);
								
					//PROCESS IN NEWBLOCKDB
					transaction.process(newBlockDb, null, false);
								
					//TRANSACTION PROCESSES
					transactionProcessed = true;
										
					// GO TO NEXT TRANSACTION
					break;
						
				} catch (Exception e) {
					
					if (ctrl.isOnStopping()) {
						return null;
					}

                    transactionProcessed = true;

                    LOGGER.error(e.getMessage(), e);
                    //REMOVE FROM LIST

                    break;                    
				}
				
			}
		}
		while(count < MAX_BLOCK_SIZE && totalBytes < MAX_BLOCK_SIZE_BYTE && transactionProcessed == true);

		long start2 = System.currentTimeMillis();

		// sort by TIMESTAMP
		Collections.sort(transactionsList,  new TransactionTimestampComparator());

		long start22 = System.currentTimeMillis() - start2;

		LOGGER.debug("get Unconfirmed Transactions =" + start22 +"milsec for trans: " + transactionsList.size() );
		
		return transactionsList;
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
		
		Controller ctrl = Controller.getInstance();
		int status = ctrl.getStatus();
		//CONNECTIONS OKE? -> FORGING
		// CONNECTION not NEED now !!
		// TARGET_WIN will be small
		if(status != Controller.STATUS_OK
				///|| ctrl.isProcessingWalletSynchronize()
				) {
			setForgingStatus(ForgingStatus.FORGING_ENABLED);
			return;
		}

		// NOT NEED to wait - TARGET_WIN will be small
		if (Controller.getInstance().isReadyForging())
			setForgingStatus(ForgingStatus.FORGING);
		else
			setForgingStatus(ForgingStatus.FORGING_WAIT);
	}
	
}
