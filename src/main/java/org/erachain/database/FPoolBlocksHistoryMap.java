package org.erachain.database;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.DCUMapImpl;
import org.mapdb.BTreeMap;
import org.mapdb.DB;

import java.util.Map;
import java.util.TreeMap;

/**
 * Height -> block.getSignature(), credits, results
 */
@Slf4j
public class FPoolBlocksHistoryMap extends DCUMapImpl<Integer, Object[]> {

    public FPoolBlocksHistoryMap(DPSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    @Override
    public void openMap() {

        //OPEN MAP
        map = database.createTreeMap("pool_blocks_history")
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<Integer, Object[]>();
    }

    public Map.Entry<Integer, Object[]> lastEntry() {
        return ((BTreeMap) map).lastEntry();
    }
}
