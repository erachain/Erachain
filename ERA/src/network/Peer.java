package network;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
 import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.BlockChain;
import core.transaction.Transaction;
import database.DBSet;
import database.PeerMap;
import lang.Lang;
import network.message.Message;
import network.message.MessageFactory;
import ntp.NTP;
import settings.Settings;

public class Peer extends Thread{

	private final static boolean need_wait = false;
	private InetAddress address;
	private ConnectionCallback callback;
	private Socket socket;
	// KEEP_ALIVE = false - as web browser - getConnectionTimeout will break connection
	private static boolean KEEP_ALIVE = true;
	private static int SOCKET_BUFFER_SIZE = BlockChain.HARD_WORK?1024<<11:1024<<8;
	private OutputStream out;
	private Pinger pinger;
	private boolean white;
	private long pingCounter;
	private long connectionTime;
	private boolean runed;
	private int errors;
	private int requestKey = 0;
	
	private Map<Integer, BlockingQueue<Message>> messages;
	
	static Logger LOGGER = Logger.getLogger(Peer.class.getName());

	public Peer(InetAddress address)
	{
		this.address = address;
		this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
		//LOGGER.debug("@@@ new Peer(InetAddress address) : " + address.getHostAddress());

	}
	
	/*

	private void init(ConnectionCallback callback, Socket socket) {
		
		if (callback != null) {
			this.callback = callback;
		}
		
		this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
		this.pingCounter = 0;
		this.connectionTime = NTP.getTime();
		this.errors = 0;
		
		try
		{	
			if (socket == null) {
				this.socket = new Socket(address, Controller.getInstance().getNetworkPort());
			} else {
				this.socket = socket;
				this.address = socket.getInetAddress();
			}

			//ENABLE KEEPALIVE
			this.socket.setKeepAlive(KEEP_ALIVE);
			
			//TIMEOUT
			this.socket.setSoTimeout(Settings.getInstance().getConnectionTimeout());
			
			this.socket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
			this.socket.setSendBufferSize(SOCKET_BUFFER_SIZE);

			//CREATE STRINGWRITER
			this.out = socket.getOutputStream();
		}
		catch(Exception e)
		{
			//FAILED TO CONNECT NO NEED TO BLACKLIST
			//LOGGER.info("Failed to connect to : " + address);
			//LOGGER.error(e.getMessage(), e);

		}
		
		if (this.pinger == null) {

			//START COMMUNICATON THREAD
			this.start();

			//START PINGER
			this.pinger = new Pinger(this);

			//ON SOCKET CONNECT
			this.callback.onConnect(this, true);			
		} else {
			// already started
			this.callback.onConnect(this, false);
		}
		
		this.runed = true;

		// BROADCAST UNCONFIRMED TRANSACTIONS to PEER
		if(!Controller.getInstance().isOnStopping()){
		List<Transaction> transactions = Controller.getInstance().getUnconfirmedTransactions();
		if (transactions != null && !transactions.isEmpty())
			this.callback.broadcastUnconfirmedToPeer(transactions, this);
		}

	}
	
	
	public Peer(ConnectionCallback callback, Socket socket)
	{

		this.white = false;
		init(callback, socket);

	}
	

	// connect and run
	public void connect(ConnectionCallback callback)
	{
		if(Controller.getInstance().isOnStopping()){
			return;
		}
		
		this.white = true;
		init(callback, null);

	}


	// connect to old reused peer
	public void reconnect(Socket socket)
	{
			
		if (this.socket!=null) {
			this.close();
		}

		this.white = false;
		init(null, socket);

	}
*/
	
