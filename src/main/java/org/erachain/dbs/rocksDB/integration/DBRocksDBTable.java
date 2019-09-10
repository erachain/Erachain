package org.erachain.dbs.rocksDB.integration;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Arrays;
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
import org.rocksdb.ColumnFamilyHandle;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
public class DBRocksDBTable<K, V> implements org.erachain.dbs.rocksDB.integration.InnerDBTable<K, V> {

    private List<ColumnFamilyHandle> columnFamilyHandles;
    private ColumnFamilyHandle columnFamilyFieldSize;
    //  интерфейс доступа к БД
    private RocksDB db;

    //  Сериализатор ключей
    private Byteable byteableKey;
    //  Сериализатор значений
    private Byteable byteableValue;
    private String NAME_TABLE;
    private RocksDbSettings settings;
    private String root;
    // индексы
    private List<IndexDB> indexes;

    //Для пересчета размеров таблицы
    private ByteableInteger byteableInteger = new ByteableInteger();

    private int counterFlush = 0;

    private final int numberBeforeFlush = 4000;

    public DBRocksDBTable(Byteable byteableKey, Byteable byteableValue, String NAME_TABLE, List<IndexDB> indexes, String root) {
        this(byteableKey, byteableValue, NAME_TABLE, indexes, RocksDbSettings.getDefaultSettings(), root);
    }

    public DBRocksDBTable(Byteable byteableKey, Byteable byteableValue, String NAME_TABLE, List<IndexDB> indexes, RocksDbSettings settings, String root) {
        this.byteableKey = byteableKey;
        this.byteableValue = byteableValue;
        this.NAME_TABLE = NAME_TABLE;
        this.settings = settings;
        this.root = root;
        // Чтобы не было NullPointerException
        if (indexes == null) {
            indexes = new ArrayList<>();
        }
        this.indexes = indexes;
        db = new RocksDB(NAME_TABLE, indexes, settings, root);
        columnFamilyHandles = db.getColumnFamilyHandles();
        for (int i = 0; i < indexes.size(); i++) {
            indexes.get(i).setColumnFamilyHandle(columnFamilyHandles.get(i));
        }
        columnFamilyFieldSize = columnFamilyHandles.get(columnFamilyHandles.size() - 1);
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
        logger.info("put invoked");
        counterFlush++;
        final byte[] keyBytes = byteableKey.toBytesObject(key);
        logger.info("keyBytes.length = " + keyBytes.length);
        byte[] old = db.get(keyBytes);
        if (old == null || old.length == 0) {
            byte[] sizeBytes = db.getDb().getData(columnFamilyFieldSize, new byte[]{0});
            Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
            size++;
            logger.info("put size = " + size);
            db.getDb().putData(columnFamilyFieldSize, new byte[]{0}, byteableInteger.toBytesObject(size));
        }
        byte[] bytesValue = byteableValue.toBytesObject(value);
        db.put(columnFamilyHandles.get(0), keyBytes, bytesValue);
        logger.info("valueBytes.length = " + bytesValue.length);
        for (int i = 1; i < indexes.size(); i++) {
            IndexDB indexDB = indexes.get(i);
            if (indexDB instanceof SimpleIndexDB) {
                logger.info("SimpleIndex");
                SimpleIndexDB simpleIndexDB = (SimpleIndexDB) indexDB;
                Object apply = simpleIndexDB.getBiFunction().apply(key, value);
                byte[] bytes = indexDB.getIndexByteable().toBytes(apply, key);
                if (bytes == null) {
                    continue;
                }
                logger.info("SimpleIndex.bytes.length = " + bytes.length);
                byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                db.put(indexDB.getColumnFamilyHandle(), concatenateBiFunctionKey, keyBytes);
            } else if (indexDB instanceof ArrayIndexDB) {
                logger.info("ArrayIndex");
                ArrayIndexDB arrayIndexDB = (ArrayIndexDB) indexDB;
                BiFunction biFunction = arrayIndexDB.getBiFunction();
                Object[] apply = (Object[]) biFunction.apply(key, value);
                logger.info("ArrayIndex.count.elements = " + apply.length);
                for (Object valueIndex : apply) {
                    byte[] bytes = indexDB.getIndexByteable().toBytes(valueIndex, key);
                    if (bytes == null) {
                        continue;
                    }
                    byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                    logger.info("ArrayIndex.bytes.length = " + bytes.length);
                    db.put(indexDB.getColumnFamilyHandle(), concatenateBiFunctionKey, keyBytes);
                }

            } else if (indexDB instanceof ListIndexDB) {
                logger.info("ListIndex");
                ListIndexDB listIndexDB = (ListIndexDB) indexDB;
                BiFunction biFunction = listIndexDB.getBiFunction();
                List<Object> apply = (List<Object>) biFunction.apply(key, value);
                logger.info("ListIndex.count.elements = " + apply.size());
                for (Object valueIndex : apply) {
                    byte[] bytes = indexDB.getIndexByteable().toBytes(valueIndex, key);
                    if (bytes == null) {
                        continue;
                    }
                    byte[] concatenateBiFunctionKey = Arrays.concatenate(bytes, keyBytes);
                    logger.info("ListIndex.bytes.length = " + bytes.length);
                    db.put(indexDB.getColumnFamilyHandle(), concatenateBiFunctionKey, keyBytes);
                }

            } else {
                throw new UnsupportedTypeIndexException();
            }
        }

        if (counterFlush % numberBeforeFlush == 0) {
            db.flush();
            counterFlush = 0;
        }
    }

