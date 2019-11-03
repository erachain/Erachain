package org.erachain.core.item.assets;

import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.IssueAssetTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.OrderMap;
import org.erachain.ntp.NTP;
import org.junit.Test;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class OrderTest {

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
    Order orderREC;
    Trade tradeREC;
    // CREATE KNOWN ACCOUNT
    PrivateKeyAccount accountA;
    PrivateKeyAccount accountB;
    IssueAssetTransaction issueAssetTransaction;
    AssetCls assetA;
    long keyA;
    AssetCls assetB;
    long keyB;
    CreateOrderTransaction orderCreation;
    BigDecimal bal_A_keyA;
    BigDecimal bal_A_keyB;
    BigDecimal bal_B_keyA;
    BigDecimal bal_B_keyB;
    Order order_AB_1;
    Order order_AB_2;
    Order order_AB_3;
    Order order_AB_4;
    Order order_AB_5;
    Order order_AB_6;
    Order order_AB_7;
    Order order_AB_8;
    Order order_BA_1;
    Order order_BA_2;
    Order order_BA_3;
    Order order_BA_4;
    Order order_BA_5;
    Order order_BA_6;
    Order order_BA_7;
    Order order_BA_8;
    Long order_AB_1_ID;
    Long order_AB_2_ID;
    Long order_AB_3_ID;
    Long order_AB_4_ID;
    Long order_AB_5_ID;
    Long order_AB_6_ID;
    Long order_AB_7_ID;
    Long order_AB_8_ID;
    Long order_BA_1_ID;
    Long order_BA_2_ID;
    Long order_BA_3_ID;
    Long order_BA_4_ID;
    Long order_BA_5_ID;
    Long order_BA_6_ID;
    Long order_BA_7_ID;
    Long order_BA_8_ID;
    BigDecimal trade_1_amoA;
    BigDecimal trade_1_amoB;
    BigDecimal trade_2_amoA;
    BigDecimal trade_2_amoB;
    BigDecimal trade_3_amoA;
    BigDecimal trade_3_amoB;
    BigDecimal trade_4_amoA;
    BigDecimal trade_4_amoB;
    BigDecimal trade_5_amoA;
    BigDecimal trade_5_amoB;
    private byte[] icon = new byte[0]; // default value
    private byte[] image = new byte[0]; // default value

    int haveAssetScale = 8;
    int wantAssetScale = 8;

    //@Before
    private void init() {

        dcSet = DCSet.createEmptyDatabaseSet();
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
                accountB.getLastTimestamp(dcSet)[0], new byte[64]);
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

        init();

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

        DCSet forkDC = dcSet.fork();
        // создадим первый ордер который изменит ордера стенки
        orderCreation = new CreateOrderTransaction(accountB, assetA.getKey(dcSet), assetB.getKey(dcSet),
                new BigDecimal("10"),
                new BigDecimal("10"), (byte) 0, timestamp++, 0L);
        orderCreation.sign(accountB, Transaction.FOR_NETWORK);
        orderCreation.setDC(forkDC, Transaction.FOR_NETWORK, 2, ++seqNo);
        orderCreation.process(null, Transaction.FOR_NETWORK);

        OrderMap ordersMap = forkDC.getOrderMap();
        // тут в базе форкнутой должен быть ордер из стенки в измененном виде
        // а повторный расчет в форке не должен его дублировать
        List<Order> orders = ordersMap.getOrdersForTradeWithFork(assetB.getKey(dcSet), assetA.getKey(dcSet), false);

        assertEquals(orders.size(), len);
        assertEquals(ordersMap.size(), len);

        // эмуляция отмены некотрого ордера
        ordersMap.delete(orders.get(5).getId());

        assertEquals(ordersMap.size(), len-1);

        // эмуляция отмены измененого ордера, записанного в Форк
        ordersMap.delete(orders.get(0));

        assertEquals(ordersMap.size(), len-2);

        // ПОВТОРНО отмены измененого ордера, записанного в Форк
        ordersMap.delete(orders.get(0));

        assertEquals(ordersMap.size(), len-2);

        // добавим назад
        ordersMap.add(orders.get(0));

        assertEquals(ordersMap.size(), len-1);

        // ПОВТОРНО добавим назад
        ordersMap.add(orders.get(0));

        assertEquals(ordersMap.size(), len-1);

    }

    @Test
    public void orphan() {
    }
}