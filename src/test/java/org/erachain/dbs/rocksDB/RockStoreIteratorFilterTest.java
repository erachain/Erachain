package org.erachain.dbs.rocksDB;

import com.google.common.collect.Iterators;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMapImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@Slf4j
public class RockStoreIteratorFilterTest {

    Random random = new Random();
    DCSet dcSet;
    GenesisBlock gb;

    PrivateKeyAccount accountA;
    PrivateKeyAccount accountB;

    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    byte FEE_POWER = (byte) 0;

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
        accountA.changeBalance(dcSet, false, false, ERM_KEY, BigDecimal.valueOf(100), false, false, false, false);
        accountA.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(10), false, false, false, false);

        accountB.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, dcSet);
        accountB.changeBalance(dcSet, false, false, ERM_KEY, BigDecimal.valueOf(100), false, false, false, false);
        accountB.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(10), false, false, false, false);

        timestamp = NTP.getTime();
    }

    @Test
    public void hasNext() {
        try {

            init(IDB.DBS_ROCK_DB);

            TransactionFinalMapImpl txMap = dcSet.getTransactionFinalMap();
            String address = "7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW";
            Account recipientAcc = new Account(address);
            BigDecimal amount_asset = new BigDecimal("1");

            int seqNo = 1;

            RSend assetTransfer = new RSend(accountA, FEE_POWER, recipientAcc, 1, amount_asset, timestamp++, 0L);
            assetTransfer.sign(accountA, Transaction.FOR_NETWORK);
            assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
            txMap.put(assetTransfer);

            assetTransfer = new RSend(accountB, FEE_POWER, recipientAcc, 1, amount_asset, timestamp++, 0L);
            assetTransfer.sign(accountB, Transaction.FOR_NETWORK);
            assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
            txMap.put(assetTransfer);

            assetTransfer = new RSend(accountB, FEE_POWER, accountA, 1, amount_asset, timestamp++, 0L);
            assetTransfer.sign(accountB, Transaction.FOR_NETWORK);
            assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
            txMap.put(assetTransfer);


            String address2 = "7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC";
            Account recipientAcc2 = new Account(address2);
            amount_asset = new BigDecimal("10");

            assetTransfer = new RSend(accountA, FEE_POWER, recipientAcc2, 1, amount_asset, timestamp++, 0L);
            assetTransfer.sign(accountA, Transaction.FOR_NETWORK);
            assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
            txMap.put(assetTransfer);

            assetTransfer = new RSend(accountB, FEE_POWER, accountA, 1, amount_asset, timestamp++, 0L);
            assetTransfer.sign(accountB, Transaction.FOR_NETWORK);
            assetTransfer.setDC(dcSet, Transaction.FOR_NETWORK, 1, seqNo++, true);
            txMap.put(assetTransfer);

            Account findAccount = recipientAcc;
            String findAddress = findAccount.getAddress();
            IteratorCloseable<Long> iterator = txMap.getBiDirectionAddressIterator(
                    findAddress, null, false, 0, 5);

            int count = 0;
            while (iterator.hasNext()) {
                Long key = iterator.next();
                Transaction transaction = txMap.get(key);
                assertEquals(true, transaction.isInvolved(findAccount));
                count++;
            }
            assertEquals(count, 2);

            iterator = txMap.getBiDirectionAddressIterator(
                    findAddress, null, true, 0, 5);
            count = 0;
            while (iterator.hasNext()) {
                Long key = iterator.next();
                Transaction transaction = txMap.get(key);
                assertEquals(true, transaction.isInvolved(findAccount));
                count++;
            }
            assertEquals(count, 2);

            ////////////

            findAccount = accountB;
            findAddress = findAccount.getAddress();
            iterator = txMap.getBiDirectionAddressIterator(
                    findAddress, null, false, 0, 5);

            count = 0;
            while (iterator.hasNext()) {
                Long key = iterator.next();
                Transaction transaction = txMap.get(key);
                assertEquals(true, transaction.isInvolved(findAccount));
                count++;
            }
            assertEquals(count, 3);

            iterator = txMap.getBiDirectionAddressIterator(
                    findAddress, null, true, 0, 5);
            count = 0;
            while (iterator.hasNext()) {
                Long key = iterator.next();
                Transaction transaction = txMap.get(key);
                assertEquals(true, transaction.isInvolved(findAccount));
                count++;
            }
            assertEquals(count, 3);

            iterator = txMap.getBiDirectionAddressIterator(
                    null, null, false, 0, 0);

            assertEquals(Iterators.size(iterator), 265);

            iterator = txMap.getBiDirectionAddressIterator(
                    null, null, true, 0, 0);
            assertEquals(Iterators.size(iterator), 265);

        } finally {
            dcSet.close();
        }
    }


    @Test
    public void next() {
    }
}