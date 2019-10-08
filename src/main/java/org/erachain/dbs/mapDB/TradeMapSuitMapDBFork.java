package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TradeSerializer;
import org.erachain.datachain.TradeMap;
import org.erachain.datachain.TradeMapSuit;
import org.mapdb.BTreeMap;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.util.Iterator;
import java.util.Map;

/**
 * Хранит сделки на бирже
 * Ключ: ссылка на иницатора + ссылка на цель
 * Значение - Сделка
Initiator DBRef (Long) + Target DBRef (Long) -> Trade
 */
@Slf4j
public class TradeMapSuitMapDBFork extends DBMapSuitFork<Tuple2<Long, Long>, Trade> implements TradeMapSuit {

    public TradeMapSuitMapDBFork(TradeMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger, null);
    }

    @Override
    protected void getMap() {
        //OPEN MAP
        map = database.createTreeMap("trades")
                .valueSerializer(new TradeSerializer())
                .comparator(Fun.TUPLE2_COMPARATOR)
                //.comparator(Fun.COMPARATOR)
                .makeOrGet();
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
        return null;
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getHaveIterator(long have) {
        return null;
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getWantIterator(long want) {
        return null;
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getPairIterator(long have, long want) {
        return null;
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getPairTimestampIterator(long have, long want, long timestamp) {
        return null;
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getPairHeightIterator(long have, long want, int heightStart) {
        return null;
    }

}
