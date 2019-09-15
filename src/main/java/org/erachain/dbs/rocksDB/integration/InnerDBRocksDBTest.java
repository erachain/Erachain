package org.erachain.dbs.rocksDB.integration;

import lombok.Getter;
import lombok.Setter;
import org.erachain.dbs.rocksDB.common.DB;
import org.erachain.dbs.rocksDB.exceptions.UnsupportedRocksDBOperationException;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;

import java.util.*;

public class InnerDBRocksDBTest<K, V> implements InnerDBTable<K, V> {
    @Getter
    @Setter
    private DB db;
    private List<SimpleIndexDB> indexes;
    @Getter
    @Setter
    private Byteable byteable = new ByteableTransaction();

    @Override
    public Map<K, V> getMap() {
        throw new UnsupportedRocksDBOperationException();
    }

    @Override
    public int size() {
        return (int) db.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return db.get(key) != null;
    }

    @Override
    public V get(Object key) {
        return (V) db.get(key);
    }

    @Override
    public void put(K key, V value) {
        db.put(key,value);
    }

    @Override
    public void remove(Object key) {
        db.remove(key);
    }

    @Override
    public void clear() {
        db.reset();
    }

    @Override
    public Set<K> keySet() {
        return receiveKeySet();
    }

    @Override
    public Set<V> values() {
        return receiveValues();
    }

    @Override
    public void close() {
    }

    @Override
    public Iterator<K> getIterator(boolean descending) {
        return null;
    }

    @Override
    public Iterator<K> getIndexIterator(IndexDB indexDB, boolean descending) {
        return null;
    }

    @Override
    public Iterator<K> getIndexIterator(int indexDB, boolean descending) {
        return null;
    }

    private Set<K> receiveKeySet() {
        return (Set<K>) db.keySet();
    }

    private Set<V> receiveValues() {
        return (Set<V>) db.values();
    }
}
