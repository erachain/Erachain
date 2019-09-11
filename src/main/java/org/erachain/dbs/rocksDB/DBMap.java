package org.erachain.dbs.rocksDB;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.database.IDB;
import org.erachain.database.SortableList;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.InnerDBTable;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.util.*;


@Slf4j
public abstract class DBMap<T, U> extends org.erachain.database.DBMap<T, U> {

    protected InnerDBTable<T, U> tableDB;
    protected List<IndexDB> indexes;

    public DBMap(DBASet databaseSet, DB database) {

        //super(databaseSet, database);
        this.databaseSet = databaseSet;

        // create INDEXES before
        this.createIndexes(database);

        //OPEN MAP
        getMap(database);

        if (databaseSet.isWithObserver()) {
            observableData = new HashMap<Integer, Integer>(8, 1);
        }

    }

    public DBMap(DBASet databaseSet) {
        super(databaseSet);
    }

    protected abstract void getMap(DB database);

    protected abstract void getMemoryMap();

    protected U getDefaultValue() {
        return null;
    }

    protected void createIndexes() {
    }

    @Override
    public int size() {
        return tableDB.size();
    }

    @Override
    public U get(T key) {
        try {
            if (tableDB.containsKey(key)) {
                return tableDB.get(key);
            }
            return getDefaultValue();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getDefaultValue();
        }
    }

    @Override
    public Set<T> getKeys() {
        return tableDB.keySet();
    }

    @Override
    public Collection<U> getValues() {
        return tableDB.values();
    }

    @Override
    public boolean set(T key, U value) {
        U old = get(key);
        tableDB.put(key, value);
        //NOTIFY
        if (observableData != null) {
            if (observableData.containsKey(org.erachain.database.DBMap.NOTIFY_ADD)) {
                setChanged();
                notifyObservers(new ObserverMessage((Integer) observableData.get(org.erachain.database.DBMap.NOTIFY_ADD), value));
            }
        }
        return old != null;
    }

    public void put(T key, U value) {
        tableDB.put(key, value);
        //NOTIFY
        if (observableData != null) {
            if (observableData.containsKey(org.erachain.database.DBMap.NOTIFY_ADD)) {
                setChanged();
                notifyObservers(new ObserverMessage((Integer) observableData.get(org.erachain.database.DBMap.NOTIFY_ADD), value));
            }
        }
    }

    @Override
    public U delete(T key) {
        U value = null;
        if (tableDB.containsKey(key)) {
            value = tableDB.get(key);
            tableDB.remove(key);
            //NOTIFY
            if (observableData != null) {
                if (observableData.containsKey(org.erachain.database.DBMap.NOTIFY_REMOVE)) {
                    setChanged();
                    notifyObservers(new ObserverMessage((Integer) this.observableData.get(org.erachain.database.DBMap.NOTIFY_REMOVE), value));
                }
            }
        }
        return value;
    }

    @Override
    public boolean contains(T key) {
        return tableDB.containsKey(key);
    }

    @Override
    public Map<Integer, Integer> getObservableData() {
        return observableData;
    }

    //@Override
    public List<U> getLastValues(int limit) {
        return ((DBRocksDBTable<T, U>) tableDB).getLatestValues(limit);
    }

    @Override
    public void reset() {
        tableDB.clear();
        if (observableData != null) {
            //NOTIFY LIST
            if (observableData.containsKey(org.erachain.database.DBMap.NOTIFY_RESET)) {
                setChanged();
                notifyObservers(new ObserverMessage((Integer) observableData.get(org.erachain.database.DBMap.NOTIFY_RESET), this));
            }
        }
    }

    //@Override
    public void close() {
        tableDB.close();
    }
}
