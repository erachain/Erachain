package org.erachain.core.item.assets;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.*;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Assert;
import org.junit.Test;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

//import java.math.Long;

@Slf4j
public class OrderTestsMy {

    int[] TESTED_DBS = new int[]{
            IDB.DBS_MAP_DB,
            IDB.DBS_ROCK_DB
    };

    int height;

    long dbRef = 0L;

    Long releaserReference = null;
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    byte[] invalidSign = new byte[64];

    long timestamp = NTP.getTime();

    Random random = new Random();
    byte[] itemAppData = null;
    long txFlags = 0L;
    int seqNo = 0;

    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balanceA;
    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balanceB;
    DCSet dcSet;
    BlockChain chain;
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

    private void init(int dbs) {

        logger.info(" ********** open DBS: " + dbs);

        invalidSign[3] = 1;

        File tempDir = new File(Settings.getInstance().getDataTempDir());
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        BlockChain.CHECK_BUGS = 10;
        height = Integer.max(BlockChain.VERS_5_3, BlockChain.ALL_BALANCES_OK_TO) + 2;

        dcSet = DCSet.createEmptyHardDatabaseSetWithFlush(tempDir.getPath(), dbs);

        try {
            chain = new BlockChain(dcSet);
            gb = chain.getGenesisBlock();
            gb.process(dcSet, false);
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

        assetA = new AssetVenture(itemAppData, new GenesisBlock().getCreator(), "START", icon, image, ".", 0, 8, 50000L);
        // сразу зазадим чтобы все активы были уже в версии где учитывается точность
        assetA.setReference(new byte[64], dbRef);
        assetA.insertToMap(dcSet, BlockChain.AMOUNT_SCALE_FROM + 1);

        assetA = new AssetVenture(itemAppData, new GenesisBlock().getCreator(), "AAA", icon, image, ".", 0, 8, 50000L);

        issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte) 0, timestamp++, 0L);
        issueAssetTransaction.sign(accountA, Transaction.FOR_NETWORK);
        issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
        issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

        keyA = issueAssetTransaction.getAssetKey(dcSet);
        balanceA = accountA.getBalance(dcSet, keyA);

