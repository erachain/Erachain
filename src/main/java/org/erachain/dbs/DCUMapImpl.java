package org.erachain.dbs;

import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Тут старый вариант от MapDB - и как форкнутая и нет может быть.
 * Форкнутая - по getForkedMap()
 * @param <T>
 * @param <U>
 */
public abstract class DCUMapImpl<T, U> extends DBTabCommonImpl<T, U> implements DBTab<T, U> {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    protected Map<T, U> map;
    protected DBTab<T, U> parent;
    protected Map<Integer, NavigableSet<Fun.Tuple2<?, T>>> indexes = new HashMap<Integer, NavigableSet<Fun.Tuple2<?, T>>>();

    //ConcurrentHashMap deleted;
    protected HashMap deleted;
    protected Boolean EXIST = true;
    protected int shiftSize;

    int uses = 0;

    public DCUMapImpl(DBASet databaseSet) {
        super(databaseSet);
    }

    public DCUMapImpl(DBASet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public DCUMapImpl(DBTab<T, U> parent, DBASet dcSet) {
        super(parent, dcSet);

        if (false && Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()) {
            // System.out.println("########################### Free Memory:"
            // + Runtime.getRuntime().freeMemory());
            if (Runtime.getRuntime().freeMemory() < Controller.MIN_MEMORY_TAIL) {
                System.gc();
                if (Runtime.getRuntime().freeMemory() < Controller.MIN_MEMORY_TAIL >> 1)
                    Controller.getInstance().stopAll(97);
            }
        }

        this.parent = parent;

        // OPEN MAP
        if (parent != null || database == null) {
            this.getMemoryMap();
        } else {
            this.getMap();
        }
    }

    protected abstract void getMap();
    protected abstract void getMemoryMap();
    protected abstract U getDefaultValue();

    protected void createIndexes() {
    }

    public Map getMapSuit() {
        return map;
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
    protected <V> void createIndex(int index, NavigableSet<?> indexSet, NavigableSet<?> descendingIndexSet, Fun.Function2<V, T, U> function) {
        assert(index > 0 && index < DESCENDING_SHIFT_INDEX);

        Bind.secondaryKey((Bind.MapWithModificationListener<T, U>) this.map, (NavigableSet<Fun.Tuple2<V, T>>) indexSet, function);
        this.indexes.put(index, (NavigableSet<Fun.Tuple2<?, T>>) indexSet);

        Bind.secondaryKey((Bind.MapWithModificationListener<T, U>) this.map, (NavigableSet<Fun.Tuple2<V, T>>) descendingIndexSet, function);
        this.indexes.put(index + DESCENDING_SHIFT_INDEX, (NavigableSet<Fun.Tuple2<?, T>>) descendingIndexSet);
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
    protected <V> void createIndexes(int index, NavigableSet<?> indexSet, NavigableSet<?> descendingIndexSet, Fun.Function2<V[], T, U> function) {
        assert(index > 0 && index < DESCENDING_SHIFT_INDEX);
        Bind.secondaryKeys((BTreeMap<T, U>) this.map, (NavigableSet<Fun.Tuple2<V, T>>) indexSet, function);
        this.indexes.put(index, (NavigableSet<Fun.Tuple2<?, T>>) indexSet);

        Bind.secondaryKeys((BTreeMap<T, U>) this.map, (NavigableSet<Fun.Tuple2<V, T>>) descendingIndexSet, function);
        this.indexes.put(index + DESCENDING_SHIFT_INDEX, (NavigableSet<Fun.Tuple2<?, T>>) descendingIndexSet);
    }

    //@Override
    public NavigableSet<Fun.Tuple2<?, T>> getIndex(int index, boolean descending) {

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
        NavigableSet<Fun.Tuple2<?, T>> indexSet = getIndex(index, descending);
        if (indexSet != null) {

            org.erachain.datachain.IndexIterator<T> u = new org.erachain.datachain.IndexIterator<T>(this.indexes.get(index));
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
        SortableList<T, U> list;
        if (this.size() < 1000) {
            list = new SortableList<T, U>(this);
        } else {
            // обрезаем полный список в базе до 1000
            list = SortableList.makeSortableList(this, false, 1000);
        }

        return list;
    }

    // ERROR if key is not unique for each value:
    // After removing the key from the fork, which is in the parent, an incorrect post occurs
    //since from.deleted the key is removed and there is no parent in the parent and that
    // the deleted ones are smaller and the size is increased by 1
    @Override
    public int size() {
        this.addUses();

        int u = this.map.size();

        if (this.parent != null) {
            if (this.deleted != null)
                u -= this.deleted.size();

            u -= this.shiftSize;
            u += this.parent.size();
        }

        this.outUses();
        return u;
    }

    @Override
    public U get(T key) {

        if (DCSet.isStoped()) {
            return null;
        }

        this.addUses();

        try {
            U u = this.map.get(key);
            if (u != null) {
                this.outUses();
                return u;
            }

            if (parent != null) {
                if (this.deleted == null || !this.deleted.containsKey(key)) {
                    u = this.parent.get(key);
                    this.outUses();
                    return u;
                }
            }

            u = this.getDefaultValue();
            this.outUses();
            return u;
        } catch (Exception e) {

            U u = this.getDefaultValue();
            this.outUses();
            return u;
        }
    }

    @Override
    public Set<T> keySet() {

        this.addUses();
        Set<T> u = this.map.keySet();

        if (this.parent != null)
            u.addAll(this.parent.keySet());

        this.outUses();
        return u;
    }

    @Override
    public Collection<U> values() {
        this.addUses();
        Collection<U> u = this.map.values();

        if (this.parent != null)
            u.addAll(this.parent.values());

        this.outUses();
        return u;
    }

    @Override
    public boolean set(T key, U value) {
        if (DCSet.isStoped()) {
            return false;
        }

        this.addUses();

        try {

            U old;
            if (this.parent != null) {
                // найдем и в Родительских тоже
                old = get(key);
                this.map.put(key, value);
            } else {
                old = this.map.put(key, value);
            }

            if (this.parent != null) {
                //if (old != null)
                //	++this.shiftSize;
                if (this.deleted != null) {
                    if (this.deleted.remove(key) != null)
                        ++this.shiftSize;
                }
            } else {

                // NOTIFY if not FORKED
                if (this.observableData != null && (old == null || !old.equals(value))) {
                    if (this.observableData.containsKey(DBTab.NOTIFY_ADD) && !DCSet.isStoped()) {
                        this.setChanged();
                        Integer observeItem = this.observableData.get(DBTab.NOTIFY_ADD);
                        if (
                                observeItem.equals(ObserverMessage.ADD_UNC_TRANSACTION_TYPE)
                                        || observeItem.equals(ObserverMessage.WALLET_ADD_ORDER_TYPE)
                                        || observeItem.equals(ObserverMessage.ADD_PERSON_STATUS_TYPE)
                                        || observeItem.equals(ObserverMessage.REMOVE_PERSON_STATUS_TYPE)
                        ) {
                            this.notifyObservers(new ObserverMessage(observeItem, new Pair<T, U>(key, value)));
                        } else {
                            this.notifyObservers(
                                    new ObserverMessage(observeItem, value));
                        }
                    }
                }
            }

            this.outUses();
            return old != null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        this.outUses();
        return false;
    }

    @Override
    public void put(T key, U value) {
        set(key, value);
    }

    @Override
    public U remove(T key) {

        if (DCSet.isStoped()) {
            return null;
        }

        this.addUses();
        U value = null;

        value = this.map.remove(key);

        if (this.parent != null) {
            // это форкнутая таблица

            if (this.deleted == null) {
                this.deleted = new HashMap(1024 , 0.75f);
            }

            // добавляем в любом случае, так как
            // Если это был ордер или еще что, что подлежит обновлению в форкнутой базе
            // и это есть в основной базе, то в воркнутую будет помещена так же запись.
            // Получаем что запись есть и в Родителе и в Форкнутой таблице!
            // Поэтому если мы тут удалили то должны добавить что удалили - в deleted
            this.deleted.put(key, EXIST);

            if (value == null) {
                // если тут нету то создадим пометку что удалили
                value = this.parent.get(key);
            }

            this.outUses();
            return value;

        } else {

            // NOTIFY
            if (this.observableData != null) {
                if (this.observableData.containsKey(DBTab.NOTIFY_REMOVE)) {
                    this.setChanged();
                    Integer observItem = this.observableData.get(DBTab.NOTIFY_REMOVE);
                    if (
                            observItem.equals(ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE)
                                    || observItem.equals(ObserverMessage.WALLET_REMOVE_ORDER_TYPE)
                                    || observItem.equals(ObserverMessage.REMOVE_AT_TX)
                    ) {
                        this.notifyObservers(new ObserverMessage(observItem, new Pair<T, U>(key, value)));
                    } else {
                        this.notifyObservers(new ObserverMessage(observItem, value));
                    }
                }
            }
        }

        this.outUses();
        return value;

    }

    @Override
    public U removeValue(T key) {
        return remove(key);
    }

    @Override
    public void delete(T key) {
        remove(key);
    }

    @Override
    public void deleteValue(T key) {
        remove(key);
    }

    @Override
    public boolean contains(T key) {

        if (DCSet.isStoped()) {
            return false;
        }

        this.addUses();

        if (this.map.containsKey(key)) {
            this.outUses();
            return true;
        } else {
            if (this.deleted == null || !this.deleted.containsKey(key)) {
                if (this.parent != null) {
                    boolean u = this.parent.contains(key);

                    this.outUses();
                    return u;
                }
            }
        }

        this.outUses();
        return false;
    }

    public void addObserver(Observer o) {

        // NOT for FORK
        if (this.parent != null)
            return;

        super.addObserver(o);
    }

    public void addUses() {
        if (database != null) {
            uses++;
        }
    }

    public void outUses() {
        if (database != null) {
            uses--;
        }
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Сбросить
     */
    @Override
    public void clear() {
        //RESET MAP
        if (this.database.getEngine().isClosed())
            return;

        this.map.clear();

        // NOTYFIES
        if (this.observableData != null) {
            //NOTIFY LIST
            if (this.observableData.containsKey(NOTIFY_RESET)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_RESET), this));
            }

        }
    }

    @Override
    public void commit() {}

    @Override
    public void rollback() {}

    @Override
    public void close() {}

    @Override
    public String toString() {
        if (parent == null)  {
            return getClass().getName();
        }
        return getClass().getName() + ".FORK";
    }
}
