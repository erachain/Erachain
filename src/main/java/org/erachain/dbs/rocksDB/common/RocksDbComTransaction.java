package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;

/**
 * Транзакция RocksDB. Может существовать отдельно от Базы и их быть несколько у одной базы
 * Работа с записями идет в Транзакцию и изменения сольются в родительскую базу только после commit.
 * По сути это аналог реализации форка от бюазы данных в MapDB
 */
@Slf4j
public class RocksDbComTransaction implements RocksDbCom
{
    public Transaction dbTransaction;
    public TransactionDB parentDB;
    WriteOptions writeOptions;
    ReadOptions readOptions;

    protected final ColumnFamilyHandle defaultColumnFamily;

    public RocksDbComTransaction(TransactionDB parentDB, WriteOptions writeOptions, ReadOptions readOptions) {
        this.parentDB = parentDB;
        defaultColumnFamily = parentDB.getDefaultColumnFamily();

        this.writeOptions = writeOptions;
        this.readOptions = readOptions;
        dbTransaction = parentDB.beginTransaction(writeOptions);
    }

    @Override
    public void put(byte[] key, byte[] value) throws RocksDBException {
        dbTransaction.put(key, value);
    }

    @Override
    public void put(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value) throws RocksDBException {
        dbTransaction.put(columnFamilyHandle, key, value);
    }

    @Override
    public void put(byte[] key, byte[] value, WriteOptions writeOptions) throws RocksDBException {
        dbTransaction.put(key, value);
    }

    @Override
    public byte[] get(byte[] key) throws RocksDBException {
        return dbTransaction.get(readOptions, key);
    }

    @Override
    public byte[] get(ColumnFamilyHandle columnFamilyHandle, byte[] key) throws RocksDBException {
        return dbTransaction.get(columnFamilyHandle, readOptions, key);
    }

    @Override
    public void remove(byte[] key) throws RocksDBException {
        dbTransaction.delete(key);
    }

    @Override
    public void remove(ColumnFamilyHandle columnFamilyHandle, byte[] key) throws RocksDBException {
        dbTransaction.delete(columnFamilyHandle, key);
    }

    @Override
    public void remove(byte[] key, WriteOptions writeOptions) throws RocksDBException {
        dbTransaction.delete(key);
    }

    @Override
    public RocksIterator getIterator() {
        return dbTransaction.getIterator(readOptions, defaultColumnFamily);
    }

    @Override
    public RocksIterator getIterator(ColumnFamilyHandle indexDB) {
        return dbTransaction.getIterator(readOptions, indexDB);
    }

    @Override
    public void close() {
            dbTransaction.close();
            writeOptions.dispose();
    }
}