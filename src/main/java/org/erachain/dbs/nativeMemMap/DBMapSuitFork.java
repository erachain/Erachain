package org.erachain.dbs.nativeMemMap;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.*;
import org.erachain.dbs.mapDB.DBMapSuit;
import org.mapdb.Fun;

import java.util.*;

/**
 * Это только форкнутые таблицы
 * суперкласс для таблиц цепочки блоков с функционалом Форканья (см. fork()
 * @param <T>
 * @param <U>
<br><br>
ВНИМАНИЕ !!! Вторичные ключи не хранят дубли - тоесть запись во втричном ключе не будет учтена иперезапишется если такой же ключ прийдет
Поэтому нужно добавлять униальность

 */

@Slf4j
public abstract class DBMapSuitFork<T, U> extends DBMapSuit<T, U> implements ForkedMap {

    protected DBTab<T, U> parent;

    /**
     * пометка какие индексы не используются - отключим для ускорения
     */
    boolean OLD_USED_NOW = false;

    protected Comparator COMPARATOR;

    //ConcurrentHashMap deleted;
    Map<T, Boolean> deleted;
    Boolean EXIST = true;
    int shiftSize;

    public DBMapSuitFork(DBTab parent, DBASet dcSet, Comparator comparator, DBTab cover) {
        //this.logger = logger;
        this.databaseSet = dcSet;
        this.database = dcSet.database;
        this.cover = cover;

        if (Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()) {
            if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() >> 10)
                    + (Controller.MIN_MEMORY_TAIL)) {

                logger.debug("########################### Max=Total Memory [MB]:" + (Runtime.getRuntime().totalMemory() >> 20)
                        + " " + cover.getClass().getName());
                logger.debug("########################### Free Memory [MB]:" + (Runtime.getRuntime().freeMemory() >> 20)
                        + " " + cover.getClass().getName());

                // у родителя чистим - у себя нет, так как только создали
                ((DBASet) parent.getDBSet()).clearCache();
                System.gc();
                if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() >> 10)
                        + (Controller.MIN_MEMORY_TAIL << 1))
                    Controller.getInstance().stopAndExit(1021);
            }
        }

        this.parent = parent;

        COMPARATOR = comparator;
        if (COMPARATOR == null) {
            this.deleted = new HashMap(1024, 0.75f);
        } else {
            this.deleted = new TreeMap<T, Boolean>(COMPARATOR);
        }

        this.openMap();

    }

    public DBMapSuitFork(DBTab parent, DBASet dcSet) {
        this(parent, dcSet, null, null);
    }

    public Map getMap() {
        return map;
    }

    @Override
    public DBTab getParent() {
        return parent;
    }

    // ERROR if key is not unique for each value:
    // After removing the key from the fork, which is in the parent, an incorrect post occurs
    //since from.deleted the key is removed and there is no parent in the parent and that
    // the deleted ones are smaller and the size is increased by 1
    @Override
    public int size() {
        //this.addUses();

        int u = this.map.size();

        if (this.deleted != null)
            u -= this.deleted.size();

        u -= this.shiftSize;
        u += this.parent.size();

        //this.outUses();
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

            if (this.deleted == null || !this.deleted.containsKey(key)) {
                u = this.parent.get(key);
                this.outUses();
                return u;
            }

            u = this.getDefaultValue(key);
            this.outUses();
            return u;
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            U u = this.getDefaultValue(key);
            this.outUses();
            return u;
        }
    }

    @Override
    public Set<T> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<U> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean set(T key, U value) {
        if (DCSet.isStoped()) {
            return false;
        }

        this.addUses();

        try {

            // сначала проверим - есть ли он тут включая родителя
            boolean exist = this.contains(key);

            this.map.put(key, value);

            if (this.deleted != null) {
                if (this.deleted.remove(key) != null)
                    ++this.shiftSize;
            }

            this.outUses();
            return exist;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        this.outUses();
        return false;
    }

    @Override
    public void put(T key, U value) {
        if (DCSet.isStoped()) {
            return;
        }

        this.addUses();

        try {

            this.map.put(key, value);

            if (this.deleted != null) {
                if (this.deleted.remove(key) != null)
                    ++this.shiftSize;
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        this.outUses();
    }

    @Override
    public U remove(T key) {

        if (DCSet.isStoped()) {
            return null;
        }

        this.addUses();
        U value = null;

        value = this.map.remove(key);

        // это форкнутая таблица
        if (value == null && !this.deleted.containsKey(key)) {
            // если тут нету то создадим пометку что удалили
            value = this.parent.get(key);
        }

        // добавляем в любом случае, так как
        // Если это был ордер или еще что, что подлежит обновлению в форкнутой базе
        // и это есть в основной базе, то в воркнутую будет помещена так же запись.
        // Получаем что запись есть и в Родителе и в Форкнутой таблице!
        // Поэтому если мы тут удалили то должны добавить что удалили - в deleted
        this.deleted.put(key, EXIST);

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
        }

        if (this.deleted == null || !this.deleted.containsKey(key)) {
            boolean u = this.parent.contains(key);

            this.outUses();
            return u;
        }

        this.outUses();
        return false;
    }

    @Override
    public IteratorCloseable<T> getIterator() {
        this.addUses();

        Iterator<T> parentIterator = parent.getIterator();
        IteratorCloseable<T> iterator = new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted), map.keySet().iterator()), Fun.COMPARATOR);

        this.outUses();
        return iterator;

    }

    // TODO надо рекурсию к Родителю по итератору делать
    @Override
    public IteratorCloseable<T> getIndexIterator(int index, boolean descending) {
        this.addUses();

        Iterator<T> parentIterator = parent.getIndexIterator(index, descending);
        IteratorCloseable<T> iterator;

        if (index > 0) {
            // 0 - это главный индекс - он не в списке indexes
            NavigableSet<Fun.Tuple2<?, T>> indexSet = getIndex(index, descending);
            if (indexSet != null) {
                iterator = new org.erachain.datachain.IndexIterator<>(this.indexes.get(index));
            } else {
                if (descending) {
                    iterator = new IteratorCloseableImpl(((NavigableMap<T, U>) this.map).descendingKeySet().iterator());
                } else {
                    iterator = new IteratorCloseableImpl(this.map.keySet().iterator());
                }
            }
        } else {
            if (descending) {
                iterator = new IteratorCloseableImpl(((NavigableMap<T, U>) this.map).descendingKeySet().iterator());
            } else {
                iterator = new IteratorCloseableImpl(this.map.keySet().iterator());
            }
        }

        IteratorCloseable iteratorMerged = new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted), iterator), descending ? Fun.REVERSE_COMPARATOR : Fun.COMPARATOR);

        this.outUses();
        return iteratorMerged;
    }

    @Override
    public boolean writeToParent() {

        boolean updated = false;

        // сперва нужно удалить старые значения
        // см issues/1276
        if (deleted != null) {
            Iterator<T> iteratorDeleted = this.deleted.keySet().iterator();
            while (iteratorDeleted.hasNext()) {
                parent.delete(iteratorDeleted.next());
                updated = true;
            }
            deleted = null;
        }

        // теперь внести новые

        Iterator<T> iterator = this.map.keySet().iterator();
        while (iterator.hasNext()) {
            T key = iterator.next();
            parent.put(key, this.map.get(key));
            updated = true;
        }

        return updated;

    }

    @Override
    public void close() {
        parent = null;
        deleted = null;
        super.close();
    }

    @Override
    public String toString() {
        return getClass().getName() + ".FORK";
    }
}