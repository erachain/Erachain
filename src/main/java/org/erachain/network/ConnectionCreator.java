package org.erachain.network;
// 30/03

import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.network.message.PeersMessage;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.erachain.settings.Settings;

import java.util.List;

/**
 * класс поиска каналов связи - подключается к внешним узлам создавая пиры
 * смотрит сколько соединений уже есть и если еще недостаточно то цепляется ко всему что сможет
 */
public class ConnectionCreator extends Thread {

    private static final Logger LOGGER = LoggerFactory            .getLogger(ConnectionCreator.class);
    private ConnectionCallback callback;
    private boolean isRun;

    public ConnectionCreator(ConnectionCallback callback) {
        this.callback = callback;
        this.setName("Thread ConnectionCreator - " + this.getId());
    }

    public void run() {
        this.isRun = true;

        int sleep_time = 0;

        List<Peer> knownPeers = null;
        int needloadCountMax = 1000;
        int needloadCount = needloadCountMax;

        while (isRun) {

            try {

                Thread.sleep(100);

                int maxReceivePeers = 4; // Settings.getInstance().getMaxReceivePeers();

                //CHECK IF WE NEED NEW CONNECTIONS
                if (this.isRun && Settings.getInstance().getMinConnections() >= callback.getActivePeersCounter(true)) {

                    needloadCount++;
                    if (needloadCount > needloadCountMax) {
                        // try update banned peers each minute
                        //GET LIST OF KNOWN PEERS
                        knownPeers = PeerManager.getInstance().getKnownPeers();
                        needloadCount = 0;
                    }

                    //ITERATE knownPeers
                    for (Peer peer : knownPeers) {
                        if (Network.isMyself(peer.getAddress())) {
                            continue;
                        }

                        if (!this.isRun)
                            return;

                        //CHECK IF WE ALREADY HAVE MIN CONNECTIONS
                        if (Settings.getInstance().getMinConnections() <= callback.getActivePeersCounter(true)) {
                            // stop use KNOWN peers
                            break;
                        }

                        //CHECK IF SOCKET IS NOT LOCALHOST
                        //if(true)
                        if (peer.getAddress().isSiteLocalAddress()
                                || peer.getAddress().isLoopbackAddress()
                                || peer.getAddress().isAnyLocalAddress()) {
                            continue;
                        }

                        //CHECK IF ALREADY CONNECTED TO PEER
                        //CHECK IF PEER ALREADY used
                        // new PEER from NETWORK poll or original from DB
                        peer = callback.getKnownPeer(peer);
                        if (peer.isUsed()) {
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
									.replace("%activeConnections%", String.valueOf(callback.getActivePeersCounter(false)))
									);
									*/

                        //CONNECT
                        //CHECK IF ALREADY CONNECTED TO PEER
                        if (peer.connect(callback) == 0) {
                        }

                    }
                }

                //CHECK IF WE STILL NEED NEW CONNECTIONS
                // USE unknown peers from known peers
                if (this.isRun && Settings.getInstance().getMinConnections() >= callback.getActivePeersCounter(true)) {
                    //OLD SCHOOL ITERATE activeConnections
                    //avoids Exception when adding new elements
                    List<Peer> peers = callback.getActivePeers(false);
                    for (int i = 0; i < callback.getActivePeersCounter(false); i++) {
                        if (!this.isRun)
                            return;

                        Peer peer = peers.get(i);
                        if (peer.isBanned())
                            continue;

                        //CHECK IF WE ALREADY HAVE MAX CONNECTIONS for WHITE
                        if (Settings.getInstance().getMinConnections() <= callback.getActivePeersCounter(true) << 1)
                            break;

                        //ASK PEER FOR PEERS
                        Message getPeersMessage = MessageFactory.getInstance().createGetPeersMessage();
                        long start = System.currentTimeMillis();
                        PeersMessage peersMessage = (PeersMessage) peer.getResponse(getPeersMessage);
                        if (peersMessage != null) {
                            peer.setPing((int) (System.currentTimeMillis() - start));

                            int foreignPeersCounter = 0;
                            //FOR ALL THE RECEIVED PEERS

                            for (Peer newPeer : peersMessage.getPeers()) {

                                if (!this.isRun)
                                    return;

                                if (Network.isMyself(newPeer.getAddress())) {
                                    continue;
                                }
                                //CHECK IF WE ALREADY HAVE MAX CONNECTIONS for WHITE
                                if (Settings.getInstance().getMinConnections() <= callback.getActivePeersCounter(true) << 1)
                                    break;

                                if (foreignPeersCounter >= maxReceivePeers) {
                                    // FROM EACH peer get only maxReceivePeers
                                    break;
                                }

                                //CHECK IF THAT PEER IS NOT BLACKLISTED
                                if (PeerManager.getInstance().isBanned(newPeer))
                                    continue;

                                //CHECK IF SOCKET IS NOT LOCALHOST
                                if (newPeer.getAddress().isSiteLocalAddress()
                                        || newPeer.getAddress().isLoopbackAddress()
                                        || newPeer.getAddress().isAnyLocalAddress())
                                    continue;

                                if (!Settings.getInstance().isTryingConnectToBadPeers() && newPeer.isBad())
                                    continue;

                                //CHECK IF ALREADY CONNECTED TO PEER
                                //CHECK IF PEER ALREADY used
                                newPeer = callback.getKnownPeer(newPeer);
                                if (newPeer.isUsed()) {
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
										.replace("%activeConnections%", String.valueOf(callback.getActivePeersCounter(false)))
										);
										*/


                                //CONNECT
                                newPeer.connect(callback);
                                if (newPeer.isUsed()) {
                                    foreignPeersCounter++;
                                }
                            }
                        }
                    }
                }

                //SLEEP
                int counter = callback.getActivePeersCounter(false);
                if (counter < 6)
                    continue;
                else if (counter < 10)
                    Thread.sleep(10000);
                else
                    Thread.sleep(20000);

            } catch (Exception e) {
                //LOGGER.error(e.getMessage(),e);
            }
        }
    }

    public void halt() {
        this.isRun = false;
    }
}
