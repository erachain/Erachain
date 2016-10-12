package core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.account.Account;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Base58;
import core.transaction.ArbitraryTransaction;
import core.transaction.Transaction;
import database.DBSet;
import ntp.NTP;
import settings.Settings;
import utils.Pair;

public class BlockChain
{

	public static final int START_LEVEL = 1;

	public static final int MAX_SIGNATURES = Settings.BLOCK_MAX_SIGNATURES;
	public static final int TARGET_COUNT = 100;
	public static final int REPEAT_WIN = 3;
	
	public static final int GENESIS_WIN_VALUE = 1000;

	public static final BigDecimal MIN_FEE_IN_BLOCK = new BigDecimal("0.00010000");
	public static final int FEE_PER_BYTE = 32;
	public static final int FEE_SCALE = 8;
	public static final BigDecimal FEE_RATE = BigDecimal.valueOf(1, FEE_SCALE);
	public static final float FEE_POW_BASE = (float)1.5;
	public static final int FEE_POW_MAX = 6;
	//
	public static final int FEE_INVITED_DEEP = 15; // levels foe deep
	public static final int FEE_INVITED_SHIFT = 3; // total FEE -> fee for Forger and fee for Inviter
	public static final int FEE_INVITED_SHIFT_IN_LEVEL = 2;

	// GIFTS for R_SertifyPubKeys
	public static final BigDecimal GIFTED_ERMO_AMOUNT = new BigDecimal(1000);
	public static final int GIFTED_COMPU_AMOUNT = 90000 * FEE_PER_BYTE;
	//public static final BigDecimal GIFTED_COMPU_AMOUNT = new BigDecimal("0.00010000");

	static Logger LOGGER = Logger.getLogger(BlockChain.class.getName());
	private GenesisBlock genesisBlock;
	private long genesisTimestamp;
	private List<Block> lastBlocksForTarget;

	
	private Block waitWinBuffer;
	private int checkPoint = 1;

	
	private DBSet dbSet;
	
