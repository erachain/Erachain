package org.erachain.core.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.ItemFactory;
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
import org.mapdb.Fun.Tuple4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TestRecPerson {

    static Logger LOGGER = LoggerFactory.getLogger(TestRecPerson.class.getName());

    int[] TESTED_DBS = new int[]{
            IDB.DBS_MAP_DB,
            IDB.DBS_ROCK_DB
    };

    BigDecimal BG_ZERO = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    //long ALIVE_KEY = StatusCls.ALIVE_KEY;
    byte FEE_POWER = (byte) 1;
    byte[] personReference = new byte[64];
    long timestamp = NTP.getTime();

    long flags = 0l;
    Long last_ref;
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
    List<PrivateKeyAccount> sertifiedPrivateKeys = new ArrayList<PrivateKeyAccount>();
    List<PublicKeyAccount> sertifiedPublicKeys = new ArrayList<PublicKeyAccount>();
    PersonCls personGeneral;
    PersonCls person;
    long genesisPersonKey = -1;
    long personKey = -1;
    IssuePersonRecord issuePersonTransaction;
    RSertifyPubKeys r_SertifyPubKeys;
    KKPersonStatusMap dbPS;
    PersonAddressMap dbPA;
    AddressPersonMap dbAP;
    //int version = 0; // without signs of person
    int version = 1; // with signs of person
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[2000]; // default value
    private byte[] ownerSignature = new byte[Crypto.SIGNATURE_LENGTH];
    //CREATE EMPTY MEMORY DATABASE
    private DCSet dcSet;
    private Controller cntrl;
    private BlockChain bchain;
    private GenesisBlock gb;

    int seqNo = 1;

    // INIT PERSONS
    private void init(int dbs) {
        LOGGER.info(" ********** open DBS: " + dbs);

        File tempDir = new File(Settings.getInstance().getDataTempDir());
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
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

        // GET RIGHTS TO CERTIFIER
        byte gender = 1;
        long birthDay = timestamp - 12345678;
        personGeneral = new PersonHuman(certifier, "Ermolaev Dmitrii Sergeevich as sertifier", birthDay, birthDay - 1,
                gender, "Slav", (float) 28.12345, (float) 133.7777,
                "white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей", ownerSignature);
        //personGeneral.setKey(genesisPersonKey);

        GenesisIssuePersonRecord genesis_issue_person = new GenesisIssuePersonRecord(personGeneral);
        genesis_issue_person.setDC(dcSet, Transaction.FOR_NETWORK, 3, seqNo++);
        genesis_issue_person.process(gb, Transaction.FOR_NETWORK);
        //genesisPersonKey = dcSet.getIssuePersonMap().size();
        genesisPersonKey = genesis_issue_person.getAssetKey(dcSet);

        GenesisCertifyPersonRecord genesis_certify = new GenesisCertifyPersonRecord(certifier, genesisPersonKey);
        genesis_certify.setDC(dcSet, Transaction.FOR_NETWORK, 3, seqNo++);
        genesis_certify.process(gb, Transaction.FOR_NETWORK);

        certifier.setLastTimestamp(new long[]{last_ref, 0}, dcSet);
        certifier.changeBalance(dcSet, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
        certifier.changeBalance(dcSet, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);

        person = new PersonHuman(certifier, "Ermolaev Dmitrii Sergeevich", birthDay, birthDay - 2,
                gender, "Slav", (float) 28.12345, (float) 133.7777,
                "white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей", ownerSignature);

        //person.setKey(genesisPersonKey + 1);
        //CREATE ISSUE PERSON TRANSACTION
        issuePersonTransaction = new IssuePersonRecord(certifier, person, FEE_POWER, timestamp, certifier.getLastTimestamp(dcSet)[0]);

        sertifiedPrivateKeys.add(userAccount1);
        sertifiedPrivateKeys.add(userAccount2);
        sertifiedPrivateKeys.add(userAccount3);

        sertifiedPublicKeys.add(new PublicKeyAccount(userAccount1.getPublicKey()));
        sertifiedPublicKeys.add(new PublicKeyAccount(userAccount2.getPublicKey()));
        sertifiedPublicKeys.add(new PublicKeyAccount(userAccount3.getPublicKey()));

    }

    public void initPersonalize() {

        issuePersonTransaction.setDC(dcSet, Transaction.FOR_NETWORK, 3, seqNo++);
        assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(Transaction.FOR_NETWORK, flags));

        issuePersonTransaction.sign(certifier, Transaction.FOR_NETWORK);

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
        r_SertifyPubKeys = new RSertifyPubKeys(version, certifier, FEE_POWER, personKey,
                sertifiedPublicKeys,
                timestamp, certifier.getLastTimestamp(dcSet)[0]);

    }

    //ISSUE PERSON TRANSACTION

    @Test
    public void validateSignatureIssuePersonRecord() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                issuePersonTransaction.sign(certifier, Transaction.FOR_NETWORK);

                //CHECK IF ISSUE PERSON TRANSACTION IS VALID
                assertEquals(true, issuePersonTransaction.isSignatureValid(dcSet));

                //INVALID SIGNATURE
                issuePersonTransaction = new IssuePersonRecord(certifier, person, FEE_POWER, timestamp, certifier.getLastTimestamp(dcSet)[0], new byte[64]);
                //CHECK IF ISSUE PERSON IS INVALID
                assertEquals(false, issuePersonTransaction.isSignatureValid(dcSet));

            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void validateIssuePersonRecord() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                issuePersonTransaction.setDC(dcSet);

                //issuePersonTransaction.sign(certifier, Transaction.FOR_NETWORK);

                // ADD FEE
                userAccount1.changeBalance(dcSet, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);

                //CHECK IF ISSUE PERSON IS VALID
                assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(Transaction.FOR_NETWORK, flags));

                //CREATE INVALID ISSUE PERSON - INVALID PERSONALIZE
                issuePersonTransaction = new IssuePersonRecord(userAccount1, person, FEE_POWER, timestamp, timestamp + 10L, new byte[64]);
                issuePersonTransaction.setDC(dcSet);
                assertEquals(Transaction.ITEM_PERSON_OWNER_SIGNATURE_INVALID, issuePersonTransaction.isValid(Transaction.FOR_NETWORK, flags));

                ((PersonHuman) person).sign(certifier);
                assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(Transaction.FOR_NETWORK, flags));

            } finally {
                dcSet.close();
            }
        }
    }


    @Test
    public void parseIssuePersonRecord() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                LOGGER.info("person: " + person.getTypeBytes()[0] + ", " + person.getTypeBytes()[1]);

                // PARSE PERSON

                byte[] rawPerson = person.toBytes(false, false);
                assertEquals(rawPerson.length, person.getDataLength(false));
                person.setReference(new byte[64]);
                rawPerson = person.toBytes(true, false);
                assertEquals(rawPerson.length, person.getDataLength(true));

                rawPerson = person.toBytes(false, false);
                PersonCls parsedPerson = null;
                try {
                    //PARSE FROM BYTES
                    parsedPerson = (PersonCls) ItemFactory.getInstance()
                            .parse(ItemCls.PERSON_TYPE, rawPerson, false);
                } catch (Exception e) {
                    fail("Exception while parsing transaction.  : " + e);
                }
                assertEquals(rawPerson.length, person.getDataLength(false));
                assertEquals(parsedPerson.getHeight(), person.getHeight());
                assertEquals(person.getOwner().getAddress(), parsedPerson.getOwner().getAddress());
                assertEquals(person.getName(), parsedPerson.getName());
                assertEquals(person.getDescription(), parsedPerson.getDescription());
                assertEquals(person.getItemTypeName(), parsedPerson.getItemTypeName());
                assertEquals(person.getBirthday(), parsedPerson.getBirthday());
                assertEquals(person.getDeathday(), parsedPerson.getDeathday());
                assertEquals(person.getGender(), parsedPerson.getGender());
                assertEquals(person.getRace(), parsedPerson.getRace());
                assertEquals(true, person.getBirthLatitude() == parsedPerson.getBirthLatitude());
                assertEquals(true, person.getBirthLongitude() == parsedPerson.getBirthLongitude());
                assertEquals(person.getSkinColor(), parsedPerson.getSkinColor());
                assertEquals(person.getEyeColor(), parsedPerson.getEyeColor());
                assertEquals(person.getHairColor(), parsedPerson.getHairColor());
                assertEquals(person.getHeight(), parsedPerson.getHeight());


                // PARSE ISSEU PERSON RECORD
                issuePersonTransaction.sign(certifier, Transaction.FOR_NETWORK);
                //issuePersonTransaction.process(dcSet, false);

                //CONVERT TO BYTES
                byte[] rawIssuePersonRecord = issuePersonTransaction.toBytes(Transaction.FOR_NETWORK, true);

                //CHECK DATA LENGTH
                assertEquals(rawIssuePersonRecord.length, issuePersonTransaction.getDataLength(Transaction.FOR_NETWORK, true));

                IssuePersonRecord parsedIssuePersonRecord = null;
                try {
                    //PARSE FROM BYTES
                    parsedIssuePersonRecord = (IssuePersonRecord) TransactionFactory.getInstance().parse(rawIssuePersonRecord, Transaction.FOR_NETWORK);

                } catch (Exception e) {
                    fail("Exception while parsing transaction.  : " + e);
                }

                //CHECK INSTANCE
                assertEquals(true, parsedIssuePersonRecord instanceof IssuePersonRecord);

                //CHECK SIGNATURE
                assertEquals(true, Arrays.equals(issuePersonTransaction.getSignature(), parsedIssuePersonRecord.getSignature()));

                //CHECK ISSUER
                assertEquals(issuePersonTransaction.getCreator().getAddress(), parsedIssuePersonRecord.getCreator().getAddress());

                parsedPerson = (PersonHuman) parsedIssuePersonRecord.getItem();

                //CHECK OWNER
                assertEquals(person.getOwner().getAddress(), parsedPerson.getOwner().getAddress());

                //CHECK NAME
                assertEquals(person.getName(), parsedPerson.getName());

                //CHECK REFERENCE
                //assertEquals(issuePersonTransaction.getReference(), parsedIssuePersonRecord.getReference());

                //CHECK TIMESTAMP
                assertEquals(issuePersonTransaction.getTimestamp(), parsedIssuePersonRecord.getTimestamp());

                //CHECK DESCRIPTION
                assertEquals(person.getDescription(), parsedPerson.getDescription());

                assertEquals(person.getItemTypeName(), parsedPerson.getItemTypeName());
                assertEquals(person.getBirthday(), parsedPerson.getBirthday());
                assertEquals(person.getGender(), parsedPerson.getGender());
                assertEquals(person.getRace(), parsedPerson.getRace());
                assertEquals(true, person.getBirthLatitude() == parsedPerson.getBirthLatitude());
                assertEquals(true, person.getBirthLongitude() == parsedPerson.getBirthLongitude());
                assertEquals(person.getSkinColor(), parsedPerson.getSkinColor());
                assertEquals(person.getEyeColor(), parsedPerson.getEyeColor());
                assertEquals(person.getHairColor(), parsedPerson.getHairColor());
                assertEquals(person.getHeight(), parsedPerson.getHeight());

                //PARSE TRANSACTION FROM WRONG BYTES
                rawIssuePersonRecord = new byte[issuePersonTransaction.getDataLength(Transaction.FOR_NETWORK, true)];

                try {
                    //PARSE FROM BYTES
                    TransactionFactory.getInstance().parse(rawIssuePersonRecord, Transaction.FOR_NETWORK);

                    //FAIL
                    fail("this should throw an exception");
                } catch (Exception e) {
                    //EXCEPTION IS THROWN OK
                }
            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void processIssuePersonRecord() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                issuePersonTransaction.setDC(dcSet);

                assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(Transaction.FOR_NETWORK, flags));
                issuePersonTransaction.setDC(dcSet, Transaction.FOR_NETWORK, 3, seqNo++);
                issuePersonTransaction.sign(certifier, Transaction.FOR_NETWORK);

                issuePersonTransaction.process(gb, Transaction.FOR_NETWORK);

                LOGGER.info("person KEY: " + person.getKey(dcSet));

                BigDecimal eraUSE = new BigDecimal("1000");
                if (BlockChain.ERA_COMPU_ALL_UP) {
                    eraUSE = eraUSE.add(certifier.addDEVAmount(ERM_KEY));
                }

                //CHECK BALANCE ISSUER
                assertEquals(eraUSE.setScale(certifier.getBalanceUSE(ERM_KEY, dcSet).scale()), certifier.getBalanceUSE(ERM_KEY, dcSet));

                BigDecimal compuUSE = new BigDecimal("1");
                if (BlockChain.ERA_COMPU_ALL_UP) {
                    compuUSE = compuUSE.add(certifier.addDEVAmount(FEE_KEY));
                }
                assertEquals(compuUSE.subtract(issuePersonTransaction.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        certifier.getBalanceUSE(FEE_KEY, dcSet));

                //CHECK PERSON EXISTS DB AS CONFIRMED:  key > -1
                long key = dcSet.getIssuePersonMap().get(issuePersonTransaction);
                assertEquals(true, key >= 0);
                assertEquals(true, dcSet.getItemPersonMap().contains(key));

                //CHECK PERSON IS CORRECT
                assertEquals(true, Arrays.equals(dcSet.getItemPersonMap().get(key).toBytes(true, false), person.toBytes(true, false)));

                //CHECK REFERENCE SENDER
                assertEquals(issuePersonTransaction.getTimestamp(), new Long(certifier.getLastTimestamp(dcSet)[0]));

                //////// ORPHAN /////////
                issuePersonTransaction.orphan(gb, Transaction.FOR_NETWORK);

                //CHECK BALANCE ISSUER
                assertEquals(eraUSE.setScale(certifier.getBalanceUSE(ERM_KEY, dcSet).scale()), certifier.getBalanceUSE(ERM_KEY, dcSet));
                assertEquals(compuUSE.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), certifier.getBalanceUSE(FEE_KEY, dcSet));

                //CHECK PERSON EXISTS ISSUER
                assertEquals(false, dcSet.getItemPersonMap().contains(personKey));

                //CHECK REFERENCE ISSUER
                //assertEquals(issuePersonTransaction.getReference(), certifier.getLastReference(dcSet));
            } finally {
                dcSet.close();
            }
        }
    }


    ///////////////////////////////////////
    // PERSONONALIZE RECORD
    ///////////////////////////////////////
    @Test
    public void validatePersonalizeRecord() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                initPersonalize();

                assertEquals(Transaction.VALIDATE_OK, r_SertifyPubKeys.isValid(Transaction.FOR_NETWORK, flags));

                //r_SertifyPerson.sign(maker, false);
                //r_SertifyPerson.process(dcSet, false);

                //CREATE INVALID PERSONALIZE RECORD NOT ENOUGH ERM BALANCE
                RSertifyPubKeys personalizeRecord_0 = new RSertifyPubKeys(0, userAccount1, FEE_POWER, personKey,
                        sertifiedPublicKeys,
                        356, timestamp, userAccount1.getLastTimestamp(dcSet)[0]);
                assertEquals(Transaction.CREATOR_NOT_PERSONALIZED, personalizeRecord_0.isValid(Transaction.FOR_NETWORK, flags));

                //CREATE INVALID PERSONALIZE RECORD KEY NOT EXIST
                personalizeRecord_0 = new RSertifyPubKeys(0, certifier, FEE_POWER, personKey + 10,
                        sertifiedPublicKeys,
                        356, timestamp, certifier.getLastTimestamp(dcSet)[0]);
                assertEquals(Transaction.ITEM_PERSON_NOT_EXIST, personalizeRecord_0.isValid(Transaction.FOR_NETWORK, flags));

                //CREATE INVALID ISSUE PERSON FOR INVALID PERSONALIZE
                personalizeRecord_0 = new RSertifyPubKeys(0, userAccount1, FEE_POWER, personKey,
                        sertifiedPublicKeys,
                        356, timestamp, userAccount1.getLastTimestamp(dcSet)[0]);
                //CREATE INVALID ISSUE PERSON - NOT FEE
                personalizeRecord_0.setDC(dcSet, Transaction.FOR_NETWORK, 3, seqNo++);
                assertEquals(Transaction.NOT_ENOUGH_FEE, personalizeRecord_0.isValid(Transaction.FOR_NETWORK, flags));
                // ADD FEE
                userAccount1.changeBalance(dcSet, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
                //assertEquals(Transaction.NOT_ENOUGH_RIGHTS, personalizeRecord_0.isValid(dcSet, Transaction.FOR_NETWORK));
                assertEquals(Transaction.CREATOR_NOT_PERSONALIZED, personalizeRecord_0.isValid(Transaction.FOR_NETWORK, flags));
                // ADD RIGHTS
                userAccount1.changeBalance(dcSet, false, ERM_KEY, BigDecimal.valueOf(10000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
                assertEquals(Transaction.CREATOR_NOT_PERSONALIZED, personalizeRecord_0.isValid(Transaction.FOR_NETWORK, flags));

                List<PublicKeyAccount> sertifiedPublicKeys011 = new ArrayList<PublicKeyAccount>();
                sertifiedPublicKeys011.add(new PublicKeyAccount(new byte[60]));
                sertifiedPublicKeys011.add(new PublicKeyAccount(userAccount2.getPublicKey()));
                sertifiedPublicKeys011.add(new PublicKeyAccount(userAccount3.getPublicKey()));
                personalizeRecord_0 = new RSertifyPubKeys(0, certifier, FEE_POWER, personKey,
                        sertifiedPublicKeys011,
                        356, timestamp, certifier.getLastTimestamp(dcSet)[0]);
                assertEquals(Transaction.INVALID_PUBLIC_KEY, personalizeRecord_0.isValid(Transaction.FOR_NETWORK, flags));


            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void validateSignaturePersonalizeRecord() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // SIGN only by certifier
                version = 0;
                initPersonalize();

                r_SertifyPubKeys.sign(certifier, Transaction.FOR_NETWORK);
                // TRUE
                assertEquals(true, r_SertifyPubKeys.isSignatureValid(dcSet));

                version = 1;
                r_SertifyPubKeys = new RSertifyPubKeys(version, certifier, FEE_POWER, personKey,
                        sertifiedPublicKeys,
                        timestamp, certifier.getLastTimestamp(dcSet)[0]);

                r_SertifyPubKeys.sign(certifier, Transaction.FOR_NETWORK);
                // + sign by user
                r_SertifyPubKeys.signUserAccounts(sertifiedPrivateKeys);
                // true !
                //CHECK IF PERSONALIZE RECORD SIGNATURE IS VALID
                assertEquals(true, r_SertifyPubKeys.isSignatureValid(dcSet));

                //INVALID SIGNATURE
                r_SertifyPubKeys.setTimestamp(r_SertifyPubKeys.getTimestamp() + 1);

                //CHECK IF PERSONALIZE RECORD SIGNATURE IS INVALID
                assertEquals(false, r_SertifyPubKeys.isSignatureValid(dcSet));

                // BACK TO VALID
                r_SertifyPubKeys.setTimestamp(r_SertifyPubKeys.getTimestamp() - 1);
                assertEquals(true, r_SertifyPubKeys.isSignatureValid(dcSet));

                r_SertifyPubKeys.signature = new byte[64];
                //CHECK IF PERSONALIZE RECORD SIGNATURE IS INVALID
                assertEquals(false, r_SertifyPubKeys.isSignatureValid(dcSet));

                // BACK TO VALID
                r_SertifyPubKeys.sign(certifier, Transaction.FOR_NETWORK);
                assertEquals(true, r_SertifyPubKeys.isSignatureValid(dcSet));


            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void parsePersonalizeRecord() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                version = 1;
                initPersonalize();

                // SIGN
                r_SertifyPubKeys.signUserAccounts(sertifiedPrivateKeys);
                r_SertifyPubKeys.sign(certifier, Transaction.FOR_NETWORK);

                //CONVERT TO BYTES
                byte[] rawPersonTransfer = r_SertifyPubKeys.toBytes(Transaction.FOR_NETWORK, true);

                //CHECK DATALENGTH
                assertEquals(rawPersonTransfer.length, r_SertifyPubKeys.getDataLength(Transaction.FOR_NETWORK, true));

                try {
                    //PARSE FROM BYTES
                    RSertifyPubKeys parsedPersonTransfer = (RSertifyPubKeys) TransactionFactory.getInstance().parse(rawPersonTransfer, Transaction.FOR_NETWORK);

                    //CHECK INSTANCE
                    assertEquals(true, parsedPersonTransfer instanceof RSertifyPubKeys);

                    //CHECK TYPEBYTES
                    assertEquals(true, Arrays.equals(r_SertifyPubKeys.getTypeBytes(), parsedPersonTransfer.getTypeBytes()));

                    //CHECK TIMESTAMP
                    assertEquals(r_SertifyPubKeys.getTimestamp(), parsedPersonTransfer.getTimestamp());

                    //CHECK REFERENCE
                    //assertEquals(r_SertifyPubKeys.getReference(), parsedPersonTransfer.getReference());

                    //CHECK CREATOR
                    assertEquals(r_SertifyPubKeys.getCreator().getAddress(), parsedPersonTransfer.getCreator().getAddress());

                    //CHECK FEE POWER
                    assertEquals(r_SertifyPubKeys.getFeePow(), parsedPersonTransfer.getFeePow());

                    //CHECK SIGNATURE
                    assertEquals(true, Arrays.equals(r_SertifyPubKeys.getSignature(), parsedPersonTransfer.getSignature()));

                    //CHECK KEY
                    assertEquals(r_SertifyPubKeys.getKey(), parsedPersonTransfer.getKey());

                    //CHECK AMOUNT
                    assertEquals(r_SertifyPubKeys.getAmount(certifier), parsedPersonTransfer.getAmount(certifier));

                    //CHECK USER SIGNATURES
                    assertEquals(true, Arrays.equals(r_SertifyPubKeys.getSertifiedPublicKeys().get(2).getPublicKey(),
                            parsedPersonTransfer.getSertifiedPublicKeys().get(2).getPublicKey()));

                } catch (Exception e) {
                    fail("Exception while parsing transaction." + e);
                }

                //PARSE TRANSACTION FROM WRONG BYTES
                rawPersonTransfer = new byte[r_SertifyPubKeys.getDataLength(Transaction.FOR_NETWORK, true)];

                try {
                    //PARSE FROM BYTES
                    TransactionFactory.getInstance().parse(rawPersonTransfer, Transaction.FOR_NETWORK);

                    //FAIL
                    fail("this should throw an exception");
                } catch (Exception e) {
                    //EXCEPTION IS THROWN OK
                }
            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void process_orphan_PersonalizeRecord() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                //assertEquals( null, dbPS.getItem(personKey, ALIVE_KEY));

                assertEquals(false, userAccount1.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(false, userAccount2.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(false, userAccount3.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));

                initPersonalize();

                // .a - personKey, .b - end_date, .c - block height, .d - reference
                // PERSON STATUS ALIVE
                // exist assertEquals( null, dbPS.getItem(personKey));
                // exist assertEquals( new TreeMap<String, Stack<Tuple3<Integer, Integer, byte[]>>>(), dbPA.getItems(personKey));

                // .a - personKey, .b - end_date, .c - block height, .d - reference
                // PERSON STATUS ALIVE
                //assertEquals( genesisPersonKey + 1, personKey);
                //assertEquals( null, dbPS.getItem(personKey, ALIVE_KEY));

                // ADDRESSES
                assertEquals(null, dbAP.getItem(userAddress1));
                // PERSON -> ADDRESS
                assertEquals(null, dbPA.getItem(personKey, userAddress1));

                assertEquals(null, dbAP.getItem(userAddress2));
                // PERSON -> ADDRESS
                assertEquals(null, dbPA.getItem(personKey, userAddress2));

                assertEquals(null, dbAP.getItem(userAddress3));
                // PERSON -> ADDRESS
                assertEquals(null, dbPA.getItem(personKey, userAddress3));

                BigDecimal oil_amount_diff = BigDecimal.valueOf(BlockChain.GIFTED_COMPU_AMOUNT, BlockChain.FEE_SCALE);

                BigDecimal erm_amount = certifier.getBalanceUSE(ERM_KEY, dcSet);
                BigDecimal oil_amount = certifier.getBalanceUSE(FEE_KEY, dcSet);

                BigDecimal erm_amount_user = userAccount1.getBalanceUSE(ERM_KEY, dcSet);
                BigDecimal oil_amount_user = userAccount1.getBalanceUSE(FEE_KEY, dcSet);

                //// PROCESS /////
                r_SertifyPubKeys.signUserAccounts(sertifiedPrivateKeys);
                r_SertifyPubKeys.setDC(dcSet);
                r_SertifyPubKeys.sign(certifier, Transaction.FOR_NETWORK);
                r_SertifyPubKeys.process(gb, Transaction.FOR_NETWORK);
                int transactionIndex = gb.getTransactionSeq(r_SertifyPubKeys.getSignature());

                //CHECK BALANCE SENDER
                assertEquals(erm_amount, certifier.getBalanceUSE(ERM_KEY, dcSet));
                // CHECK FEE BALANCE - FEE - GIFT
                assertEquals(oil_amount.subtract(oil_amount_diff).subtract(r_SertifyPubKeys.getFee()),
                        certifier.getBalanceUSE(FEE_KEY, dcSet));

                //CHECK BALANCE RECIPIENT
                assertEquals(BG_ZERO, userAccount1.getBalanceUSE(ERM_KEY, dcSet));
                assertEquals(BlockChain.GIFTED_COMPU_AMOUNT, userAccount1.getBalanceUSE(FEE_KEY, dcSet));
                assertEquals(BG_ZERO, userAccount2.getBalanceUSE(ERM_KEY, dcSet));
                assertEquals(BG_ZERO, userAccount2.getBalanceUSE(FEE_KEY, dcSet));
                assertEquals(BG_ZERO, userAccount3.getBalanceUSE(ERM_KEY, dcSet));
                assertEquals(BG_ZERO, userAccount3.getBalanceUSE(FEE_KEY, dcSet));

                //CHECK REFERENCE SENDER
                assertEquals(r_SertifyPubKeys.getTimestamp(), certifier.getLastTimestamp(dcSet));

                //CHECK REFERENCE RECIPIENT
                // TRUE - new reference for first send FEE
                assertEquals(r_SertifyPubKeys.getTimestamp(), userAccount1.getLastTimestamp(dcSet));
                // byte[0]
                assertEquals(null, userAccount2.getLastTimestamp(dcSet));
                assertEquals(null, userAccount3.getLastTimestamp(dcSet));

                ////////// TO DATE ////////
                // .a - personKey, .b - end_date, .c - block height, .d - reference
                int to_date = RSertifyPubKeys.DEFAULT_DURATION + (int) (r_SertifyPubKeys.getTimestamp() / 86400000.0);

                // PERSON STATUS ALIVE - beg_date = person birthDay
                //assertEquals( (long)person.getBirthday(), (long)dbPS.getItem(personKey, ALIVE_KEY).a);
                // PERSON STATUS ALIVE - to_date = 0 - permanent alive
                //assertEquals( (long)Long.MAX_VALUE, (long)dbPS.getItem(personKey, ALIVE_KEY).b);
                //assertEquals( true, Arrays.equals(dbPS.getItem(personKey, ALIVE_KEY).c, r_SertifyPubKeys.getSignature()));
                //assertEquals( (int)dbPS.getItem(personKey, ALIVE_KEY).d, transactionIndex);

                // ADDRESSES
                assertEquals((long) personKey, (long) dbAP.getItem(userAddress1).a);
                assertEquals(to_date, (int) dbAP.getItem(userAddress1).b);
                assertEquals(1, (int) dbAP.getItem(userAddress1).c);
//		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress1).d, r_SertifyPubKeys.getSignature()));
                // PERSON -> ADDRESS
                assertEquals(to_date, (int) dbPA.getItem(personKey, userAddress1).a);
                assertEquals(1, (int) dbPA.getItem(personKey, userAddress1).b);
//		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress1).c, r_SertifyPubKeys.getSignature()));

                assertEquals((long) personKey, (long) dbAP.getItem(userAddress2).a);
                assertEquals(to_date, (int) dbAP.getItem(userAddress2).b);
                assertEquals(1, (int) dbAP.getItem(userAddress2).c);
//		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress2).d, r_SertifyPubKeys.getSignature()));
                // PERSON -> ADDRESS
                assertEquals(to_date, (int) dbPA.getItem(personKey, userAddress2).a);
                assertEquals(1, (int) dbPA.getItem(personKey, userAddress2).b);
//		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress2).c, r_SertifyPubKeys.getSignature()));

                assertEquals((long) personKey, (long) dbAP.getItem(userAddress3).a);
                assertEquals(to_date, (int) dbAP.getItem(userAddress3).b);
                assertEquals(1, (int) dbAP.getItem(userAddress3).c);
//		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress3).d, r_SertifyPubKeys.getSignature()));
                // PERSON -> ADDRESS
                assertEquals(to_date, (int) dbPA.getItem(personKey, userAddress3).a);
                assertEquals(1, (int) dbPA.getItem(personKey, userAddress3).b);
//		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress3).c, r_SertifyPubKeys.getSignature()));

                assertEquals(true, userAccount1.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(true, userAccount2.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(true, userAccount3.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));

                ////////// ORPHAN //////////////////
                r_SertifyPubKeys.orphan(gb, Transaction.FOR_NETWORK);

                //CHECK BALANCE SENDER
                assertEquals(erm_amount, certifier.getBalanceUSE(ERM_KEY, dcSet));
                assertEquals(oil_amount, certifier.getBalanceUSE(FEE_KEY, dcSet));

                //CHECK BALANCE RECIPIENT
                assertEquals(erm_amount_user, userAccount1.getBalanceUSE(ERM_KEY, dcSet));
                assertEquals(oil_amount_user, userAccount1.getBalanceUSE(FEE_KEY, dcSet));
                assertEquals(BG_ZERO, userAccount2.getBalanceUSE(ERM_KEY, dcSet));
                assertEquals(BG_ZERO, userAccount2.getBalanceUSE(FEE_KEY, dcSet));
                assertEquals(BG_ZERO, userAccount3.getBalanceUSE(ERM_KEY, dcSet));
                assertEquals(BG_ZERO, userAccount3.getBalanceUSE(FEE_KEY, dcSet));

                //CHECK REFERENCE SENDER
                //assertEquals(r_SertifyPubKeys.getReference(), certifier.getLastReference(dcSet));

                //CHECK REFERENCE RECIPIENT
                assertEquals(null, userAccount1.getLastTimestamp(dcSet));
                assertEquals(null, userAccount2.getLastTimestamp(dcSet));
                assertEquals(null, userAccount3.getLastTimestamp(dcSet));

                // .a - personKey, .b - end_date, .c - block height, .d - reference
                // PERSON STATUS ALIVE - must not be modified!
                //assertEquals( (long)person.getBirthday(), (long)dbPS.getItem(personKey, ALIVE_KEY).a);

                // ADDRESSES
                assertEquals(null, dbAP.getItem(userAddress1));
                // PERSON -> ADDRESS
                assertEquals(null, dbPA.getItem(personKey, userAddress1));

                assertEquals(null, dbAP.getItem(userAddress2));
                // PERSON -> ADDRESS
                assertEquals(null, dbPA.getItem(personKey, userAddress2));

                assertEquals(null, dbAP.getItem(userAddress3));
                // PERSON -> ADDRESS
                assertEquals(null, dbPA.getItem(personKey, userAddress3));

                assertEquals(false, userAccount1.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(false, userAccount2.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(false, userAccount3.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));

                /////////////////////////////////////////////// TEST DURATIONS
                // TRY DURATIONS
                int end_date = 222;
                r_SertifyPubKeys = new RSertifyPubKeys(0, certifier, FEE_POWER, personKey,
                        sertifiedPublicKeys,
                        end_date, timestamp, certifier.getLastTimestamp(dcSet)[0]);
                r_SertifyPubKeys.signUserAccounts(sertifiedPrivateKeys);
                r_SertifyPubKeys.sign(certifier, Transaction.FOR_NETWORK);
                r_SertifyPubKeys.process(gb, Transaction.FOR_NETWORK);

                int abs_end_date = end_date + (int) (r_SertifyPubKeys.getTimestamp() / 86400000.0);

                // PERSON STATUS ALIVE - date_begin
                //assertEquals( (long)person.getBirthday(), (long)dbPS.getItem(personKey, ALIVE_KEY).a);

                assertEquals(abs_end_date, (int) userAccount1.getPersonDuration(dcSet).b);
                assertEquals(true, userAccount2.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));

                // TEST LIST and STACK
                int end_date2 = -12;
                r_SertifyPubKeys = new RSertifyPubKeys(0, certifier, FEE_POWER, personKey,
                        sertifiedPublicKeys,
                        end_date2, timestamp, certifier.getLastTimestamp(dcSet)[0]);
                r_SertifyPubKeys.signUserAccounts(sertifiedPrivateKeys);
                r_SertifyPubKeys.sign(certifier, Transaction.FOR_NETWORK);
                r_SertifyPubKeys.process(gb, Transaction.FOR_NETWORK);

                int abs_end_date2 = end_date2 + (int) (r_SertifyPubKeys.getTimestamp() / 86400000.0);

                assertEquals(abs_end_date2, (int) userAccount2.getPersonDuration(dcSet).b);
                assertEquals(false, userAccount2.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));

                r_SertifyPubKeys.orphan(gb, Transaction.FOR_NETWORK);

                assertEquals(abs_end_date, (int) userAccount2.getPersonDuration(dcSet).b);
                assertEquals(true, userAccount2.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));

            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void fork_process_orphan_PersonalizeRecord() {

        // TODO !!!
        // need .process in DB then .process in FORK - then test DB values!!!
        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                //assertEquals( null, dbPS.getItem(personKey, ALIVE_KEY));

                assertEquals(false, userAccount1.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(false, userAccount2.isPerson(dcSet, dcSet.getBlockMap().size()));
                assertEquals(false, userAccount3.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));

                BigDecimal oil_amount_start = certifier.getBalanceUSE(FEE_KEY, dcSet);

                initPersonalize();

                // .a - personKey, .b - end_date, .c - block height, .d - reference
                // PERSON STATUS ALIVE
                // exist assertEquals( null, dbPS.getItem(personKey));
                // exist assertEquals( new TreeMap<String, Stack<Tuple3<Integer, Integer, byte[]>>>(), dbPA.getItems(personKey));

                // .a - personKey, .b - end_date, .c - block height, .d - reference
                // PERSON STATUS ALIVE
                assertEquals(2, personKey);
                //assertEquals( null, dbPS.getItem(personKey, ALIVE_KEY));

                // ADDRESSES
                assertEquals(null, dbAP.getItem(userAddress1));
                // PERSON -> ADDRESS
                assertEquals(null, dbPA.getItem(personKey, userAddress1));

                assertEquals(null, dbAP.getItem(userAddress2));
                // PERSON -> ADDRESS
                assertEquals(null, dbPA.getItem(personKey, userAddress2));

                assertEquals(null, dbAP.getItem(userAddress3));
                // PERSON -> ADDRESS
                assertEquals(null, dbPA.getItem(personKey, userAddress3));

                BigDecimal oil_amount_diff;
                if (false)
                    oil_amount_diff = BigDecimal.valueOf(BlockChain.GIFTED_COMPU_AMOUNT, BlockChain.FEE_SCALE);
                else
                    oil_amount_diff = RSertifyPubKeys.BONUS_FOR_PERSON_4_11.add(RSertifyPubKeys.BONUS_FOR_PERSON_4_11);

                BigDecimal erm_amount = certifier.getBalanceUSE(ERM_KEY, dcSet);
                BigDecimal oil_amount = certifier.getBalanceUSE(FEE_KEY, dcSet);

                BigDecimal erm_amount_user = userAccount1.getBalanceUSE(ERM_KEY, dcSet);
                BigDecimal oil_amount_user = userAccount1.getBalanceUSE(FEE_KEY, dcSet);

                last_ref = certifier.getLastTimestamp(dcSet)[0];

                //// PROCESS /////
                r_SertifyPubKeys.signUserAccounts(sertifiedPrivateKeys);
                r_SertifyPubKeys.sign(certifier, Transaction.FOR_NETWORK);

                DCSet fork = dcSet.fork();

                BigDecimal userAccount2ERM = userAccount2.getBalanceUSE(ERM_KEY, fork);
                BigDecimal userAccount2FEE = userAccount2.getBalanceUSE(FEE_KEY, fork);
                BigDecimal userAccount3ERM = userAccount3.getBalanceUSE(ERM_KEY, fork);
                BigDecimal userAccount3FEE = userAccount3.getBalanceUSE(FEE_KEY, fork);

                //CHECK REFERENCE RECIPIENT
                long[] userAccount1ref = userAccount1.getLastTimestamp(fork);
                long[] userAccount2ref = userAccount2.getLastTimestamp(fork);
                long[] userAccount3ref = userAccount3.getLastTimestamp(fork);

                PersonAddressMap dbPA_fork = fork.getPersonAddressMap();
                AddressPersonMap dbAP_fork = fork.getAddressPersonMap();
                KKPersonStatusMap dbPS_fork = fork.getPersonStatusMap();

                r_SertifyPubKeys.setDC(fork);
                r_SertifyPubKeys.process(gb, Transaction.FOR_NETWORK);
                int transactionIndex = gb.getTransactionSeq(r_SertifyPubKeys.getSignature());

                //CHECK BALANCE SENDER
                assertEquals(erm_amount, certifier.getBalanceUSE(ERM_KEY, dcSet));
                // CHECK FEE BALANCE - FEE - GIFT
                assertEquals(oil_amount, certifier.getBalanceUSE(FEE_KEY, dcSet));

                // IN FORK
                //CHECK BALANCE SENDER
                assertEquals(erm_amount, certifier.getBalanceUSE(ERM_KEY, fork));
                // CHECK FEE BALANCE - FEE - GIFT
                // тут мы заверителю возвращаем его затраты - у него должно сать столько же как до внесения персоны
                assertEquals(oil_amount_start, certifier.getBalanceUSE(FEE_KEY, fork));

                //CHECK BALANCE RECIPIENT
                assertEquals(erm_amount_user, userAccount1.getBalanceUSE(ERM_KEY, dcSet));
                // in FORK
                //CHECK BALANCE RECIPIENT
                if (BlockChain.DEVELOP_USE)
                    assertEquals(oil_amount_diff.add(userAccount1.addDEVAmount(FEE_KEY)), userAccount1.getBalanceUSE(FEE_KEY, fork));
                else
                    assertEquals(oil_amount_diff, userAccount1.getBalanceUSE(FEE_KEY, fork));

                //CHECK REFERENCE SENDER
                assertEquals(last_ref, (Long) certifier.getLastTimestamp(dcSet)[0]);
                assertEquals(r_SertifyPubKeys.getTimestamp(), (Long) certifier.getLastTimestamp(fork)[0]);

                //CHECK REFERENCE RECIPIENT
                // TRUE - new reference for first send FEE
                assertEquals(null, userAccount1.getLastTimestamp(dcSet));
                assertEquals(r_SertifyPubKeys.getTimestamp(), (Long) userAccount1.getLastTimestamp(fork)[0]);

                ////////// TO DATE ////////
                // .a - personKey, .b - end_date, .c - block height, .d - reference
                int to_date = RSertifyPubKeys.DEFAULT_DURATION + (int) (r_SertifyPubKeys.getTimestamp() / 86400000.0);

                // PERSON STATUS ALIVE - beg_date = person birthDay
                //assertEquals( null, dbPS.getItem(personKey, ALIVE_KEY));
                //assertEquals( (long)person.getBirthday(), (long)dbPS_fork.getItem(personKey, ALIVE_KEY).a);
                // PERSON STATUS ALIVE - to_date = 0 - permanent alive
                //assertEquals( (long)Long.MAX_VALUE, (long)dbPS_fork.getItem(personKey, ALIVE_KEY).b);
                //assertEquals( true, Arrays.equals(dbPS.getItem(personKey, ALIVE_KEY).c, r_SertifyPubKeys.getSignature()));
                //assertEquals( (int)dbPS_fork.getItem(personKey, ALIVE_KEY).d, transactionIndex);

                // ADDRESSES
                assertEquals(null, dbAP.getItem(userAddress1));
                Tuple4<Long, Integer, Integer, Integer> item_AP = dbAP_fork.getItem(userAddress1);
                assertEquals((long) personKey, (long) item_AP.a);
                assertEquals(to_date, (int) item_AP.b);
                assertEquals(1, (int) item_AP.c);
//		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress1).d, r_SertifyPubKeys.getSignature()));
                // PERSON -> ADDRESS
                assertEquals(null, dbPA.getItem(personKey, userAddress1));
                assertEquals(to_date, (int) dbPA_fork.getItem(personKey, userAddress1).a);
                assertEquals(1, (int) dbPA_fork.getItem(personKey, userAddress1).b);
//		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress1).c, r_SertifyPubKeys.getSignature()));

                assertEquals(null, dbAP.getItem(userAddress2));
                assertEquals((long) personKey, (long) dbAP_fork.getItem(userAddress2).a);
                assertEquals(to_date, (int) dbAP_fork.getItem(userAddress2).b);
                //assertEquals( 1, (int)dbAP_fork.getItem(userAddress2).c);
//		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress2).d, r_SertifyPubKeys.getSignature()));

                // PERSON -> ADDRESS
                assertEquals(null, dbPA.getItem(personKey, userAddress2));
                assertEquals(to_date, (int) dbPA_fork.getItem(personKey, userAddress2).a);
                //assertEquals( 1, (int)dbPA_fork.getItem(personKey, userAddress2).b);
//		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress2).c, r_SertifyPubKeys.getSignature()));

                assertEquals(null, dbAP.getItem(userAddress3));
                assertEquals((long) personKey, (long) dbAP_fork.getItem(userAddress3).a);
                assertEquals(to_date, (int) dbAP_fork.getItem(userAddress3).b);
                //assertEquals( 1, (int)dbAP_fork.getItem(userAddress3).c);
//		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress3).d, r_SertifyPubKeys.getSignature()));

                // PERSON -> ADDRESS
                assertEquals(null, dbPA.getItem(personKey, userAddress3));
                assertEquals(to_date, (int) dbPA_fork.getItem(personKey, userAddress3).a);
                //assertEquals( 1, (int)dbPA_fork.getItem(personKey, userAddress3).b);
//		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress3).c, r_SertifyPubKeys.getSignature()));

                assertEquals(false, userAccount1.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(null, userAccount1.getPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(false, userAccount2.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(false, userAccount3.isPerson(dcSet, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));

                assertEquals(true, userAccount1.isPerson(fork, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertNotEquals(null, userAccount1.getPerson(fork, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(true, userAccount2.isPerson(fork, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(true, userAccount3.isPerson(fork, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));

                ////////// ORPHAN //////////////////
                r_SertifyPubKeys.orphan(gb, Transaction.FOR_NETWORK);

                //CHECK BALANCE SENDER
                assertEquals(erm_amount, certifier.getBalanceUSE(ERM_KEY, fork));
                assertEquals(oil_amount, certifier.getBalanceUSE(FEE_KEY, fork));

                //CHECK BALANCE RECIPIENT
                assertEquals(erm_amount_user, userAccount1.getBalanceUSE(ERM_KEY, fork));
                assertEquals(oil_amount_user.setScale(userAccount1.getBalanceUSE(FEE_KEY, fork).scale()), userAccount1.getBalanceUSE(FEE_KEY, fork));
                assertEquals(userAccount2ERM, userAccount2.getBalanceUSE(ERM_KEY, fork));
                assertEquals(userAccount2FEE, userAccount2.getBalanceUSE(FEE_KEY, fork));
                assertEquals(userAccount3ERM, userAccount3.getBalanceUSE(ERM_KEY, fork));
                assertEquals(userAccount3FEE, userAccount3.getBalanceUSE(FEE_KEY, fork));

                //CHECK REFERENCE SENDER
                //assertEquals(r_SertifyPubKeys.getReference(), certifier.getLastReference(fork));

                //CHECK REFERENCE RECIPIENT
                // дело втом что для того чтобы был известен публичный ключ в ситеме по счету - при Процессинге его закатываем
                // а при откате - игнорируем удаление - но тогда там может спутаться при откате у других
                assertNotEquals(userAccount1ref, userAccount1.getLastTimestamp(fork));
                assertNotEquals(userAccount2ref, userAccount2.getLastTimestamp(fork));
                assertNotEquals(userAccount3ref, userAccount3.getLastTimestamp(fork));

                // .a - personKey, .b - end_date, .c - block height, .d - reference
                // PERSON STATUS ALIVE - must not be modified!
                //assertEquals( (long)person.getBirthday(), (long)dbPS_fork.getItem(personKey, ALIVE_KEY).a);

                // ADDRESSES
                assertEquals(null, dbAP_fork.getItem(userAddress1));
                // PERSON -> ADDRESS
                assertEquals(null, dbPA_fork.getItem(personKey, userAddress1));

                assertEquals(null, dbAP_fork.getItem(userAddress2));
                // PERSON -> ADDRESS
                assertEquals(null, dbPA_fork.getItem(personKey, userAddress2));

                assertEquals(null, dbAP_fork.getItem(userAddress3));
                // PERSON -> ADDRESS
                assertEquals(null, dbPA_fork.getItem(personKey, userAddress3));

                assertEquals(false, userAccount1.isPerson(fork, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(false, userAccount2.isPerson(fork, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
                assertEquals(false, userAccount3.isPerson(fork, dcSet.getBlockSignsMap().get(dcSet.getBlockMap().getLastBlockSignature())));
            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void validatePersonHumanRecord() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                long birthDay = timestamp - 12345678;

                person = new PersonHuman(certifier, "Ermolaev Dmitrii Sergeevich", birthDay, birthDay - 2,
                        (byte) 0, "Slav", (float) 28.12345, (float) 133.7777,
                        "white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей", ownerSignature);

                GenesisIssuePersonRecord genesis_issue_person = new GenesisIssuePersonRecord(personGeneral);
                genesis_issue_person.setDC(dcSet, Transaction.FOR_NETWORK, 3, seqNo++);
                genesis_issue_person.process(gb, Transaction.FOR_NETWORK);
                //genesisPersonKey = dcSet.getIssuePersonMap().size();
                genesisPersonKey = genesis_issue_person.getAssetKey(dcSet);

            } finally {
                dcSet.close();
            }
        }
    }
}
