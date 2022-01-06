package org.erachain.core.transaction;

import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.templates.Template;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemTemplateMap;
import org.erachain.ntp.NTP;
import org.erachain.utils.Corekeys;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

//import java.math.BigInteger;
//import java.util.ArrayList;
//import java.util.List;

public class TestRecTemplate {

    static Logger LOGGER = LoggerFactory.getLogger(TestRecTemplate.class.getName());

    int forDeal = Transaction.FOR_NETWORK;

    //Long Transaction.FOR_NETWORK = null;

    int asPack = Transaction.FOR_NETWORK;
    long FEE_KEY = AssetCls.FEE_KEY;
    long VOTE_KEY = AssetCls.ERA_KEY;
    byte FEE_POWER = (byte) 1;
    byte[] templateReference = new byte[64];
    long timestamp = NTP.getTime();

    byte[] itemAppData = null;
    long txFlags = 0L;

    byte[] data = "test123!".getBytes();
    byte[] dbData = null;
    byte[] isText = new byte[]{1};
    byte[] encrypted = new byte[]{0};
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    TemplateCls template;
    long templateKey = -1;
    IssueTemplateRecord issueTemplateRecord;
    RSignNote signNoteRecord;
    ItemTemplateMap templateMap;
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;
    private List<String> imagelinks = new ArrayList<String>();

    // INIT TEMPLATES
    private void init() {

        db = DCSet.createEmptyDatabaseSet(0);
        templateMap = db.getItemTemplateMap();

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

    }

