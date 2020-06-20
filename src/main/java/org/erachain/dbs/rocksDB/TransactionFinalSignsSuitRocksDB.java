package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.datachain.TransactionFinalMapSignsSuit;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.mapdb.DB;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

@Slf4j
public class TransactionFinalSignsSuitRocksDB extends DBMapSuit<byte[], Long> implements TransactionFinalMapSignsSuit {

    private final String NAME_TABLE = "TRANSACTION_FINAL_SIGNS_TABLE";

    /**
     * Задает обрезание длинны подписи - чем меньше тем быстрее поиск и запись но больше вероятность повтора и отклонения.
     * Если задаем ключ более 16 байт то не нужна проверка на Двойную запись - иначе долгий поиск в РоксДБ получается.
     * При изменени - нужно пересобрать всю базу трнзакций иначе поиск сломается с новой длинной
     */
    public static int KEY_LEN = 24;

    public TransactionFinalSignsSuitRocksDB(DBASet databaseSet, DB database, boolean sizeEnable) {
        super(databaseSet, database, logger, sizeEnable);
    }

    @Override
    public void openMap() {

        // see https://github.com/facebook/rocksdb/wiki/Read-Modify-Write-Benchmarks
        map = new DBRocksDBTableDBCommitedAsBath<>(new ByteableTrivial(), new ByteableLong(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(false, false).setReadaheadSize(100).setFillCache(false),
                databaseSet, sizeEnable);
    }

    @Override
    public boolean contains(byte[] signature) {

        byte[] key = new byte[Integer.min(KEY_LEN, signature.length)];
        System.arraycopy(signature, 0, key, 0, key.length);
        return super.contains(key);
    }

    @Override
    public Long get(byte[] signature) {
        byte[] key = new byte[Integer.min(KEY_LEN, signature.length)];
        System.arraycopy(signature, 0, key, 0, key.length);
        return super.get(key);
    }

    @Override
    public void delete(byte[] signature) {
        byte[] key = new byte[Integer.min(KEY_LEN, signature.length)];
        System.arraycopy(signature, 0, key, 0, key.length);
        super.delete(key);
    }

    @Override
    public Long remove(byte[] signature) {
        byte[] key = new byte[Integer.min(KEY_LEN, signature.length)];
        System.arraycopy(signature, 0, key, 0, key.length);
        return super.remove(key);
    }

    @Override
    public boolean set(byte[] signature, Long refernce) {
        byte[] key = new byte[Integer.min(KEY_LEN, signature.length)];
        System.arraycopy(signature, 0, key, 0, key.length);
        return super.set(key, refernce);

    }

    @Override
    public void put(byte[] signature, Long refernce) {
        byte[] key = new byte[Integer.min(KEY_LEN, signature.length)];
        System.arraycopy(signature, 0, key, 0, key.length);
        super.put(key, refernce);

    }

}
