package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

import java.util.Map;


/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Уникальный Отпечаток<br>
 */

public class ItemImprintMap extends ItemMap {
    static final String NAME = "item_imprints";
    private static final int TYPE = ItemCls.IMPRINT_TYPE;

    public ItemImprintMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                "item_imprints",
                ObserverMessage.RESET_IMPRINT_TYPE,
                ObserverMessage.ADD_IMPRINT_TYPE,
                ObserverMessage.REMOVE_IMPRINT_TYPE,
                ObserverMessage.LIST_IMPRINT_TYPE
        );
    }

    public ItemImprintMap(ItemImprintMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    // type+name not initialized yet! - it call as Super in New
    protected void getMap(DB database) {
        //OPEN MAP
        map = database.createTreeMap(NAME)
                .valueSerializer(new ItemSerializer(TYPE))
                .makeOrGet();
    }

}
