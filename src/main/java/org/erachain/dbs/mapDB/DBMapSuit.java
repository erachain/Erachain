package org.erachain.dbs.mapDB;

import org.erachain.database.DBASet;
import org.erachain.datachain.IndexIterator;
import org.erachain.dbs.DBSuitImpl;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Function2;
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
    public Object getSource() {
        return map;
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

    @Override
    public IteratorCloseable<T> getIndexIterator(int index, boolean descending) {
        this.addUses();

        // 0 - это главный индекс - он не в списке indexes
        NavigableSet<Tuple2<?, T>> indexSet = getIndex(index, descending);
        if (indexSet != null) {

            IndexIterator<T> u = new org.erachain.datachain.IndexIterator<>(this.indexes.get(index));
            this.outUses();
            return u;

        } else {
            if (descending) {
                Iterator<T> u = ((NavigableMap<T, U>) this.map).descendingKeySet().iterator();
                this.outUses();
                return new IteratorCloseableImpl(u);
            }

            Iterator<T> u = this.map.keySet().iterator();
            this.outUses();
            return new IteratorCloseableImpl(u);

        }
    }

    @Override
    public IteratorCloseable<T> getIterator() {
        this.addUses();

        Iterator<T> u = map.keySet().iterator();

        this.outUses();
        return new IteratorCloseableImpl(u);

    }

    @Override
    public IteratorCloseable<T> getDescendingIterator() {
        this.addUses();

        Iterator<T> u = ((NavigableMap) map).descendingMap().keySet().iterator();

        this.outUses();
        return new IteratorCloseableImpl(u);

    }

    public IteratorCloseable<T> getIterator(T fromKey, boolean descending) {
        this.addUses();

        if (descending) {
            IteratorCloseable result =
                    // делаем закрываемый Итератор
                    IteratorCloseableImpl.make(
                            // берем индекс с обратным отсчетом
                            ((NavigableMap) this.map).descendingMap()
                                    // задаем границы, так как он обратный границы меняем местами
                                    .subMap(fromKey == null || fromKey.equals(0L) ? Long.MAX_VALUE : fromKey, 0L).keySet().iterator());
            return result;
        }

        IteratorCloseable result =
                // делаем закрываемый Итератор
                IteratorCloseableImpl.make(
                        ((NavigableMap) this.map)
                                // задаем границы, так как он обратный границы меняем местами
                                .subMap(fromKey == null || fromKey.equals(0L) ? 0L : fromKey,
                                        Long.MAX_VALUE).keySet().iterator());


        this.outUses();
        return new IteratorCloseableImpl(result);

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

            this.outUses();
            return this.getDefaultValue();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            this.outUses();
            return this.getDefaultValue();
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

    @Override
    public boolean set(T key, U value) {
        this.addUses();

        U old = this.map.put(key, value);

        this.outUses();

        return old != null;
    }

    @Override
    public void put(T key, U value) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.set, так как
        /// если в подклассе будет из SET вызов PUT то он придет сюда и при перевузове THIS.SET отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> set(key, value);
        ///
        this.addUses();

        this.map.put(key, value);

        this.outUses();
    }

    @Override
    public U remove(T key) {

        this.addUses();

        //REMOVE
        if (this.map.containsKey(key)) {
            U value = this.map.remove(key);
            this.outUses();
            return value;
        }

        this.outUses();
        return null;

    }

    @Override
    public U removeValue(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///

        this.addUses();

        //REMOVE
        if (this.map.containsKey(key)) {
            U value = this.map.remove(key);
            this.outUses();
            return value;
        }

        this.outUses();
        return null;
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
        this.map.remove(key);
        this.outUses();

    }

    @Override
    public void deleteValue(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///

        this.addUses();
        this.map.remove(key);
        this.outUses();
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
    public void clear() {

        if (this.database.getEngine().isClosed())
            return;

        this.addUses();

        //RESET MAP
        this.map.clear();

        //RESET INDEXES
        for (Set<Tuple2<?, T>> set : this.indexes.values()) {
            set.clear();
        }

        this.outUses();

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