	public Peer(ConnectionCallback callback, Socket socket)
	{
	
		//LOGGER.debug("@@@ new Peer(ConnectionCallback callback, Socket socket) : " + socket.getInetAddress().getHostAddress());

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
			this.socket.setKeepAlive(KEEP_ALIVE);
			
			//TIMEOUT
			this.socket.setSoTimeout(Settings.getInstance().getConnectionTimeout());
			
			this.socket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
			this.socket.setSendBufferSize(SOCKET_BUFFER_SIZE);
			
			//CREATE STRINGWRITER
			this.out = socket.getOutputStream();
			
			//START COMMUNICATON THREAD
			this.start();
			
			//START PINGER
			if (this.pinger == null)
				this.pinger = new Pinger(this);
			else
				this.pinger.setPing(Integer.MAX_VALUE);

			
			//ON SOCKET CONNECT
			this.callback.onConnect(this, true);

			this.runed = true;

			// BROADCAST UNCONFIRMED TRANSACTIONS to PEER
			if(!Controller.getInstance().isOnStopping()){
			List<Transaction> transactions = Controller.getInstance().getUnconfirmedTransactions();
			if (transactions != null && !transactions.isEmpty())
				this.callback.broadcastUnconfirmedToPeer(transactions, this);
			}

			//LOGGER.debug("@@@ new Peer(ConnectionCallback callback, Socket socket) : " + socket.getInetAddress().getHostAddress());
			
		}
		catch(Exception e)
		{
			//FAILED TO CONNECT NO NEED TO BLACKLIST
			LOGGER.info("Failed to connect to : " + address);
			LOGGER.error(e.getMessage(), e);

		}

	}
	
	// connect and run
	public void connect(ConnectionCallback callback)
	{
		if(Controller.getInstance().isOnStopping()){
			return;
		}
		
		// GOOD WORK
		//LOGGER.debug("@@@ connect(ConnectionCallback callback) : " + address.getHostAddress());

		this.callback = callback;
		this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
		this.white = true;
		this.pingCounter = 0;
		this.connectionTime = NTP.getTime();
		this.errors = 0;
		
		int steep = 0;
		try
		{
			//OPEN SOCKET
			steep++;
			if (this.socket != null)
				this.socket.close();
			
			this.socket = new Socket(address, Controller.getInstance().getNetworkPort());
			
			//ENABLE KEEPALIVE
			steep++;
			this.socket.setKeepAlive(KEEP_ALIVE);

			//TIMEOUT
			steep++;
			this.socket.setSoTimeout(Settings.getInstance().getConnectionTimeout());
			
			this.socket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
			this.socket.setSendBufferSize(SOCKET_BUFFER_SIZE);

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
				this.pinger.setPing(Integer.MAX_VALUE);

				// already started
				this.callback.onConnect(this, false);
			}

			this.runed = true;
			
			// BROADCAST UNCONFIRMED TRANSACTIONS to PEER
			if(!Controller.getInstance().isOnStopping()){
			List<Transaction> transactions = Controller.getInstance().getUnconfirmedTransactions();
			if (transactions != null && !transactions.isEmpty())
				this.callback.broadcastUnconfirmedToPeer(transactions, this);
			}
			
			//LOGGER.debug("@@@ connect(callback) : " + address.getHostAddress());

		}
		catch(Exception e)
		{
			//FAILED TO CONNECT NO NEED TO BLACKLIST
			if (steep != 1) {
				LOGGER.error(e.getMessage(), e);
				LOGGER.debug("Failed to connect to : " + address + " on steep: " + steep);
			}
		}
	}


	// connect to old reused peer
	public void reconnect(Socket socket)
	{

		//LOGGER.debug("@@@ reconnect(socket) : " + socket.getInetAddress().getHostAddress());

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
			this.socket.setKeepAlive(KEEP_ALIVE);

			this.socket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
			this.socket.setSendBufferSize(SOCKET_BUFFER_SIZE);

			//TIMEOUT
			this.socket.setSoTimeout(Settings.getInstance().getConnectionTimeout());
			
			//CREATE STRINGWRITER
			this.out = socket.getOutputStream();
									
			//ON SOCKET CONNECT
			this.callback.onConnect(this, false);			

			this.pinger.setPing(Integer.MAX_VALUE);

			this.runed = true;
			
			// BROADCAST UNCONFIRMED TRANSACTIONS to PEER
			if(!Controller.getInstance().isOnStopping()){
			List<Transaction> transactions = Controller.getInstance().getUnconfirmedTransactions();
			if (transactions != null && !transactions.isEmpty())
				this.callback.broadcastUnconfirmedToPeer(transactions, this);
			}
			
			//LOGGER.debug("@@@ reconnect(socket) : " + socket.getInetAddress().getHostAddress());

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
	
	public void setNeedPing()
	{		
		this.pinger.setNeedPing();
	}

	public void setMessageQueue(Message message) {
		this.pinger.setMessageQueue(message);
	}

	public void setMessageQueuePing(Message message) {
		this.pinger.setMessageQueuePing(message);
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

	/*
	public boolean tryPing()
	{
		return this.pinger.tryPing();
	}
	*/

	public void setPing(int ping)
	{		
		this.pinger.setPing(ping);
	}
	
	public boolean isPinger()
	{
		return this.pinger != null;
	}
	public boolean isUsed()
	{
		return this.socket != null && this.socket.isConnected() && this.runed;
	}
		
	private void clearResponse() {
		
		/*
		Message message = MessageFactory.getInstance().createGetHWeightMessage();
		while (this.messages.size() > 0) {
			for(int item: this.messages.keySet()) {
				if (this.messages.get(item).size() == 0) {
					this.messages.get(item).add(message);
				}
				break;
			}
		}
		*/
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
				
				try {
					Thread.sleep(1000);
				}
				catch (Exception e) {		
				}

				in = null;				
				continue;

			}
			
			// CHECK stream
			if (in == null) {
				try 
				{
					in = new DataInputStream(socket.getInputStream());
				} 
				catch (Exception e) 
				{
					//LOGGER.error(e.getMessage(), e);
					
					//DISCONNECT
					callback.tryDisconnect(this, 0, e.getMessage());
					continue;
				}
			}

			//READ FIRST 4 BYTES
			byte[] messageMagic = new byte[Message.MAGIC_LENGTH];
			try 
			{

				if (true) {
					// MORE EFFECTIVE
					try {
						in.readFully(messageMagic);					
					}
					catch (java.net.SocketTimeoutException timeOut) {
						///LOGGER.error("readFully - " + timeOut.getMessage(), timeOut);
						continue;
					}			 
					catch (java.io.IOException err) {
						//LOGGER.error("readFully - " + err.getMessage(), err);
						callback.tryDisconnect(this, 1, " readFully - " + err.getMessage());
						continue;
					}
				} else {
					if (in.available()>0) {
						in.readFully(messageMagic);
					} else {
						try {
							Thread.sleep(1);
						}
						catch (Exception e) {
						}
	
						continue;
					}
				}
			}
			catch (java.io.EOFException e) 
			{
				// DISCONNECT and BAN
				//this.pinger.tryPing();
				callback.tryDisconnect(this, 0, "readFully EOFException - " + e.getMessage());
				continue;						
			} catch (Exception e) 
			{
				//LOGGER.error("readFully - " + e.getMessage(), e);
				
				// DISCONNECT and BAN
				////this.pinger.tryPing();
				//callback.tryDisconnect(this, 0, "readFully wrong - " + e.getMessage());
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
				catch (java.net.SocketTimeoutException timeOut) {
					//LOGGER.debug("parse SocketTimeoutException timeOut:: " + timeOut);
					
					//this.pinger.tryPing();
					continue;
				}
				catch (java.io.EOFException e) 
				{
					// DISCONNECT and BAN
					//this.pinger.tryPing();
					callback.tryDisconnect(this, 0, "parse EOFException - " + e.getMessage());
					continue;
				
				}
				catch (Exception e) 
				{
					//LOGGER.error(e.getMessage(), e);
					//if (this.socket.isClosed())
					
					//DISCONNECT and BAN
					callback.tryDisconnect(this, 6, "parse message wrong - " + e.getMessage());
					continue;
				}
				
				if (false && (message.getType() == Message.GET_HWEIGHT_TYPE || message.getType() == Message.HWEIGHT_TYPE)) {
					LOGGER.debug("received message " + message.viewType() + " from " + this.address.toString());
					LOGGER.debug("isRequest " + message.isRequest() + " hasId " + message.hasId());
					LOGGER.debug(" Id " + message.getId() + " containsKey: " + this.messages.containsKey(message.getId()));
				}
				if (message.getType() != Message.TRANSACTION_TYPE) {
					//LOGGER.debug("received message " + message.viewType() + " from " + this.address.toString());
				}

				//CHECK IF WE ARE WAITING FOR A RESPONSE WITH THAT ID
				if(message.getType() != Message.PING_TYPE
						&& !message.isRequest()
						&& message.hasId()
						&& this.messages.containsKey(message.getId()) ) {
						//ADD TO OUR OWN LIST
					if (false && (message.getType() == Message.GET_HWEIGHT_TYPE || message.getType() == Message.HWEIGHT_TYPE))
						LOGGER.debug(" response add ");
					
					try {
						this.messages.get(message.getId()).add(message);
					} catch (java.lang.IllegalStateException e) {
						LOGGER.debug("received message " + message.viewType() + " from " + this.address.toString());
						LOGGER.debug("isRequest " + message.isRequest() + " hasId " + message.hasId());
						LOGGER.debug(" Id " + message.getId() + " containsKey: " + this.messages.containsKey(message.getId()));
						LOGGER.error(e.getMessage(), e);
					}
				} else {
					//CALLBACK
					// see in network.Network.onMessage(Message)
					// and then see controller.Controller.onMessage(Message)
					if (false && (message.getType() == Message.GET_HWEIGHT_TYPE || message.getType() == Message.HWEIGHT_TYPE))
						LOGGER.debug(" onMess solve ");
					
					this.callback.onMessage(message);
				}
			}
			else
			{
				//ERROR and BAN
				callback.tryDisconnect(this, 3600, "parse - received message with wrong magic");
				continue;
			}
		}
	}
	
	private int sendUsed = 0;
	public boolean sendMessage(Message message)
	{
		try 
		{
			//CHECK IF SOCKET IS STILL ALIVE
			if(!this.socket.isConnected())
			{
				//ERROR
				callback.tryDisconnect(this, 0, "SEND - socket not still alive");
				
				return false;
			}
			
			//if (message.getType() != Message.TRANSACTION_TYPE) {
			//	LOGGER.debug("try sendMessage to: " + this.socket.getInetAddress() + " " + message.viewType());
			//}

			while(false && sendUsed > 0) {
				try {
					Thread.sleep(10);
				}
				catch (Exception e) {
				}
			}

			//SEND MESSAGE
			synchronized(this.out)
			{
				//if (sendUsed > 0) LOGGER.debug("sendUsed: " + sendUsed);
				//sendUsed++;
				this.out.write(message.toBytes());
				this.out.flush();
				//--sendUsed;
			}

			if (false && message.getType() != Message.TRANSACTION_TYPE) {
				LOGGER.debug("try sendMessage to " + this.address + " " + Message.viewType(message.getType()));
			}

			//if (message.getType() != Message.TRANSACTION_TYPE) {
			//	LOGGER.debug("try sendMessage OK ");
			//}

			//RETURN
			return true;
		}
		catch (IOException e) 
		{
			//ERROR
			//LOGGER.debug("try sendMessage to " + this.address + " " + Message.viewType(message.getType()) + " ERROR: " + e.getMessage());
			callback.tryDisconnect(this, 0, "SEND - " + e.getMessage());

			//RETURN
			//--sendUsed;
			return false;
		}
		catch (Exception e) 
		{
			//ERROR
			LOGGER.debug("try sendMessage to " + this.address + " " + Message.viewType(message.getType()) + " ERROR: " + e.getMessage());

			//RETURN
			//--sendUsed;
			return false;
		}
	}

	public synchronized int getResponseKey()
	//public int getResponseKey()
	{
		//GENERATE ID
		this.requestKey += 1;
		
		if (this.requestKey  < 1 || this.requestKey >= Integer.MAX_VALUE - 1) {
			this.requestKey = 1;
		}
		
		while (this.messages.containsKey(this.requestKey)) {
			this.requestKey +=1;
		}
		
		return this.requestKey;
	}

	public Message getResponse(Message message, long timeSOT)
	{

		int thisRequestKey = this.getResponseKey();

		BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(1);
		
		message.setId(thisRequestKey);
		
		//PUT QUEUE INTO MAP SO WE KNOW WE ARE WAITING FOR A RESPONSE
		this.messages.put(thisRequestKey, blockingQueue);
		
		//WHEN FAILED TO SEND MESSAGE
		if(!this.sendMessage(message))
		{
			//LOGGER.debug(" messages.remove( " + thisRequestKey + " ) by SEND ERROR" + this.getAddress().getHostAddress());
			this.messages.remove(thisRequestKey);
			return null;
		}
		
		Message response = null;
		try 
		{
			response = blockingQueue.poll(timeSOT, TimeUnit.MILLISECONDS);
			//LOGGER.debug(" messages.remove( " + thisRequestKey + " ) by good RESPONSE " + this.getAddress().getHostAddress()
			//		 + " " + (response==null?"NULL":response.toString()));
			this.messages.remove(thisRequestKey);
		} 
		catch (InterruptedException e)
		{
			//NO MESSAGE RECEIVED WITHIN TIME;
			//LOGGER.debug(" messages.remove( " + thisRequestKey + " ) by ERRROR " + this.getAddress().getHostAddress() + e.getMessage());
			this.messages.remove(thisRequestKey);
			//LOGGER.error(e.getMessage(), e);
		}

		return response;
	}
	
	public Message getResponse(Message message)
	{
		return getResponse(message, Settings.getInstance().getConnectionTimeout());
	}

	// call from ping
	public void onPingFail(String mess)
	{
		// , 
		this.callback.tryDisconnect(this, 5, "onPingFail : " + this.address.getHostAddress() + " - " + mess);
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
		return Controller.getInstance().getDBSet().getPeerMap().isBad(this.getAddress()); 
	}
	public boolean isBanned()
	{
		return Controller.getInstance().getDBSet().getPeerMap().isBanned(address.getAddress());
	}
	

	public void ban(int banForMinutes, String mess)
	{
		this.callback.tryDisconnect(this, banForMinutes, mess); 
	}

	public void close() 
	{	
		
		if (!runed) {
			return;
		}
		
		// CLEAR all messages BlockingQueue
		//this.clearResponse();

		runed = false;
		
		//LOGGER.info("Try close peer : " + address.getHostAddress());
		
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
		
}
