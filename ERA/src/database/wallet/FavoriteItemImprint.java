package database.wallet;

import core.item.templates.TemplateCls;
import org.mapdb.DB;
import utils.ObserverMessage;

import java.util.List;
import java.util.Observer;

public class FavoriteItemImprint extends FavoriteItem {

    // favorites init SET
    public FavoriteItemImprint(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_IMPRINT_FAVORITES_TYPE, "imprint", TemplateCls.INITIAL_FAVORITES);
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
        this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_IMPRINT_TYPE_FAVORITES_TYPE, key));
    }

    public void delete(Long key) {
        this.itemsSet.remove(key);
        this.dWSet.commit();

        //NOTIFY
        //this.notifyFavorites();
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.DELETE_IMPRINT_FAVORITES_TYPE, key));
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
