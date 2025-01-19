package org.erachain.datachain;

import com.google.common.collect.Iterators;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.mapDB.TradeSuitMapDB;
import org.erachain.dbs.mapDB.TradeSuitMapDBFork;
import org.erachain.dbs.rocksDB.TradeSuitRocksDB;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import java.io.IOException;
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
                    map = new TradeSuitMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    //map = new NativeMapTreeMapFork(parent, databaseSet, Fun.TUPLE2_COMPARATOR, this);
                    //break;
                default:
                    map = new TradeSuitMapDBFork((TradeMap) parent, databaseSet);
            }
        }
    }

    @Override
    public void put(Trade trade) {
        this.put(new Tuple2<Long, Long>(trade.getInitiator(), trade.getTarget()), trade);
    }

    /**
     * поиск ключей для протокольных вторичных индексов с учетом Родительской таблицы (если база форкнута)
     *
     * @param orderID
     * @param descending
     * @return
     */
    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getIteratorByInitiator(Long orderID, boolean descending) {
        return ((TradeSuit) this.map).getIteratorByInitiator(orderID, descending);
    }

    @Override
    public List<Trade> getInitiatedTrades(Order order, boolean useCancel) {
        return getInitiatedTrades(order.getId(), useCancel);
    }

    public List<Trade> getInitiatedTrades(Long orderId, boolean useCancel) {
        //FILTER ALL TRADES
        try (IteratorCloseable<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getIteratorByInitiator(orderId, false)) {

            //GET ALL TRADES FOR KEYS
            List<Trade> trades = new ArrayList<Trade>();
            Trade trade;
            while (iterator.hasNext()) {
                trade = this.get(iterator.next());
                if (!useCancel && !trade.isTrade())
                    continue;

                trades.add(trade);
            }

            //RETURN
            return trades;
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * Надо сперва кто его покусал с обратном порядке, потом его самого
     *
     * @param orderID
     * @param useCancel
     * @param descending
     * @return
     */
    @Override
    public List<Trade> getTradesByOrderID(Long orderID, boolean useCancel, boolean descending) {
        //ADD REVERSE KEYS
        if (Controller.getInstance().onlyProtocolIndexing) {
            return new ArrayList<>();
        }

        List<Trade> trades = new ArrayList<Trade>();
        try (IteratorCloseable<Tuple2<Long, Long>> iterator =
                     descending ? ((TradeSuit) this.map).getIteratorByTarget(orderID, true)
                             : ((TradeSuit) this.map).getIteratorByInitiator(orderID, false)) {
            //GET ALL ORDERS FOR KEYS as INITIATOR
            Trade trade;
            while (iterator.hasNext()) {
                trade = this.get(iterator.next());
                if (!useCancel && !trade.isTrade())
                    continue;

                trades.add(trade);
            }
        } catch (IOException e) {
        }

        try (IteratorCloseable<Tuple2<Long, Long>> iterator =
                     descending ? ((TradeSuit) this.map).getIteratorByInitiator(orderID, true)
                             : ((TradeSuit) this.map).getIteratorByTarget(orderID, false)) {
            //GET ALL ORDERS FOR KEYS as TARGET
            Trade trade;
            while (iterator.hasNext()) {
                trade = this.get(iterator.next());
                if (!useCancel && !trade.isTrade())
                    continue;

                trades.add(trade);
            }
        } catch (IOException e) {
        }

        //RETURN
        return trades;
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> iteratorByAssetKey(long assetKey, boolean descending) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        return ((TradeSuit) this.map).getIteratorByAssetKey(assetKey, descending);

    }

    @Override
    public List<Trade> getTradesByPair(long have, long want, Object fromKey, int limit, boolean useCancel, boolean useChange) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return new ArrayList<>();
        }

        try (IteratorCloseable<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getPairIteratorDesc(have, want)) {
            if (iterator == null)
                return new ArrayList<Trade>();

            //Iterators.advance(iterator, offset);

            // тут итератор нен ужно закрывтьа так как базовый итератор уже закроем
            Iterator<Tuple2<Long, Long>> iteratorLimit = Iterators.limit(iterator, limit);

            List<Trade> trades = new ArrayList<Trade>();
            Trade trade;
            while (iteratorLimit.hasNext()) {
                trade = this.get(iteratorLimit.next());
                if (!useCancel && !trade.isTrade())
                    continue;
                if (!useChange && trade.isChange())
                    continue;

                trades.add(trade);
            }
            return trades;

        } catch (IOException e) {
            return new ArrayList<>();
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public Trade getLastTrade(long have, long want, boolean useCancel) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }


        try (IteratorCloseable<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getPairIteratorDesc(have, want)) {
            if (iterator == null)
                return null;

            Trade trade;
            while (iterator.hasNext()) {
                trade = this.get(iterator.next());
                if (useCancel)
                    return trade;

                if (trade.isTrade()) {
                    return trade;
                }
            }

        } catch (IOException e) {
        }

        //RETURN
        return null;
    }

    public IteratorCloseable<Tuple2<Long, Long>> getPairIterator(long have, long want) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        return ((TradeSuit) this.map).getPairIteratorDesc(have, want);
    }

    public IteratorCloseable<Tuple2<Long, Long>> getPairIterator(long have, long want, int heightStart, int heightEnd) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        return ((TradeSuit) this.map).getPairHeightIterator(have, want, heightStart, heightEnd);
    }

    /**
     * Get trades by timestamp. From Timestamp to deep.
     *
     * @param startTimestamp is time
     * @param stopTimestamp
     * @param limit
     */
    @Override
    public List<Trade> getTradesByTimestamp(long startTimestamp, long stopTimestamp, int limit) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        // тут индекс не по времени а по номерам блоков как лонг
        int fromBlock = startTimestamp == 0 ? 0 : Controller.getInstance().getBlockChain().getHeightOnTimestampMS(startTimestamp);
        int toBlock = stopTimestamp == 0 ? 0 : Controller.getInstance().getBlockChain().getHeightOnTimestampMS(stopTimestamp);

        //RETURN
        return getTradesFromToHeight(fromBlock, toBlock, limit);
    }

    /**
     * Get trades by timestamp. From Timestamp to deep.
     * @param have      include
     * @param want      wish
     * @param startTimestamp is time
     * @param stopTimestamp
     * @param limit
     */
    @Override
    public List<Trade> getTradesByTimestamp(long have, long want, long startTimestamp, long stopTimestamp, int limit) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        // тут индекс не по времени а по номерам блоков как лонг
        int fromBlock = startTimestamp == 0 ? 0 : Controller.getInstance().getBlockChain().getHeightOnTimestampMS(startTimestamp);
        int toBlock = stopTimestamp == 0 ? 0 : Controller.getInstance().getBlockChain().getHeightOnTimestampMS(stopTimestamp);

        //RETURN
        return getTradesFromToHeight(have, want, fromBlock, toBlock, limit);
    }

    @Override
    public List<Trade> getTradesFromTradeID(long[] startTradeID, int limit) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Trade> trades = new ArrayList<Trade>();
        try (IteratorCloseable<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getIteratorFromID(startTradeID)) {

            int counter = limit;
            Trade trade;
            while (iterator.hasNext()) {
                trade = this.get(iterator.next());
                if (!trade.isTrade())
                    continue;

                trades.add(trade);
                if (limit > 0 && --counter < 0)
                    break;
            }
        } catch (IOException e) {
        }

        return trades;
    }

    @Override
    public List<Trade> getTradesFromToOrderID(long startOrderID, long stopOrderID, int limit, boolean useCancel) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Trade> trades = new ArrayList<Trade>();
        try (IteratorCloseable<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getPairOrderIDIterator(startOrderID, stopOrderID)) {

            int counter = limit;
            Trade trade;
            while (iterator.hasNext()) {
                trade = this.get(iterator.next());
                if (!useCancel && !trade.isTrade())
                    continue;

                trades.add(trade);

                if (limit > 0 && --counter < 0)
                    break;
            }
        } catch (IOException e) {
        }

        return  trades;
    }

    @Override
    public List<Trade> getTradesFromToOrderID(long have, long want, long startOrderID, long stopOrderID, int limit, boolean useCancel) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Trade> trades = new ArrayList<Trade>();
        try (IteratorCloseable<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getPairOrderIDIterator(have, want, startOrderID, stopOrderID)) {

            int counter = limit;
            Trade trade;
            while (iterator.hasNext()) {
                trade = this.get(iterator.next());
                if (!useCancel && !trade.isTrade())
                    continue;

                trades.add(trade);

                if (limit > 0 && --counter < 0)
                    break;
            }
        } catch (IOException e) {
        }

        return  trades;
    }

    @Override
    public List<Trade> getTradesFromToHeight(int start, int stop, int limit) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Trade> trades = new ArrayList<Trade>();
        try (IteratorCloseable<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getPairHeightIterator(start, stop)) {
            if (iterator == null)
                return null;

            int counter = limit;
            Trade trade;
            while (iterator.hasNext()) {
                trade = this.get(iterator.next());
                if (!trade.isTrade())
                    continue;

                trades.add(trade);
                if (limit > 0 && --counter < 0)
                    break;
            }
        } catch (IOException e) {
        }

        //RETURN
        return trades;
    }

    @Override
    public List<Trade> getTradesFromToHeight(long have, long want, int start, int stop, int limit) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Trade> trades = new ArrayList<Trade>();
        try (IteratorCloseable<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getPairHeightIterator(have, want, start, stop)) {
            if (iterator == null)
                return null;

            int counter = limit;
            Trade trade;
            while (iterator.hasNext()) {
                trade = this.get(iterator.next());
                if (!trade.isTrade())
                    continue;

                trades.add(trade);
                if (limit > 0 && --counter < 0)
                    break;
            }
        } catch (IOException e) {
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

        // тут индекс не по времени а по номерам блоков как лонг
        ///int heightStart = Controller.getInstance().getMyHeight();
        //// с последнего -- long refDBstart = Transaction.makeDBRef(heightStart, 0);
        int heightEnd = heightStart - BlockChain.BLOCKS_PER_DAY(heightStart);

        try (IteratorCloseable<Tuple2<Long, Long>> iterator = ((TradeSuit) this.map).getPairHeightIterator(have, want, heightStart, heightEnd)) {
            if (iterator == null)
                return null;

            while (iterator.hasNext()) {
                Trade trade = this.get(iterator.next());
                if (!trade.isTrade())
                    continue;

                if (trade.getHaveKey() == want) {
                    volume = volume.add(trade.getAmountHave());
                } else {
                    volume = volume.add(trade.getAmountWant());
                }
            }
        } catch (IOException e) {
        }

        //RETURN
        return volume;
    }

    @Override
    public void delete(Trade trade) {
        this.delete(new Tuple2<Long, Long>(trade.getInitiator(), trade.getTarget()));
    }
}
