package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;

import java.util.List;

/**
 * База данных RocksDB с поддержкой транзакционной модели.
 * Причем сама база не делает commit & rollback. Для этого нужно отдельно создавать Транзакцию
 */
@Slf4j
public class RocksDbComOptTransactedDB extends RocksDbComDB {

    public RocksDbComOptTransactedDB(RocksDB transactionDB) {
        super(transactionDB);
    }

    //@Override
    public static OptimisticTransactionDB openDB(String file, DBOptions dbOptions,
                                                 List<ColumnFamilyDescriptor> columnFamilyDescriptors,
                                                 List<ColumnFamilyHandle> columnFamilyHandles) throws RocksDBException {
        return OptimisticTransactionDB.open(dbOptions, file, columnFamilyDescriptors, columnFamilyHandles);
    }

}