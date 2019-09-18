package org.erachain.datachain;

import org.erachain.core.account.Account;
import org.junit.Test;
import org.mapdb.Fun;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.*;

public class ItemAssetBalanceMapTest {

    DCSet dcSet;
    String address = "7CzxxwH7u9aQtx5iNHskLQjyJvybyKg8rF";
    BigDecimal balA = new BigDecimal("0.1");
    BigDecimal balB = new BigDecimal("0.2");
    Fun.Tuple2<BigDecimal, BigDecimal> balAB = new Fun.Tuple2<>(balA, balB);
    ItemAssetBalanceTab map;

    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance2;

    private void init() {

        dcSet = DCSet.createEmptyDatabaseSet();
        map = dcSet.getAssetBalanceMap();

        balance = new Fun.Tuple5<>(balAB, balAB, balAB, balAB, balAB);


    }

    @Test
    public void set() {

        init();

        Account account = new Account("73EotEbxvAo39tyugJSyL5nbcuMWs4aUpS");
        map.set(account.getShortAddressBytes(), 2L, balance);

        balance2 = map.get(account.getShortAddressBytes(), 2L);

        assertEquals(Arrays.equals(account.getShortAddressBytes(), ItemAssetBalanceTab.getShortAccountFromKey(account.getShortAddressBytes())), true);

        Account account2 = new Account(ItemAssetBalanceTab.getShortAccountFromKey(account.getShortAddressBytes()));

        assertEquals(Arrays.equals(account.getAddressBytes(), account2.getAddressBytes()), true);
        assertEquals(Arrays.equals(account.getShortAddressBytes(), account2.getShortAddressBytes()), true);
        assertEquals(account.getAddress(), account2.getAddress());

    }

}