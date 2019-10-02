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
                                        TransactionDB dbCore, List<ColumnFamilyHandle> columnFamilyHandles,
                                        WriteOptions writeOptions, ReadOptions readOptions) {
        super(pathName, name, indexes, null);
        this.dbCore = dbCore;
        this.columnFamilyHandles = columnFamilyHandles;
        this.readOptions = readOptions;
        this.writeOptions = writeOptions;

        table = new RocksDbComTransaction(dbCore, writeOptions, readOptions);

    }

    public RocksDbDataSourceTransaction(String pathName, String name, List<IndexDB> indexes,
                                        TransactionDB dbCore, List<ColumnFamilyHandle> columnFamilyHandles) {
        this(pathName, name, indexes, dbCore, columnFamilyHandles,
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions());
    }

    @Override
    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) {
        return;
    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) {
        return;
    }

    /**
     * Close only Transaction - not parentDB
     */
    @Override
    public void close() {
        resetDbLock.writeLock().lock();
        try {
            table.close();
        } catch (Exception e) {
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

}
