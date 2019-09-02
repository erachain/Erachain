package org.erachain.core.account;

import org.erachain.core.block.GenesisBlock;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.junit.Test;
import org.mapdb.Fun;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class AccountTest {

    DCSet db;
    GenesisBlock gb;

    void init() {
        db = DCSet.createEmptyDatabaseSet();
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

        Account account = new Account("76ACGgH8c63VrrgEw1wQA4Dno1JuPLTsWe");

        Fun.Tuple2 balance = account.getBalance(db, Transaction.RIGHTS_KEY, 1);
        BigDecimal balAB = (BigDecimal) balance.b;

        assertEquals(balAB.intValue(), 7437);

    }
}