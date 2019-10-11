package org.erachain.dbs;

import org.erachain.database.DBASet;
import org.erachain.database.IDB;
import org.erachain.database.SortableList;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * К Обработке данных добалены события. Это Суперкласс для таблиц проекта.
 * Однако в каждой таблице есть еще обертка для каждой СУБД отдельно - DBMapSuit
 * @param <T>
 * @param <U>
 */
public abstract class DBTabCommonImpl<T, U> extends Observable implements DBTab<T, U> {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    //public static final int NOTIFY_COUNT = 5;

    public int DESCENDING_SHIFT_INDEX = 10000;

    protected int dbsUsed;

    public static int DEFAULT_INDEX = 0;
    protected DBASet databaseSet;
    protected DB database;
    protected DBTab<T, U> parent;

    protected Map<Integer, Integer> observableData;

    public DBTabCommonImpl() {
    }

    public DBTabCommonImpl(DBASet databaseSet) {

        this.databaseSet = databaseSet;

        if (databaseSet != null && databaseSet.isWithObserver()) {
            observableData = new HashMap<Integer, Integer>(8, 1);
        }
    }

    public DBTabCommonImpl(int dbsUsed, DBASet databaseSet, DB database) {
        this.dbsUsed = dbsUsed;
        this.databaseSet = databaseSet;
        this.database = database;

        //OPEN MAP
        openMap();

        if (databaseSet.isWithObserver()) {
            observableData = new HashMap<Integer, Integer>(8, 1);
        }

        this.databaseSet.addTable(this);

    }

    public DBTabCommonImpl(DBASet databaseSet, DB database) {
        this(IDB.DBS_MAP_DB, databaseSet, database);
    }

    /**
     * Это лоя форкеутой таблицы вызов - запомнить Родителя и все - индексы тут не нужны и обсерверы
     * @param parent
     * @param databaseSet
     */
    public DBTabCommonImpl(int dbsUsed, DBTab parent, DBASet databaseSet) {

        this.dbsUsed = dbsUsed;
        this.databaseSet = databaseSet;
        this.database = databaseSet.database;
        this.parent = parent;

    }
    public DBTabCommonImpl(DBTab parent, DBASet databaseSet) {

        this.databaseSet = databaseSet;
        this.database = databaseSet.database;
        this.parent = parent;

    }

    @Override
    public IDB getDBSet() {
        return this.databaseSet;
    }

    protected abstract void openMap();

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

    //@Override
    //public NavigableSet<Fun.Tuple2<?, T>> getIndex(int index, boolean descending) {
    //    return map.getIndex(index, descending);
    //}

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

    @Override
    public Integer deleteObservableData(int index) {
        return this.observableData.remove(index);
    }

    @Override
    public Integer setObservableData(int index, Integer data) {
        return this.observableData.put(index, data);
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

}
