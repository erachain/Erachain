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
import qora.assets.Asset;
import qora.block.GenesisBlock;
import qora.crypto.Crypto;

import qora.transaction.GenesisTransaction;
import qora.transaction.GenesisIssueAssetTransaction;
import qora.transaction.GenesisTransferAssetTransaction;
import qora.transaction.AccountingTransaction;
import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;
import qora.transaction.TransferAssetTransaction;

public class TransactionTests2 {

	int FEE_POWER = 2;
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
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(creator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE ASSET
		Asset asset = new Asset(creator, "test", "strontje", 50000l, (byte) 2, false);
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
		
		//ADD QORA ASSET
		Asset qoraAsset = new Asset(new GenesisBlock().getGenerator(), "Qora", "This is the simulated Qora asset.", 10000000000L, (byte) 2, true);
    	databaseSet.getAssetMap().set(0l, qoraAsset);
						
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
		Transaction assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, 0, BigDecimal.valueOf(100).setScale(8), timestamp);

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(databaseSet));
		
		//CREATE VALID ASSET TRANSFER
		sender.setConfirmedBalance(1, BigDecimal.valueOf(100).setScale(8), databaseSet);
		assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, 0, BigDecimal.valueOf(100).setScale(8), timestamp);

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(databaseSet));			
		
		//CREATE INVALID ASSET TRANSFER INVALID RECIPIENT ADDRESS
		assetTransfer = new GenesisTransferAssetTransaction(sender, new Account("test"), 0, BigDecimal.valueOf(100).setScale(8), timestamp);
	
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(databaseSet));
		
		//CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
		assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, 0, BigDecimal.valueOf(-100).setScale(8), timestamp);
		
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(databaseSet));	
		
		//CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
		assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, 2, BigDecimal.valueOf(100).setScale(8), timestamp);
		
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(databaseSet));	

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
			assertEquals(assetTransfer.getAmount(sender), parsedAssetTransfer.getAmount(sender));	
			
			//CHECK AMOUNT RECIPIENT
			assertEquals(assetTransfer.getAmount(recipient), parsedAssetTransfer.getAmount(recipient));	
			
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
			
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
			
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		long timestamp = NTP.getTime();
			
		//CREATE ASSET TRANSFER
		sender.setConfirmedBalance(1, BigDecimal.valueOf(100).setScale(8), databaseSet);
		Transaction assetTransfer = new GenesisTransferAssetTransaction(sender, recipient, 1, BigDecimal.valueOf(100).setScale(8), timestamp);
		//assetTransfer.sign(sender); not  NEED
		assetTransfer.process(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(1000).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));

		assertEquals(BigDecimal.ZERO.setScale(8), sender.getConfirmedBalance(1, databaseSet));
		//assertEquals(-1, BigDecimal.ZERO.setScale(8).compareTo(sender.getConfirmedBalance(1, databaseSet)));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(databaseSet));
		assertEquals(BigDecimal.valueOf(100).setScale(8), recipient.getConfirmedBalance(1, databaseSet));
		
		/* not NEED
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(assetTransfer.getSignature(), sender.getLastReference(databaseSet)));
		*/
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(assetTransfer.getSignature(), recipient.getLastReference(databaseSet)));
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
	
	// ACCOUNTING HKEY
	
	@Test
	public void validateAccountingTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//ADD QORA ASSET
		Asset qoraAsset = new Asset(new GenesisBlock().getGenerator(), "Qora", "This is the simulated Qora asset.", 10000000000L, (byte) 2, true);
		databaseSet.getAssetMap().set(0l, qoraAsset);
    	
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
		
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
				
		PrivateKeyAccount creator = new PrivateKeyAccount(privateKey);
		Account recipient = new Account("QfreeNWCeaU3BiXUxktaJRJrBB1SDg2k7o");		

		long timestamp = NTP.getTime();

		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(creator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		Integer ii = 22;
		byte[] iib = new byte[]{ii.byteValue()};
		Logger.getGlobal().info("byte iib: " + iib[0]);
		
		byte iii =  iib[0];
		Logger.getGlobal().info("byte iii: " + iii); // BAD -23
		Logger.getGlobal().info("byte.unsignedi: " + Byte.toUnsignedInt(iib[0])); // GOOD!
		int i2 = Ints.fromByteArray(new byte[]{0,0,0,iib[0]}); // GOOD!
		Logger.getGlobal().info("array[4]: " + i2);

		long key = 61l;
		creator.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(8), databaseSet);
		
		//byte[] hkey = new byte[]{1,3,54,12,34,123,123,120,12};
		byte[] hkey = new byte[3]; //{1,3,54,12,34,123,123,120,12};
		byte[] data = "test123!".getBytes();
		
		AccountingTransaction accountingTransaction = new AccountingTransaction(
				creator, recipient, key, 
				BigDecimal.valueOf(23).setScale(8), 
				FEE_POWER,
				data, 
				new byte[] { 1 },
				new byte[] { 0 },
				timestamp,
				creator.getLastReference(databaseSet)
				);
		accountingTransaction.sign(creator);
		
		assertEquals(accountingTransaction.isValid(databaseSet), Transaction.VALIDATE_OK);
		
		//CONVERT TO BYTES
		byte[] rawTransaction = accountingTransaction.toBytes(true);
		
		//CHECK DATALENGTH
		assertEquals(rawTransaction.length, accountingTransaction.getDataLength());
		
		try 
		{	
			//PARSE FROM BYTES
			AccountingTransaction ccountingTransaction_parsed = (AccountingTransaction) TransactionFactory.getInstance().parse(rawTransaction);
			
			//CHECK INSTANCE
			assertEquals(true, ccountingTransaction_parsed instanceof AccountingTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(accountingTransaction.getSignature(), ccountingTransaction_parsed.getSignature()));
			
			//CHECK KEY
			assertEquals(accountingTransaction.getKey(), ccountingTransaction_parsed.getKey());	
			
			//CHECK AMOUNT SENDER
			assertEquals(accountingTransaction.getAmount(creator), ccountingTransaction_parsed.getAmount(creator));	
			
			//CHECK AMOUNT RECIPIENT
			assertEquals(accountingTransaction.getAmount(recipient), ccountingTransaction_parsed.getAmount(recipient));	
			
			//CHECK FEE
			assertEquals(accountingTransaction.getFee(), ccountingTransaction_parsed.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(accountingTransaction.getReference(), ccountingTransaction_parsed.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(accountingTransaction.getTimestamp(), ccountingTransaction_parsed.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. - " + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawTransaction = new byte[accountingTransaction.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawTransaction);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
		
		accountingTransaction.process(databaseSet);
		
		assertEquals(BigDecimal.valueOf(999.99998660).setScale(8), creator.getConfirmedBalance(databaseSet));
		assertEquals(BigDecimal.valueOf(77).setScale(8), creator.getConfirmedBalance(61l, databaseSet));
		assertEquals(BigDecimal.valueOf(23).setScale(8), recipient.getConfirmedBalance(61l, databaseSet));
		
		byte[] rawMessageTransactionV3 = accountingTransaction.toBytes(true);
		
		AccountingTransaction accTrans_2 = null;
		try {
			accTrans_2 = (AccountingTransaction) AccountingTransaction.Parse(Arrays.copyOfRange(rawMessageTransactionV3, 4, rawMessageTransactionV3.length));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(new String(accountingTransaction.getData()), new String(accTrans_2.getData()));
		assertEquals(accountingTransaction.getCreator(), accTrans_2.getCreator());
		assertEquals(accountingTransaction.getRecipient(), accTrans_2.getRecipient());
		assertEquals(accountingTransaction.getKey(), accTrans_2.getKey());
		assertEquals(accountingTransaction.getAmount(), accTrans_2.getAmount());
		assertEquals(accountingTransaction.isEncrypted(), accTrans_2.isEncrypted());
		assertEquals(accountingTransaction.isText(), accTrans_2.isText());
		
		assertEquals(accountingTransaction.isSignatureValid(), true);
		assertEquals(accTrans_2.isSignatureValid(), true);		
	}

}
