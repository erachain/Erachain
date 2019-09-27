package org.erachain.datachain;

//04/01 +- 

import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.TransactionFinalSignsSuitMapDB;
import org.erachain.dbs.mapDB.TransactionFinalSignsSuitMapDBFork;
import org.erachain.dbs.rocksDB.TransactionFinalSignsSuitRocksDB;
import org.mapdb.DB;

import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Поиск по подписи ссылки на транзакцию
 * signature (as UUID bytes16) -> <BlockHeoght, Record No> (as Long)
 */
public class TransactionFinalMapSignsImpl extends DBTabImpl<byte[], Long> implements TransactionFinalMapSigns {

    static int KEY_LEN = 12;

    public TransactionFinalMapSignsImpl(int dbs, DCSet databaseSet, DB database) {
        super(dbs, databaseSet, database);
    }

    public TransactionFinalMapSignsImpl(int dbs, TransactionFinalMapSigns parent, DCSet dcSet) {
        super(dbs, parent, dcSet);
    }

    @Override
    protected void getMap() {
        // OPEN MAP
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new TransactionFinalSignsSuitRocksDB(databaseSet, database);
                    break;
                default:
                    map = new TransactionFinalSignsSuitMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    //map = new TransactionFinalSignsSuitRocksDBFork((TransactionFinalMapSigns) parent, databaseSet);
                    //break;
                default:
                    //map = new nativeMapTreeMapFork(parent, databaseSet, Fun.BYTE_ARRAY_COMPARATOR, null);
                    map = new TransactionFinalSignsSuitMapDBFork((TransactionFinalMapSigns) parent, databaseSet);
            }
        }
    }


    @Override
    public boolean contains(byte[] signature) {

        byte[] key = new byte[KEY_LEN];
        System.arraycopy(signature, 0, key, 0, KEY_LEN);
        return super.contains(key);
    }

    @Override
    public Long get(byte[] signature) {
        byte[] key = new byte[KEY_LEN];
        System.arraycopy(signature, 0, key, 0, KEY_LEN);
        return super.get(key);
    }

    @Override
    public void delete(byte[] signature) {
        byte[] key = new byte[KEY_LEN];
        System.arraycopy(signature, 0, key, 0, KEY_LEN);
        super.remove(key);
    }

    @Override
    public Long remove(byte[] signature) {
        byte[] key = new byte[KEY_LEN];
        System.arraycopy(signature, 0, key, 0, KEY_LEN);
        return super.remove(key);
    }

    @Override
    public boolean set(byte[] signature, Long refernce) {
        byte[] key = new byte[KEY_LEN];
        System.arraycopy(signature, 0, key, 0, KEY_LEN);
        return super.set(key, refernce);

    }

}