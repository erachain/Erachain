package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TradeSerializer;
import org.erachain.datachain.TradeSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

/**
 * Хранит сделки на бирже
 * Ключ: ссылка на инициатора + ссылка на цель
 * Значение - Сделка
 * Initiator DBRef (Long) + Target DBRef (Long) -> Trade
 */
@Slf4j
public class TradeSuitMapDB extends DBMapSuit<Tuple2<Long, Long>, Trade> implements TradeSuit {

    private BTreeMap pairKeyMap;
    private BTreeMap wantKeyMap;
    private BTreeMap haveKeyMap;
    private BTreeMap targetsKeyMap;

    public TradeSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
    }

    @Override
    public void openMap() {
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

        this.map = map;

        ///////////////////////////// HERE PROTOCOL INDEXES

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        //////////////// NOT PROTOCOL INDEXES

        //PAIR KEY
        this.pairKeyMap = database.createTreeMap("trades_key_pair")
                .comparator(Fun.TUPLE3_COMPARATOR)
                .makeOrGet();

        //BIND PAIR KEY
        Bind.secondaryKey(map, this.pairKeyMap, new Fun.Function2<Tuple3<String, Long, Integer>, Tuple2<Long, Long>, Trade>() {
            @Override
            public Tuple3<String, Long, Integer> run(Tuple2<Long, Long> key, Trade value) {

                String pairKey = makeKey(value.getHaveKey(), value.getWantKey());

                // обратная сортировка поэтому все вычитаем
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

                // обратная сортировка поэтому все вычитаем
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

                // обратная сортировка поэтому все вычитаем
                return new Tuple3<String, Long, Integer>(haveKey, Long.MAX_VALUE - value.getInitiator(),
                        Integer.MAX_VALUE - value.getSequence());
            }
        });

        // TODO: тут получается вообще лишний индекс - причем он 2 раза делается на одну запись - обе стороны
        //REVERSE KEY
        this.targetsKeyMap = database.createTreeMap("trades_key_reverse")
                //.comparator(new Fun.Tuple2Comparator(Fun.BYTE_ARRAY_COMPARATOR, Fun.BYTE_ARRAY_COMPARATOR))
                .comparator(Fun.TUPLE2_COMPARATOR)
                .makeOrGet();

        //BIND REVERSE KEY
        Bind.secondaryKey(map, this.targetsKeyMap, new Fun.Function2<Tuple2<Long, Long>, Tuple2<Long, Long>, Trade>() {
            @Override
            public Tuple2<Long, Long> run(Tuple2<Long, Long> key, Trade value) {

                return new Tuple2<Long, Long>(key.b, key.a);
            }
        });
        if (false) {
            Bind.secondaryKey(map, this.targetsKeyMap, new Fun.Function2<Tuple2<Long, Long>, Tuple2<Long, Long>, Trade>() {
                @Override
                public Tuple2<Long, Long> run(Tuple2<Long, Long> key, Trade value) {
                    return new Tuple2<Long, Long>(key.a, key.b);
                }
            });
        }

    }

    static String makeKey(long have, long want) {
        if (have > want) {
            return have + "/" + want;
        } else {
            return want + "/" + have;
        }

    }

    /**
     * поиск ключей для протокольных вторичных индексов с учетом Родительской таблицы (если база форкнута)
     *
     * @param orderID
     * @return
     */
    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getIteratorByInitiator(Long orderID) {
        //FILTER ALL KEYS
        return new IteratorCloseableImpl(((BTreeMap<Tuple2<Long, Long>, Trade>) map).subMap(
                Fun.t2(orderID, null),
                Fun.t2(orderID, Long.MAX_VALUE)).keySet().iterator());
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getIteratorByTarget(Long orderID) {

        if (targetsKeyMap == null)
            return null;

        //ADD REVERSE KEYS
        return new IteratorCloseableImpl(((BTreeMap<Tuple2, Tuple2<Long, Long>>) this.targetsKeyMap).subMap(
                Fun.t2(orderID, null),
                Fun.t2(orderID, Fun.HI())).values().iterator());
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getHaveIterator(long have) {

        if (this.haveKeyMap == null)
            return null;

        String haveKey = String.valueOf(have);
        return new IteratorCloseableImpl(((BTreeMap<Tuple3, Tuple2<Long, Long>>)
                this.haveKeyMap).subMap(
                    Fun.t3(haveKey, null, null),
                    Fun.t3(haveKey, Fun.HI(), Fun.HI())).values().iterator());
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getWantIterator(long want) {

        if (this.wantKeyMap == null)
            return null;

        String wantKey = String.valueOf(want);

        return new IteratorCloseableImpl(((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.wantKeyMap).subMap(
                Fun.t3(wantKey, null, null),
                Fun.t3(wantKey, Fun.HI(), Fun.HI())).values().iterator());
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairIteratorDesc(long have, long want) {

        if (this.pairKeyMap == null)
            return null;

        String pairKey = makeKey(have, want);

        return  new IteratorCloseableImpl(((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t3(pairKey, null, null),
                Fun.t3(pairKey, Fun.HI(), Fun.HI())).values().iterator());

    }

    /**
     * Так как тут основной индекс - он без обратной сортировки
     *
     * @param startHeight
     * @param stopHeight
     * @return
     */
    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairHeightIterator(int startHeight, int stopHeight) {

        if (this.pairKeyMap == null)
            return null;

        // так как тут обратный отсчет то вычитаем со старта еще и все номера транзакций
        return new IteratorCloseableImpl(((BTreeMap<Tuple2<Long, Long>, Trade>) this.map).subMap(
                Fun.t2(startHeight > 0 ? Transaction.makeDBRef(startHeight, 0) : null, null),
                Fun.t2(stopHeight > 0 ? Transaction.makeDBRef(stopHeight, Integer.MAX_VALUE) : Long.MAX_VALUE, Long.MAX_VALUE)).keySet().iterator());
    }

    /**
     * Get trades by Height
     * @param have
     * @param want
     * @param startHeight
     * @param stopHeight from to height
     * @return
     */
    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairHeightIterator(long have, long want, int startHeight, int stopHeight) {

        if (this.pairKeyMap == null)
            return null;

        String pairKey = makeKey(have, want);
        Object toEnd = stopHeight > 0 ? Long.MAX_VALUE - Transaction.makeDBRef(stopHeight, 0) : Fun.HI();

        // так как тут обратный отсчет то вычитаем со старта еще и все номера транзакций
        return new IteratorCloseableImpl(((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t3(pairKey, startHeight > 0 ? Long.MAX_VALUE - Transaction.makeDBRef(startHeight, Integer.MAX_VALUE) : null, null),
                Fun.t3(pairKey, toEnd, Fun.HI())).values().iterator());
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getIteratorFromID(long[] startTradeID) {
        if (this.pairKeyMap == null)
            return null;

        return new IteratorCloseableImpl(((BTreeMap<Tuple2<Long, Long>, Trade>) this.map).subMap(
                // обратная сортировка поэтому все вычитаем и -1 для всех getSequence
                Fun.t2(startTradeID == null ? null : startTradeID[0], startTradeID == null ? null : startTradeID[1]),
                Fun.t2(Long.MAX_VALUE, Long.MAX_VALUE)).keySet().iterator());
    }

    /**
     * Так как тут основной индекс - он без обратной сортировки
     * @param startOrderID
     * @param stopOrderID
     * @return
     */
    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairOrderIDIterator(long startOrderID, long stopOrderID) {
        if (this.pairKeyMap == null)
            return null;

        return new IteratorCloseableImpl(((BTreeMap<Tuple2<Long, Long>, Trade>) this.map).subMap(
                // обратная сортировка поэтому все вычитаем и -1 для всех getSequence
                Fun.t2(startOrderID > 0 ? startOrderID : null, null),
                Fun.t2(stopOrderID > 0 ? stopOrderID : Long.MAX_VALUE, Long.MAX_VALUE)).keySet().iterator());
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairOrderIDIterator(long have, long want, long startOrderID, long stopOrderID) {
        if (this.pairKeyMap == null)
            return null;

        String pairKey = makeKey(have, want);
        Object toEnd = stopOrderID > 0 ? Long.MAX_VALUE - stopOrderID : Fun.HI();
        return new IteratorCloseableImpl(((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                // обратная сортировка поэтому все вычитаем и -1 для всех getSequence
                Fun.t3(pairKey, startOrderID > 0 ? Long.MAX_VALUE - startOrderID : null, null),
                Fun.t3(pairKey, toEnd, Fun.HI())).values().iterator());
    }

}
