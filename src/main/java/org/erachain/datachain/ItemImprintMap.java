package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;


/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Уникальный Отпечаток<br>
 */

public class ItemImprintMap extends ItemMap {

    public ItemImprintMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                ItemCls.IMPRINT_TYPE, ObserverMessage.RESET_IMPRINT_TYPE,
                ObserverMessage.ADD_IMPRINT_TYPE,
                ObserverMessage.REMOVE_IMPRINT_TYPE,
                ObserverMessage.LIST_IMPRINT_TYPE
        );
    }

    public ItemImprintMap(ItemImprintMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
