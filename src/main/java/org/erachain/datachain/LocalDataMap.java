package org.erachain.datachain;

import org.mapdb.DB;
import org.mapdb.DB.BTreeMapMaker;

import java.util.HashMap;
import java.util.Map;

/**
 * я так понял - это отслеживание версии базы данных - и если она новая то все удаляем и заново закачиваем
 */
public class LocalDataMap extends DCMap<String, String> {

    public static final String LOCAL_DATA_VERSION_KEY = "dataversion";

    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public LocalDataMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public LocalDataMap(DCMap<String, String> parent) {
        super(parent, null);
    }


    @Override
    protected Map<String, String> getMap(DB database) {
        /// OPEN MAP
        BTreeMapMaker createTreeMap = database.createTreeMap("LocalDataMap");
        return createTreeMap.makeOrGet();
    }

    @Override
    protected Map<String, String> getMemoryMap() {
        return new HashMap<String, String>();
    }


    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected String getDefaultValue() {
        return null;
    }

}