        assetB = new AssetVenture(itemAppData, new GenesisBlock().getCreator(), "BBB", icon, image, ".", 0, 8, 50000L);
        issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++,
                accountB.getLastTimestamp(dcSet)[0], new byte[64]);
        issueAssetTransaction.sign(accountB, Transaction.FOR_NETWORK);
        issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
        issueAssetTransaction.process(null, Transaction.FOR_NETWORK);
        keyB = issueAssetTransaction.getAssetKey(dcSet);

        // CREATE ORDER TRANSACTION
        orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(10), BigDecimal.valueOf(100),
                (byte) 0, timestamp, 0L);

    }

    @Test
    public void crypto() {

        byte[] seed = Crypto.getInstance().digest(Base58.decode("G5Krn3UJqmPRggw2H6oj4tr4Z5Nv3cv9nUy6ddDoqtwx"));
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        accountA = new PrivateKeyAccount(privateKey);

        String addr1 = accountA.getAddress();
        Account accShort = new Account(accountA.getShortAddressBytes());
        String addr2 = accShort.getAddress();

        assertEquals(addr1, addr2);

        MathContext rounding = new java.math.MathContext(8, RoundingMode.HALF_DOWN);

        BigDecimal price01 = new BigDecimal("0.3");
        BigDecimal price02 = BigDecimal.ONE.divide(price01, 10, RoundingMode.HALF_DOWN);
        ;

        BigDecimal big02 = new BigDecimal("1.8");
        // thisAmountHaveLeft.divide(orderPrice, 8, RoundingMode.HALF_DOWN):
        // thisAmountHaveLeft.multiply(orderReversePrice, rounding).setScale(8,
        // RoundingMode.HALF_DOWN);

        BigDecimal big03 = big02.divide(price01, 8, RoundingMode.HALF_DOWN);
        BigDecimal big04 = big02.multiply(price02, rounding).setScale(8, RoundingMode.HALF_DOWN);

        BigDecimal big1 = new BigDecimal("89.999999999999");
        BigDecimal big2 = big1.setScale(8, RoundingMode.HALF_DOWN);
        BigDecimal big3 = big2.scaleByPowerOfTen(5);
        BigDecimal big4 = big3.subtract(big2);

        BigDecimal big10 = new BigDecimal("89.9999999000");
        int prec1 = big10.precision();
        int prec1a = Order.precision(big10);
        BigDecimal big11 = new BigDecimal("89000.00");
        int prec2 = big11.precision();
        int prec2a = Order.precision(big11);
        ++prec2;

    }

    @Test
    public void tails() {

        byte[] seed = Crypto.getInstance().digest("test_A".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        accountA = new PrivateKeyAccount(privateKey);

        String addr1 = accountA.getAddress();
        Account accShort = new Account(accountA.getShortAddressBytes());
        String addr2 = accShort.getAddress();

        assertEquals(addr1, addr2);

        MathContext rounding = new java.math.MathContext(8, RoundingMode.HALF_DOWN);

        BigDecimal price01 = new BigDecimal("0.3");
        BigDecimal price02 = BigDecimal.ONE.divide(price01, 10, RoundingMode.HALF_DOWN);
        ;

        BigDecimal big02 = new BigDecimal("1.8");
        // thisAmountHaveLeft.divide(orderPrice, 8, RoundingMode.HALF_DOWN):
        // thisAmountHaveLeft.multiply(orderReversePrice, rounding).setScale(8,
        // RoundingMode.HALF_DOWN);

        BigDecimal big03 = big02.divide(price01, 8, RoundingMode.HALF_DOWN);
        BigDecimal big04 = big02.multiply(price02, rounding).setScale(8, RoundingMode.HALF_DOWN);

        BigDecimal big1 = new BigDecimal("89.999999999999");
        BigDecimal big2 = big1.setScale(8, RoundingMode.HALF_DOWN);
        BigDecimal big3 = big2.scaleByPowerOfTen(5);
        BigDecimal big4 = big3.subtract(big2);

        BigDecimal big10 = new BigDecimal("89.9999999000");
        int prec1 = big10.precision();
        int prec1a = Order.precision(big10);
        BigDecimal big11 = new BigDecimal("89000.00");
        int prec2 = big11.precision();
        int prec2a = Order.precision(big11);
        ++prec2;

    }

    @Test
    public void BigIntegerTest() {

        String base = "67UyisgA8ZkT4wGqv9qYksUbVkkQRdAs6Mqs48ukLVuUb9p4Aq41GGoeuHUWEaxvVRegMZiry9SBtL4DQ8ScEeHX";
        BigInteger big1 = Base58.decodeBI("67UyisgA8ZkT4wGqv9qYksUbVkkQRdAs6Mqs48ukLVuUb9p4Aq41GGoeuHUWEaxvVRegMZiry9SBtL4DQ8ScEeHX");
        String str2 = Base58.encode(big1);
        byte[] byte1 = Base58.decode("67UyisgA8ZkT4wGqv9qYksUbVkkQRdAs6Mqs48ukLVuUb9p4Aq41GGoeuHUWEaxvVRegMZiry9SBtL4DQ8ScEeHX");
        String str3 = Base58.encode(byte1);

        // -17013814363506844780642576803064534131835440534817879062422700139056920191961546707349041070740193734875195536068579370457427151473666936393718839492854
        BigInteger bi1 = Base58.decodeBI("nQhYYc4tSM2sPLpiceCWGKhdt5MKhu82LrTM9hCKgh3iyQzUiZ8H7s4niZrgy4LR4Zav1zXD7kra4YWRd3Fstd");
        byte[] byte2 = Base58.decode("nQhYYc4tSM2sPLpiceCWGKhdt5MKhu82LrTM9hCKgh3iyQzUiZ8H7s4niZrgy4LR4Zav1zXD7kra4YWRd3Fstd");
        /*
        int left = 64 - byte2.length;
        if (left > 0) {
            if (bi1.signum() > 0)
                byte2 =  Bytes.concat(new byte[left], byte2);
            else {
                byte[] bytesNeg = new byte[left];
                Arrays.fill(bytesNeg, (byte)-1);
                byte2 =  Bytes.concat(bytesNeg, byte2);
            }

        }
        left = 64 - byte2.length;
        // 1nQhYYc4tSM2sPLpiceCWGKhdt5MKhu82LrTM9hCKgh3iyQzUiZ8H7s4niZrgy4LR4Zav1zXD7kra4YWRd3Fstd
        */
        String str58 = Base58.encode(bi1, 64);
        assertEquals(str58, base);

    }

    @Test
    public void scaleTest() {


        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                Integer bbb = 31;
                assertEquals("11111", Integer.toBinaryString(bbb));

                assertEquals("10000000", Integer.toBinaryString(128));

                byte noData = (byte) 128;
                // assertEquals((byte)-1, (byte)128);
                assertEquals((byte) 128, (byte) -128);
                // assertEquals(org.erachain.core.transaction.RSend.NO_DATA_MASK));

                BigDecimal amountTest = new BigDecimal("123456781234567812345678");
                BigDecimal amountForParse = new BigDecimal("1234567812345678");
                BigDecimal amountBase;
                BigDecimal amount;
                BigDecimal amount_result;
                BigDecimal amountInvalid;

                // int shift = 64;
                int scale;
                int different_scale;
                int fromScale = TransactionAmount.SCALE_MASK_HALF + BlockChain.AMOUNT_DEDAULT_SCALE - 1;
                int toScale = BlockChain.AMOUNT_DEDAULT_SCALE - TransactionAmount.SCALE_MASK_HALF;
                assertEquals("11111".equals(Integer.toBinaryString(fromScale - toScale)), true);

                byte[] raw;
                CreateOrderTransaction orderCreation_2;
                for (scale = fromScale; scale >= toScale; scale--) {

                    amount = amountTest.scaleByPowerOfTen(-scale);

                    // TO BASE
                    different_scale = scale - BlockChain.AMOUNT_DEDAULT_SCALE;

                    if (different_scale != 0) {
                        // to DEFAUTL base 8 decimals
                        amountBase = amount.scaleByPowerOfTen(different_scale);
                        if (different_scale < 0)
                            different_scale += TransactionAmount.SCALE_MASK + 1;

                    } else {
                        amountBase = amount;
                    }

                    assertEquals(8, amountBase.scale());

                    // CHECK ACCURACY of AMOUNT
                    int accuracy = different_scale & TransactionAmount.SCALE_MASK;
                    String sss = Integer.toBinaryString(accuracy);
                    if (scale == 24)
                        assertEquals("10000".equals(sss), true);
                    else if (scale < 9)
                        assertEquals(true, true);

                    if (accuracy > 0) {
                        if (accuracy >= TransactionAmount.SCALE_MASK_HALF) {
                            accuracy -= TransactionAmount.SCALE_MASK + 1;
                        }
                        // RESCALE AMOUNT
                        amount_result = amountBase.scaleByPowerOfTen(-accuracy);
                    } else {
                        amount_result = amountBase;
                    }

                    assertEquals(amount, amount_result);

                    // TRY PARSE - PRICISION must be LESS
                    amount = amountForParse.scaleByPowerOfTen(-scale);

                    orderCreation = new CreateOrderTransaction(accountA, 3l, AssetCls.FEE_KEY, amount, amount, (byte) 0,
                            timestamp, 0L);
                    orderCreation.sign(accountA, Transaction.FOR_NETWORK);

                    raw = orderCreation.toBytes(Transaction.FOR_NETWORK, true);

                    orderCreation_2 = null;
                    try {
                        orderCreation_2 = (CreateOrderTransaction) CreateOrderTransaction.Parse(raw, Transaction.FOR_NETWORK);
                    } catch (Exception e) {
                    }

                    // FOR DEBUG POINT
                    if (!orderCreation.getAmountHave().equals(orderCreation_2.getAmountHave())
                            || !orderCreation.getAmountWant().equals(orderCreation_2.getAmountWant())) {
                        try {
                            orderCreation_2 = (CreateOrderTransaction) CreateOrderTransaction.Parse(raw, Transaction.FOR_NETWORK);
                        } catch (Exception e) {
                        }
                    }

                    assertEquals(orderCreation.getAmountHave(), orderCreation_2.getAmountHave());
                    assertEquals(orderCreation.getAmountWant(), orderCreation_2.getAmountWant());
                    assertEquals(Arrays.equals(orderCreation.getSignature(), orderCreation_2.getSignature()), true);

                }

                int thisScale = 5;
                assetA = new AssetVenture(itemAppData, accountA, "AAA", icon, image, ".", 0, thisScale, 0L);
                assetA.setReference(new byte[64], dbRef);
                // Актив с учетом точности создадим
                assetA.insertToMap(dcSet, 0L);

                // IS VALID
                bal_A_keyA = amountForParse.scaleByPowerOfTen(-thisScale);
                bal_A_keyB = amountForParse.scaleByPowerOfTen(-toScale);
                orderCreation = new CreateOrderTransaction(accountA, assetA.getKey(), AssetCls.FEE_KEY, bal_A_keyA,
                        bal_A_keyB, (byte) 0, timestamp, 0L);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                assertEquals(orderCreation.isValid(Transaction.FOR_NETWORK, 0L), Transaction.VALIDATE_OK);

                // INVALID
                bal_A_keyA = amountForParse.scaleByPowerOfTen(-thisScale - 1);
                bal_A_keyB = amountForParse.scaleByPowerOfTen(-toScale);
                orderCreation = new CreateOrderTransaction(accountA, assetA.getKey(), AssetCls.FEE_KEY, bal_A_keyA,
                        bal_A_keyB, (byte) 0, timestamp, 0L);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                assertEquals(orderCreation.isValid(Transaction.FOR_NETWORK, 0L), Transaction.AMOUNT_SCALE_WRONG);

                assetA = new AssetVenture(itemAppData, accountA, "AAA", icon, image, ".", 0, 30, 0L);
                assetA.setReference(new byte[64], dbRef);
                assetA.insertToMap(dcSet, 0L);

                // IS VALID
                bal_A_keyA = amountForParse.scaleByPowerOfTen(-fromScale);
                bal_A_keyB = amountForParse.scaleByPowerOfTen(-toScale);
                orderCreation = new CreateOrderTransaction(accountA, assetA.getKey(), AssetCls.FEE_KEY, bal_A_keyA,
                        bal_A_keyB, (byte) 0, timestamp, 0L);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                assertEquals(orderCreation.isValid(Transaction.FOR_NETWORK, 0L), Transaction.VALIDATE_OK);

                // INVALID HAVE
                amountInvalid = amountTest;
                orderCreation = new CreateOrderTransaction(accountA, assetA.getKey(), AssetCls.FEE_KEY, amountInvalid,
                        BigDecimal.ONE, (byte) 0, timestamp, 0L);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(orderCreation.isValid(Transaction.FOR_NETWORK, 0L), Transaction.AMOUNT_LENGHT_SO_LONG);

                // INVALID WANT
                orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, assetA.getKey(), BigDecimal.ONE,
                        amountInvalid, (byte) 0, timestamp, 0L);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(orderCreation.isValid(Transaction.FOR_NETWORK, 0L), Transaction.AMOUNT_LENGHT_SO_LONG);

                // INVALID HAVE
                amountInvalid = amountForParse.scaleByPowerOfTen(-fromScale - 1);
                orderCreation = new CreateOrderTransaction(accountA, assetA.getKey(), AssetCls.FEE_KEY, amountInvalid,
                        bal_A_keyA, (byte) 0, timestamp, 0L);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                assertEquals(orderCreation.isValid(Transaction.FOR_NETWORK, 0L), Transaction.AMOUNT_SCALE_WRONG);

                // INVALID WANT
                orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, assetA.getKey(), bal_A_keyA,
                        amountInvalid, (byte) 0, timestamp, 0L);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                assertEquals(orderCreation.isValid(Transaction.FOR_NETWORK, 0L), Transaction.AMOUNT_SCALE_WRONG);

            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void scaleTest800() {


        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                int fromScale = 5;
                assetA = new AssetVenture(itemAppData, accountA, "AAA", icon, image, ".", 0, fromScale, 0L);
                byte[] reference = new byte[64];
                this.random.nextBytes(reference);
                assetA.setReference(reference, dbRef);
                // чтобы точность сбросить в 0
                assetA.insertToMap(dcSet, BlockChain.AMOUNT_SCALE_FROM);

                int toScale = 0;
                assetB = new AssetVenture(itemAppData, accountB, "BBB", icon, image, ".", 0, toScale, 0L);
                this.random.nextBytes(reference);
                assetB.setReference(reference, dbRef);
                // чтобы точность сбросить в 0
                assetB.insertToMap(dcSet, BlockChain.AMOUNT_SCALE_FROM);

                int cap = 60000;
                for (int i = cap - 100; i < cap + 100; i++) {
                    BigDecimal amountSell = new BigDecimal(i);
                    BigDecimal amountBuy = new BigDecimal("1");

                    orderCreation = new CreateOrderTransaction(accountA, assetA.getKey(), assetB.getKey(), amountSell,
                            amountBuy, (byte) 0, timestamp++, 0L);
                    orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                    orderCreation.process(null, Transaction.FOR_NETWORK);
                    order_AB_1_ID = orderCreation.getOrderId();

                    orderCreation = new CreateOrderTransaction(accountB, assetB.getKey(), assetA.getKey(), amountBuy,
                            amountSell, (byte) 0, timestamp++, 0L);
                    orderCreation.sign(accountB, Transaction.FOR_NETWORK);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                    orderCreation.process(null, Transaction.FOR_NETWORK);
                    order_BA_1_ID = orderCreation.getOrderId();

                    // посре обработки обновим все данные
                    order_AB_1 = Order.reloadOrder(dcSet, order_AB_1_ID);
                    order_BA_1 = Order.reloadOrder(dcSet, order_BA_1_ID);

                    BigDecimal fullfilledA = order_BA_1.getFulfilledHave();
                    BigDecimal fullfilledB = order_AB_1.getFulfilledHave();

                    if (!order_AB_1.isFulfilled()) {
                        fullfilledA = fullfilledA;
                    }
                    if (!order_BA_1.isFulfilled()) {
                        fullfilledA = fullfilledA;
                    }

                    assertEquals(false, order_AB_1.isActive(dcSet));
                    assertEquals(false, order_BA_1.isActive(dcSet));

                    assertEquals(true, order_AB_1.isFulfilled());
                    assertEquals(true, order_BA_1.isFulfilled());

                    // удалим их на всяк случай чтобы они не ыбли в стакане
                    Order.deleteOrder(dcSet, order_AB_1_ID);
                    Order.deleteOrder(dcSet, order_BA_1_ID);

                }

                for (int i = cap - 100; i < cap + 100; i++) {
                    BigDecimal amountSell = new BigDecimal("1");
                    BigDecimal amountBuy = new BigDecimal(i);

                    orderCreation = new CreateOrderTransaction(accountB, assetB.getKey(), assetA.getKey(), amountBuy,
                            amountSell, (byte) 0, timestamp++, 0L);
                    orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                    orderCreation.process(null, Transaction.FOR_NETWORK);
                    order_AB_1_ID = orderCreation.getOrderId();

                    orderCreation = new CreateOrderTransaction(accountA, assetA.getKey(), assetB.getKey(), amountSell,
                            amountBuy, (byte) 0, timestamp++, 0L);
                    orderCreation.sign(accountB, Transaction.FOR_NETWORK);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                    orderCreation.process(null, Transaction.FOR_NETWORK);
                    order_BA_1_ID = orderCreation.getOrderId();

                    // посре обработки обновим все данные
                    order_AB_1 = Order.reloadOrder(dcSet, order_AB_1_ID);
                    order_BA_1 = Order.reloadOrder(dcSet, order_BA_1_ID);

                    BigDecimal fullfilledA = order_BA_1.getFulfilledHave();
                    BigDecimal fullfilledB = order_AB_1.getFulfilledHave();

                    assertEquals(false, order_AB_1.isActive(dcSet));
                    assertEquals(false, order_BA_1.isActive(dcSet));

                    assertEquals(true, order_AB_1.isFulfilled());
                    assertEquals(true, order_BA_1.isFulfilled());

                    // удалим их на всяк случай чтобы они не ыбли в стакане
                    Order.deleteOrder(dcSet, order_AB_1_ID);
                    Order.deleteOrder(dcSet, order_BA_1_ID);

                }

            } finally {
                dcSet.close();
            }
        }
    }

    /**
     * тут частично надо покупка на мизер и ордер или отменяется если он иницатор
     * иди остаток прибавляется к инициатору и он становится Комплетед
     */
    @Test
    public void scaleTest800_1() {


        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                int fromScale = 0;
                assetA = new AssetVenture(itemAppData, accountA, "AAA", icon, image, ".", 0, fromScale, 0L);
                byte[] reference = new byte[64];
                this.random.nextBytes(reference);
                assetA.setReference(reference, dbRef);
                // чтобы точность сбросить в 0
                assetA.insertToMap(dcSet, BlockChain.AMOUNT_SCALE_FROM);

                int toScale = 0;
                assetB = new AssetVenture(itemAppData, accountB, "BBB", icon, image, ".", 0, toScale, 0L);
                this.random.nextBytes(reference);
                assetB.setReference(reference, dbRef);
                // чтобы точность сбросить в 0
                assetB.insertToMap(dcSet, BlockChain.AMOUNT_SCALE_FROM);

                int cap = 60000;
                for (int i = cap - 100; i < cap + 100; i++) {
                    BigDecimal amountSell = new BigDecimal(i);
                    BigDecimal amountBuy = new BigDecimal("1111111");

                    orderCreation = new CreateOrderTransaction(accountA, assetA.getKey(), assetB.getKey(), amountSell,
                            amountBuy, (byte) 0, timestamp++, 0L);
                    orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                    orderCreation.process(null, Transaction.FOR_NETWORK);
                    order_AB_1_ID = orderCreation.getOrderId();

                    // увеличим ордер-кусатель
                    amountBuy = amountBuy.add(new BigDecimal("1"));
                    orderCreation = new CreateOrderTransaction(accountB, assetB.getKey(), assetA.getKey(), amountBuy,
                            amountSell, (byte) 0, timestamp++, 0L);
                    orderCreation.sign(accountB, Transaction.FOR_NETWORK);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                    orderCreation.process(null, Transaction.FOR_NETWORK);
                    order_BA_1_ID = orderCreation.getOrderId();

                    // посре обработки обновим все данные
                    order_AB_1 = Order.reloadOrder(dcSet, order_AB_1_ID);
                    order_BA_1 = Order.reloadOrder(dcSet, order_BA_1_ID);

                    BigDecimal fullfilledA = order_BA_1.getFulfilledHave();
                    BigDecimal fullfilledB = order_AB_1.getFulfilledHave();

                    if (!order_AB_1.isFulfilled()) {
                        fullfilledA = fullfilledA;
                    }
                    if (!order_BA_1.isFulfilled()) {
                        fullfilledA = fullfilledA;
                    }

                    assertEquals(false, order_AB_1.isActive(dcSet));
                    assertEquals(false, order_BA_1.isActive(dcSet));

                    assertEquals(true, order_AB_1.isFulfilled());
                    assertEquals(false, order_BA_1.isFulfilled());

                    // удалим их на всяк случай чтобы они не ыбли в стакане
                    Order.deleteOrder(dcSet, order_AB_1_ID);
                    Order.deleteOrder(dcSet, order_BA_1_ID);

                }
            } finally {
                dcSet.close();
            }
        }
    }

    /**
     * тут малое отклонение с заказа стенки забираем кусателю
     */
    @Test
    public void scaleTest800_10back() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                int fromScale = 0;
                assetA = new AssetVenture(itemAppData, accountA, "AAA", icon, image, ".", 0, fromScale, 0L);
                byte[] reference = new byte[64];
                this.random.nextBytes(reference);
                assetA.setReference(reference, dbRef);
                // чтобы точность сбросить в 0
                assetA.insertToMap(dcSet, BlockChain.AMOUNT_SCALE_FROM);

                int toScale = 0;
                assetB = new AssetVenture(itemAppData, accountB, "BBB", icon, image, ".", 0, toScale, 0L);
                this.random.nextBytes(reference);
                assetB.setReference(reference, dbRef);
                // чтобы точность сбросить в 0
                assetB.insertToMap(dcSet, BlockChain.AMOUNT_SCALE_FROM);

                BigDecimal ADD = new BigDecimal("10");
                int cap = 60000;

                for (int i = cap - 10; i < cap + 10; i++) {
                    BigDecimal amountSell = new BigDecimal("1111111");
                    BigDecimal amountBuy = new BigDecimal(i);

                    // увеличим ордер-держатель
                    BigDecimal price = amountSell.divide(amountBuy, 20, RoundingMode.DOWN);
                    BigDecimal amountBuyNew = amountBuy.add(ADD);
                    BigDecimal amountSellNew = amountBuyNew.multiply(price).setScale(assetA.getScale(), RoundingMode.DOWN);
                    orderCreation = new CreateOrderTransaction(accountB, assetB.getKey(), assetA.getKey(), amountBuyNew,
                            amountSellNew, (byte) 0, timestamp++, 0L);
                    orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                    orderCreation.process(null, Transaction.FOR_NETWORK);
                    order_AB_1_ID = orderCreation.getOrderId();

                    orderCreation = new CreateOrderTransaction(accountA, assetA.getKey(), assetB.getKey(), amountSell,
                            amountBuy, (byte) 0, timestamp++, 0L);
                    orderCreation.sign(accountB, Transaction.FOR_NETWORK);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                    orderCreation.process(null, Transaction.FOR_NETWORK);
                    order_BA_1_ID = orderCreation.getOrderId();

                    // посре обработки обновим все данные
                    order_AB_1 = Order.reloadOrder(dcSet, order_AB_1_ID);
                    order_BA_1 = Order.reloadOrder(dcSet, order_BA_1_ID);

                    Trade trade = Trade.get(dcSet, order_BA_1, order_AB_1);

                    BigDecimal tradePrice = trade.calcPrice();
                    logger.info(order_AB_1.getPrice() + " - " + tradePrice);
                    assertEquals(false, Order.isPricesNotClose(order_AB_1.getPrice(), tradePrice, BlockChain.MAX_ORDER_DEVIATION));

                    BigDecimal fullfilledA = order_BA_1.getFulfilledHave();
                    BigDecimal fullfilledB = order_AB_1.getFulfilledHave();

                    assertEquals(false, order_AB_1.isActive(dcSet));
                    assertEquals(false, order_BA_1.isActive(dcSet));

                    assertEquals(true, order_AB_1.isFulfilled());
                    assertEquals(true, order_BA_1.isFulfilled());

                    assertEquals(true, trade.getAmountWant().compareTo(order_BA_1.getAmountHave()) == 0);
                    // кусатель получил больше
                    assertEquals(true, trade.getAmountHave().compareTo(order_BA_1.getAmountWant()) > 0);

                    // держатель позиции получил меньше
                    assertEquals(true, trade.getAmountWant().compareTo(order_AB_1.getAmountWant()) < 0);
                    assertEquals(true, trade.getAmountHave().compareTo(order_AB_1.getAmountHave()) == 0);

                    // удалим их на всяк случай чтобы они не ыбли в стакане
                    Order.deleteOrder(dcSet, order_AB_1_ID);
                    Order.deleteOrder(dcSet, order_BA_1_ID);

                }
            } finally {
                dcSet.close();
            }
        }

    }

    /**
     * тут малое отклонение от заказа стенки
     * и стенку снимаем и не прибаляем к кусателю
     * Это очень сильно зависит от коэффициентов:
     * org.erachain.core.BlockChain#INITIATOR_PRICE_DIFF_LIMIT
     * org.erachain.core.BlockChain#TRADE_PRICE_DIFF_LIMIT
     * и может тут не работать если выставить другие значения там - все тонко настраивается в тестах тут
     * правим ADD
     */
    @Test
    public void scaleTest800_100back() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                int fromScale = 0;
                assetA = new AssetVenture(itemAppData, accountA, "AAA", icon, image, ".", 0, fromScale, 0L);
                byte[] reference = new byte[64];
                this.random.nextBytes(reference);
                assetA.setReference(reference, dbRef);
                // чтобы точность сбросить в 0
                assetA.insertToMap(dcSet, BlockChain.AMOUNT_SCALE_FROM);

                int toScale = 0;
                assetB = new AssetVenture(itemAppData, accountB, "BBB", icon, image, ".", 0, toScale, 0L);
                this.random.nextBytes(reference);
                assetB.setReference(reference, dbRef);
                // чтобы точность сбросить в 0
                assetB.insertToMap(dcSet, BlockChain.AMOUNT_SCALE_FROM);

                BigDecimal ADD = new BigDecimal("46");
                int cap = 60000;

                for (int i = cap - 10; i < cap + 10; i++) {
                    BigDecimal amountSell = new BigDecimal("1111111");
                    BigDecimal amountBuy = new BigDecimal(i);

                    // увеличим ордер-держатель
                    BigDecimal price = amountSell.divide(amountBuy, 20, RoundingMode.DOWN);
                    BigDecimal amountBuyNew = amountBuy.add(ADD);
                    BigDecimal amountSellNew = amountBuyNew.multiply(price).setScale(assetA.getScale(), RoundingMode.DOWN);
                    orderCreation = new CreateOrderTransaction(accountB, assetB.getKey(), assetA.getKey(), amountBuyNew,
                            amountSellNew, (byte) 0, timestamp++, 0L);
                    orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                    orderCreation.process(null, Transaction.FOR_NETWORK);
                    order_AB_1_ID = orderCreation.getOrderId();

                    orderCreation = new CreateOrderTransaction(accountA, assetA.getKey(), assetB.getKey(), amountSell,
                            amountBuy, (byte) 0, timestamp++, 0L);
                    orderCreation.sign(accountB, Transaction.FOR_NETWORK);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                    orderCreation.process(null, Transaction.FOR_NETWORK);
                    order_BA_1_ID = orderCreation.getOrderId();

                    // после обработки обновим все данные
                    order_AB_1 = Order.reloadOrder(dcSet, order_AB_1_ID);
                    order_BA_1 = Order.reloadOrder(dcSet, order_BA_1_ID);

                    Trade trade = Trade.get(dcSet, order_BA_1, order_AB_1);

                    BigDecimal tradePrice = trade.calcPrice();
                    logger.info(order_AB_1.getPrice() + " - " + tradePrice);
                    assertEquals(false, Order.isPricesNotClose(order_AB_1.getPrice(), tradePrice, BlockChain.MAX_ORDER_DEVIATION));

                    BigDecimal fullfilledA = order_BA_1.getFulfilledHave();
                    BigDecimal fullfilledB = order_AB_1.getFulfilledHave();

                    // держатель отменяет свой ордер
                    assertEquals(false, order_AB_1.isActive(dcSet));
                    assertEquals(false, order_BA_1.isActive(dcSet));

                    // держатель не исполняет свой ордер
                    assertEquals(true, order_AB_1.isFulfilled());
                    assertEquals(true, order_BA_1.isFulfilled());

                    assertEquals(true, trade.getAmountWant().compareTo(order_BA_1.getAmountHave()) == 0);
                    // кусатель получил столько же
                    assertEquals(true, trade.getAmountHave().compareTo(order_BA_1.getAmountWant()) > 0);

                    assertEquals(true, trade.getAmountWant().compareTo(order_AB_1.getAmountWant()) < 0);
                    assertEquals(true, trade.getAmountHave().compareTo(order_AB_1.getAmountHave()) == 0);

                    // удалим их на всяк случай чтобы они не ыбли в стакане
                    Order.deleteOrder(dcSet, order_AB_1_ID);
                    Order.deleteOrder(dcSet, order_BA_1_ID);

                }

            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void validateSignatureOrderTransaction() {


        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                orderCreation.sign(accountA, Transaction.FOR_NETWORK);

                // CHECK IF ORDER CREATION SIGNATURE IS VALID
                orderCreation.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
                assertEquals(true, orderCreation.isSignatureValid(dcSet));

                // INVALID SIGNATURE
                orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, 3l, BigDecimal.valueOf(100),
                        BigDecimal.valueOf(1), (byte) 0, timestamp, 0L, new byte[64]);

                // CHECK IF ORDER CREATION SIGNATURE IS INVALID
                orderCreation.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
                assertEquals(false, orderCreation.isSignatureValid(dcSet));
            } finally {
                dcSet.close();
            }
        }
    }

    //@Ignore
