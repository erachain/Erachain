package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.DB;
import org.mapdb.Hasher;
import org.mapdb.SerializerBase;

import java.util.Map;
import java.util.TreeMap;


/**
 * seek reference to tx_Parent by address+timestamp
 * account.address -> LAST[TX.timestamp + TX.dbRef]
 * account.address + TX.timestamp -> PARENT[TX.timestamp + TX.dbRef]
 *
 */
public class ReferenceMap extends DCMap<byte[], long[]> {

    public ReferenceMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public ReferenceMap(ReferenceMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    protected void getMap(DB database) {
        //OPEN MAP
        map = database.createHashMap("references")
                .keySerializer(SerializerBase.BASIC)
                .hasher(Hasher.BASIC)
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected Map<byte[], long[]> getMemoryMap() {
        map = new TreeMap<>(UnsignedBytes.lexicographicalComparator());
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected long[] getDefaultValue() {
        // NEED for toByte for not referenced accounts
        return null;
    }

}