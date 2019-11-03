package org.erachain.datachain;

import com.google.common.collect.Iterables;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBMap;
import org.erachain.database.SortableList;
import org.erachain.database.serializer.TradeSerializer;
import org.erachain.utils.ObserverMessage;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.math.BigDecimal;
import java.util.*;

/**
 * Хранит сделки на бирже
 * Ключ: ссылка на иницатора + ссылка на цель
 * Значение - Сделка
Initiator DBRef (Long) + Target DBRef (Long) -> Trade
 */
@SuppressWarnings("rawtypes")
public class TradeMap extends DCMap<Tuple2<Long, Long>, Trade> {

    private BTreeMap pairKeyMap;
    private BTreeMap wantKeyMap;
    private BTreeMap haveKeyMap;
    private BTreeMap reverseKeyMap;

    public TradeMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_TRADE_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ORDER_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_TRADE_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_TRADE_TYPE);
        }
    }

    public TradeMap(TradeMap parent, DCSet dcSet) {
        super(parent, dcSet);

    }
    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Tuple2<Long, Long>, Trade> getMap(DB database) {
        //OPEN MAP
        return this.openMap(database);
    }

    @Override
    protected Map<Tuple2<Long, Long>, Trade> getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        return this.openMap(database);
    }

    @SuppressWarnings("unchecked")
    private Map<Tuple2<Long, Long>, Trade> openMap(final DB database) {
        //OPEN MAP

        /* EXAMPLE for Long, Long
        Fun.Tuple2Comparator<Long, Long> comparator = new Fun.Tuple2Comparator<Long, Long>(Fun.COMPARATOR,
                UnsignedBytes.lexicographicalComparator());
        NavigableSet<Tuple2<Integer, Long>> heightIndex = database.createTreeSet("transactions_index_timestamp")
                .comparator(comparator).makeOrGet();

        //.comparator(new Fun.Tuple2Comparator(Fun.BYTE_ARRAY_COMPARATOR, Fun.BYTE_ARRAY_COMPARATOR)) - for Tuple2<byte[]m byte[]>

                */

        BTreeMap<Tuple2<Long, Long>, Trade> map = database.createTreeMap("trades")
                .valueSerializer(new TradeSerializer())
                .comparator(Fun.TUPLE2_COMPARATOR)
                //.comparator(Fun.COMPARATOR)
                .makeOrGet();

        ///////////////////////////// HERE PROTOCOL INDEXES

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return map;

        //////////////// NOT PROTOCOL INDEXES

        //CHECK IF NOT MEMORY DATABASE
        if (parent == null) {
            //PAIR KEY
            this.pairKeyMap = database.createTreeMap("trades_key_pair")
                    .comparator(Fun.TUPLE3_COMPARATOR)
                    .makeOrGet();

            //BIND PAIR KEY
            Bind.secondaryKey(map, this.pairKeyMap, new Fun.Function2<Tuple3<String, Long, Integer>, Tuple2<Long, Long>, Trade>() {
                @Override
                public Tuple3<String, Long, Integer> run(Tuple2<Long, Long> key, Trade value) {
                    long have = value.getHaveKey();
                    long want = value.getWantKey();
                    String pairKey;
                    if (have > want) {
                        pairKey = have + "/" + want;
                    } else {
                        pairKey = want + "/" + have;
                    }

                    return new Tuple3<String, Long, Integer>(pairKey, Long.MAX_VALUE - value.getInitiator(),
                            Integer.MAX_VALUE - value.getSequence());
                }
            });

            //
            this.wantKeyMap = database.createTreeMap("trades_key_want")
                    .comparator(Fun.TUPLE3_COMPARATOR)
                    .makeOrGet();

            //BIND
            Bind.secondaryKey(map, this.wantKeyMap, new Fun.Function2<Tuple3<String, Long, Integer>, Tuple2<Long, Long>, Trade>() {
                @Override
                public Tuple3<String, Long, Integer> run(Tuple2<Long, Long> key, Trade value) {
                    long want = value.getWantKey();

                    String wantKey;
                    wantKey = String.valueOf(want);

                    return new Tuple3<String, Long, Integer>(wantKey, Long.MAX_VALUE - value.getInitiator(),
                            Integer.MAX_VALUE - value.getSequence());
                }
            });

            //
            this.haveKeyMap = database.createTreeMap("trades_key_have")
                    .comparator(Fun.TUPLE3_COMPARATOR)
                    .makeOrGet();

            //BIND
            Bind.secondaryKey(map, this.haveKeyMap, new Fun.Function2<Tuple3<String, Long, Integer>, Tuple2<Long, Long>, Trade>() {
                @Override
                public Tuple3<String, Long, Integer> run(Tuple2<Long, Long> key, Trade value) {
                    long have = value.getHaveKey(); //order.getHaveAssetKey();

                    String haveKey;
                    haveKey = String.valueOf(have);

                    return new Tuple3<String, Long, Integer>(haveKey, Long.MAX_VALUE - value.getInitiator(),
                            Integer.MAX_VALUE - value.getSequence());
                }
            });

        }

        //REVERSE KEY
        this.reverseKeyMap = database.createTreeMap("trades_key_reverse")
                //.comparator(new Fun.Tuple2Comparator(Fun.BYTE_ARRAY_COMPARATOR, Fun.BYTE_ARRAY_COMPARATOR))
                .comparator(Fun.TUPLE2_COMPARATOR)
                .makeOrGet();

        //BIND REVERSE KEY
        Bind.secondaryKey(map, this.reverseKeyMap, new Fun.Function2<Tuple2<Long, Long>, Tuple2<Long, Long>, Trade>() {
            @Override
            public Tuple2<Long, Long> run(Tuple2<Long, Long> key, Trade value) {

                return new Tuple2<Long, Long>(key.b, key.a);
            }
        });
        Bind.secondaryKey(map, this.reverseKeyMap, new Fun.Function2<Tuple2<Long, Long>, Tuple2<Long, Long>, Trade>() {
            @Override
            public Tuple2<Long, Long> run(Tuple2<Long, Long> key, Trade value) {
                return new Tuple2<Long, Long>(key.a, key.b);
            }
        });

        //RETURN
        return map;
    }

    @Override
    protected Trade getDefaultValue() {
        return null;
    }

    public void add(Trade trade) {
        this.set(new Tuple2<Long, Long>(trade.getInitiator(), trade.getTarget()), trade);
    }

    /**
     * поиск ключей для протокольных вторичных индексов с учетом Родительской таблицы (если база форкнута)
     * @param order
     * @return
     */
    @SuppressWarnings("unchecked")
    private Set<Tuple2> getKeys(Order order) {

        Map uncastedMap = this.map;

        //FILTER ALL KEYS
        Set<Tuple2> keys = ((BTreeMap<Tuple2, Order>) uncastedMap).subMap(
                Fun.t2(order.getId(), null),
                Fun.t2(order.getId(), Fun.HI())).keySet();

        //IF THIS IS A FORK
        if (this.parent != null) {
            //GET ALL KEYS FOR FORK
            Set<Tuple2> parentKeys = ((TradeMap) this.parent).getKeys(order);

            if (this.deleted != null) {
                //DELETE DELETED
                parentKeys.removeAll(this.deleted.keySet());
            }

            //COMBINE SETS
            combinedKeys.addAll(forkKeys);


            //CONVERT SET BACK TO COLLECTION
            keys = combinedKeys;
        }

        return keys;
    }

    @SuppressWarnings("unchecked")
    public List<Trade> getInitiatedTrades(Order order) {
        //FILTER ALL TRADES
        Collection<Tuple2> keys = this.getKeys(order);

        //GET ALL TRADES FOR KEYS
        List<Trade> trades = new ArrayList<Trade>();
        for (Tuple2 key : keys) {
            trades.add(this.get(key));
        }

        //RETURN
        return trades;
    }

    @SuppressWarnings("unchecked")
    public SortableList<Tuple2<Long, Long>, Trade> getTrades(Long orderID) {
        //ADD REVERSE KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple2, Tuple2<Long, Long>>) this.reverseKeyMap).subMap(
                Fun.t2(orderID, null),
                Fun.t2(orderID, Fun.HI())).values();

        //RETURN
        return new SortableList<Tuple2<Long, Long>, Trade>(this, keys);
    }

    @SuppressWarnings("unchecked")
    public SortableList<Tuple2<Long, Long>, Trade> getTradesByOrderIDAsSorted(Long orderID) {
        //ADD REVERSE KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple2, Tuple2<Long, Long>>) this.reverseKeyMap).subMap(
                Fun.t2(orderID, null),
                Fun.t2(orderID, Fun.HI())).values();

        //RETURN
        return new SortableList<Tuple2<Long, Long>, Trade>(this, keys);
    }

    public List<Trade> getTradesByOrderID(Long orderID) {
        //ADD REVERSE KEYS
        Collection<Tuple2<Long, Long>> tradesKeys = ((BTreeMap<Tuple2, Tuple2<Long, Long>>) this.reverseKeyMap).subMap(
                Fun.t2(orderID, null),
                Fun.t2(orderID, Fun.HI())).values();

        //GET ALL ORDERS FOR KEYS
        List<Trade> trades = new ArrayList<Trade>();

        for (Tuple2<Long, Long> tradeKey : tradesKeys) {
            trades.add(this.get(tradeKey));
        }

        //RETURN
        return trades;
    }


    @SuppressWarnings("unchecked")
    public List<Trade> getTrades(long haveWant)
    // get trades for order as HAVE and as WANT
    {

        if (this.haveKeyMap == null)
            return new ArrayList<Trade>();

        String haveKey = String.valueOf(haveWant);
        HashSet<Tuple2<Long, Long>> tradesKeys = new HashSet<Tuple2<Long, Long>>(((BTreeMap<Tuple3, Tuple2<Long, Long>>)
                this.haveKeyMap).subMap(
                    Fun.t3(haveKey, null, null),
                    Fun.t3(haveKey, Fun.HI(), Fun.HI())).values());

        String wantKey = String.valueOf(haveWant);

        tradesKeys.addAll(((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.wantKeyMap).subMap(
                Fun.t3(wantKey, null, null),
                Fun.t3(wantKey, Fun.HI(), Fun.HI())).values());

        //GET ALL ORDERS FOR KEYS
        List<Trade> trades = new ArrayList<Trade>();

        for (Tuple2<Long, Long> tradeKey : tradesKeys) {
            trades.add(this.get(tradeKey));
        }

        //RETURN
        return trades;
    }

    @SuppressWarnings("unchecked")
    public SortableList<Tuple2<Long, Long>, Trade> getTradesSortableList(long have, long want) {

        String pairKey;
        if (have > want) {
            pairKey = have + "/" + want;
        } else {
            pairKey = want + "/" + have;
        }

        if (this.pairKeyMap != null) {
            //FILTER ALL KEYS
            Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                    Fun.t3(pairKey, null, null),
                    Fun.t3(pairKey, Fun.HI(), Fun.HI())).values();

            //RETURN
            return new SortableList<Tuple2<Long, Long>, Trade>(this, keys);
        }

        return new SortableList<Tuple2<Long, Long>, Trade>(this, new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public List<Trade> getTrades(long have, long want, int offset, int limit) {

        if (this.pairKeyMap == null)
            return new ArrayList<Trade>();

        String pairKey;
        if (have > want) {
            pairKey = have + "/" + want;
        } else {
            pairKey = want + "/" + have;
        }

        //FILTER ALL KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t3(pairKey, null, null),
                Fun.t3(pairKey, Fun.HI(), Fun.HI())).values();

        Iterable iterable;

        if (offset > 0) {
            iterable = Iterables.skip(keys, offset);
        } else {
            iterable = keys;
        }

        if (limit > 0 && keys.size() > limit) {
            iterable = Iterables.limit(iterable, limit);
        }

        List<Trade> trades = new ArrayList<Trade>();

        Iterator iterator = iterable.iterator();
        while (iterator.hasNext()) {
            trades.add(this.get((Tuple2<Long, Long>) iterator.next()));
        }

        //RETURN
        return trades;
    }

    @SuppressWarnings("unchecked")
    public Trade getLastTrade(long have, long want) {

        if (this.pairKeyMap == null)
            return null;

        String pairKey;
        if (have > want) {
            pairKey = have + "/" + want;
        } else {
            pairKey = want + "/" + have;
        }

        //FILTER ALL KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t3(pairKey, null, null),
                Fun.t3(pairKey, Fun.HI(), Fun.HI()))
                    //.descendingMap()
                    .values();

        Iterator iterator = keys.iterator();
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
    public List<Trade> getTradesByTimestamp(long have, long want, long timestamp, int limit) {

        if (this.pairKeyMap == null)
            return new ArrayList<Trade>();

        String pairKey;
        if (have > want)
            pairKey = have + "/" + want;
        else
            pairKey = want + "/" + have;

        // тут индекс не по времени а по номерам блоков как лонг
        int heightStart = Controller.getInstance().getMyHeight();
        int heightEnd = heightStart - Controller.getInstance().getBlockChain().getBlockOnTimestamp(timestamp);
        long refDBend = Transaction.makeDBRef(heightEnd, 0);

        //FILTER ALL KEYS
        //// обратный отсчет по номерам блоков
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t3(pairKey, null, null), // с самого посденего и до нужного вверх
                Fun.t3(pairKey, Long.MAX_VALUE - refDBend, Fun.HI())).values();

        Iterable iterable;

        if (limit > 0 && keys.size() > limit) {
            iterable = Iterables.limit(keys, limit);
        } else {
            iterable = keys;
        }

        List<Trade> trades = new ArrayList<Trade>();

        Iterator iterator = iterable.iterator();
        while (iterator.hasNext()) {
            trades.add(this.get((Tuple2<Long, Long>) iterator.next()));
        }

        //RETURN
        return trades;
    }

    public BigDecimal getVolume24(long have, long want) {

        BigDecimal volume = BigDecimal.ZERO;

        if (this.pairKeyMap == null)
            return volume;

        String pairKey;
        if (have > want)
            pairKey = have + "/" + want;
        else
            pairKey = want + "/" + have;

        // тут индекс не по времени а по номерам блоков как лонг
        int heightStart = Controller.getInstance().getMyHeight();
        //// с последнего -- long refDBstart = Transaction.makeDBRef(heightStart, 0);
        int heightEnd = heightStart - BlockChain.BLOCKS_PER_DAY(heightStart);
        long refDBend = Transaction.makeDBRef(heightEnd, 0);

        //FILTER ALL KEYS
        //// обратный отсчет по номерам блоков
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                //Fun.t3(pairKey, Long.MAX_VALUE - refDBstart, null),
                Fun.t3(pairKey, null, null), // с самого посденего и до нужного вверх
                Fun.t3(pairKey, Long.MAX_VALUE - refDBend, Fun.HI())).values();

        Iterator iterator = keys.iterator();
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

    public void delete(Trade trade) {
        this.delete(new Tuple2<Long, Long>(trade.getInitiator(), trade.getTarget()));
    }
}
