package org.erachain.dbs.mapDB;

import org.erachain.core.account.Account;
import org.erachain.core.block.GenesisBlock;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ReferenceMapImpl;
import org.junit.Test;
import org.mapdb.Fun;

import static org.junit.Assert.assertEquals;

public class ReferenceSuitMapDBForkTest {

    DCSet dcSet;
    Account account;
    ReferenceMapImpl referenceMap;
    Fun.Tuple2<String, Integer> key;
    Fun.Tuple3<Integer, Integer, Integer> lastPoint;
    Fun.Tuple3<Integer, Integer, Integer> currentPoint;

    private void init() {

        account = new Account("7CzxxwH7u9aQtx5iNHskLQjyJvybyKg8rF");
        dcSet = DCSet.createEmptyDatabaseSet(IDB.DBS_MAP_DB);
        GenesisBlock gb = new GenesisBlock();
        try {
            gb.process(dcSet, false);
        } catch (Exception e) {

        }

        referenceMap = dcSet.getReferenceMap();

    }

    @Test
    public void deleteInFork() {

        init();

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

    }
}