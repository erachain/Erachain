package org.erachain.dbs.rocksDB.integration;

import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;

/**
 * Это реализация InnerDBTable для тестирования.
 * @param <K>
 * @param <V>
 */
public class InnerDBRocksDBTest<K, V> extends DBRocksDBTable<K, V> implements InnerDBTable<K, V> {

    public InnerDBRocksDBTest(String NAME_TABLE) {
        super(new ByteableTrivial(), new ByteableTrivial(), NAME_TABLE,
                null, RocksDbSettings.getDefaultSettings(), null);
    }

}
