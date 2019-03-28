package org.erachain.core.web;

import com.google.common.primitives.SignedBytes;
import org.erachain.datachain.DCMap;
import org.erachain.datachain.DCSet;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.Map;

public class OrphanNameStorageMap extends DCMap<byte[], Map<String, String>> {

    public OrphanNameStorageMap(DCSet dcSet, DB database) {
        super(dcSet, database);
    }

    public OrphanNameStorageMap(DCMap<byte[], Map<String, String>> parent) {
        super(parent, null);
    }

    @Override
    protected Map<byte[], Map<String, String>> getMap(DB database) {

        return database.createTreeMap("OrphanNameStorageMap")
                .comparator(SignedBytes.lexicographicalComparator())
                .makeOrGet();

    }

    @Override
    protected Map<byte[], Map<String, String>> getMemoryMap() {
        return new HashMap<byte[], Map<String, String>>();
    }

    @Override
    protected Map<String, String> getDefaultValue() {
        return null;
    }

    @Override
    protected void createIndexes(DB database) {
    }

    public void add(byte[] txAndName, String key, String value) {
        Map<String, String> keyValueMap = this.get(txAndName);
        if (keyValueMap == null) {
            keyValueMap = new HashMap<String, String>();
        }

        keyValueMap.put(key, value);

        this.set(txAndName, keyValueMap);

    }

}
