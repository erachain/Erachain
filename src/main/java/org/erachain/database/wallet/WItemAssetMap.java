package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

public class WItemAssetMap extends WItemMap {

    static final String NAME = "asset";
    static final int TYPE = ItemCls.ASSET_TYPE;

    public WItemAssetMap(DWSet dWSet, DB database) {
        super(dWSet, database,
                TYPE, NAME,
                ObserverMessage.WALLET_RESET_ASSET_TYPE,
                ObserverMessage.WALLET_ADD_ASSET_TYPE,
                ObserverMessage.WALLET_REMOVE_ASSET_TYPE,
                ObserverMessage.WALLET_LIST_ASSET_TYPE
        );
    }

}
