package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.mapdb.DB;

/**
 * see datachain.IssueItemMap
 */

public class IssueUnionMap extends IssueItemMap {

    public IssueUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, ItemCls.UNION_TYPE);
    }

    public IssueUnionMap(IssueUnionMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
