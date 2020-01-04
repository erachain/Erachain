package org.erachain.datachain;

import org.mapdb.DB;

/**
 * see datachain.IssueItemMap
 */
public class IssueAssetMap extends IssueItemMap {

    public IssueAssetMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public IssueAssetMap(IssueAssetMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
