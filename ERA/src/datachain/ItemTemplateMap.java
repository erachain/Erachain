package datachain;

import core.item.ItemCls;
import database.serializer.ItemSerializer;
import org.mapdb.DB;
import utils.ObserverMessage;

import java.util.Map;

//import java.util.HashMap;
//import java.util.Map;
//import org.mapdb.Atomic;
//import database.serializer.ItemSerializer;

public class ItemTemplateMap extends Item_Map {
    static final String NAME = "item_templates";
    static final int TYPE = ItemCls.TEMPLATE_TYPE;

    public ItemTemplateMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                //TYPE,
                "item_templates",
                ObserverMessage.RESET_TEMPLATE_TYPE,
                ObserverMessage.ADD_TEMPLATE_TYPE,
                ObserverMessage.REMOVE_TEMPLATE_TYPE,
                ObserverMessage.LIST_TEMPLATE_TYPE
        );
    }

    public ItemTemplateMap(ItemTemplateMap parent) {
        super(parent);
    }

    // type+name not initialized yet! - it call as Super in New
    protected Map<Long, ItemCls> getMap(DB database) {

        //OPEN MAP
        return database.createTreeMap(NAME)
                .valueSerializer(new ItemSerializer(TYPE))
                .makeOrGet();
    }

}
