package network;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
 import org.apache.log4j.Logger;

import controller.Controller;
import database.DBSet;
import lang.Lang;
import network.message.Message;
import network.message.MessageFactory;
import ntp.NTP;
import settings.Settings;

public class Peer extends Thread{

	private InetAddress address;
	private ConnectionCallback callback;
	private Socket socket;
	private OutputStream out;
	private Pinger pinger;
	private boolean white;
	private long pingCounter;
	private long connectionTime;
	private boolean runed;
	private int errors;
	
	private Map<Integer, BlockingQueue<Message>> messages;
	
	static Logger LOGGER = Logger.getLogger(Peer.class.getName());

	public Peer(InetAddress address)
	{
		this.address = address;
		this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
	}
	
	public Peer(ConnectionCallback callback, Socket socket)
	{
		try
		{	
			this.callback = callback;
			this.socket = socket;
			this.address = socket.getInetAddress();
			this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
			this.white = false;
			this.pingCounter = 0;
			this.connectionTime = NTP.getTime();
			this.errors = 0;
			
			//ENABLE KEEPALIVE
			//this.socket.setKeepAlive(true);
			
			//TIMEOUT
			this.socket.setSoTimeout(1000*60*60);
			
			//CREATE STRINGWRITER
			this.out = socket.getOutputStream();
			
			//START COMMUNICATON THREAD
			this.start();
			
			//START PINGER
			this.pinger = new Pinger(this);
			
			//ON SOCKET CONNECT
			this.callback.onConnect(this, true);			

			this.runed = true;
		}
		catch(Exception e)
		{
			//FAILED TO CONNECT NO NEED TO BLACKLIST
			//LOGGER.info("Failed to connect to : " + address);
			//LOGGER.error(e.getMessage(), e);

		}

	}
	
	// connect to old reused peer
	public void reconnect(Socket socket)
	{
		try
		{	
			
			if (this.socket!=null) {
				this.close();
			}
			
			this.socket = socket;
			this.address = socket.getInetAddress();
			this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
			this.white = false;
			this.pingCounter = 0;
			this.connectionTime = NTP.getTime();
			this.errors = 0;
			
			//ENABLE KEEPALIVE
			//this.socket.setKeepAlive(true);
			
			//TIMEOUT
			this.socket.setSoTimeout(1000*60*60);
			
			//CREATE STRINGWRITER
			this.out = socket.getOutputStream();
									
			//ON SOCKET CONNECT
			this.callback.onConnect(this, false);			

			this.runed = true;
		}
		catch(Exception e)
		{
			//FAILED TO CONNECT NO NEED TO BLACKLIST
			//LOGGER.info("Failed to connect to : " + address);
			//LOGGER.error(e.getMessage(), e);

		}
	}

	
	public InetAddress getAddress()
	{
		return address;
	}
	
	public long getPingCounter()
	{
		return this.pingCounter;
	}
	public int getErrors()
	{
		return this.errors;
	}
	public long resetErrors()
	{
		return this.errors = 0;
	}
	
	public void addPingCounter()
	{
		this.pingCounter ++;
	}
	public void addError()
	{
		this.errors ++;
	}
	
	public long getPing()
	{
		if (this.pinger == null)
			return 1999999;
		
		return this.pinger.getPing();
	}
	
	public boolean isPinger()
	{
		return this.pinger != null;
	}
	public boolean isUsed()
	{
		return this.socket != null && this.socket.isConnected() && this.runed;
	}
	
	// connect and run
	public void connect(ConnectionCallback callback)
	{
		if(DBSet.getInstance().isStoped()){
			return;
		}
		
		this.callback = callback;
		this.white = true;
		this.pingCounter = 0;
		this.connectionTime = NTP.getTime();
		this.errors = 0;
		
		int steep = 0;
		try
		{
			//OPEN SOCKET
			steep++;
			this.socket = new Socket(address, Controller.getInstance().getNetworkPort());
			
			//ENABLE KEEPALIVE
			//steep++;
			//this.socket.setKeepAlive(true);
			
			//TIMEOUT
			steep++;
			this.socket.setSoTimeout(1000*60*60);
			
			//CREATE STRINGWRITER
			steep++;
			this.out = socket.getOutputStream();
						
			if (this.pinger == null) {
				//START PINGER
				this.pinger = new Pinger(this);

				//START COMMUNICATON THREAD
				steep++;
				this.start();

				//ON SOCKET CONNECT
				steep++;
				this.callback.onConnect(this, true);			
			} else {
				// already started
				this.callback.onConnect(this, false);
			}

			this.runed = true;
		}
		catch(Exception e)
		{
			//FAILED TO CONNECT NO NEED TO BLACKLIST
			if (steep != 1) {
				//LOGGER.error(e.getMessage(), e);
				//LOGGER.info("Failed to connect to : " + address + " on steep: " + steep);
			}
		}
	}
	
