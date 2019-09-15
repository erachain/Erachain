package org.erachain.dbs;

import java.util.Collection;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Set;

public interface DBMapSuit<T, U> {

    int size();

    U get(T key);

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

    Set<T> keySet();

    Collection<U> values();

    Iterator<T> getIterator(int index, boolean descending);

    NavigableMap<?, T> getIndex(int index, boolean descending);

    //Collection<T> getSubKeys(T from, T to);

    //Collection<T> getSubHeadKeys(T to);

    //Collection<T> getSubTailKeys(T from);

    void reset();

}
