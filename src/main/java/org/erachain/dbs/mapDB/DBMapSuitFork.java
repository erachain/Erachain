package org.erachain.dbs.mapDB;

import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.dbs.DBTab;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;

/**
 * Оболочка для Карты от конкретной СУБД чтобы эту оболочку вставлять в Таблицу, которая форкнута (см. fork()).
 * Тут всегда должен быть задан Родитель. Здесь другой порядок обработки данных в СУБД
 * @param <T>
 * @param <U>
<br><br>
ВНИМАНИЕ !!! Вторичные ключи не хранят дубли - тоесть запись во втричном ключе не будет учтена иперезапишется если такой же ключ прийдет
Поэтому нужно добавлять униальность

 */
public abstract class DBMapSuitFork<T, U> extends DBMapSuit<T, U> {

    protected DBTab<T, U> parent;

    //ConcurrentHashMap deleted;
    ///////// - если ключи набор байт или других примитивов - то неверный поиск в этом виде таблиц HashMap deleted;
    /// поэтому берем медленный но правильный TreeMap
    TreeMap<T, Boolean> deleted;
    int shiftSize;

    public DBMapSuitFork(DBTab parent, DBASet dcSet, Logger logger, U defaultValue) {
        assert (parent != null);

        this.databaseSet = dcSet;
        this.database = dcSet.database;
        this.logger = logger;
        this.defaultValue = defaultValue;

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
        set(key, value);
    }

    @Override
    public U remove(T key) {

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
    public String toString() {
        return getClass().getName() + ".FORK";
    }

}