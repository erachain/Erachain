package org.erachain.core.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.math.BigDecimal;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.Test;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;

public class TestRec_Send_Movable {

    static Logger LOGGER = LoggerFactory.getLogger(TestRec_Send_Movable.class.getName());

    Long releaserReference = null;

    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();

    byte version = 2;
    byte prop2 = 0;    
    byte prop1_backward = org.erachain.core.transaction.TransactionAmount.BACKWARD_MASK;

    long flags = 0l;
    Controller cntrl;
    //CREATE KNOWN ACCOUNT
    byte[] privateKey_1 = Crypto.getInstance().createKeyPair(Crypto.getInstance().digest("tes213sdffsdft".getBytes())).getA();
    PrivateKeyAccount deliver = new PrivateKeyAccount(privateKey_1);
    byte[] privateKey_2 = Crypto.getInstance().createKeyPair(Crypto.getInstance().digest("tes213sdffsdft2".getBytes())).getA();
    PrivateKeyAccount producer = new PrivateKeyAccount(privateKey_2);
    byte[] privateKey_3 = Crypto.getInstance().createKeyPair(Crypto.getInstance().digest("tes213sdffsdft3".getBytes())).getA();
    PrivateKeyAccount spender = new PrivateKeyAccount(privateKey_3);

    AssetCls asset;
    AssetCls assetMovable;
    
    long assetKey;
    int scale = 3;
    
    R_Send r_Send;
    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> deliverBalance;
    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> producerBalance;
    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> spenderBalance;
    
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;
    private BlockChain bchain;

