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

	public static final int GET_BLOCK_TIMEOUT = 60000;
	private static final int BYTES_MAX_GET = 1024<<12;
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
	
		Controller cnt = Controller.getInstance();
		//int originalHeight = 0;
		
		//ORPHAN BLOCK IN FORK TO VALIDATE THE NEW BLOCKS
		if(lastCommonBlock != null)
		{
	
			//GET LAST BLOCK
			Block lastBlock = fork.getBlockMap().getLastBlock();
			
			int lastHeight = lastBlock.getHeight(fork);
			LOGGER.debug("*** core.Synchronizer.checkNewBlocks - lastBlock["
					+ lastHeight + "]\n"
					+ " newBlocks.size = " + newBlocks.size()
					+ "\n search common block in FORK"
					+ " in mainDB: " + lastBlock.getHeight(fork.getParent())
					+ " in ForkDB: " + lastBlock.getHeight(fork)
					+ "\n for lastCommonBlock = " + lastCommonBlock.getHeight(fork));

			byte[] lastCommonBlockSignature = lastCommonBlock.getSignature();
			//ORPHAN LAST BLOCK UNTIL WE HAVE REACHED COMMON BLOCK
			while(!Arrays.equals(lastBlock.getSignature(), lastCommonBlockSignature))
			{
				LOGGER.debug("*** ORPHAN LAST BLOCK UNTIL WE HAVE REACHED COMMON BLOCK [" + lastBlock.getHeightByParent(fork) + "]");
				if (cnt.getBlockChain().getCheckPoint(fork) > lastBlock.getHeightByParent(fork)
						|| lastBlock.getVersion() == 0) {
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
				lastBlock = fork.getBlockMap().get(lastBlock.getReference());
			}

			LOGGER.debug("*** core.Synchronizer.checkNewBlocks - lastBlock[" + lastHeight + "]");
	
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

				// RELEASE MEMORY in FORK DB
				if(false) fork.getBlockMap().wipe(block);
			}
			else
			{

				//block.isSignatureValid();
				//block.isValid(fork);
				
				//INVALID BLOCK THROW EXCEPTION
				String mess = "Dishonest peer by not is Valid block, heigh: " + heigh;
				peer.ban(BAN_BLOCK_TIMES, mess);
				throw new Exception(mess);
			}
		}
		
		LOGGER.debug("*** core.Synchronizer.checkNewBlocks - END");

	}

	// process new BLOCKS to DB and orphan DB
	public List<Transaction> synchronize_blocks(DCSet dcSet, Block lastCommonBlock, List<Block> newBlocks, Peer peer) throws Exception
	{
		TreeMap<String, Transaction> orphanedTransactions = new TreeMap<String, Transaction>();
		Controller cnt = Controller.getInstance();

		//VERIFY ALL BLOCKS TO PREVENT ORPHANING INCORRECTLY
		if (core.BlockGenerator.TEST_001) {
			///checkNewBlocks(dcSet.forkinFile(), lastCommonBlock, newBlocks, peer);
			checkNewBlocks(dcSet.fork(), lastCommonBlock, newBlocks, peer);
		} else {
			checkNewBlocks(dcSet.fork(), lastCommonBlock, newBlocks, peer);
		}
		
		//NEW BLOCKS ARE ALL VALID SO WE CAN ORPHAN THEM FOR REAL NOW
		Map<String, byte[]> states = new TreeMap<String, byte[]>();

		if(lastCommonBlock != null)
		{

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
				this.pipeProcessOrOrphan(dcSet, lastBlock, true, false);
				///kjhjk
				lastBlock = dcSet.getBlockMap().getLastBlock();
			}

		}
		
		//PROCESS THE NEW BLOCKS
		LOGGER.debug("*** synchronize PROCESS NEW blocks.size:" + newBlocks.size());
		for(Block block: newBlocks)
		{

			if (cnt.isOnStopping())
				throw new Exception("on stoping");

			//SYNCHRONIZED PROCESSING
			LOGGER.debug("*** begin PIPE");
			this.pipeProcessOrOrphan(dcSet, block, false, false);

			LOGGER.debug("*** begin REMOVE orphanedTransactions");
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
		LOGGER.error("Synchronizing from peer: " + peer.toString() + ":"
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
		Tuple2<byte[], List<byte[]>> headers = this.findHeaders(peer, peerHeight, lastBlockSignature, checkPointHeight);
		List<byte[]> signatures = headers.b;

		//FIND FIRST COMMON BLOCK in HEADERS CHAIN
		Block common = dcSet.getBlockMap().get(headers.a);
		if (common == null) {
			
			//INVALID BLOCK THROW EXCEPTION
			String mess = "Dishonest peer on COMMON block not found!!";
			peer.ban(BAN_BLOCK_TIMES>>4, mess);
			//STOP BLOCKBUFFER
			throw new Exception(mess);
		} else {
			
		}

		int commonBlockHeight = common.getHeight(dcSet);

		if (cnt.isOnStopping())
			throw new Exception("on stoping");

		LOGGER.info("Synchronizing from COMMON blockHeight " + commonBlockHeight);
		
		//CHECK COMMON BLOCK EXISTS
		if(Arrays.equals(common.getSignature(), lastBlockSignature))
		{
			
			if (signatures.size() == 0) {
				// TODO it is because incorrect calculate WIN_TARGET value
				//dcSet.getBlockSignsMap().setFullWeight(Controller.getInstance().getPeerHWeights().get(peer).b);
				Tuple2<Integer, Long> myHW = Controller.getInstance().getBlockChain().getHWeightFull(dcSet);
				Controller.getInstance().setWeightOfPeer(peer, myHW);
				LOGGER.info("  set new Weight " + myHW + " for PEER " + peer.getAddress().getHostAddress());
				return;
			}
			
			// CONNON BLOCK is my LAST BLOCK in CHAIN
			signatures.remove(0);
			
			//CREATE BLOCK BUFFER
			LOGGER.debug("START BUFFER"
					+ " peer: " + peer.getAddress().getHostName()
					+ " for blocks: " + signatures.size());
			BlockBuffer blockBuffer = new BlockBuffer(signatures, peer);
			Block blockFromPeer;

			String errorMess = null;
			//GET AND PROCESS BLOCK BY BLOCK
			for(byte[] signature: signatures)
			{
				if (cnt.isOnStopping()) {
					//STOP BLOCKBUFFER
					blockBuffer.stopThread();
					throw new Exception("on stoping");
				}
				
				//GET BLOCK
				LOGGER.debug("try get BLOCK from BUFFER");

				long time1 = System.currentTimeMillis();
				blockFromPeer = blockBuffer.getBlock(signature);
				
				if (blockFromPeer == null) {
					
					//STOP BLOCKBUFFER
					blockBuffer.stopThread();

					if (true) break;
					
					//INVALID BLOCK THROW EXCEPTION
					String mess = "Dishonest peer on block null";
					peer.ban(BAN_BLOCK_TIMES>>4, mess);
					throw new Exception(mess);
				}

				LOGGER.debug("BLOCK getted "
						+ " time ms: " + (System.currentTimeMillis() - time1)
						+ " size kB: " + (blockFromPeer.getDataLength(false)/1000 )
						+ " from " + peer.getAddress().getHostAddress()
						);

				if (cnt.isOnStopping()) {
					//STOP BLOCKBUFFER
					blockBuffer.stopThread();
					throw new Exception("on stoping");
				}
				
				blockFromPeer.setCalcGeneratingBalance(dcSet); // NEED SET it

				if (cnt.isOnStopping()) {
					//STOP BLOCKBUFFER
					blockBuffer.stopThread();
					throw new Exception("on stoping");
				}
								
				if(!blockFromPeer.isSignatureValid()) {
					errorMess = "invalid Sign!";
					break;
				}
				
				if (blockFromPeer.getTimestamp(dcSet) + (BlockChain.WIN_BLOCK_BROADCAST_WAIT_MS>>2) > NTP.getTime()) {
					errorMess = "invalid Timestamp from FUTURE";
					break;
				}
				
				if (!blockFromPeer.isValid(dcSet)) {
					errorMess = "invalid Transactions";
					break;
				}

				try {
					//PROCESS BLOCK
		
					LOGGER.debug("try pipeProcessOrOrphan");
					this.pipeProcessOrOrphan(dcSet, blockFromPeer, false, false);
					LOGGER.debug("synchronize BLOCK END process");
					blockBuffer.clearBlock(blockFromPeer.getSignature());
					LOGGER.debug("synchronize clear from BLOCK BUFFER");
					continue;
					
				} catch (Exception e) {	
					
					//STOP BLOCKBUFFER
					blockBuffer.stopThread();

					if (cnt.isOnStopping()) {
						throw new Exception("on stoping");
					} else {
						throw new Exception(e);
					}
				}
			}

			//STOP BLOCKBUFFER
			blockBuffer.stopThread();

			if (errorMess != null) {
				//INVALID BLOCK THROW EXCEPTION
				String mess = "Dishonest peer on block " + errorMess;
				peer.ban(BAN_BLOCK_TIMES>>2, mess);
				throw new Exception(mess);
			}

		}
		else
		{
			
			//GET THE BLOCKS FROM SIGNATURES
			List<Block> blocks = this.getBlocks(dcSet, signatures, peer);

			if (cnt.isOnStopping()) {
				throw new Exception("on stoping");
			}

			//SYNCHRONIZE BLOCKS
			/*
			LOGGER.error("core.Synchronizer.synchronize from common block for blocks: " + blocks.size());
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
		SignaturesMessage response;
		try {
			response = (SignaturesMessage) peer.getResponse(message, 10000);
		} catch (java.lang.ClassCastException e) {
			peer.ban(1, "Cannot retrieve headers");
			throw new Exception("Failed to communicate with peer (retrieve headers) - response = null");			
		}

		if (response == null) {
			// cannot retrieve headers
			peer.ban(5, "Cannot retrieve headers");
			throw new Exception("Failed to communicate with peer (retrieve headers) - response = null");
		}

		return response.getSignatures();
	}
	
	private Tuple2<byte[], List<byte[]>> findHeaders(Peer peer, int peerHeight, byte[] lastBlockSignature, int checkPointHeight) throws Exception
	{

		DCSet dcSet = DCSet.getInstance();
		Controller cnt = Controller.getInstance();

		LOGGER.info("findHeaders(Peer: " + peer.getAddress().getHostAddress()
				+ ", peerHeight: " + peerHeight
				+ ", checkPointHeight: " + checkPointHeight);

		List<byte[]> headers = this.getBlockSignatures(lastBlockSignature, peer);

		LOGGER.info("findHeaders(Peer) headers.size: " + headers.size());

		if (headers.size() > 0) {
			// end of my CHAIN is common
			return new Tuple2<byte[], List<byte[]>>(lastBlockSignature, headers);
		} else {
			//String mess = "Dishonest peer: headers.size == 1";
			//peer.ban(BAN_BLOCK_TIMES>>2, mess);
			//throw new Exception(mess);			
		}

		
		//int myChainHeight = Controller.getInstance().getBlockChain().getHeight();
		int maxChainHeight = dcSet.getBlockSignsMap().getHeight(lastBlockSignature);
		if (maxChainHeight < checkPointHeight) {
			String mess = "Dishonest peer: my checkPointHeight[" + checkPointHeight
					+ "\n -> not found";
			peer.ban(BAN_BLOCK_TIMES<<1, mess);
			throw new Exception(mess);
		}

		LOGGER.info("findHeaders "
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
			checkPointHeightSignature = dcSet.getBlockHeightsMap().getSignByHeight(checkPointHeight);
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
		int steep = 2;
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
				lastBlockSignatureCommon = dcSet.getBlockHeightsMap().getSignByHeight(maxChainHeight);				
			}

			LOGGER.info("findHeaders try found COMMON header"
					+ " steep: " + steep
					+ " maxChainHeight: " + maxChainHeight);

			headers = this.getBlockSignatures(lastBlockSignatureCommon, peer);

			LOGGER.info("findHeaders try found COMMON header"
					+ " founded headers: " + headers.size()
					);

			if (headers.size() > 1) {
				if (maxChainHeight == checkPointHeight) {
					String mess = "Dishonest peer by headers.size > 1 " + peer.getAddress().getHostAddress();				
					peer.ban(BAN_BLOCK_TIMES, mess);
					throw new Exception(mess);
				}
				break;
			}

			if (steep < 1000)
				steep <<= 1;
			
		} while ( maxChainHeight > checkPointHeight && headers.isEmpty());

		LOGGER.info("findHeaders AFTER try found COMMON header"
				+ " founded headers: " + headers.size()
				);

		// CLEAR head of common headers exclude LAST!
		while ( headers.size() > 1 && dcSet.getBlockMap().contains(headers.get(0))) {
			lastBlockSignatureCommon = headers.remove(0);
		}

		LOGGER.info("findHeaders headers CLEAR"
				+ "now headers: " + headers.size()
				);
		
		return new Tuple2<byte[], List<byte[]>>(lastBlockSignatureCommon, headers);
	}

	private List<Block> getBlocks(DCSet dcSet, List<byte[]> signatures, Peer peer) throws Exception {
		
		LOGGER.debug("try get BLOCKS from common block SIZE:" + signatures.size() + " - " + peer.getAddress());

		List<Block> blocks = new ArrayList<Block>();
		Controller cnt = Controller.getInstance();
		
		int bytesGet = 0;
		boolean checkPeer = true;
		for(byte[] signature: signatures)
		{
			if (cnt.isOnStopping()) {
				throw new Exception("on stoping");
			}

			//ADD TO LIST
			Block block = getBlock(signature, peer, checkPeer);
			if (block == null)
				break;
			
			// NOW generating balance not was send by NET
			// need to SET it!
			block.setCalcGeneratingBalance(dcSet);

			blocks.add(block);
			bytesGet += block.getDataLength(true);
			LOGGER.debug("block added with RECS:" + block.getTransactionCount() + " bytesGet kb: " + bytesGet/1000);
			if (bytesGet > BYTES_MAX_GET) {
				break;
			}
		}
		
		return blocks;
	}
	
	// chack = true - check this signature in peer
	public static Block getBlock(byte[] signature, Peer peer, boolean check) throws Exception
	{
		
		//CREATE MESSAGE
		Message message = MessageFactory.getInstance().createGetBlockMessage(signature);
		
		//SEND MESSAGE TO PEER
		BlockMessage response = (BlockMessage) peer.getResponse(message, GET_BLOCK_TIMEOUT);
		
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
	public synchronized void pipeProcessOrOrphan(DCSet dcSet, Block block, boolean doOrphan, boolean hardFlush) throws Exception
	{
		Controller cnt = Controller.getInstance();
		
		//CHECK IF WE ARE STILL PROCESSING BLOCKS
		if (cnt.isOnStopping()) {
			throw new Exception("on stoping");
		}

		int blockSize = 500 + (block.getDataLength(false))>>(hardFlush?0:2);
		dcSet.getBlockMap().setProcessing(true);

		if(doOrphan)
		{

			try {
				block.orphan(dcSet);
				dcSet.getBlockMap().setProcessing(false);
				dcSet.updateTxCounter(-block.getTransactionCount());
				// FARDFLUSH not use in each case - only after accumulate size
				dcSet.flush(blockSize, false);
				
				if(Controller.getInstance().isOnStopping())
					throw new Exception("on stoping");
				
				// NOTIFY to WALLET
				if (//!BlockChain.HARD_WORK && 
						cnt.doesWalletExists() && cnt.useGui)
					dcSet.getBlockMap().notifyOrphanChain(block);

			} catch (Exception e) {
				
				dcSet.rollback();
				
				if (cnt.isOnStopping()) {
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
				dcSet.updateTxCounter(block.getTransactionCount());
				// FARDFLUSH not use in each case - only after accumulate size
				dcSet.flush(blockSize, false);

				if(Controller.getInstance().isOnStopping())
					throw new Exception("on stoping");

				// NOTIFY to WALLET
				if (//!BlockChain.HARD_WORK && 
						cnt.doesWalletExists() && cnt.useGui)
					dcSet.getBlockMap().notifyProcessChain(block);
				
			} catch (Exception e) {

				if(Controller.getInstance().isOnStopping())
					return;
				
				dcSet.rollback();
				
				if (cnt.isOnStopping()) {
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
