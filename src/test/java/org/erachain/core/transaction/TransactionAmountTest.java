package org.erachain.core.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class TransactionAmountTest {

    static Logger LOGGER = LoggerFactory.getLogger(TransactionAmountTest.class.getName());

    long ERA_KEY = AssetCls.ERA_KEY;
    long FEE_KEY = AssetCls.FEE_KEY;

    long flags = 0L;
    byte feePow = 0;
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
    private long seqNo = 0;

    // INIT ASSETS
    private void init() {

        System.setProperty("qwe", "qw");

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
        assertEquals((byte) 128, (byte) -128);
    }

    @Test
    public void isValid() {
    }

    @Test
    public void noAmount() {

        System.out.println("(byte) 128 : " + Integer.toBinaryString((byte) 128));
        System.out.println("(byte)-128 : " + Integer.toBinaryString((byte) -128));

        RSend rSend = new RSend(maker, null, null, feePow, recipient, 1L, amount, "",
                null, isText, encrypted, 1L, flags);

        System.out.println("prop1: " + Integer.toBinaryString(rSend.getTypeBytes()[2])
                + "\nprop2: " + Integer.toBinaryString(rSend.getTypeBytes()[3]));

        System.out.println("propView: " + rSend.viewProperies());

        rSend = new RSend(maker, null, null, feePow, recipient, 0L, null, "",
                null, isText, encrypted, 1L, flags);

        System.out.println("prop1: " + Integer.toBinaryString(rSend.getTypeBytes()[2])
                + "\nprop2: " + Integer.toBinaryString(rSend.getTypeBytes()[3]));

        System.out.println("propView: " + rSend.viewProperies());

    }

}
