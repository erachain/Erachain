package org.erachain.dbs.mapDB;

import org.erachain.core.block.GenesisBlock;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TimeTXWaitMap;
import org.erachain.dbs.IteratorCloseable;
import org.junit.Test;
import org.mapdb.Fun;

public class TimeWaitSuitMapDBTest {

    DCSet dcSet;
    TimeTXWaitMap timeTXWaitMap;
    Fun.Tuple2<Integer, Long> key;

    private void init() {

        dcSet = DCSet.createEmptyDatabaseSet(IDB.DBS_MAP_DB);
        GenesisBlock gb = new GenesisBlock();
        try {
            gb.process(dcSet);
        } catch (Exception e) {

        }

        timeTXWaitMap = dcSet.getTimeTXWaitMap();

    }

    @Test
    public void iterator() {

        init();

        timeTXWaitMap.put(Transaction.parseDBRef("123-1"), 1234);
        timeTXWaitMap.put(Transaction.parseDBRef("125-2"), 2234);
        timeTXWaitMap.put(Transaction.parseDBRef("333-5"), 234);

        IteratorCloseable<Fun.Tuple2<Integer, Long>> iterator = timeTXWaitMap.getTXIterator();
        while (iterator.hasNext()) {
            key = iterator.next();
            System.out.println(key.a + " - " + Transaction.viewDBRef(key.b));
        }

        System.out.println(" DELETE: ");
        timeTXWaitMap.delete(Transaction.parseDBRef("123-1"));
        iterator = timeTXWaitMap.getTXIterator();
        while (iterator.hasNext()) {
            key = iterator.next();
            System.out.println(key.a + " - " + Transaction.viewDBRef(key.b));
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