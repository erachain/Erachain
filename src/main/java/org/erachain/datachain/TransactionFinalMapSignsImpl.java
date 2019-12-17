package org.erachain.datachain;

//04/01 +- 

import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.TransactionFinalSignsSuitMapDB;
import org.erachain.dbs.mapDB.TransactionFinalSignsSuitMapDBFork;
import org.erachain.dbs.nativeMemMap.NativeMapTreeMapFork;
import org.erachain.dbs.rocksDB.TransactionFinalSignsSuitRocksDB;
import org.mapdb.DB;
import org.mapdb.Fun;

import static org.erachain.database.IDB.DBS_MAP_DB;
import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Поиск по подписи ссылки на транзакцию
 * signature (as UUID bytes16) -> <BlockHeoght, Record No> (as Long)
 */
public class TransactionFinalMapSignsImpl extends DBTabImpl<byte[], Long> implements TransactionFinalMapSigns {

    /**
     * Задает обрезание длинны подписи - чем меньше тем быстрее поиск и запись но больше вероятность повтора и отклонения
     */
    public static int KEY_LEN = 8;

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
                case DBS_MAP_DB:
                    map = new TransactionFinalSignsSuitMapDBFork((TransactionFinalMapSigns) parent, databaseSet, true);
                    break;
                case DBS_ROCK_DB:
                default:
                    map = new NativeMapTreeMapFork(parent, databaseSet, Fun.BYTE_ARRAY_COMPARATOR, this);
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
        super.delete(key);
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

    @Override
    public void put(byte[] signature, Long refernce) {
        byte[] key = new byte[KEY_LEN];
        System.arraycopy(signature, 0, key, 0, KEY_LEN);
        super.put(key, refernce);

    }

}