package test.records;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
 import org.apache.log4j.Logger;

import ntp.NTP;

import org.junit.Test;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.assets.AssetVenture;
import core.transaction.GenesisIssueAssetTransaction;
import core.transaction.GenesisTransferAssetTransaction;
import core.transaction.IssueAssetTransaction;
import core.transaction.R_SignNote;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import database.DBSet;;

public class TestRecGenesisAsset {

	static Logger LOGGER = Logger.getLogger(TestRecGenesisAsset.class.getName());

	Long releaserReference = null;

	long FEE_KEY = 1l;
	byte FEE_POWER = (byte)1;
	byte[] assetReference = new byte[64];
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
	AssetCls asset;
	long key = -1l;
	GenesisIssueAssetTransaction genesisIssueAssetTransaction;
	
	private void initIssue(boolean toProcess) {
	
		//CREATE EMPTY MEMORY DATABASE
		db = DBSet.createEmptyDatabaseSet();
		
		//CREATE ASSET
		asset = GenesisBlock.makeAsset(0);
		//byte[] rawAsset = asset.toBytes(true); // reference is new byte[64]
		//assertEquals(rawAsset.length, asset.getDataLength());
				
		//CREATE ISSUE ASSET TRANSACTION
		genesisIssueAssetTransaction = new GenesisIssueAssetTransaction(asset);
		if (toProcess)
		{ 
			genesisIssueAssetTransaction.process(db, false);
			key = asset.getKey(db);
		}
		
	}
	
	//GENESIS
	
	// GENESIS ISSUE
	@Test
	public void validateGenesisIssueAssetTransaction() 
	{
		
		initIssue(false);
		
		//genesisIssueAssetTransaction.sign(creator);
		//CHECK IF ISSUE ASSET TRANSACTION IS VALID
		assertEquals(true, genesisIssueAssetTransaction.isSignatureValid());
		assertEquals(Transaction.VALIDATE_OK, genesisIssueAssetTransaction.isValid(db, releaserReference));
				
		//CONVERT TO BYTES
		//LOGGER.info("CREATOR: " + genesisIssueAssetTransaction.getCreator().getPublicKey());
		byte[] rawGenesisIssueAssetTransaction = genesisIssueAssetTransaction.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawGenesisIssueAssetTransaction.length, genesisIssueAssetTransaction.getDataLength(false));
		//LOGGER.info("rawGenesisIssueAssetTransaction.length") + ": + rawGenesisIssueAssetTransaction.length);
		
		try 
		{	
			//PARSE FROM BYTES
			GenesisIssueAssetTransaction parsedGenesisIssueAssetTransaction = (GenesisIssueAssetTransaction) TransactionFactory.getInstance().parse(rawGenesisIssueAssetTransaction, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedGenesisIssueAssetTransaction instanceof GenesisIssueAssetTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getSignature(), parsedGenesisIssueAssetTransaction.getSignature()));
									
			//CHECK NAME
			assertEquals(genesisIssueAssetTransaction.getItem().getName(), parsedGenesisIssueAssetTransaction.getItem().getName());
				
			//CHECK DESCRIPTION
			assertEquals(genesisIssueAssetTransaction.getItem().getDescription(), parsedGenesisIssueAssetTransaction.getItem().getDescription());
				
			AssetCls asset = (AssetCls)genesisIssueAssetTransaction.getItem();
			AssetCls asset1 = (AssetCls)parsedGenesisIssueAssetTransaction.getItem();

			//CHECK QUANTITY
			assertEquals(asset.getQuantity(), asset1.getQuantity());
			
			//DIVISIBLE
			assertEquals(asset.isDivisible(), asset1.isDivisible());
						
			
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction." + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawGenesisIssueAssetTransaction = new byte[genesisIssueAssetTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawGenesisIssueAssetTransaction, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	
	@Test
	public void parseGenesisIssueAssetTransaction() 
	{
		
		initIssue(false);
		
		//CONVERT TO BYTES
		byte[] rawGenesisIssueAssetTransaction = genesisIssueAssetTransaction.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawGenesisIssueAssetTransaction.length, genesisIssueAssetTransaction.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			GenesisIssueAssetTransaction parsedGenesisIssueAssetTransaction = (GenesisIssueAssetTransaction) TransactionFactory.getInstance().parse(rawGenesisIssueAssetTransaction, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedGenesisIssueAssetTransaction instanceof GenesisIssueAssetTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getSignature(), parsedGenesisIssueAssetTransaction.getSignature()));
						
