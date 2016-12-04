package network;
// 30/03
import java.util.List;

import lang.Lang;
import network.message.Message;
import network.message.MessageFactory;
import network.message.PeersMessage;

import org.apache.log4j.Logger;

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
				if(this.isRun && Settings.getInstance().getMinConnections() >= callback.getActivePeers().size())
				{			
					//GET LIST OF KNOWN PEERS
					List<Peer> knownPeers = PeerManager.getInstance().getKnownPeers();
					
					//int knownPeersCounter = 0;
										
					//ITERATE knownPeers
					for(Peer peer: knownPeers)
					{
						if (Network.isMyself(peer.getAddress())) {
							continue;
						}
							
						//knownPeersCounter ++;
	
						//CHECK IF WE ALREADY HAVE MAX CONNECTIONS
						if(!this.isRun)
							return;
						
						if(Settings.getInstance().getMaxConnections() <= callback.getActivePeers().size()) {
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
						
						if (!this.isRun)
							return;

						/*
						LOGGER.info(
								Lang.getInstance().translate("Connecting to known peer %peer% :: %knownPeersCounter% / %allKnownPeers% :: Connections: %activeConnections%")
									.replace("%peer%", peer.getAddress().getHostAddress())
									.replace("%knownPeersCounter%", String.valueOf(knownPeersCounter))
									.replace("%allKnownPeers%", String.valueOf(knownPeers.size()))
									.replace("%activeConnections%", String.valueOf(callback.getActiveConnections().size()))
									);
						*/

						//CONNECT
						//CHECK IF ALREADY CONNECTED TO PEER
						peer.connect(callback);							
					}
				}
				
				//CHECK IF WE STILL NEED NEW CONNECTIONS
				if(this.isRun && Settings.getInstance().getMinConnections() >= callback.getActivePeers().size())
				{
					//OLD SCHOOL ITERATE activeConnections
					//avoids Exception when adding new elements
					for(int i=0; i<callback.getActivePeers().size(); i++)
					{
						if (!this.isRun)
							return;

						Peer peer = callback.getActivePeers().get(i);
	
						//CHECK IF WE ALREADY HAVE MAX CONNECTIONS
						
						if(Settings.getInstance().getMaxConnections() <= callback.getActivePeers().size())
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
								//CHECK IF WE ALREADY HAVE MAX CONNECTIONS
								if(Settings.getInstance().getMaxConnections() <= callback.getActivePeers().size())
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
										.replace("%activeConnections%", String.valueOf(callback.getActiveConnections().size()))
										);
								*/

								//CONNECT
								newPeer.connect(callback);
							}
						}
					}
				}			
				//SLEEP
				int sleep_time = 1;
				if (callback.getActivePeers().size()<3)
					sleep_time = 3;
				else if (callback.getActivePeers().size()< (Settings.getInstance().getMaxConnections()>>1))
					sleep_time = 10;
				else if (callback.getActivePeers().size()< Settings.getInstance().getMaxConnections())
					sleep_time = 20;
				else 
					sleep_time = 40;
					
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
