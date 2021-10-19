package org.erachain.dbs.mapDB;

import org.erachain.database.DBASet;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;

/**
 * EXPEREMENTAL !!!
 * Оболочка эмулирующая Map, но это Set - для упрощения работы некоторых поисковых вещей - задумка для TimeDoneSuitMapDB и подобных
 *
 * @param <T>
 */
public abstract class DBMapSetSuit<T> extends DBMapSuit<T, Boolean> {

    protected Set<T> set;

    // for DCMapSuit
    public DBMapSetSuit() {
        super();
    }

    /**
     * @param databaseSet
     * @param database    - общая база данных для данного набора - вообще надо ее в набор свтавить и все.
     *                    У каждой таблицы внутри может своя база данных открытьваться.
     *                    А команды базы данных типа close commit должны из таблицы передаваться в свою.
     *                    Если в общей базе таблица, то не нужно обработка так как она делается в наборе наверху
     * @param logger
     * @param sizeEnable
     * @param cover
     */
    public DBMapSetSuit(DBASet databaseSet, DB database, Logger logger, boolean sizeEnable, DBTab cover) {
        super(databaseSet, database, logger, sizeEnable, cover);
    }

    public DBMapSetSuit(DBASet databaseSet, DB database, Logger logger, boolean sizeEnable) {
        this(databaseSet, database, logger, sizeEnable, null);
    }

    public DBMapSetSuit(DBASet databaseSet, DB database, Logger logger) {
        this(databaseSet, database, logger, false, null);
    }

    @Override
    public Object getSource() {
        return set;
    }

    @Override
    public IteratorCloseable<T> getIterator() {
        this.addUses();
        try {

            Iterator<T> u = set.iterator();

            return IteratorCloseableImpl.make(u);
        } finally {
            this.outUses();
        }

    }

    @Override
    public IteratorCloseable<T> getDescendingIterator() {
        this.addUses();
        try {

            Iterator<T> u = ((NavigableSet) set).descendingIterator();

            return IteratorCloseableImpl.make(u);
        } finally {
            this.outUses();
        }

    }

    public IteratorCloseable<T> getIterator(T fromKey, boolean descending) {
        this.addUses();

        try {
            if (descending) {
                return
                        // делаем закрываемый Итератор
                        IteratorCloseableImpl.make(
                                // берем индекс с обратным отсчетом
                                ((NavigableSet) set).descendingSet()
                                        // задаем границы, так как он обратный границы меняем местами
                                        .subSet(fromKey == null || fromKey.equals(LO) ? HI : fromKey,
                                                LO).iterator());
            }

            return
                    // делаем закрываемый Итератор
                    IteratorCloseableImpl.make(
                            ((NavigableSet) set)
                                    // задаем границы, так как он обратный границы меняем местами
                                    .subSet(fromKey == null ? LO : fromKey,
                                            HI).iterator());

        } finally {
            this.outUses();
        }


    }


    public int size() {

        if (!sizeEnable)
            return -1;

        this.addUses();
        try {
            int u = set.size();
            return u;
        } finally {
            this.outUses();
        }
    }

    @Override
    public boolean set(T key, Boolean value) {
        this.addUses();
        try {

            return this.set.add(key);
        } finally {
            this.outUses();
        }
    }

    @Override
    public void put(T key, Boolean value) {
        set(key, null);
    }

    @Override
    public Boolean remove(T key) {

        this.addUses();
        try {

            //REMOVE
            if (this.set.contains(key)) {
                return this.set.remove(key);
            }

            return null;
        } finally {
            this.outUses();
        }

    }

    @Override
    public Boolean removeValue(T key) {
        return null;
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Удалить
     *
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
            set.remove(key);
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
            set.remove(key);
        } finally {
            this.outUses();
        }
    }


    @Override
    public boolean contains(T key) {

        this.addUses();
        try {

            if (set.contains(key)) {
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
            set.clear();

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
        set = null;
    }

    @Override
    public boolean isClosed() {
        return database.getEngine().isClosed();
    }

    @Override
    public void commit() {
    }

    @Override
    public void rollback() {
    }

    @Override
    public void afterRollback() {
    }

}
