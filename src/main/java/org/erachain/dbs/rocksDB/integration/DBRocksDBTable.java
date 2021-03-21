package org.erachain.dbs.rocksDB.integration;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Arrays;
import org.erachain.database.DBASet;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.rocksDB.IteratorCloseableImpl;
import org.erachain.dbs.rocksDB.common.RocksDbDataSource;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.exceptions.UnsupportedRocksDBOperationException;
import org.erachain.dbs.rocksDB.exceptions.UnsupportedTypeIndexException;
import org.erachain.dbs.rocksDB.indexes.ArrayIndexDB;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.erachain.dbs.rocksDB.utils.FileUtil;
import org.erachain.settings.Settings;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import java.io.File;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.erachain.dbs.rocksDB.common.RocksDbDataSource.SIZE_BYTE_KEY;
import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;
import static org.rocksdb.RocksDB.loadLibrary;

/**
 * Данный класс представляет собой основной доступ и функционал к таблице БД RocksDB
 * Тут происходит обработка настроенных вторичных индексов.
 * вызывается из SUIT
 *
 * @param <K>
 * @param <V>
 */
@Slf4j
public abstract class DBRocksDBTable<K, V> implements InnerDBTable
        <K, V> {

    protected boolean logON = false;

    //  интерфейс доступа к БД
    ///public RocksDbDataSourceImpl dbSource;
    public RocksDbDataSource dbSource;

    // индексы
    protected List<IndexDB> indexes;
    protected List<ColumnFamilyHandle> columnFamilyHandles;
    protected ColumnFamilyHandle columnFamilyFieldSize;

    //  Сериализатор ключей
    protected Byteable byteableKey;
    //  Сериализатор значений
    protected Byteable byteableValue;
    protected String NAME_TABLE;
    protected RocksDbSettings settings;
    protected boolean enableSize;
    protected String root;

    //Для пересчета размеров таблицы
    protected ByteableInteger byteableInteger = new ByteableInteger();

    static Random randFork = new Random();

    public WriteOptions writeOptions;

    static {
        try {
            logger.info("load libraries");
            loadLibrary(new ArrayList<String>() {{
                add(".");
            }});
            logger.info("loaded success");
        } catch (Throwable throwable) {
            logger.error(throwable.getMessage(), throwable);
        }
    }

    /**
     *  @param byteableKey
     * @param byteableValue
     * @param NAME_TABLE
     * @param indexes is null - not use size Counter
     * @param settings
     * @param dbaSet
     * @param enableSize
     */
    public DBRocksDBTable(Byteable byteableKey, Byteable byteableValue, String NAME_TABLE, List<IndexDB> indexes,
                          RocksDbSettings settings, WriteOptions writeOptions, DBASet dbaSet, boolean enableSize) {
        this.byteableKey = byteableKey;
        this.byteableValue = byteableValue;
        this.NAME_TABLE = NAME_TABLE;
        this.settings = settings;
        this.enableSize = enableSize;
        this.writeOptions = writeOptions;
        this.root = (dbaSet == null // in TESTs
                || dbaSet.getFile() == null ? // in Memory or in TESTs
                Settings.getInstance().getDataChainPath()
                : dbaSet.getFile().getParent()) + ROCKS_DB_FOLDER;
        this.indexes = indexes;
    }

    /**
     * Use random name for TMP Fork
     * @param byteableKey
     * @param byteableValue
     * @param indexes
     * @param settings
     * @param writeOptions
     * @param enableSize
     */
    public DBRocksDBTable(Byteable byteableKey, Byteable byteableValue, List<IndexDB> indexes,
                          RocksDbSettings settings, WriteOptions writeOptions, boolean enableSize) {
        this.byteableKey = byteableKey;
        this.byteableValue = byteableValue;
        this.settings = settings;
        this.enableSize = enableSize;
        this.writeOptions = writeOptions;
        this.root = Settings.getInstance().getDataChainPath();
        this.indexes = indexes;

        File dbFile;
        do {
            dbFile = new File(root, "fork_RDB" + randFork.nextInt());
        } while (dbFile.exists());

        this.NAME_TABLE = dbFile.getName();

    }

    public DBRocksDBTable(Byteable byteableKey, Byteable byteableValue, String NAME_TABLE, List<IndexDB> indexes, DBASet dbaSet, boolean enableSize) {
        this(byteableKey, byteableValue, NAME_TABLE, indexes, RocksDbSettings.getDefaultSettings(),
                new WriteOptions().setSync(true).setDisableWAL(false), dbaSet, enableSize);
    }

    /**
     * for TESTs. new ArrayList<>() - size counter enable
     * @param NAME_TABLE
     * @param enableSize
     */
    public DBRocksDBTable(String NAME_TABLE, boolean enableSize) {
        this(new ByteableTrivial(), new ByteableTrivial(), NAME_TABLE,
                new ArrayList<>(), RocksDbSettings.getDefaultSettings(),
                new WriteOptions().setSync(true).setDisableWAL(false), null, enableSize);
    }

    protected void afterOpen() {
        columnFamilyHandles = dbSource.getColumnFamilyHandles();
        if (enableSize) {
            columnFamilyFieldSize = columnFamilyHandles.get(columnFamilyHandles.size() - 1);
        }
    }

    @Override
    public Map<K, V> getMap() {
        throw new UnsupportedRocksDBOperationException();
    }

    @Override
    public int size() {
        return dbSource.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return dbSource.contains(byteableKey.toBytesObject(key));
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
        //counterFlush++;
        final byte[] keyBytes = byteableKey.toBytesObject(key);
        boolean hasIndexes = indexes != null && !indexes.isEmpty();
        byte[] old;
        boolean oldExist;

        if (logON) logger.info("keyBytes.length = " + keyBytes.length);

        if (hasIndexes) {
            oldExist = (old = dbSource.get(keyBytes)) != null;

            // Значение старое было значит удалим вторичные ключи
            putIndexes(key, value, keyBytes, true, old);

        } else {
            oldExist = dbSource.contains(keyBytes);
        }

        if (enableSize) {

            if (!oldExist) {

                sizeBytes = dbSource.get(columnFamilyFieldSize, optionsReadDBcont, SIZE_BYTE_KEY);
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size++;
                if (logON) logger.info("put size = " + size);

                dbSource.put(columnFamilyFieldSize, SIZE_BYTE_KEY, byteableInteger.toBytesObject(size));
            }
        }

        byte[] bytesValue = byteableValue.toBytesObject(value);
        dbSource.put(keyBytes, bytesValue);

        if (logON) logger.info("valueBytes.length = " + bytesValue.length);

        return oldExist;
    }

    @Override
    public void put(K key, V value) {
        if (logON) logger.info("put invoked");
        //counterFlush++;
        final byte[] keyBytes = byteableKey.toBytesObject(key);
        boolean hasIndexes = indexes != null && !indexes.isEmpty();
        byte[] old;
        boolean oldExist;

        if (hasIndexes) {
            // Значение старое было значит удалим вторичные ключи
            oldExist = (old = dbSource.get(keyBytes)) != null;
            putIndexes(key, value, keyBytes, true, old);
        } else {
            oldExist = false;
        }

        if (logON) logger.info("keyBytes.length = " + keyBytes.length);

        if (enableSize) {
            if (!hasIndexes) {
                oldExist = dbSource.contains(keyBytes);
            }

            if (!oldExist) {
                // быстро возьмем
                sizeBytes = dbSource.get(columnFamilyFieldSize, optionsReadDBcont, SIZE_BYTE_KEY);
                ///byte[] sizeBytes = dbSource.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size++;
                if (logON) logger.info("put size = " + size);

                dbSource.put(columnFamilyFieldSize, SIZE_BYTE_KEY, byteableInteger.toBytesObject(size));
            }
        }

        byte[] bytesValue = byteableValue.toBytesObject(value);
        dbSource.put(keyBytes, bytesValue);
        if (logON) logger.info("valueBytes.length = " + bytesValue.length);
    }

    void removeIndexes(Object key, byte[] keyBytes, byte[] valueByte) {
        Object value = byteableValue.receiveObjectFromBytes(valueByte);
        for (IndexDB indexDB : indexes) {
            if (indexDB instanceof SimpleIndexDB) {
                SimpleIndexDB simpleIndexDB = (SimpleIndexDB) indexDB;

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

        // Есть вторичные ключи и значение старое было
        if (indexes != null && !indexes.isEmpty()) {
            if (old != null && old.length > 0) {
                removeIndexes(key, keyBytes, old);
            }
        }

        if (enableSize) {
            if (old != null && old.length > 0) {
                // UPDATE SIZE
                // быстро возьмем
                sizeBytes = dbSource.get(columnFamilyFieldSize, optionsReadDBcont, SIZE_BYTE_KEY);
                ///byte[] sizeBytes = dbSource.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size--;
                dbSource.put(columnFamilyFieldSize, SIZE_BYTE_KEY, byteableInteger.toBytesObject(size));
            }
        }
        dbSource.delete(keyBytes);
        if (old == null)
            return null;

        return (V) byteableValue.receiveObjectFromBytes(old);
    }

    // TODO переделать на REMOVE так как тут берется Значение
    @Override
    public V removeValue(Object key) {
        final byte[] keyBytes = byteableKey.toBytesObject(key);

        byte[] old = dbSource.get(keyBytes);

        // Есть вторичные ключи и значение старое было
        if (indexes != null && !indexes.isEmpty()) {
            if (old != null && old.length > 0) {
                removeIndexes(key, keyBytes, old);
            }
        }

        if (enableSize) {
            if (old != null && old.length > 0) {
                // UPDATE SIZE
                // быстро возьмем
                sizeBytes = dbSource.get(columnFamilyFieldSize, optionsReadDBcont, SIZE_BYTE_KEY);
                ///byte[] sizeBytes = dbSource.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size--;
                dbSource.put(columnFamilyFieldSize, SIZE_BYTE_KEY, byteableInteger.toBytesObject(size));
            }
        }
        dbSource.delete(keyBytes);
        if (old == null)
            return null;

        return (V) byteableValue.receiveObjectFromBytes(old);
    }

    @Override
    public void delete(Object key) {
        final byte[] keyBytes = byteableKey.toBytesObject(key);

        byte[] old = null;
        boolean oldGetted = false;

        // Есть вторичные ключи и значение старое было
        if (indexes != null && !indexes.isEmpty()) {
            old = dbSource.get(keyBytes);
            oldGetted = true;
            if (old != null && old.length > 0) {
                removeIndexes(key, keyBytes, old);
            }
        }

        if (enableSize) {
            if (!oldGetted) {
                old = dbSource.get(keyBytes);
            }
            if (old != null && old.length > 0) {
                // UPDATE SIZE
                // быстро возьмем
                sizeBytes = dbSource.get(columnFamilyFieldSize, optionsReadDBcont, SIZE_BYTE_KEY);
                ///byte[] sizeBytes = dbSource.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size--;
                dbSource.put(columnFamilyFieldSize, SIZE_BYTE_KEY, byteableInteger.toBytesObject(size));
            }
        }
        dbSource.delete(keyBytes);
    }

    @Override
    public void deleteValue(Object key) {
        final byte[] keyBytes = byteableKey.toBytesObject(key);

        byte[] old = null;
        boolean oldGetted = false;

        // Есть вторичные ключи и значение старое было
        if (indexes != null && !indexes.isEmpty()) {
            old = dbSource.get(keyBytes);
            oldGetted = true;
            if (old != null && old.length > 0) {
                removeIndexes(key, keyBytes, old);
            }
        }

        if (enableSize) {
            if (!oldGetted) {
                old = dbSource.get(keyBytes);
            }
            if (old != null && old.length > 0) {
                // UPDATE SIZE
                // быстро возьмем
                sizeBytes = dbSource.get(columnFamilyFieldSize, optionsReadDBcont, SIZE_BYTE_KEY);
                ///byte[] sizeBytes = dbSource.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size--;
                dbSource.put(columnFamilyFieldSize, SIZE_BYTE_KEY, byteableInteger.toBytesObject(size));
            }
        }
        dbSource.delete(keyBytes);
    }

    @Override
    public void clear() {
        dbSource.close();
        FileUtil.recursiveDelete(dbSource.getDbPathAndFile().toString());
        openSource();
        columnFamilyHandles = dbSource.getColumnFamilyHandles();
        if (enableSize) {
            columnFamilyFieldSize = columnFamilyHandles.get(columnFamilyHandles.size() - 1);
        }
    }

    @Override
    public void clearCache() {
        dbSource.clearCache();
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

    public Set<K> filterAppropriateValuesAsKeys(byte[] filter, int indexDB) {
        return dbSource.filterApprropriateValues(filter, indexDB)
                .stream().map((bytes -> (K) byteableKey.receiveObjectFromBytes(bytes))).collect(Collectors.toSet());
    }

    public Set<K> filterAppropriateValuesAsKeys(byte[] filter, ColumnFamilyHandle indexDB) {
        return dbSource.filterApprropriateValues(filter, indexDB)
                .stream().map((bytes -> (K) byteableKey.receiveObjectFromBytes(bytes))).collect(Collectors.toSet());
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

    @Override
    public void close() {
        dbSource.close();
    }

    @Override
    public IteratorCloseable<K> getIterator(boolean descending, boolean isIndex) {
        return new IteratorCloseableImpl(dbSource.iterator(descending, isIndex), byteableKey);
    }

    public IteratorCloseable<K> getIndexIterator(ColumnFamilyHandle indexDB, boolean descending, boolean isIndex) {
        return new IteratorCloseableImpl(dbSource.indexIterator(descending, indexDB, isIndex), byteableKey);
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
