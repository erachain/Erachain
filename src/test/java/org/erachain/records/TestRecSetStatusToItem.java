package org.erachain.records;

import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.core.transaction.IssuePersonRecord;
import org.erachain.core.transaction.RSetStatusToItem;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.junit.Test;
import org.mapdb.Fun.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

//import java.math.BigInteger;
//import java.util.ArrayList;
//import java.util.List;

public class TestRecSetStatusToItem {

    static Logger LOGGER = LoggerFactory.getLogger(TestRecSetStatusToItem.class.getName());

    Long releaserReference = null;

    boolean asPack = false;
    long ERM_KEY = AssetCls.ERA_KEY;
    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] statusReference = new byte[64];
    long timestamp = NTP.getTime();
    long status_key = 1l;
    Long to_date = null;
    long personkey;

    long flags = 0l;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    int mapSize;
    PersonCls personGeneral;
    PersonCls person;
    IssuePersonRecord issuePersonTransaction;
    RSetStatusToItem setStatusTransaction;
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    private byte[] ownerSignature = new byte[Crypto.SIGNATURE_LENGTH];
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;

    // INIT STATUSS
    private void init() {

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
        maker.changeBalance(db, false, false, ERM_KEY, BigDecimal.valueOf(10000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        maker.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        //statusMap = db.getItemStatusMap();
        //mapSize = statusMap.size();

        long birthDay = timestamp - 12345678;
        person = new PersonHuman(maker, "Ermolaev1 Dmitrii Sergeevich", birthDay, birthDay - 1,
                (byte) 1, "Slav", (float) 128.12345, (float) 33.7777,
                "white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей", ownerSignature);

        //CREATE ISSUE PERSON TRANSACTION
        issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0], null);
        issuePersonTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issuePersonTransaction.process(gb, Transaction.FOR_NETWORK);
        person = (PersonCls) issuePersonTransaction.getItem();
        personkey = person.getKey(db);

        timestamp += 100;
        setStatusTransaction = new RSetStatusToItem(maker, FEE_POWER, status_key,
                person.getItemType(), person.getKey(db),
                to_date, birthDay + 1000,
                45646533, 987978972,
                "teasdsdst TEST".getBytes(StandardCharsets.UTF_8),
                "teasdskkj kjh kj EST".getBytes(StandardCharsets.UTF_8),
                0l,
                "DESCRIPTION".getBytes(StandardCharsets.UTF_8),
                timestamp, maker.getLastTimestamp(db)[0]);
        timestamp += 100;

    }

    //SET STATUS TRANSACTION

    @Test
    public void validateSignatureSetStatusTransaction() {

        init();


        //CREATE SET STATUS TRANSACTION
        setStatusTransaction.sign(maker, Transaction.FOR_NETWORK);

        //CHECK IF ISSUE STATUS TRANSACTION IS VALID
        assertEquals(true, setStatusTransaction.isSignatureValid(db));

        //INVALID SIGNATURE
        setStatusTransaction = new RSetStatusToItem(maker, FEE_POWER, status_key,
                person.getItemType(), person.getKey(db), to_date, null,
                323234, 2342342, null, "test TEST 11".getBytes(StandardCharsets.UTF_8), 0l, null,
                timestamp, maker.getLastTimestamp(db)[0], new byte[64]);

        //CHECK IF ISSUE STATUS IS INVALID
        assertEquals(false, setStatusTransaction.isSignatureValid(db));
    }


    @Test
    public void parseSetStatusTransaction() {

        init();

        setStatusTransaction.sign(maker, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawIssueStatusTransaction = setStatusTransaction.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawIssueStatusTransaction.length, setStatusTransaction.getDataLength(Transaction.FOR_NETWORK, true));

        RSetStatusToItem parsedSetStatusTransaction = null;
        try {
            //PARSE FROM BYTES
            parsedSetStatusTransaction = (RSetStatusToItem) TransactionFactory.getInstance().parse(rawIssueStatusTransaction, Transaction.FOR_NETWORK);
            LOGGER.info("parsedSetStatusTransaction: " + parsedSetStatusTransaction);

        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

        //CHECK LEN
        assertEquals(parsedSetStatusTransaction.getDataLength(Transaction.FOR_NETWORK, true),
                setStatusTransaction.getDataLength(Transaction.FOR_NETWORK, true));

        //CHECK INSTANCE
        assertEquals(true, parsedSetStatusTransaction instanceof RSetStatusToItem);

        //CHECK SIGNATURE
        assertEquals(true, Arrays.equals(setStatusTransaction.getSignature(), parsedSetStatusTransaction.getSignature()));

        //CHECK STATUS KEY
        assertEquals(setStatusTransaction.getKey(), parsedSetStatusTransaction.getKey());

        //CHECK TO DATE
        assertEquals(setStatusTransaction.getEndDate(), parsedSetStatusTransaction.getEndDate());

        //CHECK ISSUER
        assertEquals(setStatusTransaction.getCreator().getAddress(), parsedSetStatusTransaction.getCreator().getAddress());

        ItemCls item = ItemCls.getItem(db, setStatusTransaction.getItemType(), setStatusTransaction.getItemKey());
        ItemCls itemParsed = ItemCls.getItem(db, parsedSetStatusTransaction.getItemType(), parsedSetStatusTransaction.getItemKey());
        //CHECK NAME
        assertEquals(item.getName(), itemParsed.getName());

        //CHECK OWNER
        assertEquals(item.getMaker().getAddress(), itemParsed.getMaker().getAddress());

        //CHECK DESCRIPTION
        assertEquals(item.getDescription(), itemParsed.getDescription());

        //CHECK FEE
        assertEquals(setStatusTransaction.getFee(), parsedSetStatusTransaction.getFee());

        //CHECK REFERENCE
        //assertEquals(setStatusTransaction.getReference(), parsedSetStatusTransaction.getReference());

        //CHECK TIMESTAMP
        assertEquals(setStatusTransaction.getTimestamp(), parsedSetStatusTransaction.getTimestamp());

        assertEquals(setStatusTransaction.getRefParent(), parsedSetStatusTransaction.getRefParent());

    }


    @Test
    public void process_orphanSetStatusTransaction() {

        init();
        setStatusTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(Transaction.CREATOR_NOT_PERSONALIZED, setStatusTransaction.isValid(Transaction.FOR_NETWORK, flags));
        assertEquals(db.getPersonStatusMap().get(person.getKey(db)).size(), 0);

        Tuple5<Long, Long, byte[], Integer, Integer> statusDuration = db.getPersonStatusMap().getItem(personkey, status_key);
        // TEST TIME and EXPIRE TIME for ALIVE person
        assertEquals(null, statusDuration);


        setStatusTransaction.sign(maker, Transaction.FOR_NETWORK);
        setStatusTransaction.process(gb, Transaction.FOR_NETWORK);

        statusDuration = db.getPersonStatusMap().getItem(personkey, status_key);
        // TEST TIME and EXPIRE TIME for ALIVE person
        Long endDate = statusDuration.a;
        //days *= (long)86400;
        assertEquals((long) endDate, Long.MIN_VALUE);

        to_date = timestamp + 1234L * 84600000L;
        RSetStatusToItem setStatusTransaction_2 = new RSetStatusToItem(maker, FEE_POWER, status_key,
                person.getItemType(), person.getKey(db), to_date, null,
                234354, 546567,
                "wersdfsdfsdftest TEST".getBytes(StandardCharsets.UTF_8),
                "test TEST".getBytes(StandardCharsets.UTF_8),
                0l,
                "tasasdasdasfsdfsfdsdfest TEST".getBytes(StandardCharsets.UTF_8),
                timestamp + 10, maker.getLastTimestamp(db)[0]);
        setStatusTransaction_2.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        setStatusTransaction_2.sign(maker, Transaction.FOR_NETWORK);
        setStatusTransaction_2.process(gb, Transaction.FOR_NETWORK);

        statusDuration = db.getPersonStatusMap().getItem(personkey, status_key);
        endDate = statusDuration.a;
        assertEquals(endDate, to_date);


        ////// ORPHAN 2 ///////
        setStatusTransaction_2.orphan(gb, Transaction.FOR_NETWORK);

        statusDuration = db.getPersonStatusMap().getItem(personkey, status_key);
        endDate = statusDuration.a;
        assertEquals((long) endDate, Long.MIN_VALUE);

        //CHECK REFERENCE SENDER
        assertEquals((long)setStatusTransaction.getTimestamp(), maker.getLastTimestamp(db)[0]);

        ////// ORPHAN ///////
        setStatusTransaction.orphan(gb, Transaction.FOR_NETWORK);

        statusDuration = db.getPersonStatusMap().getItem(personkey, status_key);
        assertEquals(statusDuration, null);

        //CHECK REFERENCE SENDER
        //assertEquals(setStatusTransaction.getReference(), maker.getLastReference(db));
    }

    // TODO - in statement - valid on key = 999
}
