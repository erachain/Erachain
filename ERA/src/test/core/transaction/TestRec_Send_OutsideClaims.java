package core.transaction;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.assets.AssetVenture;
import core.wallet.Wallet;
import datachain.DCSet;
import ntp.NTP;


public class TestRec_Send_OutsideClaims {

    static Logger LOGGER = Logger.getLogger(TestRec_Send_OutsideClaims.class.getName());

    Long releaserReference = null;

    long ERM_KEY = 3;
    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();
    byte version = 2;
    byte prop2 = 0;
    
    byte prop1_backward = core.transaction.TransactionAmount.BACKWARD_MASK;

    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balanceA;
    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balanceB;

    IssueAssetTransaction issueAssetTransaction;
    AssetCls assetA;
    long keyA;

    long flags = 0l;
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

        db = DCSet.createEmptyDatabaseSet();
        Controller.getInstance().setDCSet(db);
        gb = new GenesisBlock();
        try {
            gb.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // FEE FUND
        maker.setLastTimestamp(gb.getTimestamp(db), db);
        maker.changeBalance(db, false, ERM_KEY, BigDecimal.valueOf(100), false);
        maker.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1), false);
        recipient.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1), false);
        recipient2.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1), false);

        assetA = new AssetVenture(maker, "AAA", icon, image, ".", AssetCls.AS_OUTSIDE_OTHER_CLAIM, 2, 0L);

        // set SCALABLE assets ++
        assetA.insertToMap(db, BlockChain.AMOUNT_SCALE_FROM);

        issueAssetTransaction = new IssueAssetTransaction(maker, assetA, (byte) 0, timestamp++, 0l, new byte[64]);
        issueAssetTransaction.setDC(db, false);
        issueAssetTransaction.process(null, false);

        keyA = issueAssetTransaction.getAssetKey(db);
        balanceA = maker.getBalance(db, keyA);

    }


    @Test
    public void validate_R_Send_OutSideClaim() {

        init();

        //////////////// VALIDATE
        
        /// invalid CLAIM
        R_Send r_SendV3 = new R_Send(version,
                prop1_backward,
                prop2,
                maker, FEE_POWER, recipient, keyA, amount, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(maker, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.INVALID_BACKWARD_ACTION);

        /// invalid CLAIM
        r_SendV3 = new R_Send(version,
                (byte) 0,
                prop2,
                recipientPK, FEE_POWER, recipient, keyA, amount, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(maker, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.NO_BALANCE);

        /// invalid CLAIM
        r_SendV3 = new R_Send(version,
                prop1_backward,
                prop2,
                recipientPK, FEE_POWER, recipient, keyA, amount, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(maker, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.INVALID_CLAIM_RECIPIENT);

        /////////// PROCESS
        /// CLAIM
        r_SendV3 = new R_Send(version,
                (byte) 0,
                prop2,
                maker, FEE_POWER, recipient, keyA, amount, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(maker, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.VALIDATE_OK);
        
        r_SendV3.process(gb, false);

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

        r_SendV3 = new R_Send(version,
                (byte) 0,
                prop2,
                recipientPK, FEE_POWER, recipient2, keyA, BigDecimal.ONE, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.VALIDATE_OK);

        r_SendV3.process(gb, false);

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
        r_SendV3 = new R_Send(version,
                (byte) 0,
                prop2,
                recipientPK, FEE_POWER, recipient2, credit_keyA, BigDecimal.ONE, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.INVALID_CLAIM_DEBT_RECIPIENT);

        //////// TRY RE CLAIM to not emitter
        r_SendV3 = new R_Send(version,
                this.prop1_backward,
                prop2,
                recipientPK, FEE_POWER, recipient2, credit_keyA, BigDecimal.ONE, head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.INVALID_CLAIM_DEBT_RECIPIENT);

        //////// TRY IN CLAIM to EMITTER - NO BALANCE
        r_SendV3 = new R_Send(version,
                (byte) 0,
                prop2,
                recipientPK2, FEE_POWER, maker, credit_keyA, new BigDecimal(2), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK2, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.NO_BALANCE);

        //////// TRY IN CLAIM to EMITTER - VALID
        r_SendV3 = new R_Send(version,
                (byte) 0,
                prop2,
                recipientPK, FEE_POWER, maker, credit_keyA, new BigDecimal(2), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.VALIDATE_OK);
        
        r_SendV3.process(gb, false);

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
        r_SendV3 = new R_Send(version,
                prop1_backward,
                prop2,
                recipientPK, FEE_POWER, maker, credit_keyA, new BigDecimal(1), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.VALIDATE_OK);
        
        balanceA = recipientPK.getBalance(db, keyA);
        balanceB = maker.getBalance(db, keyA);

        r_SendV3.process(gb, false);

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
        r_SendV3 = new R_Send(version,
                prop1_backward,
                prop2,
                maker, FEE_POWER, recipientPK, credit_keyA, new BigDecimal(1), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.INVALID_CLAIM_DEBT_RECIPIENT);

        //////// TRY CLOSE OVER amount CLAIM to EMITTER - INVALID
        r_SendV3 = new R_Send(version,
                prop1_backward,
                prop2,
                recipientPK, FEE_POWER, maker, keyA, new BigDecimal(10), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.NO_INCLAIM_BALANCE);

        //////// TRY CLOSE CLAIM to EMITTER - VALID
        r_SendV3 = new R_Send(version,
                prop1_backward,
                prop2,
                recipientPK, FEE_POWER, maker, keyA, new BigDecimal(1), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.VALIDATE_OK);

        balanceA = recipientPK.getBalance(db, keyA);
        balanceB = maker.getBalance(db, keyA);

        r_SendV3.process(gb, false);

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
        r_SendV3 = new R_Send(version,
                (byte) 0,
                prop2,
                recipientPK2, FEE_POWER, maker, credit_keyA, new BigDecimal(1), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK2, false);
        r_SendV3.setDC(db, false);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.VALIDATE_OK);

        r_SendV3.process(gb, false);

        //////// TRY CLOSE CLAIM to EMITTER - VALID
        r_SendV3 = new R_Send(version,
                prop1_backward,
                prop2,
                recipientPK2, FEE_POWER, maker, keyA, new BigDecimal(1), head, data, isText, encrypted, timestamp, ++timestamp);
        r_SendV3.sign(recipientPK2, false);
        r_SendV3.setDC(db, false);

        balanceA = recipientPK2.getBalance(db, keyA);
        balanceB = maker.getBalance(db, keyA);

        assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.VALIDATE_OK);

        r_SendV3.process(gb, false);

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
