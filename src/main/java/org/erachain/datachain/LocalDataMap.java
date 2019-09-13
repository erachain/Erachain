package org.erachain.datachain;

import org.mapdb.DB;
import org.mapdb.DB.BTreeMapMaker;

import java.util.HashMap;

/**
 * я так понял - это отслеживание версии базы данных - и если она новая то все удаляем и заново закачиваем
 */
public class LocalDataMap extends DCMap<String, String> {

    public static final String LOCAL_DATA_VERSION_KEY = "dataversion";

    public LocalDataMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public LocalDataMap(DCMap<String, String> parent) {
        super(parent, null);
    }


    @Override
    protected void getMap() {
        /// OPEN MAP
        BTreeMapMaker createTreeMap = database.createTreeMap("LocalDataMap");
        map = createTreeMap.makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<String, String>();
    }


    @Override
    protected void createIndexes() {
    }

    @Override
    protected String getDefaultValue() {
        return null;
    }

}