	// dbSet_in = db() - for test
	public BlockChain(DBSet dbSet_in)
	{	
		//CREATE GENESIS BLOCK
		genesisBlock = new GenesisBlock();
		genesisTimestamp = genesisBlock.getTimestamp(null);
		
		dbSet = dbSet_in;
		if (dbSet_in == null) {
			dbSet = DBSet.getInstance();
		}

		if(Settings.getInstance().isTestnet()) {
			LOGGER.info( ((GenesisBlock)genesisBlock).getTestNetInfo() );
		}
		
		if(	!dbSet.getBlockMap().contains(genesisBlock.getSignature())
			// not need now || dbSet.getBlockMap().get(genesisBlock.getSignature()).getTimestamp(dbSet) != genesisBlock.getTimestamp(dbSet)
			)
		// process genesis block
		{
			if(dbSet_in == null && dbSet.getBlockMap().getLastBlockSignature() != null)
			{
				LOGGER.info("reCreate Database...");	
		
	        	try {
	        		dbSet.close();
					Controller.getInstance().reCreateDB(false);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(),e);
				}
			}

        	//PROCESS
        	genesisBlock.process(dbSet);

        }
	}

	public GenesisBlock getGenesisBlock() {
		return this.genesisBlock;
	}
	public long getTimestamp(int height) {
		return this.genesisTimestamp + (long)height * (long)Block.GENERATING_MIN_BLOCK_TIME;
	}
	public long getTimestamp() {
		return this.genesisTimestamp + (long)getHeight() * (long)Block.GENERATING_MIN_BLOCK_TIME;
	}

	// BUFFER of BLOCK for WIN solving
	public Block getWaitWinBuffer() {
		return this.waitWinBuffer;
	}
	public void clearWaitWinBuffer() {
		this.waitWinBuffer = null;
	}
	public Block popWaitWinBuffer() {
		Block block = this.waitWinBuffer; 
		this.waitWinBuffer = null;
		return block;
	}
	
	// SOLVE WON BLOCK
	// 0 - unchanged;
	// 1 - changed, need broadcasting;
	public boolean setWaitWinBuffer(Block block) {
				
		if (this.waitWinBuffer == null
				|| block.calcWinValue(dbSet) > this.waitWinBuffer.calcWinValue(dbSet)) {

			this.waitWinBuffer = block;

			LOGGER.info("setWaitWinBuffer - WIN value: "
					+ block.calcWinValue(dbSet));

			return true;
		}
		
		return false;
	}
	
	// 
	public int getHeight() {
		
		//GET LAST BLOCK
		byte[] lastBlockSignature = dbSet.getBlockMap().getLastBlockSignature();
		return dbSet.getBlockSignsMap().getHeight(lastBlockSignature);
	}

	public Tuple2<Integer, Long> getHWeight(boolean withWinBuffer) {
		
		//GET LAST BLOCK
		byte[] lastBlockSignature = dbSet.getBlockMap().getLastBlockSignature();
		// test String b58 = Base58.encode(lastBlockSignature);
		
		int height;
		long weight;
		if (withWinBuffer && this.waitWinBuffer != null) {
			// with WIN BUFFER BLOCK
			height = 1;
			weight = this.waitWinBuffer.calcWinValueTargeted(dbSet);
		} else {
			height = 0;
			weight = 0l;				
		}
		
		if (lastBlockSignature == null) {
			height++;
		} else {
			height += dbSet.getBlockSignsMap().getHeight(lastBlockSignature);
			weight += dbSet.getBlockSignsMap().getFullWeight();
		}
		
		return  new Tuple2<Integer, Long>(height, weight);
		
	}
	
	public long getFullWeight() {
		
		return dbSet.getBlockSignsMap().getFullWeight();
	}

	public int getCheckPoint() {
		
		int checkPoint = getHeight() - BlockChain.MAX_SIGNATURES; 
		if ( checkPoint > this.checkPoint)
			this.checkPoint = checkPoint;
		
		return this.checkPoint;
	}
	public void setCheckPoint(int checkPoint) {
		
		if (checkPoint > 1)
			this.checkPoint = checkPoint;
	}

	public List<byte[]> getSignatures(byte[] parent) {
		
		LOGGER.debug("getSignatures for ->" + Base58.encode(parent));
		
		List<byte[]> headers = new ArrayList<byte[]>();
		
		//CHECK IF BLOCK EXISTS
		if(dbSet.getBlockMap().contains(parent))
		{
			Block childBlock = dbSet.getBlockMap().get(parent).getChild(dbSet);
			
			int counter = 0;
			while(childBlock != null && counter < MAX_SIGNATURES)
			{
				headers.add(childBlock.getSignature());
				
				childBlock = childBlock.getChild(dbSet);
				
				counter ++;
			}
			//LOGGER.debug("get size " + counter);
		} else {
			LOGGER.debug("*** getSignatures NOT FOUND !");
			
		}
		
		return headers;		
	}

	public Block getBlock(byte[] header) {

		return dbSet.getBlockMap().get(header);
	}
	public Block getBlock(int height) {

		byte[] signature = dbSet.getBlockHeightsMap().get((long)height);
		return dbSet.getBlockMap().get(signature);
	}

	public int isNewBlockValid(Block block) {
		
		//CHECK IF NOT GENESIS
		if(block instanceof GenesisBlock)
		{
			LOGGER.error("core.BlockChain.isNewBlockValid ERROR -> as GenesisBlock");
			return 1;
		}
		
		//CHECK IF SIGNATURE IS VALID
		if(!block.isSignatureValid())
		{
			LOGGER.error("core.BlockChain.isNewBlockValid ERROR -> signature");
			return 2;
		}
		
		//CHECK IF WE KNOW THIS BLOCK
		if(dbSet.getBlockMap().contains(block.getSignature()))
		{
			LOGGER.error("core.BlockChain.isNewBlockValid ERROR -> already in DB #" + block.getHeight(dbSet));
			return 3;
		}

		Block lastBlock = this.getLastBlock();
		if(!Arrays.equals(lastBlock.getSignature(), block.getReference())) {
			LOGGER.error("core.BlockChain.isNewBlockValid ERROR -> reference NOT to last block");
			return 4;
		}
		
		return 0;
	}
	
	public Pair<Block, List<Transaction>> scanTransactions(Block block, int blockLimit, int transactionLimit, int type, int service, Account account) 
	{	
		//CREATE LIST
		List<Transaction> transactions = new ArrayList<Transaction>();
		
		//IF NO BLOCK START FROM GENESIS
		if(block == null)
		{
			block = new GenesisBlock();
		}
		
		//START FROM BLOCK
		int scannedBlocks = 0;
		do
		{		
			//FOR ALL TRANSACTIONS IN BLOCK
			for(Transaction transaction: block.getTransactions())
			{
				//CHECK IF ACCOUNT INVOLVED
				if(account != null && !transaction.isInvolved(account))
				{
					continue;
				}
				
				//CHECK IF TYPE OKE
				if(type != -1 && transaction.getType() != type)
				{
					continue;
				}
				
				//CHECK IF SERVICE OKE
				if(service != -1 && transaction.getType() == Transaction.ARBITRARY_TRANSACTION)
				{
					ArbitraryTransaction arbitraryTransaction = (ArbitraryTransaction) transaction;
					
					if(arbitraryTransaction.getService() != service)
					{
						continue;
					}
				}
				
				//ADD TO LIST
				transactions.add(transaction);
			}
			
			//SET BLOCK TO CHILD
			block = block.getChild(dbSet);
			scannedBlocks++;
		}
		//WHILE BLOCKS EXIST && NOT REACHED TRANSACTIONLIMIT && NOT REACHED BLOCK LIMIT
		while(block != null && (transactions.size() < transactionLimit || transactionLimit == -1) && (scannedBlocks < blockLimit || blockLimit == -1)); 
		
		//CHECK IF WE REACHED THE END
		if(block == null)
		{
			block = this.getLastBlock();
		}
		else
		{
			block = block.getParent(dbSet);
		}
		
		//RETURN PARENT BLOCK AS WE GET CHILD RIGHT BEFORE END OF WHILE
		return new Pair<Block, List<Transaction>>(block, transactions);
	}
	
	public Block getLastBlock() 
	{	
		return dbSet.getBlockMap().getLastBlock();
	}

	// get last blocks for target
	public List<Block> getLastBlocksForTarget() 
	{	

		Block last = dbSet.getBlockMap().getLastBlock();
		
		if (this.lastBlocksForTarget != null
				&& Arrays.equals(this.lastBlocksForTarget.get(0).getSignature(), last.getSignature())) {
			return this.lastBlocksForTarget;
		}
		
		List<Block> list =  new ArrayList<Block>();

		if (last == null || last.getVersion() == 0) {
			return list;
		}

		for (int i=0; i < TARGET_COUNT && last.getVersion() > 0; i++) {
			list.add(last);
			last = last.getParent(dbSet);
		}
		
		return list;
	}

	
	// calc Target by last blocks in chain
	// ignore BIG win_values
	public long getTarget() 
	{	
		
		long target = 0;
		
		List<Block> lastBlocks = this.getLastBlocksForTarget();
		if (lastBlocks == null || lastBlocks.isEmpty())
			return 0l;
		
		long win_value;
		int size = 0;
		for (Block block: lastBlocks)
		{
			win_value = block.calcWinValue(dbSet);
			if (size > 20 && win_value > target<<2) {
				// NOT USE BIG values
				win_value = target<<2;
			}
			target += win_value;
			size++;
		}
		return target /= size;
	}

	public boolean isGoodWinForTarget(int height, long winned_value, long target) { 
		// not use small values
		if (height < 100) {}
		else if (height < 10000) {
			if ((target>>2) > winned_value)
				return false;
		} else {
			if ((target>>1) > winned_value)
				return false;
		}
		
		return true;
	}
	
}
