package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.DBMapSuitImpl;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.InnerDBTable;
import org.mapdb.DB;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


@Slf4j
public abstract class DBMapSuit<T, U> extends DBMapSuitImpl<T, U> {

    protected DBASet databaseSet;
    protected DB database;

    protected InnerDBTable<T, U> map;
    protected List<IndexDB> indexes;

    // for DCMapSuit
    public DBMapSuit() {
    }

    public DBMapSuit(DBASet databaseSet, DB database) {

        this.databaseSet = databaseSet;
        // database - is null

        // create INDEXES before
        createIndexes();

        //OPEN MAP
        getMap();

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
        boolean old = contains(key);
        map.put(key, value);
        return old;
    }

    @Override
    public void put(T key, U value) {
        map.put(key, value);
    }

    @Override
    public U remove(T key) {
        U value = null;
        if (map.containsKey(key)) {
            value = map.get(key);
            map.remove(key);
        }
        return value;
    }

    // TODO сделать это у РоксДБ есть
    @Override
    public U removeValue(T key) {
        return remove(key);
    }

    @Override
    public void delete(T key) {
        map.remove(key);
    }


    // TODO сделать это у РоксДБ есть
    @Override
    public void deleteValue(T key) {
        map.remove(key);
    }

    @Override
    public boolean contains(T key) {
        return map.containsKey(key);
    }

    //@Override
    public List<U> getLastValues(int limit) {
        return ((DBRocksDBTable<T, U>) map).getLatestValues(limit);
    }

    //@Override
    //public NavigableSet<Fun.Tuple2<?, T>> getIndex(int index, boolean descending) {
    //    return map.getIndex(index, descending);
    //}

    @Override
    public Iterator<T> getIterator(int index, boolean descending) {
        return map.getIndexIterator(index, descending);
    }

    @Override
    public void reset() {
        map.clear();
    }

    //@Override
    public void close() {
        map.close();
    }

}