    @Override
    public void remove(Object key) {
        final byte[] keyBytes = byteableKey.toBytesObject(key);
        byte[] old = db.get(keyBytes);
        if (old != null && old.length != 0) {
            byte[] sizeBytes = db.getDb().getData(columnFamilyFieldSize, new byte[]{0});
            Integer size = byteableInteger.receiveObjectFromBytes(sizeBytes);
            size--;
            db.getDb().putData(columnFamilyFieldSize, new byte[]{0}, byteableInteger.toBytesObject(size));
        }
        db.remove(columnFamilyHandles.get(0), keyBytes);
        for (int i = 1; i < indexes.size(); i++) {
            IndexDB indexDB = indexes.get(i);
            if (indexDB instanceof SimpleIndexDB) {
                SimpleIndexDB simpleIndexDB = (SimpleIndexDB) indexDB;
                byte[] valueByte = db.get(keyBytes);
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
                byte[] valueByte = db.get(keyBytes);
                if (valueByte == null) {
                    continue;
                }
                Object value = byteableValue.receiveObjectFromBytes(valueByte);
                Object[] apply = (Object[]) biFunction.apply(key, value);
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
                byte[] valueByte = db.get(keyBytes);
                if (valueByte == null) {
                    continue;
                }
                Object value = byteableValue.receiveObjectFromBytes(valueByte);
                List<Object> apply = (List<Object>) biFunction.apply(key, value);
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
    public void clear() {
        db.close();
        FileUtil.recursiveDelete(db.getDb().getDbPath().toString());
        db = new RocksDB(NAME_TABLE, indexes, settings, root);
        columnFamilyHandles = db.getColumnFamilyHandles();
        for (int i = 0; i < indexes.size(); i++) {
            indexes.get(i).setColumnFamilyHandle(columnFamilyHandles.get(i));
        }
        columnFamilyFieldSize = columnFamilyHandles.get(columnFamilyHandles.size() - 1);
    }

    @Override
    public Set<K> keySet() {
        Set<byte[]> set = db.keySet();
        return set.stream().map((bytes -> (K) byteableKey.receiveObjectFromBytes(bytes))).collect(Collectors.toSet());
    }

    @Override
    public Collection<V> values() {
        Set<byte[]> set = db.values();
        return set.stream().map((bytes -> (V) byteableValue.receiveObjectFromBytes(bytes))).collect(Collectors.toList());
    }

    public Set<K> filterAppropriateValuesAsKeys(byte[] filter, IndexDB indexDB) {
        Set<byte[]> set = db.filterAppropriateValuesAsKeys(filter, indexDB);
        return set.stream().map((bytes -> (K) byteableKey.receiveObjectFromBytes(bytes))).collect(Collectors.toSet());
    }

    public Set<K> filterAppropriateKeys(byte[] filter) {
        Set<byte[]> set = db.filterAppropriateValuesAsKeys(filter);
        return set.stream().map((bytes -> (K) byteableKey.receiveObjectFromBytes(bytes))).collect(Collectors.toSet());
    }

    public Set<V> filterAppropriateValues(byte[] filter) {
        Set<byte[]> set = db.filterAppropriateValues(filter);
        return set.stream().map((bytes -> (V) byteableValue.receiveObjectFromBytes(bytes))).collect(Collectors.toSet());
    }

    public IndexDB receiveIndexByName(String name) {
        return db.recieveIndexByName(name);
    }

    public void addIndex(IndexDB indexes) {
        this.indexes.add(indexes);
    }

    public List<V> getLatestValues(long limit) {
        Set<byte[]> latestValues = db.getLatestValues(limit);
        return latestValues.stream().map((bytes) -> (V) byteableValue.receiveObjectFromBytes(bytes)).collect(Collectors.toList());
    }

    public Set<V> getValuesPrevious(K key, long limit) {
        Set<byte[]> valuesPrev = db.getValuesPrevious(byteableKey.toBytesObject(key), limit);
        return valuesPrev.stream().map((bytes) -> (V) byteableValue.receiveObjectFromBytes(bytes)).collect(Collectors.toSet());
    }

    public Set<V> getValuesNext(K key, long limit) {
        Set<byte[]> valuesNext = db.getValuesNext(byteableKey.toBytesObject(key), limit);
        return valuesNext.stream().map((bytes) -> (V) byteableValue.receiveObjectFromBytes(bytes)).collect(Collectors.toSet());
    }

    public void close() {
        db.close();
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

    public Iterator<K> getIndexIterator(boolean descending, IndexDB indexDB) {
        DBIterator iterator = db.indexIterator(descending, indexDB.getColumnFamilyHandle());
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

    public Collection<K> keys(byte[] fromKey, long limit, String indexDBName) {
        Set<byte[]> keysNext = db.getKeysNext(fromKey, limit, receiveIndexByName(indexDBName));
        return keysNext.stream().map((bytes) -> (K) byteableKey.receiveObjectFromBytes(bytes)).collect(Collectors.toSet());
    }
    public Collection<V> values(byte[] fromKey, long limit, String indexDBName) {
        Set<byte[]> keysNext = db.getKeysNext(fromKey, limit, receiveIndexByName(indexDBName));
        return keysNext.stream().map((bytes) -> {
            byte[] value = db.get(bytes);
            if (value != null) {
                return (V) byteableValue.receiveObjectFromBytes(value);
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }


    public Set<K> keys(byte[] fromKey, long limit) {
        Set<byte[]> keysNext = db.getKeysNext(fromKey, limit);
        return keysNext.stream().map((bytes) -> (K) byteableKey.receiveObjectFromBytes(bytes)).collect(Collectors.toSet());
    }
}
