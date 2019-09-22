package org.erachain.database;
// 30/03 ++

import org.erachain.dbs.DBMapSuit;
import org.mapdb.DB;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import org.mapdb.Serializer;

abstract public class DBASet implements IDB {

    private static final String VERSION = "version";

    int DBS_USED;

    protected File DATA_FILE;
    public DB database;
    protected int uses;

    protected List<DBMapSuit> externalMaps = new ArrayList<>();

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

    public DB getDatabase() {
        return this.database;
    }

    public int getVersion() {
        this.uses++;
        int u = this.database.getAtomicInteger(VERSION).intValue();
        this.uses--;
        return u;
    }

    public File getFile() {
        return DATA_FILE;
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

    public List<DBMapSuit> getExternalMaps() {
        return externalMaps;
    }

    public void addExternalMaps(DBMapSuit mapSuit) {
        externalMaps.add(mapSuit);
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