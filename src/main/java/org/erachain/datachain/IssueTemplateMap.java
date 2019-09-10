package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.Map;

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

    @Override
    protected void getMap(DB database) {
        //OPEN MAP
        map = database.createTreeMap("template_OrphanData")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .counterEnable()
                .makeOrGet();
    }

}
