package org.erachain.core.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetUnique;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IssueAssetSeriesTransactionTest {

    static Logger LOGGER = LoggerFactory.getLogger(IssueAssetSeriesTransactionTest.class.getName());

    ExLink exLink = null;

    int forDeal = Transaction.FOR_NETWORK;

    int[] TESTED_DBS = new int[]{
            IDB.DBS_MAP_DB,
            //IDB.DBS_ROCK_DB
    };

    //Long Transaction.FOR_NETWORK = null;

    int asPack = Transaction.FOR_NETWORK;

    long dbRef = 0L;
    long dbRef2 = 1000L;
    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 1;
    byte[] assetReference = new byte[64];
    byte[] assetReference2 = new byte[64];

    long timestamp;

    byte version = 2;
    byte prop2 = 0;
    byte prop1_backward = org.erachain.core.transaction.TransactionAmount.BACKWARD_MASK;

    byte[] itemAppData = null;
    byte[] itemAppData2 = null;
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
    AssetCls assetUnique;
    AssetCls assetVenture;
    long key = 0;
    RSend rsend;

    byte[] txSign = new byte[64];
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance5;
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value

    private byte[] icon2 = "qwe qweiou sdjk fh".getBytes(StandardCharsets.UTF_8);
    private byte[] image2 = "qwe qweias d;alksd;als dajd lkasj dlou sdjk fh".getBytes(StandardCharsets.UTF_8);

    //CREATE EMPTY MEMORY DATABASE
    private DCSet dcSet;
    private GenesisBlock gb;
    private BlockChain bchain;

    // INIT ASSETS
    private void init(int dbs) {

        assetReference2[0] = (byte) dbs;

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
        BlockChain.ALL_VALID_BEFORE = 0;
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
        key = asset.getKey();

        boolean iconAsURL = true;
        int iconType = AssetCls.MEDIA_TYPE_VIDEO;
        boolean imageAsURL = true;
        int imageType = AssetCls.MEDIA_TYPE_AUDIO;
        Long startDate = System.currentTimeMillis();
        Long stopDate = null;
        itemAppData2 = AssetCls.makeAppData(iconAsURL, iconType, imageAsURL, imageType,
                startDate, stopDate, "tag", null, true, true);
        assetUnique = new AssetUnique(itemAppData2, maker, "NFT", icon2, image2, ".asd..", AssetCls.AS_NON_FUNGIBLE);
        assetUnique.setReference(Crypto.getInstance().digest(assetUnique.toBytes(forDeal, false, false)), dbRef2);

        assetVenture = new AssetVenture(itemAppData, maker, "movable", icon, image, "...", 0, 8, 10L);
        assetVenture.setReference(Crypto.getInstance().digest(assetVenture.toBytes(forDeal, false, false)), dbRef);

        System.arraycopy(assetUnique.getReference(), 0, txSign, 0, 32);
        System.arraycopy(assetVenture.getReference(), 0, txSign, 32, 32);
    }


    @Test
    public void parse() {

        init(IDB.DBS_MAP_DB);

        //CREATE UPDATE ORDER
        IssueAssetSeriesTransaction tx = new IssueAssetSeriesTransaction(maker, null, txSign,
                (AssetVenture) assetVenture, FEE_POWER, timestamp, 0L);
        tx.sign(maker, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES - [228 - 117 = 111 (asset.len)]
        byte[] rawTX = tx.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        //assertEquals(rawTX.length, tx.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            Transaction parsedTX = TransactionFactory.getInstance().parse(rawTX, Transaction.FOR_NETWORK);

            //CHECK INSTANCE
            assertEquals(true, parsedTX instanceof IssueAssetSeriesTransaction);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(tx.getSignature(), parsedTX.getSignature()));

            //CHECK ISSUER
            assertEquals(tx.getCreator().getAddress(), parsedTX.getCreator().getAddress());

            //CHECK ORIG REFERENCE
            assertEquals(Arrays.equals(tx.getOrigAssetRef(), ((IssueAssetSeriesTransaction) parsedTX).getOrigAssetRef()), true);

            //CHECK TIMESTAMP
            assertEquals(tx.getTimestamp(), parsedTX.getTimestamp());

            assertEquals(tx.getItem(), ((IssueAssetSeriesTransaction) parsedTX).getItem());

            assertEquals(Arrays.equals(tx.getItem().getAppData(), ((IssueAssetSeriesTransaction) parsedTX).getItem().getAppData()), true);

            assertEquals(((AssetVenture) tx.getItem()).getQuantity(), ((AssetVenture) ((IssueAssetSeriesTransaction) parsedTX).getItem()).getQuantity());

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

                //CREATE SERIES
                final IssueAssetSeriesTransaction createOrderTX = new IssueAssetSeriesTransaction(maker, null, new byte[64], (AssetVenture) assetVenture, FEE_POWER, timestamp, 0L);
                TransactionTests.signAndProcess(dcSet, maker, null, createOrderTX, ++height, 1);
                Order orderOrig = dcSet.getOrderMap().get(createOrderTX.dbRef);
                assertEquals(orderOrig.isActive(), true);
                orderOrig = dcSet.getCompletedOrderMap().get(createOrderTX.dbRef);
                assertEquals(orderOrig, null);
                assertEquals(orderOrig.isActive(), true);

            } finally {
                dcSet.close();
            }
        }
    }
}