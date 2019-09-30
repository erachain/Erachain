package org.erachain.dbs.rocksDB.common;

import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteOptions;

import java.nio.file.Path;
import java.util.List;

public interface RocksDbCom {
    Path getDbPath();

    boolean isAlive();

    String getDBName();

    void put(byte[] key, byte[] value);

    void put(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value);

    void put(byte[] key, byte[] value, WriteOptions writeOptions);

    byte[] get(byte[] key);

    byte[] get(ColumnFamilyHandle columnFamilyHandle, byte[] key);

    void remove(byte[] key);

    void remove(ColumnFamilyHandle columnFamilyHandle, byte[] key);

    void remove(byte[] key, WriteOptions writeOptions);

    int size();

    //@Override
    int parentSize();

    boolean isEmpty();

    RocksIterator getIterator();

    RocksIterator getIterator(ColumnFamilyHandle indexDB);

    void close();

    org.rocksdb.Transaction getDbCore();

    org.rocksdb.TransactionDB getDbCoreParent();

    List<ColumnFamilyHandle> getColumnFamilyHandles();
}
