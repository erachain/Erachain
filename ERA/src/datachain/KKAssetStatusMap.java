package datachain;

import org.mapdb.DB;
import utils.ObserverMessage;

public class KKAssetStatusMap extends KK_Map {
    public KKAssetStatusMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "asset_status",
                ObserverMessage.RESET_ASSET_STATUS_TYPE,
                ObserverMessage.ADD_ASSET_STATUS_TYPE,
                ObserverMessage.REMOVE_ASSET_STATUS_TYPE,
                ObserverMessage.LIST_ASSET_STATUS_TYPE
        );
    }

    public KKAssetStatusMap(KKAssetStatusMap parent) {
        super(parent);
    }


}
