package org.erachain.dbs.mapDB;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.item.assets.Trade;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TradeSerializer;
import org.erachain.datachain.TradeMap;
import org.erachain.datachain.TradeMapImpl;
import org.erachain.datachain.TradeSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorParent;
import org.erachain.dbs.MergedOR_IteratorsNoDuplicates;
import org.mapdb.BTreeMap;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.util.Iterator;

/**
 * Хранит сделки на бирже
 * Ключ: ссылка на иницатора + ссылка на цель
 * Значение - Сделка
 * Initiator DBRef (Long) + Target DBRef (Long) -> Trade
 */
@Slf4j
public class TradeSuitMapDBFork extends DBMapSuitFork<Tuple2<Long, Long>, Trade> implements TradeSuit {

    public TradeSuitMapDBFork(TradeMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap("trades")
                .valueSerializer(new TradeSerializer())
                .comparator(Fun.TUPLE2_COMPARATOR)
                //.comparator(Fun.COMPARATOR)
                .makeOrGet();
    }

    /**
     * поиск ключей для протокольных вторичных индексов с учетом Родительской таблицы (если база форкнута)
     * - нужно для отката Заказа - просмотр по всем его покусанным сделкам
     *
     * @param orderID
     * @param descending
     * @return
     */
    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getIteratorByInitiator(Long orderID, boolean descending) {
        // берем из родителя
        IteratorCloseable<Tuple2<Long, Long>> parentIterator = ((TradeMapImpl) parent).getIteratorByInitiator(orderID);
        // берем свои
        Iterator<Tuple2<Long, Long>> iteratorForked = ((BTreeMap<Tuple2<Long, Long>, Trade>) map).subMap(
                Fun.t2(orderID, null),
                Fun.t2(orderID, Long.MAX_VALUE)).keySet().iterator();

        // создаем с учетом удаленных
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted), iteratorForked), Fun.TUPLE2_COMPARATOR);

    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getIteratorByTarget(Long orderID, boolean descending) {
        return null;
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getHaveIterator(long have) {
        return null;
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getWantIterator(long want) {
        return null;
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairIteratorDesc(long have, long want) {
        return null;
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairHeightIterator(int startHeight, int stopHeight) {
        return null;
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairHeightIterator(long have, long want, int startHeight, int stopHeight) {
        return null;
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getIteratorFromID(long[] startTradeID) {
        return null;
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairOrderIDIterator(long startOrderID, long stopOrderID) {
        return null;
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairOrderIDIterator(long have, long want, long startOrderID, long stopOrderID) {
        return null;
    }

}
