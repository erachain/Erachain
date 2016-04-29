package test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
 import org.apache.log4j.Logger;

import ntp.NTP;

import org.junit.Test;

import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.assets.AssetUnique;
import core.item.assets.AssetVenture;
import core.transaction.CancelOrderTransaction;
import core.transaction.CreateOrderTransaction;
import core.transaction.IssueAssetTransaction;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import core.transaction.TransferAssetTransaction;
import core.transaction.MessageTransaction;

//import com.google.common.primitives.Longs;

import database.DBSet;

public class TestRecAsset {

	static Logger LOGGER = Logger.getLogger(TestRecAsset.class.getName());

	byte[] releaserReference = null;

	long FEE_KEY = AssetCls.DILE_KEY;
	byte FEE_POWER = (byte)1;
	byte[] assetReference = new byte[64];
	long timestamp = NTP.getTime();
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("tes213sdffsdft".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
	
	AssetCls asset;
	long key = -1;

	// INIT ASSETS
	private void init() {
		
		db = DBSet.createEmptyDatabaseSet();
		gb = new GenesisBlock();
		gb.process(db);
		
		// OIL FUND
		maker.setLastReference(gb.getGeneratorSignature(), db);
		maker.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);
		
		asset = new AssetVenture(maker, "aasdasd", "asdasda", 50000l, (byte) 2, true);
		//key = asset.getKey();


	}
	
	
	//ISSUE ASSET TRANSACTION
	
	@Test
	public void validateSignatureIssueAssetTransaction() 
	{
		
		init();
		
		//CREATE ASSET
		AssetUnique asset = new AssetUnique(maker, "test", "strontje");
				
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastReference(db));
		issueAssetTransaction.sign(maker, false);
		
		//CHECK IF ISSUE ASSET TRANSACTION IS VALID
		assertEquals(true, issueAssetTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastReference(db), new byte[64]);
		
