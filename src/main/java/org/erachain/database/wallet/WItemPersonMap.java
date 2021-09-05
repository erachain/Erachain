package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

public class WItemPersonMap extends WItemMap {

    static final String NAME = "person";
    static final int TYPE = ItemCls.PERSON_TYPE;


    public WItemPersonMap(DWSet dWSet, DB database) {
        super(dWSet, database,
                TYPE, NAME,
                ObserverMessage.WALLET_RESET_PERSON_TYPE,
                ObserverMessage.WALLET_ADD_PERSON_TYPE,
                ObserverMessage.WALLET_REMOVE_PERSON_TYPE,
                ObserverMessage.WALLET_LIST_PERSON_TYPE
        );
    }

}
