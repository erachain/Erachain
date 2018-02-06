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
	// Слишком бльшой буфер позволяет много посылок накидать не ожидая их приема. Но запросы с возратом остаются в очереди на долго
	// поэтому нужно ожидание дольще делать
	private static int SOCKET_BUFFER_SIZE = BlockChain.HARD_WORK?1024<<11:1024<<8;
	private static int MAX_BEFORE_PING = SOCKET_BUFFER_SIZE<<1;
	private OutputStream out;
	private Pinger pinger;
	private boolean white;
	private long pingCounter;
	private long connectionTime;
	private boolean runed;
	private int errors;
	private int requestKey = 0;
	
	private long sendedBeforePing = 0l;
	private long maxBeforePing;
	
	private Map<Integer, BlockingQueue<Message>> messages;
	
	static Logger LOGGER = Logger.getLogger(Peer.class.getName());

	public Peer(InetAddress address)
	{
		this.address = address;
		this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
		//LOGGER.debug("@@@ new Peer(InetAddress address) : " + address.getHostAddress());
		this.setName("Thread Peer - "+ this.getId());
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

			// IT is STARTED
			this.runed = true;

			//ON SOCKET CONNECT
			this.callback.onConnect(this, true);			
		} else {

			// IT is STARTED
			this.runed = true;

			// already started
			this.callback.onConnect(this, false);
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
			this.sendedBeforePing = 0l;
			this.maxBeforePing = MAX_BEFORE_PING;

			this.setName("Thread Peer - "+ this.getId());
			
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

			// IT is STARTED
			this.runed = true;

			//ON SOCKET CONNECT
			this.callback.onConnect(this, true);

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
		this.sendedBeforePing = 0l;
		this.maxBeforePing = MAX_BEFORE_PING;
		
		int step = 0;
		try
		{
			//OPEN SOCKET
			step++;
			if (this.socket != null)
				this.socket.close();
			
			this.socket = new Socket(address, Controller.getInstance().getNetworkPort());
			
			//ENABLE KEEPALIVE
			step++;
			this.socket.setKeepAlive(KEEP_ALIVE);

			//TIMEOUT
			step++;
			this.socket.setSoTimeout(Settings.getInstance().getConnectionTimeout());
			
			this.socket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
			this.socket.setSendBufferSize(SOCKET_BUFFER_SIZE);

			//CREATE STRINGWRITER
			step++;
			this.out = socket.getOutputStream();

			//LOGGER.debug("@@@ connect(callback) : " + address.getHostAddress());

		}
		catch(Exception e)
		{
			//FAILED TO CONNECT NO NEED TO BLACKLIST
			if (step != 1) {
				LOGGER.error(e.getMessage(), e);
				LOGGER.debug("Failed to connect to : " + address + " on step: " + step);
			}
			
			return;
			
		}

		if (this.pinger == null) {
			//START PINGER
			this.pinger = new Pinger(this);

			//START COMMUNICATON THREAD
			step++;
			this.start();

			// IT is STARTED
			this.runed = true;

			//ON SOCKET CONNECT
			step++;
			this.callback.onConnect(this, true);			
		} else {
			this.pinger.setPing(Integer.MAX_VALUE);

			// IT is STARTED
			this.runed = true;

			// already started
			this.callback.onConnect(this, false);
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
			this.sendedBeforePing = 0l;
			this.maxBeforePing = MAX_BEFORE_PING;
			
			//ENABLE KEEPALIVE
			this.socket.setKeepAlive(KEEP_ALIVE);

			this.socket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
			this.socket.setSendBufferSize(SOCKET_BUFFER_SIZE);

			//TIMEOUT
			this.socket.setSoTimeout(Settings.getInstance().getConnectionTimeout());
			
			//CREATE STRINGWRITER
			this.out = socket.getOutputStream();
									
			this.pinger.setPing(Integer.MAX_VALUE);
						
			// IT is STARTED
			this.runed = true;

			//ON SOCKET CONNECT
			this.callback.onConnect(this, false);			

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

	public void setMessageWinBlock(Message message) {
		this.pinger.setMessageWinBlock(message);
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

	public boolean tryPing(long timer)
	{
		return this.pinger.tryPing(timer);
	}

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
			
	public void run()
	{
		byte[] messageMagic = null;	
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

			try {
				Thread.sleep(1);
			}
			catch (Exception e) {		
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
			messageMagic = new byte[Message.MAGIC_LENGTH];
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
					catch (java.net.SocketException err) {
						//callback.tryDisconnect(this, 1, " readFully - " + err.getMessage());
						callback.tryDisconnect(this, 0, "");
						continue;
					}
					catch (java.io.IOException err) {
						//LOGGER.error("readFully - " + err.getMessage(), err);
						//callback.tryDisconnect(this, 1, " readFully - " + err.getMessage());
						callback.tryDisconnect(this, 0, "");
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
				
				//CHECK IF WE ARE WAITING FOR A RESPONSE WITH THAT ID
				if(!message.isRequest()
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
			messageMagic = null;
		}
	}
	
	public boolean sendMessage(Message message)
	{
		//CHECK IF SOCKET IS STILL ALIVE
		if(!this.runed || this.socket == null)
		{
			callback.tryDisconnect(this, 0, "SEND - not runned");
			return false;
		}
		
		if(!this.socket.isConnected())
		{
			//ERROR
			callback.tryDisconnect(this, 0, "SEND - socket not still alive");
			
			return false;
		}
		
		byte[] bytes = message.toBytes();

		
		//SEND MESSAGE
		synchronized(this.out)
		{

			try {
				this.out.write(bytes);
				this.out.flush();
			}
			catch (IOException e) 
			{
				//ERROR
				//LOGGER.debug("try sendMessage to " + this.address + " " + Message.viewType(message.getType()) + " ERROR: " + e.getMessage());
				//callback.tryDisconnect(this, 5, "SEND - " + e.getMessage());
				callback.tryDisconnect(this, 0, "try write 1");

				//RETURN
				return false;
			}
			catch (Exception e) 
			{
				//ERROR
				//LOGGER.debug("try sendMessage to " + this.address + " " + Message.viewType(message.getType()) + " ERROR: " + e.getMessage());
				//callback.tryDisconnect(this, 5, "SEND - " + e.getMessage());
				callback.tryDisconnect(this, 0, "try write 2");

				//RETURN
				return false;
			}

		}

		// странно - если идет передача блоков в догоняющую ноду в ее буфер
		// и тут пинговать то она зависает в ожидании надолго и синхронизация удаленной ноды встает на 30-50 секунд
		// ели урать тут пинги то блоки передаются без останова быстро
		// ХОТЯ! при передаче неподтвержденных заявок пингт нормально работают - видимо тут влоенный вызов запрещен по synchronized
		int messageSize = bytes.length;
		int type = message.getType(); 
		if (type == Message.GET_PING_TYPE
				|| type == Message.GET_HWEIGHT_TYPE) {
			this.sendedBeforePing = 0l;		
		} else {
			this.sendedBeforePing += bytes.length;
		}

		if (false && type != Message.GET_PING_TYPE
				&& type != Message.GET_HWEIGHT_TYPE
				&& type != Message.HWEIGHT_TYPE
				&& type != Message.GET_BLOCK_TYPE
				&& this.sendedBeforePing > this.maxBeforePing) {
			
			if (messageSize < this.maxBeforePing) {
				
				LOGGER.debug("PING >> send to " + this.address.getHostAddress() + " " + Message.viewType(message.getType())
				+ " bytes:" + this.sendedBeforePing
				+ " maxBeforePing: " + this.maxBeforePing);

				this.pinger.tryPing(10000);
				Controller.getInstance().notifyObserveUpdatePeer(this);

				long ping = this.getPing(); 

				if (ping < 0) {
					if (this.maxBeforePing > MAX_BEFORE_PING>>2) {
						this.maxBeforePing >>=2;			
					}
					LOGGER.debug("PING << send to " + this.address.getHostAddress() + " " + Message.viewType(message.getType())
					+ " ms: " + ping
					+ " maxBeforePing >>=2: " + this.maxBeforePing);
				} else if (ping > 5000) {
					if (this.maxBeforePing > MAX_BEFORE_PING>>2) {
						this.maxBeforePing >>=1;			
					}
					LOGGER.debug("PING << send to " + this.address.getHostAddress() + " " + Message.viewType(message.getType())
					+ " ms: " + ping
					+ " maxBeforePing >>=1: " + this.maxBeforePing);
				} else if (ping < 50) {
					if (this.maxBeforePing < MAX_BEFORE_PING<<3) {
						this.maxBeforePing <<=2;
					}
					LOGGER.debug("PING << send to <<=2" + this.address.getHostAddress() + " " + Message.viewType(message.getType())
					+ " ms: " + ping
					+ " maxBeforePing: " + this.maxBeforePing);
				} else if (ping < 100) {
					if (this.maxBeforePing < MAX_BEFORE_PING<<3) {
						this.maxBeforePing <<=1;
					}
					LOGGER.debug("PING << send to " + this.address.getHostAddress() + " " + Message.viewType(message.getType())
					+ " ms: " + ping
					+ " maxBeforePing: <<=1" + this.maxBeforePing);
				}					
			}
			
		}

		//RETURN
		return true;
	}

	public synchronized int getResponseKey()
	//public int getResponseKey()
	{
		//GENERATE ID
		this.requestKey += 1;
		
		if (this.requestKey  < 1 || this.requestKey >= Integer.MAX_VALUE - 1) {
			this.requestKey = 1;
		}
		
		long counter = 0;
		while (this.messages.containsKey(this.requestKey)) {
			this.requestKey +=1;
			counter++;
		}
		
		if (counter > 100000) {
			LOGGER.error("getResponseKey find counter: " + counter); 
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
