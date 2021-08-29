package org.erachain.core.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.epoch.SmartContract;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.wallet.Wallet;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.junit.Test;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;


public class TestRecSendOutsideClaims {

    static Logger LOGGER = LoggerFactory.getLogger(TestRecSendOutsideClaims.class.getName());

    //Long Transaction.FOR_NETWORK = null;

    long ERM_KEY = 3;
    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();
    byte version = 2;
    byte prop2 = 0;

    ExLink exLink = null;
    SmartContract smartContract = null;

    byte prop1_backward = org.erachain.core.transaction.TransactionAmount.BACKWARD_MASK;

    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balanceA;
    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balanceB;

    IssueAssetTransaction issueAssetTransaction;
    AssetCls assetA;
    long keyA;

    byte[] itemAppData = null;
    long txFlags = 0L;

    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    byte[] accountSeed1 = Wallet.generateAccountSeed(seed, 1);
    PrivateKeyAccount recipientPK = new PrivateKeyAccount(accountSeed1);
    Account recipient = new Account(recipientPK.getAddress());
    byte[] accountSeed2 = Wallet.generateAccountSeed(seed, 2);
    PrivateKeyAccount recipientPK2 = new PrivateKeyAccount(accountSeed2);
    Account recipient2 = new Account(recipientPK2.getAddress());

    BigDecimal amount = BigDecimal.valueOf(10); //;
    
    String head = null;
    byte[] data = null;
    byte[] isText = new byte[]{1};
    byte[] encrypted = new byte[]{0};
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;

