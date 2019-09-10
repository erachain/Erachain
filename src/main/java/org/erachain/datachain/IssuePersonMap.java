package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.Map;

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

    @Override
    protected void getMap(DB database) {
        //OPEN MAP
        map = database.createTreeMap("person_OrphanData")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .counterEnable()
                .makeOrGet();
    }

}
