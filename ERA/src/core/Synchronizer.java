package core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// import org.apache.log4j.Logger;
import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;

import network.Peer;
import network.message.BlockMessage;
import network.message.Message;
import network.message.MessageFactory;
import network.message.SignaturesMessage;
import network.message.TransactionMessage;
import ntp.NTP;
import settings.Settings;
import utils.ObserverMessage;
import at.AT;
import at.AT_API_Platform_Impl;
import at.AT_Constants;
import controller.Controller;
import core.block.Block;
import core.crypto.Base58;
import core.transaction.Transaction;
import datachain.DCSet;
import datachain.TransactionMap;
import settings.Settings;

import com.google.common.primitives.Bytes;

public class Synchronizer
{
	private static final Logger LOGGER = Logger.getLogger(Synchronizer.class);
	private static final byte[] PEER_TEST = new byte[]{(byte)185, (byte)195, (byte)26, (byte)245}; // 185.195.26.245
	
	//private boolean run = true;
	//private Block runedBlock;
	private Peer fromPeer;
	
	
	public Synchronizer()
	{
		//this.run = true;
	}
	
	public static int BAN_BLOCK_TIMES = BlockChain.GENERATING_MIN_BLOCK_TIME / 60 * 8;
	
	public Peer getPeer() {
		return fromPeer;
	}
	
