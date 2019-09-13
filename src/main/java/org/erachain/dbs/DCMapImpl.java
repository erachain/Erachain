package org.erachain.dbs;

import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Observer;
import java.util.Set;

public abstract class DCMapImpl<T, U> extends DBMapImpl<T, U> implements DCMap<T, U> {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    protected DCMap<T, U> parent;

    /**
     * пометка какие индексы не используются - отключим для ускорения
     */
    boolean OLD_USED_NOW = false;

    //ConcurrentHashMap deleted;
    HashMap deleted;
    Boolean EXIST = true;
    int shiftSize;


    public DCMapImpl(DBASet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public DCMapImpl(DCMap<T, U> parent, DBASet dcSet) {
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

        // OPEN MAP
        if (dcSet == null || dcSet.getDatabase() == null) {
            this.getMemoryMap();
        } else {
            this.getMap();
        }
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
            U u = this.map.get(key);
            if (u != null) {
                this.outUses();
                return u;
            }

            if (parent != null) {
                if (this.deleted == null || !this.deleted.containsKey(key)) {
                    u = this.parent.get(key);
                    this.outUses();
                    return u;
                }
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

        if (this.parent != null) {
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

        } else {

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
        return getClass().getName() + ".FORK";
    }
}
