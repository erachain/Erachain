package org.erachain.dbs.rocksDB.integration;

import org.rocksdb.ColumnFamilyHandle;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * TODO зачем выделен этот файл, какой функционал он несет
 * Почему не взять сразу RocksDB ?
 * @param <K>
 * @param <V>
 */
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

    void commit();

    void rollback();

    //NavigableSet<Fun.Tuple2<?, K>> getIndex(int index, boolean descending);

    Iterator<K> getIterator(boolean descending);

    Iterator<K> getIndexIterator(ColumnFamilyHandle indexDB, boolean descending);

    Iterator<K> getIndexIteratorFilter(byte[] filter, boolean descending);

    Iterator<K> getIndexIteratorFilter(ColumnFamilyHandle indexDB, byte[] filter, boolean descending);

    Iterator<K> getIndexIterator(int indexDB, boolean descending);

}
