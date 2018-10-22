package org.erachain.utils;

import org.erachain.controller.Controller;
import org.erachain.network.Peer;
import org.mapdb.Fun.Tuple3;

public class Chain {


    public static int hw_Test() {

        Controller cnt = Controller.getInstance();
        if (cnt.getPeerHWeights().isEmpty()) {
            return 0;
        }

        Tuple3<Integer, Long, Peer> maxPeerWeight = cnt.getMaxPeerHWeight(0);
        return 1;
    }

}
