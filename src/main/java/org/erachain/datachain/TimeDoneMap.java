package org.erachain.datachain;

import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.TimeDoneSuitMapDB;
import org.erachain.dbs.mapDB.TimeDoneSuitMapDBFork;
import org.mapdb.DB;

import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Хранит исполненные трнзакции, или отмененные - все что уже не активно<br>
 * <br>
 * Ключ: ссылка на запись создания заказа<br>
 * Значение: заказ<br>
 */
public class TimeDoneMap extends DBTabImpl<Integer, Long> {

    public TimeDoneMap(int dbs, DCSet databaseSet, DB database) {
        super(dbs, databaseSet, database);
    }

    public TimeDoneMap(int dbs, TimeDoneMap parent, DCSet dcSet) {
        super(dbs, parent, dcSet);
    }

    @Override
    public void openMap() {
        // OPEN MAP
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new TimeDoneSuitMapDB(databaseSet, database);
                    break;
                default:
                    map = new TimeDoneSuitMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    //map = new BlocksSuitMapDBFotk((TransactionMap) parent, databaseSet);
                    //break;
                default:
                    map = new TimeDoneSuitMapDBFork((TimeDoneMap) parent, databaseSet);
            }
        }
    }
}
