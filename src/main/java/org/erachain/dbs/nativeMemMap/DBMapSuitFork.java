package org.erachain.dbs.nativeMemMap;

import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.ForkedMap;
import org.erachain.dbs.mapDB.DBMapSuit;
import org.slf4j.Logger;

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

    public DBMapSuitFork(DBTab parent, DBASet dcSet, Comparator comparator, Logger logger, DBTab cover) {
        this.logger = logger;
        this.databaseSet = dcSet;
        this.database = dcSet.database;
        this.cover = cover;

        if (Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()) {
            // System.out.println("########################### Free Memory:"
            // + Runtime.getRuntime().freeMemory());
            if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() >> 10)
                    + (Controller.MIN_MEMORY_TAIL)) {
                databaseSet.clearCache();
                System.gc();
                if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() >> 10)
                        + (Controller.MIN_MEMORY_TAIL << 1))
                    Controller.getInstance().stopAll(1021);
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

    public DBMapSuitFork(DBTab parent, DBASet dcSet, Logger logger) {
        this(parent, dcSet, null, logger, null);
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

            u = this.getDefaultValue();
            this.outUses();
            return u;
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            U u = this.getDefaultValue();
            this.outUses();
            return u;
        }
    }

    @Override
    public Set<T> keySet() {
        // тут обработка удаленных еще нужна
        Long error = null;
        error++;
        return null;
    }

    @Override
    public Collection<U> values() {
        // тут обработка удаленных еще нужна
        Long error = null;
        error++;
        return null;
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
            logger.error(e.getMessage(), e);
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