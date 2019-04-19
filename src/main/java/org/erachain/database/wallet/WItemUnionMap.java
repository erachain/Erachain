package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

public class WItemUnionMap extends WItemMap {

    static final String NAME = "union";
    static final int TYPE = ItemCls.UNION_TYPE;


    public WItemUnionMap(DWSet dWSet, DB database) {
        super(dWSet, database,
                TYPE, NAME,
                ObserverMessage.WALLET_RESET_UNION_TYPE,
                ObserverMessage.WALLET_ADD_UNION_TYPE,
                ObserverMessage.WALLET_REMOVE_UNION_TYPE,
                ObserverMessage.WALLET_LIST_UNION_TYPE
        );
    }

}
