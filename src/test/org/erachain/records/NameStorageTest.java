package org.erachain.records;

import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.naming.Name;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.ArbitraryTransactionV3;
import org.erachain.core.transaction.RegisterNameTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.erachain.utils.Corekeys;
import org.erachain.utils.Pair;
import org.erachain.utils.StorageUtils;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

//import org.erachain.core.transaction.GenesisTransaction;

@SuppressWarnings("unchecked")
@Ignore
public class NameStorageTest {

    Long releaserReference = null;
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();
    long flags = 0l;
    private DCSet databaseSet;
    private PrivateKeyAccount sender;

    @Before
    public void setup() {
        //Ed25519.load();

        databaseSet = DCSet.createEmptyDatabaseSet();

        // CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        sender = new PrivateKeyAccount(privateKey);

        // CREATE KNOWN ACCOUNT
        seed = Crypto.getInstance().digest("buyer".getBytes());
        privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        PrivateKeyAccount buyer = new PrivateKeyAccount(privateKey);

        // PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
        //Transaction transaction = new GenesisTransaction(sender, BigDecimal
        //		.valueOf(1000), NTP.getTime());
        //transaction.process(databaseSet, false);
        //sender.setLastReference(genesisBlock.getGeneratorSignature(), databaseSet);
        sender.changeBalance(databaseSet, false, FEE_KEY, BigDecimal.valueOf(1), false);


        // PROCESS GENESIS TRANSACTION TO MAKE SURE BUYER HAS FUNDS
        //transaction = new GenesisTransaction(buyer, BigDecimal.valueOf(1000)
        //		, NTP.getTime());
        //transaction.process(databaseSet, false);
        buyer.changeBalance(databaseSet, false, FEE_KEY, BigDecimal.valueOf(1), false);

        // CREATE SIGNATURE
        long timestamp = NTP.getTime();
        Name name = new Name(sender, "drizzt", "this is the value");

        // CREATE NAME REGISTRATION
        Transaction nameRegistration = new RegisterNameTransaction(null,
                sender, name, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        //nameRegistration.sign(sender);


        // CHECK IF NAME REGISTRATION IS VALID
        assertEquals(Transaction.VALIDATE_OK,
                nameRegistration.isValid(Transaction.FOR_NETWORK, flags));
        nameRegistration.process(null, Transaction.FOR_NETWORK);
    }

    @Test
    public void testNameStorageNotChangedIfNotOwner() throws Exception {
        long timestamp = NTP.getTime();

        // We have nothing in name storage for drizzt here.
        assertNull(databaseSet.getNameStorageMap().getOpt("drizzt",
                Corekeys.PROFILEENABLE.toString()));
        assertNull(databaseSet.getNameStorageMap().get("drizzt"));

        JSONObject storageJsonObject = StorageUtils.getStorageJsonObject(
                Collections.singletonList(new Pair<String, String>(
                        Corekeys.PROFILEENABLE.toString(), "yes")), null, null,
                null, null, null);
        storageJsonObject.put("name", "drizzt");
        byte[] data = storageJsonObject.toString().getBytes();

        // ADDING KEY COMPLETE WITH YES
        ArbitraryTransactionV3 arbitraryTransaction = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);

        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        // KEY IS THERE!
        assertEquals(
                "yes",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILEENABLE.toString()));


        byte[] seed = Crypto.getInstance().digest("test2".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        PrivateKeyAccount badSender = new PrivateKeyAccount(privateKey);


        storageJsonObject = StorageUtils.getStorageJsonObject(
                null, Arrays.asList(
                        Corekeys.PROFILEENABLE.toString()), null,
                null, null, null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        // ADDING KEY COMPLETE WITH YES
        arbitraryTransaction = new ArbitraryTransactionV3(
                null, badSender, null, 10, data, (byte) 0,
                timestamp, badSender.getLastTimestamp(databaseSet));


        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        // KEY IS STILL THERE!
        assertEquals(
                "yes",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILEENABLE.toString()));


    }

