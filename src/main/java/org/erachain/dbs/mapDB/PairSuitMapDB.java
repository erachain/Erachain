package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.item.assets.TradePair;
import org.erachain.database.DBASet;
import org.erachain.database.PairSuit;
import org.erachain.database.serializer.TradePairSerializer;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

/**
 * Хранит сделки на бирже
 * Ключ: ссылка на иницатора + ссылка на цель
 * Значение - Сделка
 * Initiator DBRef (Long) + Target DBRef (Long) -> Trade
 */
@Slf4j
public class PairSuitMapDB extends DBMapSuit<Tuple2<Long, Long>, TradePair> implements PairSuit {

    public PairSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
    }

    @Override
    public void openMap() {

        BTreeMap<Tuple2<Long, Long>, TradePair> map = database.createTreeMap("trade_pairs")
                .valueSerializer(new TradePairSerializer())
                .comparator(Fun.TUPLE2_COMPARATOR)
                .makeOrGet();

        this.map = map;

    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getIterator(long have) {
        return IteratorCloseableImpl.make(((BTreeMap<Tuple2<Long, Long>, TradePair>) map).subMap(Fun.t2(have, null),
                Fun.t2(have, Fun.HI())).keySet().iterator());

    }

}
