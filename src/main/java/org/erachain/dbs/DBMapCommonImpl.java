package org.erachain.dbs;

import org.erachain.database.DBASet;
import org.erachain.database.IDB;
import org.erachain.database.IndexIterator;
import org.erachain.database.SortableList;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * К Обработке данных добалены события. Это Суперкласс для таблиц проекта.
 * Однако в каждой таблице есть еще обертка для каждой СУБД отдельно - DBMapSuit
 * @param <T>
 * @param <U>
 */
public abstract class DBMapCommonImpl<T, U> extends Observable implements DBMap<T, U> {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    //public static final int NOTIFY_COUNT = 5;

    public int DESCENDING_SHIFT_INDEX = 10000;

    public static int DEFAULT_INDEX = 0;
    protected DBASet databaseSet;
    protected DB database;
    protected DBMapSuit<T, U> map;
    protected DBMap<T, U> parent;
    protected Map<Integer, NavigableSet<Tuple2<?, T>>> indexes;

    protected Map<Integer, Integer> observableData;

    public DBMapCommonImpl() {
    }

    public DBMapCommonImpl(DBASet databaseSet) {

        this.databaseSet = databaseSet;

        //CREATE INDEXES
        this.indexes = new HashMap<Integer, NavigableSet<Tuple2<?, T>>>();

        if (databaseSet != null && databaseSet.isWithObserver()) {
            observableData = new HashMap<Integer, Integer>(8, 1);
        }
    }

    public DBMapCommonImpl(DBASet databaseSet, DB database) {
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

    /**
     * Это лоя форкеутой таблицы вызов - запомнить Родителя и все - индексы тут не нужны и обсерверы
     * @param parent
     * @param databaseSet
     */
    public DBMapCommonImpl(DBMap parent, DBASet databaseSet) {

        this.databaseSet = databaseSet;
        this.parent = parent;

    }

    @Override
    public IDB getDBSet() {
        return this.databaseSet;
    }

    protected abstract void getMap();

    protected abstract void createIndexes();

    @Override
    public Map<Integer, Integer> getObservableData() {
        return observableData;
    }

    @Override
    public Integer deleteObservableData(int index) {
        return this.observableData.remove(index);
    }

    @Override
    public Integer setObservableData(int index, Integer data) {
        return this.observableData.put(index, data);
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
    }

    /**
     *
     * @param index <b>primary Index = 0</b>, secondary index = 1...10000
     * @param descending true if need descending sort
     * @return
     */
    @Override
    public Iterator<T> getIterator(int index, boolean descending) {

        // 0 - это главный индекс - он не в списке indexes
        if (index > 0 && this.indexes != null && this.indexes.containsKey(index)) {
            // IT IS INDEX ID in this.indexes

            if (descending) {
                index += DESCENDING_SHIFT_INDEX;
            }

            IndexIterator<T> u = new IndexIterator<T>(this.indexes.get(index));
            return u;

        } else {
            if (descending) {
                Iterator<T> u = ((NavigableMap<T, U>) this.map).descendingKeySet().iterator();
                return u;
            }

            Iterator<T> u = ((NavigableMap<T, U>) this.map).keySet().iterator();
            return u;

        }
    }
    public int getDefaultIndex() {
        return DEFAULT_INDEX;
    }

    @Override
    public SortableList<T, U> getList() {
        SortableList<T, U> list;
        if (this.size() < 1000) {
            list = new SortableList<T, U>(this);
        } else {
            // обрезаем полный список в базе до 1000
            list = SortableList.makeSortableList(this, false, 1000);
        }

        return list;
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Сбросить
     */
    @Override
    public void reset() {
        //RESET MAP
        this.map.reset();

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