			//CHECK NAME
			assertEquals(genesisIssueAssetTransaction.getItem().getName(), parsedGenesisIssueAssetTransaction.getItem().getName());
				
			//CHECK DESCRIPTION
			assertEquals(genesisIssueAssetTransaction.getItem().getDescription(), parsedGenesisIssueAssetTransaction.getItem().getDescription());
				
			//CHECK QUANTITY
			AssetCls asset = (AssetCls)genesisIssueAssetTransaction.getItem();
			AssetCls asset1 = (AssetCls)parsedGenesisIssueAssetTransaction.getItem();
			assertEquals(asset.getQuantity(), asset1.getQuantity());
			
			//DIVISIBLE
			assertEquals(asset.isDivisible(), asset1.isDivisible());
			
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction." + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawGenesisIssueAssetTransaction = new byte[genesisIssueAssetTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawGenesisIssueAssetTransaction, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	
	@Test
	public void processGenesisIssueAssetTransaction()
	{
		
		initIssue(true);		
		LOGGER.info("asset KEY: " + key);
		
		//CHECK BALANCE ISSUER - null
		//assertEquals(BigDecimal.valueOf(asset.getQuantity()).setScale(8), maker.getConfirmedBalance(key, db));
		
		//CHECK ASSET EXISTS SENDER
		long key = db.getIssueAssetMap().get(genesisIssueAssetTransaction);
		assertEquals(true, db.getItemAssetMap().contains(key));
		
		//CHECK ASSET IS CORRECT
		assertEquals(true, Arrays.equals(db.getItemAssetMap().get(key).toBytes(true), asset.toBytes(true)));
		
		//CHECK ASSET BALANCE SENDER - null
		//assertEquals(true, db.getAssetBalanceMap().get(maker.getAddress(), key).compareTo(new BigDecimal(asset.getQuantity())) == 0);
				
		//CHECK REFERENCE SENDER - null
		//assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getSignature(), maker.getLastReference(db)));
	}
	
	
	@Test
	public void orphanIssueAssetTransaction()
	{
		
		initIssue(true);

		//assertEquals(new BigDecimal(asset.getQuantity()).setScale(8), maker.getConfirmedBalance(key, db));
		//assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getSignature(), maker.getLastReference(db)));
		
		genesisIssueAssetTransaction.orphan(db, false);
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.ZERO.setScale(8), maker.getConfirmedBalance(key,db));
		
		//CHECK ASSET EXISTS SENDER
		assertEquals(false, db.getItemAssetMap().contains(key));
		
		//CHECK ASSET BALANCE SENDER
		assertEquals(0, db.getAssetBalanceMap().get(maker.getAddress(), key).longValue());
				
