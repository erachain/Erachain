package org.erachain.network;

import org.erachain.controller.Controller;
import org.erachain.database.PeerMap;
import org.apache.log4j.Logger;
import org.erachain.settings.Settings;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * приемник содинения - отвечает на входящие запросы и создает канал связи
 */
public class ConnectionAcceptor extends Thread {

    static Logger LOGGER = Logger.getLogger(ConnectionAcceptor.class.getName());
    private ConnectionCallback callback;
    private ServerSocket socket;
    private boolean isRun;

    public ConnectionAcceptor(ConnectionCallback callback) {
        this.callback = callback;
        this.setName("Thread ConnectionAcceptor - " + this.getId());
    }

    public void run() {
        this.isRun = true;


        PeerMap map = Controller.getInstance().getDBSet().getPeerMap();
        while (this.isRun && !this.isInterrupted()) {
            try {

                if (socket == null) {
                    //START LISTENING
                    socket = new ServerSocket(Controller.getInstance().getNetworkPort());
                }


                //CHECK IF WE HAVE MAX CONNECTIONS CONNECTIONS
                if (Settings.getInstance().getMaxConnections() <= callback.getActivePeersCounter(false)) {
                    //IF SOCKET IS OPEN CLOSE IT
                    if (!socket.isClosed()) {
                        socket.close();
                    }

                    //Thread.sleep(50);
                } else {
                    //REOPEN SOCKET
                    if (socket.isClosed()) {
                        socket = new ServerSocket(Controller.getInstance().getNetworkPort());
                    }

                    //ACCEPT CONNECTION
                    Socket connectionSocket = socket.accept();


                    //CHECK IF SOCKET IS NOT LOCALHOST || WE ARE ALREADY CONNECTED TO THAT SOCKET || BLACKLISTED
                    if (
                        /*connectionSocket.getInetAddress().isSiteLocalAddress()
                         * || connectionSocket.getInetAddress().isAnyLocalAddress()
                         * || connectionSocket.getInetAddress().isLoopbackAddress()
                         *  */
							
							/*
							(
									(NTP.getTime() < Transaction.getPOWFIX_RELEASE() ) 
									&& 
									callback.isConnectedTo(connectionSocket.getInetAddress())
							)
							||
							*/
                            map.isBanned(connectionSocket.getInetAddress().getAddress())
                            )

                    {
                        //DO NOT CONNECT TO OURSELF/EXISTING CONNECTION
                        // or BANNED
                        connectionSocket.close();
                    } else {

                        if (!this.isRun)
                            break;

                        //CREATE PEER
                        ////new Peer(callback, connectionSocket);
                        //LOGGER.info("START ACCEPT CONNECT FROM " + connectionSocket.getInetAddress().getHostAddress()
                        //		+ " isMy:" + Network.isMyself(connectionSocket.getInetAddress())
                        //		+ " my:" + Network.getMyselfAddress());

                        callback.startPeer(connectionSocket);
                    }
                }
            } catch (Exception e) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException es) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    break;
                }
                {
                }

                //LOGGER.info(e.getMessage(),e);
                //LOGGER.info(Lang.getInstance().translate("Error accepting new connection") + " - " + e.getMessage());
            }
            if (this.isInterrupted()) break;
        }

    }

    public void halt() {
        //this.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.isRun = false;
    }
}
