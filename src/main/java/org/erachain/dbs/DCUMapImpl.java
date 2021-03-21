package org.erachain.dbs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import lombok.Getter;
import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Тут старый вариант от MapDB - и как форкнутая и нет может быть.
 * Форкнутая - по getForkedMap()
 * @param <T>
 * @param <U>
 */
public abstract class DCUMapImpl<T, U> extends DBTabImpl<T, U> implements ForkedMap {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    protected Map<T, U> map;
    @Getter
    protected DCUMapImpl<T, U> parent;
    protected Map<Integer, NavigableSet<Fun.Tuple2<?, T>>> indexes = new HashMap<Integer, NavigableSet<Fun.Tuple2<?, T>>>();

    //protected ConcurrentHashMap deleted;
    protected Map deleted;
    protected Boolean EXIST = true;
    protected int shiftSize;

    int uses = 0;

    public DCUMapImpl(DBASet databaseSet) {
        super(databaseSet);
        createIndexes();
    }

    public DCUMapImpl(DBASet databaseSet, DB database, String tabName, Serializer tabSerializer, boolean sizeEnable) {
        super(databaseSet, database, tabName, tabSerializer, sizeEnable);
        createIndexes();
    }

    public DCUMapImpl(DBASet databaseSet, DB database, boolean sizeEnable) {
        super(databaseSet, database, sizeEnable);
        createIndexes();
    }
    public DCUMapImpl(DBASet databaseSet, DB database) {
        super(databaseSet, database, false);
        createIndexes();

    }

