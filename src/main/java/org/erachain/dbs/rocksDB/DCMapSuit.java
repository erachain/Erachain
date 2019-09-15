package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * суперкласс для таблиц цепочки блоков с функционалом Форканья (см. fork()
 * Тут всегда должен быть задан Родитель
 * @param <T>
 * @param <U>
 */
@Slf4j
public abstract class DCMapSuit<T, U> extends DBMapSuit<T, U> {

    protected org.erachain.dbs.DBMap<T, U> parent;

    /**
     * пометка какие индексы не используются - отключим для ускорения
     */
    boolean OLD_USED_NOW = false;

    //ConcurrentHashMap deleted;
    HashMap deleted;
    Boolean EXIST = true;
    int shiftSize;

    public DCMapSuit(org.erachain.dbs.DBMap parent, DBASet dcSet) {
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
        if (parent != null) {
            if (deleted != null) {
                u -= deleted.size();
            }
            u -= shiftSize;
            u += parent.size();
        }
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
                    if (parent != null) {
                        return parent.get(key);
                    }
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
        u.addAll(parent.getKeys());
        return u;
    }

    @Override
    public Collection<U> values() {
        Collection<U> u = map.values();
        u.addAll(parent.getValues());
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
        if (DCSet.isStoped()) {
            return;
        }
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
            this.deleted = new HashMap(1024 , 0.75f);
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
            this.deleted = new HashMap(1024 , 0.75f);
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
