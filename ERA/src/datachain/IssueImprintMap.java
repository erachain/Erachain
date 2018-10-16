package datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.Map;

/**
 * see datachain.Issue_ItemMap
 *
 * @return
 */

public class IssueImprintMap extends Issue_ItemMap {

    public IssueImprintMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public IssueImprintMap(IssueImprintMap parent) {
        super(parent);
    }

    @Override
    protected Map<byte[], Long> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("imprint_OrphanData")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .counterEnable()
                .makeOrGet();
    }

}
