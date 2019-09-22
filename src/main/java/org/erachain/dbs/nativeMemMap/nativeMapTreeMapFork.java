package org.erachain.dbs.nativeMemMap;

import org.erachain.database.DBASet;
import org.erachain.dbs.DBTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

public class nativeMapTreeMapFork<T, U> extends DBMapSuitFotk<T, U>
{

    static Logger logger = LoggerFactory.getLogger(nativeMapTreeMapFork.class.getSimpleName());

    private U DEFAULT_VALUE;
    public nativeMapTreeMapFork(DBTab parent, DBASet databaseSet, U defaultValue) {
        super(parent, databaseSet);
        DEFAULT_VALUE = defaultValue;
    }
    public nativeMapTreeMapFork(DBTab parent, DBASet databaseSet) {
        super(parent, databaseSet);
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
