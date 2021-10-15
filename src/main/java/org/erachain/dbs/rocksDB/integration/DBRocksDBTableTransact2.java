package org.erachain.dbs.rocksDB.integration;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Arrays;
import org.erachain.database.DBASet;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.Transacted;
import org.erachain.dbs.rocksDB.IteratorCloseableImpl;
import org.erachain.dbs.rocksDB.common.*;
import org.erachain.dbs.rocksDB.exceptions.UnsupportedRocksDBOperationException;
import org.erachain.dbs.rocksDB.exceptions.UnsupportedTypeIndexException;
import org.erachain.dbs.rocksDB.indexes.ArrayIndexDB;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ReadOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.erachain.dbs.rocksDB.common.RocksDbDataSourceImpl.SIZE_BYTE_KEY;
import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

/**
 * Данный класс представляет собой основной доступ и функционал к таблице БД RocksDB
 * Тут происходит обработка настроенных вторичных индексов.
 * вызывается из SUIT
 *
 * @param <K>
 * @param <V>
 */
@Slf4j
public class DBRocksDBTableTransact2<K, V> implements InnerDBTable
        <K, V> {

    private boolean logON = false;

    // индексы
    private List<IndexDB> indexes; // если добавим enableSize то надо передлывать обработку вторичных ключей - как в integration.DBRocksDBTable.delete
    private List<ColumnFamilyHandle> columnFamilyHandles;
    private ColumnFamilyHandle columnFamilyFieldSize;

    //  интерфейс доступа к БД
    ///public RocksDbDataSourceImpl dbSource;
    public RocksDbDataSource dbSource;

    //  Сериализатор ключей
    private Byteable byteableKey;
    //  Сериализатор значений
    private Byteable byteableValue;
    private String NAME_TABLE;
    private RocksDbSettings settings;
    protected boolean enableSize;
    private String root;

    //Для пересчета размеров таблицы
    private ByteableInteger byteableInteger = new ByteableInteger();

    /**
     * @param byteableKey
     * @param byteableValue
     * @param NAME_TABLE
     * @param indexes       is null - not use size Counter
     * @param settings
     * @param dbaSet
     * @param enableSize
     */
    public DBRocksDBTableTransact2(Byteable byteableKey, Byteable byteableValue, String NAME_TABLE, List<IndexDB> indexes, RocksDbSettings settings, DBASet dbaSet, boolean enableSize) {
        this.byteableKey = byteableKey;
        this.byteableValue = byteableValue;
        this.NAME_TABLE = NAME_TABLE;
        this.settings = settings;
        this.enableSize = enableSize;
        this.root = (dbaSet == null // in TESTs
                || dbaSet.getFile() == null ? // in Memory or in TESTs
                Settings.getInstance().getDataChainPath()
                : dbaSet.getFile().getParent()) + ROCKS_DB_FOLDER;
        // Чтобы не было NullPointerException
        //if (indexes == null) {
        //    indexes = new ArrayList<>();
        //}
        this.indexes = indexes;
        dbSource = new RocksDbTransactSourceImpl2(this.root, NAME_TABLE, indexes, settings);
        columnFamilyHandles = dbSource.getColumnFamilyHandles();
        if (columnFamilyHandles.size() > 1) {
            // если indexes = null то размер не будем считать
            columnFamilyFieldSize = columnFamilyHandles.get(columnFamilyHandles.size() - 1);
        }
    }

    public DBRocksDBTableTransact2(Byteable byteableKey, Byteable byteableValue, String NAME_TABLE, List<IndexDB> indexes, DBASet dbaSet, boolean enableSize) {
        this(byteableKey, byteableValue, NAME_TABLE, indexes, RocksDbSettings.getDefaultSettings(), dbaSet, enableSize);
    }

    /**
     * for TESTs. new ArrayList<>() - size counter enable
     *
     * @param NAME_TABLE
     * @param enableSize
     */
    public DBRocksDBTableTransact2(String NAME_TABLE, boolean enableSize) {
        this(new ByteableTrivial(), new ByteableTrivial(), NAME_TABLE,
                new ArrayList<>(), RocksDbSettings.getDefaultSettings(), null, enableSize);
    }

    @Override
    public Map<K, V> getMap() {
        throw new UnsupportedRocksDBOperationException();
    }

    @Override
    public void openSource() {
        dbSource = new RocksDbTransactSourceImpl(this.root, NAME_TABLE, indexes, settings, enableSize);
    }

    @Override
    public int size() {
        return dbSource.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return dbSource.get(byteableKey.toBytesObject(key)) != null;
    }

    @Override
    public V get(Object key) {
        byte[] bytes = dbSource.get(byteableKey.toBytesObject(key));
        if (bytes == null) {
            return null;
        }
        return (V) byteableValue.receiveObjectFromBytes(bytes);
    }

    void putIndexes(Object key, V value, byte[] keyBytes, boolean oldGetted, byte[] old) {
        if (!oldGetted) {
            old = dbSource.get(keyBytes);
        }
        if (old != null && old.length > 0) {
            removeIndexes(key, keyBytes, old);
        }

        for (IndexDB indexDB : indexes) {
            if (indexDB instanceof SimpleIndexDB) {
                if (logON) logger.info("SimpleIndex");
                ////// тут получаем ответы от двух функций Индекса - формирования ключа и преобразования его в байты
                //// причем у Глеба тут опять передается ключ первичный - даже для серилиазации результат из вервого вызова
                SimpleIndexDB simpleIndexDB = (SimpleIndexDB) indexDB;
                Object apply = simpleIndexDB.getBiFunction().apply(key, value);
                byte[] bytes = indexDB.getIndexByteable().toBytes(apply);
                if (bytes == null) {
                    continue;
                }
                if (logON) logger.info("SimpleIndex.bytes.length = " + bytes.length);
                byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                dbSource.put(indexDB.getColumnFamilyHandle(), concatenateBiFunctionKey, keyBytes);
            } else if (indexDB instanceof ArrayIndexDB) {
                if (logON) logger.info("ArrayIndex");
                ArrayIndexDB arrayIndexDB = (ArrayIndexDB) indexDB;
                BiFunction biFunction = arrayIndexDB.getBiFunction();
                Object[] apply = (Object[]) biFunction.apply(key, value);
                if (apply == null) {
                    continue;
                }
                if (logON) logger.info("ArrayIndex.count.elements = " + apply.length);
                for (Object valueIndex : apply) {
                    byte[] bytes = indexDB.getIndexByteable().toBytes(valueIndex);
                    if (bytes == null) {
                        continue;
                    }
                    byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                    if (logON) logger.info("ArrayIndex.bytes.length = " + bytes.length);
                    dbSource.put(indexDB.getColumnFamilyHandle(), concatenateBiFunctionKey, keyBytes);
                }

            } else if (indexDB instanceof ListIndexDB) {
                if (logON) logger.info("ListIndex");
                ListIndexDB listIndexDB = (ListIndexDB) indexDB;
                BiFunction biFunction = listIndexDB.getBiFunction();
                List<Object> apply = (List<Object>) biFunction.apply(key, value);
                if (apply == null) {
                    continue;
                }
                if (logON) logger.info("ListIndex.count.elements = " + apply.size());
                for (Object valueIndex : apply) {
                    byte[] bytes = indexDB.getIndexByteable().toBytes(valueIndex);
                    if (bytes == null) {
                        continue;
                    }
                    byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                    if (logON) logger.info("ListIndex.bytes.length = " + bytes.length);
                    dbSource.put(indexDB.getColumnFamilyHandle(), concatenateBiFunctionKey, keyBytes);
                }

            } else {
                throw new UnsupportedTypeIndexException();
            }
        }

    }

    // опции для быстрого чтения
    ReadOptions optionsReadDBcont = new ReadOptions(false, false);
    byte[] sizeBytes = new byte[4];

    @Override
    public boolean set(K key, V value) {
        if (logON) logger.info("put invoked");

        final byte[] keyBytes = byteableKey.toBytesObject(key);
        if (logON) logger.info("keyBytes.length = " + keyBytes.length);
        byte[] old = dbSource.get(keyBytes);
        if (old == null || old.length == 0) {
            if (columnFamilyFieldSize != null) {
                // быстро возьмем
                sizeBytes = dbSource.get(columnFamilyFieldSize, optionsReadDBcont, SIZE_BYTE_KEY);
                ///byte[] sizeBytes = dbSource.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size++;
                if (logON) logger.info("put size = " + size);
                dbSource.put(columnFamilyFieldSize, SIZE_BYTE_KEY, byteableInteger.toBytesObject(size));
            }
        } else {
            // удалим вторичные ключи
            if (indexes != null && !indexes.isEmpty()) {
                removeIndexes(key, keyBytes, old);
            }
        }

        byte[] bytesValue = byteableValue.toBytesObject(value);
        dbSource.put(columnFamilyHandles.get(0), keyBytes, bytesValue);
        if (logON) logger.info("valueBytes.length = " + bytesValue.length);
        if (indexes != null && !indexes.isEmpty()) {
            putIndexes(key, value, keyBytes, true, old);
        }

        return old != null;
    }

    @Override
    public void put(K key, V value) {
        if (logON) logger.info("put invoked");
        //counterFlush++;
        final byte[] keyBytes = byteableKey.toBytesObject(key);
        if (logON) logger.info("keyBytes.length = " + keyBytes.length);
        byte[] old = dbSource.get(keyBytes);
        if (old == null || old.length == 0) {
            if (columnFamilyFieldSize != null) {
                // быстро возьмем
                sizeBytes = dbSource.get(columnFamilyFieldSize, optionsReadDBcont, SIZE_BYTE_KEY);
                ///byte[] sizeBytes = dbSource.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size++;
                if (logON) logger.info("put size = " + size);
                dbSource.put(columnFamilyFieldSize, SIZE_BYTE_KEY, byteableInteger.toBytesObject(size));
            }
        } else {
            // удалим вторичные ключи
            if (indexes != null && !indexes.isEmpty()) {
                removeIndexes(key, keyBytes, old);
            }
        }

        byte[] bytesValue = byteableValue.toBytesObject(value);
        dbSource.put(columnFamilyHandles.get(0), keyBytes, bytesValue);
        if (logON) logger.info("valueBytes.length = " + bytesValue.length);
        if (indexes != null && !indexes.isEmpty()) {
            putIndexes(key, value, keyBytes, true, old);
        }
    }

    void removeIndexes(Object key, byte[] keyBytes, byte[] valueByte) {
        for (IndexDB indexDB : indexes) {
            if (indexDB instanceof SimpleIndexDB) {
                SimpleIndexDB simpleIndexDB = (SimpleIndexDB) indexDB;
                //byte[] valueByte = db.get(keyBytes);
                if (valueByte == null) {
                    continue;
                }
                Object value = byteableValue.receiveObjectFromBytes(valueByte);
                byte[] bytes = indexDB.getIndexByteable().toBytes(simpleIndexDB.getBiFunction().apply(key, value));
                if (bytes == null) {
                    continue;
                }
                byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                dbSource.delete(indexDB.getColumnFamilyHandle(),
                        concatenateBiFunctionKey);
            } else if (indexDB instanceof ArrayIndexDB) {
                ArrayIndexDB arrayIndexDB = (ArrayIndexDB) indexDB;
                BiFunction biFunction = arrayIndexDB.getBiFunction();
                //byte[] valueByte = db.get(keyBytes);
                if (valueByte == null) {
                    continue;
                }
                Object value = byteableValue.receiveObjectFromBytes(valueByte);
                Object[] apply = (Object[]) biFunction.apply(key, value);
                if (apply == null) {
                    continue;
                }
                for (Object valueIndex : apply) {
                    byte[] bytes = indexDB.getIndexByteable().toBytes(valueIndex);
                    if (bytes == null) {
                        continue;
                    }
                    byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                    dbSource.delete(indexDB.getColumnFamilyHandle(),
                            concatenateBiFunctionKey);
                }
            } else if (indexDB instanceof ListIndexDB) {
                ListIndexDB listIndexDB = (ListIndexDB) indexDB;
                BiFunction biFunction = listIndexDB.getBiFunction();
                //byte[] valueByte = db.get(keyBytes);
                if (valueByte == null) {
                    continue;
                }
                Object value = byteableValue.receiveObjectFromBytes(valueByte);
                List<Object> apply = (List<Object>) biFunction.apply(key, value);
                if (apply == null) {
                    continue;
                }
                for (Object valueIndex : apply) {
                    byte[] bytes = indexDB.getIndexByteable().toBytes(valueIndex);
                    if (bytes == null) {
                        continue;
                    }
                    byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                    dbSource.delete(indexDB.getColumnFamilyHandle(),
                            concatenateBiFunctionKey);
                }
            } else {
                throw new UnsupportedTypeIndexException();
            }
        }
    }

    @Override
    public V remove(Object key) {
        final byte[] keyBytes = byteableKey.toBytesObject(key);
        byte[] old = dbSource.get(keyBytes);
        if (old != null && old.length != 0) {
            if (columnFamilyFieldSize != null) {
                // быстро возьмем
                sizeBytes = dbSource.get(columnFamilyFieldSize, optionsReadDBcont, SIZE_BYTE_KEY);
                ///byte[] sizeBytes = dbSource.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size--;
                dbSource.put(columnFamilyFieldSize, SIZE_BYTE_KEY, byteableInteger.toBytesObject(size));
            }
            if (indexes != null && !indexes.isEmpty()) {
                removeIndexes(key, keyBytes, old);
            }
        }
        dbSource.delete(columnFamilyHandles.get(0), keyBytes);
        if (old == null)
            return null;

        return (V) byteableValue.receiveObjectFromBytes(old);
    }

    @Override
    public V removeValue(Object key) {
        final byte[] keyBytes = byteableKey.toBytesObject(key);
        byte[] old = dbSource.get(keyBytes);
        if (old != null && old.length != 0) {
            if (columnFamilyFieldSize != null) {
                // быстро возьмем
                sizeBytes = dbSource.get(columnFamilyFieldSize, optionsReadDBcont, SIZE_BYTE_KEY);
                ///byte[] sizeBytes = dbSource.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size--;
                dbSource.put(columnFamilyFieldSize, SIZE_BYTE_KEY, byteableInteger.toBytesObject(size));
            }
            if (indexes != null && !indexes.isEmpty()) {
                removeIndexes(key, keyBytes, old);
            }
        }
        dbSource.delete(columnFamilyHandles.get(0), keyBytes);
        if (old == null)
            return null;

        return (V) byteableValue.receiveObjectFromBytes(old);
    }

    @Override
    public void delete(Object key) {
        final byte[] keyBytes = byteableKey.toBytesObject(key);
        byte[] old = dbSource.get(keyBytes);
        if (old != null && old.length != 0) {
            if (columnFamilyFieldSize != null) {
                // быстро возьмем
                sizeBytes = dbSource.get(columnFamilyFieldSize, optionsReadDBcont, SIZE_BYTE_KEY);
                ///byte[] sizeBytes = dbSource.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size--;
                dbSource.put(columnFamilyFieldSize, SIZE_BYTE_KEY, byteableInteger.toBytesObject(size));
            }
            if (indexes != null && !indexes.isEmpty()) {
                removeIndexes(key, keyBytes, old);
            }
        }
        dbSource.delete(columnFamilyHandles.get(0), keyBytes);
    }

    @Override
    public void deleteValue(Object key) {
        final byte[] keyBytes = byteableKey.toBytesObject(key);
        byte[] old = dbSource.get(keyBytes);
        if (old != null && old.length != 0) {
            if (columnFamilyFieldSize != null) {
                // быстро возьмем
                sizeBytes = dbSource.get(columnFamilyFieldSize, optionsReadDBcont, SIZE_BYTE_KEY);
                ///byte[] sizeBytes = dbSource.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size--;
                dbSource.put(columnFamilyFieldSize, SIZE_BYTE_KEY, byteableInteger.toBytesObject(size));
            }
            if (indexes != null && !indexes.isEmpty()) {
                removeIndexes(key, keyBytes, old);
            }
        }
        dbSource.delete(columnFamilyHandles.get(0), keyBytes);
    }

    @Override
    public void clear() {
        dbSource.close();
        try {
            Files.walkFileTree(dbSource.getDbPathAndFile(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (IOException e) {
        }
        dbSource = new RocksDbTransactSourceImpl2(root, NAME_TABLE, indexes, settings);
        columnFamilyHandles = dbSource.getColumnFamilyHandles();
        if (columnFamilyHandles.size() > 1) {
            // если indexes = null то размер не будем считать
            columnFamilyFieldSize = columnFamilyHandles.get(columnFamilyHandles.size() - 1);
        }
    }

    @Override
    public Set<K> keySet() {
        Set<byte[]> set = dbSource.keySet();
        return set.stream().map((bytes -> (K) byteableKey.receiveObjectFromBytes(bytes))).collect(Collectors.toSet());
    }

    @Override
    public List<V> values() {
        return dbSource.values().stream().map((bytes -> (V) byteableValue.receiveObjectFromBytes(bytes))).collect(Collectors.toList());
    }

    @Override
    public void clearCache() {
    }

    public List<K> filterAppropriateValuesAsKeys(byte[] filter, int indexDB) {
        return dbSource.filterApprropriateValues(filter, indexDB)
                .stream().map((bytes -> (K) byteableKey.receiveObjectFromBytes(bytes))).collect(Collectors.toList());
    }

    public List<K> filterAppropriateValuesAsKeys(byte[] filter, ColumnFamilyHandle indexDB) {
        return dbSource.filterApprropriateValues(filter, indexDB)
                .stream().map((bytes -> (K) byteableKey.receiveObjectFromBytes(bytes))).collect(Collectors.toList());
    }

    public Set<byte[]> filterAppropriateValuesAsByteKeys(byte[] filter, int indexDB) {
        return dbSource.filterApprropriateValues(filter, indexDB);
    }

    public Set<byte[]> filterAppropriateValuesAsByteKeys(byte[] filter, ColumnFamilyHandle indexDB) {
        return dbSource.filterApprropriateValues(filter, indexDB);
    }

    public Set<K> filterAppropriateKeys(byte[] filter) {
        return dbSource.filterApprropriateValues(filter)
                .stream().map((bytes -> (K) byteableKey.receiveObjectFromBytes(bytes))).collect(Collectors.toSet());
    }

    public List<V> filterAppropriateValues(byte[] filter) {
        return dbSource.filterApprropriateValues(filter)
                .stream().map((bytes -> (V) byteableValue.receiveObjectFromBytes(bytes))).collect(Collectors.toList());
    }

    public IndexDB getIndexByName(String name) {
        return indexes.stream().filter(indexDB -> indexDB.getNameIndex().equals(name)).findFirst().get();
    }

    public IndexDB getIndex(int index) {
        return indexes.get(index);
    }


    public void addIndex(IndexDB indexes) {
        this.indexes.add(indexes);
    }

    public List<V> getLatestValues(long limit) {
        return dbSource.getLatestValues(limit).stream().map((bytes) -> (V) byteableValue.receiveObjectFromBytes(bytes)).collect(Collectors.toList());
    }

    public List<V> getValuesPrevious(K key, long limit) {
        return dbSource.getValuesPrevious(byteableKey.toBytesObject(key), limit)
                .stream().map((bytes) -> (V) byteableValue.receiveObjectFromBytes(bytes)).collect(Collectors.toList());
    }

    public List<V> getValuesNext(K key, long limit) {
        return dbSource.getValuesNext(byteableKey.toBytesObject(key), limit)
                .stream().map((bytes) -> (V) byteableValue.receiveObjectFromBytes(bytes)).collect(Collectors.toList());
    }

    public void close() {
        dbSource.close();
    }

    public void commit() {
        ((Transacted) dbSource).commit();
    }

    public void rollback() {
        ((Transacted) dbSource).rollback();
    }

    @Override
    public IteratorCloseable<K> getIterator(boolean descending, boolean isIndex) {
        DBIterator iterator = dbSource.iterator(descending, isIndex);
        return new IteratorCloseable<K>() {
            /// нужно обязательно освобождать память, см https://github.com/facebook/rocksdb/wiki/RocksJava-Basics
            boolean isClosed;
            @Override
            public void close() {
                try {
                    iterator.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                isClosed = true;
            }

            @Override
            public void finalize() throws Throwable {
                if (!isClosed) {
                    close();
                    logger.warn("FINALIZE used");
                }
                super.finalize();
            }
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public K next() {
                return (K) byteableKey.receiveObjectFromBytes(iterator.next());
            }
        };
    }

    public IteratorCloseable<K> getIndexIterator(ColumnFamilyHandle indexDB, boolean descending, boolean isIndex) {
        DBIterator iterator = dbSource.indexIterator(descending, indexDB, isIndex);
        return new IteratorCloseable<K>() {
            /// нужно обязательно освобождать память, см https://github.com/facebook/rocksdb/wiki/RocksJava-Basics
            boolean isClosed;
            @Override
            public void close() {
                try {
                    iterator.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                isClosed = true;
            }

            @Override
            public void finalize() throws Throwable {
                if (!isClosed) {
                    close();
                    logger.warn("FINALIZE used");
                }
                super.finalize();
            }
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public K next() {
                return (K) byteableKey.receiveObjectFromBytes(iterator.next());
            }
        };
    }

    @Override
    public IteratorCloseable<K> getIndexIteratorFilter(byte[] filter, boolean descending, boolean isIndex) {
        return new IteratorCloseableImpl(dbSource.indexIteratorFilter(descending, filter, isIndex), byteableKey);
    }

    @Override
    public IteratorCloseable<K> getIndexIteratorFilter(byte[] start, byte[] stop, boolean descending, boolean isIndex) {
        return new IteratorCloseableImpl(dbSource.indexIteratorFilter(descending, start, stop, isIndex), byteableKey);
    }

    @Override
    public IteratorCloseable<K> getIndexIteratorFilter(K start, K stop, boolean descending, boolean isIndex) {
        return new IteratorCloseableImpl(dbSource.indexIteratorFilter(descending,
                byteableKey.toBytesObject(start),
                byteableKey.toBytesObject(stop), isIndex), byteableKey);
    }

    @Override
    public IteratorCloseable<K> getIndexIteratorFilter(ColumnFamilyHandle indexDB, byte[] filter, boolean descending, boolean isIndex) {
        return new IteratorCloseableImpl(dbSource.indexIteratorFilter(descending, indexDB, filter, isIndex), byteableKey);
    }

    @Override
    public IteratorCloseable<K> getIndexIteratorFilter(ColumnFamilyHandle indexDB, byte[] start, byte[] stop, boolean descending, boolean isIndex) {
        return new IteratorCloseableImpl(dbSource.indexIteratorFilter(descending, indexDB, start, stop, isIndex), byteableKey);
    }

    public IteratorCloseable<K> getIndexIterator(int index, boolean descending, boolean isIndex) {
        return getIndexIterator(columnFamilyHandles.get(index), descending, isIndex);
    }

    public Set<K> keys(byte[] fromKey, long limit, int indexDB) {
        Set<byte[]> keysNext = dbSource.getKeysNext(fromKey, limit, indexes.get(indexDB).getColumnFamilyHandle());
        return keysNext.stream().map((bytes) -> (K) byteableKey.receiveObjectFromBytes(bytes)).collect(Collectors.toSet());
    }

    public List<V> values(byte[] fromKey, long limit, int indexDB) {
        Set<byte[]> keysNext = dbSource.getKeysNext(fromKey, limit, indexes.get(indexDB).getColumnFamilyHandle());
        return keysNext.stream().map((bytes) -> {
            byte[] value = dbSource.get(bytes);
            if (value != null) {
                return (V) byteableValue.receiveObjectFromBytes(value);
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    public Set<K> keys(byte[] fromKey, long limit) {
        Set<byte[]> keysNext = dbSource.getKeysNext(fromKey, limit);
        return keysNext.stream().map((bytes) -> (K) byteableKey.receiveObjectFromBytes(bytes)).collect(Collectors.toSet());
    }
}
