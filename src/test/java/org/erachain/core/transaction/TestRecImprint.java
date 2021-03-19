package org.erachain.core.transaction;

import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.imprints.Imprint;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemImprintMap;
import org.erachain.ntp.NTP;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

//import java.math.BigInteger;
//import java.util.ArrayList;
//import java.util.List;

public class TestRecImprint {

    static Logger LOGGER = LoggerFactory.getLogger(TestRecImprint.class.getName());

    ExLink exLink = null;

    int asPack = Transaction.FOR_NETWORK;
    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 1;
    byte[] imprintReference = new byte[64];
    long timestamp = NTP.getTime();

    long flags = 0l;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    String name_total = "123890TYRH76576567567tytryrtyr61fdhgdfdskdfhuiweyriusdfyf8s7fssudfgdytrttygd";
    byte[] digest;
    Imprint imprint;
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;

    // INIT IMPRINTS
    private void init() {

        name_total = Imprint.hashNameToBase58(name_total);
        digest = Base58.decode(name_total);

        db = DCSet.createEmptyDatabaseSet(0);
        gb = new GenesisBlock();
        try {
            gb.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // FEE FUND
        maker.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        maker.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        imprint = new Imprint(maker, name_total, icon, image, "");

    }


    //ISSUE IMPRINT TRANSACTION

    @Test
    public void validateSignatureIssueImprintTransaction() {

        init();

        byte[] reference = imprint.getCuttedReference();
        assertEquals(true, Arrays.equals(digest, reference));
        assertEquals(name_total, Base58.encode(reference));

        //CREATE ISSUE IMPRINT TRANSACTION
        IssueImprintRecord issueImprintTransaction = new IssueImprintRecord(maker, exLink, imprint, FEE_POWER, timestamp);
        issueImprintTransaction.sign(maker, Transaction.FOR_NETWORK);

        //CHECK IF ISSUE IMPRINT TRANSACTION IS VALID
        assertEquals(true, issueImprintTransaction.isSignatureValid(db));

        // CHECK REFERENCE OF ITEM NOT CHANGED
        Imprint impr_1 = (Imprint) issueImprintTransaction.getItem();
        assertEquals(name_total, Base58.encode(impr_1.getCuttedReference()));

        //INVALID SIGNATURE
        issueImprintTransaction = new IssueImprintRecord(maker, imprint, FEE_POWER, timestamp, new byte[64]);

        //CHECK IF ISSUE IMPRINT IS INVALID
        assertEquals(false, issueImprintTransaction.isSignatureValid(db));
    }


    @Test
    public void parseIssueImprintTransaction() {

        init();

        byte[] raw = imprint.toBytes(false, false);
        assertEquals(raw.length, imprint.getDataLength(false));

        //CREATE ISSUE IMPRINT TRANSACTION
        IssueImprintRecord issueImprintRecord = new IssueImprintRecord(maker, exLink, imprint, FEE_POWER, timestamp);
        issueImprintRecord.sign(maker, asPack);
        //issueImprintRecord.process(db, false);

        //CONVERT TO BYTES
        byte[] rawIssueImprintTransaction = issueImprintRecord.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawIssueImprintTransaction.length, issueImprintRecord.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            IssueImprintRecord parsedIssueImprintTransaction = (IssueImprintRecord) TransactionFactory.getInstance().parse(rawIssueImprintTransaction, asPack);
            LOGGER.info("parsedIssueImprintTransaction: " + parsedIssueImprintTransaction);

            //CHECK INSTANCE
            assertEquals(true, parsedIssueImprintTransaction instanceof IssueImprintRecord);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(issueImprintRecord.getSignature(), parsedIssueImprintTransaction.getSignature()));

            //CHECK ISSUER
            assertEquals(issueImprintRecord.getCreator().getAddress(), parsedIssueImprintTransaction.getCreator().getAddress());

            //CHECK OWNER
            assertEquals(issueImprintRecord.getItem().getMaker().getAddress(), parsedIssueImprintTransaction.getItem().getMaker().getAddress());

            //CHECK NAME
            assertEquals(issueImprintRecord.getItem().getName(), parsedIssueImprintTransaction.getItem().getName());

            //CHECK DESCRIPTION
            assertEquals(issueImprintRecord.getItem().getDescription(), parsedIssueImprintTransaction.getItem().getDescription());

            //CHECK FEE
            assertEquals(issueImprintRecord.getFee(), parsedIssueImprintTransaction.getFee());

            //CHECK REFERENCE
            //assertEquals(issueImprintRecord.getReference(), parsedIssueImprintTransaction.getReference());

            //CHECK TIMESTAMP
            assertEquals(issueImprintRecord.getTimestamp(), parsedIssueImprintTransaction.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

    }


    @Test
    public void processIssueImprintTransaction() {

        init();

        //CREATE ISSUE IMPRINT TRANSACTION
        IssueImprintRecord issueImprintRecord = new IssueImprintRecord(maker, exLink, imprint, FEE_POWER, timestamp);
        issueImprintRecord.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(issueImprintRecord.getItem().getName(), Base58.encode(imprint.getCuttedReference()));
        issueImprintRecord.sign(maker, Transaction.FOR_NETWORK);

        assertEquals(Transaction.VALIDATE_OK, issueImprintRecord.isValid(Transaction.FOR_NETWORK, flags));

        issueImprintRecord.process(gb, Transaction.FOR_NETWORK);

        LOGGER.info("imprint KEY: " + imprint.getKey(db));

        //CHECK IMPRINT EXISTS SENDER
        ///////// NOT FONT THROUGHT db.get(issueImprintRecord)
        //long key = db.getIssueImprintMap().get(issueImprintRecord);
        long key = issueImprintRecord.getItem().getKey(db);
        assertEquals(true, db.getItemImprintMap().contains(key));

        ImprintCls imprint_2 = new Imprint(maker, Imprint.hashNameToBase58("test132_2"), icon, image, "e");
        IssueImprintRecord issueImprintTransaction_2 = new IssueImprintRecord(maker, exLink, imprint_2, FEE_POWER, timestamp + 10);
        issueImprintTransaction_2.sign(maker, Transaction.FOR_NETWORK);
        issueImprintTransaction_2.setDC(db, Transaction.FOR_NETWORK, 1, 2, true);
        issueImprintTransaction_2.process(gb, Transaction.FOR_NETWORK);
        LOGGER.info("imprint_2 KEY: " + imprint_2.getKey(db));
        issueImprintTransaction_2.orphan(gb, Transaction.FOR_NETWORK);
        ItemImprintMap imprintMap = db.getItemImprintMap();
        int mapSize = imprintMap.size();
        assertEquals(0, mapSize - 1);

        //CHECK IMPRINT IS CORRECT
        assertEquals(true, Arrays.equals(db.getItemImprintMap().get(key).toBytes(true, false), imprint.toBytes(true, false)));

        //CHECK REFERENCE SENDER
        //assertEquals(true, Arrays.equals(issueImprintRecord.getSignature(), maker.getLastReference()));
    }


    @Test
    public void orphanIssueImprintTransaction() {

        init();

        //CREATE ISSUE IMPRINT TRANSACTION
        IssueImprintRecord issueImprintRecord = new IssueImprintRecord(maker, exLink, imprint, FEE_POWER, timestamp);
        issueImprintRecord.sign(maker, Transaction.FOR_NETWORK);
        issueImprintRecord.setDC(db, Transaction.FOR_NETWORK, 1, 2, true);
        issueImprintRecord.process(gb, Transaction.FOR_NETWORK);
        long key = db.getIssueImprintMap().get(issueImprintRecord);
        //		assertEquals(true, Arrays.equals(issueImprintRecord.getSignature(), maker.getLastReference()));

        issueImprintRecord.orphan(gb, Transaction.FOR_NETWORK);

        //CHECK IMPRINT EXISTS SENDER
        assertEquals(false, db.getItemImprintMap().contains(key));

        //CHECK REFERENCE SENDER
        //assertEquals(true, Arrays.equals(issueImprintRecord.getReference(), maker.getLastReference()));
    }

    // TODO - in statement - valid on key = 999
}
