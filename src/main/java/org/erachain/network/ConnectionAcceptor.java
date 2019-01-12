package org.erachain.network;

import org.erachain.controller.Controller;
import org.erachain.database.PeerMap;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.erachain.settings.Settings;
import sun.nio.ch.Net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;

/**
 * приемник содинения - отвечает на входящие запросы и создает канал связи
 * запускает первый слушатель и ждет входящего соединения
 */
public class ConnectionAcceptor extends Thread {

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
        while (this.isRun && !this.isInterrupted()) {

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
                Socket connectionSocket = socket.accept();

                //CHECK IF SOCKET IS NOT LOCALHOST || WE ARE ALREADY CONNECTED TO THAT SOCKET || BLACKLISTED
                if (
                        map.isBanned(connectionSocket.getInetAddress().getAddress())
                        ) {
                    //DO NOT CONNECT TO OURSELF/EXISTING CONNECTION
                    // or BANNED
                    connectionSocket.close();
                    continue;
                }

                if (!this.isRun)
                    break;

                //CREATE PEER
                ////new Peer(callback, connectionSocket);
                //LOGGER.info("START ACCEPT CONNECT FROM " + connectionSocket.getInetAddress().getHostAddress()
                //		+ " isMy:" + Network.isMyself(connectionSocket.getInetAddress())
                //		+ " my:" + Network.getMyselfAddress());

                Peer peer = network.startPeer(connectionSocket);
                if (!peer.isUsed()) {
                    // если в процессе
                    if (!peer.isBanned() || connectionSocket.isClosed())
                        peer.ban("WROND ACCEPT");
                }

                //CHECK IF WE HAVE MAX CONNECTIONS CONNECTIONS
                if (Settings.getInstance().getMaxConnections() <= network.getActivePeersCounter(false)) {
                    // get only income peers;
                    List<Peer> incomePeers = network.getIncomedPeers();
                    if (incomePeers != null && !incomePeers.isEmpty()) {
                        Peer peerForBan = incomePeers.get(random.nextInt((incomePeers.size())));
                        peerForBan.ban(10, "Clear place for new connection");
                    }
                }

            } catch (Exception e) {

                try {
                    socket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                //LOGGER.info(e.getMessage(),e);
                //LOGGER.info(Lang.getInstance().translate("Error accepting new connection") + " - " + e.getMessage());
                break;
            }
            if (this.isInterrupted()) break;
        }

    }

    public void halt() {
        //this.interrupt();
        this.isRun = false;

        try {
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
