package org.erachain.core.transaction;

import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestRecVouch {

    static Logger LOGGER = LoggerFactory.getLogger(TestRecVouch.class.getName());

    //Long releaserReference = null;

    long ERM_KEY = AssetCls.ERA_KEY;
    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();

    long flags = 0l;

    int height = 1;
    int seq = 3;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("tes213sdffsdft".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;

    // INIT ASSETS
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
        maker.changeBalance(db, false, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false, false);
        maker.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false, false);

    }


    //ISSUE ASSET TRANSACTION

    @Test
    public void validateSignature_R_Vouch() {

        init();

        //CREATE VOUCH RECORD
        Transaction vouchRecord = new RVouch(maker, FEE_POWER, height, seq, timestamp, maker.getLastTimestamp(db)[0]);
        vouchRecord.sign(maker, Transaction.FOR_NETWORK);

        //CHECK IF TRANSACTION IS VALID
        assertEquals(true, vouchRecord.isSignatureValid(db));

        //INVALID SIGNATURE
        vouchRecord = new RVouch(maker, FEE_POWER, height, seq, timestamp, maker.getLastTimestamp(db)[0], new byte[64]);

        //CHECK IF VOUCH IS INVALID
        assertEquals(false, vouchRecord.isSignatureValid(db));
    }

    @Ignore
    //TODO actualize the test
    @Test
    public void validate_R_Vouch() {

        init();

        //CREATE VOUCH RECORD
        Transaction vouchRecord = new RVouch(maker, FEE_POWER, height, seq, timestamp, maker.getLastTimestamp(db)[0]);
        assertEquals(Transaction.VALIDATE_OK, vouchRecord.isValid(Transaction.FOR_NETWORK, flags));

        vouchRecord = new RVouch(maker, FEE_POWER, -1, seq, timestamp, maker.getLastTimestamp(db)[0], new byte[64]);
        assertEquals(Transaction.INVALID_BLOCK_HEIGHT, vouchRecord.isValid(Transaction.FOR_NETWORK, flags));

        // SET <2 in isValid()
        vouchRecord = new RVouch(maker, FEE_POWER, 1, -1, timestamp, maker.getLastTimestamp(db)[0], new byte[64]);
        assertEquals(Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR, vouchRecord.isValid(Transaction.FOR_NETWORK, flags));

        vouchRecord = new RVouch(maker, FEE_POWER, 99, 1, timestamp, maker.getLastTimestamp(db)[0], new byte[64]);
        assertEquals(Transaction.INVALID_BLOCK_HEIGHT, vouchRecord.isValid(Transaction.FOR_NETWORK, flags));

        vouchRecord = new RVouch(maker, FEE_POWER, 1, 88, timestamp, maker.getLastTimestamp(db)[0], new byte[64]);
        assertEquals(Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR, vouchRecord.isValid(Transaction.FOR_NETWORK, flags));
    }


    @Test
    public void parseR_Vouch() {

        init();

        //CREATE ISSUE ASSET TRANSACTION
        RVouch vouchRecord = new RVouch(maker, FEE_POWER, height, seq, timestamp, maker.getLastTimestamp(db)[0]);
        vouchRecord.sign(maker, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawR_Vouch = vouchRecord.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawR_Vouch.length, vouchRecord.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            RVouch parsedR_Vouch = (RVouch) TransactionFactory.getInstance().parse(rawR_Vouch, Transaction.FOR_NETWORK);

            //CHECK INSTANCE
            assertEquals(true, parsedR_Vouch instanceof RVouch);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(vouchRecord.getSignature(), parsedR_Vouch.getSignature()));

            //CHECK ISSUER
            assertEquals(vouchRecord.getCreator().getAddress(), parsedR_Vouch.getCreator().getAddress());

            //CHECK HEIGHT
            assertEquals(vouchRecord.getRefHeight(), parsedR_Vouch.getRefHeight());

            //CHECK SEQno
            assertEquals(vouchRecord.getRefSeqNo(), parsedR_Vouch.getRefSeqNo());

            //CHECK FEE
            assertEquals(vouchRecord.getFee(), parsedR_Vouch.getFee());

            //CHECK REFERENCE
            //assertEquals((long)vouchRecord.getReference(), (long)parsedR_Vouch.getReference());

            //CHECK TIMESTAMP
            assertEquals(vouchRecord.getTimestamp(), parsedR_Vouch.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction.");
        }

        //PARSE TRANSACTION FROM WRONG BYTES
        rawR_Vouch = new byte[vouchRecord.getDataLength(Transaction.FOR_NETWORK, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawR_Vouch, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }

@Ignore
    @Test
    public void processR_Vouch() {

        init();

        RVouch vouchRecord = new RVouch(maker, FEE_POWER, height, seq, timestamp, maker.getLastTimestamp(db)[0]);
        //vouchRecord.sign(maker, false);

        //assertEquals(Transaction.VALIDATE_OK, vouchRecord.isValid(db, releaserReference));

		/*
		Block block = new Block(1, gb.getReference(), gb.getTimestamp() + 1000,
				BlockGenerator.getNextBlockGeneratingBalance(db, gb), maker,
				//BlockGenerator.calculateSignature(db, gb, maker)
				new byte[]{}
				);

		block.addTransaction(vouchRecord);
		block.process(db);
		 */
        vouchRecord.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
    vouchRecord.process(gb, Transaction.FOR_NETWORK);

        Tuple2<Integer, Integer> ggg = new Tuple2<Integer, Integer>(height, seq);

        Tuple2<BigDecimal, List<Long>> value = db.getVouchRecordMap().get(Transaction.makeDBRef(height, seq));
		/*
		assertEquals(new Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>(
				new BigDecimal(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				new ArrayList<Tuple2<Integer, Integer>>(
						//new Tuple2<Integer, Integer>(height, seq)
						//ggg
						)), value);

		 */

    vouchRecord.orphan(gb, Transaction.FOR_NETWORK);

        value = db.getVouchRecordMap().get(Transaction.makeDBRef(height, seq));
        assertEquals(value, new Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>(
                BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                new ArrayList<Tuple2<Integer, Integer>>(
                )));

        //CHECK REFERENCE SENDER
        //assertEquals(vouchRecord.getReference(), maker.getLastReference(db));
    }

}
