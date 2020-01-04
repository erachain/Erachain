package org.erachain.datachain;

import org.mapdb.DB;

/**
 * see datachain.IssueItemMap
 */

public class IssueImprintMap extends IssueItemMap {

    public IssueImprintMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public IssueImprintMap(IssueImprintMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
