package org.erachain.dbs.rocksDB.integration;

import lombok.Getter;
import lombok.Setter;
import org.erachain.dbs.rocksDB.common.DB;
import org.erachain.dbs.rocksDB.exceptions.UnsupportedRocksDBOperationException;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.rocksdb.ColumnFamilyHandle;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO зачем выделен этот файл, какой функционал он несет, почему нельзя было его встрогить в супер
 * Это реализация InnerDBTable для тестирования.  В данный момент не актуален.
 * Встроить можно все что угодно куда угодно
 * это упрощенный для тестов?
 * @param <K>
 * @param <V>
 */
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
    public Iterator<K> getIterator(boolean descending) {
        return null;
    }

    @Override
    public Iterator<K> getIndexIterator(ColumnFamilyHandle indexDB, boolean descending) {
        return null;
    }

    @Override
    public Iterator<K> getIndexIteratorFilter(byte[] filter, boolean descending) {
        return null;
    }

    @Override
    public Iterator<K> getIndexIteratorFilter(ColumnFamilyHandle indexDB, byte[] filter, boolean descending) {
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

    @Override
    public void close() { db.close(); }
    @Override
    public void commit() {}
    @Override
    public void rollback() {}
}
