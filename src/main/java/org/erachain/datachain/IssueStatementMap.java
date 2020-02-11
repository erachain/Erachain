package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.mapdb.DB;

/**
 * see datachain.IssueItemMap
 */

public class IssueStatementMap extends IssueItemMap {

    public IssueStatementMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, ItemCls.STATEMENT_TYPE);
    }

    public IssueStatementMap(IssueStatementMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
