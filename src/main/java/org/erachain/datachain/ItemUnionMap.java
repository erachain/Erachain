package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Объединение<br>
 */
public class ItemUnionMap extends ItemMap {

    public ItemUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                ItemCls.UNION_TYPE, ObserverMessage.RESET_UNION_TYPE,
                ObserverMessage.ADD_UNION_TYPE,
                ObserverMessage.REMOVE_UNION_TYPE,
                ObserverMessage.LIST_UNION_TYPE
        );

    }

    public ItemUnionMap(ItemUnionMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
