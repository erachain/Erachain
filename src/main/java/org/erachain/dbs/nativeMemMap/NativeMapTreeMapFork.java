package org.erachain.dbs.nativeMemMap;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.ForkedMap;

import java.util.Comparator;
import java.util.TreeMap;

@Slf4j
public class NativeMapTreeMapFork<T, U> extends DBMapSuitFork<T, U> implements ForkedMap {

    public NativeMapTreeMapFork(DBTab parent, DBASet databaseSet, Comparator comparator, U defaultValue) {
        super(parent, databaseSet, comparator, logger, defaultValue);
    }

    @Override
    protected void openMap() {

        // OPEN MAP
        if (COMPARATOR == null) {
            map = new TreeMap<T, U>();
        } else {
            map = new TreeMap<T, U>(COMPARATOR);
        }

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
    }

}
