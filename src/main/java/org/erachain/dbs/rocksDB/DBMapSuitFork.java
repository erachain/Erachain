package org.erachain.dbs.rocksDB;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.*;
import org.mapdb.Fun;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 * Оболочка для Карты от конкретной СУБД чтобы эту оболочку вставлять в Таблицу, которая форкнута (см. fork()).
 * Тут всегда должен быть задан Родитель. Здесь другой порядок обработки данных в СУБД.
 * Так как тут есть слив в базу и WriteBatchIndexed - то не нужны всякие примочки с deleted
 * @param <T>
 * @param <U>
 */
public abstract class DBMapSuitFork<T, U> extends DBMapSuit<T, U> implements ForkedMap {

    @Getter
    protected DBTab<T, U> parent;

    //ConcurrentHashMap deleted;
    ///////// - если ключи набор байт или других примитивов - то неверный поиск в этом виде таблиц HashMap deleted;
    /// поэтому берем медленный но правильный TreeMap
    TreeMap<T, Boolean> deleted;
    Boolean EXIST = true;
    int shiftSize;

    public DBMapSuitFork(DBTab parent, DBASet dcSet, Logger logger, boolean enableSize, DBTab cover) {
        assert (parent != null);

        this.databaseSet = dcSet;
        this.database = dcSet.database;
        this.logger = logger;
        this.cover = cover;
        this.sizeEnable = enableSize;

        this.parent = parent;

        this.openMap();

    }


    @Override
    public int size() {
        int u = map.size();

        if (deleted != null) {
            u -= deleted.size();
        }

        u -= shiftSize;
        u += parent.size();
        return u;
    }

    @Override
    public U get(T key) {
        if (DCSet.isStoped()) {
            return null;
        }
        try {
            if (map.containsKey(key)) {
                return map.get(key);
            } else {
                if (deleted == null || !deleted.containsKey(key)) {
                    return parent.get(key);
                }
            }

            return getDefaultValue();
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return getDefaultValue();
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

    // TODO тут надо упростить так как внутри иногда берется предыдущее значение
    @Override
    public boolean set(T key, U value) {

        try {
            // сначала проверим - есть ли он тут включая родителя
            boolean exist = this.contains(key);

            map.put(key, value);
            if (deleted != null) {
                if (deleted.remove(key) != null) {
                    shiftSize++;
                }
            }
            return exist;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return false;
    }

    @Override
    public void put(T key, U value) {
        try {
            map.put(key, value);
            if (deleted != null) {
                if (deleted.remove(key) != null) {
                    shiftSize++;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    // TODO тут надо упростить так как внутри иногда берется предыдущее значение
    @Override
    public U remove(T key) {

        U value = this.map.get(key);
        this.map.delete(key);

        if (this.deleted == null) {
            //this.deleted = new HashMap<T, U>(1024 , 0.75f);
            this.deleted = new TreeMap<T, Boolean>();
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

    // TODO сделать вызов из РоксДМ
    @Override
    public U removeValue(T key) {

        U value = this.map.get(key);
        this.map.deleteValue(key);

        if (value == null && !this.deleted.containsKey(key)) {
            //this.deleted = new HashMap<T, U>(1024 , 0.75f);
            this.deleted = new TreeMap<T, Boolean>();
        }

        if (value == null) {
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

        this.map.delete(key);

        if (this.deleted == null) {
            //this.deleted = new HashMap(1024 , 0.75f);
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

        this.map.deleteValue(key);

        if (this.deleted == null) {
            //this.deleted = new HashMap(1024 , 0.75f);
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
        if (map.containsKey(key)) {
            return true;
        } else {
            if (deleted == null || !deleted.containsKey(key)) {
                return parent.contains(key);
            }
        }
        return false;
    }

    @Override
    public IteratorCloseable<T> getIndexIterator(int index, boolean descending) {

        Iterator<T> parentIterator = parent.getIndexIterator(index, descending);
        IteratorCloseable<T> iterator;

        if (index == 0) {
            // тут берем сами ключи у записей
            iterator = map.getIterator(descending, false);
        } else {
            // это вторичные индексы, потому как результат нужно взять не сами ключи а значения у записей
            iterator = map.getIndexIterator(index, descending, true);
        }

        IteratorCloseable iteratorMerged = new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted), iterator), Fun.COMPARATOR);

        return iteratorMerged;

    }

    @Override
    public IteratorCloseable<T> getIterator() {
        Iterator<T> parentIterator = parent.getIterator();
        IteratorCloseable<T> iterator = map.getIterator(false, false);

        IteratorCloseable iteratorMerged = new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted), iterator), Fun.COMPARATOR);

        return iteratorMerged;

    }

    /**
     * просто стедаем коммит и все
     * !!!!!!! нет нельзя так как в родиельской мапке тоже нету коммита еще и там свой WriteBatch есть и скорее всего
     * текущий Батник не сольется туда правильно ни как - надо все же организовывать новую базу тут и все из нее
     * сливать как и из МапДМФорк базы
     @Override public void writeToParent() {
     commit();
     }
      *
      * @return
     */

    @Override
    public boolean writeToParent() {

        boolean updated = false;

        // сперва нужно удалить старые значения
        // см issues/1276

        if (deleted != null) {
            // тут обычная карта в памяти -- ее не нужно особо закрывать
            Iterator deletedIterator = this.deleted.keySet().iterator();
            while (deletedIterator.hasNext()) {
                parent.getSuit().delete(deletedIterator.next());
                updated = true;
            }
            deleted = null;
        }

        // теперь внести новые

        /// обязательно нужно осовбождать память - см. тут
        /// https://github.com/facebook/rocksdb/wiki/RocksJava-Basics
        try (IteratorCloseable<T> iterator = this.getIterator()) {
            while (iterator.hasNext()) {
                T key = iterator.next();
                U item = this.map.get(key);
                if (item != null) {
                    parent.getSuit().put(key, this.map.get(key));
                    updated = true;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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
