package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

public class WItemImprintMap extends WItemMap {

    //static Logger logger = LoggerFactory.getLogger(WItemImprintMap.class.getName());
    static final String NAME = "imprint";
    static final int TYPE = ItemCls.IMPRINT_TYPE;


    public WItemImprintMap(DWSet dWSet, DB database) {
        super(dWSet, database,
                TYPE, "item_imprints",
                ObserverMessage.WALLET_RESET_IMPRINT_TYPE,
                ObserverMessage.WALLET_ADD_IMPRINT_TYPE,
                ObserverMessage.WALLET_REMOVE_IMPRINT_TYPE,
                ObserverMessage.WALLET_LIST_IMPRINT_TYPE
        );
    }
}
