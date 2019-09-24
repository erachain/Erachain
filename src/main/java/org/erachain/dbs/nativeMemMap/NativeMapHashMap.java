package org.erachain.dbs.nativeMemMap;

import org.erachain.database.DBASet;
import org.erachain.dbs.mapDB.DBMapSuit;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class NativeMapHashMap<T, U> extends DBMapSuit<T, U> {

    static Logger logger = LoggerFactory.getLogger(NativeMapHashMap.class.getSimpleName());

    private U DEFAULT_VALUE;

    public NativeMapHashMap(DBASet databaseSet, DB database, U defaultValue) {
        super(databaseSet, database);
        DEFAULT_VALUE = defaultValue;
    }

    @Override
    protected void getMap() {

        // OPEN MAP
        map = new HashMap<T, U>();

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
    }

    public U getDefaultValue() {
        return DEFAULT_VALUE;
    }

}