	public void run()
	{
		
		
		DataInputStream in = null;

		while(true)
		{

			// CHECK connection
			if (socket == null || !socket.isConnected() || socket.isClosed()
					|| !runed
					) {
				
				in = null;
				
				try {
					Thread.sleep(50);
				}
				catch (Exception e) {		
				}
				continue;
			}
			
			// CHECH stream
			if (in == null) {
				try 
				{
					in = new DataInputStream(socket.getInputStream());
				} 
				catch (Exception e) 
				{
					//LOGGER.error(e.getMessage(), e);
					
					//DISCONNECT
					callback.onDisconnect(this);
					try {
						Thread.sleep(10);
					}
					catch (Exception e1) {
						
					}
					continue;
				}
			}

			//READ FIRST 4 BYTES
			byte[] messageMagic = new byte[Message.MAGIC_LENGTH];
			try 
			{

				if (in.available()>0) {
					in.readFully(messageMagic);
				} else {
					try {
						Thread.sleep(10);
					}
					catch (Exception e) {
						
					}
					continue;
				}
			} 
			catch (Exception e) 
			{
				//LOGGER.error(e.getMessage(), e);
				
				//DISCONNECT
				callback.onError(this, "readFully wrong - " + e.getMessage());
				try {
					Thread.sleep(10);
				}
				catch (Exception e1) {
					
				}
				continue;
			}
			
			if(Arrays.equals(messageMagic, Controller.getInstance().getMessageMagic()))
			{
				//PROCESS NEW MESSAGE
				Message message;
				try 
				{
					message = MessageFactory.getInstance().parse(this, in);
				} 
				catch (Exception e) 
				{
					//LOGGER.error(e.getMessage(), e);
					
					//DISCONNECT
					callback.onError(this, "parse message wrong - " + e.getMessage());
					try {
						Thread.sleep(10);
					}
					catch (Exception e1) {
						
					}
					continue;
				}
				
				//LOGGER.info("received message " + message.getType() + " from " + this.address.toString());
				
				//CHECK IF WE ARE WAITING FOR A MESSAGE WITH THAT ID
				if(message.hasId() && this.messages.containsKey(message.getId()))
				{
					//ADD TO OUR OWN LIST
					this.messages.get(message.getId()).add(message);
				}
				else
				{
					//CALLBACK
					// see in network.Network.onMessage(Message)
					// and then see controller.Controller.onMessage(Message)
					try // ICREATOR
					{
						this.callback.onMessage(message);
					} 
					catch (Exception e) 
					{
						LOGGER.error(e.getMessage(), e);
						//DISCONNECT
						//this.onPingFail("onMessage error");
						callback.onError(this, "onMessage wrong - " + e.getMessage());
						//callback.onDisconnect(this); // ICREATOR
						try {
							Thread.sleep(10);
						}
						catch (Exception e1) {
							
						}
						continue;
					}
				}
			}
			else
			{
				//ERROR
				callback.onError(this, "received message with wrong magic");
				try {
					Thread.sleep(10);
				}
				catch (Exception e) {
					
				}
				continue;
			}
		}
	}
	
	public boolean sendMessage(Message message)
	{
		try 
		{
			//CHECK IF SOCKET IS STILL ALIVE
			if(!this.socket.isConnected())
			{
				//ERROR
				callback.onError(this, "socket not still alive");
				
				return false;
			}
			
			//SEND MESSAGE
			synchronized(this.out)
			{
				this.out.write(message.toBytes());
				this.out.flush();
			}
			
			//RETURN
			return true;
		}
		catch (Exception e) 
		{
			//ERROR
			//LOGGER.error(e.getMessage(),e);

			callback.onError(this, "sendMessage - " + e.getMessage());
			
			//RETURN
			return false;
		}
	}
	
	public Message getResponse(Message message)
	{
		//GENERATE ID
		int id = (int) ((Math.random() * Integer.MAX_VALUE) + 1);
		
		//SET ID
		message.setId(id);
		
		//PUT QUEUE INTO MAP SO WE KNOW WE ARE WAITING FOR A RESPONSE
		BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(1);
		this.messages.put(id, blockingQueue);
		
		//WHEN FAILED TO SEND MESSAGE
		if(!this.sendMessage(message))
		{
			return null;
		}
		
		try 
		{
			Message response = blockingQueue.poll(Settings.getInstance().getConnectionTimeout(), TimeUnit.MILLISECONDS);
			this.messages.remove(id);
			
			return response;
		} 
		catch (InterruptedException e)
		{
			//this.callback.onDisconnect(this); // icreator
			//NO MESSAGE RECEIVED WITHIN TIME;
			//LOGGER.error(e.getMessage(), e);

			return null;
		}
	}
	
	// call from ping
	public void onPingFail(String mess)
	{
		this.callback.onError(this, "onPingFail : " + this.address.getHostAddress() + " - " + mess);
	}
	

	// TRUE = You;  FALSE = Remote
	public boolean isWhite()
	{
		return this.white; 
	}
	
	public long getConnectionTime()
	{
		return this.connectionTime; 
	}	
	
	public boolean isBad()
	{
		return DBSet.getInstance().getPeerMap().isBad(this.getAddress()); 
	}
	
	public void close() 
	{
		
		
		if (!runed) {
			return;
		}
		
		runed = false;
		
		//LOGGER.info("Try close peer : " + address);
		
		try
		{
			/*
			//STOP PINGER
			if(this.pinger != null)
			{
				//this.pinger.stopPing();
				
				synchronized (this.pinger) {
					try {
						this.pinger.wait();
					} catch(Exception e) {
						
					}
				}
			}
				*/
			
			//CHECK IS SOCKET EXISTS
			if(socket != null)
			{
				//CHECK IF SOCKET IS CONNECTED
				if(socket.isConnected())
				{
					//CLOSE SOCKET
					socket.close();
				}
				socket = null;
			}
		}
		catch(Exception e)
		{
			//LOGGER.error(e.getMessage(), e);
	
		}		
	}
	
	// icreator - wair is DB is busy
	// https://github.com/jankotek/mapdb/search?q=ClosedByInterruptException&type=Issues&utf8=%E2%9C%93
	//
	public void goInterrupt_old()
	{
		DBSet dbSet = DBSet.getInstance();
		//int i = 0;
		while( dbSet.isBusy() || dbSet.getBlockMap().isProcessing()) {
			try {
				LOGGER.info(" PEER.goInterrupt wait DB : " + address);
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
