package org.erachain.core.transaction;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetUnique;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.dapp.DApp;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Test;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.Assert.*;

@Slf4j
public class TestRecAsset {

    static Logger LOGGER = LoggerFactory.getLogger(TestRecAsset.class.getName());

    ExLink exLink = null;
    DApp DAPP = null;

    int forDeal = Transaction.FOR_NETWORK;

    int[] TESTED_DBS = new int[]{
            IDB.DBS_MAP_DB,
            IDB.DBS_ROCK_DB};

    //Long Transaction.FOR_NETWORK = null;

    long dbRef = 0L;
    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 1;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();

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
    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance5;
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;
    private BlockChain bchain;

    // INIT ASSETS
    private void init(int dbs) {

        logger.info(" ********** open DBS: " + dbs);

        File tempDir = new File(Settings.getInstance().getDataTempDir());
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        db = DCSet.createEmptyHardDatabaseSet(dbs);
        cntrl = Controller.getInstance();
        cntrl.initBlockChain(db);
        bchain = cntrl.getBlockChain();
        gb = bchain.getGenesisBlock();
        //gb.process(db);

        // FEE FUND
        maker.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        maker.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        maker_1.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);

        asset = new AssetVenture(itemAppData, maker, "aasdasd", icon, image, "asdasda", 1, 8, 50000l);
        // set SCALABLE assets ++
        asset.setReference(Crypto.getInstance().digest(asset.toBytes(forDeal, false, false)), dbRef);
        asset.insertToMap(db, BlockChain.AMOUNT_SCALE_FROM);
        asset.insertToMap(db, 0l);
        key = asset.getKey();

