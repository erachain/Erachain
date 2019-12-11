package org.erachain.dbs.rocksDB.comm;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.settings.Settings;
import org.rocksdb.*;

import java.util.ArrayList;
import java.util.List;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

/**
 * База данных RocksDB с поддержкой транзакционной модели.
 * Причем сама база не делает commit & rollback. Для этого нужно отдельно создавать Транзакцию
 */
@Slf4j
public class RocksDbDataSourceOptTransactedDB extends RocksDbDataSourceTransactionedImpl {


    public RocksDbDataSourceOptTransactedDB(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings,
                                            TransactionDBOptions transactionDbOptions,
                                            WriteOptions writeOptions, boolean enableSize) {
        super(pathName, name, indexes, settings, writeOptions, enableSize);

        this.transactionDbOptions = transactionDbOptions;

        // Создаем или открываем ДБ
        initDB();
    }

    public RocksDbDataSourceOptTransactedDB(String name, List<IndexDB> indexes, RocksDbSettings settings, boolean enableSize) {
        this(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER, name, indexes, settings,
                new TransactionDBOptions(),
                new WriteOptions().setSync(true).setDisableWAL(false), enableSize);
    }

    public RocksDbDataSourceOptTransactedDB(String name, boolean enableSize) {
        this(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER, name, new ArrayList<>(),
                new RocksDbSettings(),
                new TransactionDBOptions(),
                new WriteOptions().setSync(true).setDisableWAL(false), enableSize);
    }

    @Override
    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = OptimisticTransactionDB.open(options, getDbPathAndFile().toString());
        // создаем позже открытия иначе крах
        dbOptions = new DBOptions(options);
    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = OptimisticTransactionDB.open(dbOptions, getDbPathAndFile().toString(), columnFamilyDescriptors, columnFamilyHandles);
        this.dbOptions = dbOptions;
    }

    public void beginTransaction(WriteOptions writeOptions, ReadOptions readOptions) {
        this.readOptions = readOptions;
        this.writeOptions = writeOptions;

        this.dbTransaction = ((OptimisticTransactionDB) dbCore).beginTransaction(writeOptions);

    }

    public void beginTransaction() {
        beginTransaction(new WriteOptions(), new ReadOptions());
    }

}