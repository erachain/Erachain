package org.erachain.datachain;

import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.mapDB.TimeWaitSuitMapDB;
import org.erachain.dbs.mapDB.TimeWaitSuitMapDBFork;
import org.mapdb.DB;
import org.mapdb.Fun;

import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Хранит исполненные трнзакции, или отмененные - все что уже не активно<br>
 * <br>
 * Ключ: ссылка на запись создания заказа<br>
 * Значение: заказ<br>
 */
public class TimeWaitMap extends DBTabImpl<Integer, Long> implements TimeTXintf<Integer, Long> {

    public TimeWaitMap(int dbs, DCSet databaseSet, DB database) {
        super(dbs, databaseSet, database);
    }

    public TimeWaitMap(int dbs, TimeWaitMap parent, DCSet dcSet) {
        super(dbs, parent, dcSet);
    }

    @Override
    public void openMap() {
        // OPEN MAP
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new TimeWaitSuitMapDB(databaseSet, database);
                    break;
                default:
                    map = new TimeWaitSuitMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new TimeWaitSuitMapDBFork((TimeWaitMap) parent, databaseSet);
                    break;
                default:
                    map = new TimeWaitSuitMapDBFork((TimeWaitMap) parent, databaseSet);
            }
        }
    }

    @Override
    public IteratorCloseable<Fun.Tuple2<Integer, Long>> getTXIterator() {
        return ((TimeTXintf) map).getTXIterator();
    }
}
