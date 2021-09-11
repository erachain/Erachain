package org.erachain.database;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.DCUMapImpl;
import org.mapdb.DB;

import java.util.TreeMap;

/**
 *
 */
@Slf4j
public class FPoolBlocksMap extends DCUMapImpl<Integer, Object[]> {

    public FPoolBlocksMap(DPSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    @Override
    public void openMap() {

        sizeEnable = true; // разрешаем счет размера - это будет немного тормозить работу

        //OPEN MAP
        map = database.createTreeMap("pool_blocks")
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<Integer, Object[]>();
    }

}
