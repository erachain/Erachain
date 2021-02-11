package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.item.assets.TradePair;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TradeSerializer;
import org.erachain.datachain.PairMap;
import org.erachain.datachain.PairSuit;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

/**
 * Хранит сделки на бирже
 * Ключ: ссылка на иницатора + ссылка на цель
 * Значение - Сделка
 * Initiator DBRef (Long) + Target DBRef (Long) -> Trade
 */
@Slf4j
public class PairSuitMapDBFork extends DBMapSuitFork<Tuple2<Long, Long>, TradePair> implements PairSuit {

    public PairSuitMapDBFork(PairMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap("pairs")
                .valueSerializer(new TradeSerializer())
                .comparator(Fun.TUPLE2_COMPARATOR)
                .makeOrGet();
    }

}
