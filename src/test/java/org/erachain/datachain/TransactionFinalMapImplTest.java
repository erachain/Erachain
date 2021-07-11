package org.erachain.datachain;

import com.google.common.collect.Iterators;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.IssueAssetTransaction;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.IDB;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.Pair;
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
public class TransactionFinalMapImplTest {

    int[] TESTED_DBS = new int[]{
            IDB.DBS_MAP_DB
            //, IDB.DBS_ROCK_DB
    };

    ExLink exLink = null;

    byte[] isText = new byte[]{1};
    byte[] enCrypted = new byte[]{0};

    Random random = new Random();
    long flags = 0L;
    int seqNo = 0;

    boolean useCancel = false;

    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balanceA;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balanceB;
    DCSet dcSet;
    GenesisBlock gb;

    PrivateKeyAccount accountA;
    PrivateKeyAccount accountB;
    IssueAssetTransaction issueAssetTransaction;
    AssetCls assetA;
    long keyA;
    AssetCls assetB;
    long keyB;

    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    byte[] invalidSign = new byte[64];

    long timestamp;

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

        timestamp = NTP.getTime();
    }

    @Test
    public void findTransactions() {
    }

    @Test
    public void findTransactionsCount() {
    }

    /**
     * Проверка Итераторов - были ошибки преобразования типов и двойные Значения в списке итератора
     */
    @Test
    public void findTransactionsKeys() {
        //192.168.1.156:9047/apirecords/find?address=7PXf6Bk9m7uLrC9ATTHPyEtxRkCeeWDG3b&type=31&startblock=0

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                String address = "7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW";
                String sender = null;
                String recipient = null;
                int minHeight = 0;
                int maxHeight = 0;
                int type = 0;
                int service = 0;
                boolean desc = false;
                int offset = 0;
                int limit = 0;

                int seqNo = 1;

                Account recipientAcc = new Account(address);
                BigDecimal amount_asset = new BigDecimal("1");

                RSend assetTransfer = new RSend(accountA, FEE_POWER, recipientAcc, 1, amount_asset, timestamp++, 0L);
                assetTransfer.sign(accountA, Transaction.FOR_NETWORK);
                assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
                dcSet.getTransactionFinalMap().put(assetTransfer);

                assetTransfer = new RSend(accountB, FEE_POWER, accountA, 1, amount_asset, timestamp++, 0L);
                assetTransfer.sign(accountB, Transaction.FOR_NETWORK);
                assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
                dcSet.getTransactionFinalMap().put(assetTransfer);

                assetTransfer = new RSend(accountB, FEE_POWER, accountA, 1, amount_asset, timestamp++, 0L);
                assetTransfer.sign(accountB, Transaction.FOR_NETWORK);
                assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
                dcSet.getTransactionFinalMap().put(assetTransfer);

                Iterator<Long> iterator = dcSet.getTransactionFinalMap().findTransactionsKeys(accountA.getAddress(), sender, recipient,
                        null, minHeight, maxHeight, type, service, desc, offset, limit);

                // .size сбрасывает Итератор на конец списка
                assertEquals(3, Iterators.size(iterator));

                /// пошлем сами себе - эта трнзакция будет в обоих Итераторах
                assetTransfer = new RSend(accountA, FEE_POWER, accountA, 1, amount_asset, timestamp++, 0L);
                assetTransfer.sign(accountA, Transaction.FOR_NETWORK);
                assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
                dcSet.getTransactionFinalMap().put(assetTransfer);

                //Set<BlExpUnit> iteratorA = dcSet.getTransactionFinalCalculatedMap().getBlExpCalculatedsByAddress(accountA.getAddress());

                IteratorCloseable<Fun.Tuple2<Long, Long>> iteratorT = dcSet.getTradeMap().iteratorByAssetKey(1L, true);

                //Iterator iteratorU = dcSet.getTransactionTab().findTransactionsKeys(accountA.getAddress(), sender, recipient, minHeight, desc, type, service, offset);

                iterator = dcSet.getTransactionFinalMap().findTransactionsKeys(accountA.getAddress(), sender, recipient,
                        null, minHeight, maxHeight, type, service, desc, offset, limit);


                // .size сбрасывает Итератор на конец списка
                /// фигово что тут будут повторения ключей - так как в обоих Итераторах есть значения то они удваиваются в слитом итераторе
                assertEquals(4, Iterators.size(iterator));

            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void getTransactionsByAddressFromID() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                int seqNo = 1;

                String address = "7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW";
                Account recipientAcc = new Account(address);
                BigDecimal amount_asset = new BigDecimal("1");

                RSend assetTransfer = new RSend(accountA, FEE_POWER, recipientAcc, 1, amount_asset, timestamp++, 0L);
                assetTransfer.sign(accountA, Transaction.FOR_NETWORK);
                assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
                dcSet.getTransactionFinalMap().put(assetTransfer);

                assetTransfer = new RSend(accountB, FEE_POWER, recipientAcc, 1, amount_asset, timestamp++, 0L);
                assetTransfer.sign(accountB, Transaction.FOR_NETWORK);
                assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
                dcSet.getTransactionFinalMap().put(assetTransfer);

                assetTransfer = new RSend(accountB, FEE_POWER, accountA, 1, amount_asset, timestamp++, 0L);
                assetTransfer.sign(accountB, Transaction.FOR_NETWORK);
                assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
                dcSet.getTransactionFinalMap().put(assetTransfer);


                String address2 = "7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC";
                Account recipientAcc2 = new Account(address2);
                amount_asset = new BigDecimal("10");

                assetTransfer = new RSend(accountA, FEE_POWER, recipientAcc2, 1, amount_asset, timestamp++, 0L);
                assetTransfer.sign(accountA, Transaction.FOR_NETWORK);
                assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
                dcSet.getTransactionFinalMap().put(assetTransfer);

                assetTransfer = new RSend(accountB, FEE_POWER, accountA, 1, amount_asset, timestamp++, 0L);
                assetTransfer.sign(accountB, Transaction.FOR_NETWORK);
                assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
                dcSet.getTransactionFinalMap().put(assetTransfer);

                List<Transaction> find = dcSet.getTransactionFinalMap().getTransactionsByAddressFromID(
                        recipientAcc.getShortAddressBytes(), null, 0, 5, false, true);

                // .size сбрасывает Итератор на конец списка
                assertEquals(2, find.size());

                find = dcSet.getTransactionFinalMap().getTransactionsByAddressFromID(
                        recipientAcc.getShortAddressBytes(), null, 0, 5, false, false);

                // .size сбрасывает Итератор на конец списка
                assertEquals(2, find.size());


            } finally {
                dcSet.close();
            }
        }

    }

    @Test
    public void getTransactionsByTitleFromBetter() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                int seqNo = 1;

                String address = "7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW";
                Account recipientAcc = new Account(address);
                BigDecimal amount_asset = new BigDecimal("1");
                String title = "forging";
                TransactionFinalMapImpl map = dcSet.getTransactionFinalMap();

                RSend assetTransfer;
                for (int i = 0; i < 100; i++) {
                    assetTransfer = new RSend(accountA, exLink, FEE_POWER, recipientAcc, 1L, amount_asset, title + i,
                            null, isText, enCrypted, timestamp++, 0L);
                    assetTransfer.sign(accountA, Transaction.FOR_NETWORK);
                    assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
                    map.put(assetTransfer);

                    assetTransfer = new RSend(accountA, exLink, FEE_POWER, recipientAcc, 1L, amount_asset, "for",
                            null, isText, enCrypted, timestamp++, 0L);
                    assetTransfer.sign(accountA, Transaction.FOR_NETWORK);
                    assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
                    map.put(assetTransfer);

                    assetTransfer = new RSend(accountA, exLink, FEE_POWER, recipientAcc, 1L, amount_asset, "forgen",
                            null, isText, enCrypted, timestamp++, 0L);
                    assetTransfer.sign(accountA, Transaction.FOR_NETWORK);
                    assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
                    map.put(assetTransfer);

                }

                Long fromSeqNo = Transaction.makeDBRef(1, 30);
                // WORDS + asFilter
                Pair<String, Boolean>[] wordsForging = new Pair[]{new Pair("forging", true)};
                Pair<String, Boolean>[] wordsForgenFilter = new Pair[]{new Pair("forgen", true)};
                Pair<String, Boolean>[] wordsForgen = new Pair[]{new Pair("forgen", false)};
                Pair<String, Boolean>[] wordsFor = new Pair[]{new Pair("for", false)};
                String fromWord;

                List<Transaction> find;
                //////////////////////// DIRECT FIND
                boolean descending = false;

                /// FIRST
                find = dcSet.getTransactionFinalMap().getTransactionsByTitleFromBetter(
                        wordsForging, 0, null, null, 0, 20, descending);
                assertEquals(20, find.size());
                assertEquals("1-1", find.get(0).viewHeightSeq());

                find = dcSet.getTransactionFinalMap().getTransactionsByTitleFromBetter(
                        wordsForgenFilter, 0, null, null, 0, 20, descending);
                assertEquals(20, find.size());
                assertEquals("1-3", find.get(0).viewHeightSeq());

                find = dcSet.getTransactionFinalMap().getTransactionsByTitleFromBetter(
                        wordsForgen, 0, null, null, 0, 20, descending);
                assertEquals(20, find.size());
                assertEquals("1-3", find.get(0).viewHeightSeq());

                find = dcSet.getTransactionFinalMap().getTransactionsByTitleFromBetter(
                        wordsFor, 0, null, null, 0, 20, descending);
                assertEquals(20, find.size());
                assertEquals("1-2", find.get(0).viewHeightSeq());

                /// MEDDLE
                ////////////// работет именно тогда когда обновляем Начальный Поск Слово - fromWord (если не с начала ищем а с заданной позиции)
                fromWord = ((RSend) map.get(fromSeqNo)).getTitle();
                assertEquals("forgen", fromWord);
                find = dcSet.getTransactionFinalMap().getTransactionsByTitleFromBetter(
                        wordsForging, 0, fromWord, fromSeqNo, 0, 20, descending);
                assertEquals(20, find.size());
                assertEquals("1-30", find.get(0).viewHeightSeq());

                find = dcSet.getTransactionFinalMap().getTransactionsByTitleFromBetter(
                        wordsForgenFilter, 0, fromWord, fromSeqNo, 0, 20, descending);
                assertEquals(20, find.size());
                assertEquals("1-30", find.get(0).viewHeightSeq());

                find = dcSet.getTransactionFinalMap().getTransactionsByTitleFromBetter(
                        wordsForgen, 0, fromWord, fromSeqNo, 0, 20, descending);
                assertEquals(20, find.size());
                assertEquals("1-30", find.get(0).viewHeightSeq());

                find = dcSet.getTransactionFinalMap().getTransactionsByTitleFromBetter(
                        wordsFor, 0, fromWord, fromSeqNo, 0, 20, descending);
                assertEquals(20, find.size());
                assertEquals("1-32", find.get(0).viewHeightSeq());

                /////////////// REVERSE FIND
                descending = !descending;

                /// LAST
                find = dcSet.getTransactionFinalMap().getTransactionsByTitleFromBetter(
                        wordsForging, 0, null, null, 0, 20, descending);
                assertEquals(20, find.size());
                assertEquals("1-133", find.get(0).viewHeightSeq());

                find = dcSet.getTransactionFinalMap().getTransactionsByTitleFromBetter(
                        wordsForging, 0, fromWord, fromSeqNo, 0, 5, descending);
                assertEquals(5, find.size());

                find = dcSet.getTransactionFinalMap().getTransactionsByTitleFromBetter(
                        wordsFor, 0, fromWord, fromSeqNo, 0, 5, descending);
                assertEquals(5, find.size());


            } finally {
                dcSet.close();
            }
        }

    }

    // тут нет проверок
    @Test
    public void deleteHeight() {
    }

}