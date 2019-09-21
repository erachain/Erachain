package org.erachain.dbs.rocksDB.common;

import org.rocksdb.ColumnFamilyHandle;

import java.util.List;
import java.util.Set;

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

}
