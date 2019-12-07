package org.erachain.dbs.rocksDB;

import lombok.Getter;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.ForkedMap;
import org.erachain.dbs.IteratorCloseable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collection;
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
        Set<T> u = map.keySet();
        u.addAll(parent.keySet());
        return u;
    }

    @Override
    public Collection<U> values() {
        Collection<U> u = map.values();
        u.addAll(parent.values());
        return u;
    }

    // TODO тут надо упростить так как внутри иногда берется предыдущее значение
    @Override
    public boolean set(T key, U value) {

        try {
            boolean old = map.containsKey(key);
            map.put(key, value);
            if (deleted != null) {
                if (deleted.remove(key) != null) {
                    shiftSize++;
                }
            }
            return old;
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

    // TODO сделать вызов из РоксДМ
    @Override
    public U removeValue(T key) {

        U value = this.map.get(key);
        this.map.deleteValue(key);

        if (this.deleted == null) {
            //this.deleted = new HashMap<T, U>(1024 , 0.75f);
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

        try (IteratorCloseable<T> iterator = this.getIterator()) {
            while (iterator.hasNext()) {
                T key = iterator.next();
                U item = this.map.get(key);
                if (item != null) {
                    parent.getSuit().put(key, this.map.get(key));
                    updated = true;
                }
            }

            if (deleted != null) {
                iterator = this.deleted.keySet().iterator();
                while (iterator.hasNext()) {
                    parent.getSuit().delete(iterator.next());
                    updated = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return updated;

    }

    @Override
    public String toString() {
        return getClass().getName() + ".FORK";
    }
}
