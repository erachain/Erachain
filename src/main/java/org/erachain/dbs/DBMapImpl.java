package org.erachain.dbs;

import org.erachain.database.DBASet;
import org.erachain.database.IndexIterator;
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
public abstract class DBMapImpl<T, U> extends DBMapCommonImpl implements DBMap<T, U> {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    public DBMapImpl() {
    }

    public DBMapImpl(DBASet databaseSet) {
        super(databaseSet);

    }

    public DBMapImpl(DBASet databaseSet, DB database) {
        super(databaseSet, database);
    }

    /**
     * Это лоя форкеутой таблицы вызов - запомнить Родителя и все - индексы тут не нужны и обсерверы
     * @param parent
     * @param databaseSet
     */
    public DBMapImpl(DBMap parent, DBASet databaseSet) {
        super(parent, databaseSet);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public Set<T> getKeys() {
        return this.map.keySet();
    }

    @Override
    public Collection<U> getValues() {
        return this.map.values();
    }

    @Override
    public U get(T key) {
        return this.map.get(key);
    }

    @Override
    public boolean set(T key, U value) {
        return this.map.set(key, value) != null;
    }

    @Override
    public void put(T key, U value) {
        this.map.put(key, value);
    }

    @Override
    public U remove(T key) {

        U value = this.map.remove(key);

        if (value != null) {
            //NOTIFY
            if (this.observableData != null) {
                if (this.observableData.containsKey(NOTIFY_REMOVE)) {
                    this.setChanged();
                    this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_REMOVE), value));
                }
            }
        }

        return value;
    }

    @Override U removeValue(T key) {
        return remove(key);
    }

    @Override void delete(T key) {
        remove(key);
    }

    @Override void deleteValue(T key) {
        remove(key);
    }

    @Override
    public boolean contains(T key) {
        return map.contains(key);
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

    public void getMap() {}
    public void createIndexes() {}
}
