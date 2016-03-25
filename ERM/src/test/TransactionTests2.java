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
import qora.transaction.IssueAssetTransaction;
import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;
import qora.transaction.RecStatement;;

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
	Asset asset;
	long key = -1l;
	GenesisIssueAssetTransaction genesisIssueAssetTransaction;
	
	private void initIssue(boolean toProcess) {
	
		//CREATE EMPTY MEMORY DATABASE
		db = DBSet.createEmptyDatabaseSet();
		
		//CREATE ASSET
		asset = GenesisBlock.makeVenture(0);
		//byte[] rawAsset = asset.toBytes(true); // reference is new byte[64]
		//assertEquals(rawAsset.length, asset.getDataLength());
				
		//CREATE ISSUE ASSET TRANSACTION
		genesisIssueAssetTransaction = new GenesisIssueAssetTransaction(maker, asset, timestamp);
		if (toProcess)
		{ 
			genesisIssueAssetTransaction.process(db);
			key = asset.getKey(db);
		}
		
	}

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
		
		initIssue(false);
		
		//genesisIssueAssetTransaction.sign(creator);
		//CHECK IF ISSUE ASSET TRANSACTION IS VALID
		assertEquals(true, genesisIssueAssetTransaction.isSignatureValid());
		assertEquals(Transaction.VALIDATE_OK, genesisIssueAssetTransaction.isValid(db));
				
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

	
	@Test
	public void parseGenesisIssueAssetTransaction() 
	{
		
		initIssue(false);
		
		//CONVERT TO BYTES
		byte[] rawGenesisIssueAssetTransaction = genesisIssueAssetTransaction.toBytes(true);
		
		//CHECK DATA LENGTH
		assertEquals(rawGenesisIssueAssetTransaction.length, genesisIssueAssetTransaction.getDataLength());
		
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
			
			//CHECK OWNER
			assertEquals(genesisIssueAssetTransaction.getAsset().getCreator().getAddress(), parsedGenesisIssueAssetTransaction.getAsset().getCreator().getAddress());
			
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

	
	@Test
	public void processGenesisIssueAssetTransaction()
	{
		
		initIssue(true);		
		Logger.getGlobal().info("asset KEY: " + key);
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.valueOf(asset.getQuantity()).setScale(8), maker.getConfirmedBalance(key, db));
		
		//CHECK ASSET EXISTS SENDER
		long key = db.getIssueAssetMap().get(genesisIssueAssetTransaction);
		assertEquals(true, db.getAssetMap().contains(key));
		
		//CHECK ASSET IS CORRECT
		assertEquals(true, Arrays.equals(db.getAssetMap().get(key).toBytes(true), asset.toBytes(true)));
		
		//CHECK ASSET BALANCE SENDER
		assertEquals(true, db.getBalanceMap().get(maker.getAddress(), key).compareTo(new BigDecimal(asset.getQuantity())) == 0);
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getSignature(), maker.getLastReference(db)));
	}
	
	
	@Test
	public void orphanIssueAssetTransaction()
	{
		
		initIssue(true);

		assertEquals(new BigDecimal(asset.getQuantity()).setScale(8), maker.getConfirmedBalance(key, db));
		assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getSignature(), maker.getLastReference(db)));
		
		genesisIssueAssetTransaction.orphan(db);
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.ZERO.setScale(8), maker.getConfirmedBalance(key,db));
		
		//CHECK ASSET EXISTS SENDER
		assertEquals(false, db.getAssetMap().contains(key));
		
		//CHECK ASSET BALANCE SENDER
		assertEquals(0, db.getBalanceMap().get(maker.getAddress(), key).longValue());
				
		//CHECK REFERENCE SENDER
		// it for not genesis - assertEquals(true, Arrays.equals(genesisIssueAssetTransaction.getReference(), maker.getLastReference(db)));
		assertEquals(true, Arrays.equals(new byte[0], maker.getLastReference(db)));

	}

	//GENESIS TRANSFER ASSET
	
	@Test
	public void validateSignatureGenesisTransferAssetTransaction() 
	{
		
		initIssue(false);
		
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		long timestamp = NTP.getTime();
		
		//CREATE ASSET TRANSFER
		Transaction assetTransfer = new GenesisTransferAssetTransaction(maker, recipient, key
				, BigDecimal.valueOf(100).setScale(8), timestamp);
		//assetTransfer.sign(sender);
		
		//CHECK IF ASSET TRANSFER SIGNATURE IS VALID
		assertEquals(true, assetTransfer.isSignatureValid());		
	}
	
	@Test
	public void validateGenesisTransferAssetTransaction() 
	{
		
		initIssue(true);
		
		//CREATE SIGNATURE
		Account recipient = new Account("QgcphUTiVHHfHg8e1LVgg5jujVES7ZDUTr");

		//CREATE VALID ASSET TRANSFER
		Transaction assetTransfer = new GenesisTransferAssetTransaction(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), timestamp);

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db));

		assetTransfer.process(db);

		//CREATE VALID ASSET TRANSFER
		maker.setConfirmedBalance(1, BigDecimal.valueOf(100).setScale(8), db);
		assetTransfer = new GenesisTransferAssetTransaction(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), timestamp);

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db));			
		
		//CREATE INVALID ASSET TRANSFER INVALID RECIPIENT ADDRESS
		assetTransfer = new GenesisTransferAssetTransaction(maker, new Account("test"), key, BigDecimal.valueOf(100).setScale(8), timestamp);
	
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db));
		
		//CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
		assetTransfer = new GenesisTransferAssetTransaction(maker, recipient, key, BigDecimal.valueOf(-100).setScale(8), timestamp);
		
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db));	
		
		//CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
		assetTransfer = new GenesisTransferAssetTransaction(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), timestamp);
		
		//CHECK IF ASSET TRANSFER IS INVALID
		// nor need for genesis - assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db));
	}
	
	@Test
	public void parseGenesisTransferAssetTransaction() 
	{

		initIssue(true);		
		
		//CREATE SIGNATURE
		Account recipient = new Account("QgcphUTiVHHfHg8e1LVgg5jujVES7ZDUTr");

		//CREATE VALID ASSET TRANSFER
		GenesisTransferAssetTransaction genesisTransferAsset = new GenesisTransferAssetTransaction(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), timestamp);
		//genesisTransferAsset.sign(maker);
		//genesisTransferAsset.process(db);

		//CONVERT TO BYTES
		byte[] rawGenesisTransferAsset = genesisTransferAsset.toBytes(true);
		
		//CHECK DATALENGTH
		assertEquals(rawGenesisTransferAsset.length, genesisTransferAsset.getDataLength());
		
		try 
		{	
			//PARSE FROM BYTES
			GenesisTransferAssetTransaction parsedAssetTransfer = (GenesisTransferAssetTransaction) TransactionFactory.getInstance().parse(rawGenesisTransferAsset);
			Logger.getGlobal().info(" 1: " + parsedAssetTransfer.getKey() );

			//CHECK INSTANCE
			assertEquals(true, parsedAssetTransfer instanceof GenesisTransferAssetTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(genesisTransferAsset.getSignature(), parsedAssetTransfer.getSignature()));
			
			//CHECK KEY
			assertEquals(genesisTransferAsset.getKey(), parsedAssetTransfer.getKey());	
			
			//CHECK AMOUNT SENDER
			assertEquals(genesisTransferAsset.viewAmount(maker), parsedAssetTransfer.viewAmount(maker));	
			
			//CHECK AMOUNT RECIPIENT
			assertEquals(genesisTransferAsset.viewAmount(recipient), parsedAssetTransfer.viewAmount(recipient));	
			
			//CHECK FEE
			assertEquals(genesisTransferAsset.getFee(), parsedAssetTransfer.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(genesisTransferAsset.getReference(), parsedAssetTransfer.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(genesisTransferAsset.getTimestamp(), parsedAssetTransfer.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawGenesisTransferAsset = new byte[genesisTransferAsset.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawGenesisTransferAsset);
			
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
		
		//CHECK BALANCE SENDER
		assertEquals(total, maker.getConfirmedBalance(key, db));
			
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
			
		//CREATE ASSET TRANSFER
		Transaction assetTransfer = new GenesisTransferAssetTransaction(maker, recipient, key, amoSend, timestamp);
		// assetTransfer.sign(sender); // not  NEED
		assetTransfer.process(db);
		
		//CHECK BALANCE SENDER
		assertEquals(total.subtract(amoSend), maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(amoSend, recipient.getConfirmedBalance(key, db));
		
		/* not NEED
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(assetTransfer.getSignature(), sender.getLastReference(databaseSet)));
		*/
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(assetTransfer.getSignature(), recipient.getLastReference(db)));
	}
	
	@Test
	public void orphanGenesisTransferAssetTransaction()
	{
		
		initIssue(true);
		
		BigDecimal total = BigDecimal.valueOf(asset.getQuantity()).setScale(8);
		BigDecimal amoSend = BigDecimal.valueOf(100).setScale(8);
			
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
			
		//CREATE ASSET TRANSFER
		Transaction assetTransfer = new GenesisTransferAssetTransaction(maker, recipient, key, amoSend, timestamp);
		// assetTransfer.sign(sender); not NEED
		assetTransfer.process(db);
		assetTransfer.orphan(db);
		
		//CHECK BALANCE SENDER
		assertEquals(total, maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(key, db));
		
		/* not NEED
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
		*/
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(assetTransfer.getSignature(), recipient.getLastReference(db)));
	}
	

	// RECORD STATEMENT
	@Test
	public void validateRecStatement() 
	{
		
		init();
				
		byte[] data = "test123!1234567890987654321".getBytes();
		Logger.getGlobal().info("data.len: " + data.length);
		
		
		RecStatement recStatement = new RecStatement(
				RecStatement.makeProps(), maker, 
				FEE_POWER, 
				data,
				new byte[] { 1 },
				timestamp, maker.getLastReference(db)
				);
		recStatement.sign(maker);
		
		assertEquals(recStatement.isValid(db), Transaction.VALIDATE_OK);
		
		recStatement.process(db);
		
		//assertEquals(BigDecimal.valueOf(0.99999571).setScale(8), maker.getConfirmedBalance(OIL_KEY, db));
		
		byte[] rawRecStatement = recStatement.toBytes(true);

		assertEquals(rawRecStatement.length, recStatement.getDataLength());
		
		RecStatement recStatement_2 = null;
		try {
			recStatement_2 = (RecStatement) RecStatement.Parse(rawRecStatement);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(new String(recStatement.getData()), new String(recStatement_2.getData()));
		assertEquals(recStatement.getCreator(), recStatement_2.getCreator());
		assertEquals(recStatement.isText(), recStatement_2.isText());
		
		assertEquals(recStatement.isSignatureValid(), true);
		assertEquals(recStatement_2.isSignatureValid(), true);		
	}
	
	@Test
	public void types() 
	{
		int type = 153;
		byte[] data = new byte[]{1,2,13,-1,5,6,7,9};
		byte[] data2 = new byte[]{(byte)type,-33,0,0};
		Logger.getGlobal().info("ddd: " + data2[0]);
		
		//READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, Integer.BYTES);
		int type2 = Byte.toUnsignedInt(typeBytes[3]);
		Logger.getGlobal().info("type2: " + type2);

		//Logger.getGlobal().info("Integer.BYTES: " + Integer.BYTES);
		//Logger.getGlobal().info("typeBytes.len: " + typeBytes.length);
		Logger.getGlobal().info("Integer.bitCount(2): " + Integer.bitCount(12445678));
		String mask1 = Integer.toBinaryString(255); // 11111111
		int int1 = (int)29 & (int)255;
		Logger.getGlobal().info(int1 + " mask1: " + mask1 );

		
		
	}
	
}