    // INIT ASSETS
    private void init(boolean withIssue) {

        db = DCSet.createEmptyDatabaseSet();
        cntrl = Controller.getInstance();
        cntrl.initBlockChain(db);
        bchain = cntrl.getBlockChain();
        gb = bchain.getGenesisBlock();
        //gb.process(db);

        // FEE FUND
        deliver.setLastTimestamp(gb.getTimestamp(), db);
        deliver.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1), false);

        producer.setLastTimestamp(gb.getTimestamp(), db);
        producer.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1), false);

        asset = new AssetVenture(deliver, "aasdasd", icon, image, "asdasda", AssetCls.AS_INSIDE_ASSETS, 8, 50000l);
        // set SCALABLE assets ++
        asset.insertToMap(db, BlockChain.AMOUNT_SCALE_FROM);
        asset.insertToMap(db, 0l);

        assetMovable = new AssetVenture(producer, "movable", icon, image, "...", AssetCls.AS_OUTSIDE_GOODS, scale, 500l);

        if (withIssue) {
    
            //CREATE ISSUE ASSET TRANSACTION
            IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(producer, assetMovable, FEE_POWER, ++timestamp, 0l);
            issueAssetTransaction.sign(producer, Transaction.FOR_NETWORK);
            issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1);
            issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);
    
            assetKey = assetMovable.getKey(db);
        }

    }
  
    /////////////////////////////////////////////
    ////////////
    
    

    @Test
    public void process_Movable_Asset() {

        init(true);

        long keyMovable = assetMovable.getKey(db);

        producerBalance = producer.getBalance(db, keyMovable);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(500), producerBalance.a.a);
        assertEquals(BigDecimal.valueOf(500), producerBalance.a.b);
        assertEquals(BigDecimal.valueOf(500), producerBalance.c.a);
        assertEquals(BigDecimal.valueOf(500), producerBalance.c.b);


        //CREATE SIGNATURE
        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
        long timestamp = NTP.getTime();

        //CREATE ASSET TRANSFER
        r_Send = new R_Send(producer, FEE_POWER, recipient, keyMovable, BigDecimal.valueOf(1000),
                "", null, new byte[]{1}, new byte[]{1},
                timestamp++, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.NO_BALANCE);

        r_Send = new R_Send(producer, FEE_POWER, recipient, keyMovable, BigDecimal.valueOf(5),
                "", null, new byte[]{1}, new byte[]{1},
                timestamp++, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 2);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.VALIDATE_OK);


        r_Send.sign(producer, Transaction.FOR_NETWORK);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 3);
        r_Send.process(gb, Transaction.FOR_NETWORK);

        producerBalance = producer.getBalance(db, keyMovable);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(500), producerBalance.a.a);
        assertEquals(BigDecimal.valueOf(495), producerBalance.a.b);

        assertEquals(BigDecimal.valueOf(500), producerBalance.c.b);
        assertEquals(BigDecimal.valueOf(500), producerBalance.c.b);

        //CHECK BALANCE RECIPIENT
        assertEquals(BigDecimal.valueOf(5), recipient.getBalanceUSE(keyMovable, db));

        //CHECK REFERENCE SENDER
        assertEquals(r_Send.getTimestamp(), producer.getLastTimestamp(db));

        //CHECK REFERENCE RECIPIENT
        assertNotEquals(r_Send.getTimestamp(), recipient.getLastTimestamp(db));

        //////////////////////////////////////////////////
        /// ORPHAN
        /////////////////////////////////////////////////
        r_Send.orphan(Transaction.FOR_NETWORK);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(500), producer.getBalanceUSE(keyMovable, db));

        //CHECK BALANCE RECIPIENT
        assertEquals(BigDecimal.ZERO, recipient.getBalanceUSE(keyMovable, db));

        //CHECK REFERENCE SENDER
        //assertEquals(r_Send.getReference(), producer.getLastReference(db));

        //CHECK REFERENCE RECIPIENT
        assertNotEquals(r_Send.getTimestamp(), recipient.getLastTimestamp(db));
    }

    
    @Test
    public void validate_R_Send_Movable_Asset() {

        init(true);

        producerBalance = producer.getBalance(db, assetKey);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(500), producerBalance.a.a);
        assertEquals(BigDecimal.valueOf(500), producerBalance.a.b);
        assertEquals(BigDecimal.valueOf(500), producerBalance.c.a);
        assertEquals(BigDecimal.valueOf(500), producerBalance.c.b);

        //CREATE ASSET TRANSFER
        r_Send = new R_Send(producer, FEE_POWER, spender, assetKey, BigDecimal.valueOf(1000),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.NO_BALANCE);

        r_Send = new R_Send(producer, FEE_POWER, spender, assetKey, BigDecimal.valueOf(50),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 2);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.VALIDATE_OK);

        r_Send.sign(producer, Transaction.FOR_NETWORK);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 3);
        r_Send.process(gb, Transaction.FOR_NETWORK);

        producerBalance = producer.getBalance(db, assetKey);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(500), producerBalance.a.a);
        assertEquals(BigDecimal.valueOf(450), producerBalance.a.b);

        assertEquals(BigDecimal.valueOf(500), producerBalance.c.a);
        assertEquals(BigDecimal.valueOf(500), producerBalance.c.b);

        //CHECK BALANCE RECIPIENT
        deliverBalance = spender.getBalance(db, assetKey);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(50), deliverBalance.a.a);
        assertEquals(BigDecimal.valueOf(50), deliverBalance.a.b);

        assertEquals(BigDecimal.valueOf(0), deliverBalance.c.a);
        assertEquals(BigDecimal.valueOf(0), deliverBalance.c.b);

        //////////////////////////////////////////////////
        /// ORPHAN
        /////////////////////////////////////////////////
        r_Send.orphan(Transaction.FOR_NETWORK);

        //CHECK BALANCE SENDER
        producerBalance = producer.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(500), producerBalance.a.a);
        assertEquals(BigDecimal.valueOf(500), producerBalance.a.b);

        //CHECK BALANCE RECIPIENT
        deliverBalance = spender.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(0), deliverBalance.a.a);
        assertEquals(BigDecimal.valueOf(0), deliverBalance.a.b);

        // BACK PROCESS
        r_Send.process(gb, Transaction.FOR_NETWORK);

        // INVALID
        r_Send = new R_Send(
                version,
                prop1_backward,
                prop2,
                deliver, FEE_POWER, spender, assetKey, BigDecimal.valueOf(-100),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 4);
        
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.NO_HOLD_BALANCE);

        // GET ON HOLD - доставщик берет к себе на руки товар
        r_Send = new R_Send(
                version,
                prop1_backward,
                prop2,
                deliver, FEE_POWER, producer, assetKey, BigDecimal.valueOf(-10),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 5);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.VALIDATE_OK);

        r_Send.sign(producer, Transaction.FOR_NETWORK);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 6);
        r_Send.process(gb, Transaction.FOR_NETWORK);

        producerBalance = producer.getBalance(db, assetKey);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(500), producerBalance.a.a);
        assertEquals(BigDecimal.valueOf(450), producerBalance.a.b);

        assertEquals(BigDecimal.valueOf(500), producerBalance.c.a);
        assertEquals(BigDecimal.valueOf(490), producerBalance.c.b);

        //CHECK BALANCE RECIPIENT
        deliverBalance = deliver.getBalance(db, assetKey);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(0), deliverBalance.a.a);
        assertEquals(BigDecimal.valueOf(0), deliverBalance.a.b);

        assertEquals(BigDecimal.valueOf(10), deliverBalance.c.a);
        assertEquals(BigDecimal.valueOf(10), deliverBalance.c.b);

        //////////////////////////////////////////////////
        /// ORPHAN
        /////////////////////////////////////////////////
        r_Send.orphan(Transaction.FOR_NETWORK);

        //CHECK BALANCE SENDER
        producerBalance = producer.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(500), producerBalance.a.a);
        assertEquals(BigDecimal.valueOf(450), producerBalance.a.b);
        assertEquals(BigDecimal.valueOf(500), producerBalance.c.a);
        assertEquals(BigDecimal.valueOf(500), producerBalance.c.b);

        //CHECK BALANCE RECIPIENT
        deliverBalance = deliver.getBalance(db, assetKey);
        assertEquals(BigDecimal.valueOf(0), deliverBalance.a.a);
        assertEquals(BigDecimal.valueOf(0), deliverBalance.a.b);
        assertEquals(BigDecimal.valueOf(0), deliverBalance.c.a);
        assertEquals(BigDecimal.valueOf(0), deliverBalance.c.b);

        r_Send.process(gb, Transaction.FOR_NETWORK);

        //////////////////////
        // GET ON HOLD - доставщик передает новому собственнику на руки товар - это подтверждает собственник
        r_Send = new R_Send(
                version,
                prop1_backward,
                prop2,
                spender, FEE_POWER, deliver, assetKey, BigDecimal.valueOf(-10),
                "", null, new byte[]{1}, new byte[]{1},
                ++timestamp, 0l);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 2);
        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.VALIDATE_OK);

        r_Send.sign(producer, Transaction.FOR_NETWORK);
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 5);
        r_Send.process(gb, Transaction.FOR_NETWORK);

        spenderBalance = spender.getBalance(db, assetKey);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(50), spenderBalance.a.a);
        assertEquals(BigDecimal.valueOf(50), spenderBalance.a.b);

        assertEquals(BigDecimal.valueOf(10), spenderBalance.c.a);
        assertEquals(BigDecimal.valueOf(10), spenderBalance.c.b);

        //CHECK BALANCE RECIPIENT
        deliverBalance = deliver.getBalance(db, assetKey);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(0), deliverBalance.a.a);
        assertEquals(BigDecimal.valueOf(0), deliverBalance.a.b);

        assertEquals(BigDecimal.valueOf(10), deliverBalance.c.a);
        assertEquals(BigDecimal.valueOf(0), deliverBalance.c.b);

        //////////////////////////////////////////////////
        /// ORPHAN
        /////////////////////////////////////////////////
        r_Send.orphan(Transaction.FOR_NETWORK);

        spenderBalance = spender.getBalance(db, assetKey);

        //CHECK BALANCE SPENDER
        assertEquals(BigDecimal.valueOf(50), spenderBalance.a.a);
        assertEquals(BigDecimal.valueOf(50), spenderBalance.a.b);

        assertEquals(BigDecimal.valueOf(0), spenderBalance.c.a);
        assertEquals(BigDecimal.valueOf(0), spenderBalance.c.b);

        //CHECK BALANCE DELIVER
        deliverBalance = deliver.getBalance(db, assetKey);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(0), deliverBalance.a.a);
        assertEquals(BigDecimal.valueOf(0), deliverBalance.a.b);

        assertEquals(BigDecimal.valueOf(10), deliverBalance.c.a);
        assertEquals(BigDecimal.valueOf(10), deliverBalance.c.b);

        r_Send.process(gb, Transaction.FOR_NETWORK);

    }

}
