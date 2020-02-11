package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Стату<br>
 */
public class ItemStatusMap extends ItemMap {

    public ItemStatusMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                ItemCls.STATUS_TYPE, ObserverMessage.RESET_STATUS_TYPE,
                ObserverMessage.ADD_STATUS_TYPE,
                ObserverMessage.REMOVE_STATUS_TYPE,
                ObserverMessage.LIST_STATUS_TYPE
        );
    }

    public ItemStatusMap(ItemStatusMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
