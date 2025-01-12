package org.erachain.dapp.epoch;

import junit.framework.TestCase;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.BlockGenerator;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.erachain.core.item.assets.AssetTypes.AS_INSIDE_ASSETS;
import static org.erachain.core.transaction.Transaction.FEE_KEY;

public class MoneyStakingTest extends TestCase {

    Controller cntrl;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test123".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    PrivateKeyAccount privAcc1 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, "1"));
    PrivateKeyAccount privAcc2 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, "2"));
    PrivateKeyAccount privAcc3 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, "3"));
    AssetCls asset;
    AssetCls assetMovable;

    long assetKey = 2L;
    BigDecimal amount = new BigDecimal("1000");
    RSend rSendTx;
    byte[] isText = new byte[]{1};
    byte[] encryptMessage = new byte[]{0};
    long timestamp;
    int height;
    byte[] itemAppData = null;

    DCSet dcSet;
    GenesisBlock gb;
    private BlockChain bchain;
    BlockGenerator blockGenerator;
    int forDeal = Transaction.FOR_NETWORK;
    long dbRef = 0L;
    int seqNo = 0;
    Block block;
    Block block2;
    List<Transaction> txs = new ArrayList<>();
    Fun.Tuple2<List<Transaction>, Integer> orderedTransactions;
    JSONObject pars = new JSONObject();
    String parsStr;

    private byte[] icon = null;
    private byte[] image = null;
    MoneyStaking moneyStaking = new MoneyStaking();

    private void init() throws Exception {

        dcSet = DCSet.createEmptyDatabaseSet(IDB.DBS_MAP_DB);
        cntrl = Controller.getInstance();
        timestamp = NTP.getTime();
        cntrl.initBlockChain(dcSet);
        bchain = cntrl.getBlockChain();
        BlockChain.ALL_VALID_BEFORE = 0;
        gb = bchain.getGenesisBlock();

        // FEE FUND
        maker.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, dcSet);
        maker.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        privAcc1.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, dcSet);

        asset = new AssetVenture(itemAppData, maker, "aasdasd", icon, image, "asdasda", AS_INSIDE_ASSETS, 2, 50000L);
        // set SCALABLE assets ++
        asset.setReference(Crypto.getInstance().digest(asset.toBytes(forDeal, false, false)), dbRef);
        asset.insertToMap(dcSet, BlockChain.AMOUNT_SCALE_FROM);
        asset.insertToMap(dcSet, 0L);
        assetKey = asset.getKey();

        height = 5;
        rSendTx = new RSend(maker, null, null, (byte) 0, privAcc1, assetKey,
                amount, "TEST", null, isText, encryptMessage, timestamp++, 0L);
        rSendTx.sign(maker, Transaction.FOR_NETWORK);
        seqNo = 1;
        rSendTx.setHeightSeq(height, seqNo);
        txs.add(rSendTx);
        orderedTransactions = new Fun.Tuple2<>(txs, seqNo);

        blockGenerator = new BlockGenerator(dcSet, bchain, false);
        block = blockGenerator.generateNextBlock(maker, gb, orderedTransactions,
                1000, 1000L, 1000L);
        block.sign(maker);
        // Исполним перевод
        block.process(dcSet, true);

        pars.put("dApp", 1012);
        pars.put("%", "12");
        parsStr = pars.toString();

    }

    public void testToBytes() throws Exception {
        init();

        MoneyStaking dApp = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx, block);
        byte[] data = dApp.toBytes(Transaction.FOR_DB_RECORD);
        assertEquals(dApp.length(Transaction.FOR_DB_RECORD), data.length);
        MoneyStaking parsedDApp = MoneyStaking.Parse(data, 0, Transaction.FOR_DB_RECORD);
        assertEquals(parsedDApp.itemDescription, dApp.itemDescription);
    }

    public void testProcess() throws Exception {
        init();

        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        MoneyStaking dApp = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx, block);
        dApp.process();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000"), new BigDecimal("1000")));
        Object[][][] statePoints = (Object[][][]) dApp.peekState(rSendTx.getDBRef());
        assertEquals(statePoints.length, 1);
        assertEquals(statePoints[0].length, 1);
        assertEquals(dApp.valueGet(maker.getAddress()), null);
        Object[] stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], BigDecimal.ZERO);
        assertEquals(stateAcc1[1], 1487845369333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));

        Transaction rSendTx1 = rSendTx.copy();
        height += 30;
        rSendTx1.setDC(dcSet);
        rSendTx1.setHeightSeq(height, 1);
        MoneyStaking dApp1 = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx1, block);
        dApp1.process();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000"), new BigDecimal("1000")));
        Object[][][] statePoints1 = (Object[][][]) dApp1.peekState(rSendTx1.getDBRef());
        assertEquals(statePoints1.length, 1);
        assertEquals(statePoints1[0].length, 1);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], new BigDecimal("0.03421"));
        assertEquals(stateAcc1[1], 1487854873333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));

        if (true) {

            Transaction rSendTx2 = new RSend(privAcc1, null, null, (byte) 0, privAcc2, assetKey,
                    amount, "TEST", null, isText, encryptMessage, timestamp++, 0L);
            height += 700;
            rSendTx2.setHeightSeq(height, 1);
            rSendTx2.setDC(dcSet);
            MoneyStaking dApp2 = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx2, block);
            dApp2.process();
            assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("-0.75"), new BigDecimal("-1000.75")));
            assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000.75"), new BigDecimal("1000.75")));
            assertEquals(privAcc2.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
            Object[][][] statePoints2 = (Object[][][]) dApp.peekState(rSendTx2.getDBRef());
            assertEquals(statePoints2[0].length, 3);
            stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
            assertEquals(stateAcc1[0], new BigDecimal("0"));
            assertEquals(stateAcc1[1], 1488056473333L);
            assertEquals(stateAcc1[2], new BigDecimal("1000"));
            Object[] stateAcc2 = (Object[]) dApp.valueGet(privAcc2.getAddress());
            assertEquals(stateAcc2[0], new BigDecimal("0"));
            assertEquals(stateAcc2[1], 1488056473333L);
            assertEquals(stateAcc2[2], new BigDecimal("0"));


            Transaction rSendTx3 = new RSend(privAcc1, null, null, (byte) 0, privAcc2, assetKey,
                    amount, "TEST", null, isText, encryptMessage, timestamp++, 0L);
            height += 700;
            rSendTx3.setHeightSeq(height, 1);
            rSendTx3.setDC(dcSet);
            MoneyStaking dApp3 = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx3, block);
            dApp3.process();
            assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("-1.47"), new BigDecimal("-1001.47")));
            assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1001.47"), new BigDecimal("1001.47")));
            assertEquals(privAcc2.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
            Object[][][] statePoints3 = (Object[][][]) dApp.peekState(rSendTx3.getDBRef());
            assertEquals(statePoints3[0].length, 3);
            stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
            assertEquals(stateAcc1[0], new BigDecimal("0"));
            assertEquals(stateAcc1[1], 1488258073333L);
            assertEquals(stateAcc1[2], new BigDecimal("1000.75"));
            stateAcc2 = (Object[]) dApp.valueGet(privAcc2.getAddress());
            assertEquals(stateAcc2[0], new BigDecimal("0"));
            assertEquals(stateAcc2[1], 1488258073333L);
            assertEquals(stateAcc2[2], new BigDecimal("0"));

            dApp3.orphan();
            assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("-0.75"), new BigDecimal("-1000.75")));
            assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000.75"), new BigDecimal("1000.75")));
            assertEquals(privAcc2.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
            stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
            assertEquals(stateAcc1[0], new BigDecimal("0"));
            assertEquals(stateAcc1[1], 1488056473333L);
            assertEquals(stateAcc1[2], new BigDecimal("1000"));
            stateAcc2 = (Object[]) dApp.valueGet(privAcc2.getAddress());
            assertEquals(stateAcc2[0], new BigDecimal("0"));
            assertEquals(stateAcc2[1], 1488056473333L);
            assertEquals(stateAcc2[2], new BigDecimal("0"));

            dApp2.orphan();
            assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
            assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000"), new BigDecimal("1000")));
            stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
            assertEquals(stateAcc1[0], new BigDecimal("0.03421"));
            assertEquals(stateAcc1[1], 1487854873333L);
            assertEquals(stateAcc1[2], new BigDecimal("1000"));
            assertEquals(dApp.valueGet(privAcc2.getAddress()), null);
        }

        dApp1.orphan();
        assertEquals(dApp.valueGet(maker.getAddress()), null);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], BigDecimal.ZERO);
        assertEquals(stateAcc1[1], 1487845369333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));
        assertEquals(dApp.valueGet(privAcc2.getAddress()), null);

        dApp.orphan();
        assertEquals(dApp.valueGet(maker.getAddress()), null);
        assertEquals(dApp.valueGet(privAcc1.getAddress()), null);
        assertEquals(dApp.valueGet(privAcc2.getAddress()), null);

    }

    //@Test
    public void testProcessDemerrage() throws Exception {
        init();

        pars.put("dApp", 1012);
        pars.put("%", "-12");
        parsStr = pars.toString();

        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        MoneyStaking dApp = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx, block);
        dApp.process();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000"), new BigDecimal("1000")));
        Object[][][] statePoints = (Object[][][]) dApp.peekState(rSendTx.getDBRef());
        assertEquals(statePoints.length, 1);
        assertEquals(statePoints[0].length, 1);
        assertEquals(dApp.valueGet(maker.getAddress()), null);
        Object[] stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], BigDecimal.ZERO);
        assertEquals(stateAcc1[1], 1487845369333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));

        Transaction rSendTx1 = rSendTx.copy();
        height += 30;
        rSendTx1.setDC(dcSet);
        rSendTx1.setHeightSeq(height, 1);
        MoneyStaking dApp1 = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx1, block);
        dApp1.process();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000"), new BigDecimal("1000")));
        Object[][][] statePoints1 = (Object[][][]) dApp1.peekState(rSendTx1.getDBRef());
        assertEquals(statePoints1.length, 1);
        assertEquals(statePoints1[0].length, 1);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], new BigDecimal("-0.03422"));
        assertEquals(stateAcc1[1], 1487854873333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));

        if (true) {

            Transaction rSendTx2 = new RSend(privAcc1, null, null, (byte) 0, privAcc2, assetKey,
                    amount, "TEST", null, isText, encryptMessage, timestamp++, 0L);
            height += 700;
            rSendTx2.setHeightSeq(height, 1);
            rSendTx2.setDC(dcSet);
            MoneyStaking dApp2 = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx2, block);
            dApp2.process();
            assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0.76"), new BigDecimal("-999.24")));
            assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("999.24"), new BigDecimal("999.24")));
            assertEquals(privAcc2.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
            Object[][][] statePoints2 = (Object[][][]) dApp.peekState(rSendTx2.getDBRef());
            assertEquals(statePoints2[0].length, 3);
            stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
            assertEquals(stateAcc1[0], new BigDecimal("0"));
            assertEquals(stateAcc1[1], 1488056473333L);
            assertEquals(stateAcc1[2], new BigDecimal("1000"));
            Object[] stateAcc2 = (Object[]) dApp.valueGet(privAcc2.getAddress());
            assertEquals(stateAcc2[0], new BigDecimal("0"));
            assertEquals(stateAcc2[1], 1488056473333L);
            assertEquals(stateAcc2[2], new BigDecimal("0"));


            Transaction rSendTx3 = new RSend(privAcc1, null, null, (byte) 0, privAcc2, assetKey,
                    amount, "TEST", null, isText, encryptMessage, timestamp++, 0L);
            height += 700;
            rSendTx3.setHeightSeq(height, 1);
            rSendTx3.setDC(dcSet);
            MoneyStaking dApp3 = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx3, block);
            dApp3.process();
            assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1.49"), new BigDecimal("-998.51")));
            assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("998.51"), new BigDecimal("998.51")));
            assertEquals(privAcc2.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
            Object[][][] statePoints3 = (Object[][][]) dApp.peekState(rSendTx3.getDBRef());
            assertEquals(statePoints3[0].length, 3);
            stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
            assertEquals(stateAcc1[0], new BigDecimal("0"));
            assertEquals(stateAcc1[1], 1488258073333L);
            assertEquals(stateAcc1[2], new BigDecimal("999.24"));
            stateAcc2 = (Object[]) dApp.valueGet(privAcc2.getAddress());
            assertEquals(stateAcc2[0], new BigDecimal("0"));
            assertEquals(stateAcc2[1], 1488258073333L);
            assertEquals(stateAcc2[2], new BigDecimal("0"));

            dApp3.orphan();
            assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0.76"), new BigDecimal("-999.24")));
            assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("999.24"), new BigDecimal("999.24")));
            assertEquals(privAcc2.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
            stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
            assertEquals(stateAcc1[0], new BigDecimal("0"));
            assertEquals(stateAcc1[1], 1488056473333L);
            assertEquals(stateAcc1[2], new BigDecimal("1000"));
            stateAcc2 = (Object[]) dApp.valueGet(privAcc2.getAddress());
            assertEquals(stateAcc2[0], new BigDecimal("0"));
            assertEquals(stateAcc2[1], 1488056473333L);
            assertEquals(stateAcc2[2], new BigDecimal("0"));

            dApp2.orphan();
            assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
            assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000"), new BigDecimal("1000")));
            stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
            assertEquals(stateAcc1[0], new BigDecimal("-0.03422"));
            assertEquals(stateAcc1[1], 1487854873333L);
            assertEquals(stateAcc1[2], new BigDecimal("1000"));
            assertEquals(dApp.valueGet(privAcc2.getAddress()), null);
        }

        dApp1.orphan();
        assertEquals(dApp.valueGet(maker.getAddress()), null);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], BigDecimal.ZERO);
        assertEquals(stateAcc1[1], 1487845369333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));
        assertEquals(dApp.valueGet(privAcc2.getAddress()), null);

        dApp.orphan();
        assertEquals(dApp.valueGet(maker.getAddress()), null);
        assertEquals(dApp.valueGet(privAcc1.getAddress()), null);
        assertEquals(dApp.valueGet(privAcc2.getAddress()), null);

    }

    public void testProcessZero() throws Exception {
        init();

        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        MoneyStaking dApp = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx, block);
        dApp.process();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000"), new BigDecimal("1000")));
        Object[][][] statePoints = (Object[][][]) dApp.peekState(rSendTx.getDBRef());
        assertEquals(statePoints.length, 1);
        assertEquals(statePoints[0].length, 1);
        assertEquals(dApp.valueGet(maker.getAddress()), null);
        Object[] stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], BigDecimal.ZERO);
        assertEquals(stateAcc1[1], 1487845369333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));

        Transaction rSendTx1 = rSendTx.copy();
        height += 30;
        rSendTx1.setDC(dcSet);
        rSendTx1.setHeightSeq(height, 1);
        MoneyStaking dApp1 = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx1, block);
        dApp1.process();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000"), new BigDecimal("1000")));
        Object[][][] statePoints1 = (Object[][][]) dApp1.peekState(rSendTx1.getDBRef());
        assertEquals(statePoints1.length, 1);
        assertEquals(statePoints1[0].length, 1);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], new BigDecimal("0.03421"));
        assertEquals(stateAcc1[1], 1487854873333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));

        Transaction rSendTx2 = new RSend(privAcc1, null, null, (byte) 0, privAcc2, assetKey,
                amount, "TEST", null, isText, encryptMessage, timestamp++, 0L);
        height += 700;
        rSendTx2.setHeightSeq(height, 1);
        rSendTx2.setDC(dcSet);
        MoneyStaking dApp2 = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx2, block);
        dApp2.process();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("-0.75"), new BigDecimal("-1000.75")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000.75"), new BigDecimal("1000.75")));
        assertEquals(privAcc2.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
        Object[][][] statePoints2 = (Object[][][]) dApp.peekState(rSendTx2.getDBRef());
        assertEquals(statePoints2[0].length, 3);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], new BigDecimal("0"));
        assertEquals(stateAcc1[1], 1488056473333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));
        Object[] stateAcc2 = (Object[]) dApp.valueGet(privAcc2.getAddress());
        assertEquals(stateAcc2[0], new BigDecimal("0"));
        assertEquals(stateAcc2[1], 1488056473333L);
        assertEquals(stateAcc2[2], new BigDecimal("0"));


        // обнулим баланс отправителя и тогда все что у него есть должно перейти к получателю
        dcSet.getAssetBalanceMap().put(privAcc1.getShortAddressBytes(), assetKey,
                Account.makeBalanceOWN(dcSet.getAssetBalanceMap().get(privAcc1.getShortAddressBytes(), assetKey),
                        new Object[]{BigDecimal.ZERO, BigDecimal.ZERO}));

        Transaction rSendTx3 = new RSend(privAcc1, null, null, (byte) 0, privAcc2, assetKey,
                amount, "TEST", null, isText, encryptMessage, timestamp++, 0L);
        height += 700;
        rSendTx3.setHeightSeq(height, 1);
        rSendTx3.setDC(dcSet);
        MoneyStaking dApp3 = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx3, block);
        dApp3.process();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("-0.75"), new BigDecimal("-1000.75")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
        assertEquals(privAcc2.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
        Object[][][] statePoints3 = (Object[][][]) dApp.peekState(rSendTx3.getDBRef());
        assertEquals(statePoints3[0].length, 3);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], new BigDecimal("0"));
        assertEquals(stateAcc1[1], 1488258073333L);
        assertEquals(stateAcc1[2], new BigDecimal("0"));
        stateAcc2 = (Object[]) dApp.valueGet(privAcc2.getAddress());
        assertEquals(stateAcc2[0], new BigDecimal("0"));
        assertEquals(stateAcc2[1], 1488258073333L);
        assertEquals(stateAcc2[2], new BigDecimal("0"));

        dApp3.orphan();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("-0.75"), new BigDecimal("-1000.75")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
        assertEquals(privAcc2.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], new BigDecimal("0"));
        assertEquals(stateAcc1[1], 1488056473333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));
        stateAcc2 = (Object[]) dApp.valueGet(privAcc2.getAddress());
        assertEquals(stateAcc2[0], new BigDecimal("0"));
        assertEquals(stateAcc2[1], 1488056473333L);
        assertEquals(stateAcc2[2], new BigDecimal("0"));

        dApp2.orphan();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000"), new BigDecimal("1000")));
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], new BigDecimal("0.03421"));
        assertEquals(stateAcc1[1], 1487854873333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));
        assertEquals(dApp.valueGet(privAcc2.getAddress()), null);

        dApp1.orphan();
        assertEquals(dApp.valueGet(maker.getAddress()), null);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], BigDecimal.ZERO);
        assertEquals(stateAcc1[1], 1487845369333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));
        assertEquals(dApp.valueGet(privAcc2.getAddress()), null);

        dApp.orphan();
        assertEquals(dApp.valueGet(maker.getAddress()), null);
        assertEquals(dApp.valueGet(privAcc1.getAddress()), null);
        assertEquals(dApp.valueGet(privAcc2.getAddress()), null);

    }

    public void testProcessDemerrageZero() throws Exception {
        init();

        pars.put("dApp", 1012);
        pars.put("%", "-12");
        parsStr = pars.toString();

        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        MoneyStaking dApp = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx, block);
        dApp.process();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000"), new BigDecimal("1000")));
        Object[][][] statePoints = (Object[][][]) dApp.peekState(rSendTx.getDBRef());
        assertEquals(statePoints.length, 1);
        assertEquals(statePoints[0].length, 1);
        assertEquals(dApp.valueGet(maker.getAddress()), null);
        Object[] stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], BigDecimal.ZERO);
        assertEquals(stateAcc1[1], 1487845369333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));

        Transaction rSendTx1 = rSendTx.copy();
        height += 30;
        rSendTx1.setDC(dcSet);
        rSendTx1.setHeightSeq(height, 1);
        MoneyStaking dApp1 = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx1, block);
        dApp1.process();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000"), new BigDecimal("1000")));
        Object[][][] statePoints1 = (Object[][][]) dApp1.peekState(rSendTx1.getDBRef());
        assertEquals(statePoints1.length, 1);
        assertEquals(statePoints1[0].length, 1);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], new BigDecimal("-0.03422"));
        assertEquals(stateAcc1[1], 1487854873333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));

        Transaction rSendTx2 = new RSend(privAcc1, null, null, (byte) 0, privAcc2, assetKey,
                amount, "TEST", null, isText, encryptMessage, timestamp++, 0L);
        height += 700;
        rSendTx2.setHeightSeq(height, 1);
        rSendTx2.setDC(dcSet);
        MoneyStaking dApp2 = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx2, block);
        dApp2.process();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0.76"), new BigDecimal("-999.24")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("999.24"), new BigDecimal("999.24")));
        assertEquals(privAcc2.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
        Object[][][] statePoints2 = (Object[][][]) dApp.peekState(rSendTx2.getDBRef());
        assertEquals(statePoints2[0].length, 3);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], new BigDecimal("0"));
        assertEquals(stateAcc1[1], 1488056473333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));
        Object[] stateAcc2 = (Object[]) dApp.valueGet(privAcc2.getAddress());
        assertEquals(stateAcc2[0], new BigDecimal("0"));
        assertEquals(stateAcc2[1], 1488056473333L);
        assertEquals(stateAcc2[2], new BigDecimal("0"));


        // Уменьшим баланс так что демередж будет его превышать
        // должно обнулить счет отправителя и остаток перекинуть на получателя
        dcSet.getAssetBalanceMap().put(privAcc1.getShortAddressBytes(), assetKey,
                Account.makeBalanceOWN(dcSet.getAssetBalanceMap().get(privAcc1.getShortAddressBytes(), assetKey),
                        new Object[]{new BigDecimal("0.21"), new BigDecimal("0.21")}));

        Transaction rSendTx3 = new RSend(privAcc1, null, null, (byte) 0, privAcc2, assetKey,
                amount, "TEST", null, isText, encryptMessage, timestamp++, 0L);
        height += 700;
        rSendTx3.setHeightSeq(height, 1);
        rSendTx3.setDC(dcSet);
        MoneyStaking dApp3 = (MoneyStaking) moneyStaking.of(parsStr, pars, asset, rSendTx3, block);
        dApp3.process();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0.97"), new BigDecimal("-999.03")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0.00"), new BigDecimal("0.00")));
        assertEquals(privAcc2.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
        Object[][][] statePoints3 = (Object[][][]) dApp.peekState(rSendTx3.getDBRef());
        assertEquals(statePoints3[0].length, 4);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], new BigDecimal("0"));
        assertEquals(stateAcc1[1], 1488258073333L);
        assertEquals(stateAcc1[2], new BigDecimal("0.21"));
        stateAcc2 = (Object[]) dApp.valueGet(privAcc2.getAddress());
        assertEquals(stateAcc2[0], new BigDecimal("0"));
        assertEquals(stateAcc2[1], 1488258073333L);
        assertEquals(stateAcc2[2], new BigDecimal("0"));

        dApp3.orphan();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0.76"), new BigDecimal("-999.24")));
        // наш установленный баланс после 3й транзакции
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0.21"), new BigDecimal("0.21")));
        assertEquals(privAcc2.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("0")));
        statePoints2 = (Object[][][]) dApp.peekState(rSendTx2.getDBRef());
        assertEquals(statePoints2[0].length, 3);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], new BigDecimal("0"));
        assertEquals(stateAcc1[1], 1488056473333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));
        stateAcc2 = (Object[]) dApp.valueGet(privAcc2.getAddress());
        assertEquals(stateAcc2[0], new BigDecimal("0"));
        assertEquals(stateAcc2[1], 1488056473333L);
        assertEquals(stateAcc2[2], new BigDecimal("0"));

        dApp2.orphan();
        assertEquals(maker.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("0"), new BigDecimal("-1000")));
        assertEquals(privAcc1.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN), new Fun.Tuple2<>(new BigDecimal("1000"), new BigDecimal("1000")));
        statePoints1 = (Object[][][]) dApp1.peekState(rSendTx1.getDBRef());
        assertEquals(statePoints1.length, 1);
        assertEquals(statePoints1[0].length, 1);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], new BigDecimal("-0.03422"));
        assertEquals(stateAcc1[1], 1487854873333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));

        dApp1.orphan();
        assertEquals(dApp.valueGet(maker.getAddress()), null);
        stateAcc1 = (Object[]) dApp.valueGet(privAcc1.getAddress());
        assertEquals(stateAcc1[0], BigDecimal.ZERO);
        assertEquals(stateAcc1[1], 1487845369333L);
        assertEquals(stateAcc1[2], new BigDecimal("1000"));
        assertEquals(dApp.valueGet(privAcc2.getAddress()), null);

        dApp.orphan();
        assertEquals(dApp.valueGet(maker.getAddress()), null);
        assertEquals(dApp.valueGet(privAcc1.getAddress()), null);
        assertEquals(dApp.valueGet(privAcc2.getAddress()), null);

    }

    public void testOrphanBody() {
    }
}