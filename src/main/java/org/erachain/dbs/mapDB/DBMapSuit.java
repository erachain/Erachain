package org.erachain.dbs.mapDB;

import org.erachain.database.DBASet;
import org.erachain.datachain.IndexIterator;
import org.erachain.dbs.DBSuitImpl;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;

import java.util.*;

/**
 * Оболочка для Карты от конкретной СУБД чтобы эту оболочку вставлять в Таблицу, которая запускает события для ГУИ.
 * Для каждой СУБД свой порядок обработки команд
 * @param <T>
 * @param <U>
 */
public abstract class DBMapSuit<T, U> extends DBSuitImpl<T, U> {

    protected Logger logger;
    public int DESCENDING_SHIFT_INDEX = 10000;

    protected DBASet databaseSet;
    protected DB database;

    protected Map<T, U> map;
    // USE for .subMap iterator
    protected T HI;
    protected T LO;

    protected Map<Integer, NavigableSet<Fun.Tuple2<?, T>>> indexes = new HashMap<>();

    /**
     * Если включено, то незабываем еще аключить при создании карты - .counterEnable()
     */
    protected boolean sizeEnable;

    // for DCMapSuit
    public DBMapSuit() {
    }

    /**
     * @param databaseSet
     * @param database - общая база данных для данного набора - вообще надо ее в набор свтавить и все.
     *                 У каждой таблицы внутри может своя база данных открытьваться.
     *                 А команды базы данных типа close commit должны из таблицы передаваться в свою.
     *                 Если в общей базе таблица, то не нужно обработка так как она делается в наборе наверху
     * @param logger
     * @param sizeEnable
     * @param cover
     */
    public DBMapSuit(DBASet databaseSet, DB database, Logger logger, boolean sizeEnable, DBTab cover) {

        this.databaseSet = databaseSet;
        this.database = database;
        this.logger = logger;
        this.cover = cover;
        this.sizeEnable = sizeEnable;

        openMap();
        createIndexes();
        logger.info("USED");
    }

    public DBMapSuit(DBASet databaseSet, DB database, Logger logger, boolean sizeEnable) {
        this(databaseSet, database, logger, sizeEnable, null);
    }

    public DBMapSuit(DBASet databaseSet, DB database, Logger logger) {
        this(databaseSet, database, logger, false, null);
    }

    @Override
    public Object getSource() {
        return map;
    }

    //@Override
    public NavigableSet<Tuple2<?, T>> getIndex(int index, boolean descending) {

        // 0 - это главный индекс - он не в списке indexes
        if (index > 0 && this.indexes != null && this.indexes.containsKey(index)) {
            // IT IS INDEX ID in this.indexes

            if (false && /// old VERSOPN - сейчас 1 индекс за все направления отвечает
                    descending) {
                index += DESCENDING_SHIFT_INDEX;
            }

            return this.indexes.get(index).descendingSet();

        }

        return null;
    }

    @Override
    public IteratorCloseable<T> getIndexIterator(int index, boolean descending) {
        this.addUses();
        try {

            // 0 - это главный индекс - он не в списке indexes
            NavigableSet<Tuple2<?, T>> indexSet = getIndex(index, descending);
            if (indexSet != null) {

                IndexIterator<T> u = new org.erachain.datachain.IndexIterator<>(this.indexes.get(index));
                return u;

            } else {
                if (descending) {
                    Iterator<T> u = ((NavigableMap<T, U>) this.map).descendingKeySet().iterator();
                    return new IteratorCloseableImpl(u);
                }

                Iterator<T> u = this.map.keySet().iterator();
                return new IteratorCloseableImpl(u);

            }
        } finally {
            outUses();
        }
    }

    @Override
    public IteratorCloseable<T> getIterator() {
        this.addUses();
        try {

            Iterator<T> u = map.keySet().iterator();

            return new IteratorCloseableImpl(u);
        } finally {
            this.outUses();
        }

    }

    @Override
    public IteratorCloseable<T> getDescendingIterator() {
        this.addUses();
        try {

            Iterator<T> u = ((NavigableMap) map).descendingMap().keySet().iterator();

            return new IteratorCloseableImpl(u);
        } finally {
            this.outUses();
        }

    }

