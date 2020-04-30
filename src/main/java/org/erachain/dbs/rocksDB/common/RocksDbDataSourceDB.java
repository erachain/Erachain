package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.settings.Settings;
import org.rocksdb.*;

import java.util.List;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

/**
 * Обычная база данных RocksDB - без Транзакционной системы
 */
@Slf4j
public class RocksDbDataSourceDB extends RocksDbDataSourceImpl {

    public RocksDbDataSourceDB(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings, WriteOptions writeOptions, boolean enableSize) {
        super(pathName, name, indexes, settings, writeOptions, enableSize);
        // Создаем или открываем ДБ
        initDB();
        // оборачиваем ее к костюм
        table = new RocksDbComDB(dbCore);
    }

    public RocksDbDataSourceDB(String name, List<IndexDB> indexes, RocksDbSettings settings, boolean enableSize) {
        this(Settings.getInstance().getDataChainPath() + ROCKS_DB_FOLDER, name, indexes, settings,
                new WriteOptions().setSync(true).setDisableWAL(false), enableSize);
    }

    @Override
    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = RocksDB.open(options, getDbPathAndFile().toString());
    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = RocksDB.open(dbOptions, getDbPathAndFile().toString(), columnFamilyDescriptors, columnFamilyHandles);

    }
}
