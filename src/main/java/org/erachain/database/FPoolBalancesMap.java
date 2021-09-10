package org.erachain.database;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.DCUMapImpl;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.TreeMap;

/**
 *
 */
@Slf4j
public class FPoolBalancesMap extends DCUMapImpl<Fun.Tuple2<Long, String>, BigDecimal> {

    public FPoolBalancesMap(DPSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap("pool_bals")
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<Fun.Tuple2<Long, String>, BigDecimal>();
    }

}
