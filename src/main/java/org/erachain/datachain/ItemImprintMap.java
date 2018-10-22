package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

import java.util.Map;

//import java.util.HashMap;
//import java.util.Map;
//import org.mapdb.Atomic;
//import org.erachain.database.serializer.ItemSerializer;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Уникальный Отпечаток<br>
 */

public class ItemImprintMap extends Item_Map {
    static final String NAME = "item_imprints";
    static final int TYPE = ItemCls.IMPRINT_TYPE;

    public ItemImprintMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                //TYPE,
                "item_imprints",
                ObserverMessage.RESET_IMPRINT_TYPE,
                ObserverMessage.ADD_IMPRINT_TYPE,
                ObserverMessage.REMOVE_IMPRINT_TYPE,
                ObserverMessage.LIST_IMPRINT_TYPE
        );
    }

    public ItemImprintMap(ItemImprintMap parent) {
        super(parent);
    }

    // type+name not initialized yet! - it call as Super in New
    protected Map<Long, ItemCls> getMap(DB database) {

        //OPEN MAP
        return database.createTreeMap(NAME)
                .valueSerializer(new ItemSerializer(TYPE))
                //.valueSerializer(new ImprintSerializer())
                .makeOrGet();
    }

}
