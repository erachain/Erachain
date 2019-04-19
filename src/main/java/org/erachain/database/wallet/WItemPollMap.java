package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

public class WItemPollMap extends WItemMap {

    static final String NAME = "poll";
    static final int TYPE = ItemCls.UNION_TYPE;


    public WItemPollMap(DWSet dWSet, DB database) {
        super(dWSet, database,
                TYPE, "item_polls",
                ObserverMessage.WALLET_RESET_POLL_TYPE,
                ObserverMessage.WALLET_ADD_POLL_TYPE,
                ObserverMessage.WALLET_REMOVE_POLL_TYPE,
                ObserverMessage.WALLET_LIST_POLL_TYPE
        );
    }

}
