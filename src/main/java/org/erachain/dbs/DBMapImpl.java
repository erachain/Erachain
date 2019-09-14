package org.erachain.dbs;

import org.erachain.database.*;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class DBMapImpl<T, U> extends Observable implements DBMap<T, U> {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    //public static final int NOTIFY_COUNT = 5;

    public int DESCENDING_SHIFT_INDEX = 10000;

    public static int DEFAULT_INDEX = 0;
    protected DBASet databaseSet;
    protected DB database;
    protected DBMapSuit<T, U> map;
    protected Map<Integer, NavigableSet<Tuple2<?, T>>> indexes;

    protected Map<Integer, Integer> observableData;

    public DBMapImpl() {
    }

    public DBMapImpl(DBASet databaseSet) {

        this.databaseSet = databaseSet;

        //CREATE INDEXES
        this.indexes = new HashMap<Integer, NavigableSet<Tuple2<?, T>>>();

        if (databaseSet != null) {
            this.database = databaseSet.database;
            if (databaseSet.isWithObserver()) {
                observableData = new HashMap<Integer, Integer>(8, 1);
            }
        }
    }

    public DBMapImpl(DBASet databaseSet, DB database) {
        this.databaseSet = databaseSet;
        this.database = database;

        //OPEN MAP
        getMap();

        //CREATE INDEXES
        this.indexes = new HashMap<Integer, NavigableSet<Tuple2<?, T>>>();

        if (this.map !=  null) {
            this.createIndexes();
        }

        if (databaseSet.isWithObserver()) {
            observableData = new HashMap<Integer, Integer>(8, 1);
        }

    }

    @Override
    public IDB getDBSet() {
        return this.databaseSet;
    }

    protected abstract void getMap();

    protected abstract void getMemoryMap();

    protected abstract U getDefaultValue();

    protected abstract void createIndexes();

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

    @Override
    public void addUses() {
        if (this.databaseSet != null) {
            this.databaseSet.addUses();
        }
    }

    @Override
    public void outUses() {
        if (this.databaseSet != null) {
            this.databaseSet.outUses();
        }
    }

    @Override
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
            if (this.map.contains(key)) {
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
    public Set<T> getKeys() {
        this.addUses();
        Set<T> u = this.map.keySet();
        this.outUses();
        return u;
    }

    @Override
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
    @Override
    public boolean set(T key, U value) {
        this.addUses();
        //try {

            U old = this.map.put(key, value);

            //COMMIT and NOTIFY if not FORKED
            // TODO - удалить тут этот ак как у нас везде управляемый внешний коммит
            if (false) this.databaseSet.commit();

            //NOTIFY
            if (this.observableData != null && (old == null || !old.equals(value))) {
                if (this.observableData.containsKey(NOTIFY_ADD)) {
                    this.setChanged();
                    this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_ADD), value));
                }
            }

        //    this.outUses();
        //} catch (Exception e) {
        //    logger.error(e.getMessage(), e);
        //}

        this.outUses();
        return old != null;
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Удалить
     * @param key
     * @return
     */
    @Override
    public U delete(T key) {

        this.addUses();

        U value;

        //try {
            //REMOVE
            if (this.map.contains(key)) {
                value = this.map.remove(key);

                //NOTIFY
                if (this.observableData != null) {
                    if (this.observableData.containsKey(NOTIFY_REMOVE)) {
                        this.setChanged();
                        this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_REMOVE), value));
                    }
                }

            } else
                value = null;

        //} catch (Exception e) {
        //    value = null;
        //    logger.error(e.getMessage(), e);
        //}

        this.outUses();

        return value;
    }

    @Override
    public boolean contains(T key) {

        this.addUses();

        if (this.map.contains(key)) {
            this.outUses();
            return true;
        }

        this.outUses();
        return false;
    }

    @Override
    public Map<Integer, Integer> getObservableData() {
        return observableData;
    }

    @Override
    public boolean checkObserverMessageType(int messageType, int thisMessageType) {
        if (observableData == null || observableData.isEmpty() || !observableData.containsKey(thisMessageType))
            return false;


        return observableData.get(messageType) == thisMessageType;
    }

    /**
     * Соединяется прямо к списку SortableList для отображения в ГУИ
     * Нужен только для сортировки<br>
     * TODO надо его убрать отсюла нафиг чтобы не тормозило и только
     * по месту работало окнкретно как надо
     * @param o
     */
    @Override
    public void addObserver(Observer o) {

        this.addUses();

        //ADD OBSERVER
        super.addObserver(o);

        //NOTIFY
        if (this.observableData != null) {
            if (this.observableData.containsKey(NOTIFY_LIST)) {
                if (false) {
                    //CREATE LIST
                    SortableList<T, U> list;
                    if (this.size() < 1000) {
                        list = new SortableList<T, U>(this);
                    } else {
                        List<T> keys = new ArrayList<T>();
                        // тут может быть ошибка если основной индекс не TreeMap
                        try {
                            // обрезаем полный список в базе до 1000
                            Iterator iterator = this.getIterator(DEFAULT_INDEX, false);
                            int i = 0;
                            while (iterator.hasNext() && ++i < 1000) {
                                keys.add((T) iterator.next());
                            }
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }

                        list = new SortableList<T, U>(this, keys);
                    }

                    //UPDATE
                    o.update(null, new ObserverMessage(this.observableData.get(NOTIFY_LIST), list));
                } else {

                    //UPDATE
                    o.update(null, new ObserverMessage(this.observableData.get(NOTIFY_LIST), this));

                }
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
    @Override
    public Iterator<T> getIterator(int index, boolean descending) {
        this.addUses();

        // 0 - это главный индекс - он не в списке indexes
        if (index > 0 && this.indexes != null && this.indexes.containsKey(index)) {
            // IT IS INDEX ID in this.indexes

            if (descending) {
                index += DESCENDING_SHIFT_INDEX;
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

    @Override
    public SortableList<T, U> getList() {
        addUses();
        SortableList<T, U> list;
        if (this.size() < 1000) {
            list = new SortableList<T, U>(this);
        } else {
            // обрезаем полный список в базе до 1000
            list = SortableList.makeSortableList(this, false, 1000);
        }

        outUses();
        return list;
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Сбросить
     */
    @Override
    public void reset() {
        this.addUses();

        //RESET MAP
        this.map.reset();

        //RESET INDEXES
        for (Set<Tuple2<?, T>> set : this.indexes.values()) {
            set.clear();
        }

        // NOTYFIES
        if (this.observableData != null) {
            //NOTIFY LIST
            if (this.observableData.containsKey(NOTIFY_RESET)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_RESET), this));
            }

        }

        this.outUses();
    }
}
