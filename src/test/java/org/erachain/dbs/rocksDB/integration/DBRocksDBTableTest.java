package org.erachain.dbs.rocksDB.integration;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.dbs.rocksDB.DBMapSuit;
import org.erachain.dbs.rocksDB.ItemAssetBalanceSuitRocksDB;
import org.erachain.dbs.rocksDB.utils.ConstantsRocksDB;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Test;
import org.mapdb.Fun;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksIterator;
import org.rocksdb.TransactionDB;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@Slf4j
public class DBRocksDBTableTest {

    //DCSet dcSet;
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

    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance1;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance2;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance3;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance4;

    private void init() {

        try {
            File tempDir = new File(ConstantsRocksDB.ROCKS_DB_FOLDER);
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        balance1 = new Fun.Tuple5<>(balAB, balAB, balAB, balAB, balAB);
        balance2 = new Fun.Tuple5<>(balBA, balBA, balBA, balBA, balBA);
        balance3 = new Fun.Tuple5<>(balAC, balAC, balAC, balAC, balAC);
        balance4 = new Fun.Tuple5<>(balBD, balBD, balAB, balBD, balBD);

    }

    @Test
    public void get() {
    }

    // TODO надо сделать проверку добавления и удаления с вторичными ключами и с учетом размера твблици - чтобы вторичные ключи правильно удалялись
    @Test
    public void put() {
    }

    @Test
    public void filterAppropriateValuesAsKeys() {
    }

    @Test
    public void filterAppropriateValuesAsByteKeys() {
    }

    @Test
    public void filterAppropriateKeys() {
    }

    @Test
    public void filterAppropriateValues() {
    }

    @Test
    public void receiveIndexByName() {
    }

    @Test
    public void receiveIndexSorted() {

        init();

        ItemAssetBalanceSuitRocksDB tab = new ItemAssetBalanceSuitRocksDB(null, null, null);

        long assetKey1 = 1L;

        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            long randLong = rand.nextLong();
            if (randLong < 0)
                randLong = -randLong;
            int assetKey = (int) randLong;
            if (assetKey < 0)
                assetKey = -assetKey;

            int randInt = rand.nextInt();
            BigDecimal balTest = new BigDecimal(randInt + "." + randLong);
            balTest = balTest.movePointLeft(rand.nextInt(20) - 3);
            balTest = balTest.setScale(TransactionAmount.maxSCALE, RoundingMode.HALF_DOWN);

            balTest = new BigDecimal(i - 5);

            //Account account = new PublicKeyAccount(Crypto.getInstance().digest(Longs.toByteArray(randLong)));

            // создаем новые ключи
            byte[] key = Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey));

