package org.erachain.dbs.mapDB;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.TimeTXintf;
import org.erachain.datachain.TimeWaitMap;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorParent;
import org.erachain.dbs.MergedOR_IteratorsNoDuplicates;
import org.mapdb.Bind;
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
public class TimeWaitSuitMapDBFork extends DBMapSuitFork<Integer, Long> implements TimeTXintf<Integer, Long> {

    private NavigableSet keySet;

    public TimeWaitSuitMapDBFork(TimeWaitMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger, false, null);
    }

    @Override
    public void openMap() {

        //OPEN MAP
        //OPEN MAP
        map = database.createTreeMap("time_wait")
                .makeOrGet();

        // ADDRESS HAVE/WANT KEY
        this.keySet = database.createTreeSet("time_wait_keys")
                .makeOrGet();

        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.keySet,
                new Fun.Function2<Fun.Tuple2<Integer, Long>, Integer, Long>() {
                    @Override
                    public Fun.Tuple2<Integer, Long> run(
                            Integer height, Long dbRef) {
                        return new Fun.Tuple2<Integer, Long>(height, dbRef);
                    }
                });

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        ///////////////////// HERE NOT PROTOCOL INDEXES

    }

    /**
     * Use parent iterator
     *
     * @return
     */
    @Override
    public IteratorCloseable<Fun.Tuple2<Integer, Long>> getTXIterator() {
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parent.getIterator(), deleted), keySet.iterator()), Fun.COMPARATOR);
    }

}