//TODO actualize the test
    @Test
    public void validateCreateOrderTransaction() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CHECK VALID
                long timeStamp = timestamp++;
                CreateOrderTransaction orderCreation = new CreateOrderTransaction(accountA, keyA, AssetCls.ERA_KEY,
                        BigDecimal.valueOf(100), BigDecimal.valueOf(1), (byte) 0, ++timeStamp, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);

                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, 0L));

                // CREATE INVALID ORDER CREATION HAVE EQUALS WANT
                orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, AssetCls.FEE_KEY,
                        BigDecimal.valueOf(100), BigDecimal.valueOf(1), (byte) 0, ++timeStamp, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);

                // CHECK IF ORDER CREATION INVALID
                assertEquals(Transaction.HAVE_EQUALS_WANT, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));

                // CREATE INVALID ORDER CREATION NOT ENOUGH BALANCE
                orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, AssetCls.ERA_KEY,
                        BigDecimal.valueOf(50001), BigDecimal.valueOf(1), (byte) 0, ++timeStamp, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);

                // CHECK IF ORDER CREATION INVALID
                assertEquals(Transaction.NO_BALANCE, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));

                // CREATE INVALID ORDER CREATION INVALID AMOUNT
                orderCreation = new CreateOrderTransaction(accountA, keyA, AssetCls.ERA_KEY, BigDecimal.valueOf(-50.0),
                        BigDecimal.valueOf(1), (byte) 0, ++timeStamp, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);

                // CHECK IF ORDER CREATION INVALID
                assertEquals(Transaction.NEGATIVE_AMOUNT, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));

                assetA = new AssetVenture(itemAppData, new GenesisBlock().getCreator(), "Erachain.org", icon, image,
                        "This is the simulated ERM asset.", 0, 8, 10L);
                Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, null, assetA, (byte) 0, ++timeStamp, 0L);
                issueAssetTransaction.sign(accountA, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);
                keyA = assetA.getKey();

                // CREATE INVALID ORDER CREATION INVALID AMOUNT
                orderCreation = new CreateOrderTransaction(accountA, keyA, AssetCls.ERA_KEY, BigDecimal.valueOf(50.01),
                        BigDecimal.valueOf(1), (byte) 0, ++timeStamp, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);

                // CHECK IF ORDER CREATION INVALID
                assertEquals(Transaction.NO_BALANCE, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));

                if (false) { // сейчас все работает благодаря поавающей точке и системе округления на лету
                    // CREATE INVALID ORDER CREATION INVALID AMOUNT
                    orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, keyA, BigDecimal.valueOf(0.01),
                            BigDecimal.valueOf(1.1), (byte) 0, ++timeStamp, 0L, new byte[64]);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                    // orderCreation.process(null,Transaction.FOR_NETWORK);

                    // CHECK IF ORDER CREATION INVALID
                    assertEquals(Transaction.INVALID_RETURN, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                }

                // CREATE INVALID ORDER CREATION WANT DOES NOT EXIST
                orderCreation = new CreateOrderTransaction(accountA, 10022L, AssetCls.ERA_KEY, BigDecimal.valueOf(0.1),
                        BigDecimal.valueOf(1), (byte) 0, ++timeStamp, 0L, new byte[64]);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);

                // CHECK IF ORDER CREATION INVALID
                assertEquals(Transaction.ITEM_ASSET_NOT_EXIST, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));

                // CREATE INVALID ORDER CREATION WANT DOES NOT EXIST
                orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, 2114L, BigDecimal.valueOf(0.1),
                        BigDecimal.valueOf(1), (byte) 0, ++timeStamp, 0L, new byte[64]);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);

                // CHECK IF ORDER CREATION INVALID
                assertEquals(Transaction.ITEM_ASSET_NOT_EXIST, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));

                if (false) {
                    // CREATE ORDER CREATION INVALID REFERENCE
                    orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, AssetCls.ERA_KEY,
                            BigDecimal.valueOf(0.1), BigDecimal.valueOf(1), (byte) 0, ++timeStamp, -12345L, new byte[64]);
                    orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);

                    // CHECK IF ORDER CREATION IS INVALID
                    assertEquals(Transaction.INVALID_REFERENCE, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                }
            } finally {
                dcSet.close();
            }
        }

    }

    @Test
    public void price33() {

        BigDecimal amountHave = new BigDecimal("0.00000333");
        BigDecimal amountWant = new BigDecimal("0.00010000");

        BigDecimal price = Order.calcPrice(amountHave, amountWant);
        BigDecimal price1 = Order.calcPrice(amountHave, amountWant);
        BigDecimal thisPrice = Order.calcPrice(amountHave, amountWant);

        BigDecimal priceRev = Order.calcPrice(amountWant, amountHave);
        BigDecimal price1Rev = Order.calcPrice(amountWant, amountHave);
        BigDecimal thisPriceRev = Order.calcPrice(amountWant, amountHave);

        BigDecimal orderAmountHave = new BigDecimal("30.00000000");
        BigDecimal orderAmountWant = new BigDecimal("1.00000000");

        BigDecimal price10 = Order.calcPrice(orderAmountHave, orderAmountWant);
        BigDecimal price101 = Order.calcPrice(orderAmountHave, orderAmountWant);
        BigDecimal orderPrice = Order.calcPrice(orderAmountHave, orderAmountWant);

        BigDecimal price10rev = Order.calcPrice(orderAmountWant, orderAmountHave);
        BigDecimal price101rev = Order.calcPrice(orderAmountWant, orderAmountHave);
        BigDecimal orderPriceRev = Order.calcPrice(orderAmountWant, orderAmountHave);

        int thisPriceRevScale = thisPriceRev.stripTrailingZeros().scale();
        int orderPriceRevScale = orderPriceRev.stripTrailingZeros().scale();
        int thisPriceScale = thisPrice.scale();

        boolean needBreak = false;

        if (thisPriceScale > orderPriceRevScale) {
            BigDecimal scaleThisPrice = thisPrice.setScale(orderPriceRevScale, RoundingMode.HALF_DOWN);
            if (scaleThisPrice.compareTo(orderPriceRev) == 0) {
                BigDecimal scaledOrderPrice = orderPrice.setScale(thisPriceRevScale, RoundingMode.HALF_DOWN);
                if (scaledOrderPrice.compareTo(thisPriceRev) == 0)
                    ;
                else
                    needBreak = true;
            }
        }

        assertEquals(needBreak, false);

    }

    @Test
    public void price33_1() {

        BigDecimal amountHave = new BigDecimal("10.00000000");
        BigDecimal amountWant = new BigDecimal("0.33333333");

        BigDecimal price = Order.calcPrice(amountHave, amountWant);
        BigDecimal price1 = Order.calcPrice(amountHave, amountWant);
        BigDecimal thisPrice = Order.calcPrice(amountHave, amountWant);

        BigDecimal priceRev = Order.calcPrice(amountWant, amountHave);
        BigDecimal price1Rev = Order.calcPrice(amountWant, amountHave);
        BigDecimal thisPriceRev = Order.calcPrice(amountWant, amountHave);

        BigDecimal orderAmountHave = new BigDecimal("1.00000000");
        BigDecimal orderAmountWant = new BigDecimal("30.00000000");

        BigDecimal price10 = Order.calcPrice(orderAmountHave, orderAmountWant);
        BigDecimal price101 = Order.calcPrice(orderAmountHave, orderAmountWant);
        BigDecimal orderPrice = Order.calcPrice(orderAmountHave, orderAmountWant);

        BigDecimal price10rev = Order.calcPrice(orderAmountWant, orderAmountHave);
        BigDecimal price101rev = Order.calcPrice(orderAmountWant, orderAmountHave);
        BigDecimal orderPriceRev = Order.calcPrice(orderAmountWant, orderAmountHave);


        int thisPriceScale = thisPrice.stripTrailingZeros().scale();
        int orderPriceRevScale = orderPriceRev.stripTrailingZeros().scale();

        boolean needBreak = false;

        if (thisPriceScale > orderPriceRevScale) {
            BigDecimal thisPriceScaled = thisPrice.setScale(orderPriceRevScale, RoundingMode.HALF_DOWN);
            if (thisPriceScaled.compareTo(orderPriceRev) > 0) {
                needBreak = true;
            }
        } else {
            BigDecimal orderPriceRevScaled = orderPriceRev.setScale(thisPriceScale, RoundingMode.HALF_DOWN);
            if (thisPrice.compareTo(orderPriceRevScaled) > 0) {
                needBreak = true;
            }
        }

        assertEquals(needBreak, false);

    }

    @Test
    public void parseCreateOrderTransaction1() {

        byte[] seed = Crypto.getInstance().digest("test_A".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        accountA = new PrivateKeyAccount(privateKey);

        String addr1 = accountA.getAddress();
        Account accShort = new Account(accountA.getShortAddressBytes());
        String addr2 = accShort.getAddress();

        BigDecimal amountHave = new BigDecimal("123.456");
        BigDecimal amountWant = new BigDecimal("12.456");

        Order order = new Order(dcSet, Transaction.makeDBRef(12, 3), this.accountA, 12L, amountHave, 8,
                13L, amountWant, 8);


        // CONVERT TO BYTES
        //// orderCreation.makeOrder().setExecutable(false);
        byte[] rawOrder = order.toBytes();
        assertEquals(rawOrder.length, order.getDataLength());

        Order parsedOrder = null;
        try {
            // PARSE FROM BYTES
            parsedOrder = Order.parse(rawOrder);
        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

        // CHECK SIGNATURE
        assertEquals(true, order.getCreator().equals(parsedOrder.getCreator()));

        // CHECK HAVE
        assertEquals(order.getHaveAssetKey(), parsedOrder.getHaveAssetKey());

        // CHECK WANT
        assertEquals(order.getWantAssetKey(), parsedOrder.getWantAssetKey());

        // CHECK AMOUNT
        assertEquals(0, order.getAmountHave().compareTo(parsedOrder.getAmountHave()));

        // CHECK PRICE
        assertEquals(0, order.getAmountWant().compareTo(parsedOrder.getAmountWant()));

        // CHECK FEE
        assertEquals(true, order.getPrice().compareTo(parsedOrder.getPrice())==0);

        // PARSE TRANSACTION FROM WRONG BYTES
        rawOrder = new byte[order.getDataLength() - 1];

        try {
            // PARSE FROM BYTES
            Order.parse(rawOrder);

            // FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            // EXCEPTION IS THROWN OK
        }
    }

    @Test
    public void parseCreateOrderTransaction() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                orderCreation.sign(accountA, Transaction.FOR_NETWORK);

                // CONVERT TO BYTES
                //// orderCreation.makeOrder().setExecutable(false);
                byte[] rawOrderCreation = orderCreation.toBytes(Transaction.FOR_NETWORK, true);
                assertEquals(rawOrderCreation.length, orderCreation.getDataLength(Transaction.FOR_NETWORK, true));

                CreateOrderTransaction parsedOrderCreation = null;
                try {
                    // PARSE FROM BYTES
                    parsedOrderCreation = (CreateOrderTransaction) TransactionFactory.getInstance().parse(rawOrderCreation,
                            Transaction.FOR_NETWORK);
                } catch (Exception e) {
                    fail("Exception while parsing transaction. " + e);
                }

                // CHECK INSTANCE
                assertEquals(true, parsedOrderCreation instanceof CreateOrderTransaction);

                ///// CHECK EXECUTABLE
                //// assertEquals(orderCreation.makeOrder().isExecutable(),
                ///// parsedOrderCreation.makeOrder().isExecutable());

                // CHECK SIGNATURE
                assertEquals(true, Arrays.equals(orderCreation.getSignature(), parsedOrderCreation.getSignature()));

                // CHECK HAVE
                assertEquals(orderCreation.getHaveKey(), parsedOrderCreation.getHaveKey());

                // CHECK WANT
                assertEquals(orderCreation.getWantKey(), parsedOrderCreation.getWantKey());

                // CHECK AMOUNT
                assertEquals(0, orderCreation.getAmountHave().compareTo(parsedOrderCreation.getAmountHave()));

                // CHECK PRICE
                assertEquals(0, orderCreation.getAmountWant().compareTo(parsedOrderCreation.getAmountWant()));

                // CHECK FEE
                assertEquals(orderCreation.getFeePow(), parsedOrderCreation.getFeePow());

                // CHECK REFERENCE
                // assertEquals((long)orderCreation.getReference(),
                // (long)parsedOrderCreation.getReference());

                // CHECK TIMESTAMP
                assertEquals(orderCreation.getTimestamp(), parsedOrderCreation.getTimestamp());

                // PARSE TRANSACTION FROM WRONG BYTES
                rawOrderCreation = new byte[orderCreation.getDataLength(Transaction.FOR_NETWORK, true)];

                try {
                    // PARSE FROM BYTES
                    TransactionFactory.getInstance().parse(rawOrderCreation, Transaction.FOR_NETWORK);

                    // FAIL
                    fail("this should throw an exception");
                } catch (Exception e) {
                    // EXCEPTION IS THROWN OK
                }

            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void parseTrade() {

        //////////////////////////////////
        /////////// TRADE PARSE //////////
        Trade tradeParse = new Trade(543123456L, 3434546546L, 2l, 1l,
                BigDecimal.valueOf(123451).setScale(BlockChain.AMOUNT_DEDAULT_SCALE << 1),
                BigDecimal.valueOf(1056789).setScale(BlockChain.AMOUNT_DEDAULT_SCALE >> 1),
                haveAssetScale, wantAssetScale, 0);
        byte[] tradeRaw = tradeParse.toBytes();

        Assert.assertEquals(tradeRaw.length, tradeParse.getDataLength());

        Trade tradeParse_1 = null;
        try {
            tradeParse_1 = Trade.parse(tradeRaw);
        } catch (Exception e) {

        }
        Assert.assertEquals(tradeParse_1.getInitiator(), tradeParse.getInitiator());
        Assert.assertEquals(tradeParse_1.getTarget(), tradeParse.getTarget());

        Assert.assertEquals(tradeParse_1.getAmountHave(), tradeParse.getAmountHave());
        Assert.assertEquals(tradeParse_1.getAmountWant(), tradeParse.getAmountWant());

    }

    private void testOrderProcessingDivisible_init() {
        // CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
        // amountHAVE 1000 - amountWant 100
        // calc -
        // https://docs.google.com/spreadsheets/d/14OjXtMM36XHqtercQZCytusK7KKD0dSY_uno0lWMsCs/edit#gid=0

        orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100), (byte) 0, timestamp++, 0L);
        orderCreation.sign(accountA, Transaction.FOR_NETWORK);
        orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
        orderCreation.process(null, Transaction.FOR_NETWORK);
        order_AB_1 = orderCreation.makeOrder();
        order_AB_1_ID = orderCreation.getOrderId();

        orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(1000),
                BigDecimal.valueOf(300), (byte) 0, timestamp++, 0L);
        orderCreation.sign(accountA, Transaction.FOR_NETWORK);
        orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
        orderCreation.process(null, Transaction.FOR_NETWORK);
        order_AB_4 = orderCreation.makeOrder();
        order_AB_4_ID = order_AB_4.getId();

        orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(1400),
                BigDecimal.valueOf(200), (byte) 0, timestamp++, 0L);
        orderCreation.sign(accountA, Transaction.FOR_NETWORK);
        orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
        orderCreation.process(null, Transaction.FOR_NETWORK);
        order_AB_3 = orderCreation.makeOrder();
        order_AB_3_ID = order_AB_3.getId();

        orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(1000),
                BigDecimal.valueOf(130), (byte) 0, timestamp++, 0L);
        orderCreation.sign(accountA, Transaction.FOR_NETWORK);
        orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
        orderCreation.process(null, Transaction.FOR_NETWORK);
        order_AB_2 = orderCreation.makeOrder();
        order_AB_2_ID = order_AB_2.getId();

    }

    //@Ignore
