package org.erachain.dbs.benchmarks;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.account.Account;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.datachain.ItemAssetBalanceSuit;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.rocksDB.utils.ConstantsRocksDB;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Test;
import org.mapdb.Fun;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

@Slf4j
public class benchDBSsimple {
    int[] TESTED_DBS = new int[]{1,2,3};

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

    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance1;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance2;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance3;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance4;

    private void init(int dbs) {

        logger.info("DBS_TEST: " + dbs);

        try {
            File tempDir = new File(ConstantsRocksDB.ROCKS_DB_FOLDER);
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        dcSet = DCSet.createEmptyDatabaseSet(dbs);
        map = dcSet.getAssetBalanceMap();

        balance1 = new Fun.Tuple5<>(balAB, balAB, balAB, balAB, balAB);
        balance2 = new Fun.Tuple5<>(balBA, balBA, balBA, balBA, balBA);
        balance3 = new Fun.Tuple5<>(balAA, balAA, balAA, balAA, balAA);
        balance4 = new Fun.Tuple5<>(balBB, balBB, balBB, balBB, balBB);

    }

    @Test
    public void set() {

        for (int dbs: TESTED_DBS) {

            init(dbs);

            map.put(account1.getShortAddressBytes(), 2L, balance1);

            balance2 = map.get(account1.getShortAddressBytes(), 2L);

            assertEquals(Arrays.equals(account1.getShortAddressBytes(), ItemAssetBalanceMap.getShortAccountFromKey(account1.getShortAddressBytes())), true);

            Account account = new Account(ItemAssetBalanceMap.getShortAccountFromKey(account1.getShortAddressBytes()));

            assertEquals(Arrays.equals(account.getAddressBytes(), account1.getAddressBytes()), true);
            assertEquals(Arrays.equals(account.getShortAddressBytes(), account1.getShortAddressBytes()), true);
            assertEquals(account.getAddress(), account1.getAddress());
        }
    }

    @Test
    public void assetIterator() {
        for (int dbs: TESTED_DBS) {
            init(dbs);

            int size = map.size();

            long assetKey1 = 1L;
            long assetKey2 = 2L;
            int iteratorSize = 0;

            int iteratorSize1 = 0;
            Iterator<byte[]> assetKeys = ((ItemAssetBalanceSuit) ((DBTabImpl) map).getSuit()).getIteratorByAsset(assetKey1);
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
            assetKeys = ((ItemAssetBalanceSuit) ((DBTabImpl) map).getSuit()).getIteratorByAsset(assetKey1);
            while (assetKeys.hasNext()) {
                iteratorSize++;
                assetKeys.next();
            }
            assertEquals(1, iteratorSize);

            iteratorSize = 0;
            assetKeys = ((ItemAssetBalanceSuit) ((DBTabImpl) map).getSuit()).getIteratorByAsset(assetKey2);
            while (assetKeys.hasNext()) {
                iteratorSize++;
                assetKeys.next();
            }
            assertEquals(1, iteratorSize);


            map.set(Bytes.concat(account2.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance2);

            map.set(Bytes.concat(account3.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance1);

            assertEquals(map.size(), size + 4);

            //////////////////
            assetKeys = ((ItemAssetBalanceSuit) ((DBTabImpl) map).getSuit()).getIteratorByAsset(assetKey1);

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
            assetKeys = ((ItemAssetBalanceSuit) ((DBTabImpl) map).getSuit()).getIteratorByAsset(assetKey2);
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

            assetKeys = ((ItemAssetBalanceSuit) ((DBTabImpl) map).getSuit()).getIteratorByAsset(assetKey1);
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
    }

    @Test
    public void addressIterator() {
        for (int dbs : TESTED_DBS) {
            init(dbs);

            int size = map.size();

            long assetKey1 = 1L;
            long assetKey2 = 2L;
            int iteratorSize;

            Iterator<byte[]> accountKeys;

            // проверить не затирают ли ключи друг друга
            map.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance1);
            map.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey2)), balance1);

            iteratorSize = 0;
            accountKeys = ((ItemAssetBalanceSuit) ((DBTabImpl) map).getSuit()).accountIterator(account1);
            while (accountKeys.hasNext()) {
                iteratorSize++;
                accountKeys.next();
            }
            assertEquals(2, iteratorSize);
            assertEquals(map.size(), iteratorSize);

            map.set(Bytes.concat(account2.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance2);

            map.set(Bytes.concat(account3.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance1);

            assertEquals(map.size(), size + 4);

            //////////////////
            accountKeys = ((ItemAssetBalanceSuit) ((DBTabImpl) map).getSuit()).accountIterator(account1);

            int found1 = 0;
            int found2 = 0;
            int found3 = 0;
            iteratorSize = 0;
            while (accountKeys.hasNext()) {
                iteratorSize++;

                byte[] key = accountKeys.next();
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
            accountKeys = ((ItemAssetBalanceSuit) ((DBTabImpl) map).getSuit()).accountIterator(account2);
            iteratorSize = 0;
            found1 = 0;
            found2 = 0;
            while (accountKeys.hasNext()) {
                iteratorSize++;

                byte[] key = accountKeys.next();
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

            accountKeys = ((ItemAssetBalanceSuit) ((DBTabImpl) map).getSuit()).accountIterator(account3);
            iteratorSize = 0;
            found1 = 0;
            found2 = 0;
            while (accountKeys.hasNext()) {
                iteratorSize++;

                byte[] key = accountKeys.next();
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

}
