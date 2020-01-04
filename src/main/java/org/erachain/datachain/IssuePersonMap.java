package org.erachain.datachain;

import org.mapdb.DB;

/**
 * see datachain.IssueItemMap
 */

public class IssuePersonMap extends IssueItemMap {

    public IssuePersonMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public IssuePersonMap(IssuePersonMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
