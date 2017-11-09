package network;
// 30/03
import org.apache.log4j.Logger;

import controller.Controller;
import database.DBSet;
import datachain.DCSet;
import network.message.Message;
import network.message.MessageFactory;
import settings.Settings;

public class Pinger extends Thread
{
	
	private static final Logger LOGGER = Logger.getLogger(Pinger.class);
	private Peer peer;
	//private boolean run;
	private long ping;
	
	public Pinger(Peer peer)
	{
		this.peer = peer;
		//this.run = true;
		this.ping = Long.MAX_VALUE;
		
		this.start();
	}
	
	public long getPing()
	{
		return this.ping;
	}

	public void setPing(long ping)
	{		
		this.ping = ping;
	}

	/*
	public boolean isRun()
	{
		return this.run;
	}
	*/
	
	public void run()
	{
	
		while(true)
		{

			//SLEEP
			try 
			{
				Thread.sleep(100000 + Settings.getInstance().getPingInterval());
			} 
			catch (InterruptedException e)
			{
				//FAILED TO SLEEP
			}

			if(!this.peer.isUsed()) {
				continue;
			}

			this.peer.addPingCounter();

			//CREATE PING
			Message pingMessage = MessageFactory.getInstance().createPingMessage();
						
			//GET RESPONSE
			long start = System.currentTimeMillis();
			Message response = this.peer.getResponse(pingMessage);

			//CHECK IF VALID PING
			if(response == null || response.getType() != Message.PING_TYPE)
			{
				//PING FAILES
				this.peer.onPingFail(response == null?"response == null": "response.getType() != Message.PING_TYPE" );
				try {
					Thread.sleep(30000);
				}
				catch (Exception e) {		
				}
				continue;
			}

			try
			{

				//UPDATE PING
				this.ping = System.currentTimeMillis() - start;
								
				if(!Controller.getInstance().isOnStopping()){
					Controller.getInstance().getDBSet().getPeerMap().addPeer(this.peer, 0);
				}
			}
			catch(Exception e)
			{
				//PING FAILES
				this.peer.onPingFail(e.getMessage());
				try {
					Thread.sleep(30000);
				}
				catch (Exception e1) {		
				}
				continue;
				
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
