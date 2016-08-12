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
import settings.Settings;
import at.AT;
import at.AT_API_Platform_Impl;
import at.AT_Constants;
import controller.Controller;
import core.block.Block;
import core.crypto.Base58;
import core.transaction.Transaction;
import settings.Settings;

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
				if (lastBlock.getHeight(fork) == 1 || lastBlock.getParent(fork) == null)
				{
					break;
				}
				lastBlock.orphan(fork);
				lastBlock = fork.getBlockMap().getLastBlock();
			}
	
			// AT_TRANSACTION - not from GENESIS BLOCK
			while ( lastBlock.getHeight(fork) >= height && lastBlock.getHeight(fork) > 1)
			{
				if (lastBlock.getHeight(fork) == 1 || lastBlock.getParent(fork) == null)
				{
					break;
				}
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
			if(block.isValid(fork))
			{
				//PROCESS TO VALIDATE NEXT BLOCKS
				block.process(fork);
			}
			else
			{
				AT_API_Platform_Impl.getInstance().setDBSet( fork );
				//INVALID BLOCK THROW EXCEPTION
				throw new Exception("Dishonest peer by not valid block.heigh: " + heigh);
			}
		}
	}

	// process new BLOCKS to DB and orphan DB
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
				if (lastBlock.getHeight(dbSet) == 1 || lastBlock.getParent(dbSet) == null)
				{
					break;
				}

				//ADD ORPHANED TRANSACTIONS
				orphanedTransactions.addAll(lastBlock.getTransactions());
				
				lastBlock.orphan(dbSet);
				lastBlock = dbSet.getBlockMap().getLastBlock();
			}

			// NOT orphan GENESIS BLOCK
			while ( lastBlock.getHeight(dbSet) >= height && lastBlock.getHeight(dbSet) > 1 )
			{
				lastBlock.orphan(dbSet);
				if (lastBlock.getHeight(dbSet) == 1 || lastBlock.getParent(dbSet) == null)
				{
					break;
				}
				orphanedTransactions.addAll(lastBlock.getTransactions());
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
	
	public void synchronize(DBSet dbSet, int lastTrueBlockHeight, Peer peer) throws Exception
	{
		LOGGER.info("Synchronizing from peer: " + peer.toString() + ":" + peer.getAddress().getHostAddress() + " - " + peer.getPing());
		
		//FIND LAST COMMON BLOCK
		Block common =  this.findLastCommonBlock(peer);
				
		int commonBlockHeight = common.getHeight(dbSet);
		if (lastTrueBlockHeight > commonBlockHeight )
			// MAX orhpan CHAIN LEN
			throw new Exception("Dishonest peer on TRUE block > CONFIRMS_TRUE " + common.getHeight(dbSet));

		LOGGER.info("Synchronizing from COMMON blockHeight " + commonBlockHeight);
		
		//CHECK COMMON BLOCK EXISTS
		List<byte[]> signatures;
		if(Arrays.equals(common.getSignature(), dbSet.getBlockMap().getLastBlockSignature()))
		{
			//GET NEXT 500 SIGNATURES
			signatures = this.getBlockSignatures(common, BlockChain.MAX_SIGNATURES, peer);
			if (signatures.size() == 0) {
				//INVALID HEADERS
				throw new Exception("HEADERS.size == 0 from peer on block " + peer
						+ "\n on Common Block[" + common.getHeight(dbSet) + "]"
						+ "[" + Base58.encode(common.getSignature()) + "]"
						+ "\n and My Block full Weight: " + Controller.getInstance().getMyHWeight().a
						+ "/" + Controller.getInstance().getMyHWeight().b);
			}
			
			//CREATE BLOCK BUFFER
			BlockBuffer blockBuffer = new BlockBuffer(signatures, peer);
			
			//GET AND PROCESS BLOCK BY BLOCK
			for(byte[] signature: signatures)
			{
				//GET BLOCK
				Block blockFromPeer = blockBuffer.getBlock(signature);
				//int blockHeightFromPeer = blockFromPeer.getHeight(dbSet);
				
				//PROCESS BLOCK
				if(!this.process(dbSet, blockFromPeer))
				{
					//INVALID BLOCK THROW EXCEPTION
					throw new Exception("Dishonest peer on block " + blockFromPeer.getHeight(dbSet));
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
			LOGGER.info("core.Synchronizer.synchronize from common block for blocks: " + blocks.size());
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
		byte[] startSignature = start.getSignature();
		List<byte[]> headers = this.getBlockSignatures(startSignature, peer);
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

		LOGGER.info("core.Synchronizer.getBlockSignatures(byte[], Peer) for: " + Base58.encode(header));

		///CREATE MESSAGE
		Message message = MessageFactory.getInstance().createGetHeadersMessage(header);
		
		//SEND MESSAGE TO PEER
		// see response callback in controller.Controller.onMessage(Message)
		// type = GET_SIGNATURES_TYPE
		SignaturesMessage response = (SignaturesMessage) peer.getResponse(message);

		if (response == null)
			throw new Exception("Failed to communicate with peer - response = null");

		return response.getSignatures();
	}
	
	private Block findLastCommonBlock(Peer peer) throws Exception
	{
		
		DBSet dbSet = DBSet.getInstance();
		
		Block block = dbSet.getBlockMap().getLastBlock();
		long myBlockWeight = block.getWinValue(dbSet);
		int myBlockHeight = block.getHeight(dbSet);
		
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
			throw new Exception("Dishonest peer: my block[" + myBlockHeight + "] Weight: " + myBlockWeight
					+ "\n -> headers.size() == 0");
		}
		
		//FIND LAST COMMON BLOCK IN HEADERS
		for(int i=headers.size()-1; i>=0; i--)
		{
			//CHECK IF WE KNOW BLOCK
			if(dbSet.getBlockMap().contains(headers.get(i)))
			{
				Block newBlock = dbSet.getBlockMap().get(headers.get(i));
				int hhh = newBlock.getHeight(dbSet);
				return newBlock;
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
			throw new Exception("*** Invalid block");
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
			if(block.isValid(dbSet))
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
