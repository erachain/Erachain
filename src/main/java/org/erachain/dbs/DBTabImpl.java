package org.erachain.dbs;

import lombok.Getter;
import org.erachain.database.DBASet;
import org.erachain.database.IDB;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * К Обработке данных добалены события. Это Суперкласс для таблиц проекта.
 * Однако в каждой таблице есть еще обертка для каждой СУБД отдельно - DBSuit
 * @param <T>
 * @param <U>
 */
public abstract class DBTabImpl<T, U> extends Observable implements DBTab<T, U> {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    protected final String TAB_NAME;
    protected final Serializer TAB_SERIALIZER;

    public int DESCENDING_SHIFT_INDEX = 10000;

    protected int dbsUsed;

    public static int DEFAULT_INDEX = 0;
    protected DBASet databaseSet;
    protected DB database;

    protected DBSuit<T, U> map;
    // Эта Карта не должна путаться вверху с DCU картой - иначе НУЛ при заходе в DBMapCommonImpl
    ////protected DBSuit<T, U> map;

    @Getter
    protected DBTab<T, U> parent;

    protected boolean sizeEnable;

    protected Map<Integer, Integer> observableData;

    public DBTabImpl() {
        TAB_NAME = null;
        TAB_SERIALIZER = null;
        databaseSet.addTable(this);
    }

    public DBTabImpl(DBASet databaseSet) {
        TAB_NAME = null;
        TAB_SERIALIZER = null;

        this.databaseSet = databaseSet;
        databaseSet.addTable(this);

        if (databaseSet != null && databaseSet.isWithObserver()) {
            observableData = new HashMap<Integer, Integer>(8, 1);
        }
    }

    public DBTabImpl(int dbsUsed, DBASet databaseSet, DB database, boolean sizeEnable, String tabName, Serializer serializer) {
        this.dbsUsed = dbsUsed;
        this.databaseSet = databaseSet;
        this.database = database;
        this.sizeEnable = sizeEnable;
        TAB_NAME = tabName;
        TAB_SERIALIZER = serializer;
        databaseSet.addTable(this);

        //OPEN MAP
        openMap();

        if (databaseSet.isWithObserver()) {
            observableData = new HashMap<Integer, Integer>(8, 1);
        }

    }

    public DBTabImpl(int dbsUsed, DBASet databaseSet, DB database) {
        this(dbsUsed, databaseSet, database, false, null, null);
    }

    public DBTabImpl(DBASet databaseSet, DB database, String tabName, Serializer tabSerializer, boolean sizeEnable) {
        this(IDB.DBS_MAP_DB, databaseSet, database, sizeEnable, tabName, tabSerializer);
    }

    public DBTabImpl(DBASet databaseSet, DB database, boolean sizeEnable) {
        this(IDB.DBS_MAP_DB, databaseSet, database, sizeEnable, null, null);
    }

    /**
     * Это лоя форкеутой таблицы вызов - запомнить Родителя и все - индексы тут не нужны и обсерверы
     * @param parent
     * @param databaseSet
     */
    public DBTabImpl(int dbsUsed, DBTab parent, DBASet databaseSet, boolean sizeEnable) {

        this.dbsUsed = dbsUsed;
        this.databaseSet = databaseSet;
        this.database = databaseSet.database;
        this.sizeEnable = sizeEnable;
        this.parent = parent;
        TAB_NAME = ((DBTabImpl) parent).TAB_NAME;
        TAB_SERIALIZER = ((DBTabImpl) parent).TAB_SERIALIZER;

        databaseSet.addTable(this);

        // OPEN MAP
        this.openMap();

    }

    public DBTabImpl(int dbsUsed, DBTab parent, DBASet databaseSet) {
        this(dbsUsed, parent, databaseSet, false);
    }

    /**
     * Это лоя форкеутой таблицы вызов - запомнить Родителя и все - индексы тут не нужны и обсерверы
     * @param parent
     * @param databaseSet
     */
    public DBTabImpl(DBTab parent, DBASet databaseSet, boolean sizeEnable) {

        this.databaseSet = databaseSet;
        this.database = databaseSet.database;
        this.parent = parent;
        TAB_NAME = ((DBTabImpl) parent).TAB_NAME;
        TAB_SERIALIZER = ((DBTabImpl) parent).TAB_SERIALIZER;
        this.sizeEnable = sizeEnable;
        databaseSet.addTable(this);

        // OPEN MAP
        this.openMap();

    }

    // for TESTS etc.
    public void setSource(DBSuit map) {
        this.map = map;
    }

    @Override
    public DBSuit getSuit() {
        return map;
    }

