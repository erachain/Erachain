package org.erachain.dbs;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.IDB;
import org.erachain.database.SortableList;
import org.erachain.rocksDB.indexes.IndexDB;
import org.erachain.rocksDB.integration.DBRocksDBTable;
import org.erachain.rocksDB.integration.InnerDBTable;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.*;

import static org.erachain.datachain.DBConstants.*;

@Slf4j
public abstract class DBMap<T, U> extends Observable {
    protected IDB databaseSet;
    protected Map<Integer, Integer> observableData;

    public DBMap() {
    }

    public DBMap(IDB databaseSet) {
        this.databaseSet = databaseSet;
        if (databaseSet != null && databaseSet.isWithObserver()) {
            observableData = new HashMap<>();
        }
    }

    public void init(IDB databaseSet) {
        this.databaseSet = databaseSet;
        getMap(null);
        createIndexes();
        if (databaseSet != null && databaseSet.isWithObserver()) {
            observableData = new HashMap<>();
        }
    }

    public DBMap(IDB databaseSet, DB database) {
        this.databaseSet = databaseSet;
        getMap(database);
        createIndexes();
        if (databaseSet.isWithObserver()) {
            observableData = new HashMap<>();
        }
    }


    public IDB getDBSet() {
        return databaseSet;
    }

    protected abstract void getMap(DB database);

    protected abstract void getMemoryMap();

    protected U getDefaultValue() {
        return null;
    }

    protected void createIndexes() {
    }

    abstract int size();

    abstract U get(T key);

    abstract Set<T> getKeys();

    abstract Collection<U> getValues();

    abstract void put(T key, U value);

    abstract U delete(T key);

    abstract boolean contains(T key);

    public Map<Integer, Integer> getObservableData() {
        return observableData;
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
            if (observableData.containsKey(NOTIFY_LIST)) {
                o.update(null, new ObserverMessage(observableData.get(NOTIFY_LIST), this));
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
            if (observableData.containsKey(NOTIFY_RESET)) {
                setChanged();
                notifyObservers(new ObserverMessage(observableData.get(NOTIFY_RESET), this));
            }
        }
    }

    public void close() {
        tableDB.close();
    }
}
