package org.erachain.dbs.nativeMemMap;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.DBTab;

import java.util.Comparator;
import java.util.TreeMap;

@Slf4j
public class nativeMapTreeMapFork<T, U> extends DBMapSuitFork<T, U>
{

    public nativeMapTreeMapFork(DBTab parent, DBASet databaseSet, Comparator comparator, U defaultValue) {
        super(parent, databaseSet, comparator, logger, defaultValue);
    }

    @Override
    protected void openMap() {

        // OPEN MAP
        if (COMPARATOR == null) {
            map = new TreeMap<T, U>();
        } else {
            map = new TreeMap<T, U>(COMPARATOR);
            ///map = new HashMap<T, U>(Hasher.BYTE_ARRAY);
        }

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
    }

}
