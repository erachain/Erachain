package org.erachain.database;

import org.mapdb.DB;

public interface IDB {

    DB getDatabase();

    boolean isWithObserver();

    boolean isDynamicGUI();

    void commit();

    void addUses();

    void outUses();

    boolean isBusy();

    void close();

}