        assetMovable = new AssetVenture(itemAppData, maker, "movable", icon, image, "...", 0, 8, 500l);
        assetMovable.setReference(Crypto.getInstance().digest(assetMovable.toBytes(forDeal, false, false)), dbRef);

    }

    @Test
    public void testScale() {

        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);

                int scalse_in = 5;
                int scalse_asset = 1;
                int scale_default = 8;
                BigDecimal amount_in = BigDecimal.valueOf(12345.123).setScale(scalse_in, BigDecimal.ROUND_HALF_DOWN);
                BigDecimal amount_asset = amount_in.setScale(scalse_asset, BigDecimal.ROUND_HALF_DOWN);
                // TO BASE SCALE
                BigDecimal amount_tx = amount_asset.scaleByPowerOfTen(amount_asset.scale() - scale_default);
                // FROM BASE SCALE to ASSET SCALE
                BigDecimal amount_asset_out = amount_tx.scaleByPowerOfTen(amount_tx.scale() - scalse_asset);


                //CREATE ASSET
                AssetVenture asset = new AssetVenture(itemAppData, maker, "test", icon, image, "strontje", 0, scalse_asset, 10000l);

                //CREATE ISSUE ASSET TRANSACTION
                Transaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, 0l);
                issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 2, 1, true);
                issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);
                asset.insertToMap(db, BlockChain.AMOUNT_SCALE_FROM);

                long assetKey = asset.getKey();

                long timestamp = NTP.getTime();

                Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

                //CREATE VALID ASSET TRANSFER
                RSend assetTransfer = new RSend(maker, FEE_POWER, recipient, assetKey, amount_asset, timestamp, 0l);
                assetTransfer.sign(maker, Transaction.FOR_NETWORK);

                //CONVERT TO BYTES
                byte[] rawAssetTransfer = assetTransfer.toBytes(Transaction.FOR_NETWORK, true);

                //CHECK DATALENGTH
                assertEquals(rawAssetTransfer.length, assetTransfer.getDataLength(Transaction.FOR_NETWORK, true));

                try {
                    //PARSE FROM BYTES
                    RSend parsedAssetTransfer = (RSend) TransactionFactory.getInstance().parse(rawAssetTransfer, Transaction.FOR_NETWORK);

                    //CHECK INSTANCE
                    assertEquals(true, parsedAssetTransfer instanceof RSend);

                    BigDecimal ammountParsed = parsedAssetTransfer.getAmount();
                    parsedAssetTransfer.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
                    BigDecimal ammountParsed_inDC = parsedAssetTransfer.getAmount();

                    assertEquals(ammountParsed_inDC, amount_asset);


                } catch (Exception e) {
                    fail("Exception while parsing transaction.");
                }
            } finally {
                db.close();
            }
        }
    }

    //ISSUE ASSET TRANSACTION

    @Test
    public void validateSignatureIssueAssetTransaction() {

        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);

                //CREATE ASSET
                AssetUnique asset = new AssetUnique(itemAppData, maker, "test", icon, image, "strontje", 0);

                //CREATE ISSUE ASSET TRANSACTION
                Transaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, 0l);
                issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);

                //CHECK IF ISSUE ASSET TRANSACTION IS VALID
                issueAssetTransaction.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
                assertEquals(true, issueAssetTransaction.isSignatureValid(db));

                //INVALID SIGNATURE
                issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0], new byte[64]);

                //CHECK IF ISSUE ASSET IS INVALID
                issueAssetTransaction.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
                assertEquals(false, issueAssetTransaction.isSignatureValid(db));
            } finally {
                db.close();
            }
        }
    }

    @Test
    public void parseIssueAssetTransaction() {

        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);

                //CREATE SIGNATURE
                AssetUnique assetUni = new AssetUnique(itemAppData, maker, "test", icon, image, "strontje", 0);
                LOGGER.info("asset: " + assetUni.getTypeBytes()[0] + ", " + assetUni.getTypeBytes()[1]);
                byte[] rawUni = assetUni.toBytes(forDeal, false, false);
                assertEquals(rawUni.length, assetUni.getDataLength(false));
                assetUni.setReference(new byte[64], dbRef);
                rawUni = assetUni.toBytes(forDeal, true, false);
                assertEquals(rawUni.length, assetUni.getDataLength(true));

                //CREATE SIGNATURE
                AssetVenture asset = new AssetVenture(itemAppData, maker, "test", icon, image, "strontje", 0, 8, 1000l);
                LOGGER.info("asset: " + asset.getTypeBytes()[0] + ", " + asset.getTypeBytes()[1]);
                byte[] raw = asset.toBytes(forDeal, false, false);
                assertEquals(raw.length, asset.getDataLength(false));
                asset.setReference(new byte[64], dbRef);
                raw = asset.toBytes(forDeal, true, false);
                assertEquals(raw.length, asset.getDataLength(true));

                //CREATE ISSUE ASSET TRANSACTION
                IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, 0l);
                issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 0, 0, true);
                issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);

                //CONVERT TO BYTES
                byte[] rawIssueAssetTransaction = issueAssetTransaction.toBytes(Transaction.FOR_NETWORK, true);

                //CHECK DATA LENGTH
                assertEquals(rawIssueAssetTransaction.length, issueAssetTransaction.getDataLength(Transaction.FOR_NETWORK, true));

                try {
                    //PARSE FROM BYTES
                    IssueAssetTransaction parsedIssueAssetTransaction = (IssueAssetTransaction) TransactionFactory.getInstance().parse(rawIssueAssetTransaction, Transaction.FOR_NETWORK);

                    //CHECK INSTANCE
                    assertEquals(true, parsedIssueAssetTransaction instanceof IssueAssetTransaction);

                    //CHECK SIGNATURE
                    assertEquals(true, Arrays.equals(issueAssetTransaction.getSignature(), parsedIssueAssetTransaction.getSignature()));

                    //CHECK ISSUER
                    assertEquals(issueAssetTransaction.getCreator().getAddress(), parsedIssueAssetTransaction.getCreator().getAddress());

                    //CHECK OWNER
                    assertEquals(issueAssetTransaction.getItem().getMaker().getAddress(), parsedIssueAssetTransaction.getItem().getMaker().getAddress());

                    //CHECK NAME
                    assertEquals(issueAssetTransaction.getItem().getName(), parsedIssueAssetTransaction.getItem().getName());

                    //CHECK DESCRIPTION
                    assertEquals(issueAssetTransaction.getItem().getDescription(), parsedIssueAssetTransaction.getItem().getDescription());

                    //CHECK QUANTITY
                    assertEquals(((AssetCls) issueAssetTransaction.getItem()).getQuantity(), ((AssetCls) parsedIssueAssetTransaction.getItem()).getQuantity());

                    //SCALE
                    assertEquals(((AssetCls) issueAssetTransaction.getItem()).getScale(), ((AssetCls) parsedIssueAssetTransaction.getItem()).getScale());

                    //ASSET TYPE
                    assertEquals(((AssetCls) issueAssetTransaction.getItem()).getAssetType(), ((AssetCls) parsedIssueAssetTransaction.getItem()).getAssetType());

                    //CHECK REFERENCE
                    //assertEquals((long)issueAssetTransaction.getReference(), (long)parsedIssueAssetTransaction.getReference());

                    //CHECK TIMESTAMP
                    assertEquals(issueAssetTransaction.getTimestamp(), parsedIssueAssetTransaction.getTimestamp());
                } catch (Exception e) {
                    fail("Exception while parsing transaction.");
                }

                //PARSE TRANSACTION FROM WRONG BYTES
                rawIssueAssetTransaction = new byte[issueAssetTransaction.getDataLength(Transaction.FOR_NETWORK, true)];

                try {
                    //PARSE FROM BYTES
                    TransactionFactory.getInstance().parse(rawIssueAssetTransaction, Transaction.FOR_NETWORK);

                    //FAIL
                    fail("this should throw an exception");
                } catch (Exception e) {
                    //EXCEPTION IS THROWN OK
                }
            } finally {
                db.close();
            }
        }
    }

    //TODO actualize the test
    @Test
    public void processIssueAssetTransaction() {

        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);

                AssetUnique asset = new AssetUnique(itemAppData, maker, "test", icon, image, "strontje", 0);

                //CREATE ISSUE ASSET TRANSACTION
                IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0]);
                issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
                assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

                issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);

                long key = asset.getKey();
                LOGGER.info("asset KEY: " + key);

                //CHECK BALANCE ISSUER
                assertEquals(BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

                //CHECK ASSET EXISTS SENDER
                assertEquals(true, db.getItemAssetMap().contains(key));

                //CHECK ASSET IS CORRECT
                assertEquals(true, Arrays.equals(db.getItemAssetMap().get(key).toBytes(forDeal, true, false), asset.toBytes(forDeal, true, false)));

                //CHECK ASSET BALANCE SENDER
                assertEquals(true, db.getAssetBalanceMap().get(maker.getShortAddressBytes(), key).a.b.compareTo(new BigDecimal(asset.getQuantity())) == 0);

                //CHECK REFERENCE SENDER
                assertEquals((long) issueAssetTransaction.getTimestamp(), (long) maker.getLastTimestamp(db)[0]);
            } finally {
                db.close();
            }
        }
    }

    //TODO actualize the test
    @Test
    public void orphanIssueAssetTransaction() {

        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);

                AssetUnique asset = new AssetUnique(itemAppData, maker, "test", icon, image, "strontje", 0);

                //CREATE ISSUE ASSET TRANSACTION
                IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0]);
                issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
                issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);
                long key = asset.getKey();
                assertEquals(new BigDecimal(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));
                assertEquals((long) issueAssetTransaction.getTimestamp(), (long) maker.getLastTimestamp(db)[0]);

                issueAssetTransaction.orphan(gb, Transaction.FOR_NETWORK);

                //CHECK BALANCE ISSUER
                assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

                //CHECK ASSET EXISTS SENDER
                assertEquals(false, db.getItemAssetMap().contains(key));

                //CHECK ASSET BALANCE SENDER
                assertEquals(0, db.getAssetBalanceMap().get(maker.getShortAddressBytes(), key).a.b.longValue());

                //CHECK REFERENCE SENDER
                //assertEquals(issueAssetTransaction.getReference(), maker.getLastReference(db));
            } finally {
                db.close();
            }
        }
    }

    //TRANSFER ASSET

    @Test
    public void validateSignatureR_Send() {

        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);

                AssetUnique asset = new AssetUnique(itemAppData, maker, "test", icon, image, "strontje", 0);

                //CREATE ISSUE ASSET TRANSACTION
                IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0]);
                issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
                issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);
                long key = asset.getKey();

                //CREATE SIGNATURE
                Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

                //CREATE ASSET TRANSFER
                Transaction assetTransfer = new RSend(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db)[0]);
                assetTransfer.sign(maker, Transaction.FOR_NETWORK);

                //CHECK IF ASSET TRANSFER SIGNATURE IS VALID
                assetTransfer.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
                assertEquals(true, assetTransfer.isSignatureValid(db));

                //INVALID SIGNATURE
                assetTransfer = new RSend(maker, FEE_POWER, recipient, 0, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db)[0]);
                assetTransfer.sign(maker, Transaction.FOR_NETWORK);
                assetTransfer = new RSend(maker, FEE_POWER, recipient, 0, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp + 1, maker.getLastTimestamp(db)[0], assetTransfer.getSignature());

                //CHECK IF ASSET TRANSFER SIGNATURE IS INVALID
                assetTransfer.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
                assertEquals(false, assetTransfer.isSignatureValid(db));
            } finally {
                db.close();
            }
        }
    }

    @Test
    public void validateR_Send() {

        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);

                assertEquals(new BigDecimal("0"), maker.getBalanceUSE(key, db));

                //CREATE ISSUE ASSET TRANSACTION
                IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0]);
                issueAssetTransaction.setDC(db, true);
                issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);
                assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(Transaction.FOR_NETWORK,
                        txFlags | Transaction.NOT_VALIDATE_FLAG_PUBLIC_TEXT));

                issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);
                long key = asset.getKey();
                assertEquals(new BigDecimal(asset.getQuantity()), maker.getBalanceUSE(key, db));

                //CREATE SIGNATURE
                Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

                //CREATE VALID ASSET TRANSFER
                Transaction assetTransfer = new RSend(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp + 100, maker.getLastTimestamp(db)[0]);
                assetTransfer.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
                assetTransfer.sign(maker, Transaction.FOR_NETWORK);

                //CHECK IF ASSET TRANSFER IS VALID
                assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, txFlags));

                assetTransfer.process(gb, Transaction.FOR_NETWORK);

                //CREATE VALID ASSET TRANSFER
                //maker.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), db);
                assetTransfer = new RSend(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp + 200, maker.getLastTimestamp(db)[0]);
                assetTransfer.setDC(db, true);
                assetTransfer.sign(maker, Transaction.FOR_NETWORK);

                //CHECK IF ASSET TRANSFER IS VALID
                assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, txFlags));

                //CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
                assetTransfer = new RSend(maker, FEE_POWER, recipient, 0, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db)[0]);
                assetTransfer.setDC(db, Transaction.FOR_NETWORK, BlockChain.ALL_BALANCES_OK_TO + 1, 1, true);
                assetTransfer.sign(maker, Transaction.FOR_NETWORK);

                //CHECK IF ASSET TRANSFER IS INVALID
                assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, txFlags));

            } finally {
                db.close();
            }
        }
    }

    @Test
    public void parseR_Send() {

        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);

                //CREATE SIGNATURE
                Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
                long timestamp = NTP.getTime();

                //CREATE VALID ASSET TRANSFER
                RSend assetTransfer = new RSend(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db)[0]);
                assetTransfer.sign(maker, Transaction.FOR_NETWORK);

                //CONVERT TO BYTES
                byte[] rawAssetTransfer = assetTransfer.toBytes(Transaction.FOR_NETWORK, true);

                //CHECK DATALENGTH
                assertEquals(rawAssetTransfer.length, assetTransfer.getDataLength(Transaction.FOR_NETWORK, true));

                try {
                    //PARSE FROM BYTES
                    RSend parsedAssetTransfer = (RSend) TransactionFactory.getInstance().parse(rawAssetTransfer, Transaction.FOR_NETWORK);

                    //CHECK INSTANCE
                    assertEquals(true, parsedAssetTransfer instanceof RSend);

                    //CHECK TYPEBYTES
                    assertEquals(true, Arrays.equals(assetTransfer.getTypeBytes(), parsedAssetTransfer.getTypeBytes()));

                    //CHECK TIMESTAMP
                    assertEquals(assetTransfer.getTimestamp(), parsedAssetTransfer.getTimestamp());

                    //CHECK REFERENCE
                    assertEquals(assetTransfer.getExtFlags(), parsedAssetTransfer.getExtFlags());

                    //CHECK CREATOR
                    assertEquals(assetTransfer.getCreator().getAddress(), parsedAssetTransfer.getCreator().getAddress());

                    //CHECK FEE POWER
                    assertEquals(assetTransfer.getFeePow(), parsedAssetTransfer.getFeePow());

                    //CHECK SIGNATURE
                    assertEquals(true, Arrays.equals(assetTransfer.getSignature(), parsedAssetTransfer.getSignature()));

                    //CHECK KEY
                    assertEquals(assetTransfer.getKey(), parsedAssetTransfer.getKey());

                    //CHECK AMOUNT
                    assertEquals(assetTransfer.getAmount(maker), parsedAssetTransfer.getAmount(maker));

                    //CHECK AMOUNT RECIPIENT
                    assertEquals(assetTransfer.getAmount(recipient), parsedAssetTransfer.getAmount(recipient));

                } catch (Exception e) {
                    fail("Exception while parsing transaction." + e);
                }

                //PARSE TRANSACTION FROM WRONG BYTES
                rawAssetTransfer = new byte[assetTransfer.getDataLength(Transaction.FOR_MYPACK, true)];

                try {
                    //PARSE FROM BYTES
                    TransactionFactory.getInstance().parse(rawAssetTransfer, Transaction.FOR_NETWORK);

                    //FAIL
                    fail("this should throw an exception");
                } catch (Exception e) {
                    //EXCEPTION IS THROWN OK
                }
            } finally {
                db.close();
            }
        }
    }

    @Test
    public void processR_Send() {

        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);

                //CREATE SIGNATURE
                Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
                long timestamp = NTP.getTime();

                //CREATE ASSET TRANSFER
                maker.changeBalance(db, false, false, key, BigDecimal.valueOf(200).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
                Transaction assetTransfer = new RSend(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db)[0]);
                assetTransfer.sign(maker, Transaction.FOR_NETWORK);
                assetTransfer.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
                assetTransfer.isValid(Transaction.FOR_NETWORK, txFlags);
                assetTransfer.process(gb, Transaction.FOR_NETWORK);

                //CHECK BALANCE SENDER
                assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

                //CHECK BALANCE RECIPIENT
                assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

                //CHECK REFERENCE SENDER
                assertEquals(assetTransfer.getTimestamp(), maker.getLastTimestamp(db));

                //CHECK REFERENCE RECIPIENT
                assertNotEquals(assetTransfer.getTimestamp(), recipient.getLastTimestamp(db));
            } finally {
                db.close();
            }
        }
    }

    //TODO actualize the test
    @Test
    public void orphanR_Send() {

        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);

                //CREATE SIGNATURE
                Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
                long timestamp = NTP.getTime();

                //CREATE ASSET TRANSFER
                long key = 1l;
                maker.changeBalance(db, false, false, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
                Transaction assetTransfer = new RSend(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db)[0]);
                assetTransfer.sign(maker, Transaction.FOR_NETWORK);
                assetTransfer.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
                assetTransfer.process(gb, Transaction.FOR_NETWORK);
                assetTransfer.orphan(gb, Transaction.FOR_NETWORK);

                //CHECK BALANCE SENDER
                assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

                //CHECK BALANCE RECIPIENT
                assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

                //CHECK REFERENCE SENDER
                //assertEquals(assetTransfer.getReference(), maker.getLastReference(db));

                //CHECK REFERENCE RECIPIENT
                assertNotEquals(assetTransfer.getTimestamp(), recipient.getLastTimestamp(db));
            } finally {
                db.close();
            }
        }
    }

    //MESSAGE ASSET

    @Test
    public void validateSignatureMessageTransaction() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                //AssetUnique asset = new AssetUnique(maker, "test", "strontje");

                //CREATE ISSUE ASSET TRANSACTION
                IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db)[0]);
                issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);
                issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
                issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);
                long key = asset.getKey();

                //CREATE SIGNATURE
                Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

                //CREATE ASSET TRANSFER
                Transaction messageTransaction = new RSend(maker, exLink, DAPP, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp, maker.getLastTimestamp(db)[0]);
                messageTransaction.sign(maker, Transaction.FOR_NETWORK);

                //CHECK IF ASSET TRANSFER SIGNATURE IS VALID
                messageTransaction.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
                assertEquals(true, messageTransaction.isSignatureValid(db));

                //INVALID SIGNATURE
                messageTransaction = new RSend(maker, exLink, DAPP, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp, maker.getLastTimestamp(db)[0]);
                messageTransaction.sign(maker, Transaction.FOR_NETWORK);
                messageTransaction = new RSend(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp + 1, maker.getLastTimestamp(db)[0], messageTransaction.getSignature());

                //CHECK IF ASSET TRANSFER SIGNATURE IS INVALID
                messageTransaction.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
                assertEquals(false, messageTransaction.isSignatureValid(db));

            } finally {
                db.close();
            }

        }
    }

    @Test
    public void validateMessageTransaction() {

        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);

                //CREATE ISSUE ASSET TRANSACTION
                IssueAssetTransaction issueMessageTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp++, maker.getLastTimestamp(db)[0]);
                assertEquals(Transaction.VALIDATE_OK, issueMessageTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

                issueMessageTransaction.sign(maker, Transaction.FOR_NETWORK);
                issueMessageTransaction.process(gb, Transaction.FOR_NETWORK);
                long key = asset.getKey();
                //assertEquals(asset.getQuantity(), maker.getConfirmedBalance(FEE_KEY, db));
                assertEquals(new BigDecimal(asset.getQuantity()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

                //CREATE SIGNATURE
                Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

                //timestamp += 100;
                //CREATE VALID ASSET TRANSFER
                Transaction messageTransaction = new RSend(maker, exLink, DAPP, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp++, maker.getLastTimestamp(db)[0]);

                //CHECK IF ASSET TRANSFER IS VALID
                assertEquals(Transaction.VALIDATE_OK, messageTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

                messageTransaction.sign(maker, Transaction.FOR_NETWORK);
                messageTransaction.process(gb, Transaction.FOR_NETWORK);
                timestamp++;

                //CREATE VALID ASSET TRANSFER
                //maker.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), db);
                messageTransaction = new RSend(maker, exLink, DAPP, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp++, maker.getLastTimestamp(db)[0]);

                //CHECK IF ASSET TRANSFER IS VALID
                assertEquals(Transaction.VALIDATE_OK, messageTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

                //CREATE INVALID ASSET TRANSFER INVALID RECIPIENT ADDRESS
                messageTransaction = new RSend(maker, exLink, DAPP, FEE_POWER, new Account("test"), key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp++, maker.getLastTimestamp(db)[0]);

                //CHECK IF ASSET TRANSFER IS INVALID
                assertEquals(Transaction.INVALID_ADDRESS, messageTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

                //CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
                messageTransaction = new RSend(maker, exLink, DAPP, FEE_POWER, recipient, key, BigDecimal.valueOf(-100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp++, maker.getLastTimestamp(db)[0]);

                //CHECK IF ASSET TRANSFER IS INVALID
                assertEquals(Transaction.NOT_MOVABLE_ASSET, messageTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

                //CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
                messageTransaction = new RSend(maker, exLink, DAPP, FEE_POWER, recipient, 99, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp++, maker.getLastTimestamp(db)[0]);

                //CHECK IF ASSET TRANSFER IS INVALID
                assertEquals(Transaction.ITEM_ASSET_NOT_EXIST, messageTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

                //CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
                messageTransaction = new RSend(maker, exLink, DAPP, FEE_POWER, recipient, key - 1, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp++, maker.getLastTimestamp(db)[0]);

                //CHECK IF ASSET TRANSFER IS INVALID
                assertEquals(Transaction.NO_BALANCE, messageTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

                //CREATE INVALID ASSET TRANSFER WRONG REFERENCE
                messageTransaction = new RSend(maker, exLink, DAPP, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp++, -123L);

                //CHECK IF ASSET TRANSFER IS INVALID
                assertEquals(Transaction.INVALID_REFERENCE, messageTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

                // NOT DIVISIBLE
                asset = new AssetVenture(itemAppData, maker, "not divisible", icon, image, "asdasda", 0, 8, 0l);
                IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp++, maker.getLastTimestamp(db)[0]);
                assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(Transaction.FOR_NETWORK, txFlags));
                issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);
                issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);
                Long key_1 = issueAssetTransaction.getAssetKey();
                assertEquals(key + 1, (long) key_1);
                assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key_1, db));

                BigDecimal amo = BigDecimal.TEN.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
                //CREATE INVALID ASSET TRANSFER WRONG REFERENCE
                messageTransaction = new RSend(maker, exLink, DAPP, FEE_POWER, recipient, key_1,
                        amo,
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp++, maker.getLastTimestamp(db)[0]);
                assertEquals(Transaction.VALIDATE_OK, messageTransaction.isValid(Transaction.FOR_NETWORK, txFlags));
                messageTransaction.process(gb, Transaction.FOR_NETWORK);

                //CHECK IF UNLIMITED ASSET TRANSFERED with no balance
                assertEquals(BigDecimal.ZERO.subtract(amo), maker.getBalanceUSE(key_1, db));

                // TRY INVALID SEND FRON NOT CREATOR

                messageTransaction = new RSend(maker_1, exLink, DAPP, FEE_POWER, recipient, key_1,
                        amo,
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp++, maker_1.getLastTimestamp(db)[0]);
                assertEquals(Transaction.NO_BALANCE, messageTransaction.isValid(Transaction.FOR_NETWORK, txFlags));

                //CHECK IF UNLIMITED ASSET TRANSFERED with no balance
                assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker_1.getBalanceUSE(key_1, db));

            } finally {
                db.close();
            }
        }
    }

    @Test
    public void parseMessageTransaction() {

        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);

                //CREATE SIGNATURE
                Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
                long timestamp = NTP.getTime();

                //CREATE VALID ASSET TRANSFER
                RSend r_Send = new RSend(maker, exLink, DAPP, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp, maker.getLastTimestamp(db)[0]);
                r_Send.sign(maker, Transaction.FOR_NETWORK);

                //CONVERT TO BYTES
                byte[] rawAssetTransfer = r_Send.toBytes(Transaction.FOR_NETWORK, true);

                //CHECK DATALENGTH
                assertEquals(rawAssetTransfer.length, r_Send.getDataLength(Transaction.FOR_NETWORK, true));

                try {
                    //PARSE FROM BYTES
                    RSend parsedAssetTransfer = (RSend) TransactionFactory.getInstance().parse(rawAssetTransfer, Transaction.FOR_NETWORK);

                    //CHECK INSTANCE
                    assertEquals(true, parsedAssetTransfer instanceof RSend);

                    //CHECK TYPEBYTES
                    assertEquals(true, Arrays.equals(r_Send.getTypeBytes(), parsedAssetTransfer.getTypeBytes()));

                    //CHECK TIMESTAMP
                    assertEquals(r_Send.getTimestamp(), parsedAssetTransfer.getTimestamp());

                    //CHECK REFERENCE
                    //assertEquals(r_Send.getReference(), parsedAssetTransfer.getReference());

                    //CHECK CREATOR
                    assertEquals(r_Send.getCreator().getAddress(), parsedAssetTransfer.getCreator().getAddress());

                    //CHECK FEE POWER
                    assertEquals(r_Send.getFeePow(), parsedAssetTransfer.getFeePow());

                    //CHECK SIGNATURE
                    assertEquals(true, Arrays.equals(r_Send.getSignature(), parsedAssetTransfer.getSignature()));

                    //CHECK KEY
                    assertEquals(r_Send.getKey(), parsedAssetTransfer.getKey());

                    //CHECK AMOUNT
                    assertEquals(r_Send.getAmount(maker), parsedAssetTransfer.getAmount(maker));

                    //CHECK AMOUNT RECIPIENT
                    assertEquals(r_Send.getAmount(recipient), parsedAssetTransfer.getAmount(recipient));

                } catch (Exception e) {
                    fail("Exception while parsing transaction." + e);
                }

                //PARSE TRANSACTION FROM WRONG BYTES
                rawAssetTransfer = new byte[r_Send.getDataLength(Transaction.FOR_NETWORK, true)];

                try {
                    //PARSE FROM BYTES
                    TransactionFactory.getInstance().parse(rawAssetTransfer, Transaction.FOR_NETWORK);

                    //FAIL
                    fail("this should throw an exception");
                } catch (Exception e) {
                    //EXCEPTION IS THROWN OK
                }
            } finally {
                db.close();
            }
        }
    }

    //@Ignore Руслан тут опнаставил Игноров везде
