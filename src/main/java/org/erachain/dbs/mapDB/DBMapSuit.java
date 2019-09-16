package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.DBMapCommonImpl;
import org.erachain.dbs.DBMapSuitImpl;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple2;

import java.util.*;

@Slf4j
public abstract class DBMapSuit<T, U> extends DBMapSuitImpl<T, U> {

    public int DESCENDING_SHIFT_INDEX = 10000;

    protected DBASet databaseSet;
    protected DB database;

    protected Map<T, U> map;
    protected Map<Integer, NavigableSet<Fun.Tuple2<?, T>>> indexes = new HashMap<>();

    // for DCMapSuit
    public DBMapSuit() {
    }

    public DBMapSuit(DBASet databaseSet, DB database) {
        this.databaseSet = databaseSet;
        this.database = database;
        getMap();
        createIndexes();
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
    protected <V> void createIndex(int index, NavigableSet<?> indexSet, NavigableSet<?> descendingIndexSet, Function2<V, T, U> function) {
        assert(index > 0 && index < DESCENDING_SHIFT_INDEX);

        Bind.secondaryKey((Bind.MapWithModificationListener<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) indexSet, function);
        this.indexes.put(index, (NavigableSet<Tuple2<?, T>>) indexSet);

        Bind.secondaryKey((Bind.MapWithModificationListener<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) descendingIndexSet, function);
        this.indexes.put(index + DESCENDING_SHIFT_INDEX, (NavigableSet<Tuple2<?, T>>) descendingIndexSet);
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
        assert(index > 0 && index < DESCENDING_SHIFT_INDEX);
        Bind.secondaryKeys((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) indexSet, function);
        this.indexes.put(index, (NavigableSet<Tuple2<?, T>>) indexSet);

        Bind.secondaryKeys((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) descendingIndexSet, function);
        this.indexes.put(index + DESCENDING_SHIFT_INDEX, (NavigableSet<Tuple2<?, T>>) descendingIndexSet);
    }

    //@Override
    public NavigableSet<Tuple2<?, T>> getIndex(int index, boolean descending) {

        // 0 - это главный индекс - он не в списке indexes
        if (index > 0 && this.indexes != null && this.indexes.containsKey(index)) {
            // IT IS INDEX ID in this.indexes

            if (descending) {
                index += DESCENDING_SHIFT_INDEX;
            }

            return this.indexes.get(index);

        }

        return null;
    }

    /**
     *
     * @param index <b>primary Index = 0</b>, secondary index = 1...10000
     * @param descending true if need descending sort
     * @return
     */
    @Override
    public Iterator<T> getIterator(int index, boolean descending) {
        this.addUses();

        // 0 - это главный индекс - он не в списке indexes
        NavigableSet<Tuple2<?, T>> indexSet = getIndex(index, descending);
        if (indexSet != null) {

            org.erachain.datachain.IndexIterator<T> u = new org.erachain.datachain.IndexIterator<T>(this.indexes.get(index));
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

    @Override
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
            //logger.error(e.getMessage(), e);

            U u = this.getDefaultValue();
            this.outUses();
            return u;
        }
    }

    @Override
    public Set<T> keySet() {
        this.addUses();
        Set<T> u = this.map.keySet();
        this.outUses();
        return u;
    }

    @Override
    public Collection<U> values() {
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
    @Override
    public boolean set(T key, U value) {
        this.addUses();

        U old = this.map.put(key, value);

        this.outUses();

        if (this.observableData != null) {
            if (this.observableData.containsKey(NOTIFY_ADD)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_ADD), value));
            }
        }

        return old != null;
    }

    @Override
    public void put(T key, U value) {
        set(key, value);
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Удалить
     * @param key
     * @return
     */
    @Override
    public U remove(T key) {

        this.addUses();

        U value;

        //try {
        //REMOVE
        if (this.map.containsKey(key)) {
            value = this.map.remove(key);
            this.outUses();

            //NOTIFY
            if (this.observableData != null) {
                if (this.observableData.containsKey(NOTIFY_REMOVE)) {
                    this.setChanged();
                    this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_REMOVE), value));
                }
            }

        } else {
            value = null;
            this.outUses();
        }

        return value;
    }

    @Override
    public U removeValue(T key) {
        return remove(key);
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Удалить
     * @param key
     * @return
     */
    @Override
    public void delete(T key) {

        this.addUses();
        this.map.remove(key);
        this.outUses();

        //NOTIFY
        if (this.observableData != null) {
            if (this.observableData.containsKey(NOTIFY_DELETE)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_DELETE), key));
            }
        }

    }

    @Override
    public void deleteValue(T key) {
        delete(key);
    }


    @Override
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
     * уведомляет только счетчик если он разрешен, иначе Сбросить
     */
    @Override
    public void reset() {
        this.addUses();

        //RESET MAP
        this.map.clear();

        //RESET INDEXES
        for (Set<Tuple2<?, T>> set : this.indexes.values()) {
            set.clear();
        }

        this.outUses();

        // NOTYFIES
        if (this.observableData != null) {
            //NOTIFY LIST
            if (this.observableData.containsKey(NOTIFY_RESET)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_RESET), this));
            }

        }

    }
}
