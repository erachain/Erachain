package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

// found by hash -> record signature
// TODO: переделать ссылку на транзакцию на Long
public class HashesMap extends DCMap<byte[], byte[]> {
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public HashesMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public HashesMap(HashesMap parent) {
        super(parent, null);
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<byte[], byte[]> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("hashes_keys")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected Map<byte[], byte[]> getMemoryMap() {
        return new TreeMap<byte[], byte[]>(UnsignedBytes.lexicographicalComparator());
    }

    @Override
    protected byte[] getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

}
