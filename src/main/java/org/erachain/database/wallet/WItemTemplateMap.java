package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.erachain.utils.ObserverMessage;

import java.util.Map;

public class WItemTemplateMap extends WItem_Map {

    //static Logger LOGGER = Logger.getLogger(WItemTemplateMap.class.getName());
    static final String NAME = "template";
    static final int TYPE = ItemCls.TEMPLATE_TYPE;


    public WItemTemplateMap(DWSet dWSet, DB database) {
        super(dWSet, database,
                TYPE, "item_templates",
                ObserverMessage.WALLET_RESET_TEMPLATE_TYPE,
                ObserverMessage.WALLET_ADD_TEMPLATE_TYPE,
                ObserverMessage.WALLET_REMOVE_TEMPLATE_TYPE,
                ObserverMessage.WALLET_LIST_TEMPLATE_TYPE
        );
    }

    public WItemTemplateMap(WItemTemplateMap parent) {
        super(parent);
    }

    @Override
    // type+name not initialized yet! - it call as Super in New
    protected Map<Tuple2<String, String>, ItemCls> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap(NAME)
                .keySerializer(BTreeKeySerializer.TUPLE2)
                .valueSerializer(new ItemSerializer(TYPE))
                .counterEnable()
                .makeOrGet();
    }

}
