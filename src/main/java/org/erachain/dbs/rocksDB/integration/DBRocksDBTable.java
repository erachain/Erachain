package org.erachain.dbs.rocksDB.integration;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Arrays;
import org.erachain.database.DBASet;
import org.erachain.dbs.rocksDB.common.DBIterator;
import org.erachain.dbs.rocksDB.common.RocksDB;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.exceptions.UnsupportedRocksDBOperationException;
import org.erachain.dbs.rocksDB.exceptions.UnsupportedTypeIndexException;
import org.erachain.dbs.rocksDB.indexes.ArrayIndexDB;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.utils.FileUtil;
import org.erachain.settings.Settings;
import org.rocksdb.ColumnFamilyHandle;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

/**
 * TODO зачем выделен этот файл, какой функционал он несет, почему нельзя было его встрогить в супер
 *
 * Походу тут происходит обработка настроенных вторичных индексов
 *
 * @param <K>
 * @param <V>
 */
@Slf4j
public class DBRocksDBTable<K, V> implements org.erachain.dbs.rocksDB.integration.InnerDBTable
        <K, V> {

    private boolean logON = false;

    // индексы
    private List<IndexDB> indexes;
    private List<ColumnFamilyHandle> columnFamilyHandles;
    private ColumnFamilyHandle columnFamilyFieldSize;

    //  интерфейс доступа к БД
    public RocksDB db;

    //  Сериализатор ключей
    private Byteable byteableKey;
    //  Сериализатор значений
    private Byteable byteableValue;
    private String NAME_TABLE;
    private RocksDbSettings settings;
    private String root;

    //Для пересчета размеров таблицы
    private ByteableInteger byteableInteger = new ByteableInteger();

    ///private int counterFlush = 0;

    //private final int numberBeforeFlush = 4000;

    public DBRocksDBTable(Byteable byteableKey, Byteable byteableValue, String NAME_TABLE, List<IndexDB> indexes, DBASet dbaSet) {
        this(byteableKey, byteableValue, NAME_TABLE, indexes, RocksDbSettings.getDefaultSettings(), dbaSet);
    }

    /**
     *
     * @param byteableKey
     * @param byteableValue
     * @param NAME_TABLE
     * @param indexes is null - not use size Counter
     * @param settings
     * @param dbaSet
     */
    public DBRocksDBTable(Byteable byteableKey, Byteable byteableValue, String NAME_TABLE, List<IndexDB> indexes, RocksDbSettings settings, DBASet dbaSet) {
        this.byteableKey = byteableKey;
        this.byteableValue = byteableValue;
        this.NAME_TABLE = NAME_TABLE;
        this.settings = settings;
        this.root = (dbaSet == null // in TESTs
                || dbaSet.getFile() == null ? // in Memory or in TESTs
                Settings.getInstance().getDataDir()
                : dbaSet.getFile().getParent()) + ROCKS_DB_FOLDER;
        // Чтобы не было NullPointerException
        //if (indexes == null) {
        //    indexes = new ArrayList<>();
        //}
        this.indexes = indexes;
        db = new RocksDB(NAME_TABLE, indexes, settings, this.root);
        columnFamilyHandles = db.getColumnFamilyHandles();
        if (columnFamilyHandles.size() > 1) {
            // если indexes = null то размер не будем считать
            columnFamilyFieldSize = columnFamilyHandles.get(columnFamilyHandles.size() - 1);
        }
    }

    @Override
    public Map<K, V> getMap() {
        throw new UnsupportedRocksDBOperationException();
    }

    @Override
    public int size() {
        return db.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return db.get(byteableKey.toBytesObject(key)) != null;
    }

    @Override
    public V get(Object key) {
        byte[] bytes = db.get(byteableKey.toBytesObject(key));
        if (bytes == null) {
            return null;
        }
        return (V) byteableValue.receiveObjectFromBytes(bytes);
    }

    @Override
    public void put(K key, V value) {
        if (logON) logger.info("put invoked");
        //counterFlush++;
        final byte[] keyBytes = byteableKey.toBytesObject(key);
        if (logON) logger.info("keyBytes.length = " + keyBytes.length);
        byte[] old = db.get(keyBytes);
        if (old == null || old.length == 0) {
            if (columnFamilyFieldSize != null) {
                byte[] sizeBytes = db.getDb().getData(columnFamilyFieldSize, new byte[]{0});
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size++;
                if (logON) logger.info("put size = " + size);
                db.getDb().putData(columnFamilyFieldSize, new byte[]{0}, byteableInteger.toBytesObject(size));
            }
        } else {
            // удалим вторичные ключи
            if (indexes != null && !indexes.isEmpty()) {
                removeIndexes(key, keyBytes, old);
            }
        }

        byte[] bytesValue = byteableValue.toBytesObject(value);
        db.put(columnFamilyHandles.get(0), keyBytes, bytesValue);
        if (logON) logger.info("valueBytes.length = " + bytesValue.length);
        if (indexes != null && !indexes.isEmpty()) {
            for (IndexDB indexDB : indexes) {
                if (indexDB instanceof SimpleIndexDB) {
                    if (logON) logger.info("SimpleIndex");
                    ////// тут получаем ответы от двух функций Индекса - формирования ключа и преобразования его в байты
                    //// причем у Глеба тут опять передается ключ первичный - даже для серилиазации результат из вервого вызова
                    SimpleIndexDB simpleIndexDB = (SimpleIndexDB) indexDB;
                    Object apply = simpleIndexDB.getBiFunction().apply(key, value);
                    byte[] bytes = indexDB.getIndexByteable().toBytes(apply, key);
                    if (bytes == null) {
                        continue;
                    }
                    if (logON) logger.info("SimpleIndex.bytes.length = " + bytes.length);
                    byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                    db.put(indexDB.getColumnFamilyHandle(), concatenateBiFunctionKey, keyBytes);
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
                        byte[] bytes = indexDB.getIndexByteable().toBytes(valueIndex, key);
                        if (bytes == null) {
                            continue;
                        }
                        byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                        if (logON) logger.info("ArrayIndex.bytes.length = " + bytes.length);
                        db.put(indexDB.getColumnFamilyHandle(), concatenateBiFunctionKey, keyBytes);
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
                        byte[] bytes = indexDB.getIndexByteable().toBytes(valueIndex, key);
                        if (bytes == null) {
                            continue;
                        }
                        byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                        if (logON) logger.info("ListIndex.bytes.length = " + bytes.length);
                        db.put(indexDB.getColumnFamilyHandle(), concatenateBiFunctionKey, keyBytes);
                    }

                } else {
                    throw new UnsupportedTypeIndexException();
                }
            }
        }

        //if (counterFlush % numberBeforeFlush == 0) {
        //    db.flush();
        //    counterFlush = 0;
        //}
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
                byte[] bytes = indexDB.getIndexByteable().toBytes(simpleIndexDB.getBiFunction().apply(key, value), key);
                if (bytes == null) {
                    continue;
                }
                byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                db.remove(indexDB.getColumnFamilyHandle(),
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
                    byte[] bytes = indexDB.getIndexByteable().toBytes(valueIndex, key);
                    if (bytes == null) {
                        continue;
                    }
                    byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                    db.remove(indexDB.getColumnFamilyHandle(),
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
                    byte[] bytes = indexDB.getIndexByteable().toBytes(valueIndex, key);
                    if (bytes == null) {
                        continue;
                    }
                    byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                    db.remove(indexDB.getColumnFamilyHandle(),
                            concatenateBiFunctionKey);
                }
            } else {
                throw new UnsupportedTypeIndexException();
            }


        }

    }
    @Override
    public void remove(Object key) {
        final byte[] keyBytes = byteableKey.toBytesObject(key);
        byte[] old = db.get(keyBytes);
        if (old != null && old.length != 0) {
            if (columnFamilyFieldSize != null) {
                byte[] sizeBytes = db.getDb().getData(columnFamilyFieldSize, new byte[]{0});
                Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
                size--;
                db.getDb().putData(columnFamilyFieldSize, new byte[]{0}, byteableInteger.toBytesObject(size));
            }
            if (indexes != null && !indexes.isEmpty()) {
                removeIndexes(key, keyBytes, old);
            }
        }
        db.remove(columnFamilyHandles.get(0), keyBytes);
    }


    @Override
    public void clear() {
        db.close();
        FileUtil.recursiveDelete(db.getDb().getDbPath().toString());
        db = new RocksDB(NAME_TABLE, indexes, settings, root);
        columnFamilyHandles = db.getColumnFamilyHandles();
        if (columnFamilyHandles.size() > 1) {
            // если indexes = null то размер не будем считать
            columnFamilyFieldSize = columnFamilyHandles.get(columnFamilyHandles.size() - 1);
        }
    }

    @Override
    public Set<K> keySet() {
        Set<byte[]> set = db.keySet();
        return set.stream().map((bytes -> (K) byteableKey.receiveObjectFromBytes(bytes))).collect(Collectors.toSet());
    }

    @Override
    public List<V> values() {
        return db.values().stream().map((bytes -> (V) byteableValue.receiveObjectFromBytes(bytes))).collect(Collectors.toList());
    }

    public List<K> filterAppropriateValuesAsKeys(byte[] filter, int indexDB) {
        return db.filterAppropriateValuesAsKeys(filter, indexDB)
                .stream().map((bytes -> (K) byteableKey.receiveObjectFromBytes(bytes))).collect(Collectors.toList());
    }
    public List<K> filterAppropriateValuesAsKeys(byte[] filter, ColumnFamilyHandle indexDB) {
        return db.filterAppropriateValuesAsKeys(filter, indexDB)
                .stream().map((bytes -> (K) byteableKey.receiveObjectFromBytes(bytes))).collect(Collectors.toList());
    }

    public List<byte[]> filterAppropriateValuesAsByteKeys(byte[] filter, int indexDB) {
        return db.filterAppropriateValuesAsKeys(filter, indexDB);
    }
    public List<byte[]> filterAppropriateValuesAsByteKeys(byte[] filter, ColumnFamilyHandle indexDB) {
        return db.filterAppropriateValuesAsKeys(filter, indexDB);
    }

    public Set<K> filterAppropriateKeys(byte[] filter) {
        return db.filterAppropriateValuesAsKeys(filter)
                .stream().map((bytes -> (K) byteableKey.receiveObjectFromBytes(bytes))).collect(Collectors.toSet());
    }

    public List<V> filterAppropriateValues(byte[] filter) {
        return db.filterAppropriateValues(filter)
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
        return db.getLatestValues(limit).stream().map((bytes) -> (V) byteableValue.receiveObjectFromBytes(bytes)).collect(Collectors.toList());
    }

    public List<V> getValuesPrevious(K key, long limit) {
        return db.getValuesPrevious(byteableKey.toBytesObject(key), limit)
                .stream().map((bytes) -> (V) byteableValue.receiveObjectFromBytes(bytes)).collect(Collectors.toList());
    }

    public List<V> getValuesNext(K key, long limit) {
        return db.getValuesNext(byteableKey.toBytesObject(key), limit)
                .stream().map((bytes) -> (V) byteableValue.receiveObjectFromBytes(bytes)).collect(Collectors.toList());
    }

    public void close() {
        db.close();
    }
    public void commit() {
        db.commit();
    }
    public void rollback() {
        db.rollback();
    }

    @Override
    public Iterator<K> getIterator(boolean descending) {
        DBIterator iterator = db.iterator(descending);
        return new Iterator<K>() {
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

    public Iterator<K> getIndexIterator(ColumnFamilyHandle indexDB, boolean descending) {
        DBIterator iterator = db.indexIterator(descending, indexDB);
        return new Iterator<K>() {
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
    public Iterator<K> getIndexIteratorFilter(byte[] filter, boolean descending) {
        DBIterator iterator = db.indexIteratorFilter(descending, filter);
        return new Iterator<K>() {
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
    public Iterator<K> getIndexIteratorFilter(ColumnFamilyHandle indexDB, byte[] filter, boolean descending) {
        DBIterator iterator = db.indexIteratorFilter(descending, indexDB, filter);
        return new Iterator<K>() {
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

    public Iterator<K> getIndexIterator(int index, boolean descending) {
        return getIndexIterator(columnFamilyHandles.get(index), descending);
    }

    public Set<K> keys(byte[] fromKey, long limit, int indexDB) {
        Set<byte[]> keysNext = db.getKeysNext(fromKey, limit, indexes.get(indexDB));
        return keysNext.stream().map((bytes) -> (K) byteableKey.receiveObjectFromBytes(bytes)).collect(Collectors.toSet());
    }
    public List<V> values(byte[] fromKey, long limit, int indexDB) {
        Set<byte[]> keysNext = db.getKeysNext(fromKey, limit, indexes.get(indexDB));
        return keysNext.stream().map((bytes) -> {
            byte[] value = db.get(bytes);
            if (value != null) {
                return (V) byteableValue.receiveObjectFromBytes(value);
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    public Set<K> keys(byte[] fromKey, long limit) {
        Set<byte[]> keysNext = db.getKeysNext(fromKey, limit);
        return keysNext.stream().map((bytes) -> (K) byteableKey.receiveObjectFromBytes(bytes)).collect(Collectors.toSet());
    }
}
