package org.erachain.dbs.mapDB;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.database.DBASet;
import org.erachain.datachain.TimeTXDoneMap;
import org.erachain.datachain.TimeTXintf;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorParentSecondaryKey;
import org.erachain.dbs.MergedOR_IteratorsNoDuplicates;
import org.mapdb.Bind;
import org.mapdb.Fun;

import java.util.Iterator;
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
     * @param descending
     * @return
     */
    @Override
    public IteratorCloseable<Fun.Tuple2<Integer, Long>> getTXIterator(boolean descending) {

        IteratorCloseable parentIterator = ((TimeTXintf) parent).getTXIterator(descending);

       Iterator iterator;

        if (descending) {
            iterator = keySet.descendingIterator();
        } else {
            iterator = keySet.iterator();
        }

        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParentSecondaryKey(parentIterator, deleted), iterator), Fun.COMPARATOR, descending);
    }
}