    @Override
    public IteratorCloseable<T> getIterator(T fromKey, T toKey, boolean descending) {
        this.addUses();

        try {
            if (descending) {
                return
                        // делаем закрываемый Итератор
                        IteratorCloseableImpl.make(
                                // берем индекс с обратным отсчетом
                                ((NavigableMap) this.map).descendingMap()
                                        // задаем границы, так как он обратный границы меняем местами
                                        .subMap(fromKey == null || fromKey.equals(LO) ? HI : fromKey,
                                                toKey == null ? LO : toKey).keySet().iterator());
            }

            return
                    // делаем закрываемый Итератор
                    IteratorCloseableImpl.make(
                            ((NavigableMap) this.map)
                                    // задаем границы, так как он обратный границы меняем местами
                                    .subMap(fromKey == null ? LO : fromKey,
                                            toKey == null ? HI : toKey).keySet().iterator());

        } finally {
            this.outUses();
        }

    }

    @Override
    public IteratorCloseable<T> getIterator(T fromKey, boolean descending) {
        return getIterator(fromKey, null, descending);
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

        if (!sizeEnable)
            return -1;

        this.addUses();
        try {
            int u = this.map.size();
            return u;
        } finally {
            this.outUses();
        }
    }

    @Override
    public U get(T key) {

        this.addUses();
        try {

            try {
                if (this.map.containsKey(key)) {
                    U u = this.map.get(key);
                    return u;
                }

                return this.getDefaultValue(key);

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return this.getDefaultValue(key);
            }
        } finally {
            this.outUses();
        }
    }

    @Override
    public Set<T> keySet() {
        this.addUses();
        try {
            Set<T> u = this.map.keySet();
            return u;
        } finally {
            this.outUses();
        }
    }

    @Override
    public Collection<U> values() {
        this.addUses();
        try {
            Collection<U> u = this.map.values();
            return u;
        } finally {
            this.outUses();
        }
    }

    @Override
    public boolean set(T key, U value) {
        this.addUses();
        try {

            U old = this.map.put(key, value);

            return old != null;
        } finally {
            this.outUses();
        }
    }

    @Override
    public void put(T key, U value) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.set, так как
        /// если в подклассе будет из SET вызов PUT то он придет сюда и при перевузове THIS.SET отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> set(key, value);
        ///
        this.addUses();
        try {
            this.map.put(key, value);

        } finally {
            this.outUses();
        }
    }

    @Override
    public U remove(T key) {

        this.addUses();
        try {

            //REMOVE
            if (this.map.containsKey(key)) {
                U value = this.map.remove(key);
                return value;
            }

            return null;
        } finally {
            this.outUses();
        }

    }

    @Override
    public U removeValue(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///

        this.addUses();
        try {

            //REMOVE
            if (this.map.containsKey(key)) {
                U value = this.map.remove(key);
                return value;
            }

            return null;
        } finally {
            this.outUses();
        }
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Удалить
     * @param key
     * @return
     */
    @Override
    public void delete(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///

        this.addUses();
        try {
            this.map.remove(key);
        } finally {
            this.outUses();
        }

    }

    @Override
    public void deleteValue(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///

        this.addUses();
        try {
            this.map.remove(key);
        } finally {
            this.outUses();
        }
    }


    @Override
    public boolean contains(T key) {

        this.addUses();
        try {

            if (this.map.containsKey(key)) {
                return true;
            }

            return false;
        } finally {
            this.outUses();
        }
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Сбросить
     */
    @Override
    public void clear() {

        if (this.database.getEngine().isClosed())
            return;

        this.addUses();
        try {

            //RESET MAP
            this.map.clear();

            //RESET INDEXES
            for (Set<Tuple2<?, T>> set : this.indexes.values()) {
                set.clear();
            }

        } finally {
            this.outUses();
        }

    }

    @Override
    public void clearCache() {
        // систится у всей базы
    }

    @Override
    public void close() {
        databaseSet = null;
        database = null;
        map = null;
    }

    @Override
    public boolean isClosed() {
        return database.getEngine().isClosed();
    }

    @Override
    public void commit() {}

    @Override
    public void rollback() {}

    @Override
    public void afterRollback() {
    }

}
