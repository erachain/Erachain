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

import java.util.List;

//
/**
 * класс поиска каналов связи - подключается к внешним узлам создавая пиры
 * смотрит сколько соединений во вне (white) уже есть и если еще недостаточно то цепляется ко всему что сможет
 */
public class ConnectionCreator extends MonitoredThread {

    // как часто запрашивать все пиры у других пиров
    private static long GET_PEERS_PERIOD = 60 * 10 * 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionCreator.class);
    private Network network;
    private static long getPeersTimestamp;
    private boolean isRun;

    public ConnectionCreator(Network network) {
        this.network = network;
        this.setName("ConnectionCreator - " + this.getId());
    }

    private int connectToPeersOfThisPeer(Peer peer, int maxReceivePeers, boolean onlyWhite) {

        if (!this.isRun)
            return 0;

        //CHECK IF WE ALREADY HAVE MAX CONNECTIONS for WHITE
        if (Settings.getInstance().getMinConnections() < network.getActivePeersCounter(true)
            || (Settings.getInstance().getMaxConnections() >> 1) < network.getActivePeersCounter(false))
            return 0;

        LOGGER.info("GET peers from: " + peer + " get max: " + maxReceivePeers);

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

            this.setMonitorStatus("peer.recurse " + newPeer.toString());

            if (!this.isRun)
                return 0;

            if (foreignPeersCounter >= maxReceivePeers) {
                // FROM EACH peer get only maxReceivePeers
                break;
            }

            if (Network.isMyself(newPeer.getAddress())) {
                continue;
            }

            //CHECK IF WE ALREADY HAVE MAX CONNECTIONS for WHITE
            if (Settings.getInstance().getMinConnections() < network.getActivePeersCounter(true)
                    || (Settings.getInstance().getMaxConnections() >> 1) < network.getActivePeersCounter(false))
                break;

            //CHECK IF THAT PEER IS NOT BLACKLISTED
            if (newPeer.isBanned())
                continue;

            //CHECK IF SOCKET IS NOT LOCALHOST
            if (newPeer.getAddress().isSiteLocalAddress()
                    || newPeer.getAddress().isLoopbackAddress()
                    || newPeer.getAddress().isAnyLocalAddress())
                continue;

            if (!Settings.getInstance().isTryingConnectToBadPeers() && newPeer.isBad())
                continue;

            try {
                Thread.sleep(100);
            } catch (java.lang.OutOfMemoryError e) {
                Controller.getInstance().stopAll(94);
                break;
            } catch (Exception e) {
            }

            newPeer = network.getKnownPeer(newPeer, Network.ANY_TYPE);

            if (!network.isGoodForConnect(newPeer))
                continue;

            if (!this.isRun)
                return 0;

            //CONNECT
            this.setMonitorStatusBefore("peer.connect.recurse");
            if (!newPeer.connect(null, network, "connected in recurse +++ "))
                continue;
            //network.tryConnection(null, newPeer, "connected in recurse +++ "); - тормозит коннект

            this.setMonitorStatusAfter();

            if (newPeer.isUsed()) {
                // RECURSE to OTHER PEERS
                foreignPeersCounter++;
                connectToPeersOfThisPeer(newPeer, maxReceivePeers >> 1, onlyWhite);

            }
        }

        return foreignPeersCounter;

    }

    public void run() {
        this.isRun = true;

        List<Peer> knownPeers = null;

        this.initMonitor();
        while (this.network.run) {
            this.setMonitorPoint();

            try {
                Thread.sleep(100);
            } catch (java.lang.OutOfMemoryError e) {
                Controller.getInstance().stopAll(96);
                break;
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }

            if (!this.isRun)
                break;

            this.setName("ConnectionCreator - " + this.getId()
                    + " white:" + network.getActivePeersCounter(true)
                    + " total:" + network.getActivePeersCounter(false));

            //CHECK IF WE NEED NEW CONNECTIONS
            if (this.isRun && Settings.getInstance().getMinConnections() > network.getActivePeersCounter(true)) {

                //GET LIST OF KNOWN PEERS
                knownPeers = network.getKnownPeers();

                //ITERATE knownPeers
                for (Peer peer : knownPeers) {

                    //CHECK IF WE ALREADY HAVE MIN CONNECTIONS
                    if (Settings.getInstance().getMinConnections() <= network.getActivePeersCounter(true)) {
                        // stop use KNOWN peers
                        break;
                    }

                    if (Network.isMyself(peer.getAddress())) {
                        continue;
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(),e);
                    }

                    if (!this.isRun)
                        break;

                    //CHECK IF SOCKET IS NOT LOCALHOST
                    //if(true)
                    if (peer.getAddress().isSiteLocalAddress()
                            || peer.getAddress().isLoopbackAddress()
                            || peer.getAddress().isAnyLocalAddress()) {
                        continue;
                    }

                    //CHECK IF PEER ALREADY used
                    // new PEER from NETWORK poll or original from DB
                    peer = network.getKnownPeer(peer, Network.ANY_TYPE);

                    if (!network.isGoodForConnect(peer))
                        continue;

                    if (!this.network.run)
                        break;

                    LOGGER.info("try connect to: " + peer);

                    //CONNECT
                    this.setMonitorStatusBefore("peer.connect");
                    if(!peer.connect(null, network, "connected +++ "))
                        continue;

                    this.setMonitorStatusAfter();

                    if (peer.isUsed()) {
                        // TRY CONNECT to WHITE peers of this PEER
                        connectToPeersOfThisPeer(peer, 4, true);
                    }
                }
            }

            //CHECK IF WE STILL NEED NEW CONNECTIONS
            // USE unknown peers from known peers
            if (this.isRun && Settings.getInstance().getMinConnections() > network.getActivePeersCounter(true)) {
                //OLD SCHOOL ITERATE activeConnections
                //avoids Exception when adding new elements
                List<Peer> peers = network.getActivePeers(false);
                for (Peer peer: peers) {

                    if (!this.isRun)
                        break;

                    if (peer.isBanned())
                        continue;

                    if (Settings.getInstance().getMinConnections() <= network.getActivePeersCounter(true)) {
                        break;
                    }

                    long timesatmp = NTP.getTime();
                    if (timesatmp - getPeersTimestamp > GET_PEERS_PERIOD) {
                        connectToPeersOfThisPeer(peer, Settings.getInstance().getMaxConnections(), false);
                        getPeersTimestamp = timesatmp;
                    }

                }
            }

            //SLEEP
            int counter = network.getActivePeersCounter(true);
            if (counter == 0
                    || counter < 6 && !BlockChain.DEVELOP_USE)
                continue;

            int needMinConnections = Settings.getInstance().getMinConnections();

            this.setName("Thread ConnectionCreator - " + this.getId() + " white:" + counter
                    + " total:" + network.getActivePeersCounter(false));

            if (!this.isRun)
                break;

            this.setMonitorStatus("sleep");

            try {
                if (counter < needMinConnections)
                    Thread.sleep(BlockChain.DEVELOP_USE ? 10000 : 5000);
                else
                    Thread.sleep(30000);
            } catch (java.lang.OutOfMemoryError e) {
                Controller.getInstance().stopAll(95);
                return;
            } catch (Exception e) {
            }

        }

        this.setMonitorStatus("halted");
        LOGGER.info("halted");

    }

    public void halt() {
        this.isRun = false;
        LOGGER.info("on halt");
    }
}
