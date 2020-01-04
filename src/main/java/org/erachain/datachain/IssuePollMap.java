package org.erachain.datachain;

import org.mapdb.DB;

/**
 * see datachain.IssueItemMap
 */

public class IssuePollMap extends IssueItemMap {

    public IssuePollMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public IssuePollMap(IssuePollMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
