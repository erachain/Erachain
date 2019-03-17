package org.erachain.database;

import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;

import java.util.*;

public abstract class DBMap<T, U> extends Observable {

    public static final int NOTIFY_RESET = 1;
    public static final int NOTIFY_ADD = 2;
    public static final int NOTIFY_REMOVE = 3;
    public static final int NOTIFY_LIST = 4;
    public static final int NOTIFY_COUNT = 5;

    public static final int DEFAULT_INDEX = 0;

    protected static Logger LOGGER;

    protected IDB databaseSet;
    protected Map<T, U> map;
    protected Map<Integer, NavigableSet<Tuple2<?, T>>> indexes;

    protected Map<Integer, Integer> observableData;

    public DBMap() {
    }

    public DBMap(IDB databaseSet, DB database) {
        this.databaseSet = databaseSet;

        //OPEN MAP
        this.map = this.getMap(database);

        //CREATE INDEXES
        this.indexes = new HashMap<Integer, NavigableSet<Tuple2<?, T>>>();
        this.createIndexes(database);

        if (databaseSet.isWithObserver()) {
            observableData = new HashMap<Integer, Integer>(8, 1);
        }

    }


    public DCSet getDBSet() {
        return (DCSet) this.databaseSet;
    }


    protected abstract Map<T, U> getMap(DB database);

    protected abstract Map<T, U> getMemoryMap();

    protected abstract U getDefaultValue();

    //protected Map<Integer, Integer> getObservableData() {
    //    return this.observableData;
    //}

    protected abstract void createIndexes(DB database);

    /**
     * Make SECODATY INDEX
     * INDEX ID = 0 - its is PRIMARY - not use it here
     *
     * @param index index ID. Must be 1...9999
     * @param indexSet
     * @param descendingIndexSet
     * @param function
     * @param <V>
     */
    @SuppressWarnings("unchecked")
    protected <V> void createIndex(int index, NavigableSet<?> indexSet, NavigableSet<?> descendingIndexSet, Function2<V, T, U> function) {
        assert(index > 0 && index < 10000);
        Bind.secondaryKey((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) indexSet, function);
        this.indexes.put(index, (NavigableSet<Tuple2<?, T>>) indexSet);

        Bind.secondaryKey((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) descendingIndexSet, function);
        this.indexes.put(index + 10000, (NavigableSet<Tuple2<?, T>>) descendingIndexSet);
    }

    /**
     * Make SECODATY INDEX
     * INDEX ID = 0 - its is PRIMARY - not use it here
     *
     * @param index index ID. Must be 1...9999
     * @param indexSet
     * @param descendingIndexSet
     * @param function
     * @param <V>
     */
    @SuppressWarnings("unchecked")
    protected <V> void createIndexes(int index, NavigableSet<?> indexSet, NavigableSet<?> descendingIndexSet, Function2<V[], T, U> function) {
        assert(index > 0 && index < 10000);
        Bind.secondaryKeys((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) indexSet, function);
        this.indexes.put(index, (NavigableSet<Tuple2<?, T>>) indexSet);

        Bind.secondaryKeys((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) descendingIndexSet, function);
        this.indexes.put(index + 10000, (NavigableSet<Tuple2<?, T>>) descendingIndexSet);
    }

    public void addUses() {
        if (this.databaseSet != null) {
            this.databaseSet.addUses();
        }
    }

    public void outUses() {
        if (this.databaseSet != null) {
            this.databaseSet.outUses();
        }
    }

    public int size() {
        this.addUses();
        int u = this.map.size();
        this.outUses();
        return u;
    }

    public U get(T key) {

        this.addUses();

        try {
            if (this.map.containsKey(key)) {
                U u = this.map.get(key);
                this.outUses();
                return u;
            }

            U u = this.getDefaultValue();
            this.outUses();
            return u;
        } catch (Exception e)
        //else
        {
            //LOGGER.error(e.getMessage(), e);

            U u = this.getDefaultValue();
            this.outUses();
            return u;
        }
    }

    public Set<T> getKeys() {
        this.addUses();
        Set<T> u = this.map.keySet();
        this.outUses();
        return u;
    }

