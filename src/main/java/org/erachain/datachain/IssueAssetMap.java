package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.Map;

/**
 * see datachain.Issue_ItemMap
 *
 * @return
 */
public class IssueAssetMap extends Issue_ItemMap {

    public IssueAssetMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public IssueAssetMap(IssueAssetMap parent) {
        super(parent);
    }

    @Override
    protected Map<byte[], Long> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("asset_OrphanData")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .counterEnable()
                .makeOrGet();
    }

}
