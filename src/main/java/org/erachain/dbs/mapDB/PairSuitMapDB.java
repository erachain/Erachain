package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.item.assets.Pair;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TradeSerializer;
import org.erachain.datachain.PairSuit;
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
public class PairSuitMapDB extends DBMapSuit<Tuple2<Long, Long>, Pair> implements PairSuit {

    public PairSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
    }

    @Override
    public void openMap() {

        BTreeMap<Tuple2<Long, Long>, Pair> map = database.createTreeMap("pairs")
                .valueSerializer(new TradeSerializer())
                .comparator(Fun.TUPLE2_COMPARATOR)
                .makeOrGet();

        this.map = map;

    }
}
