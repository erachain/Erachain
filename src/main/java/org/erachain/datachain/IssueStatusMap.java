package org.erachain.datachain;

import org.mapdb.DB;

/**
 * see datachain.IssueItemMap
 */

public class IssueStatusMap extends IssueItemMap {

    public IssueStatusMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public IssueStatusMap(IssueStatusMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
