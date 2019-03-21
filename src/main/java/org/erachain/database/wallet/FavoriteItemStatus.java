package org.erachain.database.wallet;

import org.erachain.core.item.statuses.StatusCls;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

import java.util.List;
import java.util.Observer;

public class FavoriteItemStatus extends FavoriteItem {

    // favorites init SET
    public FavoriteItemStatus(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_STATUS_FAVORITES_TYPE, "status", StatusCls.INITIAL_FAVORITES);
    }
}
