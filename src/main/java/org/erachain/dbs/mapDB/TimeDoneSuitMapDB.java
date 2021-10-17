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
 * Хранит исполненные транзакции, или отмененные - все что уже не активно для запуска по времени. Нужно для откатов<br>
 * <br>
 * Ключ: ссылка на ID транзакции, значение - ожидаемый блок<br>
 * TODO заменить на один SET - без основной MAP, так как она по сути дублируется во вторичном ключе на SET - но тогда нужно другой класс SuitSet делать и у РоксДБ
 * Значение: заказ<br>
 */

@Slf4j
public class TimeDoneSuitMapDB extends DBMapSuit<Long, Integer> implements TimeTXintf<Integer, Long> {

    /**
     * тут как раз хранится весь список - как Set - блок который ждем + SeqNo транзакции которая ждет
     */
    private NavigableSet<Fun.Tuple2<Integer, Long>> keySet;

    public TimeDoneSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, false);
    }

    @Override
    public void openMap() {

        //HI = Long.MAX_VALUE;
        //LO = 0L;

        //OPEN MAP
        map = database.createTreeMap("time_done")
                .makeOrGet();

        this.keySet = database.createTreeSet("timed_done_keys")
                .makeOrGet();

        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.keySet,
                new Fun.Function2<Integer, Long, Integer>() {
                    @Override
                    public Integer run(Long dbRef, Integer height) {
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