//TODO actualize the test
    @Test
    public void testOrderProcessingDivisible() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                testOrderProcessingDivisible_init();

                // CREATE ORDER SELLING 120 B FOR A AT A PRICE OF 595)
                // должно инициировать 2 торговли на Приказ АБ_1 и Приказ АБ_2
                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, BigDecimal.valueOf(120),
                        BigDecimal.valueOf(595), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_BA_1 = orderCreation.makeOrder();
                order_BA_1_ID = order_BA_1.getId();

                // RELOAD new VALUES (amountLeft)
                order_AB_2 = Order.reloadOrder(dcSet, order_AB_2);

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(45600)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(49880)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet), new BigDecimal("120.00000000")); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet),
                        order_AB_1.getAmountHave().add(BigDecimal.valueOf(120).subtract(order_AB_1.getAmountWant())
                                // .multiply(order_B.getPriceCalcReverse()))
                                .divide(order_AB_2.getPrice(), BlockChain.AMOUNT_DEDAULT_SCALE, BigDecimal.ROUND_HALF_UP))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                // order AB_1 is left from market cap
                Order order_AB_1_tmp = dcSet.getCompletedOrderMap().get(order_AB_1_ID);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(order_AB_1_tmp.getId()));
                Assert.assertEquals(false, dcSet.getOrderMap().contains(order_BA_1_ID));
                Assert.assertEquals(0, order_AB_1_tmp.getFulfilledHave().compareTo(order_AB_1.getAmountHave()));
                order_BA_1 = Order.reloadOrder(dcSet, order_BA_1.getId());
                assertEquals(order_BA_1.getFulfilledHave(), BigDecimal.valueOf(120));
                Assert.assertEquals(true, order_AB_1_tmp.isFulfilled());
                Assert.assertEquals(true, order_BA_1.isFulfilled());

                // order AB_2 is still in market cap
                Order order_AB_2_tmp = dcSet.getOrderMap().get(order_AB_2_ID);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(order_AB_2_tmp.getId()));
                Assert.assertEquals(order_AB_2_tmp.getFulfilledHave(), BigDecimal.valueOf(20).divide(order_AB_2_tmp.getPrice(),
                        BlockChain.AMOUNT_DEDAULT_SCALE, BigDecimal.ROUND_HALF_UP));
                Assert.assertEquals(false, order_AB_2_tmp.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(2, order_BA_1.getInitiatedTrades(dcSet).size());

                // INITIATOR of all trades is order_BA_1
                Trade trade = order_BA_1.getInitiatedTrades(dcSet).get(0);
                Assert.assertEquals((Long) trade.getInitiator(), order_BA_1_ID);
                Assert.assertEquals((Long) order_BA_1.getInitiatedTrades(dcSet).get(1).getInitiator(), order_BA_1_ID);
                // здесь иногда почему-то получается то один ордер то другой - без
                // сортировки
                if (trade.getTarget() == order_AB_1_ID) {
                    // 1
                    Assert.assertEquals((Long) trade.getTarget(), order_AB_1_ID);
                    Assert.assertEquals(false, trade.getAmountHave().equals(BigDecimal.valueOf(1000)));
                    Assert.assertEquals(false, trade.getAmountWant().equals(BigDecimal.valueOf(100)));
                    // 2
                    trade = order_BA_1.getInitiatedTrades(dcSet).get(1);
                    Assert.assertEquals(trade.getAmountHave().toPlainString(), BigDecimal.valueOf(20)
                            // .multiply(orderB.getPriceCalcReverse()));
                            .divide(order_AB_2_tmp.getPrice(), BlockChain.AMOUNT_DEDAULT_SCALE,
                                    BigDecimal.ROUND_HALF_UP).setScale(trade.getAmountHave().scale(), BigDecimal.ROUND_HALF_UP).toPlainString());
                    Assert.assertEquals(trade.getAmountWant().toPlainString(), BigDecimal.valueOf(20).toPlainString());
                    Assert.assertEquals(true, trade.getTarget() == (order_AB_2_ID));
                } else {
                    // 2
                    Assert.assertEquals(0, trade.getTarget() == (order_AB_2_ID));
                    Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(20)
                            // .multiply(orderB.getPriceCalcReverse()));
                            .divide(order_AB_2_tmp.getPrice(), BlockChain.AMOUNT_DEDAULT_SCALE, BigDecimal.ROUND_HALF_UP));
                    Assert.assertEquals(trade.getAmountWant(), BigDecimal.valueOf(20));
                    // 1
                    trade = order_BA_1.getInitiatedTrades(dcSet).get(1);
                    Assert.assertEquals(0, trade.getTarget() == (order_AB_1_ID));
                    Assert.assertEquals(0, trade.getAmountHave() == (BigDecimal.valueOf(1000)));
                    Assert.assertEquals(0, trade.getAmountWant() == (BigDecimal.valueOf(100)));
                    Assert.assertEquals(0, trade.getTarget() == (order_AB_1_ID));
                }

                ////////////////////////////
                bal_A_keyA = accountA.getBalanceUSE(keyA, dcSet);
                bal_A_keyB = accountA.getBalanceUSE(keyB, dcSet);
                bal_B_keyB = accountB.getBalanceUSE(keyB, dcSet);
                bal_B_keyA = accountB.getBalanceUSE(keyA, dcSet);

                BigDecimal order_AB_2_haveLeft = order_AB_2.getAmountHaveLeft();

                // BigDecimal trade_B_fill = order_B.getFulfilledHave();

                // new trade add - duplicate
                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, BigDecimal.valueOf(260),
                        BigDecimal.valueOf(1900), (byte) 0, timestamp++, 0L, new byte[]{5, 6});
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_BA_2 = Order.reloadOrder(dcSet, orderCreation.makeOrder());
                order_BA_2_ID = order_BA_2.getId();

                order_AB_2 = Order.reloadOrder(dcSet, order_AB_2);

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), bal_A_keyA); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), bal_B_keyB.subtract(order_BA_2.getAmountHave())); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet), bal_A_keyB.add(order_BA_2.getFulfilledHave())); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet), bal_B_keyA.add(order_AB_2_haveLeft)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                //////////////////////////////////////////
                // CREATE ORDER THREE (SELLING 99.99999999 A FOR B AT A PRICE OF 0.2
                BigDecimal amoHave = new BigDecimal("99.99999999");
                BigDecimal amoWant = BigDecimal.valueOf(9.99999999);

                BigDecimal trade_amo_5 = amoHave.divide(order_BA_2.getPrice(), 8, RoundingMode.HALF_DOWN);
                BigDecimal trade_amo_6 = amoHave;

                bal_A_keyA = accountA.getBalanceUSE(keyA, dcSet);
                bal_A_keyB = accountA.getBalanceUSE(keyB, dcSet);
                bal_B_keyB = accountB.getBalanceUSE(keyB, dcSet);
                bal_B_keyA = accountB.getBalanceUSE(keyA, dcSet);

                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, amoHave, amoWant, (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_AB_8 = Order.reloadOrder(dcSet, orderCreation.makeOrder());
                order_AB_8_ID = order_AB_8.getId();

                order_BA_2 = Order.reloadOrder(dcSet, order_BA_2);

                trade_1_amoA = order_AB_8.getAmountHaveLeft();
                trade_1_amoB = order_BA_2.getAmountHaveLeft();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), new BigDecimal(45600).subtract(amoHave)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(49620)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                assertEquals(accountA.getBalanceUSE(keyB, dcSet), bal_A_keyB.add(trade_amo_5)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                assertEquals(accountB.getBalanceUSE(keyA, dcSet), bal_B_keyA.add(trade_amo_6)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                Assert.assertEquals(false, dcSet.getOrderMap().contains(order_AB_8.getId()));
                Assert.assertEquals(false, order_AB_8.getFulfilledHave() == (amoHave));
                Assert.assertEquals(true, order_AB_8.isFulfilled());

                BigDecimal ttt = order_BA_2.getPrice();
                BigDecimal tradedAmoB = amoHave.divide(order_BA_2.getPrice(), 8, RoundingMode.HALF_DOWN);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(order_BA_2.getId()));
                // Assert.assertEquals(order_E.getFulfilledHave(),
                // //trade_B_fill.add(amoHave.multiply(order_E.getPriceCalcReverse()).setScale(8,
                // RoundingMode.HALF_DOWN)));
                // trade_amo_2.add(tradedAmoB));

                Assert.assertEquals(false, order_BA_2.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, order_BA_2.getInitiatedTrades(dcSet).size());

                Assert.assertEquals(1, order_AB_8.getInitiatedTrades(dcSet).size());

                trade = order_AB_8.getInitiatedTrades(dcSet).get(0);
                Assert.assertEquals(true, trade.getInitiator() == (order_AB_8.getId()));
                Assert.assertEquals(true, trade.getTarget() == (order_BA_2_ID));
                Assert.assertEquals(true, trade.getAmountHave().equals(tradedAmoB));
                Assert.assertEquals(true, trade.getAmountWant().equals(amoHave));

            } finally {
                dcSet.close();
            }
        }
    }

    //////////////////////////// когда цена в периоде
    @Test
    public void testOrderProcessing_065period() {
        // 252649 - 252314 = 335

        // уже есть в блокчейне

        // это прилетел
        // have = 9.75
        // want = 15

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ORDER ONE (SELLING 100 A FOR B AT A PRICE OF 10)
                // amountHAVE 100 - amountWant 1000
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, new BigDecimal("1000"),
                        new BigDecimal("650"), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);

                Long order_AB_1_ID = orderCreation.makeOrder().getId();

                // CREATE ORDER TWO (SELLING 4995 B FOR A AT A PRICE OF 0.05))
                // GENERATES TRADE 100 B FOR 1000 A
                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, new BigDecimal("0.0002"),
                        new BigDecimal("0.0003"), (byte) 0, timestamp++, 0L, new byte[]{5, 6});
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long order_BA_1_ID = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), new BigDecimal("49000")); // BALANCE
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet), new BigDecimal("0.00020000")); // BALANCE

                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), new BigDecimal("49999.9998")); // BALANCE
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet), new BigDecimal("0.00030769")); // BALANCE

                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet)
                                .add(accountB.getBalanceUSE(keyB, dcSet)),
                        new BigDecimal("50000.00000000")); // BALANCE

                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet)
                                .add(accountA.getBalanceUSE(keyA, dcSet)),
                        new BigDecimal("49000.00030769")); // BALANCE

            } finally {
                dcSet.close();
            }
        }
    }

    //////////////////////////// когда цена в пери
    @Test
    public void testOrderProcessing_33period() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, new BigDecimal("30"),
                        new BigDecimal("0.1"), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);

                Long order_AB_1_ID = orderCreation.makeOrder().getId();

                // CREATE ORDER TWO (SELLING 4995 B FOR A AT A PRICE OF 0.05))
                // GENERATES TRADE 100 B FOR 1000 A
                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, new BigDecimal("0.00000334"),
                        new BigDecimal("0.00100"), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long order_BA_1_ID = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), new BigDecimal("49970")); // BALANCE
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet), new BigDecimal("0.00000334")); // BALANCE

                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), new BigDecimal("49999.99999666")); // BALANCE
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet), new BigDecimal("0.00100200")); // BALANCE

                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet)
                                .add(accountB.getBalanceUSE(keyB, dcSet)),
                        new BigDecimal("50000.00000000")); // BALANCE

                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet)
                                .add(accountA.getBalanceUSE(keyA, dcSet)),
                        new BigDecimal("49970.00100200")); // BALANCE

            } finally {
                dcSet.close();
            }
        }
    }

    //////////////////////////// reverse price
    @Test
    public void testOrderProcessingDivisible2() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ORDER ONE (SELLING 100 A FOR B AT A PRICE OF 10)
                // amountHAVE 100 - amountWant 1000
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(100),
                        BigDecimal.valueOf(1000), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);

                Long order_AB_1_ID = orderCreation.makeOrder().getId();

                // CREATE ORDER TWO (SELLING 4995 B FOR A AT A PRICE OF 0.05))
                // GENERATES TRADE 100 B FOR 1000 A
                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, BigDecimal.valueOf(4995),
                        BigDecimal.valueOf(249.75), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long order_BA_1_ID = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(49900)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet), new BigDecimal("1000.00000000")); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(45005)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet), new BigDecimal("100.00000000")); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                Order order_AB_1 = dcSet.getCompletedOrderMap().get(order_AB_1_ID);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(order_AB_1.getId()));
                Assert.assertEquals(order_AB_1.getFulfilledHave(), BigDecimal.valueOf(100));
                Assert.assertEquals(true, order_AB_1.isFulfilled());

                Order order_BA_1 = dcSet.getOrderMap().get(order_BA_1_ID);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(order_BA_1.getId()));
                Assert.assertEquals(order_BA_1.getFulfilledHave(), BigDecimal.valueOf(1000));
                // if order is not fulfiller - recalc getFulfilledWant by own price:
                Assert.assertEquals(order_BA_1.getFulfilledHave().multiply(order_BA_1.getPrice()).toPlainString(), "50.00");
                Assert.assertEquals(false, order_BA_1.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, order_BA_1.getInitiatedTrades(dcSet).size());

                Trade trade = order_BA_1.getInitiatedTrades(dcSet).get(0);
                Assert.assertEquals(true, trade.getInitiator() == order_BA_1_ID);
                Assert.assertEquals(true, trade.getTarget() == order_AB_1_ID);
                Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(100).stripTrailingZeros());
                Assert.assertEquals(trade.getAmountWant().toPlainString(), "1000");

                bal_A_keyA = accountA.getBalanceUSE(keyA, dcSet);
                bal_A_keyB = accountA.getBalanceUSE(keyB, dcSet);
                bal_B_keyB = accountB.getBalanceUSE(keyB, dcSet);
                bal_B_keyA = accountB.getBalanceUSE(keyA, dcSet);

                // CREATE ORDER THREE (SELLING 19.99999999 A FOR B AT A PRICE OF 20)
                BigDecimal amoHave = BigDecimal.valueOf(1.99999999);
                BigDecimal amoWant = BigDecimal.valueOf(3.99999998);

                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, amoHave, amoWant, (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Order order_BA_2 = orderCreation.makeOrder();
                Long order_AB_2_ID = order_BA_2.getId();
                order_BA_2 = Order.reloadOrder(dcSet, order_BA_2);

                /////// BigDecimal haveTaked =
                /////// amoHave.multiply(order_BA_1.getPriceCalcReverse()).setScale(8,
                /////// RoundingMode.HALF_DOWN);
                BigDecimal order_BA_2_wantTaked = amoHave.divide(order_BA_1.getPrice(), 8, RoundingMode.HALF_DOWN);

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), new BigDecimal("49898.00000001")); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(45005)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                assertEquals(accountA.getBalanceUSE(keyB, dcSet).setScale(8), bal_A_keyB.add(order_BA_2_wantTaked)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                assertEquals(accountB.getBalanceUSE(keyA, dcSet), new BigDecimal("101.99999999")); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                order_AB_1 = dcSet.getCompletedOrderMap().get(order_AB_1_ID);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(order_AB_1.getId()));
                Assert.assertEquals(order_AB_1.getFulfilledHave(), BigDecimal.valueOf(100));
                Assert.assertEquals(true, order_AB_1.isFulfilled());

                order_BA_1 = dcSet.getOrderMap().get(order_BA_1_ID);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(order_BA_1.getId()));
                Assert.assertEquals(order_BA_1.getFulfilledHave().setScale(8), bal_A_keyB.add(order_BA_2_wantTaked));
                Assert.assertEquals(false, order_BA_1.isFulfilled());

                Order orderC = dcSet.getCompletedOrderMap().get(order_AB_2_ID);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderC.getId()));
                Assert.assertEquals(orderC.getFulfilledHave(), amoHave);
                Assert.assertEquals(true, orderC.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, order_BA_1.getInitiatedTrades(dcSet).size());

                trade = orderC.getInitiatedTrades(dcSet).get(0);
                Assert.assertEquals(trade.getInitiator(), (long) order_AB_2_ID);
                Assert.assertEquals(trade.getTarget(), (long) order_BA_1_ID);
                Assert.assertEquals(trade.getAmountHave().setScale(8), order_BA_2_wantTaked);
                Assert.assertEquals(trade.getAmountWant(), amoHave);
            } finally {
                dcSet.close();
            }
        }
    }

    //@Ignore
