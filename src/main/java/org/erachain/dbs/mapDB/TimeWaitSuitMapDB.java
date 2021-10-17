package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.TimeTXintf;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.util.NavigableSet;

/**
 * Хранит исполненные транзакции, или отмененные - все что уже не активно для запуска по времени<br>
 * <br>
 * Ключ: блок, значение - ссылка на ID транзакции, поэтому в основной мапке только последняя трнзакция на этот ожидаемый блок<br>
 * Для прохода по всем транзакциям использовать только getTXIterator!!!
 * Значение: заказ<br>
 */

@Slf4j
public class TimeWaitSuitMapDB extends DBMapSuit<Long, Integer> implements TimeTXintf<Integer, Long> {

    private NavigableSet keySet;

    public TimeWaitSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, false);
    }

    @Override
    public void openMap() {

        //HI = Long.MAX_VALUE;
        //LO = 0L;

        //OPEN MAP
        map = database.createTreeMap("time_wait")
                .makeOrGet();

        this.keySet = database.createTreeSet("timed_wait_keys")
                .makeOrGet();

        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.keySet,
                new Fun.Function2<Integer, Long, Integer>() {
                    @Override
                    public Integer run(
                            Long dbRef, Integer height) {
                        return height;
                    }
                });

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        ///////////////////// HERE NOT PROTOCOL INDEXES

    }

    @Override
    public IteratorCloseable<Fun.Tuple2<Integer, Long>> getTXIterator() {
        return IteratorCloseableImpl.make(keySet.iterator());
    }

}
