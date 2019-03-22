package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.Map;

/**
 * see datachain.IssueItemMap
 */

public class IssueStatusMap extends IssueItemMap {

    public IssueStatusMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public IssueStatusMap(IssueStatusMap parent) {
        super(parent);
    }

    @Override
    protected Map<byte[], Long> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("status_OrphanData")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .counterEnable()
                .makeOrGet();
    }

}
