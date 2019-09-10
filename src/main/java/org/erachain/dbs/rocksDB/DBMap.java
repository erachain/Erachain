package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.IDB;
import org.erachain.database.SortableList;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.InnerDBTable;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

import java.util.*;


@Slf4j
public abstract class DBMap<T, U> extends org.erachain.dbs.DBMap {

    protected InnerDBTable<T, U> tableDB;

    public DBMap(IDB databaseSet, DB database) {
        super(databaseSet, database);
    }

    public DBMap(IDB databaseSet) {
        super(databaseSet);
    }

    protected abstract void getMap(DB database);

    protected abstract void getMemoryMap();

    protected U getDefaultValue() {
        return null;
    }

    protected void createIndexes() {
    }

    public int size() {
        return tableDB.size();
    }

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

    public Set<T> getKeys() {
        return tableDB.keySet();
    }

    public Collection<U> getValues() {
        return tableDB.values();
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

    public boolean contains(T key) {
        return tableDB.containsKey(key);
    }

    public Map<Integer, Integer> getObservableData() {
        return observableData;
    }

    public List<U> getLastValues(int limit) {
        return ((DBRocksDBTable<T, U>) tableDB).getLatestValues(limit);
    }

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

    public void close() {
        tableDB.close();
    }
}
