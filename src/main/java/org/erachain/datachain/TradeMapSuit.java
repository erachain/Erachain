package org.erachain.datachain;

import org.erachain.core.item.assets.Order;
import org.mapdb.Fun;

import java.util.Iterator;

public interface TradeMapSuit {

    Iterator<Fun.Tuple2> getIterator(Order order);
    Iterator<Fun.Tuple2<Long, Long>> getReverseIterator(Long orderID);
    Iterator<Fun.Tuple2<Long, Long>> getHaveIterator(long have);
    Iterator<Fun.Tuple2<Long, Long>> getWantIterator(long want);
    Iterator<Fun.Tuple2<Long, Long>> getPairIterator(long have, long want);
    Iterator<Fun.Tuple2<Long, Long>> getPairTimestampIterator(long have, long want, long timestamp);
    Iterator<Fun.Tuple2<Long, Long>> getPairHeightIterator(long have, long want, int heightStart);

}
