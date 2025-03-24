package org.erachain.dapp.epoch;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import junit.framework.TestCase;
import lombok.SneakyThrows;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.BlockGenerator;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.dapp.DApp;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMapImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.ntp.NTP;
import org.junit.Test;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.erachain.core.item.assets.AssetCls.ERA_KEY;
import static org.erachain.core.transaction.Transaction.FEE_KEY;

public class OddEvenDAppTest extends TestCase {

    Controller cntrl;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test123".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    PrivateKeyAccount privAcc1 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, "1"));
    PrivateKeyAccount privAcc2 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, "2"));
    PrivateKeyAccount privAcc3 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, "3"));

    BigDecimal amount = new BigDecimal("10");
    byte[] isText = new byte[]{1};
    byte[] encryptMessage = new byte[]{0};
    long timestamp;

    DCSet dcSet;
    GenesisBlock gb;
    private BlockChain bchain;
    BlockGenerator blockGenerator;

    OddEvenDApp oddEvenDApp;
    Account dAppAccount;

    // INIT PERSONS
    private void init() throws Exception {

        oddEvenDApp = new OddEvenDApp("1", "wait");
        dAppAccount = oddEvenDApp.getStock();

        dcSet = DCSet.createEmptyDatabaseSet(IDB.DBS_MAP_DB);
        cntrl = Controller.getInstance();
        timestamp = NTP.getTime();
        cntrl.initBlockChain(dcSet);
        bchain = cntrl.getBlockChain();
        BlockChain.ALL_VALID_BEFORE = 0;
        gb = bchain.getGenesisBlock();
        cntrl.transactionsPool = new TransactionPoolStub();
        //cntrl.getBlockGenerator().stop = true;

        // FEE FUND
        maker.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, dcSet);
        maker.changeBalance(dcSet, false, false, ERA_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        maker.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        privAcc1.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, dcSet);

        blockGenerator = new BlockGenerator(dcSet, bchain, false);

        OddEvenDApp.DISABLED_BEFORE = 0;

    }

    private void testOrphan(DCSet dcSet) throws Exception {

        TransactionFinalMapImpl finalMap = dcSet.getTransactionFinalMap();

        BigDecimal amountSend = new BigDecimal("10");
        Transaction rSendTxDApp = new RSend(maker, null, null, (byte) 0, dAppAccount, ERA_KEY,
                amountSend, null, "1".getBytes(StandardCharsets.UTF_8), isText, encryptMessage,
                // Одинаковое время жестко задаем
                1735678800005L,
                0L);
        int txType = rSendTxDApp.getType();
        rSendTxDApp.sign(maker, Transaction.FOR_NETWORK);
        ArrayList<Transaction> txsDApp = new ArrayList<>();
        txsDApp.add(rSendTxDApp);
        Fun.Tuple2<List<Transaction>, Integer> txsBlock = new Fun.Tuple2<>(txsDApp, 1);

        assertEquals(new BigDecimal("1000.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));
        assertFalse(dcSet.getTimeTXWaitMap().getTXIterator(false).hasNext());
        assertFalse(dcSet.getTimeTXDoneMap().getTXIterator(false).hasNext());

        Block block2DApp = blockGenerator.generateNextBlock(maker, gb, txsBlock,
                1000, 1000L, 1000L);
        block2DApp.sign(maker);
        // Исполним перевод
        block2DApp.process(dcSet, true);

        System.out.println(rSendTxDApp.viewSignature());
        assertTrue(finalMap.get(rSendTxDApp.getSignature()) != null);

        /// Проверим как ключи в БД легли
        IteratorCloseable<Long> iteratorLong;
        iteratorLong = finalMap.getIteratorByCreator(maker.getShortAddressBytes(), false);
        assertEquals(rSendTxDApp.viewSignature(), finalMap.get(iteratorLong.next()).viewSignature());
        iteratorLong = finalMap.getIteratorByCreator(maker.getShortAddressBytes(), true);
        assertEquals(rSendTxDApp.viewSignature(), finalMap.get(iteratorLong.next()).viewSignature());

        iteratorLong = finalMap.getIteratorByCreator(maker.getShortAddressBytes(), false);
        assertEquals(1, Iterators.size(iteratorLong));

        iteratorLong = finalMap.getIteratorByAddressAndType(OddEvenDApp.MAKER.getShortAddressBytes(), txType, false, false);
        assertEquals(rSendTxDApp.viewSignature(), finalMap.get(iteratorLong.next()).viewSignature());
        iteratorLong = finalMap.getIteratorByAddressAndType(OddEvenDApp.MAKER.getShortAddressBytes(), txType, false, true);
        assertEquals(rSendTxDApp.viewSignature(), finalMap.get(iteratorLong.next()).viewSignature());

        if (!dcSet.isFork()) {
            iteratorLong = finalMap.getIteratorByRecipient(OddEvenDApp.MAKER.getShortAddressBytes(), false);
            assertEquals(rSendTxDApp.viewSignature(), finalMap.get(iteratorLong.next()).viewSignature());
            iteratorLong = finalMap.getIteratorByRecipient(OddEvenDApp.MAKER.getShortAddressBytes(), true);
            assertEquals(rSendTxDApp.viewSignature(), finalMap.get(iteratorLong.next()).viewSignature());

            iteratorLong = finalMap.getIteratorByRecipient(OddEvenDApp.MAKER.getShortAddressBytes(), false);
            assertEquals(1, Iterators.size(iteratorLong));
        }


        assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));

        Transaction txDApp = dcSet.getTransactionFinalMap().get(2, 1);
        DApp dApp = txDApp.getDApp();
        assertEquals(OddEvenDApp.class, dApp.getClass());
        OddEvenDApp itDApp = (OddEvenDApp)dApp;
        assertEquals("wait", itDApp.getStatus());

        assertTrue(dcSet.getTimeTXWaitMap().getTXIterator(false).hasNext());
        assertFalse(dcSet.getTimeTXDoneMap().getTXIterator(false).hasNext());

        assertEquals(block2DApp.heightBlock, 2);
        assertEquals(block2DApp.getHeight(), 2);
        assertEquals(block2DApp.blockHead.heightBlock, 2);

        Fun.Tuple2<List<Transaction>, Integer> uTx = new Fun.Tuple2<>(new ArrayList<>(), 0);
        Block block3 = blockGenerator.generateNextBlock(maker, block2DApp, uTx,
                1000, 1000L, 1000L);
        block3.process(dcSet, true);

        iteratorLong = finalMap.getIteratorByCreator(maker.getShortAddressBytes(), false);
        assertEquals(1, Iterators.size(iteratorLong));
        if (!dcSet.isFork()) {
            iteratorLong = finalMap.getIteratorByRecipient(OddEvenDApp.MAKER.getShortAddressBytes(), false);
            assertEquals(1, Iterators.size(iteratorLong));
        }

        Block block4 = blockGenerator.generateNextBlock(maker, block3, uTx,
                1000, 1000L, 1000L);
        block4.process(dcSet, true);

        iteratorLong = finalMap.getIteratorByCreator(maker.getShortAddressBytes(), false);
        assertEquals(1, Iterators.size(iteratorLong));
        if (!dcSet.isFork()) {
            iteratorLong = finalMap.getIteratorByRecipient(OddEvenDApp.MAKER.getShortAddressBytes(), false);
            assertEquals(1, Iterators.size(iteratorLong));
        }

        assertTrue(dcSet.getTimeTXWaitMap().getTXIterator(false).hasNext());
        assertFalse(dcSet.getTimeTXDoneMap().getTXIterator(false).hasNext());
        assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));

        Block block5Done = blockGenerator.generateNextBlock(maker, block4, uTx,
                1000, 1000L, 1000L);
        block5Done.process(dcSet, true);

        iteratorLong = finalMap.getIteratorByCreator(maker.getShortAddressBytes(), false);
        assertEquals(1, Iterators.size(iteratorLong));
        if (!dcSet.isFork()) {
            iteratorLong = finalMap.getIteratorByRecipient(OddEvenDApp.MAKER.getShortAddressBytes(), false);
            assertEquals(1, Iterators.size(iteratorLong));
        }

        assertFalse(dcSet.getTimeTXWaitMap().getTXIterator(false).hasNext());
        assertTrue(dcSet.getTimeTXDoneMap().getTXIterator(false).hasNext());

        txDApp = dcSet.getTransactionFinalMap().get(2, 1);
        dApp = txDApp.getDApp();
        assertEquals(OddEvenDApp.class, dApp.getClass());
        itDApp = (OddEvenDApp)dApp;
        assertEquals("You win! x2 = 20", itDApp.getStatus());
        assertEquals(new BigDecimal("1010.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));

        Block block6After = blockGenerator.generateNextBlock(maker, block5Done, uTx,
                1000, 1000L, 1000L);
        block6After.process(dcSet, true);

        assertEquals(block6After.heightBlock, 6);
        assertEquals(block6After.getHeight(), 6);
        assertEquals(block6After.blockHead.heightBlock, 6);
        assertEquals(new BigDecimal("1010.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));

        assertFalse(dcSet.getTimeTXWaitMap().getTXIterator(false).hasNext());
        assertTrue(dcSet.getTimeTXDoneMap().getTXIterator(false).hasNext());

        iteratorLong = finalMap.getIteratorByCreator(maker.getShortAddressBytes(), false);
        assertEquals(1, Iterators.size(iteratorLong));
        if (!dcSet.isFork()) {
            iteratorLong = finalMap.getIteratorByRecipient(OddEvenDApp.MAKER.getShortAddressBytes(), false);
            assertEquals(1, Iterators.size(iteratorLong));
        }

        Long fromID = null; int offset = 0; int pageSize = 10; boolean useForge = false;
        List<Transaction> transactions;
        if (!dcSet.isFork()) {
            transactions = finalMap.getTransactionsByAddressFromID(OddEvenDApp.MAKER.getShortAddressBytes(),
                    fromID, offset, pageSize, !useForge, true);
            assertEquals(1, transactions.size());
        }


        /////////// ORPHAN
        block6After.orphan(dcSet);
        assertEquals(block6After.heightBlock, 6);
        assertEquals(block6After.getHeight(), 6);
        assertEquals(block6After.blockHead.heightBlock, 6);

        iteratorLong = finalMap.getIteratorByCreator(maker.getShortAddressBytes(), false);
        assertEquals(1, Iterators.size(iteratorLong));
        if (!dcSet.isFork()) {
            iteratorLong = finalMap.getIteratorByRecipient(OddEvenDApp.MAKER.getShortAddressBytes(), false);
            assertEquals(1, Iterators.size(iteratorLong));

            transactions = finalMap.getTransactionsByAddressFromID(OddEvenDApp.MAKER.getShortAddressBytes(),
                    fromID, offset, pageSize, !useForge, true);
            assertEquals(1, transactions.size());
        }

        assertFalse(dcSet.getTimeTXWaitMap().getTXIterator(false).hasNext());
        assertTrue(dcSet.getTimeTXDoneMap().getTXIterator(false).hasNext());
        assertEquals(new BigDecimal("1010.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));

        block5Done.orphan(dcSet);
        assertTrue(dcSet.getTimeTXWaitMap().getTXIterator(false).hasNext());
        assertFalse(dcSet.getTimeTXDoneMap().getTXIterator(false).hasNext());
        txDApp = dcSet.getTransactionFinalMap().get(2, 1);
        dApp = txDApp.getDApp();
        assertEquals(OddEvenDApp.class, dApp.getClass());
        itDApp = (OddEvenDApp)dApp;
        assertEquals("wait", itDApp.getStatus());
        assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));

        block4.orphan(dcSet);
        block3.orphan(dcSet);
        assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));

        iteratorLong = finalMap.getIteratorByCreator(maker.getShortAddressBytes(), false);
        assertEquals(1, Iterators.size(iteratorLong));
        if (!dcSet.isFork()) {
            iteratorLong = finalMap.getIteratorByRecipient(OddEvenDApp.MAKER.getShortAddressBytes(), false);
            assertEquals(1, Iterators.size(iteratorLong));
        }

        if (!dcSet.isFork()) {
            transactions = finalMap.getTransactionsByAddressFromID(OddEvenDApp.MAKER.getShortAddressBytes(),
                    fromID, offset, pageSize, !useForge, true);
            assertEquals(1, transactions.size());
        }

        assertTrue(dcSet.getTimeTXWaitMap().getTXIterator(false).hasNext());
        assertFalse(dcSet.getTimeTXDoneMap().getTXIterator(false).hasNext());
        block2DApp.orphan(dcSet);
        assertEquals(new BigDecimal("1000.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));
        assertFalse(dcSet.getTimeTXWaitMap().getTXIterator(false).hasNext());
        assertFalse(dcSet.getTimeTXDoneMap().getTXIterator(false).hasNext());

        iteratorLong = finalMap.getIteratorByCreator(maker.getShortAddressBytes(), false);
        assertEquals(0, Iterators.size(iteratorLong));
        if (!dcSet.isFork()) {
            iteratorLong = finalMap.getIteratorByRecipient(OddEvenDApp.MAKER.getShortAddressBytes(), false);
            assertEquals(0, Iterators.size(iteratorLong));
        }

        if (!dcSet.isFork()) {
            transactions = finalMap.getTransactionsByAddressFromID(OddEvenDApp.MAKER.getShortAddressBytes(),
                    fromID, offset, pageSize, !useForge, true);
            assertEquals(0, transactions.size());
        }

    }

    @Test
    @SneakyThrows
    public void testOrphanFork() {
        init();

        testOrphan(dcSet);

        DCSet dcSetFork = dcSet.fork("test");

        testOrphan(dcSetFork);

    }

    @Test
    @SneakyThrows
    public void testOrphanHalfFork() {
        init();

        BigDecimal amountSend = new BigDecimal("10");
        Transaction rSendTxDApp = new RSend(maker, null, null, (byte) 0, dAppAccount, ERA_KEY,
                amountSend, null, "1".getBytes(StandardCharsets.UTF_8), isText, encryptMessage,
                // Одинаковое время жестко задаем
                1735678800005L,
                0L);
        rSendTxDApp.sign(maker, Transaction.FOR_NETWORK);
        ArrayList<Transaction> txsDApp = new ArrayList<>();
        txsDApp.add(rSendTxDApp);
        Fun.Tuple2<List<Transaction>, Integer> txsBlock = new Fun.Tuple2<>(txsDApp, 1);

        assertEquals(new BigDecimal("1000.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));
        assertFalse(dcSet.getTimeTXWaitMap().getTXIterator(false).hasNext());
        assertFalse(dcSet.getTimeTXDoneMap().getTXIterator(false).hasNext());

        Block block2DApp = blockGenerator.generateNextBlock(maker, gb, txsBlock,
                1000, 1000L, 1000L);
        block2DApp.sign(maker);
        // Исполним перевод
        block2DApp.process(dcSet, true);
        assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));

        Transaction txDApp = dcSet.getTransactionFinalMap().get(2, 1);
        DApp dApp = txDApp.getDApp();
        assertEquals(OddEvenDApp.class, dApp.getClass());
        OddEvenDApp itDApp = (OddEvenDApp)dApp;
        assertEquals("wait", itDApp.getStatus());

        assertTrue(dcSet.getTimeTXWaitMap().getTXIterator(false).hasNext());
        assertFalse(dcSet.getTimeTXDoneMap().getTXIterator(false).hasNext());

        assertEquals(block2DApp.heightBlock, 2);
        assertEquals(block2DApp.getHeight(), 2);
        assertEquals(block2DApp.blockHead.heightBlock, 2);

        Fun.Tuple2<List<Transaction>, Integer> uTxEmpty = new Fun.Tuple2<>(new ArrayList<>(), 0);
        Block block3 = blockGenerator.generateNextBlock(maker, block2DApp, uTxEmpty,
                1000, 1000L, 1000L);
        block3.process(dcSet, true);
        assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));

        {
            // Теперь проверим как исполнится в форке, когда запуск не в нем был
            DCSet dcSetFork = dcSet.fork("test");

            Block block4 = blockGenerator.generateNextBlock(maker, block3, uTxEmpty,
                    1000, 1000L, 1000L);
            block4.process(dcSetFork, true);
            assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));

            assertTrue(dcSetFork.getTimeTXWaitMap().getTXIterator(false).hasNext());
            assertFalse(dcSetFork.getTimeTXDoneMap().getTXIterator(false).hasNext());
            assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSetFork));

            Block block5Done = blockGenerator.generateNextBlock(maker, block4, uTxEmpty,
                    1000, 1000L, 1000L);
            block5Done.process(dcSetFork, true);
            assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));

            assertFalse(dcSetFork.getTimeTXWaitMap().getTXIterator(false).hasNext());
            assertTrue(dcSetFork.getTimeTXDoneMap().getTXIterator(false).hasNext());

            txDApp = dcSetFork.getTransactionFinalMap().get(2, 1);
            dApp = txDApp.getDApp();
            assertEquals(OddEvenDApp.class, dApp.getClass());
            itDApp = (OddEvenDApp) dApp;
            assertEquals("You win! x2 = 20", itDApp.getStatus());
            assertEquals(new BigDecimal("1010.00000000"), maker.getBalanceUSE(ERA_KEY, dcSetFork));

            Block block6After = blockGenerator.generateNextBlock(maker, block5Done, uTxEmpty,
                    1000, 1000L, 1000L);
            block6After.process(dcSetFork, true);
            assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));

            assertEquals(block6After.heightBlock, 6);
            assertEquals(block6After.getHeight(), 6);
            assertEquals(block6After.blockHead.heightBlock, 6);
            assertEquals(new BigDecimal("1010.00000000"), maker.getBalanceUSE(ERA_KEY, dcSetFork));

            assertFalse(dcSetFork.getTimeTXWaitMap().getTXIterator(false).hasNext());
            assertTrue(dcSetFork.getTimeTXDoneMap().getTXIterator(false).hasNext());

            /////////// ORPHAN
            block6After.orphan(dcSetFork);
            assertEquals(block6After.heightBlock, 6);
            assertEquals(block6After.getHeight(), 6);
            assertEquals(block6After.blockHead.heightBlock, 6);

            assertFalse(dcSetFork.getTimeTXWaitMap().getTXIterator(false).hasNext());
            assertTrue(dcSetFork.getTimeTXDoneMap().getTXIterator(false).hasNext());
            assertEquals(new BigDecimal("1010.00000000"), maker.getBalanceUSE(ERA_KEY, dcSetFork));

            block5Done.orphan(dcSetFork);

            assertTrue(dcSetFork.getTimeTXWaitMap().getTXIterator(false).hasNext());
            assertFalse(dcSetFork.getTimeTXDoneMap().getTXIterator(false).hasNext());
            txDApp = dcSetFork.getTransactionFinalMap().get(2, 1);
            dApp = txDApp.getDApp();
            assertEquals(OddEvenDApp.class, dApp.getClass());
            itDApp = (OddEvenDApp) dApp;
            assertEquals("wait", itDApp.getStatus());
            assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSetFork));

            block4.orphan(dcSetFork);

            assertTrue(dcSetFork.getTimeTXWaitMap().getTXIterator(false).hasNext());
            assertFalse(dcSetFork.getTimeTXDoneMap().getTXIterator(false).hasNext());
            txDApp = dcSetFork.getTransactionFinalMap().get(2, 1);
            dApp = txDApp.getDApp();
            assertEquals(OddEvenDApp.class, dApp.getClass());
            itDApp = (OddEvenDApp) dApp;
            assertEquals("wait", itDApp.getStatus());
            assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSetFork));

        }

        assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));
        block3.orphan(dcSet);

        assertTrue(dcSet.getTimeTXWaitMap().getTXIterator(false).hasNext());
        assertFalse(dcSet.getTimeTXDoneMap().getTXIterator(false).hasNext());
        txDApp = dcSet.getTransactionFinalMap().get(2, 1);
        dApp = txDApp.getDApp();
        assertEquals(OddEvenDApp.class, dApp.getClass());
        itDApp = (OddEvenDApp) dApp;
        assertEquals("wait", itDApp.getStatus());
        assertEquals(new BigDecimal("990.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));

        block2DApp.orphan(dcSet);

        assertEquals(new BigDecimal("1000.00000000"), maker.getBalanceUSE(ERA_KEY, dcSet));
        assertFalse(dcSet.getTimeTXWaitMap().getTXIterator(false).hasNext());
        assertFalse(dcSet.getTimeTXDoneMap().getTXIterator(false).hasNext());

    }

    @Test
    public void testGetLastDigit() {
        Integer res = OddEvenDApp.sumLastDigits("CxMfAmPEL8xycmfGLy9WTCB7THvBHDqwm7wNGiGmzqLZjsruQYqBXtDbDe3RHWBS4AVJE5CjJJqVJziSo52NTLv", 3);
        assertEquals(res.intValue(), 2);

        assertEquals(OddEvenDApp.sumLastDigits("3RHWBS4AVJE5CjJJq0VJ0zoN0TLv", 3), -10);
        assertEquals(OddEvenDApp.sumLastDigits("3RHWBS4AVJE5Cj5JJq9VJ7zoNTLv", 3), 1);

    }

    @Test
    public void testIsDisabled() {
        OddEvenDApp contract = new OddEvenDApp("1", "wait");
        assertEquals(contract.isDisabled(OddEvenDApp.DISABLED_BEFORE - 1), true);
        assertEquals(contract.isDisabled(OddEvenDApp.DISABLED_BEFORE + 1), false);
    }

    @Test
    public void testParse() {

        OddEvenDApp contract = new OddEvenDApp("1", "wait");
        byte[] data = contract.toBytes(Transaction.FOR_NETWORK);
        assertEquals(data.length, contract.length(Transaction.FOR_NETWORK));
        OddEvenDApp contractParse = OddEvenDApp.Parse(data, 0, Transaction.FOR_NETWORK);
        assertEquals(contractParse.getName(), contract.getName());

        data = contract.toBytes(Transaction.FOR_DB_RECORD);
        assertEquals(data.length, contract.length(Transaction.FOR_DB_RECORD));
        contractParse = OddEvenDApp.Parse(data, 0, Transaction.FOR_DB_RECORD);
        assertEquals(contractParse.getName(), contract.getName());
    }

    String getResHeightBlock(int heightBlock) {
        int mod10 = heightBlock % 10;
        switch (mod10) {
            case 0:
                return  "0";
            case 9:
                // девятка - тогда по одному биту более старшему
                return (heightBlock & 16) != 0? "1" : "2";
            default:
                return  (mod10 & 1) != 0? "1" : "2";
        }
    }

    @Test
    public void testResolveJson_1() {

        int heightBlock = 16;
        System.out.println(Integer.toBinaryString(heightBlock));
        System.out.println(heightBlock + " -> " + getResHeightBlock(heightBlock));
        assertEquals("2", getResHeightBlock(heightBlock));

        heightBlock = 121279;
        System.out.println(Integer.toBinaryString(heightBlock));
        System.out.println(heightBlock + " -> " + getResHeightBlock(heightBlock));
        assertEquals("1", getResHeightBlock(heightBlock));

        heightBlock = 125419;
        System.out.println(Integer.toBinaryString(heightBlock));
        System.out.println(heightBlock + " -> " + getResHeightBlock(heightBlock));
        assertEquals("2", getResHeightBlock(heightBlock));

        heightBlock = 12133;
        System.out.println(Integer.toBinaryString(heightBlock));
        System.out.println(heightBlock + " -> " + getResHeightBlock(heightBlock));
        assertEquals("1", getResHeightBlock(heightBlock));

        heightBlock = 121290;
        System.out.println(Integer.toBinaryString(heightBlock));
        System.out.println(heightBlock + " -> " + getResHeightBlock(heightBlock));
        assertEquals("0", getResHeightBlock(heightBlock));

        heightBlock = 121040;
        System.out.println(Integer.toBinaryString(heightBlock));
        System.out.println(heightBlock + " -> " + getResHeightBlock(heightBlock));
        assertEquals("0", getResHeightBlock(heightBlock));


    }
}