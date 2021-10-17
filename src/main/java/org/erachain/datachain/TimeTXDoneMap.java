package org.erachain.datachain;

import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.TimeDoneSuitMapDB;
import org.erachain.dbs.mapDB.TimeDoneSuitMapDBFork;
import org.mapdb.DB;

import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Хранит исполненные транзакции, или отмененные - все что уже не активно для запуска по времени<br>
 * <br>
 * Ключ: ссылка на ID транзакции, значение - ожидаемый блок<br>
 * Значение: заказ<br>
 */
public class TimeTXDoneMap extends DBTabImpl<Long, Integer> {

    public TimeTXDoneMap(int dbs, DCSet databaseSet, DB database) {
        super(dbs, databaseSet, database);
    }

    public TimeTXDoneMap(int dbs, TimeTXDoneMap parent, DCSet dcSet) {
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
                    map = new TimeDoneSuitMapDBFork((TimeTXDoneMap) parent, databaseSet);
            }
        }
    }
}