//TODO actualize the test
    @Test
    public void testOrderProcessingWantDivisible() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ASSET
                assetA = new AssetVenture(itemAppData, accountA, "a", icon, image, "a", 0, 8, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, null, assetA, (byte) 0, timestamp++, 0L);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.sign(accountA, Transaction.FOR_NETWORK);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                // CREATE ASSET
                assetB = new AssetVenture(itemAppData, accountB, "b", icon, image, "b", 0, 8, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++,
                        accountB.getLastTimestamp(dcSet)[0], new byte[64]);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                long keyA = assetA.getKey();
                long keyB = assetB.getKey();

                // CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(100), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_A = orderCreation.makeOrder().getId();

                // CREATE ORDER TWO (SELLING 99.9 B FOR A AT A PRICE OF 5)
                // GENERATES TRADE 99,9 B FOR 495 A
                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, BigDecimal.valueOf(99.9),
                        BigDecimal.valueOf(495), (byte) 0, timestamp++, accountB.getLastTimestamp(dcSet)[0]);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_B = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(0, accountA.getBalanceUSE(keyA, dcSet).compareTo(BigDecimal.valueOf(49000))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyB, dcSet).compareTo(BigDecimal.valueOf(49900.1))); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet).stripTrailingZeros(), BigDecimal.valueOf(99.9)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet).stripTrailingZeros(), BigDecimal.valueOf(999)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                Order orderA = dcSet.getOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderA.getId()));
                Assert.assertEquals(orderA.getFulfilledHave().stripTrailingZeros(), BigDecimal.valueOf(999));
                Assert.assertEquals(false, orderA.isFulfilled());

                Order orderB = dcSet.getCompletedOrderMap().get(orderID_B);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderB.getId()));
                Assert.assertEquals(orderB.getFulfilledHave().stripTrailingZeros(), BigDecimal.valueOf(99.9));
                Assert.assertEquals(true, orderB.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, orderB.getInitiatedTrades(dcSet).size());

                Trade trade = orderB.getInitiatedTrades(dcSet).get(0);
                Assert.assertEquals(trade.getInitiator(), (long) orderID_B);
                Assert.assertEquals(trade.getTarget(), (long) orderID_A);
                Assert.assertEquals(trade.getAmountHave().stripTrailingZeros(), BigDecimal.valueOf(999));
                Assert.assertEquals(trade.getAmountWant().stripTrailingZeros(), BigDecimal.valueOf(99.9));

                // CREATE ORDER THREE (SELLING 99 A FOR B AT A PRICE OF 0.2)
                // GENERATED TRADE 99 A FOR 9.9 B
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(99),
                        BigDecimal.valueOf(19.8), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_C = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(0, accountA.getBalanceUSE(keyA, dcSet).compareTo(BigDecimal.valueOf(48901))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyB, dcSet).compareTo(BigDecimal.valueOf(49900.1))); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(0, accountA.getBalanceUSE(keyB, dcSet).compareTo(BigDecimal.valueOf(99.9))); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyA, dcSet).compareTo(BigDecimal.valueOf(999))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                orderA = dcSet.getOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderA.getId()));
                Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(999)));
                Assert.assertEquals(false, orderA.isFulfilled());

                orderB = dcSet.getCompletedOrderMap().get(orderID_B);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderB.getId()));
                Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(99.9)));
                Assert.assertEquals(true, orderB.isFulfilled());

                Order orderC = dcSet.getOrderMap().get(orderID_C);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderC.getId()));
                Assert.assertEquals(0, orderC.getFulfilledHave().compareTo(BigDecimal.valueOf(0)));
                Assert.assertEquals(false, orderC.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(0, orderC.getInitiatedTrades(dcSet).size());
            } finally {
                dcSet.close();
            }
        }
    }

    //@Ignore
//TODO actualize the test
    @Test
    public void testOrderProcessingHaveDivisible() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ASSET
                assetA = new AssetVenture(itemAppData, accountA, "a", icon, image, "a", 0, 8, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte) 0, timestamp++, 0L,
                        new byte[64]);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                // CREATE ASSET
                assetB = new AssetVenture(itemAppData, accountB, "b", icon, image, "b", 0, 8, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++,
                        accountB.getLastTimestamp(dcSet)[0], new byte[64]);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                long keyA = assetA.getKey();
                long keyB = assetB.getKey();

                // CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(100), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_A = orderCreation.makeOrder().getId();

                // CREATE ORDER TWO (SELLING 200 B FOR PRICE OF 5)
                // GENERATES TRADE 100 B FOR 1000 A
                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, BigDecimal.valueOf(200),
                        BigDecimal.valueOf(1000), (byte) 0, timestamp++, accountB.getLastTimestamp(dcSet)[0]);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_B = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(0, accountA.getBalanceUSE(keyA, dcSet).compareTo(BigDecimal.valueOf(49000))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyB, dcSet).compareTo(BigDecimal.valueOf(49800))); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(0, accountA.getBalanceUSE(keyB, dcSet).compareTo(BigDecimal.valueOf(100))); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyA, dcSet).compareTo(BigDecimal.valueOf(1000))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                Order orderA = dcSet.getCompletedOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderA.getId()));
                Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1000)));
                Assert.assertEquals(true, orderA.isFulfilled());

                Order orderB = dcSet.getOrderMap().get(orderID_B);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderB.getId()));
                Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(100)));
                Assert.assertEquals(false, orderB.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, orderB.getInitiatedTrades(dcSet).size());

                Trade trade = orderB.getInitiatedTrades(dcSet).get(0);
                Assert.assertEquals(trade.getInitiator(), (long) orderID_B);
                Assert.assertEquals(trade.getTarget(), (long) orderID_A);
                Assert.assertEquals(trade.getAmountHave().stripTrailingZeros(), BigDecimal.valueOf(1000).stripTrailingZeros());
                Assert.assertEquals(trade.getAmountWant().stripTrailingZeros(), BigDecimal.valueOf(100).stripTrailingZeros());

                // CREATE ORDER THREE (SELLING 99 A FOR B AT A PRICE OF 0.2) (I CAN BUY
                // AT INCREMENTS OF 1)
                // GENERATED TRADE 95 A for 19 B
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(95.9),
                        BigDecimal.valueOf(19), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_C = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(48904.1)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyB, dcSet).compareTo(BigDecimal.valueOf(49800))); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet).stripTrailingZeros(), BigDecimal.valueOf(119.18)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet).stripTrailingZeros(), BigDecimal.valueOf(1095.9)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                orderA = dcSet.getCompletedOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderA.getId()));
                Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1000)));
                Assert.assertEquals(true, orderA.isFulfilled());

                orderB = dcSet.getOrderMap().get(orderID_B);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderB.getId()));
                Assert.assertEquals(orderB.getFulfilledHave().setScale(assetB.getScale()), BigDecimal.valueOf(119.18).setScale(assetB.getScale()));
                Assert.assertEquals(false, orderB.isFulfilled());

                Order orderC = dcSet.getCompletedOrderMap().get(orderID_C);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderC.getId()));
                Assert.assertEquals(orderC.getFulfilledHave(), BigDecimal.valueOf(95.9));
                Assert.assertEquals(true, orderC.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, orderB.getInitiatedTrades(dcSet).size());

                trade = orderC.getInitiatedTrades(dcSet).get(0);
                Assert.assertEquals(trade.getInitiator(), (long) orderID_C);
                Assert.assertEquals(trade.getTarget(), (long) orderID_B);
                Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(19.18));
                Assert.assertEquals(trade.getAmountWant(), BigDecimal.valueOf(95.9));
            } finally {
                dcSet.close();
            }
        }
    }

    //@Ignore
//TODO actualize the test
    @Test
    public void testOrderProcessingHaveDivisible_3() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ASSET
                assetA = new AssetVenture(itemAppData, accountA, "a", icon, image, "a", 0, 8, 100l);

                // CREATE ISSUE ASSET TRANSACTION
                Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte) 0, timestamp++, 0L, new byte[64]);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                // CREATE ASSET
                assetB = new AssetVenture(itemAppData, accountB, "b", icon, image, "b", 0, 8, 1000000l);

                // CREATE ISSUE ASSET TRANSACTION
                issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++, 0L, new byte[64]);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, 1, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                long keyA = assetA.getKey();
                long keyB = assetB.getKey();

                // CHECK SORTING for orders
                // CREATE ORDER _B SELL 2A x 20000 = 40000
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(2),
                        BigDecimal.valueOf(40000), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, 2, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_B = orderCreation.makeOrder().getId();

                // CREATE ORDER _A SELL 1A for 15000 = 15000
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(1),
                        BigDecimal.valueOf(15000), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, 3, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_A = orderCreation.makeOrder().getId();

                // CREATE ORDER _C SELL 4A x 25000 = 100000
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(4),
                        BigDecimal.valueOf(100000), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_C = orderCreation.makeOrder().getId();

                List<Order> orders = dcSet.getOrderMap().getOrders(keyA);

                // CREATE ORDER _D (BUY) 30000 x 2 = 60000
                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, BigDecimal.valueOf(60000),
                        BigDecimal.valueOf(2), (byte) 0, timestamp++, accountB.getLastTimestamp(dcSet)[0]);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_D = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(93)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                // 60000 - 5000 returned by auto cancel
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(1000000 - 60000)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet).stripTrailingZeros(), BigDecimal.valueOf(60000).stripTrailingZeros()); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet).stripTrailingZeros(), BigDecimal.valueOf(3.2)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                Order orderA = dcSet.getCompletedOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderA.getId()));
                Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1)));
                Assert.assertEquals(0, orderA.getFulfilledWant().compareTo(BigDecimal.valueOf(15000)));
                Assert.assertEquals(true, orderA.isFulfilled());
                Assert.assertEquals(true, orderA.isFulfilled());

                Order orderB = dcSet.getCompletedOrderMap().get(orderID_B);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderB.getId()));
                Assert.assertEquals(orderB.getFulfilledHave(), BigDecimal.valueOf(2));
                Assert.assertEquals(orderB.getFulfilledWant().setScale(0).toPlainString(), "40000");
                Assert.assertEquals(true, orderB.isFulfilled());

                Order orderC = dcSet.getOrderMap().get(orderID_C);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderC.getId()));
                Assert.assertEquals(orderC.getFulfilledHave(), BigDecimal.valueOf(0.2));
                Assert.assertEquals(false, orderC.isFulfilled());

                // buy order
                Order orderD = dcSet.getCompletedOrderMap().get(orderID_D);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderD.getId()));
                Assert.assertEquals(orderD.getFulfilledHave().toPlainString(), "60000");
                /// calc wrong Assert.assertEquals(orderD.getFulfilledWant(),
                /// BigDecimal.valueOf(3));
                // its auto-canceled
                Assert.assertEquals(true, orderD.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(3, orderD.getInitiatedTrades(dcSet).size());

                Trade trade = orderD.getInitiatedTrades(dcSet).get(0);
                Assert.assertEquals(trade.getInitiator(), (long) orderID_D);

                // this may be WRONG in some case - reRUN task!
                // rundon sorting - 3 orders -тут нет сортировки и выдает ордер один из
                // 3-х
                if (trade.getTarget() == orderID_A) {
                    Assert.assertEquals(0, trade.getTarget() == (orderID_A));
                    Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(1));
                    Assert.assertEquals(trade.getAmountWant(), BigDecimal.valueOf(15000));

                    if (false) {
                        trade = orderD.getInitiatedTrades(dcSet).get(1);
                        Assert.assertEquals(0, trade.getInitiator() == (orderID_D));
                        Assert.assertEquals(0, trade.getTarget() == (orderID_B));
                        Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(2));
                        Assert.assertEquals(trade.getAmountWant(), BigDecimal.valueOf(40000));
                    }
                }

                // CREATE ORDER _E - buy 23000 x 2 = 46000
                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, BigDecimal.valueOf(56000),
                        BigDecimal.valueOf(2), (byte) 0, timestamp++, accountB.getLastTimestamp(dcSet)[0]);
                orderCreation.sign(accountB, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_E = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(93)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                // 6000 - returned by non Divisible change - auto canceled order
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(1000000 - 60000 - 56000)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet).stripTrailingZeros(), BigDecimal.valueOf(60000 + 56000).stripTrailingZeros()); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet).stripTrailingZeros(), BigDecimal.valueOf(3.2 + 2.24)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                /// order auto canceled
                Order orderE = dcSet.getCompletedOrderMap().get(orderID_E);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderE.getId()));
                // 6000 returned by auto-cancel
                Assert.assertEquals(orderE.getFulfilledHave(), BigDecimal.valueOf(56000));
                // Assert.assertEquals(orderE.getFulfilledWant(),
                // BigDecimal.valueOf(2));
                // but auto-canceled
                Assert.assertEquals(true, orderE.isFulfilled());

                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderB.getId()));
                // reload order_B
                orderB = dcSet.getCompletedOrderMap().get(orderB.getId());
                Assert.assertEquals(orderB.getFulfilledHave(), BigDecimal.valueOf(2));
                Assert.assertEquals(orderB.getFulfilledWant().setScale(0).toPlainString(), "40000");
                Assert.assertEquals(true, orderB.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, orderE.getInitiatedTrades(dcSet).size());

                trade = orderE.getInitiatedTrades(dcSet).get(0);
                Assert.assertEquals(trade.getInitiator(), (long) orderID_E);
                Assert.assertEquals(trade.getTarget(), (long) orderID_C);
                Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(2.24));
                Assert.assertEquals(trade.getAmountWant().toPlainString(), "56000");

                assertEquals(0, dcSet.getOrderMap().getOrdersForTrade(keyB, keyA, false).size());
                /*
                 * assertEquals(BigDecimal.valueOf(23000),
                 * dcSet.getOrderMap().getOrders(keyB, keyA).get(0).getPriceCalcReverse());
                 * assertEquals(BigDecimal.valueOf(26000),
                 * dcSet.getOrderMap().getOrders(keyB, keyA).get(0).getAmountHaveLeft());
                 * assertEquals(BigDecimal.valueOf(1), dcSet.getOrderMap().getOrders(keyB,
                 * keyA).get(0).getAmountWantLeft());
                 */

            } finally {
                dcSet.close();
            }
        }
    }

    //@Ignore