	private void checkNewBlocks(DCSet fork, Block lastCommonBlock, List<Block> newBlocks, Peer peer) throws Exception
	{
		
		LOGGER.debug("*** core.Synchronizer.checkNewBlocks - START");

		if (BlockChain.USE_AT_ATX) AT_API_Platform_Impl.getInstance().setDBSet( fork );
	
		Controller cnt = Controller.getInstance();
		//int originalHeight = 0;
		
		//ORPHAN BLOCK IN FORK TO VALIDATE THE NEW BLOCKS
		Map<String, byte[]> states = new TreeMap<String, byte[]>();
		int height_AT = 0;
		if(lastCommonBlock != null)
		{
			if (BlockChain.USE_AT_ATX) {
				//GET STATES TO RESTORE
				states = fork.getParent().getATStateMap().getStates( lastCommonBlock.getHeight(fork) );
				
				//HEIGHT TO ROLL BACK
		//		originalHeight = lastCommonBlock.getHeight();
				height_AT = (int)(Math.round( lastCommonBlock.getHeight(fork) /AT_Constants.STATE_STORE_DISTANCE))
					*AT_Constants.STATE_STORE_DISTANCE;
			}
	
			//GET LAST BLOCK
			Block lastBlock = fork.getBlockMap().getLastBlock();
			
			int lastHeight = lastBlock.getHeight(fork);
			LOGGER.debug("*** core.Synchronizer.checkNewBlocks - lastBlock["
					+ lastHeight + "]\n"
					+ "newBlocks.size = " + newBlocks.size()
					+ "\n search common block in FORK"
					+ " in mainDB: " + lastBlock.getHeight(fork.getParent())
					+ "\n for lastCommonBlock = " + lastCommonBlock.getHeight(fork));

			//ORPHAN LAST BLOCK UNTIL WE HAVE REACHED COMMON BLOCK
			while(!Arrays.equals(lastBlock.getSignature(), lastCommonBlock.getSignature()))
			{
				LOGGER.debug("*** ORPHAN LAST BLOCK UNTIL WE HAVE REACHED COMMON BLOCK [" + lastBlock.getHeightByParent(fork) + "]");
				if (cnt.getBlockChain().getCheckPoint(fork) > lastBlock.getHeightByParent(fork)) {
					//cnt.closePeerOnError(peer, "Dishonest peer by not valid lastCommonBlock["
					//		+ lastCommonBlock.getHeight(fork) + "]"); // icreator

					String mess = "Dishonest peer by not valid lastCommonBlock["
							+ lastCommonBlock.getHeight(fork) + "]";
					peer.ban(BAN_BLOCK_TIMES>>2, mess);
					throw new Exception(mess);
				}
				//LOGGER.debug("*** core.Synchronizer.checkNewBlocks - try orphan: " + lastBlock.getHeight(fork));
				if (cnt.isOnStopping())
					throw new Exception("on stoping");

				//runedBlock = lastBlock; // FOR quick STOPPING
				lastBlock.orphan(fork);
				LOGGER.debug("*** core.Synchronizer.checkNewBlocks - orphaned!");
				lastBlock = fork.getBlockMap().getLastBlock();
			}

			LOGGER.debug("*** core.Synchronizer.checkNewBlocks - lastBlock[" + lastHeight + "]"	+ " run AT_TRANSACTION in FORK");

			if (BlockChain.USE_AT_ATX) {
				// AT_TRANSACTION - not from GENESIS BLOCK
				while ( lastBlock.getHeight(fork) >= height_AT && lastBlock.getHeight(fork) > 1)
				{
					LOGGER.debug("*** ORPHAN AT BLOCK [" + lastBlock.getHeightByParent(fork) + "]");
					if (cnt.isOnStopping())
						throw new Exception("on stoping");

					newBlocks.add( 0 , lastBlock );
					//Block tempBlock = fork.getBlockMap().getLastBlock();
					//runedBlock = lastBlock; // FOR quick STOPPING
					lastBlock.orphan(fork);
					LOGGER.debug("*** core.Synchronizer.checkNewBlocks - orphaned!");
					lastBlock = fork.getBlockMap().getLastBlock();
				}
				
				for ( String id : states.keySet() )
				{
					if (cnt.isOnStopping())
						throw new Exception("on stoping");

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

		LOGGER.debug("*** core.Synchronizer.checkNewBlocks - VALIDATE THE NEW BLOCKS in FORK");

		for(Block block: newBlocks)
		{
			int heigh = block.getHeightByParent(fork);

			//CHECK IF VALID
			if(block.isSignatureValid() && block.isValid(fork))
			{
				//PROCESS TO VALIDATE NEXT BLOCKS
				//runedBlock = block;
				block.process(fork);
			}
			else
			{
				if (BlockChain.USE_AT_ATX) AT_API_Platform_Impl.getInstance().setDBSet( fork.getParent() );

				//block.isSignatureValid();
				//block.isValid(fork);
				
				//INVALID BLOCK THROW EXCEPTION
				String mess = "Dishonest peer by not is Valid block, heigh: " + heigh;
				peer.ban(BAN_BLOCK_TIMES, mess);
				throw new Exception(mess);
			}
		}

		if (BlockChain.USE_AT_ATX) AT_API_Platform_Impl.getInstance().setDBSet( fork.getParent() );
		
		LOGGER.debug("*** core.Synchronizer.checkNewBlocks - END");

	}

	// process new BLOCKS to DB and orphan DB
	public List<Transaction> synchronize_blocks(DCSet dcSet, Block lastCommonBlock, List<Block> newBlocks, Peer peer) throws Exception
	{
		TreeMap<String, Transaction> orphanedTransactions = new TreeMap<String, Transaction>();
		Controller cnt = Controller.getInstance();

		//VERIFY ALL BLOCKS TO PREVENT ORPHANING INCORRECTLY
		checkNewBlocks(dcSet.fork(), lastCommonBlock, newBlocks, peer);	
		
		//NEW BLOCKS ARE ALL VALID SO WE CAN ORPHAN THEM FOR REAL NOW
		Map<String, byte[]> states = new TreeMap<String, byte[]>();
		int height_AT = 0;

		if(lastCommonBlock != null)
		{
			if (BlockChain.USE_AT_ATX) {
				//GET STATES TO RESTORE
				states = dcSet.getATStateMap().getStates( lastCommonBlock.getHeight(dcSet) );
				
				//HEIGHT TO ROLL BACK
				height_AT = (int)(Math.round( lastCommonBlock.getHeight(dcSet)/AT_Constants.STATE_STORE_DISTANCE))
						*AT_Constants.STATE_STORE_DISTANCE;
			}

			//GET LAST BLOCK
			Block lastBlock = dcSet.getBlockMap().getLastBlock();
			
			//ORPHAN LAST BLOCK UNTIL WE HAVE REACHED COMMON BLOCK
			while(!Arrays.equals(lastBlock.getSignature(), lastCommonBlock.getSignature()))
			{
				if (cnt.isOnStopping())
					throw new Exception("on stoping");

				//ADD ORPHANED TRANSACTIONS
				//orphanedTransactions.addAll(lastBlock.getTransactions());
				for (Transaction transaction: lastBlock.getTransactions()) {
					if (cnt.isOnStopping())
						throw new Exception("on stoping");
					orphanedTransactions.put(new BigInteger(1, transaction.getSignature()).toString(16), transaction);					
				}
				LOGGER.debug("*** synchronize - orphanedTransactions.size:" + orphanedTransactions.size());
				
				//runedBlock = lastBlock; // FOR quick STOPPING
				LOGGER.debug("*** synchronize - orphan block...");
				this.pipeProcessOrOrphan(dcSet, lastBlock, true);
				lastBlock = dcSet.getBlockMap().getLastBlock();
			}

			if (BlockChain.USE_AT_ATX) {
				// THEN orphan next AT_height blocks
				//NOT orphan GENESIS BLOCK
				while ( lastBlock.getHeight(dcSet) >= height_AT && lastBlock.getHeight(dcSet) > 1 )
				{
					
					if (cnt.isOnStopping())
						throw new Exception("on stoping");

					//orphanedTransactions.addAll(lastBlock.getTransactions());
					for (Transaction transaction: lastBlock.getTransactions()) {
						if (cnt.isOnStopping())
							throw new Exception("on stoping");
						orphanedTransactions.put(new BigInteger(1, transaction.getSignature()).toString(16), transaction);					
					}
					LOGGER.debug("*** synchronize - AT orphanedTransactions.size:" + orphanedTransactions.size());
					
					//runedBlock = lastBlock; // FOR quick STOPPING
					LOGGER.debug("*** synchronize - orphan AT block...");
					this.pipeProcessOrOrphan(dcSet, lastBlock, true);
					lastBlock = dcSet.getBlockMap().getLastBlock();
				}
				
				for ( String id : states.keySet() )
				{
					if (cnt.isOnStopping())
						throw new Exception("on stoping");

					byte[] address = Base58.decode( id ); //25 BYTES
					address = Bytes.ensureCapacity( address , AT_Constants.AT_ID_SIZE, 0 ); // 32 BYTES
					AT at = dcSet.getATMap().getAT( address );
					
					at.setState( states.get( id ) );
					
					dcSet.getATMap().update( at , height_AT );
					
				}
	
				dcSet.getATMap().deleteAllAfterHeight( height_AT );
				dcSet.getATStateMap().deleteStatesAfter( height_AT );
			}

		}
		
		//PROCESS THE NEW BLOCKS
		LOGGER.debug("*** synchronize PROCESS NEW blocks.size:" + newBlocks.size());
		for(Block block: newBlocks)
		{

			if (cnt.isOnStopping())
				throw new Exception("on stoping");

			//SYNCHRONIZED PROCESSING
			this.pipeProcessOrOrphan(dcSet, block, false);
			for (Transaction transaction: block.getTransactions()) {
				if (cnt.isOnStopping())
					throw new Exception("on stoping");
				
				String key = new BigInteger(1, transaction.getSignature()).toString(16);
				if (orphanedTransactions.containsKey(key))
						orphanedTransactions.remove(key);					
			}
		}
		
		// CLEAR for DEADs
		TransactionMap map = dcSet.getTransactionMap();
		List<Transaction> orphanedTransactionsList = new ArrayList<Transaction>();
		for (Transaction transaction: orphanedTransactions.values()) {
			if (cnt.isOnStopping())
				throw new Exception("on stoping");
						
			//CHECK IF DEADLINE PASSED
			if(!(transaction.getDeadline() < NTP.getTime() ||
					map.contains(transaction.getSignature())))
			{
				orphanedTransactionsList.add(transaction);
			}
		}
		
		return orphanedTransactionsList;
	}
	
	public void synchronize(DCSet dcSet, int checkPointHeight, Peer peer, int peerHeight) throws Exception
	{

		Controller cnt = Controller.getInstance();

		if (cnt.isOnStopping())
			throw new Exception("on stoping");

		/*
		LOGGER.debug("Synchronizing from peer: " + peer.toString() + ":"
					+ peer.getAddress().getHostAddress() + " - " + peer.getPing());
					*/

		fromPeer = peer;
		
		byte[] lastBlockSignature = dcSet.getBlockMap().getLastBlockSignature();
				
		// FIND HEADERS for common CHAIN
		if (Arrays.equals(peer.getAddress().getAddress(), PEER_TEST)) {
			LOGGER.info("Synchronizing from peer: " + peer.toString() + ":"
					+ peer.getAddress().getHostAddress()
					//+ ", ping: " + peer.getPing()
					);			
		}
		Tuple2<byte[], List<byte[]>> signatures = this.findHeaders(peer, peerHeight, lastBlockSignature, checkPointHeight);
		if (signatures.b.size() == 0) {
			//String mess = "Dishonest peer - signatures == []: " + peer.getAddress().getHostAddress();
			//peer.ban(BAN_BLOCK_TIMES, mess);
			//throw new Exception(mess);
			//return;
		}

		//FIND FIRST COMMON BLOCK in HEADERS CHAIN
		Block common = dcSet.getBlockMap().get(signatures.a);
		if (common == null) { // icreator
			
			//INVALID BLOCK THROW EXCEPTION
			String mess = "Dishonest peer on COMMON block not found!!";
			peer.ban(BAN_BLOCK_TIMES>>4, mess);
			//STOP BLOCKBUFFER
			throw new Exception(mess);
		}

		int commonBlockHeight = common.getHeight(dcSet);

		if (cnt.isOnStopping())
			throw new Exception("on stoping");

		LOGGER.info("Synchronizing from COMMON blockHeight " + commonBlockHeight);
		
		//CHECK COMMON BLOCK EXISTS
		if(Arrays.equals(common.getSignature(), lastBlockSignature))
		{
			
			if (signatures.b.size() == 0) {
				// TODO it is because incorrect calculate WIN_TARGET value
				//dcSet.getBlockSignsMap().setFullWeight(Controller.getInstance().getPeerHWeights().get(peer).b);
				Tuple2<Integer, Long> myHW = Controller.getInstance().getMyHWeight(false);
				Controller.getInstance().setWeightOfPeer(peer, myHW);
				LOGGER.info("  set new Weight " + myHW + " for PEER " + peer.getAddress().getHostAddress());
				return;
			}
			// CONNON BLOCK is my LAST BLOCK in CHAIN
			
			//CREATE BLOCK BUFFER
			LOGGER.debug("synchronize try get BLOCKS in BUFFER"
					+ " peer: " + peer.getAddress().getHostName()
					+ " for blocks: " + signatures.b.size());
			BlockBuffer blockBuffer = new BlockBuffer(signatures.b, peer);

			//GET AND PROCESS BLOCK BY BLOCK
			for(byte[] signature: signatures.b)
			{
				if (cnt.isOnStopping()) {
					//STOP BLOCKBUFFER
					blockBuffer.stopThread();
					throw new Exception("on stoping");
				}
				
				//GET BLOCK
				///LOGGER.debug("synchronize try get BLOCK"
				///		+ " for: " + signature.toString()
				///		);

				Block blockFromPeer = blockBuffer.getBlock(signature);

				///LOGGER.debug("synchronize BLOCK getted"
				///		);

				if (cnt.isOnStopping()) {
					//STOP BLOCKBUFFER
					blockBuffer.stopThread();
					throw new Exception("on stoping");
				}
				
				if (blockFromPeer == null) { // icreator
					
					//INVALID BLOCK THROW EXCEPTION
					String mess = "Dishonest peer on block null";
					peer.ban(BAN_BLOCK_TIMES>>4, mess);
					//STOP BLOCKBUFFER
					blockBuffer.stopThread();
					throw new Exception(mess);
				}
				blockFromPeer.setCalcGeneratingBalance(dcSet); // NEED SET it

				if (cnt.isOnStopping()) {
					//STOP BLOCKBUFFER
					blockBuffer.stopThread();
					throw new Exception("on stoping");
				}
				
				//PROCESS BLOCK
				///LOGGER.debug("synchronize - simple ADD NEW BLOCK process..."
				///		+ " height:" + blockFromPeer.getHeightByParent(dcSet));
				
				if(blockFromPeer.isSignatureValid() && blockFromPeer.isValid(dcSet))
				{
					try {
						this.pipeProcessOrOrphan(dcSet, blockFromPeer, false);
					} catch (Exception e) {	
						if (cnt.isOnStopping()) {
							//STOP BLOCKBUFFER
							blockBuffer.stopThread();
							throw new Exception("on stoping");
						} else {
							LOGGER.error(e.getMessage(),e);
						}
					}
				} else {

					//INVALID BLOCK THROW EXCEPTION
					String mess = "Dishonest peer on block " + blockFromPeer.getHeight(dcSet);
					peer.ban(BAN_BLOCK_TIMES>>1, mess);
					//STOP BLOCKBUFFER
					blockBuffer.stopThread();
					throw new Exception(mess);
				}
				
				///LOGGER.debug("synchronize BLOCK END process"
				///		);

			}

			//STOP BLOCKBUFFER
			blockBuffer.stopThread();
		}
		else
		{
			
			//GET THE BLOCKS FROM SIGNATURES
			List<Block> blocks = this.getBlocks(dcSet, signatures.b, peer);

			if (cnt.isOnStopping()) {
				throw new Exception("on stoping");
			}

			//SYNCHRONIZE BLOCKS
			/*
			LOGGER.debug("core.Synchronizer.synchronize from common block for blocks: " + blocks.size());
			*/
			
			List<Transaction> orphanedTransactions = this.synchronize_blocks(dcSet, common, blocks, peer);
			if (cnt.isOnStopping()) {
				throw new Exception("on stoping");
			}

			//SEND ORPHANED TRANSACTIONS TO PEER
			TransactionMap map = dcSet.getTransactionMap();
			for(Transaction transaction: orphanedTransactions)
			{
				if (cnt.isOnStopping()) {
					throw new Exception("on stoping");
				}

				byte[] sign = transaction.getSignature();
				if (!map.contains(sign))
					map.set(sign, transaction);
			}
		}
		
		fromPeer = null;
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
			// cannot retrieve headers
			peer.ban(3, "Cannot retrieve headers");
			throw new Exception("Failed to communicate with peer (retrieve headers) - response = null");
		}
		
		List<byte[]> signatures = response.getSignatures();
		if (signatures.size() == 0) {
			//peer.ban(20, "result HEADERS is EMPTY");
			//throw new Exception("result HEADERS is EMPTY");			
		} else {
			// remove my FINDED HEADER
			signatures.remove(0);
		}

		return signatures;
	}
	
	private Tuple2<byte[], List<byte[]>> findHeaders(Peer peer, int peerHeight, byte[] lastBlockSignature, int checkPointHeight) throws Exception
	{

		DCSet dcSet = DCSet.getInstance();
		Controller cnt = Controller.getInstance();

		LOGGER.debug("findHeaders(Peer: " + peer.getAddress().getHostAddress()
				+ ", peerHeight: " + peerHeight
				+ ", checkPointHeight: " + checkPointHeight);

		List<byte[]> headers = this.getBlockSignatures(lastBlockSignature, peer);

		LOGGER.debug("findHeaders(Peer) headers.size: " + headers.size());

		if (headers.size() > 0) {
			// end of my CHAIN is common
			return new Tuple2<byte[], List<byte[]>>(lastBlockSignature, headers);
		}

		
		//int myChainHeight = Controller.getInstance().getBlockChain().getHeight();
		int maxChainHeight = dcSet.getBlockSignsMap().getHeight(lastBlockSignature);
		if (maxChainHeight < checkPointHeight) {
			String mess = "Dishonest peer: my checkPointHeight[" + checkPointHeight
					+ "\n -> not found";
			peer.ban(BAN_BLOCK_TIMES<<1, mess);
			throw new Exception(mess);
		}

		LOGGER.debug("findHeaders "
				+ " maxChainHeight: " + maxChainHeight
				+ " to minHeight: " + checkPointHeight);

		// try get check point block from peer
		// GENESIS block nake ERROR in network.Peer.sendMessage(Message) -> this.out.write(message.toBytes());
		// TODO fix it error
		byte[] checkPointHeightSignature;
		Block checkPointHeightCommonBlock = null;
		if (checkPointHeight == 1) {
			checkPointHeightCommonBlock = cnt.getBlockChain().getGenesisBlock().getChild(dcSet);
			if (checkPointHeightCommonBlock == null) {
				return new Tuple2<byte[], List<byte[]>>(lastBlockSignature, headers);				
			}
			checkPointHeightSignature = checkPointHeightCommonBlock.getSignature();
		} else {
			checkPointHeightSignature = dcSet.getBlockMap().getSignByHeight(checkPointHeight);
		}
		
		try {
			// try get common block from PEER
			checkPointHeightCommonBlock = getBlock(checkPointHeightSignature, peer, true);
		} catch (Exception e) {
			String mess = "in getBlock:\n" + e.getMessage() + "\n *** in Peer: " + peer.getAddress().getHostAddress();
			//// banned in getBlock -- peer.ban(BAN_BLOCK_TIMES>>3, mess);
			throw new Exception(mess);
		}

		if (checkPointHeightCommonBlock == null
				|| checkPointHeightSignature == null) {
			String mess = "Dishonest peer: my block[" + checkPointHeight
					+ "\n -> common BLOCK not found";
			peer.ban(BAN_BLOCK_TIMES>>1, mess);

			throw new Exception(mess);
		}

		//GET HEADERS UNTIL COMMON BLOCK IS FOUND OR ALL BLOCKS HAVE BEEN CHECKED
		//int steep = BlockChain.SYNCHRONIZE_PACKET>>2;
		int steep = 4;
		byte[] lastBlockSignatureCommon;
		do {
			if (cnt.isOnStopping()) {
				throw new Exception("on stoping");
			}

			maxChainHeight -= steep;
			
			if (maxChainHeight < checkPointHeight) {
				maxChainHeight = checkPointHeight;
				lastBlockSignatureCommon = checkPointHeightCommonBlock.getSignature();
			} else {
				lastBlockSignatureCommon = dcSet.getBlockMap().getSignByHeight(maxChainHeight);				
			}

			LOGGER.debug("findHeaders try found COMMON header"
					+ " steep: " + steep
					+ " maxChainHeight: " + maxChainHeight);

			headers = this.getBlockSignatures(lastBlockSignatureCommon, peer);

			LOGGER.debug("findHeaders try found COMMON header"
					+ " founded headers: " + headers.size()
					);

			if (headers.size() > 0) {
				if (maxChainHeight == checkPointHeight) {
					String mess = "Dishonest peer by headers.size==0 " + peer.getAddress().getHostAddress();				
					peer.ban(BAN_BLOCK_TIMES, mess);
					throw new Exception(mess);
				}
				break;
			}

			if (steep < 1000)
				steep <<= 1;
			
		} while ( maxChainHeight > checkPointHeight && headers.isEmpty());

		LOGGER.debug("findHeaders AFTER try found COMMON header"
				+ " founded headers: " + headers.size()
				);

		// CLEAR head of common headers
		while ( !headers.isEmpty() && dcSet.getBlockMap().contains(headers.get(0))) {
			lastBlockSignatureCommon = headers.remove(0);
		}

		if (false && headers.isEmpty()) {
			String mess = "Dishonest peer by headers.size==0 " + peer.getAddress().getHostAddress();
			
			peer.ban(BAN_BLOCK_TIMES>>1, mess);
			throw new Exception(mess);
		}

		LOGGER.debug("findHeaders headers CLEAR"
				+ "now headers: " + headers.size()
				);
		
		return new Tuple2<byte[], List<byte[]>>(lastBlockSignatureCommon, headers);
	}

	private List<Block> getBlocks(DCSet dcSet, List<byte[]> signatures, Peer peer) throws Exception {
		
		List<Block> blocks = new ArrayList<Block>();
		Controller cnt = Controller.getInstance();
		
		for(byte[] signature: signatures)
		{
			if (cnt.isOnStopping()) {
				throw new Exception("on stoping");
			}

			//ADD TO LIST
			Block block = getBlock(signature, peer, false);
			// NOW generating balance not was send by NET
			// need to SET it!
			block.setCalcGeneratingBalance(dcSet);

			blocks.add(block);	
		}
		
		return blocks;
	}
	
	// chack = true - check this signature in peer
	public static Block getBlock(byte[] signature, Peer peer, boolean check) throws Exception
	{
		
		//CREATE MESSAGE
		Message message = MessageFactory.getInstance().createGetBlockMessage(signature);
		
		//SEND MESSAGE TO PEER
		BlockMessage response = (BlockMessage) peer.getResponse(message);
		
		//CHECK IF WE GOT RESPONSE
		if(response == null)
		{
			if (check) {
				return null;
			} else {
				//ERROR
				throw new Exception("Peer timed out");
			}
		}
		
		Block block = response.getBlock();
		if(block == null)
		{
			int banTime = BAN_BLOCK_TIMES>>2;
			String mess = "*** Dishonest peer - Block is NULL. Ban for " + banTime;
			peer.ban(banTime, mess);
			throw new Exception(mess);
		}
		
		//CHECK BLOCK SIGNATURE
		if(!block.isSignatureValid())
		{
			int banTime = BAN_BLOCK_TIMES;
			String mess = "*** Dishonest peer - Invalid block --signature. Ban for " + banTime;
			peer.ban(banTime, mess);
			throw new Exception(mess);
		}
		
		block.makeTransactionsHash();
		//ADD TO LIST
		return block;
	}
	
	
	//SYNCHRONIZED DO NOT PROCCESS A BLOCK AT THE SAME TIME
	//SYNCHRONIZED MIGHT HAVE BEEN PROCESSING PREVIOUS BLOCK
	public synchronized void pipeProcessOrOrphan(DCSet dcSet, Block block, boolean doOrphan) throws Exception
	{
		Controller cnt = Controller.getInstance();
		
		//CHECK IF WE ARE STILL PROCESSING BLOCKS
		if (cnt.isOnStopping()) {
			throw new Exception("on stoping");
		}

		dcSet.getBlockMap().setProcessing(true);
		int count = block.getTransactionCount();
		if (count < 100)
			count = 100;

		if(doOrphan)
		{

			try {
				block.orphan(dcSet);
				dcSet.getBlockMap().setProcessing(false);
				dcSet.flush(block);
				
				if(Controller.getInstance().isOnStopping())
					throw new Exception("on stoping");
				
				// NOTIFY to WALLET
				if (!BlockChain.HARD_WORK && cnt.doesWalletExists() && cnt.useGui)
					dcSet.getBlockMap().notifyOrphanChain(block);

			} catch (Exception e) {
				
				dcSet.rollback();
				
				if (cnt.isOnStopping()) {
					dcSet.getBlockMap().setProcessing(false);
					throw new Exception("on stoping");
				} else {
					throw new Exception(e);					
				}
			}
			
		} else {
			
			//PROCESS
			try {
				block.process(dcSet);
				dcSet.getBlockMap().setProcessing(false);
				dcSet.flush(block);

				if(Controller.getInstance().isOnStopping())
					throw new Exception("on stoping");

				// NOTIFY to WALLET
				if (!BlockChain.HARD_WORK && cnt.doesWalletExists() && cnt.useGui)
					dcSet.getBlockMap().notifyProcessChain(block);
				
			} catch (Exception e) {

				dcSet.rollback();
				
				if (cnt.isOnStopping()) {
					dcSet.getBlockMap().setProcessing(false);
					throw new Exception("on stoping");
				} else {
					throw new Exception(e);					
				}
			}
				
		}
	}

	
	public void stop() {
		
		//this.run = false;
		//if (runedBlock != null)
		//	runedBlock.stop();
		
		//this.pipeProcessOrOrphan(DBSet.getInstance(), null, false);
	}
}
