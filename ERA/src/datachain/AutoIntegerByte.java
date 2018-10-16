package datachain;


import database.DBMap;
import org.apache.log4j.Logger;
import org.mapdb.Atomic;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.Map;

// Block Height -> creator

/**
 * Главный класс для инкриментов - не используется
 */
public abstract class AutoIntegerByte extends DCMap<Integer, byte[]> {
    static Logger LOGGER = Logger.getLogger(AutoIntegerByte.class.getName());

    // protected int type;
    protected Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
    protected Atomic.Integer atomicKey;
    protected int key;

    public AutoIntegerByte(DCSet databaseSet, DB database, String name) {
        super(databaseSet, database);

        this.atomicKey = database.getAtomicInteger(name + "_key");
        this.key = this.atomicKey.get();
    }

    public AutoIntegerByte(DCSet databaseSet, DB database,
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

    public AutoIntegerByte(AutoIntegerByte parent) {
        super(parent, null);

        this.key = parent.size();
    }

    @Override
    public int size() {
        return this.key;
    }

    public void setSize(int size) {
        // INCREMENT ATOMIC KEY IF EXISTS
        if (this.atomicKey != null) {
            this.atomicKey.set(size);
        }
        this.key = size;
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Integer, byte[]> getMemoryMap() {
        return new HashMap<Integer, byte[]>();
    }

    @Override
    protected byte[] getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public long add(byte[] item) {
        // INCREMENT ATOMIC KEY IF EXISTS
        if (this.atomicKey != null) {
            this.atomicKey.incrementAndGet();
        }

        // INCREMENT KEY
        this.key++;

        // INSERT WITH NEW KEY
        this.set(this.key, item);

        // RETURN KEY
        return this.key;
    }

    public byte[] last() {
        return this.get(this.key);
    }

    public void remove() {
        super.delete(key);

        if (this.atomicKey != null) {
            this.atomicKey.decrementAndGet();
        }

        // DECREMENT KEY
        --this.key;

    }

}
