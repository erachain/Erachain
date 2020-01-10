package org.erachain.database;

import org.mapdb.DB;

public interface IDB {

    int DBS_FAST = 0;
    int DBS_MAP_DB = 1;
    int DBS_ROCK_DB = 2;
    int DBS_NATIVE_MAP = 3;
    int DBS_MAP_DB_IN_MEM = 4;

    DB getDatabase();

    boolean isWithObserver();

    boolean isDynamicGUI();

    void commit();

    void addUses();

    void outUses();

    boolean isBusy();

    void close();

}