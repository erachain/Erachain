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
import org.erachain.core.transaction.TransactionFactory;
import org.erachain.core.wallet.Wallet;
import org.erachain.dapp.DApp;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.mapdb.Fun;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.erachain.core.item.assets.AssetTypes.AS_INSIDE_ASSETS;
import static org.erachain.core.transaction.Transaction.FEE_KEY;

public class MoneyStaking_1Test extends TestCase {

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
    byte[] itemAppData = AssetCls.makeAppData(false, 0, false, 0, null, null,
            null, null, false, false, true);

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
    String parsStr = "{\"DApp\":1012, \"%\":\"-7.3\", \"info\":\"Паевой взнос Инновационного Потребительского Общества «ПОЛЬЗА», выраженный в рублях РФ. Взнос является возвращаемым.\"}";

    private byte[] icon = null;
    private byte[] image = null;
    MoneyStaking moneyStaking = new MoneyStaking();

    private void init() throws Exception {

        //dcSet = DCSet.createEmptyDatabaseSet(IDB.DBS_MAP_DB);
        dcSet = DCSet.createEmptyHardDatabaseSet(new File("ERA_TEST/datachainTest", "base.dat"), false, false, IDB.DBS_MAP_DB);
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

        asset = new AssetVenture(itemAppData, maker, "DEMURRAGE", icon, image, parsStr, AS_INSIDE_ASSETS, 2, 0L);
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

    }

    @Test
    public void testToBytes() throws Exception {
        init();

        int forDeal = Transaction.FOR_NETWORK;
        boolean withSignature = true;
        assertNotNull(rSendTx.getDApp());
        // Тут для передачи в Сеть - Эпохальные Смарт-контракты не передаются в байты
        byte[] bytes = rSendTx.toBytes(forDeal, withSignature);
        assertEquals(rSendTx.getDataLength(forDeal, withSignature), bytes.length);
        Transaction parsedTx = TransactionFactory.getInstance().parse(bytes, forDeal);
        assertNull(parsedTx.getDApp());
        assertEquals(parsedTx.getDataLength(forDeal, withSignature), bytes.length);

        // Тут запись в БД - должны в байты конвертироваться
        forDeal = Transaction.FOR_DB_RECORD;
        DApp dApp = rSendTx.getDApp();
        byte[] bytesDApp = dApp.toBytes(forDeal);
        assertEquals(dApp.length(forDeal), bytesDApp.length);
        MoneyStaking parsedDApp = (MoneyStaking) DApp.Parses(bytesDApp, 0, forDeal);
        assertEquals(dApp.getHTML(null), parsedDApp.getHTML(null));

        bytes = rSendTx.toBytes(forDeal, withSignature);
        assertEquals(rSendTx.getDataLength(forDeal, withSignature), bytes.length);
        parsedTx = TransactionFactory.getInstance().parse(bytes, forDeal);
        assertEquals(parsedTx.getDataLength(forDeal, withSignature), bytes.length);
        assertNotNull(parsedTx.getDApp());
        parsedDApp = (MoneyStaking) parsedTx.getDApp();
        assertEquals(dApp.getHTML(null), parsedDApp.getHTML(null));

    }

    @Test
    public void testOrphanBody() {
    }
}