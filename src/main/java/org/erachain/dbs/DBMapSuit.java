package org.erachain.dbs;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public interface DBMapSuit<T, U> {

    int size();

    U get(T key);

    void put(T key, U value);

    U set(T key, U value);

    U remove(T key);

    void delete(T key);

    boolean contains(T key);

    Set<T> keySet();

    Collection<U> values();

    Iterator<T> getIterator(int index, boolean descending);

    //Collection<T> getSubKeys(T from, T to);

    //Collection<T> getSubHeadKeys(T to);

    //Collection<T> getSubTailKeys(T from);

    void reset();

}
