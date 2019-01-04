package org.erachain.database;
// upd 09/03

import org.erachain.datachain.DCSet;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple2;
import org.erachain.utils.ObserverMessage;

import java.util.*;

//import org.erachain.database.wallet.DWSet;

public abstract class DBMap<T, U> extends Observable {

    public static final int NOTIFY_RESET = 1;
    public static final int NOTIFY_ADD = 2;
    public static final int NOTIFY_REMOVE = 3;
    public static final int NOTIFY_LIST = 4;
    public static final int NOTIFY_COUNT = 5;

    public static final int DEFAULT_INDEX = 0;
    static Logger LOGGER = LoggerFactory.getLogger(DBMap.class.getName());
    protected IDB databaseSet;
    protected Map<T, U> map;
    private Map<Integer, NavigableSet<Tuple2<?, T>>> indexes;

    public DBMap(IDB databaseSet, DB database) {
        this.databaseSet = databaseSet;

        //OPEN MAP
        this.map = this.getMap(database);

        //CREATE INDEXES
        this.indexes = new HashMap<Integer, NavigableSet<Tuple2<?, T>>>();
        this.createIndexes(database);
    }


    public DCSet getDBSet() {
        return (DCSet) this.databaseSet;
    }


    protected abstract Map<T, U> getMap(DB database);

    protected abstract Map<T, U> getMemoryMap();

    protected abstract U getDefaultValue();

    protected abstract Map<Integer, Integer> getObservableData();

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

    public boolean set(T key, U value) {
        this.addUses();
        try {

            U old = this.map.put(key, value);

            //COMMIT and NOTIFY if not FORKED
            // TODO
            this.databaseSet.commit();

            //NOTIFY ADD
            if (this.getObservableData().containsKey(NOTIFY_ADD)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.getObservableData().get(NOTIFY_ADD), value));
            }

            if (this.getObservableData().containsKey(NOTIFY_COUNT)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.getObservableData().get(NOTIFY_COUNT), this)); /// SLOW .size()));
            }

            this.outUses();
            return old != null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        this.outUses();
        return false;
    }

    public void delete(T key) {

        this.addUses();

        try {
            //REMOVE
            if (this.map.containsKey(key)) {
                U value = this.map.remove(key);

                //NOTIFY REMOVE
                if (this.getObservableData().containsKey(NOTIFY_REMOVE)) {
                    this.setChanged();
                    this.notifyObservers(new ObserverMessage(this.getObservableData().get(NOTIFY_REMOVE), value));
                }

                if (this.getObservableData().containsKey(NOTIFY_COUNT)) {
                    this.setChanged();
                    this.notifyObservers(new ObserverMessage(this.getObservableData().get(NOTIFY_COUNT), this)); /// SLOW .size()));
                }

            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        this.outUses();

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

    @Override
    public void addObserver(Observer o) {

        this.addUses();

        //ADD OBSERVER
        super.addObserver(o);

        //NOTIFY LIST if this not FORK
        if (this.getObservableData().containsKey(NOTIFY_LIST)) {
            //CREATE LIST
            SortableList<T, U> list = new SortableList<T, U>(this);

            //UPDATE
            o.update(null, new ObserverMessage(this.getObservableData().get(NOTIFY_LIST), list));
        }

        if (this.getObservableData().containsKey(NOTIFY_COUNT)) {
            this.setChanged();
            this.notifyObservers(new ObserverMessage(this.getObservableData().get(NOTIFY_COUNT), this)); /// SLOW .size()));
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

        if (index == DEFAULT_INDEX) {
            if (descending) {
                Iterator<T> u = ((NavigableMap<T, U>) this.map).descendingKeySet().iterator();
                this.outUses();
                return u;
            }

            Iterator<T> u = ((NavigableMap<T, U>) this.map).keySet().iterator();
            this.outUses();
            return u;
        } else {
            if (descending) {
                index += 10000;
            }

            IndexIterator<T> u = new IndexIterator<T>(this.indexes.get(index));
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

    public void reset() {
        this.addUses();

        //RESET MAP
        this.map.clear();

        //RESET INDEXES
        for (Set<Tuple2<?, T>> set : this.indexes.values()) {
            set.clear();
        }

        //NOTIFY LIST
        if (this.getObservableData().containsKey(NOTIFY_RESET)) {
            //CREATE LIST
            /////SortableList<T, U> list = new SortableList<T, U>(this);

            //UPDATE
            this.setChanged();
            this.notifyObservers(new ObserverMessage(this.getObservableData().get(NOTIFY_RESET), null));
        }

        if (this.getObservableData().containsKey(NOTIFY_COUNT)) {
            this.setChanged();
            this.notifyObservers(new ObserverMessage(this.getObservableData().get(NOTIFY_COUNT), this));
        }

        this.outUses();
    }
}
