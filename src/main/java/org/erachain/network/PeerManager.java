package org.erachain.network;

import org.erachain.controller.Controller;
import org.erachain.network.message.Message;
import org.erachain.settings.Settings;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PeerManager extends MonitoredThread {

    private Network network;

    private static final Logger LOGGER = LoggerFactory.getLogger(PeerManager.class);

    private static final int QUEUE_LENGTH = 20;
    BlockingQueue<Peer> blockingQueue = new ArrayBlockingQueue<Peer>(QUEUE_LENGTH);

    public PeerManager(Network network) {
        this.network = network;
    }

    public static PeerManager getInstance() {
        return null;
    }

    public List<Peer> getBestPeers() {
        return Controller.getInstance().getDBSet().getPeerMap().getBestPeers(Settings.getInstance().getMaxSentPeers() << 2, false);
    }


    public List<Peer> getKnownPeers() {
        List<Peer> knownPeers = new ArrayList<Peer>();
        //ASK DATABASE FOR A LIST OF PEERS
        if (!Controller.getInstance().isOnStopping()) {
            knownPeers = Controller.getInstance().getDBSet().getPeerMap().getBestPeers(
                    Settings.getInstance().getMaxReceivePeers() << 2, true);
        }

        //RETURN
        return knownPeers;
    }

    public void addPeer(Peer peer, int banForMinutes) {
        //ADD TO DATABASE
        if (!Controller.getInstance().isOnStopping()) {
            Controller.getInstance().getDBSet().getPeerMap().addPeer(peer, banForMinutes);
        }
    }

    public boolean isBanned(Peer peer) {
        return Controller.getInstance().getDBSet().getPeerMap().isBanned(peer.getAddress());
    }

    private void processPeers(Peer peer) {
    }

    public void run() {

        //Controller cnt = Controller.getInstance();

        Message message = null;

        while (this.network != null) {
            try {
                processPeers(blockingQueue.take());
            } catch (java.lang.OutOfMemoryError e) {
                Controller.getInstance().stopAll(86);
                break;
            } catch (java.lang.IllegalMonitorStateException e) {
                break;
            } catch (java.lang.InterruptedException e) {
                break;
            }

        }

        LOGGER.info("WinBlock Selector halted");

    }

}


    public void halt() {
        this.network = null;
    }

}