    @Override
    public IDB getDBSet() {
        return this.databaseSet;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isSizeEnable() {
        return sizeEnable;
    }

    @Override
    public U getDefaultValue(T key) {
        return null;
    }

    @Override
    public Set<T> keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection<U> values() {
        return this.map.values();
    }

    @Override
    public U get(T key) {
        return this.map.get(key);
    }

    /**
     * уведомляет только счетчик если он разрешен, иначе Добавить
     * @param key
     * @param value
     * @return
     */
    @Override
    public boolean set(T key, U value) {

        boolean result = this.map.set(key, value);

        if (this.observableData != null) {
            if (this.observableData.containsKey(NOTIFY_ADD)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_ADD), value));
            }
        }

        return result;

    }

    @Override
    public void put(T key, U value) {

        this.map.put(key, value);

        if (this.observableData != null) {
            if (this.observableData.containsKey(NOTIFY_ADD)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_ADD), value));
            }
        }
    }

    private U removeHere(T key) {

        U value = this.map.remove(key);

        if (value != null) {
            //NOTIFY
            if (this.observableData != null) {
                if (this.observableData.containsKey(NOTIFY_REMOVE)) {
                    this.setChanged();
                    Integer observItem = this.observableData.get(NOTIFY_REMOVE);
                    if (
                            observItem.equals(ObserverMessage.WALLET_REMOVE_ORDER_TYPE)
                                    || observItem.equals(ObserverMessage.REMOVE_AT_TX)
                    ) {
                        this.notifyObservers(new ObserverMessage(observItem, new Pair<T, U>(key, value)));
                    } else {
                        this.notifyObservers(new ObserverMessage(observItem, value));
                    }
                }
            }
        }

        return value;
    }

    @Override
    public U remove(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///
        return removeHere(key);
    }

    @Override
    public U removeValue(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///
        return removeHere(key);
    }

    private void deleteHere(T key) {
        this.map.delete(key);

        //NOTIFY
        if (this.observableData != null) {
            if (this.observableData.containsKey(NOTIFY_REMOVE)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_REMOVE), key));
            }
        }

    }

    @Override
    public void delete(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///

        deleteHere(key);
    }

    @Override
    public void deleteValue(T key) {
        /// ВНИМАНИЕ - нельзя тут так делать - перевызывать родственный метод this.remove, так как
        /// если в подклассе будет из REMOVE вызов DELETE то он придет сюда и при перевузове THIS.REMOVE отсюда
        /// улетит опять в подкласс и получим зацикливание, поэто тут надо весь код повторить
        /// -----> remove(key, value);
        ///

        deleteHere(key);
    }

    @Override
    public boolean contains(T key) {
        return map.contains(key);
    }

    /**
     * @param index      <b>primary Index = 0</b>, secondary index = 1...10000
     * @param descending true if need descending sort
     * @return
     */
    @Override
    public IteratorCloseable<T> getIndexIterator(int index, boolean descending) {
        return map.getIndexIterator(index, descending);
    }

    @Override
    public IteratorCloseable<T> getIterator() {
        return map.getIterator();
    }

    @Override
    public IteratorCloseable<T> getDescendingIterator() {
        return map.getDescendingIterator();
    }

    @Override
    public IteratorCloseable<T> getIterator(T fromKey, boolean descending) {
        return map.getIterator(fromKey, descending);
    }


    /**
     * уведомляет только счетчик если он разрешен, иначе Сбросить
     */
    @Override
    public void clear() {
        //RESET MAP
        this.map.clear();

        // NOTYFIES
        if (this.observableData != null) {
            //NOTIFY LIST
            if (this.observableData.containsKey(NOTIFY_RESET)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_RESET), this));
            }
        }
    }

    @Override
    public void notifyObserverList() {
        // NOTYFIES
        if (this.observableData != null) {
            //NOTIFY LIST
            if (this.observableData.containsKey(NOTIFY_LIST)) {
                this.setChanged();
                this.notifyObservers(new ObserverMessage(this.observableData.get(NOTIFY_LIST), this));
            }
        }
    }

    @Override
    public boolean writeToParent() {

        if (((ForkedMap) this.map).writeToParent()) {
            if (parent != null) {
                // нужно кинуть событие обновления в ГУИ родителя - если при сливе были изменения
                parent.notifyObserverList();
            }
            return true;
        }
        return false;

    }

    @Override
    public void clearCache() { map.clearCache(); }

    @Override
    public void close() {
        if (map != null)
            map.close();
        databaseSet = null;
        database = null;
        map = null;
    }

    @Override
    public boolean isClosed() {
        return map.isClosed();
    }

    @Override
    public void commit() { map.commit(); }

    @Override
    public void rollback() { map.rollback(); }

    @Override
    public void afterRollback() {
    }

    /////////////////
///////////////

    /**
     * Теперь просто передает себя
     *
     * @param o
     */
    @Override
    public void addObserver(Observer o) {

        //ADD OBSERVER
        super.addObserver(o);

        //NOTIFY
        if (this.observableData != null) {
            if (this.observableData.containsKey(NOTIFY_LIST)) {
                //UPDATE
                o.update(null, new ObserverMessage(this.observableData.get(NOTIFY_LIST), this));
            }
        }
    }

    public int getDefaultIndex() {
        return DEFAULT_INDEX;
    }

    @Override
    public Integer deleteObservableData(int index) {
        return this.observableData.remove(index);
    }

    @Override
    public Integer setObservableData(int index, Integer data) {
        return this.observableData.put(index, data);
    }

    @Override
    public Map<Integer, Integer> getObservableData() {
        return observableData;
    }

    @Override
    public boolean checkObserverMessageType(int messageType, int thisMessageType) {
        if (observableData == null || observableData.isEmpty() || !observableData.containsKey(thisMessageType))
            return false;

        return observableData.get(messageType) == thisMessageType;
    }

}
