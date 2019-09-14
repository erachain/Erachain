package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.DBMapImpl;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.InnerDBTable;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;


@Slf4j
public abstract class DBMapSuit<T, U> extends DBMapImpl<T, U> {

    protected InnerDBTable<T, U> map;
    protected List<IndexDB> indexes;

    public DBMapSuit(DBASet databaseSet, DB database) {

        //super(databaseSet, database);
        this.databaseSet = databaseSet;

        // create INDEXES before
        this.createIndexes();

        //OPEN MAP
        getMap();

        if (databaseSet.isWithObserver()) {
            observableData = new HashMap<Integer, Integer>(8, 1);
        }

    }

    public DBMapSuit(DBASet databaseSet) {
        super(databaseSet);
    }

    protected abstract void getMap();

    protected U getDefaultValue() {
        return null;
    }

    protected void createIndexes() {
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
            //logger.error(e.getMessage(), e);
            return getDefaultValue();
        }
    }

    @Override
    public Set<T> getKeys() {
        return map.keySet();
    }

    @Override
    public Collection<U> getValues() {
        return map.values();
    }

    @Override
    public boolean set(T key, U value) {
        U old = get(key);
        map.put(key, value);
        //NOTIFY
        if (observableData != null) {
            if (observableData.containsKey(org.erachain.dbs.DBMap.NOTIFY_ADD)) {
                setChanged();
                notifyObservers(new ObserverMessage((Integer) observableData.get(org.erachain.dbs.DBMap.NOTIFY_ADD), value));
            }
        }
        return old != null;
    }

    public void put(T key, U value) {
        map.put(key, value);
        //NOTIFY
        if (observableData != null) {
            if (observableData.containsKey(org.erachain.dbs.DBMap.NOTIFY_ADD)) {
                setChanged();
                notifyObservers(new ObserverMessage((Integer) observableData.get(org.erachain.dbs.DBMap.NOTIFY_ADD), value));
            }
        }
    }

    @Override
    public U delete(T key) {
        U value = null;
        if (map.containsKey(key)) {
            value = map.get(key);
            map.remove(key);
            //NOTIFY
            if (observableData != null) {
                if (observableData.containsKey(org.erachain.dbs.DBMap.NOTIFY_REMOVE)) {
                    setChanged();
                    notifyObservers(new ObserverMessage((Integer) this.observableData.get(org.erachain.dbs.DBMap.NOTIFY_REMOVE), value));
                }
            }
        }
        return value;
    }

    @Override
    public boolean contains(T key) {
        return map.containsKey(key);
    }

    @Override
    public Map<Integer, Integer> getObservableData() {
        return observableData;
    }

    //@Override
    public List<U> getLastValues(int limit) {
        return ((DBRocksDBTable<T, U>) map).getLatestValues(limit);
    }

    /**
     * @param descending true if need descending sort
     * @return
     */
    public Iterator<T> getIndexIterator(IndexDB indexDB, boolean descending) {
        return ((DBRocksDBTable<T, U>) map).getIndexIterator(descending, indexDB);
    }

    /*
    @Override
    public Iterator<T> getIterator(boolean descending) {
        return map.getIterator(descending);
    }

     */

    @Override
    public void reset() {
        map.clear();
        if (observableData != null) {
            //NOTIFY LIST
            if (observableData.containsKey(org.erachain.dbs.DBMap.NOTIFY_RESET)) {
                setChanged();
                notifyObservers(new ObserverMessage((Integer) observableData.get(org.erachain.dbs.DBMap.NOTIFY_RESET), this));
            }
        }
    }

    //@Override
    public void close() {
        ((DBRocksDBTable<T, U>) map).close();
    }

}
