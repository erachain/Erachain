package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.mapdb.DB;
import org.mapdb.Fun;


@Slf4j
public class ItemsValuesMapDB extends DBMapSuit<Fun.Tuple3<Long, Byte, byte[]>, byte[]> {

    public ItemsValuesMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, false);
    }

    @Override
    public void openMap() {

        //OPEN MAP
        map = database.createTreeMap("items_values")
                .comparator(new Fun.Tuple3Comparator<>(Fun.COMPARATOR, Fun.COMPARATOR, Fun.BYTE_ARRAY_COMPARATOR))
                .makeOrGet();

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        ///////////////////// HERE NOT PROTOCOL INDEXES

    }

}
