package network;
// 30/03 ++
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;
// import org.apache.log4j.Logger;
import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.BlockChain;
//import core.BlockChain;
import core.transaction.Transaction;
//import database.DBSet;
//import database.TransactionMap;
import datachain.DCSet;
//import lang.Lang;
import network.message.FindMyselfMessage;
import network.message.Message;
import network.message.MessageFactory;
import ntp.NTP;
import settings.Settings;
import utils.ObserverMessage;

public class Network extends Observable implements ConnectionCallback {

	
	public static final int PEER_SLEEP_TIME = BlockChain.HARD_WORK?0:1;
	private static final int MAX_HANDLED_MESSAGES_SIZE = BlockChain.HARD_WORK?4096<<4:4096;
	private static final int PINGED_MESSAGES_SIZE = BlockChain.HARD_WORK?1024<<5:1024<<4;
	
	private ConnectionCreator creator;
	private ConnectionAcceptor acceptor;

	private static final Logger LOGGER = Logger.getLogger(Network.class);

	private List<Peer> knownPeers;
	
	private SortedSet<String> handledMessages;
	
	private boolean run;
	
	private static InetAddress myselfAddress;
	
	public Network()
	{	
		this.knownPeers = new ArrayList<Peer>();
		this.run = true;
		
		this.start();
	}

	public static InetAddress getMyselfAddress()
	{	
		return myselfAddress;
	}

	private void start()
	{
		this.handledMessages = Collections.synchronizedSortedSet(new TreeSet<String>());
		
		//START ConnectionCreator THREAD
		creator = new ConnectionCreator(this);
		creator.start();
		
		//START ConnectionAcceptor THREAD
		acceptor = new ConnectionAcceptor(this);
		acceptor.start();
	}
	
