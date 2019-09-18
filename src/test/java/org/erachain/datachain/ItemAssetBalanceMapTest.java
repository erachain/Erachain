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
    Account account3 = new Account("7Ca34FCVKEgVy7ZZqRB41Wzj3aSFvtfvDp");

    BigDecimal balA = new BigDecimal("0.1");
    BigDecimal balB = new BigDecimal("0.2");
    Fun.Tuple2<BigDecimal, BigDecimal> balAB = new Fun.Tuple2<>(balA, balB);
    Fun.Tuple2<BigDecimal, BigDecimal> balBA = new Fun.Tuple2<>(balB, balA);
    Fun.Tuple2<BigDecimal, BigDecimal> balAA = new Fun.Tuple2<>(balA, balA);
    Fun.Tuple2<BigDecimal, BigDecimal> balBB = new Fun.Tuple2<>(balB, balB);

    ItemAssetBalanceMap map;
    ItemAssetBalanceTab map;

    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance1;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance2;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance3;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance4;

    private void init() {

        dcSet = DCSet.createEmptyDatabaseSet();
        map = new ItemAssetBalanceMap(dcSet, dcSet.getDatabase());

        balance1 = new Fun.Tuple5<>(balAB, balAB, balAB, balAB, balAB);
        balance2 = new Fun.Tuple5<>(balBA, balBA, balBA, balBA, balBA);
        balance3 = new Fun.Tuple5<>(balAA, balAA, balAA, balAA, balAA);
        balance4 = new Fun.Tuple5<>(balBB, balBB, balBB, balBB, balBB);

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
        int iteratorSize = 0;

        int iteratorSize1 = 0;
        Iterator<byte[]> assetKeys = map.assetKeys(assetKey1).iterator();
        while (assetKeys.hasNext()) {
            iteratorSize1++;
            byte[] key = assetKeys.next();
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            assertEquals(assetKey, assetKey2);

        }
        assertEquals(0, iteratorSize1);
        assertEquals(map.size(), iteratorSize);

        // проверить не затирают ли ключи друг друга
        map.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance1);
        map.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey2)), balance1);

        iteratorSize = 0;
        assetKeys = map.assetKeys(assetKey1).iterator();
        while (assetKeys.hasNext()) {
            iteratorSize++;
            assetKeys.next();
        }
        assertEquals(1, iteratorSize);

        iteratorSize = 0;
        assetKeys = map.assetKeys(assetKey2).iterator();
        while (assetKeys.hasNext()) {
            iteratorSize++;
            assetKeys.next();
        }
        assertEquals(1, iteratorSize);



        map.set(Bytes.concat(account2.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance2);

        map.set(Bytes.concat(account3.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance1);

        assertEquals(map.size(), size + 4);

        //////////////////
        assetKeys = map.assetKeys(assetKey1).iterator();

        int found1 = 0;
        int found2 = 0;
        int found3 = 0;
        iteratorSize = 0;
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
            } else if (account3.equals(shortAddress)) {
                found3++;
            }
        }
        assertEquals(1, found1);
        assertEquals(1, found2);
        assertEquals(1, found3);
        assertEquals(3, iteratorSize);


        //////////////////
        assetKeys = map.assetKeys(assetKey2).iterator();
        iteratorSize = 0;
        found1 = 0;
        found2 = 0;
        found3 = 0;
        while (assetKeys.hasNext()) {
            iteratorSize++;

            byte[] key = assetKeys.next();
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            assertEquals(assetKey, assetKey2);

            if (account1.equals(ItemAssetBalanceMap.getShortAccountFromKey(key))) {
                found1++;
            } else if (account2.equals(ItemAssetBalanceMap.getShortAccountFromKey(key))) {
                found2++;
            } else if (account3.equals(ItemAssetBalanceMap.getShortAccountFromKey(key))) {
                found3++;
            }
        }
        assertEquals(1, found1);
        assertEquals(0, found2);
        assertEquals(0, found3);
        assertEquals(1, iteratorSize);


        ///////////// тот же КЛЮЧ
        map.set(Bytes.concat(account3.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance3);

        assetKeys = map.assetKeys(assetKey1).iterator();
        iteratorSize = 0;
        found1 = 0;
        found2 = 0;
        found3 = 0;
        while (assetKeys.hasNext()) {
            iteratorSize++;

            byte[] key = assetKeys.next();
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            assertEquals(assetKey, assetKey1);

            if (account1.equals(ItemAssetBalanceMap.getShortAccountFromKey(key))) {
                found1++;
            } else if (account2.equals(ItemAssetBalanceMap.getShortAccountFromKey(key))) {
                found2++;
            } else if (account3.equals(ItemAssetBalanceMap.getShortAccountFromKey(key))) {
                found3++;
            }
        }
        assertEquals(1, found1);
        assertEquals(1, found2);
        assertEquals(1, found3);
        assertEquals(3, iteratorSize);

    }

    @Test
    public void addressIterator() {
        init();

        int size = map.size();

        long assetKey1 = 1L;
        long assetKey2 = 2L;
        int iteratorSize;

        Iterator<byte[]> addressKeys;

        // проверить не затирают ли ключи друг друга
        map.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance1);
        map.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey2)), balance1);

        iteratorSize = 0;
        addressKeys = map.addressKeys(account1).iterator();
        while (addressKeys.hasNext()) {
            iteratorSize++;
            addressKeys.next();
        }
        assertEquals(2, iteratorSize);
        assertEquals(map.size(), iteratorSize);

        map.set(Bytes.concat(account2.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance2);

        map.set(Bytes.concat(account3.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance1);

        assertEquals(map.size(), size + 4);

        //////////////////
        addressKeys = map.addressKeys(account1).iterator();

        int found1 = 0;
        int found2 = 0;
        int found3 = 0;
        iteratorSize = 0;
        while (addressKeys.hasNext()) {
            iteratorSize++;

            byte[] key = addressKeys.next();
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            byte[] shortAddress = ItemAssetBalanceMap.getShortAccountFromKey(key);

            assertEquals(true, account1.equals(shortAddress));

            if (assetKey == assetKey1) {
                found1++;
            } else if (assetKey == assetKey2) {
                found2++;
            }
        }
        assertEquals(1, found1);
        assertEquals(1, found2);
        assertEquals(2, iteratorSize);


        //////////////////
        addressKeys = map.addressKeys(account2).iterator();
        iteratorSize = 0;
        found1 = 0;
        found2 = 0;
        while (addressKeys.hasNext()) {
            iteratorSize++;

            byte[] key = addressKeys.next();
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            byte[] shortAddress = ItemAssetBalanceMap.getShortAccountFromKey(key);

            assertEquals(true, account2.equals(shortAddress));

            if (assetKey == assetKey1) {
                found1++;
            } else if (assetKey == assetKey2) {
                found2++;
            }
        }
        assertEquals(1, found1);
        assertEquals(0, found2);
        assertEquals(1, iteratorSize);


        ///////////// тот же КЛЮЧ
        map.set(Bytes.concat(account3.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance3);

        addressKeys = map.addressKeys(account3).iterator();
        iteratorSize = 0;
        found1 = 0;
        found2 = 0;
        while (addressKeys.hasNext()) {
            iteratorSize++;

            byte[] key = addressKeys.next();
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            byte[] shortAddress = ItemAssetBalanceMap.getShortAccountFromKey(key);

            assertEquals(true, account3.equals(shortAddress));

            if (assetKey == assetKey1) {
                found1++;
            } else if (assetKey == assetKey2) {
                found2++;
            }
        }
        assertEquals(1, found1);
        assertEquals(0, found2);
        assertEquals(1, iteratorSize);
    }

}