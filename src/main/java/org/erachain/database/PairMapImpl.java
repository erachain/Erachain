package org.erachain.database;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.item.assets.TradePair;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.mapDB.PairSuitMapDB;
import org.erachain.dbs.mapDB.PairSuitMapDBFork;
import org.erachain.dbs.rocksDB.PairSuitRocksDB;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Хранит сводные данные по паре на бирже
 * asset1 (Long) + asset2 (Long) -> Pair
 */
@Slf4j
public class PairMapImpl extends DBTabImpl<Tuple2<Long, Long>, TradePair> implements PairMap {

    public PairMapImpl(int dbs, DLSet databaseSet, DB database) {
        super(dbs, databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_PAIR_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_PAIR_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_PAIR_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_PAIR_TYPE);
        }
    }

    public PairMapImpl(int dbs, PairMap parent, DLSet dcSet) {
        super(dbs, parent, dcSet);
    }

    @Override
    public void openMap() {
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new PairSuitRocksDB(databaseSet, database);
                    break;
                default:
                    map = new PairSuitMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                default:
                    map = new PairSuitMapDBFork((PairMap) parent, databaseSet);
            }
        }
    }

    public IteratorCloseable<Tuple2<Long, Long>> getIterator(long have) {
        return ((PairSuit) map).getIterator(have);
    }


    public TradePair get(Long asset1, Long asset2) {
        return this.get(new Tuple2<Long, Long>(asset1, asset2));
    }

    public TradePair get(TradePair tradePair) {
        return this.get(new Tuple2<Long, Long>(tradePair.getAssetKey1(), tradePair.getAssetKey2()));
    }

    public void put(TradePair tradePair) {
        this.put(new Tuple2<Long, Long>(tradePair.getAssetKey1(), tradePair.getAssetKey2()), tradePair);
    }

    @Override
    public void put(Tuple2<Long, Long> key, TradePair tradePair) {
        super.put(key, tradePair);
        // reverse KEY and PAIR
        super.put(new Tuple2<Long, Long>(key.b, key.a), TradePair.reverse(tradePair));
    }

    public void delete(TradePair tradePair) {
        this.delete(new Tuple2<Long, Long>(tradePair.getAssetKey1(), tradePair.getAssetKey2()));
    }

}
