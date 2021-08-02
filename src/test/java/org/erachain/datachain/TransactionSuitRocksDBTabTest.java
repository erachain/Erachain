package org.erachain.datachain;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.ntp.NTP;
import org.junit.Test;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

public class TransactionSuitRocksDBTabTest {

    DCSet db;
    String address = "7CzxxwH7u9aQtx5iNHskLQjyJvybyKg8rF";
    AddressForging forgingMap;
    Long key = 2L;
    Fun.Tuple2<Integer, Integer> lastPoint;
    Fun.Tuple2<Integer, Integer> currentPoint;

    long timestamp = NTP.getTime();
    int blockHeight = 111;
    int seqNo = 1;
    byte[] isText = new byte[]{(byte) 1};
    byte[] encrypted = new byte[]{(byte) 0};
    byte feePow = (byte) 0;

    private void init() {

        db = DCSet.createEmptyDatabaseSet(0);
        forgingMap = db.getAddressForging();

    }

    @Test
    public void clearByDeadTimeAndLimit() {
    }

    @Test
    public void getTransactions() {
        init();

        Random random = new Random();
        Controller cnt = Controller.getInstance();

        PrivateKeyAccount creator = new PrivateKeyAccount(Base58.decode("72JYKUGbBgTSx1rix4iUSphRrb5p71hbketrb8maESrF"));
        Account recipient = new PublicKeyAccount("71JYKUGbBgTSx1rix4iUSphRrb5p71hbketrb8maESrF");

        Transaction transaction;

        //CREATE MESSAGE TRANSACTION
        transaction = new RSend(creator, feePow, recipient, 0L, new BigDecimal("0.00000001"), timestamp++, 0L);

        transaction.sign(creator, Transaction.FOR_NETWORK);
        transaction.setDC(db, Transaction.FOR_NETWORK, blockHeight, seqNo++, true);

        db.getTransactionTab().put(transaction);

        transaction = new RSend(creator, feePow, recipient, 01, new BigDecimal("0.00000001"), timestamp++, 0L);

        transaction.sign(creator, Transaction.FOR_NETWORK);
        transaction.setDC(db, Transaction.FOR_NETWORK, blockHeight, seqNo++, true);

        db.getTransactionTab().put(transaction);

        long keepTime = BlockChain.GENERATING_MIN_BLOCK_TIME_MS(timestamp) << 3;
        keepTime = timestamp - (keepTime >> 1) + (keepTime << (5 - Controller.HARD_WORK >> 1));
        db.getTransactionTab().clearByDeadTimeAndLimit(keepTime, false);

        List<Transaction> txs1 = db.getTransactionTab().getTransactions(1000, false);
        List<Transaction> txs2 = db.getTransactionTab().getTransactions(1000, false);

        int size = txs1.size();
    }
}