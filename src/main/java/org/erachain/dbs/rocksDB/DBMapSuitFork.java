package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.DBTab;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * Оболочка для Карты от конкретной СУБД чтобы эту оболочку вставлять в Таблицу, которая форкнута (см. fork()).
 * Тут всегда должен быть задан Родитель. Здесь другой порядок обработки данных в СУБД
 * @param <T>
 * @param <U>
 */
@Slf4j
public abstract class DBMapSuitFork<T, U> extends DBMapSuit<T, U> {

    protected DBTab<T, U> parent;

    //ConcurrentHashMap deleted;
    ///////// - если ключи набор байт или других примитивов - то неверный поиск в этом виде таблиц HashMap deleted;
    /// поэтому берем медленный но правильный TreeMap
    TreeMap<T, Boolean> deleted;
    Boolean EXIST = true;
    int shiftSize;

    public DBMapSuitFork(DBTab parent, DBASet dcSet) {
        assert (parent != null);

        this.databaseSet = dcSet;
        this.database = dcSet.database;

        if (false) {
            if (Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()) {
                // System.out.println("########################### Free Memory:"
                // + Runtime.getRuntime().freeMemory());
                if (Runtime.getRuntime().freeMemory() < Controller.MIN_MEMORY_TAIL) {
                    System.gc();
                    if (Runtime.getRuntime().freeMemory() < Controller.MIN_MEMORY_TAIL >> 1)
                        Controller.getInstance().stopAll(97);
                }
            }
        }

        this.parent = parent;

        this.getMap();

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
            //logger.error(e.getMessage(), e);
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

    @Override
    public U remove(T key) {

        U value = this.map.get(key);
        this.map.remove(key);

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

        this.map.remove(key);

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
    public String toString() {
        return getClass().getName() + ".FORK";
    }
}
