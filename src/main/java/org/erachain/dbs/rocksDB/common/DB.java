package org.erachain.dbs.rocksDB.common;

import org.rocksdb.ColumnFamilyHandle;

import java.util.List;
import java.util.Set;

/**
 * TODO зачем выделен этот файл, какой функционал он несет, почему нельзя было его встрогить в супер
 * Этот интерфейс позаимствовани из проекта "tron". Скорее всего он использовался для разделения функционала.
 * Можно удалить.
 * Встроить можно все что угодно куда угодно
 * Почему не взят класс из DB MapDB или не сделан общий?
 * @param <K>
 * @param <V>
 */
public interface DB<K, V> {

    V get(K k);

    void put(K k, V v);

    int size();

    boolean isEmpty();

    void remove(K k);

    Set<K> keySet();

    void reset();

    List<ColumnFamilyHandle> getColumnFamilyHandles();

    List<byte[]> values() throws RuntimeException;

    List<byte[]> filterAppropriateValuesAsKeys(byte[] filter, ColumnFamilyHandle indexDB);

    List<byte[]> filterAppropriateValuesAsKeys(byte[] filter, int indexDB);

    List<byte[]> filterAppropriateValuesAsKeys(byte[] filter);

    void close();
    void commit();
    void rollback();

}
