package org.erachain.core.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.ItemFactory;
import org.erachain.core.item.polls.Poll;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.voting.PollOption;
import org.erachain.core.wallet.Wallet;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestRecPoll {

    static Logger LOGGER = LoggerFactory.getLogger(TestRecPoll.class.getName());

    int forDeal = Transaction.FOR_NETWORK;

    //Long Transaction.FOR_NETWORK = null;

    BigDecimal BG_ZERO = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    //long ALIVE_KEY = StatusCls.ALIVE_KEY;
    byte FEE_POWER = (byte) 1;
    byte[] pollReference = new byte[64];
    long timestamp = NTP.getTime();
    long dbRef = 0L;

    byte[] itemAppData = null;
    long txFlags = 4l;

    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount certifier = new PrivateKeyAccount(privateKey);
    //GENERATE ACCOUNT SEED
    int nonce = 1;
    //byte[] accountSeed;
    //core.wallet.Wallet.generateAccountSeed(byte[], int)
    byte[] accountSeed1 = Wallet.generateAccountSeed(seed, nonce++);
    PrivateKeyAccount userAccount1 = new PrivateKeyAccount(accountSeed1);
    String userAddress1 = userAccount1.getAddress();
    byte[] accountSeed2 = Wallet.generateAccountSeed(seed, nonce++);
    PrivateKeyAccount userAccount2 = new PrivateKeyAccount(accountSeed2);
    String userAddress2 = userAccount2.getAddress();
    byte[] accountSeed3 = Wallet.generateAccountSeed(seed, nonce++);
    PrivateKeyAccount userAccount3 = new PrivateKeyAccount(accountSeed3);
    String userAddress3 = userAccount3.getAddress();
    PollCls pollGeneral;
    PollCls poll;
    long pollKey = -1;
    PollOption pollOption;
    List<String> options = new ArrayList<String>();
    IssuePollRecord issuePollTransaction;
    int version = 0;
    long parent = -1;
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    ;
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value

    //PollAddressMap dbPA;
    //AddressPollMap dbAP;
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;

    // INIT POLLS
    @Before
    public void init() {

        if (!Settings.getInstance().isTestNet())
            fail("You need switch key '-testnet'");

        if (BlockChain.TESTS_VERS == 0)
            fail("You need change key 'BlockChain.TESTS_VERS' to current version");


        db = DCSet.createEmptyDatabaseSet(0);
        Controller.getInstance().setDCSet(db);
        gb = new GenesisBlock();
        try {
            gb.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //dbPA = db.getPollAddressMap();
        //dbAP = db.getAddressPollMap();


        options.add("first ORTION");
        options.add("second ORTION");
        options.add("probe probe");

        // GET RIGHTS TO CERTIFIER
        pollGeneral = new Poll(itemAppData, certifier, "СССР", icon, image, "wqeqwe", options);
        //GenesisIssuePollRecord genesis_issue_poll = new GenesisIssuePollRecord(pollGeneral, registrar);
        //genesis_issue_poll.process(db, false);
        //GenesisCertifyPollRecord genesis_certify = new GenesisCertifyPollRecord(registrar, 0L);
        //genesis_certify.process(db, false);

        certifier.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        certifier.changeBalance(db, false, false, ERM_KEY, BlockChain.MAJOR_ERA_BALANCE_BD, false, false, false);
        certifier.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        poll = new Poll(itemAppData, certifier, "РСФСР", icon, image, "Россия", options);

        //CREATE ISSUE POLL TRANSACTION
        issuePollTransaction = new IssuePollRecord(certifier, null, poll, FEE_POWER, timestamp, certifier.getLastTimestamp(db)[0]);
        issuePollTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);


    }

    public void initPollalize() {


        assertEquals(Transaction.VALIDATE_OK, issuePollTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

        issuePollTransaction.sign(certifier, Transaction.FOR_NETWORK);

        issuePollTransaction.process(gb, Transaction.FOR_NETWORK);
        pollKey = poll.getKey();

        assertEquals(1, pollKey);
        //assertEquals( null, dbPS.getItem(pollKey));

    }

    //ISSUE POLL TRANSACTION

    @Test
    public void validateSignatureIssuePollRecord() {


        issuePollTransaction.sign(certifier, Transaction.FOR_NETWORK);

        //CHECK IF ISSUE POLL TRANSACTION IS VALID
        assertEquals(true, issuePollTransaction.isSignatureValid(db));

        //INVALID SIGNATURE
        issuePollTransaction = new IssuePollRecord(certifier, poll, FEE_POWER, timestamp, certifier.getLastTimestamp(db)[0], new byte[64]);
        //CHECK IF ISSUE POLL IS INVALID
        assertEquals(false, issuePollTransaction.isSignatureValid(db));

    }


    @Test
    public void validateIssuePollRecord() {


        issuePollTransaction.sign(certifier, Transaction.FOR_NETWORK);

        //CHECK IF ISSUE POLL IS VALID
        assertEquals(Transaction.VALIDATE_OK, issuePollTransaction.isValid(Transaction.FOR_NETWORK, txFlags));


        //CREATE INVALID ISSUE POLL - INVALID POLLALIZE
        issuePollTransaction = new IssuePollRecord(userAccount1, poll, FEE_POWER, timestamp, 0l, new byte[64]);
        issuePollTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        if (!Settings.getInstance().isTestNet())
            assertEquals(Transaction.NOT_ENOUGH_FEE, issuePollTransaction.isValid(Transaction.FOR_NETWORK, txFlags));
        // ADD FEE
        userAccount1.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        assertEquals(Transaction.VALIDATE_OK, issuePollTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

        //CHECK IF ISSUE POLL IS VALID
        userAccount1.changeBalance(db, false, false, ERM_KEY, BlockChain.MINOR_ERA_BALANCE_BD, false, false, false);
        assertEquals(Transaction.VALIDATE_OK, issuePollTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

        //CHECK
        userAccount1.changeBalance(db, false, false, ERM_KEY, BlockChain.MAJOR_ERA_BALANCE_BD, false, false, false);
        assertEquals(Transaction.VALIDATE_OK, issuePollTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

    }


    @Test
    public void parseIssuePollRecord() {


        LOGGER.info("poll: " + poll.getTypeBytes()[0] + ", " + poll.getTypeBytes()[1]);

        // PARSE POLL

        byte[] rawPoll = poll.toBytes(forDeal, false, false);
        assertEquals(rawPoll.length, poll.getDataLength(false));
        poll.setReference(new byte[64], dbRef);
        rawPoll = poll.toBytes(forDeal, true, false);
        assertEquals(rawPoll.length, poll.getDataLength(true));

        rawPoll = poll.toBytes(forDeal, false, false);
        PollCls parsedPoll = null;
        try {
            //PARSE FROM BYTES
            parsedPoll = (PollCls) ItemFactory.getInstance()
                    .parse(forDeal, ItemCls.POLL_TYPE, rawPoll, false);
        } catch (Exception e) {
            fail("Exception while parsing transaction.  : " + e);
        }
        assertEquals(rawPoll.length, poll.getDataLength(false));
        assertEquals(poll.getMaker().getAddress(), parsedPoll.getMaker().getAddress());
        assertEquals(poll.getName(), parsedPoll.getName());
        assertEquals(poll.getDescription(), parsedPoll.getDescription());
        assertEquals(poll.getItemTypeName(), parsedPoll.getItemTypeName());

        // PARSE ISSEU POLL RECORD
        issuePollTransaction.sign(certifier, Transaction.FOR_NETWORK);
        issuePollTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issuePollTransaction.process(gb, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawIssuePollRecord = issuePollTransaction.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawIssuePollRecord.length, issuePollTransaction.getDataLength(Transaction.FOR_NETWORK, true));

        IssuePollRecord parsedIssuePollRecord = null;
        try {
            //PARSE FROM BYTES
            parsedIssuePollRecord = (IssuePollRecord) TransactionFactory.getInstance().parse(rawIssuePollRecord, Transaction.FOR_NETWORK);

        } catch (Exception e) {
            fail("Exception while parsing transaction.  : " + e);
        }

        //CHECK INSTANCE
        assertEquals(true, parsedIssuePollRecord instanceof IssuePollRecord);

        //CHECK SIGNATURE
        assertEquals(true, Arrays.equals(issuePollTransaction.getSignature(), parsedIssuePollRecord.getSignature()));

        //CHECK ISSUER
        assertEquals(issuePollTransaction.getCreator().getAddress(), parsedIssuePollRecord.getCreator().getAddress());

        parsedPoll = (Poll) parsedIssuePollRecord.getItem();

        //CHECK OWNER
        assertEquals(poll.getMaker().getAddress(), parsedPoll.getMaker().getAddress());

        //CHECK NAME
        assertEquals(poll.getName(), parsedPoll.getName());

        //CHECK REFERENCE
        //assertEquals(issuePollTransaction.getReference(), parsedIssuePollRecord.getReference());

        //CHECK TIMESTAMP
        assertEquals(issuePollTransaction.getTimestamp(), parsedIssuePollRecord.getTimestamp());

        //CHECK DESCRIPTION
        assertEquals(poll.getDescription(), parsedPoll.getDescription());

        assertEquals(poll.getItemTypeName(), parsedPoll.getItemTypeName());

        assertEquals(poll.getOptions().size(), parsedPoll.getOptions().size());
        assertEquals(poll.getOptions().get(2), parsedPoll.getOptions().get(2));

        //PARSE TRANSACTION FROM WRONG BYTES
        rawIssuePollRecord = new byte[issuePollTransaction.getDataLength(Transaction.FOR_NETWORK, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawIssuePollRecord, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }

    @Ignore
//TODO actualize the test
    @Test
    public void processIssuePollRecord() {

        assertEquals(Transaction.VALIDATE_OK, issuePollTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

        issuePollTransaction.sign(certifier, Transaction.FOR_NETWORK);
        issuePollTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issuePollTransaction.process(gb, Transaction.FOR_NETWORK);

        LOGGER.info("poll KEY: " + poll.getKey());

        //CHECK BALANCE ISSUER
        //assertEquals(BlockChain.MAJOR_ERA_BALANCE_BD, registrar.getBalanceUSE(ERM_KEY, db));
        //assertEquals(issuePollTransaction.getFee().setScale(BlockChain.AMOUNT_DEDAULT_SCALE), registrar.getBalanceUSE(FEE_KEY, db));

        //CHECK POLL EXISTS DB AS CONFIRMED:  key > -1
        long key = issuePollTransaction.key;
        assertEquals(1l, key);
        assertEquals(true, db.getItemPollMap().contains(key));

        //CHECK POLL IS CORRECT
        assertEquals(true, Arrays.equals(db.getItemPollMap().get(key).toBytes(forDeal, true, false), poll.toBytes(forDeal, true, false)));

        //CHECK REFERENCE SENDER
        assertEquals(issuePollTransaction.getTimestamp(), certifier.getLastTimestamp(db));

        //////// ORPHAN /////////
        issuePollTransaction.orphan(gb, Transaction.FOR_NETWORK);

        //CHECK BALANCE ISSUER
        if (!Settings.getInstance().isTestNet())
            assertEquals(BlockChain.MAJOR_ERA_BALANCE_BD, certifier.getBalanceUSE(ERM_KEY, db));
        assertEquals(BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), certifier.getBalanceUSE(FEE_KEY, db));

        //CHECK POLL EXISTS ISSUER
        assertEquals(false, db.getItemPollMap().contains(pollKey));

        //CHECK REFERENCE ISSUER
        //assertEquals(issuePollTransaction.getReference(), registrar.getLastReference(db));
    }


}
