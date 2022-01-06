package org.erachain.datachain;

import org.erachain.dbs.IteratorCloseable;
import org.mapdb.Fun;

public interface TimeTXintf<T, U> {

    IteratorCloseable<Fun.Tuple2<T, U>> getTXIterator(boolean descending);

}
