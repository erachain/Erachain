package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.mapdb.DB;

/**
 * see datachain.IssueItemMap
 */
public class IssueAssetMap extends IssueItemMap {

    public IssueAssetMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, ItemCls.ASSET_TYPE);
    }

    public IssueAssetMap(IssueAssetMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
