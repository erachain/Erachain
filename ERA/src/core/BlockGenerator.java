package core;


import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import ntp.NTP;
import settings.Settings;
import utils.ObserverMessage;
import utils.TransactionTimestampComparator;
import at.AT_Block;
import at.AT_Constants;
import at.AT_Controller;

import controller.Controller;
import core.BlockGenerator.ForgingStatus;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.block.BlockFactory;
import core.transaction.Transaction;
import datachain.DCSet;
import datachain.TransactionMap;
import lang.Lang;
import network.Peer;
import network.message.Message;
import network.message.MessageFactory;
import network.message.SignaturesMessage;

public class BlockGenerator extends Thread implements Observer
{	
	
	public static final boolean TEST_001 = true;
	static Logger LOGGER = Logger.getLogger(BlockGenerator.class.getName());
	
	private static final int MAX_BLOCK_SIZE = BlockChain.HARD_WORK?22221:1000;
	public static final int MAX_BLOCK_SIZE_BYTE = 
			BlockChain.HARD_WORK?BlockChain.MAX_BLOCK_BYTES:BlockChain.MAX_BLOCK_BYTES>>2;

	static final int FLUSH_TIMEPOINT = BlockChain.GENERATING_MIN_BLOCK_TIME_MS - (BlockChain.GENERATING_MIN_BLOCK_TIME_MS>>2);
	static final int WIN_TIMEPOINT = BlockChain.GENERATING_MIN_BLOCK_TIME_MS>>2;
	private PrivateKeyAccount acc_winner;
	private List<Block> lastBlocksForTarget;
	private byte[] solvingReference;
	
	private List<PrivateKeyAccount> cachedAccounts;
	private static Controller ctrl = Controller.getInstance();

	
	private ForgingStatus forgingStatus = ForgingStatus.FORGING_DISABLED;
	private boolean walletOnceUnlocked = false;
	private static int status = 0;

	private int orphanto = 0;
	
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

    public static int getStatus()
    {
        return status;
    }	

    public void setOrphanTo(int height)
    {
    	this.orphanto =  height;
    }	

    public static String viewStatus()
    {
    	
		switch (status) {
		case -1:
			return "-1 STOPed";
		case 1:
			return "1 FLUSH, WAIT";
		case 2:
			return "2 FLUSH, TRY";
		case 3:
			return "3 UPDATE";
		case 31:
			return "31 UPDATE SAME";
		case 41:
			return "41 WAIT MAKING";
		case 4:
			return "4 PREPARE MAKING";
		case 5:
			return "5 GET WIN ACCOUNT";
		case 6:
			return "6 WAIT BEFORE MAKING";
		case 7:
			return "7 MAKING NEW BLOCK";
		case 8:
			return "8 BROADCASTING";
		default:
			return "0 WAIT";
		}
    }	

	public BlockGenerator(boolean withObserve)
	{
		if(Settings.getInstance().isGeneratorKeyCachingEnabled())
		{
			this.cachedAccounts = new ArrayList<PrivateKeyAccount>();
		}
		
		if (withObserve) addObserver();
		this.setName("Thread BlockGenerator - "+ this.getId());
	}

