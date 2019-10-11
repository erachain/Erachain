package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TradeSerializer;
import org.erachain.datachain.TradeMapSuit;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.util.Iterator;
import java.util.Map;

/**
 * Хранит сделки на бирже
 * Ключ: ссылка на иницатора + ссылка на цель
 * Значение - Сделка
Initiator DBRef (Long) + Target DBRef (Long) -> Trade
 */
@Slf4j
public class TradeMapSuitMapDB extends DBMapSuit<Tuple2<Long, Long>, Trade> implements TradeMapSuit {

    private BTreeMap pairKeyMap;
    private BTreeMap wantKeyMap;
    private BTreeMap haveKeyMap;
    private BTreeMap reverseKeyMap;

    public TradeMapSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, null);
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

    }

    /**
     * поиск ключей для протокольных вторичных индексов с учетом Родительской таблицы (если база форкнута)
     * @param order
     * @return
     */
    @Override
    public Iterator<Tuple2> getIterator(Order order) {
        //FILTER ALL KEYS
        Map uncastedMap = map;
        return  ((BTreeMap<Tuple2, Order>) uncastedMap).subMap(
                Fun.t2(order.getId(), null),
                Fun.t2(order.getId(), Fun.HI())).keySet().iterator();
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getReverseIterator(Long orderID) {
        //ADD REVERSE KEYS
        return  ((BTreeMap<Tuple2, Tuple2<Long, Long>>) this.reverseKeyMap).subMap(
                Fun.t2(orderID, null),
                Fun.t2(orderID, Fun.HI())).values().iterator();
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getHaveIterator(long have) {

        if (this.haveKeyMap == null)
            return null;

        String haveKey = String.valueOf(have);
        return ((BTreeMap<Tuple3, Tuple2<Long, Long>>)
                this.haveKeyMap).subMap(
                    Fun.t3(haveKey, null, null),
                    Fun.t3(haveKey, Fun.HI(), Fun.HI())).values().iterator();
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getWantIterator(long want) {

        if (this.wantKeyMap == null)
            return null;

        String wantKey = String.valueOf(want);

        return ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.wantKeyMap).subMap(
                Fun.t3(wantKey, null, null),
                Fun.t3(wantKey, Fun.HI(), Fun.HI())).values().iterator();
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getPairIterator(long have, long want) {

        if (this.pairKeyMap == null)
            return null;

        String pairKey;
        if (have > want) {
            pairKey = have + "/" + want;
        } else {
            pairKey = want + "/" + have;
        }

        return  ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t3(pairKey, null, null),
                Fun.t3(pairKey, Fun.HI(), Fun.HI())).values().iterator();

    }

    /**
     * Get trades by timestamp
     * @param have
     * @param want
     * @param timestamp from to height
     */
    @Override
    public Iterator<Tuple2<Long, Long>> getPairTimestampIterator(long have, long want, long timestamp) {

        if (this.pairKeyMap == null)
            return null;

        String pairKey;
        if (have > want) {
            pairKey = have + "/" + want;
        } else {
            pairKey = want + "/" + have;
        }

        // тут индекс не по времени а по номерам блоков как лонг
        int heightStart = Controller.getInstance().getMyHeight();
        int heightEnd = heightStart - Controller.getInstance().getBlockChain().getBlockOnTimestamp(timestamp);
        long refDBend = Transaction.makeDBRef(heightEnd, 0);

        return  ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t3(pairKey, null, null),
                Fun.t3(pairKey, Long.MAX_VALUE - refDBend, Fun.HI())).values().iterator();
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getPairHeightIterator(long have, long want, int heightStart) {

        if (this.pairKeyMap == null)
            return null;

        String pairKey;
        if (have > want) {
            pairKey = have + "/" + want;
        } else {
            pairKey = want + "/" + have;
        }

        // тут индекс не по времени а по номерам блоков как лонг
        ///int heightStart = Controller.getInstance().getMyHeight();
        //// с последнего -- long refDBstart = Transaction.makeDBRef(heightStart, 0);
        int heightEnd = heightStart - BlockChain.BLOCKS_PER_DAY(heightStart);
        long refDBend = Transaction.makeDBRef(heightEnd, 0);

        return  ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t3(pairKey, null, null),
                Fun.t3(pairKey, Long.MAX_VALUE - refDBend, Fun.HI())).values().iterator();
    }

}
