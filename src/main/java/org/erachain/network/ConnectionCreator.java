package org.erachain.network;
// 30/03

import org.erachain.core.BlockChain;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.network.message.PeersMessage;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

//
/**
 * класс поиска каналов связи - подключается к внешним узлам создавая пиры
 * смотрит сколько соединений во вне (white) уже есть и если еще недостаточно то цепляется ко всему что сможет
 */
public class ConnectionCreator extends Thread {

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

    private int connectToPeersOfThisPeer(Peer peer, int maxReceivePeers) {

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

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
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
            newPeer = network.getKnownPeer(newPeer);
            if (newPeer.isUsed()) {
                continue;
            }

            if (newPeer.isBanned())
                continue;

            if (!this.isRun)
                return 0;

            //CONNECT
            if (!newPeer.isOnUsed())
                newPeer.connect(network,"connected in recurse +++ ");

            if (newPeer.isUsed()) {
                // RECURSE to OTHER PEERS
                foreignPeersCounter++;
                connectToPeersOfThisPeer(newPeer, maxReceivePeers >> 1);

            }
        }

        return foreignPeersCounter;

    }

    public void run() {
        this.isRun = true;

        List<Peer> knownPeers = null;

        while (isRun) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }

            if (!this.isRun)
                return;

            this.setName("ConnectionCreator - " + this.getId()
                    + " white:" + network.getActivePeersCounter(true)
                    + " total:" + network.getActivePeersCounter(false));

            //CHECK IF WE NEED NEW CONNECTIONS
            if (this.isRun && Settings.getInstance().getMinConnections() > network.getActivePeersCounter(true)) {

                //GET LIST OF KNOWN PEERS
                knownPeers = PeerManager.getInstance().getKnownPeers();

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
                        return;

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
                    peer = network.getKnownPeer(peer);
                    if (peer.isUsed()) {
                        continue;
                    }

                    if (peer.isBanned())
                        continue;

                    if (!this.isRun)
                        return;

                    LOGGER.info("try connect to: " + peer);

                    /*
                    LOGGER.info(
                            Lang.getInstance().translate("Connecting to known peer %peer% :: %knownPeersCounter% / %allKnownPeers% :: Connections: %activeConnections%")
                                .replace("%peer%", peer)
                                .replace("%knownPeersCounter%", String.valueOf(knownPeersCounter))
                                .replace("%allKnownPeers%", String.valueOf(knownPeers.size()))
                                .replace("%activeConnections%", String.valueOf(callback.getActivePeersCounter(true)))
                                );
                                */

                    //CONNECT
                    //CHECK IF ALREADY CONNECTED TO PEER
                    if (!peer.isOnUsed())
                        peer.connect(network, "connected +++ ");

                    if (peer.isUsed()) {
                        // TRY CONNECT to WHITE peers of this PEER
                        connectToPeersOfThisPeer(peer, 4);
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
                        return;

                    if (peer.isBanned())
                        continue;

                    if (Settings.getInstance().getMinConnections() <= network.getActivePeersCounter(true)) {
                        break;
                    }

                    long timesatmp = NTP.getTime();
                    if (timesatmp - getPeersTimestamp > GET_PEERS_PERIOD) {
                        connectToPeersOfThisPeer(peer, Settings.getInstance().getMaxConnections());
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
                return;

            try {
                if (counter < needMinConnections)
                    Thread.sleep(BlockChain.DEVELOP_USE ? 10000 : 1000);
                else
                    Thread.sleep(60000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }

        }
    }

    public void halt() {
        this.isRun = false;
        LOGGER.info("on halt");
    }
}
