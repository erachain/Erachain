package org.erachain.datachain;


import org.erachain.dbs.DBTab;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Главный класс для инкриментов - не используется
 * Block Height -> creator
 */
public abstract class AutoIntegerByte extends DCUMap<Integer, byte[]> {
    static Logger LOGGER = LoggerFactory.getLogger(AutoIntegerByte.class.getName());

    // protected int type;
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
                this.observableData.put(DBTab.NOTIFY_RESET, observeReset);
            if (observeList > 0)
                this.observableData.put(DBTab.NOTIFY_LIST, observeList);
            if (observeAdd > 0)
                this.observableData.put(DBTab.NOTIFY_ADD, observeAdd);
            if (observeRemove > 0)
                this.observableData.put(DBTab.NOTIFY_REMOVE, observeRemove);
        }
    }

    public AutoIntegerByte(AutoIntegerByte parent, DCSet dcSet) {
        super(parent, dcSet);

        this.key = parent.size();
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<Integer, byte[]>();
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

    public long add(byte[] item) {
        // INCREMENT ATOMIC KEY IF EXISTS
        if (this.atomicKey != null) {
            this.atomicKey.incrementAndGet();
        }

        // INCREMENT KEY
        this.key++;

        // INSERT WITH NEW KEY
        super.put(this.key, item);

        // RETURN KEY
        return this.key;
    }

    public byte[] last() {
        return this.get(this.key);
    }

    public void delete() {
        super.delete(key);

        if (this.atomicKey != null) {
            this.atomicKey.decrementAndGet();
        }

        // DECREMENT KEY
        --this.key;

    }

    @Override
    public boolean writeToParent() {
        boolean result = super.writeToParent();
        ((AutoIntegerByte) parent).atomicKey.set(this.key);
        ((AutoIntegerByte) parent).key = this.key;
        return result;
    }

    /**
     * Если откатить базу данных то нужно и локальные значения сбросить
     */
    @Override
    public void afterRollback() {
        this.key = atomicKey.get();
    }

}
