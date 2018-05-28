package datachain;

public interface IDB {

    public void commit();

    public void addUses();

    public void outUses();

    public boolean isBusy();

}
