package org.erachain.dbs.nativeMemMap;

import java.util.HashMap;

public class BHashMap extends HashMap {

    BHashMap() {
        super();
    }

    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

}
