package org.erachain.dbs;

import org.erachain.database.SortableList;
import org.erachain.database.DBASet;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * К Обработке данных добалены события. Это Суперкласс для таблиц проекта.
 * Однако в каждой таблице есть еще обертка для каждой СУБД отдельно - DBMapSuit
 * @param <T>
 * @param <U>
 */
public abstract class DBTabImpl<T, U> extends DBTabCommonImpl<T, U> implements DBTab<T, U> {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    // Эта Карта не должна путаться вверху с DCU картой - иначе НУЛ при заходе в DBMapCommonImpl
    protected DBMapSuit<T, U> map;

    //public DBMapImpl() {
    //}

    //public DBMapImpl(DBASet databaseSet) {
    //    super(databaseSet);
    //
    //}

    public DBTabImpl(DBASet databaseSet, DB database) {
        super(databaseSet, database);
    }

    /**
     * Это лоя форкеутой таблицы вызов - запомнить Родителя и все - индексы тут не нужны и обсерверы
     * @param parent
     * @param databaseSet
     */
    public DBTabImpl(DBTab parent, DBASet databaseSet) {
        super(parent, databaseSet);

        // OPEN MAP
        this.getMap();
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public Set<T> keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection<U> values() {
        return this.map.values();
    }

    @Override
    public U get(T key) {
        return this.map.get(key);
    }

    @Override
    public boolean set(T key, U value) {

        boolean result = this.map.set(key, value);

        if (this.observableData != null) {
            if (this.observableData.containsKey(NOTIFY_ADD)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_ADD), value));
            }
        }

        return result;

    }

    @Override
    public void put(T key, U value) {

        this.map.put(key, value);

        if (this.observableData != null) {
            if (this.observableData.containsKey(NOTIFY_ADD)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_ADD), value));
            }
        }
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

    @Override
    public U removeValue(T key) {
        return remove(key);
    }

    @Override
    public void delete(T key) {
        this.map.delete(key);

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
        return map.getIterator(index, descending);
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
