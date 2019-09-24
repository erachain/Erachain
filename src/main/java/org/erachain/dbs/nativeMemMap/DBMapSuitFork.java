package org.erachain.dbs.nativeMemMap;

import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.mapDB.DBMapSuit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Observer;
import java.util.Set;

/**
 * Это только форкнутые таблицы
 * суперкласс для таблиц цепочки блоков с функционалом Форканья (см. fork()
 * @param <T>
 * @param <U>
<br><br>
ВНИМАНИЕ !!! Вторичные ключи не хранят дубли - тоесть запись во втричном ключе не будет учтена иперезапишется если такой же ключ прийдет
Поэтому нужно добавлять униальность

 */
public abstract class DBMapSuitFork<T, U> extends DBMapSuit<T, U> {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    protected DBTab<T, U> parent;

    /**
     * пометка какие индексы не используются - отключим для ускорения
     */
    boolean OLD_USED_NOW = false;

    //ConcurrentHashMap deleted;
    HashMap deleted;
    Boolean EXIST = true;
    int shiftSize;

    public DBMapSuitFork(DBTab parent, DBASet dcSet) {
        this.databaseSet = dcSet;
        this.database = dcSet.database;

        if (Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()) {
            // System.out.println("########################### Free Memory:"
            // + Runtime.getRuntime().freeMemory());
            if (Runtime.getRuntime().freeMemory() < Controller.MIN_MEMORY_TAIL) {
                System.gc();
                if (Runtime.getRuntime().freeMemory() < Controller.MIN_MEMORY_TAIL >> 1)
                    Controller.getInstance().stopAll(97);
            }
        }

        this.parent = parent;

        this.getMap();

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

        u.addAll(this.parent.keySet());

        this.outUses();
        return u;
    }

    @Override
    public Collection<U> values() {
        this.addUses();
        Collection<U> u = this.map.values();

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

            U old = this.map.put(key, value);

            if (this.deleted != null) {
                if (this.deleted.remove(key) != null)
                    ++this.shiftSize;
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

    public void addObserver(Observer o) {
    }

    @Override
    public String toString() {
        return getClass().getName() + ".FORK";
    }
}