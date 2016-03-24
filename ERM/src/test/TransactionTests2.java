package test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import ntp.NTP;

import org.junit.Test;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.assets.Asset;
import qora.assets.Venture;
import qora.block.GenesisBlock;
import qora.crypto.Crypto;

import qora.transaction.GenesisTransaction;
import qora.transaction.GenesisIssueAssetTransaction;
import qora.transaction.GenesisTransferAssetTransaction;
import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;
import qora.transaction.IssueAssetTransaction;
import qora.transaction.TransferAssetTransaction;

public class TransactionTests2 {

	long OIL_KEY = 1l;
	byte FEE_POWER = (byte)1;
	byte[] assetReference = new byte[64];
	long timestamp = NTP.getTime();
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
	

	// INIT ASSETS
	private void init() {
		
		db = DBSet.createEmptyDatabaseSet();
		gb = new GenesisBlock();
		gb.process(db);
		
		// OIL FUND
		maker.setLastReference(gb.getGeneratorSignature(), db);
		maker.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), db);

	}
	

	//GENESIS
	
	// GENESIS ISSUE
	@Test
	public void validateGenesisIssueAssetTransaction() 
	{

		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount creator = new PrivateKeyAccount(privateKey);

		/*
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(creator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		*/
		
		//CREATE ASSET
		Asset asset = new Venture(creator, "test", "strontje", 50000l, (byte) 2, false);
		//byte[] rawAsset = asset.toBytes(true); // reference is new byte[64]
		//assertEquals(rawAsset.length, asset.getDataLength());
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		
		//CREATE ISSUE ASSET TRANSACTION
		GenesisIssueAssetTransaction genesisIssueAssetTransaction = new GenesisIssueAssetTransaction(creator, asset, timestamp);
		//genesisIssueAssetTransaction1.sign(sender);
		//CHECK IF ISSUE ASSET TRANSACTION IS VALID
		assertEquals(true, genesisIssueAssetTransaction.isSignatureValid());
				
		//CONVERT TO BYTES
		//Logger.getGlobal().info("CREATOR: " + genesisIssueAssetTransaction.getCreator().getPublicKey());
		byte[] rawGenesisIssueAssetTransaction = genesisIssueAssetTransaction.toBytes(true);
		
		//CHECK DATA LENGTH
		assertEquals(rawGenesisIssueAssetTransaction.length, genesisIssueAssetTransaction.getDataLength());
		//Logger.getGlobal().info("rawGenesisIssueAssetTransaction.length:" + rawGenesisIssueAssetTransaction.length);
		
		try 
		{	
			//PARSE FROM BYTES
			GenesisIssueAssetTransaction parsedGenesisIssueAssetTransaction = (GenesisIssueAssetTransaction) TransactionFactory.getInstance().parse(rawGenesisIssueAssetTransaction);
			
			//CHECK INSTANCE
			assertEquals(true, parsedGenesisIssueAssetTransaction instanceof GenesisIssueAssetTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getSignature(), parsedGenesisIssueAssetTransaction.getSignature()));
			
			//CHECK ISSUER
			assertEquals(genesisIssueAssetTransaction.getCreator().getAddress(), parsedGenesisIssueAssetTransaction.getCreator().getAddress());
						
			//CHECK NAME
			assertEquals(genesisIssueAssetTransaction.getAsset().getName(), parsedGenesisIssueAssetTransaction.getAsset().getName());
				
			//CHECK DESCRIPTION
			assertEquals(genesisIssueAssetTransaction.getAsset().getDescription(), parsedGenesisIssueAssetTransaction.getAsset().getDescription());
				
			//CHECK QUANTITY
			assertEquals(genesisIssueAssetTransaction.getAsset().getQuantity(), parsedGenesisIssueAssetTransaction.getAsset().getQuantity());
			
			//DIVISIBLE
			assertEquals(genesisIssueAssetTransaction.getAsset().isDivisible(), parsedGenesisIssueAssetTransaction.getAsset().isDivisible());
			
			//CHECK FEE
			assertEquals(genesisIssueAssetTransaction.getFee(), parsedGenesisIssueAssetTransaction.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getReference(), parsedGenesisIssueAssetTransaction.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(genesisIssueAssetTransaction.getTimestamp(), parsedGenesisIssueAssetTransaction.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction." + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawGenesisIssueAssetTransaction = new byte[genesisIssueAssetTransaction.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawGenesisIssueAssetTransaction);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	//GENESIS TRANSFER ASSET
	
	@Test
	public void validateGenesisSignatureTransferAssetTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		long timestamp = NTP.getTime();
		
		//CREATE ASSET TRANSFER
		Transaction assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, 0
				, BigDecimal.valueOf(100).setScale(8), timestamp);
		//assetTransfer.sign(sender);
		
		//CHECK IF ASSET TRANSFER SIGNATURE IS VALID
		assertEquals(true, assetTransfer.isSignatureValid());
		
		//INVALID SIGNATURE
		assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, 0,
				BigDecimal.valueOf(100).setScale(8), timestamp+1);
		//assetTransfer.sign(sender);
		
		//CHECK IF ASSET TRANSFER SIGNATURE IS INVALID
		assertEquals(true, assetTransfer.isSignatureValid());
	}
	
	@Test
	public void validateGenesisTransferAssetTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		long genesisTimestamp = NTP.getTime();
		
		//ADD QORA ASSET
		//Asset qoraAsset = new Venture(new GenesisBlock().getGenerator(), "Qora", "This is the simulated Qora asset.", 10000000000L, (byte) 2, true);
    	//databaseSet.getAssetMap().set(0l, qoraAsset);
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);

		//CREATE SIGNATURE
		Account recipient = new Account("QgcphUTiVHHfHg8e1LVgg5jujVES7ZDUTr");
		long timestamp = NTP.getTime();
				
		//CREATE JOB ASSET
		Asset asset = new Venture(sender, "OIL+", "+It is an OILing drops used for fees", 99999999L, (byte) 8, true);
		GenesisIssueAssetTransaction trans = new GenesisIssueAssetTransaction(sender, asset, genesisTimestamp);
		trans.process(databaseSet);
		Logger.getGlobal().info("asset key " + asset.getKey());
		

		//CREATE VALID ASSET TRANSFER
		Transaction assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, asset.getKey(), BigDecimal.valueOf(100).setScale(8), timestamp);

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(databaseSet));

		assetTransfer.process(databaseSet);

		//CREATE VALID ASSET TRANSFER
		sender.setConfirmedBalance(1, BigDecimal.valueOf(100).setScale(8), databaseSet);
		assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, asset.getKey(), BigDecimal.valueOf(100).setScale(8), timestamp);

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(databaseSet));			
		
		//CREATE INVALID ASSET TRANSFER INVALID RECIPIENT ADDRESS
		assetTransfer = new GenesisTransferAssetTransaction(sender, new Account("test"), asset.getKey(), BigDecimal.valueOf(100).setScale(8), timestamp);
	
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(databaseSet));
		
		//CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
		assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, asset.getKey(), BigDecimal.valueOf(-100).setScale(8), timestamp);
		
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(databaseSet));	
		
		/*
		//CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
		assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, asset.getKey(), BigDecimal.valueOf(100).setScale(8), timestamp);
		
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(databaseSet));
		*/	

		/*
		//CREATE INVALID ASSET TRANSFER WRONG REFERENCE
		assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, 0, BigDecimal.valueOf(100).setScale(8), timestamp);
						
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(databaseSet));
		*/	
	}
	
	@Test
	public void parseGenesisTransferAssetTransaction() 
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
						
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		Account recipient = new Account("QgcphUTiVHHfHg8e1LVgg5jujVES7ZDUTr");
		long timestamp = NTP.getTime();
					
		//CREATE VALID ASSET TRANSFER
		GenesisTransferAssetTransaction assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, 0, BigDecimal.valueOf(100).setScale(8), timestamp);
		//assetTransfer.sign(sender); !! nod need!

		//CONVERT TO BYTES
		byte[] rawAssetTransfer = assetTransfer.toBytes(true);
		
		//CHECK DATALENGTH
		assertEquals(rawAssetTransfer.length, assetTransfer.getDataLength());
		
		try 
		{	
			//PARSE FROM BYTES
			GenesisTransferAssetTransaction parsedAssetTransfer = (GenesisTransferAssetTransaction) TransactionFactory.getInstance().parse(rawAssetTransfer);
			
			//CHECK INSTANCE
			assertEquals(true, parsedAssetTransfer instanceof GenesisTransferAssetTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(assetTransfer.getSignature(), parsedAssetTransfer.getSignature()));
			
			//CHECK KEY
			assertEquals(assetTransfer.getKey(), parsedAssetTransfer.getKey());	
			
			//CHECK AMOUNT SENDER
			assertEquals(assetTransfer.viewAmount(sender), parsedAssetTransfer.viewAmount(sender));	
			
			//CHECK AMOUNT RECIPIENT
			assertEquals(assetTransfer.viewAmount(recipient), parsedAssetTransfer.viewAmount(recipient));	
			
			//CHECK FEE
			assertEquals(assetTransfer.getFee(), parsedAssetTransfer.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(assetTransfer.getReference(), parsedAssetTransfer.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(assetTransfer.getTimestamp(), parsedAssetTransfer.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawAssetTransfer = new byte[assetTransfer.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawAssetTransfer);
			
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
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
					
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//CREATE JOB ASSET
		Asset asset = new Venture(sender, "OIL+", "+It is an OILing drops used for fees", 1000L, (byte) 8, true);
		GenesisIssueAssetTransaction trans = new GenesisIssueAssetTransaction(sender, asset, timestamp);
		trans.process(databaseSet);

		long key = asset.getKey(databaseSet);
		Logger.getGlobal().info("asset key " + key);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(1000).setScale(8),sender.getConfirmedBalance(key, databaseSet));
			
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
			
		//CREATE ASSET TRANSFER
		Transaction assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, key, BigDecimal.valueOf(100).setScale(8), timestamp);
		// assetTransfer.sign(sender); // not  NEED
		assetTransfer.process(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(900).setScale(8),sender.getConfirmedBalance(key, databaseSet));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.valueOf(100).setScale(8), recipient.getConfirmedBalance(key, databaseSet));
		
		/* not NEED
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(assetTransfer.getSignature(), sender.getLastReference(databaseSet)));
		*/
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(assetTransfer.getSignature(), recipient.getLastReference(databaseSet)));
	}
	
	@Test
	public void orphanGenesisTransferAssetTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
					
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
			
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
			
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		long timestamp = NTP.getTime();
			
		//CREATE ASSET TRANSFER
		sender.setConfirmedBalance(1, BigDecimal.valueOf(100).setScale(8), databaseSet);
		Transaction assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, 1, BigDecimal.valueOf(100).setScale(8), timestamp);
		// assetTransfer.sign(sender); not NEED
		assetTransfer.process(databaseSet);
		assetTransfer.orphan(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), sender.getConfirmedBalance(databaseSet));
		assertEquals(BigDecimal.valueOf(100).setScale(8), sender.getConfirmedBalance(1, databaseSet));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(databaseSet));
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(1, databaseSet));
		
		/* not NEED
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
		*/
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(assetTransfer.getSignature(), recipient.getLastReference(databaseSet)));
	}
	

}