///////TODO actualize the test
    @Test
    public void processMessageTransaction() {

        for (int dbs : TESTED_DBS) {

            try {

                init(dbs);

                //CREATE SIGNATURE
                Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
                long timestamp = NTP.getTime();

                //CREATE ASSET TRANSFER
                maker.changeBalance(db, false, false, key, BigDecimal.valueOf(200).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
                assertEquals(BigDecimal.valueOf(200).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));
                assertEquals(BigDecimal.ZERO, recipient.getBalanceUSE(key, db));


                Transaction messageTransaction = new RSend(maker, exLink, DAPP, FEE_POWER, recipient, key, BigDecimal.valueOf(50).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp, maker.getLastTimestamp(db)[0]);
                messageTransaction.sign(maker, Transaction.FOR_NETWORK);
                messageTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
                messageTransaction.process(gb, Transaction.FOR_NETWORK);

                //CHECK BALANCE SENDER
                assertEquals(BigDecimal.valueOf(150).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

                assertEquals(BigDecimal.valueOf(50).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

                //CHECK REFERENCE SENDER
                assertEquals((long) messageTransaction.getTimestamp(), maker.getLastTimestamp(db)[0]);

            } finally {
                db.close();
            }
        }
    }

    @Test
    public void orphanMessageTransaction() {

        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                //CREATE SIGNATURE
                Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
                long timestamp = NTP.getTime();


                //CREATE ASSET TRANSFER
                BigDecimal amountSend = BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
                BigDecimal bal = maker.getBalanceUSE(key, db);

                maker.changeBalance(db, false, false, key, amountSend, false, false, false);
                Transaction messageTransaction = new RSend(maker, exLink, DAPP, FEE_POWER, recipient, key, amountSend,
                        "headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
                        timestamp, maker.getLastTimestamp(db)[0]);
                messageTransaction.sign(maker, Transaction.FOR_NETWORK);
                messageTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
                messageTransaction.process(gb, Transaction.FOR_NETWORK);
                messageTransaction.orphan(gb, Transaction.FOR_NETWORK);

                //CHECK BALANCE SENDER
                assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

                //CHECK BALANCE RECIPIENT
                assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

                //CHECK REFERENCE RECIPIENT
                assertNotEquals(messageTransaction.getTimestamp(), recipient.getLastTimestamp(db));
            } finally {
                db.close();
            }
        }
    }
}
