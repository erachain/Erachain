/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.erachain.dbs.rocksDB.common;

import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksIterator;

import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * TODO ??
 * @param <V>
 */
public interface DbSourceInter<V> extends BatchSourceInter<byte[], V> {

    RocksIterator getIterator();
    RocksIterator getIterator(ColumnFamilyHandle indexDB);

    String getDBName();

    void setDBName(String name);

    void initDB(List<IndexDB> indexes);

    boolean isAlive();

    void close();
    void commit();
    void rollback();
    void reset();

    Set<byte[]> allKeys() throws RuntimeException;

    Collection<byte[]> allValues() throws RuntimeException;

    Collection<byte[]> filterApprropriateKeys(byte[] filter) throws RuntimeException;
    Collection<byte[]> filterApprropriateValues(byte[] filter, ColumnFamilyHandle IndexDB);
    Collection<byte[]> filterApprropriateValues(byte[] filter, int IndexDB);

}
