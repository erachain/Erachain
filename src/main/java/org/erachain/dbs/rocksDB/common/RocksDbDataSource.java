package org.erachain.dbs.rocksDB.common;

import org.erachain.dbs.rocksDB.RockStoreIterator;
import org.rocksdb.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RocksDbDataSource {

    byte[] SIZE_BYTE_KEY = new byte[]{0};

    Path getDbPathAndFile();

    boolean isAlive();

    void clearCache();

    void close();

    Set<byte[]> keySet() throws RuntimeException;

    List<byte[]> values() throws RuntimeException;

    RocksIterator getIterator();

    RocksIterator getIterator(ColumnFamilyHandle indexDB);

    Set<byte[]> filterApprropriateKeys(byte[] filter) throws RuntimeException;

    List<byte[]> filterApprropriateValues(byte[] filter) throws RuntimeException;

    Set<byte[]> filterApprropriateValues(byte[] filter, ColumnFamilyHandle indexDB) throws RuntimeException;

    Set<byte[]> filterApprropriateValues(byte[] filter, int indexDB) throws RuntimeException;

    String getDBName();

    void put(byte[] key, byte[] value);

    void put(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value);

    void put(byte[] key, byte[] value, WriteOptions writeOptions);

    boolean contains(byte[] key);

    boolean contains(ColumnFamilyHandle columnFamilyHandle, byte[] key);

    byte[] get(byte[] key);

    byte[] get(final ReadOptions readOptions, final byte[] key);

    byte[] get(ColumnFamilyHandle columnFamilyHandle, byte[] key);

    byte[] get(ColumnFamilyHandle columnFamilyHandle, ReadOptions readOptions, byte[] key);

    void delete(byte[] key);

    void delete(ColumnFamilyHandle columnFamilyHandle, byte[] key);

    void deleteRange(byte[] keyFrom, byte[] keyToExclude);

    void deleteRange(ColumnFamilyHandle columnFamilyHandle, byte[] keyFrom, byte[] keyToExclude);

    void delete(byte[] key, WriteOptions writeOptions);

    void deleteValue(byte[] key);

    void deleteValue(ColumnFamilyHandle columnFamilyHandle, byte[] key);

    void deleteValue(byte[] key, WriteOptions writeOptions);

    RockStoreIterator iterator(boolean descending, boolean isIndex);

    RockStoreIterator indexIterator(boolean descending, ColumnFamilyHandle columnFamilyHandle, boolean isIndex);

    /**
     * @param descending
     * @param indexDB    primary & secondary index No
     * @param isIndex use Values or Keys in as result value in NEXT
     * @return
     */
    RockStoreIterator indexIterator(boolean descending, int indexDB, boolean isIndex);

    RockStoreIterator indexIteratorFilter(boolean descending, byte[] filter, boolean isIndex);

    RockStoreIterator indexIteratorFilter(boolean descending, byte[] start, byte[] stop, boolean isIndex);

    RockStoreIterator indexIteratorFilter(boolean descending, ColumnFamilyHandle columnFamilyHandle, byte[] filter, boolean isIndex);

    RockStoreIterator indexIteratorFilter(boolean descending, ColumnFamilyHandle columnFamilyHandle, byte[] start, byte[] stop, boolean isIndex);

    void write(WriteBatch batch);

    void updateByBatch(Map<byte[], byte[]> rows);

    void updateByBatch(Map<byte[], byte[]> rows, WriteOptions writeOptions);

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

    org.rocksdb.RocksDB getDbCore();

    List<ColumnFamilyHandle> getColumnFamilyHandles();
}
