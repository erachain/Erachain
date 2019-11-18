package org.erachain.dbs;

import org.erachain.database.IDB;
import org.erachain.database.SortableList;

import java.util.Map;

/**
 * Описатель Таблиц (Tab), в которых есть Обернутые карты - (Suit)
 * @param <T>
 * @param <U>
 */
public interface DBTab<T, U> extends IMap<T, U>, ForkedMap {

    int NOTIFY_RESET = 1;
    int NOTIFY_ADD = 2;
    int NOTIFY_REMOVE = 3;
    int NOTIFY_LIST = 4;

    IDB getDBSet();

    Map<Integer, Integer> getObservableData();

    Integer deleteObservableData(int index);

    Integer setObservableData(int index, Integer data);

    boolean checkObserverMessageType(int messageType, int thisMessageType);

    //NavigableSet<Fun.Tuple2<?, T>> getIndex(int index, boolean descending);

    SortableList<T, U> getList();

    void notifyObserverList();

}
