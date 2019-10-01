package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;

import java.util.List;

/**
 * Самый низкий уровень доступа к функциям RocksDB
 */
@Slf4j
public class RocksDbComTransactDB implements RocksDbCom {

    TransactionDB transactionDB;
    protected final ColumnFamilyHandle defaultColumnFamily;

    public RocksDbComTransactDB(TransactionDB transactionDB) {
        this.transactionDB = transactionDB;
        defaultColumnFamily = transactionDB.getDefaultColumnFamily();
    }

    public static TransactionDB createDB(String file, Options options, TransactionDBOptions transactionDbOptions) throws RocksDBException {
        return TransactionDB.open(options, transactionDbOptions, file);
    }

    public static TransactionDB openDB(String file, DBOptions dbOptions,
                                TransactionDBOptions transactionDbOptions,
                                 List<ColumnFamilyDescriptor> columnFamilyDescriptors,
                                 List<ColumnFamilyHandle> columnFamilyHandles) throws RocksDBException {
        return TransactionDB.open(dbOptions, transactionDbOptions, file, columnFamilyDescriptors, columnFamilyHandles);
    }

    @Override
    public void put(byte[] key, byte[] value) throws RocksDBException {
        transactionDB.put(key, value);
    }

    @Override
    public void put(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value) throws RocksDBException {
        transactionDB.put(columnFamilyHandle, key, value);
    }

    @Override
    public void put(byte[] key, byte[] value, WriteOptions writeOptions) throws RocksDBException {
        transactionDB.put(key, value);
    }

    @Override
    public byte[] get(byte[] key) throws RocksDBException {
        return transactionDB.get(key);
    }

    @Override
    public byte[] get(ColumnFamilyHandle columnFamilyHandle, byte[] key) throws RocksDBException {
        return transactionDB.get(columnFamilyHandle, key);
    }

    @Override
    public void remove(byte[] key) throws RocksDBException {
        transactionDB.delete(key);
    }

    @Override
    public void remove(ColumnFamilyHandle columnFamilyHandle, byte[] key) throws RocksDBException {
        transactionDB.delete(columnFamilyHandle, key);
    }

    @Override
    public void remove(byte[] key, WriteOptions writeOptions) throws RocksDBException {
        transactionDB.delete(key);
    }

    @Override
    public RocksIterator getIterator() {
        return transactionDB.newIterator(defaultColumnFamily);
    }

    @Override
    public RocksIterator getIterator(ColumnFamilyHandle indexDB) {
        return transactionDB.newIterator(indexDB);
    }

    @Override
    public void close() {
        transactionDB.close();
    }

}