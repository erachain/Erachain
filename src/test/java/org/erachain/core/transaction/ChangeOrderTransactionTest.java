package org.erachain.core.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.item.assets.Order;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Test;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ChangeOrderTransactionTest {

    static Logger LOGGER = LoggerFactory.getLogger(ChangeOrderTransactionTest.class.getName());

    ExLink exLink = null;

    int forDeal = Transaction.FOR_NETWORK;

    int[] TESTED_DBS = new int[]{
            IDB.DBS_MAP_DB,
            //IDB.DBS_ROCK_DB
    };

    //Long Transaction.FOR_NETWORK = null;

    int asPack = Transaction.FOR_NETWORK;

    long dbRef = 0L;
    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 1;
    byte[] assetReference = new byte[64];

    long timestamp;

    byte version = 2;
    byte prop2 = 0;
    byte prop1_backward = org.erachain.core.transaction.TransactionAmount.BACKWARD_MASK;

    byte[] itemAppData = null;
    long txFlags = 0L;

    Controller cntrl;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("tes213sdffsdft".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    byte[] seed_1 = Crypto.getInstance().digest("tes213sdffsdft_1".getBytes());
    byte[] privateKey_1 = Crypto.getInstance().createKeyPair(seed_1).getA();
    PrivateKeyAccount maker_1 = new PrivateKeyAccount(privateKey_1);
    AssetCls asset;
    AssetCls assetMovable;
    long key = 0;
    RSend rsend;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance5;
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet dcSet;
    private GenesisBlock gb;
    private BlockChain bchain;

    // INIT ASSETS
    private void init(int dbs) {

        LOGGER.info(" ********** open DBS: " + dbs);

        File tempDir = new File(Settings.getInstance().getDataTempDir());
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        dcSet = DCSet.createEmptyHardDatabaseSet(dbs);
        cntrl = Controller.getInstance();
        timestamp = NTP.getTime();

        cntrl.initBlockChain(dcSet);
        bchain = cntrl.getBlockChain();
        gb = bchain.getGenesisBlock();
        //gb.process(db);

        // FEE FUND
        maker.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, dcSet);
        maker.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        maker_1.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, dcSet);

        asset = new AssetVenture(itemAppData, maker, "aasdasd", icon, image, "asdasda", 1, 8, 50000L);
        // set SCALABLE assets ++
        asset.setReference(Crypto.getInstance().digest(asset.toBytes(forDeal, false, false)), dbRef);
        asset.insertToMap(dcSet, BlockChain.AMOUNT_SCALE_FROM);
        asset.insertToMap(dcSet, 0L);
        key = asset.getKey(dcSet);

        assetMovable = new AssetVenture(itemAppData, maker, "movable", icon, image, "...", 0, 8, 500L);
        assetMovable.setReference(Crypto.getInstance().digest(assetMovable.toBytes(forDeal, false, false)), dbRef);

    }


    @Test
    public void parse() {

        init(IDB.DBS_MAP_DB);

        //CREATE UPDATE ORDER
        ChangeOrderTransaction tx = new ChangeOrderTransaction(maker, new byte[64], BigDecimal.TEN, FEE_POWER, timestamp, 0L);
        tx.sign(maker, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawTX = tx.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawTX.length, tx.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            ChangeOrderTransaction parsedTX = (ChangeOrderTransaction) TransactionFactory.getInstance().parse(rawTX, Transaction.FOR_NETWORK);

            //CHECK INSTANCE
            assertEquals(true, parsedTX instanceof ChangeOrderTransaction);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(tx.getSignature(), parsedTX.getSignature()));

            //CHECK ISSUER
            assertEquals(tx.getCreator().getAddress(), parsedTX.getCreator().getAddress());

            //CHECK REFERENCE
            //assertEquals((long)tx.getReference(), (long)parsedTX.getReference());

            //CHECK TIMESTAMP
            assertEquals(tx.getTimestamp(), parsedTX.getTimestamp());

            assertEquals(tx.getAmountWant(), parsedTX.getAmountWant());

            assertEquals(Arrays.equals(tx.getOrderRef(), parsedTX.getOrderRef()), true);

        } catch (Exception e) {
            fail("Exception while parsing transaction.");
        }

        //PARSE TRANSACTION FROM WRONG BYTES
        rawTX = new byte[tx.getDataLength(Transaction.FOR_NETWORK, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawTX, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }

    }

    @Test
    public void toBytes() {
    }

    @Test
    public void getDataLength() {
    }

    @Test
    public void isValid() {
    }

    @Test
    public void makeItemsKeys() {
    }

    @Test
    public void process() {
        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);


                int height = 1;

                //CREATE ORDER
                CreateOrderTransaction createOrderTX = new CreateOrderTransaction(maker, key, FEE_KEY,
                        BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        BigDecimal.valueOf(0.1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), FEE_POWER, ++timestamp, 0L);
                TransactionTests.signAndProcess(dcSet, maker, gb, createOrderTX, ++height, 1);
                Order orderOrig = dcSet.getOrderMap().get(createOrderTX.dbRef);
                assertEquals(orderOrig.isActive(), true);

                //CREATE UPDATE ORDER 1
                ChangeOrderTransaction changeOrderTX_1 = new ChangeOrderTransaction(maker, createOrderTX.getSignature(),
                        BigDecimal.TEN, FEE_POWER, ++timestamp, 0L);
                TransactionTests.signAndProcess(dcSet, maker, gb, changeOrderTX_1, ++height, 1);

                orderOrig = dcSet.getCompletedOrderMap().get(createOrderTX.dbRef);
                assertEquals(orderOrig.isCanceled(), true);

                Order orderChanged_1 = dcSet.getOrderMap().get(changeOrderTX_1.dbRef);
                assertEquals(orderChanged_1.isActive(), true);

                //CREATE UPDATE ORDER 2
                ChangeOrderTransaction changeOrderTX_2 = new ChangeOrderTransaction(maker, changeOrderTX_1.getSignature(),
                        BigDecimal.TEN, FEE_POWER, ++timestamp, 0L);
                TransactionTests.signAndProcess(dcSet, maker, gb, changeOrderTX_2, ++height, 1);

                orderOrig = dcSet.getCompletedOrderMap().get(createOrderTX.dbRef);
                assertEquals(orderOrig.isCanceled(), true);

                orderChanged_1 = dcSet.getCompletedOrderMap().get(changeOrderTX_1.dbRef);
                assertEquals(orderChanged_1.isCanceled(), true);

                Order orderChanged_2 = dcSet.getOrderMap().get(changeOrderTX_2.dbRef);
                assertEquals(orderChanged_2.isActive(), true);

            } finally {
                dcSet.close();
            }
        }
    }
}