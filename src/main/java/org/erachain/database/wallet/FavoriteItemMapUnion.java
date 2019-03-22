package org.erachain.database.wallet;

import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

public class FavoriteItemMapUnion extends FavoriteItemMap {

    // favorites init SET
    public FavoriteItemMapUnion(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_UNION_FAVORITES_TYPE, "union", 10);
    }
}
