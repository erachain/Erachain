package org.erachain.datachain;

import org.erachain.dbs.IteratorCloseable;
import org.mapdb.Fun;

public interface TradeSuit {

    IteratorCloseable<Fun.Tuple2<Long, Long>> getIteratorByInitiator(Long orderID);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getIteratorByTarget(Long orderID);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getHaveIterator(long have);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getWantIterator(long want);

    /**
     * Обратная сортировка - для просмотра последних
     * @param have
     * @param want
     * @return
     */
    IteratorCloseable<Fun.Tuple2<Long, Long>> getPairIteratorDesc(long have, long want);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getPairHeightIterator(int startHeight, int stopHeight);
    IteratorCloseable<Fun.Tuple2<Long, Long>> getPairHeightIterator(long have, long want, int startHeight, int stopHeight);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getIteratorFromID(long[] startTradeID);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getPairOrderIDIterator(long startOrderID, long stopOrderID);
    IteratorCloseable<Fun.Tuple2<Long, Long>> getPairOrderIDIterator(long have, long want, long startOrderID, long stopOrderID);

}
