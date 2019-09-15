package org.erachain.dbs.rocksDB.integration;

import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.mapdb.Fun;

import java.util.*;

public class DBMapDB<K, V> implements InnerDBTable<K, V> {
    public DBMapDB() {
    }

    public DBMapDB(Map<K, V> map) {
        this.map = map;
    }

    private Map<K, V> map;

    @Override
    public Map<K, V> getMap() {
        return map;
    }

    public void setMap(Map<K, V> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }


    @Override
    public void put(K key, V value) {
         map.put(key, value);
    }

    @Override
    public void remove(Object key) {
        map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public void close() {

    }

    //@Override
    //public NavigableSet<Fun.Tuple2<?, T>> getIndex(int index, boolean descending) {
    //    return map.getIndex(index, descending);
    //}

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

}
