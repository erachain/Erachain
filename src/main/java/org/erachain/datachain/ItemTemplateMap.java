package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Шаблон<br>
 */
public class ItemTemplateMap extends ItemMap {

    public ItemTemplateMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                ItemCls.TEMPLATE_TYPE, ObserverMessage.RESET_TEMPLATE_TYPE,
                ObserverMessage.ADD_TEMPLATE_TYPE,
                ObserverMessage.REMOVE_TEMPLATE_TYPE,
                ObserverMessage.LIST_TEMPLATE_TYPE
        );
    }

    public ItemTemplateMap(ItemTemplateMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
