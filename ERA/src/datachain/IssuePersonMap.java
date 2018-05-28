package datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.Map;

public class IssuePersonMap extends Issue_ItemMap {

    public IssuePersonMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public IssuePersonMap(IssuePersonMap parent) {
        super(parent);
    }

    @Override
    protected Map<byte[], Long> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("person_OrphanData")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .counterEnable()
                .makeOrGet();
    }

}
