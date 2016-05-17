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
import core.transaction.R_Send;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;

//import com.google.common.primitives.Longs;

import database.DBSet;

public class TransactionTests3AssetsAsPack {

	static Logger LOGGER = Logger.getLogger(TransactionTests3AssetsAsPack.class.getName());

	byte[] releaserReference;
	static boolean asPack = false;
	
	long FEE_KEY = 1l;
	byte FEE_POWER = (byte)1;
	byte[] assetReference = new byte[64];
	long timestamp = 0l;
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
	
	AssetCls asset;
	long key = -1;

	// INIT ASSETS
	private void init() {
		
		db = DBSet.createEmptyDatabaseSet();
		gb = new GenesisBlock();
		gb.process(db);
		
		// FEE FUND
		maker.setLastReference(gb.getGeneratorSignature(), db);
		maker.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);
		
		asset = new AssetVenture(maker, "a", "a", 50000l, (byte) 2, true);
		//key = asset.getKey();

		releaserReference = maker.getLastReference(db);

	}
	
	
	//ISSUE ASSET TRANSACTION
	
	@Test
	public void validateSignatureIssueAssetTransaction() 
	{
		
		init();
		
		//CREATE ASSET
		AssetUnique asset = new AssetUnique(maker, "test", "strontje");
				
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, releaserReference);
		assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(db, releaserReference));

		issueAssetTransaction.sign(maker, asPack);
		
		//CHECK IF ISSUE ASSET TRANSACTION IS VALID
		assertEquals(true, issueAssetTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, releaserReference, new byte[64]);
		
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
		boolean includeReference = false;
		byte [] raw = asset.toBytes(includeReference);
		assertEquals(raw.length, asset.getDataLength(includeReference));

		asset.setReference(new byte[64]);
		raw = asset.toBytes(true);
		assertEquals(raw.length, asset.getDataLength(true));
				
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, releaserReference);
		issueAssetTransaction.sign(maker, asPack);
		issueAssetTransaction.process(db, asPack);
		
		//CONVERT TO BYTES
		byte[] rawIssueAssetTransaction = issueAssetTransaction.toBytes(true, releaserReference);
		
		//CHECK DATA LENGTH
		assertEquals(rawIssueAssetTransaction.length, issueAssetTransaction.getDataLength(asPack));
		
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
			fail("Exception while parsing transaction. " + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawIssueAssetTransaction = new byte[issueAssetTransaction.getDataLength(asPack)];
		
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
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, releaserReference);
		issueAssetTransaction.sign(maker, asPack);
		
		assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(db, releaserReference));
		
		issueAssetTransaction.process(db, asPack);
		
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
		assertEquals(true, Arrays.equals(issueAssetTransaction.getSignature(), releaserReference));
	}
	
	
	@Test
	public void orphanIssueAssetTransaction()
	{
		
		init();				
				
		AssetUnique asset = new AssetUnique(maker, "test", "strontje");
				
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, releaserReference);
		issueAssetTransaction.sign(maker, asPack);
		issueAssetTransaction.process(db, asPack);
		long key = db.getIssueAssetMap().get(issueAssetTransaction);
		assertEquals(new BigDecimal(1).setScale(8), maker.getConfirmedBalance(key,db));
		assertEquals(true, Arrays.equals(issueAssetTransaction.getSignature(), releaserReference));
		
		issueAssetTransaction.orphan(db, asPack);
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.ZERO.setScale(8), maker.getConfirmedBalance(key,db));
		
		//CHECK ASSET EXISTS SENDER
		assertEquals(false, db.getItemAssetMap().contains(key));
		
		//CHECK ASSET BALANCE SENDER
		assertEquals(0, db.getAssetBalanceMap().get(maker.getAddress(), key).longValue());
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueAssetTransaction.getReference(), releaserReference));
	}
	

	//TRANSFER ASSET
	
	@Test
	public void validateSignatureR_Send() 
	{
		
		init();
		
		AssetUnique asset = new AssetUnique(maker, "test", "strontje");
				
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, releaserReference);
		issueAssetTransaction.sign(maker, asPack);
		issueAssetTransaction.process(db, asPack);
		long key = db.getIssueAssetMap().get(issueAssetTransaction);

		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		
		//CREATE ASSET TRANSFER
		Transaction assetTransfer = new R_Send(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), releaserReference);
		assetTransfer.sign(maker, asPack);
		
		//CHECK IF ASSET TRANSFER SIGNATURE IS VALID
		assertEquals(true, assetTransfer.isSignatureValid());
		
		//INVALID SIGNATURE
		assetTransfer = new R_Send(maker, recipient, 0, BigDecimal.valueOf(100).setScale(8), releaserReference);
		assetTransfer.sign(maker, asPack);
		assetTransfer = new R_Send(maker, recipient, 0, BigDecimal.valueOf(101).setScale(8),assetTransfer.getSignature());
		
		//CHECK IF ASSET TRANSFER SIGNATURE IS INVALID
		assertEquals(false, assetTransfer.isSignatureValid());
	}
	
	@Test
	public void validateR_Send() 
	{	
		
		init();
						
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, releaserReference);
		issueAssetTransaction.sign(maker, asPack);
		assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(db, releaserReference));
		
		issueAssetTransaction.process(db, asPack);
		long key = asset.getKey();
		//assertEquals(asset.getQuantity(), maker.getConfirmedBalance(FEE_KEY, db));
		assertEquals(new BigDecimal(asset.getQuantity()).setScale(8), maker.getConfirmedBalance(key, db));
		
		//CREATE SIGNATURE
		Account recipient = new Account("QgcphUTiVHHfHg8e1LVgg5jujVES7ZDUTr");
				
		//CREATE VALID ASSET TRANSFER
		Transaction assetTransfer = new R_Send(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), releaserReference);
		assetTransfer.sign(maker, asPack);

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));
		
		assetTransfer.process(db, asPack);
		
		//CREATE VALID ASSET TRANSFER
		//maker.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(8), db);
		assetTransfer = new R_Send(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), releaserReference);

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));			
		
		//CREATE INVALID ASSET TRANSFER INVALID RECIPIENT ADDRESS
		assetTransfer = new R_Send(maker, new Account("test"), key, BigDecimal.valueOf(100).setScale(8), releaserReference);
	
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));
		
		//CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
		assetTransfer = new R_Send(maker, recipient, key, BigDecimal.valueOf(-100).setScale(8), releaserReference);
		
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));	
		
		//CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
		assetTransfer = new R_Send(maker, recipient, 0, BigDecimal.valueOf(100).setScale(8), releaserReference);
		assetTransfer.sign(maker, asPack);
		assetTransfer.process(db, asPack);
		
		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(db, releaserReference));	
						
	}
	
	@Test
	public void parseR_Send() 
	{

		init();
		
		//CREATE SIGNATURE
		Account recipient = new Account("QgcphUTiVHHfHg8e1LVgg5jujVES7ZDUTr");
					
		//CREATE VALID ASSET TRANSFER
		R_Send assetTransfer = new R_Send(maker, recipient, 0, BigDecimal.valueOf(100).setScale(8), releaserReference);
		assetTransfer.sign(maker, asPack);

		//CONVERT TO BYTES
		byte[] rawAssetTransfer = assetTransfer.toBytes(true, releaserReference);
		
		//CHECK DATALENGTH
		assertEquals(rawAssetTransfer.length, assetTransfer.getDataLength(asPack));
		
		try 
		{	
			//PARSE FROM BYTES
			R_Send parsedAssetTransfer = (R_Send) TransactionFactory.getInstance().parse(rawAssetTransfer, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedAssetTransfer instanceof R_Send);
			
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
						
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawAssetTransfer = new byte[assetTransfer.getDataLength(asPack)];
		
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
	public void processR_Send()
	{

		init();
		
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		byte[] maker_LastReference = releaserReference;
		byte[] recipient_LastReference = recipient.getLastReference(db);
			
		//CREATE ASSET TRANSFER
		long key = 221;
		maker.setConfirmedBalance(key, BigDecimal.valueOf(200).setScale(8), db);
		Transaction assetTransfer = new R_Send(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), releaserReference);
		assetTransfer.sign(maker, asPack);
		assetTransfer.process(db, asPack);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.ZERO.setScale(8), maker.getConfirmedBalance(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(100).setScale(8), maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(100).setScale(8), recipient.getConfirmedBalance(key, db));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(maker_LastReference, releaserReference));
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(recipient_LastReference, recipient.getLastReference(db)));
	}
	
	@Test
	public void orphanR_Send()
	{
		
		init();
		
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		byte[] maker_LastReference = releaserReference;
		byte[] recipient_LastReference = recipient.getLastReference(db);
			
		//CREATE ASSET TRANSFER
		long key = 1l;
		maker.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(8), db);
		Transaction assetTransfer = new R_Send(maker, recipient, key, BigDecimal.valueOf(100).setScale(8), releaserReference);
		assetTransfer.sign(maker, asPack);
		assetTransfer.process(db, asPack);
		assetTransfer.orphan(db, asPack);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.ZERO.setScale(8), maker.getConfirmedBalance(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(100).setScale(8), maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(FEE_KEY, db));
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(key, db));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(maker_LastReference, releaserReference));
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(recipient_LastReference, recipient.getLastReference(db)));
	}

	
	//CANCEL ORDER
	
	@Test
	public void validateSignatureCancelOrderTransaction()
	{
		

		init();
		
		//CREATE ORDER CANCEL
		Transaction cancelOrderTransaction = new CancelOrderTransaction(maker, BigInteger.TEN, FEE_POWER, timestamp, releaserReference);
		cancelOrderTransaction.sign(maker, asPack);
		//CHECK IF ORDER CANCEL IS VALID
		assertEquals(true, cancelOrderTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		cancelOrderTransaction = new CancelOrderTransaction(maker, BigInteger.TEN, FEE_POWER, timestamp, releaserReference, new byte[1]);
		
		//CHECK IF ORDER CANCEL
		assertEquals(false, cancelOrderTransaction.isSignatureValid());
	}
	
	@Test
	public void validateCancelOrderTransaction() 
	{

		init();
				
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, System.currentTimeMillis(), releaserReference, new byte[64]);
		issueAssetTransaction.sign(maker, asPack);
		issueAssetTransaction.process(db, asPack);
		//LOGGER.info("IssueAssetTransaction .creator.getBalance(1, db): " + account.getBalance(1, dbSet));
		key = asset.getKey();

		//CREATE ORDER
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(maker, key, FEE_KEY, BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(0.1).setScale(8), FEE_POWER, System.currentTimeMillis(), releaserReference, new byte[]{5,6});
		createOrderTransaction.sign(maker, asPack);
		createOrderTransaction.process(db, asPack);
		
		//this.creator.getBalance(1, db).compareTo(this.fee) == -1)
		//LOGGER.info("createOrderTransaction.creator.getBalance(1, db): " + createOrderTransaction.getCreator().getBalance(1, dbSet));
		//LOGGER.info("CreateOrderTransaction.creator.getBalance(1, db): " + account.getBalance(1, dbSet));

		//CREATE CANCEL ORDER
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(maker, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), releaserReference, new byte[]{1,2});		
		//CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(account, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), account.getLastReference(dbSet));
		//cancelOrderTransaction.sign(account);
		//CHECK IF CANCEL ORDER IS VALID
		assertEquals(Transaction.VALIDATE_OK, cancelOrderTransaction.isValid(db, releaserReference));
		
		//CREATE INVALID CANCEL ORDER ORDER DOES NOT EXIST
		cancelOrderTransaction = new CancelOrderTransaction(maker, new BigInteger(new byte[]{5,7}), FEE_POWER, System.currentTimeMillis(), releaserReference, new byte[]{1,2});		
		
		//CHECK IF CANCEL ORDER IS INVALID
		assertEquals(Transaction.ORDER_DOES_NOT_EXIST, cancelOrderTransaction.isValid(db, releaserReference));
		
		//CREATE INVALID CANCEL ORDER INCORRECT CREATOR
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidCreator = new PrivateKeyAccount(privateKey);
		cancelOrderTransaction = new CancelOrderTransaction(invalidCreator, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), releaserReference, new byte[]{1,2});		
		
		//CHECK IF CANCEL ORDER IS INVALID
		assertEquals(Transaction.INVALID_ORDER_CREATOR, cancelOrderTransaction.isValid(db, releaserReference));
				
		//CREATE INVALID CANCEL ORDER NO BALANCE
		DBSet fork = db.fork();
		cancelOrderTransaction = new CancelOrderTransaction(maker, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), releaserReference, new byte[]{1,2});		
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
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(maker, BigInteger.TEN, FEE_POWER, timestamp, releaserReference);
		cancelOrderTransaction.sign(maker, asPack);
		
		//CONVERT TO BYTES
		byte[] rawCancelOrder = cancelOrderTransaction.toBytes(true, releaserReference);
		
		//CHECK DATALENGTH
		assertEquals(rawCancelOrder.length, cancelOrderTransaction.getDataLength(asPack));
		
		try 
		{	
			//PARSE FROM BYTES
			CancelOrderTransaction parsedCancelOrder = (CancelOrderTransaction) TransactionFactory.getInstance().parse(rawCancelOrder, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedCancelOrder instanceof CancelOrderTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(cancelOrderTransaction.getSignature(), parsedCancelOrder.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(cancelOrderTransaction.getAmount(maker), parsedCancelOrder.getAmount(maker));	
			
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
		rawCancelOrder = new byte[cancelOrderTransaction.getDataLength(asPack)];
		
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
		Transaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, System.currentTimeMillis(), releaserReference, new byte[64]);
		issueAssetTransaction.sign(maker, asPack);
		issueAssetTransaction.process(db, asPack);
		key = asset.getKey();
		
		//CREATE ORDER
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(maker, key, FEE_KEY, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(100).setScale(8), FEE_POWER, System.currentTimeMillis(), releaserReference, new byte[]{5,6});
		createOrderTransaction.sign(maker, asPack);
		createOrderTransaction.process(db, asPack);
		
		//CREATE CANCEL ORDER
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(maker, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), releaserReference, new byte[]{1,2});
		cancelOrderTransaction.sign(maker, asPack);
		cancelOrderTransaction.process(db, asPack);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(asset.getQuantity()).setScale(8), maker.getConfirmedBalance(key, db));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(cancelOrderTransaction.getSignature(), releaserReference));
				
		//CHECK ORDER EXISTS
		assertEquals(false, db.getOrderMap().contains(new BigInteger(new byte[]{5,6})));
	}

	@Test
	public void orphanCancelOrderTransaction()
	{
		init();
		
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, System.currentTimeMillis(), releaserReference);
		issueAssetTransaction.sign(maker, asPack);
		assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(db, releaserReference));
		issueAssetTransaction.process(db, asPack);

		long key = asset.getKey();
		LOGGER.info("asset.getReg(): " + asset.getReference());
		LOGGER.info("asset.getKey(): " + key);

		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(50000).setScale(8), maker.getConfirmedBalance(key, db));
		
		//CREATE ORDER
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(maker, key, FEE_KEY, BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(1).setScale(8), FEE_POWER, System.currentTimeMillis(), releaserReference, new byte[]{5,6});
		createOrderTransaction.sign(maker, asPack);
		createOrderTransaction.process(db, asPack);

		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(49000).setScale(8), maker.getConfirmedBalance(key, db));
		
		//CREATE CANCEL ORDER
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(maker, new BigInteger(new byte[]{5,6}), FEE_POWER, System.currentTimeMillis(), releaserReference, new byte[]{1,2});
		cancelOrderTransaction.sign(maker, asPack);
		cancelOrderTransaction.process(db, asPack);
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(50000).setScale(8), maker.getConfirmedBalance( key, db));
		cancelOrderTransaction.orphan(db, asPack);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(49000).setScale(8), maker.getConfirmedBalance( key, db));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(createOrderTransaction.getSignature(), releaserReference));
				
		//CHECK ORDER EXISTS
		assertEquals(true, db.getOrderMap().contains(new BigInteger(new byte[]{5,6})));
	}
	
	@Test
	public void validateMessageTransaction() 
	{
		
		init();
		
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		
		byte[] data = "test123!".getBytes();
		
		PrivateKeyAccount creator = new PrivateKeyAccount(privateKey);
		Account recipient = new Account("QfreeNWCeaU3BiXUxktaJRJrBB1SDg2k7o");		

		long key = 2l;
		
		creator.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(8), db);
				
		R_Send r_Send = new R_Send(
				creator, 
				recipient, 
				key, 
				BigDecimal.valueOf(10).setScale(8), 
				data,
				new byte[] { 1 },
				new byte[] { 0 },
				maker.getLastReference()
				);
		r_Send.sign(creator, asPack);
		
		assertEquals(r_Send.isValid(db, releaserReference), Transaction.VALIDATE_OK);
		
		r_Send.process(db, asPack);
		
		assertEquals(BigDecimal.valueOf(1).setScale(8), creator.getConfirmedBalance(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(90).setScale(8), creator.getConfirmedBalance(key, db));
		assertEquals(BigDecimal.valueOf(10).setScale(8), recipient.getConfirmedBalance(key, db));
		
		byte[] rawMessageTransaction = r_Send.toBytes(true, releaserReference);
		
		R_Send messageTransaction_2 = null;
		try {
			messageTransaction_2 = (R_Send) R_Send.Parse(rawMessageTransaction, releaserReference);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		assertEquals(new String(r_Send.getData()), new String(messageTransaction_2.getData()));
		assertEquals(r_Send.getCreator(), messageTransaction_2.getCreator());
		assertEquals(r_Send.getRecipient(), messageTransaction_2.getRecipient());
		assertEquals(r_Send.getKey(), messageTransaction_2.getKey());
		assertEquals(r_Send.getAmount(), messageTransaction_2.getAmount());
		assertEquals(r_Send.isEncrypted(), messageTransaction_2.isEncrypted());
		assertEquals(r_Send.isText(), messageTransaction_2.isText());
		
		assertEquals(r_Send.isSignatureValid(), true);
		assertEquals(messageTransaction_2.isSignatureValid(), true);		
	}
	

}
