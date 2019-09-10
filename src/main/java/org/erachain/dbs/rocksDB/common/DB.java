package org.erachain.dbs.rocksDB.common;

import org.erachain.dbs.rocksDB.indexes.IndexDB;

import java.util.Set;

public interface DB<K, V> {

    V get(K k);

    void put(K k, V v);

    int size();

    boolean isEmpty();

    void remove(K k);

    Set<K> keySet();

    void reset();

    Set<byte[]> values() throws RuntimeException;

    Set<byte[]> filterAppropriateValuesAsKeys(byte[] filter, IndexDB indexDB);

    Set<byte[]> filterAppropriateValuesAsKeys(byte[] filter);

    IndexDB recieveIndexByName(String name);

}
