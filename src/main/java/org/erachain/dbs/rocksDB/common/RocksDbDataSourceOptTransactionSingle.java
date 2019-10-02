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
public class RocksDbDataSourceOptTransactionSingle extends RocksDbDataSourceImpl {

    ReadOptions readOptions;
    WriteOptions writeOptions;

    public RocksDbDataSourceOptTransactionSingle(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings,
                                                 WriteOptions writeOptions, ReadOptions readOptions) {
        super(pathName, name, indexes, settings, writeOptions);
        // Создаем или открываем ДБ
        initDB();

        this.readOptions = readOptions;
        this.writeOptions = writeOptions;

        // оборачиваем в костюм Транзакцию от нее
        table = new RocksDbComOptTransactionSingle((OptimisticTransactionDB) dbCore, writeOptions, readOptions);
        afterOpenTable();

    }

    public RocksDbDataSourceOptTransactionSingle(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this(pathName, name, indexes, settings,
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions());
    }

    @Override
    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = RocksDbComOptTransactedDB.createDB(getDbPathAndFile().toString(), options, columnFamilyDescriptors, columnFamilyHandles);
    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = RocksDbComOptTransactedDB.openDB(getDbPathAndFile().toString(), dbOptions, columnFamilyDescriptors, columnFamilyHandles);
    }

}