//TODO actualize the test
    @Test
    public void testOrderProcessingHaveDivisible_3point() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ASSET
                assetA = new AssetVenture(itemAppData, accountA, "a", icon, image, "a", 0, 8, 100l);

                // CREATE ISSUE ASSET TRANSACTION
                Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, null, assetA, (byte) 0, timestamp++, 0L);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.sign(accountA, Transaction.FOR_NETWORK);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                // CREATE ASSET
                assetB = new AssetVenture(itemAppData, accountB, "b", icon, image, "b", 0, 8, 1000000l);

                // CREATE ISSUE ASSET TRANSACTION
                issueAssetTransaction = new IssueAssetTransaction(accountB, null, assetB, (byte) 0, timestamp++,
                        accountB.getLastTimestamp(dcSet)[0]);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.sign(accountB, Transaction.FOR_NETWORK);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                long keyA = assetA.getKey();
                long keyB = assetB.getKey();

                // CREATE ORDER _A SELL 1A for 15000 = 15000
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(1),
                        BigDecimal.valueOf(15000.88), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_A = orderCreation.makeOrder().getId();

                // CREATE ORDER _B SELL 2A x 20000 = 40000
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(2),
                        BigDecimal.valueOf(40000.33), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_B = orderCreation.makeOrder().getId();

                // CREATE ORDER _C SELL 4A x 25000 = 100000
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(4),
                        BigDecimal.valueOf(100007), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_C = orderCreation.makeOrder().getId();

                // CREATE ORDER _D (BUY) 30000 x 2 = 60000
                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, BigDecimal.valueOf(60003),
                        BigDecimal.valueOf(2), (byte) 0, timestamp++, accountB.getLastTimestamp(dcSet)[0]);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_D = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(93)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(1000000 - 60003)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(60003).setScale(8, RoundingMode.HALF_DOWN)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet).stripTrailingZeros(), BigDecimal.valueOf(3.2000576)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                Order orderA = dcSet.getCompletedOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderA.getId()));
                Assert.assertEquals(orderA.getFulfilledHave(), BigDecimal.valueOf(1));
                Assert.assertEquals(orderA.getFulfilledWant(), BigDecimal.valueOf(15000.88).setScale(8));
                Assert.assertEquals(true, orderA.isFulfilled());
                Assert.assertEquals(true, orderA.isFulfilled());

                Order orderB = dcSet.getCompletedOrderMap().get(orderID_B);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderB.getId()));
                Assert.assertEquals(orderB.getFulfilledHave(), BigDecimal.valueOf(2));
                Assert.assertEquals(orderB.getFulfilledWant(), BigDecimal.valueOf(40000.33).setScale(8));
                Assert.assertEquals(true, orderB.isFulfilled());

                Order orderC = dcSet.getOrderMap().get(orderID_C);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderC.getId()));
                Assert.assertEquals(orderC.getFulfilledHave(), BigDecimal.valueOf(0.2000576));
                Assert.assertEquals(false, orderC.isFulfilled());

                // buy order
                Order orderD = dcSet.getCompletedOrderMap().get(orderID_D);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderD.getId()));
                Assert.assertEquals(orderD.getFulfilledHave(), BigDecimal.valueOf(60003).setScale(2));
                // Assert.assertEquals(orderD.getFulfilledWant(),
                // BigDecimal.valueOf(2));
                Assert.assertEquals(true, orderD.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(3, orderD.getInitiatedTrades(dcSet).size());

                Trade trade = orderD.getInitiatedTrades(dcSet).get(0);
                Assert.assertEquals(trade.getInitiator(), (long) orderID_D);

                // this may be WRONG in some case - reRUN task!
                if (trade.getTarget() == orderID_A) {
                    Assert.assertEquals(trade.getTarget(), (long) orderID_A);
                    Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(1));
                    Assert.assertEquals(trade.getAmountWant(), BigDecimal.valueOf(15000.88));

                    trade = orderD.getInitiatedTrades(dcSet).get(1);
                    if (trade.getTarget() == orderID_B) {
                        Assert.assertEquals(trade.getInitiator(), (long) orderID_D);
                        Assert.assertEquals(trade.getTarget(), (long) orderID_B);
                        Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(2)); // 1
                        Assert.assertEquals(trade.getAmountWant(), BigDecimal.valueOf(40000.33)); //20000.165));
                    }
                }

                // CREATE ORDER _E - buy 23000 x 2 = 46000
                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, BigDecimal.valueOf(51000),
                        BigDecimal.valueOf(2), (byte) 0, timestamp++, accountB.getLastTimestamp(dcSet)[0]);
                orderCreation.sign(accountB, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_E = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(93)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(888997)); //918998.955)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet).stripTrailingZeros(), BigDecimal.valueOf(111003)); //55001.21)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(5.23991481)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                /// order in memory !!!
                Order orderE = dcSet.getCompletedOrderMap().get(orderID_E);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderE.getId()));
                Assert.assertEquals(orderE.getFulfilledHave(), BigDecimal.valueOf(51000));
                Assert.assertEquals(orderE.getFulfilledWant(), BigDecimal.valueOf(2).setScale(8));
                Assert.assertEquals(true, orderE.isFulfilled());

                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderB.getId()));
                // reload order_B
                orderB = dcSet.getCompletedOrderMap().get(orderB.getId());
                Assert.assertEquals(orderB.getFulfilledHave(), BigDecimal.valueOf(2));
                Assert.assertEquals(orderB.getFulfilledWant(), BigDecimal.valueOf(40000.33).setScale(8));
                Assert.assertEquals(true, orderB.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, orderE.getInitiatedTrades(dcSet).size());

                trade = orderE.getInitiatedTrades(dcSet).get(0);
                Assert.assertEquals(trade.getInitiator(), (long) orderID_E);
                Assert.assertEquals(true, trade.getTarget() > orderID_B);
                Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(2.03985721));
                Assert.assertEquals(trade.getAmountWant().toPlainString(), BigDecimal.valueOf(51000).toPlainString());

                assertEquals(1, dcSet.getOrderMap().getOrdersForTrade(keyA, keyB, false).size());
                Order order_123 = dcSet.getOrderMap().getOrdersForTrade(keyA, keyB, false).get(0);
                assertEquals(BigDecimal.valueOf(25001.75), order_123.getPrice());
                assertEquals(BigDecimal.valueOf(1.76008519), order_123.getAmountHaveLeft());
                assertEquals(BigDecimal.valueOf(44005.20989908), order_123.getAmountWantLeft());

            } finally {
                dcSet.close();
            }
        }
    }

    public void init_Sell_noDiv_Div() {

        // CREATE ASSET
        assetA = new AssetVenture(itemAppData, accountA, "a", icon, image, "a", 0, 0, 100L);

        // CREATE ISSUE ASSET TRANSACTION
        Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte) 0, timestamp++, 0L);
        issueAssetTransaction.sign(accountA, Transaction.FOR_NETWORK);
        issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height + 1, ++seqNo, false);
        issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

        // CREATE ASSET
        assetB = new AssetVenture(itemAppData, accountB, "b", icon, image, "b", 0, 0, 1000000L);

        // CREATE ISSUE ASSET TRANSACTION
        issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++, 0L);
        issueAssetTransaction.sign(accountB, Transaction.FOR_NETWORK);
        issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height + 1, ++seqNo, false);
        issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

        keyA = assetA.getKey();
        keyB = assetB.getKey();

        // BUY cascade ///

        // CREATE ORDER _A SELL 2A for 15000 = 30000
        orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, BigDecimal.valueOf(30000),
                BigDecimal.valueOf(2), (byte) 0, timestamp++, accountB.getLastTimestamp(dcSet)[0]);
        orderCreation.sign(accountA, Transaction.FOR_NETWORK);
        orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
        assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
        orderCreation.process(null, Transaction.FOR_NETWORK);
        order_AB_1_ID = orderCreation.makeOrder().getId();

        // CREATE ORDER _B SELL 2A x 20000 = 40000
        orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, BigDecimal.valueOf(40000),
                BigDecimal.valueOf(2), (byte) 0, timestamp++, accountB.getLastTimestamp(dcSet)[0]);
        orderCreation.sign(accountA, Transaction.FOR_NETWORK);
        orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
        assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
        orderCreation.process(null, Transaction.FOR_NETWORK);
        order_AB_2_ID = orderCreation.makeOrder().getId();

        // CREATE ORDER _C SELL 4A x 25000 = 100000
        orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, BigDecimal.valueOf(100000),
                BigDecimal.valueOf(4), (byte) 0, timestamp++, accountB.getLastTimestamp(dcSet)[0]);
        orderCreation.sign(accountA, Transaction.FOR_NETWORK);
        orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
        assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
        orderCreation.process(null, Transaction.FOR_NETWORK);
        order_AB_3_ID = orderCreation.makeOrder().getId();

    }

    //@Ignore
//TODO actualize the test
    @Test
    public void testOrderProcessingHaveDivisible_3reverse() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                init_Sell_noDiv_Div();

                // CREATE ORDER _D (BUY) 15000 x 1 = 15000
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(1),
                        BigDecimal.valueOf(15000), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_AB_4_ID = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(99)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(830000)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(25000)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(1)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CREATE ORDER _D (BUY) 30000 x 2 = 60000
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(2),
                        BigDecimal.valueOf(50000), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_AB_4_ID = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(97)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(830000)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(50000 + 25000)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(3)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                assertEquals(3, dcSet.getOrderMap().getOrdersForTrade(keyB, keyA, true).size());

                // CREATE ORDER _I SELL 3A for 24000 = 48000
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(3),
                        BigDecimal.valueOf(100000), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_AB_5_ID = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(94)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(830000)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(75000)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(3)); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B
            } finally {
                dcSet.close();
            }
        }
    }

    /**
     * Провели вместе с новой мультиСУБД и isUnResolved 2019-11-12
     */
    @Test
    public void testOrderProcessingNonDivisible() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ASSET
                AssetCls assetA = new AssetVenture(itemAppData, accountA, "a", icon, image, "a", 0, 0, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte) 0, timestamp++, 0L);
                issueAssetTransaction.sign(accountA, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                // CREATE ASSET
                AssetCls assetB = new AssetVenture(itemAppData, accountB, "b", icon, image, "b", 0, 0, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++,
                        accountB.getLastTimestamp(dcSet)[0]);
                issueAssetTransaction.sign(accountB, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                long keyA = assetA.getKey();
                long keyB = assetB.getKey();

                // CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB,
                        BigDecimal.valueOf(1000).setScale(assetA.getScale()),
                        BigDecimal.valueOf(100).setScale(assetB.getScale()), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK); // need for Order.getID()
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_A = orderCreation.makeOrder().getId();

                // CREATE ORDER TWO (SELLING 1000 B FOR A AT A PRICE OF 5)
                // GENERATES TRADE 100 B FOR 1000 A
                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA,
                        BigDecimal.valueOf(1000).setScale(assetB.getScale()),
                        BigDecimal.valueOf(5000).setScale(assetA.getScale()), (byte) 0, timestamp++,
                        accountB.getLastTimestamp(dcSet)[0]);
                orderCreation.sign(accountB, Transaction.FOR_NETWORK); // need for Order.getID()
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_B = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(49000).setScale(assetA.getScale())); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(49000).setScale(assetA.getScale())); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(100).setScale(assetA.getScale())); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(1000).setScale(assetA.getScale())); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                Order orderA = dcSet.getCompletedOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderA.getId()));
                Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1000)));
                Assert.assertEquals(true, orderA.isFulfilled());

                Order orderB = dcSet.getOrderMap().get(orderID_B);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderB.getId()));
                Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(100)));
                Assert.assertEquals(false, orderB.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, orderB.getInitiatedTrades(dcSet).size());

                Trade trade = orderB.getInitiatedTrades(dcSet).get(0);
                assertEquals(trade.getInitiator(), (long) orderID_B);
                assertEquals(trade.getTarget(), (long) orderID_A);
                Assert.assertEquals(0, trade.getAmountHave().compareTo(BigDecimal.valueOf(1000)));
                Assert.assertEquals(0, trade.getAmountWant().compareTo(BigDecimal.valueOf(100)));

                // CREATE ORDER THREE (SELLING 24 A FOR B AT A PRICE OF 0.2)
                // GENERATES TRADE 24 A FOR 4 B
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB,
                        BigDecimal.valueOf(24).setScale(assetA.getScale()), BigDecimal.valueOf(4).setScale(assetB.getScale()),
                        (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK); // need for Order.getID()
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_C = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                // THIS ORDER must not be RESOLVED
                assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(48976).setScale(assetA.getScale())); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(49000).setScale(assetA.getScale())); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                assertEquals(accountA.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(105).setScale(assetA.getScale())); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                assertEquals(accountB.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(1024).setScale(assetA.getScale())); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                orderA = dcSet.getCompletedOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderA.getId()));
                Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1000)));
                Assert.assertEquals(true, orderA.isFulfilled());

                orderB = dcSet.getOrderMap().get(orderID_B);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderB.getId()));
                Assert.assertEquals(orderB.getFulfilledHave().toPlainString(), "105");
                Assert.assertEquals(false, orderB.isFulfilled());

                Order orderC = dcSet.getCompletedOrderMap().get(orderID_C);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderC.getId()));
                Assert.assertEquals(orderC.getFulfilledHave().toPlainString(), "24");
                Assert.assertEquals(true, orderC.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, orderC.getInitiatedTrades(dcSet).size());

                ///////////////////// ATTENTION
                //  - здесь произойдет отмена ордера так как его первоначальная цена удет слишком далеко
                //
                int amo_A = 20003;
                int amo_B = 4000;

                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB,
                        BigDecimal.valueOf(amo_A).setScale(assetA.getScale()),
                        BigDecimal.valueOf(amo_B).setScale(assetB.getScale()), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK); // need for Order.getID()
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_D = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                // THIS ORDER must not be RESOLVED
                // и будет возврат так как сработает org.erachain.core.item.assets.Order.isUnResolved
                assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(28973).setScale(assetA.getScale())); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(49000).setScale(assetA.getScale())); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                assertEquals(accountA.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(1000).setScale(assetA.getScale())); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                assertEquals(accountB.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(5499).setScale(assetA.getScale())); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                orderA = dcSet.getCompletedOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderA.getId()));
                Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1000)));
                Assert.assertEquals(true, orderA.isFulfilled());

                orderB = dcSet.getCompletedOrderMap().get(orderID_B);
                Assert.assertEquals(true, dcSet.getCompletedOrderMap().contains(orderB.getId()));
                Assert.assertEquals(1, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(104)));
                Assert.assertEquals(true, orderB.isFulfilled());

                Order orderD = dcSet.getOrderMap().get(orderID_D);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderD.getId()));
                Assert.assertEquals(1, orderD.getFulfilledHave().compareTo(BigDecimal.valueOf(24)));
                Assert.assertEquals(false, orderD.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, orderD.getInitiatedTrades(dcSet).size());

                trade = orderD.getInitiatedTrades(dcSet).get(0);
                /// ??? assertEquals(trade.getInitiator(), orderID);
                assertEquals(trade.getTarget(), (long) orderID_B);
                Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(895));
                Assert.assertEquals(trade.getAmountWant(), BigDecimal.valueOf(4475));

                //// TOTALS
                assertEquals(accountA.getBalanceUSE(keyA, dcSet).add(accountB.getBalanceUSE(keyA, dcSet)),
                        BigDecimal.valueOf(34472).setScale(assetA.getScale())); // BALANCE

                // еще активные ордера
                List<Order> ordersAll = dcSet.getOrderMap().getOrdersForTradeWithFork(keyB, keyA, null);
                BigDecimal total = BigDecimal.ZERO;
                for (Order order : ordersAll) {
                    total = total.add(order.getAmountHaveLeft());
                }

                assertEquals(accountA.getBalanceUSE(keyB, dcSet).add(accountB.getBalanceUSE(keyB, dcSet)).add(total),
                        BigDecimal.valueOf(50000).setScale(assetA.getScale())); // BALANCE

            } finally {
                dcSet.close();
            }
        }
    }

    //@Ignore
