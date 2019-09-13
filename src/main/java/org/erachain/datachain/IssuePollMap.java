package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.BTreeKeySerializer;
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

    @Override
    protected void getMap() {
        //OPEN MAP
        map = database.createTreeMap("poll_OrphanData")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .counterEnable()
                .makeOrGet();
    }

}
