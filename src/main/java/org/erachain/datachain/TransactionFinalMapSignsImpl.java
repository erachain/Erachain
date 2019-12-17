package org.erachain.datachain;

//04/01 +- 

import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.TransactionFinalSignsSuitMapDB;
import org.erachain.dbs.mapDB.TransactionFinalSignsSuitMapDBFork;
import org.erachain.dbs.nativeMemMap.NativeMapTreeMapFork;
import org.erachain.dbs.rocksDB.TransactionFinalSignsSuitRocksDB;
import org.mapdb.DB;
import org.mapdb.Fun;

import static org.erachain.database.IDB.*;

/**
 * Поиск по подписи ссылки на транзакцию
 * signature (as UUID bytes16) -> <BlockHeoght, Record No> (as Long)
 */
public class TransactionFinalMapSignsImpl extends DBTabImpl<byte[], Long> implements TransactionFinalMapSigns {

    public TransactionFinalMapSignsImpl(int dbs, DCSet databaseSet, DB database, boolean sizeEnable) {
        super(dbs, databaseSet, database, sizeEnable);
    }

    public TransactionFinalMapSignsImpl(int dbs, TransactionFinalMapSigns parent, DCSet dcSet, boolean sizeEnable) {
        super(dbs, parent, dcSet, sizeEnable);
    }

    @Override
    public void openMap() {
        // OPEN MAP
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new TransactionFinalSignsSuitRocksDB(databaseSet, database, sizeEnable);
                    break;
                default:
                    map = new TransactionFinalSignsSuitMapDB(databaseSet, database, sizeEnable);
            }
        } else {
            switch (dbsUsed) {
                case DBS_NATIVE_MAP:
                    map = new NativeMapTreeMapFork<>(parent, databaseSet, Fun.BYTE_ARRAY_COMPARATOR, this);
                    break;
                case DBS_ROCK_DB:
                case DBS_MAP_DB:
                default:
                    // поидее это самая быстрая реализация для больших блоков
                    map = new TransactionFinalSignsSuitMapDBFork((TransactionFinalMapSigns) parent, databaseSet, false);
            }
        }
    }
}