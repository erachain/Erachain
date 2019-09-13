package org.erachain.datachain;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

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

    @Override
    protected void getMap() {
        //OPEN MAP
        map = database.createTreeMap("statement_OrphanData")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .counterEnable()
                .makeOrGet();
    }

}
