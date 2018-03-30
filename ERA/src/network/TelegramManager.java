package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import controller.Controller;
import core.BlockChain;
import core.Synchronizer;
import core.account.Account;
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
	private static final int KEEP_TIME = 60000 * 10;

	private Network network;
	private boolean isRun;
	
	static Logger LOGGER = Logger.getLogger(TelegramManager.class.getName());

	// pool of messages
	private Map<String, TelegramMessage> handledTelegrams;
	// timestamp lists for clear
	private TreeMap<Long, List<String>> telegramsForTime;
	// lists for address
	private Map<String, List<String>> telegramsForAddress;

	public TelegramManager(Network network)
	{
	
		this.network = network;
		this.handledTelegrams = new HashMap<String, TelegramMessage>();
		this.telegramsForTime = new TreeMap<Long, List<String>>();
		this.telegramsForAddress = new HashMap<String, List<String>>();

	}

	// GET telegram
	public TelegramMessage getTelegram(String signature)
	{
		return handledTelegrams.get(signature);
	}
	
	// GET telegrams for RECIPIENT from TIME
	public List<TelegramMessage> getTelegramsForAddress(String address, long timestamp)
	{
		TelegramMessage telegram;
		List<TelegramMessage> telegrams = new ArrayList<TelegramMessage>();
		//ASK DATABASE FOR A LIST OF PEERS
		if(!Controller.getInstance().isOnStopping()) {
			List<String> keys = telegramsForAddress.get(address);
			for (String key: keys) {
				telegram = handledTelegrams.get(key);
				if (timestamp > 0) {
					if (telegram.getTransaction().getTimestamp() >= timestamp)
						telegrams.add(telegram);
				} else {
					telegrams.add(telegram);
				}
			}
		}
		
		//RETURN
		return telegrams;
	}

	// GET telegrams for RECIPIENT from TIME
	public List<TelegramMessage> getTelegramsFromTimestamp(long timestamp)
	{
		TelegramMessage telegram;
		List<TelegramMessage> telegrams = new ArrayList<TelegramMessage>();
		if(!Controller.getInstance().isOnStopping()) {
			
			SortedMap<Long, List<String>> subMap = telegramsForTime.tailMap(timestamp);
			for (Entry<Long, List<String>> item: subMap.entrySet()) {
				List<String> signatures = item.getValue();
				if (signatures != null) {
					for (String signature: signatures) {
						telegram = handledTelegrams.get(signature);
						telegrams.add(telegram);						
					}
				}
			}
		}
		
		//RETURN
		return telegrams;
	}

	public synchronized boolean addTelegram(TelegramMessage message)
	{
		
		String address;
		HashSet<Account> recipients;
		
		Transaction transaction = message.getTransaction();
		
		// CHECK IF SIGNATURE IS VALID OR GENESIS TRANSACTION
		Account creator = transaction.getCreator();
		if (creator == null
				|| !transaction.isSignatureValid(DCSet.getInstance())) {
			// DISHONEST PEER
			this.network.tryDisconnect(message.getSender(), Synchronizer.BAN_BLOCK_TIMES, "ban PeerOnError - invalid telegram signature");
			return true;
		}
		
		long timestamp = transaction.getTimestamp();
		if (timestamp > NTP.getTime() + 10000) {
			// DISHONEST PEER
			this.network.tryDisconnect(message.getSender(), Synchronizer.BAN_BLOCK_TIMES, "ban PeerOnError - invalid telegram timestamp >>");
			return true;			
		} else if (KEEP_TIME + timestamp < NTP.getTime()) {
			// DISHONEST PEER
			this.network.tryDisconnect(message.getSender(), Synchronizer.BAN_BLOCK_TIMES, "ban PeerOnError - invalid telegram timestamp <<");
			return true;			
		}

		//CHECK IF LIST IS FULL
		if(this.handledTelegrams.size() > MAX_HANDLED_TELEGRAMS_SIZE)
		{
			List<String> signatires = this.telegramsForTime.remove(this.telegramsForTime.firstKey());
			for (String signature: signatires) {
				this.handledTelegrams.remove(signature);
				///LOGGER.error("handledMessages size OVERHEAT! "); 						
			}
		}
			
		String signatureKey = java.util.Base64.getEncoder().encodeToString(transaction.getSignature());
		
		Message old_value = this.handledTelegrams.put(signatureKey, message.copy());
		if (old_value != null)
			return true;
		
		// MAP timestamps
		List<String> timeSignatures = this.telegramsForTime.get(timestamp);
		if (timeSignatures == null) {
			timeSignatures = new ArrayList<String>();
		}
		timeSignatures.add(signatureKey);
		this.telegramsForTime.put((Long)timestamp, timeSignatures);
		
		// MAP addresses
		recipients = transaction.getRecipientAccounts();
		if (recipients != null) {
			for (Account recipient: recipients) {
				address = recipient.getAddress();
				List<String> addressSignatures = this.telegramsForAddress.get(address);
				if (addressSignatures == null) {
					addressSignatures = new ArrayList<String>();
				}
				addressSignatures.add(signatureKey);
				this.telegramsForAddress.put(address, addressSignatures);
			}
		}

		return false;
	}

	public void run()
	{
		int i;
		this.isRun = true;
		TelegramMessage telegram;
		String address;
		HashSet<Account> recipients;
		
		while(this.isRun && !this.isInterrupted())
		{
			try{ 
				Thread.sleep(1000);	
			} catch(InterruptedException  es) {
				return;
			}

			long timestamp = NTP.getTime();

			synchronized(this.handledTelegrams)
			{
				
				do {
					Entry<Long, List<String>> firstItem = this.telegramsForTime.firstEntry();
					if (firstItem == null)
						break;
					
					long timeKey = firstItem.getKey();
					
					if (timeKey + KEEP_TIME < timestamp) {
						List<String> signatires = firstItem.getValue();
						// for all signatures on this TIME
						for (String signature: signatires) {
							telegram = this.handledTelegrams.remove(signature);
							if (telegram != null) {
								recipients = telegram.getTransaction().getRecipientAccounts();
								if (recipients != null) {
									for (Account recipient: recipients) {
										address = recipient.getAddress();
										List<String> addressSignatures = this.telegramsForAddress.get(address);
										if (addressSignatures != null) {
											i = 0;
											for (String item_sign: addressSignatures) {
												if (item_sign.equals(signature)) {
													addressSignatures.remove(i);
													break;
												}
												i++;
											}
										}
										// IF list is empty
										if (addressSignatures.isEmpty()) {
											this.telegramsForAddress.remove(address);
										} else {
											this.telegramsForAddress.put(address, addressSignatures);
										}
									}
								}
							}
						}
						this.telegramsForTime.remove(timeKey);
					} else {
						break;
					}
				} while (true);
			}
		}
	}
	
	public void halt()
	{		
		this.isRun = false;
	}

}
