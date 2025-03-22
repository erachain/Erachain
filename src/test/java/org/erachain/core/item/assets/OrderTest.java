package org.erachain.core.item.assets;

import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.IssueAssetTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.OrderMap;
import org.erachain.datachain.TransactionFinalMapImpl;
import org.erachain.datachain.TransactionFinalMapSigns;
import org.erachain.dbs.DBSuit;
import org.erachain.dbs.rocksDB.DBMapSuit;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Test;
import org.mapdb.Fun;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@Slf4j
public class OrderTest {

    int[] TESTED_DBS = new int[]{
            IDB.DBS_MAP_DB,
            IDB.DBS_ROCK_DB};

    int height;

    Long releaserReference = null;
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();

    Random random = new Random();
    byte[] itemAppData = null;
    int seqNo = 0;

    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balanceA;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balanceB;
    DCSet dcSet;
    BlockChain chain;
    GenesisBlock gb;
    // CREATE KNOWN ACCOUNT
    PrivateKeyAccount accountA;
    PrivateKeyAccount accountB;
    IssueAssetTransaction issueAssetTransaction;
    AssetCls assetA;
    long keyA;
    AssetCls assetB;
    long keyB;
    CreateOrderTransaction orderCreation;
    private byte[] icon = new byte[0]; // default value
    private byte[] image = new byte[0]; // default value

