package org.erachain.datachain;

import org.mapdb.DB;

/**
 * see datachain.IssueItemMap
 */

public class IssueTemplateMap extends IssueItemMap {

    public IssueTemplateMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public IssueTemplateMap(IssueTemplateMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
