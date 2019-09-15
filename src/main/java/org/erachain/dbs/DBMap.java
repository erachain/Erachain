package org.erachain.dbs;

import org.erachain.database.IDB;
import org.erachain.database.SortableList;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public interface DBMap<T, U> {
    int NOTIFY_RESET = 1;
    int NOTIFY_ADD = 2;
    int NOTIFY_REMOVE = 3;
    int NOTIFY_LIST = 4;

    IDB getDBSet();

    int size();

    U get(T key);

    Set<T> getKeys();

    Collection<U> getValues();

    /**
     *
     * @param key
     * @param value
     * @return If has old value = true
     */
    boolean set(T key, U value);

    /**
     * not check old value
     * @param key
     * @param value
     */
    void put(T key, U value);

    /**
     *
     * @param key
     * @return old value
     */
    U remove(T key);

    /**
     * not check old value
     * @param key
     * @return
     */
    void delete(T key);

    /**
     * Remove only Value - not Key.
     * @param key
     * @return old value
     */
    U removeValue(T key);

    /**
     * Delete only Value - not Key.
     * not check old value
     * @param key
     * @return
     */
    void deleteValue(T key);

    boolean contains(T key);

    Map<Integer, Integer> getObservableData();

    Integer deleteObservableData(int index);

    Integer setObservableData(int index, Integer data);

    boolean checkObserverMessageType(int messageType, int thisMessageType);

    //NavigableSet<Fun.Tuple2<?, T>> getIndex(int index, boolean descending);

    Iterator<T> getIterator(int index, boolean descending);

    int getDefaultIndex();

    SortableList<T, U> getList();

    void reset();
}
