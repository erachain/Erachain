package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import controller.Controller;
import core.BlockChain;
import core.Synchronizer;
import core.transaction.Transaction;
import database.DBSet;
import database.PeerMap;
import datachain.DCSet;
import network.message.Message;
import network.message.TelegramMessage;
import ntp.NTP;
import settings.Settings;
import utils.Pair;

public class TelegramManager extends Thread {

	private static final int MAX_HANDLED_TELEGRAMS_SIZE = BlockChain.HARD_WORK?4096<<4:4096;

	private Network network;
	private boolean isRun;
	
	static Logger LOGGER = Logger.getLogger(TelegramManager.class.getName());

	// pool of messages
	private TreeMap<String, Message> handledTelegrams;
	// timestamp lists for clear
	private TreeMap<Long, List<String>> telegramsTime;

	public TelegramManager(Network network)
	{
	
		this.network = network;
		this.handledTelegrams = new TreeMap<String, Message>();
		this.telegramsTime = new TreeMap<Long, List<String>>();

	}
	
	public void run()
	{
		this.isRun = true;
		
		
		while(this.isRun && !this.isInterrupted())
		{
			try{ 
				Thread.sleep(100);	
			} catch(InterruptedException  es) {
			}

			synchronized(this.handledTelegrams)
			{
				long timestamp = NTP.getTime();
				List<String> timeSignatures = this.telegramsTime.get(timestamp);
				if (timeSignatures == null) {
					timeSignatures = new ArrayList<String>();
				} else {
					timeSignatures.add(key);
				}
			}
		}
	}
	
	public void halt()
	{		
		this.isRun = false;
	}

	public List<Peer> getBestPeers()
	{
		return Controller.getInstance().getDBSet().getPeerMap().getBestPeers(Settings.getInstance().getMaxSentPeers(), false);
	}
	
	
	public List<Peer> getKnownPeers()
	{
		List<Peer> knownPeers = new ArrayList<Peer>();
		//ASK DATABASE FOR A LIST OF PEERS
		if(!Controller.getInstance().isOnStopping()){
			knownPeers = Controller.getInstance().getDBSet().getPeerMap().getBestPeers(Settings.getInstance().getMaxReceivePeers(), true);
		}
		
		//RETURN
		return knownPeers;
	}
	
	public void addPeer(Peer peer, int banForMinutes)
	{
		//ADD TO DATABASE
		if(!Controller.getInstance().isOnStopping()){
			Controller.getInstance().getDBSet().getPeerMap().addPeer(peer, banForMinutes);
		}
	}
		
	public boolean isBanned(Peer peer)
	{
		return Controller.getInstance().getDBSet().getPeerMap().isBanned(peer.getAddress());
	}
	
	public boolean addTelegram(TelegramMessage message)
	{
		
		Transaction transaction = message.getTransaction();
		
		// CHECK IF SIGNATURE IS VALID OR GENESIS TRANSACTION
		if (transaction.getCreator() == null
				|| !transaction.isSignatureValid(DCSet.getInstance())) {
			// DISHONEST PEER
			this.network.tryDisconnect(message.getSender(), Synchronizer.BAN_BLOCK_TIMES, "ban PeerOnError - invalid telegram signature");
			return true;
		}

		synchronized(this.handledTelegrams)
		{
			//CHECK IF LIST IS FULL
			if(this.handledTelegrams.size() > MAX_HANDLED_TELEGRAMS_SIZE)
			{
				List<String> signatires = this.telegramsTime.remove(this.telegramsTime.firstKey());
				for (String signature: signatires) {
					this.handledTelegrams.remove(signature);
					///LOGGER.error("handledMessages size OVERHEAT! "); 						
				}
			}
			
			String key = java.util.Base64.getEncoder().encodeToString(transaction.getSignature());
			
			Message old_value = this.handledTelegrams.put(key, message);
			if (old_value != null)
				return true;
			
			long timestamp = transaction.getTimestamp();
			List<String> timeSignatures = this.telegramsTime.get(timestamp);
			if (timeSignatures == null) {
				timeSignatures = new ArrayList<String>();
			} else {
				timeSignatures.add(key);
			}
			
			this.telegramsTime.put(timestamp, timeSignatures);
		}
		

		return false;
	}

}
