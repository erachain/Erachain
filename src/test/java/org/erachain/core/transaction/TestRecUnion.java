package org.erachain.core.transaction;

import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.ItemFactory;
import org.erachain.core.item.unions.Union;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.wallet.Wallet;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
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

public class TestRecUnion {

    static Logger LOGGER = LoggerFactory.getLogger(TestRecUnion.class.getName());

    //int releaserReference = null;

    BigDecimal BG_ZERO = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    ExLink linkTo = null;
    //long ALIVE_KEY = StatusCls.ALIVE_KEY;
    byte FEE_POWER = (byte) 1;
    long dbRef = 0L;
    byte[] unionReference = new byte[64];
    long timestamp = NTP.getTime();

    long[] itemFlags = null;
    long txFlags = 0l;

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
    List<PrivateKeyAccount> certifiedPrivateKeys = new ArrayList<PrivateKeyAccount>();
    List<PublicKeyAccount> certifiedPublicKeys = new ArrayList<PublicKeyAccount>();
    UnionCls unionGeneral;
    UnionCls union;
    long unionKey = -1;
    IssueUnionRecord issueUnionTransaction;
    //KKUnionStatusMap dbPS;
    int version = 0;
    long parent = -1;
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //UnionAddressMap dbPA;
    //AddressUnionMap dbAP;
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;