    @Test
    public void testAddRemoveComplete() throws Exception {

        long timestamp = NTP.getTime();

        // We have nothing in name storage for drizzt here.
        assertNull(databaseSet.getNameStorageMap().getOpt("drizzt",
                Corekeys.PROFILEENABLE.toString()));
        assertNull(databaseSet.getNameStorageMap().get("drizzt"));

        JSONObject storageJsonObject = StorageUtils.getStorageJsonObject(
                Collections.singletonList(new Pair<String, String>(
                        Corekeys.PROFILEENABLE.toString(), "yes")), null, null,
                null, null, null);
        storageJsonObject.put("name", "drizzt");
        byte[] data = storageJsonObject.toString().getBytes();

        // ADDING KEY COMPLETE WITH YES
        ArbitraryTransaction arbitraryTransaction = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);
        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        // KEY IS THERE!
        assertEquals(
                "yes",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILEENABLE.toString()));

        // CHANGING KEY

        storageJsonObject = StorageUtils.getStorageJsonObject(Collections
                .singletonList(new Pair<String, String>(Corekeys.PROFILEENABLE
                        .toString(), "anothervalue")), null, null, null, null, null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        arbitraryTransaction = new ArbitraryTransactionV3(null, sender, null, 10,
                data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);
        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        // NEW KEY IS THERE!
        assertEquals(
                "anothervalue",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILEENABLE.toString()));

        // REMOVING KEY COMPLETE

        storageJsonObject = StorageUtils.getStorageJsonObject(null,
                Arrays.asList(Corekeys.PROFILEENABLE.toString()), null, null,
                null, null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        arbitraryTransaction = new ArbitraryTransactionV3(null, sender, null, 10,
                data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));

        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);

        assertNull(databaseSet.getNameStorageMap().getOpt("drizzt",
                Corekeys.PROFILEENABLE.toString()));

    }

    @Test
    public void testAddRemoveListKeys() throws Exception {

        long timestamp = NTP.getTime();

        // We have nothing in name storage for drizzt here.
        assertNull(databaseSet.getNameStorageMap().getOpt("drizzt",
                Corekeys.PROFILELIKEPOSTS.toString()));
        assertNull(databaseSet.getNameStorageMap().get("drizzt"));

        JSONObject storageJsonObject = StorageUtils.getStorageJsonObject(null,
                null, Collections.singletonList(new Pair<String, String>(
                        Corekeys.PROFILELIKEPOSTS.toString(), "skerberus")),
                null, null, null);
        storageJsonObject.put("name", "drizzt");
        byte[] data = storageJsonObject.toString().getBytes();

        // ADDING Skerberus as List key
        ArbitraryTransaction arbitraryTransaction = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);
        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        assertEquals(
                "skerberus",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILELIKEPOSTS.toString()));

        storageJsonObject = StorageUtils.getStorageJsonObject(null, null,
                Collections.singletonList(new Pair<String, String>(
                        Corekeys.PROFILELIKEPOSTS.toString(), "vrontis")),
                null, null, null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        // ADDING vrontis as List key
        arbitraryTransaction = new ArbitraryTransactionV3(null, sender, null, 10,
                data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);

        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        // KEY IS THERE!
        assertEquals("skerberus;vrontis", databaseSet.getNameStorageMap()
                .getOpt("drizzt", Corekeys.PROFILELIKEPOSTS.toString()));

        // removing step by step!

        storageJsonObject = StorageUtils.getStorageJsonObject(null, null, null,
                Collections.singletonList(new Pair<String, String>(
                        Corekeys.PROFILELIKEPOSTS.toString(), "skerberus")),
                null, null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        // removing skerberus as List key
        arbitraryTransaction = new ArbitraryTransactionV3(null, sender, null, 10,
                data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);

        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        // KEY IS THERE!
        assertEquals(
                "vrontis",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILELIKEPOSTS.toString()));

        // nothing happens cause not part of the list
        storageJsonObject = StorageUtils
                .getStorageJsonObject(
                        null,
                        null,
                        null,
                        Collections.singletonList(new Pair<String, String>(
                                Corekeys.PROFILELIKEPOSTS.toString(), "haloman")),
                        null, null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        // removing skerberus as List key
        arbitraryTransaction = new ArbitraryTransactionV3(null, sender, null, 10,
                data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);

        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        // KEY IS THERE!
        assertEquals(
                "vrontis",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILELIKEPOSTS.toString()));

        // removing last person
        storageJsonObject = StorageUtils
                .getStorageJsonObject(
                        null,
                        null,
                        null,
                        Collections.singletonList(new Pair<String, String>(
                                Corekeys.PROFILELIKEPOSTS.toString(), "vrontis")),
                        null, null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        // removing skerberus as List key
        arbitraryTransaction = new ArbitraryTransactionV3(null, sender, null, 10,
                data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);
        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        // KEY IS THERE!
        assertNull(databaseSet.getNameStorageMap().getOpt("drizzt",
                Corekeys.PROFILELIKEPOSTS.toString()));

        // adding more than one element!

        storageJsonObject = StorageUtils.getStorageJsonObject(null, null,
                Collections.singletonList(new Pair<String, String>(
                        Corekeys.PROFILELIKEPOSTS.toString(), "a;b;c")), null,
                null, null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        arbitraryTransaction = new ArbitraryTransactionV3(null, sender, null, 10,
                data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);

        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        assertEquals(
                "a;b;c",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILELIKEPOSTS.toString()));

        // removing more than one

        storageJsonObject = StorageUtils.getStorageJsonObject(null, null, null,
                Collections.singletonList(new Pair<String, String>(
                        Corekeys.PROFILELIKEPOSTS.toString(), "a;c;nothing")),
                null, null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        arbitraryTransaction = new ArbitraryTransactionV3(null, sender, null, 10,
                data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);

        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        // KEY IS THERE!
        assertEquals(
                "b",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILELIKEPOSTS.toString()));

    }

    @Test
    public void testAddWithoutSeperatorAndCheckBasicOrphaning()
            throws Exception {
        long timestamp = NTP.getTime();

        JSONObject storageJsonObject = StorageUtils.getStorageJsonObject(null,
                null, null, null, Collections
                        .singletonList(new Pair<String, String>(
                                Corekeys.WEBSITE.toString(), "first")), null);
        storageJsonObject.put("name", "drizzt");
        byte[] data = storageJsonObject.toString().getBytes();

        ArbitraryTransaction arbitraryTransaction = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);

        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        DCSet.getInstance().getTransactionMap().add(arbitraryTransaction);

        assertEquals(
                "first",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.WEBSITE.toString()));

        storageJsonObject = StorageUtils.getStorageJsonObject(null, null, null,
                null, Collections.singletonList(new Pair<String, String>(
                        Corekeys.WEBSITE.toString(), " second")), null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        ArbitraryTransaction arbitraryTransaction2 = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction2.sign(sender, Transaction.FOR_NETWORK);

        arbitraryTransaction2.process(null, Transaction.FOR_NETWORK);

        DCSet.getInstance().getTransactionMap().add(arbitraryTransaction2);

        assertEquals(
                "first second",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.WEBSITE.toString()));

        // ORPHANING FIRST TX!
        arbitraryTransaction.orphan(block, Transaction.FOR_NETWORK);

        assertEquals(
                " second",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.WEBSITE.toString()));

        // ORPHANING second TX!
        arbitraryTransaction2.orphan(block, Transaction.FOR_NETWORK);

        assertNull(databaseSet.getNameStorageMap().getOpt("drizzt",
                Corekeys.WEBSITE.toString()));
    }

    @Test
    public void testComplexOrphanNameStorageTest() throws Exception {
        long timestamp = NTP.getTime();

        String random_linking_example = "randomlinkingExample";
        JSONObject storageJsonObject = StorageUtils.getStorageJsonObject(
                Collections.singletonList(new Pair<String, String>(
                        Corekeys.PROFILEENABLE.toString(), "yes")), null,
                Collections.singletonList(new Pair<String, String>(
                        random_linking_example, "skerberus")), null,
                Collections.singletonList(new Pair<String, String>(
                        Corekeys.WEBSITE.toString(), "first")), null);
        storageJsonObject.put("name", "drizzt");
        byte[] data = storageJsonObject.toString().getBytes();

        ArbitraryTransaction arbitraryTransaction = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);
        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        DCSet.getInstance().getTransactionMap().add(arbitraryTransaction);

        // After first tx
        // Profenable:yes
        // Website: first
        // random : skerberus

        storageJsonObject = StorageUtils.getStorageJsonObject(null, null,
                Collections.singletonList(new Pair<String, String>(
                        random_linking_example, "vrontis")), null, Collections
                        .singletonList(new Pair<String, String>(
                                Corekeys.WEBSITE.toString(), "second")), null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        ArbitraryTransaction arbitraryTransaction2 = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction2.sign(sender, Transaction.FOR_NETWORK);
        arbitraryTransaction2.process(null, Transaction.FOR_NETWORK);

        DCSet.getInstance().getTransactionMap().add(arbitraryTransaction2);

        // After second tx
        // Profenable:yes
        // Website: firstsecond
        // random : skerberus;vrontis

        storageJsonObject = StorageUtils.getStorageJsonObject(null, null,
                Collections.singletonList(new Pair<String, String>("asdf",
                        "asdf")), null, Collections
                        .singletonList(new Pair<String, String>(
                                Corekeys.WEBSITE.toString(), "third")), null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        ArbitraryTransaction arbitraryTransaction3 = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction3.sign(sender, Transaction.FOR_NETWORK);
        arbitraryTransaction3.process(null, Transaction.FOR_NETWORK);

        DCSet.getInstance().getTransactionMap().add(arbitraryTransaction3);

        // After second tx
        // Profenable:yes
        // Website: firstsecondthird
        // random : skerberus;vrontis
        // asdf : asdf

        assertEquals("firstsecondthird", databaseSet.getNameStorageMap()
                .getOpt("drizzt", Corekeys.WEBSITE.toString()));
        assertEquals(
                "yes",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILEENABLE.toString()));
        assertEquals("skerberus;vrontis", databaseSet.getNameStorageMap()
                .getOpt("drizzt", random_linking_example));
        assertEquals("asdf",
                databaseSet.getNameStorageMap().getOpt("drizzt", "asdf"));

        // removing second one -->

        // Profenable:yes
        // Website: firstthird
        // random : skerberus
        // asdf : asdf
        arbitraryTransaction2.orphan(block, Transaction.FOR_NETWORK);

        assertEquals(
                "firstthird",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.WEBSITE.toString()));
        assertEquals(
                "yes",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILEENABLE.toString()));
        assertEquals(
                "skerberus",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        random_linking_example));
        assertEquals("asdf",
                databaseSet.getNameStorageMap().getOpt("drizzt", "asdf"));

    }

    @Test
    public void testComplexOrphaning2() throws Exception {
        long timestamp = NTP.getTime();
        String random_linking_example = "randomlinkingExample";
        JSONObject storageJsonObject = StorageUtils.getStorageJsonObject(
                Collections.singletonList(new Pair<String, String>(
                        Corekeys.PROFILEENABLE.toString(), "yes")), null,
                Collections.singletonList(new Pair<String, String>(
                        random_linking_example, "skerberus")), null,
                Collections.singletonList(new Pair<String, String>(
                        Corekeys.WEBSITE.toString(), "first")), null);
        storageJsonObject.put("name", "drizzt");
        byte[] data = storageJsonObject.toString().getBytes();

        ArbitraryTransaction arbitraryTransaction = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);
        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        DCSet.getInstance().getTransactionMap().add(arbitraryTransaction);

        // After first tx
        // Profenable:yes
        // Website: first
        // random : skerberus

        storageJsonObject = StorageUtils.getStorageJsonObject(null, null,
                Collections.singletonList(new Pair<String, String>(
                        random_linking_example, "vrontis")), null, Collections
                        .singletonList(new Pair<String, String>(
                                Corekeys.WEBSITE.toString(), "second")), null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        ArbitraryTransaction arbitraryTransaction2 = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction2.sign(sender, Transaction.FOR_NETWORK);
        arbitraryTransaction2.process(null, Transaction.FOR_NETWORK);

        DCSet.getInstance().getTransactionMap().add(arbitraryTransaction2);

        // After second tx
        // Profenable:yes
        // Website: firstsecond
        // random : skerberus;vrontis

        storageJsonObject = StorageUtils.getStorageJsonObject(null, null,
                Collections.singletonList(new Pair<String, String>("asdf",
                        "asdf")), null, Collections
                        .singletonList(new Pair<String, String>(
                                Corekeys.WEBSITE.toString(), "third")), null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        ArbitraryTransaction arbitraryTransaction3 = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction3.sign(sender, Transaction.FOR_NETWORK);
        arbitraryTransaction3.process(null, Transaction.FOR_NETWORK);

        DCSet.getInstance().getTransactionMap().add(arbitraryTransaction3);

        // After third tx
        // Profenable:yes
        // Website: firstsecondthird
        // random : skerberus;vrontis
        // asdf : asdf

        assertEquals("firstsecondthird", databaseSet.getNameStorageMap()
                .getOpt("drizzt", Corekeys.WEBSITE.toString()));
        assertEquals(
                "yes",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILEENABLE.toString()));
        assertEquals("skerberus;vrontis", databaseSet.getNameStorageMap()
                .getOpt("drizzt", random_linking_example));
        assertEquals("asdf",
                databaseSet.getNameStorageMap().getOpt("drizzt", "asdf"));

        // removing first one -->

        // Website: secondthird
        // random : vrontis
        // asdf : asdf
        arbitraryTransaction.orphan(block, Transaction.FOR_NETWORK);

        assertEquals(
                "secondthird",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.WEBSITE.toString()));
        assertNull(

                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILEENABLE.toString()));
        assertEquals(
                "vrontis",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        random_linking_example));
        assertEquals("asdf",
                databaseSet.getNameStorageMap().getOpt("drizzt", "asdf"));

        // removing new first
        // Website: third
        // asdf : asdf
        arbitraryTransaction2.orphan(block, Transaction.FOR_NETWORK);

        assertEquals(
                "third",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.WEBSITE.toString()));
        assertNull(

                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILEENABLE.toString()));
        assertNull(databaseSet.getNameStorageMap().getOpt("drizzt",
                random_linking_example));
        assertEquals("asdf",
                databaseSet.getNameStorageMap().getOpt("drizzt", "asdf"));

    }

    @Test
    public void testOrphanComplex3() throws Exception {
        long timestamp = NTP.getTime();
        String random_linking_example = "randomlinkingExample";
        JSONObject storageJsonObject = StorageUtils.getStorageJsonObject(
                Collections.singletonList(new Pair<String, String>(
                        Corekeys.PROFILEENABLE.toString(), "yes")), null,
                Collections.singletonList(new Pair<String, String>(
                        random_linking_example, "skerberus")), null,
                Collections.singletonList(new Pair<String, String>(
                        Corekeys.WEBSITE.toString(), "first")), null);
        storageJsonObject.put("name", "drizzt");
        byte[] data = storageJsonObject.toString().getBytes();

        ArbitraryTransaction arbitraryTransaction = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction.sign(sender, Transaction.FOR_NETWORK);
        arbitraryTransaction.process(null, Transaction.FOR_NETWORK);

        DCSet.getInstance().getTransactionMap().add(arbitraryTransaction);

        // After first tx
        // Profenable:yes
        // Website: first
        // random : skerberus

        storageJsonObject = StorageUtils.getStorageJsonObject(null, null,
                Collections.singletonList(new Pair<String, String>(
                        random_linking_example, "vrontis")), null, Collections
                        .singletonList(new Pair<String, String>(
                                Corekeys.WEBSITE.toString(), "second")), null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        ArbitraryTransaction arbitraryTransaction2 = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction2.sign(sender, Transaction.FOR_NETWORK);
        arbitraryTransaction2.process(null, Transaction.FOR_NETWORK);

        DCSet.getInstance().getTransactionMap().add(arbitraryTransaction2);

        // After second tx
        // Profenable:yes
        // Website: firstsecond
        // random : skerberus;vrontis

        storageJsonObject = StorageUtils.getStorageJsonObject(null, null,
                Collections.singletonList(new Pair<String, String>("asdf",
                        "asdf")), null, Collections
                        .singletonList(new Pair<String, String>(
                                Corekeys.WEBSITE.toString(), "third")), null);
        storageJsonObject.put("name", "drizzt");
        data = storageJsonObject.toString().getBytes();

        ArbitraryTransaction arbitraryTransaction3 = new ArbitraryTransactionV3(
                null, sender, null, 10, data, (byte) 0,
                timestamp, sender.getLastTimestamp(databaseSet));
        arbitraryTransaction3.sign(sender, Transaction.FOR_NETWORK);
        arbitraryTransaction3.process(null, Transaction.FOR_NETWORK);

        DCSet.getInstance().getTransactionMap().add(arbitraryTransaction3);

        // After third tx
        // Profenable:yes
        // Website: firstsecondthird
        // random : skerberus;vrontis
        // asdf : asdf

        assertEquals("firstsecondthird", databaseSet.getNameStorageMap()
                .getOpt("drizzt", Corekeys.WEBSITE.toString()));
        assertEquals(
                "yes",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILEENABLE.toString()));
        assertEquals("skerberus;vrontis", databaseSet.getNameStorageMap()
                .getOpt("drizzt", random_linking_example));
        assertEquals("asdf",
                databaseSet.getNameStorageMap().getOpt("drizzt", "asdf"));

        // --> removing last one
        // Profenable:yes
        // Website: firstsecond
        // random : skerberus;vrontis
        arbitraryTransaction3.orphan(block, Transaction.FOR_NETWORK);

        assertEquals(
                "firstsecond",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.WEBSITE.toString()));
        assertEquals(
                "yes",
                databaseSet.getNameStorageMap().getOpt("drizzt",
                        Corekeys.PROFILEENABLE.toString()));
        assertEquals("skerberus;vrontis", databaseSet.getNameStorageMap()
                .getOpt("drizzt", random_linking_example));
        assertNull(databaseSet.getNameStorageMap().getOpt("drizzt", "asdf"));
    }

}
