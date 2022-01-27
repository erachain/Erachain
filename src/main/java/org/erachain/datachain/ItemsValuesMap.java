package org.erachain.datachain;

import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.ItemsValuesMapDB;
import org.erachain.dbs.nativeMemMap.NativeMapTreeMapFork;
import org.erachain.dbs.rocksDB.ItemsValuesRocksDB;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;

import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Хранит по вещи что угодно<br>
 * <br>
 * Ключ: номер актив + байт тип + байт тип связи<br>
 * Значение: что угодно - byte[]<br>
 */
public class ItemsValuesMap extends DBTabImpl<Tuple3<Long, Byte, Byte>, byte[]> {

    public ItemsValuesMap(int dbs, DCSet databaseSet, DB database) {
        super(dbs, databaseSet, database);
    }

    public ItemsValuesMap(int dbs, ItemsValuesMap parent, DCSet dcSet) {
        super(dbs, parent, dcSet);
    }

    @Override
    public void openMap() {
        // OPEN MAP
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new ItemsValuesRocksDB(databaseSet, database);
                    break;
                default:
                    map = new ItemsValuesMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    //map = new BlocksSuitMapDBFotk((TransactionMap) parent, databaseSet);
                    //break;
                default:
                    map = new NativeMapTreeMapFork(parent, databaseSet, Fun.TUPLE3_COMPARATOR, this);
            }
        }
    }

}
