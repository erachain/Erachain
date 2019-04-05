package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import java.util.Map;

public class WItemAssetMap extends WItemMap {
    //static Logger logger = LoggerFactory.getLogger(WItemAssetMap.class.getName());

    static final String NAME = "asset";
    static final int TYPE = ItemCls.ASSET_TYPE;

    public WItemAssetMap(DWSet dWSet, DB database) {
        super(dWSet, database,
                TYPE, NAME,
                ObserverMessage.WALLET_RESET_ASSET_TYPE,
                ObserverMessage.WALLET_ADD_ASSET_TYPE,
                ObserverMessage.WALLET_REMOVE_ASSET_TYPE,
                ObserverMessage.WALLET_LIST_ASSET_TYPE
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
