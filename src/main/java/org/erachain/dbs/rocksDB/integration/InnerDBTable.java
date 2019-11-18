package org.erachain.dbs.rocksDB.integration;

import org.rocksdb.ColumnFamilyHandle;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Этот файл изначально был создан как интерфейс для "закрытия" бд,
 * в связи с индексами этот подход не получился.\Может быть удален
 * @param <K>
 * @param <V>
 */
public interface InnerDBTable<K, V> {

    void openSource();

    Map<K,V> getMap();

    int size();

    boolean containsKey(Object key);

    V get(Object key);

    boolean set(K key, V value);

    void put(K key, V value);

    V remove(Object key);

    V removeValue(Object key);

    void delete(Object key);

    void deleteValue(Object key);

    void clear();

    Set<K> keySet();

    Collection<V> values();

    void clearCache();

    void close();

    //NavigableSet<Fun.Tuple2<?, K>> getIndex(int index, boolean descending);

    Iterator<K> getIterator(boolean descending, boolean isIndex);

    Iterator<K> getIndexIterator(ColumnFamilyHandle indexDB, boolean descending, boolean isIndex);

    Iterator<K> getIndexIteratorFilter(byte[] filter, boolean descending, boolean isIndex);

    Iterator<K> getIndexIteratorFilter(ColumnFamilyHandle indexDB, byte[] filter, boolean descending, boolean isIndex);

    Iterator<K> getIndexIterator(int indexDB, boolean descending, boolean isIndex);

}
