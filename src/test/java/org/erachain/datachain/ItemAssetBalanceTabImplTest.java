package org.erachain.datachain;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.rocksDB.utils.ConstantsRocksDB;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Test;
import org.mapdb.Fun;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@Slf4j
public class ItemAssetBalanceTabImplTest {

    int[] TESTED_DBS = new int[]{1,2,3};

    DCSet dcSet;
    Account account1 = new Account("7CzxxwH7u9aQtx5iNHskLQjyJvybyKg8rF");
    Account account2 = new Account("73EotEbxvAo39tyugJSyL5nbcuMWs4aUpS");
    Account account3 = new Account("7Ca34FCVKEgVy7ZZqRB41Wzj3aSFvtfvDp");

    BigDecimal balA = new BigDecimal("0.1");
    BigDecimal balB = new BigDecimal("0.2");
    BigDecimal balC = new BigDecimal("3.0001");
    BigDecimal balD = new BigDecimal("0.005");
    Fun.Tuple2<BigDecimal, BigDecimal> balAB = new Fun.Tuple2<>(balA, balB);
    Fun.Tuple2<BigDecimal, BigDecimal> balBA = new Fun.Tuple2<>(balB, balA);
    Fun.Tuple2<BigDecimal, BigDecimal> balAC = new Fun.Tuple2<>(balA, balC);
    Fun.Tuple2<BigDecimal, BigDecimal> balBD = new Fun.Tuple2<>(balB, balD);

