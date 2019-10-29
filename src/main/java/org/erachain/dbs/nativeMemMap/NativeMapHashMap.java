package org.erachain.dbs.nativeMemMap;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.mapDB.DBMapSuit;
import org.mapdb.DB;

import java.util.HashMap;

@Slf4j
public class NativeMapHashMap<T, U> extends DBMapSuit<T, U> {

    public NativeMapHashMap(DBASet databaseSet, DB database, U defaultValue) {
        super(databaseSet, database, logger, defaultValue);
    }

    @Override
    public void openMap() {

        // OPEN MAP
        map = new HashMap<T, U>();

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
    }

}