	public void addObserver() {
		new Thread()
		{
			@Override
			public void run() {
				
				//WE HAVE TO WAIT FOR THE WALLET TO ADD THAT LISTENER.
				while(!ctrl.doesWalletExists() || !ctrl.doesWalletDatabaseExists())
				{
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
//						does not matter
					}
				}
				
				ctrl.addWalletListener(BlockGenerator.this);
				syncForgingStatus();
			}
		}.start();
		ctrl.addObserver(this);
	}
	
	public boolean addUnconfirmedTransaction(Transaction transaction)
	{
		return this.addUnconfirmedTransaction(DCSet.getInstance(), transaction);
	}
	public boolean addUnconfirmedTransaction(DCSet db, Transaction transaction) 
	{
		
		if (transaction.getDeadline() < NTP.getTime())
			return false;
		
		//ADD TO TRANSACTION DATABASE 
		return !db.getTransactionMap().add(transaction);
		
	}
		
	private List<PrivateKeyAccount> getKnownAccounts()
	{
		//CHECK IF CACHING ENABLED
		if(Settings.getInstance().isGeneratorKeyCachingEnabled())
		{
			List<PrivateKeyAccount> privateKeyAccounts = ctrl.getPrivateKeyAccounts();
			
			//IF ACCOUNTS EXISTS
			if(!privateKeyAccounts.isEmpty())
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
			return ctrl.getPrivateKeyAccounts();
		}
	}
	
	public void setForgingStatus(ForgingStatus status)
	{
		if(forgingStatus != status)
		{
			forgingStatus = status;
			ctrl.forgingStatusChanged(forgingStatus);
		}
	}
	
	
	public void run()
	{

		BlockChain bchain = ctrl.getBlockChain();
		DCSet dcSet = DCSet.getInstance();

		Peer peer = null;
		long timeTmp;
		long timePoint = 0;
		long flushPoint = 0;
		Block waitWin = null;
		long timeUpdate = 0;
		int shift_height = 0;
		byte[] unconfirmedTransactionsHash;
		long max_winned_value;
		long winned_value;				
		long winned_value_account;
		long max_winned_value_account;
		int height = BlockChain.getHeight(dcSet) + 1;
		long target = bchain.getTarget(dcSet);
		Block generatedBlock;

		int wait_new_block_broadcast;
		long wait_steep;
		boolean newWinner;


		while(!ctrl.isOnStopping())
		{
			try {
				
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
				}
				
				if (ctrl.isOnStopping()) {
					status = -1;
					return;
				}
				
				if (this.orphanto > 0) {
					status = 9;
					ctrl.setForgingStatus(ForgingStatus.FORGING_WAIT);
					try
					{
						while (bchain.getHeight(dcSet) > this.orphanto) {
							Block block = bchain.getLastBlock(dcSet);
							ctrl.orphanInPipe(bchain.getLastBlock(dcSet));
						}
					}
					catch(Exception e)
					{
						LOGGER.error(e.getMessage(), e);
					}
					this.orphanto = 0;
					ctrl.checkStatusAndObserve(0);

				}
				
				timeTmp = bchain.getTimestamp(dcSet) + BlockChain.GENERATING_MIN_BLOCK_TIME_MS;
				if (timeTmp > NTP.getTime())
					continue;
				
				if (timePoint != timeTmp) {
					timePoint = timeTmp;
					Timestamp timestampPoit = new Timestamp(timePoint);
					dcSet.getTransactionMap().clear(timePoint - BlockChain.GENERATING_MIN_BLOCK_TIME_MS);
					
					LOGGER.info("+ + + + + START GENERATE POINT on " + timestampPoit);

					flushPoint = FLUSH_TIMEPOINT + timePoint;
					this.solvingReference = null;
					status = 0;

					// GET real HWeight
					ctrl.pingAllPeers(false);						
					
				}
				
				// is WALLET
				if(ctrl.doesWalletExists()) {
	

					if (timePoint + BlockChain.WIN_BLOCK_BROADCAST_WAIT_MS > NTP.getTime()) {
						continue;
					}

					status = 41;
					
					//CHECK IF WE HAVE CONNECTIONS and READY to GENERATE
					syncForgingStatus();
					
					//Timestamp timestamp = new Timestamp(NTP.getTime());
					//LOGGER.info("NTP.getTime() " + timestamp);
					
					//waitWin = bchain.getWaitWinBuffer();

					ctrl.checkStatusAndObserve(1);
					if (forgingStatus == ForgingStatus.FORGING // FORGING enabled
							&& !ctrl.needUpToDate()
							&& (this.solvingReference == null // AND GENERATING NOT MAKED
								|| !Arrays.equals(this.solvingReference, dcSet.getBlockMap().getLastBlockSignature())
							)
						)
					{
						
						/////////////////////////////// TRY FORGING ////////////////////////
		
						if(ctrl.isOnStopping()) {
							status = -1;
							return;
						}
		
						//SET NEW BLOCK TO SOLVE
						this.solvingReference = dcSet.getBlockMap().getLastBlockSignature();
						Block solvingBlock = dcSet.getBlockMap().get(this.solvingReference);
								
						if(ctrl.isOnStopping()) {
							status = -1;
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
						
						
						unconfirmedTransactionsHash = null;
						max_winned_value = 0;
						max_winned_value_account = 0;
						height = bchain.getHeight(dcSet) + 1;
						target = bchain.getTarget(dcSet);
		
						///if (height > BlockChain.BLOCK_COUNT) return;

						//PREVENT CONCURRENT MODIFY EXCEPTION
						List<PrivateKeyAccount> knownAccounts = this.getKnownAccounts();
						synchronized(knownAccounts)
						{
							
							status = 5;
							
							for(PrivateKeyAccount account: knownAccounts)
							{
							
								winned_value_account = Block.calcGeneratingBalance(dcSet, account, height);
								winned_value = account.calcWinValue(dcSet, bchain, this.lastBlocksForTarget, height, target);
								if(winned_value < 1l)
									continue;
								
								if (winned_value > max_winned_value) {
									//this.winners.put(account, winned_value);
									acc_winner = account;
									max_winned_value = winned_value;
									max_winned_value_account = winned_value_account;
									
								}
							}
						}
						
						if(acc_winner != null) {
							
							if (ctrl.isOnStopping()) {
								status = -1;
								return;
							}
			
							wait_new_block_broadcast = (int)((WIN_TIMEPOINT>>1) + WIN_TIMEPOINT * 4 * (target - max_winned_value) / target);
							
							newWinner = false;
							if (wait_new_block_broadcast > 0) {
	
								status = 6;
								
								LOGGER.info("@@@@@@@@ wait for new winner and BROADCAST: " + wait_new_block_broadcast/1000);
								// SLEEP and WATCH break
								wait_steep = wait_new_block_broadcast / 100;
								do {
									try
									{
										Thread.sleep(100);
									}
									catch (InterruptedException e) 
									{
									}
									
									if (ctrl.isOnStopping()) {
										status = -1;
										return;
									}
	
									waitWin = bchain.getWaitWinBuffer();
									if (waitWin != null && waitWin.calcWinValue(dcSet) > max_winned_value) {
										// NEW WINNER received
										newWinner = true;
										break;
									}
											
								} while (wait_steep-- > 0 && NTP.getTime() < timePoint + BlockChain.GENERATING_MIN_BLOCK_TIME_MS);
							}

							if (newWinner)
							{
								LOGGER.info("NEW WINER RECEIVED - drop my block");
							} else {
								// MAKING NEW BLOCK
								status = 7;
				
								// GET VALID UNCONFIRMED RECORDS for current TIMESTAMP
								LOGGER.info("GENERATE my BLOCK");
										
								generatedBlock = null;
								try {
									generatedBlock = generateNextBlock(dcSet, acc_winner, solvingBlock,
										getUnconfirmedTransactions(dcSet, timePoint, bchain, max_winned_value));
								} catch (java.lang.OutOfMemoryError e) {
									// TRY CATCH OUTofMemory error - heap space
									LOGGER.error(e.getMessage(), e);									
								}
					
								solvingBlock = null;
									
								if (generatedBlock == null) {
									if (ctrl.isOnStopping()) {
										return;
									}
									
									LOGGER.error("generateNextBlock is NULL... try wait");
									try
									{
										Thread.sleep(10000);
									}
									catch (InterruptedException e) 
									{
									}

									continue;
								} else {
									
									//PASS BLOCK TO CONTROLLER
									///ctrl.newBlockGenerated(block);
									LOGGER.info("bchain.setWaitWinBuffer, size: " + generatedBlock.getTransactionCount());
									if (bchain.setWaitWinBuffer(dcSet, generatedBlock)) {
					
										// need to BROADCAST
										status = 8;
										ctrl.broadcastWinBlock(generatedBlock, null);
										generatedBlock = null;
										status = 0;
									} else {
										generatedBlock = null;
										LOGGER.info("my BLOCK is weak ((...");
									}
								}	
							}
						}
					}
				}
				
				////////////////////////////  FLUSH NEW BLOCK /////////////////////////
				ctrl.checkStatusAndObserve(1);
				if (!ctrl.needUpToDate()) {
	
					// try solve and flush new block from Win Buffer		
					waitWin = bchain.getWaitWinBuffer();
					if (waitWin != null) {
	
						this.solvingReference = null;
	
						// FLUSH WINER to DB MAP
						LOGGER.info("wait to FLUSH WINER to DB MAP " + (flushPoint - NTP.getTime())/1000);
		
						status = 1;
		
						while (flushPoint > NTP.getTime()) {
							try
							{
								Thread.sleep(100);
							}
							catch (InterruptedException e) 
							{
							}
		
							if (ctrl.isOnStopping()) {
								status = -1;
								return;
							}
						}
						
						// FLUSH WINER to DB MAP
						LOGGER.debug("TRY to FLUSH WINER to DB MAP");
		
						try {
							
							status = 2;
							if (!ctrl.flushNewBlockGenerated()) {
								// NEW BLOCK not FLUSHED
								LOGGER.error("NEW BLOCK not FLUSHED");
							}
	
							if (ctrl.isOnStopping()) {
								status = -1;
								return;
							}
								
						} catch (Exception e) {
							// TODO Auto-generated catch block
							if (ctrl.isOnStopping()) {
								status = -1;
								return;
							}
							// if FLUSH out of memory
							bchain.clearWaitWinBuffer();
							LOGGER.error(e.getMessage(), e);
						}
					}
				}
	
				////////////////////////// UPDATE ////////////////////

				timeUpdate = timePoint + BlockChain.GENERATING_MIN_BLOCK_TIME_MS + BlockChain.WIN_BLOCK_BROADCAST_WAIT_MS - NTP.getTime();
				if (timeUpdate > 0)
					continue;

				if(timeUpdate + BlockChain.GENERATING_MIN_BLOCK_TIME_MS + (BlockChain.GENERATING_MIN_BLOCK_TIME_MS>>1) < 0) {
					// MAY BE PAT SITUATION
					//shift_height = -1;

					Tuple3<Integer, Long, Peer> maxPeer = ctrl.getMaxPeerHWeight(-1);
					if (maxPeer != null) {
						peer = maxPeer.c;
					}
					
					if (peer != null && ctrl.getActivePeersCounter() > 3) {
						
						Tuple2<Integer, Long> myHW = ctrl.getBlockChain().getHWeightFull(dcSet);
						if (myHW.a < maxPeer.a || myHW.b < maxPeer.b) {
							
							if (myHW.a > 1) {
	
								LOGGER.error("ctrl.getMaxPeerHWeight(-1) " + peer.getAddress() + " - " + maxPeer);
	 
								SignaturesMessage response;
								try {
									try
									{
										Thread.sleep(1000);
									}
									catch (InterruptedException e) 
									{
									}
	
									byte[] prevSignature = dcSet.getBlockHeightsMap().get(myHW.a - 1);
									response = (SignaturesMessage) peer.getResponse(
											MessageFactory.getInstance().createGetHeadersMessage(prevSignature), 30000);
								} catch (java.lang.ClassCastException e) {
									peer.ban(1, "Cannot retrieve headers - from UPDATE");
									throw new Exception("Failed to communicate with peer (retrieve headers) - response = null - from UPDATE");			
								}
		
								if (response != null) {
									List<byte[]> headers = response.getSignatures();
									byte[] lastSignature = bchain.getLastBlockSignature(dcSet);
									int headersSize = headers.size();
									if (headersSize == 2) {
										if (Arrays.equals(headers.get(1), lastSignature)) {
											ctrl.pingAllPeers(false);
											ctrl.setWeightOfPeer(peer, ctrl.getBlockChain().getHWeightFull(dcSet));
											try
											{
												Thread.sleep(15000);
											}
											catch (InterruptedException e) 
											{
											}
											continue;
										} else {
											ctrl.orphanInPipe(bchain.getLastBlock(dcSet));
										} 
									} else if (headersSize < 2 ) {
										ctrl.orphanInPipe(bchain.getLastBlock(dcSet));										
									}
								}
							 } else {
								 if (ctrl.getActivePeersCounter() < BlockChain.NEED_PEERS_FOR_UPDATE)
									 continue;
							 }
						}
					}
				} else { 
					//shift_height = 0;
				}

				/// CHECK PEERS HIGHER 
				ctrl.checkStatusAndObserve(shift_height);
				//CHECK IF WE ARE NOT UP TO DATE
				if (ctrl.needUpToDate()) {
					
					if (ctrl.isOnStopping()) {
						status = -1;
						return;
					}
					
					status = 3;
					
					this.solvingReference = null;
					bchain.clearWaitWinBuffer();
					
					ctrl.update(shift_height);
					
					status = 0;

					if (ctrl.isOnStopping()) {
						status = -1;
						return;
					}
				}
					
			} catch (Exception e) {
				if (ctrl.isOnStopping()) {
					status = -1;
					return;
				}
				LOGGER.error(e.getMessage(), e);

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
	
	public static Block generateNextBlock(DCSet dcSet, PrivateKeyAccount account,
			Block parentBlock, Tuple2<List<Transaction>, Integer> transactionsItem)
	{
		
		if (transactionsItem == null) {
			return null;
		}
		
		int version = parentBlock.getNextBlockVersion(dcSet);
		byte[] atBytes;
		atBytes = new byte[0];

		//CREATE NEW BLOCK
		
		Block newBlock = new Block(version, parentBlock.getSignature(), account, transactionsItem, atBytes);
			//	//BlockFactory.getInstance().create(version, parentBlock.getSignature(), account, trans, atBytes);
		// SET GENERATING BALANCE here
		newBlock.setCalcGeneratingBalance(dcSet);
		newBlock.sign(account);
		
		return newBlock;

	}
	
	public static Tuple2<List<Transaction>, Integer> getUnconfirmedTransactions(DCSet dcSet, long timestamp, BlockChain bchain, long max_winned_value)
	{
		
		long timrans1 = System.currentTimeMillis();
					
		//CREATE FORK OF GIVEN DATABASE
		DCSet newBlockDb = dcSet.fork();
		Block waitWin;
		
		long start = System.currentTimeMillis();
		List<Transaction> orderedTransactions = new ArrayList<Transaction>(dcSet.getTransactionMap().getSubSet(timestamp));
		//List<Transaction> orderedTransactions = new ArrayList<Transaction>(dcSet.getTransactionMap().getValuesAll());
		long tickets = System.currentTimeMillis() - start;
		int txCount = orderedTransactions.size();
		long ticketsCount = System.currentTimeMillis() - tickets;
		LOGGER.debug("=== time: " + tickets + "ms for SIZE: " + txCount + " .size() ms: " + ticketsCount);

		// TODO make SORT by FEE to!
		// toBYTE / FEE + TIMESTAMP !!
		////Collections.sort(orderedTransactions, new TransactionFeeComparator());
		// sort by TIMESTAMP
		Collections.sort(orderedTransactions, new TransactionTimestampComparator());
		long tickets2 = System.currentTimeMillis() - start - tickets;
		LOGGER.error("=== sort time " + tickets2);
		start = System.currentTimeMillis();
		
		//Collections.sort(orderedTransactions, Collections.reverseOrder());
		
		List<Transaction> transactionsList = new ArrayList<Transaction>();

	//	boolean transactionProcessed;
		long totalBytes = 0;
		int counter = 0;

		//do
		//{
		//	transactionProcessed = false;
						
			for(Transaction transaction: orderedTransactions)
			{
								
				if (ctrl.isOnStopping()) {
					return null;
				}

				if (bchain != null ) {
					waitWin = bchain.getWaitWinBuffer();
					if (waitWin != null && waitWin.calcWinValue(dcSet) > max_winned_value) {
						return null;
					}
				}

				try{

					//CHECK TRANSACTION TIMESTAMP AND DEADLINE
					if(transaction.getTimestamp() > timestamp || transaction.getDeadline() < timestamp || !transaction.isSignatureValid()) {
						// INVALID TRANSACTION
						// REMOVE FROM LIST
						//transactionProcessed = true;
						//orderedTransactions.remove(transaction);
						continue;
					}
						
					transaction.setDC(newBlockDb, false);
					
					if (transaction.isValid(newBlockDb, null) != Transaction.VALIDATE_OK) {
						// INVALID TRANSACTION
						// REMOVE FROM LIST
						//transactionProcessed = true;
						//orderedTransactions.remove(transaction);
						continue;
					}
														
					//CHECK IF ENOUGH ROOM
					totalBytes += transaction.getDataLength(false);

					if(totalBytes > MAX_BLOCK_SIZE_BYTE
							|| ++counter> MAX_BLOCK_SIZE) {
						counter--;
						break;
					}
					
					////ADD INTO LIST
					transactionsList.add(transaction);
								
					//REMOVE FROM LIST
					//orderedTransactions.remove(transaction);
								
					//PROCESS IN NEWBLOCKDB
					transaction.process(newBlockDb, null, false);
								
					//TRANSACTION PROCESSES
					//transactionProcessed = true;
										
					// GO TO NEXT TRANSACTION
					continue;
						
				} catch (Exception e) {
					
					if (ctrl.isOnStopping()) {
						return null;
					}

               //     transactionProcessed = true;

                    LOGGER.error(e.getMessage(), e);
                    //REMOVE FROM LIST

                    break;                    
				}
				
			}
		//}
		//while(counter < MAX_BLOCK_SIZE && totalBytes < MAX_BLOCK_SIZE_BYTE && transactionProcessed == true);
		orderedTransactions = null;
		waitWin = null;
		newBlockDb = null;

		LOGGER.debug("get Unconfirmed Transactions = " + (System.currentTimeMillis() - start) +"ms for trans: " + counter );
		start = System.currentTimeMillis();

		// sort by TIMESTAMP
		Collections.sort(transactionsList,  new TransactionTimestampComparator());

		LOGGER.debug("sort 2 Unconfirmed Transactions =" + (System.currentTimeMillis() - start) +"milsec for trans: " + counter );
		
		return new Tuple2<List<Transaction>, Integer>(transactionsList, counter);
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
		
		if(!Settings.getInstance().isForgingEnabled() || getKnownAccounts().isEmpty()) {
			setForgingStatus(ForgingStatus.FORGING_DISABLED);
			return;
		}
		
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
		if (ctrl.isReadyForging())
			setForgingStatus(ForgingStatus.FORGING);
		else
			setForgingStatus(ForgingStatus.FORGING_WAIT);
	}
	
}
