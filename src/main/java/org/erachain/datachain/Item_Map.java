package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.database.DBMap;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.erachain.utils.Pair;

import java.util.*;

/**
 * Хранение сущностей
 *
 * ключ: номер, с самоувеличением
 * Значение: Сущность
 */
public abstract class Item_Map extends DCMap<Long, ItemCls> {

    static Logger LOGGER = LoggerFactory.getLogger(Item_Map.class.getName());

    // protected int type;
    protected Atomic.Long atomicKey;
    protected long key;

    public Item_Map(DCSet databaseSet, DB database, String name) {
        super(databaseSet, database);

        this.atomicKey = database.getAtomicLong(name + "_key");
        this.key = this.atomicKey.get();
    }

    public Item_Map(DCSet databaseSet, DB database,
                    // int type,
                    String name, int observeReset, int observeAdd, int observeRemove, int observeList) {

        this(databaseSet, database, name);

        if (databaseSet.isWithObserver()) {
            if (observeReset > 0)
                this.observableData.put(DBMap.NOTIFY_RESET, observeReset);
            if (databaseSet.isDynamicGUI()) {
                if (observeAdd > 0)
                    this.observableData.put(DBMap.NOTIFY_ADD, observeAdd);
                if (observeRemove > 0)
                    this.observableData.put(DBMap.NOTIFY_REMOVE, observeRemove);
            }
            if (observeList > 0)
                this.observableData.put(DBMap.NOTIFY_LIST, observeList);
        }
    }

    public Item_Map(Item_Map parent) {
        super(parent, null);

        this.key = parent.getLastKey();
    }

    public long getLastKey() {
        return this.key;
    }

    public void setLastKey(long key) {
        // INCREMENT ATOMIC KEY IF EXISTS
        if (this.atomicKey != null) {
            this.atomicKey.set(key);
        }
        this.key = key;
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Long, ItemCls> getMemoryMap() {
        return new HashMap<Long, ItemCls>();
    }

    @Override
    protected ItemCls getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public ItemCls get(Long key) {
        ItemCls item = super.get(key);
        if (item == null)
            return null;

        item.setKey(key);
        return item;
    }

    public long add(ItemCls item) {
        // INCREMENT ATOMIC KEY IF EXISTS
        if (this.atomicKey != null) {
            this.atomicKey.incrementAndGet();
        }

        // INCREMENT KEY
        this.key++;
        item.setKey(key);

        // INSERT WITH NEW KEY
        this.set(this.key, item);

        // RETURN KEY
        return this.key;
    }

    public void remove(long key) {
        super.delete(key);

        if (this.key != key)
            // it is not top of STACK (for UNIQUE items with short NUM)
            return;

        // delete on top STACK

        if (this.atomicKey != null) {
            this.atomicKey.decrementAndGet();
        }

        // DECREMENT KEY
        --this.key;

    }

    // get list items in name substring str
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<ItemCls> get_By_Name(String str, boolean caseCharacter) {
        List<ItemCls> txs = new ArrayList<>();
        if (str == null || str.length() < 3)
            return null;

        Iterator<Pair<Long, ItemCls>> it = this.getList().iterator();
        while (it.hasNext()) {
            Pair<Long, ItemCls> a = it.next();
            String s1 = a.getB().getName();
            if (!caseCharacter) {
                s1 = s1.toLowerCase();
                str = str.toLowerCase();
            }
            if (s1.contains(str))
                txs.add(a.getB());
        }

        return txs;
    }
}
