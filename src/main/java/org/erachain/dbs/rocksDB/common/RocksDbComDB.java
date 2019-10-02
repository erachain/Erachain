package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;

import java.util.List;

/**
 * Обычная база данных RocksDB - без Транзакционной системы
 */
@Slf4j
public class RocksDbComDB implements RocksDbCom {

    RocksDB rocksDB;
    protected final ColumnFamilyHandle defaultColumnFamily;

    public RocksDbComDB(RocksDB rocksDB) {
        this.rocksDB = rocksDB;
        defaultColumnFamily = rocksDB.getDefaultColumnFamily();
    }

    /**
     * Create DB and columnFamily
     *
     * @param file
     * @param options
     * @param columnFamilyDescriptors
     * @param columnFamilyHandles
     * @return
     * @throws RocksDBException
     */
    public static RocksDB createDB(String file, Options options,
                                   List<ColumnFamilyDescriptor> columnFamilyDescriptors,
                                   List<ColumnFamilyHandle> columnFamilyHandles) throws RocksDBException {
        // MAKE DATABASE
        RocksDB rocksDB = RocksDB.open(options, file);

        // MAKE COLUMNS FAMILY
        columnFamilyHandles.add(rocksDB.getDefaultColumnFamily());
        for (ColumnFamilyDescriptor columnFamilyDescriptor : columnFamilyDescriptors) {
            ColumnFamilyHandle columnFamilyHandle = rocksDB.createColumnFamily(columnFamilyDescriptor);
            columnFamilyHandles.add(columnFamilyHandle);
        }

        return rocksDB;
    }

    public static RocksDB openDB(String file, DBOptions dbOptions,
                     List<ColumnFamilyDescriptor> columnFamilyDescriptors,
                     List<ColumnFamilyHandle> columnFamilyHandles) throws RocksDBException {
        return RocksDB.open(dbOptions, file, columnFamilyDescriptors, columnFamilyHandles);
    }

    @Override
    public void put(byte[] key, byte[] value) throws RocksDBException {
        rocksDB.put(key, value);
    }

    @Override
    public void put(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value) throws RocksDBException {
        rocksDB.put(columnFamilyHandle, key, value);
    }

    @Override
    public void put(byte[] key, byte[] value, WriteOptions writeOptions) throws RocksDBException {
        rocksDB.put(key, value);
    }

    @Override
    public byte[] get(byte[] key) throws RocksDBException {
        return rocksDB.get(key);
    }

    @Override
    public byte[] get(ColumnFamilyHandle columnFamilyHandle, byte[] key) throws RocksDBException {
        return rocksDB.get(columnFamilyHandle, key);
    }

    @Override
    public void remove(byte[] key) throws RocksDBException {
        rocksDB.delete(key);
    }

    @Override
    public void remove(ColumnFamilyHandle columnFamilyHandle, byte[] key) throws RocksDBException {
        rocksDB.delete(columnFamilyHandle, key);
    }

    @Override
    public void remove(byte[] key, WriteOptions writeOptions) throws RocksDBException {
        rocksDB.delete(key);
    }

    @Override
    public RocksIterator getIterator() {
        return rocksDB.newIterator(defaultColumnFamily);
    }

    @Override
    public RocksIterator getIterator(ColumnFamilyHandle indexDB) {
        return rocksDB.newIterator(indexDB);
    }

    @Override
    public void close() {
        rocksDB.close();
    }

}