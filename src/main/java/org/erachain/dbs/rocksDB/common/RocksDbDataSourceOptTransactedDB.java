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

    public RocksDbDataSourceOptTransactedDB(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings,
                                            WriteOptions writeOptions) {
        super(pathName, name, indexes, settings, writeOptions);
        // Создаем или открываем ДБ
        initDB();
        // оборачиваем ее к костюм
        table = new RocksDbComOptTransactedDB(dbCore);
        afterOpenTable();
    }

    public RocksDbDataSourceOptTransactedDB(String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER, name, indexes, settings,
                new WriteOptions().setSync(true).setDisableWAL(false));
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
