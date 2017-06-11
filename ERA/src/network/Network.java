package network;
// 30/03 ++
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;
// import org.apache.log4j.Logger;
import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.BlockChain;
import core.transaction.Transaction;
import database.DBSet;
import database.TransactionMap;
import lang.Lang;
import network.message.FindMyselfMessage;
import network.message.Message;
import network.message.MessageFactory;
import settings.Settings;
import utils.ObserverMessage;

public class Network extends Observable implements ConnectionCallback {

	
	private static final int MAX_HANDLED_MESSAGES_SIZE = 10000;
	
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
		
		//PASS TO CONTROLLER
		Controller.getInstance().onConnect(peer);
		
		if(Controller.getInstance().isOnStopping())
			return;

		//NOTIFY OBSERVERS
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, peer));		
		
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, this.knownPeers));		
	}

	@Override
	public void tryDisconnect(Peer peer, int banForMinutes, String error) {

		if (!peer.isUsed())
			return;

		LOGGER.info("tryDisconnect : " + peer.getAddress().getHostAddress());
		if (banForMinutes != 0) { 
			LOGGER.info("     ban for minutes: " + banForMinutes);
			//ADD TO BLACKLIST
			PeerManager.getInstance().addPeer(peer, banForMinutes);
		}
		if (error != null) 
			LOGGER.info("     mess: " + error);
		
		//CLOSE CONNECTION IF STILL ACTIVE
		peer.close();
		
		//PASS TO CONTROLLER
		Controller.getInstance().afterDisconnect(peer);

		//NOTIFY OBSERVERS
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, peer));		
		
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
	
	private void addHandledMessage(byte[] hash)
	{
		try
		{
			synchronized(this.handledMessages)
			{
				//CHECK IF LIST IS FULL
				if(this.handledMessages.size() > MAX_HANDLED_MESSAGES_SIZE)
				{
					this.handledMessages.remove(this.handledMessages.first());
				}
				
				this.handledMessages.add(new String(hash));
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
				if(this.handledMessages.contains(new String(message.getHash())))
				{
					return;
				}
				
				//ADD TO HANDLED MESSAGES
				this.addHandledMessage(message.getHash());
			}
		}		
		
		switch(message.getType())
		{
		//PING
		case Message.PING_TYPE:
			
			//CREATE PING
			Message response = MessageFactory.getInstance().createPingMessage();
			
			//SET ID
			response.setId(message.getId());
			
			//SEND BACK TO SENDER
			message.getSender().sendMessage(response);
			
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
			
			if(Arrays.equals(findMyselfMessage.getFoundMyselfID(),Controller.getInstance().getFoundMyselfID())) {
				//LOGGER.info("network.onMessage - Connected to self. Disconnection.");
				
				Network.myselfAddress = message.getSender().getAddress(); 
				tryDisconnect(message.getSender(), 9999, null);
			}
			
			break;
			
		//SEND TO CONTROLLER
		default:
			
			Controller.getInstance().onMessage(message);
			break;
		}		
	}

	public void broadcast(Message message, List<Peer> exclude) 
	{		
		//LOGGER.info(Lang.getInstance().translate("Broadcasting"));
		
		try
		{
			for(int i=0; i < this.knownPeers.size() ; i++)
			{
				
				if (!this.run)
					return;
				
				Peer peer = this.knownPeers.get(i);
				if (!peer.isUsed()) {
					continue;
				}
				
				//EXCLUDE PEERS
				if(peer != null && (exclude == null || !exclude.contains(peer)))
				{
					peer.sendMessage(message);
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

	public void broadcastUnconfirmedToPeer(List<Transaction> transactions, Peer peer) 
	{		

		byte[] peerByte = peer.getAddress().getAddress();
		DBSet dbSet = DBSet.getInstance();
		TransactionMap dbMap = dbSet.getTransactionMap();
				
		for (Transaction transaction: transactions) {

			if (!this.run || !peer.isUsed()) {
				return;
			}
							
			Message message = MessageFactory.getInstance()
					.createTransactionMessage(transaction);

			if (dbMap.isBroadcastedToPeer(transaction, peerByte))
				continue;
			
			try
			{
				if (peer.sendMessage(message)) {
					dbMap.addBroadcastedPeer(transaction, peerByte);
				}
			} catch(Exception e)
			{
				LOGGER.error(e.getMessage(),e);
			}
		}
		
		LOGGER.info(Lang.getInstance().translate("Broadcasting end"));
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
		while (this.knownPeers.size() > 0) {
			try {
				this.knownPeers.get(0).close();
				this.knownPeers.remove(0); // icreator
			} catch (Exception e) {

			}
		}
		this.acceptor.halt();
		this.creator.halt();
	}
}
