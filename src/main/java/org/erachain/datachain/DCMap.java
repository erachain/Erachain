package org.erachain.datachain;

// upd 09/03

import org.erachain.controller.Controller;
import org.erachain.database.DBMap;
import org.erachain.database.IDB;
import org.erachain.database.wallet.DWSet;
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

    static Logger LOGGER = LoggerFactory.getLogger(DCMap.class.getName());
    protected DCMap<T, U> parent;
    protected List<T> deleted;
    private int shiftSize;

    public DCMap(IDB databaseSet, DB database) {
        super(databaseSet, database);
    }

    public DCMap(DCMap<T, U> parent, IDB dcSet) {
        super();

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

        this.databaseSet = dcSet;

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
                if (this.deleted == null || !this.deleted.contains(key)) {
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

            if (this.parent != null) {
                //if (old != null)
                //	++this.shiftSize;
                if (this.deleted != null) {
                    if (this.deleted.remove(key))
                        ++this.shiftSize;
                }
            } else {
                // COMMIT and NOTIFY if not FORKED

                // IT IS NOT FORK
                if (false && !(this.databaseSet instanceof DWSet
                        && Controller.getInstance().isProcessingWalletSynchronize())) {
                    // TODO
                    ///// this.databaseSet.commit();
                }

                // NOTIFY ADD
                if (this.getObservableData().containsKey(DBMap.NOTIFY_ADD) && !DCSet.isStoped()) {
                    this.setChanged();
                    if (
                            this.getObservableData().get(DBMap.NOTIFY_ADD).equals(ObserverMessage.ADD_UNC_TRANSACTION_TYPE)
                                    || this.getObservableData().get(DBMap.NOTIFY_ADD).equals(ObserverMessage.WALLET_ADD_ORDER_TYPE)
                                    || this.getObservableData().get(DBMap.NOTIFY_ADD).equals(ObserverMessage.ADD_AT_TX_TYPE)
                    ) {
                        this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_ADD),
                                new Pair<T, U>(key, value)));
                    } else {
                        this.notifyObservers(
                                new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_ADD), value));
                    }
                }

                if (this.getObservableData().containsKey(DBMap.NOTIFY_COUNT)) {
                    this.setChanged();
                    this.notifyObservers(
                            new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_COUNT), this)); /// SLOW .size()));
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
                    this.deleted = new ArrayList<T>();
                }
                if (this.parent.contains(key)) {
                    this.deleted.add(key);
                    value = this.parent.get(key);
                }
            }
            this.outUses();
            return value;

        } else if (this.parent == null) {

            // NOTIFY REMOVE
            if (this.getObservableData().containsKey(DBMap.NOTIFY_REMOVE)) {
                this.setChanged();
                if (
                        this.getObservableData().get(DBMap.NOTIFY_REMOVE).equals(ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE)
                                || this.getObservableData().get(DBMap.NOTIFY_REMOVE).equals(ObserverMessage.WALLET_REMOVE_ORDER_TYPE)
                                || this.getObservableData().get(DBMap.NOTIFY_REMOVE).equals(ObserverMessage.REMOVE_AT_TX)
                ) {
                    this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_REMOVE),
                            new Pair<T, U>(key, value)));
                } else {
                    this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_REMOVE), value));
                }
            }

            if (this.getObservableData().containsKey(DBMap.NOTIFY_COUNT)) {
                this.setChanged();
                this.notifyObservers(
                        new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_COUNT), this)); /// SLOW .size()));
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
            if (this.deleted == null || !this.deleted.contains(key)) {
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
