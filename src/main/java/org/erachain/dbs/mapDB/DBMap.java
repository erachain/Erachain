package org.erachain.dbs.mapDB;

import org.erachain.database.DBASet;
import org.erachain.dbs.DBMapImpl;
import org.erachain.database.IndexIterator;
import org.erachain.database.SortableList;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class DBMap<T, U> extends DBMapImpl<T, U> implements org.erachain.dbs.DBMap<T, U> {

    public static final int NOTIFY_RESET = 1;
    public static final int NOTIFY_ADD = 2;
    public static final int NOTIFY_REMOVE = 3;
    public static final int NOTIFY_LIST = 4;
    //public static final int NOTIFY_COUNT = 5;

    public int DESCENDING_SHIFT_INDEX = 10000;

    public static int DEFAULT_INDEX = 0;
    private static Logger logger = LoggerFactory.getLogger(DBMap.class.getName());
    protected DBASet databaseSet;
    protected Map<T, U> map;
    protected Map<Integer, NavigableSet<Tuple2<?, T>>> indexes;

    protected Map<Integer, Integer> observableData;

    public DBMap(DBASet databaseSet) {
        super(databaseSet);
    }

    public DBMap(DBASet databaseSet, DB database) {
        super(databaseSet, database);
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
        //    return old != null;
        //} catch (Exception e) {
        //    logger.error(e.getMessage(), e);
        //}

        this.outUses();
        return false;
    }

    public U delete(T key) {

        this.addUses();

        U value;

        //try {
            //REMOVE
            if (this.map.containsKey(key)) {
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

        if (this.map.containsKey(key)) {
            this.outUses();
            return true;
        }

        this.outUses();
        return false;
    }

    public Map<Integer, Integer> getObservableData() {
        return observableData;
    }

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
                            logger.error(e.getMessage(), e);
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
            if (this.observableData.containsKey(NOTIFY_RESET)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_RESET), this));
            }

        }

        this.outUses();
    }
}