    // INIT UNIONS
    private void init() {

        db = DCSet.createEmptyDatabaseSet(0);

        gb = new GenesisBlock();
        try {
            gb.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //dbPA = db.getUnionAddressMap();
        //dbAP = db.getAddressUnionMap();
        //dbPS = db.getUnionStatusMap();

        // GET RIGHTS TO CERTIFIER
        unionGeneral = new Union(itemFlags, certifier, "СССР", timestamp - 12345678,
                parent, icon, image, "Союз Совестких Социалистических Республик");
        //GenesisIssueUnionRecord genesis_issue_union = new GenesisIssueUnionRecord(unionGeneral, registrar);
        //genesis_issue_union.process(db, false);
        //GenesisCertifyUnionRecord genesis_certify = new GenesisCertifyUnionRecord(registrar, 0L);
        //genesis_certify.process(db, false);

        certifier.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        certifier.changeBalance(db, false, false, ERM_KEY, BlockChain.MAJOR_ERA_BALANCE_BD, false, false, false);
        certifier.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        union = new Union(itemFlags, certifier, "РСФСР", timestamp - 1234567,
                parent + 1, icon, image, "Россия");


        //CREATE ISSUE UNION TRANSACTION
        issueUnionTransaction = new IssueUnionRecord(certifier, linkTo, union, FEE_POWER, timestamp, certifier.getLastTimestamp(db)[0]);

        certifiedPrivateKeys.add(userAccount1);
        certifiedPrivateKeys.add(userAccount2);
        certifiedPrivateKeys.add(userAccount3);

        certifiedPublicKeys.add(new PublicKeyAccount(userAccount1.getPublicKey()));
        certifiedPublicKeys.add(new PublicKeyAccount(userAccount2.getPublicKey()));
        certifiedPublicKeys.add(new PublicKeyAccount(userAccount3.getPublicKey()));

    }

    public void initUnionalize() {


        assertEquals(Transaction.VALIDATE_OK, issueUnionTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

        issueUnionTransaction.sign(certifier, Transaction.FOR_NETWORK);

        issueUnionTransaction.process(gb, Transaction.FOR_NETWORK);
        unionKey = union.getKey(db);

        assertEquals(1, unionKey);
        //assertEquals( null, dbPS.getItem(unionKey));

    }

    //ISSUE UNION TRANSACTION

    @Test
    public void validateSignatureIssueUnionRecord() {

        init();

        issueUnionTransaction.sign(certifier, Transaction.FOR_NETWORK);

        //CHECK IF ISSUE UNION TRANSACTION IS VALID
        assertEquals(true, issueUnionTransaction.isSignatureValid(db));

        //INVALID SIGNATURE
        issueUnionTransaction = new IssueUnionRecord(certifier, union, FEE_POWER, timestamp, certifier.getLastTimestamp(db)[0], new byte[64]);
        //CHECK IF ISSUE UNION IS INVALID
        assertEquals(false, issueUnionTransaction.isSignatureValid(db));

    }

    @Ignore
    //TODO actualize the test
    @Test
    public void validateIssueUnionRecord() {

        init();
        issueUnionTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issueUnionTransaction.sign(certifier, Transaction.FOR_NETWORK);

        //CHECK IF ISSUE UNION IS VALID
        assertEquals(Transaction.VALIDATE_OK, issueUnionTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

        //CREATE INVALID ISSUE UNION - INVALID UNIONALIZE
        issueUnionTransaction = new IssueUnionRecord(userAccount1, union, FEE_POWER, timestamp, userAccount1.getLastTimestamp(db)[0], new byte[64]);
        assertEquals(Transaction.NOT_ENOUGH_FEE, issueUnionTransaction.isValid(Transaction.FOR_NETWORK, txFlags));
        // ADD FEE
        userAccount1.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        assertEquals(Transaction.CREATOR_NOT_PERSONALIZED, issueUnionTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

        //CHECK IF ISSUE UNION IS VALID
        userAccount1.changeBalance(db, false, false, ERM_KEY, BlockChain.MINOR_ERA_BALANCE_BD, false, false, false);
        assertEquals(Transaction.CREATOR_NOT_PERSONALIZED, issueUnionTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

        //CHECK
        userAccount1.changeBalance(db, false, false, ERM_KEY, BlockChain.MAJOR_ERA_BALANCE_BD, false, false, false);
        assertEquals(Transaction.VALIDATE_OK, issueUnionTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

    }


    @Test
    public void parseIssueUnionRecord() {

        init();

        LOGGER.info("union: " + union.getTypeBytes()[0] + ", " + union.getTypeBytes()[1]);

        // PARSE UNION

        byte[] rawUnion = union.toBytes(false, false);
        assertEquals(rawUnion.length, union.getDataLength(false));
        union.setReference(new byte[64], dbRef);
        rawUnion = union.toBytes(true, false);
        assertEquals(rawUnion.length, union.getDataLength(true));

        rawUnion = union.toBytes(false, false);
        UnionCls parsedUnion = null;
        try {
            //PARSE FROM BYTES
            parsedUnion = (UnionCls) ItemFactory.getInstance()
                    .parse(ItemCls.UNION_TYPE, rawUnion, false);
        } catch (Exception e) {
            fail("Exception while parsing transaction.  : " + e);
        }
        assertEquals(rawUnion.length, union.getDataLength(false));
        assertEquals(union.getMaker().getAddress(), parsedUnion.getMaker().getAddress());
        assertEquals(union.getName(), parsedUnion.getName());
        assertEquals(union.getDescription(), parsedUnion.getDescription());
        assertEquals(union.getItemTypeName(), parsedUnion.getItemTypeName());
        assertEquals(union.getBirthday(), parsedUnion.getBirthday());
        assertEquals(union.getParent(), parsedUnion.getParent());

        // PARSE ISSEU UNION RECORD
        issueUnionTransaction.sign(certifier, Transaction.FOR_NETWORK);
        issueUnionTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issueUnionTransaction.process(gb, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawIssueUnionRecord = issueUnionTransaction.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawIssueUnionRecord.length, issueUnionTransaction.getDataLength(Transaction.FOR_NETWORK, true));

        IssueUnionRecord parsedIssueUnionRecord = null;
        try {
            //PARSE FROM BYTES
            parsedIssueUnionRecord = (IssueUnionRecord) TransactionFactory.getInstance().parse(rawIssueUnionRecord, Transaction.FOR_NETWORK);

        } catch (Exception e) {
            fail("Exception while parsing transaction.  : " + e);
        }

        //CHECK INSTANCE
        assertEquals(true, parsedIssueUnionRecord instanceof IssueUnionRecord);

        //CHECK SIGNATURE
        assertEquals(true, Arrays.equals(issueUnionTransaction.getSignature(), parsedIssueUnionRecord.getSignature()));

        //CHECK ISSUER
        assertEquals(issueUnionTransaction.getCreator().getAddress(), parsedIssueUnionRecord.getCreator().getAddress());

        parsedUnion = (Union) parsedIssueUnionRecord.getItem();

        //CHECK OWNER
        assertEquals(union.getMaker().getAddress(), parsedUnion.getMaker().getAddress());

        //CHECK NAME
        assertEquals(union.getName(), parsedUnion.getName());

        //CHECK REFERENCE
        //assertEquals(issueUnionTransaction.getReference(), parsedIssueUnionRecord.getReference());

        //CHECK TIMESTAMP
        assertEquals(issueUnionTransaction.getTimestamp(), parsedIssueUnionRecord.getTimestamp());

        //CHECK DESCRIPTION
        assertEquals(union.getDescription(), parsedUnion.getDescription());

        assertEquals(union.getItemTypeName(), parsedUnion.getItemTypeName());
        assertEquals(union.getBirthday(), parsedUnion.getBirthday());
        assertEquals(union.getParent(), parsedUnion.getParent());

        //PARSE TRANSACTION FROM WRONG BYTES
        rawIssueUnionRecord = new byte[issueUnionTransaction.getDataLength(Transaction.FOR_NETWORK, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawIssueUnionRecord, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }

    @Ignore
    //TODO actualize the test
    @Test
    public void processIssueUnionRecord() {

        init();
        issueUnionTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(Transaction.VALIDATE_OK, issueUnionTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

        issueUnionTransaction.sign(certifier, Transaction.FOR_NETWORK);

        issueUnionTransaction.process(gb, Transaction.FOR_NETWORK);

        LOGGER.info("union KEY: " + union.getKey(db));

        //CHECK BALANCE ISSUER
        assertEquals(BlockChain.MAJOR_ERA_BALANCE_BD, certifier.getBalanceUSE(ERM_KEY, db));
        assertEquals(BigDecimal.valueOf(1).subtract(issueUnionTransaction.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), certifier.getBalanceUSE(FEE_KEY, db));

        //CHECK UNION EXISTS DB AS CONFIRMED:  key > -1
        long key = db.getIssueUnionMap().get(issueUnionTransaction);
        assertEquals(1, key);
        assertEquals(true, db.getItemUnionMap().contains(key));

        //CHECK UNION IS CORRECT
        assertEquals(true, Arrays.equals(db.getItemUnionMap().get(key).toBytes(true, false), union.toBytes(true, false)));

        //CHECK REFERENCE SENDER
        assertEquals(issueUnionTransaction.getTimestamp(), certifier.getLastTimestamp(db));

        //////// ORPHAN /////////
        issueUnionTransaction.orphan(gb, Transaction.FOR_NETWORK);

        //CHECK BALANCE ISSUER
        assertEquals(BlockChain.MAJOR_ERA_BALANCE_BD, certifier.getBalanceUSE(ERM_KEY, db));
        assertEquals(BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), certifier.getBalanceUSE(FEE_KEY, db));

        //CHECK UNION EXISTS ISSUER
        assertEquals(false, db.getItemUnionMap().contains(unionKey));

        //CHECK REFERENCE ISSUER
        //assertEquals(issueUnionTransaction.getReference(), registrar.getLastReference(db));
    }


}
