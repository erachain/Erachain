package org.erachain.rocksDB.integration;

import org.erachain.rocksDB.common.RocksDbSettings;
import org.erachain.rocksDB.transformation.Byteable;
import org.erachain.rocksDB.transformation.ByteableTrivial;

import java.util.List;
import java.util.Set;

public class DBRocksDBSetByteable<E> {
    private byte[] VALUE = new byte[0];
    private Byteable byteable;
    private final DBRocksDBTable<E, byte[]> rocksDBTable;

    public DBRocksDBSetByteable(String NAME_SET, Byteable byteable, String root) {
        this.byteable = byteable;
        rocksDBTable = new DBRocksDBTable<>(byteable, new ByteableTrivial(), NAME_SET, null, RocksDbSettings.getDefaultSettings(), root);
    }

    public boolean contains(E key) {
        return rocksDBTable.containsKey(key);
    }

    public void add(E key) {
        rocksDBTable.put(key, VALUE);
    }

    public void addAll(List<E> keys) {
        for (E key : keys) {
            add(key);
        }
    }

    public void remove(E key) {
        rocksDBTable.remove(key);
    }

    public void clear() {
        rocksDBTable.clear();
    }

    public Set<E> keySet() {
        return rocksDBTable.keySet();
    }

    public long size() {
        return rocksDBTable.size();
    }

    public Set<E> getFromToKeys(E fromKey, long limit) {
        return rocksDBTable.keys(byteable.toBytesObject(fromKey), limit);
    }

    public void close() {
        rocksDBTable.close();
    }
}
