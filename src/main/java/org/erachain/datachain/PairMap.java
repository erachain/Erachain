package org.erachain.datachain;

import org.erachain.core.item.assets.Pair;
import org.erachain.dbs.DBTab;
import org.mapdb.Fun;

public interface PairMap extends DBTab<Fun.Tuple2<Long, Long>, Pair> {
    Pair get(Long key1, Long key2);

    Pair get(Pair pair);

    void put(Pair pair);

    void delete(Pair pair);
}
