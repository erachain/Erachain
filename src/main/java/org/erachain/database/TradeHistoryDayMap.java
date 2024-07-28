package org.erachain.database;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.DCUMapImpl;
import org.mapdb.BTreeMap;
import org.mapdb.DB;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
@Slf4j
public class TradeHistoryDayMap extends DCUMapImpl<String, BigDecimal[]> {

    public TradeHistoryDayMap(DTHSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap("day")
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<String, BigDecimal[]>();
    }

    @Override
    public BigDecimal[] getDefaultValue(String key) {
        return new BigDecimal[5];
    }

    public Map<String, BigDecimal[]> getPoints(String pair) {
        return ((BTreeMap) this.map).subMap(pair + " ", pair + " 99");
    }
}
