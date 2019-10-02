package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.rocksdb.*;

import java.util.List;

/**
 * База данных RocksDB с поддержкой транзакционной модели.
 * Причем сама база не делает commit & rollback. Для этого нужно отдельно создавать Транзакцию
 */
@Slf4j
public class RocksDbDataSourceTransaction extends RocksDbDataSourceImpl {

    ReadOptions readOptions;
    WriteOptions writeOptions;

    public RocksDbDataSourceTransaction(String pathName, String name, List<IndexDB> indexes,
                                        TransactionDB dbCore, WriteOptions writeOptions, ReadOptions readOptions) {
        super(pathName, name, indexes, null);
        this.dbCore = dbCore;
        this.readOptions = readOptions;
        this.writeOptions = writeOptions;
        ////initDB();
        table = new RocksDbComTransaction(dbCore);
    }

    public RocksDbDataSourceTransaction(TransactionDB dbCore, ReadOptions readOptions) {
        this.dbCore = dbCore;
        this.readOptions = readOptions;
    }

    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors,
                            ) throws RocksDBException {
        return;
    }

    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        return;
    }

}
