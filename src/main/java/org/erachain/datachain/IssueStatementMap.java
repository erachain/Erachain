package org.erachain.datachain;

import org.mapdb.DB;

/**
 * see datachain.IssueItemMap
 */

public class IssueStatementMap extends IssueItemMap {

    public IssueStatementMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public IssueStatementMap(IssueStatementMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
