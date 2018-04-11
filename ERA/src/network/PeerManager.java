package network;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import controller.Controller;
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
		return Controller.getInstance().getDBSet().getPeerMap().getBestPeers(Settings.getInstance().getMaxSentPeers()<<2, false);
	}
	
	
	public List<Peer> getKnownPeers()
	{
		List<Peer> knownPeers = new ArrayList<Peer>();
		//ASK DATABASE FOR A LIST OF PEERS
		if(!Controller.getInstance().isOnStopping()){
			knownPeers = Controller.getInstance().getDBSet().getPeerMap().getBestPeers(Settings.getInstance().getMaxReceivePeers()<<2, true);
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
}
