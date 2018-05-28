package test.records;

import core.BlockChain;
import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.transaction.R_Hashes;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import datachain.DCSet;
import datachain.HashesSignsMap;
import ntp.NTP;
import org.junit.Test;
import org.mapdb.Fun.Tuple3;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

//import java.math.BigInteger;
//import java.util.ArrayList;

public class TestRecHash {

    Long releaserReference = null;

    boolean asPack = false;
    long FEE_KEY = AssetCls.FEE_KEY;
    long VOTE_KEY = AssetCls.ERA_KEY;
    byte FEE_POWER = (byte) 1;
    long timestamp = NTP.getTime();

    long flags = 0l;

    byte[] url = "http://sdsdf.com/dfgr/1".getBytes();
    byte[] data = "test123!".getBytes();

    byte[][] hashes = new byte[12][32];
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    R_Hashes hashesRecord;
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;

    // INIT
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
        maker.setLastTimestamp(gb.getTimestamp(db), db);
        maker.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);

    }

    //

    @Test
    public void validateSignature() {

        init();

        hashesRecord = new R_Hashes(maker, FEE_POWER, url, data, hashes, timestamp + 10, maker.getLastTimestamp(db));
        hashesRecord.sign(maker, asPack);

        //CHECK IF ISSUE PLATE TRANSACTION IS VALID
        assertEquals(true, hashesRecord.isSignatureValid(db));

        //INVALID SIGNATURE
        hashesRecord = new R_Hashes(maker, FEE_POWER, url, data, hashes, timestamp + 10, maker.getLastTimestamp(db), new byte[64]);

        //CHECK IF ISSUE PLATE IS INVALID
        assertEquals(false, hashesRecord.isSignatureValid(db));
    }


    @Test
    public void parse() {

        init();


        hashesRecord = new R_Hashes(maker, FEE_POWER, url, data, hashes, timestamp + 10, maker.getLastTimestamp(db));
        hashesRecord.sign(maker, asPack);

        //CONVERT TO BYTES
        byte[] rawHashesRecord = hashesRecord.toBytes(true, null);

        //CHECK DATA LENGTH
        assertEquals(rawHashesRecord.length, hashesRecord.getDataLength(false));

        R_Hashes parsed = null;
        try {
            //PARSE FROM BYTES
            parsed = (R_Hashes) TransactionFactory.getInstance().parse(rawHashesRecord, releaserReference);
        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

        //CHECK INSTANCE
        assertEquals(true, parsed instanceof R_Hashes);

        //CHECK TIMESTAMP
        assertEquals(hashesRecord.getTimestamp(), parsed.getTimestamp());

        //CHECK REFERENCE
        //assertEquals(hashesRecord.getReference(), parsed.getReference());

        //CHECK ISSUER
        assertEquals(hashesRecord.getCreator().getAddress(), parsed.getCreator().getAddress());

        //CHECK FEE
        assertEquals(hashesRecord.getFeePow(), parsed.getFeePow());

        //CHECK SIGNATURE
        assertEquals(true, Arrays.equals(hashesRecord.getSignature(), parsed.getSignature()));


        /////////////////////////////////
        //CHECK URL
        assertEquals(true, Arrays.equals(hashesRecord.getURL(), parsed.getURL()));

        //CHECK NAME
        assertEquals(true, Arrays.equals(hashesRecord.getData(), parsed.getData()));

        //CHECK HASHES
        assertEquals(true, Arrays.equals(hashesRecord.getHashesB58(), parsed.getHashesB58()));

    }


    @Test
    public void process() {

        init();

        byte[] hash0 = maker.getPublicKey();
        hashes[0] = hash0;

        hashesRecord = new R_Hashes(maker, FEE_POWER, url, data, hashes, timestamp + 10, maker.getLastTimestamp(db));

        assertEquals(Transaction.VALIDATE_OK, hashesRecord.isValid(releaserReference, flags));

        hashesRecord.sign(maker, false);
        hashesRecord.process(gb, false);

        //CHECK REFERENCE SENDER
        assertEquals(hashesRecord.getTimestamp(), maker.getLastTimestamp(db));

        HashesSignsMap map = db.getHashesSignsMap();
        Stack<Tuple3<Long, Integer, Integer>> result = map.get(hash0);
        assertEquals(result.size(), 1);

        ///// ORPHAN
        hashesRecord.orphan(false);

        //CHECK REFERENCE SENDER
        //assertEquals(hashesRecord.getReference(), maker.getLastReference(db));

        result = map.get(hash0);
        assertEquals(result.size(), 0);

    }


}
