package org.erachain.dbs.rocksDB.common;

import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.rocksdb.ColumnFamilyHandle;

import java.util.Collection;
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

    Collection<byte[]> values() throws RuntimeException;

    Set<byte[]> filterAppropriateValuesAsKeys(byte[] filter, ColumnFamilyHandle indexDB);

    Set<byte[]> filterAppropriateValuesAsKeys(byte[] filter, int indexDB);

    Set<byte[]> filterAppropriateValuesAsKeys(byte[] filter);

}
