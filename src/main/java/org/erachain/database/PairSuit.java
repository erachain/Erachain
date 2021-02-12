package org.erachain.database;

import org.erachain.dbs.IteratorCloseable;
import org.mapdb.Fun;

public interface PairSuit {
    IteratorCloseable<Fun.Tuple2<Long, Long>> getIterator(long have);

}
