package org.erachain.datachain;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.junit.Test;
import org.mapdb.Fun;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.*;

public class ItemAssetBalanceMapTest {

    DCSet dcSet;
    Account account1 = new Account("7CzxxwH7u9aQtx5iNHskLQjyJvybyKg8rF");
    Account account2 = new Account("73EotEbxvAo39tyugJSyL5nbcuMWs4aUpS");

    BigDecimal balA = new BigDecimal("0.1");
    BigDecimal balB = new BigDecimal("0.2");
    Fun.Tuple2<BigDecimal, BigDecimal> balAB = new Fun.Tuple2<>(balA, balB);
    Fun.Tuple2<BigDecimal, BigDecimal> balBA = new Fun.Tuple2<>(balB, balA);

    ItemAssetBalanceMap map;

    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance1;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance2;

    private void init() {

        dcSet = DCSet.createEmptyDatabaseSet();
        map = new ItemAssetBalanceMap(dcSet, dcSet.getDatabase());

        balance1 = new Fun.Tuple5<>(balAB, balAB, balAB, balAB, balAB);
        balance2 = new Fun.Tuple5<>(balBA, balBA, balBA, balBA, balBA);

    }

    @Test
    public void set() {

        init();

        map.set(account1.getShortAddressBytes(), 2L, balance1);

        balance2 = map.get(account1.getShortAddressBytes(), 2L);

        assertEquals(Arrays.equals(account1.getShortAddressBytes(), map.getShortAccountFromKey(account1.getShortAddressBytes())), true);

        Account account = new Account(map.getShortAccountFromKey(account1.getShortAddressBytes()));

        assertEquals(Arrays.equals(account.getAddressBytes(), account1.getAddressBytes()), true);
        assertEquals(Arrays.equals(account.getShortAddressBytes(), account1.getShortAddressBytes()), true);
        assertEquals(account.getAddress(), account1.getAddress());

    }

    @Test
    public void assetIterator() {
        init();

        int size = map.size();

        long assetKey1 = 1L;
        long assetKey2 = 2L;

        int iteratorSize1 = 0;
        Iterator<byte[]> assetKeys = map.assetKeys(assetKey1).iterator();
        while (assetKeys.hasNext()) {
            iteratorSize1++;
            byte[] key = assetKeys.next();
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            assertEquals(assetKey, assetKey2);

        }

        map.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance1);
        map.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey2)), balance1);

        map.set(Bytes.concat(account2.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance2);

        assertEquals(map.size(), size + 3);

        //////////////////
        assetKeys = map.assetKeys(assetKey1).iterator();

        int iteratorSize = 0;
        int found1 = 0;
        int found2 = 0;
        while (assetKeys.hasNext()) {
            iteratorSize++;

            byte[] key = assetKeys.next();
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            assertEquals(assetKey, assetKey1);
            byte[] shortAddress = ItemAssetBalanceMap.getShortAccountFromKey(key);

            if (account1.equals(shortAddress)) {
                found1++;
            } else if (account2.equals(shortAddress)) {
                found2++;
            }
        }
        assertEquals(1, found1);
        assertEquals(1, found2);
        assertEquals(iteratorSize, iteratorSize1 + 2);


        //////////////////
        assetKeys = map.assetKeys(assetKey2).iterator();

        iteratorSize = 0;
        found1 = 0;
        found2 = 0;
        while (assetKeys.hasNext()) {
            iteratorSize++;

            byte[] key = assetKeys.next();
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            assertEquals(assetKey, assetKey2);

            if (account1.equals(ItemAssetBalanceMap.getShortAccountFromKey(key))) {
                found1++;
            }
            if (account2.equals(ItemAssetBalanceMap.getShortAccountFromKey(key))) {
                found2++;
            }
        }
        assertEquals(1, found1);
        assertEquals(0, found2);


        assetKeys = map.assetKeys(assetKey2).iterator();
        while (assetKeys.hasNext()) {
            byte[] key = assetKeys.next();
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            assertEquals(assetKey, assetKey2);
            assertNotEquals(true, account2.equals(ItemAssetBalanceMap.getShortAccountFromKey(key)));
        }

    }

    @Test
    public void addressIterator() {
    }

    @Test
    public void getBalancesSortableList() {
    }

    @Test
    public void getBalancesSortableList1() {
    }
}