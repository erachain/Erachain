package org.erachain.dbs.mapDB;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.*;
import org.mapdb.Fun;
import org.slf4j.Logger;

import java.util.*;

/**
 * Оболочка для Карты от конкретной СУБД чтобы эту оболочку вставлять в Таблицу, которая форкнута (см. fork()).
 * Тут всегда должен быть задан Родитель. Здесь другой порядок обработки данных в СУБД
 * @param <T>
 * @param <U>
<br><br>
ВНИМАНИЕ !!! Вторичные ключи не хранят дубли - тоесть запись во втричном ключе не будет учтена иперезапишется если такой же ключ прийдет
Поэтому нужно добавлять униальность

 */
public abstract class DBMapSuitFork<T, U> extends DBMapSuit<T, U> implements ForkedMap {

    @Getter
    protected DBTab<T, U> parent;
    Comparator<? super T> COMPARATOR = Fun.COMPARATOR;

    //ConcurrentHashMap deleted;
    ///////// - если ключи набор байт или других примитивов - то неверный поиск в этом виде таблиц HashMap deleted;
    /// поэтому берем медленный но правильный TreeMap
    /// НЕТ - это определяется на лету при созданий - по типу ключа
    Map<T, Boolean> deleted;
    int shiftSize;

    public DBMapSuitFork(DBTab parent, DBASet dcSet, Logger logger, boolean sizeEnable, DBTab cover) {
        assert (parent != null);

        this.databaseSet = dcSet;
        this.database = dcSet.database;
        this.logger = logger;
        this.cover = cover;
        this.sizeEnable = sizeEnable;

        if (Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()) {
            // System.out.println("########################### Free Memory:"
            // + Runtime.getRuntime().freeMemory());
            if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() >> 10)
                    + Controller.MIN_MEMORY_TAIL) {
                // у родителя чистим - у себя нет, так как только создали
                ((DCSet) parent.getDBSet()).clearCache();
                System.gc();
                if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() >> 10)
                        + (Controller.MIN_MEMORY_TAIL << 1)) {
                    logger.error("Heap Memory Overflow");
                    Controller.getInstance().stopAndExit(1011);
                }
            }
        }

        this.parent = parent;

        this.openMap();

    }

    public DBMapSuitFork(DBTab parent, DBASet dcSet, Logger logger, DBTab cover) {
        this(parent, dcSet, logger, false, cover);
    }

    public DBMapSuitFork(DBTab parent, DBASet dcSet, Logger logger) {
        this(parent, dcSet, logger, false, null);
    }


    // ERROR if key is not unique for each value:
    // After removing the key from the fork, which is in the parent, an incorrect post occurs
    //since from.deleted the key is removed and there is no parent in the parent and that
    // the deleted ones are smaller and the size is increased by 1
    @Override
    public int size() {

        int u = this.map.size();

        if (this.deleted != null)
            u -= this.deleted.size();

        u -= this.shiftSize;
        u += this.parent.size();

        return u;
    }

    public void makeDeletedMap(T key) {
        if (key instanceof byte[]) {
            this.deleted = new TreeMap(Fun.BYTE_ARRAY_COMPARATOR);
        } else {
            this.deleted = new HashMap(1024, 0.75f);
        }
    }

    @Override
    public U get(T key) {

        try {
            U u = this.map.get(key);
            if (u != null) {
                return u;
            }

            if (this.deleted == null || !this.deleted.containsKey(key)) {
                u = this.parent.get(key);
                return u;
            }

            u = this.getDefaultValue(key);
            return u;
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            U u = this.getDefaultValue(key);
            return u;
        }
    }

    @Override
    public Set<T> keySet() {
        // тут обработка удаленных еще нужна
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<U> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean set(T key, U value) {

        try {

            // сначала проверим - есть ли он тут включая родителя
            boolean exist = this.contains(key);

            this.map.put(key, value);

            if (this.deleted != null) {
                if (this.deleted.remove(key) != null)
                    ++this.shiftSize;
            }

            return exist;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return false;
    }

    @Override
    public void put(T key, U value) {

        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.set, так как
        /// если в подклассе будет из SET вызов PUT то он придет сюда и при перевузове THIS.SET отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> set(key, value);
        ///

        try {

            this.map.put(key, value);

            if (this.deleted != null) {
                if (this.deleted.remove(key) != null)
                    ++this.shiftSize;
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    @Override
    public U remove(T key) {

        U value = this.map.remove(key);

        if (this.deleted == null) {
            makeDeletedMap(key);
        }

        if (value == null && !this.deleted.containsKey(key)) {
            // если тут нету то попобуем в Родителе найти
            value = this.parent.get(key);
        }

        // добавляем в любом случае, так как
        // Если это был ордер или еще что, что подлежит обновлению в форкнутой базе
        // и это есть в основной базе, то в воркнутую будет помещена так же запись.
        // Получаем что запись есть и в Родителе и в Форкнутой таблице!
        // Поэтому если мы тут удалили то должны добавить что удалили - в deleted
        this.deleted.put(key, EXIST);

        return value;

    }

    @Override
    public U removeValue(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///

        U value = this.map.remove(key);

        if (this.deleted == null) {
            makeDeletedMap(key);
        }

        if (value == null && !this.deleted.containsKey(key)) {
            // если тут нету то попобуем в Родителе найти
            value = this.parent.get(key);
        }

        // добавляем в любом случае, так как
        // Если это был ордер или еще что, что подлежит обновлению в форкнутой базе
        // и это есть в основной базе, то в воркнутую будет помещена так же запись.
        // Получаем что запись есть и в Родителе и в Форкнутой таблице!
        // Поэтому если мы тут удалили то должны добавить что удалили - в deleted
        this.deleted.put(key, EXIST);

        return value;

    }

    @Override
    public void delete(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///

        this.map.remove(key);

        if (this.deleted == null) {
            makeDeletedMap(key);
        }

        // добавляем в любом случае, так как
        // Если это был ордер или еще что, что подлежит обновлению в форкнутой базе
        // и это есть в основной базе, то в воркнутую будет помещена так же запись.
        // Получаем что запись есть и в Родителе и в Форкнутой таблице!
        // Поэтому если мы тут удалили то должны добавить что удалили - в deleted
        this.deleted.put(key, EXIST);

    }

    @Override
    public void deleteValue(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///

        this.map.remove(key);

        if (this.deleted == null) {
            makeDeletedMap(key);
        }

        // добавляем в любом случае, так как
        // Если это был ордер или еще что, что подлежит обновлению в форкнутой базе
        // и это есть в основной базе, то в воркнутую будет помещена так же запись.
        // Получаем что запись есть и в Родителе и в Форкнутой таблице!
        // Поэтому если мы тут удалили то должны добавить что удалили - в deleted
        this.deleted.put(key, EXIST);

    }

    @Override
    public boolean contains(T key) {

        if (this.map.containsKey(key)) {
            return true;
        } else {
            if (this.deleted == null || !this.deleted.containsKey(key)) {
                boolean u = this.parent.contains(key);

                return u;
            }
        }

        return false;
    }

    @Override
    public IteratorCloseable<T> getIterator() {
        this.addUses();
        try {
            return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                    new IteratorParent(parent.getIterator(), deleted),
                    map.keySet().iterator()), COMPARATOR, false);

        } finally {
            this.outUses();
        }

    }

    @Override
    public IteratorCloseable<T> getDescendingIterator() {
        this.addUses();
        try {
            return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                    new IteratorParent(parent.getDescendingIterator(), deleted),
                    ((NavigableMap) map).descendingMap().keySet().iterator()), COMPARATOR, true);

        } finally {
            this.outUses();
        }

    }

    @Override
    public IteratorCloseable<T> getIterator(T fromKey, T toKey, boolean descending) {
        this.addUses();

        try {

            IteratorCloseable<T> iterator;
            if (descending) {
                iterator =
                        // делаем закрываемый Итератор
                        IteratorCloseableImpl.make(
                                // берем индекс с обратным отсчетом
                                ((NavigableMap) this.map).descendingMap()
                                        // задаем границы, так как он обратный границы меняем местами
                                        .subMap(fromKey == null || fromKey.equals(LO) ? HI : fromKey,
                                                toKey == null ? LO : toKey).keySet().iterator());
            } else {

                iterator =
                        // делаем закрываемый Итератор
                        IteratorCloseableImpl.make(
                                ((NavigableMap) this.map)
                                        // задаем границы, так как он обратный границы меняем местами
                                        .subMap(fromKey == null ? LO : fromKey,
                                                toKey == null ? HI : toKey).keySet().iterator());
            }

            return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                    new IteratorParent(parent.getIterator(fromKey, toKey, descending), deleted),
                    iterator), COMPARATOR, descending);

        } finally {
            this.outUses();
        }

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
                new IteratorParent(parentIterator, deleted), iterator),
                Fun.COMPARATOR, descending);

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
                parent.getSuit().delete(iteratorDeleted.next());
                updated = true;
            }
            deleted = null;
        }

        // теперь внести новые

        Iterator<T> iterator = this.map.keySet().iterator();

        while (iterator.hasNext()) {
            T key = iterator.next();
            parent.getSuit().put(key, this.map.get(key));
            updated = true;
        }

        return updated;
    }

    @Override
    public void close() {
        // у всей базы чистится parent.clearCache();
        parent = null;
        deleted = null;
        super.close();
    }

    @Override
    public String toString() {
        return getClass().getName() + ".FORK";
    }

}