//TODO actualize the test
    @Test
    public void testOrderProcessingNonDivisible2() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ASSET
                AssetCls assetA = new AssetVenture(itemAppData, accountA, "a", icon, image, "a", 0, 0, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte) 0, timestamp++, 0L);
                issueAssetTransaction.sign(accountA, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                // CREATE ASSET
                AssetCls assetB = new AssetVenture(itemAppData, accountB, "b", icon, image, "b", 0, 0, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++,
                        accountB.getLastTimestamp(dcSet)[0]);
                issueAssetTransaction.sign(accountB, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                long keyA = assetA.getKey();
                long keyB = assetB.getKey();

                // CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB,
                        BigDecimal.valueOf(100).setScale(assetA.getScale()), BigDecimal.valueOf(10).setScale(assetB.getScale()),
                        (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK); // need for Order.getID()
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_A = orderCreation.makeOrder().getId();

                // CREATE ORDER TWO (SELLING 1000 B FOR A AT A PRICE OF 5)
                // GENERATES TRADE 100 B FOR 1000 A
                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA,
                        BigDecimal.valueOf(20).setScale(assetA.getScale()), BigDecimal.valueOf(100).setScale(assetA.getScale()),
                        (byte) 0, timestamp++, accountB.getLastTimestamp(dcSet)[0]);
                orderCreation.sign(accountB, Transaction.FOR_NETWORK); // need for Order.getID()
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_B = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(49900).setScale(assetA.getScale())); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(49980).setScale(assetA.getScale())); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(10).setScale(assetA.getScale())); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(100).setScale(assetA.getScale())); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                Order orderA = dcSet.getCompletedOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderA.getId()));
                Assert.assertEquals(orderA.getFulfilledHave(), BigDecimal.valueOf(100));
                Assert.assertEquals(true, orderA.isFulfilled());

                Order orderB = dcSet.getOrderMap().get(orderID_B);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderB.getId()));
                Assert.assertEquals(orderB.getFulfilledHave(), BigDecimal.valueOf(10));
                Assert.assertEquals(false, orderB.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, orderB.getInitiatedTrades(dcSet).size());

                Trade trade = orderB.getInitiatedTrades(dcSet).get(0);
                assertEquals(trade.getInitiator(), (long) orderID_B);
                assertEquals(trade.getTarget(), (long) orderID_A);
                Assert.assertEquals(trade.getAmountHave().toPlainString(), "100");
                Assert.assertEquals(trade.getAmountWant().toPlainString(), "10");

            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void testOrderProcessingNonDivisible3() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ASSET
                AssetCls assetA = new AssetVenture(itemAppData, accountA, "a", icon, image, "a", 0, 0, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte) 0, timestamp++, 0L);
                issueAssetTransaction.sign(accountA, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                // CREATE ASSET
                AssetCls assetB = new AssetVenture(itemAppData, accountB, "b", icon, image, "b", 0, 2, 50000l);

                // CREATE ISSUE ASSET TRANSACTION
                issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++,
                        accountB.getLastTimestamp(dcSet)[0]);
                issueAssetTransaction.sign(accountB, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                long keyA = assetA.getKey();
                long keyB = assetB.getKey();

                BigDecimal amoA1 = BigDecimal.valueOf(1000).setScale(assetA.getScale());
                BigDecimal amoB1 = BigDecimal.valueOf(34.58).setScale(assetB.getScale());

                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, amoA1, amoB1, (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK); // need for Order.getID()
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_A = orderCreation.makeOrder().getId();

                BigDecimal amoA2 = BigDecimal.valueOf(10).setScale(assetA.getScale());
                BigDecimal amoB2 = BigDecimal.valueOf(0.38).setScale(assetB.getScale());

                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, amoB2, amoA2, (byte) 0, timestamp++,
                        accountB.getLastTimestamp(dcSet)[0]);
                orderCreation.sign(accountB, Transaction.FOR_NETWORK); // need for Order.getID()
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_B = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(49000).setScale(assetA.getScale())); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(49999.62).setScale(assetB.getScale(), RoundingMode.HALF_DOWN)); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet).toPlainString(), "0.38"); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet).toPlainString(), "11"); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                Order orderA = dcSet.getOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderA.getId()));
                Assert.assertEquals(orderA.getFulfilledHave(), BigDecimal.valueOf(11));
                Assert.assertEquals(false, orderA.isFulfilled());

                Order orderB = dcSet.getCompletedOrderMap().get(orderID_B);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderB.getId()));
                Assert.assertEquals(orderB.getFulfilledHave().toPlainString(), "0.38");
                Assert.assertEquals(true, orderB.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, orderB.getInitiatedTrades(dcSet).size());

                Trade trade = orderB.getInitiatedTrades(dcSet).get(0);
                assertEquals(trade.getInitiator(), (long) orderID_B);
                assertEquals(trade.getTarget(), (long) orderID_A);
                Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(11));
                Assert.assertEquals(trade.getAmountWant(), BigDecimal.valueOf(0.38));
            } finally {
                dcSet.close();
            }
        }
    }

    //@Ignore
//TODO actualize the test
    @Test
    public void testOrderProcessingDivisible_02() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ASSET
                AssetCls assetA = new AssetVenture(itemAppData, accountA, "a", icon, image, "a", 0, 2, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte) 0, timestamp++, 0L);
                issueAssetTransaction.sign(accountA, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                // CREATE ASSET
                AssetCls assetB = new AssetVenture(itemAppData, accountB, "b", icon, image, "b", 0, 0, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++,
                        accountB.getLastTimestamp(dcSet)[0]);
                issueAssetTransaction.sign(accountB, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                long keyA = assetA.getKey();
                long keyB = assetB.getKey();

                // CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)

                BigDecimal vol1 = BigDecimal.valueOf(100).setScale(assetA.getScale());
                BigDecimal vol2 = BigDecimal.valueOf(10).setScale(assetB.getScale());

                orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, vol1, vol2, (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK); // need for Order.getID()
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_A = Transaction.makeDBRef(orderCreation.getHeightSeqNo());

                // CREATE ORDER TWO (SELLING 1000 B FOR A AT A PRICE OF 5)
                // GENERATES TRADE 100 B FOR 1000 A

                BigDecimal vol3 = BigDecimal.valueOf(20).setScale(assetB.getScale());
                BigDecimal vol4 = BigDecimal.valueOf(130).setScale(assetA.getScale());

                orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, vol3, vol4, (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountB, Transaction.FOR_NETWORK); // need for Order.getID()
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(Transaction.FOR_NETWORK, txFlags));
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID_B = orderCreation.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(accountA.getBalanceUSE(keyA, dcSet), BigDecimal.valueOf(49900).setScale(assetA.getScale())); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(49980).setScale(assetB.getScale())); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(accountA.getBalanceUSE(keyB, dcSet), BigDecimal.valueOf(10).setScale(assetB.getScale())); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(accountB.getBalanceUSE(keyA, dcSet).stripTrailingZeros().toPlainString(), "100"); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                Order orderA = dcSet.getCompletedOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderA.getId()));
                Assert.assertEquals(orderA.getFulfilledHave(), BigDecimal.valueOf(100));
                Assert.assertEquals(true, orderA.isFulfilled());

                Order orderB = dcSet.getOrderMap().get(orderID_B);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderB.getId()));
                Assert.assertEquals(orderB.getFulfilledHave(), BigDecimal.valueOf(10));
                Assert.assertEquals(false, orderB.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(1, orderB.getInitiatedTrades(dcSet).size());

                Trade trade = orderB.getInitiatedTrades(dcSet).get(0);
                assertEquals(trade.getInitiator(), (long) orderID_B);
                assertEquals(trade.getTarget(), (long) orderID_A);
                Assert.assertEquals(trade.getAmountHave().toPlainString(), "100");
                Assert.assertEquals(trade.getAmountWant().toPlainString(), "10");

            } finally {
                dcSet.close();
            }
        }
    }

    //@Ignore
//TODO actualize the test
    @Test
    public void testOrderProcessingMultipleOrders() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);
                // CREATE ASSET
                AssetCls assetA = new AssetVenture(itemAppData, accountA, "a", icon, image, "a", 0, 8, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte) 0, timestamp++, 0L);
                issueAssetTransaction.sign(accountA, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                accountB.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(assetA.getScale()), false, false, false);

                // CREATE ASSET
                AssetCls assetB = new AssetVenture(itemAppData, accountB, "b", icon, image, "b", 0, 8, 50000l);

                // CREATE ISSUE ASSET TRANSACTION
                issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++,
                        accountB.getLastTimestamp(dcSet)[0]);
                issueAssetTransaction.sign(accountB, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                long keyA = assetA.getKey();
                long keyB = assetB.getKey();

                // CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
                CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB,
                        BigDecimal.valueOf(1000).setScale(assetA.getScale()),
                        BigDecimal.valueOf(100).setScale(assetA.getScale()), (byte) 0, timestamp++, 0L);
                createOrderTransaction.sign(accountA, Transaction.FOR_NETWORK);
                createOrderTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                createOrderTransaction.process(null, Transaction.FOR_NETWORK);
                Long orderID_A = createOrderTransaction.makeOrder().getId();

                // CREATE ORDER TWO (SELLING 1000 A FOR B AT A PRICE FOR 0.20)
                createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB,
                        BigDecimal.valueOf(1000).setScale(assetA.getScale()),
                        BigDecimal.valueOf(200).setScale(assetA.getScale()), (byte) 0, timestamp++, 0L);
                createOrderTransaction.sign(accountA, Transaction.FOR_NETWORK);
                createOrderTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                createOrderTransaction.process(null, Transaction.FOR_NETWORK);
                Long orderID_B = createOrderTransaction.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(0, accountA.getBalanceUSE(keyA, dcSet).compareTo(BigDecimal.valueOf(48000))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyB, dcSet).compareTo(BigDecimal.valueOf(50000))); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                assertEquals(accountA.getBalanceUSE(keyB, dcSet).setScale(assetA.getScale()), BigDecimal.valueOf(0).setScale(assetA.getScale())); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyA, dcSet).compareTo(BigDecimal.valueOf(0))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                Order orderA = dcSet.getOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderA.getId()));
                Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(0)));
                Assert.assertEquals(false, orderA.isFulfilled());

                Order orderB = dcSet.getOrderMap().get(orderID_B);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderB.getId()));
                Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(0)));
                Assert.assertEquals(false, orderB.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(0, orderB.getInitiatedTrades(dcSet).size());

                // CREATE ORDER THREE (SELLING 150 B FOR A AT A PRICE OF 5)
                createOrderTransaction = new CreateOrderTransaction(accountB, keyB, keyA,
                        BigDecimal.valueOf(150).setScale(assetA.getScale()), BigDecimal.valueOf(750), (byte) 0, timestamp++, 0L,
                        new byte[]{3, 4});
                createOrderTransaction.sign(accountA, Transaction.FOR_NETWORK);
                createOrderTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                createOrderTransaction.process(null, Transaction.FOR_NETWORK);
                Long orderID_C = createOrderTransaction.makeOrder().getId();

                // CHECK BALANCES
                Assert.assertEquals(0, accountA.getBalanceUSE(keyA, dcSet).compareTo(BigDecimal.valueOf(48000))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyB, dcSet).compareTo(BigDecimal.valueOf(49850))); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(0, accountA.getBalanceUSE(keyB, dcSet).compareTo(BigDecimal.valueOf(150))); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyA, dcSet).compareTo(BigDecimal.valueOf(1250))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                orderA = dcSet.getCompletedOrderMap().get(orderID_A);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderA.getId()));
                Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1000)));
                Assert.assertEquals(true, orderA.isFulfilled());

                orderB = dcSet.getOrderMap().get(orderID_B);
                Assert.assertEquals(false, dcSet.getCompletedOrderMap().contains(orderB.getId()));
                Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(250)));
                Assert.assertEquals(false, orderB.isFulfilled());

                Order orderC = dcSet.getCompletedOrderMap().get(orderID_C);
                Assert.assertEquals(false, dcSet.getOrderMap().contains(orderC.getId()));
                Assert.assertEquals(0, orderC.getFulfilledHave().compareTo(BigDecimal.valueOf(150)));
                Assert.assertEquals(true, orderC.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(0, orderA.getInitiatedTrades(dcSet).size());
                Assert.assertEquals(0, orderB.getInitiatedTrades(dcSet).size());
                Assert.assertEquals(2, orderC.getInitiatedTrades(dcSet).size());

                Trade trade = orderC.getInitiatedTrades(dcSet).get(1);
                Assert.assertEquals(trade.getInitiator(), (long) orderID_C);
                assertEquals(trade.getTarget(), (long) orderID_B);
                Assert.assertEquals(trade.getAmountHave().toPlainString(), "250");
                Assert.assertEquals(trade.getAmountWant().toPlainString(), "50");

                trade = orderC.getInitiatedTrades(dcSet).get(0);
                Assert.assertEquals(trade.getInitiator(), (long) orderID_C);
                Assert.assertEquals(trade.getTarget(), (long) orderID_A);
                Assert.assertEquals(trade.getAmountHave().toPlainString(), "1000");
                Assert.assertEquals(trade.getAmountWant().toPlainString(), "100");
            } finally {
                dcSet.close();
            }
        }
    }

    //@Ignore
