package org.erachain.core.transaction;

import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.HashesSignsMap;
import org.erachain.ntp.NTP;
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

    ExLink exLink = null;
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
    RHashes hashesRecord;
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;

    // INIT
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

    //

    @Test
    public void validateSignature() {

        init();

        hashesRecord = new RHashes(maker, exLink, FEE_POWER, url, data, hashes, timestamp + 10, maker.getLastTimestamp(db)[0]);
        hashesRecord.sign(maker, Transaction.FOR_NETWORK);

        //CHECK IF ISSUE PLATE TRANSACTION IS VALID
        hashesRecord.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
        assertEquals(true, hashesRecord.isSignatureValid(db));

        //INVALID SIGNATURE
        hashesRecord = new RHashes(maker, exLink, FEE_POWER, url, data, hashes, timestamp + 10, maker.getLastTimestamp(db)[0], new byte[64]);

        //CHECK IF ISSUE PLATE IS INVALID
        hashesRecord.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
        assertEquals(false, hashesRecord.isSignatureValid(db));
    }


    @Test
    public void parse() {

        init();


        hashesRecord = new RHashes(maker, exLink, FEE_POWER, url, data, hashes, timestamp + 10, maker.getLastTimestamp(db)[0]);
        hashesRecord.sign(maker, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawHashesRecord = hashesRecord.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawHashesRecord.length, hashesRecord.getDataLength(Transaction.FOR_NETWORK, true));

        RHashes parsed = null;
        try {
            //PARSE FROM BYTES
            parsed = (RHashes) TransactionFactory.getInstance().parse(rawHashesRecord, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

        //CHECK INSTANCE
        assertEquals(true, parsed instanceof RHashes);

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

        hashesRecord = new RHashes(maker, exLink, FEE_POWER, null, data, hashes, timestamp + 10, maker.getLastTimestamp(db)[0]);
        hashesRecord.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(Transaction.VALIDATE_OK, hashesRecord.isValid(Transaction.FOR_NETWORK, flags));

        hashesRecord.sign(maker, Transaction.FOR_NETWORK);
        hashesRecord.process(gb, Transaction.FOR_NETWORK);

        //CHECK REFERENCE SENDER
        assertEquals(hashesRecord.getTimestamp(), maker.getLastTimestamp(db));

        HashesSignsMap map = db.getHashesSignsMap();
        Stack<Tuple3<Long, Integer, Integer>> result = map.get(hash0);
        assertEquals(result.size(), 1);

        ///// ORPHAN
        hashesRecord.orphan(gb, Transaction.FOR_NETWORK);

        //CHECK REFERENCE SENDER
        //assertEquals(hashesRecord.getReference(), maker.getLastReference(db));

        result = map.get(hash0);
        assertEquals(result.size(), 0);

    }


}
