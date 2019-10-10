package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.BTreeKeySerializer;
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

    @Override
    protected void openMap() {
        //OPEN MAP
        map = database.createTreeMap("status_OrphanData")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .counterEnable()
                .makeOrGet();
    }

}
