package org.erachain.network;

import org.apache.commons.net.util.SubnetUtils;
import org.erachain.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class LocalPeerScanner extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(LocalPeerScanner.class);

    Network network;

    public LocalPeerScanner(Network network) {
        this.network = network;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }
    public List<InetAddress> scanLocalNetForPeers(int port) throws IOException {

        List<InetAddress> result = new ArrayList<>();
        InetAddress localHost = Inet4Address.getLocalHost();

        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);

        int counter = 0;
        for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
            if (counter > 10)
                break;

            if (localHost.equals(address.getAddress())) {
                SubnetUtils utils = new SubnetUtils(address.getAddress().getHostAddress() + "/" + address.getNetworkPrefixLength());
                String[] allIps = utils.getInfo().getAllAddresses();
                for (int i = 0; i < allIps.length; i++) {

                    if (counter > 10)
                        break;

                    InetAddress host = InetAddress.getByName(allIps[i]);
                    if (localHost.equals(host) || network.isKnownAddress(host, false))
                        continue;
                    Socket scanSocket = new Socket();
                    try {
                        scanSocket.connect(new InetSocketAddress(host, port), 100);
                        //network.startPeer(scanSocket);
                        scanSocket.close();
                        Peer peer = new Peer(host);
                        //Peer peer = new Peer(network, scanSocket,"found new local Peer");
                        //network.onConnect(peer);
                        //network.addPeer(peer, 0);
                        peer.connect(null, network, " connect to local Peer");
                        result.add(host);
                        counter++;
                    }
                    catch(SocketTimeoutException e){

                    }
                }
            }
        }
        return result;
    }

    @Override
    public void run() {
        while (!isInterrupted()){
            try {
                scanLocalNetForPeers(Controller.getInstance().getNetworkPort());
                if (true)
                    break;

                sleep(60000);
            }
            catch (Exception e){
                logger.debug(e.getMessage());
            }
        }

    }
}
