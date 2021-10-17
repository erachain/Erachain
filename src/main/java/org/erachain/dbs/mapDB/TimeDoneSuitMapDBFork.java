package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.TimeDoneMap;
import org.mapdb.Bind;
import org.mapdb.Fun;

import java.util.NavigableSet;

/**
 * Хранит исполненные ордера, или отмененные - все что уже не активно<br>
 * <br>
 * Ключ: ссылка на запись создания заказа<br>
 * Значение: заказ<br>
 */

@Slf4j
public class TimeDoneSuitMapDBFork extends DBMapSuitFork<Integer, Long> {

    private NavigableSet waitKeySet;

    public TimeDoneSuitMapDBFork(TimeDoneMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger, false, null);
    }

    @Override
    public void openMap() {

        //OPEN MAP
        //OPEN MAP
        map = database.createTreeMap("time_done")
                .makeOrGet();

        // ADDRESS HAVE/WANT KEY
        this.waitKeySet = database.createTreeSet("time_done_keys")
                .makeOrGet();

        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.waitKeySet,
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

}
