package org.erachain.dbs;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple2;

import java.util.*;

@Slf4j
public abstract class DBMapSuitImpl<T, U> implements DBMapSuit<T, U> {

    protected abstract void getMap();

    @Override
    public int getDefaultIndex() {
        return 0;
    }

    protected void createIndexes() {
    }

}
