package org.erachain.dbs.rocksDB.basic;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erachain.database.IDB;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.DB;

import java.util.*;

/**
 * суперкласс для таблиц цепочки блоков с функционалом Форканья (см. fork()
 *
 * @param <T>
 * @param <U>
 */
@Slf4j
@NoArgsConstructor
public abstract class DCMap<T, U> extends DBMap<T, U> {

    protected DCMap<T, U> parent;
    protected List<T> deleted;
    private int shiftSize;

    public DCMap(IDB databaseSet, DB database) {
        super(databaseSet, database);
    }

    public DCMap(DCMap<T, U> parent, IDB dcSet) {
        super(dcSet);
        this.parent = parent;
        tableDB = getMemoryMap();
    }


    @Override
    public int size() {
        int u = tableDB.size();
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
            if (tableDB.containsKey(key)) {
                return tableDB.get(key);
            } else {
                if (deleted == null || !deleted.contains(key)) {
                    if (parent != null) {
                        return parent.get(key);
                    }
                }
            }

            return getDefaultValue();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return getDefaultValue();
        }
    }

    @Override
    public Set<T> getKeys() {
        Set<T> u = tableDB.keySet();
        if (parent != null) {
            u.addAll(parent.getKeys());
        }
        return u;
    }

    @Override
    public Collection<U> getValues() {
        Collection<U> u = tableDB.values();
        if (parent != null) {
            u.addAll(parent.getValues());
        }
        return u;
    }

    @Override
    public void put(T key, U value) {
        if (DCSet.isStoped()) {
            return;
        }
        try {
//            U old = tableDB.get(key);
            tableDB.put(key, value);
            if (parent != null) {
                if (deleted != null) {
                    if (deleted.remove(key)) {
                        shiftSize++;
                    }
                }
            } else {
                // NOTIFY if not FORKED
                if (observableData != null
                        /*&& (old == null || !old.equals(value))*/) {
                    if (observableData.containsKey(NOTIFY_ADD) && !DCSet.isStoped()) {
                        setChanged();
                        Integer observeItem = observableData.get(NOTIFY_ADD);
                        if (observeItem.equals(ObserverMessage.ADD_UNC_TRANSACTION_TYPE)
                                || observeItem.equals(ObserverMessage.WALLET_ADD_ORDER_TYPE)
                                || observeItem.equals(ObserverMessage.ADD_PERSON_STATUS_TYPE)
                                || observeItem.equals(ObserverMessage.REMOVE_PERSON_STATUS_TYPE)) {
                            notifyObservers(new ObserverMessage(observeItem, new Pair<>(key, value)));
                        } else {
                            notifyObservers(new ObserverMessage(observeItem, value));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public U delete(T key) {
        if (DCSet.isStoped()) {
            return null;
        }
        U value = tableDB.get(key);
        tableDB.remove(key);
        if (value == null) {
            if (parent != null) {
                if (deleted == null) {
                    deleted = new ArrayList<T>();
                }
                if (parent.contains(key)) {
                    deleted.add(key);
                    value = parent.get(key);
                }
            }
            return value;
        } else if (parent == null) {
            // NOTIFY
            if (observableData != null) {
                if (observableData.containsKey(NOTIFY_REMOVE)) {
                    setChanged();
                    Integer observItem = this.observableData.get(NOTIFY_REMOVE);
                    if (observItem.equals(ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE)
                            || observItem.equals(ObserverMessage.WALLET_REMOVE_ORDER_TYPE)
                            || observItem.equals(ObserverMessage.REMOVE_AT_TX)) {
                        notifyObservers(new ObserverMessage(observItem, new Pair<>(key, value)));
                    } else {
                        notifyObservers(new ObserverMessage(observItem, value));
                    }
                }
            }
        }
        return value;

    }

    @Override
    public boolean contains(T key) {
        if (DCSet.isStoped()) {
            return false;
        }
        if (tableDB.containsKey(key)) {
            return true;
        } else {
            if (deleted == null || !deleted.contains(key)) {
                if (parent != null) {
                    return parent.contains(key);
                }
            }
        }
        return false;
    }

    public void addObserver(Observer o) {
        if (parent != null)  {
            return;
        }
        super.addObserver(o);
    }

    @Override
    public String toString() {
        if (parent == null) {
            return getClass().getName();
        }
        return parent.getClass().getName() + ".parent";
    }
}
