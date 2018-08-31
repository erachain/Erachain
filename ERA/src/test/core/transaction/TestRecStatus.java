package core.transaction;

import core.BlockChain;
import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.statuses.Status;
import core.item.statuses.StatusCls;
import datachain.DCSet;
import datachain.ItemStatusMap;
import ntp.NTP;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import utils.Corekeys;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

//import java.math.BigInteger;
//import java.util.ArrayList;
//import java.util.List;

public class TestRecStatus {

    static Logger LOGGER = Logger.getLogger(TestRecStatus.class.getName());

    //Long releaserReference = null;

    boolean asPack = false;
    long ERM_KEY = AssetCls.ERA_KEY;
    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] statusReference = new byte[64];
    long timestamp = NTP.getTime();

    long flags = 0l;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    int mapSize;
    ItemStatusMap statusMap;
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;

    // INIT STATUSS
    private void init() {

        db = DCSet.createEmptyDatabaseSet();
        gb = new GenesisBlock();
        try {
            gb.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // FEE FUND
        maker.setLastTimestamp(gb.getTimestamp(), db);
        maker.changeBalance(db, false, ERM_KEY, BigDecimal.valueOf(10000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
        maker.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
        statusMap = db.getItemStatusMap();
        mapSize = statusMap.size();

    }

    @Test
    public void testAddreessVersion() {
        int vers = Corekeys.findAddressVersion("E");
        assertEquals(-1111, vers);
    }

    //ISSUE STATUS TRANSACTION

    @Test
    public void validateSignatureIssueStatusTransaction() {

        init();

        //CREATE STATUS
        Status status = new Status(maker, "test", icon, image, "strontje", true);

        //CREATE ISSUE STATUS TRANSACTION
        Transaction issueStatusTransaction = new IssueStatusRecord(maker, status, FEE_POWER, timestamp, maker.getLastTimestamp(db));
        issueStatusTransaction.sign(maker, Transaction.FOR_NETWORK);

        //CHECK IF ISSUE STATUS TRANSACTION IS VALID
        assertEquals(true, issueStatusTransaction.isSignatureValid(db));

        //INVALID SIGNATURE
        issueStatusTransaction = new IssueStatusRecord(maker, status, FEE_POWER, timestamp, maker.getLastTimestamp(db), new byte[64]);

        //CHECK IF ISSUE STATUS IS INVALID
        assertEquals(false, issueStatusTransaction.isSignatureValid(db));
    }

    @Ignore
//TODO actualize the test
    @Test
    public void parseIssueStatusTransaction() {

        init();

        StatusCls status = new Status(maker, "test132", icon, image, "12345678910strontje", true);
        byte[] raw = status.toBytes(false, false);
        assertEquals(raw.length, status.getDataLength(false));

        //CREATE ISSUE STATUS TRANSACTION
        IssueStatusRecord issueStatusRecord = new IssueStatusRecord(maker, status, FEE_POWER, timestamp, maker.getLastTimestamp(db));
        issueStatusRecord.sign(maker, Transaction.FOR_NETWORK);
        issueStatusRecord.setDC(db, Transaction.FOR_NETWORK, 1, 1);
        issueStatusRecord.process(gb, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawIssueStatusTransaction = issueStatusRecord.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawIssueStatusTransaction.length, issueStatusRecord.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            IssueStatusRecord parsedIssueStatusTransaction = (IssueStatusRecord) TransactionFactory.getInstance().parse(rawIssueStatusTransaction, Transaction.FOR_NETWORK);
            LOGGER.info("parsedIssueStatusTransaction: " + parsedIssueStatusTransaction);

            //CHECK INSTANCE
            assertEquals(true, parsedIssueStatusTransaction instanceof IssueStatusRecord);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(issueStatusRecord.getSignature(), parsedIssueStatusTransaction.getSignature()));

            //CHECK ISSUER
            assertEquals(issueStatusRecord.getCreator().getAddress(), parsedIssueStatusTransaction.getCreator().getAddress());

            //CHECK OWNER
            assertEquals(issueStatusRecord.getItem().getOwner().getAddress(), parsedIssueStatusTransaction.getItem().getOwner().getAddress());

            //CHECK NAME
            assertEquals(issueStatusRecord.getItem().getName(), parsedIssueStatusTransaction.getItem().getName());

            //CHECK DESCRIPTION
            assertEquals(issueStatusRecord.getItem().getDescription(), parsedIssueStatusTransaction.getItem().getDescription());

            //CHECK FEE
            assertEquals(issueStatusRecord.getFee(), parsedIssueStatusTransaction.getFee());

            //CHECK REFERENCE
            //assertEquals(issueStatusRecord.getReference(), parsedIssueStatusTransaction.getReference());

            //CHECK TIMESTAMP
            assertEquals(issueStatusRecord.getTimestamp(), parsedIssueStatusTransaction.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

    }


    @Test
    public void process_orphanIssueStatusTransaction() {

        init();

        Status status = new Status(maker, "test", icon, image, "strontje", true);

        //CREATE ISSUE STATUS TRANSACTION
        IssueStatusRecord issueStatusRecord = new IssueStatusRecord(maker, status, FEE_POWER, timestamp, maker.getLastTimestamp(db));
        issueStatusRecord.setDC(db, Transaction.FOR_NETWORK, 1, 1);
        assertEquals(Transaction.CREATOR_NOT_PERSONALIZED, issueStatusRecord.isValid(Transaction.FOR_NETWORK, flags));

        issueStatusRecord.sign(maker, Transaction.FOR_NETWORK);
        issueStatusRecord.process(gb, Transaction.FOR_NETWORK);

        LOGGER.info("status KEY: " + status.getKey(db));

        //CHECK STATUS EXISTS SENDER
        long key = db.getIssueStatusMap().get(issueStatusRecord);
        assertEquals(true, db.getItemStatusMap().contains(key));

        StatusCls status_2 = new Status(maker, "test132_2", icon, image, "2_12345678910strontje", true);
        IssueStatusRecord issueStatusTransaction_2 = new IssueStatusRecord(maker, status_2, FEE_POWER, timestamp + 10, maker.getLastTimestamp(db));
        issueStatusTransaction_2.sign(maker, Transaction.FOR_NETWORK);
        issueStatusTransaction_2.setDC(db, Transaction.FOR_NETWORK, 1, 1);
        issueStatusTransaction_2.process(gb, Transaction.FOR_NETWORK);
        LOGGER.info("status_2 KEY: " + status_2.getKey(db));
        issueStatusTransaction_2.orphan(Transaction.FOR_NETWORK);
        assertEquals(mapSize + 1, statusMap.size());

        //CHECK STATUS IS CORRECT
        assertEquals(true, Arrays.equals(db.getItemStatusMap().get(key).toBytes(true, false), status.toBytes(true, false)));

        //CHECK REFERENCE SENDER
        assertEquals(issueStatusRecord.getTimestamp(), maker.getLastTimestamp(db));

        ////// ORPHAN ///////

        issueStatusRecord.orphan(Transaction.FOR_NETWORK);

        assertEquals(mapSize, statusMap.size());

        //CHECK STATUS EXISTS SENDER
        assertEquals(false, db.getItemStatusMap().contains(key));

        //CHECK REFERENCE SENDER
        //assertEquals(issueStatusRecord.getReference(), maker.getLastReference(db));
    }

    // TODO - in statement - valid on key = 999
}