    ItemAssetBalanceTab map;

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
        balance3 = new Fun.Tuple5<>(balAC, balAC, balAC, balAC, balAC);
        balance4 = new Fun.Tuple5<>(balBD, balBD, balAB, balBD, balBD);

    }

    @Test
    public void set() {

        for (int dbs: TESTED_DBS) {

            init(dbs);

            boolean found = map.contains(account1.getShortAddressBytes(), 2L);

            assertEquals(found, false);

            map.set(account1.getShortAddressBytes(), 2L, balance1);

            // make SAME KEY as NEW OBJECT
            found = map.contains(account1.getShortAddressBytes(), 2L);

            assertEquals(found, true);

            // make SAME KEY as NEW OBJECT
            found = map.contains(account1.getShortAddressBytes(), 3L);

            assertEquals(found, false);

            // make SAME KEY as NEW OBJECT
            found = map.contains(account2.getShortAddressBytes(), 2L);

            assertEquals(found, false);

            Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
                    balance = map.get(account1.getShortAddressBytes(), 2L);

            assertEquals(balance1, balance);


            assertEquals(Arrays.equals(account1.getShortAddressBytes(), ItemAssetBalanceTab.getShortAccountFromKey(account1.getShortAddressBytes())), true);

            Account account = new Account(ItemAssetBalanceTab.getShortAccountFromKey(account1.getShortAddressBytes()));

            assertEquals(Arrays.equals(account.getAddressBytes(), account1.getAddressBytes()), true);
            assertEquals(Arrays.equals(account.getShortAddressBytes(), account1.getShortAddressBytes()), true);
            assertEquals(account.getAddress(), account1.getAddress());
        }
    }

    @Test
    public void addressIteratorSort() {
        for (int dbs : TESTED_DBS) {
            init(dbs);

            long assetKeyTMP = 0L;

            Random rand = new Random();
            for (int i = 0; i < 10; i++) {
                long randLong = rand.nextLong();
                if (randLong < 0)
                    randLong = -randLong;

                int randInt = rand.nextInt();
                BigDecimal balTest = new BigDecimal(randInt + "." + randLong);
                balTest = balTest.movePointLeft(rand.nextInt(20) - 3);
                balTest = balTest.setScale(TransactionAmount.maxSCALE, RoundingMode.HALF_DOWN);

                balTest = new BigDecimal(i - 5);

                // account = new PublicKeyAccount(Crypto.getInstance().digest(Longs.toByteArray(randLong)));

                // создаем новые ключи
                byte[] key = Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(randLong >> 5));

                balance1 = new Fun.Tuple5<>(new Fun.Tuple2(balTest, balTest), balAB, balAC, balBD, balBD);
                //logger.info(balTest.toPlainString());
                map.set(key, balance1);
            }

            BigDecimal value;
            Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
                    balance = null;
            Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
                    balanceTmp;

            Collection<byte[]> assetKeysSet = ((ItemAssetBalanceSuit) ((DBTabImpl) map).getMapSuit()).accountKeys(account1);
            int iteratorSize = 0;
            for (byte[] key : assetKeysSet) {
                iteratorSize++;
                long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
                //assertEquals(assetKey, assetKey1);
                balanceTmp = map.get(key);

                // Нужно положить их с отсутпом
                logger.error("DBS: " + dbs + "  assetKey: " + assetKey + " SET bal:"
                        + balanceTmp.a.b);

                if (assetKeyTMP > 0 && assetKeyTMP > assetKey) {
                    //logger.error("DBS: " + dbs + "  assetKey: " + assetKey);
                    // всегда идем по возрастанию
                    //assertEquals(assetKeyTMP, assetKey);
                }
                assetKeyTMP = assetKey;
            }
        }
    }

    @Test
    public void assetIteratorSort() {
        for (int dbs: TESTED_DBS) {
            init(dbs);

            long assetKey1 = 1L;

            Random rand = new Random();
            for (int i=0; i < 10; i++) {
                long randLong = rand.nextLong();
                if (randLong < 0)
                    randLong = -randLong;

                int randInt = rand.nextInt();
                BigDecimal balTest = new BigDecimal( randInt + "." + randLong);
                balTest = balTest.movePointLeft(rand.nextInt(20) - 3);
                balTest = balTest.setScale(TransactionAmount.maxSCALE, RoundingMode.HALF_DOWN);

                balTest = new BigDecimal( i - 5);

                Account account = new PublicKeyAccount(Crypto.getInstance().digest(Longs.toByteArray(randLong)));

                // создаем новые ключи
                byte[] key = Bytes.concat(account.getShortAddressBytes(), Longs.toByteArray(assetKey1));

                balance1 = new Fun.Tuple5<>(new Fun.Tuple2(balTest, balTest), balAB, balAC, balBD, balBD);
                //logger.info(balTest.toPlainString());
                map.set(key, balance1);
            }

            BigDecimal value;
            Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
                    balance = null;
            Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
                    balanceTmp;

            Collection<byte[]> assetKeysSet = ((ItemAssetBalanceSuit) ((DBTabImpl) map).getMapSuit()).assetKeys(assetKey1);
            int iteratorSize = 0;
            for (byte[] key: assetKeysSet) {
                iteratorSize++;
                long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
                assertEquals(assetKey, assetKey1);
                balanceTmp = map.get(key);

                // Нужно положить их с отсутпом
                logger.error("DBS: " + dbs + "  iteratorSize: " + iteratorSize + " SET bal:"
                        + balanceTmp.a.b);

                if (balance != null && balanceTmp.a.b.compareTo(balance.a.b) > 0) {
                    //logger.error("DBS: " + dbs + "  iteratorSize: " + iteratorSize);
                    // всегда идем по возрастанию
                    //assertEquals(balanceTmp.a.b, balance.a.b);
                }
                balance = balanceTmp;
            }

            //////////////////
            Iterator<byte[]> assetKeys = ((ItemAssetBalanceSuit) ((DBTabImpl) map).getMapSuit()).assetIterator(assetKey1);

            iteratorSize = 0;
            while (assetKeys.hasNext()) {
                iteratorSize++;

                byte[] key = assetKeys.next();
                long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
                assertEquals(assetKey, assetKey1);
                balanceTmp = map.get(key);

                // Нужно положить их с отсутпом
                logger.error("DBS: " + dbs + "  iteratorSize: " + iteratorSize + " ITER bal:"
                        + balanceTmp.a.b);

                if (balance != null && balanceTmp.a.b.compareTo(balance.a.b) > 0) {
                    //logger.error("DBS: " + dbs + "  iteratorSize: " + iteratorSize);
                    // всегда идем по возрастанию
                    //assertEquals(balanceTmp.a.b, balance.a.b);
                }
                balance = balanceTmp;

            }
            assertEquals(map.size(), iteratorSize);

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
            Iterator<byte[]> assetKeys = ((ItemAssetBalanceSuit)((DBTabImpl)map).getMapSuit()).assetKeys(assetKey1).iterator();
            while (assetKeys.hasNext()) {
                iteratorSize1++;
                byte[] key = assetKeys.next();
                long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
                assertEquals(assetKey, assetKey2);

            }
            assertEquals(0, iteratorSize1);
            assertEquals(map.size(), iteratorSize);

            // проверить не затирают ли ключи друг друга
            map.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance1);
            map.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey2)), balance1);

            iteratorSize = 0;
            assetKeys = ((ItemAssetBalanceSuit)((DBTabImpl)map).getMapSuit()).assetKeys(assetKey1).iterator();
            while (assetKeys.hasNext()) {
                iteratorSize++;
                assetKeys.next();
            }
            assertEquals(1, iteratorSize);

            iteratorSize = 0;
            assetKeys = ((ItemAssetBalanceSuit)((DBTabImpl)map).getMapSuit()).assetKeys(assetKey2).iterator();
            while (assetKeys.hasNext()) {
                iteratorSize++;
                assetKeys.next();
            }
            assertEquals(1, iteratorSize);


            map.set(Bytes.concat(account2.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance2);

            byte[] keyAccount3Asset1 = Bytes.concat(account3.getShortAddressBytes(), Longs.toByteArray(assetKey1));
            map.set(keyAccount3Asset1, balance1);

            Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
                    balance = map.get(keyAccount3Asset1);

            assertEquals(balance, balance1);

            assertEquals(map.size(), size + 4);

            //////////////////
            assetKeys = ((ItemAssetBalanceSuit)((DBTabImpl)map).getMapSuit()).assetKeys(assetKey1).iterator();

            int found1 = 0;
            int found2 = 0;
            int found3 = 0;
            iteratorSize = 0;
            while (assetKeys.hasNext()) {
                iteratorSize++;

                byte[] key = assetKeys.next();
                long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
                assertEquals(assetKey, assetKey1);
                byte[] shortAddress = ItemAssetBalanceTab.getShortAccountFromKey(key);

                BigDecimal balanceTMP = map.get(key).a.b;

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
            assetKeys = ((ItemAssetBalanceSuit)((DBTabImpl)map).getMapSuit()).assetKeys(assetKey2).iterator();
            iteratorSize = 0;
            found1 = 0;
            found2 = 0;
            found3 = 0;
            while (assetKeys.hasNext()) {
                iteratorSize++;

                byte[] key = assetKeys.next();
                long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
                assertEquals(assetKey, assetKey2);

                if (account1.equals(ItemAssetBalanceTab.getShortAccountFromKey(key))) {
                    found1++;
                } else if (account2.equals(ItemAssetBalanceTab.getShortAccountFromKey(key))) {
                    found2++;
                } else if (account3.equals(ItemAssetBalanceTab.getShortAccountFromKey(key))) {
                    found3++;
                }
            }
            assertEquals(1, found1);
            assertEquals(0, found2);
            assertEquals(0, found3);
            assertEquals(1, iteratorSize);


            ///////////// тот же КЛЮЧ но новый баланс
            map.set(keyAccount3Asset1, balance3);

            assetKeys = ((ItemAssetBalanceSuit)((DBTabImpl)map).getMapSuit()).assetKeys(assetKey1).iterator();
            iteratorSize = 0;
            found1 = 0;
            found2 = 0;
            found3 = 0;
            while (assetKeys.hasNext()) {
                iteratorSize++;

                byte[] key = assetKeys.next();
                long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
                assertEquals(assetKey, assetKey1);

                if (account1.equals(ItemAssetBalanceTab.getShortAccountFromKey(key))) {
                    found1++;
                } else if (account2.equals(ItemAssetBalanceTab.getShortAccountFromKey(key))) {
                    found2++;
                } else if (account3.equals(ItemAssetBalanceTab.getShortAccountFromKey(key))) {
                    found3++;
                }
            }
            assertEquals(1, found1);
            assertEquals(1, found2);
            assertEquals(1, found3);
            assertEquals(3, iteratorSize);

           balance = map.get(keyAccount3Asset1);

            assertEquals(balance, balance3);
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
            accountKeys = ((ItemAssetBalanceSuit)((DBTabImpl)map).getMapSuit()).accountKeys(account1).iterator();
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
            accountKeys = ((ItemAssetBalanceSuit)((DBTabImpl)map).getMapSuit()).accountKeys(account1).iterator();

            int found1 = 0;
            int found2 = 0;
            int found3 = 0;
            iteratorSize = 0;
            while (accountKeys.hasNext()) {
                iteratorSize++;

                byte[] key = accountKeys.next();
                long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
                byte[] shortAddress = ItemAssetBalanceTab.getShortAccountFromKey(key);

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
            accountKeys = ((ItemAssetBalanceSuit)((DBTabImpl)map).getMapSuit()).accountKeys(account2).iterator();
            iteratorSize = 0;
            found1 = 0;
            found2 = 0;
            while (accountKeys.hasNext()) {
                iteratorSize++;

                byte[] key = accountKeys.next();
                long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
                byte[] shortAddress = ItemAssetBalanceTab.getShortAccountFromKey(key);

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

            accountKeys = ((ItemAssetBalanceSuit)((DBTabImpl)map).getMapSuit()).accountKeys(account3).iterator();
            iteratorSize = 0;
            found1 = 0;
            found2 = 0;
            while (accountKeys.hasNext()) {
                iteratorSize++;

                byte[] key = accountKeys.next();
                long assetKey = ItemAssetBalanceTab.getAssetKeyFromKey(key);
                byte[] shortAddress = ItemAssetBalanceTab.getShortAccountFromKey(key);

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