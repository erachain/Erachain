package database.wallet;

import core.item.statuses.StatusCls;
import org.mapdb.DB;
import utils.ObserverMessage;

import java.util.List;
import java.util.Observer;

public class FavoriteItemStatus extends FavoriteItem {

    // favorites init SET
    public FavoriteItemStatus(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_STATUS_FAVORITES_TYPE, "status", StatusCls.INITIAL_FAVORITES);
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
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_STATUS_TYPE_FAVORITES_TYPE, key));
    }

    public void delete(Long key) {
        this.itemsSet.remove(key);
        this.dWSet.commit();

        //NOTIFY
        //this.notifyFavorites();
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.DELETE_STATUS_FAVORITES_TYPE, key));
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
