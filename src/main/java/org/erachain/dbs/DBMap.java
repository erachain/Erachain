package org.erachain.dbs;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.IDB;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.*;

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
            if (observableData.containsKey(org.erachain.database.DBMap.NOTIFY_LIST)) {
                o.update(null, new ObserverMessage(observableData.get(org.erachain.database.DBMap.NOTIFY_LIST), this));
            }
        }
    }

    /**
     * @param descending true if need descending sort
     * @return
     */
    abstract Iterator<T> getIndexIterator(int indexDB, boolean descending);

    abstract Iterator<T> getIterator(boolean descending);

    public void reset() {
        if (observableData != null) {
            //NOTIFY LIST
            if (observableData.containsKey(org.erachain.database.DBMap.NOTIFY_RESET)) {
                setChanged();
                notifyObservers(new ObserverMessage(observableData.get(org.erachain.database.DBMap.NOTIFY_RESET), this));
            }
        }
    }

    abstract void close();
}
