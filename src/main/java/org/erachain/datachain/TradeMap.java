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

    List<Trade> getInitiatedTrades(Order order);

    List<Trade> getTradesByOrderID(Long orderID);

    @SuppressWarnings("unchecked")
    List<Trade> getTrades(long haveWant)
    // get trades for order as HAVE and as WANT
    ;

    @SuppressWarnings("unchecked")
    List<Trade> getTrades(long have, long want, Object offset, int limit, boolean useCancel);

    @SuppressWarnings("unchecked")
    Trade getLastTrade(long have, long want, boolean andCancel);

    List<Trade> getTradesByTimestamp(long startTimestamp, long stopTimestamp, int limit);
    List<Trade> getTradesByTimestamp(long have, long want, long startTimestamp, long stopTimestamp, int limit);

    List<Trade> getTradesByHeight(int start, int stop, int limit);
    List<Trade> getTradesByHeight(long have, long want, int start, int stop, int limit);

    List<Trade> getTradesFromTradeID(long[] startTradeID, int limit);

    List<Trade> getTradesByTradeID(long[] startTradeID, int limit);

    List<Trade> getTradesByOrderID(long startOrderID, long stopOrderID, int limit);
    List<Trade> getTradesByOrderID(long have, long want, long startOrderID, long stopOrderID, int limit);

    BigDecimal getVolume24(long have, long want);

    void delete(Trade trade);
}
