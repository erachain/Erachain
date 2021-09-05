package org.erachain.core.web;

import org.apache.commons.lang3.StringUtils;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.DCUMap;
import org.mapdb.DB;
import org.mapdb.DB.BTreeMapMaker;

import java.util.*;

@Deprecated
public class NameStorageMap extends DCUMap<String, Map<String, String>> {

    public NameStorageMap(DCSet dcSet, DB database) {
        super(dcSet, database);
    }

    public NameStorageMap(DCUMap<String, Map<String, String>> parent) {
        super(parent, null);
    }

    @Override
    public void openMap() {
        // OPEN MAP
        BTreeMapMaker createTreeMap = database.createTreeMap("NameStorageMap");
        map = createTreeMap.makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<String, Map<String, String>>();
    }

    public void add(String name, String key, String value) {
        Map<String, String> keyValueMap = this.get(name);
        if (keyValueMap == null) {
            keyValueMap = new HashMap<String, String>();
        }

        keyValueMap.put(key, value);

        this.put(name, keyValueMap);
    }

    public void addListEntries(String name, String key,
                               List<String> entriesToAdd) {
        Map<String, String> keyValueMap = this.get(name);
        if (keyValueMap == null) {
            keyValueMap = new HashMap<String, String>();
        }

        String currentListAsString = keyValueMap.get(key);
        List<String> currentList = new ArrayList<String>();
        if (currentListAsString != null) {
            currentList = new ArrayList<String>(Arrays.asList(StringUtils
                    .split(currentListAsString, ";")));
        }
        for (String entry : entriesToAdd) {
            if (!currentList.contains(entry)) {
                currentList.add(entry);
            }
        }

        String joinedResults = StringUtils.join(currentList, ";");

        keyValueMap.put(key, joinedResults);

        this.put(name, keyValueMap);
    }

    public void removeListEntries(String name, String key,
                                  List<String> entriesToRemove) {
        Map<String, String> keyValueMap = this.get(name);
        if (keyValueMap == null) {
            return;
        }

        String currentListAsString = keyValueMap.get(key);
        if (currentListAsString == null) {
            return;
        }
        List<String> currentList = new ArrayList<String>(
                Arrays.asList(StringUtils.split(currentListAsString, ";")));
        for (String entry : entriesToRemove) {
            currentList.remove(entry);
        }

        String joinedResults = StringUtils.join(currentList, ";");

        if (joinedResults.isEmpty()) {
            keyValueMap.remove(key);
        } else {
            keyValueMap.put(key, joinedResults);
        }


        this.put(name, keyValueMap);
    }

    public void remove(String name, String key) {
        Map<String, String> keyValueMap = this.get(name);
        if (keyValueMap != null) {
            keyValueMap.remove(key);
        }
        this.put(name, keyValueMap);
    }

    public String getOpt(String name, String key) {
        Map<String, String> keyValueMap = this.get(name);
        if (keyValueMap == null) {
            return null;
        }

        return keyValueMap.get(key);
    }

}
