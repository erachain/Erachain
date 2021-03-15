package org.erachain.records;

import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetUnique;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.transaction.*;
import org.erachain.datachain.DCSet;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TransactionTests3AssetsAsPack {

    static Logger LOGGER = LoggerFactory.getLogger(TransactionTests3AssetsAsPack.class.getName());
    static int asPack = Transaction.FOR_NETWORK;
    //Long Transaction.FOR_NETWORK;

    long dbRef = 0L;

    long FEE_KEY = 1l;
    byte FEE_POWER = (byte) 1;
    byte[] assetReference = new byte[64];
    long timestamp = 0l;

    long flags = 0l;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    AssetCls asset;
    long key = -1;
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value

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
        maker.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        asset = new AssetVenture(maker, "a", icon, image, "a", 0, 8, 50000l);
        //key = asset.getKey();
        
    }


    //ISSUE ASSET TRANSACTION

    @Ignore
    //TODO actualize the test
    @Test
    public void validateSignatureIssueAssetTransaction() {

        init();

        //CREATE ASSET
        AssetUnique asset = new AssetUnique(maker, "test", icon, image, "strontje", 0);

        //CREATE ISSUE ASSET TRANSACTION
        Transaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, 0l);
        issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(Transaction.FOR_NETWORK, flags));

        issueAssetTransaction.sign(maker, asPack);

        //CHECK IF ISSUE ASSET TRANSACTION IS VALID
        assertEquals(true, issueAssetTransaction.isSignatureValid(db));

        //INVALID SIGNATURE
        issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, 0l, new byte[64]);

        //CHECK IF ISSUE ASSET IS INVALID
        assertEquals(false, issueAssetTransaction.isSignatureValid(db));
    }

    @Ignore
    //TODO actualize the test
    @Test
    public void parseIssueAssetTransaction() {

        init();

        //CREATE SIGNATURE
        AssetUnique asset = new AssetUnique(maker, "test", icon, image, "strontje", 0);
        LOGGER.info("asset: " + asset.getTypeBytes()[0] + ", " + asset.getTypeBytes()[1]);
        boolean includeReference = false;
        byte[] raw = asset.toBytes(includeReference, false);
        assertEquals(raw.length, asset.getDataLength(includeReference));

        asset.setReference(new byte[64], dbRef);
        raw = asset.toBytes(true, false);
        assertEquals(raw.length, asset.getDataLength(true));

        //CREATE ISSUE ASSET TRANSACTION
        IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, 0l);
        issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);
        issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);

        //CONVERT TO BYTES
        byte[] rawIssueAssetTransaction = issueAssetTransaction.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawIssueAssetTransaction.length, issueAssetTransaction.getDataLength(asPack, true));

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

            //CHECK FEE
            assertEquals(issueAssetTransaction.getFee(), parsedIssueAssetTransaction.getFee());

            //CHECK REFERENCE
            //assertEquals(issueAssetTransaction.getReference(), parsedIssueAssetTransaction.getReference());

            //CHECK TIMESTAMP
            assertEquals(issueAssetTransaction.getTimestamp(), parsedIssueAssetTransaction.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

        //PARSE TRANSACTION FROM WRONG BYTES
        rawIssueAssetTransaction = new byte[issueAssetTransaction.getDataLength(asPack, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawIssueAssetTransaction, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }

    @Ignore
    //TODO actualize the test
    @Test
    public void processIssueAssetTransaction() {

        init();

        AssetUnique asset = new AssetUnique(maker, "test", icon, image, "strontje", 0);

        //CREATE ISSUE ASSET TRANSACTION
        IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, 0l);
        issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issueAssetTransaction.sign(maker, asPack);

        assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(Transaction.FOR_NETWORK, flags));

        issueAssetTransaction.process(gb, asPack);

        LOGGER.info("asset KEY: " + asset.getKey(db));

        //CHECK BALANCE ISSUER
        assertEquals(BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(asset.getKey(db), db));

        //CHECK ASSET EXISTS SENDER
        long key = db.getIssueAssetMap().get(issueAssetTransaction);
        assertEquals(true, db.getItemAssetMap().contains(key));

        //CHECK ASSET IS CORRECT
        assertEquals(true, Arrays.equals(db.getItemAssetMap().get(key).toBytes(true, false), asset.toBytes(true, false)));

        //CHECK ASSET BALANCE SENDER
        assertEquals(true, db.getAssetBalanceMap().get(maker.getShortAddressBytes(), key).a.b.compareTo(new BigDecimal(asset.getQuantity())) == 0);

        //CHECK REFERENCE SENDER
        assertEquals(issueAssetTransaction.getSignature(), Transaction.FOR_NETWORK);
    }

    @Ignore
    //TODO actualize the test
    @Test
    public void orphanIssueAssetTransaction() {

        init();

        AssetUnique asset = new AssetUnique(maker, "test", icon, image, "strontje", 0);

        //CREATE ISSUE ASSET TRANSACTION
        IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, 0l);
        issueAssetTransaction.sign(maker, asPack);
        issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issueAssetTransaction.process(gb, asPack);
        long key = db.getIssueAssetMap().get(issueAssetTransaction);
        assertEquals(new BigDecimal(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));
        assertEquals(issueAssetTransaction.getSignature(), Transaction.FOR_NETWORK);

        issueAssetTransaction.orphan(gb, asPack);

        //CHECK BALANCE ISSUER
        assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

        //CHECK ASSET EXISTS SENDER
        assertEquals(false, db.getItemAssetMap().contains(key));

        //CHECK ASSET BALANCE SENDER
        assertEquals(0, db.getAssetBalanceMap().get(maker.getShortAddressBytes(), key).a.b.longValue());

        //CHECK REFERENCE SENDER
        //assertEquals(issueAssetTransaction.getReference(), Transaction.FOR_NETWORK);
    }


    //TRANSFER ASSET

    @Test
    public void validateSignatureR_Send() {

        init();

        AssetUnique asset = new AssetUnique(maker, "test", icon, image, "strontje", 0);

        //CREATE ISSUE ASSET TRANSACTION
        IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, 0l);
        issueAssetTransaction.sign(maker, asPack);
        issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issueAssetTransaction.process(gb, asPack);
        long key = db.getIssueAssetMap().get(issueAssetTransaction);

        //CREATE SIGNATURE
        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

        //CREATE ASSET TRANSFER
        Transaction assetTransfer = new RSend(maker, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), 0l);
        assetTransfer.sign(maker, asPack);

        //CHECK IF ASSET TRANSFER SIGNATURE IS VALID
        assertEquals(true, assetTransfer.isSignatureValid(db));

        //INVALID SIGNATURE
        assetTransfer = new RSend(maker, recipient, 0, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), 0l);
        assetTransfer.sign(maker, asPack);
        assetTransfer = new RSend(maker, recipient, 0, BigDecimal.valueOf(101).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), -123L);

        //CHECK IF ASSET TRANSFER SIGNATURE IS INVALID
        assertEquals(false, assetTransfer.isSignatureValid(db));
    }

    @Ignore
    //TODO actualize the test
    @Test
    public void validateR_Send() {

        init();

        //CREATE ISSUE ASSET TRANSACTION
        IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, 0l);
        issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issueAssetTransaction.sign(maker, asPack);
        assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(Transaction.FOR_NETWORK, flags));

        issueAssetTransaction.process(gb, asPack);
        long key = asset.getKey(db);
        //assertEquals(asset.getQuantity(), maker.getConfirmedBalance(FEE_KEY, db));
        assertEquals(new BigDecimal(asset.getQuantity()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

        //CREATE SIGNATURE
        Account recipient = new Account("QgcphUTiVHHfHg8e1LVgg5jujVES7ZDUTr");

        //CREATE VALID ASSET TRANSFER
        Transaction assetTransfer = new RSend(maker, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), 0l);
        assetTransfer.sign(maker, asPack);

        //CHECK IF ASSET TRANSFER IS VALID
        assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, flags));

        assetTransfer.process(gb, asPack);

        //CREATE VALID ASSET TRANSFER
        //maker.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), db);
        assetTransfer = new RSend(maker, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), 0l);

        //CHECK IF ASSET TRANSFER IS VALID
        assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, flags));

        //CREATE INVALID ASSET TRANSFER INVALID RECIPIENT ADDRESS
        assetTransfer = new RSend(maker, new Account("test"), key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), 0l);

        //CHECK IF ASSET TRANSFER IS INVALID
        assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, flags));

        //CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
        assetTransfer = new RSend(maker, recipient, key, BigDecimal.valueOf(-100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), 0l);

        //CHECK IF ASSET TRANSFER IS INVALID
        assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, flags));

        //CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
        assetTransfer = new RSend(maker, recipient, 0, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), 0l);
        assetTransfer.sign(maker, asPack);
        assetTransfer.process(gb, asPack);

        //CHECK IF ASSET TRANSFER IS INVALID
        assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, flags));

    }

    @Ignore
    //TODO actualize the test
    @Test
    public void parseR_Send() {

        init();

        //CREATE SIGNATURE
        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

        //CREATE VALID ASSET TRANSFER
        RSend assetTransfer = new RSend(maker, recipient, 0, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), 0l);
        assetTransfer.sign(maker, asPack);

        //CONVERT TO BYTES
        byte[] rawAssetTransfer = assetTransfer.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATALENGTH
        assertEquals(rawAssetTransfer.length, assetTransfer.getDataLength(asPack, true));

        try {
            //PARSE FROM BYTES
            RSend parsedAssetTransfer = (RSend) TransactionFactory.getInstance().parse(rawAssetTransfer, asPack);

            //CHECK INSTANCE
            assertEquals(true, parsedAssetTransfer instanceof RSend);

            //CHECK TYPEBYTES
            assertEquals(true, Arrays.equals(assetTransfer.getTypeBytes(), parsedAssetTransfer.getTypeBytes()));

            //CHECK CREATOR
            assertEquals(assetTransfer.getCreator().getAddress(), parsedAssetTransfer.getCreator().getAddress());

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(assetTransfer.getSignature(), parsedAssetTransfer.getSignature()));

            //CHECK KEY
            assertEquals(assetTransfer.getKey(), parsedAssetTransfer.getKey());

            //CHECK AMOUNT
            assertEquals(assetTransfer.getAmount(maker), parsedAssetTransfer.getAmount(maker));

            //CHECK AMOUNT RECIPIENT
            assertEquals(assetTransfer.getAmount(recipient), parsedAssetTransfer.getAmount(recipient));

        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

        //PARSE TRANSACTION FROM WRONG BYTES
        rawAssetTransfer = new byte[assetTransfer.getDataLength(asPack, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawAssetTransfer, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }

    @Ignore
    //TODO actualize the test
    @Test
    public void processR_Send() {

        init();

        //CREATE SIGNATURE
        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
        //Long maker_LastReference = Transaction.FOR_NETWORK;
        Long recipient_LastReference = recipient.getLastTimestamp(db)[0];

        //CREATE ASSET TRANSFER
        long key = 221;
        maker.changeBalance(db, false, false, key, BigDecimal.valueOf(200).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        Transaction assetTransfer = new RSend(maker, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), 0l);
        assetTransfer.sign(maker, asPack);
        assetTransfer.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assetTransfer.process(gb, asPack);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

        //CHECK BALANCE RECIPIENT
        assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

        //CHECK REFERENCE RECIPIENT
        assertEquals(recipient_LastReference, recipient.getLastTimestamp(db));
    }

    @Ignore
    //TODO actualize the test
    @Test
    public void orphanR_Send() {

        init();

        //CREATE SIGNATURE
        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
        //Long maker_LastReference = Transaction.FOR_NETWORK;
        Long recipient_LastReference = recipient.getLastTimestamp(db)[0];

        //CREATE ASSET TRANSFER
        long key = 1l;
        maker.changeBalance(db, false, false, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        Transaction assetTransfer = new RSend(maker, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), 0l);
        assetTransfer.sign(maker, asPack);
        assetTransfer.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assetTransfer.process(gb, asPack);
        assetTransfer.orphan(gb, asPack);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

        //CHECK BALANCE RECIPIENT
        assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

        //CHECK REFERENCE RECIPIENT
        assertEquals(recipient_LastReference, recipient.getLastTimestamp(db));
    }


    //CANCEL ORDER

    @Test
    public void validateSignatureCancelOrderTransaction() {


        init();

        //CREATE ORDER CANCEL
        Transaction cancelOrderTransaction = new CancelOrderTransaction(maker, new byte[10], FEE_POWER, timestamp, 0l);
        cancelOrderTransaction.sign(maker, asPack);
        //CHECK IF ORDER CANCEL IS VALID
        assertEquals(true, cancelOrderTransaction.isSignatureValid(db));

        //INVALID SIGNATURE
        cancelOrderTransaction = new CancelOrderTransaction(maker, new byte[10], FEE_POWER, timestamp, 0l, new byte[1]);

        //CHECK IF ORDER CANCEL
        assertEquals(false, cancelOrderTransaction.isSignatureValid(db));
    }
    @Ignore
    //TODO actualize the test
    @Test
    public void validateCancelOrderTransaction() {

        init();

        //CREATE ISSUE ASSET TRANSACTION
        Transaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, System.currentTimeMillis(), 0l, new byte[64]);
        issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issueAssetTransaction.sign(maker, asPack);
        issueAssetTransaction.process(gb, asPack);
        //logger.info("IssueAssetTransaction .creator.getBalance(1, db): " + account.getBalance(1, dcSet));
        key = asset.getKey(db);

        //CREATE ORDER
        CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(maker, key, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), BigDecimal.valueOf(0.1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), FEE_POWER, System.currentTimeMillis(), 0l, new byte[]{5, 6});
        createOrderTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        createOrderTransaction.sign(maker, asPack);
        createOrderTransaction.process(gb, asPack);

        //this.creator.getBalance(1, db).compareTo(this.fee) == -1)
        //logger.info("createOrderTransaction.creator.getBalance(1, db): " + createOrderTransaction.getCreator().getBalance(1, dcSet));
        //logger.info("CreateOrderTransaction.creator.getBalance(1, db): " + account.getBalance(1, dcSet));

        //CREATE CANCEL ORDER
        CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(maker, new byte[]{5, 6}, FEE_POWER, System.currentTimeMillis(), 0l, new byte[]{1, 2});
        //CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(account, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), account.getLastReference(dcSet));
        //cancelOrderTransaction.sign(account);
        //CHECK IF CANCEL ORDER IS VALID
        cancelOrderTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        assertEquals(Transaction.VALIDATE_OK, cancelOrderTransaction.isValid(Transaction.FOR_NETWORK, flags));

        //CREATE INVALID CANCEL ORDER ORDER DOES NOT EXIST
        cancelOrderTransaction = new CancelOrderTransaction(maker, new byte[]{5, 7}, FEE_POWER, System.currentTimeMillis(), 0l, new byte[]{1, 2});

        //CHECK IF CANCEL ORDER IS INVALID
        assertEquals(Transaction.ORDER_DOES_NOT_EXIST, cancelOrderTransaction.isValid(Transaction.FOR_NETWORK, flags));

        //CREATE INVALID CANCEL ORDER INCORRECT CREATOR
        seed = Crypto.getInstance().digest("invalid".getBytes());
        privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        PrivateKeyAccount invalidCreator = new PrivateKeyAccount(privateKey);
        cancelOrderTransaction = new CancelOrderTransaction(invalidCreator, new byte[]{5, 6}, FEE_POWER, System.currentTimeMillis(), 0l, new byte[]{1, 2});

        //CHECK IF CANCEL ORDER IS INVALID
        assertEquals(Transaction.INVALID_ORDER_CREATOR, cancelOrderTransaction.isValid(Transaction.FOR_NETWORK, flags));

        //CREATE INVALID CANCEL ORDER NO BALANCE
        DCSet fork = db.fork(this.toString());
        cancelOrderTransaction = new CancelOrderTransaction(maker, new byte[]{5, 6}, FEE_POWER, System.currentTimeMillis(), 0l, new byte[]{1, 2});
        maker.changeBalance(fork, false, false, FEE_KEY, BigDecimal.ZERO, false, false, false);

        //CHECK IF CANCEL ORDER IS INVALID
        assertEquals(Transaction.NOT_ENOUGH_FEE, cancelOrderTransaction.isValid(Transaction.FOR_NETWORK, flags));

        //CREATE CANCEL ORDER INVALID REFERENCE
        cancelOrderTransaction = new CancelOrderTransaction(maker, new byte[]{5, 6}, FEE_POWER, System.currentTimeMillis(), -123L, new byte[]{1, 2});

        //CHECK IF NAME REGISTRATION IS INVALID
        assertEquals(Transaction.INVALID_REFERENCE, cancelOrderTransaction.isValid(Transaction.FOR_NETWORK, flags));

    }

    @Ignore
    //TODO actualize the test
    @Test
    public void parseCancelOrderTransaction() {


        init();

        //CREATE CANCEL ORDER
        CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(maker, new byte[11], FEE_POWER, timestamp, 0l);
        cancelOrderTransaction.sign(maker, asPack);

        //CONVERT TO BYTES
        byte[] rawCancelOrder = cancelOrderTransaction.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATALENGTH
        assertEquals(rawCancelOrder.length, cancelOrderTransaction.getDataLength(asPack, true));

        try {
            //PARSE FROM BYTES
            CancelOrderTransaction parsedCancelOrder = (CancelOrderTransaction) TransactionFactory.getInstance().parse(rawCancelOrder, asPack);

            //CHECK INSTANCE
            assertEquals(true, parsedCancelOrder instanceof CancelOrderTransaction);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(cancelOrderTransaction.getSignature(), parsedCancelOrder.getSignature()));

            //CHECK AMOUNT CREATOR
            assertEquals(cancelOrderTransaction.getAmount(maker), parsedCancelOrder.getAmount(maker));

            //CHECK OWNER
            assertEquals(cancelOrderTransaction.getCreator().getAddress(), parsedCancelOrder.getCreator().getAddress());

            //CHECK ORDER
            assertEquals(0, cancelOrderTransaction.getOrderID().compareTo(parsedCancelOrder.getOrderID()));

            //CHECK FEE
            assertEquals(cancelOrderTransaction.getFee(), parsedCancelOrder.getFee());

            //CHECK REFERENCE
            //assertEquals(cancelOrderTransaction.getReference(), parsedCancelOrder.getReference());

            //CHECK TIMESTAMP
            assertEquals(cancelOrderTransaction.getTimestamp(), parsedCancelOrder.getTimestamp());
        } catch (Exception e) {
            fail("Exception while parsing transaction.");
        }

        //PARSE TRANSACTION FROM WRONG BYTES
        rawCancelOrder = new byte[cancelOrderTransaction.getDataLength(asPack, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawCancelOrder, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }

    @Ignore
    @Test
    public void processCancelOrderTransaction() {

        init();

        //CREATE ASSET

        //CREATE ISSUE ASSET TRANSACTION
        Transaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, System.currentTimeMillis(), 0l, new byte[64]);
        issueAssetTransaction.sign(maker, asPack);
        issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issueAssetTransaction.process(gb, asPack);
        key = asset.getKey(db);

        //CREATE ORDER
        CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(maker, key, FEE_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), FEE_POWER, System.currentTimeMillis(), 0l, new byte[]{5, 6});
        createOrderTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        createOrderTransaction.sign(maker, asPack);
        createOrderTransaction.process(gb, asPack);

        //CREATE CANCEL ORDER
        CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(maker, new byte[]{5, 6}, FEE_POWER, System.currentTimeMillis(), 0l, new byte[]{1, 2});
        cancelOrderTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        cancelOrderTransaction.sign(maker, asPack);
        cancelOrderTransaction.process(gb, asPack);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(asset.getQuantity()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

        //CHECK REFERENCE SENDER
        assertEquals(cancelOrderTransaction.getSignature(), Transaction.FOR_NETWORK);

        //CHECK ORDER EXISTS
        assertEquals(false, db.getOrderMap().contains(12L));
    }

    @Ignore
    //TODO actualize the test
    @Test
    public void orphanCancelOrderTransaction() {
        init();

        //CREATE ISSUE ASSET TRANSACTION
        IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, System.currentTimeMillis(), 0l);
        issueAssetTransaction.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        issueAssetTransaction.sign(maker, asPack);
        assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(Transaction.FOR_NETWORK, flags));
        issueAssetTransaction.process(gb, asPack);

        long key = asset.getKey(db);
        LOGGER.info("asset.getReg(): " + asset.getReference());
        LOGGER.info("asset.getKey(): " + key);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(50000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

        //CREATE ORDER
        CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(maker, key, FEE_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), FEE_POWER, System.currentTimeMillis(), 0l, new byte[]{5, 6});
        createOrderTransaction.sign(maker, asPack);
        createOrderTransaction.process(gb, asPack);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(49000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

        //CREATE CANCEL ORDER
        CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(maker, new byte[]{5, 6}, FEE_POWER, System.currentTimeMillis(), 0l, new byte[]{1, 2});
        cancelOrderTransaction.sign(maker, asPack);
        cancelOrderTransaction.process(gb, asPack);
        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(50000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));
        cancelOrderTransaction.orphan(gb, asPack);

        //CHECK BALANCE SENDER
        assertEquals(BigDecimal.valueOf(49000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

        //CHECK REFERENCE SENDER
        assertEquals(createOrderTransaction.getSignature(), Transaction.FOR_NETWORK);

        //CHECK ORDER EXISTS
        assertEquals(true, db.getOrderMap().contains(12L));
    }
    @Ignore
    @Test
    public void validateMessageTransaction() {

        init();

        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();

        byte[] data = "test123!".getBytes();

        PrivateKeyAccount creator = new PrivateKeyAccount(privateKey);
        Account recipient = new Account("QfreeNWCeaU3BiXUxktaJRJrBB1SDg2k7o");

        long key = 2l;

        creator.changeBalance(db, false, false, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        RSend r_Send = new RSend(
                creator,
                recipient,
                key,
                BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
                "headdd", data,
                new byte[]{1},
                new byte[]{0},
                maker.getLastTimestamp()[0]
        );
        r_Send.setDC(db, Transaction.FOR_NETWORK, 1, 1, true);
        r_Send.sign(creator, asPack);

        assertEquals(r_Send.isValid(Transaction.FOR_NETWORK, flags), Transaction.VALIDATE_OK);

        r_Send.process(gb, asPack);

        assertEquals(BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), creator.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(90).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), creator.getBalanceUSE(key, db));
        assertEquals(BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

        byte[] rawMessageTransaction = r_Send.toBytes(Transaction.FOR_NETWORK, true);

        RSend messageTransaction_2 = null;
        try {
            messageTransaction_2 = (RSend) RSend.Parse(rawMessageTransaction, asPack);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(new String(r_Send.getData()), new String(messageTransaction_2.getData()));
        assertEquals(r_Send.getCreator(), messageTransaction_2.getCreator());
        assertEquals(r_Send.getRecipient(), messageTransaction_2.getRecipient());
        assertEquals(r_Send.getKey(), messageTransaction_2.getKey());
        assertEquals(r_Send.getAmount(), messageTransaction_2.getAmount());
        assertEquals(r_Send.isEncrypted(), messageTransaction_2.isEncrypted());
        assertEquals(r_Send.isText(), messageTransaction_2.isText());

        assertEquals(r_Send.isSignatureValid(db), true);
        assertEquals(messageTransaction_2.isSignatureValid(db), true);
    }


}
