package database.wallet;

import org.mapdb.DB;
import utils.ObserverMessage;

public class FavoriteItemUnion extends FavoriteItem {

    // favorites init SET
    public FavoriteItemUnion(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_UNION_FAVORITES_TYPE, "union", 3);
    }

}
