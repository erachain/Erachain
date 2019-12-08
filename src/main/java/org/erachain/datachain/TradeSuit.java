package org.erachain.datachain;

import org.erachain.core.item.assets.Order;
import org.erachain.dbs.IteratorCloseable;
import org.mapdb.Fun;

public interface TradeSuit {

    IteratorCloseable<Fun.Tuple2<Long, Long>> getIterator(Order order);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getIteratorByKeys(Long orderID);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getTargetsIterator(Long orderID);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getHaveIterator(long have);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getWantIterator(long want);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getPairIterator(long have, long want);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getPairHeightIterator(long have, long want, int startHeight, int stopHeight);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getPairOrderIDIterator(long have, long want, long startOrderID, long stopOrderID);

}
