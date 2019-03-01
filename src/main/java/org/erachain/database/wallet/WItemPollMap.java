package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import java.util.Map;

public class WItemPollMap extends WItem_Map {

    static final String NAME = "poll";
    static final int TYPE = ItemCls.UNION_TYPE;


    public WItemPollMap(DWSet dWSet, DB database) {
        super(dWSet, database,
                TYPE, "item_unions",
                ObserverMessage.WALLET_RESET_POLL_TYPE,
                ObserverMessage.WALLET_ADD_POLL_TYPE,
                ObserverMessage.WALLET_REMOVE_POLL_TYPE,
                ObserverMessage.WALLET_LIST_POLL_TYPE
        );
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
