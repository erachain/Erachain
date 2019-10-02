package org.erachain.dbs.rocksDB.common;

import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteOptions;

public interface RocksDbCom {

    //RocksDB createDB(String file, Options options,
    //                        List<ColumnFamilyDescriptor> columnFamilyDescriptors,
    //                        List<ColumnFamilyHandle> columnFamilyHandles) throws RocksDBException;

    //RocksDB openDB(String file, DBOptions dbOptions,
    //                      List<ColumnFamilyDescriptor> columnFamilyDescriptors,
    //                      List<ColumnFamilyHandle> columnFamilyHandles) throws RocksDBException;

    void put(byte[] key, byte[] value) throws RocksDBException;

    void put(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value) throws RocksDBException;

    void put(byte[] key, byte[] value, WriteOptions writeOptions) throws RocksDBException;

    boolean contains(byte[] key);

    boolean contains(ColumnFamilyHandle columnFamilyHandle, byte[] key);

    byte[] get(byte[] key) throws RocksDBException;

    byte[] get(ColumnFamilyHandle columnFamilyHandle, byte[] key) throws RocksDBException;

    void remove(byte[] key) throws RocksDBException;

    void remove(ColumnFamilyHandle columnFamilyHandle, byte[] key) throws RocksDBException;

    void remove(byte[] key, WriteOptions writeOptions) throws RocksDBException;

    void remove(ColumnFamilyHandle columnFamilyHandle, byte[] key, WriteOptions writeOptions) throws RocksDBException;

    RocksIterator getIterator();

    RocksIterator getIterator(ColumnFamilyHandle indexDB);

    void close();

}
