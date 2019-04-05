package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

public class WItemStatusMap extends WItemMap {

    //static Logger logger = LoggerFactory.getLogger(WItemStatusMap.class.getName());
    static final String NAME = "status";
    static final int TYPE = ItemCls.STATUS_TYPE;


    public WItemStatusMap(DWSet dWSet, DB database) {
        super(dWSet, database,
                TYPE, "item_statuses",
                ObserverMessage.WALLET_RESET_STATUS_TYPE,
                ObserverMessage.WALLET_ADD_STATUS_TYPE,
                ObserverMessage.WALLET_REMOVE_STATUS_TYPE,
                ObserverMessage.WALLET_LIST_STATUS_TYPE
        );
    }

}
