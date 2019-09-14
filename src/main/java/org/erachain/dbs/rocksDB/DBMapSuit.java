package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.InnerDBTable;
import org.mapdb.DB;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


@Slf4j
public abstract class DBMapSuit<T, U> implements org.erachain.dbs.DBMapSuit<T, U> {

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

    protected abstract void getMap();

    protected void createIndexes() {
    }

    protected abstract U getDefaultValue();

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
    public U set(T key, U value) {
        U old = get(key);
        map.put(key, value);
        return old;
    }

    @Override
    public void put(T key, U value) {
        map.put(key, value);
    }

    @Override
    public void delete(T key) {
        map.remove(key);
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

    @Override
    public boolean contains(T key) {
        return map.containsKey(key);
    }

    //@Override
    public List<U> getLastValues(int limit) {
        return ((DBRocksDBTable<T, U>) map).getLatestValues(limit);
    }

    @Override
    public Iterator<T> getIterator(int index, boolean descending) {
        return map.getIndexIterator(descending, index);
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
