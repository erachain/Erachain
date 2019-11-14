package org.erachain.datachain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.TradeMapSuitMapDB;
import org.erachain.dbs.mapDB.TradeMapSuitMapDBFork;
import org.erachain.dbs.nativeMemMap.NativeMapTreeMapFork;
import org.erachain.dbs.rocksDB.TradeSuitRocksDB;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Хранит сделки на бирже
 * Ключ: ссылка на иницатора + ссылка на цель
 * Значение - Сделка
Initiator DBRef (Long) + Target DBRef (Long) -> Trade
 */
@Slf4j
public class TradeMapImpl extends DBTabImpl<Tuple2<Long, Long>, Trade> implements TradeMap {

    public TradeMapImpl(int dbs, DCSet databaseSet, DB database) {
        super(dbs, databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_TRADE_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_TRADE_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_TRADE_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_TRADE_TYPE);
        }
    }
    public TradeMapImpl(int dbs, TradeMap parent, DCSet dcSet) {
        super(dbs, parent, dcSet);
    }

    @Override
    public void openMap() {
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new TradeSuitRocksDB(databaseSet, database);
                    break;
                default:
                    map = new TradeMapSuitMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    //map = new BlocksSuitMapDBFotk((TransactionMap) parent, databaseSet);
                    map = new NativeMapTreeMapFork(parent, databaseSet, Fun.BYTE_ARRAY_COMPARATOR, null);
                    break;
                default:
                    map = new TradeMapSuitMapDBFork((TradeMap)parent, databaseSet);
            }
        }
    }

    @Override
    public void put(Trade trade) {
        this.put(new Tuple2<Long, Long>(trade.getInitiator(), trade.getTarget()), trade);
    }

    /**
     * поиск ключей для протокольных вторичных индексов с учетом Родительской таблицы (если база форкнута)
     * @param order
     * @return
     */
    @Override
    public Iterator<Tuple2<Long, Long>> getIterator(Order order) {
        return ((TradeSuit) this.map).getIterator(order);
    }

    @Override
    public List<Trade> getInitiatedTrades(Order order) {
        //FILTER ALL TRADES
        Iterator<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getIterator(order);

        //GET ALL TRADES FOR KEYS
        List<Trade> trades = new ArrayList<Trade>();
        while (iterator.hasNext()) {
            trades.add(this.get(iterator.next()));
        }

        //RETURN
        return trades;
    }

    @Override
    public List<Trade> getTradesByOrderID(Long orderID) {
        //ADD REVERSE KEYS
        if (Controller.getInstance().onlyProtocolIndexing) {
            return new ArrayList<>();
        }

        Iterator<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getIteratorByKeys(orderID);
        //GET ALL ORDERS FOR KEYS as INITIATOR
        List<Trade> trades = new ArrayList<Trade>();
        while (iterator.hasNext()) {
            trades.add(this.get(iterator.next()));
        }

        iterator = ((TradeSuit) this.map).getTargetsIterator(orderID);
        //GET ALL ORDERS FOR KEYS as TARGET
        while (iterator.hasNext()) {
            trades.add(this.get(iterator.next()));
        }

        //RETURN
        return trades;
    }

    @Override
    public List<Trade> getTrades(long haveWant)
    // get trades for order as HAVE and as WANT
    {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return new ArrayList<>();
        }

        Iterator<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getHaveIterator(haveWant);
        if (iterator == null)
            return new ArrayList<Trade>();

        iterator = Iterators.mergeSorted(ImmutableList.of(iterator,
                ((TradeSuit) this.map).getWantIterator(haveWant)), Fun.COMPARATOR);

        //GET ALL ORDERS FOR KEYS
        List<Trade> trades = new ArrayList<Trade>();
        while (iterator.hasNext()) {
            trades.add(this.get(iterator.next()));
        }

        //RETURN
        return trades;
    }

    @Override
    public List<Trade> getTrades(long have, long want, int offset, int limit) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return new ArrayList<>();
        }
        Iterator<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getPairIterator(have, want);
        if (iterator == null)
            return new ArrayList<Trade>();

        Iterators.advance(iterator, offset);

        iterator = Iterators.limit(iterator, limit);

        List<Trade> trades = new ArrayList<Trade>();
        while (iterator.hasNext()) {
            trades.add(this.get((Tuple2<Long, Long>) iterator.next()));
        }

        //RETURN
        return trades;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Trade getLastTrade(long have, long want) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }
        Iterator<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getPairIterator(have, want);
        if (iterator == null)
            return null;

        if (iterator.hasNext()) {
             return this.get((Tuple2<Long, Long>) iterator.next());
        }

        //RETURN
        return null;
    }

    /**
     * Get transaction by timestamp
     *  @param have      include
     * @param want      wish
     * @param timestamp is time
     * @param limit
     */
    @Override
    public List<Trade> getTradesByTimestamp(long have, long want, long timestamp, int limit) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }
        Iterator<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getPairTimestampIterator(have, want, timestamp);
        if (iterator == null)
            return null;

        iterator = Iterators.limit(iterator, limit);

        List<Trade> trades = new ArrayList<Trade>();
        while (iterator.hasNext()) {
            trades.add(this.get((Tuple2<Long, Long>) iterator.next()));
        }

        //RETURN
        return trades;
    }

    @Override
    public BigDecimal getVolume24(long have, long want) {

        BigDecimal volume = BigDecimal.ZERO;

        if (Controller.getInstance().onlyProtocolIndexing) {
            return volume;
        }

        // тут индекс не по времени а по номерам блоков как лонг
        int heightStart = Controller.getInstance().getMyHeight();
        Iterator<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getPairHeightIterator(have, want, heightStart);
        if (iterator == null)
            return null;

        while (iterator.hasNext()) {
            Trade trade = this.get((Tuple2<Long, Long>) iterator.next());
            if (trade.getHaveKey() == want) {
                volume = volume.add(trade.getAmountHave());
            } else {
                volume = volume.add(trade.getAmountWant());
            }
        }

        //RETURN
        return volume;
    }

    @Override
    public void delete(Trade trade) {
        this.delete(new Tuple2<Long, Long>(trade.getInitiator(), trade.getTarget()));
    }
}
