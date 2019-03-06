package org.erachain.database.wallet;

import org.erachain.core.item.templates.TemplateCls;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

import java.util.List;
import java.util.Observer;

public class FavoriteItemTemplate extends FavoriteItem {

    // favorites init SET
    public FavoriteItemTemplate(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_TEMPLATE_FAVORITES_TYPE, "template", TemplateCls.INITIAL_FAVORITES);
    }

    /*
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
        this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_TEMPLATE_TYPE_FAVORITES_TYPE, key));
    }

    public void delete(Long key) {
        this.itemsSet.remove(key);
        this.dWSet.commit();

        //NOTIFY
        //this.notifyFavorites();
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.DELETE_TEMPLATE_FAVORITES_TYPE, key));
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

    */

}
