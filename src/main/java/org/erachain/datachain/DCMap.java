package org.erachain.datachain;

// upd 09/03

import org.erachain.controller.Controller;
import org.erachain.database.DBMap;
import org.erachain.database.IDB;
import org.erachain.database.wallet.DWSet;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple2;
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
public abstract class DCMap<T, U> extends Observable {

    static Logger LOGGER = LoggerFactory.getLogger(DCMap.class.getName());
    protected DCMap<T, U> parent;
    protected IDB databaseSet;
    protected Map<T, U> map;
    protected List<T> deleted;
    private Map<Integer, NavigableSet<Tuple2<?, T>>> indexes;
    private boolean worked;
    private int shiftSize;

    public DCMap(IDB databaseSet, DB database) {
        this.databaseSet = databaseSet;

        // OPEN MAP
        this.map = this.getMap(database);

        // CREATE INDEXES
        this.indexes = new HashMap<Integer, NavigableSet<Tuple2<?, T>>>();
        this.createIndexes(database);
    }

    public DCMap(DCMap<T, U> parent, IDB dcSet) {

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

    public DCSet getDCSet() {
        return (DCSet) this.databaseSet;
    }

    public boolean isWorked() {
        return this.worked;
    }

    protected abstract Map<T, U> getMap(DB database);

    protected abstract Map<T, U> getMemoryMap();

    protected abstract U getDefaultValue();

    protected abstract Map<Integer, Integer> getObservableData();

    /**
     * @param database
     */
    protected abstract void createIndexes(DB database);

    /**
     * Make SECODATY INDEX
     * INDEX ID = 0 - its is PRIMARY - not use it here
     *
     * @param index index ID. Must be 1...9999
     * @param indexSet
     * @param descendingIndexSet
     * @param function
     * @param <V>
     */
    @SuppressWarnings("unchecked")
    protected <V> void createIndex(int index, NavigableSet<?> indexSet, NavigableSet<?> descendingIndexSet,
                                   Function2<V, T, U> function) {
        assert(index > 0 && index < 10000);
        Bind.secondaryKey((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) indexSet, function);
        this.indexes.put(index, (NavigableSet<Tuple2<?, T>>) indexSet);

        Bind.secondaryKey((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) descendingIndexSet, function);
        this.indexes.put(index + 10000, (NavigableSet<Tuple2<?, T>>) descendingIndexSet);
    }

    /**
     * Make SECODATY INDEX
     * INDEX ID = 0 - its is PRIMARY - not use it here
     *
     * @param index index ID. Must be 1...9999
     * @param indexSet
     * @param descendingIndexSet
     * @param function
     * @param <V>
     */
    @SuppressWarnings("unchecked")
    protected <V> void createIndexes(int index, NavigableSet<?> indexSet, NavigableSet<?> descendingIndexSet,
                                     Function2<V[], T, U> function) {
        assert(index > 0 && index < 10000);
        Bind.secondaryKeys((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) indexSet, function);
        this.indexes.put(index, (NavigableSet<Tuple2<?, T>>) indexSet);

        Bind.secondaryKeys((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) descendingIndexSet, function);
        this.indexes.put(index + 10000, (NavigableSet<Tuple2<?, T>>) descendingIndexSet);
    }

    public void addUses() {
        worked = true;
        if (this.databaseSet != null) {
            this.databaseSet.addUses();
        }
    }

    public void outUses() {
        worked = false;
        if (this.databaseSet != null) {
            this.databaseSet.outUses();
        }
    }

    // ERROR if key is not unique for each value:
    // After removing the key from the fork, which is in the parent, an incorrect post occurs
    //since from.deleted the key is removed and there is no parent in the parent and that
    // the deleted ones are smaller and the size is increased by 1
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

    public Set<T> getKeys() {

        this.addUses();
        Set<T> u = this.map.keySet();

        if (this.parent != null)
            u.addAll(this.parent.getKeys());

        this.outUses();
        return u;
    }

    public Collection<U> getValues(int count, boolean descending, int i) {
        this.addUses();
        Iterator<T> u;

        if (descending) {
            u = ((NavigableMap<T, U>) this.map).descendingKeySet().iterator();
        } else {
            u = ((NavigableMap<T, U>) this.map).keySet().iterator();
        }

        Collection<U> v = new ArrayList<U>();
        while (u.hasNext() && i++ < count) {
            v.add(this.map.get(u.next()));
        }

        if (this.parent != null)
            v.addAll(this.parent.getValues(count, descending, i));

        this.outUses();
        return v;
    }

    public Collection<U> getValues(int count, boolean descending) {

        return getValues(count, descending, 0);

    }

    public Collection<U> getValuesAll() {
        this.addUses();
        Collection<U> u = this.map.values();

        if (this.parent != null)
            u.addAll(this.parent.getValuesAll());

        this.outUses();
        return u;
    }

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

    @Override
    public void addObserver(Observer o) {

        // NOT for FORK
        if (this.parent != null)
            return;

        this.addUses();

        // ADD OBSERVER
        super.addObserver(o);

        // NOTIFY LIST FORK
        if (this.getObservableData().containsKey(DBMap.NOTIFY_LIST)) {
            // CREATE LIST
            SortableList<T, U> list = new SortableList<T, U>(this);

            // UPDATE
            o.update(null, new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_LIST), list));
        }

        if (this.getObservableData().containsKey(DBMap.NOTIFY_COUNT)) {
            this.setChanged();
            this.notifyObservers(
                    new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_COUNT), this)); /// SLOW .size()));
        }

        this.outUses();
    }

    /**
     *
     * @param index <b>primary Index = 0</b>, secondary index = 1...10000
     * @param descending true if need descending sort
     * @return
     */
    public Iterator<T> getIterator(int index, boolean descending) {
        this.addUses();

        if (index == DBMap.DEFAULT_INDEX) {
            if (descending) {
                Iterator<T> u = ((NavigableMap<T, U>) this.map).descendingKeySet().iterator();
                this.outUses();
                return u;
            }

            Iterator<T> u = ((NavigableMap<T, U>) this.map).keySet().iterator();
            this.outUses();
            return u;
        } else {
            if (descending) {
                index += 10000;
            }

            IndexIterator<T> u = new IndexIterator<T>(this.indexes.get(index));
            this.outUses();
            return u;
        }
    }

    public SortableList<T, U> getList() {
        this.addUses();
        SortableList<T, U> u = new SortableList<T, U>(this);
        this.outUses();
        return u;
    }

    public SortableList<T, U> getParentList2() {
        this.addUses();

        if (this.parent != null) {
            SortableList<T, U> u = new SortableList<T, U>(this.parent);
            this.outUses();
            return u;
        }
        this.outUses();
        return null;
    }

    public void reset() {
        this.addUses();

        // RESET MAP
        this.map.clear();

        // RESET INDEXES
        for (Set<Tuple2<?, T>> set : this.indexes.values()) {
            set.clear();
        }

        // NOTIFY LIST
        if (this.getObservableData().containsKey(DBMap.NOTIFY_RESET)) {
            // CREATE LIST
            ///// SortableList<T, U> list = new SortableList<T, U>(this);

            // UPDATE
            this.setChanged();
            this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_RESET), null));
        }

        if (this.getObservableData().containsKey(DBMap.NOTIFY_COUNT)) {
            this.setChanged();
            this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_COUNT), this));
        }

        this.outUses();
    }

    @Override
    public String toString() {
        if (this.parent == null)
            return "DCmain";

        return this.parent.toString() + ".parent";
    }
}
