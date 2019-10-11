package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Шаблон<br>
 */
public class ItemTemplateMap extends ItemMap {
    static final String NAME = "item_templates";
    private static final int TYPE = ItemCls.TEMPLATE_TYPE;

    public ItemTemplateMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                "item_templates",
                ObserverMessage.RESET_TEMPLATE_TYPE,
                ObserverMessage.ADD_TEMPLATE_TYPE,
                ObserverMessage.REMOVE_TEMPLATE_TYPE,
                ObserverMessage.LIST_TEMPLATE_TYPE
        );
    }

    public ItemTemplateMap(ItemTemplateMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    // type+name not initialized yet! - it call as Super in New
    protected void openMap() {
        //OPEN MAP
        map = database.createTreeMap(NAME)
                .valueSerializer(new ItemSerializer(TYPE))
                .makeOrGet();
    }

}
