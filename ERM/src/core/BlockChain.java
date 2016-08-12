package core;

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
	public static final int MAX_SIGNATURES = Settings.BLOCK_MAX_SIGNATURES;
	
	static Logger LOGGER = Logger.getLogger(BlockChain.class.getName());
	private GenesisBlock genesisBlock;
	private long genesisTimestamp;
	
	private Block waitWinBuffer;
	
	// dbSet_in = db() - for test
	public BlockChain(DBSet dbSet_in)
	{	
		//CREATE GENESIS BLOCK
		genesisBlock = new GenesisBlock();
		genesisTimestamp = genesisBlock.getTimestamp(null);
		
		DBSet dbSet = dbSet_in;
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
		return this.genesisTimestamp + height * Block.GENERATING_MIN_BLOCK_TIME;
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
	// 2 - broadcasting + need update BlockMap;  
	public Tuple2<Integer, Block> setWaitWinBuffer(DBSet dbSet, Block block) {
		
		if (this.waitWinBuffer != null)
			if (Arrays.equals(block.getReference(), this.waitWinBuffer.getSignature())) {
				// NEW BLOCK INCOMED - PULL CURRENT BLOCK in MAP
				// TODO
				this.waitWinBuffer = block;
				return new Tuple2<Integer, Block>(2, block);
			}
		
		
		if (this.waitWinBuffer == null
				|| block.getWinValue(dbSet) > this.waitWinBuffer.getWinValue(dbSet)) {

			this.waitWinBuffer = block;
			return new Tuple2<Integer, Block>(1, block);
		}
		
		return new Tuple2<Integer, Block>(0, this.waitWinBuffer);
	}
	
	public Tuple2<Integer, Long> getHWeight(DBSet dbSet) {
		
		//GET LAST BLOCK
		byte[] lastBlockSignature = dbSet.getBlockMap().getLastBlockSignature();
		String b58 = Base58.encode(lastBlockSignature);
		if (lastBlockSignature == null)
			return new Tuple2<Integer, Long>(1, 0L);
		
		return new Tuple2<Integer, Long>(dbSet.getHeightMap().getHeight(lastBlockSignature),
				dbSet.getHeightMap().getFullWeight());
	}
	public long getFullWeight(DBSet dbSet) {
		
		return dbSet.getHeightMap().getFullWeight();
	}

	public List<byte[]> getSignatures(DBSet dbSet, byte[] parent) {
		
		//LOGGER.debug("getSignatures for ->" + Base58.encode(parent));
		
		List<byte[]> headers = new ArrayList<byte[]>();
		
		//CHECK IF BLOCK EXISTS
		if(dbSet.getBlockMap().contains(parent))
		{
			Block parentBlock = dbSet.getBlockMap().get(parent).getChild(dbSet);
			
			int counter = 0;
			while(parentBlock != null && counter < MAX_SIGNATURES)
			{
				headers.add(parentBlock.getSignature());
				
				parentBlock = parentBlock.getChild(dbSet);
				
				counter ++;
			}
			//LOGGER.debug("get size " + counter);
		} else {
			LOGGER.debug("*** getSignatures NOT FOUND !");
			
		}
		
		return headers;		
	}

	public Block getBlock(DBSet dbSet, byte[] header) {

		return dbSet.getBlockMap().get(header);
	}

	public boolean isNewBlockValid(DBSet dbSet, Block block) {
		
		//CHECK IF NOT GENESIS
		if(block instanceof GenesisBlock)
		{
			LOGGER.error("core.BlockChain.isNewBlockValid ERROR -> as GenesisBlock");
			return false;
		}
		
		//CHECK IF SIGNATURE IS VALID
		if(!block.isSignatureValid())
		{
			LOGGER.error("core.BlockChain.isNewBlockValid ERROR -> signature");
			return false;
		}
		
		//CHECK IF WE KNOW THIS BLOCK
		if(dbSet.getBlockMap().contains(block.getSignature()))
		{
			LOGGER.error("core.BlockChain.isNewBlockValid ERROR -> already in DB");
			return false;
		}
		
		//CHECK IF REFERENCES EQUAL
		// AND blocks in current TIME
		Block lastBlock = this.getLastBlock(dbSet);
		if(Arrays.equals(lastBlock.getReference(), block.getReference())
				//&&  NTP.getTime() - 2 * Block.GENERATING_MIN_BLOCK_TIME < lastBlock.getTimestamp(dbSet)
				)
		{
			// TRY SELECT BEST BLOCK from NEW from PEER and LAST in DB
			if (false && lastBlock.getWinValue(dbSet) < block.getWinValue(dbSet)
					&& !Arrays.equals(lastBlock.getSignature(), block.getSignature())
					&& !Arrays.equals(this.genesisBlock.getSignature(), block.getSignature())
					) {
				//// TODO тут нельзя откаты делать - несинхранизированые записи и база с двойными записями получается
				//dbSet.getBlockMap().setProcessing(true);
				lastBlock.orphan(dbSet);	
				//dbSet.getBlockMap().setProcessing(false);
				LOGGER.error("core.BlockChain.isNewBlockValid *** knownBlock orphan and newBlock taken!");
			} else {
				LOGGER.error("core.BlockChain.isNewBlockValid ERROR -> last ref same");
				return false;
			}
		}
		
		return true;
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
		
		DBSet dbSet = DBSet.getInstance();
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
	
	public Block getLastBlock(DBSet db) 
	{	
		return db.getBlockMap().getLastBlock();
	}
}
