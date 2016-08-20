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
		return this.genesisTimestamp + height * Block.GENERATING_MIN_BLOCK_TIME;
	}
	public long getTimestamp() {
		return this.genesisTimestamp + getHeight() * Block.GENERATING_MIN_BLOCK_TIME;
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

			LOGGER.error("setWaitWinBuffer - WIN value: "
					+ block.calcWinValue(dbSet));

			return true;
		}
		
		return false;
	}
	
	// 
	public int getHeight() {
		
		//GET LAST BLOCK
		byte[] lastBlockSignature = dbSet.getBlockMap().getLastBlockSignature();
		return dbSet.getHeightMap().getHeight(lastBlockSignature);
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
			weight = this.waitWinBuffer.calcWinValue(dbSet);
		} else {
			height = 0;
			weight = 0l;				
		}
		
		if (lastBlockSignature == null) {
			height++;
		} else {
			height += dbSet.getHeightMap().getHeight(lastBlockSignature);
			weight += dbSet.getHeightMap().getFullWeight();
		}
		
		return  new Tuple2<Integer, Long>(height, weight);
		
	}
	
	public long getFullWeight() {
		
		return dbSet.getHeightMap().getFullWeight();
	}

	public int getCheckPoint() {
		
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
}