		//CHECK REFERENCE SENDER
		// it for not genesis - assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getReference(), maker.getLastReference(db)));
		assertEquals(null, maker.getLastReference(db));

	}

	//GENESIS TRANSFER ASSET
	
	@Test
	public void validateSignatureGenesisTransferAssetTransaction() 
	{
		
		initIssue(false);
		
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		
		//CREATE ASSET TRANSFER
		Transaction assetTransfer = new GenesisTransferAssetTransaction(recipient, key, BigDecimal.valueOf(100).setScale(8));
		//assetTransfer.sign(sender);
		
		//CHECK IF ASSET TRANSFER SIGNATURE IS VALID
		assertEquals(true, assetTransfer.isSignatureValid());		
	}
	
	@Test
	public void validateGenesisTransferAssetTransaction() 
	{
		
		initIssue(true);
		
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

		//CREATE VALID ASSET TRANSFER
		Transaction assetTransfer = new GenesisTransferAssetTransaction(recipient, key, BigDecimal.valueOf(100).setScale(8));

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));

		assetTransfer.process(db, false);

		//CREATE VALID ASSET TRANSFER
		maker.setConfirmedBalance(1, BigDecimal.valueOf(100).setScale(8), db);
		assetTransfer = new GenesisTransferAssetTransaction(recipient, key, BigDecimal.valueOf(100).setScale(8));

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));			
		
		//CREATE INVALID ASSET TRANSFER INVALID RECIPIENT ADDRESS
		assetTransfer = new GenesisTransferAssetTransaction(new Account("test"), key, BigDecimal.valueOf(100).setScale(8));
	
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));
		
		//CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
		assetTransfer = new GenesisTransferAssetTransaction(recipient, key, BigDecimal.valueOf(-100).setScale(8));
		
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));	
		
		//CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
		assetTransfer = new GenesisTransferAssetTransaction(recipient, key, BigDecimal.valueOf(100).setScale(8));
		
		//CHECK IF ASSET TRANSFER IS INVALID
		// nor need for genesis - assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db));
	}
	
	@Test
	public void parseGenesisTransferAssetTransaction() 
	{

		initIssue(true);		
		
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

		//CREATE VALID ASSET TRANSFER
		GenesisTransferAssetTransaction genesisTransferAsset = new GenesisTransferAssetTransaction(recipient, key, BigDecimal.valueOf(100).setScale(8));
		//genesisTransferAsset.sign(maker);
		//genesisTransferAsset.process(db);

		//CONVERT TO BYTES
		byte[] rawGenesisTransferAsset = genesisTransferAsset.toBytes(true, null);
		
		//CHECK DATALENGTH
		assertEquals(rawGenesisTransferAsset.length, genesisTransferAsset.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			GenesisTransferAssetTransaction parsedAssetTransfer = (GenesisTransferAssetTransaction) TransactionFactory.getInstance().parse(rawGenesisTransferAsset, releaserReference);
			LOGGER.info(" 1: " + parsedAssetTransfer.getKey() );

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
			
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawGenesisTransferAsset = new byte[genesisTransferAsset.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawGenesisTransferAsset, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void processGenesisTransferAssetTransaction()
	{

		initIssue(true);
		
		BigDecimal total = BigDecimal.valueOf(asset.getQuantity()).setScale(8);
		BigDecimal amoSend = BigDecimal.valueOf(100).setScale(8);
		//assertEquals(total, amoSend);
		
		//CHECK BALANCE SENDER - null
		//assertEquals(total, maker.getConfirmedBalance(key, db));
			
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
			
		//CREATE ASSET TRANSFER
		Transaction assetTransfer = new GenesisTransferAssetTransaction(recipient, key, amoSend);
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, null));
		
		// assetTransfer.sign(sender); // not  NEED
		assetTransfer.process(db, false);
		
		//CHECK BALANCE SENDER - null
		//assertEquals(total.subtract(amoSend), maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(amoSend, recipient.getConfirmedBalance(key, db));
		
		/* not NEED
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(assetTransfer.getSignature(), sender.getLastReference(databaseSet)));
		*/
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(assetTransfer.getTimestamp(), recipient.getLastReference(db));
	}
	
	@Test
	public void orphanGenesisTransferAssetTransaction()
	{
		
		initIssue(true);
		
		BigDecimal total = BigDecimal.valueOf(asset.getQuantity()).setScale(8);
		BigDecimal amoSend = BigDecimal.valueOf(100).setScale(8);
			
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
			
		//CREATE ASSET TRANSFER
		Transaction assetTransfer = new GenesisTransferAssetTransaction(recipient, key, amoSend);
		// assetTransfer.sign(sender); not NEED
		assetTransfer.process(db, false);
		assetTransfer.orphan(db, false);
		
		//CHECK BALANCE SENDER - null
		//assertEquals(total, maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(key, db));
		
		/* not NEED
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
		*/
		
		//CHECK REFERENCE RECIPIENT
		assertNotEquals(assetTransfer.getSignature(), recipient.getLastReference(db));
	}
	
}
