package org.erachain.core.account;

import org.erachain.core.block.GenesisBlock;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.junit.Test;
import org.mapdb.Fun;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class AccountTest {

    DCSet db;
    GenesisBlock gb;

    void init() {
        db = DCSet.createEmptyHardDatabaseSet();
        gb = new GenesisBlock();

        try {
            gb.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    @Test
    public void getBalance() {
        init();

        /// Tuple2[323980.18401546, 323980.18401546] //
        Account account = new Account("76ACGgH8c63VrrgEw1wQA4Dno1JuPLTsWe");

        Fun.Tuple5 balance5 = account.getBalance(db, Transaction.RIGHTS_KEY);
        assertEquals(((BigDecimal)((Fun.Tuple2)balance5.a).a).intValue(), 0);
        assertEquals(((BigDecimal)((Fun.Tuple2)balance5.a).b).intValue(), 0);

        Fun.Tuple2 balance = account.getBalance(db, Transaction.RIGHTS_KEY, 1);

        assertEquals(((BigDecimal)balance.a).intValue(), 0);
        assertEquals(((BigDecimal)balance.b).intValue(), 7437);

        Fun.Tuple2 balance2 = account.getBalance(db, Transaction.RIGHTS_KEY, 1);

        assertEquals(((BigDecimal)balance2.a).intValue(), 0);
        assertEquals(((BigDecimal)balance2.b).intValue(), 7437);

        ItemAssetBalanceMap map = db.getAssetBalanceAccountingMap();
        map.reset();

        Fun.Tuple2 balance3 = account.getBalance(db, Transaction.RIGHTS_KEY, 1);

        assertEquals(((BigDecimal)balance3.a).intValue(), 0);
        assertEquals(((BigDecimal)balance3.b).intValue(), 0);

    }
}