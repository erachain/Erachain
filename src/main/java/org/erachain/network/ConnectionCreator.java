package org.erachain.network;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.network.message.PeersMessage;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

//
/**
 * класс поиска каналов связи - подключается к внешним узлам создавая пиры
 * смотрит сколько соединений во вне (white) уже есть и если еще недостаточно то цепляется ко всему что сможет
 */
public class ConnectionCreator extends MonitoredThread {

    // как часто запрашивать все пиры у других пиров
    private static long GET_PEERS_PERIOD = 60 * 10 * 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionCreator.class.getSimpleName());
    private Network network;
    private static long getPeersTimestamp;

    public ConnectionCreator(Network network) {
        this.network = network;
        this.setName("ConnectionCreator - " + this.getId());
    }

    private int connectToPeersOfThisPeer(Peer peer, int maxReceivePeers, boolean onlyWhite) {

        if (!this.network.run)
            return 0;

        //CHECK IF WE ALREADY HAVE MAX CONNECTIONS for WHITE
        if (Settings.getInstance().getMinConnections() < network.getActivePeersCounter(true, false)
                || (Settings.getInstance().getMaxConnections() >> 1) < network.getActivePeersCounter(false, false))
            return 0;

        LOGGER.info("GET peers from: " + peer + " get max: " + maxReceivePeers);
        this.setMonitorStatus("recourse peer " + peer + " maxReceivePeers: " + maxReceivePeers);


        //ASK PEER FOR PEERS
        Message getPeersMessage = MessageFactory.getInstance().createGetPeersMessage();
        long start = System.currentTimeMillis();
        PeersMessage peersMessage = (PeersMessage) peer.getResponse(getPeersMessage);
        if (peersMessage == null) {
            return 0;
        }

        peer.setPing((int) (System.currentTimeMillis() - start));

        int foreignPeersCounter = 0;
        //FOR ALL THE RECEIVED PEERS

        for (Peer newPeer: peersMessage.getPeers()) {

            if (!this.network.run)
                return 0;

            // если произошел полныцй разрыв сети - то прекратим поиск по рекурсии
            if (network.noActivePeers(false) && foreignPeersCounter > 4)
                return 0;

            if (maxReceivePeers > 0 && foreignPeersCounter >= maxReceivePeers) {
                // FROM EACH peer get only maxReceivePeers
                break;
            }

            if (Network.isMyself(newPeer.getAddress())) {
                continue;
            }

            //CHECK IF WE ALREADY HAVE MAX CONNECTIONS for WHITE
            if (Settings.getInstance().getMinConnections() < network.getActivePeersCounter(true, false)
                    || (Settings.getInstance().getMaxConnections() >> 1) < network.getActivePeersCounter(false, false))
                break;

            //CHECK IF THAT PEER IS NOT BLACKLISTED
            if (newPeer.isBanned())
                continue;

            //CHECK IF SOCKET IS NOT LOCALHOST
            if (newPeer.getAddress().isAnyLocalAddress()
                    || newPeer.getAddress().isLoopbackAddress()) {
                continue;
            }
            if (newPeer.getAddress().isSiteLocalAddress()) {
                LOGGER.debug("Local peer: {}",newPeer.getAddress());
                //continue;
            }

            if (!Settings.getInstance().isTryingConnectToBadPeers() && newPeer.isBad())
                continue;

            try {
                Thread.sleep(100);
            } catch (java.lang.OutOfMemoryError e) {
                LOGGER.error(e.getMessage(), e);
                Controller.getInstance().stopAndExit(234);
                break;
            } catch (InterruptedException e) {
                break;
            }

            newPeer = network.getKnownPeer(newPeer, Network.ANY_TYPE);

            if (!network.isGoodForConnect(newPeer))
                continue;

            if (!this.network.run)
                return 0;

            // огрничим перебор тут так как в ответе из текущего пира может быть очень много стрых пиров
            // которые уже отвечают. Лучше это делать позже при последующей проверке
            foreignPeersCounter++;

            //CONNECT
            this.setMonitorStatusBefore("recourse peer " + peer + " maxReceivePeers: " + maxReceivePeers
                    + " foreignPeersCounter: " + foreignPeersCounter
                    + " try connect to " + newPeer);
            if (!newPeer.connect(null, network, "connected in recurse +++ "))
                continue;
            //network.tryConnection(null, newPeer, "connected in recurse +++ "); - тормозит коннект

            this.setMonitorStatusAfter();


            if (false) {
                // не надо - так внутри же все запускается!
                newPeer.setNeedPing();
            }

            if (newPeer.isUsed() && maxReceivePeers > 1) {
                // RECURSE to OTHER PEERS
                foreignPeersCounter++;
                connectToPeersOfThisPeer(newPeer, maxReceivePeers >> 1, onlyWhite);

            }
        }

        return foreignPeersCounter;

    }

    public void run() {

        List<Peer> knownPeers = null;

        this.initMonitor();
        while (this.network.run) {
            this.setMonitorPoint();

            try {
                Thread.sleep(100);
            } catch (java.lang.OutOfMemoryError e) {
                LOGGER.error(e.getMessage(), e);
                Controller.getInstance().stopAndExit(236);
                break;
            } catch (InterruptedException e) {
                break;
            }

            if (!this.network.run)
                break;

            this.setName("ConnectionCreator - " + this.getId()
                    + " white:" + network.getActivePeersCounter(true, false)
                    + " total:" + network.getActivePeersCounter(false, false));

            if (BlockChain.START_PEER != null) {
                // TRY CONNECT to WHITE peers of this PEER
                try {
                    Peer startPeer = new Peer(InetAddress.getByAddress(BlockChain.START_PEER));

                    connectToPeersOfThisPeer(startPeer, 1, true);
                } catch (Exception e) {

                }
            }

            //CHECK IF WE NEED NEW CONNECTIONS
            if (Settings.getInstance().getMinConnections() > network.getActivePeersCounter(true, false)) {

                if (network.localPeerScanner == null) {

                    //GET LIST OF KNOWN PEERS
                    knownPeers = network.getAllPeers();

                    //ITERATE knownPeers
                    for (Peer peer : knownPeers) {

                        //CHECK IF WE ALREADY HAVE MIN CONNECTIONS
                        if (Settings.getInstance().getMinConnections() <= network.getActivePeersCounter(true, false)) {
                            // stop use KNOWN peers
                            break;
                        }

                        if (Network.isMyself(peer.getAddress())) {
                            continue;
                        }

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            break;
                        }

                        if (!this.network.run)
                            break;

                        //CHECK IF SOCKET IS NOT LOCALHOST
                        //if(true)
                        if (peer.getAddress().isAnyLocalAddress()
                                || peer.getAddress().isLoopbackAddress()) {
                            continue;
                        }
                        if (peer.getAddress().isSiteLocalAddress()) {
                            //continue;
                            LOGGER.debug("Local peer: {}", peer.getAddress());
                        }

                        //CHECK IF PEER ALREADY used
                        // new PEER from NETWORK poll or original from DB
                        peer = network.getKnownPeer(peer, Network.ANY_TYPE);

                        if (!network.isGoodForConnect(peer))
                            continue;

                        LOGGER.info("try connect to: " + peer);

                        //CONNECT
                        this.setMonitorStatusBefore("peer.connect " + peer);
                        if (!peer.connect(null, network, "connected +++ "))
                            continue;

                        this.setMonitorStatusAfter();

                        if (peer.isUsed()) {

                            if (false) {
                                // не надо - так внутри же все запускается!
                                peer.setNeedPing();
                            }

                            // TRY CONNECT to WHITE peers of this PEER
                            connectToPeersOfThisPeer(peer, 4, true);
                        }
                    }

                    //CHECK IF WE STILL NEED NEW CONNECTIONS
                    // USE unknown peers from known peers
                    if (this.network.run && Settings.getInstance().getMinConnections() > network.getActivePeersCounter(true, false)) {
                        //OLD SCHOOL ITERATE activeConnections
                        //avoids Exception when adding new elements
                        List<Peer> peers = network.getActivePeers(false);
                        for (Peer peer : peers) {

                            if (!this.network.run)
                                break;

                            // если произошел полныцй разрыв сети - то прекратим поиск тут
                            if (network.noActivePeers(false))
                                break;

                            if (peer.isBanned())
                                continue;

                            if (Settings.getInstance().getMinConnections() <= network.getActivePeersCounter(true, false)) {
                                break;
                            }

                            long timesatmp = NTP.getTime();
                            if (timesatmp - getPeersTimestamp > GET_PEERS_PERIOD) {
                                connectToPeersOfThisPeer(peer, -1, false);
                                getPeersTimestamp = timesatmp;
                            }

                        }
                    }

                } else {
                    // only LOCALS
                    try {
                        network.localPeerScanner.scanLocalNetForPeers(BlockChain.NETWORK_PORT);
                    } catch (IOException e) {
                    }
                    continue;
                }
            }


            //SLEEP
            int counter = network.getActivePeersCounter(true, false);
            if (counter == 0
                    || counter < 6 && !BlockChain.TEST_MODE)
                continue;

            int needMinConnections = Settings.getInstance().getMinConnections();

            this.setName("Thread ConnectionCreator - " + this.getId() + " white:" + counter
                    + " total:" + network.getActivePeersCounter(false, false));

            if (!this.network.run)
                break;

            this.setMonitorStatus("sleep");

            try {
                if (counter < needMinConnections)
                    Thread.sleep(BlockChain.TEST_MODE ? 5000 : 5000);
                else
                    Thread.sleep(10000);
            } catch (java.lang.OutOfMemoryError e) {
                LOGGER.error(e.getMessage(), e);
                Controller.getInstance().stopAndExit(238);
                return;
            } catch (InterruptedException e) {
                break;
            }

        }

        this.setMonitorStatus("halted");
        LOGGER.info("halted");

    }

    public void halt() {
        LOGGER.info("on halt");
    }
}
