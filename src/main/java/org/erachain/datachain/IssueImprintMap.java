package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.mapdb.DB;

/**
 * see datachain.IssueItemMap
 */

public class IssueImprintMap extends IssueItemMap {

    public IssueImprintMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, ItemCls.IMPRINT_TYPE);
    }

    public IssueImprintMap(IssueImprintMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