            balance1 = new Fun.Tuple5<>(new Fun.Tuple2(balTest, balTest), balAB, balAC, balBD, balBD);
            //logger.info(balTest.toPlainString());
            tab.set(key, balance1);
        }

        BigDecimal value;
        Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
                balance = null;
        Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
                balanceTmp;

        ColumnFamilyHandle indexDB = ((DBMapSuit) tab).getIndex(1).getColumnFamilyHandle();

        long assetKeyTMP = 0;
        int iteratorSize = 0;
        try (RocksIterator iteratorFilteredNative = ((TransactionDB) ((DBRocksDBTable) tab.map).dbSource.getDbCore()).newIterator(indexDB)) {
            iteratorFilteredNative.seek(account1.getShortAddressBytes());
            while (iteratorFilteredNative.isValid()) {
                byte[] keyIter = iteratorFilteredNative.key();
                byte[] valueIter = iteratorFilteredNative.value();
                iteratorSize++;
                long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(valueIter);
                byte[] addressKey = ItemAssetBalanceMap.getShortAccountFromKey(valueIter);
                assertEquals(account1.equals(addressKey), true);

                balanceTmp = tab.get(valueIter);

                // Нужно положить их с отсутпом
                logger.error(" assetKey sorted: " + assetKey + " for bal:" + balanceTmp.a.b);

                if (assetKeyTMP > 0 && assetKeyTMP > assetKey) {
                    logger.error(" assetKey sorted: " + assetKey + " for bal:" + balanceTmp.a.b);
                    // всегда идем по возрастанию
                    assertEquals(assetKeyTMP > assetKey, false);
                }
                balance = balanceTmp;

                iteratorFilteredNative.next();
            }
        }
        logger.error(" NATIVE completed ");

        Set<byte[]> keysFiltered = ((DBRocksDBTable) tab.map).dbSource.filterApprropriateValues(account1.getShortAddressBytes(), indexDB);

        assetKeyTMP = 0;
        iteratorSize = 0;
        for (byte[] key: keysFiltered) {
            iteratorSize++;
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            byte[] addressKey = ItemAssetBalanceMap.getShortAccountFromKey(key);
            assertEquals(account1.equals(addressKey), true);

            balanceTmp = tab.get(key);

            // Нужно положить их с отсутпом
            logger.info(" assetKey sorted: " + assetKey + " for bal:" + balanceTmp.a.b);

            if (assetKeyTMP > 0 && assetKeyTMP > assetKey) {
                logger.error(" assetKey sorted: " + assetKey + " for bal:" + balanceTmp.a.b);
                // всегда идем по возрастанию
                assertEquals(assetKeyTMP > assetKey, false);
            }
            balance = balanceTmp;
        }

        logger.error(" Filter Apprropriate completed ");

        Iterator<byte[]> iterator = tab.accountIterator(account1);

        assetKeyTMP = 0;
        iteratorSize = 0;
        while (iterator.hasNext()) {
            byte[] key = iterator.next();
            iteratorSize++;
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            byte[] addressKey = ItemAssetBalanceMap.getShortAccountFromKey(key);
            assertEquals(account1.equals(addressKey), true);

            balanceTmp = tab.get(key);

            // Нужно положить их с отсутпом
            logger.error(" assetKey sorted: " + assetKey + " for bal:" + balanceTmp.a.b);

            if (assetKeyTMP > 0 && assetKeyTMP > assetKey) {
                logger.error(" assetKey sorted: " + assetKey + " for bal:" + balanceTmp.a.b);
                // всегда идем по возрастанию
                assertEquals(assetKeyTMP > assetKey, false);
            }
            balance = balanceTmp;
        }

        logger.error(" TAB account Keys completed ");

    }

    @Test
    public void getIndexIterator() {
    }

    @Test
    public void assetIteratorAndReplace() {
        init();

        ItemAssetBalanceSuitRocksDB tab = new ItemAssetBalanceSuitRocksDB(null, null, null);

        int size = tab.size();

        long assetKey1 = 1L;
        long assetKey2 = 2L;
        int iteratorSize = 0;

        int iteratorSize1 = 0;
        Iterator<byte[]> assetKeys = tab.getIteratorByAsset(assetKey1);
        while (assetKeys.hasNext()) {
            iteratorSize1++;
            byte[] key = assetKeys.next();
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            assertEquals(assetKey, assetKey2);

        }
        assertEquals(0, iteratorSize1);
        assertEquals(tab.size(), iteratorSize);

        // проверить не затирают ли ключи друг друга
        tab.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance1);
        tab.set(Bytes.concat(account1.getShortAddressBytes(), Longs.toByteArray(assetKey2)), balance1);

        iteratorSize = 0;
        assetKeys = tab.getIteratorByAsset(assetKey1);
        while (assetKeys.hasNext()) {
            iteratorSize++;
            assetKeys.next();
        }
        assertEquals(1, iteratorSize);

        iteratorSize = 0;
        assetKeys = tab.getIteratorByAsset(assetKey2);
        while (assetKeys.hasNext()) {
            iteratorSize++;
            assetKeys.next();
        }
        assertEquals(1, iteratorSize);


        tab.set(Bytes.concat(account2.getShortAddressBytes(), Longs.toByteArray(assetKey1)), balance2);

        byte[] keyAccount3Asset1 = Bytes.concat(account3.getShortAddressBytes(), Longs.toByteArray(assetKey1));
        tab.set(keyAccount3Asset1, balance1);

        Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
                balance = tab.get(keyAccount3Asset1);

        assertEquals(balance, balance1);

        assertEquals(tab.size(), size + 4);

        //////////////////
        assetKeys = tab.getIteratorByAsset(assetKey1);

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

            BigDecimal balanceTMP = tab.get(key).a.b;

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
        assetKeys = tab.getIteratorByAsset(assetKey2);
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


        ///////////// тот же КЛЮЧ но новый баланс
        tab.set(keyAccount3Asset1, balance3);

        assetKeys = tab.getIteratorByAsset(assetKey1);
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

        balance = tab.get(keyAccount3Asset1);

        assertEquals(balance, balance3);

        tab.close();
    }

}