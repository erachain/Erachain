package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;

/**
 * Самый низкий уровень доступа к функциям RocksDB
 */
@Slf4j
public class RocksDbComTransactDB implements RocksDbCom {

    TransactionDB dbCore;

    public RocksDbComTransactDB(TransactionDB transactionDB) {
        this.dbCore = transactionDB;
    }

    @Override
    public void put(byte[] key, byte[] value) throws RocksDBException {
        dbCore.put(key, value);
    }

    @Override
    public void put(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value) throws RocksDBException {
        dbCore.put(columnFamilyHandle, key, value);
    }

    @Override
    public void put(byte[] key, byte[] value, WriteOptions writeOptions) throws RocksDBException {
        dbCore.put(key, value);
    }

    @Override
    public byte[] get(byte[] key) throws RocksDBException {
        return dbCore.get(key);
    }

    @Override
    public byte[] get(ColumnFamilyHandle columnFamilyHandle, byte[] key) throws RocksDBException {
        return dbCore.get(columnFamilyHandle, key);
    }

    @Override
    public void remove(byte[] key) throws RocksDBException {
        dbCore.delete(key);
    }

    @Override
    public void remove(ColumnFamilyHandle columnFamilyHandle, byte[] key) throws RocksDBException {
        dbCore.delete(columnFamilyHandle, key);
    }

    @Override
    public void remove(byte[] key, WriteOptions writeOptions) throws RocksDBException {
        dbCore.delete(key);
    }

    @Override
    public RocksIterator getIterator() {
        return dbCore.newIterator(dbCore.getDefaultColumnFamily());
    }

    @Override
    public RocksIterator getIterator(ColumnFamilyHandle indexDB) {
        return dbCore.newIterator(indexDB);
    }

    @Override
    public void close() {
        dbCore.close();
    }

}