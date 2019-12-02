package org.erachain.datachain;

import org.erachain.core.item.assets.Order;
import org.mapdb.Fun;

import java.util.Iterator;

public interface TradeSuit {

    Iterator<Fun.Tuple2<Long, Long>> getIterator(Order order);

    Iterator<Fun.Tuple2<Long, Long>> getIteratorByKeys(Long orderID);

    Iterator<Fun.Tuple2<Long, Long>> getTargetsIterator(Long orderID);

    Iterator<Fun.Tuple2<Long, Long>> getHaveIterator(long have);

    Iterator<Fun.Tuple2<Long, Long>> getWantIterator(long want);

    Iterator<Fun.Tuple2<Long, Long>> getPairIterator(long have, long want);

    Iterator<Fun.Tuple2<Long, Long>> getPairHeightIterator(long have, long want, int startHeight, int stopHeight);

    Iterator<Fun.Tuple2<Long, Long>> getPairOrderIDIterator(long have, long want, long startOrderID, long stopOrderID);

}
