package org.erachain.core.item.assets;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.IssueAssetTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.OrderMap;
import org.erachain.dbs.rocksDB.OrdersSuitRocksDB;
import org.erachain.dbs.rocksDB.common.RockStoreIterator;
import org.erachain.dbs.rocksDB.common.RocksDbDataSource;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.utils.ConstantsRocksDB;
import org.erachain.ntp.NTP;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Test;
import org.mapdb.Fun;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@Slf4j
public class OrderTest {

    int[] TESTED_DBS = new int[]{
            //IDB.DBS_MAP_DB,
            IDB.DBS_ROCK_DB};

    Long releaserReference = null;
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();

    Random random = new Random();
    long flags = 0l;
    int seqNo = 0;

    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balanceA;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balanceB;
    DCSet dcSet;
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
        try {
            File tempDir = new File(ConstantsRocksDB.ROCKS_DB_FOLDER);
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        dcSet = DCSet.createEmptyDatabaseSet(dbs);
        gb = new GenesisBlock();

        try {
            gb.process(dcSet);
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
        accountA.changeBalance(dcSet, false, ERM_KEY, BigDecimal.valueOf(100), false);
        accountA.changeBalance(dcSet, false, FEE_KEY, BigDecimal.valueOf(10), false);

        accountB.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, dcSet);
        accountB.changeBalance(dcSet, false, ERM_KEY, BigDecimal.valueOf(100), false);
        accountB.changeBalance(dcSet, false, FEE_KEY, BigDecimal.valueOf(10), false);

        assetA = new AssetVenture(new GenesisBlock().getCreator(), "AAA", icon, image, ".", 0, 8, 50000L);

        issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte) 0, timestamp++, 0l, new byte[64]);
        issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, 2, ++seqNo);
        issueAssetTransaction.process(null,Transaction.FOR_NETWORK);

        keyA = issueAssetTransaction.getAssetKey(dcSet);
        balanceA = accountA.getBalance(dcSet, keyA);

        assetB = new AssetVenture(new GenesisBlock().getCreator(), "BBB", icon, image, ".", 0, 8, 50000L);
        issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++,
                0L, new byte[64]);
        issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, 2, ++seqNo);
        issueAssetTransaction.process(null,Transaction.FOR_NETWORK);
        keyB = issueAssetTransaction.getAssetKey(dcSet);

        // CREATE ORDER TRANSACTION
        orderCreation = new CreateOrderTransaction(accountA, keyA, 3l, BigDecimal.valueOf(10), BigDecimal.valueOf(100),
                (byte) 0, timestamp, 0l);

    }

    // reload from DB
    private Order reloadOrder(Order order) {

        return dcSet.getCompletedOrderMap().contains(order.getId()) ? dcSet.getCompletedOrderMap().get(order.getId())
                : dcSet.getOrderMap().get(order.getId());

    }

    private Order reloadOrder(Long orderId) {

        return dcSet.getCompletedOrderMap().contains(orderId) ? dcSet.getCompletedOrderMap().get(orderId)
                : dcSet.getOrderMap().get(orderId);

    }

    private void removeOrder(Long orderId) {

        dcSet.getCompletedOrderMap().delete(orderId);
        dcSet.getOrderMap().delete(orderId);

    }

    /**
     * Ситуация следующая - есть список ордеров которые подходят для текущего и один из них не поностью исполняется
     *  - он переписывается в форкнутую базу
     *  Затем второй ордер к этому списку обрабатывается. And в списке полявляется двойная запись
     *  ранее покусанного ордера и его родитель из родительской таблицы. Надо сэмулировать такой случай и проверять тут
     *
     */
    @Test
    public void processDoubleInFork() {

        for (int dbs : TESTED_DBS) {
            init(dbs);

            // создадим много ордеров
            int len = 10;
            for (int i = 0; i < len; i++) {
                BigDecimal amountSell = new BigDecimal("100");
                BigDecimal amountBuy = new BigDecimal("" + (100 - (len >> 1) + i));

                orderCreation = new CreateOrderTransaction(accountA, assetB.getKey(dcSet), assetA.getKey(dcSet), amountBuy,
                        amountSell, (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, 2, ++seqNo);
                orderCreation.process(null, Transaction.FOR_NETWORK);

            }

            OrderMap ordersMap = dcSet.getOrderMap();
            OrdersSuitRocksDB source = (OrdersSuitRocksDB) ordersMap.getSource();
            DBRocksDBTable<Long, Order> mapRocks = source.map;
            RocksDbDataSource mapSource = mapRocks.dbSource;
            RockStoreIterator iteratorRocks = mapSource.indexIterator(false, 1);
            int count = 0;
            while (iteratorRocks.hasNext()) {
                byte[] key = iteratorRocks.next();
                count++;
            }
            assertEquals(count, len);

            Iterator<Long> iterator = ordersMap.getIterator();
            count = 0;
            while (iterator.hasNext()) {
                Long key = iterator.next();
                Order value = ((OrdersSuitRocksDB) ordersMap.getSource()).get(key);
                String price = value.viewPrice();
                count++;
            }
            assertEquals(count, len);

            // тут в базе форкнутой должен быть ордер из стенки в измененном виде
            // а повторный расчет в форке не должен его дублировать
            List<Order> orders = ordersMap.getOrdersForTradeWithFork(assetB.getKey(dcSet), assetA.getKey(dcSet),
                    null);

            DCSet forkDC = dcSet.fork();
            // создадим первый ордер который изменит ордера стенки
            orderCreation = new CreateOrderTransaction(accountB, assetA.getKey(dcSet), assetB.getKey(dcSet),
                    new BigDecimal("10"),
                    new BigDecimal("10"), (byte) 0, timestamp++, 0L);
            orderCreation.sign(accountB, Transaction.FOR_NETWORK);
            orderCreation.setDC(forkDC, Transaction.FOR_NETWORK, 2, ++seqNo);
            orderCreation.process(null, Transaction.FOR_NETWORK);

            ordersMap = forkDC.getOrderMap();
            // тут в базе форкнутой должен быть ордер из стенки в измененном виде
            // а повторный расчет в форке не должен его дублировать
            orders = ordersMap.getOrdersForTradeWithFork(assetB.getKey(forkDC), assetA.getKey(forkDC), null);

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

            dcSet.close();
        }
    }

    @Test
    public void orphan() {
    }
}