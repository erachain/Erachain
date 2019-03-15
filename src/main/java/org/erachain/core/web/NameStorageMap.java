package org.erachain.core.web;

import org.erachain.datachain.DCMap;
import org.erachain.datachain.DCSet;
import org.apache.commons.lang3.StringUtils;
import org.mapdb.DB;
import org.mapdb.DB.BTreeMapMaker;

import java.util.*;

public class NameStorageMap extends DCMap<String, Map<String, String>> {

    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public NameStorageMap(DCSet dcSet, DB database) {
        super(dcSet, database);
    }

    public NameStorageMap(DCMap<String, Map<String, String>> parent) {
        super(parent, null);
    }

    @Override
    protected Map<String, Map<String, String>> getMap(DB database) {
        // OPEN MAP
        BTreeMapMaker createTreeMap = database.createTreeMap("NameStorageMap");
        return createTreeMap.makeOrGet();
    }

    @Override
    protected Map<String, Map<String, String>> getMemoryMap() {
        return new HashMap<String, Map<String, String>>();
    }

    @Override
    protected Map<String, String> getDefaultValue() {
        return null;
    }

    protected void createIndexes(DB database) {
    }

    public void add(String name, String key, String value) {
        Map<String, String> keyValueMap = this.get(name);
        if (keyValueMap == null) {
            keyValueMap = new HashMap<String, String>();
        }

        keyValueMap.put(key, value);

        this.set(name, keyValueMap);
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

        this.set(name, keyValueMap);
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


        this.set(name, keyValueMap);
    }

    public void remove(String name, String key) {
        Map<String, String> keyValueMap = this.get(name);
        if (keyValueMap != null) {
            keyValueMap.remove(key);
        }
        this.set(name, keyValueMap);
    }

    public String getOpt(String name, String key) {
        Map<String, String> keyValueMap = this.get(name);
        if (keyValueMap == null) {
            return null;
        }

        return keyValueMap.get(key);
    }

}
