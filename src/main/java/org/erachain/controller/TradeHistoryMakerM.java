package org.erachain.controller;

import org.erachain.database.DTHSet;
import org.erachain.database.TradeHistoryMonthMap;
import org.erachain.dbs.DCUMapImpl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

public class TradeHistoryMakerM extends TradeHistoryMaker {

    protected TradeHistoryMonthMap tradesHistoryMap;

    TradeHistoryMakerM(Controller cnt, DTHSet dthSet) {
        super(cnt, dthSet);
        // точно больше месяца прошло
        diffPeriod = 60000L * 60L * 24L * 33L;
        tradesHistoryMap = dthSet.getTradesHistoryMonthMap();

    }

    @Override
    public DCUMapImpl<String, BigDecimal[]> getMap() {
        return tradesHistoryMap;
    }

    @Override
    String makeKey(String pair, Timestamp pointTimeStamp) {
        // года с 1900-го
        return String.format("%s %4d-%02d-%02d", pair, pointTimeStamp.getYear() + 1900, pointTimeStamp.getMonth() + 1, 15);
    }

    @Override
    public Map<String, BigDecimal[]> getPoints(String pair) {
        return tradesHistoryMap.getPoints(pair);
    }

}
