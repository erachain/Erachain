package org.erachain.controller;

import org.erachain.database.DTHSet;
import org.erachain.database.TradeHistoryDayMap;
import org.erachain.dbs.DCUMapImpl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

public class TradeHistoryMakerD extends TradeHistoryMaker {

    protected TradeHistoryDayMap tradesHistoryMap;

    TradeHistoryMakerD(Controller cnt, DTHSet dthSet) {
        super(cnt, dthSet);
        // один день
        diffPeriod = 60000L * 60L * 24L;
        tradesHistoryMap = dthSet.getTradesHistoryDayMap();

    }

    @Override
    public DCUMapImpl<String, BigDecimal[]> getMap() {
        return tradesHistoryMap;
    }

    @Override
    String makeKey(String pair, Timestamp pointTimeStamp) {
        return String.format("%s %4d-%02d-%02d", pair, pointTimeStamp.getYear() + 1900, pointTimeStamp.getMonth() + 1, pointTimeStamp.getDate() + 1);
    }

    @Override
    public Map<String, BigDecimal[]> getPoints(String pair) {
        return tradesHistoryMap.getPoints(pair);
    }

}
