package org.erachain.core.blocks;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.BlockGenerator;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.BlockFactory;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.GenesisIssueAssetTransaction;
import org.erachain.core.transaction.GenesisTransferAssetTransaction;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

;

public class BlockTests {
    static Logger LOGGER = LoggerFactory.getLogger(BlockTests.class.getName());
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[Crypto.SIGNATURE_LENGTH];
    long timestamp = NTP.getTime();
    long flags = 0l;
    boolean forDB = true;
    List<Transaction> transactions = new ArrayList<Transaction>();
    Fun.Tuple2<List<Transaction>, Integer> orderedTransactions = new Fun.Tuple2<>(new ArrayList<Transaction>(), 0);
    byte[] transactionsHash = new byte[Crypto.HASH_LENGTH];
    byte[] atBytes = new byte[0];
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
    Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");
    Transaction payment;
    //CREATE EMPTY MEMORY DATABASE

    private Controller cntrl;
    private DCSet db;
    private BlockChain blockChain;
    private GenesisBlock gb;
    private BlockGenerator blockGenerator;
    List<Transaction> gbTransactions;

    private void init() {

        db = DCSet.createEmptyDatabaseSet(0);
        cntrl = Controller.getInstance();
        cntrl.initBlockChain(db);
        blockChain = cntrl.getBlockChain();
        gb = blockChain.getGenesisBlock();
        //gb.process(db);
        try {
            //blockChain = new BlockChain(db);
        } catch (Exception e) {
        }

        blockGenerator = new BlockGenerator(db, blockChain, false);
        //gb = blockChain.getGenesisBlock();
        gbTransactions = gb.getTransactions();

        generator.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        generator.changeBalance(db, false, false, ERM_KEY, BigDecimal.valueOf(1000), false, false, false);
        generator.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1000), false, false, false); // need for payments
    }

    private void initTrans(List<Transaction> transactions, long timestamp) {
        payment = new RSend(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100), timestamp, generator.getLastTimestamp(db)[0]);
        payment.sign(generator, Transaction.FOR_NETWORK);
        transactions.add(payment);

    }

    @Test
    public void parseHeadBlock() {

        //GENERATE NEW HEAD
        Block.BlockHead head = new Block.BlockHead(1, new byte[Block.REFERENCE_LENGTH], generator, 100,
                new byte[Block.TRANSACTIONS_HASH_LENGTH], new byte[Block.SIGNATURE_LENGTH], 23, 10000,
                45000, 33000, 567554563654L, 3456, 12, size);
        byte[] raw = head.toBytes();


        Block.BlockHead parsedHead = null;
        try {
            //PARSE FROM BYTES
            parsedHead = Block.BlockHead.parse(raw);
        } catch (Exception e) {
            fail("Exception while parsing transaction.");
        }

        //CHECK SIGNATURE
        assertEquals(true, Arrays.equals(head.signature, parsedHead.signature));

        //CHECK GENERATOR
        assertEquals(head.creator.getAddress(), parsedHead.creator.getAddress());

        //CHECK HEIGHT
        assertEquals(head.heightBlock, parsedHead.heightBlock);

        //CHECK BASE TARGET
        assertEquals(head.forgingValue, parsedHead.forgingValue);

        //CHECK FEE
        assertEquals(head.totalFee, parsedHead.totalFee);
        assertEquals(head.emittedFee, parsedHead.emittedFee);

    }

    @Ignore
    //TODO actualize the test
    @Test
    public void validateSignatureGenesisBlock() {

        gb = new GenesisBlock();

        //CHECK IF SIGNATURE VALID
        LOGGER.info("getGeneratorSignature " + gb.getSignature().length
                + " : " + Base58.encode(gb.getSignature()));

        assertEquals(true, gb.isSignatureValid());

        //ADD TRANSACTION SIGNATURE
        LOGGER.info("getGeneratorSignature " + Base58.encode(gb.getSignature()));

        //ADD a GENESIS TRANSACTION for invalid SIGNATURE
        List<Transaction> transactions = gb.getTransactions();
        transactions.add(new GenesisTransferAssetTransaction(
                new Account("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ"), 1l, BigDecimal.valueOf(1)));
        gb.setTransactionsForTests(transactions);

        // SIGNATURE invalid
        assertEquals(false, gb.isSignatureValid());

        assertEquals(true, gb.isValid(db, false));

    }

    @Test
    public void validateGenesisBlock() {

        db = DCSet.createEmptyDatabaseSet(0);
        gb = new GenesisBlock();

        //CHECK IF VALID
        assertEquals(true, gb.isValid(db, false));

        //ADD INVALID GENESIS TRANSACTION
        List<Transaction> transactions = gb.getTransactions();
        transactions.add(new GenesisTransferAssetTransaction(
                new Account("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ"), 1l, BigDecimal.valueOf(-1000)));
        gb.setTransactionsForTests(transactions);

        //CHECK IF INVALID
        assertEquals(false, gb.isValid(db, false));

        //CREATE NEW BLOCK
        gb = new GenesisBlock();

        //CHECK IF VALID
        assertEquals(true, gb.isValid(db, false));

        //PROCESS
        try {
            gb.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //CHECK IF INVALID
        assertEquals(false, gb.isValid(db, false));
    }
    @Ignore
    //TODO actualize the test
    @Test
    public void parseGenesisBlock() {
        //gb.process();

        //CONVERT TO BYTES
        byte[] rawBlock = gb.toBytes(true, forDB);
        //CHECK length
        assertEquals(rawBlock.length, gb.getDataLength(forDB));

        Block parsedBlock = null;
        try {
            //PARSE FROM BYTES
            parsedBlock = BlockFactory.getInstance().parse(rawBlock, 1);

        } catch (Exception e) {
            fail("Exception while parsing transaction." + e);
        }

        //CHECK length
        assertEquals(rawBlock.length, parsedBlock.getDataLength(forDB));

        //CHECK SIGNATURE
        assertEquals(true, Arrays.equals(gb.getSignature(), parsedBlock.getSignature()));

        //CHECK TRANSACTION COUNT
        assertEquals(gb.getTransactionCount(), parsedBlock.getTransactionCount());

        //CHECK REFERENCE
        assertEquals(true, Arrays.equals(gb.getReference(), parsedBlock.getReference()));

        //CHECK GENERATOR
        assertEquals(gb.getCreator().getAddress(), parsedBlock.getCreator().getAddress());

        Transaction tx = gb.getTransaction(gb.getTransactionCount());
        Transaction txParsed = parsedBlock.getTransaction(parsedBlock.getTransactionCount());
        assertEquals(tx.getFee(), txParsed.getFee());

        //CHECK INSTANCE
        ////assertEquals(true, parsedBlock instanceof GenesisBlock);

        //PARSE TRANSACTION FROM WRONG BYTES
        rawBlock = new byte[50];

        try {
            //PARSE FROM BYTES
            BlockFactory.getInstance().parse(rawBlock, 1);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }
    @Ignore
    //TODO actualize the test
    @Test
    public void processGenesisBlock() {

        //PROCESS GENESISBLOCK
        GenesisBlock genesisBlock = new GenesisBlock();

        //CHECK VALID
        assertEquals(true, genesisBlock.isSignatureValid());
        assertEquals(true, genesisBlock.isValid(db, false));

        //PROCESS BLOCK
        try {
            genesisBlock.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Account recipient1 = new Account("73CcZe3PhwvqMvWxDznLAzZBrkeTZHvNzo");
        Account recipient2 = new Account("7FUUEjDSo9J4CYon4tsokMCPmfP4YggPnd");

        //CHECK LAST REFERENCE GENERATOR
        assertEquals((long) recipient1.getLastTimestamp(db)[0], 0);
        assertEquals((long) recipient2.getLastTimestamp(db)[0], 0);

        //CHECK BALANCE RECIPIENT 1
        assertEquals(0, recipient1.getBalanceUSE(ERM_KEY, db).compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, recipient1.getBalanceUSE(FEE_KEY, db).compareTo(BigDecimal.valueOf(0.0)));

        //CHECK BALANCE RECIPIENT2
        assertEquals(0, recipient2.getBalanceUSE(ERM_KEY, db).compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, recipient2.getBalanceUSE(FEE_KEY, db).compareTo(BigDecimal.valueOf(0.0)));

        int height = genesisBlock.getHeight() + 1;
        Tuple3<Integer, Integer, Integer> forgingData = recipient1.getForgingData(db, height);
        assertEquals(-1, (int) forgingData.a);

        forgingData = recipient2.getForgingData(db, height);
        assertEquals(-1, (int) forgingData.a);

        //ORPHAN BLOCK
        try {
            genesisBlock.orphan(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assertEquals(true, recipient1.getLastTimestamp(db)[0] == 0l);
        assertEquals(true, recipient2.getLastTimestamp(db)[0] == 0l);

        //CHECK BALANCE RECIPIENT 1
        assertEquals(recipient1.getBalanceUSE(ERM_KEY, db), BigDecimal.valueOf(0));
        assertEquals(recipient1.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(0));
        //CHECK BALANCE RECIPIENT 2
        assertEquals(true, recipient2.getBalanceUSE(ERM_KEY, db).compareTo(BigDecimal.valueOf(0)) == 0);
        assertEquals(true, recipient2.getBalanceUSE(FEE_KEY, db).compareTo(BigDecimal.valueOf(0)) == 0);

    }

    ////////////////
    @Ignore
    //TODO actualize the test
    @Test
    public void validateSignatureBlock() {

        init();
        try {
            gb.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        //PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
        //Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000), NTP.getTime());
        //transaction.process(databaseSet, false);

        //GENERATE NEXT BLOCK
        //BigDecimal genBal = generator.getGeneratingBalance(db);
        BlockGenerator blockGenerator = new BlockGenerator(db, null, false);
        Block newBlock = blockGenerator.generateNextBlock(generator, gb,
                orderedTransactions,
                1000, 1000l, 1000l);
        newBlock.sign(generator);

        ////ADD TRANSACTION SIGNATURE
        ///newBlock.makeTransactionsHash();

        //CHECK IF SIGNATURE VALID
        assertEquals(true, newBlock.isSignatureValid());

        //INVALID TRANSACTION HASH
        Transaction payment = new RSend(generator, FEE_POWER, generator, FEE_KEY, BigDecimal.valueOf(1), timestamp, generator.getLastTimestamp(db)[0]);
        payment.sign(generator, Transaction.FOR_NETWORK);
        transactions.add(payment);

        // SET TRANSACTIONS to BLOCK
        newBlock.setTransactionsForTests(transactions);

        //CHECK IF SIGNATURE INVALID
        assertEquals(false, newBlock.isSignatureValid());

        //INVALID GENERATOR SIGNATURE
        //newBlock = BlockFactory.getInstance().create(newBlock.getVersion(), newBlock.getReference(), generator, new byte[Crypto.HASH_LENGTH], new byte[0]);
        newBlock = blockGenerator.generateNextBlock(generator, gb,
                orderedTransactions,
                1000, 1000l, 1000l);
        newBlock.sign(generator);
        newBlock.setTransactionsForTests(transactions);

        ///CHECK IF SIGNATURE INVALID
        assertEquals(false, newBlock.isSignatureValid());

        //VALID TRANSACTION SIGNATURE
        newBlock = blockGenerator.generateNextBlock(generator, gb,
                orderedTransactions,
                1000, 1000l, 1000l);

        //ADD TRANSACTION
        Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");
        long timestamp = newBlock.getTimestamp();
        payment = new RSend(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100), timestamp, generator.getLastTimestamp(db)[0]);
        payment.sign(generator, Transaction.FOR_NETWORK);
        assertEquals(Transaction.VALIDATE_OK, payment.isValid(Transaction.FOR_NETWORK, flags));
        transactions = new ArrayList<Transaction>();
        transactions.add(payment);

        //ADD TRANSACTION SIGNATURE
        newBlock.setTransactionsForTests(transactions);

        //CHECK VALID TRANSACTION SIGNATURE
        assertEquals(false, newBlock.isSignatureValid());

        newBlock.sign(generator);
        //CHECK VALID TRANSACTION SIGNATURE
        assertEquals(true, newBlock.isSignatureValid());

        //INVALID TRANSACTION SIGNATURE
        newBlock = blockGenerator.generateNextBlock(generator, gb,
                orderedTransactions,
                1000, 1000l, 1000l);

        //ADD TRANSACTION
        payment = new RSend(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(200), NTP.getTime(), generator.getLastTimestamp(db)[0], payment.getSignature());
        transactions = new ArrayList<Transaction>();
        transactions.add(payment);

        //ADD TRANSACTION SIGNATURE
        newBlock.setTransactionsForTests(transactions);
        newBlock.sign(generator);

        //CHECK INVALID TRANSACTION SIGNATURE
        assertEquals(false, newBlock.isValid(db, false));
        // BUT valid HERE
        assertEquals(true, newBlock.isSignatureValid());
    }

    @Test
    public void validateBlock() {
        init();

        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);

        Transaction transaction;

        //PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		/*
		transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000), NTP.getTime());
		transaction.process(databaseSet);
		 */

        // (issuer, recipient, 0l, bdAmount, timestamp)
        // need add VOLUME for generating new block - 0l asset!
        transaction = new GenesisTransferAssetTransaction(generator,
                ERM_KEY, BigDecimal.valueOf(100000));
        transaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        transaction.process(gb, Transaction.FOR_NETWORK);
        transaction = new GenesisTransferAssetTransaction(generator,
                FEE_KEY, BigDecimal.valueOf(1000));
        transaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        transaction.process(gb, Transaction.FOR_NETWORK);

        //GENERATE NEXT BLOCK
        //BigDecimal genBal = generator.getGeneratingBalance(db);
        BlockGenerator blockGenerator = new BlockGenerator(db, null, false);
        Block newBlock = blockGenerator.generateNextBlock(generator, gb,
                orderedTransactions,
                1000, 1000l, 1000l);

        // SET WIN VALUE and TARGET
        newBlock.makeHeadMind(db);

        //CHECK IF VALID
        assertEquals(true, newBlock.isValid(db, false));

        //CHANGE REFERENCE
        ////Block invalidBlock = BlockFactory.getInstance().create(newBlock.getVersion(), new byte[128], newBlock.getCreator(), transactionsHash, atBytes);
        Block invalidBlock = blockGenerator.generateNextBlock(generator, gb,
                orderedTransactions,
                1000, 1000l, 1000l);

        invalidBlock.setReferenceForTests(new byte[Block.SIGNATURE_LENGTH]);
        invalidBlock.sign(generator);

        //CHECK IF INVALID
        assertEquals(false, invalidBlock.isValid(db, false));

        //VRON NUMBER
        invalidBlock = blockGenerator.generateNextBlock(generator, gb,
                orderedTransactions,
                1000, 1000l, 1000l);
        //CHECK IF INVALID
        assertEquals(false, invalidBlock.isValid(db, false));

        //ADD INVALID TRANSACTION
        invalidBlock = blockGenerator.generateNextBlock(generator, gb,
                orderedTransactions,
                1000, 1000l, 1000l);
        Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");
        long timestamp = newBlock.getTimestamp();
        Transaction payment = new RSend(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(-100), timestamp, generator.getLastTimestamp(db)[0]);
        payment.sign(generator, Transaction.FOR_NETWORK);

        transactions = new ArrayList<Transaction>();
        transactions.add(payment);

        //ADD TRANSACTION SIGNATURE
        invalidBlock.setTransactionsForTests(transactions);
        invalidBlock.sign(generator);

        //CHECK IF INVALID
        assertEquals(false, invalidBlock.isValid(db, false));

        //ADD GENESIS TRANSACTION
        invalidBlock = blockGenerator.generateNextBlock(generator, gb,
                orderedTransactions,
                1000, 1000l, 1000l);

        //transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000), newBlock.getTimestamp());
        transaction = new GenesisIssueAssetTransaction(GenesisBlock.makeAsset(Transaction.RIGHTS_KEY));
        transactions.add(transaction);

        //ADD TRANSACTION SIGNATURE
        invalidBlock.setTransactionsForTests(transactions);
        invalidBlock.sign(generator);

        //CHECK IF INVALID
        assertEquals(false, invalidBlock.isValid(db, false));
    }

    @Test
    public void parseBlock() {
        init();
        try {
            gb.process(db);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);

        //PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
        //Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000), NTP.getTime());
        //transaction.process(databaseSet, false);
        generator.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        generator.changeBalance(db, false, false, ERM_KEY, BigDecimal.valueOf(1000), false, false, false);
        generator.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1000), false, false, false);


        //GENERATE NEXT BLOCK
        Block block = blockGenerator.generateNextBlock(generator, gb,
                orderedTransactions,
                1000, 1000l, 1000l);

        //FORK
        DCSet fork = db.fork(this.toString());

        //GENERATE PAYMENT 1
        Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");
        long timestamp = block.getTimestamp();
        Transaction payment1 = new RSend(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100), timestamp, generator.getLastTimestamp(db)[0]);
        payment1.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        payment1.sign(generator, Transaction.FOR_NETWORK);
        assertEquals(Transaction.VALIDATE_OK, payment1.isValid(Transaction.FOR_NETWORK, flags));

        //payment1.process(fork);
        transactions = new ArrayList<Transaction>();
        transactions.add(payment1);


        //GENERATE PAYMENT 2
        Account recipient2 = new Account("7AfGz1FJ6tUnxxKSAHfcjroFEm8jSyVm7r");
        Transaction payment2 = new RSend(generator, FEE_POWER, recipient2, FEE_KEY, BigDecimal.valueOf(100), timestamp, generator.getLastTimestamp(fork)[0]);
        payment2.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        payment2.sign(generator, Transaction.FOR_NETWORK);
        assertEquals(Transaction.VALIDATE_OK, payment2.isValid(Transaction.FOR_NETWORK, flags));

        transactions.add(payment2);

        //ADD TRANSACTION SIGNATURE
        block.setTransactionsForTests(transactions);
        block.sign(generator);

        //CONVERT TO BYTES
        byte[] rawBlock = block.toBytes(true, forDB);

        Block parsedBlock = null;
        try {
            //PARSE FROM BYTES
            parsedBlock = BlockFactory.getInstance().parse(rawBlock, 0);
        } catch (Exception e) {
            fail("Exception while parsing transaction.");
        }

        //CHECK INSTANCE
        assertEquals(false, parsedBlock instanceof GenesisBlock);

        //CHECK SIGNATURE
        assertEquals(true, Arrays.equals(block.getSignature(), parsedBlock.getSignature()));

        //CHECK GENERATOR
        assertEquals(block.getCreator().getAddress(), parsedBlock.getCreator().getAddress());

        //CHECK REFERENCE
        assertEquals(true, Arrays.equals(block.getReference(), parsedBlock.getReference()));

        //CHECK TRANSACTIONS COUNT
        assertEquals(block.getTransactionCount(), parsedBlock.getTransactionCount());

        //CHECK REFERENCE
        assertEquals(true, Arrays.equals(block.getTransactionsHash(), parsedBlock.getTransactionsHash()));

        //PARSE TRANSACTION FROM WRONG BYTES
        rawBlock = new byte[50];

        try {
            //PARSE FROM BYTES
            BlockFactory.getInstance().parse(rawBlock, 3);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }

    @Ignore
    //TODO actualize the test
    @Test
    public void processBlock() {

        init();
        // already processed gb.process(db);

        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);

        //PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS for generate
        //Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000), NTP.getTime());
        //transaction.process(databaseSet, false);
        generator.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        generator.changeBalance(db, false, false, ERM_KEY, BigDecimal.valueOf(100000), false, false, false);
        generator.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1000), false, false, false);

        //GENERATE NEXT BLOCK
        Block block = blockGenerator.generateNextBlock(generator, gb,
                orderedTransactions,
                1000, 1000l, 1000l);

        //FORK
        DCSet fork = db.fork(this.toString());

        //GENERATE PAYMENT 1
        Account recipient1 = new Account("7JU8UTuREAJG2yht5ASn7o1Ur34P1nvTk5");
        // TIMESTAMP for org.erachain.records make lower
        long timestamp = block.getTimestamp() - 1000;
        Transaction payment1 = new RSend(generator, FEE_POWER, recipient1, FEE_KEY, BigDecimal.valueOf(100), timestamp++, generator.getLastTimestamp(fork)[0]);
        payment1.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        payment1.sign(generator, Transaction.FOR_NETWORK);
        assertEquals(Transaction.VALIDATE_OK, payment1.isValid(Transaction.FOR_NETWORK, flags));

        payment1.process(block, Transaction.FOR_NETWORK);

        transactions.add(payment1);

        //GENERATE PAYMENT 2
        Account recipient2 = new Account("7G1G45RX4td59daBv6PoN84nAJA49NZ47i");
        Transaction payment2 = new RSend(generator, FEE_POWER, recipient2, ERM_KEY,
                BigDecimal.valueOf(10), timestamp++, generator.getLastTimestamp(fork)[0]);
        payment2.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        payment2.sign(generator, Transaction.FOR_NETWORK);
        assertEquals(Transaction.VALIDATE_OK, payment2.isValid(Transaction.FOR_NETWORK, flags));

        transactions.add(payment2);

        //ADD TRANSACTION SIGNATURE
        block.setTransactionsForTests(transactions);

        ////generator.setLastForgingData(db, block.getHeightByParent(db));
        generator.setForgingData(db, block.getHeight(), payment2.getAmount().intValue());
        //block.setCalcGeneratingBalance(db);
        block.sign(generator);

        //CHECK VALID
        assertEquals(true, block.isSignatureValid());
        assertEquals(true, block.isValid(db, false));

        //PROCESS BLOCK
        try {
            block.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //CHECK BALANCE GENERATOR
        assertEquals(generator.getBalanceUSE(ERM_KEY, db), BigDecimal.valueOf(100990));
        //assertEquals(generator.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(900.00009482));
        assertEquals(generator.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(1900.0000));

        //CHECK LAST REFERENCE GENERATOR
        assertEquals((long) generator.getLastTimestamp(db)[0], (long) payment2.getTimestamp());

        //CHECK BALANCE RECIPIENT 1
        assertEquals(recipient1.getBalanceUSE(ERM_KEY, db), BigDecimal.valueOf(0));
        assertEquals(recipient1.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(100));

        //CHECK LAST REFERENCE RECIPIENT 1
        //assertEquals((long)recipient1.getLastReference(db), (long)payment1.getTimestamp());
        assertEquals((long) recipient1.getLastTimestamp(db)[0], 0l);

        //CHECK BALANCE RECIPIENT2
        assertEquals(recipient2.getBalanceUSE(ERM_KEY, db), BigDecimal.valueOf(10));
        assertEquals(recipient2.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(0));

        //CHECK LAST REFERENCE RECIPIENT 2
        assertNotEquals((long)recipient2.getLastTimestamp(db)[0], (long)payment2.getTimestamp());

        //CHECK TOTAL FEE
        assertEquals(block.blockHead.totalFee, BigDecimal.valueOf(0.00065536));

        //CHECK TOTAL TRANSACTIONS
        assertEquals(2, block.getTransactionCount());

        //CHECK LAST BLOCK
        assertEquals(true, Arrays.equals(block.getSignature(), db.getBlockMap().last().getSignature()));


        ////////////////////////////////////
        //ORPHAN BLOCK
        //////////////////////////////////
        try {
            block.orphan(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //CHECK BALANCE GENERATOR
        assertEquals(generator.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(2000));

        //CHECK LAST REFERENCE GENERATOR
        assertEquals((long) generator.getLastTimestamp(db)[0], gb.getTimestamp());

        //CHECK BALANCE RECIPIENT 1
        assertEquals(recipient1.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(0));

        //CHECK LAST REFERENCE RECIPIENT 1
        assertNotEquals(recipient1.getLastTimestamp(db), payment1.getTimestamp());

        //CHECK BALANCE RECIPIENT 2
        assertEquals(true, recipient2.getBalanceUSE(FEE_KEY, db).compareTo(BigDecimal.valueOf(0)) == 0);

        //CHECK LAST REFERENCE RECIPIENT 2
        assertEquals((long) recipient2.getLastTimestamp(db)[0], (long) 0);

        //CHECK LAST BLOCK
        assertEquals(true, Arrays.equals(gb.getSignature(), db.getBlockMap().last().getSignature()));
    }
}
