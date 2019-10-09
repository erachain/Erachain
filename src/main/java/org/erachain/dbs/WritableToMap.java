package org.erachain.dbs;

import org.erachain.database.IDB;

/**
 *
 */
public interface WritableToMap<K, V> {

    IDB getDBSet();

    void close();

    void writeTo(WritableToMap dbaSetMap);

    void put(K key, V value);

    void delete(K key);

}