    // INIT ASSETS
    private void init() {

        db = DCSet.createEmptyDatabaseSet(0);
        Controller.getInstance().setDCSet(db);
        gb = new GenesisBlock();
        try {
            gb.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // FEE FUND
        maker.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        maker.changeBalance(db, false, false, ERM_KEY, BigDecimal.valueOf(100), false, false, false);
        maker.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1), false, false, false);
        recipient.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1), false, false, false);
        recipient2.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1), false, false, false);

        assetA = new AssetVenture(itemAppData, maker, "AAA", icon, image, ".", AssetCls.AS_OUTSIDE_OTHER_CLAIM, 2, 0L);

        // set SCALABLE assets ++
        assetA.insertToMap(db, BlockChain.AMOUNT_SCALE_FROM);

        issueAssetTransaction = new IssueAssetTransaction(maker, assetA, (byte) 0, timestamp++, 0l, new byte[64]);
        issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issueAssetTransaction.process(null, Transaction.FOR_NETWORK);

        keyA = issueAssetTransaction.getAssetKey(db);
        balanceA = maker.getBalance(db, keyA);

    }


    @Test
    public void validate_R_Send_OutSideClaim() {

        init();

        //////////////// VALIDATE
        
        /// invalid CLAIM
        RSend r_SendV3 = new RSend(version,
                prop1_backward,
                prop2,
                maker, exLink, smartContract, FEE_POWER, recipient, keyA, amount, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(maker, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.INVALID_BACKWARD_ACTION);

        /// invalid CLAIM
        r_SendV3 = new RSend(version,
                (byte) 0,
                prop2,
                recipientPK, exLink, smartContract, FEE_POWER, recipient, keyA, amount, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(maker, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.NO_BALANCE);

        /// invalid CLAIM
        r_SendV3 = new RSend(version,
                prop1_backward,
                prop2,
                recipientPK, exLink, smartContract, FEE_POWER, recipient, keyA, amount, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(maker, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.INVALID_CLAIM_RECIPIENT);

        /////////// PROCESS
        /// CLAIM
        r_SendV3 = new RSend(version,
                (byte) 0,
                prop2,
                maker, exLink, smartContract, FEE_POWER, recipient, keyA, amount, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(maker, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);
        
        r_SendV3.process(gb, Transaction.FOR_NETWORK);

        balanceA = maker.getBalance(db, keyA);
        balanceB = recipient.getBalance(db, keyA);
        assertEquals(BigDecimal.valueOf(-10), balanceA.a.a);
        assertEquals(BigDecimal.valueOf(-10), balanceA.a.b);
        assertEquals(BigDecimal.valueOf(0), balanceA.b.a);
        assertEquals(BigDecimal.valueOf(0), balanceA.b.b);

        assertEquals(BigDecimal.valueOf(10), balanceB.a.a);
        assertEquals(BigDecimal.valueOf(10), balanceB.a.b);
        assertEquals(BigDecimal.valueOf(0), balanceB.b.a);
        assertEquals(BigDecimal.valueOf(0), balanceB.b.b);

        assertEquals(r_SendV3.isSignatureValid(db), true);

        r_SendV3 = new RSend(version,
                (byte) 0,
                prop2,
                recipientPK, exLink, smartContract, FEE_POWER, recipient2, keyA, BigDecimal.ONE, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);

        r_SendV3.process(gb, Transaction.FOR_NETWORK);

        balanceA = recipientPK.getBalance(db, keyA);
        balanceB = recipient2.getBalance(db, keyA);
        assertEquals(BigDecimal.valueOf(9), balanceA.a.a);
        assertEquals(BigDecimal.valueOf(9), balanceA.a.b);
        assertEquals(BigDecimal.valueOf(0), balanceA.b.a);
        assertEquals(BigDecimal.valueOf(0), balanceA.b.b);

        assertEquals(BigDecimal.valueOf(1), balanceB.a.a);
        assertEquals(BigDecimal.valueOf(1), balanceB.a.b);
        assertEquals(BigDecimal.valueOf(0), balanceB.b.a);
        assertEquals(BigDecimal.valueOf(0), balanceB.b.b);

        //////// TRY IN CLAIM to not emitter
        long credit_keyA = -keyA;
        r_SendV3 = new RSend(version,
                (byte) 0,
                prop2,
                recipientPK, exLink, smartContract, FEE_POWER, recipient2, credit_keyA, BigDecimal.ONE, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.INVALID_CLAIM_DEBT_RECIPIENT);

        //////// TRY RE CLAIM to not emitter
        r_SendV3 = new RSend(version,
                this.prop1_backward,
                prop2,
                recipientPK, exLink, smartContract, FEE_POWER, recipient2, credit_keyA, BigDecimal.ONE, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.INVALID_CLAIM_DEBT_RECIPIENT);

        //////// TRY IN CLAIM to EMITTER - NO BALANCE
        r_SendV3 = new RSend(version,
                (byte) 0,
                prop2,
                recipientPK2, exLink, smartContract, FEE_POWER, maker, credit_keyA, new BigDecimal(2), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK2, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.NO_BALANCE);

        //////// TRY IN CLAIM to EMITTER - VALID
        r_SendV3 = new RSend(version,
                (byte) 0,
                prop2,
                recipientPK, exLink, smartContract, FEE_POWER, maker, credit_keyA, new BigDecimal(2), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);
        
        r_SendV3.process(gb, Transaction.FOR_NETWORK);

        balanceA = recipientPK.getBalance(db, keyA);
        balanceB = maker.getBalance(db, keyA);
        assertEquals(BigDecimal.valueOf(9), balanceA.a.a);
        assertEquals(BigDecimal.valueOf(9), balanceA.a.b);
        assertEquals(BigDecimal.valueOf(-2), balanceA.b.a);
        assertEquals(BigDecimal.valueOf(-2), balanceA.b.b);

        assertEquals(BigDecimal.valueOf(-10), balanceB.a.a);
        assertEquals(BigDecimal.valueOf(-10), balanceB.a.b);
        assertEquals(BigDecimal.valueOf(2), balanceB.b.a);
        assertEquals(BigDecimal.valueOf(2), balanceB.b.b);

        //////// TRY backward IN CLAIM to EMITTER - VALID
        r_SendV3 = new RSend(version,
                prop1_backward,
                prop2,
                recipientPK, exLink, smartContract, FEE_POWER, maker, credit_keyA, new BigDecimal(1), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);
        
        balanceA = recipientPK.getBalance(db, keyA);
        balanceB = maker.getBalance(db, keyA);

        r_SendV3.process(gb, Transaction.FOR_NETWORK);

        balanceA = recipientPK.getBalance(db, keyA);
        balanceB = maker.getBalance(db, keyA);
        assertEquals(BigDecimal.valueOf(9), balanceA.a.a);
        assertEquals(BigDecimal.valueOf(9), balanceA.a.b);
        assertEquals(BigDecimal.valueOf(-1), balanceA.b.a);
        assertEquals(BigDecimal.valueOf(-1), balanceA.b.b);

        assertEquals(BigDecimal.valueOf(-10), balanceB.a.a);
        assertEquals(BigDecimal.valueOf(-10), balanceB.a.b);
        assertEquals(BigDecimal.valueOf(1), balanceB.b.a);
        assertEquals(BigDecimal.valueOf(1), balanceB.b.b);

        //////// TRY backward IN CLAIM FROM EMITTER - INVALID !
        r_SendV3 = new RSend(version,
                prop1_backward,
                prop2,
                maker, exLink, smartContract, FEE_POWER, recipientPK, credit_keyA, new BigDecimal(1), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.INVALID_CLAIM_DEBT_RECIPIENT);

        //////// TRY CLOSE OVER amount CLAIM to EMITTER - INVALID
        r_SendV3 = new RSend(version,
                prop1_backward,
                prop2,
                recipientPK, exLink, smartContract, FEE_POWER, maker, keyA, new BigDecimal(10), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.NO_INCLAIM_BALANCE);

        //////// TRY CLOSE CLAIM to EMITTER - VALID
        r_SendV3 = new RSend(version,
                prop1_backward,
                prop2,
                recipientPK, exLink, smartContract, FEE_POWER, maker, keyA, new BigDecimal(1), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);

        balanceA = recipientPK.getBalance(db, keyA);
        balanceB = maker.getBalance(db, keyA);

        r_SendV3.process(gb, Transaction.FOR_NETWORK);

        balanceA = recipientPK.getBalance(db, keyA);
        balanceB = maker.getBalance(db, keyA);
        assertEquals(BigDecimal.valueOf(9), balanceA.a.a);
        assertEquals(BigDecimal.valueOf(8), balanceA.a.b);
        assertEquals(BigDecimal.valueOf(-1), balanceA.b.a);
        assertEquals(BigDecimal.valueOf(0), balanceA.b.b);

        assertEquals(BigDecimal.valueOf(-10), balanceB.a.a);
        assertEquals(BigDecimal.valueOf(-9), balanceB.a.b);
        assertEquals(BigDecimal.valueOf(1), balanceB.b.a);
        assertEquals(BigDecimal.valueOf(0), balanceB.b.b);


        /* **********************
        */

        //////// TRY CLOSE CLAIM to EMITTER - VALID
        r_SendV3 = new RSend(version,
                (byte) 0,
                prop2,
                recipientPK2, exLink, smartContract, FEE_POWER, maker, credit_keyA, new BigDecimal(1), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK2, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);

        r_SendV3.process(gb, Transaction.FOR_NETWORK);

        //////// TRY CLOSE CLAIM to EMITTER - VALID
        r_SendV3 = new RSend(version,
                prop1_backward,
                prop2,
                recipientPK2, exLink, smartContract, FEE_POWER, maker, keyA, new BigDecimal(1), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK2, Transaction.FOR_NETWORK);
        r_SendV3.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);

        balanceA = recipientPK2.getBalance(db, keyA);
        balanceB = maker.getBalance(db, keyA);

        assertEquals(r_SendV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);

        r_SendV3.process(gb, Transaction.FOR_NETWORK);

        balanceA = recipientPK2.getBalance(db, keyA);
        balanceB = maker.getBalance(db, keyA);
        assertEquals(BigDecimal.valueOf(1), balanceA.a.a);
        assertEquals(BigDecimal.valueOf(0), balanceA.a.b);
        assertEquals(BigDecimal.valueOf(-1), balanceA.b.a);
        assertEquals(BigDecimal.valueOf(0), balanceA.b.b);

        assertEquals(BigDecimal.valueOf(-10), balanceB.a.a);
        assertEquals(BigDecimal.valueOf(-8), balanceB.a.b);
        assertEquals(BigDecimal.valueOf(2), balanceB.b.a);
        assertEquals(BigDecimal.valueOf(0), balanceB.b.b);

    }

}
