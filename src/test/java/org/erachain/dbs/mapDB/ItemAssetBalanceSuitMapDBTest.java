package org.erachain.dbs.mapDB;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceSuit;
import org.erachain.datachain.ItemAssetBalanceTab;
import org.erachain.dbs.rocksDB.ItemAssetBalanceSuitRocksDB;
import org.junit.Test;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.Iterator;

import static org.junit.Assert.*;

public class ItemAssetBalanceSuitMapDBTest {

    DCSet dcSet;
    Account account1 = new Account("7CzxxwH7u9aQtx5iNHskLQjyJvybyKg8rF");
    Account account2 = new Account("73EotEbxvAo39tyugJSyL5nbcuMWs4aUpS");

    BigDecimal balA = new BigDecimal("0.1");
    BigDecimal balB = new BigDecimal("0.2");
    Fun.Tuple2<BigDecimal, BigDecimal> balAB = new Fun.Tuple2<>(balA, balB);
    ItemAssetBalanceSuitRocksDB map;

    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance2;

    private void init() {

        dcSet = DCSet.createEmptyDatabaseSet();
        map = (ItemAssetBalanceSuitRocksDB)dcSet.getAssetBalanceMap().getMapSuit();

        balance = new Fun.Tuple5<>(balAB, balAB, balAB, balAB, balAB);


    }

    @Test
    public void assetIterator() {
        init();

        int size = map.size();

        long assetKey1 = 1L;
        long assetKey2 = 2L;

        int iteratorSize1 = 0;
        Iterator<byte[]> assetKeys = ((ItemAssetBalanceSuit) map).assetIterator(assetKey1);
        while (assetKeys.hasNext()) {
            iteratorSize1++;
            byte[] key = assetKeys.next();
            long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
            assertEquals(assetKey, assetKey2);

        }

        map.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance);
        map.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey2)), balance);

        map.set(Bytes.concat(account2.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance);

        assertEquals(map.size(), size + 3);

        //////////////////
        assetKeys = ((ItemAssetBalanceSuit) map).assetIterator(assetKey2);

        int iteratorSize = 0;
        int found1 = 0;
        int found2 = 0;
        while (assetKeys.hasNext()) {
            iteratorSize++;

            byte[] key = assetKeys.next();
            long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
            assertEquals(assetKey, assetKey1);
            byte[] shortAddress = ItemAssetBalanceTab.getShortAccountFromKey(key);

            if (account1.equals(shortAddress)) {
                found1++;
            } else if (account2.equals(shortAddress)) {
                found2++;
            }
        }
        assertEquals(1, found1);
        assertEquals(1, found2);
        assertEquals(iteratorSize + 2, iteratorSize);


        //////////////////
        assetKeys = ((ItemAssetBalanceSuit) map).assetIterator(assetKey2);

        iteratorSize = 0;
        found1 = 0;
        found2 = 0;
        while (assetKeys.hasNext()) {
            iteratorSize++;

            byte[] key = assetKeys.next();
            long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
            assertEquals(assetKey, assetKey2);

            if (account1.equals(ItemAssetBalanceTab.getShortAccountFromKey(key))) {
                found1++;
            }
            if (account2.equals(ItemAssetBalanceTab.getShortAccountFromKey(key))) {
                found2++;
            }
        }
        assertEquals(1, found1);
        assertEquals(0, found2);


        assetKeys = ((ItemAssetBalanceSuit) map).assetIterator(assetKey2);
        while (assetKeys.hasNext()) {
            byte[] key = assetKeys.next();
            long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
            assertEquals(assetKey, assetKey2);
            assertNotEquals(true, account2.equals(ItemAssetBalanceTab.getShortAccountFromKey(key)));
        }

    }

    @Test
    public void addressIterator() {
    }
}