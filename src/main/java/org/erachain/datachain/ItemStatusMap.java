package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Стату<br>
 */
public class ItemStatusMap extends ItemMap {

    static final String NAME = "item_statuses";
    private static final int TYPE = ItemCls.STATUS_TYPE;

    public ItemStatusMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                NAME,
                ObserverMessage.RESET_STATUS_TYPE,
                ObserverMessage.ADD_STATUS_TYPE,
                ObserverMessage.REMOVE_STATUS_TYPE,
                ObserverMessage.LIST_STATUS_TYPE
        );
    }

    public ItemStatusMap(ItemStatusMap parent, DCSet dcSet) {
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
