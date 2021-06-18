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
import org.erachain.dbs.rocksDB.DBMapSuit;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Assert;
import org.junit.Test;
import org.mapdb.Fun;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@Slf4j
public class OrderProcessTest {

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

        assetA = new AssetVenture(itemAppData, gb.getCreator(), "ERA", icon, image, ".", 0, 8, 0L);

        issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte) 0, timestamp++, 0L);
        issueAssetTransaction.sign(accountA, Transaction.FOR_NETWORK);
        issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
        issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

        keyA = issueAssetTransaction.getAssetKey(dcSet);
        balanceA = accountA.getBalance(dcSet, keyA);

        assetB = new AssetVenture(itemAppData, gb.getCreator(), "COMPU", icon, image, ".", 0, 8, 0L);
        issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++, 0L);
        issueAssetTransaction.sign(accountB, Transaction.FOR_NETWORK);
        issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
        issueAssetTransaction.process(null, Transaction.FOR_NETWORK);
        keyB = issueAssetTransaction.getAssetKey(dcSet);

    }

    /**
     *
     */
    @Test
    public void process1() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                assetB = new AssetVenture(itemAppData, gb.getCreator(), "COMPU", icon, image, ".", 0, 8, 0L);
                issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++, 0L);
                issueAssetTransaction.sign(accountB, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);
                long asset10key = issueAssetTransaction.getAssetKey(dcSet);

                BigDecimal have1 = new BigDecimal("8.12");
                BigDecimal want1 = new BigDecimal("0.01771142");
                orderCreation = new CreateOrderTransaction(accountA, assetA.getKey(dcSet), assetB.getKey(dcSet),
                        have1, want1,
                        (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, true);
                orderCreation.process(null, Transaction.FOR_NETWORK);

                //Order order1 = new Order()
                //OrderProcess.process(dcSet);
                long order_AB_1_ID = orderCreation.getOrderId();
                Order order_AB_1 = Order.reloadOrder(dcSet, order_AB_1_ID);

                Assert.assertEquals(accountA.getBalanceForPosition(dcSet, keyA, Account.BALANCE_POS_OWN).b,
                        have1.negate()); // BALANCE
                Assert.assertEquals(accountA.getBalanceForPosition(dcSet, keyA, Account.BALANCE_POS_PLEDGE).b,
                        have1); // BALANCE
                Assert.assertEquals(accountA.getBalanceForPosition(dcSet, keyB, Account.BALANCE_POS_OWN).b,
                        new BigDecimal("0")); // BALANCE

                // https://explorer.erachain.org/index/blockexplorer.html?order=2068416-1&lang=en
                BigDecimal have2 = new BigDecimal("0.01112428");
                BigDecimal want2 = new BigDecimal("5.10005");
                orderCreation = new CreateOrderTransaction(accountB, assetB.getKey(dcSet), assetA.getKey(dcSet),
                        have2, want2,
                        (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, true);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                long order_BA_1_ID = orderCreation.getOrderId();
                Order order_BA_1 = Order.reloadOrder(dcSet, order_BA_1_ID);
                assertEquals(false, order_BA_1.isActive(dcSet));
                assertEquals(true, order_BA_1.isCompleted());

                assertEquals(true, order_AB_1.isActive(dcSet));
                assertEquals(false, order_AB_1.isCompleted());

                Assert.assertEquals(accountA.getBalanceForPosition(dcSet, keyA, Account.BALANCE_POS_OWN).b,
                        have1.negate()); // BALANCE
                Assert.assertEquals(accountB.getBalanceForPosition(dcSet, keyB, Account.BALANCE_POS_OWN).b,
                        have2.negate()); // BALANCE

                Trade trade1 = Trade.get(dcSet, order_BA_1, order_AB_1);
                Assert.assertEquals(trade1.getAmountWant(), have2);
                Assert.assertEquals(trade1.getAmountWant().multiply(order_BA_1.getPrice())
                        .setScale(8, RoundingMode.HALF_DOWN).stripTrailingZeros(), want2);
                Assert.assertEquals(trade1.getAmountHave(), new BigDecimal("5.10005147"));
                Assert.assertEquals(trade1.getAmountWant(), new BigDecimal("0.01112428"));
                // сделка проходит по более выгодной цене и получаем больше чем хотели:
                Assert.assertEquals(1, trade1.getAmountHave().compareTo(want2)); // 5.10005147 > 5.10005
                Assert.assertEquals(trade1.calcPrice(), new BigDecimal("0.002181209359"));
                Assert.assertEquals(order_AB_1.getPrice(), new BigDecimal("0.00218120936")); // 0.00218120936
                Assert.assertEquals(order_BA_1.calcPriceReverse(), new BigDecimal("0.002181209988"));

                Assert.assertEquals(accountA.getBalanceForPosition(dcSet, keyA, Account.BALANCE_POS_PLEDGE).b,
                        have1.subtract(trade1.getAmountHave())); // BALANCE
                Assert.assertEquals(accountA.getBalanceForPosition(dcSet, keyB, Account.BALANCE_POS_OWN).b,
                        trade1.getAmountWant()); // BALANCE

                Assert.assertEquals(accountB.getBalanceForPosition(dcSet, keyA, Account.BALANCE_POS_OWN).b,
                        trade1.getAmountHave()); // BALANCE
                Assert.assertEquals(accountB.getBalanceForPosition(dcSet, keyA, Account.BALANCE_POS_PLEDGE).b,
                        have2.subtract(trade1.getAmountWant()).stripTrailingZeros()); // BALANCE

                // https://explorer.erachain.org/index/blockexplorer.html?order=2068567-2&lang=en
                BigDecimal have3 = new BigDecimal("0.00658715");
                BigDecimal want3 = new BigDecimal("3.01994853");
                orderCreation = new CreateOrderTransaction(accountB, assetB.getKey(dcSet), assetA.getKey(dcSet),
                        have3, want3,
                        (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, true);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                long order_BA_2_ID = orderCreation.getOrderId();
                Order order_BA_2 = Order.reloadOrder(dcSet, order_BA_2_ID);

                assertEquals(false, order_BA_2.isActive(dcSet));

                assertEquals(accountA.getBalanceForPosition(dcSet, keyA, Account.BALANCE_POS_OWN).b,
                        have1.negate()); // BALANCE

                Trade trade2 = Trade.get(dcSet, order_BA_2, order_AB_1);
                assertEquals(trade2.getAmountWant(), have3);
                assertEquals(trade2.getAmountWant().multiply(order_BA_2.getPrice())
                        .setScale(8, RoundingMode.HALF_DOWN).stripTrailingZeros(), want3);
                assertEquals(trade2.getAmountHave(), new BigDecimal("3.01994853"));
                assertEquals(trade2.getAmountWant(), new BigDecimal("0.00658715"));
                // сделка проходит по более выгодной цене и получаем больше чем хотели:
                assertEquals(trade2.getAmountHave(), want3); // 5.10005147 > 5.10005
                assertEquals(trade2.calcPrice(), new BigDecimal("0.002181212671"));
                assertEquals(order_BA_2.calcPriceReverse(), new BigDecimal("0.002181212671"));

                assertEquals(accountB.getBalanceForPosition(dcSet, keyB, Account.BALANCE_POS_OWN).b,
                        have2.negate().subtract(have3)); // BALANCE

                assertEquals(accountA.getBalanceForPosition(dcSet, keyA, Account.BALANCE_POS_PLEDGE).b.stripTrailingZeros(),
                        BigDecimal.ZERO); // BALANCE
                assertEquals(accountA.getBalanceForPosition(dcSet, keyB, Account.BALANCE_POS_OWN).b,
                        have2.add(have3)); // BALANCE

                assertEquals(accountB.getBalanceForPosition(dcSet, keyA, Account.BALANCE_POS_OWN).b.stripTrailingZeros(),
                        have1); // BALANCE
                assertEquals(accountB.getBalanceForPosition(dcSet, keyA, Account.BALANCE_POS_PLEDGE).b,
                        BigDecimal.ZERO); // BALANCE

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

                    orderCreation = new CreateOrderTransaction(accountA, assetB.getKey(dcSet), assetA.getKey(dcSet), amountBuy,
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

                List<Order> orders = ordersMap.getOrdersForTradeWithFork(assetB.getKey(dcSet), assetA.getKey(dcSet),
                        null);
                assertEquals(orders.size(), len);

                //////////////////////////////////////////////////////////////////////////
                dcSet.flush(99999, true, false);
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

                orders = ordersMap.getOrdersForTradeWithFork(assetB.getKey(dcSet), assetA.getKey(dcSet),
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

            long have = assetB.getKey(dcSet);
            long want = assetA.getKey(dcSet);

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
                CreateOrderTransaction orderCreation1 = new CreateOrderTransaction(accountA, assetA.getKey(dcSet), assetB.getKey(dcSet),
                        amount10, amount100,
                        (byte) 0, timestamp++, 0L);
                orderCreation1.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation1.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, true);
                orderCreation1.process(null, Transaction.FOR_NETWORK);

                assertEquals(accountA.getBalanceForPosition(assetA.getKey(), Account.BALANCE_POS_PLEDGE).b, amount10);

                CreateOrderTransaction orderCreation2 = new CreateOrderTransaction(accountB, assetB.getKey(dcSet), assetA.getKey(dcSet),
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