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

	//public static final int START_LEVEL = 1;
	
	public static final int TESTNET_PORT = 9035;
	public static final int MAINNET_PORT = 9036;
	public static final int DEFAULT_WEB_PORT = 9037;
	public static final int DEFAULT_RPC_PORT = 9038;

	//
	public static final int MAX_ORPHAN = 30; // max orphan blocks in chain
	public static final int TARGET_COUNT = 100;
	public static final int BASE_TARGET = 1024 * 3;
	public static final int REPEAT_WIN = 6; // GENESIS START TOP ACCOUNTS
	
	// RIGHTs 
	public static final int GENESIS_ERA_TOTAL = 9999999;
	public static final int GENERAL_ERM_BALANCE = GENESIS_ERA_TOTAL / 100;
	public static final int MAJOR_ERM_BALANCE = 33000;
	public static final int MINOR_ERM_BALANCE = 1000;
	public static final int MIN_GENERATING_BALANCE = 100;
	public static final BigDecimal MIN_GENERATING_BALANCE_BD = new BigDecimal(MIN_GENERATING_BALANCE);
	//public static final int GENERATING_RETARGET = 10;
	public static final int GENERATING_MIN_BLOCK_TIME = 288; // 300 PER DAY
	public static final int BLOCKS_PER_DAY = 24 * 60 * 60 / GENERATING_MIN_BLOCK_TIME; // 300 PER DAY
	//public static final int GENERATING_MAX_BLOCK_TIME = 1000;
	public static final int MAX_BLOCK_BYTES = 2<<21; //4 * 1048576;
	public static final int MAX_REC_DATA_BYTES = MAX_BLOCK_BYTES>>1;
	public static final int GENESIS_WIN_VALUE = 1000;
	public static final String[] GENESIS_ADMINS = new String[]{"78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5",
			"7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC"};

	// CHAIN
	public static final int CONFIRMS_HARD = 3; // for reference by signature 
	// MAX orphan CHAIN
	public static final int CONFIRMS_TRUE = MAX_ORPHAN; // for reference by ITEM_KEY

	//TESTNET 
												//   1486444444444l
	public static final long DEFAULT_MAINNET_STAMP = 1487710780444l;

	//public static final int FEE_MIN_BYTES = 200;
	public static final int FEE_PER_BYTE = 64;
	public static final int FEE_SCALE = 8;
	public static final BigDecimal FEE_RATE = BigDecimal.valueOf(1, FEE_SCALE);
	public static final BigDecimal MIN_FEE_IN_BLOCK = BigDecimal.valueOf(FEE_PER_BYTE * 8 * 128, FEE_SCALE);
	public static final float FEE_POW_BASE = (float)1.5;
	public static final int FEE_POW_MAX = 6;
	public static final int ISSUE_MULT_FEE = 1<<10;
	public static final int ISSUE_ASSET_MULT_FEE = 1<<8;
	//
	public static final int FEE_INVITED_DEEP = 4; // levels for deep
	public static final int FEE_INVITED_SHIFT = 5; // 2^5 = 64 - total FEE -> fee for Forger and fee for Inviter
	public static final int FEE_INVITED_SHIFT_IN_LEVEL = 3;
	public static final int FEE_FOR_ANONIMOUSE = 33;

	// issue PORSON
	public static final BigDecimal PERSON_MIN_ERM_BALANCE = BigDecimal.valueOf(10000000).setScale(8);

	// SERTIFY
	// need RIGHTS for non PERSON account
	public static final BigDecimal PSERT_GENERAL_ERM_BALANCE = BigDecimal.valueOf(1000000).setScale(8);
	// need RIGHTS for PERSON account
	public static final BigDecimal PSERT_MIN_ERM_BALANCE = BigDecimal.valueOf(1000).setScale(8);
	// GIFTS for R_SertifyPubKeys
	public static final int GIFTED_COMPU_AMOUNT = 256 * FEE_PER_BYTE;

	static Logger LOGGER = Logger.getLogger(BlockChain.class.getName());
	private GenesisBlock genesisBlock;
	private long genesisTimestamp;

	
	private Block waitWinBuffer;
	private int checkPoint = 1;
	//private int target = 0;

	
	//private DBSet dbSet;
	
	// dbSet_in = db() - for test
	public BlockChain(DBSet dbSet_in)
	{	
		//CREATE GENESIS BLOCK
		genesisBlock = new GenesisBlock();
		genesisTimestamp = genesisBlock.getTimestamp(null);
		
		DBSet dbSet = dbSet_in;
		if (dbSet == null) {
			dbSet = DBSet.getInstance();
		}

		if(Settings.getInstance().isTestnet()) {
			LOGGER.info( ((GenesisBlock)genesisBlock).getTestNetInfo() );
		}
		
		if(	!dbSet.getBlockMap().contains(genesisBlock.getSignature()) )
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
	public long getTimestamp(DBSet dbSet) {
		return this.genesisTimestamp + (long)getHeight(dbSet) * (long)Block.GENERATING_MIN_BLOCK_TIME;
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
	public boolean setWaitWinBuffer(DBSet dbSet, Block block) {
				
		LOGGER.info("try set new winBlock: " + block.toString(dbSet));
		
		if (this.waitWinBuffer == null
				|| block.calcWinValue(dbSet) > this.waitWinBuffer.calcWinValue(dbSet)) {

			this.waitWinBuffer = block;

			LOGGER.info("new winBlock setted!");
			return true;
		}
		
		LOGGER.info("new winBlock ignored!");
		return false;
	}
	
	// 
	public int getHeight(DBSet dbSet) {
		
		//GET LAST BLOCK
		byte[] lastBlockSignature = dbSet.getBlockMap().getLastBlockSignature();
		return dbSet.getBlockSignsMap().getHeight(lastBlockSignature);
	}

	public Tuple2<Integer, Long> getHWeight(DBSet dbSet, boolean withWinBuffer) {
		
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
	
	public long getFullWeight(DBSet dbSet) {
		
		return dbSet.getBlockSignsMap().getFullWeight();
	}

	public int getCheckPoint(DBSet dbSet) {
		
		int checkPoint = getHeight(dbSet) - BlockChain.MAX_ORPHAN; 
		if ( checkPoint > this.checkPoint)
			this.checkPoint = checkPoint;
		
		return this.checkPoint;
	}
	public void setCheckPoint(int checkPoint) {
		
		if (checkPoint > 1)
			this.checkPoint = checkPoint;
	}
	
	public static int getNetworkPort() {
		if(Settings.getInstance().isTestnet()) {
			return BlockChain.TESTNET_PORT;
		} else {
			return BlockChain.MAINNET_PORT;
		}
	}


	public List<byte[]> getSignatures(DBSet dbSet, byte[] parent) {
		
		//LOGGER.debug("getSignatures for ->" + Base58.encode(parent));
		
		List<byte[]> headers = new ArrayList<byte[]>();
		
		//CHECK IF BLOCK EXISTS
		if(dbSet.getBlockMap().contains(parent))
		{
			Block childBlock = dbSet.getBlockMap().get(parent).getChild(dbSet);
			
			int counter = 0;
			while(childBlock != null && counter < MAX_ORPHAN)
			{
				headers.add(childBlock.getSignature());
				
				childBlock = childBlock.getChild(dbSet);
				
				counter ++;
			}
			//LOGGER.debug("get size " + counter);
		} else {
			//LOGGER.debug("*** getSignatures NOT FOUND !");
			
		}
		
		return headers;		
	}

	public Block getBlock(DBSet dbSet, byte[] header) {

		return dbSet.getBlockMap().get(header);
	}
	public Block getBlock(DBSet dbSet, int height) {

		byte[] signature = dbSet.getBlockHeightsMap().get((long)height);
		return dbSet.getBlockMap().get(signature);
	}

	public int isNewBlockValid(DBSet dbSet, Block block) {
		
		//CHECK IF NOT GENESIS
		if(block instanceof GenesisBlock)
		{
			LOGGER.debug("core.BlockChain.isNewBlockValid ERROR -> as GenesisBlock");
			return 1;
		}
		
		//CHECK IF SIGNATURE IS VALID
		if(!block.isSignatureValid())
		{
			LOGGER.debug("core.BlockChain.isNewBlockValid ERROR -> signature");
			return 2;
		}
		
		//CHECK IF WE KNOW THIS BLOCK
		if(dbSet.getBlockMap().contains(block.getSignature()))
		{
			LOGGER.debug("core.BlockChain.isNewBlockValid ERROR -> already in DB #" + block.getHeight(dbSet));
			return 3;
		}

		byte[] lastSignature = dbSet.getBlockMap().getLastBlockSignature();
		if(!Arrays.equals(lastSignature, block.getReference())) {
			LOGGER.debug("core.BlockChain.isNewBlockValid ERROR -> reference NOT to last block");
			return 4;
		}
		
		return 0;
	}
	
	public Pair<Block, List<Transaction>> scanTransactions(DBSet dbSet, Block block, int blockLimit, int transactionLimit, int type, int service, Account account) 
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
			block = this.getLastBlock(dbSet);
		}
		else
		{
			block = block.getParent(dbSet);
		}
		
		//RETURN PARENT BLOCK AS WE GET CHILD RIGHT BEFORE END OF WHILE
		return new Pair<Block, List<Transaction>>(block, transactions);
	}
	
	public Block getLastBlock(DBSet dbSet) 
	{	
		return dbSet.getBlockMap().getLastBlock();
	}
	public byte[] getLastBlockSignature(DBSet dbSet) 
	{	
		return dbSet.getBlockMap().getLastBlockSignature();
	}

	// get last blocks for target
	public List<Block> getLastBlocksForTarget(DBSet dbSet) 
	{	

		Block last = dbSet.getBlockMap().getLastBlock();
		
		/*
		if (this.lastBlocksForTarget != null
				&& Arrays.equals(this.lastBlocksForTarget.get(0).getSignature(), last.getSignature())) {
			return this.lastBlocksForTarget;
		}
		*/
		
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
	
	// ignore BIG win_values
	public static long getTarget(DBSet dbSet, Block block)
	{
		
		/*
		int height = block.getParentHeight(dbSet);
		if (block.getTargetValue() > 0)
			return block.getTargetValue();
		 */
		
		long min_value = 0;
		long win_value = 0;
		Block parent = block.getParent(dbSet);
		int i = 0;
		long value = 0;
		
		while (parent != null && parent.getVersion() > 0 && i < BlockChain.TARGET_COUNT)
		{
			i++;
			value = parent.calcWinValue(dbSet);
			if (min_value==0
					|| min_value > value) {
				min_value = value;
			}
			
			if (min_value + (min_value<<1) < value)
				value = min_value + (min_value<<1);

			parent = parent.getParent(dbSet);
		}
		
		if (i == 0) {
			return block.calcWinValue(dbSet);
		}		

		int height = block.getParentHeight(dbSet);
		min_value = min_value<<1;

		parent = block.getParent(dbSet);
		i = 0;
		while (parent != null && parent.getVersion() > 0 && i < BlockChain.TARGET_COUNT)
		{
			i++;
			value = parent.calcWinValue(dbSet);
			if (height > TARGET_COUNT && min_value < value)
				value = min_value;
			win_value += value;
			
			parent = parent.getParent(dbSet);
		}

		long target = win_value / i;
		
		return target;
	}

	// calc Target by last blocks in chain
	public long getTarget(DBSet dbSet)
	{	
		return getTarget(dbSet, this.getLastBlock(dbSet));
	}

	// GET MIN TARGET
	// TODO GENESIS_CHAIN
	public static int getMinTarget(int height) {
		int base;
		if ( height < BlockChain.REPEAT_WIN)
			// FOR not repeated WINS - not need check BASE_TARGET
			base = BlockChain.BASE_TARGET>>1;
		else if ( height < BlockChain.TARGET_COUNT)
			base = (BlockChain.BASE_TARGET>>1) + (BlockChain.BASE_TARGET>>2);
		else
			base = (BlockChain.BASE_TARGET>>1) + (BlockChain.BASE_TARGET>>3);

		return base;

	}
	
}
