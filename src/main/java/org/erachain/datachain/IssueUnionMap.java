package org.erachain.datachain;

import org.mapdb.DB;

/**
 * see datachain.IssueItemMap
 */

public class IssueUnionMap extends IssueItemMap {

    public IssueUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public IssueUnionMap(IssueUnionMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
