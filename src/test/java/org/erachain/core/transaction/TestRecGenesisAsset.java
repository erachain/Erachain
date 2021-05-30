package org.erachain.core.transaction;

import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.*;

;
@Ignore
public class TestRecGenesisAsset {

    static Logger LOGGER = LoggerFactory.getLogger(TestRecGenesisAsset.class.getName());

    int forDeal = Transaction.FOR_NETWORK;

    //int asPack = Transaction.FOR_NETWORK;

    long FEE_KEY = 1l;
    byte FEE_POWER = (byte) 1;
    byte[] assetReference = new byte[64];

    long flags = 0l;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    AssetCls asset;
    long key = 0l;
    GenesisIssueAssetTransaction genesisIssueAssetTransaction;
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;

    private void initIssue(boolean toProcess) {

        //CREATE EMPTY MEMORY DATABASE
        db = DCSet.createEmptyDatabaseSet(0);

        //CREATE ASSET
        asset = GenesisBlock.makeAsset(0);
        //byte[] rawAsset = asset.toBytes(true); // reference is new byte[64]
        //assertEquals(rawAsset.length, asset.getDataLength());

        //CREATE ISSUE ASSET TRANSACTION
        genesisIssueAssetTransaction = new GenesisIssueAssetTransaction(asset);
        if (toProcess) {
            genesisIssueAssetTransaction.process(gb, Transaction.FOR_NETWORK);
            key = asset.getKey(db);
        }

    }

    //GENESIS

    // GENESIS ISSUE
    @Test
    public void validateGenesisIssueAssetTransaction() {

        initIssue(false);

        //genesisIssueAssetTransaction.sign(creator);
        //CHECK IF ISSUE ASSET TRANSACTION IS VALID
        assertEquals(true, genesisIssueAssetTransaction.isSignatureValid());
        assertEquals(Transaction.VALIDATE_OK, genesisIssueAssetTransaction.isValid(Transaction.FOR_NETWORK, flags));

        //CONVERT TO BYTES
        //logger.info("CREATOR: " + genesisIssueAssetTransaction.getCreator().getPublicKey());
        byte[] rawGenesisIssueAssetTransaction = genesisIssueAssetTransaction.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawGenesisIssueAssetTransaction.length, genesisIssueAssetTransaction.getDataLength(Transaction.FOR_NETWORK, true));
        //logger.info("rawGenesisIssueAssetTransaction.length") + ": + rawGenesisIssueAssetTransaction.length);

