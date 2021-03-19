package org.erachain.dbs.rocksDB.integration;

import org.erachain.dbs.IteratorCloseable;
import org.rocksdb.ColumnFamilyHandle;

import java.util.Collection;
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

    IteratorCloseable<K> getIterator(boolean descending, boolean isIndex);

    IteratorCloseable<K> getIndexIterator(ColumnFamilyHandle indexDB, boolean descending, boolean isIndex);

    IteratorCloseable<K> getIndexIteratorFilter(byte[] filter, boolean descending, boolean isIndex);

    IteratorCloseable<K> getIndexIteratorFilter(byte[] start, byte[] stop, boolean descending, boolean isIndex);

    IteratorCloseable<K> getIndexIteratorFilter(K start, K stop, boolean descending, boolean isIndex);

    IteratorCloseable<K> getIndexIteratorFilter(ColumnFamilyHandle indexDB, byte[] filter, boolean descending, boolean isIndex);

    IteratorCloseable<K> getIndexIteratorFilter(ColumnFamilyHandle indexDB, byte[] start, byte[] stop, boolean descending, boolean isIndex);

    IteratorCloseable<K> getIndexIterator(int indexDB, boolean descending, boolean isIndex);

}
