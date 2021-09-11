package org.erachain.database;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.dbs.DCUMapImpl;
import org.json.simple.JSONObject;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 *
 */
@Slf4j
public class FPoolBalancesMap extends DCUMapImpl<Tuple2<Long, String>, BigDecimal> {

    private NavigableMap<Tuple2<String, Long>, Tuple2<Long, String>> addressBals;

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
    protected void createIndexes() {

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        this.addressBals = database.createTreeMap("pool_addr_bals")
                .comparator(Fun.TUPLE2_COMPARATOR).makeOrGet();

        //BIND CREDITPRS KEY
        Bind.secondaryKey((BTreeMap) map, this.addressBals, new Fun.Function2<Tuple2<String, Long>,
                Tuple2<Long, String>, BigDecimal>() {
            @Override
            public Tuple2<String, Long> run(Tuple2<Long, String> key, BigDecimal value) {
                return new Tuple2<String, Long>(key.b, key.a);
            }
        });

    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<Tuple2<Long, String>, BigDecimal>();
    }

    public JSONObject getAddressBalances(String address) {
        Iterator<Tuple2<Long, String>> iterator = addressBals.subMap(new Tuple2(address, null), new Tuple2(address, Fun.HI)).values().iterator();
        JSONObject out = new JSONObject();
        Tuple2<Long, String> key;
        while (iterator.hasNext()) {
            key = iterator.next();
            out.put(key.a, map.get(key));
        }

        return out;
    }

}
