package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
// import org.apache.log4j.Logger;
import org.apache.log4j.Logger;

import network.Peer;
import network.message.BlockMessage;
import network.message.Message;
import network.message.MessageFactory;
import network.message.SignaturesMessage;
import network.message.TransactionMessage;
import at.AT;
import at.AT_API_Platform_Impl;
import at.AT_Constants;
import core.block.Block;
import core.crypto.Base58;
import core.transaction.Transaction;

import com.google.common.primitives.Bytes;

import database.DBSet;

public class Synchronizer
{
	private static final Logger LOGGER = Logger.getLogger(Synchronizer.class);
	
	private boolean run = true;
	
	public Synchronizer()
	{
		this.run = true;
	}
	
	private void checkNewBlocks(DBSet fork, Block lastCommonBlock, List<Block> newBlocks) throws Exception
	{
		
		AT_API_Platform_Impl.getInstance().setDBSet( fork );
	
		//int originalHeight = 0;
		
		//ORPHAN BLOCK IN FORK TO VALIDATE THE NEW BLOCKS
		if(lastCommonBlock != null)
		{
			//GET STATES TO RESTORE
			Map<String, byte[]> states = fork.getATStateMap().getStates( lastCommonBlock.getHeight(fork) );
			
			//HEIGHT TO ROLL BACK
	//		originalHeight = lastCommonBlock.getHeight();
			int height = (int)(Math.round( lastCommonBlock.getHeight(fork) /AT_Constants.STATE_STORE_DISTANCE))
					*AT_Constants.STATE_STORE_DISTANCE;
	
			//GET LAST BLOCK
			Block lastBlock = fork.getBlockMap().getLastBlock();
			
			//ORPHAN LAST BLOCK UNTIL WE HAVE REACHED COMMON BLOCK
			while(!Arrays.equals(lastBlock.getSignature(), lastCommonBlock.getSignature()))
			{
				lastBlock.orphan(fork);
				lastBlock = fork.getBlockMap().getLastBlock();
			}
	
			while ( lastBlock.getHeight(fork) >= height && lastBlock.getHeight(fork) > 11 )
			{
				newBlocks.add( 0 , lastBlock );
				//Block tempBlock = fork.getBlockMap().getLastBlock();
				lastBlock.orphan(fork);
				lastBlock = fork.getBlockMap().getLastBlock();
				//lastBlock = tempBlock;
			}
			
			for ( String id : states.keySet() )
			{
				byte[] address = Base58.decode( id ); //25 BYTES
				address = Bytes.ensureCapacity( address , AT_Constants.AT_ID_SIZE, 0 ); // 32 BYTES
				AT at = fork.getATMap().getAT( address );
				
				at.setState( states.get( id ) );
				
				fork.getATMap().update( at , height );
				
			}
			
			fork.getATMap().deleteAllAfterHeight( height );
			fork.getATStateMap().deleteStatesAfter( height );
			
	
		}
		
		//VALIDATE THE NEW BLOCKS
		for(Block block: newBlocks)
		{
			int heigh = block.getHeight(fork);

			//CHECK IF VALID
			if(block.isValid(fork, false))
			{
				//PROCESS TO VALIDATE NEXT BLOCKS
				block.process(fork);
			}
			else
			{
				AT_API_Platform_Impl.getInstance().setDBSet( fork );
				//INVALID BLOCK THROW EXCEPTION
				throw new Exception("Dishonest peer");
			}
		}
	}

