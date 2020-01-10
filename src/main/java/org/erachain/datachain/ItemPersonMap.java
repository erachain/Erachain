package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Персона<br>
 */

public class ItemPersonMap extends ItemMap {

    public ItemPersonMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                ItemCls.PERSON_TYPE, ObserverMessage.RESET_PERSON_TYPE,
                ObserverMessage.ADD_PERSON_TYPE,
                ObserverMessage.REMOVE_PERSON_TYPE,
                ObserverMessage.LIST_PERSON_TYPE
        );
    }

    public ItemPersonMap(ItemMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
