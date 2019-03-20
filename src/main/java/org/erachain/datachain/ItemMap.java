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
 * <p>
 * ключ: номер, с самоувеличением
 * Значение: Сущность
 */
public abstract class ItemMap extends DCMap<Long, ItemCls> {

    private static Logger logger = LoggerFactory.getLogger(ItemMap.class.getName());

    // protected int type;
    protected Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
    protected Atomic.Long atomicKey;
    protected long key;

    public ItemMap(DCSet databaseSet, DB database, String name) {
        super(databaseSet, database);

        atomicKey = database.getAtomicLong(name + "_key");
        key = atomicKey.get();
    }

    public ItemMap(DCSet databaseSet, DB database,
                   // int type,
                   String name, int observeReset, int observeAdd, int observeRemove, int observeList) {
        this(databaseSet, database, name);
        if (databaseSet.isWithObserver()) {
            if (observeReset > 0) {
                observableData.put(DBMap.NOTIFY_RESET, observeReset);
            }
            if (databaseSet.isDynamicGUI()) {
                if (observeAdd > 0) {
                    observableData.put(DBMap.NOTIFY_ADD, observeAdd);
                }
                if (observeRemove > 0) {
                    observableData.put(DBMap.NOTIFY_REMOVE, observeRemove);
                }
            }
            if (observeList > 0) {
                observableData.put(DBMap.NOTIFY_LIST, observeList);
            }
        }
    }

    public ItemMap(ItemMap parent) {
        super(parent, null);
        key = parent.getLastKey();
    }

    public long getLastKey() {
        return key;
    }

    public void setLastKey(long key) {
        // INCREMENT ATOMIC KEY IF EXISTS
        if (atomicKey != null) {
            atomicKey.set(key);
        }
        this.key = key;
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Long, ItemCls> getMemoryMap() {
        return new HashMap<>();
    }

    @Override
    protected ItemCls getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return observableData;
    }

    public ItemCls get(Long key) {
        ItemCls item = super.get(key);
        if (item == null) {
            return null;
        }

        item.setKey(key);
        return item;
    }

    public long add(ItemCls item) {
        // INCREMENT ATOMIC KEY IF EXISTS
        if (atomicKey != null) {
            atomicKey.incrementAndGet();
        }

        // INCREMENT KEY
        key++;
        item.setKey(key);

        // INSERT WITH NEW KEY
        set(key, item);

        // RETURN KEY
        return key;
    }

    public void remove(long key) {
        super.delete(key);

        if (this.key != key) {
            // it is not top of STACK (for UNIQUE items with short NUM)
            return;
        }
        // delete on top STACK

        if (atomicKey != null) {
            atomicKey.decrementAndGet();
        }

        // DECREMENT KEY
        --this.key;

    }

    // get list items in name substring str
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<ItemCls> get_By_Name(String str, boolean caseCharacter) {
        List<ItemCls> result = new ArrayList<>();
        if (str == null || str.length() < 3) {
            return null;
        }
        for (Pair<Long, ItemCls> a : getList()) {
            String s1 = a.getB().getName();
            if (!caseCharacter) {
                s1 = s1.toLowerCase();
                str = str.toLowerCase();
            }
            if (s1.contains(str))
                result.add(a.getB());
        }
        return result;
    }
}
