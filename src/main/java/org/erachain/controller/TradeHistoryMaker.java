package org.erachain.controller;

import org.erachain.database.DTHSet;
import org.erachain.dbs.DCUMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

abstract public class TradeHistoryMaker {

    private static final Logger logger = LoggerFactory.getLogger(TradeHistoryMaker.class.getSimpleName());

    protected Controller cnt;
    protected DTHSet dthSet;

    protected long diffPeriod;

    TradeHistoryMaker(Controller cnt, DTHSet dthSet) {
        this.cnt = cnt;
        this.dthSet = dthSet;
    }

    protected void updatePointValues(BigDecimal[] values, BigDecimal price, BigDecimal volume) {
        if (values[0] == null) {
            // дата начала точки
            values[0] = price;
            values[1] = price;
            values[2] = volume;
            values[3] = price;
            values[4] = price;
        } else {
            values[1] = price;
            values[2] = values[2].add(volume);
            if (values[3].compareTo(price) > 0)
                // минимальное значение обновим
                values[3] = price;

            if (values[4].compareTo(price) < 0) {
                // максимальное значение обновим
                values[4] = price;
            }
        }
    }

    abstract String makeKey(String pair, Timestamp pointTimeStamp);

    abstract DCUMapImpl<String, BigDecimal[]> getMap();

    abstract Map<String, BigDecimal[]> getPoints(String pair);

}
