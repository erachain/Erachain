package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
 import org.apache.log4j.Logger;

import controller.Controller;
import core.account.Account;
import core.block.Block;
import core.block.GenesisBlock;
import core.item.assets.AssetCls;
import core.transaction.ArbitraryTransaction;
import core.transaction.GenesisIssueAssetTransaction;
import core.transaction.Transaction;
import database.DBSet;
import ntp.NTP;
import settings.Settings;
import utils.Pair;

public class BlockChain
{
	public static final int MAX_SIGNATURES = Settings.BLOCK_MAX_SIGNATURES;
	
	static Logger LOGGER = Logger.getLogger(BlockChain.class.getName());
	
	public BlockChain()
	{	
		//CREATE GENESIS BLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		DBSet db = DBSet.getInstance();

		if(Settings.getInstance().isTestnet()) {
			LOGGER.info( ((GenesisBlock)genesisBlock).getTestNetInfo() );
		}
		
		if(	!db.getBlockMap().contains(genesisBlock.getSignature())
			||
			db.getBlockMap().get(genesisBlock.getSignature()).getTimestamp() != genesisBlock.getTimestamp())
		// process genesis block
		{
			if(db.getBlockMap().getLastBlockSignature() != null)
			{
				LOGGER.info("reCreate Database...");	
		
	        	try {
	        		db.close();
					Controller.getInstance().reCreateDB(false);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(),e);
				}
			}

        	//PROCESS
        	genesisBlock.process();

        }
	}
	
	public int getHeight() {
		
		//GET LAST BLOCK
		byte[] lastBlockSignature = DBSet.getInstance().getBlockMap().getLastBlockSignature();
		
		//RETURN HEIGHT
		return DBSet.getInstance().getHeightMap().get(lastBlockSignature);
	}

	public List<byte[]> getSignatures(byte[] parent) {
		
		List<byte[]> headers = new ArrayList<byte[]>();
		
		//CHECK IF BLOCK EXISTS
		if(DBSet.getInstance().getBlockMap().contains(parent))
		{
			Block parentBlock = DBSet.getInstance().getBlockMap().get(parent).getChild();
			
			int counter = 0;
			while(parentBlock != null && counter < MAX_SIGNATURES)
			{
				headers.add(parentBlock.getSignature());
				
				parentBlock = parentBlock.getChild();
				
				counter ++;
			}
		}
		
		return headers;		
	}

	public Block getBlock(byte[] header) {

		return DBSet.getInstance().getBlockMap().get(header);
	}

	public boolean isNewBlockValid(Block block) {
		
		//CHECK IF NOT GENESIS
		if(block instanceof GenesisBlock)
		{
			return false;
		}
		
		//CHECK IF SIGNATURE IS VALID
		if(!block.isSignatureValid())
		{
			return false;
		}
		
		//CHECK IF WE KNOW REFERENCE
		if(!DBSet.getInstance().getBlockMap().contains(block.getReference()))
		{
			return false;
		}
		
		//CHECK IF REFERENCE IS LASTBLOCK
		if(!Arrays.equals(DBSet.getInstance().getBlockMap().getLastBlockSignature(), block.getReference()))
		{
			return false;
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
			block = block.getChild();
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
			block = block.getParent();
		}
		
		//RETURN PARENT BLOCK AS WE GET CHILD RIGHT BEFORE END OF WHILE
		return new Pair<Block, List<Transaction>>(block, transactions);
	}
	
	public Block getLastBlock() 
	{	
		return DBSet.getInstance().getBlockMap().getLastBlock();
	}
}
