package org.erachain.dbs.nativeMemMap;

import org.erachain.database.DBASet;
import org.erachain.dbs.DBMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

public class nativeMapTreeMap<T, U> extends DCMapSuit<T, U>
{

    static Logger logger = LoggerFactory.getLogger(nativeMapTreeMap.class.getSimpleName());

    private U DEFAULT_VALUE;
    public nativeMapTreeMap(DBMap parent, DBASet databaseSet, U defaultValue) {
        super(parent, databaseSet);
        DEFAULT_VALUE = defaultValue;
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
