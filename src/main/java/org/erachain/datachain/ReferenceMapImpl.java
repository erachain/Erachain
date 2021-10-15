package org.erachain.datachain;

import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.ReferenceSuitMapDB;
import org.erachain.dbs.mapDB.ReferenceSuitMapDBFork;
import org.erachain.dbs.nativeMemMap.NativeMapTreeMapFork;
import org.erachain.dbs.rocksDB.ReferenceSuitRocksDB;
import org.mapdb.DB;
import org.mapdb.Fun;

import static org.erachain.database.IDB.DBS_MAP_DB;
import static org.erachain.database.IDB.DBS_ROCK_DB;


/**
 * Используется для хранения последней ссылки на транзакцию со счета либо истории транзакций по счету
 * Для ускорения работы протокола сохранение Истории может быть выключено - смотри core.BlockChain#NOT_STORE_REFFS_HISTORY.
 * По идее раз запоминание истории выключено то можно использовать HashMap для ускорения. И всегда ее использовать для протокольных индексов,
 * а для поиска по диапазону всегда использовать вторичный индекс, который можно и отключить для скорости.
 * И так как список транзакций по создателю уже есть во вторичных ключах (datachain.TransactionFinalMapImpl#getTransactionsBySender)
 * seek reference to tx_Parent by address+timestamp
 * account.address -> LAST[TX.timestamp + TX.dbRef]
 * account.address + TX.timestamp -> PARENT[TX.timestamp + TX.dbRef]
 */
public class ReferenceMapImpl extends DBTabImpl<byte[], long[]>
        implements ReferenceMap {

    public ReferenceMapImpl(int dbsUsed, DCSet databaseSet, DB database) {
        super(dbsUsed, databaseSet, database);
    }

    public ReferenceMapImpl(int dbsUsed, ReferenceMap parent, DCSet dcSet) {
        super(dbsUsed, parent, dcSet);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new ReferenceSuitRocksDB(databaseSet, database);
                    break;
                default:
                    map = new ReferenceSuitMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_MAP_DB:
                    map = new ReferenceSuitMapDBFork((ReferenceMap) parent, databaseSet);
                    break;
                case DBS_ROCK_DB:
                    // not ready? map = new ReferenceSuitRocksDBFork((ReferenceMap) parent, databaseSet);
                    //break;
                default:
                    //not ready? map = new ReferenceSuitMapDBFork((ReferenceMap) parent, databaseSet);
                    map = new NativeMapTreeMapFork(parent, databaseSet, Fun.BYTE_ARRAY_COMPARATOR, this);
            }
        }
    }

}