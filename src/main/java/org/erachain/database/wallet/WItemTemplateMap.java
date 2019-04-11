package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

public class WItemTemplateMap extends WItemMap {

    //static Logger logger = LoggerFactory.getLogger(WItemTemplateMap.class.getName());
    static final String NAME = "template";
    static final int TYPE = ItemCls.TEMPLATE_TYPE;


    public WItemTemplateMap(DWSet dWSet, DB database) {
        super(dWSet, database,
                TYPE, "item_templates",
                ObserverMessage.WALLET_RESET_TEMPLATE_TYPE,
                ObserverMessage.WALLET_ADD_TEMPLATE_TYPE,
                ObserverMessage.WALLET_REMOVE_TEMPLATE_TYPE,
                ObserverMessage.WALLET_LIST_TEMPLATE_TYPE
        );
    }

}
