package database.wallet;

import org.mapdb.DB;
import utils.ObserverMessage;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class FavoriteItem extends Observable {

    protected DWSet dWSet;
    protected Set<Long> itemsSet;

    protected int observer_favorites;

    // favorites init SET
    public FavoriteItem(DWSet dWSet, DB database, int observer_favorites,
                        String treeSet, int initialAdd //, WItem_Map map
    ) {
        this.dWSet = dWSet;
        this.observer_favorites = observer_favorites;

        //OPEN MAP
        this.itemsSet = database.getTreeSet(treeSet + "Favorites");

        for (long i = 1; i <= initialAdd; i++) {
            //CHECK IF CONTAINS ITEM
            if (!this.itemsSet.contains(i)
                //&& map.contains(i)
            ) {
                this.add(i);
            }
        }
    }

    public void replace(List<Long> keys) {
        this.itemsSet.clear();
        this.itemsSet.addAll(keys);
        this.dWSet.commit();

        //NOTIFY
        this.notifyFavorites();
    }

    public void add(Long key) {
        this.itemsSet.add(key);
        this.dWSet.commit();

        //NOTIFY
        this.notifyFavorites();
    }

    public void delete(Long key) {
        this.itemsSet.remove(key);
        this.dWSet.commit();

        //NOTIFY
        this.notifyFavorites();
    }

    public boolean contains(Long key) {
        return this.itemsSet.contains(key);
    }

    @Override
    public void addObserver(Observer o) {
        //ADD OBSERVER
        super.addObserver(o);

        //NOTIFY LIST
        this.notifyFavorites();
    }

    protected void notifyFavorites() {
        this.setChanged();
        this.notifyObservers(new ObserverMessage(this.observer_favorites, this.itemsSet));
    }
}
