package utils;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import controller.Controller;
import network.Peer;

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
