package network;
// 30/03
import java.util.List;

import lang.Lang;
import network.message.Message;
import network.message.MessageFactory;
import network.message.PeersMessage;

import org.apache.log4j.Logger;

import controller.Controller;
import settings.Settings;

public class ConnectionCreator extends Thread {

	private ConnectionCallback callback;
	private boolean isRun;
	
	private static final Logger LOGGER = Logger
			.getLogger(ConnectionCreator.class);
	public ConnectionCreator(ConnectionCallback callback)
	{
		this.callback = callback;
	}
	
	public void run()
	{
		this.isRun = true;

		int sleep_time = 0;

		while(isRun)
		{
			
			try
			{	

				int maxReceivePeers = 4; // Settings.getInstance().getMaxReceivePeers();
				
				//CHECK IF WE NEED NEW CONNECTIONS
				if(this.isRun && Settings.getInstance().getMinConnections() >= callback.getActivePeers(true).size())
				{			
					
					// try update banned peers each minute 
					//GET LIST OF KNOWN PEERS
					List<Peer> knownPeers = PeerManager.getInstance().getKnownPeers();
										
					//ITERATE knownPeers
					for(Peer peer: knownPeers)
					{
						if (Network.isMyself(peer.getAddress())) {
							continue;
						}
	
						if(!this.isRun)
							return;
						
						//CHECK IF WE ALREADY HAVE MIN CONNECTIONS
						if(Settings.getInstance().getMinConnections() <= callback.getActivePeers(true).size()) {
							// stop use KNOWN peers
							break;
						}
												
						//CHECK IF SOCKET IS NOT LOCALHOST
						//if(true)
						if(peer.getAddress().isSiteLocalAddress() 
								|| peer.getAddress().isLoopbackAddress()
								|| peer.getAddress().isAnyLocalAddress()) {
							continue;
						}

						//CHECK IF ALREADY CONNECTED TO PEER
						//CHECK IF PEER ALREADY used
						// new PEER from NETWORK poll or original from DB
						peer = callback.getKnownPeer(peer);
						if(peer.isUsed()) {
							continue;
						}
						
						if (peer.isBanned())
							continue;

						if (!this.isRun)
							return;

						/*
						LOGGER.info(
								Lang.getInstance().translate("Connecting to known peer %peer% :: %knownPeersCounter% / %allKnownPeers% :: Connections: %activeConnections%")
									.replace("%peer%", peer.getAddress().getHostAddress())
									.replace("%knownPeersCounter%", String.valueOf(knownPeersCounter))
									.replace("%allKnownPeers%", String.valueOf(knownPeers.size()))
									.replace("%activeConnections%", String.valueOf(callback.getActivePeers(false).size()))
									);
									*/

						//CONNECT
						//CHECK IF ALREADY CONNECTED TO PEER
						peer.connect(callback);
					}
				}
				
				//CHECK IF WE STILL NEED NEW CONNECTIONS
				// USE unknown peers from known peers
				if(this.isRun && Settings.getInstance().getMinConnections() >= callback.getActivePeers(true).size())
				{
					//OLD SCHOOL ITERATE activeConnections
					//avoids Exception when adding new elements
					for(int i=0; i<callback.getActivePeers(false).size(); i++)
					{
						if (!this.isRun)
							return;

						Peer peer = callback.getActivePeers(false).get(i);
						if (peer.isBanned())
							continue;
	
						//CHECK IF WE ALREADY HAVE MAX CONNECTIONS for WHITE					
						if(Settings.getInstance().getMinConnections() <= callback.getActivePeers(true).size()<<1)
							break;
						
						//ASK PEER FOR PEERS
						Message getPeersMessage = MessageFactory.getInstance().createGetPeersMessage();
						PeersMessage peersMessage = (PeersMessage) peer.getResponse(getPeersMessage);
						if(peersMessage != null)
						{
							int foreignPeersCounter = 0;
							//FOR ALL THE RECEIVED PEERS
							
							for(Peer newPeer: peersMessage.getPeers())
							{
								
								if (!this.isRun)
									return;

								if (Network.isMyself(newPeer.getAddress())) {
									continue;
								}
								//CHECK IF WE ALREADY HAVE MAX CONNECTIONS for WHITE
								if(Settings.getInstance().getMinConnections() <= callback.getActivePeers(true).size()<<1)
									break;

								if(foreignPeersCounter >= maxReceivePeers) {
									// FROM EACH peer get only maxReceivePeers
									break;
								}
								
								//CHECK IF THAT PEER IS NOT BLACKLISTED
								if(PeerManager.getInstance().isBanned(newPeer))
									continue;
								
								//CHECK IF SOCKET IS NOT LOCALHOST
								if(newPeer.getAddress().isSiteLocalAddress()
										|| newPeer.getAddress().isLoopbackAddress() 
										|| newPeer.getAddress().isAnyLocalAddress())
									continue;

								if(!Settings.getInstance().isTryingConnectToBadPeers() && newPeer.isBad())
									continue;
								
								//CHECK IF ALREADY CONNECTED TO PEER
								//CHECK IF PEER ALREADY used
								newPeer = callback.getKnownPeer(newPeer);
								if(newPeer.isUsed()) {
									continue;
								}
								
								// TODO small height not use
								//Controller.getInstance().getMyHeight();
								// newPeer.
								if (newPeer.isBanned())
									continue;
									
								if (!this.isRun)
									return;
								
								/*
								int maxReceivePeersForPrint = (maxReceivePeers > peersMessage.getPeers().size()) ? peersMessage.getPeers().size() : maxReceivePeers;  
								LOGGER.info(
									Lang.getInstance().translate("Connecting to peer %newpeer% proposed by %peer% :: %foreignPeersCounter% / %maxReceivePeersForPrint% / %allReceivePeers% :: Connections: %activeConnections%")
										.replace("%newpeer%", newPeer.getAddress().getHostAddress())
										.replace("%peer%", peer.getAddress().getHostAddress())
										.replace("%foreignPeersCounter%", String.valueOf(foreignPeersCounter))
										.replace("%maxReceivePeersForPrint%", String.valueOf(maxReceivePeersForPrint))
										.replace("%allReceivePeers%", String.valueOf(peersMessage.getPeers().size()))
										.replace("%activeConnections%", String.valueOf(callback.getActivePeers(false).size()))
										);
										*/

								
								//CONNECT
								newPeer.connect(callback);
								if (newPeer.isUsed()) {
									foreignPeersCounter ++;
								}
							}
						}
					}
				}
				
				//SLEEP
				int cnt = callback.getActivePeers(true).size();
				int minCnt = Settings.getInstance().getMinConnections();
				if (cnt < 4) {
					Thread.sleep(30000);
					continue;
				}
				else if (cnt < minCnt)
					// BANNES PEERS update each minute
					sleep_time = 1;
				else
					// sleep
					sleep_time = 10;
					
				Thread.sleep(sleep_time * 60000);

			}
			catch(Exception e)
			{
				//LOGGER.error(e.getMessage(),e);
			}					
		}
	}
	
	public void halt()
	{
		this.isRun = false;
	}
}
