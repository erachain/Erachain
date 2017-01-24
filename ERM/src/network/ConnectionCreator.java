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

		while(isRun)
		{
			try
			{	

				Thread.sleep(50);	

				int maxReceivePeers = Settings.getInstance().getMaxReceivePeers();
				
				//CHECK IF WE NEED NEW CONNECTIONS
				if(this.isRun && Settings.getInstance().getMinConnections() >= callback.getActivePeers(true).size())
				{			
					//GET LIST OF KNOWN PEERS
					List<Peer> knownPeers = PeerManager.getInstance().getKnownPeers();
					
					int knownPeersCounter = 0;
										
					//ITERATE knownPeers
					for(Peer peer: knownPeers)
					{
						if (Network.isMyself(peer.getAddress())) {
							continue;
						}
							
						knownPeersCounter ++;
	
						if(!this.isRun)
							return;
						
						//CHECK IF WE ALREADY HAVE MAX CONNECTIONS
						if(Settings.getInstance().getMaxConnections() <= callback.getActivePeers(true).size()<<1) {
							try {
								Thread.sleep(10);
							}
							catch (Exception e) {		
							}
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
						
						if (PeerManager.getInstance().isBlacklisted(peer.getAddress()))
							continue;

						if (!this.isRun)
							return;

						
						LOGGER.info(
								Lang.getInstance().translate("Connecting to known peer %peer% :: %knownPeersCounter% / %allKnownPeers% :: Connections: %activeConnections%")
									.replace("%peer%", peer.getAddress().getHostAddress())
									.replace("%knownPeersCounter%", String.valueOf(knownPeersCounter))
									.replace("%allKnownPeers%", String.valueOf(knownPeers.size()))
									.replace("%activeConnections%", String.valueOf(callback.getActivePeers(false).size()))
									);
									

						//CONNECT
						//CHECK IF ALREADY CONNECTED TO PEER
						peer.connect(callback);							
					}
				}
				
				//CHECK IF WE STILL NEED NEW CONNECTIONS
				if(this.isRun && Settings.getInstance().getMinConnections() >= callback.getActivePeers(true).size())
				{
					//OLD SCHOOL ITERATE activeConnections
					//avoids Exception when adding new elements
					for(int i=0; i<callback.getActivePeers(true).size(); i++)
					{
						if (!this.isRun)
							return;

						Peer peer = callback.getActivePeers(true).get(i);
	
						//CHECK IF WE ALREADY HAVE MAX CONNECTIONS for WHITE					
						if(Settings.getInstance().getMaxConnections() <= callback.getActivePeers(true).size()<<1)
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
								if(Settings.getInstance().getMaxConnections() <= callback.getActivePeers(true).size()<<1)
									break;

								if(foreignPeersCounter >= maxReceivePeers) {
									break;
								}

								foreignPeersCounter ++;
								
								//CHECK IF THAT PEER IS NOT BLACKLISTED
								if(PeerManager.getInstance().isBlacklisted(newPeer))
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
								if (PeerManager.getInstance().isBlacklisted(newPeer.getAddress()))
									continue;
									
								if (!this.isRun)
									return;
								
								
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
										

								
								//CONNECT
								newPeer.connect(callback);
							}
						}
					}
				}			
				//SLEEP
				int sleep_time = 1;
				if (callback.getActivePeers(true).size()<3)
					sleep_time = 1;
				else if (callback.getActivePeers(true).size()< (Settings.getInstance().getMaxConnections()>>2))
					sleep_time = 5;
				else if (callback.getActivePeers(true).size()< Settings.getInstance().getMaxConnections()>>1)
					sleep_time = 10;
				else 
					sleep_time = 20;
					
				Thread.sleep(sleep_time * 1000);

			}
			catch(Exception e)
			{
				//LOGGER.error(e.getMessage(),e);
				
				LOGGER.info("Error creating new connection - " + e.getMessage());			
			}					
		}
	}
	
	public void halt()
	{
		this.isRun = false;
	}
}
