package org.erachain.database;

public interface IDB {

    public void commit();

    public void addUses();

    public void outUses();

    public boolean isBusy();

    public void openDBSet();

    public void close();


}
