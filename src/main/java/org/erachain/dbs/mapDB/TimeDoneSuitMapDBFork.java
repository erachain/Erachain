package org.erachain.dbs.mapDB;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.TimeTXDoneMap;
import org.erachain.datachain.TimeTXintf;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorParent;
import org.erachain.dbs.MergedOR_IteratorsNoDuplicates;
import org.mapdb.Bind;
import org.mapdb.Fun;

import java.util.NavigableSet;

@Slf4j
public class TimeDoneSuitMapDBFork extends DBMapSuitFork<Long, Integer> implements TimeTXintf<Integer, Long> {

    private NavigableSet<Fun.Tuple2<Integer, Long>> keySet;

    public TimeDoneSuitMapDBFork(TimeTXDoneMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger, false, null);
    }

    @Override
    public void openMap() {

        //OPEN MAP
        //OPEN MAP
        map = database.createTreeMap("time_done")
                .makeOrGet();

        // ADDRESS HAVE/WANT KEY
        this.keySet = database.createTreeSet("time_done_keys")
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
