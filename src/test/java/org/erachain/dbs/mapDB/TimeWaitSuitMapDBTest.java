package org.erachain.dbs.mapDB;

import org.erachain.core.block.GenesisBlock;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TimeWaitMap;
import org.erachain.dbs.IteratorCloseable;
import org.junit.Test;
import org.mapdb.Fun;

public class TimeWaitSuitMapDBTest {

    DCSet dcSet;
    TimeWaitMap timeWaitMap;
    Fun.Tuple2<Integer, Long> key;

    private void init() {

        dcSet = DCSet.createEmptyDatabaseSet(IDB.DBS_MAP_DB);
        GenesisBlock gb = new GenesisBlock();
        try {
            gb.process(dcSet);
        } catch (Exception e) {

        }

        timeWaitMap = dcSet.getTimeWaitMap();

    }

    @Test
    public void iterator() {

        init();

        timeWaitMap.put(1, 1L);
        timeWaitMap.put(1, 2L);
        timeWaitMap.put(1, 3L);

        IteratorCloseable<Fun.Tuple2<Integer, Long>> iterator = timeWaitMap.getTXIterator();
        while (iterator.hasNext()) {
            key = iterator.next();
            System.out.println(key.toString());
        }

    }

    @Test
    public void deleteInFork() {

        init();

        /*
        long[] pointStart = new long[]{2345L, 123L};

        assertEquals(account.getLastTimestamp(dcSet), null);
        account.setLastTimestamp(pointStart, dcSet);
        assertEquals(pointStart, account.getLastTimestamp(dcSet));

        long[] point2 = new long[]{pointStart[0] + 9767, pointStart[1] + 123};
        account.setLastTimestamp(point2, dcSet);
        assertEquals(point2, account.getLastTimestamp(dcSet));

        DCSet forkDCSet = dcSet.fork(this.toString());
        account.removeLastTimestamp(forkDCSet, point2[0]);
        long[] pointStartCopy = new long[]{2345L, 123L};
        assertEquals(2345L, account.getLastTimestamp(forkDCSet)[0]);
        assertEquals(123L, account.getLastTimestamp(forkDCSet)[1]);

        account.removeLastTimestamp(forkDCSet);
        assertEquals(null, account.getLastTimestamp(forkDCSet));


         */
    }

}