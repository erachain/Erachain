package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;

import java.util.List;

/**
 * База данных RocksDB с поддержкой транзакционной модели.
 * Причем сама база не делает commit & rollback. Для этого нужно отдельно создавать Транзакцию
 */
@Slf4j
public class RocksDbComTransactedDB extends RocksDbComDB {

    public RocksDbComTransactedDB(RocksDB transactionDB) {
        super(transactionDB);
    }

    public static TransactionDB createDB(String file, Options options, TransactionDBOptions transactionDbOptions) throws RocksDBException {
        return TransactionDB.open(options, transactionDbOptions, file);
    }

    public static TransactionDB openDB(String file, DBOptions dbOptions,
                                TransactionDBOptions transactionDbOptions,
                                 List<ColumnFamilyDescriptor> columnFamilyDescriptors,
                                 List<ColumnFamilyHandle> columnFamilyHandles) throws RocksDBException {
        return TransactionDB.open(dbOptions, transactionDbOptions, file, columnFamilyDescriptors, columnFamilyHandles);
    }

}