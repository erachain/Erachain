package datachain;

import core.item.ItemCls;
import database.serializer.ItemSerializer;
import org.mapdb.DB;
import utils.ObserverMessage;

import java.util.Map;

//import java.util.HashMap;
//import java.util.Map;
//import org.mapdb.Atomic;

public class ItemUnionMap extends Item_Map {

    static final String NAME = "item_unions";
    static final int TYPE = ItemCls.UNION_TYPE;

    public ItemUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                //TYPE,
                NAME,
                ObserverMessage.RESET_UNION_TYPE,
                ObserverMessage.ADD_UNION_TYPE,
                ObserverMessage.REMOVE_UNION_TYPE,
                ObserverMessage.LIST_UNION_TYPE
        );

    }

    public ItemUnionMap(ItemUnionMap parent) {
        super(parent);
    }

    // type+name not initialized yet! - it call as Super in New
    protected Map<Long, ItemCls> getMap(DB database) {

        //OPEN MAP
        return database.createTreeMap(NAME)
                .valueSerializer(new ItemSerializer(TYPE))
                //.valueSerializer(new AssetSerializer())
                .makeOrGet();
    }

}
