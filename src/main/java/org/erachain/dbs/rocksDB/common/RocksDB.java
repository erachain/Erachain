package org.erachain.dbs.rocksDB.common;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDBException;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

@Slf4j
public class RocksDB implements DB<byte[], byte[]>, Flusher {

    private ColumnFamilyHandle columnFamilyFieldSize;
    private ByteableInteger byteableInteger = new ByteableInteger();
    @Getter
    @Setter
    private boolean dbSync;

    @Getter
    private RocksDbDataSourceImpl db;

    private List<IndexDB> indexes;

    @Getter
    private List<ColumnFamilyHandle> columnFamilyHandles;

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
        this.indexes = indexes;
        columnFamilyHandles = db.initDB(settings, indexes);
        db.initSizeField();
        columnFamilyFieldSize = columnFamilyHandles.get(columnFamilyHandles.size() - 1);
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
        byte[] sizeBytes = db.getData(columnFamilyFieldSize, new byte[]{0});
        return byteableInteger.receiveObjectFromBytes(sizeBytes);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
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

    public Set<byte[]> values() throws RuntimeException {
        return db.allValues();
    }

    public DBIterator iterator(boolean descending) {
        return db.iterator(descending);
    }

    public DBIterator indexIterator(boolean descending, ColumnFamilyHandle columnFamilyHandle) {
        return db.indexIterator(descending, columnFamilyHandle);
    }

    public DBIterator indexIterator(boolean descending, int index) {
        return db.indexIterator(descending, columnFamilyHandles.get(index));
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
    public Set<byte[]> filterAppropriateValuesAsKeys(byte[] filter, IndexDB indexDB) {
        return db.filterApprropriateValues(filter, indexDB);
    }

    @Override
    public Set<byte[]> filterAppropriateValuesAsKeys(byte[] filter) {
        return db.filterApprropriateKeys(filter);
    }

    public Set<byte[]> filterAppropriateValues(byte[] filter) {
        return db.filterApprropriateValues(filter);
    }

    @Override
    public IndexDB recieveIndexByName(String name) {
        return indexes.stream().filter(indexDB ->
                indexDB.getNameIndex().equals(name)).findFirst().get();
    }

    public Set<byte[]> getLatestValues(long limit) {
        return db.getLatestValues(limit);
    }

    public Set<byte[]> getValuesPrevious(byte[] key, long limit) {
        return db.getValuesPrevious(key, limit);
    }

    public Set<byte[]> getValuesNext(byte[] key, long limit) {
        return db.getValuesNext(key, limit);
    }

    public Set<byte[]> getKeysNext(byte[] key, long limit) {
        return db.getKeysNext(key, limit);
    }

    public Set<byte[]> getKeysNext(byte[] key, long limit, IndexDB index) {
        return db.getKeysNext(key, limit, index.getColumnFamilyHandle());
    }
}