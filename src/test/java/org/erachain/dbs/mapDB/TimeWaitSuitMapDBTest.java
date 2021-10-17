package org.erachain.dbs.mapDB;

import org.erachain.core.block.GenesisBlock;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TimeTXWaitMap;
import org.erachain.dbs.IteratorCloseable;
import org.junit.Test;
import org.mapdb.Fun;

import static org.junit.Assert.assertEquals;

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

        Fun.Tuple2<String, Integer>[] values = new Fun.Tuple2[]{
                new Fun.Tuple2("123-1", 1234), new Fun.Tuple2("523-1", 2234), new Fun.Tuple2("333-5", 234),
        };
        timeTXWaitMap.put(Transaction.parseDBRef(values[0].a), values[0].b);
        timeTXWaitMap.put(Transaction.parseDBRef(values[1].a), values[1].b);
        timeTXWaitMap.put(Transaction.parseDBRef(values[2].a), values[2].b);

        IteratorCloseable<Fun.Tuple2<Integer, Long>> iterator = timeTXWaitMap.getTXIterator();
        int i = 0;
        while (iterator.hasNext()) {
            key = iterator.next();
            System.out.println(key.a + " - " + Transaction.viewDBRef(key.b));

            switch (++i) {
                case 1:
                    assertEquals(values[2].b, key.a);
                    break;
                case 2:
                    assertEquals(values[0].b, key.a);
                    break;
                case 3:
                    assertEquals(values[1].b, key.a);
                    break;
                default:
                    assertEquals("wrong index", "");

            }

        }

        System.out.println(" DELETE: " + values[0].a);
        timeTXWaitMap.delete(Transaction.parseDBRef(values[0].a));
        iterator = timeTXWaitMap.getTXIterator();
        i = 0;
        while (iterator.hasNext()) {
            key = iterator.next();
            System.out.println(key.a + " - " + Transaction.viewDBRef(key.b));

            switch (++i) {
                case 1:
                    assertEquals(values[2].b, key.a);
                    break;
                case 2:
                    assertEquals(values[1].b, key.a);
                    break;
                default:
                    assertEquals("wrong index", "");
            }
        }

    }

    @Test
    public void deleteInFork() {

        init();

        Fun.Tuple2<String, Integer>[] values = new Fun.Tuple2[]{
                new Fun.Tuple2("123-1", 1234), new Fun.Tuple2("523-1", 2234), new Fun.Tuple2("333-5", 234),
                // in FORK:
                new Fun.Tuple2("220-4", 1534),

        };
        timeTXWaitMap.put(Transaction.parseDBRef(values[0].a), values[0].b);
        timeTXWaitMap.put(Transaction.parseDBRef(values[1].a), values[1].b);
        timeTXWaitMap.put(Transaction.parseDBRef(values[2].a), values[2].b);

        IteratorCloseable<Fun.Tuple2<Integer, Long>> iterator = timeTXWaitMap.getTXIterator();
        int i = 0;
        while (iterator.hasNext()) {
            key = iterator.next();
            System.out.println(key.a + " - " + Transaction.viewDBRef(key.b));

            switch (++i) {
                case 1:
                    assertEquals(values[2].b, key.a);
                    break;
                case 2:
                    assertEquals(values[0].b, key.a);
                    break;
                case 3:
                    assertEquals(values[1].b, key.a);
                    break;
                default:
                    assertEquals("wrong index", "");

            }
        }

        System.out.println(" *** ADD in FORK *** " + values[3].a);

        DCSet forkDCSet = dcSet.fork(this.toString());
        timeTXWaitMap = forkDCSet.getTimeTXWaitMap();
        timeTXWaitMap.put(Transaction.parseDBRef(values[3].a), values[3].b);

        iterator = timeTXWaitMap.getTXIterator();
        i = 0;
        while (iterator.hasNext()) {
            key = iterator.next();
            System.out.println(key.a + " - " + Transaction.viewDBRef(key.b));

            switch (++i) {
                case 1:
                    assertEquals(values[2].b, key.a);
                    break;
                case 2:
                    assertEquals(values[0].b, key.a);
                    break;
                case 3:
                    assertEquals(values[3].b, key.a);
                    break;
                case 4:
                    assertEquals(values[1].b, key.a);
                    break;
                default:
                    assertEquals("wrong index", "");

            }
        }

        System.out.println(" DELETE: " + values[0].a);
        timeTXWaitMap.delete(Transaction.parseDBRef(values[0].a));
        iterator = timeTXWaitMap.getTXIterator();
        i = 0;
        while (iterator.hasNext()) {
            key = iterator.next();
            System.out.println(key.a + " - " + Transaction.viewDBRef(key.b));

            switch (++i) {
                case 1:
                    assertEquals(values[2].b, key.a);
                    break;
                case 2:
                    assertEquals(values[3].b, key.a);
                    break;
                case 3:
                    assertEquals(values[1].b, key.a);
                    break;
                default:
                    assertEquals("wrong index", "");
            }
        }

    }

}