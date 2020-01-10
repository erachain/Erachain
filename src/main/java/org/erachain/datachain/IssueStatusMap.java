package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.mapdb.DB;

/**
 * see datachain.IssueItemMap
 */

public class IssueStatusMap extends IssueItemMap {

    public IssueStatusMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, ItemCls.STATUS_TYPE);
    }

    public IssueStatusMap(IssueStatusMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