        try {
            //PARSE FROM BYTES
            GenesisIssueAssetTransaction parsedGenesisIssueAssetTransaction = (GenesisIssueAssetTransaction) TransactionFactory.getInstance().parse(rawGenesisIssueAssetTransaction, Transaction.FOR_NETWORK);

            //CHECK INSTANCE
            assertEquals(true, parsedGenesisIssueAssetTransaction instanceof GenesisIssueAssetTransaction);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getSignature(), parsedGenesisIssueAssetTransaction.getSignature()));

            //CHECK NAME
            assertEquals(genesisIssueAssetTransaction.getItem().getName(), parsedGenesisIssueAssetTransaction.getItem().getName());

            //CHECK DESCRIPTION
            assertEquals(genesisIssueAssetTransaction.getItem().getDescription(), parsedGenesisIssueAssetTransaction.getItem().getDescription());

            AssetCls asset = (AssetCls) genesisIssueAssetTransaction.getItem();
            AssetCls asset1 = (AssetCls) parsedGenesisIssueAssetTransaction.getItem();

            //CHECK QUANTITY
            assertEquals(asset.getQuantity(), asset1.getQuantity());

            //SCALE
            assertEquals(asset.getScale(), asset1.getScale());

            //ASSET TYPE
            assertEquals(asset.getAssetType(), asset1.getAssetType());


        } catch (Exception e) {
            fail("Exception while parsing transaction." + e);
        }

        //PARSE TRANSACTION FROM WRONG BYTES
        rawGenesisIssueAssetTransaction = new byte[genesisIssueAssetTransaction.getDataLength(Transaction.FOR_NETWORK, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawGenesisIssueAssetTransaction, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }


    @Test
    public void parseGenesisIssueAssetTransaction() {

        initIssue(false);

        //CONVERT TO BYTES
        byte[] rawGenesisIssueAssetTransaction = genesisIssueAssetTransaction.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATA LENGTH
        assertEquals(rawGenesisIssueAssetTransaction.length, genesisIssueAssetTransaction.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            GenesisIssueAssetTransaction parsedGenesisIssueAssetTransaction = (GenesisIssueAssetTransaction) TransactionFactory.getInstance().parse(rawGenesisIssueAssetTransaction, Transaction.FOR_NETWORK);

            //CHECK INSTANCE
            assertEquals(true, parsedGenesisIssueAssetTransaction instanceof GenesisIssueAssetTransaction);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getSignature(), parsedGenesisIssueAssetTransaction.getSignature()));

            //CHECK NAME
            assertEquals(genesisIssueAssetTransaction.getItem().getName(), parsedGenesisIssueAssetTransaction.getItem().getName());

            //CHECK DESCRIPTION
            assertEquals(genesisIssueAssetTransaction.getItem().getDescription(), parsedGenesisIssueAssetTransaction.getItem().getDescription());

            //CHECK QUANTITY
            AssetCls asset = (AssetCls) genesisIssueAssetTransaction.getItem();
            AssetCls asset1 = (AssetCls) parsedGenesisIssueAssetTransaction.getItem();
            assertEquals(asset.getQuantity(), asset1.getQuantity());

            //SCALE
            assertEquals(asset.getScale(), asset1.getScale());

            //ASSET TYPE
            assertEquals(asset.getAssetType(), asset1.getAssetType());

        } catch (Exception e) {
            fail("Exception while parsing transaction." + e);
        }

        //PARSE TRANSACTION FROM WRONG BYTES
        rawGenesisIssueAssetTransaction = new byte[genesisIssueAssetTransaction.getDataLength(Transaction.FOR_NETWORK, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawGenesisIssueAssetTransaction, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }


    @Test
    public void processGenesisIssueAssetTransaction() {

        initIssue(true);
        LOGGER.info("asset KEY: " + key);

        //CHECK BALANCE ISSUER - null
        //assertEquals(BigDecimal.valueOf(asset.getQuantity()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getConfirmedBalance(key, db));

        //CHECK ASSET EXISTS SENDER
        long key = db.getIssueAssetMap().get(genesisIssueAssetTransaction);
        assertEquals(true, db.getItemAssetMap().contains(key));

        //CHECK ASSET IS CORRECT
        assertEquals(true, Arrays.equals(db.getItemAssetMap().get(key).toBytes(forDeal, true, false), asset.toBytes(forDeal, true, false)));

        //CHECK ASSET BALANCE SENDER - null
        //assertEquals(true, db.getAssetBalanceMap().get(maker.getAddress(), key).compareTo(new BigDecimal(asset.getQuantity())) == 0);

        //CHECK REFERENCE SENDER - null
        //assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getSignature(), maker.getLastReference(db)));
    }


    @Test
    public void orphanIssueAssetTransaction() {

        initIssue(true);

        //assertEquals(new BigDecimal(asset.getQuantity()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getConfirmedBalance(key, db));
        //assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getSignature(), maker.getLastReference(db)));

        genesisIssueAssetTransaction.orphan(gb, Transaction.FOR_NETWORK);

        //CHECK BALANCE ISSUER
        assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

        //CHECK ASSET EXISTS SENDER
        assertEquals(false, db.getItemAssetMap().contains(key));

        //CHECK ASSET BALANCE SENDER
        assertEquals(0, db.getAssetBalanceMap().get(maker.getShortAddressBytes(), key).a.b.longValue());

        //CHECK REFERENCE SENDER
        // it for not genesis - assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getReference(), maker.getLastReference(db)));
        assertEquals(null, maker.getLastTimestamp(db));

    }

    //GENESIS TRANSFER ASSET

    @Test
    public void validateSignatureGenesisTransferAssetTransaction() {

        initIssue(true);

        //CREATE SIGNATURE
        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
        System.out.println("key" + key);

        //CREATE ASSET TRANSFER
        Transaction assetTransfer = new GenesisTransferAssetTransaction(recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));
        //assetTransfer.sign(sender);

        //CHECK IF ASSET TRANSFER SIGNATURE IS VALID
        assertEquals(true, assetTransfer.isSignatureValid(db));
    }

    @Test
    public void validateGenesisTransferAssetTransaction() {

        initIssue(true);

        //CREATE SIGNATURE
        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

        //CREATE VALID ASSET TRANSFER
        Transaction assetTransfer = new GenesisTransferAssetTransaction(recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));

        //CHECK IF ASSET TRANSFER IS VALID
        assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, flags));

        assetTransfer.process(gb, Transaction.FOR_NETWORK);

        //CREATE VALID ASSET TRANSFER
        maker.changeBalance(db, false, false, 1, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        assetTransfer = new GenesisTransferAssetTransaction(recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));

        //CHECK IF ASSET TRANSFER IS VALID
        assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, flags));

        //CREATE INVALID ASSET TRANSFER INVALID RECIPIENT ADDRESS
        assetTransfer = new GenesisTransferAssetTransaction(new Account("test"), key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));

        //CHECK IF ASSET TRANSFER IS INVALID
        assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, flags));

        //CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
        assetTransfer = new GenesisTransferAssetTransaction(recipient, key, BigDecimal.valueOf(-100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));

        //CHECK IF ASSET TRANSFER IS INVALID
        assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, flags));

        //CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
        assetTransfer = new GenesisTransferAssetTransaction(recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));

        //CHECK IF ASSET TRANSFER IS INVALID
        // nor need for genesis - assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db));
    }

    @Test
    public void parseGenesisTransferAssetTransaction() {

        initIssue(true);

        //CREATE SIGNATURE
        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

        //CREATE VALID ASSET TRANSFER
        GenesisTransferAssetTransaction genesisTransferAsset = new GenesisTransferAssetTransaction(recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));
        //genesisTransferAsset.sign(maker);
        //genesisTransferAsset.process(db);

        //CONVERT TO BYTES
        byte[] rawGenesisTransferAsset = genesisTransferAsset.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATALENGTH
        assertEquals(rawGenesisTransferAsset.length, genesisTransferAsset.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            GenesisTransferAssetTransaction parsedAssetTransfer = (GenesisTransferAssetTransaction) TransactionFactory.getInstance().parse(rawGenesisTransferAsset, Transaction.FOR_NETWORK);
            LOGGER.info(" 1: " + parsedAssetTransfer.getKey());

            //CHECK INSTANCE
            assertEquals(true, parsedAssetTransfer instanceof GenesisTransferAssetTransaction);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(genesisTransferAsset.getSignature(), parsedAssetTransfer.getSignature()));

            //CHECK KEY
            assertEquals(genesisTransferAsset.getKey(), parsedAssetTransfer.getKey());

            //CHECK AMOUNT SENDER
            assertEquals(genesisTransferAsset.getAmount(maker), parsedAssetTransfer.getAmount(maker));

            //CHECK AMOUNT RECIPIENT
            assertEquals(genesisTransferAsset.getAmount(recipient), parsedAssetTransfer.getAmount(recipient));

        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

        //PARSE TRANSACTION FROM WRONG BYTES
        rawGenesisTransferAsset = new byte[genesisTransferAsset.getDataLength(Transaction.FOR_NETWORK, true)];

        try {
            //PARSE FROM BYTES
            TransactionFactory.getInstance().parse(rawGenesisTransferAsset, Transaction.FOR_NETWORK);

            //FAIL
            fail("this should throw an exception");
        } catch (Exception e) {
            //EXCEPTION IS THROWN OK
        }
    }

    @Test
    public void processGenesisTransferAssetTransaction() {

        initIssue(true);

        BigDecimal total = BigDecimal.valueOf(asset.getQuantity()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
        BigDecimal amoSend = BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
        //assertEquals(total, amoSend);

        //CHECK BALANCE SENDER - null
        //assertEquals(total, maker.getConfirmedBalance(key, db));

        //CREATE SIGNATURE
        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

        //CREATE ASSET TRANSFER
        Transaction assetTransfer = new GenesisTransferAssetTransaction(recipient, key, amoSend);
        assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, flags));

        // assetTransfer.sign(sender); // not  NEED
        assetTransfer.process(gb, Transaction.FOR_NETWORK);

        //CHECK BALANCE SENDER - null
        //assertEquals(total.subtract(amoSend), maker.getConfirmedBalance(key, db));

        //CHECK BALANCE RECIPIENT
        assertEquals(amoSend, recipient.getBalanceUSE(key, db));

		/* not NEED
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(assetTransfer.getSignature(), sender.getLastReference(databaseSet)));
		 */

        //CHECK REFERENCE RECIPIENT
        assertEquals(assetTransfer.getTimestamp(), recipient.getLastTimestamp(db));
    }

    @Test
    public void orphanGenesisTransferAssetTransaction() {

        initIssue(true);

        BigDecimal total = BigDecimal.valueOf(asset.getQuantity()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
        BigDecimal amoSend = BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

        //CREATE SIGNATURE
        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

        //CREATE ASSET TRANSFER
        Transaction assetTransfer = new GenesisTransferAssetTransaction(recipient, key, amoSend);
        // assetTransfer.sign(sender); not NEED
        assetTransfer.process(gb, Transaction.FOR_NETWORK);
        assetTransfer.orphan(gb, Transaction.FOR_NETWORK);

        //CHECK BALANCE SENDER - null
        //assertEquals(total, maker.getConfirmedBalance(key, db));

        //CHECK BALANCE RECIPIENT
        assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

		/* not NEED
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
		 */

        //CHECK REFERENCE RECIPIENT
        assertNotEquals(assetTransfer.getSignature(), recipient.getLastTimestamp(db));
    }

    @Test
    public void processGenesisTransferAssetTransaction3() {

        initIssue(true);

        BigDecimal total = BigDecimal.valueOf(asset.getQuantity()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
        BigDecimal amoSend = BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
        //assertEquals(total, amoSend);

        //CHECK BALANCE SENDER - null
        //assertEquals(total, maker.getConfirmedBalance(key, db));

        Account owner = new Account("7JS4ywtcqrcVpRyBxfqyToS2XBDeVrdqZL");
        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

        //CREATE ASSET TRANSFER
        Transaction assetTransfer = new GenesisTransferAssetTransaction(recipient, -key, amoSend, owner);
        assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(Transaction.FOR_NETWORK, flags));

        /// PARSE
        byte[] rawGenesisTransferAsset = assetTransfer.toBytes(Transaction.FOR_NETWORK, true);

        //CHECK DATALENGTH
        assertEquals(rawGenesisTransferAsset.length, assetTransfer.getDataLength(Transaction.FOR_NETWORK, true));

        try {
            //PARSE FROM BYTES
            GenesisTransferAssetTransaction parsedAssetTransfer = (GenesisTransferAssetTransaction) TransactionFactory.getInstance().parse(rawGenesisTransferAsset, Transaction.FOR_NETWORK);
            System.out.println(" 1: " + parsedAssetTransfer.getKey());

            //CHECK INSTANCE
            assertEquals(true, parsedAssetTransfer instanceof GenesisTransferAssetTransaction);

            //CHECK SIGNATURE
            assertEquals(true, Arrays.equals(assetTransfer.getSignature(), parsedAssetTransfer.getSignature()));

            //CHECK KEY
            assertEquals(assetTransfer.getKey(), parsedAssetTransfer.getKey());

            //CHECK AMOUNT SENDER
            assertEquals(assetTransfer.getAmount(maker), parsedAssetTransfer.getAmount(maker));

            //CHECK AMOUNT RECIPIENT
            assertEquals(assetTransfer.getAmount(recipient), parsedAssetTransfer.getAmount(recipient));

            //CHECK A
            GenesisTransferAssetTransaction aaa = (GenesisTransferAssetTransaction) assetTransfer;
            assertEquals(true, aaa.getCreator().equals(parsedAssetTransfer.getCreator()));

        } catch (Exception e) {
            fail("Exception while parsing transaction. " + e);
        }

        // assetTransfer.sign(sender); // not  NEED
        assetTransfer.process(gb, Transaction.FOR_NETWORK);

        //CHECK BALANCE SENDER - null
        //assertEquals(total.subtract(amoSend), maker.getConfirmedBalance(key, db));

        //CHECK BALANCE RECIPIENT
        assertEquals(amoSend, recipient.getBalance(db, -key));
        System.out.println(" 1: " + recipient.getBalance(db, key));

        assertEquals(BigDecimal.ZERO.subtract(amoSend), owner.getBalance(db, -key));

		/* not NEED
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(assetTransfer.getSignature(), sender.getLastReference(databaseSet)));
		 */

        //CHECK REFERENCE RECIPIENT
        assertEquals(assetTransfer.getTimestamp(), recipient.getLastTimestamp(db));

        ///////////////////////////
        ////////////////////////////
        assetTransfer.orphan(gb, Transaction.FOR_NETWORK);

        //CHECK BALANCE SENDER - null
        //assertEquals(total, maker.getConfirmedBalance(key, db));

        //CHECK BALANCE RECIPIENT
        assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalance(db, -key));
        System.out.println(" 1: " + recipient.getBalance(db, key));

        assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), owner.getBalance(db, key));

        //CHECK REFERENCE RECIPIENT
        assertNotEquals(assetTransfer.getSignature(), recipient.getLastTimestamp(db));
    }


}
