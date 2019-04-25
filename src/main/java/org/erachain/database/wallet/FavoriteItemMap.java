package org.erachain.database.wallet;

import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

import java.util.*;

public class FavoriteItemMap extends Observable {

    protected DWSet dWSet;
    private SortedSet<Long> itemsSet;

    private int observerFavorites;

    // favorites init SET
    public FavoriteItemMap(DWSet dWSet, DB database, int observerFavorites,
                           String name, int initialAdd) {
        this.dWSet = dWSet;
        this.observerFavorites = observerFavorites;

        //OPEN MAP
        itemsSet = database.getTreeSet(name + "Favorites");

        for (long i = 1; i <= initialAdd; i++) {
            //CHECK IF CONTAINS ITEM
            if (!itemsSet.contains(i)) {
                add(i);
            }
        }
    }

    public void replace(List<Long> keys) {
        itemsSet.clear();
        itemsSet.addAll(keys);
        dWSet.commit();
        //NOTIFY
        notifyFavorites();
    }

    public void add(Long key) {
        itemsSet.add(key);
        dWSet.commit();
        //NOTIFY
        notifyFavorites();
    }

    public void delete(Long key) {
        itemsSet.remove(key);
        dWSet.commit();
        //NOTIFY
        this.notifyFavorites();
    }

    public boolean contains(Long key) {
        return itemsSet.contains(key);
    }

    public long size() {
        return itemsSet.size();
    }

    public Collection<Long> getFromToKeys(long fromKey, long toKey) {
        return itemsSet.subSet(fromKey, toKey);
    }

    public int getObserverEvent() {
        return observerFavorites;
    }

    @Override
    public void addObserver(Observer o) {
        //ADD OBSERVER
        super.addObserver(o);
        //NOTIFY LIST
        notifyFavorites();
    }

    private void notifyFavorites() {
        setChanged();
        notifyObservers(new ObserverMessage(observerFavorites, itemsSet));
    }
}
