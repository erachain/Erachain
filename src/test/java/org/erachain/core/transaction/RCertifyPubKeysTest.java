package org.erachain.core.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.IDB;
import org.erachain.datachain.AddressPersonMap;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.KKPersonStatusMap;
import org.erachain.datachain.PersonAddressMap;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Test;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RCertifyPubKeysTest {

    static Logger LOGGER = LoggerFactory.getLogger(RCertifyPubKeysTest.class.getName());

    int[] TESTED_DBS = new int[]{
            IDB.DBS_MAP_DB
            , IDB.DBS_ROCK_DB
    };

    BigDecimal BG_ZERO = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    //long ALIVE_KEY = StatusCls.ALIVE_KEY;
    byte FEE_POWER = (byte) 1;
    byte[] personReference = new byte[64];
    long timestamp = NTP.getTime();

    long dbRef = 0L;
    long flags = 0l;
    Long last_ref;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount registrar = new PrivateKeyAccount(privateKey);

    //GENERATE ACCOUNT SEED
    int nonce = 1;

    byte[] certifierSeed = Wallet.generateAccountSeed(seed, nonce++);
    PrivateKeyAccount certifier = new PrivateKeyAccount(certifierSeed);

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
    PersonCls personGeneral;
    PersonCls person;
    long genesisPersonKey = -1;
    long personKey = -1;
    IssuePersonRecord issuePersonTransaction;
    RCertifyPubKeys r_CertifyPubKeys;
    KKPersonStatusMap dbPS;
    PersonAddressMap dbPA;
    AddressPersonMap dbAP;
    //int version = 0; // without signs of person
    int version = 1; // with signs of person
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    int seqNo = 1;
    private byte[] image = new byte[18000]; // default value
    private byte[] ownerSignature = new byte[Crypto.SIGNATURE_LENGTH];
    //CREATE EMPTY MEMORY DATABASE
    private DCSet dcSet;
    private BlockChain bchain;
    private Controller cntrl;
    private GenesisBlock gb;

    // INIT PERSONS
    private void init(int dbs) {
        LOGGER.info(" ********** open DBS: " + dbs);

        File tempDir = new File(Settings.getInstance().getDataTempDir());
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
            LOGGER.info(" ********** " + e.getMessage());
        }

        dcSet = DCSet.createEmptyHardDatabaseSetWithFlush(null, dbs);

        cntrl = Controller.getInstance();
        cntrl.initBlockChain(dcSet);
        bchain = cntrl.getBlockChain();
        gb = bchain.getGenesisBlock();

        dbPA = dcSet.getPersonAddressMap();
        dbAP = dcSet.getAddressPersonMap();
        dbPS = dcSet.getPersonStatusMap();

        try {
            gb.process(dcSet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        last_ref = gb.getTimestamp();

        registrar.setLastTimestamp(new long[]{last_ref, 0}, dcSet);
        registrar.changeBalance(dcSet, false, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        registrar.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        certifier.setLastTimestamp(new long[]{last_ref, 0}, dcSet);
        certifier.changeBalance(dcSet, false, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        certifier.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        byte gender = 0;
        long birthDay = timestamp - 12345678;

        ownerSignature = new byte[64];
        ownerSignature[1] = (byte) 1;
        person = new PersonHuman(flags, registrar, "Ermolaev Dmitrii Sergeevich as registrar", birthDay, birthDay - 1,
                gender, "Slav", (float) 28.12345, (float) 133.7777,
                "white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей", ownerSignature);
        person.setReference(ownerSignature, dbRef);
        dcSet.getItemPersonMap().incrementPut(person);
        long keyRegistrar = person.getKey(dcSet);

        ownerSignature = new byte[64];
        ownerSignature[1] = (byte) 2;
        person = new PersonHuman(flags, certifier, "Ermolaev Dmitrii Sergeevich as certifier", birthDay, birthDay - 1,
                gender, "Slav", (float) 28.12345, (float) 133.7777,
                "white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей", ownerSignature);
        person.setReference(ownerSignature, dbRef);
        dcSet.getItemPersonMap().incrementPut(person);
        long keyCertifier = person.getKey(dcSet);

        // внесем его как удостовренную персону
        Fun.Tuple3<Integer, Integer, Integer> itemPRegistrar = new Fun.Tuple3<Integer, Integer, Integer>(999999, 2, 2);
        Fun.Tuple4<Long, Integer, Integer, Integer> itemARegistrar = new Fun.Tuple4<Long, Integer, Integer, Integer>(keyRegistrar, 999999, 2, 3);

        dcSet.getAddressPersonMap().addItem(registrar.getShortAddressBytes(), itemARegistrar);
        dcSet.getPersonAddressMap().addItem(33L, registrar.getAddress(), itemPRegistrar);

        // внесем его как удостовренную персону
        Fun.Tuple3<Integer, Integer, Integer> itemPCertifier = new Fun.Tuple3<Integer, Integer, Integer>(999999, 2, 2);
        Fun.Tuple4<Long, Integer, Integer, Integer> itemACertifier = new Fun.Tuple4<Long, Integer, Integer, Integer>(keyCertifier, 999999, 2, 3);

        dcSet.getAddressPersonMap().addItem(certifier.getShortAddressBytes(), itemACertifier);
        dcSet.getPersonAddressMap().addItem(33L, certifier.getAddress(), itemPCertifier);

        ownerSignature = new byte[64];
        ownerSignature[1] = (byte) -1;

        // GET RIGHTS TO CERTIFIER
        personGeneral = new PersonHuman(flags, registrar, "Ermolaev Dmitrii Sergeevich as certifier", birthDay, birthDay - 1,
                gender, "Slav", (float) 28.12345, (float) 133.7777,
                "white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей", ownerSignature);
        //personGeneral.setKey(genesisPersonKey);

        GenesisIssuePersonRecord genesis_issue_person = new GenesisIssuePersonRecord(personGeneral);
        genesis_issue_person.setDC(dcSet, Transaction.FOR_NETWORK, 3, seqNo++, true);
        genesis_issue_person.process(gb, Transaction.FOR_NETWORK);
        //genesisPersonKey = dcSet.getIssuePersonMap().size();
        genesisPersonKey = genesis_issue_person.getAssetKey(dcSet);

        GenesisCertifyPersonRecord genesis_certify = new GenesisCertifyPersonRecord(registrar, genesisPersonKey);
        genesis_certify.setDC(dcSet, Transaction.FOR_NETWORK, 3, seqNo++, true);
        genesis_certify.process(gb, Transaction.FOR_NETWORK);

        person = new PersonHuman(flags, registrar, "Ermolaev Dmitrii Sergeevich", birthDay, birthDay - 2,
                gender, "Slav", (float) 28.12345, (float) 133.7777,
                "white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей", ownerSignature);

        //person.setKey(genesisPersonKey + 1);
        //CREATE ISSUE PERSON TRANSACTION
        issuePersonTransaction = new IssuePersonRecord(registrar, person, FEE_POWER, timestamp, registrar.getLastTimestamp(dcSet)[0], null);

        if (certifiedPrivateKeys.isEmpty()) {
            certifiedPrivateKeys.add(userAccount1);
            certifiedPrivateKeys.add(userAccount2);
            certifiedPrivateKeys.add(userAccount3);
        }

        if (certifiedPublicKeys.isEmpty()) {
            certifiedPublicKeys.add(new PublicKeyAccount(userAccount1.getPublicKey()));
            certifiedPublicKeys.add(new PublicKeyAccount(userAccount2.getPublicKey()));
            certifiedPublicKeys.add(new PublicKeyAccount(userAccount3.getPublicKey()));
        }

    }

    public void initPersonalize() {

        issuePersonTransaction.setDC(dcSet, Transaction.FOR_NETWORK, 3, seqNo++, true);
        assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(Transaction.FOR_NETWORK, flags));

        issuePersonTransaction.sign(registrar, Transaction.FOR_NETWORK);

        issuePersonTransaction.process(gb, Transaction.FOR_NETWORK);

        // нужно занести ее в базу чтобы считать по этой записи персону создавшую эту запись
        dcSet.getTransactionFinalMap().put(issuePersonTransaction);
        dcSet.getTransactionFinalMapSigns().put(issuePersonTransaction.signature, issuePersonTransaction.dbRef);

        personKey = person.getKey(dcSet);

        // issue 1 genesis person in init() here
        //assertEquals( genesisPersonKey + 1, personKey);
        //assertEquals( null, dbPS.getItem(personKey, ALIVE_KEY));

        //CREATE PERSONALIZE REcORD
        timestamp += 100;
        r_CertifyPubKeys = new RCertifyPubKeys(version, certifier, FEE_POWER, personKey,
                certifiedPublicKeys,
                timestamp, registrar.getLastTimestamp(dcSet)[0]);

    }

    @Test
    public void test1() {

        // see org.erachain.core.transaction.TestRecPerson.init
        for (int dbs : TESTED_DBS) {

            init(dbs);

            assertEquals(registrar.getLastTimestamp(dcSet), null);

            RCertifyPubKeys certPubKey = new RCertifyPubKeys(0, registrar, FEE_POWER,
                    person.getKey(dcSet), certifiedPublicKeys, timestamp, 0L);
            certPubKey.setDC(dcSet, Transaction.FOR_NETWORK, 3, 5, true);
            certPubKey.process(null, Transaction.FOR_NETWORK);

            registrar.getLastTimestamp(dcSet);

            assertEquals(new long[]{123L, 345L}, registrar.getLastTimestamp(dcSet));

        }

    }


    @Test
    public void parse() {
    }

    @Test
    public void toBytes() {
    }

    @Test
    public void isSignatureValid() {
    }

    @Test
    public void isValid() {
    }

    @Test
    public void process() {
    }

    @Test
    public void orphan() {
    }
}