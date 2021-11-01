package org.erachain.dapp.epoch.shibaverse;

import org.erachain.core.block.GenesisBlock;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TimeTXWaitMap;
import org.junit.Test;
import org.mapdb.Fun;

public class ShibaVerseDAPPTest {

    DCSet dcSet;
    TimeTXWaitMap timeTXWaitMap;
    Fun.Tuple2<Integer, Long> key;
    GenesisBlock gb;

    boolean descending = false;

    private void init() {

        dcSet = DCSet.createEmptyDatabaseSet(IDB.DBS_MAP_DB);
        gb = new GenesisBlock();
        try {
            gb.process(dcSet);
        } catch (Exception e) {

        }

        timeTXWaitMap = dcSet.getTimeTXWaitMap();

    }

    @Test
    public void isValid() {
    }

    @Test
    public void toBytes() {
    }

    @Test
    public void parse() {
    }

    @Test
    public void getRandHash() {

        init();

        int count = 10;
        do {
            byte[] randomArray = ShibaVerseDAPP.getRandHash(gb, gb.getTransaction(3), count);
            // make object name: "c" - comet, "0" - era, Rarity1,2, Value1,2,
            int value1 = Byte.toUnsignedInt(randomArray[7]) >>> 5;
            int value2 = Byte.toUnsignedInt(randomArray[6]) >>> 5;
            String name = "c0" + randomArray[0] + randomArray[1] + value1 + value2;

            System.out.println(name);
        } while (--count > 0);
    }
}