    private void initTemplate(boolean process) {

        template = new Template(itemAppData, maker, "test132", icon, image, "12345678910strontje");

        //CREATE ISSUE PLATE TRANSACTION
        issueTemplateRecord = new IssueTemplateRecord(maker, null, template, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0]);
        issueTemplateRecord.sign(maker, Transaction.FOR_NETWORK);
        issueTemplateRecord.setDC(db, Transaction.FOR_NETWORK, BlockChain.SKIP_INVALID_SIGN_BEFORE, 1, true);
        if (process) {
            issueTemplateRecord.process(gb, Transaction.FOR_NETWORK);
            templateKey = template.getKey();
        }
    }

    //ISSUE PLATE TRANSACTION

    @Test
    public void testAddreessVersion() {
        int vers = Corekeys.findAddressVersion("E");
        assertEquals(-1111, vers);
    }

    @Test
    public void validateSignatureIssueTemplateTransaction() {

        init();

        initTemplate(false);

        //CHECK IF ISSUE PLATE TRANSACTION IS VALID
        issueTemplateRecord.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
        assertEquals(true, issueTemplateRecord.isSignatureValid(db));

        //INVALID SIGNATURE
        issueTemplateRecord = new IssueTemplateRecord(maker, template, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0], new byte[64]);

        //CHECK IF ISSUE PLATE IS INVALID
        issueTemplateRecord.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
        assertEquals(false, issueTemplateRecord.isSignatureValid(db));
    }

    @Ignore
    //TODO actualize the test
    @Test
    public void parseIssueTemplateTransaction() {

        init();

        TemplateCls template = new Template(itemAppData, maker, "test132", icon, image, "12345678910strontje");
        byte[] raw = template.toBytes(forDeal, false, false);
        assertEquals(raw.length, template.getDataLength(false));

        //CREATE ISSUE PLATE TRANSACTION
        IssueTemplateRecord issueTemplateRecord = new IssueTemplateRecord(maker, null, template, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0]);
        issueTemplateRecord.sign(maker, Transaction.FOR_NETWORK);
        issueTemplateRecord.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issueTemplateRecord.process(gb, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawIssueTemplateTransaction = issueTemplateRecord.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawIssueTemplateTransaction.length, issueTemplateRecord.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            IssueTemplateRecord parsedIssueTemplateTransaction = (IssueTemplateRecord) TransactionFactory.getInstance().parse(rawIssueTemplateTransaction, Transaction.FOR_NETWORK);
            LOGGER.info("parsedIssueTemplateTransaction: " + parsedIssueTemplateTransaction);

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

            //CHECK FEE
            assertEquals(issueTemplateRecord.getFee(), parsedIssueTemplateTransaction.getFee());

            //CHECK REFERENCE
            //assertEquals(issueTemplateRecord.getReference(), parsedIssueTemplateTransaction.getReference());

            //CHECK TIMESTAMP
            assertEquals(issueTemplateRecord.getTimestamp(), parsedIssueTemplateTransaction.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

    }

    @Ignore
//TODO actualize the test
    @Test
    public void processIssueTemplateTransaction() {

        init();

        Template template = new Template(itemAppData, maker, "test", icon, image, "strontje");

        //CREATE ISSUE PLATE TRANSACTION
        IssueTemplateRecord issueTemplateRecord = new IssueTemplateRecord(maker, null, template, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0]);
        issueTemplateRecord.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(Transaction.VALIDATE_OK, issueTemplateRecord.isValid(Transaction.FOR_NETWORK, txFlags));

        issueTemplateRecord.sign(maker, Transaction.FOR_NETWORK);
        issueTemplateRecord.process(gb, Transaction.FOR_NETWORK);
        int mapSize = templateMap.size();

        LOGGER.info("template KEY: " + template.getKey());

        //CHECK PLATE EXISTS SENDER
        long key = issueTemplateRecord.key;
        assertEquals(true, templateMap.contains(key));

        TemplateCls template_2 = new Template(itemAppData, maker, "test132_2", icon, image, "2_12345678910strontje");
        IssueTemplateRecord issueTemplateTransaction_2 = new IssueTemplateRecord(maker, null, template_2, FEE_POWER, timestamp + 10, maker.getLastTimestamp(db)[0]);
        issueTemplateTransaction_2.sign(maker, Transaction.FOR_NETWORK);
        issueTemplateTransaction_2.process(gb, Transaction.FOR_NETWORK);
        LOGGER.info("template_2 KEY: " + template_2.getKey());
        issueTemplateTransaction_2.orphan(gb, Transaction.FOR_NETWORK);
        assertEquals(mapSize, templateMap.size());

        //CHECK PLATE IS CORRECT
        assertEquals(true, Arrays.equals(templateMap.get(key).toBytes(forDeal, true, false), template.toBytes(forDeal, true, false)));

        //CHECK REFERENCE SENDER
        assertEquals(issueTemplateRecord.getTimestamp(), maker.getLastTimestamp(db));
    }

    // TODO - in statement - valid on key = 999

    //SIGN PLATE TRANSACTION

    @Test
    public void orphanIssueTemplateTransaction() {

        init();

        Template template = new Template(itemAppData, maker, "test", icon, image, "strontje");

        //CREATE ISSUE PLATE TRANSACTION
        IssueTemplateRecord issueTemplateRecord = new IssueTemplateRecord(maker, null, template, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0]);
        issueTemplateRecord.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issueTemplateRecord.sign(maker, Transaction.FOR_NETWORK);
        issueTemplateRecord.process(gb, Transaction.FOR_NETWORK);
        long key = issueTemplateRecord.key;
        assertEquals(issueTemplateRecord.getTimestamp(), maker.getLastTimestamp(db));

        issueTemplateRecord.orphan(gb, Transaction.FOR_NETWORK);

        //CHECK PLATE EXISTS SENDER
        assertEquals(false, templateMap.contains(key));

        //CHECK REFERENCE SENDER
        //assertEquals(issueTemplateRecord.getReference(), maker.getLastReference(db));
    }

    @Test
    public void validateSignatureSignNoteTransaction() {

        init();

        initTemplate(true);

        signNoteRecord = new RSignNote(maker, FEE_POWER, templateKey, data, dbData, timestamp + 10, maker.getLastTimestamp(db)[0]);
        signNoteRecord.sign(maker, asPack);

        //CHECK IF ISSUE PLATE TRANSACTION IS VALID
        signNoteRecord.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
        assertEquals(true, signNoteRecord.isSignatureValid(db));

        //INVALID SIGNATURE
        signNoteRecord = new RSignNote(maker, FEE_POWER, templateKey, data, dbData, timestamp + 10, maker.getLastTimestamp(db)[0], new byte[64]);

        //CHECK IF ISSUE PLATE IS INVALID
        signNoteRecord.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
        assertEquals(false, signNoteRecord.isSignatureValid(db));
    }

    @Test
    public void parseSignNoteTransaction() {

        init();

        initTemplate(true);

        signNoteRecord = new RSignNote(maker, FEE_POWER, templateKey, data, dbData, timestamp + 10, maker.getLastTimestamp(db)[0]);
        signNoteRecord.sign(maker, asPack);

        //CONVERT TO BYTES
        byte[] rawSignNoteRecord = signNoteRecord.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawSignNoteRecord.length, signNoteRecord.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            RSignNote parsedSignNoteRecord = (RSignNote) TransactionFactory.getInstance().parse(rawSignNoteRecord, Transaction.FOR_NETWORK);
            LOGGER.info("parsedSignNote: " + parsedSignNoteRecord);

            //CHECK INSTANCE
            assertEquals(true, parsedSignNoteRecord instanceof RSignNote);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(signNoteRecord.getSignature(), parsedSignNoteRecord.getSignature()));

            //CHECK ISSUER
            assertEquals(signNoteRecord.getCreator().getAddress(), parsedSignNoteRecord.getCreator().getAddress());

            //CHECK OWNER
            assertEquals(signNoteRecord.getKey(), parsedSignNoteRecord.getKey());

            //CHECK NAME
            assertEquals(true, Arrays.equals(signNoteRecord.getData(), parsedSignNoteRecord.getData()));

            //CHECK DESCRIPTION
            assertEquals(signNoteRecord.isText(), parsedSignNoteRecord.isText());

            //CHECK FEE
            assertEquals(signNoteRecord.getFee(), parsedSignNoteRecord.getFee());

            //CHECK REFERENCE
            //assertEquals(signNoteRecord.getReference(), parsedSignNoteRecord.getReference());

            //CHECK TIMESTAMP
            assertEquals(signNoteRecord.getTimestamp(), parsedSignNoteRecord.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }


        // NOT DATA
        data = null;
        signNoteRecord = new RSignNote(maker, FEE_POWER, templateKey, data, dbData, timestamp + 20, maker.getLastTimestamp(db)[0]);
        signNoteRecord.sign(maker, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        rawSignNoteRecord = signNoteRecord.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawSignNoteRecord.length, signNoteRecord.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            RSignNote parsedSignNoteRecord = (RSignNote) TransactionFactory.getInstance().parse(rawSignNoteRecord, Transaction.FOR_NETWORK);
            LOGGER.info("parsedSignNote: " + parsedSignNoteRecord);

            //CHECK INSTANCE
            assertEquals(true, parsedSignNoteRecord instanceof RSignNote);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(signNoteRecord.getSignature(), parsedSignNoteRecord.getSignature()));

            //CHECK ISSUER
            assertEquals(signNoteRecord.getCreator().getAddress(), parsedSignNoteRecord.getCreator().getAddress());

            //CHECK OWNER
            assertEquals(signNoteRecord.getKey(), parsedSignNoteRecord.getKey());

            //CHECK NAME
            assertEquals(null, parsedSignNoteRecord.getData());

            //CHECK DESCRIPTION
            assertEquals(signNoteRecord.isText(), parsedSignNoteRecord.isText());

            //CHECK FEE
            assertEquals(signNoteRecord.getFee(), parsedSignNoteRecord.getFee());

            //CHECK REFERENCE
            //assertEquals(signNoteRecord.getReference(), parsedSignNoteRecord.getReference());

            //CHECK TIMESTAMP
            assertEquals(signNoteRecord.getTimestamp(), parsedSignNoteRecord.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

        // NOT KEY
        //data = null;
        templateKey = 0;
        signNoteRecord = new RSignNote(maker, FEE_POWER, templateKey, data, dbData, timestamp + 20, maker.getLastTimestamp(db)[0]);
        signNoteRecord.sign(maker, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        rawSignNoteRecord = signNoteRecord.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawSignNoteRecord.length, signNoteRecord.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            RSignNote parsedSignNoteRecord = (RSignNote) TransactionFactory.getInstance().parse(rawSignNoteRecord, Transaction.FOR_NETWORK);
            LOGGER.info("parsedSignNote: " + parsedSignNoteRecord);

            //CHECK INSTANCE
            assertEquals(true, parsedSignNoteRecord instanceof RSignNote);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(signNoteRecord.getSignature(), parsedSignNoteRecord.getSignature()));

            //CHECK ISSUER
            assertEquals(signNoteRecord.getCreator().getAddress(), parsedSignNoteRecord.getCreator().getAddress());

            //CHECK OWNER
            assertEquals(signNoteRecord.getKey(), parsedSignNoteRecord.getKey());

            //CHECK NAME
            assertEquals(null, parsedSignNoteRecord.getData());

            //CHECK DESCRIPTION
            assertEquals(signNoteRecord.isText(), parsedSignNoteRecord.isText());

            //CHECK FEE
            assertEquals(signNoteRecord.getFee(), parsedSignNoteRecord.getFee());

            //CHECK REFERENCE
            //assertEquals(signNoteRecord.getReference(), parsedSignNoteRecord.getReference());

            //CHECK TIMESTAMP
            assertEquals(signNoteRecord.getTimestamp(), parsedSignNoteRecord.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

        // NOT KEY
        data = null;
        templateKey = 0;
        signNoteRecord = new RSignNote(maker, FEE_POWER, templateKey, data, dbData, timestamp + 20, maker.getLastTimestamp(db)[0]);
        signNoteRecord.sign(maker, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        rawSignNoteRecord = signNoteRecord.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawSignNoteRecord.length, signNoteRecord.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            RSignNote parsedSignNoteRecord = (RSignNote) TransactionFactory.getInstance().parse(rawSignNoteRecord, Transaction.FOR_NETWORK);
            LOGGER.info("parsedSignNote: " + parsedSignNoteRecord);

            //CHECK INSTANCE
            assertEquals(true, parsedSignNoteRecord instanceof RSignNote);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(signNoteRecord.getSignature(), parsedSignNoteRecord.getSignature()));

            //CHECK ISSUER
            assertEquals(signNoteRecord.getCreator().getAddress(), parsedSignNoteRecord.getCreator().getAddress());

            //CHECK OWNER
            assertEquals(signNoteRecord.getKey(), parsedSignNoteRecord.getKey());

            //CHECK NAME
            assertEquals(null, parsedSignNoteRecord.getData());

            //CHECK DESCRIPTION
            assertEquals(signNoteRecord.isText(), parsedSignNoteRecord.isText());

            //CHECK FEE
            assertEquals(signNoteRecord.getFee(), parsedSignNoteRecord.getFee());

            //CHECK REFERENCE
            //assertEquals(signNoteRecord.getReference(), parsedSignNoteRecord.getReference());

            //CHECK TIMESTAMP
            assertEquals(signNoteRecord.getTimestamp(), parsedSignNoteRecord.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

    }
    @Ignore
    //TODO actualize the test
    @Test
    public void processSignNoteTransaction() {

        init();

        initTemplate(true);

        signNoteRecord = new RSignNote(maker, FEE_POWER, templateKey, data, dbData, timestamp + 10, maker.getLastTimestamp(db)[0]);
        signNoteRecord.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(Transaction.VALIDATE_OK, signNoteRecord.isValid(Transaction.FOR_NETWORK, txFlags));

        signNoteRecord.sign(maker, Transaction.FOR_NETWORK);
        signNoteRecord.process(gb, Transaction.FOR_NETWORK);

        //CHECK REFERENCE SENDER
        assertEquals(signNoteRecord.getTimestamp(), maker.getLastTimestamp(db));

        ///// ORPHAN
        signNoteRecord.orphan(gb, Transaction.FOR_NETWORK);

        //CHECK REFERENCE SENDER
        //assertEquals(signNoteRecord.getReference(), maker.getLastReference(db));
    }

    private void handleVars(String description) {
        Pattern pattern = Pattern.compile(Pattern.quote("{{") + "(.+?)" + Pattern.quote("}}"));
        //Pattern pattern = Pattern.compile("{{(.+)}}");
        Matcher matcher = pattern.matcher(description);
        while (matcher.find()) {
            String url = matcher.group(1);
            imagelinks.add(url);
            //description = description.replace(matcher.group(), getImgHtml(url));
        }
    }

    @Test
    public void regExTest() {
        String descr = "AJH {{wer}}, asdj {{we431!12}}";
        ArrayList arrayList = new ArrayList() {{
            add("wer");
            add("we431!12");
        }};
        handleVars(descr);
        assertEquals(imagelinks, arrayList);
    }
}