	@Override
	public void onConnect(Peer peer, boolean asNew) {
		
		//LOGGER.info(Lang.getInstance().translate("Connection successfull : ") + peer.getAddress());

		if (asNew) {
			//ADD TO CONNECTED PEERS
			synchronized(this.knownPeers)
			{
				this.knownPeers.add(peer);
			}
		}
					
		//ADD TO DATABASE
		PeerManager.getInstance().addPeer(peer, 0);
				
		if(Controller.getInstance().isOnStopping())
			return;

		//NOTIFY OBSERVERS
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_PEER_TYPE, peer));		
		
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, this.knownPeers));

	}

	@Override
	public void tryDisconnect(Peer peer, int banForMinutes, String error) {

		if (!peer.isUsed())
			return;

		if (banForMinutes != 0) { 
			//ADD TO BLACKLIST
			PeerManager.getInstance().addPeer(peer, banForMinutes);
		}

		if (error != null && error.length() > 0) {
			if (banForMinutes != 0) { 
				LOGGER.info(peer.getAddress().getHostAddress() + " ban for minutes: " + banForMinutes + " - " + error);
			} else {
				LOGGER.info("tryDisconnect : " + peer.getAddress().getHostAddress() + " - " + error);
			}
		}
				
		//CLOSE CONNECTION IF STILL ACTIVE
		peer.close();
		
		//PASS TO CONTROLLER
		Controller.getInstance().afterDisconnect(peer);

		//NOTIFY OBSERVERS
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_PEER_TYPE, peer));		

		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, this.knownPeers));
	}

	@Override
	public boolean isKnownAddress(InetAddress address, boolean andUsed) {
		
		try
		{
			synchronized(this.knownPeers)
			{
				//FOR ALL connectedPeers
				for(Peer knownPeer: knownPeers)
				{
					//CHECK IF ADDRESS IS THE SAME
					if(address.equals(knownPeer.getAddress())) {
						if (andUsed) {
							return knownPeer.isUsed();
						}
						return true;
					}
				}
			}
		}
		catch(Exception e)
		{
			//LOGGER.error(e.getMessage(),e);
		}
		
		return false;
	}
	
	@Override
	// IF PEER in exist in NETWORK - get it
	public Peer getKnownPeer(Peer peer) {
		
		try
		{
			InetAddress address = peer.getAddress();
			synchronized(this.knownPeers)
			{
				//FOR ALL connectedPeers
				for(Peer knownPeer: knownPeers)
				{
					//CHECK IF ADDRESS IS THE SAME
					if(address.equals(knownPeer.getAddress())) {
						return knownPeer;
					}
				}
			}
		}
		catch(Exception e)
		{
			//LOGGER.error(e.getMessage(),e);
		}
		
		return peer;
	}
	
	@Override
	public boolean isKnownPeer(Peer peer, boolean andUsed) {
		
		return this.isKnownAddress(peer.getAddress(), andUsed);
	}

	/*@Override
	public List<Peer> getKnownPeers() {
		
		return this.knownPeers;
	}
	*/
	
	// 
	public List<Peer> getActivePeers(boolean onlyWhite) {
		
		List<Peer> activePeers = new ArrayList<Peer>();
		synchronized(this.knownPeers) {
			for (Peer peer: this.knownPeers) {
				if (peer.isUsed())
					if (!onlyWhite || peer.isWhite())
						activePeers.add(peer);
			}
		}
		return activePeers;
	}

	public int getActivePeersCounter(boolean onlyWhite) {
		
		int counter = 0;
		synchronized(this.knownPeers) {
			for (Peer peer: this.knownPeers) {
				if (peer.isUsed())
					if (!onlyWhite || peer.isWhite())
						counter++;
			}
		}
		return counter;
	}

	public Peer startPeer(Socket socket) {
		
		// REUSE known peer
		InetAddress address = socket.getInetAddress();
		synchronized(this.knownPeers)
		{
			//FOR ALL connectedPeers
			for(Peer knownPeer: knownPeers)
			{
				//CHECK IF ADDRESS IS THE SAME
				if(address.equals(knownPeer.getAddress())) {
					knownPeer.reconnect(socket);
					return knownPeer;
				}
			}
		}
		
		// ADD new peer
		int maxPeers = Settings.getInstance().getMaxConnections(); 
		if (maxPeers > this.knownPeers.size()) {
			// make NEW PEER and use empty slots
			return new Peer(this, socket);
		}
		if (maxPeers > this.getActivePeersCounter(false)) {
			// use UNUSED peers				
			synchronized(this.knownPeers) {
				for (Peer knownPeer: this.knownPeers) {
					if (!knownPeer.isUsed()
							//|| !Network.isMyself(knownPeer.getAddress())
							) {
						knownPeer.reconnect(socket);
						return knownPeer;
					}
				}
			}
		}
		return null;
		
	}
	
	private void addHandledMessage(String hash)
	{
		try
		{
			synchronized(this.handledMessages)
			{
				//CHECK IF LIST IS FULL
				if(this.handledMessages.size() > MAX_HANDLED_MESSAGES_SIZE)
				{
					this.handledMessages.remove(this.handledMessages.first());
					LOGGER.error("handledMessages size OVERHEAT! "); 
				}
				
				this.handledMessages.add(hash);
			}
		}
		catch(Exception e)
		{
			//LOGGER.error(e.getMessage(),e);
		}
	}

	@Override
	public void onMessage(Message message) {
	
		//CHECK IF WE ARE STILL PROCESSING MESSAGES
		if(!this.run)
		{
			return;
		}
		
		//ONLY HANDLE BLOCK AND TRANSACTION MESSAGES ONCE
		if(message.getType() == Message.TRANSACTION_TYPE
				|| message.getType() == Message.BLOCK_TYPE
				|| message.getType() == Message.WIN_BLOCK_TYPE
				)
		{
			synchronized(this.handledMessages)
			{
				//CHECK IF NOT HANDLED ALREADY
				String key = new String(message.getHash());
				if(this.handledMessages.contains(key))
				{
					return;
				}
				
				//ADD TO HANDLED MESSAGES
				this.addHandledMessage(key);
			}
		}		
		
		switch(message.getType())
		{
		case Message.GET_HWEIGHT_TYPE:
			
			Tuple2<Integer, Long> HWeight = Controller.getInstance().getBlockChain().getHWeightFull(DCSet.getInstance());
			if (HWeight == null)
				HWeight = new Tuple2<Integer, Long>(-1, -1L);
			
			Message response = MessageFactory.getInstance().createHWeightMessage(HWeight);
			// CREATE RESPONSE WITH SAME ID
			response.setId(message.getId());
			
			//SEND BACK TO SENDER
			boolean result = message.getSender().sendMessage(response);
			if (!result) {
				LOGGER.debug("error on response GET_HWEIGHT_TYPE to " + message.getSender().getAddress());
			}
			
			break;
		
		//GETPEERS
		case Message.GET_PEERS_TYPE: 
			
			//CREATE NEW PEERS MESSAGE WITH PEERS
			Message answer = MessageFactory.getInstance().createPeersMessage(PeerManager.getInstance().getBestPeers());
			answer.setId(message.getId());
			
			//SEND TO SENDER
			message.getSender().sendMessage(answer);
			break;
			
			
		case Message.FIND_MYSELF_TYPE:

			FindMyselfMessage findMyselfMessage = (FindMyselfMessage) message;
			
			if(Arrays.equals(findMyselfMessage.getFoundMyselfID(), Controller.getInstance().getFoundMyselfID())) {
				//LOGGER.info("network.onMessage - Connected to self. Disconnection.");
				
				Network.myselfAddress = message.getSender().getAddress(); 
				tryDisconnect(message.getSender(), 99999, null);
			}
			
			break;
			
		//SEND TO CONTROLLER
		default:
			
			Controller.getInstance().onMessage(message);
			break;
		}		
	}
	

	public void pingAllPeers(List<Peer> exclude, boolean onlySynchronized) 
	{		
		LOGGER.debug("Broadcasting PING ALL");

		Controller cnt = Controller.getInstance();
		BlockChain chain = cnt.getBlockChain();
		Integer myHeight = chain.getHWeightFull(DCSet.getInstance()).a;

		//try
		if (true)
		{
			for(int i=0; i < this.knownPeers.size() ; i++)
			{
				
				if (!this.run)
					return;
				
				Peer peer = this.knownPeers.get(i);
				if (peer == null || !peer.isUsed()) {
					continue;
				}
				
				if (onlySynchronized) {
					// USE PEERS than SYNCHRONIZED to ME
					Tuple2<Integer, Long> peerHWeight = Controller.getInstance().getHWeightOfPeer(peer);
					if (peerHWeight == null || !peerHWeight.a.equals(myHeight)) {
						continue;
					}
				}
				
				//EXCLUDE PEERS
				if(exclude == null || !exclude.contains(peer))
				{
					peer.setNeedPing();
				}
			}	
		}
		//catch(Exception e)
		else
		{
			//error broadcasting
			//LOGGER.error(e.getMessage(),e);
		}
		
		LOGGER.debug("Broadcasting PING ALL end");
	}


	public void asyncBroadcastPing(Message message, List<Peer> exclude) 
	{		
		
		LOGGER.debug("ASYNC Broadcasting with Ping before " + message.viewType());
		
		try
		//if (true)
		{
			for(int i=0; i < this.knownPeers.size() ; i++)
			{
				
				if (!this.run)
					return;
				
				Peer peer = this.knownPeers.get(i);
				if (peer == null || !peer.isUsed()) {
					continue;
				}
								
				//EXCLUDE PEERS
				if(exclude == null || !exclude.contains(peer))
				{
					if (true || message.getDataLength() > PINGED_MESSAGES_SIZE) {
						//LOGGER.debug("PEER rty + Ping " + peer.getAddress().getHostAddress());
						peer.setMessageQueuePing(message);
					} else {
						//LOGGER.debug("PEER rty " + peer.getAddress().getHostAddress());
						peer.setMessageQueue(message);
					}

				}
			}	
		}
		catch(Exception e)
		//else
		{
			//error broadcasting
			//LOGGER.error(e.getMessage(),e);
		}
		
		LOGGER.debug("ASYNC Broadcasting with Ping before ENDED " + message.viewType());
	}
	
	public void asyncBroadcast(Message message, List<Peer> exclude, boolean onlySynchronized) 
	{		
		
		LOGGER.debug("ASYNC Broadcasting " + message.viewType());
		Controller cnt = Controller.getInstance();
		BlockChain chain = cnt.getBlockChain();
		Integer myHeight = chain.getHWeightFull(DCSet.getInstance()).a;
		
		try
		//if (true)
		{
			for(int i=0; i < this.knownPeers.size() ; i++)
			{
				
				if (!this.run)
					return;
				
				Peer peer = this.knownPeers.get(i);
				if (peer == null || !peer.isUsed()) {
					continue;
				}
				
				if (onlySynchronized) {
					// USE PEERS than SYNCHRONIZED to ME
					Tuple2<Integer, Long> peerHWeight = Controller.getInstance().getHWeightOfPeer(peer);
					if (peerHWeight == null || !peerHWeight.a.equals(myHeight)) {
						continue;
					}
				}
				
				//EXCLUDE PEERS
				if(exclude == null || !exclude.contains(peer))
				{
					peer.setMessageQueue(message);
					/*
					try {
						Thread.sleep(1000);
					}
					catch (Exception e) {
					}
					*/

				}
			}	
		}
		catch(Exception e)
		//else
		{
			//error broadcasting
			//LOGGER.error(e.getMessage(),e);
		}
		
		LOGGER.debug("ASYNC Broadcasting end " + message.viewType());
	}

	public void broadcast(Message message, List<Peer> exclude, boolean onlySynchronized) 
	{		
		//LOGGER.info(Lang.getInstance().translate("Broadcasting"));
		Controller cnt = Controller.getInstance();
		BlockChain chain = cnt.getBlockChain();
		Integer myHeight = chain.getHWeightFull(DCSet.getInstance()).a;
		
		try
		{
			for(int i=0; i < this.knownPeers.size() ; i++)
			{
				
				if (!this.run)
					return;
				
				Peer peer = this.knownPeers.get(i);
				if (peer == null || !peer.isUsed()) {
					continue;
				}
				
				if (onlySynchronized) {
					// USE PEERS than SYNCHRONIZED to ME
					Tuple2<Integer, Long> peerHWeight = Controller.getInstance().getHWeightOfPeer(peer);
					if (peerHWeight == null || !peerHWeight.a.equals(myHeight)) {
						continue;
					}
				}
				
				//EXCLUDE PEERS
				if(exclude == null || !exclude.contains(peer))
				{
					long start = System.currentTimeMillis();
					if (message.getType() == Message.WIN_BLOCK_TYPE) {
						int length = message.toBytes().length;
						LOGGER.debug("SEND WIN_BLOCK_TYPE to " + peer.getAddress()+ " bytes length (kB): " + (length>>10) );
					}
					peer.sendMessage(message);
					if (message.getType() == Message.WIN_BLOCK_TYPE) {
						long tickets = System.currentTimeMillis() - start;
						LOGGER.debug("SENDED time " + tickets);
					}
				}
			}
		}
		catch(Exception e)
		{
			//error broadcasting
			//LOGGER.error(e.getMessage(),e);
		}
		
		//LOGGER.info(Lang.getInstance().translate("Broadcasting end"));
	}

	@Override
	public void addObserver(Observer o)
	{
		super.addObserver(o);
		
		//SEND CONNECTEDPEERS ON REGISTER
		o.update(this, new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, this.knownPeers));
	}
	
	public static boolean isPortAvailable(int port)
	{
		try 
		{
		    ServerSocket socket = new ServerSocket(port);
		    socket.close();
		    return true;
		} 
		catch (Exception e)
		{
		    return false;
		}
	}

	public static boolean isMyself(InetAddress address)
	{
		
		if (myselfAddress != null
				&& myselfAddress.getHostAddress().equals(address.getHostAddress())) {
		    return true;
		}
	    return false;
	}

	public void stop() 
	{
		this.run = false;
		this.onMessage(null);
		while (!this.knownPeers.isEmpty()) {
			try {
				this.knownPeers.get(0).close();
				this.knownPeers.remove(0); // icreator
			} catch (Exception e) {

			}
		}
		// stop thread
		this.acceptor.halt();
		// wait for thread stop;
		while(this.acceptor.isAlive());
		// stop thread
		this.creator.halt();
	}
}
