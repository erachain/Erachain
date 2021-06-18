package org.erachain.datachain;

import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.List;

public interface TradeMap extends DBTab<Fun.Tuple2<Long, Long>, Trade> {
    void put(Trade trade);

    IteratorCloseable<Fun.Tuple2<Long, Long>> getIteratorByInitiator(Long orderID);

    IteratorCloseable<Fun.Tuple2<Long, Long>> iteratorByAssetKey(long haveWant, boolean descending);

    List<Trade> getInitiatedTrades(Order order, boolean useCancel);

    List<Trade> getTradesByOrderID(Long orderID, boolean useCancel, boolean descending);


    List<Trade> iteratorByAssetKey(long have, long want, Object offset, int limit, boolean useCancel, boolean useChange);

    Trade getLastTrade(long have, long want, boolean andCancel);

    List<Trade> getTradesByTimestamp(long startTimestamp, long stopTimestamp, int limit);

    List<Trade> getTradesByTimestamp(long have, long want, long startTimestamp, long stopTimestamp, int limit);

    List<Trade> getTradesFromToHeight(int start, int stop, int limit);

    List<Trade> getTradesFromToHeight(long have, long want, int start, int stop, int limit);

    List<Trade> getTradesFromTradeID(long[] startTradeID, int limit);

    List<Trade> getTradesFromToOrderID(long startOrderID, long stopOrderID, int limit, boolean useCancel);

    List<Trade> getTradesFromToOrderID(long have, long want, long startOrderID, long stopOrderID, int limit, boolean useCancel);

    BigDecimal getVolume24(long have, long want);

    void delete(Trade trade);
}
