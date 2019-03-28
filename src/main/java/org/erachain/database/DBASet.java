package org.erachain.database;
// 30/03 ++

import javafx.beans.Observable;
import org.erachain.settings.Settings;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Observer;

//import org.mapdb.Serializer;

abstract public class DBASet implements IDB {

    private static final String VERSION = "version";

    protected File DATA_FILE;
    protected DB database;
    protected int uses;

    private boolean withObserver;// observe
    private boolean dynamicGUI;// observe

    public DBASet() {
    }

    public DBASet(File DATA_FILE, boolean withObserver, boolean dynamicGUI) {
        this.DATA_FILE = DATA_FILE;
        this.withObserver = withObserver;
        this.dynamicGUI = dynamicGUI;
    }

    public DBASet(File DATA_FILE, DB database, boolean withObserver, boolean dynamicGUI) {
        this(DATA_FILE, withObserver, dynamicGUI);
        this.database = database;
    }

    public int getVersion() {
        this.uses++;
        int u = this.database.getAtomicInteger(VERSION).intValue();
        this.uses--;
        return u;
    }

    public void setVersion(int version) {
        this.uses++;
        this.database.getAtomicInteger(VERSION).set(version);
        this.uses--;
    }

    public boolean isWithObserver() {
        return this.withObserver;
    }

    public boolean isDynamicGUI() {
        return this.dynamicGUI;
    }

    public boolean exists() {
        return DATA_FILE.exists();
    }

    public void addUses() {
        this.uses++;
    }

    public void outUses() {
        this.uses--;
    }

    public boolean isBusy() {
        if (this.uses > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void clearCache() {
        this.database.getEngine().clearCache();
    }

    public void commit() {
        this.uses++;
        this.database.commit();
        this.uses--;

    }

    public void rollback() {
        this.uses++;
        this.database.rollback();
        this.uses--;

    }

    public void close() {
        if (this.database != null) {
            if (!this.database.isClosed()) {
                this.uses++;
                this.database.commit();
                this.database.close();
                this.uses--;

            }
        }
    }

}