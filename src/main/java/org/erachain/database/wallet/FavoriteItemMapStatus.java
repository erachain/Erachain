package org.erachain.database.wallet;

import org.erachain.core.item.statuses.StatusCls;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

public class FavoriteItemMapStatus extends FavoriteItemMap {

    // favorites init SET
    public FavoriteItemMapStatus(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_STATUS_FAVORITES_TYPE, "status", StatusCls.INITIAL_FAVORITES);
    }
}
