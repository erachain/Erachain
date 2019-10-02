package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;

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

    /**
     * if a value is found in block-cache
     */
    final StringBuilder inCache = new StringBuilder();
    @Override
    public boolean contains(byte[] key) {
        return rocksDB.keyMayExist(key, inCache // NULL not worked
        );
    }

    @Override
    public boolean contains(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        return rocksDB.keyMayExist(columnFamilyHandle, key, inCache // NULL not worked
        );
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
        rocksDB.delete(writeOptions, key);
    }

    @Override
    public void remove(ColumnFamilyHandle columnFamilyHandle, byte[] key, WriteOptions writeOptions) throws RocksDBException {
        rocksDB.delete(columnFamilyHandle, writeOptions, key);
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