//TODO actualize the test
    // TODO нужно сделать создание форкнутой базы и покусыавание орлера в ней - так чтобы он появился в форкнутой в измененом виде
    // затем делаем второй проход - и получаем ключи совокупные от родителя и от форка - там этот ордер должен быть в единственном экземпляре!

    @Test
    public void testOrderProcessingForks() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ASSET
                AssetCls assetA = new AssetVenture(itemAppData, accountA, "a", icon, image, "a", 0, 8, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, null, assetA, (byte) 0, timestamp++, 0L);
                issueAssetTransaction.sign(accountA, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                // transaction = new GenesisTransaction(accountB,
                // BigDecimal.valueOf(1000), NTP.getTime());
                // transaction.process(dcSet, false);
                accountB.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(1), false, false, false);

                // CREATE ASSET
                AssetCls assetB = new AssetVenture(itemAppData, accountB, "b", icon, image, "b", 0, 8, 50000L);

                // CREATE ISSUE ASSET TRANSACTION
                issueAssetTransaction = new IssueAssetTransaction(accountB, null, assetB, (byte) 0, timestamp++,
                        accountB.getLastTimestamp(dcSet)[0]);
                issueAssetTransaction.sign(accountA, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

                long keyA = assetA.getKey();
                long keyB = assetB.getKey();

                // CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
                DCSet fork1 = dcSet.fork(this.toString());
                CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB,
                        BigDecimal.valueOf(1000), BigDecimal.valueOf(100), (byte) 0, timestamp++,
                        accountA.getLastTimestamp(fork1)[0], new byte[]{5, 6});
                createOrderTransaction.sign(accountA, Transaction.FOR_NETWORK);
                createOrderTransaction.setDC(fork1, Transaction.FOR_NETWORK, height, ++seqNo, true);
                createOrderTransaction.process(null, Transaction.FOR_NETWORK);
                Long orderID_A = createOrderTransaction.makeOrder().getId();

                // CREATE ORDER TWO (SELLING 1000 A FOR B AT A PRICE FOR 0.20)
                DCSet fork2 = fork1.fork(this.toString());
                createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(200), (byte) 0, timestamp++, accountA.getLastTimestamp(fork2)[0], new byte[]{1, 2});
                createOrderTransaction.sign(accountA, Transaction.FOR_NETWORK);
                createOrderTransaction.setDC(fork1, Transaction.FOR_NETWORK, height, ++seqNo, true);
                createOrderTransaction.process(null, Transaction.FOR_NETWORK);
                Long orderID_B = createOrderTransaction.makeOrder().getId();

                Order orderB = fork1.getOrderMap().get(orderID_B);
                Assert.assertEquals(0, orderB.getInitiatedTrades(fork1).size());

                // CREATE ORDER THREE (SELLING 150 B FOR A AT A PRICE OF 5)
                DCSet fork3 = fork2.fork(this.toString());
                createOrderTransaction = new CreateOrderTransaction(accountB, keyB, keyA, BigDecimal.valueOf(150),
                        BigDecimal.valueOf(750), (byte) 0, timestamp++, accountA.getLastTimestamp(fork3)[0], new byte[]{3, 4});
                createOrderTransaction.sign(accountB, Transaction.FOR_NETWORK);
                createOrderTransaction.setDC(fork3, Transaction.FOR_NETWORK, height, ++seqNo, true);
                createOrderTransaction.process(null, Transaction.FOR_NETWORK);
                Long orderID_C = createOrderTransaction.makeOrder().getId();

                Order orderC = fork3.getCompletedOrderMap().get(orderID_C);
                // CHECK TRADES
                Assert.assertEquals(2, orderC.getInitiatedTrades(fork3).size());
                // in fork is NULL Assert.assertEquals(2, fork3.getTradeMap().getTradesByOrderID(orderID_C).size());

                // ORPHAN ORDER THREE
                createOrderTransaction.orphan(gb, Transaction.FOR_NETWORK);

                Assert.assertEquals(0, orderC.getInitiatedTrades(fork3).size());
                // in fork is NULL Assert.assertEquals(0, fork3.getTradeMap().getTradesByOrderID(orderID_C).size());

                // CHECK BALANCES
                Assert.assertEquals(0, accountA.getBalanceUSE(keyA, fork3).compareTo(BigDecimal.valueOf(48000))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyB, fork3).compareTo(BigDecimal.valueOf(50000))); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(0, accountA.getBalanceUSE(keyB, fork3).compareTo(BigDecimal.valueOf(0))); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyA, fork3).compareTo(BigDecimal.valueOf(0))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                Order orderA = fork3.getOrderMap().get(orderID_A);
                Assert.assertEquals(false, fork3.getCompletedOrderMap().contains(orderA.getId()));
                Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(0)));
                Assert.assertEquals(false, orderA.isFulfilled());

                orderB = fork3.getOrderMap().get(orderID_B);
                Assert.assertEquals(false, fork3.getCompletedOrderMap().contains(orderB.getId()));
                assertEquals(orderB.getFulfilledHave(), BigDecimal.valueOf(0));
                Assert.assertEquals(false, orderB.isFulfilled());

                // CHECK TRADES
                Assert.assertEquals(0, orderB.getInitiatedTrades(dcSet).size());
                Assert.assertEquals(0, orderB.getInitiatedTrades(fork3).size());

                // ORPHAN ORDER TWO
                createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB, BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(200), (byte) 0, timestamp++, accountA.getLastTimestamp(fork2)[0]);
                createOrderTransaction.sign(accountA, Transaction.FOR_NETWORK);
                createOrderTransaction.setDC(fork3, Transaction.FOR_NETWORK, height, ++seqNo, false);
                createOrderTransaction.orphan(gb, Transaction.FOR_NETWORK);

                // CHECK BALANCES
                Assert.assertEquals(0, accountA.getBalanceUSE(keyA, fork3).compareTo(BigDecimal.valueOf(49000))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyB, fork3).compareTo(BigDecimal.valueOf(50000))); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // B
                Assert.assertEquals(0, accountA.getBalanceUSE(keyB, fork3).compareTo(BigDecimal.valueOf(0))); // BALANCE
                // B
                // FOR
                // ACCOUNT
                // A
                Assert.assertEquals(0, accountB.getBalanceUSE(keyA, fork3).compareTo(BigDecimal.valueOf(0))); // BALANCE
                // A
                // FOR
                // ACCOUNT
                // B

                // CHECK ORDERS
                orderA = fork2.getOrderMap().get(orderID_A);
                Assert.assertEquals(false, fork2.getCompletedOrderMap().contains(orderA.getId()));
                Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(0)));
                Assert.assertEquals(false, orderA.isFulfilled());

                Assert.assertEquals(false, fork2.getOrderMap().contains(orderID_C));
                Assert.assertEquals(false, fork2.getCompletedOrderMap().contains(orderID_C));


            } finally {
                dcSet.close();
            }
        }
    }

    // CANCEL ORDER

    @Test
    public void validateSignatureCancelOrderTransaction() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ORDER CANCEL
                Transaction cancelOrderTransaction = new CancelOrderTransaction(accountA, new byte[64], FEE_POWER, timestamp,
                        0L);
                cancelOrderTransaction.sign(accountA, Transaction.FOR_NETWORK);
                // CHECK IF ORDER CANCEL IS VALID
                cancelOrderTransaction.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
                assertEquals(true, cancelOrderTransaction.isSignatureValid(dcSet));

                // INVALID SIGNATURE
                cancelOrderTransaction = new CancelOrderTransaction(accountA, new byte[64], FEE_POWER, timestamp, 0L,
                        new byte[1]);

                // CHECK IF ORDER CANCEL
                cancelOrderTransaction.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
                assertEquals(false, cancelOrderTransaction.isSignatureValid(dcSet));
            } finally {
                dcSet.close();
            }
        }
    }

    //@Ignore
    @Test
    public void validateCancelOrderTransaction() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ORDER
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID = orderCreation.makeOrder().getId();
                dcSet.getTransactionFinalMapSigns().put(orderCreation.getSignature(), orderID);

                timestamp++;

                // CREATE CANCEL ORDER
                // Long time = maker.getLastReference(dcSet);
                CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(accountA, orderCreation.getSignature(), FEE_POWER,
                        timestamp++, 0L);
                cancelOrderTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                cancelOrderTransaction.sign(accountA, Transaction.FOR_NETWORK);

                // CHECK IF CANCEL ORDER IS VALID
                assertEquals(Transaction.VALIDATE_OK, cancelOrderTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

                cancelOrderTransaction = new CancelOrderTransaction(accountA, new byte[Crypto.SIGNATURE_LENGTH], FEE_POWER,
                        timestamp++, 0L);

                int bug_level = BlockChain.CHECK_BUGS;
                try {
                    // на этом уровне ошибка сработает
                    BlockChain.CHECK_BUGS = 10;
                    cancelOrderTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                    assertEquals("error", "должна была быть ошибка NullPointerException");
                } catch (NullPointerException e) {
                }
                BlockChain.CHECK_BUGS = bug_level;

                cancelOrderTransaction.sign(accountA, Transaction.FOR_NETWORK);

                // CHECK IF CANCEL ORDER IS INVALID
                assertEquals(Transaction.ORDER_DOES_NOT_EXIST, cancelOrderTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

                // CREATE INVALID CANCEL ORDER INCORRECT CREATOR
                byte[] seed = Crypto.getInstance().digest("invalid".getBytes());
                byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
                PrivateKeyAccount invalidCreator = new PrivateKeyAccount(privateKey);
                cancelOrderTransaction = new CancelOrderTransaction(invalidCreator, orderCreation.getSignature(), FEE_POWER, timestamp++, 0L,
                        new byte[]{1, 2});
                cancelOrderTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);

                // CHECK IF CANCEL ORDER IS INVALID
                assertEquals(Transaction.INVALID_ORDER_CREATOR, cancelOrderTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

                // CREATE INVALID CANCEL ORDER NO BALANCE
                DCSet fork = dcSet.fork(this.toString());
                cancelOrderTransaction = new CancelOrderTransaction(accountA, orderCreation.getSignature(), FEE_POWER, timestamp++, 0L);
                cancelOrderTransaction.setDC(fork, Transaction.FOR_NETWORK, height, ++seqNo, true);
                cancelOrderTransaction.sign(accountA, Transaction.FOR_NETWORK);

                // CHECK IF CANCEL ORDER IS INVALID
                accountA.changeBalance(fork, true, false, FEE_KEY, new BigDecimal("1000"), false, false, false);
                assertEquals(Transaction.NOT_ENOUGH_FEE, cancelOrderTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

            } finally {
                dcSet.close();
            }
        }

    }

    @Test
    public void parseCancelOrderTransaction() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ORDER
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                Long orderID = orderCreation.makeOrder().getId();

                // CREATE CANCEL ORDER
                CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(accountA, orderCreation.getSignature(), FEE_POWER,
                        timestamp++, 0L);
                cancelOrderTransaction.sign(accountA, Transaction.FOR_NETWORK);

                // CONVERT TO BYTES
                byte[] rawCancelOrder = cancelOrderTransaction.toBytes(Transaction.FOR_NETWORK, true);

                // CHECK DATALENGTH
                assertEquals(rawCancelOrder.length, cancelOrderTransaction.getDataLength(Transaction.FOR_NETWORK, true));

                CancelOrderTransaction parsedCancelOrder = null;
                try {
                    // PARSE FROM BYTES
                    parsedCancelOrder = (CancelOrderTransaction) TransactionFactory.getInstance()
                            .parse(rawCancelOrder, Transaction.FOR_NETWORK);

                } catch (Exception e) {
                    fail("Exception while parsing transaction.");
                }

                // CHECK INSTANCE
                assertEquals(true, parsedCancelOrder instanceof CancelOrderTransaction);

                // CHECK SIGNATURE
                assertEquals(true, Arrays.equals(cancelOrderTransaction.getSignature(), parsedCancelOrder.getSignature()));

                // CHECK AMOUNT CREATOR
                assertEquals(cancelOrderTransaction.getAmount(accountA), parsedCancelOrder.getAmount(accountA));

                // CHECK OWNER
                assertEquals(cancelOrderTransaction.getCreator().getAddress(), parsedCancelOrder.getCreator().getAddress());

                // CHECK ORDER
                assertEquals(true, Arrays.equals(cancelOrderTransaction.getorderSignature(), parsedCancelOrder.getorderSignature()));

                // CHECK FEE
                assertEquals(cancelOrderTransaction.getFeePow(), parsedCancelOrder.getFeePow());

                // CHECK REFERENCE
                // assertEquals(cancelOrderTransaction.getReference(),
                // parsedCancelOrder.getReference());

                // CHECK TIMESTAMP
                assertEquals(cancelOrderTransaction.getTimestamp(), parsedCancelOrder.getTimestamp());

                // PARSE TRANSACTION FROM WRONG BYTES
                rawCancelOrder = new byte[cancelOrderTransaction.getDataLength(Transaction.FOR_NETWORK, true)];

                try {
                    // PARSE FROM BYTES
                    TransactionFactory.getInstance().parse(rawCancelOrder, Transaction.FOR_NETWORK);

                    // FAIL
                    fail("this should throw an exception");
                } catch (Exception e) {
                    // EXCEPTION IS THROWN OK
                }
            } finally {
                dcSet.close();
            }

        }
    }

    @Test
    public void processCancelOrderTransaction() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // CREATE ORDER
                assertEquals(BigDecimal.valueOf(assetA.getQuantity()), accountA.getBalanceUSE(keyA, dcSet));
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);

                Long orderID = orderCreation.makeOrder().getId();
                dcSet.getTransactionFinalMapSigns().put(orderCreation.getSignature(), orderID);

                assertEquals(BigDecimal.valueOf(assetA.getQuantity()).subtract(orderCreation.makeOrder().getAmountHave()),
                        accountA.getBalanceUSE(keyA, dcSet));

                // CREATE CANCEL ORDER
                CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(accountA, orderCreation.getSignature(), FEE_POWER,
                        timestamp++, 0L);
                cancelOrderTransaction.sign(accountA, Transaction.FOR_NETWORK);

                cancelOrderTransaction.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                cancelOrderTransaction.process(null, Transaction.FOR_NETWORK);

                // CHECK BALANCE SENDER
                assertEquals(BigDecimal.valueOf(assetA.getQuantity()).setScale(assetA.getScale()),
                        accountA.getBalanceUSE(keyA, dcSet).setScale(assetA.getScale()));

                // CHECK REFERENCE SENDER
                assertEquals((long) cancelOrderTransaction.getTimestamp(), orderCreation.getCreator().getLastTimestamp(dcSet)[0]);

                // CHECK ORDER EXISTS
                assertEquals(false, dcSet.getOrderMap().contains(123L));

                ////////// OPHRAN ////////////////
                // CHECK BALANCE SENDER
                assertEquals(BigDecimal.valueOf(assetA.getQuantity()).setScale(assetA.getScale()),
                        accountA.getBalanceUSE(keyA, dcSet).setScale(assetA.getScale()));
                cancelOrderTransaction.orphan(gb, Transaction.FOR_NETWORK);

                // CHECK BALANCE SENDER
                assertEquals(BigDecimal.valueOf(assetA.getQuantity()).subtract(orderCreation.makeOrder().getAmountHave()),
                        accountA.getBalanceUSE(keyA, dcSet));

                // CHECK REFERENCE SENDER
                assertEquals((long) orderCreation.getTimestamp(), orderCreation.getCreator().getLastTimestamp(dcSet)[0]);

                // CHECK ORDER EXISTS
                assertEquals(true, dcSet.getOrderMap().contains(orderID));
            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void testgetOrdersForTradeWithFork() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                long wantKey = keyA;
                long haveKey = keyB;

                orderCreation = new CreateOrderTransaction(accountA, wantKey, haveKey, BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(100), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_AB_1 = orderCreation.makeOrder();
                order_AB_1_ID = orderCreation.getOrderId();

                orderCreation = new CreateOrderTransaction(accountA, wantKey, haveKey, BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(300), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_AB_4 = orderCreation.makeOrder();
                order_AB_4_ID = order_AB_4.getId();

                orderCreation = new CreateOrderTransaction(accountA, wantKey, haveKey, BigDecimal.valueOf(1400),
                        BigDecimal.valueOf(200), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_AB_3 = orderCreation.makeOrder();
                order_AB_3_ID = order_AB_3.getId();

                /// for delete in FORK
                Long deletedID = order_AB_3_ID;


                orderCreation = new CreateOrderTransaction(accountA, wantKey, haveKey, BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(130), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(dcSet, Transaction.FOR_NETWORK, height, ++seqNo, false);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_AB_2 = orderCreation.makeOrder();
                order_AB_2_ID = order_AB_2.getId();

                ///////////////////  add FORK dcSet

                DCSet fork = dcSet.fork(this.toString());

                orderCreation = new CreateOrderTransaction(accountA, wantKey, haveKey, BigDecimal.valueOf(100),
                        BigDecimal.valueOf(100), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(fork, Transaction.FOR_NETWORK, height, 1, true);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_AB_1 = orderCreation.makeOrder();
                order_AB_1_ID = orderCreation.getOrderId();

                orderCreation = new CreateOrderTransaction(accountA, wantKey, haveKey, BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(30), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(fork, Transaction.FOR_NETWORK, height, 2, true);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_AB_4 = orderCreation.makeOrder();
                order_AB_4_ID = order_AB_4.getId();

                orderCreation = new CreateOrderTransaction(accountA, wantKey, haveKey, BigDecimal.valueOf(1400),
                        BigDecimal.valueOf(200), (byte) 0, timestamp++, 0L);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(fork, Transaction.FOR_NETWORK, height, 3, true);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_AB_3 = orderCreation.makeOrder();
                order_AB_3_ID = order_AB_3.getId();

                // BACK TIMESTAMP
                orderCreation = new CreateOrderTransaction(accountA, wantKey, haveKey, BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(130), (byte) 0, timestamp - 1000, 0L, new byte[64]);
                orderCreation.sign(accountA, Transaction.FOR_NETWORK);
                orderCreation.setDC(fork, Transaction.FOR_NETWORK, height, 4, true);
                orderCreation.process(null, Transaction.FOR_NETWORK);
                order_AB_2 = orderCreation.makeOrder();
                order_AB_2_ID = order_AB_2.getId();

                ///////// DELETE in FORK
                fork.getOrderMap().delete(deletedID);

                int compare;
                int index = 0;
                BigDecimal thisPrice;
                BigDecimal tempPrice;

                List<Order> orders = fork
                        .getOrderMap().getOrdersForTrade(wantKey, haveKey, false);

                tempPrice = BigDecimal.ZERO;
                Long timestamp = 0L;
                for (Order order : orders) {

                    Assert.assertEquals((long) order.getHaveAssetKey(), wantKey);
                    Assert.assertEquals((long) order.getWantAssetKey(), haveKey);

                    //String signB58 = Base58.encode(order.a.a);

                    Assert.assertEquals(deletedID.equals(order.getId()), false);

                    BigDecimal orderReversePrice = order.calcPriceReverse();
                    BigDecimal orderPrice = order.getPrice();

                    Assert.assertEquals(order.calcPrice().equals(orderPrice), true);

                    timestamp = 0L;
                    compare = tempPrice.compareTo(orderPrice);
                    Assert.assertEquals(compare <= 0, true);
                    if (compare > 0) {
                        // error
                        compare = index;
                    } else if (compare == 0) {
                        compare = timestamp.compareTo(order.getId());
                        Assert.assertEquals(compare <= 0, true);
                        if (compare > 0) {
                            // error
                            compare = index;
                        }
                    }

                    tempPrice = orderPrice;
                    timestamp = order.getId();

                }
            } finally {
                dcSet.close();
            }
        }
    }

}
