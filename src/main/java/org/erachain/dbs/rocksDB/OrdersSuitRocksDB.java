package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.item.assets.Order;
import org.erachain.database.DBASet;
import org.erachain.datachain.OrderMapSuit;
import org.mapdb.DB;

import java.util.ArrayList;

@Slf4j
public class OrdersSuitRocksDB extends DBMapSuit<Long, Order> implements OrderMapSuit {

    private final String NAME_TABLE = "BLOCKS_TABLE";

    public OrdersSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
    }

    @Override
    protected void getMap() {


    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
        // SIZE need count - make not empty LIST
        indexes = new ArrayList<>();
    }

}
