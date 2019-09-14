package org.erachain.dbs.nativeMemMap;

import org.erachain.database.DBASet;
import org.erachain.dbs.DBMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

public class nativeMapTreeMap<T, U> extends DCMapSuit<T, U>
{

    static Logger logger = LoggerFactory.getLogger(nativeMapTreeMap.class.getSimpleName());

    public nativeMapTreeMap(DBMap parent, DBASet databaseSet) {
        super(parent, databaseSet);
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

    protected U getDefaultValue() {
        return null;
    }

}
