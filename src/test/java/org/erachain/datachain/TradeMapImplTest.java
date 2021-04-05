package org.erachain.datachain;

import com.google.common.collect.Iterators;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.IssueAssetTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.IDB;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Test;
import org.mapdb.Fun;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Slf4j
public class TradeMapImplTest {

    int[] TESTED_DBS = new int[]{
            IDB.DBS_MAP_DB,
            IDB.DBS_ROCK_DB};

    Long releaserReference = null;
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();

    Random random = new Random();

    byte[] itemAppData = null;
    long txFlags = 0L;

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

        File tempDir = new File(Settings.getInstance().getDataTempDir());
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        dcSet = DCSet.createEmptyHardDatabaseSetWithFlush(null, dbs);
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
        accountA.changeBalance(dcSet, false, false, ERM_KEY, BigDecimal.valueOf(100), false, false, false);
        accountA.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(10), false, false, false);

        accountB.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, dcSet);
        accountB.changeBalance(dcSet, false, false, ERM_KEY, BigDecimal.valueOf(100), false, false, false);
        accountB.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(10), false, false, false);

        assetA = new AssetVenture(itemAppData, new GenesisBlock().getCreator(), "AAA", icon, image, ".", 0, 8, 50000L);

        issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte) 0, timestamp++, 0l, new byte[64]);
        issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, 2, ++seqNo, true);
        issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

        keyA = issueAssetTransaction.getAssetKey(dcSet);
        balanceA = accountA.getBalance(dcSet, keyA);

        assetB = new AssetVenture(itemAppData, new GenesisBlock().getCreator(), "BBB", icon, image, ".", 0, 8, 50000L);
        issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte) 0, timestamp++,
                0L, new byte[64]);
        issueAssetTransaction.setDC(dcSet, Transaction.FOR_NETWORK, 2, ++seqNo, true);
        issueAssetTransaction.process(null, Transaction.FOR_NETWORK);
        keyB = issueAssetTransaction.getAssetKey(dcSet);

        // CREATE ORDER TRANSACTION
        orderCreation = new CreateOrderTransaction(accountA, keyA, 3l, BigDecimal.valueOf(10), BigDecimal.valueOf(100),
                (byte) 0, timestamp, 0l);

    }

    @Test
    public void getIteratorInForkByOrder() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                IteratorCloseable<Fun.Tuple2<Long, Long>> iterator;
                int index = 1;

                TradeMap tradesMap = dcSet.getTradeMap();
                long haveKey = 2L;
                long wantKey = 1L;

                int start = 444;
                int stop = 433;

                long initiatorID = Transaction.makeDBRef(start, 3);

                long targetID = Transaction.makeDBRef(10, 1);
                Trade trade = new Trade(initiatorID, targetID, haveKey, wantKey,
                        new BigDecimal("22"), new BigDecimal("44"),
                        3, 5, index++);
                tradesMap.put(trade);

                trade = new Trade(Transaction.makeDBRef(start - 1, 3), targetID, haveKey, wantKey,
                        new BigDecimal("22"), new BigDecimal("44"),
                        3, 5, index++);
                tradesMap.put(trade);

                trade = new Trade(Transaction.makeDBRef(stop + 1, 4), targetID, haveKey, wantKey,
                        new BigDecimal("22"), new BigDecimal("44"),
                        3, 5, index++);
                tradesMap.put(trade);

                trade = new Trade(Transaction.makeDBRef(stop, 4), targetID, haveKey, wantKey,
                        new BigDecimal("22"), new BigDecimal("44"),
                        3, 5, index++);
                tradesMap.put(trade);

                DCSet forked = dcSet.fork(this.toString());
                TradeMapImpl forkedTradesMap = forked.getTradeMap();

                iterator = forkedTradesMap.getIteratorByInitiator(initiatorID);
                assertEquals(1, Iterators.size(iterator));

                // ADD to FORK
                trade = new Trade(initiatorID, Transaction.makeDBRef(stop, 4), haveKey, wantKey,
                        new BigDecimal("25"), new BigDecimal("44"),
                        3, 5, index++);
                forkedTradesMap.put(trade);

                iterator = forkedTradesMap.getIteratorByInitiator(initiatorID);
                assertEquals(2, Iterators.size(iterator));

                // DELETE in FORK
                Trade removed = forkedTradesMap.remove(new Fun.Tuple2<Long, Long>(initiatorID, targetID));
                assertNotEquals(null, removed);
                iterator = forkedTradesMap.getIteratorByInitiator(initiatorID);
                assertEquals(1, Iterators.size(iterator));


            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void getTrades() {
    }

    @Test
    public void getLastTrade() {
    }

    @Test
    public void getTradesByTimestamp() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                Iterator<Long> iterator;
                int index = 1;

                TradeMap tradesMap = dcSet.getTradeMap();
                long haveKey = 2L;
                long wantKey = 1L;

                int start = 444;
                int stop = 433;

                long targetID = Transaction.makeDBRef(10, 1);
                Trade trade = new Trade(Transaction.makeDBRef(start, 3), targetID, haveKey, wantKey,
                        new BigDecimal("22"), new BigDecimal("44"),
                        3, 5, index++);
                tradesMap.put(trade);

                trade = new Trade(Transaction.makeDBRef(start - 1, 3), targetID, haveKey, wantKey,
                        new BigDecimal("22"), new BigDecimal("44"),
                        3, 5, index++);
                tradesMap.put(trade);

                trade = new Trade(Transaction.makeDBRef(stop + 1, 4), targetID, haveKey, wantKey,
                        new BigDecimal("22"), new BigDecimal("44"),
                        3, 5, index++);
                tradesMap.put(trade);

                trade = new Trade(Transaction.makeDBRef(stop, 4), targetID, haveKey, wantKey,
                        new BigDecimal("22"), new BigDecimal("44"),
                        3, 5, index++);
                tradesMap.put(trade);

                assertEquals(4, tradesMap.getTradesByHeight(haveKey, wantKey, 0, 0, 0).size());
                assertEquals(4, tradesMap.getTradesByOrderID(haveKey, wantKey, 0, 0, 0).size());

                assertEquals(4, tradesMap.getTradesByHeight(haveKey, wantKey, start + 1, stop - 1, 0).size());
                assertEquals(4, tradesMap.getTradesByOrderID(haveKey, wantKey, Transaction.makeDBRef(start + 1, Integer.MAX_VALUE), Transaction.makeDBRef(stop - 1, 0), 0).size());

                assertEquals(4, tradesMap.getTradesByHeight(haveKey, wantKey, 0, stop, 0).size());
                assertEquals(3, tradesMap.getTradesByOrderID(haveKey, wantKey, 0, Transaction.makeDBRef(stop, 5), 0).size());
                assertEquals(4, tradesMap.getTradesByOrderID(haveKey, wantKey, 0, Transaction.makeDBRef(stop, 4), 0).size());
                assertEquals(4, tradesMap.getTradesByOrderID(haveKey, wantKey, 0, Transaction.makeDBRef(stop, 3), 0).size());

                assertEquals(0, tradesMap.getTradesByOrderID(haveKey, wantKey, Transaction.makeDBRef(stop + 1, 0), Transaction.makeDBRef(stop, 5), 0).size());
                assertEquals(1, tradesMap.getTradesByOrderID(haveKey, wantKey, Transaction.makeDBRef(stop + 1, 0), Transaction.makeDBRef(stop, 4), 0).size());
                assertEquals(1, tradesMap.getTradesByOrderID(haveKey, wantKey, Transaction.makeDBRef(stop + 1, 0), Transaction.makeDBRef(stop, 3), 0).size());

                assertEquals(4, tradesMap.getTradesByHeight(haveKey, wantKey, start, 0, 0).size());
                assertEquals(4, tradesMap.getTradesByOrderID(haveKey, wantKey, Transaction.makeDBRef(start, 4), 0, 0).size());
                assertEquals(4, tradesMap.getTradesByOrderID(haveKey, wantKey, Transaction.makeDBRef(start, 3), 0, 0).size());
                assertEquals(3, tradesMap.getTradesByOrderID(haveKey, wantKey, Transaction.makeDBRef(start, 2), 0, 0).size());

                assertEquals(1, tradesMap.getTradesByOrderID(haveKey, wantKey, Transaction.makeDBRef(start, 4), Transaction.makeDBRef(start - 1, 99), 0).size());
                assertEquals(1, tradesMap.getTradesByOrderID(haveKey, wantKey, Transaction.makeDBRef(start, 3), Transaction.makeDBRef(start - 1, 99), 0).size());
                assertEquals(0, tradesMap.getTradesByOrderID(haveKey, wantKey, Transaction.makeDBRef(start, 2), Transaction.makeDBRef(start - 1, 99), 0).size());

                assertEquals(2, tradesMap.getTradesByHeight(haveKey, wantKey, stop + 1, stop, 0).size());

                assertEquals(2, tradesMap.getTradesByHeight(haveKey, wantKey, start, start - 1, 0).size());

                assertEquals(4, tradesMap.getTradesByHeight(haveKey, wantKey, start, stop, 0).size());

                assertEquals(3, tradesMap.getTradesByHeight(haveKey, wantKey, start, stop + 1, 0).size());

                assertEquals(3, tradesMap.getTradesByHeight(haveKey, wantKey, start - 1, stop, 0).size());


            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void getVolume24() {
    }
}