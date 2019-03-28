package org.erachain.database.wallet;

import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

public class FavoriteItemMapPerson extends FavoriteItemMap {

    // favorites init SET
    public FavoriteItemMapPerson(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_PERSON_FAVORITES_TYPE, "person", 0);
    }
}
