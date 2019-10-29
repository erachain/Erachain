package org.erachain.dbs.nativeMemMap;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.ForkedMap;

import java.util.HashMap;

@Slf4j
public class NativeMapHashMapFork<T, U> extends DBMapSuitFork<T, U> implements ForkedMap {

    public NativeMapHashMapFork(DBTab parent, DBASet databaseSet, U defaultValue) {
        super(parent, databaseSet, null, logger, defaultValue);
    }

    @Override
    public void openMap() {

        // OPEN MAP
        map = new HashMap<T, U>();
        ///map = new HashMap<T, U>(Hasher.BYTE_ARRAY); - MapDB

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
    }

}
