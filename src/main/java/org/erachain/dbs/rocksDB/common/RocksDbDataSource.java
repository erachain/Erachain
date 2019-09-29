package org.erachain.dbs.rocksDB.common;

import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RocksDbDataSource {
    Path getDbPath();

    boolean isAlive();

    void close();

    void commit();

    void rollback();

    Set<byte[]> keySet() throws RuntimeException;

    List<byte[]> values() throws RuntimeException;

    RocksIterator getIterator();

    RocksIterator getIterator(ColumnFamilyHandle indexDB);

    Set<byte[]> filterApprropriateKeys(byte[] filter) throws RuntimeException;

    List<byte[]> filterApprropriateValues(byte[] filter) throws RuntimeException;

    List<byte[]> filterApprropriateValues(byte[] filter, ColumnFamilyHandle indexDB) throws RuntimeException;

    List<byte[]> filterApprropriateValues(byte[] filter, int indexDB) throws RuntimeException;

    String getDBName();

    void initDB();

    void put(byte[] key, byte[] value);

    void put(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value);

    void put(byte[] key, byte[] value, WriteOptionsWrapper optionsWrapper);

    byte[] get(byte[] key);

    byte[] get(ColumnFamilyHandle columnFamilyHandle, byte[] key);

    void remove(byte[] key);

    void remove(ColumnFamilyHandle columnFamilyHandle, byte[] key);

    void remove(byte[] key, WriteOptionsWrapper optionsWrapper);

    RockStoreIterator iterator(boolean descending);

    RockStoreIterator indexIterator(boolean descending, ColumnFamilyHandle columnFamilyHandle);

    RockStoreIteratorFilter indexIteratorFilter(boolean descending, byte[] filter);

    RockStoreIteratorFilter indexIteratorFilter(boolean descending, ColumnFamilyHandle columnFamilyHandle, byte[] filter);

    RockStoreIterator indexIterator(boolean descending, int indexDB);

    void updateByBatch(Map<byte[], byte[]> rows);

    void updateByBatch(Map<byte[], byte[]> rows, WriteOptionsWrapper optionsWrapper);

    Map<byte[], byte[]> getNext(byte[] key, long limit);

    List<byte[]> getLatestValues(long limit);

    List<byte[]> getValuesPrevious(byte[] key, long limit);

    List<byte[]> getValuesNext(byte[] key, long limit);

    Set<byte[]> getKeysNext(byte[] key, long limit);

    Set<byte[]> getKeysNext(byte[] key, long limit, ColumnFamilyHandle columnFamilyHandle);

    Map<byte[], byte[]> getPrevious(byte[] key, long limit, int precision);

    void backup(String dir) throws RocksDBException;

    boolean deleteDbBakPath(String dir);

    int size();

    boolean isEmpty();

    void flush(Map<byte[], byte[]> rows);

    void flush() throws RocksDBException;

    org.rocksdb.TransactionDB getDbCore();

    List<ColumnFamilyHandle> getColumnFamilyHandles();
}
