package org.erachain.rocksDB.integration;

import org.erachain.rocksDB.common.RocksDbSettings;
import org.erachain.rocksDB.transformation.ByteableTrivial;

import java.util.Set;

public class DBRocksDBSet {
    private byte[] VALUE = new byte[0];
    private final DBRocksDBTable<byte[], byte[]> rocksDBTable;
    public DBRocksDBSet(String NAME_SET, String root) {
        rocksDBTable = new DBRocksDBTable<>(new ByteableTrivial(), new ByteableTrivial(), NAME_SET, null, RocksDbSettings.getDefaultSettings(), root);

    }

    public boolean contains(byte[] key) {
        return rocksDBTable.containsKey(key);
    }

    public void add(byte[] key) {
        rocksDBTable.put(key, VALUE);
    }

    public void remove(byte[] key) {
        rocksDBTable.remove(key);
    }

    public void clear() {
        rocksDBTable.clear();
    }

    public long size() {
        return rocksDBTable.size();
    }

    public Set<byte[]> keySet() {
        return rocksDBTable.keySet();
    }

    public void close() {
        rocksDBTable.close();
    }
}
