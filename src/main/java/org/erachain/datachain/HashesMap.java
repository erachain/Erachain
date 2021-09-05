package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.TreeMap;

/**
 * found by hash -> record signature
 * TODO: переделать ссылку на транзакцию на Long
 */
public class HashesMap extends DCUMap<byte[], byte[]> {

    public HashesMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public HashesMap(HashesMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap("hashes_keys")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<byte[], byte[]>(UnsignedBytes.lexicographicalComparator());
    }

}
