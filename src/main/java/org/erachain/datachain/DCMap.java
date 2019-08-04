package org.erachain.datachain;

import org.erachain.controller.Controller;
import org.erachain.database.DBMap;
import org.erachain.database.IDB;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * суперкласс для таблиц цепочки блоков с функционалом Форканья (см. fork()
 * @param <T>
 * @param <U>
<br><br>
ВНИМАНИЕ !!! Вторичные ключи не хранят дубли - тоесть запись во втричном ключе не будет учтена иперезапишется если такой же ключ прийдет
Поэтому нужно добавлять униальность

 */
public abstract class DCMap<T, U> extends DBMap<T, U> {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());
    protected DCMap<T, U> parent;

    /**
     * пометка какие индексы не используются - отключим для ускорения
     */
    boolean OLD_USED_NOW = false;

    //ConcurrentHashMap deleted;
    HashMap deleted;
    Boolean EMPTY = true;
    int shiftSize = 0;


    public DCMap(IDB databaseSet, DB database) {
        super(databaseSet, database);
    }

    public DCMap(DCMap<T, U> parent, IDB dcSet) {
        super(dcSet);

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

        //this.databaseSet = dcSet;

        // OPEN MAP
        this.map = this.getMemoryMap();
    }


    // ERROR if key is not unique for each value:
    // After removing the key from the fork, which is in the parent, an incorrect post occurs
    //since from.deleted the key is removed and there is no parent in the parent and that
    // the deleted ones are smaller and the size is increased by 1
    @Override
    public int size() {
        //this.addUses();

        int u = this.map.size();

        if (this.parent != null) {
            if (this.deleted != null)
                u -= this.deleted.size();

            u -= this.shiftSize;
            u += this.parent.size();
        }

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
            if (this.map.containsKey(key)) {
                U u = this.map.get(key);
                this.outUses();
                return u;
            } else {
                if (this.deleted == null || !this.deleted.containsKey(key)) {
                    if (this.parent != null) {
                        U u = this.parent.get(key);
                        this.outUses();
                        return u;
                    }
                }
            }

            U u = this.getDefaultValue();
            this.outUses();
            return u;
        } catch (Exception e) {

            U u = this.getDefaultValue();
            this.outUses();
            return u;
        }
    }

    @Override
    public Set<T> getKeys() {

        this.addUses();
        Set<T> u = this.map.keySet();

        if (this.parent != null)
            u.addAll(this.parent.getKeys());

        this.outUses();
        return u;
    }

    @Override
    public Collection<U> getValues() {
        this.addUses();
        Collection<U> u = this.map.values();

        if (this.parent != null)
            u.addAll(this.parent.getValues());

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

            U test = this.map.get(key);

            if (this.parent != null) {
                //if (old != null)
                //	++this.shiftSize;
                if (this.deleted != null) {
                    if (this.deleted.remove(key) != null)
                        ++this.shiftSize;
                }
            } else {

                // NOTIFY if not FORKED
                if (this.observableData != null && (old == null || !old.equals(value))) {
                    if (this.observableData.containsKey(DBMap.NOTIFY_ADD) && !DCSet.isStoped()) {
                        this.setChanged();
                        Integer observeItem = this.observableData.get(DBMap.NOTIFY_ADD);
                        if (
                                observeItem.equals(ObserverMessage.ADD_UNC_TRANSACTION_TYPE)
                                        || observeItem.equals(ObserverMessage.WALLET_ADD_ORDER_TYPE)
                                        || observeItem.equals(ObserverMessage.ADD_PERSON_STATUS_TYPE)
                                        || observeItem.equals(ObserverMessage.REMOVE_PERSON_STATUS_TYPE)
                        ) {
                            this.notifyObservers(new ObserverMessage(observeItem, new Pair<T, U>(key, value)));
                        } else {
                            this.notifyObservers(
                                    new ObserverMessage(observeItem, value));
                        }
                    }
                }
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
    public U delete(T key) {

        if (DCSet.isStoped()) {
            return null;
        }

        this.addUses();
        U value = null;

        value = this.map.remove(key);

        if (value == null) {
            if (this.parent != null) {
                if (this.deleted == null) {

                    //this.deleted = new ConcurrentHashMap(1024 , 0.75f, 4);
                    this.deleted = new HashMap(1024 , 0.75f);
                }
                if (this.parent.contains(key)) {
                    this.deleted.put(key, EMPTY);
                    value = this.parent.get(key);
                }
            }
            this.outUses();
            return value;

        } else if (this.parent == null) {

            // NOTIFY
            if (this.observableData != null) {
                if (this.observableData.containsKey(DBMap.NOTIFY_REMOVE)) {
                    this.setChanged();
                    Integer observItem = this.observableData.get(DBMap.NOTIFY_REMOVE);
                    if (
                            observItem.equals(ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE)
                                    || observItem.equals(ObserverMessage.WALLET_REMOVE_ORDER_TYPE)
                                    || observItem.equals(ObserverMessage.REMOVE_AT_TX)
                    ) {
                        this.notifyObservers(new ObserverMessage(observItem, new Pair<T, U>(key, value)));
                    } else {
                        this.notifyObservers(new ObserverMessage(observItem, value));
                    }
                }
            }
        }

        this.outUses();
        return value;

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
        } else {
            if (this.deleted == null || !this.deleted.containsKey(key)) {
                if (this.parent != null) {
                    boolean u = this.parent.contains(key);

                    this.outUses();
                    return u;
                }
            }
        }

        this.outUses();
        return false;
    }

    public void addObserver(Observer o) {

        // NOT for FORK
        if (this.parent != null)
            return;

        super.addObserver(o);
    }

    @Override
    public String toString() {
        if (parent == null)  {
            return getClass().getName();
        }
        return parent.getClass().getName() + ".parent";
    }
}