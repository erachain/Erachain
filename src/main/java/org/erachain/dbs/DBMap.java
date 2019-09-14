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

    boolean set(T key, U value);

    U delete(T key);

    boolean contains(T key);

    Map<Integer, Integer> getObservableData();

    Integer deleteObservableData(int index);

    Integer setObservableData(int index, Integer data);

    boolean checkObserverMessageType(int messageType, int thisMessageType);

    Iterator<T> getIterator(int index, boolean descending);

    SortableList<T, U> getList();

    void reset();
}
