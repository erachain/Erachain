package utils;

import org.mapdb.Fun.Tuple2;

import controller.Controller;

public class Chain {

	
	public static int hw_Test() {
		
		Controller cnt = Controller.getInstance();
		if (cnt.getPeerHWeights().size() == 0) {
			return 0;
		}

		Tuple2<Integer, Long> maxPeerWeight = cnt.getMaxPeerHWeight();
		return 1;
	}

}
