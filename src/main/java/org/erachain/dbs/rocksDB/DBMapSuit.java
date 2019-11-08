package org.erachain.dbs.rocksDB;

import org.erachain.database.DBASet;
import org.erachain.dbs.DBMapSuitImpl;
import org.erachain.dbs.IMap;
import org.erachain.dbs.Transacted;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.mapdb.DB;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Оболочка для Карты от конкретной СУБД чтобы эту оболочку вставлять в Таблицу, которая запускает события для ГУИ.
 * Для каждой СУБД свой порядок обработки команд
 * @param <T>
 * @param <U>
 */

public abstract class DBMapSuit<T, U> extends DBMapSuitImpl<T, U> {

    protected Logger logger;
    protected DBASet databaseSet;
    protected DB database;

    public DBRocksDBTable<T, U> map;
    protected List<IndexDB> indexes;

    // for DBMapSuitFork
    public DBMapSuit() {
    }

    public DBMapSuit(DBASet databaseSet, DB database, Logger logger, U defaultValue, boolean sizeEnable) {

        this.databaseSet = databaseSet;
        // database - is null
        this.database = database;
        this.logger = logger;
        this.defaultValue = defaultValue;
        this.sizeEnable = sizeEnable;

        // create INDEXES before
        createIndexes();

        //OPEN MAP
        openMap();

        logger.info("USED");
    }

    public DBMapSuit(DBASet databaseSet, DB database, Logger logger, boolean sizeEnable) {
        this(databaseSet, database, logger, null, sizeEnable);
    }

    @Override
    public IMap getSource() {
        return (IMap) map;
    }

    public IndexDB getIndexByName(String name) {
        return indexes.stream().filter(indexDB -> indexDB.getNameIndex().equals(name)).findFirst().get();
    }

    /**
     * @param index only Secondary indexes [0...]
     * @return
     */
    public IndexDB getIndex(int index) {
        return indexes.get(index);
    }

    public void addIndex(IndexDB indexes) {
        this.indexes.add(indexes);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public U get(T key) {
        try {
            if (map.containsKey(key)) {
                return map.get(key);
            }
            return getDefaultValue();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getDefaultValue();
        }
    }

    @Override
    public Set<T> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<U> values() {
        return map.values();
    }

    @Override
    public boolean set(T key, U value) {
        return map.set(key, value);
    }

    @Override
    public void put(T key, U value) {
        map.put(key, value);
    }

    @Override
    public U remove(T key) {
        return map.remove(key);
    }

    // TODO сделать это у РоксДБ есть
    @Override
    public U removeValue(T key) {
        return map.removeValue(key);
    }

    @Override
    public void delete(T key) {
        map.delete(key);
    }

    // TODO сделать это у РоксДБ есть
    @Override
    public void deleteValue(T key) {
        map.deleteValue(key);
    }

    @Override
    public boolean contains(T key) {
        return map.containsKey(key);
    }

    @Override
    public Iterator<T> getIterator(int index, boolean descending) {
        if (index == 0) {
            return map.getIterator(descending, true);
        }

        // там индексы без учета первичного
        return map.getIndexIterator(index, descending, true);
    }

    @Override
    public Iterator<T> getIterator() {
        return map.getIterator(false, true);
    }

    @Override
    public void close() {
        map.close();
        //logger.info("closed");
    }

    @Override
    public boolean isClosed() {
        return map.dbSource.isAlive();
    }

    @Override
    public void commit() {
        ((Transacted) map).commit();
    }

    @Override
    public void rollback() {
        ((Transacted) map).rollback();
    }

    @Override
    public void clearCache() {
        map.clearCache();
    }

    @Override
    public void clear() {
        map.clear();
    }

}
