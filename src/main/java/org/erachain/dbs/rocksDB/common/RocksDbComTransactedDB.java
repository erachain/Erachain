package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDB;

/**
 * База данных RocksDB с поддержкой транзакционной модели.
 * Причем сама база не делает commit & rollback. Для этого нужно отдельно создавать Транзакцию
 */
@Slf4j
public class RocksDbComTransactedDB extends RocksDbComDB {

    public RocksDbComTransactedDB(RocksDB transactionDB) {
        super(transactionDB);
    }

}