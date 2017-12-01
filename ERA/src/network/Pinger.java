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
	private Peer peer;
	private boolean needPing = false;
	//private boolean run;
	private int ping;
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
			this.ping = -1;
			
			//PING FAILES
			///peer.onPingFail("on Ping fail - @ms " + this.ping);
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
		
		return ping > 0;

	}
	
	public boolean tryPing() {
		return tryPing(10000l);
	}
	
	public void run()
	{
	
		Controller cnt = Controller.getInstance();
		BlockChain chain = cnt.getBlockChain();

		int sleepTimeFull = Settings.getInstance().getPingInterval();
		int sleepTimeSteep = 100;
		int sleepSteeps = sleepTimeFull / sleepTimeSteep;
		int sleepStepTimeCounter;
		long pingOld = 100;
		while(true)
		{

			sleepStepTimeCounter = sleepSteeps;
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
					///this.peer.so(message);
					//long start = System.currentTimeMillis();

					boolean resultSend = this.peer.sendMessage(messageQueue);

					//LOGGER.debug("try ASYNC send " + messageQueue.viewType() + " " + this.peer.getAddress() + " @ms " + (System.currentTimeMillis() - start));

					messageQueue = null;
					continue;
				}

				pingOld = this.ping;

				if (this.messageQueuePing != null) {
					// PING before and THEN send
					//LOGGER.debug("try ASYNC PING sendMessage " + messageQueuePing.viewType() + " - " + this.peer.getAddress());
					///this.peer.so(message);
					
					if (tryPing(2000)) {
						Tuple2<Integer, Long> peerHWeight = cnt.getHWeightOfPeer(this.peer);
						int peerHeight = peerHWeight==null?-1:(int)peerHWeight.a;
						int myHeight = chain.getHeight(DCSet.getInstance());
						if (peerHWeight != null 
								&& peerHeight == myHeight
								//&& peerHeight > myHeight - 2
								) {
							LOGGER.debug("try ASYNC send " + messageQueuePing.viewType() + " " + this.peer.getAddress()	+ " after PING: " + this.ping);
							boolean resultSend = this.peer.sendMessage(this.messageQueuePing);
						}
					} else {
						if (pingOld < 0) {
							peer.onPingFail("on Ping fail - @ms " + this.ping);
						}
							
						//LOGGER.debug("skip ASYNC send " + messageQueuePing.viewType() + " " + this.peer.getAddress()	+ " after PING: " + this.ping);						
					}

					this.needPing = false;
					this.messageQueuePing = null;
					sleepStepTimeCounter = sleepSteeps;
					continue;
					
				} else if(this.needPing) {
					// PING NOW
					this.needPing = false;
					;
					if (!tryPing() && pingOld < 0) {
						peer.onPingFail("on Ping fail - @ms " + this.ping);
					}
					sleepStepTimeCounter = sleepSteeps;
					continue;
				}
				
			}
			
			if(this.peer.isUsed()) {
				if (!tryPing() && pingOld < 0) {
					peer.onPingFail("on Ping fail - @ms " + this.ping);
				}
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
