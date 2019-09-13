package org.erachain.dbs;

import org.erachain.database.IDB;
import org.erachain.database.SortableList;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public interface DCMap<T, U> extends DBMap<T, U> {

    IDB getDBSet();

    void addUses();

    void outUses();

    int size();

    U get(T key);

    Set<T> getKeys();

    Collection<U> getValues();

    boolean set(T key, U value);

    U delete(T key);

    boolean contains(T key);

    Map<Integer, Integer> getObservableData();

    boolean checkObserverMessageType(int messageType, int thisMessageType);

    Iterator<T> getIterator(int index, boolean descending);

    SortableList<T, U> getList();

    void reset();
}
