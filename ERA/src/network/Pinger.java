package network;
// 30/03
import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.BlockChain;
import database.DBSet;
import datachain.DCSet;
import network.message.HWeightMessage;
import network.message.Message;
import network.message.MessageFactory;
import settings.Settings;

public class Pinger extends Thread
{
	
	private static final Logger LOGGER = Logger.getLogger(Pinger.class);
	private static final int DEFAULT_QUICK_PING_TIMEOUT = BlockChain.GENERATING_MIN_BLOCK_TIME_MS>>3; 

	private Peer peer;
	private boolean needPing = false;
	//private boolean run;
	private int ping;
	private Message messageWinBlock;
	private Message messageQueue;
	private Message messageQueuePing;
	
	public Pinger(Peer peer)
	{
		this.peer = peer;
		//this.run = true;
		this.ping = Integer.MAX_VALUE;
		this.setName("Thread Pinger - "+ this.getId());
		this.start();
	}
	
	public long getPing()
	{
		return this.ping;
	}

	public void setPing(int ping)
	{		
		this.ping = ping;
	}

	public void setNeedPing()
	{		
		this.needPing = true;
	}

	public void setMessageQueue(Message message) {
		this.messageQueue = message;
	}

	public void setMessageWinBlock(Message message) {
		this.messageWinBlock = message;
	}

	public void setMessageQueuePing(Message message) {
		this.messageQueuePing = message;
	}
	
	/*
	public boolean isRun()
	{
		return this.run;
	}
	*/
	
	public boolean tryPing(long timeSOT) {
		
		//LOGGER.info("try PING " + this.peer.getAddress());

		peer.addPingCounter();

		//CREATE PING
		//Message pingMessage = MessageFactory.getInstance().createPingMessage();
		// TODO remove it and set HWeigtn response
		// TODO make wait SoTome 10 when ping
		Message pingMessage = MessageFactory.getInstance().createGetHWeightMessage();
					
		//GET RESPONSE
		long start = System.currentTimeMillis();
		Message response = peer.getResponse(pingMessage, timeSOT);

		if(Controller.getInstance().isOnStopping()) {
			return false;
		}

		//CHECK IF VALID PING
		if(response == null)
		{
			
			//UPDATE PING
			if (this.ping < 0)
				this.ping -= 1;
			else
				this.ping = -1;

			if (!this.peer.isUsed())
				return false;

			//PING FAILES
			if (this.ping < -2) {
				this.peer.ban(3, "on PING FAILES");
			}

		} else {
			//UPDATE PING
			this.ping = (int)(System.currentTimeMillis() - start);
		}
						
		//LOGGER.info("PING " + this.peer.getAddress() + " @ms " + this.ping);
		Controller.getInstance().getDBSet().getPeerMap().addPeer(peer, 0);
		
		if (response != null && response.getType() == Message.HWEIGHT_TYPE) {
			HWeightMessage hWeightMessage = (HWeightMessage) response;
			Tuple2<Integer, Long> hW = hWeightMessage.getHWeight();

			Controller.getInstance().setWeightOfPeer(peer, hW);
		}
		
		return ping >= 0;

	}
	
	public boolean tryPing() {
		return tryPing(60000l);
	}
	
	public void run()
	{
	
		Controller cnt = Controller.getInstance();
		BlockChain chain = cnt.getBlockChain();

		int sleepTimeFull = Settings.getInstance().getPingInterval();
		int sleepTimeSteep = 10;
		int sleepSteeps = sleepTimeFull / sleepTimeSteep + 10;
		int sleepStepTimeCounter;
		long pingOld = 100;
		boolean resultSend;
		while(true)
		{

			if (this.ping < 0) {
				sleepStepTimeCounter = 10000;
			} else if (this.ping > 10000) {
				sleepStepTimeCounter = 30000;				
			} else {
				sleepStepTimeCounter = sleepSteeps;				
			}
		
			while (sleepStepTimeCounter-- > 0) {
				
				//SLEEP
				try 
				{
					Thread.sleep(sleepTimeSteep);
				} 
				catch (InterruptedException e)
				{
					//FAILED TO SLEEP
				}

				if (!this.peer.isUsed()) {
					try 
					{
						Thread.sleep(500);
					} 
					catch (InterruptedException e)
					{
						//FAILED TO SLEEP
					}
					continue;
				}
								
				if (messageQueue != null) {
					//LOGGER.debug("try ASYNC sendMessage " + messageQueue.viewType() + " - " + this.peer.getAddress());

					resultSend = this.peer.sendMessage(messageQueue);
					messageQueue = null;

					if (!resultSend)
						continue;
					
					//LOGGER.debug("try ASYNC send " + messageQueue.viewType() + " " + this.peer.getAddress() + " @ms " + (System.currentTimeMillis() - start));

				}

				if (messageWinBlock != null) {
					//LOGGER.debug("try ASYNC send WINblock " + messageQueue.viewType() + " - " + this.peer.getAddress());

					resultSend = this.peer.sendMessage(messageWinBlock);
					messageWinBlock = null;

					if (!resultSend)
						continue;
					
					//LOGGER.debug("try ASYNC send WINblock " + messageQueue.viewType() + " " + this.peer.getAddress() + " @ms " + (System.currentTimeMillis() - start));

				}

				pingOld = this.ping;

				if (this.messageQueuePing != null) {
					// PING before and THEN send
					//LOGGER.debug("try ASYNC PING sendMessage " + messageQueuePing.viewType() + " - " + this.peer.getAddress());
					///this.peer.so(message);
					
					this.tryPing(DEFAULT_QUICK_PING_TIMEOUT);
					this.peer.sendMessage(this.messageQueuePing);

					this.needPing = false;
					this.messageQueuePing = null;
					sleepStepTimeCounter = sleepSteeps;
					continue;
					
				} else if(this.needPing) {
					// PING NOW
					this.needPing = false;
					tryPing();
					pingOld = this.ping;
					sleepStepTimeCounter = sleepSteeps;
					continue;
				}
				
			}
			
			if(this.peer.isUsed()) {
				tryPing();
				pingOld = this.ping;
			}
			
		}
	}

	/*
	public void stopPing() 
	{
		try
		{
			this.run = false;
			this.goInterrupt();
			this.join();
		}
		catch(Exception e)
		{
			LOGGER.debug(e.getMessage(), e);
		}
		
		try {
			this.wait();
		} catch(Exception e) {
			
		}
	}
	 */
	
	// icreator - wair is DB is busy
	// https://github.com/jankotek/mapdb/search?q=ClosedByInterruptException&type=Issues&utf8=%E2%9C%93
	//
	public void goInterrupt_old()
	{

		DCSet dcSet = DCSet.getInstance(); 
		//int i =0;
		while(dcSet.getBlockMap().isProcessing() || dcSet.isBusy() ) {
			try {
				LOGGER.info(" pinger.goInterrupt wait DB : " + this.peer.getAddress());
				Thread.sleep(50);
			}
			catch (Exception e) {		
			}
			/*
			i++;
			if (i > 20) 
				break;
				*/

		}
		this.interrupt();
	}
}
