package database;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
 import org.apache.log4j.Logger;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedBytes;

import database.DBMap;
import database.DBSet;
import network.Peer;
import ntp.NTP;
import settings.Settings;
import utils.PeerInfoComparator;
import utils.ReverseComparator;

public class PeerMap extends DBMap<byte[], byte[]> 
{
	private static final byte[] BYTE_WHITELISTED = new byte[]{0, 0};
	//private static final byte[] BYTE_BLACKLISTED = new byte[]{1, 1};
	private static final byte[] BYTE_NOTFOUND = new byte[]{2, 2};
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	static Logger LOGGER = Logger.getLogger(PeerMap.class.getName());
	
	public PeerMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], byte[]> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("peers")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}

	@Override
	protected Map<byte[], byte[]> getMemoryMap() 
	{
		return new TreeMap<byte[], byte[]>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected byte[] getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public List<Peer> getKnownPeers(int amount)
	{
		try
		{
			//GET ITERATOR
			Iterator<byte[]> iterator = this.getKeys().iterator();
			
			//PEERS
			List<Peer> peers = new ArrayList<Peer>();
			
			//ITERATE AS LONG AS:
			// 1. we have not reached the amount of peers
			// 2. we have read all records
			while(iterator.hasNext() && peers.size() < amount)
			{
				//GET ADDRESS
				byte[] addressBI = iterator.next();
				
				//CHECK IF ADDRESS IS WHITELISTED
				if(Arrays.equals(Arrays.copyOfRange(this.get(addressBI), 0, 2), BYTE_WHITELISTED))
				{
					InetAddress address = InetAddress.getByAddress(addressBI);
					
					//CHECK IF SOCKET IS NOT LOCALHOST
					if(!Settings.getInstance().isLocalAddress(address))
					{
						//CREATE PEER
						Peer peer = new Peer(address);	
						
						//ADD TO LIST
						peers.add(peer);
					}
				}			
			}
			
			//RETURN
			return peers;
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(),e);
			
			return new ArrayList<Peer>();
		}
	}
	
	
	public class PeerInfo {

		static final int TIMESTAMP_LENGTH = 8; 
		static final int STATUS_LENGTH = 2; 
		
		private byte[] address;
		private byte[] status;
		private long findingTime;
		private long whiteConnectTime;
		private long grayConnectTime;
		private long whitePingCouner;
		private long banTime;
		
		public byte[] getAddress(){
			return address;
		}
		
		public byte[] getStatus(){
			return status;
		}

		public long getFindingTime(){
			return findingTime;
		}
		
		public long getWhiteConnectTime(){
			return whiteConnectTime;
		}
		
		public long getGrayConnectTime(){
			return grayConnectTime;
		}
		
		public long getWhitePingCouner(){
			return whitePingCouner;
		}
		public long getBanTime(){
			return banTime;
		}
		public void setBanTime(long toTime){
			banTime = toTime;
		}

		public PeerInfo(byte[] address, byte[] data) {
			
			if(data != null && data.length == STATUS_LENGTH + TIMESTAMP_LENGTH * 5) {
				int position = 0;
				
				byte[] statusBytes = Arrays.copyOfRange(data, position, position + STATUS_LENGTH);
				position += STATUS_LENGTH;
				
				byte[] findTimeBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
				long longFindTime = Longs.fromByteArray(findTimeBytes);
				position += TIMESTAMP_LENGTH;

				byte[] whiteConnectTimeBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
				long longWhiteConnectTime = Longs.fromByteArray(whiteConnectTimeBytes);
				position += TIMESTAMP_LENGTH;
				
				byte[] grayConnectTimeBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
				long longGrayConnectTime = Longs.fromByteArray(grayConnectTimeBytes);
				position += TIMESTAMP_LENGTH;
				
				byte[] whitePingCounerBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
				long longWhitePingCouner = Longs.fromByteArray(whitePingCounerBytes);
				position += TIMESTAMP_LENGTH;

				byte[] banTimeBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);

				this.address = address;
				this.status = statusBytes;
				this.findingTime = longFindTime;
				this.whiteConnectTime = longWhiteConnectTime;
				this.grayConnectTime = longGrayConnectTime;
				this.whitePingCouner = longWhitePingCouner;
				this.banTime = Longs.fromByteArray(banTimeBytes);
				
			} else if (Arrays.equals(data, BYTE_WHITELISTED)) {
				this.address = address;
				this.status = BYTE_WHITELISTED;
				this.findingTime = 0;
				this.whiteConnectTime = 0;
				this.grayConnectTime = 0;
				this.whitePingCouner = 0;
				this.banTime = 0;
				
				this.updateFindingTime();
				
			} else {
				this.address = address;
				this.status = BYTE_NOTFOUND;
				this.findingTime = 0;
				this.whiteConnectTime = 0;
				this.grayConnectTime = 0;
				this.whitePingCouner = 0;
				this.banTime = 0;
				
				this.updateFindingTime();
			}
		} 
		
		public void addWhitePingCouner(int n){
			this.whitePingCouner += n;
		}
		
		public void updateWhiteConnectTime(){
			this.whiteConnectTime = NTP.getTime();
		}
		
		public void updateGrayConnectTime(){
			this.grayConnectTime = NTP.getTime();
		}
		
		public void updateFindingTime(){
			this.findingTime = NTP.getTime();
		}
		
		public byte[] toBytes(){

			byte[] findTimeBytes = Longs.toByteArray(this.findingTime);
			findTimeBytes = Bytes.ensureCapacity(findTimeBytes, TIMESTAMP_LENGTH, 0);
			
			byte[] whiteConnectTimeBytes = Longs.toByteArray(this.whiteConnectTime);
			whiteConnectTimeBytes = Bytes.ensureCapacity(whiteConnectTimeBytes, TIMESTAMP_LENGTH, 0);
			
			byte[] grayConnectTimeBytes = Longs.toByteArray(this.grayConnectTime);
			grayConnectTimeBytes = Bytes.ensureCapacity(grayConnectTimeBytes, TIMESTAMP_LENGTH, 0);
			
			byte[] whitePingCounerBytes = Longs.toByteArray(this.whitePingCouner);
			whitePingCounerBytes = Bytes.ensureCapacity(whitePingCounerBytes, TIMESTAMP_LENGTH, 0);

			byte[] banTimeBytes = Longs.toByteArray(this.banTime);
			banTimeBytes = Bytes.ensureCapacity(banTimeBytes, TIMESTAMP_LENGTH, 0);

			return Bytes.concat(this.status, findTimeBytes, whiteConnectTimeBytes, grayConnectTimeBytes, whitePingCounerBytes, banTimeBytes);	
		}
	}

	public List<Peer> getBestPeers(int amount, boolean allFromSettings)
	{
		try
		{
			//PEERS
			List<Peer> peers = new ArrayList<Peer>();
			List<PeerInfo> listPeerInfo = new ArrayList<PeerInfo>();
			
			//////// GET first all WHITE PEERS
			try
			{
				//GET ITERATOR
				Iterator<byte[]> iterator = this.getKeys().iterator();
				
				//ITERATE AS LONG AS:

				// 1. we have not reached the amount of peers
				// 2. we have read all records
				while(iterator.hasNext() && listPeerInfo.size() < amount)
				{
					//GET ADDRESS
					byte[] addressBI = iterator.next();
					
					//CHECK IF ADDRESS IS WHITELISTED
					
					byte[] data = this.get(addressBI);
					
					try
					{
						PeerInfo peerInfo = new PeerInfo(addressBI, data);
						
						InetAddress address = InetAddress.getByAddress(peerInfo.getAddress());
						//CHECK IF SOCKET IS NOT LOCALHOST
						if(Settings.getInstance().isLocalAddress(address))
							continue;
						
						if(Arrays.equals(peerInfo.getStatus(), BYTE_WHITELISTED)
								&& peerInfo.banTime < NTP.getTime()) {
							listPeerInfo.add(peerInfo);
						}
					} catch (Exception e) {
						LOGGER.error(e.getMessage(),e);
					}
				}
				Collections.sort(listPeerInfo, new ReverseComparator<PeerInfo>(new PeerInfoComparator())); 
					
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}
			
			for (PeerInfo peerInfo: listPeerInfo) {
				InetAddress address = InetAddress.getByAddress(peerInfo.getAddress());
				peers.add(new Peer(address));				
			}
			
			int cnt = peers.size();
			// show for mySelp in start only
			if (cnt > 0 && allFromSettings)
				LOGGER.info("White peers loaded from database : " + cnt);
			
			if(allFromSettings) {
				List<Peer> knownPeers = Settings.getInstance().getKnownPeers();
				LOGGER.info("Peers loaded from settings and internet: " + knownPeers.size());
				
				int insertIndex = 0;
				for (Peer knownPeer : knownPeers) {
					try
					{
						if(!allFromSettings && peers.size() >= amount)
							break;
						
						int i = 0;
						int found = -1;
						for (Peer peer : peers) {
							if(peer.getAddress().equals(knownPeer.getAddress()))
							{
								found = i;
								break;
							}
							i++;
						}
					
						if (found == -1){
							//ADD TO LIST
							if (peers.size() > insertIndex) {
								peers.add(insertIndex, knownPeer);
							} else {
								peers.add(knownPeer);
							}
							//peers.add(knownPeer);
						} else {
							// REMOVE from this PLACE
							peers.remove(found);
							// ADD in TOP
							if (peers.size() > insertIndex) {
								peers.add(insertIndex, knownPeer);
							} else {
								peers.add(knownPeer);
							}
						}
						insertIndex++;
						
					} catch (Exception e) {
						LOGGER.error(e.getMessage(),e);
					}
				}
				
			}
			
			//////// GET in end all  PEERS in DB

			cnt = peers.size();			
			//GET ITERATOR
			Iterator<byte[]> iterator = this.getKeys().iterator();
			
			//ITERATE AS LONG AS:

			// 1. we have not reached the amount of peers
			// 2. we have read all records
			while(iterator.hasNext() && peers.size() < amount)
			{
				//GET ADDRESS
				byte[] addressBI = iterator.next();
				boolean found = false;
				for (Peer peer: peers) {
					if (Arrays.equals(peer.getAddress().getAddress(), addressBI)) {
						found = true;
						break;
					}
				}
				
				if (found)
					continue;
				
				//CHECK IF ADDRESS IS NOT BANNED
				
				byte[] data = this.get(addressBI);
				
				try
				{
					PeerInfo peerInfo = new PeerInfo(addressBI, data);
					InetAddress address = InetAddress.getByAddress(peerInfo.getAddress());
					peers.add(new Peer(address));
				} catch (Exception e) {
					LOGGER.error(e.getMessage(),e);
				}
			}

			// Show only for mySelf on start
			if(allFromSettings)
				LOGGER.info("Peers loaded from database : " + (peers.size() - cnt));

			//RETURN
			return peers;
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(),e);
			
			return new ArrayList<Peer>();
		}	
	}

	public List<String> getAllPeersAddresses(int amount) {
		try
		{
			List<String> addresses = new ArrayList<String>();
			Iterator<byte[]> iterator = this.getKeys().iterator();
			while(iterator.hasNext() && (amount == -1 || addresses.size() < amount))
			{
				byte[] addressBI = iterator.next();
				addresses.add(InetAddress.getByAddress(addressBI).getHostAddress());
			}
			return addresses;
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(),e);
				
			return new ArrayList<String>();
		}
	}
	
	public List<PeerInfo> getAllPeers(int amount)
	{
		try
		{
			//GET ITERATOR
			Iterator<byte[]> iterator = this.getKeys().iterator();
			
			//PEERS
			List<PeerInfo> peers = new ArrayList<PeerInfo>();
			
			//ITERATE AS LONG AS:
			// 1. we have not reached the amount of peers
			// 2. we have read all records
			while(iterator.hasNext() && peers.size() < amount)
			{
				//GET ADDRESS
				byte[] addressBI = iterator.next();
				byte[] data = this.get(addressBI);
				
				peers.add(new PeerInfo(addressBI, data));
			}
			
			//SORT
			Collections.sort(peers, new ReverseComparator<PeerInfo>(new PeerInfoComparator())); 
			
			//RETURN
			return peers;
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(),e);
			
			return new ArrayList<PeerInfo>();
		}
	}
	
	
	public void addPeer(Peer peer, int banMinutes)
	{
		if(this.map == null){
			return;
		}
		
		PeerInfo peerInfo;
		byte[] address = peer.getAddress().getAddress();
		
		if(this.map.containsKey(address))
		{
			byte[] data = this.map.get(address);
			peerInfo = new PeerInfo(address, data);
		}
		else
		{
			peerInfo = new PeerInfo(address, null);
		}
		
		if(banMinutes > 0) {
			// add ban timer by minutes
			peerInfo.banTime = NTP.getTime() + banMinutes * 60000;
		} else {
			peerInfo.banTime = 0;		
		}
		
		if(peer.getPingCounter() > 1)
		{
			if(peer.isWhite())
			{
				peerInfo.addWhitePingCouner(1);
				peerInfo.updateWhiteConnectTime();
			}
			else
			{
				peerInfo.updateGrayConnectTime();
			}
		}
		
		//ADD PEER INTO DB
		this.map.put(address, peerInfo.toBytes());
	}
	
	public PeerInfo getInfo(InetAddress address) 
	{
		byte[] addressByte = address.getAddress();

		if(this.map == null){
			return new PeerInfo(addressByte, BYTE_NOTFOUND);
		}
		
		if(this.map.containsKey(addressByte))
		{
			byte[] data = this.map.get(addressByte);
			
			return new PeerInfo(addressByte, data);
		}
		return new PeerInfo(addressByte, BYTE_NOTFOUND);
	}
	
	public boolean isBanned(byte[] key)
	{
		//CHECK IF PEER IS BLACKLISTED
		if(this.contains(key))
		{
			byte[] data = this.map.get(key);			
			PeerInfo peerInfo = new PeerInfo(key, data);
			
			if (peerInfo.banTime < NTP.getTime()) {
				peerInfo.setBanTime(0);
				return false;
			} else {
				return true;
			}
		}
			
		return false;
	}

	public boolean isBanned(InetAddress address)
	{
		//CHECK IF PEER IS BLACKLISTED
		return isBanned(address.getAddress());
	}
	public int getBanMinutes(Peer peer)
	{
		
		byte[] key = peer.getAddress().getAddress();
		
		//CHECK IF PEER IS BLACKLISTED
		if(this.contains(key))
		{
			byte[] data = this.map.get(key);			
			PeerInfo peerInfo = new PeerInfo(key, data);
			
			if (peerInfo.getBanTime() > NTP.getTime()) {
				return (int)((peerInfo.getBanTime() - NTP.getTime()) / 60000);
			} else {
				return 0;
			}
		}
			
		return 0;
	}
	
	public boolean isBad(InetAddress address)
	{
		byte[] addressByte = address.getAddress();

		//CHECK IF PEER IS BAD
		if(this.contains(addressByte))
		{
			byte[] data = this.map.get(addressByte);
			
			PeerInfo peerInfo = new PeerInfo(addressByte, data);
			
			boolean findMoreWeekAgo = (NTP.getTime() - peerInfo.getFindingTime() > 7*24*60*60*1000);  
			
			boolean neverWhite = peerInfo.getWhitePingCouner() == 0;
			
			return findMoreWeekAgo && neverWhite;
		}
			
		return false;
	}
}
