package core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import core.block.Block;
import settings.Settings;
import network.Peer;
import network.message.BlockMessage;
import network.message.Message;
import network.message.MessageFactory;

public class BlockBuffer extends Thread
{
	private static final int BUFFER_SIZE = 20;
	
	private List<byte[]> signatures;
	private Peer peer;
	private int counter;
	private boolean error;
	private Map<byte[], BlockingQueue<Block>> blocks;
	
	private boolean run = true;

	private static final Logger LOGGER = Logger.getLogger(BlockBuffer.class);
	
	public BlockBuffer(List<byte[]> signatures, Peer peer)
	{
		this.signatures = signatures;
		this.peer = peer;
		this.counter = 0;
		this.error = false;
		this.setName("Thread BlockBuffer - "+ this.getId());
		this.blocks = new HashMap<byte[], BlockingQueue<Block>>();
		this.start();
	}
	
	public void run() 
	{
		while(this.run)
		{
			for(int i=0; i<this.signatures.size() && i<this.counter + BUFFER_SIZE; i++)
			{
				byte[] signature = this.signatures.get(i);
				
				//CHECK IF WE HAVE ALREADY LOADED THIS BLOCK
				if(!this.blocks.containsKey(signature))
				{
					//LOAD BLOCK
					this.loadBlock(signature);
				}
			}
			
			try 
			{
				Thread.sleep(10);
			} 
			catch (InterruptedException e) 
			{
				//ERROR SLEEPING
			}
		}
	}
	
	private void loadBlock(final byte[] signature)
	{
		//CREATE QUEUE
		final BlockingQueue<Block> blockingQueue = new ArrayBlockingQueue<Block>(1);
		this.blocks.put(signature, blockingQueue);
		
		//LOAD BLOCK IN THREAD
		new Thread(){
			public void run()
			{
				//CREATE MESSAGE
				Message message = MessageFactory.getInstance().createGetBlockMessage(signature);
				
				//SEND MESSAGE TO PEER
				BlockMessage response = (BlockMessage) peer.getResponse(message, Synchronizer.GET_BLOCK_TIMEOUT);
				
				//CHECK IF WE GOT RESPONSE
				if(response == null)
				{
					//ERROR
					error = true;
					return;
				}
				
				Block block = response.getBlock();
				//CHECK BLOCK SIGNATURE
				if(block == null || !block.isSignatureValid())
				{
					error = true;
					return;
				}
				
				//ADD TO LIST
				blockingQueue.add(block);
				LOGGER.debug("block BUFFER added with RECS:" + block.getTransactionCount());

			}
		}.start();
	}
	
	public Block getBlock(byte[] signature) throws Exception
	{
		//CHECK ERROR
		if(this.error)
		{
			throw new Exception("Block buffer error");
		}
		
		//UPDATE COUNTER
		this.counter = this.signatures.indexOf(signature);
		
		//CHECK IF ALREADY LOADED BLOCK
		if(!this.blocks.containsKey(signature))
		{
			//LOAD BLOCK
			this.loadBlock(signature);
		}
		
		//GET BLOCK
		if (this.error) {
			throw new Exception("Block buffer error");
		}
		
		Block block = this.blocks.get(signature).poll(Synchronizer.GET_BLOCK_TIMEOUT, TimeUnit.MILLISECONDS);
		if (block == null) {
			throw new Exception("Block buffer error");			
		}
		
		return block;
	}
	
	public void clearBlock(byte[] signature) {
		
		if(this.blocks.containsKey(signature))
		{
			this.blocks.remove(signature);
		}

	}

	public void stopThread()
	{
		try
		{
			this.run = false;

			this.join();
			
		}
		catch (InterruptedException e) 
		{
			//INTERRUPTED
		}
	}
	
}
