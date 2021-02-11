package org.erachain.database;

import org.erachain.core.item.assets.TradePair;
import org.erachain.dbs.DBTab;
import org.mapdb.Fun;

public interface PairMap extends DBTab<Fun.Tuple2<Long, Long>, TradePair> {
    TradePair get(Long key1, Long key2);

    TradePair get(TradePair tradePair);

    void put(TradePair tradePair);

    void delete(TradePair tradePair);
}