    public Collection<U> getValues() {
        this.addUses();
        Collection<U> u = this.map.values();
        this.outUses();
        return u;
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Добавить
     * @param key
     * @param value
     * @return
     */
    public boolean set(T key, U value) {
        this.addUses();
        try {

            U old = this.map.put(key, value);

            //COMMIT and NOTIFY if not FORKED
            // TODO - удалить тут этот ак как у нас везде управляемый внешний коммит
            if (false) this.databaseSet.commit();

            //NOTIFY
            if (this.observableData != null && (old == null || !old.equals(value))) {
                if (this.observableData.containsKey(NOTIFY_COUNT)) {
                    this.setChanged();
                    this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_COUNT), this)); /// SLOW .size()));
                } else if (this.observableData.containsKey(NOTIFY_ADD)) {
                    this.setChanged();
                    this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_ADD), value));
                }
            }

            this.outUses();
            return old != null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        this.outUses();
        return false;
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Удалить
     * @param key
     * @return
     */
    public U delete(T key) {

        this.addUses();

        U value;

        try {
            //REMOVE
            if (this.map.containsKey(key)) {
                value = this.map.remove(key);

                //NOTIFY
                if (this.observableData != null) {
                    if (this.observableData.containsKey(NOTIFY_COUNT)) {
                        this.setChanged();
                        this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_COUNT), this));
                    } else if (this.observableData.containsKey(NOTIFY_REMOVE)) {
                        this.setChanged();
                        this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_REMOVE), value));
                    }
                }

            } else
                value = null;

        } catch (Exception e) {
            value = null;
            LOGGER.error(e.getMessage(), e);
        }

        this.outUses();

        return value;
    }

    public boolean contains(T key) {

        this.addUses();

        if (this.map.containsKey(key)) {
            this.outUses();
            return true;
        }

        this.outUses();
        return false;
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Список
     * @param o
     */
    @Override
    public void addObserver(Observer o) {

        this.addUses();

        //ADD OBSERVER
        super.addObserver(o);

        //NOTIFY
        if (this.observableData != null) {
            if (this.observableData.containsKey(NOTIFY_COUNT)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_COUNT), this)); /// SLOW .size()));
            } else if (this.observableData.containsKey(NOTIFY_LIST)) {
                //CREATE LIST
                SortableList<T, U> list = new SortableList<T, U>(this);

                //UPDATE
                o.update(null, new ObserverMessage(this.observableData.get(NOTIFY_LIST), list));
            }
        }

        this.outUses();
    }

    /**
     *
     * @param index <b>primary Index = 0</b>, secondary index = 1...10000
     * @param descending true if need descending sort
     * @return
     */
    public Iterator<T> getIterator(int index, boolean descending) {
        this.addUses();

        // 0 - это главный индекс - он не в списке indexes
        if (index > 0 && this.indexes != null && this.indexes.containsKey(index)) {
            // IT IS INDEX ID in this.indexes

            if (descending) {
                index += 10000;
            }

            IndexIterator<T> u = new IndexIterator<T>(this.indexes.get(index));
            this.outUses();
            return u;

        } else {
            if (descending) {
                Iterator<T> u = ((NavigableMap<T, U>) this.map).descendingKeySet().iterator();
                this.outUses();
                return u;
            }

            Iterator<T> u = ((NavigableMap<T, U>) this.map).keySet().iterator();
            this.outUses();
            return u;

        }
    }

    public SortableList<T, U> getList() {
        this.addUses();
        SortableList<T, U> u = new SortableList<T, U>(this);
        this.outUses();
        return u;
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Сбросить
     */
    public void reset() {
        this.addUses();

        //RESET MAP
        this.map.clear();

        //RESET INDEXES
        for (Set<Tuple2<?, T>> set : this.indexes.values()) {
            set.clear();
        }

        // NOTYFIES
        if (this.observableData != null) {
            //NOTIFY LIST
            if (this.observableData.containsKey(NOTIFY_COUNT)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_COUNT), this));
            } else if (this.observableData.containsKey(NOTIFY_RESET)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_RESET), null));
            }

        }

        this.outUses();
    }
}
