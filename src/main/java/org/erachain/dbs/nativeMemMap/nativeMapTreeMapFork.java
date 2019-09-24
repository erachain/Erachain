package org.erachain.dbs.nativeMemMap;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.DBTab;

import java.util.TreeMap;

@Slf4j
public class nativeMapTreeMapFork<T, U> extends DBMapSuitFork<T, U>
{

    private U DEFAULT_VALUE;

    public nativeMapTreeMapFork(DBTab parent, DBASet databaseSet, U defaultValue) {
        super(parent, databaseSet, logger);
        DEFAULT_VALUE = defaultValue;
    }

    public nativeMapTreeMapFork(DBTab parent, DBASet databaseSet) {
        super(parent, databaseSet, logger);
        DEFAULT_VALUE = null;
    }

    @Override
    protected void getMap() {

        // OPEN MAP
        map = new TreeMap<T, U>();

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
    }

    public U getDefaultValue() {
        return DEFAULT_VALUE;
    }

}
