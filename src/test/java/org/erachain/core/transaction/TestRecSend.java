package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.payment.Payment;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class TestRecSend {

    static Logger LOGGER = LoggerFactory.getLogger(TestRecSend.class.getName());

    //Long Transaction.FOR_NETWORK = null;
    long assetKeyTest = 1011;
    long ERA_KEY = AssetCls.ERA_KEY;
    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();
    long dbRef = 0L;

    ExLink exLink = null;

    byte[] itemAppData = null;
    long txFlags = 0l;


    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
    BigDecimal amount = BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
    String head = "headdd";
    byte[] data = "test123!".getBytes();
    byte[] isText = new byte[]{1};
    byte[] encrypted = new byte[]{0};
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;
    Block block;
    private long seqNo =0;

    // INIT ASSETS
    private void init() {

        System.setProperty("qwe","qw");

        db = DCSet.createEmptyDatabaseSet(0);
        Controller.getInstance().setDCSet(db);
        gb = new GenesisBlock();
        block = gb;

        try {
            gb.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // FEE FUND
        maker.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        maker.changeBalance(db, false, false, ERA_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        maker.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

    }

    @Test
    public void scaleTest() {

        init();
        
        Integer bbb = 31;
        assertEquals("11111", Integer.toBinaryString(bbb));
        assertEquals("10000000", Integer.toBinaryString(128));
        assertEquals((byte)128, (byte)-128);

        byte noData = (byte) 128;
        //assertEquals((byte)-1, (byte)128);
        assertEquals((byte) 128, (byte) -128);
        //assertEquals(org.erachain.core.transaction.RSend.NO_DATA_MASK));

        BigDecimal amountTest = new BigDecimal("123456781234567812345678");
        BigDecimal amountForParse = new BigDecimal("1234567812345678");
        BigDecimal amountBase;
        BigDecimal amount;
        BigDecimal amount_result;

        AssetCls asset23 = new AssetVenture(itemAppData, maker, "AAA", icon, image, ".", 0, TransactionAmount.maxSCALE, 0L);
        asset23.insertToMap(db, BlockChain.AMOUNT_SCALE_FROM);
        long assetKey23 = asset23.getKey();

        //int shift = 64;
        int scale;
        int different_scale;
        int fromScale = TransactionAmount.SCALE_MASK_HALF + BlockChain.AMOUNT_DEDAULT_SCALE - 1;
        int toScale = BlockChain.AMOUNT_DEDAULT_SCALE - TransactionAmount.SCALE_MASK_HALF;
        assertEquals("11111".equals(Integer.toBinaryString(fromScale - toScale)), true);

        RSend r_Send;
        byte[] raw_r_Send;
        RSend r_Send_2;
        for (scale = fromScale; scale >= toScale; scale--) {

            amount = amountTest.scaleByPowerOfTen(-scale);

            // TO BASE
            different_scale = scale - BlockChain.AMOUNT_DEDAULT_SCALE;
            
            if (different_scale != 0) {
                // to DEFAUTL base 8 decimals
                amountBase = amount.scaleByPowerOfTen(different_scale);
                if (different_scale < 0)
                    different_scale += TransactionAmount.SCALE_MASK + 1;
                
            } else {
                amountBase = amount;
            }

            assertEquals(8, amountBase.scale());

            // CHECK ACCURACY of AMOUNT
            int accuracy = different_scale & TransactionAmount.SCALE_MASK;
            String sss = Integer.toBinaryString(accuracy);
            if (scale == 24)
                assertEquals("10000".equals(sss), true);
            else if (scale < 9)
                assertEquals(true, true);

            if (accuracy > 0) {
                if (accuracy >= TransactionAmount.SCALE_MASK_HALF) {
                    accuracy -= TransactionAmount.SCALE_MASK + 1;
                }
                // RESCALE AMOUNT
                amount_result = amountBase.scaleByPowerOfTen(-accuracy);
            } else {
                amount_result = amountBase;
            }
                
            assertEquals(amount, amount_result);
            
            // TRY PARSE - PRICISION must be LESS
            amount = amountForParse.scaleByPowerOfTen(-scale);

            r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey23,
                    amount,
                    "", null, isText, encrypted, timestamp, 123l
            );
            r_Send.sign(maker, Transaction.FOR_NETWORK);
            assertEquals(r_Send.isSignatureValid(db), true);
            //r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1);
            r_Send.setDC(db, Transaction.FOR_NETWORK, this.gb.heightBlock, 2, true);
            assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);
            
            raw_r_Send = r_Send.toBytes(Transaction.FOR_NETWORK, true);

            r_Send_2 = null;
            try {
                r_Send_2 = (RSend) RSend.Parse(raw_r_Send, Transaction.FOR_NETWORK);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                assertEquals(null, true);
            }
            
            // FOR DEBUG POINT
            if (!r_Send.getAmount().equals(r_Send_2.getAmount())) {
                try {
                    r_Send_2 = (RSend) RSend.Parse(raw_r_Send, Transaction.FOR_NETWORK);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
                
            
            assertEquals(r_Send.getAmount(), r_Send_2.getAmount());

            //r_Send_2.sign(maker, false);
            assertEquals(r_Send_2.isSignatureValid(db), true);
            r_Send_2.setDC(db, Transaction.FOR_NETWORK, gb.heightBlock, 1, true);
            assertEquals(r_Send_2.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);

            assertEquals(Arrays.equals(r_Send.getSignature(), r_Send_2.getSignature()), true);
            
            // NAGATIVE AMOUNT
            r_Send = new RSend(maker, exLink, FEE_POWER, recipient, ERA_KEY,
                    amount.negate(),
                    head, data, isText, encrypted, timestamp, 123l
            );
            r_Send.sign(maker, Transaction.FOR_NETWORK);
            
            raw_r_Send = r_Send.toBytes(Transaction.FOR_NETWORK, true);

            r_Send_2 = null;
            try {
                r_Send_2 = (RSend) RSend.Parse(raw_r_Send, Transaction.FOR_NETWORK);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            assertEquals(r_Send.getAmount(), r_Send_2.getAmount());
            
        }
        
        /////////////////////// VALIDATE
        int thisScale = 5;
        AssetCls assetA = new AssetVenture(itemAppData, maker, "AAA", icon, image, ".", 0, thisScale, 0L);
        assetA.insertToMap(db, 0l);
        long assetKey = assetA.getKey();
        head = "";
        data = null;

        // IS VALID
        BigDecimal bal_A_keyA = amountForParse.scaleByPowerOfTen(-thisScale);
        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey,
                bal_A_keyA,
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.sign(maker, Transaction.FOR_NETWORK);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.VALIDATE_OK);

        // INVALID
        bal_A_keyA = amountForParse.scaleByPowerOfTen(-thisScale - 1);
        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey,
                bal_A_keyA,
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.AMOUNT_SCALE_WRONG);

        ///////////////////////
        // INVALID
        BigDecimal amountInvalid = amountTest;
        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey + 1,
                amountInvalid,
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.ITEM_ASSET_NOT_EXIST);

        // INVALID
        assetA = new AssetVenture(itemAppData, maker, "AAA", icon, image, ".", 0, 30, 0L);
        assetA.insertToMap(db, 0l);
        assetKey = assetA.getKey();

        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey,
                amountInvalid,
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.AMOUNT_LENGHT_SO_LONG);

        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey,
                amountInvalid.negate(),
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.AMOUNT_LENGHT_SO_LONG);

        // INVALID
        amountInvalid = amountForParse.scaleByPowerOfTen(-fromScale - 1);
        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey,
                amountInvalid,
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.AMOUNT_SCALE_WRONG);

        amountInvalid = amountForParse.scaleByPowerOfTen(-toScale + 1);
        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey,
                amountInvalid,
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.AMOUNT_SCALE_WRONG);            

    }

    @Test
    public void scaleTestBIG() {

        init();
        
        BigDecimal amountTest = new BigDecimal("12345678123456781234560000000000");
        BigDecimal amountForParse = new BigDecimal("1234560000000000");
        BigDecimal amountBase;
        BigDecimal amount;
        BigDecimal amount_result;


        //int shift = 64;
        int scale;
        int different_scale;
        int fromScale = -5;
        int toScale = BlockChain.AMOUNT_DEDAULT_SCALE - TransactionAmount.SCALE_MASK_HALF;

        AssetCls assetBIG = new AssetVenture(itemAppData, maker, "AAA", icon, image, ".", 0, fromScale + 2, 0L);
        assetBIG.insertToMap(db, BlockChain.AMOUNT_SCALE_FROM);
        long assetKeyBIG = assetBIG.getKey();

        RSend r_Send;
        byte[] raw_r_Send;
        RSend r_Send_2;
        for (scale = fromScale; scale >= toScale; scale--) {

            amount = amountTest.scaleByPowerOfTen(-scale);

            // TO BASE
            different_scale = scale - BlockChain.AMOUNT_DEDAULT_SCALE;
            
            if (different_scale != 0) {
                // to DEFAUTL base 8 decimals
                amountBase = amount.scaleByPowerOfTen(different_scale);
                if (different_scale < 0)
                    different_scale += TransactionAmount.SCALE_MASK + 1;
                
            } else {
                amountBase = amount;
            }

            assertEquals(8, amountBase.scale());

            // CHECK ACCURACY of AMOUNT
            int accuracy = different_scale & TransactionAmount.SCALE_MASK;
            String sss = Integer.toBinaryString(accuracy);
            if (scale == 24)
                assertEquals("10000".equals(sss), true);
            else if (scale < 9)
                assertEquals(true, true);

            if (accuracy > 0) {
                if (accuracy >= TransactionAmount.SCALE_MASK_HALF) {
                    accuracy -= TransactionAmount.SCALE_MASK + 1;
                }
                // RESCALE AMOUNT
                amount_result = amountBase.scaleByPowerOfTen(-accuracy);
            } else {
                amount_result = amountBase;
            }
                
            assertEquals(amount, amount_result);
            
            // TRY PARSE - PRICISION must be LESS
            amount = amountForParse.scaleByPowerOfTen(-scale);

            r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKeyBIG,
                    amount,
                    "", null, isText, encrypted, timestamp, 123l
            );
            r_Send.sign(maker, Transaction.FOR_NETWORK);
            assertEquals(r_Send.isSignatureValid(db), true);
            //r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1);
            r_Send.setDC(db, Transaction.FOR_NETWORK, gb.heightBlock, 1, true);
            assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);
            
            raw_r_Send = r_Send.toBytes(Transaction.FOR_NETWORK, true);

            r_Send_2 = null;
            try {
                r_Send_2 = (RSend) RSend.Parse(raw_r_Send, Transaction.FOR_NETWORK);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                assertEquals(null, true);
            }
            
            // FOR DEBUG POINT
            if (!r_Send.getAmount().equals(r_Send_2.getAmount())) {
                try {
                    r_Send_2 = (RSend) RSend.Parse(raw_r_Send, Transaction.FOR_NETWORK);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
                
            
            assertEquals(r_Send.getAmount(), r_Send_2.getAmount());

            //r_Send_2.sign(maker, false);
            assertEquals(r_Send_2.isSignatureValid(db), true);
            r_Send_2.setDC(db, Transaction.FOR_NETWORK, gb.heightBlock, 1, true);
            assertEquals(r_Send_2.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);

            assertEquals(Arrays.equals(r_Send.getSignature(), r_Send_2.getSignature()), true);
            
            // NAGATIVE AMOUNT
            r_Send = new RSend(maker, exLink, FEE_POWER, recipient, ERA_KEY,
                    amount.negate(),
                    head, data, isText, encrypted, timestamp, 123l
            );
            r_Send.sign(maker, Transaction.FOR_NETWORK);
            
            raw_r_Send = r_Send.toBytes(Transaction.FOR_NETWORK, true);

            r_Send_2 = null;
            try {
                r_Send_2 = (RSend) RSend.Parse(raw_r_Send, Transaction.FOR_NETWORK);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            assertEquals(r_Send.getAmount(), r_Send_2.getAmount());
            
        }
        
        /////////////////////// VALIDATE
        int thisScale = 5;
        AssetCls assetA = new AssetVenture(itemAppData, maker, "AAA", icon, image, ".", 0, thisScale, 0L);
        assetA.insertToMap(db, 0l);
        long assetKey = assetA.getKey();
        head = "";
        data = null;

        // IS VALID
        BigDecimal bal_A_keyA = amountForParse.scaleByPowerOfTen(-thisScale);
        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey,
                bal_A_keyA,
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.sign(maker, Transaction.FOR_NETWORK);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.VALIDATE_OK);

        // VALID because trailing ZERO - amount.stripTrailingZeros()
        bal_A_keyA = amountForParse.scaleByPowerOfTen(-thisScale - 1);
        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey,
                bal_A_keyA,
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.VALIDATE_OK);

        ///////////////////////
        // INVALID
        BigDecimal amountInvalid = amountTest;
        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey + 1,
                amountInvalid,
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.ITEM_ASSET_NOT_EXIST);

        // INVALID
        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey,
                amountInvalid,
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.AMOUNT_LENGHT_SO_LONG);

        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey,
                amountInvalid.negate(),
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.AMOUNT_LENGHT_SO_LONG);

        // INVALID
        amountInvalid = amountForParse.scaleByPowerOfTen(-fromScale - 1);
        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey,
                amountInvalid,
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.VALIDATE_OK);

        amountInvalid = amountForParse.scaleByPowerOfTen(-toScale + 1);
        r_Send = new RSend(maker, exLink, FEE_POWER, recipient, assetKey,
                amountInvalid,
                head, data, isText, encrypted, timestamp, 123l
        );
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, 0l), Transaction.AMOUNT_SCALE_WRONG);            

    }

    @Test
    public void validateMessageTransactionV3() {

        //Integer bbb = -128;
        //assertEquals("1111", Integer.toBinaryString(bbb));

        init();

        /// MESSAGE + AMOUNT
        RSend r_SendV3 = new RSend(
                maker, exLink, FEE_POWER,
                recipient,
                ERA_KEY,
                amount,
                head, data,
                isText,
                encrypted,
                1547048069000l,//timestamp,
                maker.getLastTimestamp(db)[0]
        );
        r_SendV3.sign(maker, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK); //);

        assertEquals((long) maker.getLastTimestamp(db)[0], gb.getTimestamp());
        r_SendV3.process(gb, Transaction.FOR_NETWORK);
        assertEquals((long) maker.getLastTimestamp(db)[0], timestamp);

        //assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(1090).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(ERA_KEY, db));
        assertEquals(BigDecimal.valueOf(1010).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(ERA_KEY, db));

        byte[] rawMessageTransactionV3 = r_SendV3.toBytes(Transaction.FOR_NETWORK, true);
        int dd = r_SendV3.getDataLength(Transaction.FOR_NETWORK, true);
        assertEquals(rawMessageTransactionV3.length, r_SendV3.getDataLength(Transaction.FOR_NETWORK, true));


        RSend messageTransactionV3_2 = null;
        try {
            messageTransactionV3_2 = (RSend) RSend.Parse(rawMessageTransactionV3, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(new String(r_SendV3.getData()), new String(messageTransactionV3_2.getData()));
        assertEquals(r_SendV3.getCreator(), messageTransactionV3_2.getCreator());
        assertEquals(r_SendV3.getRecipient(), messageTransactionV3_2.getRecipient());
        assertEquals(r_SendV3.getKey(), messageTransactionV3_2.getKey());
        assertEquals(r_SendV3.getAmount(), messageTransactionV3_2.getAmount());
        assertEquals(r_SendV3.isEncrypted(), messageTransactionV3_2.isEncrypted());
        assertEquals(r_SendV3.isText(), messageTransactionV3_2.isText());

        assertEquals(r_SendV3.isSignatureValid(db), true);
        assertEquals(messageTransactionV3_2.isSignatureValid(db), true);

        //// MESSAGE ONLY
        r_SendV3.orphan(block, Transaction.FOR_NETWORK);
        assertEquals((long) maker.getLastTimestamp(db)[0], gb.getTimestamp());

        r_SendV3 = new RSend(
                maker, exLink, FEE_POWER,
                recipient,
                ERA_KEY,
                null,
                head, data,
                isText,
                encrypted,
                timestamp, maker.getLastTimestamp(db)[0]
        );
        r_SendV3.sign(maker, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK); //Transaction.VALIDATE_OK);

        r_SendV3.process(gb, Transaction.FOR_NETWORK);
        assertEquals((long) maker.getLastTimestamp(db)[0], timestamp);

        //assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(1100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(ERA_KEY, db));
        assertEquals(BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(ERA_KEY, db));

        rawMessageTransactionV3 = r_SendV3.toBytes(Transaction.FOR_NETWORK, true);
        dd = r_SendV3.getDataLength(Transaction.FOR_NETWORK, true);
        assertEquals(rawMessageTransactionV3.length, r_SendV3.getDataLength(Transaction.FOR_NETWORK, true));


        messageTransactionV3_2 = null;
        try {
            messageTransactionV3_2 = (RSend) RSend.Parse(rawMessageTransactionV3, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(new String(r_SendV3.getData()), new String(messageTransactionV3_2.getData()));
        assertEquals(r_SendV3.getCreator(), messageTransactionV3_2.getCreator());
        assertEquals(r_SendV3.getRecipient(), messageTransactionV3_2.getRecipient());
        assertEquals(0, messageTransactionV3_2.getKey());
        assertEquals(r_SendV3.getAmount(), messageTransactionV3_2.getAmount());
        assertEquals(r_SendV3.isEncrypted(), messageTransactionV3_2.isEncrypted());
        assertEquals(r_SendV3.isText(), messageTransactionV3_2.isText());

        assertEquals(r_SendV3.isSignatureValid(db), true);
        assertEquals(messageTransactionV3_2.isSignatureValid(db), true);


        //// AMOUNT ONLY
        r_SendV3.orphan(block, Transaction.FOR_NETWORK);
        assertEquals((long) maker.getLastTimestamp(db)[0], gb.getTimestamp());

        r_SendV3 = new RSend(
                maker, exLink, FEE_POWER,
                recipient,
                ERA_KEY,
                amount,
                "", null,
                null,
                null,
                timestamp, maker.getLastTimestamp(db)[0]
        );
        r_SendV3.sign(maker, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);

        r_SendV3.process(gb, Transaction.FOR_NETWORK);

        //assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(1090).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(ERA_KEY, db));
        assertEquals(BigDecimal.valueOf(1010).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(ERA_KEY, db));

        rawMessageTransactionV3 = r_SendV3.toBytes(Transaction.FOR_NETWORK, true);
        dd = r_SendV3.getDataLength(Transaction.FOR_NETWORK, true);
        assertEquals(rawMessageTransactionV3.length, r_SendV3.getDataLength(Transaction.FOR_NETWORK, true));


        messageTransactionV3_2 = null;
        try {
            messageTransactionV3_2 = (RSend) RSend.Parse(rawMessageTransactionV3, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(null, r_SendV3.getData());
        assertEquals(null, messageTransactionV3_2.getData());
        assertEquals(r_SendV3.getCreator(), messageTransactionV3_2.getCreator());
        assertEquals(r_SendV3.getRecipient(), messageTransactionV3_2.getRecipient());
        assertEquals(r_SendV3.getKey(), messageTransactionV3_2.getKey());
        assertEquals(r_SendV3.getAmount(), messageTransactionV3_2.getAmount());

        assertEquals(r_SendV3.isSignatureValid(db), true);
        assertEquals(messageTransactionV3_2.isSignatureValid(db), true);

        //// EMPTY - NOT AMOUNT and NOT TEXT
        r_SendV3.orphan(block, Transaction.FOR_NETWORK);

        r_SendV3 = new RSend(
                maker, exLink, FEE_POWER,
                recipient,
                ERA_KEY,
                null,
                null, null,
                null,
                null,
                timestamp, maker.getLastTimestamp(db)[0]
        );
        r_SendV3.sign(maker, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);

        r_SendV3.process(gb, Transaction.FOR_NETWORK);

        //assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(1100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(ERA_KEY, db));
        assertEquals(BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(ERA_KEY, db));

        rawMessageTransactionV3 = r_SendV3.toBytes(Transaction.FOR_NETWORK, true);
        dd = r_SendV3.getDataLength(Transaction.FOR_NETWORK, true);
        assertEquals(rawMessageTransactionV3.length, r_SendV3.getDataLength(Transaction.FOR_NETWORK, true));


        messageTransactionV3_2 = null;
        try {
            messageTransactionV3_2 = (RSend) RSend.Parse(rawMessageTransactionV3, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(null, r_SendV3.getData());
        assertEquals(null, messageTransactionV3_2.getData());
        assertEquals(r_SendV3.getCreator(), messageTransactionV3_2.getCreator());
        assertEquals(r_SendV3.getRecipient(), messageTransactionV3_2.getRecipient());
        assertEquals(0, messageTransactionV3_2.getKey());
        assertEquals(r_SendV3.getAmount(), messageTransactionV3_2.getAmount());

        assertEquals(r_SendV3.isSignatureValid(db), true);
        assertEquals(messageTransactionV3_2.isSignatureValid(db), true);

        // NEGATE for test HOLD ///////////////////
        amount = amount.negate();
        recipient.changeBalance(db, false, false, -ERA_KEY, amount.negate(), false, false, false);
        /// MESSAGE + AMOUNT
        r_SendV3 = new RSend(
                maker, exLink, FEE_POWER,
                recipient,
                -ERA_KEY,
                amount,
                head, data,
                isText,
                encrypted,
                ++timestamp, maker.getLastTimestamp(db)[0]
        );
        r_SendV3.sign(maker, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK); //ransaction.VALIDATE_OK);

        r_SendV3.process(gb, Transaction.FOR_NETWORK);

        //assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(1100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(ERA_KEY, db));
        assertEquals(BigDecimal.valueOf(1010).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(ERA_KEY, db));

        dd = r_SendV3.getDataLength(Transaction.FOR_NETWORK, true);
        assertEquals(rawMessageTransactionV3.length, r_SendV3.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            messageTransactionV3_2 = (RSend) RSend.Parse(rawMessageTransactionV3, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(new String(r_SendV3.getData()), new String(messageTransactionV3_2.getData()));
        assertEquals(r_SendV3.getCreator(), messageTransactionV3_2.getCreator());
        assertEquals(r_SendV3.getRecipient(), messageTransactionV3_2.getRecipient());
        assertEquals(r_SendV3.getKey(), messageTransactionV3_2.getKey());
        assertEquals(r_SendV3.getAmount(), messageTransactionV3_2.getAmount());
        assertEquals(r_SendV3.isEncrypted(), messageTransactionV3_2.isEncrypted());
        assertEquals(r_SendV3.isText(), messageTransactionV3_2.isText());

        assertEquals(r_SendV3.isSignatureValid(db), true);
        assertEquals(messageTransactionV3_2.isSignatureValid(db), true);

    }


    @Test
    public void validateArbitraryTransactionV3() {

        init();

        //ADD ERM ASSET
        AssetCls aTFundingAsset = new AssetVenture(itemAppData, new GenesisBlock().getCreator(), "ATFunding", icon, image, "This asset represents the funding of AT team for the integration of a Turing complete virtual machine into ERM.",
                0, 8, 250000000l);
        aTFundingAsset.setReference(assetReference, dbRef);
        db.getItemAssetMap().set(assetKeyTest, aTFundingAsset);

        GenesisBlock genesisBlock = gb; //new GenesisBlock();
		/*
		try {
			genesisBlock.process(db);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/

        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();


        Account recipient1 = new Account("79MXwfzHPDGWoQUgyPXRf2fxKuzY1osNsg");
        Account recipient2 = new Account("76abzpJK61F4TAZFkqev2EY5duHVUvycZX");
        Account recipient3 = new Account("7JU8UTuREAJG2yht5ASn7o1Ur34P1nvTk5");

        long timestamp = NTP.getTime();

        //PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS

        maker.changeBalance(db, false, false, assetKeyTest, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        List<Payment> payments = new ArrayList<Payment>();
        payments.add(new Payment(recipient1, assetKeyTest, BigDecimal.valueOf(110).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));
        payments.add(new Payment(recipient2, assetKeyTest, BigDecimal.valueOf(120).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));
        payments.add(new Payment(recipient3, assetKeyTest, BigDecimal.valueOf(201).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));

        ArbitraryTransactionV3 arbitraryTransactionV3 = new ArbitraryTransactionV3(
                maker, payments, 111,
                data,
                FEE_POWER,
                ++timestamp, 0l
        );
        arbitraryTransactionV3.sign(maker, Transaction.FOR_NETWORK);
        arbitraryTransactionV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        //if (NTP.getTime() < Transaction.getARBITRARY_TRANSACTIONS_RELEASE() || arbitraryTransactionV3.getTimestamp() < Transaction.getPOWFIX_RELEASE())
        if (false) {
            assertEquals(arbitraryTransactionV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.NOT_YET_RELEASED);
        } else {
            assertEquals(arbitraryTransactionV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);
        }

        arbitraryTransactionV3.process(gb, Transaction.FOR_NETWORK);

        //assertEquals(BigDecimal.valueOf(1).subtract(arbitraryTransactionV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(1000 - 110 - 120 - 201).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(assetKeyTest, db));
        assertEquals(BigDecimal.valueOf(110).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient1.getBalanceUSE(assetKeyTest, db));
        assertEquals(BigDecimal.valueOf(120).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient2.getBalanceUSE(assetKeyTest, db));
        assertEquals(BigDecimal.valueOf(201).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient3.getBalanceUSE(assetKeyTest, db));

        byte[] rawArbitraryTransactionV3 = arbitraryTransactionV3.toBytes(Transaction.FOR_NETWORK, true);

        ArbitraryTransactionV3 arbitraryTransactionV3_2 = null;
        try {
            arbitraryTransactionV3_2 = (ArbitraryTransactionV3) ArbitraryTransactionV3.Parse(Arrays.copyOfRange(rawArbitraryTransactionV3, 0, rawArbitraryTransactionV3.length));
            // already SIGNED - arbitraryTransactionV3_2.sign(creator);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(new String(arbitraryTransactionV3.getData()), new String(arbitraryTransactionV3_2.getData()));
        assertEquals(arbitraryTransactionV3.getPayments().get(0).toJson().toJSONString(),
                arbitraryTransactionV3_2.getPayments().get(0).toJson().toJSONString());
        assertEquals(arbitraryTransactionV3.getPayments().get(1).toJson().toJSONString(),
                arbitraryTransactionV3_2.getPayments().get(1).toJson().toJSONString());
        assertEquals(arbitraryTransactionV3.getPayments().get(2).toJson().toJSONString(),
                arbitraryTransactionV3_2.getPayments().get(2).toJson().toJSONString());
        assertEquals(arbitraryTransactionV3.getPayments().size(), arbitraryTransactionV3.getPayments().size());

        assertEquals(arbitraryTransactionV3.getService(), arbitraryTransactionV3_2.getService());
        assertEquals(arbitraryTransactionV3.getCreator(), arbitraryTransactionV3_2.getCreator());

        assertEquals(arbitraryTransactionV3.isSignatureValid(db), true);
        assertEquals(arbitraryTransactionV3_2.isSignatureValid(db), true);
    }

    @Test
    public void validateArbitraryTransactionV3withoutPayments() {

        init();

        AssetCls aTFundingAsset = new AssetVenture(itemAppData, gb.getCreator(), "ATFunding", icon, image, "This asset represents the funding of AT team for the integration of a Turing complete virtual machine into ERM.",
                0, 8, 250000000l);
        aTFundingAsset.setReference(gb.getSignature(), dbRef);
        db.getItemAssetMap().set(assetKeyTest, aTFundingAsset);

        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();

        byte[] data = "test123!".getBytes();

        long timestamp = NTP.getTime();

        //PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS

        maker.changeBalance(db, false, false, assetKeyTest, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        List<Payment> payments = new ArrayList<Payment>();

        ArbitraryTransactionV3 arbitraryTransactionV3 = new ArbitraryTransactionV3(
                maker, payments, 111,
                data,
                FEE_POWER,
                timestamp, maker.getLastTimestamp(db)[0]
        );
        arbitraryTransactionV3.sign(maker, Transaction.FOR_NETWORK);
        arbitraryTransactionV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        //if (NTP.getTime() < Transaction.getARBITRARY_TRANSACTIONS_RELEASE() || arbitraryTransactionV3.getTimestamp() < Transaction.getPOWFIX_RELEASE())
        if (false) {
            assertEquals(arbitraryTransactionV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.NOT_YET_RELEASED);
        } else {
            assertEquals(arbitraryTransactionV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);
        }

        arbitraryTransactionV3.process(gb, Transaction.FOR_NETWORK);

        //assertEquals(BigDecimal.valueOf(1).subtract(arbitraryTransactionV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(assetKeyTest, db));


        byte[] rawArbitraryTransactionV3 = arbitraryTransactionV3.toBytes(Transaction.FOR_NETWORK, true);

        ArbitraryTransactionV3 arbitraryTransactionV3_2 = null;
        try {
            arbitraryTransactionV3_2 = (ArbitraryTransactionV3) ArbitraryTransactionV3.Parse(rawArbitraryTransactionV3);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(new String(arbitraryTransactionV3.getData()), new String(arbitraryTransactionV3_2.getData()));

        assertEquals(arbitraryTransactionV3.getPayments().size(), arbitraryTransactionV3.getPayments().size());

        assertEquals(arbitraryTransactionV3.getService(), arbitraryTransactionV3_2.getService());
        assertEquals(arbitraryTransactionV3.getCreator(), arbitraryTransactionV3_2.getCreator());

        assertEquals(arbitraryTransactionV3.isSignatureValid(db), true);
        assertEquals(arbitraryTransactionV3_2.isSignatureValid(db), true);
    }

    @Test
    public void makeMessageTransactionV3_DISCREDIR_ADDRESSES() {

        // HPftF6gmSH3mn9dKSAwSEoaxW2Lb6SVoguhKyHXbyjr7 -
        PublicKeyAccount maker = new PublicKeyAccount(BlockChain.DISCREDIR_ADDRESSES[0]);
        Account recipient = new Account("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ");
        BigDecimal amount = BigDecimal.valueOf(49800).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

        long era_key = 1l;
        /// DISCREDIR_ADDRESSES
        RSend r_Send = new RSend(maker, exLink, FEE_POWER, recipient, era_key, amount, "", null, isText, encrypted, timestamp, 1l);

        byte[] data = r_Send.toBytes(Transaction.FOR_NETWORK, true);
        int port = BlockChain.NETWORK_PORT;
        data = Bytes.concat(data, Ints.toByteArray(port));
        byte[] digest = Crypto.getInstance().digest(data);
        digest = Bytes.concat(digest, digest);

        RSend r_SendSigned = new RSend(maker, FEE_POWER, recipient, era_key, amount, "", null, isText, encrypted, timestamp, 1l, digest);
        String raw = Base58.encode(r_SendSigned.toBytes(Transaction.FOR_NETWORK, true));
        System.out.print(raw);

        //DCSet dcSet = DCSet.getInstance();
        //assertEquals(r_SendSigned.isSignatureValid(dcSet), true);
        //r_SendSigned.setDC(dcSet, false);
        //assertEquals(r_SendSigned.isValid(dcSet, null), Transaction.VALIDATE_OK);
        //Controller.getInstance().broadcastTransaction(r_SendSigned);

    }

    @Test
    public void parsCalculatedTransaction() {


        /// MESSAGE + AMOUNT
        RCalculated txCalculated = new RCalculated(recipient, ERA_KEY, amount,
                head, timestamp,
                seqNo);

        byte[] rawCalculated = txCalculated.toBytes(Transaction.FOR_NETWORK, true);
        int length = txCalculated.getDataLength(Transaction.FOR_NETWORK, true);
        assertEquals(rawCalculated.length, length);


        RCalculated txCalculated_2 = null;
        try {
            txCalculated_2 = (RCalculated) RCalculated.Parse(rawCalculated);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(txCalculated.getRecipient(), txCalculated_2.getRecipient());
        assertEquals(txCalculated.getKey(), txCalculated_2.getKey());
        assertEquals(txCalculated.getAmount(), txCalculated_2.getAmount());
        assertEquals(txCalculated.getMessage(), txCalculated_2.getMessage());

        txCalculated = new RCalculated(recipient, ERA_KEY, null,
                head, timestamp,
                seqNo);

        rawCalculated = txCalculated.toBytes(Transaction.FOR_NETWORK, true);
        length = txCalculated.getDataLength(Transaction.FOR_NETWORK, true);
        assertEquals(rawCalculated.length, length);


        txCalculated_2 = null;
        try {
            txCalculated_2 = (RCalculated) RCalculated.Parse(rawCalculated);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(txCalculated.getRecipient(), txCalculated_2.getRecipient());
        assertEquals(txCalculated.getKey(), txCalculated_2.getKey());
        assertEquals(txCalculated.getMessage(), txCalculated_2.getMessage());


    }

}
