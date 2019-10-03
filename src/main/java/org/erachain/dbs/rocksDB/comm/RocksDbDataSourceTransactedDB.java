package org.erachain.dbs.rocksDB.comm;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
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
public class RocksDbDataSourceTransactedDB extends RocksDbDataSourceImpl {

    TransactionDBOptions transactionDbOptions;
    public Transaction dbTransaction;
    WriteOptions writeOptions;
    ReadOptions readOptions;

    public RocksDbDataSourceTransactedDB(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings,
                                         TransactionDBOptions transactionDbOptions,
                                         WriteOptions writeOptions) {
        super(pathName, name, indexes, settings, writeOptions);

        this.transactionDbOptions = transactionDbOptions;

        // Создаем или открываем ДБ
        initDB();

    }

    public RocksDbDataSourceTransactedDB(String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER, name, indexes, settings,
                new TransactionDBOptions(),
                new WriteOptions().setSync(true).setDisableWAL(false));
    }

    @Override
    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = TransactionDB.open(options, transactionDbOptions, getDbPathAndFile().toString());
    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = TransactionDB.open(dbOptions, transactionDbOptions, getDbPathAndFile().toString(), columnFamilyDescriptors, columnFamilyHandles);
    }

    public void beginTransaction(WriteOptions writeOptions, ReadOptions readOptions) {
        this.readOptions = readOptions;
        this.writeOptions = writeOptions;

        this.dbTransaction = ((TransactionDB) dbCore).beginTransaction(writeOptions);

    }

}