	public List<Transaction> synchronize(DBSet dbSet, Block lastCommonBlock, List<Block> newBlocks) throws Exception
	{
		List<Transaction> orphanedTransactions = new ArrayList<Transaction>();
		
		//VERIFY ALL BLOCKS TO PREVENT ORPHANING INCORRECTLY
		checkNewBlocks(dbSet.fork(), lastCommonBlock, newBlocks);	
		
		//NEW BLOCKS ARE ALL VALID SO WE CAN ORPHAN THEM FOR REAL NOW

		AT_API_Platform_Impl.getInstance().setDBSet( dbSet );
		
		if(lastCommonBlock != null)
		{
			//GET STATES TO RESTORE
			Map<String, byte[]> states = dbSet.getATStateMap().getStates( lastCommonBlock.getHeight(dbSet) );
			
			//HEIGHT TO ROLL BACK
			int height = (int)(Math.round( lastCommonBlock.getHeight(dbSet)/AT_Constants.STATE_STORE_DISTANCE))
					*AT_Constants.STATE_STORE_DISTANCE;

			//GET LAST BLOCK
			Block lastBlock = dbSet.getBlockMap().getLastBlock();
			
			//ORPHAN LAST BLOCK UNTIL WE HAVE REACHED COMMON BLOCK
			while(!Arrays.equals(lastBlock.getSignature(), lastCommonBlock.getSignature()))
			{
				//ADD ORPHANED TRANSACTIONS
				orphanedTransactions.addAll(lastBlock.getTransactions());
				
				lastBlock.orphan(dbSet);
				lastBlock = dbSet.getBlockMap().getLastBlock();
			}

			while ( lastBlock.getHeight(dbSet) >= height && lastBlock.getHeight(dbSet) > 11 )
			{
				orphanedTransactions.addAll(lastBlock.getTransactions());
				lastBlock.orphan(dbSet);
				lastBlock = dbSet.getBlockMap().getLastBlock();
			}
			
			for ( String id : states.keySet() )
			{
				byte[] address = Base58.decode( id ); //25 BYTES
				address = Bytes.ensureCapacity( address , AT_Constants.AT_ID_SIZE, 0 ); // 32 BYTES
				AT at = dbSet.getATMap().getAT( address );
				
				at.setState( states.get( id ) );
				
				dbSet.getATMap().update( at , height );
				
			}

			dbSet.getATMap().deleteAllAfterHeight( height );
			dbSet.getATStateMap().deleteStatesAfter( height );

		}
		
		//PROCESS THE NEW BLOCKS
		for(Block block: newBlocks)
		{
			//SYNCHRONIZED PROCESSING
			this.process(dbSet, block);
		}	
		
		return orphanedTransactions;
	}
	
	public void synchronize(Peer peer) throws Exception
	{
		LOGGER.info("Synchronizing: " + peer.getAddress().getHostAddress() + " - " + peer.getPing());
		
		//FIND LAST COMMON BLOCK
		Block common =  this.findLastCommonBlock(peer);
		
		DBSet dbSet = DBSet.getInstance();
		
		//CHECK COMMON BLOCK EXISTS
		List<byte[]> signatures;
		if(Arrays.equals(common.getSignature(), dbSet.getBlockMap().getLastBlockSignature()))
		{
			//GET NEXT 500 SIGNATURES
			signatures = this.getBlockSignatures(common, BlockChain.MAX_SIGNATURES, peer);
			
			//CREATE BLOCK BUFFER
			BlockBuffer blockBuffer = new BlockBuffer(signatures, peer);
			
			//GET AND PROCESS BLOCK BY BLOCK
			for(byte[] signature: signatures)
			{
				//GET BLOCK
				Block block = blockBuffer.getBlock(signature);
				
				//PROCESS BLOCK
				if(!this.process(dbSet, block))
				{
					//INVALID BLOCK THROW EXCEPTION
					throw new Exception("Dishonest peer on block " + block.getHeight(dbSet));
				}
			}
			
			//STOP BLOCKBUFFER
			blockBuffer.stopThread();
		}
		else
		{
			//GET SIGNATURES FROM COMMON HEIGHT UNTIL CURRENT HEIGHT
			signatures = this.getBlockSignatures(common, dbSet.getBlockMap().getLastBlock()
					.getHeight(dbSet) - common.getHeight(dbSet), peer);	
			
			//GET THE BLOCKS FROM SIGNATURES
			List<Block> blocks = this.getBlocks(signatures, peer);
							
			//SYNCHRONIZE BLOCKS
			List<Transaction> orphanedTransactions = this.synchronize(dbSet, common, blocks);
			
			//SEND ORPHANED TRANSACTIONS TO PEER
			for(Transaction transaction: orphanedTransactions)
			{
				TransactionMessage transactionMessage = new TransactionMessage(transaction);
				peer.sendMessage(transactionMessage);
			}
		}
	}
	
