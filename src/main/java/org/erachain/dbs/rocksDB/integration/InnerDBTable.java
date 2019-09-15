package org.erachain.dbs.rocksDB.integration;

import org.erachain.dbs.rocksDB.indexes.IndexDB;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public interface InnerDBTable<K, V> {

    Map<K,V> getMap();

    int size();

    boolean containsKey(Object key);

    V get(Object key);

    void put(K key, V value);

    void remove(Object key);

    // TODO: нужно сделать это - у РоксДБ естьт акое
    //void removeValue(Object key);

    void clear();

    Set<K> keySet();

    Collection<V> values();

    void close();

    Iterator<K> getIterator(boolean descending);

    Iterator<K> getIndexIterator(boolean descending, IndexDB indexDB);

    Iterator<K> getIndexIterator(boolean descending, int indexDB);

}
