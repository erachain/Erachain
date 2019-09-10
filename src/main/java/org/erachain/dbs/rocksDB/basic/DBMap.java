package org.erachain.dbs.rocksDB.basic;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.IDB;
import org.erachain.database.SortableList;
import org.erachain.dbs.rocksDB.integration.InnerDBTable;
import org.erachain.rocksDB.indexes.IndexDB;
import org.erachain.rocksDB.integration.DBRocksDBTable;
import org.erachain.rocksDB.integration.InnerDBTable;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

import java.util.*;

import static org.erachain.datachain.DBConstants.*;

@Slf4j
public abstract class DBMap<T, U> extends org.erachain.dbs.DBMap {

    protected InnerDBTable<T, U> tableDB;


    protected abstract InnerDBTable<T, U> getMap(DB database);

    protected abstract InnerDBTable<T, U> getMemoryMap();

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
                notifyObservers(new ObserverMessage(observableData.get(org.erachain.database.DBMap.NOTIFY_ADD), value));
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
                    notifyObservers(new ObserverMessage(this.observableData.get(org.erachain.database.DBMap.NOTIFY_REMOVE), value));
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


    /**
     * Соединяется прямо к списку SortableList для отображения в ГУИ
     * Нужен только для сортировки<br>
     *
     * @param o
     */
    @Override
    public void addObserver(Observer o) {
        super.addObserver(o);
        //NOTIFY
        if (observableData != null) {
            if (observableData.containsKey(org.erachain.database.DBMap.NOTIFY_LIST)) {
                o.update(null, new ObserverMessage(observableData.get(org.erachain.database.DBMap.NOTIFY_LIST), this));
            }
        }
    }

    /**
     * @param descending true if need descending sort
     * @return
     */
    public Iterator<T> getIndexIterator(IndexDB indexDB, boolean descending) {
        return tableDB.getIndexIterator(descending, indexDB);
    }

    public Iterator<T> getIterator(boolean descending) {
        return tableDB.getIterator(descending);
    }

    //todo Gleb нужен ли этот метод?
    public SortableList<T, U> getList() {
        SortableList<T, U> list;
        if (size() < 1000) {
            list = new SortableList<>(this);
        } else {
            // обрезаем полный список в базе до 1000
            list = SortableList.makeSortableList(this, false, 1000);
        }
        return list;

    }

    public void reset() {
        tableDB.clear();
        if (observableData != null) {
            //NOTIFY LIST
            if (observableData.containsKey(org.erachain.database.DBMap.NOTIFY_RESET)) {
                setChanged();
                notifyObservers(new ObserverMessage(observableData.get(org.erachain.database.DBMap.NOTIFY_RESET), this));
            }
        }
    }

    public void close() {
        tableDB.close();
    }
}