    //@Before
    private void init(int dbs) {

        logger.info(" ********** open DBS: " + dbs);

        File tempDir = new File(Settings.getInstance().getDataTempDir());
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        BlockChain.CHECK_BUGS = 10;
        height = BlockChain.ALL_BALANCES_OK_TO + 2;

        dcSet = DCSet.createEmptyHardDatabaseSetWithFlush(tempDir.getPath(), dbs);

        try {
            chain = new BlockChain(dcSet);
            gb = chain.getGenesisBlock();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        byte[] seed = Crypto.getInstance().digest("test_A".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        accountA = new PrivateKeyAccount(privateKey);
        seed = Crypto.getInstance().digest("test_B".getBytes());
        privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        accountB = new PrivateKeyAccount(privateKey);

        // FEE FUND
        accountA.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, dcSet);
        accountA.changeBalance(dcSet, false, false, ERM_KEY, BigDecimal.valueOf(100), false, false, false);
        accountA.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(10), false, false, false);

        accountB.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, dcSet);
        accountB.changeBalance(dcSet, false, false, ERM_KEY, BigDecimal.valueOf(100), false, false, false);
        accountB.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(10), false, false, false);

        assetA = new AssetVenture(itemAppData, gb.getCreator(), "AAA", icon, image, ".", 0, 8, 50000L);

        issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte) 0, timestamp++, 0l, new byte[64]);
        issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
        issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

        keyA = issueAssetTransaction.getAssetKey();
        balanceA = accountA.getBalance(dcSet, keyA);

        assetB = new AssetVenture(itemAppData, gb.getCreator(), "BBB", icon, image, ".", 0, 8, 50000L);
        issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++,
                0L, new byte[64]);
        issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
        issueAssetTransaction.process(null, Transaction.FOR_NETWORK);
        keyB = issueAssetTransaction.getAssetKey();

        // CREATE ORDER TRANSACTION
        orderCreation = new CreateOrderTransaction(accountA, keyA, 3l, BigDecimal.valueOf(10), BigDecimal.valueOf(100),
                (byte) 0, timestamp, 0l);

    }

    @Test
    public void powerTen() {
        assertEquals(Order.powerTen(new BigDecimal(0)), 0);
        assertEquals(Order.powerTen(new BigDecimal(500)), 2);
        assertEquals(Order.powerTen(new BigDecimal(100)), 2);
        assertEquals(Order.powerTen(new BigDecimal(50)), 1);
        assertEquals(Order.powerTen(new BigDecimal(10)), 1);
        assertEquals(Order.powerTen(new BigDecimal(5)), 0);
        assertEquals(Order.powerTen(new BigDecimal(1)), 0);
        assertEquals(Order.powerTen(new BigDecimal(0.5)), -1);
        assertEquals(Order.powerTen(new BigDecimal(0.1)), -1);
        assertEquals(Order.powerTen(new BigDecimal(0.05)), -2);
        assertEquals(Order.powerTen(new BigDecimal(0.01)), -2);

    }

    /**
     * Ситуация следующая - есть список ордеров которые подходят для текущего и один из них не поностью исполняется
     * - он переписывается в форкнутую базу
     * Затем второй ордер к этому списку обрабатывается. And в списке появляется двойная запись
     * ранее покусанного ордера и его родитель из родительской таблицы. Надо съэмулировать такой случай и проверять тут
     */
    @Test
    public void processDoubleInFork() {


        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                Iterator<Long> iterator;
                int count = 0;

                OrderMap ordersMap = dcSet.getOrderMap();

                // создадим много ордеров
                int len = 10;
                for (int i = 0; i < len; i++) {
                    BigDecimal amountSell = new BigDecimal("100");
                    BigDecimal amountBuy = new BigDecimal("" + (100 - (len >> 1) + i));

                    orderCreation = new CreateOrderTransaction(accountA, assetB.getKey(), assetA.getKey(), amountBuy,
                            amountSell, (byte) 0, timestamp++, 0L);
                    orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, true);
                    orderCreation.process(null, Transaction.FOR_NETWORK);

                    iterator = ordersMap.getIndexIterator(0, false);
                    count = 0;
                    while (iterator.hasNext()) {
                        Long key = iterator.next();
                        Order value = ordersMap.get(key);
                        String price = value.viewPrice();
                        count++;
                    }
                    assertEquals(count, i + 1);

                }

                DBSuit suit = ordersMap.getSuit();
                iterator = suit.getIndexIterator(1, false);

                count = 0;
                while (iterator.hasNext()) {
                    Long key = iterator.next();
                    count++;
                }
                assertEquals(count, len);

                iterator = ordersMap.getIndexIterator(0, false);
                count = 0;
                while (iterator.hasNext()) {
                    Long key = iterator.next();
                    Order value = ordersMap.get(key);
                    String price = value.viewPrice();
                    count++;
                }
                assertEquals(count, len);

                // тут в базе форкнутой должен быть ордер из стенки в измененном виде
                // а повторный расчет в форке не должен его дублировать
                List<Order> orders = ordersMap.getOrdersForTradeWithFork(assetB.getKey(), assetA.getKey(),
                        null);

                DCSet forkDC = dcSet.fork(this.toString());
                // создадим первый ордер который изменит ордера стенки
                orderCreation = new CreateOrderTransaction(accountB, assetA.getKey(), assetB.getKey(),
                        new BigDecimal("10"),
                        new BigDecimal("10"), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountB, Transaction.FOR_NETWORK);
                orderCreation.setDC(forkDC, Transaction.FOR_NETWORK, height, ++seqNo, true);
                orderCreation.process(null, Transaction.FOR_NETWORK);

                ordersMap = forkDC.getOrderMap();
                // тут в базе форкнутой должен быть ордер из стенки в измененном виде
                // а повторный расчет в форке не должен его дублировать
                orders = ordersMap.getOrdersForTradeWithFork(assetB.getKey(), assetA.getKey(), null);

                assertEquals(orders.size(), len);
                if (ordersMap.isSizeEnable()) {
                    assertEquals(ordersMap.size(), len);
                }

                // эмуляция отмены некотрого ордера
                ordersMap.delete(orders.get(5).getId());

                if (ordersMap.isSizeEnable()) {
                    assertEquals(ordersMap.size(), len - 1);
                }

                // эмуляция отмены измененого ордера, записанного в Форк
                ordersMap.delete(orders.get(0));

                if (ordersMap.isSizeEnable()) {
                    assertEquals(ordersMap.size(), len - 2);
                }

                // ПОВТОРНО отмены измененого ордера, записанного в Форк
                ordersMap.delete(orders.get(0));

                if (ordersMap.isSizeEnable()) {
                    assertEquals(ordersMap.size(), len - 2);
                }

                // добавим назад
                ordersMap.put(orders.get(0));

                if (ordersMap.isSizeEnable()) {
                    assertEquals(ordersMap.size(), len - 1);
                }

                // ПОВТОРНО добавим назад
                ordersMap.put(orders.get(0));

                if (ordersMap.isSizeEnable()) {
                    assertEquals(ordersMap.size(), len - 1);
                }

            } finally {
                dcSet.close();
            }
        }
    }

    /**
     * Ошибка в первичном индексе - Итератор в 2 раза больше длинны чем индексов
     * и получает какието первые значения кривые
     */
    @Test
    public void iteratorDBMain() {


        for (int dbs : TESTED_DBS) {
            try {
                init(dbs);

                Iterator iterator;

                int count = 0;

                TransactionFinalMapSigns transSignsMap = dcSet.getTransactionFinalMapSigns();
                TransactionFinalMapImpl transFinMap = dcSet.getTransactionFinalMap();
                iterator = transFinMap.getIterator();
                count = 0;
                while (iterator.hasNext()) {
                    count++;
                    Long key = (Long) iterator.next();
                    Transaction value = transFinMap.get(key);
                    String seqNo = value.viewHeightSeq();
                    Long keyFromSign = transSignsMap.get(value.getSignature());
                    if (value.getSeqNo() == 43) {
                        // косяк в Генесизе - 2 одинаковых записи
                        continue;
                    }
                    assertEquals(Transaction.viewDBRef(keyFromSign), value.viewHeightSeq());
                }
                assertEquals(count, 272);

                iterator = transSignsMap.getIterator();
                count = 0;
                while (iterator.hasNext()) {
                    byte[] key = (byte[]) iterator.next();
                    Long value = transSignsMap.get(key);
                    count++;
                }
                assertEquals(count, 271); // косяк в Генесизе - 2 одинаковых записи

                OrderMap ordersMap = dcSet.getOrderMap();

                // создадим много ордеров
                int len = 10;
                for (int i = 0; i < len; i++) {
                    BigDecimal amountSell = new BigDecimal("100");
                    BigDecimal amountBuy = new BigDecimal("" + (100 - (len >> 1) + i));

                    orderCreation = new CreateOrderTransaction(accountA, assetB.getKey(), assetA.getKey(), amountBuy,
                            amountSell, (byte) 0, timestamp++, 0L);
                    orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, true);
                    orderCreation.process(null, Transaction.FOR_NETWORK);

                }

                ///dcSet.flush(99999, true, false);
                iterator = ordersMap.getIterator();
                count = 0;
                while (iterator.hasNext()) {
                    Long key = (Long) iterator.next();
                    Order value = ordersMap.get(key);
                    String price = value.viewPrice();

                    count++;
                }
                assertEquals(count, len);

                iterator = ordersMap.getIndexIterator(1, false);
                count = 0;
                while (iterator.hasNext()) {
                    Long key = (Long) iterator.next();
                    Order value = ordersMap.get(key);
                    String price = value.viewPrice();

                    count++;
                }
                assertEquals(count, len);

                List<Order> orders = ordersMap.getOrdersForTradeWithFork(assetB.getKey(), assetA.getKey(),
                        null);
                assertEquals(orders.size(), len);

                //////////////////////////////////////////////////////////////////////////
                dcSet.flush(0, true, false);
                //////////////////////////////////////////////////////////////////////////
                iterator = ordersMap.getIterator();
                count = 0;
                while (iterator.hasNext()) {
                    Long key = (Long) iterator.next();
                    Order value = ordersMap.get(key);
                    String price = value.viewPrice();

                    count++;
                }
                assertEquals(count, len);

                iterator = ordersMap.getIndexIterator(1, false);
                count = 0;
                while (iterator.hasNext()) {
                    Long key = (Long) iterator.next();
                    Order value = ordersMap.get(key);
                    String price = value.viewPrice();

                    count++;
                }
                assertEquals(count, len);

                orders = ordersMap.getOrdersForTradeWithFork(assetB.getKey(), assetA.getKey(),
                        null);
                assertEquals(orders.size(), len);

            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void iteratorRocks() {

        try {
            init(IDB.DBS_ROCK_DB);

            long have = assetB.getKey();
            long want = assetA.getKey();

            int count = 0;

            OrderMap ordersMap = dcSet.getOrderMap();

            // создадим много ордеров
            int len = 10;
            for (int i = 0; i < len; i++) {
                BigDecimal amountSell = new BigDecimal("100");
                BigDecimal amountBuy = new BigDecimal("" + (100 - (len >> 1) + i));

                orderCreation = new CreateOrderTransaction(accountA, have, want, amountBuy,
                        amountSell, (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, true);
                orderCreation.process(null, Transaction.FOR_NETWORK);

            }

            byte[] filter = org.bouncycastle.util.Arrays.concatenate(
                    Longs.toByteArray(have),
                    Longs.toByteArray(want));

            IndexDB indexDB = ((DBMapSuit) ordersMap.getSuit()).getIndex(0);
            assertEquals(indexDB.getNameIndex(), "orders_key_have_want");
            Iterator iterator = ((DBMapSuit) ordersMap.getSuit()).map.getIndexIteratorFilter(indexDB.getColumnFamilyHandle(), filter, false, true);

            List<Order> result = new ArrayList<>();
            count = 0;
            while (iterator.hasNext()) {
                count++;
                result.add(ordersMap.get((Long) iterator.next()));

            }
            assertEquals(result.size(), len);

            List<Order> orders = ordersMap.getOrdersForTradeWithFork(have, want, null);
            assertEquals(orders.size(), len);

            /////////////// SEEK price
            orders = ordersMap.getOrdersForTradeWithFork(have, want, new BigDecimal("100"));
            assertEquals(orders.size(), len);

            orders = ordersMap.getOrdersForTradeWithFork(have, want, new BigDecimal("-100"));
            assertEquals(orders.size(), 1);

            orders = ordersMap.getOrdersForTradeWithFork(have, want, new BigDecimal("1.00"));
            assertEquals(orders.size(), 6);

        } finally {
            dcSet.close();
        }
    }

    @Test
    public void pledge1() {


        for (int dbs : TESTED_DBS) {
            try {
                init(dbs);

                TransactionFinalMapSigns transSignsMap = dcSet.getTransactionFinalMapSigns();
                TransactionFinalMapImpl transFinMap = dcSet.getTransactionFinalMap();


                BigDecimal amount1 = BigDecimal.ONE;
                BigDecimal amount10 = BigDecimal.TEN;
                BigDecimal amount100 = new BigDecimal("100");
                CreateOrderTransaction orderCreation1 = new CreateOrderTransaction(accountA, assetA.getKey(), assetB.getKey(),
                        amount10, amount100,
                        (byte) 0, timestamp++, 0L);
                orderCreation1.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation1.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, true);
                orderCreation1.process(null, Transaction.FOR_NETWORK);

                assertEquals(accountA.getBalanceForPosition(assetA.getKey(), Account.BALANCE_POS_PLEDGE).b, amount10);

                CreateOrderTransaction orderCreation2 = new CreateOrderTransaction(accountB, assetB.getKey(), assetA.getKey(),
                        amount10,
                        amount1, (byte) 0, timestamp++, 0L);
                orderCreation2.sign(accountB, Transaction.FOR_NETWORK);
                orderCreation2.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, true);
                orderCreation2.process(null, Transaction.FOR_NETWORK);

                assertEquals(accountA.getBalanceForPosition(assetA.getKey(), Account.BALANCE_POS_PLEDGE).b, amount10.subtract(amount1));

                orderCreation2.orphan(null, Transaction.FOR_NETWORK);
                assertEquals(accountA.getBalanceForPosition(assetA.getKey(), Account.BALANCE_POS_PLEDGE).b, amount10);

                orderCreation1.orphan(null, Transaction.FOR_NETWORK);
                assertEquals(accountA.getBalanceForPosition(assetA.getKey(), Account.BALANCE_POS_PLEDGE).b, BigDecimal.ZERO);

            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void orphan() {
    }



}