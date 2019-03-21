package org.erachain.database.wallet;

import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

import java.util.List;
import java.util.Observer;

public class FavoriteItemPerson extends FavoriteItem {

    // favorites init SET
    public FavoriteItemPerson(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_PERSON_FAVORITES_TYPE, "person", 0);
    }
}
