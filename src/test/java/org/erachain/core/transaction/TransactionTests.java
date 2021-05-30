package org.erachain.core.transaction;

import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.voting.Poll;
import org.erachain.core.voting.PollOption;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

//import org.apache.log4j.PropertyConfigurator;

//import org.erachain.core.transaction.MultiPaymentTransaction;
@Ignore
public class TransactionTests {

    static Logger LOGGER = LoggerFactory.getLogger(TransactionTests.class.getName());
    //Long Transaction.FOR_NETWORK = null;
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    byte FEE_POWER = (byte) 1;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();
    long last_ref;
    long new_ref;

    int forDeal = Transaction.FOR_NETWORK;

    byte[] itemAppData = null;
    long txFlags = 0L;

    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    byte[] seed_b = Crypto.getInstance().digest("buyer".getBytes());
    byte[] privateKey_b = Crypto.getInstance().createKeyPair(seed_b).getA();
    PrivateKeyAccount buyer = new PrivateKeyAccount(privateKey_b);
    Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
    DCSet databaseSet;
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;
    Block block;

    // INIT ASSETS
    private void init() {

        File log4j = new File("log4j_test.properties");
        System.out.println(log4j.getAbsolutePath());
        if (log4j.exists()) {
            System.out.println("configured");
            //PropertyConfigurator.configure(log4j.getAbsolutePath());
        }

        databaseSet = db = DCSet.createEmptyDatabaseSet(0);
        gb = new GenesisBlock();
        block = gb;

        try {
            gb.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        last_ref = gb.getTimestamp();

        // FEE FUND
        maker.setLastTimestamp(new long[]{last_ref, 0}, db);
        maker.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        new_ref = maker.getLastTimestamp(db)[0];

        buyer.setLastTimestamp(new long[]{last_ref, 0}, db);
        buyer.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        buyer.changeBalance(db, false, false, ERM_KEY, BigDecimal.valueOf(2000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false); // for bye


    }


    //PAYMENT

    @Test
    public void validateSignatureR_Send() {
String  s= "";
        init();

        //CREATE PAYMENT
        Transaction payment = new RSend(maker, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, last_ref);
        payment.sign(maker, Transaction.FOR_NETWORK);
        //CHECK IF PAYMENT SIGNATURE IS VALID
        assertEquals(true, payment.isSignatureValid(db));

        //INVALID SIGNATURE
        payment = new RSend(maker, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp + 1, last_ref, new byte[64]);

        //CHECK IF PAYMENT SIGNATURE IS INVALID
        assertEquals(false, payment.isSignatureValid(db));
    }

    @Test
    public void validateR_Send() {

        init();


        //CREATE VALID PAYMENT
        Transaction payment = new RSend(maker, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(0.5).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db)[0]);
        assertEquals(Transaction.VALIDATE_OK, payment.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID PAYMENT INVALID RECIPIENT ADDRESS
        payment = new RSend(maker, FEE_POWER, new Account("test"), FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db)[0] + 10);
        assertEquals(Transaction.INVALID_ADDRESS, payment.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID PAYMENT NEGATIVE AMOUNT
        payment = new RSend(maker, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(-100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db)[0]);
        assertEquals(Transaction.NEGATIVE_AMOUNT, payment.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID PAYMENT WRONG REFERENCE
        payment = new RSend(maker, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, -123L, new byte[64]);
        assertEquals(Transaction.INVALID_REFERENCE, payment.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID PAYMENT WRONG TIMESTAMP
        payment = new RSend(maker, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getLastTimestamp(db)[0], maker.getLastTimestamp(db)[0]);
        assertEquals(Transaction.INVALID_TIMESTAMP, payment.isValid(Transaction.FOR_NETWORK, txFlags));
        payment = new RSend(maker, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getLastTimestamp(db)[0] - 10, maker.getLastTimestamp(db)[0]);
        assertEquals(Transaction.INVALID_TIMESTAMP, payment.isValid(Transaction.FOR_NETWORK, txFlags));

    }

    @Test
    public void parseR_Send() {
        init();

        //CREATE VALID PAYMENT
        Transaction payment = new RSend(maker, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db)[0]);
        payment.sign(maker, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawPayment = payment.toBytes(Transaction.FOR_NETWORK, true);

        try {
            //PARSE FROM BYTES
            RSend parsedPayment = (RSend) TransactionFactory.getInstance().parse(rawPayment, Transaction.FOR_NETWORK);

            //CHECK INSTANCE
            assertEquals(true, parsedPayment instanceof RSend);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(payment.getSignature(), parsedPayment.getSignature()));

            //CHECK AMOUNT SENDER
            assertEquals(payment.getAmount(maker), parsedPayment.getAmount(maker));

            //CHECK AMOUNT RECIPIENT
            assertEquals(payment.getAmount(recipient), parsedPayment.getAmount(recipient));

            //CHECK FEE
            assertEquals(payment.getFee(), parsedPayment.getFee());

            //CHECK REFERENCE
            //assertEquals(payment.getReference(), parsedPayment.getReference());

            //CHECK TIMESTAMP
            assertEquals(payment.getTimestamp(), parsedPayment.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction.");
        }

        //PARSE TRANSACTION FROM WRONG BYTES
        rawPayment = new byte[payment.getDataLength(Transaction.FOR_NETWORK, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawPayment, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }

    @Test
    public void processR_Send() {

        init();

        //CREATE PAYMENT
        BigDecimal amount = BigDecimal.valueOf(0.5).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
        Transaction payment = new RSend(maker, FEE_POWER, recipient, FEE_KEY, amount.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, last_ref);
        payment.sign(maker, Transaction.FOR_NETWORK);
        BigDecimal fee = payment.getFee();
        payment.process(gb, Transaction.FOR_NETWORK);

        LOGGER.info("getConfirmedBalance: " + maker.getBalanceUSE(FEE_KEY, databaseSet));
        LOGGER.info("getConfirmedBalance FEE_KEY:" + maker.getBalanceUSE(FEE_KEY, databaseSet));

        //CHECK BALANCE SENDER
        assertEquals(0, BigDecimal.valueOf(1).subtract(amount).subtract(fee).setScale(BlockChain.AMOUNT_DEDAULT_SCALE).compareTo(maker.getBalanceUSE(FEE_KEY, databaseSet)));

        //CHECK BALANCE RECIPIENT
        assertEquals(amount, recipient.getBalanceUSE(FEE_KEY, databaseSet));

        //CHECK REFERENCE SENDER
        assertEquals((long) maker.getLastTimestamp(databaseSet)[0], timestamp);

        //CHECK REFERENCE RECIPIENT
        assertEquals(payment.getTimestamp(), recipient.getLastTimestamp(databaseSet));
    }

    @Test
    public void orphanR_Send() {

        init();

        //CREATE PAYMENT
        Transaction payment = new RSend(maker, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, last_ref);
        payment.sign(maker, Transaction.FOR_NETWORK);
        payment.process(gb, Transaction.FOR_NETWORK);

        BigDecimal amount1 = maker.getBalanceUSE(FEE_KEY, databaseSet);
        BigDecimal amount2 = recipient.getBalanceUSE(FEE_KEY, databaseSet);

        //CREATE PAYMENT2
        Transaction payment2 = new RSend(maker, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, last_ref);
        payment2.sign(maker, Transaction.FOR_NETWORK);
        payment.process(gb, Transaction.FOR_NETWORK);

        //ORPHAN PAYMENT
        payment2.orphan(gb, Transaction.FOR_NETWORK);

        //CHECK BALANCE SENDER
        assertEquals(0, amount1.compareTo(maker.getBalanceUSE(FEE_KEY, databaseSet)));

        //CHECK BALANCE RECIPIENT
        assertEquals(amount2, recipient.getBalanceUSE(FEE_KEY, databaseSet));

        //CHECK REFERENCE SENDER
        assertEquals(maker.getLastTimestamp(databaseSet)[0], maker.getLastTimestamp(databaseSet)[0]);

        //CHECK REFERENCE RECIPIENT
        assertEquals(null, recipient.getLastTimestamp(databaseSet)[0]);

    }


    /////////////////////////////////////////////////
    //CREATE POLL

    @Test
    public void validateSignatureCreatePollTransaction() {

        init();

        //CREATE POLL
        Poll poll = new Poll(maker, "test", "this is the value", new ArrayList<PollOption>());

        //CREATE POLL CREATION
        Transaction pollCreation = new CreatePollTransaction(maker, poll, FEE_POWER, timestamp, last_ref);
        pollCreation.sign(maker, Transaction.FOR_NETWORK);
        //CHECK IF POLL CREATION IS VALID
        assertEquals(true, pollCreation.isSignatureValid(db));

        //INVALID SIGNATURE
        pollCreation = new CreatePollTransaction(
                maker, poll, FEE_POWER, timestamp, last_ref, new byte[64]);

        //CHECK IF NAME REGISTRATION IS INVALID
        assertEquals(false, pollCreation.isSignatureValid(db));
    }

    @Test
    public void validateCreatePollTransaction() {

        init();

        Poll poll = new Poll(maker, "test", "this is the value", Arrays.asList(new PollOption("test")));

        //CREATE POLL CREATION
        Transaction pollCreation = new CreatePollTransaction(maker, poll, FEE_POWER, timestamp, last_ref);
        pollCreation.sign(maker, Transaction.FOR_NETWORK);

        //CHECK IF POLL CREATION IS VALID
        assertEquals(Transaction.VALIDATE_OK, pollCreation.isValid(Transaction.FOR_NETWORK, txFlags));
        pollCreation.process(gb, Transaction.FOR_NETWORK);

        //CREATE INVALID POLL CREATION INVALID NAME LENGTH
        String longName = "";
        for (int i = 1; i < 1000; i++) {
            longName += "oke";
        }
        poll = new Poll(maker, longName, "this is the value", Arrays.asList(new PollOption("test")));
        pollCreation = new CreatePollTransaction(maker, poll, FEE_POWER, timestamp, last_ref);

        //CHECK IF POLL CREATION IS INVALID
        assertEquals(Transaction.INVALID_NAME_LENGTH_MAX, pollCreation.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID POLL CREATION INVALID DESCRIPTION LENGTH
        String longDescription = "";
        for (int i = 1; i < 10000; i++) {
            longDescription += "oke";
        }
        poll = new Poll(maker, "test2", longDescription, Arrays.asList(new PollOption("test")));
        pollCreation = new CreatePollTransaction(maker, poll, FEE_POWER, timestamp, last_ref);

        //CHECK IF POLL CREATION IS INVALID
        assertEquals(Transaction.INVALID_DESCRIPTION_LENGTH_MAX, pollCreation.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID POLL CREATION NAME ALREADY TAKEN
        poll = new Poll(maker, "test", "this is the value", Arrays.asList(new PollOption("test")));
        pollCreation = new CreatePollTransaction(maker, poll, FEE_POWER, timestamp, last_ref);

        //CHECK IF POLL CREATION IS INVALID
        assertEquals(Transaction.POLL_ALREADY_CREATED, pollCreation.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID POLL CREATION NO OPTIONS
        poll = new Poll(maker, "test2", "this is the value", new ArrayList<PollOption>());
        pollCreation = new CreatePollTransaction(maker, poll, FEE_POWER, timestamp, last_ref);

        //CHECK IF POLL CREATION IS INVALID
        assertEquals(Transaction.INVALID_OPTIONS_LENGTH, pollCreation.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID POLL CREATION INVALID OPTION LENGTH
        poll = new Poll(maker, "test2", "this is the value", Arrays.asList(new PollOption(longName)));
        pollCreation = new CreatePollTransaction(maker, poll, FEE_POWER, timestamp, last_ref);

        //CHECK IF POLL CREATION IS INVALID
        assertEquals(Transaction.INVALID_OPTION_LENGTH, pollCreation.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID POLL CREATION INVALID DUPLICATE OPTIONS
        poll = new Poll(maker, "test2", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("test")));
        pollCreation = new CreatePollTransaction(maker, poll, FEE_POWER, timestamp, last_ref);

        //CHECK IF POLL CREATION IS INVALID
        assertEquals(Transaction.DUPLICATE_OPTION, pollCreation.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID POLL CREATION NOT ENOUGH BALANCE
        seed = Crypto.getInstance().digest("invalid".getBytes());
        privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
        invalidOwner.setLastTimestamp(new long[]{last_ref, 0}, databaseSet);
        poll = new Poll(maker, "test2", "this is the value", Arrays.asList(new PollOption("test")));
        pollCreation = new CreatePollTransaction(invalidOwner, poll, FEE_POWER, timestamp, invalidOwner.getLastTimestamp(databaseSet)[0]);
        // need for calc FEE
        pollCreation.sign(invalidOwner, Transaction.FOR_NETWORK);

        //CHECK IF POLL CREATION IS INVALID
        assertEquals(Transaction.NOT_ENOUGH_FEE, pollCreation.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE POLL CREATION INVALID REFERENCE
        poll = new Poll(maker, "test2", "this is the value", Arrays.asList(new PollOption("test")));
        pollCreation = new CreatePollTransaction(maker, poll, FEE_POWER, timestamp, invalidOwner.getLastTimestamp(databaseSet)[0]);
        assertEquals(Transaction.INVALID_REFERENCE, pollCreation.isValid(Transaction.FOR_NETWORK, txFlags));

    }

    @Test
    public void parseCreatePollTransaction() {

        init();

        Poll poll = new Poll(maker, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("second option")));

        //CREATE POLL CREATION
        CreatePollTransaction pollCreation = new CreatePollTransaction(maker, poll, FEE_POWER, timestamp, last_ref);
        pollCreation.sign(maker, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawPollCreation = pollCreation.toBytes(Transaction.FOR_NETWORK, true);

        try {
            //PARSE FROM BYTES
            CreatePollTransaction parsedPollCreation = (CreatePollTransaction) TransactionFactory.getInstance().parse(rawPollCreation, Transaction.FOR_NETWORK);

            //CHECK INSTANCE
            assertEquals(true, parsedPollCreation instanceof CreatePollTransaction);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(pollCreation.getSignature(), parsedPollCreation.getSignature()));

            //CHECK AMOUNT CREATOR
            assertEquals(pollCreation.getAmount(maker), parsedPollCreation.getAmount(maker));

            //CHECK POLL CREATOR
            assertEquals(pollCreation.getPoll().getCreator().getAddress(), parsedPollCreation.getPoll().getCreator().getAddress());

            //CHECK POLL NAME
            assertEquals(pollCreation.getPoll().getName(), parsedPollCreation.getPoll().getName());

            //CHECK POLL DESCRIPTION
            assertEquals(pollCreation.getPoll().getDescription(), parsedPollCreation.getPoll().getDescription());

            //CHECK POLL OPTIONS SIZE
            assertEquals(pollCreation.getPoll().getOptions().size(), parsedPollCreation.getPoll().getOptions().size());

            //CHECK POLL OPTIONS
            for (int i = 0; i < pollCreation.getPoll().getOptions().size(); i++) {
                //CHECK OPTION NAME
                assertEquals(pollCreation.getPoll().getOptions().get(i).getName(), parsedPollCreation.getPoll().getOptions().get(i).getName());
            }

            //CHECK FEE
            assertEquals(pollCreation.getFee(), parsedPollCreation.getFee());

            //CHECK REFERENCE
            //assertEquals(pollCreation.getReference(), parsedPollCreation.getReference());

            //CHECK TIMESTAMP
            assertEquals(pollCreation.getTimestamp(), parsedPollCreation.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction.");
        }

        //PARSE TRANSACTION FROM WRONG BYTES
        rawPollCreation = new byte[pollCreation.getDataLength(Transaction.FOR_NETWORK, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawPollCreation, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }



    //VOTE ON POLL

    @Test
    public void validateSignatureVoteOnPollTransaction() {

        init();

        //CREATE POLL VOTE
        Transaction pollVote = new VoteOnPollTransaction(maker, "test", 5, FEE_POWER, timestamp, last_ref);
        pollVote.sign(maker, Transaction.FOR_NETWORK);
        //CHECK IF POLL VOTE IS VALID
        assertEquals(true, pollVote.isSignatureValid(db));

        //INVALID SIGNATURE
        pollVote = new VoteOnPollTransaction(
                maker, "test", 5, FEE_POWER, timestamp, last_ref, new byte[64]);

        //CHECK IF POLL VOTE IS INVALID
        assertEquals(false, pollVote.isSignatureValid(db));
    }

    @Test
    public void validateVoteOnPollTransaction() {

        init();

        //CREATE SIGNATURE
        //logger.info("asdasd");
        long timestamp = NTP.getTime();
        Poll poll = new Poll(maker, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("test2")));
        //CREATE POLL CREATION
        Transaction pollCreation = new CreatePollTransaction(maker, poll, FEE_POWER, timestamp, last_ref);
        pollCreation.sign(maker, Transaction.FOR_NETWORK);

        //CHECK IF POLL CREATION IS VALID
        assertEquals(Transaction.VALIDATE_OK, pollCreation.isValid(Transaction.FOR_NETWORK, txFlags));
        pollCreation.process(gb, Transaction.FOR_NETWORK);

        //CREATE POLL VOTE
        Transaction pollVote = new VoteOnPollTransaction(maker, poll.getName(), 0, FEE_POWER, timestamp + 100, maker.getLastTimestamp(databaseSet)[0]);
        pollVote.sign(maker, Transaction.FOR_NETWORK);

        //CHECK IF POLL VOTE IS VALID
        assertEquals(Transaction.VALIDATE_OK, pollVote.isValid(Transaction.FOR_NETWORK, txFlags));
        //pollVote.process(databaseSet, false);

        //CREATE INVALID POLL VOTE INVALID NAME LENGTH
        String longName = "";
        for (int i = 1; i < 1000; i++) {
            longName += "oke";
        }
        pollVote = new VoteOnPollTransaction(maker, longName, 0, FEE_POWER, timestamp, last_ref);

        //CHECK IF POLL VOTE IS INVALID
        assertEquals(Transaction.INVALID_NAME_LENGTH_MAX, pollVote.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID POLL VOTE POLL DOES NOT EXIST
        pollVote = new VoteOnPollTransaction(maker, "test2", 0, FEE_POWER, timestamp, last_ref);

        //CHECK IF POLL VOTE IS INVALID
        assertEquals(Transaction.POLL_NOT_EXISTS, pollVote.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID POLL VOTE INVALID OPTION
        pollVote = new VoteOnPollTransaction(maker, "test", 5, FEE_POWER, timestamp, last_ref);

        //CHECK IF POLL VOTE IS INVALID
        assertEquals(Transaction.POLL_OPTION_NOT_EXISTS, pollVote.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID POLL VOTE INVALID OPTION
        pollVote = new VoteOnPollTransaction(maker, "test", -1, FEE_POWER, timestamp, last_ref);

        //CHECK IF POLL VOTE IS INVALID
        assertEquals(Transaction.POLL_OPTION_NOT_EXISTS, pollVote.isValid(Transaction.FOR_NETWORK, txFlags));

        //CRTEATE INVALID POLL VOTE VOTED ALREADY
        pollVote = new VoteOnPollTransaction(maker, "test", 0, FEE_POWER, timestamp, last_ref);
        pollVote.sign(maker, Transaction.FOR_NETWORK);
        pollVote.process(gb, Transaction.FOR_NETWORK);

        //CHECK IF POLL VOTE IS INVALID
        assertEquals(Transaction.ALREADY_VOTED_FOR_THAT_OPTION, pollVote.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID POLL VOTE NOT ENOUGH BALANCE
        seed = Crypto.getInstance().digest("invalid".getBytes());
        privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
        invalidOwner.setLastTimestamp(new long[]{timestamp, 0}, databaseSet);
        pollVote = new VoteOnPollTransaction(invalidOwner, "test", 0, FEE_POWER, timestamp, last_ref);
        pollVote.sign(invalidOwner, Transaction.FOR_NETWORK);


        //CHECK IF POLL VOTE IS INVALID
        ///logger.info("pollVote.getFee: " + pollVote.getFee());
        /// fee = 0 assertEquals(Transaction.NOT_ENOUGH_FEE, pollVote.isValid(databaseSet));

        //CREATE POLL CREATION INVALID REFERENCE
        pollVote = new VoteOnPollTransaction(maker, "test", 1, FEE_POWER, timestamp, invalidOwner.getLastTimestamp(databaseSet)[0] + 1);
        assertEquals(Transaction.INVALID_REFERENCE, pollVote.isValid(Transaction.FOR_NETWORK, txFlags));

    }


    @Test
    public void parseVoteOnPollTransaction() {

        init();

        //CREATE POLL Vote
        VoteOnPollTransaction pollVote = new VoteOnPollTransaction(maker, "test", 0, FEE_POWER, timestamp, last_ref);
        pollVote.sign(maker, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawPollVote = pollVote.toBytes(Transaction.FOR_NETWORK, true);
        assertEquals(rawPollVote.length, pollVote.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            VoteOnPollTransaction parsedPollVote = (VoteOnPollTransaction) TransactionFactory.getInstance().parse(rawPollVote, Transaction.FOR_NETWORK);

            //CHECK INSTANCE
            assertEquals(true, parsedPollVote instanceof VoteOnPollTransaction);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(pollVote.getSignature(), parsedPollVote.getSignature()));

            //CHECK AMOUNT CREATOR
            assertEquals(pollVote.getAmount(maker), parsedPollVote.getAmount(maker));

            //CHECK CREATOR
            assertEquals(pollVote.getCreator().getAddress(), parsedPollVote.getCreator().getAddress());

            //CHECK POLL
            assertEquals(pollVote.getPoll(), parsedPollVote.getPoll());

            //CHECK POLL OPTION
            assertEquals(pollVote.getOption(), parsedPollVote.getOption());

            //CHECK FEE
            assertEquals(pollVote.getFee(), parsedPollVote.getFee());

            //CHECK REFERENCE
            //assertEquals(pollVote.getReference(), parsedPollVote.getReference());

            //CHECK TIMESTAMP
            assertEquals(pollVote.getTimestamp(), parsedPollVote.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

        //PARSE TRANSACTION FROM WRONG BYTES
        rawPollVote = new byte[pollVote.getDataLength(Transaction.FOR_NETWORK, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawPollVote, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }

    //ARBITRARY TRANSACTION

    @Test
    public void validateSignatureArbitraryTransaction() {

        init();

        //CREATE ARBITRARY TRANSACTION
        Transaction arbitraryTransaction = new ArbitraryTransactionV3(maker, null, 4889, "test".getBytes(), FEE_POWER, timestamp, last_ref);
        arbitraryTransaction.sign(maker, Transaction.FOR_NETWORK);

        //CHECK IF ARBITRARY TRANSACTION IS VALID
        assertEquals(true, arbitraryTransaction.isSignatureValid(db));

        //INVALID SIGNATURE
        arbitraryTransaction = new ArbitraryTransactionV3(
                maker, null, 4889, "test".getBytes(), FEE_POWER, timestamp, last_ref, new byte[64]);
        //arbitraryTransaction.sign(maker);
        //CHECK IF ARBITRARY TRANSACTION IS INVALID
        assertEquals(false, arbitraryTransaction.isSignatureValid(db));
    }

    @Test
    public void validateArbitraryTransaction() {

        init();

        byte[] data = "test".getBytes();

        //CREATE ARBITRARY TRANSACTION
        Transaction arbitraryTransaction = new ArbitraryTransactionV3(maker, null, 4776, data, FEE_POWER, timestamp, last_ref);
        arbitraryTransaction.sign(maker, Transaction.FOR_NETWORK);

        //CHECK IF ARBITRARY TRANSACTION IS VALID
        assertEquals(Transaction.VALIDATE_OK, arbitraryTransaction.isValid(Transaction.FOR_NETWORK, txFlags));
        arbitraryTransaction.process(gb, Transaction.FOR_NETWORK);

        //CREATE INVALID ARBITRARY TRANSACTION INVALID data LENGTH
        byte[] longData = new byte[5000];
        arbitraryTransaction = new ArbitraryTransactionV3(maker, null, 4776, longData, FEE_POWER, timestamp, last_ref);

        //CHECK IF ARBITRARY TRANSACTION IS INVALID
        assertEquals(Transaction.INVALID_DATA_LENGTH, arbitraryTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID ARBITRARY TRANSACTION NOT ENOUGH BALANCE
        seed = Crypto.getInstance().digest("invalid".getBytes());
        privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
        arbitraryTransaction = new ArbitraryTransactionV3(invalidOwner, null, 4776, data, FEE_POWER, timestamp, last_ref);

        //CHECK IF ARBITRARY TRANSACTION IS INVALID
        assertEquals(Transaction.NO_BALANCE, arbitraryTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE ARBITRARY TRANSACTION INVALID REFERENCE
        arbitraryTransaction = new ArbitraryTransactionV3(maker, null, 4776, data, FEE_POWER, timestamp, invalidOwner.getLastTimestamp(databaseSet)[0]);

        //CHECK IF ARBITRARY TRANSACTION IS INVALID
        assertEquals(Transaction.INVALID_REFERENCE, arbitraryTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

    }


    @Test
    public void parseArbitraryTransaction() {

        init();

        //CREATE ARBITRARY TRANSACTION
        ArbitraryTransactionV3 arbitraryTransaction = new ArbitraryTransactionV3(maker, null, 4776, "test".getBytes(), FEE_POWER, timestamp, last_ref);
        arbitraryTransaction.sign(maker, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawArbitraryTransaction = arbitraryTransaction.toBytes(Transaction.FOR_NETWORK, true);

        try {
            //PARSE FROM BYTES
            ArbitraryTransactionV3 parsedArbitraryTransaction = (ArbitraryTransactionV3) TransactionFactory.getInstance().parse(rawArbitraryTransaction, Transaction.FOR_NETWORK);

            //CHECK INSTANCE
            assertEquals(true, parsedArbitraryTransaction instanceof ArbitraryTransactionV3);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(arbitraryTransaction.getSignature(), parsedArbitraryTransaction.getSignature()));

            //CHECK AMOUNT CREATOR
            assertEquals(arbitraryTransaction.getAmount(maker), parsedArbitraryTransaction.getAmount(maker));

            //CHECK CREATOR
            assertEquals(arbitraryTransaction.getCreator().getAddress(), parsedArbitraryTransaction.getCreator().getAddress());

            //CHECK VERSION
            assertEquals(arbitraryTransaction.getService(), parsedArbitraryTransaction.getService());

            //CHECK DATA
            assertEquals(true, Arrays.equals(arbitraryTransaction.getData(), parsedArbitraryTransaction.getData()));

            //CHECK FEE
            assertEquals(arbitraryTransaction.getFee(), parsedArbitraryTransaction.getFee());

            //CHECK REFERENCE
            //assertEquals(arbitraryTransaction.getReference(), parsedArbitraryTransaction.getReference());

            //CHECK TIMESTAMP
            assertEquals(arbitraryTransaction.getTimestamp(), parsedArbitraryTransaction.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction.");
        }

        //PARSE TRANSACTION FROM WRONG BYTES
        rawArbitraryTransaction = new byte[arbitraryTransaction.getDataLength(Transaction.FOR_NETWORK, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawArbitraryTransaction, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }


    @Test
    public void processArbitraryTransaction() {

        init();

        //CREATE ARBITRARY TRANSACTION
        ArbitraryTransactionV3 arbitraryTransaction = new ArbitraryTransactionV3(maker, null, 4776, "test".getBytes(), FEE_POWER, timestamp, last_ref);
        arbitraryTransaction.sign(maker, Transaction.FOR_NETWORK);
        arbitraryTransaction.process(gb, Transaction.FOR_NETWORK);

        //CHECK BALANCE SENDER
        assertEquals(0, BigDecimal.valueOf(1).subtract(arbitraryTransaction.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE).compareTo(maker.getBalanceUSE(FEE_KEY, databaseSet)));

        //CHECK REFERENCE SENDER
        assertEquals((long)arbitraryTransaction.getTimestamp(), maker.getLastTimestamp(databaseSet)[0]);
    }

    @Test
    public void orphanArbitraryTransaction() {

        init();

        //CREATE ARBITRARY TRANSACTION
        ArbitraryTransactionV3 arbitraryTransaction = new ArbitraryTransactionV3(maker, null, 4776, "test".getBytes(), FEE_POWER, timestamp, last_ref);
        arbitraryTransaction.sign(maker, Transaction.FOR_NETWORK);
        arbitraryTransaction.process(gb, Transaction.FOR_NETWORK);
        arbitraryTransaction.orphan(block, Transaction.FOR_NETWORK);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, databaseSet));

        //CHECK REFERENCE SENDER
        assertEquals((long)last_ref, maker.getLastTimestamp(databaseSet)[0]);
    }

	/*@Test
	public void validateArbitraryTransaction()
	{

		//CREATE EMPTY MEMORY DATABASE
		DLSet databaseSet = DLSet.createEmptyDatabaseSet();

		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);

		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(maker, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), NTP.getTime());
		transaction.process(databaseSet);

		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] data = "test".getBytes();

		//CREATE ARBITRARY TRANSACTION
		Transaction arbitraryTransaction = new ArbitraryTransactionV3(maker, null, 4776, data, FEE_POWER, timestamp, last_ref);

		//CHECK IF ARBITRARY TRANSACTION IS VALID
		assertEquals(Transaction.VALIDATE_OK, arbitraryTransaction.isValid(databaseSet));
		arbitraryTransaction.process(databaseSet);

		//CREATE INVALID ARBITRARY TRANSACTION INVALID data LENGTH
		byte[] longData = new byte[5000];
		arbitraryTransaction = new ArbitraryTransactionV3(maker, null, 4776, longData, FEE_POWER, timestamp, last_ref);

		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.INVALID_DATA_LENGTH, arbitraryTransaction.isValid(databaseSet));

		//CREATE INVALID ARBITRARY TRANSACTION NOT ENOUGH BALANCE
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		arbitraryTransaction = new ArbitraryTransactionV1(invalidOwner, 4776, data, FEE_POWER, timestamp, last_ref);

		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.NO_BALANCE, arbitraryTransaction.isValid(databaseSet));

		//CREATE ARBITRARY TRANSACTION INVALID REFERENCE
		arbitraryTransaction = new ArbitraryTransactionV3(maker, null, 4776, data, FEE_POWER, timestamp, invalidOwner.getLastReference(databaseSet));

		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, arbitraryTransaction.isValid(databaseSet));

	}*/

    //ISSUE ASSET TRANSACTION

    @Test
    public void validateSignatureIssueAssetTransaction() {

        init();

        //CREATE ASSET
        AssetCls asset = new AssetVenture(itemAppData, maker, "test", icon, image, "strontje", 0, 0, 50000l);
        //byte[] data = asset.toBytes(false);
        //Asset asset2 = Asset.parse(data);


        //CREATE SIGNATURE
        long timestamp = NTP.getTime();

        //CREATE ISSUE ASSET TRANSACTION
        Transaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0]);
        issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);

        //CHECK IF ISSUE ASSET TRANSACTION IS VALID
        assertEquals(true, issueAssetTransaction.isSignatureValid(db));

        //INVALID SIGNATURE
        issueAssetTransaction = new IssueAssetTransaction(
                maker, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0], new byte[64]);

        //CHECK IF ISSUE ASSET IS INVALID
        assertEquals(false, issueAssetTransaction.isSignatureValid(db));
    }


    @Test
    public void parseIssueAssetTransaction() {

        init();

        //CREATE SIGNATURE
        long timestamp = NTP.getTime();
        AssetCls asset = new AssetVenture(itemAppData, maker, "test", icon, image, "strontje", 0, 0, 50000l);

        //CREATE ISSUE ASSET TRANSACTION
        IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0]);
        issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);
        issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawIssueAssetTransaction = issueAssetTransaction.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawIssueAssetTransaction.length, issueAssetTransaction.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            IssueAssetTransaction parsedIssueAssetTransaction = (IssueAssetTransaction) TransactionFactory.getInstance().parse(rawIssueAssetTransaction, Transaction.FOR_NETWORK);

            //CHECK INSTANCE
            assertEquals(true, parsedIssueAssetTransaction instanceof IssueAssetTransaction);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(issueAssetTransaction.getSignature(), parsedIssueAssetTransaction.getSignature()));

            //CHECK ISSUER
            assertEquals(issueAssetTransaction.getCreator().getAddress(), parsedIssueAssetTransaction.getCreator().getAddress());

            //CHECK OWNER
            assertEquals(issueAssetTransaction.getItem().getMaker().getAddress(), parsedIssueAssetTransaction.getItem().getMaker().getAddress());

            //CHECK NAME
            assertEquals(issueAssetTransaction.getItem().getName(), parsedIssueAssetTransaction.getItem().getName());

            //CHECK DESCRIPTION
            assertEquals(issueAssetTransaction.getItem().getDescription(), parsedIssueAssetTransaction.getItem().getDescription());

            //CHECK QUANTITY
            assertEquals(((AssetCls) issueAssetTransaction.getItem()).getQuantity(), ((AssetCls) parsedIssueAssetTransaction.getItem()).getQuantity());

            //SCALE
            assertEquals(((AssetCls) issueAssetTransaction.getItem()).getScale(), ((AssetCls) parsedIssueAssetTransaction.getItem()).getScale());

            //ASSET TYPE
            assertEquals(((AssetCls) issueAssetTransaction.getItem()).getAssetType(), ((AssetCls) parsedIssueAssetTransaction.getItem()).getAssetType());

            //CHECK FEE
            assertEquals(issueAssetTransaction.getFee(), parsedIssueAssetTransaction.getFee());

            //CHECK REFERENCE
            //assertEquals(issueAssetTransaction.getReference(), parsedIssueAssetTransaction.getReference());

            //CHECK TIMESTAMP
            assertEquals(issueAssetTransaction.getTimestamp(), parsedIssueAssetTransaction.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction.");
        }

        //PARSE TRANSACTION FROM WRONG BYTES
        rawIssueAssetTransaction = new byte[issueAssetTransaction.getDataLength(Transaction.FOR_NETWORK, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawIssueAssetTransaction, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }


    @Test
    public void processIssueAssetTransaction() {

        init();

        //CREATE SIGNATURE
        long timestamp = NTP.getTime();
        AssetCls asset = new AssetVenture(itemAppData, maker, "test", icon, image, "strontje", 0, 0, 50000l);


        //CREATE ISSUE ASSET TRANSACTION
        IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0]);
        issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);

        assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

        issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);

        LOGGER.info("asset KEY: " + asset.getKey(DCSet.getInstance()));

        //CHECK BALANCE ISSUER
        assertEquals(BigDecimal.valueOf(50000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(asset.getKey(db), db));

        //CHECK ASSET EXISTS SENDER
        long key = db.getIssueAssetMap().get(issueAssetTransaction);
        assertEquals(true, db.getItemAssetMap().contains(key));

        //CHECK ASSET IS CORRECT
        assertEquals(true, Arrays.equals(db.getItemAssetMap().get(key).toBytes(forDeal, true, false), asset.toBytes(forDeal, true, false)));

        //CHECK ASSET BALANCE SENDER
        assertEquals(true, db.getAssetBalanceMap().get(maker.getShortAddressBytes(), key).a.b.compareTo(new BigDecimal(asset.getQuantity())) == 0);

        //CHECK REFERENCE SENDER
        assertEquals(issueAssetTransaction.getTimestamp(), maker.getLastTimestamp(db));
    }


    @Test
    public void orphanIssueAssetTransaction() {

        init();


        long timestamp = NTP.getTime();
        AssetCls asset = new AssetVenture(itemAppData, maker, "test", icon, image, "strontje", 0, 0, 50000l);

        //CREATE ISSUE ASSET TRANSACTION
        IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0]);
        issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);
        issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);
        long key = db.getIssueAssetMap().get(issueAssetTransaction);
        assertEquals(new BigDecimal(50000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));
        assertEquals(issueAssetTransaction.getTimestamp(), maker.getLastTimestamp(db));

        issueAssetTransaction.orphan(block, Transaction.FOR_NETWORK);

        //CHECK BALANCE ISSUER
        assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

        //CHECK ASSET EXISTS SENDER
        assertEquals(false, db.getItemAssetMap().contains(key));

        //CHECK ASSET BALANCE SENDER
        assertEquals(0, db.getAssetBalanceMap().get(maker.getShortAddressBytes(), key).a.b.longValue());

        //CHECK REFERENCE SENDER
        //assertEquals(issueAssetTransaction.getReference(), maker.getLastReference(db));
    }

}
