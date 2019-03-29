package org.erachain.database;

public interface IDB {

    public boolean isWithObserver();

    public boolean isDynamicGUI();

    public void commit();

    public void addUses();

    public void outUses();

    public boolean isBusy();

    public void close();

}