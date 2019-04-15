package org.erachain.database.wallet;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

public class FavoriteTransactionMap extends FavoriteItemMap {

    // favorites init SET
    public FavoriteTransactionMap(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_TRANSACTION_FAVORITES_TYPE, "transaction", 0);

    }

}