	private List<byte[]> getBlockSignatures(Block start, int amount, Peer peer) throws Exception
	{
		//ASK NEXT 500 HEADERS SINCE START
		List<byte[]> headers = this.getBlockSignatures(start.getSignature(), peer);
		List<byte[]> nextHeaders;
		if(headers.size() > 0 && headers.size() < amount)
		{
			do
			{
				nextHeaders = this.getBlockSignatures(headers.get(headers.size()-1), peer);
				headers.addAll(nextHeaders);
			}
			while(headers.size() < amount && nextHeaders.size() > 0);
		}
		
		return headers;
	}
	
	private List<byte[]> getBlockSignatures(byte[] header, Peer peer) throws Exception
	{
		///CREATE MESSAGE
		Message message = MessageFactory.getInstance().createGetHeadersMessage(header);
		
		//SEND MESSAGE TO PEER
		SignaturesMessage response = (SignaturesMessage) peer.getResponse(message);

		if (response == null)
			throw new Exception("Failed to communicate with peer");

		return response.getSignatures();
	}
	
	private Block findLastCommonBlock(Peer peer) throws Exception
	{
		
		DBSet dbSet = DBSet.getInstance();
		
		Block block = dbSet.getBlockMap().getLastBlock();
		
		//GET HEADERS UNTIL COMMON BLOCK IS FOUND OR ALL BLOCKS HAVE BEEN CHECKED
		List<byte[]> headers = this.getBlockSignatures(block.getSignature(), peer);
		
		while(headers.size() == 0 && block.getHeight(dbSet) > 1)
		{
			//GO 500 BLOCKS BACK
			for(int i=0; i<BlockChain.MAX_SIGNATURES && block.getHeight(dbSet) > 1; i++)
			{
				block = block.getParent(dbSet);
			}
			
			headers = this.getBlockSignatures(block.getSignature(), peer);
		}
		
		//CHECK IF NO HEADERS FOUND EVEN AFTER CHECKING WITH THE GENESISBLOCK
		if(headers.size() == 0)
		{
			throw new Exception("Dishonest peer");
		}
		
		//FIND LAST COMMON BLOCK IN HEADERS
		for(int i=headers.size()-1; i>=0; i--)
		{
			//CHECK IF WE KNOW BLOCK
			if(dbSet.getBlockMap().contains(headers.get(i)))
			{
				return dbSet.getBlockMap().get(headers.get(i));
			}
		}
		
		return block;
	}

	private List<Block> getBlocks(List<byte[]> signatures, Peer peer) throws Exception {
		
		List<Block> blocks = new ArrayList<Block>();
		
		for(byte[] signature: signatures)
		{
			//ADD TO LIST
			blocks.add(this.getBlock(signature, peer));	
		}
		
		return blocks;
	}
	
	private Block getBlock(byte[] signature, Peer peer) throws Exception
	{
		//CREATE MESSAGE
		Message message = MessageFactory.getInstance().createGetBlockMessage(signature);
		
		//SEND MESSAGE TO PEER
		BlockMessage response = (BlockMessage) peer.getResponse(message);
		
		//CHECK IF WE GOT RESPONSE
		if(response == null)
		{
			//ERROR
			throw new Exception("Peer timed out");
		}
		
		//CHECK BLOCK SIGNATURE
		if(!response.getBlock().isSignatureValid())
		{
			throw new Exception("Invalid block");
		}
		
		//ADD TO LIST
		return response.getBlock();
	}
	
	
	//SYNCHRONIZED DO NOT PROCCESS A BLOCK AT THE SAME TIME
	public synchronized boolean process(DBSet dbSet, Block block) 
	{
		//CHECK IF WE ARE STILL PROCESSING BLOCKS
		if(this.run)
		{
			//SYNCHRONIZED MIGHT HAVE BEEN PROCESSING PREVIOUS BLOCK
			if(block.isValid(dbSet, false))
			{
				//PROCESS
				dbSet.getBlockMap().setProcessing(true);
				block.process(dbSet);		
				dbSet.getBlockMap().setProcessing(false);
				
				return true;
			}
		}
		
		return false;
	}

	public void stop() {
		
		this.run = false;
		this.process(DBSet.getInstance(), null);
	}
}
