package org.erachain.core.transaction;

import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.templates.Template;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemTemplateMap;
import org.erachain.ntp.NTP;
import org.junit.Ignore;
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

public class TestTemplateAsPack {

    static Logger LOGGER = LoggerFactory.getLogger(TestTemplateAsPack.class.getName());

    int forDeal = Transaction.FOR_NETWORK;

    boolean includeReference = false;
    long FEE_KEY = 1l;
    byte FEE_POWER = (byte) 1;
    byte[] templateReference = new byte[64];
    long timestamp = NTP.getTime();

    byte[] itemAppData = null;
    long txFlags = 0L;

    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;

    // INIT TEMPLATES
    private void init() {

        db = DCSet.createEmptyDatabaseSet(0);
        gb = new GenesisBlock();
        try {
            gb.process(db, false);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // FEE FUND
        maker.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        maker.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

    }


    //ISSUE PLATE TRANSACTION

    @Test
    public void validateSignatureIssueTemplateTransaction() {

        init();

        //CREATE PLATE
        Template template = new Template(itemAppData, maker, "test", icon, image, "strontje");

        //CREATE ISSUE PLATE TRANSACTION
        Transaction issueTemplateTransaction = new IssueTemplateRecord(maker, template);
        issueTemplateTransaction.sign(maker, forDeal);

        //CHECK IF ISSUE PLATE TRANSACTION IS VALID
        issueTemplateTransaction.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
        assertEquals(true, issueTemplateTransaction.isSignatureValid(db));

        //INVALID SIGNATURE
        issueTemplateTransaction = new IssueTemplateRecord(maker, template, new byte[64]);

        //CHECK IF ISSUE PLATE IS INVALID
        issueTemplateTransaction.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
        assertEquals(false, issueTemplateTransaction.isSignatureValid(db));
    }

    @Ignore
    //TODO actualize the test
    @Test
    public void parseIssueTemplateTransaction() {

        init();

        TemplateCls template = new Template(itemAppData, maker, "test132", icon, image, "12345678910strontje");
        byte[] raw = template.toBytes(forDeal, includeReference, false);
        assertEquals(raw.length, template.getDataLength(includeReference));

        //CREATE ISSUE PLATE TRANSACTION
        IssueTemplateRecord issueTemplateRecord = new IssueTemplateRecord(maker, template);
        issueTemplateRecord.sign(maker, forDeal);
        issueTemplateRecord.setDC(db, Transaction.FOR_PACK, BlockChain.SKIP_INVALID_SIGN_BEFORE, 1, true);
        issueTemplateRecord.process(gb, forDeal);

        //CONVERT TO BYTES
        byte[] rawIssueTemplateTransaction = issueTemplateRecord.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawIssueTemplateTransaction.length, issueTemplateRecord.getDataLength(forDeal, true));

        IssueTemplateRecord parsedIssueTemplateTransaction = null;
        try {
            //PARSE FROM BYTES
            parsedIssueTemplateTransaction = (IssueTemplateRecord) TransactionFactory.getInstance().parse(rawIssueTemplateTransaction, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }


        //CHECK INSTANCE
        assertEquals(true, parsedIssueTemplateTransaction instanceof IssueTemplateRecord);

        //CHECK SIGNATURE
        assertEquals(true, Arrays.equals(issueTemplateRecord.getSignature(), parsedIssueTemplateTransaction.getSignature()));

        //CHECK ISSUER
        assertEquals(issueTemplateRecord.getCreator().getAddress(), parsedIssueTemplateTransaction.getCreator().getAddress());

        //CHECK OWNER
        assertEquals(issueTemplateRecord.getItem().getMaker().getAddress(), parsedIssueTemplateTransaction.getItem().getMaker().getAddress());

        //CHECK NAME
        assertEquals(issueTemplateRecord.getItem().getName(), parsedIssueTemplateTransaction.getItem().getName());

        //CHECK DESCRIPTION
        assertEquals(issueTemplateRecord.getItem().getDescription(), parsedIssueTemplateTransaction.getItem().getDescription());

    }

    @Ignore
    //TODO actualize the test
    @Test
    public void processIssueTemplateTransaction() {

        init();

        Template template = new Template(itemAppData, maker, "test", icon, image, "strontje");

        //CREATE ISSUE PLATE TRANSACTION
        IssueTemplateRecord issueTemplateRecord = new IssueTemplateRecord(maker, template);
        issueTemplateRecord.sign(maker, forDeal);
        issueTemplateRecord.setDC(db, Transaction.FOR_PACK, BlockChain.SKIP_INVALID_SIGN_BEFORE, 1, false);

        assertEquals(Transaction.VALIDATE_OK, issueTemplateRecord.isValid(Transaction.FOR_PACK, txFlags));
        Long makerReference = maker.getLastTimestamp(db)[0];
        issueTemplateRecord.process(gb, forDeal);
        //makerReference = maker.getLastTimestamp(db)[0];

        LOGGER.info("template KEY: " + template.getKey());

        //CHECK PLATE EXISTS SENDER
        long key = issueTemplateRecord.key;
        assertEquals(true, db.getItemTemplateMap().contains(key));

        TemplateCls template_2 = new Template(itemAppData, maker, "test132_2", icon, image, "2_12345678910strontje");
        IssueTemplateRecord issueTemplateTransaction_2 = new IssueTemplateRecord(maker, template_2);
        issueTemplateTransaction_2.sign(maker, forDeal);
        issueTemplateTransaction_2.setDC(db, Transaction.FOR_NETWORK, BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
        issueTemplateTransaction_2.process(gb, forDeal);
        LOGGER.info("template_2 KEY: " + template_2.getKey());
        issueTemplateTransaction_2.orphan(gb, forDeal);
        ItemTemplateMap templateMap = db.getItemTemplateMap();
        int mapSize = templateMap.size();
        assertEquals(1048573, mapSize - 4);

        //CHECK PLATE IS CORRECT
        assertEquals(true, Arrays.equals(db.getItemTemplateMap().get(key).toBytes(forDeal, includeReference, false), template.toBytes(forDeal, includeReference, false)));

        //CHECK REFERENCE SENDER
        assertEquals((long) makerReference, (long) maker.getLastTimestamp(db)[0]);
    }


    @Test
    public void orphanIssueTemplateTransaction() {

        init();

        Template template = new Template(itemAppData, maker, "test", icon, image, "strontje");
        Long makerReference = maker.getLastTimestamp(db)[0];

        //CREATE ISSUE PLATE TRANSACTION
        IssueTemplateRecord issueTemplateRecord = new IssueTemplateRecord(maker, template);
        issueTemplateRecord.sign(maker, forDeal);
        issueTemplateRecord.setDC(db, Transaction.FOR_PACK, BlockChain.SKIP_INVALID_SIGN_BEFORE, 1, true);
        issueTemplateRecord.process(gb, forDeal);
        long key = issueTemplateRecord.key;
        assertEquals((long) makerReference, (long) maker.getLastTimestamp(db)[0]);

        issueTemplateRecord.orphan(gb, forDeal);

        //CHECK PLATE EXISTS SENDER
        assertEquals(false, db.getItemTemplateMap().contains(key));

        //CHECK REFERENCE SENDER
        assertEquals((long) makerReference, (long) maker.getLastTimestamp(db)[0]);
    }


}
