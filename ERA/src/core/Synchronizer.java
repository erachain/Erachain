package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
// import org.apache.log4j.Logger;
import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;

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
	private static final byte[] PEER_TEST = new byte[]{(byte)185, (byte)146, (byte)168, (byte)226};
	
	private boolean run = true;
	
	public Synchronizer()
	{
		this.run = true;
	}
	
	static boolean USE_AT_ORPHAN = false;
	static int BAN_BLOCK_TIMES = BlockChain.GENERATING_MIN_BLOCK_TIME / 60 * 30;
	
	private void checkNewBlocks(DBSet fork, Block lastCommonBlock, List<Block> newBlocks, Peer peer) throws Exception
	{
		
		AT_API_Platform_Impl.getInstance().setDBSet( fork );
	
		Controller cnt = Controller.getInstance();
		//int originalHeight = 0;
		
		//ORPHAN BLOCK IN FORK TO VALIDATE THE NEW BLOCKS
		if(lastCommonBlock != null)
		{
			//GET STATES TO RESTORE
			Map<String, byte[]> states = fork.getParent().getATStateMap().getStates( lastCommonBlock.getHeight(fork) );
			
			//HEIGHT TO ROLL BACK
	//		originalHeight = lastCommonBlock.getHeight();
			int height_AT = lastCommonBlock.getHeight(fork);
			if (USE_AT_ORPHAN) {
				height_AT = (int)(Math.round( height_AT /AT_Constants.STATE_STORE_DISTANCE))
					*AT_Constants.STATE_STORE_DISTANCE;
			} 
	
			//GET LAST BLOCK
			Block lastBlock = fork.getBlockMap().getLastBlock();
			
			int lastHeight = lastBlock.getHeight(fork);
			LOGGER.debug("*** core.Synchronizer.checkNewBlocks - lastBlock["
					+ lastHeight + "]"
					+ " search common block in FORK"
					+ " in mainDB: " + lastBlock.getHeight(fork.getParent()));

			//ORPHAN LAST BLOCK UNTIL WE HAVE REACHED COMMON BLOCK
			while(!Arrays.equals(lastBlock.getSignature(), lastCommonBlock.getSignature()))
			{
				if (cnt.getBlockChain().getCheckPoint(fork) > lastBlock.getHeightByParent(fork)) {
					//cnt.closePeerOnError(peer, "Dishonest peer by not valid lastCommonBlock["
					//		+ lastCommonBlock.getHeight(fork) + "]"); // icreator

					String mess = "Dishonest peer by not valid lastCommonBlock["
							+ lastCommonBlock.getHeight(fork) + "]";
					peer.ban(BAN_BLOCK_TIMES>>2, mess);
					throw new Exception(mess);
				}
				lastBlock.orphan(fork);
				lastBlock = fork.getBlockMap().getLastBlock();
			}

			LOGGER.debug("*** core.Synchronizer.checkNewBlocks - lastBlock["
					+ lastHeight + "]"
					+ " run AT_TRANSACTION in FORK");

			if (USE_AT_ORPHAN) {
				// AT_TRANSACTION - not from GENESIS BLOCK
				while ( lastBlock.getHeight(fork) >= height_AT && lastBlock.getHeight(fork) > 1)
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
					
					fork.getATMap().update( at , height_AT );
					
				}
				
				fork.getATMap().deleteAllAfterHeight( height_AT );
				fork.getATStateMap().deleteStatesAfter( height_AT );
			}
	
		}
		
		//VALIDATE THE NEW BLOCKS
		for(Block block: newBlocks)
		{
			int heigh = block.getHeightByParent(fork);

			//CHECK IF VALID
			if(block.isValid(fork) && block.isSignatureValid())
			{
				//PROCESS TO VALIDATE NEXT BLOCKS
				block.process(fork);
			}
			else
			{
				AT_API_Platform_Impl.getInstance().setDBSet( fork.getParent() );

				//cnt.closePeerOnError(peer, "Dishonest peer by not valid block.heigh: " + heigh); // icreator

				block.isValid(fork);
				block.isSignatureValid();
				
				//INVALID BLOCK THROW EXCEPTION
				String mess = "Dishonest peer by not is Valid block, heigh: " + heigh;
				peer.ban(BAN_BLOCK_TIMES, mess);
				throw new Exception(mess);
			}
		}

		AT_API_Platform_Impl.getInstance().setDBSet( fork.getParent() );
	}

	// process new BLOCKS to DB and orphan DB
	public List<Transaction> synchronize(DBSet dbSet, Block lastCommonBlock, List<Block> newBlocks, Peer peer) throws Exception
	{
		List<Transaction> orphanedTransactions = new ArrayList<Transaction>();
		
		//VERIFY ALL BLOCKS TO PREVENT ORPHANING INCORRECTLY
		checkNewBlocks(dbSet.fork(), lastCommonBlock, newBlocks, peer);	
		
		//NEW BLOCKS ARE ALL VALID SO WE CAN ORPHAN THEM FOR REAL NOW
		
		if(lastCommonBlock != null)
		{
			//GET STATES TO RESTORE
			Map<String, byte[]> states = dbSet.getATStateMap().getStates( lastCommonBlock.getHeight(dbSet) );
			
			//HEIGHT TO ROLL BACK
			int height_AT = (int)(Math.round( lastCommonBlock.getHeight(dbSet)/AT_Constants.STATE_STORE_DISTANCE))
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

			if (USE_AT_ORPHAN) {
				// THEN orphan next AT_height blocks
				//NOT orphan GENESIS BLOCK
				while ( lastBlock.getHeight(dbSet) >= height_AT && lastBlock.getHeight(dbSet) > 1 )
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
					
					dbSet.getATMap().update( at , height_AT );
					
				}
	
				dbSet.getATMap().deleteAllAfterHeight( height_AT );
				dbSet.getATStateMap().deleteStatesAfter( height_AT );
			}

		}
		
		//PROCESS THE NEW BLOCKS
		for(Block block: newBlocks)
		{
			//SYNCHRONIZED PROCESSING
			this.process(dbSet, block);
		}	
		
		return orphanedTransactions;
	}
	
	public void synchronize(DBSet dbSet, int checkPointHeight, Peer peer) throws Exception
	{

		if (!this.run)
			return;

		/*
		LOGGER.error("Synchronizing from peer: " + peer.toString() + ":"
					+ peer.getAddress().getHostAddress() + " - " + peer.getPing());
					*/

		byte[] lastBlockSignature = dbSet.getBlockMap().getLastBlockSignature();
				
		// FIND HEADERS for common CHAIN
		if (Arrays.equals(peer.getAddress().getAddress(), PEER_TEST)) {
			LOGGER.info("Synchronizing from peer: " + peer.toString() + ":"
					+ peer.getAddress().getHostAddress() + ", ping: " + peer.getPing());			
		}
		Tuple2<byte[], List<byte[]>> signatures = this.findHeaders(peer, lastBlockSignature, checkPointHeight);
		if (signatures.b.size() == 0) {
			//String mess = "Dishonest peer - signatures == []: " + peer.getAddress().getHostAddress();
			//peer.ban(BAN_BLOCK_TIMES, mess);
			//throw new Exception(mess);
			//return;
		}

		//FIND FIRST COMMON BLOCK in HEADERS CHAIN
		Block common = dbSet.getBlockMap().get(signatures.a);
		int commonBlockHeight = common.getHeight(dbSet);

		if (!this.run || dbSet.isStoped())
			return;

		LOGGER.info("Synchronizing from COMMON blockHeight " + commonBlockHeight);
		
		//CHECK COMMON BLOCK EXISTS
		if(Arrays.equals(common.getSignature(), lastBlockSignature))
		{
			
			if (signatures.b.size() == 0) {
				// TODO it is because incorrect calculate WIN_TARGET value
				//dbSet.getBlockSignsMap().setFullWeight(Controller.getInstance().getPeerHWeights().get(peer).b);
				Tuple2<Integer, Long> myHW = Controller.getInstance().getMyHWeight(false);
				Controller.getInstance().setWeightOfPeer(peer, myHW);
				LOGGER.info("  set new Weight " + myHW + " for PEER " + peer.getAddress().getHostAddress());
				return;
			}
			// CONNON BLOCK is my LAST BLOCK in CHAIN
			
			//CREATE BLOCK BUFFER
			BlockBuffer blockBuffer = new BlockBuffer(signatures.b, peer);
			
			//GET AND PROCESS BLOCK BY BLOCK
			for(byte[] signature: signatures.b)
			{
				//GET BLOCK
				Block blockFromPeer = blockBuffer.getBlock(signature);
				
				if (!this.run) {
					return;
				}
				
				if (blockFromPeer == null) { // icreator
					
					//INVALID BLOCK THROW EXCEPTION
					String mess = "Dishonest peer on block null";
					peer.ban(10, mess);
					throw new Exception(mess);
				}
				blockFromPeer.setCalcGeneratingBalance(dbSet); // NEED SET it
				
				//PROCESS BLOCK
				if(!this.process(dbSet, blockFromPeer))
				{
					if (!this.run)
						return;

					//INVALID BLOCK THROW EXCEPTION
					String mess = "Dishonest peer on block " + blockFromPeer.getHeight(dbSet);
					peer.ban(BAN_BLOCK_TIMES, mess);
					throw new Exception(mess);
				}
			}
			
			//STOP BLOCKBUFFER
			blockBuffer.stopThread();
		}
		else
		{
			
			//GET THE BLOCKS FROM SIGNATURES
			List<Block> blocks = this.getBlocks(dbSet, signatures.b, peer);

			if (!this.run) {
				return;
			}

			//SYNCHRONIZE BLOCKS
			/*
			LOGGER.error("core.Synchronizer.synchronize from common block for blocks: " + blocks.size());
			*/
			List<Transaction> orphanedTransactions = this.synchronize(dbSet, common, blocks, peer);
			if (!this.run) {
				return;
			}

			//SEND ORPHANED TRANSACTIONS TO PEER
			for(Transaction transaction: orphanedTransactions)
			{
				if (!this.run) {
					return;
				}

				TransactionMessage transactionMessage = new TransactionMessage(transaction);
				peer.sendMessage(transactionMessage);
			}
		}
		
		//dbSet.commitHard();
	}
	
	/*
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
	*/
	
	private List<byte[]> getBlockSignatures(byte[] header, Peer peer) throws Exception
	{

		/*
		LOGGER.error("core.Synchronizer.getBlockSignatures(byte[], Peer) for: " + Base58.encode(header));
		*/

		///CREATE MESSAGE
		Message message = MessageFactory.getInstance().createGetHeadersMessage(header);
		
		//SEND MESSAGE TO PEER
		// see response callback in controller.Controller.onMessage(Message)
		// type = GET_SIGNATURES_TYPE
		SignaturesMessage response = (SignaturesMessage) peer.getResponse(message);

		if (response == null) {
			throw new Exception("Failed to communicate with peer - response = null");
		}

		return response.getSignatures();
	}
	
	private Tuple2<byte[], List<byte[]>> findHeaders(Peer peer, byte[] lastBlockSignature, int checkPointHeight) throws Exception
	{

		DBSet dbSet = DBSet.getInstance();
		
		List<byte[]> headers = this.getBlockSignatures(lastBlockSignature, peer);
		if (headers.size() > 0) {
			// end of my CHAIN is common
			return new Tuple2<byte[], List<byte[]>>(lastBlockSignature, headers);
		}

		
		//int myChainHeight = Controller.getInstance().getBlockChain().getHeight();
		int maxChainHeight = dbSet.getBlockSignsMap().getHeight(lastBlockSignature);
		if (maxChainHeight < checkPointHeight) {
			maxChainHeight = checkPointHeight;
		}

		LOGGER.info("core.Synchronizer.findLastCommonBlock(Peer) for: "
				+ " getBlockMap().getLastBlock: " + maxChainHeight
				+ "to minHeight: " + checkPointHeight);

		// try get check point block from peer
		// GENESIS block nake ERROR in network.Peer.sendMessage(Message) -> this.out.write(message.toBytes());
		// TODO fix it error
		byte[] checkPointHeightSignature;
		Block checkPointHeightCommonBlock = null;
		if (checkPointHeight == 1) {
			checkPointHeightCommonBlock = Controller.getInstance().getBlockChain().getGenesisBlock().getChild(dbSet);
			if (checkPointHeightCommonBlock == null) {
				return new Tuple2<byte[], List<byte[]>>(lastBlockSignature, headers);				
			}
			checkPointHeightSignature = checkPointHeightCommonBlock.getSignature();
		} else {
			checkPointHeightSignature = dbSet.getBlockHeightsMap().get((long)checkPointHeight);
		}
		
		try {
			// try get common block from PEER
			checkPointHeightCommonBlock = getBlock(checkPointHeightSignature, peer);
		} catch (Exception e) {
			String mess = "Dishonest peer - error in PEER: " + peer.getAddress().getHostAddress();
			//// banned in getBlock -- peer.ban(BAN_BLOCK_TIMES>>3, mess);
			throw new Exception(mess);
		}

		if (checkPointHeightCommonBlock == null
				|| checkPointHeightSignature == null) {
			String mess = "Dishonest peer: my block[" + checkPointHeight
					+ "\n -> common BLOCK not found";
			peer.ban(BAN_BLOCK_TIMES, mess);

			throw new Exception(mess);
		}

		//GET HEADERS UNTIL COMMON BLOCK IS FOUND OR ALL BLOCKS HAVE BEEN CHECKED
		int steep = BlockChain.MAX_ORPHAN>>2;
		byte[] lastBlockSignatureCommon;
		do {
			maxChainHeight -= steep;
			if (maxChainHeight < checkPointHeight) {
				maxChainHeight = checkPointHeight;
				lastBlockSignatureCommon = checkPointHeightCommonBlock.getSignature();
				break;
			} else {
				lastBlockSignatureCommon = dbSet.getBlockHeightsMap().get((long)maxChainHeight);				
			}
				
			headers = this.getBlockSignatures(lastBlockSignatureCommon, peer);
			if (headers.size() > 0)
				break;
			// x2
			//steep <<= 1;
			
		} while ( maxChainHeight > checkPointHeight && headers.isEmpty());

		// CLEAR head of common headers
		while ( !headers.isEmpty() && dbSet.getBlockMap().contains(headers.get(0))) {
			lastBlockSignatureCommon = headers.remove(0);
		}

		if (false && headers.isEmpty()) {
			String mess = "Dishonest peer by headers.size==0 " + peer.getAddress().getHostAddress();
			
			peer.ban(BAN_BLOCK_TIMES, mess);
			throw new Exception(mess);
		}

		
		return new Tuple2<byte[], List<byte[]>>(lastBlockSignatureCommon, headers);
	}

	private List<Block> getBlocks(DBSet dbSet, List<byte[]> signatures, Peer peer) throws Exception {
		
		List<Block> blocks = new ArrayList<Block>();
		
		for(byte[] signature: signatures)
		{
			if(!this.run) {
				return new ArrayList<Block>();
			}

			//ADD TO LIST
			Block block = getBlock(signature, peer);
			// NOW generating balance not was send by NET
			// need to SET it!
			block.setCalcGeneratingBalance(dbSet);

			blocks.add(block);	
		}
		
		return blocks;
	}
	
	public static Block getBlock(byte[] signature, Peer peer) throws Exception
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
		
		Block block = response.getBlock();
		if(block == null)
		{
			String mess = "*** Block is NULL";
			peer.ban(600, mess);
			throw new Exception(mess);
		}
		
		//CHECK BLOCK SIGNATURE
		if(!block.isSignatureValid())
		{
			String mess = "*** Invalid block --signature";
			peer.ban(600, mess);
			throw new Exception(mess);
		}
		
		block.makeTransactionsHash();
		//ADD TO LIST
		return block;
	}
	
	
	//SYNCHRONIZED DO NOT PROCCESS A BLOCK AT THE SAME TIME
	public synchronized boolean process(DBSet dbSet, Block block)
	{
		//CHECK IF WE ARE STILL PROCESSING BLOCKS
		if(this.run)
		{
			//SYNCHRONIZED MIGHT HAVE BEEN PROCESSING PREVIOUS BLOCK
			if(block.isValid(dbSet) && block.isSignatureValid())
			{
				//PROCESS
				if (!this.run)
					return true;
				
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
		//this.process(DBSet.getInstance(), null);
	}
}