		//CHECK IF ISSUE ASSET IS INVALID
		assertEquals(false, issueAssetTransaction.isSignatureValid());
	}
		

	
	@Test
	public void parseIssueAssetTransaction() 
	{
		
		init();
		
		//CREATE SIGNATURE
		AssetUnique asset = new AssetUnique(maker, "test", "strontje");
		LOGGER.info("asset: " + asset.getType()[0] + ", " + asset.getType()[1]);
		byte [] raw = asset.toBytes(false);
		assertEquals(raw.length, asset.getDataLength(false));
		asset.setReference(new byte[64]);
		raw = asset.toBytes(true);
		assertEquals(raw.length, asset.getDataLength(true));
				
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastReference(db));
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(db, false);
		
		//CONVERT TO BYTES
		byte[] rawIssueAssetTransaction = issueAssetTransaction.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawIssueAssetTransaction.length, issueAssetTransaction.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			IssueAssetTransaction parsedIssueAssetTransaction = (IssueAssetTransaction) TransactionFactory.getInstance().parse(rawIssueAssetTransaction, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedIssueAssetTransaction instanceof IssueAssetTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(issueAssetTransaction.getSignature(), parsedIssueAssetTransaction.getSignature()));
			
			//CHECK ISSUER
			assertEquals(issueAssetTransaction.getCreator().getAddress(), parsedIssueAssetTransaction.getCreator().getAddress());
			
			//CHECK OWNER
			assertEquals(issueAssetTransaction.getItem().getCreator().getAddress(), parsedIssueAssetTransaction.getItem().getCreator().getAddress());
			
			//CHECK NAME
			assertEquals(issueAssetTransaction.getItem().getName(), parsedIssueAssetTransaction.getItem().getName());
				
			//CHECK DESCRIPTION
			assertEquals(issueAssetTransaction.getItem().getDescription(), parsedIssueAssetTransaction.getItem().getDescription());
				
			//CHECK QUANTITY
			assertEquals(((AssetCls)issueAssetTransaction.getItem()).getQuantity(), ((AssetCls)parsedIssueAssetTransaction.getItem()).getQuantity());
			
			//DIVISIBLE
			assertEquals(((AssetCls)issueAssetTransaction.getItem()).isDivisible(), ((AssetCls)parsedIssueAssetTransaction.getItem()).isDivisible());
			
			//CHECK FEE
			assertEquals(issueAssetTransaction.getFee(), parsedIssueAssetTransaction.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(issueAssetTransaction.getReference(), parsedIssueAssetTransaction.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(issueAssetTransaction.getTimestamp(), parsedIssueAssetTransaction.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawIssueAssetTransaction = new byte[issueAssetTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawIssueAssetTransaction, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	
	@Test
	public void processIssueAssetTransaction()
	{
		
		init();				
		
		AssetUnique asset = new AssetUnique(maker, "test", "strontje");
				
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastReference(db));
		issueAssetTransaction.sign(maker, false);
		
		assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(db, releaserReference));
		
		issueAssetTransaction.process(db, false);
		
		LOGGER.info("asset KEY: " + asset.getKey());
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.valueOf(1).setScale(8), maker.getConfirmedBalance(asset.getKey(), db));
		
		//CHECK ASSET EXISTS SENDER
		long key = db.getIssueAssetMap().get(issueAssetTransaction);
		assertEquals(true, db.getItemAssetMap().contains(key));
		
		//CHECK ASSET IS CORRECT
		assertEquals(true, Arrays.equals(db.getItemAssetMap().get(key).toBytes(true), asset.toBytes(true)));
		
		//CHECK ASSET BALANCE SENDER
		assertEquals(true, db.getAssetBalanceMap().get(maker.getAddress(), key).compareTo(new BigDecimal(asset.getQuantity())) == 0);
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueAssetTransaction.getSignature(), maker.getLastReference(db)));
	}
	
	
	@Test
	public void orphanIssueAssetTransaction()
	{
		
		init();				
				
		AssetUnique asset = new AssetUnique(maker, "test", "strontje");
				
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastReference(db));
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(db, false);
		long key = db.getIssueAssetMap().get(issueAssetTransaction);
		assertEquals(new BigDecimal(1).setScale(8), maker.getConfirmedBalance(key,db));
		assertEquals(true, Arrays.equals(issueAssetTransaction.getSignature(), maker.getLastReference(db)));
		
		issueAssetTransaction.orphan(db, false);
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.ZERO.setScale(8), maker.getConfirmedBalance(key,db));
		
		//CHECK ASSET EXISTS SENDER
		assertEquals(false, db.getItemAssetMap().contains(key));
		
		//CHECK ASSET BALANCE SENDER
		assertEquals(0, db.getAssetBalanceMap().get(maker.getAddress(), key).longValue());
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueAssetTransaction.getReference(), maker.getLastReference(db)));
	}
	

	//TRANSFER ASSET
	
	@Test
	public void validateSignatureTransferAssetTransaction() 
	{
		
		init();
		
		AssetUnique asset = new AssetUnique(maker, "test", "strontje");
				
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastReference(db));
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(db, false);
		long key = db.getIssueAssetMap().get(issueAssetTransaction);

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		
		//CREATE ASSET TRANSFER
		Transaction assetTransfer = new TransferAssetTransaction(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));
		assetTransfer.sign(maker, false);
		
		//CHECK IF ASSET TRANSFER SIGNATURE IS VALID
		assertEquals(true, assetTransfer.isSignatureValid());
		
		//INVALID SIGNATURE
		assetTransfer = new TransferAssetTransaction(maker, recipient, 0, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));
		assetTransfer.sign(maker, false);
		assetTransfer = new TransferAssetTransaction(maker, recipient, 0, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp+1, maker.getLastReference(db), assetTransfer.getSignature());
		
		//CHECK IF ASSET TRANSFER SIGNATURE IS INVALID
		assertEquals(false, assetTransfer.isSignatureValid());
	}
	
	@Test
	public void validateTransferAssetTransaction() 
	{	
		
		init();
						
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(db, releaserReference));

		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(db, false);
		long key = asset.getKey();
		//assertEquals(asset.getQuantity(), maker.getConfirmedBalance(OIL_KEY, db));
		assertEquals(new BigDecimal(asset.getQuantity()).setScale(8), maker.getConfirmedBalance(key, db));
		
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
				
		//CREATE VALID ASSET TRANSFER
		Transaction assetTransfer = new TransferAssetTransaction(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));
		
		assetTransfer.sign(maker, false);
		assetTransfer.process(db, false);
		
		//CREATE VALID ASSET TRANSFER
		//maker.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(8), db);
		assetTransfer = new TransferAssetTransaction(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));			
		
		//CREATE INVALID ASSET TRANSFER INVALID RECIPIENT ADDRESS
		assetTransfer = new TransferAssetTransaction(maker, new Account("test"), key, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));
	
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));
		
		//CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
		assetTransfer = new TransferAssetTransaction(maker, recipient, key, BigDecimal.valueOf(-100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));
		
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));	
		
		//CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
		assetTransfer = new TransferAssetTransaction(maker, recipient, 0, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));
		//assetTransfer.sign(maker, false);
		//assetTransfer.process(db, false);
		
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));	
						
		//CREATE INVALID ASSET TRANSFER WRONG REFERENCE
		assetTransfer = new TransferAssetTransaction(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, new byte[64]);
						
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));	
	}
	
	@Test
	public void parseTransferAssetTransaction() 
	{

		init();
		
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();
					
		//CREATE VALID ASSET TRANSFER
		TransferAssetTransaction assetTransfer = new TransferAssetTransaction(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));
		assetTransfer.sign(maker, false);

		//CONVERT TO BYTES
		byte[] rawAssetTransfer = assetTransfer.toBytes(true, releaserReference);
		
		//CHECK DATALENGTH
		assertEquals(rawAssetTransfer.length, assetTransfer.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			TransferAssetTransaction parsedAssetTransfer = (TransferAssetTransaction) TransactionFactory.getInstance().parse(rawAssetTransfer, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedAssetTransfer instanceof TransferAssetTransaction);
			
			//CHECK TYPEBYTES
			assertEquals(true, Arrays.equals(assetTransfer.getTypeBytes(), parsedAssetTransfer.getTypeBytes()));				

			//CHECK TIMESTAMP
			assertEquals(assetTransfer.getTimestamp(), parsedAssetTransfer.getTimestamp());				

			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(assetTransfer.getReference(), parsedAssetTransfer.getReference()));	

			//CHECK CREATOR
			assertEquals(assetTransfer.getCreator().getAddress(), parsedAssetTransfer.getCreator().getAddress());				

			//CHECK FEE POWER
			assertEquals(assetTransfer.getFee(), parsedAssetTransfer.getFee());	

			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(assetTransfer.getSignature(), parsedAssetTransfer.getSignature()));
			
			//CHECK KEY
			assertEquals(assetTransfer.getKey(), parsedAssetTransfer.getKey());	
			
			//CHECK AMOUNT
			assertEquals(assetTransfer.viewAmount(maker), parsedAssetTransfer.viewAmount(maker));	
			
			//CHECK AMOUNT RECIPIENT
			assertEquals(assetTransfer.viewAmount(recipient), parsedAssetTransfer.viewAmount(recipient));	
						
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction." + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawAssetTransfer = new byte[assetTransfer.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawAssetTransfer, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void processTransferAssetTransaction()
	{

		init();
		
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();
			
		//CREATE ASSET TRANSFER
		maker.setConfirmedBalance(key, BigDecimal.valueOf(200).setScale(8), db);
		Transaction assetTransfer = new TransferAssetTransaction(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));
		assetTransfer.sign(maker, false);
		assetTransfer.isValid(db, releaserReference);
		assetTransfer.process(db, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(100).setScale(8), maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(100).setScale(8), recipient.getConfirmedBalance(key, db));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(assetTransfer.getSignature(), maker.getLastReference(db)));
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(assetTransfer.getSignature(), recipient.getLastReference(db)));
	}
	
	@Test
	public void orphanTransferAssetTransaction()
	{
		
		init();
		
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();
			
		//CREATE ASSET TRANSFER
		long key = 1l;
		maker.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(8), db);
		Transaction assetTransfer = new TransferAssetTransaction(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));
		assetTransfer.sign(maker, false);
		assetTransfer.process(db, false);
		assetTransfer.orphan(db, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(100).setScale(8), maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(key, db));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(assetTransfer.getReference(), maker.getLastReference(db)));
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(assetTransfer.getSignature(), recipient.getLastReference(db)));
	}

	

	//MESSAGE ASSET
	
	@Test
	public void validateSignatureMessageTransaction() 
	{
		
		init();
		
		//AssetUnique asset = new AssetUnique(maker, "test", "strontje");
				
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastReference(db));
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(db, false);
		long key = db.getIssueAssetMap().get(issueAssetTransaction);

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		
		//CREATE ASSET TRANSFER
		Transaction messageTransaction = new MessageTransaction(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(8),
				"wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastReference(db));
		messageTransaction.sign(maker, false);
		
		//CHECK IF ASSET TRANSFER SIGNATURE IS VALID
		assertEquals(true, messageTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		messageTransaction = new MessageTransaction(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(8),
				"wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastReference(db));
		messageTransaction.sign(maker, false);
		messageTransaction = new MessageTransaction(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(8), 
				"wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp+1, maker.getLastReference(db), messageTransaction.getSignature());
		
		//CHECK IF ASSET TRANSFER SIGNATURE IS INVALID
		assertEquals(false, messageTransaction.isSignatureValid());
	}
	
	@Test
	public void validateMessageTransaction() 
	{	
		
		init();
						
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueMessageTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, issueMessageTransaction.isValid(db, releaserReference));

		issueMessageTransaction.sign(maker, false);
		issueMessageTransaction.process(db, false);
		long key = asset.getKey();
		//assertEquals(asset.getQuantity(), maker.getConfirmedBalance(OIL_KEY, db));
		assertEquals(new BigDecimal(asset.getQuantity()).setScale(8), maker.getConfirmedBalance(key, db));
		
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
				
		//CREATE VALID ASSET TRANSFER
		Transaction messageTransaction = new MessageTransaction(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(8),
				"wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastReference(db));

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, messageTransaction.isValid(db, releaserReference));
		
		messageTransaction.sign(maker, false);
		messageTransaction.process(db, false);
		
		//CREATE VALID ASSET TRANSFER
		//maker.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(8), db);
		messageTransaction = new MessageTransaction(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(8),
				"wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastReference(db));

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, messageTransaction.isValid(db, releaserReference));			
		
		//CREATE INVALID ASSET TRANSFER INVALID RECIPIENT ADDRESS
		messageTransaction = new MessageTransaction(maker, FEE_POWER, new Account("test"), key, BigDecimal.valueOf(100).setScale(8),
				"wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastReference(db));
	
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, messageTransaction.isValid(db, releaserReference));
		
		//CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
		messageTransaction = new MessageTransaction(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(-100).setScale(8),
				"wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastReference(db));
		
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, messageTransaction.isValid(db, releaserReference));	
		
		//CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
		messageTransaction = new MessageTransaction(maker, FEE_POWER, recipient, 0, BigDecimal.valueOf(100).setScale(8),
				"wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastReference(db));
		//messageTransaction.sign(maker, false);
		//messageTransaction.process(db, false);
		
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, messageTransaction.isValid(db, releaserReference));	
						
		//CREATE INVALID ASSET TRANSFER WRONG REFERENCE
		messageTransaction = new MessageTransaction(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(8),
				"wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, new byte[64]);
						
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, messageTransaction.isValid(db, releaserReference));	
	}
	
	@Test
	public void parseMessageTransaction() 
	{

		init();
		
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();
					
		//CREATE VALID ASSET TRANSFER
		MessageTransaction messageTransaction = new MessageTransaction(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(8),
				"wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastReference(db));
		messageTransaction.sign(maker, false);

		//CONVERT TO BYTES
		byte[] rawAssetTransfer = messageTransaction.toBytes(true, releaserReference);
		
		//CHECK DATALENGTH
		assertEquals(rawAssetTransfer.length, messageTransaction.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			MessageTransaction parsedAssetTransfer = (MessageTransaction) TransactionFactory.getInstance().parse(rawAssetTransfer, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedAssetTransfer instanceof MessageTransaction);
			
			//CHECK TYPEBYTES
			assertEquals(true, Arrays.equals(messageTransaction.getTypeBytes(), parsedAssetTransfer.getTypeBytes()));				

			//CHECK TIMESTAMP
			assertEquals(messageTransaction.getTimestamp(), parsedAssetTransfer.getTimestamp());				

			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(messageTransaction.getReference(), parsedAssetTransfer.getReference()));	

			//CHECK CREATOR
			assertEquals(messageTransaction.getCreator().getAddress(), parsedAssetTransfer.getCreator().getAddress());				

			//CHECK FEE POWER
			assertEquals(messageTransaction.getFee(), parsedAssetTransfer.getFee());	

			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(messageTransaction.getSignature(), parsedAssetTransfer.getSignature()));
			
			//CHECK KEY
			assertEquals(messageTransaction.getKey(), parsedAssetTransfer.getKey());	
			
			//CHECK AMOUNT
			assertEquals(messageTransaction.viewAmount(maker), parsedAssetTransfer.viewAmount(maker));	
			
			//CHECK AMOUNT RECIPIENT
			assertEquals(messageTransaction.viewAmount(recipient), parsedAssetTransfer.viewAmount(recipient));	
						
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction." + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawAssetTransfer = new byte[messageTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawAssetTransfer, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void processMessageTransaction()
	{

		init();
		
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();
			
		//CREATE ASSET TRANSFER
		maker.setConfirmedBalance(key, BigDecimal.valueOf(200).setScale(8), db);
		Transaction messageTransaction = new MessageTransaction(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(8),
				"wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastReference(db));
		messageTransaction.sign(maker, false);
		messageTransaction.process(db, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(100).setScale(8), maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(100).setScale(8), recipient.getConfirmedBalance(key, db));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(messageTransaction.getSignature(), maker.getLastReference(db)));
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(messageTransaction.getSignature(), recipient.getLastReference(db)));
	}
	
	@Test
	public void orphanMessageTransaction()
	{
		
		init();
		
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();
			
		//CREATE ASSET TRANSFER
		long key = 1l;
		maker.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(8), db);
		Transaction messageTransaction = new MessageTransaction(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(8),
				"wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastReference(db));
		messageTransaction.sign(maker, false);
		messageTransaction.process(db, false);
		messageTransaction.orphan(db, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(100).setScale(8), maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(FEE_KEY, db));
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(key, db));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(messageTransaction.getReference(), maker.getLastReference(db)));
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(messageTransaction.getSignature(), recipient.getLastReference(db)));
	}

	
	//CANCEL ORDER
	
	@Test
	public void validateSignatureCancelOrderTransaction()
	{
		

		init();
		
		//CREATE ORDER CANCEL
		Transaction cancelOrderTransaction = new CancelOrderTransaction(maker, BigInteger.TEN, FEE_POWER, timestamp, maker.getLastReference(db));
		cancelOrderTransaction.sign(maker, false);
		//CHECK IF ORDER CANCEL IS VALID
		assertEquals(true, cancelOrderTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		cancelOrderTransaction = new CancelOrderTransaction(maker, BigInteger.TEN, FEE_POWER, timestamp, maker.getLastReference(db), new byte[1]);
		
		//CHECK IF ORDER CANCEL
		assertEquals(false, cancelOrderTransaction.isSignatureValid());
	}
	
	@Test
	public void validateCancelOrderTransaction() 
	{

		init();
				
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, System.currentTimeMillis(), maker.getLastReference(db), new byte[64]);
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(db, false);
		//LOGGER.info("MessageTransaction .creator.getBalance(1, db): " + account.getBalance(1, dbSet));
		key = asset.getKey();

		//CREATE ORDER
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(maker, key, FEE_KEY, BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(0.1).setScale(8), FEE_POWER, System.currentTimeMillis(), maker.getLastReference(db), new byte[]{5,6});
		createOrderTransaction.sign(maker, false);
		createOrderTransaction.process(db, false);
		
		//this.creator.getBalance(1, db).compareTo(this.fee) == -1)
		//LOGGER.info("createOrderTransaction.creator.getBalance(1, db): " + createOrderTransaction.getCreator().getBalance(1, dbSet));
		//LOGGER.info("CreateOrderTransaction.creator.getBalance(1, db): " + account.getBalance(1, dbSet));

		//CREATE CANCEL ORDER
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(maker, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), maker.getLastReference(db), new byte[]{1,2});		
		//CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(account, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), account.getLastReference(dbSet));
		//cancelOrderTransaction.sign(account);
		//CHECK IF CANCEL ORDER IS VALID
		assertEquals(Transaction.VALIDATE_OK, cancelOrderTransaction.isValid(db, releaserReference));
		
		//CREATE INVALID CANCEL ORDER ORDER DOES NOT EXIST
		cancelOrderTransaction = new CancelOrderTransaction(maker, new BigInteger(new byte[]{5,7}), FEE_POWER, System.currentTimeMillis(), maker.getLastReference(db), new byte[]{1,2});		
		
		//CHECK IF CANCEL ORDER IS INVALID
		assertEquals(Transaction.ORDER_DOES_NOT_EXIST, cancelOrderTransaction.isValid(db, releaserReference));
		
		//CREATE INVALID CANCEL ORDER INCORRECT CREATOR
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidCreator = new PrivateKeyAccount(privateKey);
		cancelOrderTransaction = new CancelOrderTransaction(invalidCreator, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), maker.getLastReference(db), new byte[]{1,2});		
		
		//CHECK IF CANCEL ORDER IS INVALID
		assertEquals(Transaction.INVALID_ORDER_CREATOR, cancelOrderTransaction.isValid(db, releaserReference));
				
		//CREATE INVALID CANCEL ORDER NO BALANCE
		DBSet fork = db.fork();
		cancelOrderTransaction = new CancelOrderTransaction(maker, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), maker.getLastReference(db), new byte[]{1,2});		
		maker.setConfirmedBalance(FEE_KEY, BigDecimal.ZERO, fork);		
		
		//CHECK IF CANCEL ORDER IS INVALID
		assertEquals(Transaction.NOT_ENOUGH_FEE, cancelOrderTransaction.isValid(fork, releaserReference));
				
		//CREATE CANCEL ORDER INVALID REFERENCE
		cancelOrderTransaction = new CancelOrderTransaction(maker, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), new byte[64], new byte[]{1,2});		
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, cancelOrderTransaction.isValid(db, releaserReference));
		
	}

	@Test
	public void parseCancelOrderTransaction() 
	{
		

		init();
		
		//CREATE CANCEL ORDER
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(maker, BigInteger.TEN, FEE_POWER, timestamp, maker.getLastReference(db));
		cancelOrderTransaction.sign(maker, false);
		
		//CONVERT TO BYTES
		byte[] rawCancelOrder = cancelOrderTransaction.toBytes(true, null);
		
		//CHECK DATALENGTH
		assertEquals(rawCancelOrder.length, cancelOrderTransaction.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			CancelOrderTransaction parsedCancelOrder = (CancelOrderTransaction) TransactionFactory.getInstance().parse(rawCancelOrder, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedCancelOrder instanceof CancelOrderTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(cancelOrderTransaction.getSignature(), parsedCancelOrder.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(cancelOrderTransaction.viewAmount(maker), parsedCancelOrder.viewAmount(maker));	
			
			//CHECK OWNER
			assertEquals(cancelOrderTransaction.getCreator().getAddress(), parsedCancelOrder.getCreator().getAddress());	
			
			//CHECK ORDER
			assertEquals(0, cancelOrderTransaction.getOrder().compareTo(parsedCancelOrder.getOrder()));	
			
			//CHECK FEE
			assertEquals(cancelOrderTransaction.getFee(), parsedCancelOrder.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(cancelOrderTransaction.getReference(), parsedCancelOrder.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(cancelOrderTransaction.getTimestamp(), parsedCancelOrder.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawCancelOrder = new byte[cancelOrderTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawCancelOrder, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void processCancelOrderTransaction()
	{

		init();
		
		//CREATE ASSET
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, System.currentTimeMillis(), maker.getLastReference(db), new byte[64]);
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(db, false);
		key = asset.getKey();
		
		//CREATE ORDER
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(maker, key, FEE_KEY, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(100).setScale(8), FEE_POWER, System.currentTimeMillis(), maker.getLastReference(db), new byte[]{5,6});
		createOrderTransaction.sign(maker, false);
		createOrderTransaction.process(db, false);
		
		//CREATE CANCEL ORDER
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(maker, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), maker.getLastReference(db), new byte[]{1,2});
		cancelOrderTransaction.sign(maker, false);
		cancelOrderTransaction.process(db, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(asset.getQuantity()).setScale(8), maker.getConfirmedBalance(key, db));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(cancelOrderTransaction.getSignature(), maker.getLastReference(db)));
				
		//CHECK ORDER EXISTS
		assertEquals(false, db.getOrderMap().contains(new BigInteger(new byte[]{5,6})));
	}

	@Test
	public void orphanCancelOrderTransaction()
	{
		init();
		
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, System.currentTimeMillis(), maker.getLastReference(db));
		issueAssetTransaction.sign(maker, false);
		assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(db, releaserReference));
		issueAssetTransaction.process(db, false);

		long key = asset.getKey();
		LOGGER.info("asset.getReg(): " + asset.getReference());
		LOGGER.info("asset.getKey(): " + key);

		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(50000).setScale(8), maker.getConfirmedBalance(key, db));
		
		//CREATE ORDER
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(maker, key, FEE_KEY, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(1).setScale(8), FEE_POWER, System.currentTimeMillis(), maker.getLastReference(db), new byte[]{5,6});
		createOrderTransaction.sign(maker, false);
		createOrderTransaction.process(db, false);

		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(49000).setScale(8), maker.getConfirmedBalance(key, db));
		
		//CREATE CANCEL ORDER
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(maker, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), maker.getLastReference(db), new byte[]{1,2});
		cancelOrderTransaction.sign(maker, false);
		cancelOrderTransaction.process(db, false);
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(50000).setScale(8), maker.getConfirmedBalance( key, db));
		cancelOrderTransaction.orphan(db, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(49000).setScale(8), maker.getConfirmedBalance( key, db));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(createOrderTransaction.getSignature(), maker.getLastReference(db)));
				
		//CHECK ORDER EXISTS
		assertEquals(true, db.getOrderMap().contains(new BigInteger(new byte[]{5,6})));
	}
	
}
