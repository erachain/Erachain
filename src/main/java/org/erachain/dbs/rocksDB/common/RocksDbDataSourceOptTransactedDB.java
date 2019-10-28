package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.settings.Settings;
import org.rocksdb.*;

import java.util.List;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

/**
 * База данных RocksDB с поддержкой транзакционной модели.
 * Причем сама база не делает commit & rollback. Для этого нужно отдельно создавать Транзакцию
 */
@Slf4j
public class RocksDbDataSourceOptTransactedDB extends RocksDbDataSourceImpl {

    TransactionDBOptions transactionDbOptions;

    public RocksDbDataSourceOptTransactedDB(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings,
                                            TransactionDBOptions transactionDbOptions,
                                            WriteOptions writeOptions, boolean enableSize) {
        super(pathName, name, indexes, settings, writeOptions, enableSize);

        this.transactionDbOptions = transactionDbOptions;

        // Создаем или открываем ДБ
        initDB();
        // оборачиваем ее к костюм
        table = new RocksDbComDB(dbCore);
    }

    public RocksDbDataSourceOptTransactedDB(String name, List<IndexDB> indexes, RocksDbSettings settings, boolean enableSize) {
        this(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER, name, indexes, settings,
                new TransactionDBOptions(),
                new WriteOptions().setSync(true).setDisableWAL(false), enableSize);
    }

    @Override
    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = OptimisticTransactionDB.open(options, getDbPathAndFile().toString());

    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = OptimisticTransactionDB.open(dbOptions, getDbPathAndFile().toString(), columnFamilyDescriptors, columnFamilyHandles);

    }

}
