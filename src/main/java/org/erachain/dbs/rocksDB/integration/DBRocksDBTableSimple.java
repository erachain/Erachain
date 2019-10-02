package org.erachain.dbs.rocksDB.integration;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;

import java.util.ArrayList;
import java.util.List;

/**
 * Данный класс представляет собой основной доступ и функционал к таблице БД RocksDB
 * Тут происходит обработка настроенных вторичных индексов.
 * вызывается из SUIT
 *
 * @param <K>
 * @param <V>
 */
@Slf4j
public class DBRocksDBTableSimple<K, V> extends DBRocksDBTable
        <K, V> {

    public DBRocksDBTableSimple(Byteable byteableKey, Byteable byteableValue, String NAME_TABLE, List<IndexDB> indexes, RocksDbSettings settings, DBASet dbaSet) {
        super(byteableKey, byteableValue, NAME_TABLE, indexes, settings, dbaSet);
    }

    public DBRocksDBTableSimple(Byteable byteableKey, Byteable byteableValue, String NAME_TABLE, List<IndexDB> indexes, DBASet dbaSet) {
        this(byteableKey, byteableValue, NAME_TABLE, indexes, RocksDbSettings.getDefaultSettings(), dbaSet);
    }

    public DBRocksDBTableSimple(String NAME_TABLE) {
        this(new ByteableTrivial(), new ByteableTrivial(), NAME_TABLE,
                new ArrayList<>(), RocksDbSettings.getDefaultSettings(), null);
    }

    @Override
    public void openSource() {
        ///dbSource = new RocksDbDataSourceImpl(this.root, NAME_TABLE, indexes, settings);
    }
}
