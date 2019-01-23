package org.erachain.network;

import org.erachain.controller.Controller;
import org.erachain.database.PeerMap;
import org.erachain.settings.Settings;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;

/**
 * приемник содинения - отвечает на входящие запросы и создает канал связи
 * запускает первый слушатель и ждет входящего соединения
 */
public class ConnectionAcceptor extends MonitoredThread {

    static Logger LOGGER = LoggerFactory.getLogger(ConnectionAcceptor.class.getName());
    private Network network;
    private ServerSocket socket;
    private boolean isRun;

    public ConnectionAcceptor(Network network) {
        this.network = network;
        this.setName("ConnectionAcceptor - " + this.getId());
    }

    public void run() {
        this.isRun = true;

        Random random = new Random();

        PeerMap map = Controller.getInstance().getDBSet().getPeerMap();
        this.initMonitor();
        while (this.isRun && !this.isInterrupted()) {
            this.setMonitorPoint();

            // на всякий случай чтобы атак не было с созданием множества конектов
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }

            Socket connectionSocket = null;
            try {

                if (socket == null) {
                    //START LISTENING
                    socket = new ServerSocket(Controller.getInstance().getNetworkPort());
                }

                //REOPEN SOCKET
                if (socket.isClosed()) {
                    socket = new ServerSocket(Controller.getInstance().getNetworkPort());
                }

                //ACCEPT CONNECTION
                this.setMonitorStatusBefore("socket.accept");
                connectionSocket = socket.accept();
                this.setMonitorStatusAfter();

                //CHECK IF SOCKET IS NOT LOCALHOST || WE ARE ALREADY CONNECTED TO THAT SOCKET || BLACKLISTED
                if (
                        map.isBanned(connectionSocket.getInetAddress().getAddress())
                        ) {
                    //DO NOT CONNECT TO OURSELF/EXISTING CONNECTION
                    // or BANNED
                    connectionSocket.shutdownOutput();
                    connectionSocket.close();
                    continue;
                }
            } catch (java.lang.OutOfMemoryError e) {
                Controller.getInstance().stopAll(90);
                break;
            } catch (java.net.SocketException e) {
                continue;

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);

                try {
                    socket.close();
                } catch (Exception e1) {
                }

                socket = null;
                continue;

            }

            if (!this.isRun)
                break;

            if (connectionSocket == null)
                continue;

            try {
                //CREATE PEER
                ////new Peer(callback, connectionSocket);
                //LOGGER.info("START ACCEPT CONNECT FROM " + connectionSocket.getInetAddress().getHostAddress()
                //		+ " isMy:" + Network.isMyself(connectionSocket.getInetAddress())
                //		+ " my:" + Network.getMyselfAddress());

                setMonitorStatusBefore("startPeer");
                Peer peer = network.startPeer(connectionSocket);
                setMonitorStatusAfter();
                if (!peer.isUsed()) {
                    // если в процессе
                    //if (!peer.isBanned() || connectionSocket.isClosed()) {
                    //    peer.ban("WROND ACCEPT");
                    //}

                    continue;
                }

                //CHECK IF WE HAVE MAX CONNECTIONS CONNECTIONS
                if (Settings.getInstance().getMaxConnections() <= network.getActivePeersCounter(false)) {
                    // get only income peers;
                    List<Peer> incomePeers = network.getIncomedPeers();
                    if (incomePeers != null && !incomePeers.isEmpty()) {
                        Peer peerForBan = incomePeers.get(random.nextInt((incomePeers.size())));
                        setMonitorStatusBefore("peerForBan.ban");
                        peerForBan.ban(10, "Clear place for new connection");
                        setMonitorStatusAfter();
                    }
                }
            } catch (java.lang.OutOfMemoryError e) {
                Controller.getInstance().stopAll(89);
                break;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

        }

        setMonitorStatus("halted");
        LOGGER.info("halted");

    }

    public void halt() {
        //this.interrupt();
        this.isRun = false;

        LOGGER.info("on halt");

        setMonitorStatusBefore("halt socket.close");
        try {
            socket.close();
        } catch (IOException e) {
        }
        setMonitorStatusAfter();

    }
}
