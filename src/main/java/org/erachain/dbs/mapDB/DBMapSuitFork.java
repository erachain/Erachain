package org.erachain.dbs.mapDB;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.*;
import org.mapdb.Fun;
import org.slf4j.Logger;

import java.io.IOException;
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
            if (Runtime.getRuntime().freeMemory() < Controller.MIN_MEMORY_TAIL) {
                // у родителя чистим - у себя нет, так как только создали
                ((DCSet)parent.getDBSet()).clearCache();
                System.gc();
                if (Runtime.getRuntime().freeMemory() < Controller.MIN_MEMORY_TAIL) {
                    logger.error("Heap Memory Overflow");
                    Controller.getInstance().stopAll(1291);
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

            u = this.getDefaultValue();
            return u;
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            U u = this.getDefaultValue();
            return u;
        }
    }

    @Override
    public Set<T> keySet() {
        Set<T> u = this.map.keySet();

        u.addAll(this.parent.keySet());

        return u;
    }

    @Override
    public Collection<U> values() {
        Collection<U> u = this.map.values();

        u.addAll(this.parent.values());

        return u;
    }

    @Override
    public boolean set(T key, U value) {

        try {

            U old = this.map.put(key, value);

            if (this.deleted != null) {
                if (this.deleted.remove(key) != null)
                    ++this.shiftSize;
            }

            return old != null;

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
            if (key instanceof byte[]) {
                this.deleted = new TreeMap(Fun.BYTE_ARRAY_COMPARATOR);
            } else {
                this.deleted = new HashMap(1024, 0.75f);
            }
        }

        // добавляем в любом случае, так как
        // Если это был ордер или еще что, что подлежит обновлению в форкнутой базе
        // и это есть в основной базе, то в воркнутую будет помещена так же запись.
        // Получаем что запись есть и в Родителе и в Форкнутой таблице!
        // Поэтому если мы тут удалили то должны добавить что удалили - в deleted
        this.deleted.put(key, EXIST);

        if (value == null) {
            // если тут нету то попобуем в Родителе найти
            value = this.parent.get(key);
        }

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
            //this.deleted = new HashMap<T, Boolean>(1024 , 0.75f);
            this.deleted = new TreeMap<T, Boolean>();
        }

        // добавляем в любом случае, так как
        // Если это был ордер или еще что, что подлежит обновлению в форкнутой базе
        // и это есть в основной базе, то в воркнутую будет помещена так же запись.
        // Получаем что запись есть и в Родителе и в Форкнутой таблице!
        // Поэтому если мы тут удалили то должны добавить что удалили - в deleted
        this.deleted.put(key, EXIST);

        if (value == null) {
            // если тут нету то попобуем в Родителе найти
            value = this.parent.get(key);
        }

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
            //this.deleted = new HashMap<T, Boolean>(1024 , 0.75f);
            this.deleted = new TreeMap<T, Boolean>();
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
            //this.deleted = new HashMap<T, Boolean>(1024 , 0.75f);
            this.deleted = new TreeMap<T, Boolean>();
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

        List<T> list = new ArrayList<>();
        try (IteratorCloseable<T> parentIterator = parent.getIterator()) {
            while (parentIterator.hasNext()) {
                T key = parentIterator.next();
                // пропустим если он есть в удаленных
                if (deleted != null && deleted.containsKey(key)
                        || map.containsKey(key))
                    continue;
                list.add(key);
            }
        } catch (IOException e) {
        }

        //Map uncastedMap = this.map;
        Iterator<T> iterator = new MergedIteratorNoDuplicates((Iterable) ImmutableList.of(list.iterator(), map.keySet().iterator()), Fun.COMPARATOR);

        this.outUses();
        return new IteratorCloseableImpl(iterator);

    }

    // TODO надо рекурсию к Родителю по итератору делать
    @Override
    public IteratorCloseable<T> getIterator(int index, boolean descending) {
        this.addUses();

        List<T> list = new ArrayList<>();
        try (IteratorCloseable<T> parentIterator = parent.getIterator(index, descending)) {
            while (parentIterator.hasNext()) {
                T key = parentIterator.next();
                // пропустим если он есть в удаленных
                if (deleted != null && deleted.containsKey(key)
                        || map.containsKey(key))
                    continue;
                list.add(key);
            }
        } catch (IOException e) {
        }

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

        Iterator iteratorUncasted = new MergedIteratorNoDuplicates((Iterable) ImmutableList.of(list.iterator(), iterator), Fun.COMPARATOR);

        this.outUses();
        return (IteratorCloseable) iteratorUncasted;
    }

    @Override
    public boolean writeToParent() {

        boolean updated = false;

        Iterator<T> iterator = this.map.keySet().iterator();

        while (iterator.hasNext()) {
            T key = iterator.next();
            parent.getSuit().put(key, this.map.get(key));
            updated = true;
        }

        if (deleted != null) {
            Iterator iteratorDeleted = this.deleted.keySet().iterator();
            while (iteratorDeleted.hasNext()) {
                parent.getSuit().delete(iteratorDeleted.next());
                updated = true;
            }
        }

        return updated;
    }

    @Override
    public String toString() {
        return getClass().getName() + ".FORK";
    }

}