    public DCUMapImpl(DCUMapImpl<T, U> parent, DBASet dcSet, boolean sizeEnable) {
        super(parent, dcSet, sizeEnable);

        if (Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()) {
            // System.out.println("########################### Free Memory:"
            // + Runtime.getRuntime().freeMemory());
            if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() >> 10)
                    + (Controller.MIN_MEMORY_TAIL)) {
                // у родителя чистим - у себя нет, так как только создали
                ((DBASet) parent.getDBSet()).clearCache();
                System.gc();
                if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() >> 10)
                        + (Controller.MIN_MEMORY_TAIL << 1)) {
                    LOGGER.error("Heap Memory Overflow");
                    Controller.getInstance().stopAll(1192);
                }
            }
        }

        this.parent = parent;

        // OPEN MAP
        if (parent != null || database == null) {
            this.getMemoryMap();
        } else {
            this.openMap();
            createIndexes();
        }

    }

    public DCUMapImpl(DCUMapImpl<T, U> parent, DBASet dcSet) {
        this(parent, dcSet, false);
    }

    public abstract void openMap();
    protected abstract void getMemoryMap();

    protected void createIndexes() {
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

    // TODO: сделать два итератора и удаленные чтобы без создания новых списков работало иначе сломшком большой LIST делается
    @Override
    public IteratorCloseable<T> getIterator() {
        this.addUses();

        try {
            if (parent == null) {
                return new IteratorCloseableImpl(map.keySet().iterator());
            }

            List<T> list = new ArrayList<>();
            Iterator<T> parentIterator = parent.getIterator();
            while (parentIterator.hasNext()) {
                T key = parentIterator.next();
                // пропустим если он есть в удаленных
                if (deleted != null && deleted.containsKey(key)
                        || map.containsKey(key))
                    continue;
                list.add(key);
            }

            /// тут нет дублей они уже удалены и дубли не взяты
            /// return new MergedIteratorNoDuplicates((Iterable) ImmutableList.of(list.iterator(), map.keySet().iterator()), Fun.COMPARATOR);
            return new IteratorCloseableImpl(Iterators.mergeSorted((Iterable) ImmutableList.of(list.iterator(), map.keySet().iterator()), Fun.COMPARATOR));

        } finally {
            this.outUses();
        }
    }

    public IteratorCloseable<T> getDescendingIterator() {
        this.addUses();

        try {
            if (parent == null) {
                if (map instanceof NavigableMap) {
                    return new IteratorCloseableImpl(((NavigableMap) map).descendingMap().keySet().iterator());
                } else {
                    return null;
                }
            }

            List<T> list = new ArrayList<>();
            Iterator<T> parentIterator = parent.getDescendingIterator();
            while (parentIterator.hasNext()) {
                T key = parentIterator.next();
                // пропустим если он есть в удаленных
                if (deleted != null && deleted.containsKey(key)
                        || map.containsKey(key))
                    continue;
                list.add(key);
            }

            /// тут нет дублей они уже удалены и дубли не взяты
            /// return new MergedIteratorNoDuplicates((Iterable) ImmutableList.of(list.iterator(), map.keySet().iterator()), Fun.COMPARATOR);
            return new IteratorCloseableImpl(Iterators.mergeSorted((Iterable) ImmutableList.of(list.iterator(),
                    ((NavigableMap) map).descendingMap().keySet().iterator()), Fun.COMPARATOR));

        } finally {
            this.outUses();
        }

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

    // TODO: сделать два итератора и удаленные чтобы без создания новых списков работало

    /**
     * @param index      <b>primary Index = 0</b>, secondary index = 1...10000
     * @param descending true if need descending sort
     * @return
     */
    @Override
    public IteratorCloseable<T> getIterator(int index, boolean descending) {
        this.addUses();

        // 0 - это главный индекс - он не в списке indexes
        NavigableSet<Fun.Tuple2<?, T>> indexSet = getIndex(index, descending);
        if (indexSet != null) {

            org.erachain.datachain.IndexIterator<T> u = new org.erachain.datachain.IndexIterator<T>(indexSet);
            this.outUses();
            return u;

        } else {
            if (descending) {
                Iterator<T> u = ((NavigableMap<T, U>) this.map).descendingKeySet().iterator();
                this.outUses();
                return new IteratorCloseableImpl(u);
            }

            Iterator<T> u = ((NavigableMap<T, U>) this.map).keySet().iterator();
            this.outUses();
            return new IteratorCloseableImpl(u);

        }
    }

    public void makeDeletedMap(T key) {
        if (key instanceof byte[]) {
            this.deleted = new TreeMap(Fun.BYTE_ARRAY_COMPARATOR);
        } else {
            this.deleted = new HashMap(1024, 0.75f);
        }
    }

    // ERROR if key is not unique for each value:
    // After removing the key from the fork, which is in the parent, an incorrect post occurs
    //since from.deleted the key is removed and there is no parent in the parent and that
    // the deleted ones are smaller and the size is increased by 1
    @Override
    public int size() {

        if (!sizeEnable)
            return -1;

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

        try {
            if (this.parent == null) {
                return u;
            }

            Set<T> combinedKeys = parent.keySet();
            if (deleted != null && !deleted.isEmpty()) {
                // что удалено тут удалим у родителя
                combinedKeys.removeAll(deleted.keySet());
            }

            // тут просто добвим - в карте дублирующие ключ схлопнутся
            combinedKeys.addAll(u);
            return combinedKeys;

        } finally {
            this.outUses();
        }
    }

    /**
     * Так как в форкнутой таблице могут быть измененые записи то их значения сдублируются тут.
     * Поэтому чтобы такого не было делаем по ключам и сборке списка из значений - это дольше будет работать
     *
     * @return
     */
    @Override
    public Collection<U> values() {
        this.addUses();

        try {
            if (this.parent == null) {
                return this.map.values();
            } else {
                Collection<U> u = new ArrayList<>();
                for (T key: this.keySet()) {
                    u.add(get(key));
                }
                return u;
            }

        } finally {
            this.outUses();
        }
    }

    private boolean setLocal(T key, U value) {
        if (DCSet.isStoped()) {
            return false;
        }

        this.addUses();

        try {

            U old = this.map.put(key, value);

            if (this.parent != null) {
                if (this.deleted != null) {
                    if (this.deleted.remove(key) != null) {
                    }
                }
                if (sizeEnable &&
                        old == null // если еще не было тут значения
                        && this.parent.contains(key) // и такой ключ есть в родителе
                ) {
                    // нужно учесть сдвиг
                    ++this.shiftSize;
                }
            } else {

                // NOTIFY if not FORKED
                if (this.observableData != null && (old == null || !old.equals(value))) {
                    if (this.observableData.containsKey(NOTIFY_ADD) && !DCSet.isStoped()) {
                        this.setChanged();
                        Integer observeItem = this.observableData.get(NOTIFY_ADD);
                        if (
                                observeItem.equals(ObserverMessage.ADD_UNC_TRANSACTION_TYPE)
                                        || observeItem.equals(ObserverMessage.WALLET_ADD_ORDER_TYPE)
                                        || observeItem.equals(ObserverMessage.ADD_PERSON_STATUS_TYPE)
                                        || observeItem.equals(ObserverMessage.REMOVE_PERSON_STATUS_TYPE)
                                        || observeItem.equals(ObserverMessage.WALLET_ACCOUNT_FAVORITE_ADD)

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
    public boolean set(T key, U value) {
        return setLocal(key, value);
    }

    /**
     * @param key
     * @param value
     */
    @Override
    public void put(T key, U value) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.set, так как
        /// если в подклассе будет из SET вызов PUT то он придет сюда и при перевузове THIS.SET отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> set(key, value);
        ///
        setLocal(key, value);

    }

    /**
     * чтобы не копировать код
     * @param key
     * @return
     */
    private U removeHere(T key) {
        if (DCSet.isStoped()) {
            return null;
        }

        this.addUses();
        U value = null;

        value = this.map.remove(key);

        if (this.parent != null) {
            // это форкнутая таблица

            if (this.deleted == null) {
                makeDeletedMap(key);
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
            } else {
                if (sizeEnable
                        && this.parent.contains(key)) {
                    // в родителе есть такой ключ - и тут было значение, значит уменьшим сдвиг
                    --this.shiftSize;
                }
            }

            this.outUses();
            return value;

        } else {

            // NOTIFY
            if (this.observableData != null) {
                if (this.observableData.containsKey(NOTIFY_REMOVE)) {
                    this.setChanged();
                    Integer observItem = this.observableData.get(NOTIFY_REMOVE);
                    if (
                            observItem.equals(ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE)
                                    || observItem.equals(ObserverMessage.WALLET_REMOVE_ORDER_TYPE)
                                    || observItem.equals(ObserverMessage.REMOVE_AT_TX)
                                    || observItem.equals(ObserverMessage.WALLET_ACCOUNT_FAVORITE_DELETE)
                                    || observItem.equals(ObserverMessage.WALLET_ACCOUNT_FAVORITE_RESET)
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
    public U remove(T key) {
        return removeHere(key);
    }

    @Override
    public U removeValue(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///
        return removeHere(key);
    }

    /**
     * Чтобы не копировать вод из delete deleteValue
     * @param key
     */
    private void deleteHere(T key) {
        removeHere(key);
    }

    @Override
    public void delete(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///

        deleteHere(key);
    }

    @Override
    public void deleteValue(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///
        deleteHere(key);

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

    /**
     * ВНИМАНИЕ!!! в связи с работой этого метода при сливе - нельяза в стандартных методах
     * @return
     */
    @Override
    public boolean writeToParent() {

        boolean updated = false;

        // сперва нужно удалить старые значения
        // см issues/1276
        if (deleted != null) {
            Iterator<T> iteratorDeleted = this.deleted.keySet().iterator();
            while (iteratorDeleted.hasNext()) {
                T key = iteratorDeleted.next();
                parent.map.remove(key);
                updated = true;
            }
        }

        // и теперь уже сливаем новые

        Iterator<T> iterator = this.map.keySet().iterator();
        while (iterator.hasNext()) {
            T key = iterator.next();
            // напрямую в карту сливаем чтобы логику Таблицы не повторить дважды
            parent.map.put(key, this.map.get(key));
            updated = true;
        }

        return updated;
    }

    @Override
    public void commit() {}

    @Override
    public void rollback() {}

    @Override
    public void clearCache() {}

    @Override
    public void close() {
        map = null;
        parent = null;
        deleted = null;
        super.close();
    }

    @Override
    public boolean isClosed() {
        return database.getEngine().isClosed();
    }

    @Override
    public String toString() {
        if (parent == null)  {
            return getClass().getName();
        }
        return getClass().getName() + ".FORK";
    }
}
