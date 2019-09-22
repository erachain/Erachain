package org.erachain.dbs.rocksDB.common;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDBException;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

/** Обертка база данных как файл с обработкой закрыть открыть сохранить.
 *
 *
 */
@Slf4j
public class RocksDB implements DB<byte[], byte[]>, Flusher
{

    @Getter
    @Setter
    // TODO ??
    private boolean dbSync;

    @Getter
    public RocksDbDataSourceImpl db;

    private WriteOptionsWrapper optionsWrapper;

    public RocksDB(String name) {
        db = new RocksDbDataSourceImpl(
                Paths.get(ROCKS_DB_FOLDER).toString(), name);
        optionsWrapper = WriteOptionsWrapper.getInstance().sync(dbSync);
        db.initDB(new ArrayList<>());
    }

    public RocksDB(String name, List<IndexDB> indexes, RocksDbSettings settings, String root) {
        db = new RocksDbDataSourceImpl(
                Paths.get(root).toString(), name);
        optionsWrapper = WriteOptionsWrapper.getInstance().sync(dbSync);
        db.initDB(settings, indexes);
    }

    @Override
    public byte[] get(byte[] key) {
        return db.getData(key);
    }

    public byte[] get(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        return db.getData(columnFamilyHandle, key);
    }

    @Override
    public void put(byte[] key, byte[] value) {
        db.putData(key, value);
    }

    public void put(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value) {
        db.putData(columnFamilyHandle, key, value);
    }

    @Override
    public int size() {
        return db.size();
    }

    @Override
    public boolean isEmpty() {
        return db.size() == 0;
    }

    @Override
    public void remove(byte[] key) {
        db.deleteData(key);
    }

    public void remove(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        db.deleteData(columnFamilyHandle, key);
    }

    @Override
    public Set<byte[]> keySet() throws RuntimeException {
        return db.allKeys();
    }

    public List<byte[]> values() throws RuntimeException {
        return db.allValues();
    }

    public DBIterator iterator(boolean descending) {
        return db.iterator(descending);
    }

    public DBIterator indexIterator(boolean descending, int index) {
        return db.indexIterator(descending, index);
    }

    public DBIterator indexIterator(boolean descending, ColumnFamilyHandle index) {
        return db.indexIterator(descending, index);
    }


    @Override
    public void flush(Map<byte[], byte[]> rows) {
        db.updateByBatch(rows, optionsWrapper);

    }

    @Override
    public void flush() {
        try {
            db.flush();
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        db.closeDB();
    }

    @Override
    public void reset() {
    }

    @Override
    public List<byte[]> filterAppropriateValuesAsKeys(byte[] filter, int indexDB) {
        return db.filterApprropriateValues(filter, indexDB);
    }

    @Override
    public List<byte[]> filterAppropriateValuesAsKeys(byte[] filter, ColumnFamilyHandle indexDB) {
        return db.filterApprropriateValues(filter, indexDB);
    }

    @Override
    public List<byte[]> filterAppropriateValuesAsKeys(byte[] filter) {
        return db.filterApprropriateValues(filter);
    }

    public List<byte[]> filterAppropriateValues(byte[] filter) {
        return db.filterApprropriateValues(filter);
    }

    public List<byte[]> getLatestValues(long limit) {
        return db.getLatestValues(limit);
    }

    public List<byte[]> getValuesPrevious(byte[] key, long limit) {
        return db.getValuesPrevious(key, limit);
    }

    public List<byte[]> getValuesNext(byte[] key, long limit) {
        return db.getValuesNext(key, limit);
    }

    public Set<byte[]> getKeysNext(byte[] key, long limit) {
        return db.getKeysNext(key, limit);
    }

    public Set<byte[]> getKeysNext(byte[] key, long limit, IndexDB index) {
        return db.getKeysNext(key, limit, index.getColumnFamilyHandle());
    }

    public List<ColumnFamilyHandle> getColumnFamilyHandles() {
        return db.getColumnFamilyHandles();
    }

    public ColumnFamilyHandle getColumnFamilyHandle(int index) {
        return db.getColumnFamilyHandles().get(index);
    }

}