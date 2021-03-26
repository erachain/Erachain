package org.erachain.core.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.junit.Test;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class TestRecSendInSideAsset {

    static Logger LOGGER = LoggerFactory.getLogger(TestRecSendInSideAsset.class.getName());

    //Long Transaction.FOR_NETWORK = null;

    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();

    byte version = 2;
    byte prop2 = 0;
    byte prop1_backward = org.erachain.core.transaction.TransactionAmount.BACKWARD_MASK;

    ExLink exLink = null;

    Tuple3<String, Long, String> creditKey;
    Tuple3<String, Long, String> creditKeyReverse;

    long flags = Transaction.NOT_VALIDATE_FLAG_PUBLIC_TEXT;
    Controller cntrl;
    //CREATE KNOWN ACCOUNT
    byte[] privateKey_1 = Crypto.getInstance().createKeyPair(Crypto.getInstance().digest("tes213sdffsdft".getBytes())).getA();
    PrivateKeyAccount creditor = new PrivateKeyAccount(privateKey_1);
    byte[] privateKey_2 = Crypto.getInstance().createKeyPair(Crypto.getInstance().digest("tes213sdffsdft2".getBytes())).getA();
    PrivateKeyAccount emitter = new PrivateKeyAccount(privateKey_2);
    byte[] privateKey_3 = Crypto.getInstance().createKeyPair(Crypto.getInstance().digest("tes213sdffsdft3".getBytes())).getA();
    PrivateKeyAccount debtor = new PrivateKeyAccount(privateKey_3);

    AssetCls asset;
    AssetCls assetInSide;
    
    long assetKey;
    int scale = 3;
    
    RSend r_Send;
    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
        creditorBalance;
    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
        emitterBalance;
    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
        debtorBalance;
    
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;
    private BlockChain bchain;

    // INIT ASSETS
    private void init(boolean withIssue) {

        db = DCSet.createEmptyDatabaseSet(0);
        cntrl = Controller.getInstance();
        cntrl.initBlockChain(db);
        bchain = cntrl.getBlockChain();
        gb = bchain.getGenesisBlock();
        //gb.process(db);

        // FEE FUND
        creditor.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        creditor.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1), false, false, false);

        emitter.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        emitter.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1), false, false, false);

        asset = new AssetVenture(flags, creditor, "aasdasd", icon, image, "asdasda", AssetCls.AS_INSIDE_ASSETS, 8, 50000l);
        // set SCALABLE assets ++
        asset.insertToMap(db, BlockChain.AMOUNT_SCALE_FROM);
        asset.insertToMap(db, 0l);

        assetInSide = new AssetVenture(flags, emitter, "inSide Asset", icon, image, "...", AssetCls.AS_INSIDE_ASSETS, scale, 500l);

        if (withIssue) {
    
            //CREATE ISSUE ASSET TRANSACTION
            IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(emitter, null, assetInSide, FEE_POWER, ++timestamp, 0l);
            issueAssetTransaction.sign(emitter, Transaction.FOR_NETWORK);
            issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
            issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);
    
            assetKey = assetInSide.getKey(db);
        }

    }
  
    /////////////////////////////////////////////
    ////////////
    @Test
    public void validate_R_Send_Movable_Asset() {

        init(true);

        emitterBalance = emitter.getBalance(db, assetKey);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(500), emitterBalance.a.a);
        assertEquals(BigDecimal.valueOf(500), emitterBalance.a.b);
        assertEquals(BigDecimal.valueOf(0), emitterBalance.b.a);
        assertEquals(BigDecimal.valueOf(0), emitterBalance.b.b);
        assertEquals(BigDecimal.valueOf(0), emitterBalance.c.a);
        assertEquals(BigDecimal.valueOf(0), emitterBalance.c.b);

        //CREATE ASSET TRANSFER
        
        // INVALID
        r_Send = new RSend(emitter, exLink, FEE_POWER, debtor, assetKey, BigDecimal.valueOf(1000),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.NO_BALANCE);

        r_Send = new RSend(emitter, exLink, FEE_POWER, creditor, assetKey, BigDecimal.valueOf(50),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.VALIDATE_OK);

        r_Send.sign(emitter, Transaction.FOR_NETWORK);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        r_Send.process(gb, Transaction.FOR_NETWORK);

        emitterBalance = emitter.getBalance(db, assetKey);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(500), emitterBalance.a.a);
        assertEquals(BigDecimal.valueOf(450), emitterBalance.a.b);

        assertEquals(BigDecimal.valueOf(0), emitterBalance.b.a);
        assertEquals(BigDecimal.valueOf(0), emitterBalance.b.b);

        //CHECK BALANCE RECIPIENT
        creditorBalance = creditor.getBalance(db, assetKey);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.a);
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.b);

        assertEquals(BigDecimal.valueOf(0), creditorBalance.b.a);
        assertEquals(BigDecimal.valueOf(0), creditorBalance.b.b);

        //////////////////////////////////////////////////
        /// ORPHAN
        /////////////////////////////////////////////////
        r_Send.orphan(gb, Transaction.FOR_NETWORK);

        //CHECK BALANCE SENDER
        emitterBalance = emitter.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(500), emitterBalance.a.a);
        assertEquals(BigDecimal.valueOf(500), emitterBalance.a.b);

        //CHECK BALANCE RECIPIENT
        creditorBalance = creditor.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(0), creditorBalance.a.a);
        assertEquals(BigDecimal.valueOf(0), creditorBalance.a.b);

        // BACK PROCESS
        r_Send.process(gb, Transaction.FOR_NETWORK);

        // INVALID
        r_Send = new RSend(
                debtor, exLink, FEE_POWER, emitter, -assetKey, BigDecimal.valueOf(10),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.NO_BALANCE);

        // INVALID
        r_Send = new RSend(
                version,
                prop1_backward,
                prop2,
                debtor, exLink, FEE_POWER, emitter, -assetKey, BigDecimal.valueOf(10),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.NO_DEBT_BALANCE);

        // INVALID
        r_Send = new RSend(
                creditor, exLink, FEE_POWER, debtor, -assetKey, BigDecimal.valueOf(100),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.NO_BALANCE);

        // GET CREDIT - дать в кредит актив
        r_Send = new RSend(
                creditor, exLink, FEE_POWER, debtor, -assetKey, BigDecimal.valueOf(10),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.VALIDATE_OK);

        r_Send.sign(emitter, Transaction.FOR_NETWORK);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        r_Send.process(gb, Transaction.FOR_NETWORK);

        creditKey = new Tuple3<String, Long, String>(creditor.getAddress(), assetKey, debtor.getAddress());
        assertEquals(BigDecimal.valueOf(10), db.getCredit_AddressesMap().get(creditKey));

        //CHECK BALANCE CREDITOR
        creditorBalance = creditor.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.a);
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.b);

        assertEquals(BigDecimal.valueOf(0), creditorBalance.b.a);
        assertEquals(BigDecimal.valueOf(-10), creditorBalance.b.b);
        assertEquals(BigDecimal.valueOf(40), creditor.getBalanceUSE(assetKey, db));

        //CHECK BALANCE DEBTOR
        debtorBalance = debtor.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(0), debtorBalance.a.a);
        assertEquals(BigDecimal.valueOf(0), debtorBalance.a.b);

        assertEquals(BigDecimal.valueOf(10), debtorBalance.b.a);
        assertEquals(BigDecimal.valueOf(10), debtorBalance.b.b);

        //////////////////////////////////////////////////
        /// ORPHAN
        /////////////////////////////////////////////////
        r_Send.orphan(gb, Transaction.FOR_NETWORK);

        assertEquals(BigDecimal.valueOf(0), db.getCredit_AddressesMap().get(creditKey));

        //CHECK BALANCE CREDITOR
        creditorBalance = creditor.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.a);
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.b);

        assertEquals(BigDecimal.valueOf(0), creditorBalance.b.a);
        assertEquals(BigDecimal.valueOf(0), creditorBalance.b.b);
        assertEquals(BigDecimal.valueOf(50), creditor.getBalanceUSE(assetKey, db));

        //CHECK BALANCE DEBTOR
        debtorBalance = debtor.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(0), debtorBalance.a.a);
        assertEquals(BigDecimal.valueOf(0), debtorBalance.a.b);

        assertEquals(BigDecimal.valueOf(0), debtorBalance.b.a);
        assertEquals(BigDecimal.valueOf(0), debtorBalance.b.b);

        // PROCESS BACK
        r_Send.process(gb, Transaction.FOR_NETWORK);
        assertEquals(BigDecimal.valueOf(10), db.getCredit_AddressesMap().get(creditKey));

        //////////////////////
        // GET backward credit

        // INVALID
        r_Send = new RSend(
                version,
                prop1_backward,
                prop2,
                creditor, exLink, FEE_POWER, debtor, -assetKey, BigDecimal.valueOf(20),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.NO_DEBT_BALANCE);

        // INVALID
        r_Send = new RSend(
                version,
                prop1_backward,
                prop2,
                creditor, exLink, FEE_POWER, debtor, -assetKey, BigDecimal.valueOf(7),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.VALIDATE_OK);

        r_Send.sign(emitter, Transaction.FOR_NETWORK);
        r_Send.process(gb, Transaction.FOR_NETWORK);

        assertEquals(BigDecimal.valueOf(3), db.getCredit_AddressesMap().get(creditKey));

        //CHECK BALANCE CREDITOR
        creditorBalance = creditor.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.a);
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.b);

        assertEquals(BigDecimal.valueOf(0), creditorBalance.b.a);
        assertEquals(BigDecimal.valueOf(-3), creditorBalance.b.b);
        assertEquals(BigDecimal.valueOf(47), creditor.getBalanceUSE(assetKey, db));

        //CHECK BALANCE DEBTOR
        debtorBalance = debtor.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(0), debtorBalance.a.a);
        assertEquals(BigDecimal.valueOf(0), debtorBalance.a.b);

        assertEquals(BigDecimal.valueOf(3), debtorBalance.b.a);
        assertEquals(BigDecimal.valueOf(3), debtorBalance.b.b);

        //////////////////////////////////////////////////
        /// ORPHAN
        /////////////////////////////////////////////////
        r_Send.orphan(gb, Transaction.FOR_NETWORK);

        assertEquals(BigDecimal.valueOf(10), db.getCredit_AddressesMap().get(creditKey));

        //CHECK BALANCE CREDITOR
        creditorBalance = creditor.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.a);
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.b);

        assertEquals(BigDecimal.valueOf(0), creditorBalance.b.a);
        assertEquals(BigDecimal.valueOf(-10), creditorBalance.b.b);
        assertEquals(BigDecimal.valueOf(40), creditor.getBalanceUSE(assetKey, db));

        //CHECK BALANCE DEBTOR
        debtorBalance = debtor.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(0), debtorBalance.a.a);
        assertEquals(BigDecimal.valueOf(0), debtorBalance.a.b);

        assertEquals(BigDecimal.valueOf(10), debtorBalance.b.a);
        assertEquals(BigDecimal.valueOf(10), debtorBalance.b.b);

        // PROCESS BACK
        r_Send.process(gb, Transaction.FOR_NETWORK);

        // SEND 2
        r_Send = new RSend(emitter, exLink, FEE_POWER, debtor, assetKey, BigDecimal.valueOf(30),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.VALIDATE_OK);

        r_Send.sign(emitter, Transaction.FOR_NETWORK);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        r_Send.process(gb, Transaction.FOR_NETWORK);

        assertEquals(BigDecimal.valueOf(3), db.getCredit_AddressesMap().get(creditKey));
        creditKeyReverse = new Tuple3<String, Long, String>(debtor.getAddress(), assetKey, creditor.getAddress());
        assertEquals(BigDecimal.valueOf(0), db.getCredit_AddressesMap().get(creditKeyReverse));

        // CREDIT 2
        r_Send = new RSend(debtor, exLink, FEE_POWER, creditor, -assetKey, BigDecimal.valueOf(30),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.VALIDATE_OK);

        r_Send.sign(emitter, Transaction.FOR_NETWORK);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        r_Send.process(gb, Transaction.FOR_NETWORK);

        assertEquals(BigDecimal.valueOf(0), db.getCredit_AddressesMap().get(creditKey));
        assertEquals(BigDecimal.valueOf(27), db.getCredit_AddressesMap().get(creditKeyReverse));


        //CHECK BALANCE CREDITOR
        creditorBalance = creditor.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.a);
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.b);

        assertEquals(BigDecimal.valueOf(30), creditorBalance.b.a);
        assertEquals(BigDecimal.valueOf(27), creditorBalance.b.b);
        assertEquals(BigDecimal.valueOf(50+27), creditor.getBalanceUSE(assetKey, db));

        //CHECK BALANCE DEBTOR
        debtorBalance = debtor.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(30), debtorBalance.a.a);
        assertEquals(BigDecimal.valueOf(30), debtorBalance.a.b);

        assertEquals(BigDecimal.valueOf(3), debtorBalance.b.a);
        assertEquals(BigDecimal.valueOf(-27), debtorBalance.b.b);

        ///////// теперь проверим возврат долга выше своего возможного значения

        // CREDIT INVALID
        r_Send = new RSend(debtor, exLink, FEE_POWER, creditor, -assetKey, BigDecimal.valueOf(60),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.NO_BALANCE);

        r_Send.sign(emitter, Transaction.FOR_NETWORK);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        r_Send.process(gb, Transaction.FOR_NETWORK);

        assertEquals(BigDecimal.valueOf(0), db.getCredit_AddressesMap().get(creditKey));
        assertEquals(BigDecimal.valueOf(27), db.getCredit_AddressesMap().get(creditKeyReverse));


        //CHECK BALANCE CREDITOR
        creditorBalance = creditor.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.a);
        assertEquals(BigDecimal.valueOf(50), creditorBalance.a.b);

        assertEquals(BigDecimal.valueOf(30), creditorBalance.b.a);
        assertEquals(BigDecimal.valueOf(27), creditorBalance.b.b);
        assertEquals(BigDecimal.valueOf(50+27), creditor.getBalanceUSE(assetKey, db));

        //CHECK BALANCE DEBTOR
        debtorBalance = debtor.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(30), debtorBalance.a.a);
        assertEquals(BigDecimal.valueOf(30), debtorBalance.a.b);

        assertEquals(BigDecimal.valueOf(3), debtorBalance.b.a);
        assertEquals(BigDecimal.valueOf(-27), debtorBalance.b.b);



    }

}
