package network;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import database.DBSet;
import ntp.NTP;
import settings.Settings;
import utils.Pair;

public class PeerManager {

	private static PeerManager instance;
	//private Map<String, Long> blacListeddWait = new TreeMap<String, Long>(); // bat not time
	//private final static long banTime = 3 * 60 * 60 * 1000;

	
	public static PeerManager getInstance()
	{
		if(instance == null)
		{
			instance = new PeerManager();
		}
		
		return instance;
	}
	
	private PeerManager()
	{
		
	}
	
	public List<Peer> getBestPeers()
	{
		return DBSet.getInstance().getPeerMap().getBestPeers(Settings.getInstance().getMaxSentPeers(), false);
	}
	
	
	public List<Peer> getKnownPeers()
	{
		List<Peer> knownPeers = new ArrayList<Peer>();
		//ASK DATABASE FOR A LIST OF PEERS
		if(!DBSet.getInstance().isStoped()){
			knownPeers = DBSet.getInstance().getPeerMap().getBestPeers(Settings.getInstance().getMaxReceivePeers(), true);
		}
		
		//RETURN
		return knownPeers;
	}
	
	public void addPeer(Peer peer, int banForMinutes)
	{
		//ADD TO DATABASE
		if(!DBSet.getInstance().isStoped()){
			DBSet.getInstance().getPeerMap().addPeer(peer, banForMinutes);
		}
	}
	
	/*
	public boolean isBlacklisted(InetAddress address)
	{
		if(DBSet.getInstance().isStoped())
			return true;
			
		byte[] key = address.getAddress();
		String keyStr = address.getHostAddress();
		if(DBSet.getInstance().getPeerMap().isBlacklisted(key)) {
			// THIS address in MAP
			
			if( blacListeddWait.containsKey(keyStr)) {
				long startBan = blacListeddWait.get(keyStr);
				if (NTP.getTime() - startBan < banTime) {
					return true;
				}
				blacListeddWait.remove(keyStr);
				DBSet.getInstance().getPeerMap().delete(key);
			}

		}
		
		
		return false;
	}
	*/
	
	public boolean isBlacklisted(Peer peer)
	{
		return DBSet.getInstance().getPeerMap().isBlacklisted(peer.getAddress());
	}
}
