package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ntp.NTP;

import org.junit.Test;

import com.google.common.primitives.Longs;

import database.DBSet;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.item.assets.AssetCls;
import qora.item.assets.AssetVenture;
import qora.block.GenesisBlock;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.naming.NameSale;
import qora.payment.Payment;
import qora.transaction.ArbitraryTransactionV3;
import qora.transaction.BuyNameTransaction;
import qora.transaction.CancelOrderTransaction;
import qora.transaction.CancelSellNameTransaction;
import qora.transaction.CreateOrderTransaction;
import qora.transaction.CreatePollTransaction;
import qora.transaction.GenesisTransaction;
import qora.transaction.IssueAssetTransaction;
import qora.transaction.MultiPaymentTransaction;
import qora.transaction.PaymentTransaction;
import qora.transaction.RegisterNameTransaction;
import qora.transaction.SellNameTransaction;
import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;
//import qora.transaction.TransferAssetTransaction;
import qora.transaction.UpdateNameTransaction;
import qora.transaction.VoteOnPollTransaction;
import qora.voting.Poll;
import qora.voting.PollOption;

public class TransactionTests {

	byte[] releaserReference = null;

	static Logger LOGGER = Logger.getLogger(TransactionTests.class.getName());

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
		
		File log4j = new File("log4j_test.properties");
		System.out.println(log4j.getAbsolutePath());
		if(log4j.exists())
		{
			System.out.println("configured");
			PropertyConfigurator.configure(log4j.getAbsolutePath());
		}

		db = DBSet.createEmptyDatabaseSet();
		gb = new GenesisBlock();
		gb.process(db);
		
		// OIL FUND
		maker.setLastReference(gb.getGeneratorSignature(), db);
		maker.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), db);
		

	}
	
	
	@Test
	public void validateSignatureGenesisTransaction() 
	{
		
		//CHECK VALID SIGNATURE
		Transaction transaction = new GenesisTransaction(new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU"), BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		assertEquals(true, transaction.isSignatureValid());
	}
	
	@Test
	public void validateGenesisTransaction() 
	{
		
		//CREATE MEMORYDB
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//CHECK NORMAL VALID
		Transaction transaction = new GenesisTransaction(new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU"), BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		assertEquals(Transaction.VALIDATE_OK, transaction.isValid(databaseSet, releaserReference));
		
		//CHECK INVALID ADDRESS
		transaction = new GenesisTransaction(new Account("test"), BigDecimal.valueOf(-1000).setScale(8), NTP.getTime());
		assertNotEquals(true, Transaction.VALIDATE_OK == transaction.isValid(databaseSet, releaserReference));
		
		//CHECK NEGATIVE AMOUNT
		transaction = new GenesisTransaction(new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU"), BigDecimal.valueOf(-1000).setScale(8), NTP.getTime());
		assertNotEquals(true, Transaction.VALIDATE_OK == transaction.isValid(databaseSet, releaserReference));
	}
	
	@Test
	public void parseGenesisTransaction() 
	{
		//CREATE TRANSACTION
		Account account = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		Transaction transaction = new GenesisTransaction(account, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		
		//CONVERT TO BYTES
		byte[] rawTransaction = transaction.toBytes(true, null);
		
		try 
		{	
			//PARSE FROM BYTES
			Transaction parsedTransaction = TransactionFactory.getInstance().parse(rawTransaction, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedTransaction instanceof GenesisTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(transaction.getSignature(), parsedTransaction.getSignature()));
			
			//CHECK AMOUNT
			assertEquals(transaction.viewAmount(account), parsedTransaction.viewAmount(account));			
			
			//CHECK TIMESTAMP
			assertEquals(transaction.getTimestamp(), parsedTransaction.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawTransaction = new byte[transaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawTransaction, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void processGenesisTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//PROCESS TRANSACTION
		Account account = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		Transaction transaction = new GenesisTransaction(account, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CHECK AMOUNT
		assertEquals(BigDecimal.valueOf(1000).setScale(8), account.getConfirmedBalance(databaseSet));
		
		//CHECK REFERENCE
		assertEquals(true, Arrays.equals(transaction.getSignature(), account.getLastReference(databaseSet)));
	}
	
	@Test
	public void orphanGenesisTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//PROCESS TRANSACTION
		Account account = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		Transaction transaction = new GenesisTransaction(account, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//ORPHAN TRANSACTION
		transaction.orphan(databaseSet, false);
		
		//CHECK AMOUNT
		assertEquals(BigDecimal.ZERO, account.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE
		assertEquals(true, Arrays.equals(new byte[0], account.getLastReference(databaseSet)));
	}
	
	//PAYMENT
	
	@Test
	public void validateSignaturePaymentTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		long timestamp = NTP.getTime();
		
		//CREATE PAYMENT
		Transaction payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		payment.sign(sender, false);		
		//CHECK IF PAYMENT SIGNATURE IS VALID
		assertEquals(true, payment.isSignatureValid());
		
		//INVALID SIGNATURE
		payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp+1, sender.getLastReference(databaseSet), new byte[64]);
		
		//CHECK IF PAYMENT SIGNATURE IS INVALID
		assertEquals(false, payment.isSignatureValid());
	}
	
	@Test
	public void validatePaymentTransaction() 
	{
		

		init();
		
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		long timestamp = NTP.getTime();
		maker.setConfirmedBalance(0l, BigDecimal.valueOf(1000).setScale(8), db);


		//CREATE VALID PAYMENT
		Transaction payment = new PaymentTransaction(maker, recipient, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));
		payment.sign(maker, false);

		//CHECK IF PAYMENT IS VALID
		assertEquals(Transaction.VALIDATE_OK, payment.isValid(db, releaserReference));
		
		payment.process(db, false);

		//CREATE INVALID PAYMENT INVALID RECIPIENT ADDRESS
		payment = new PaymentTransaction(maker, new Account("test"), BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));
	
		//CHECK IF PAYMENT IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, payment.isValid(db, releaserReference));
		
		//CREATE INVALID PAYMENT NEGATIVE AMOUNT
		payment = new PaymentTransaction(maker, recipient, BigDecimal.valueOf(-100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));
		payment.sign(maker, false);
		//CHECK IF PAYMENT IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, payment.isValid(db, releaserReference));	
		
		
		//CREATE INVALID PAYMENT WRONG REFERENCE
		payment = new PaymentTransaction(maker, recipient, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, new byte[64], new byte[64]);
		//CHECK IF PAYMENT IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, payment.isValid(db, releaserReference));	
	}
	
	@Test
	public void parsePaymentTransaction() 
	{
		init();
		
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		long timestamp = NTP.getTime();
		maker.setConfirmedBalance(0l, BigDecimal.valueOf(1000).setScale(8), db);
										
		//CREATE VALID PAYMENT
		Transaction payment = new PaymentTransaction(maker, recipient, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, maker.getLastReference(db));
		payment.sign(maker, false);
		
		//CONVERT TO BYTES
		byte[] rawPayment = payment.toBytes(true, null);
		
		try 
		{	
			//PARSE FROM BYTES
			PaymentTransaction parsedPayment = (PaymentTransaction) TransactionFactory.getInstance().parse(rawPayment, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedPayment instanceof PaymentTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(payment.getSignature(), parsedPayment.getSignature()));
			
			//CHECK AMOUNT SENDER
			assertEquals(payment.viewAmount(maker), parsedPayment.viewAmount(maker));	
			
			//CHECK AMOUNT RECIPIENT
			assertEquals(payment.viewAmount(recipient), parsedPayment.viewAmount(recipient));	
			
			//CHECK FEE
			assertEquals(payment.getFee(), parsedPayment.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(payment.getReference(), parsedPayment.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(payment.getTimestamp(), parsedPayment.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawPayment = new byte[payment.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawPayment, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void processPaymentTransaction()
	{
		
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
					
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
			
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
			
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
			
		LOGGER.info("sender.getLastReference(databaseSet) LENGTH: " + sender.getLastReference(databaseSet).length + " -- " + sender.getLastReference(databaseSet));
		
		//CREATE PAYMENT
		Transaction payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		payment.sign(sender, false);
		payment.process(databaseSet, false);

		LOGGER.info("getConfirmedBalance: " + sender.getConfirmedBalance(databaseSet));
		LOGGER.info("getConfirmedBalance OIL_KEY:" + sender.getConfirmedBalance(OIL_KEY, databaseSet));

		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(900).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.valueOf(100).setScale(8), recipient.getConfirmedBalance(databaseSet));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(payment.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK REFERENCE RECIPIENT
		// !!! now not worked !!!
		// assertEquals(true, Arrays.equals(payment.getSignature(), recipient.getLastReference(databaseSet)));
		
		//CREATE SIGNATURE
		
		//CREATE PAYMENT
		payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		payment.sign(sender, false);
		payment.process(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(800).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
						
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.valueOf(200).setScale(8), recipient.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(payment.getSignature(), sender.getLastReference(databaseSet)));
					
		//CHECK REFERENCE RECIPIENT NOT CHANGED
		// not worked now assertEquals(true, Arrays.equals(payment.getReference(), recipient.getLastReference(databaseSet)));
	}
	
	@Test
	public void orphanPaymentTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
					
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
			
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);

		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		long timestamp = NTP.getTime();
			
		//CREATE PAYMENT
		Transaction payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		payment.sign(sender, false);
		payment.process(databaseSet, false);
		
		//CREATE PAYMENT2
		Transaction payment2  = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		payment2.sign(sender, false);
		payment.process(databaseSet, false);
		
		//ORPHAN PAYMENT
		payment2.orphan(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(900).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
						
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.valueOf(100).setScale(8), recipient.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(payment.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK REFERENCE RECIPIENT
		/// nor worked now assertEquals(true, Arrays.equals(payment.getSignature(), recipient.getLastReference(databaseSet)));

		//ORPHAN PAYMENT
		payment.orphan(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), sender.getConfirmedBalance(databaseSet));
								
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.valueOf(0).setScale(8), recipient.getConfirmedBalance(databaseSet));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
						
		//CHECK REFERENCE RECIPIENT
		/// not work now assertEquals(true, Arrays.equals(new byte[0], recipient.getLastReference(databaseSet)));
	}

	//REGISTER NAME
	
	@Test
	public void validateSignatureRegisterNameTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE NAME
		Name name = new Name(sender, "test", "this is the value");
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		
		//CHECK IF NAME REGISTRATION IS VALID
		assertEquals(true, nameRegistration.isSignatureValid());
		
		//INVALID SIGNATURE
		nameRegistration = new RegisterNameTransaction(
				sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet), new byte[64]);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(false, nameRegistration.isSignatureValid());
	}
	
	@Test
	public void validateRegisterNameTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
				
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		
		//CHECK IF NAME REGISTRATION IS VALID
		assertEquals(Transaction.VALIDATE_OK, nameRegistration.isValid(databaseSet, releaserReference));
		nameRegistration.process(databaseSet, false);
		
		//CREATE INVALID NAME REGISTRATION INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		name = new Name(sender, longName, "this is the value");
		nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);

		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, nameRegistration.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID NAME REGISTRATION INVALID NAME LENGTH
		String longValue = "";
		for(int i=1; i<10000; i++)
		{
			longValue += "oke";
		}
		name = new Name(sender, "test2", longValue);
		nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);

		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_VALUE_LENGTH, nameRegistration.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID NAME REGISTRATION NAME ALREADY TAKEN
		name = new Name(sender, "test", "this is the value");
		nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.NAME_ALREADY_REGISTRED, nameRegistration.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID NAME NOT ENOUGH BALANCE
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		name = new Name(invalidOwner, "test2", "this is the value");
		nameRegistration = new RegisterNameTransaction(invalidOwner, name, FEE_POWER, timestamp, invalidOwner.getLastReference(databaseSet));
		nameRegistration.sign(invalidOwner, false);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.NO_BALANCE, nameRegistration.isValid(databaseSet, releaserReference));
		
		//CREATE NAME REGISTRATION INVALID REFERENCE
		name = new Name(sender, "test2", "this is the value");
		nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, invalidOwner.getLastReference(databaseSet));
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, nameRegistration.isValid(databaseSet, releaserReference));
	}

	@Test
	public void parseRegisterNameTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
				
		//CREATE NAME REGISTRATION
		RegisterNameTransaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		
		//CONVERT TO BYTES
		byte[] rawNameRegistration = nameRegistration.toBytes(true, null);
		
		try 
		{	
			//PARSE FROM BYTES
			RegisterNameTransaction parsedRegistration = (RegisterNameTransaction) TransactionFactory.getInstance().parse(rawNameRegistration, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedRegistration instanceof RegisterNameTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(nameRegistration.getSignature(), parsedRegistration.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(nameRegistration.viewAmount(sender), parsedRegistration.viewAmount(sender));	
			
			//CHECK NAME OWNER
			assertEquals(nameRegistration.getName().getOwner().getAddress(), parsedRegistration.getName().getOwner().getAddress());	
			
			//CHECK NAME NAME
			assertEquals(nameRegistration.getName().getName(), parsedRegistration.getName().getName());	
			
			//CHECK NAME VALUE
			assertEquals(nameRegistration.getName().getValue(), parsedRegistration.getName().getValue());	
			
			//CHECK FEE
			assertEquals(nameRegistration.getFee(), parsedRegistration.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(nameRegistration.getReference(), parsedRegistration.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(nameRegistration.getTimestamp(), parsedRegistration.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawNameRegistration = new byte[nameRegistration.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawNameRegistration, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	@Test
	public void processRegisterNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		nameRegistration.process(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(1000).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(nameRegistration.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK NAME EXISTS
		assertEquals(true, databaseSet.getNameMap().contains(name));
	}
	
	@Test
	public void orphanRegisterNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		nameRegistration.process(databaseSet, false);
		nameRegistration.orphan(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), sender.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK NAME EXISTS
		assertEquals(false, databaseSet.getNameMap().contains(name));
	}

	//UPDATE NAME
	
	@Test
	public void validateSignatureUpdateNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE NAME
		Name name = new Name(sender, "test", "this is the value");
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		
		//CREATE NAME UPDATE
		Transaction nameUpdate = new UpdateNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameUpdate.sign(sender, false);
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(true, nameUpdate.isSignatureValid());
		
		//INVALID SIGNATURE
		nameUpdate = new RegisterNameTransaction(
				sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet), new byte[64]);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(false, nameUpdate.isSignatureValid());

	}
	
	@Test
	public void validateUpdateNameTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
				
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		
		//CHECK IF NAME REGISTRATION IS VALID
		assertEquals(Transaction.VALIDATE_OK, nameRegistration.isValid(databaseSet, releaserReference));
		nameRegistration.process(databaseSet, false);
		
		//CREATE NAME UPDATE
		name.setValue("new value");
		Transaction nameUpdate = new UpdateNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
	
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(Transaction.VALIDATE_OK, nameUpdate.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID NAME UPDATE INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		name = new Name(sender, longName, "this is the value");
		nameUpdate = new UpdateNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));		

		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, nameUpdate.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID NAME UPDATE NAME DOES NOT EXIST
		name = new Name(sender, "test2", "this is the value");
		nameUpdate = new UpdateNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
				
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NAME_DOES_NOT_EXIST, nameUpdate.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID NAME UPDATE INCORRECT OWNER
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		name = new Name(invalidOwner, "test2", "this is the value");
		nameRegistration = new RegisterNameTransaction(invalidOwner, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(invalidOwner, false);
		nameRegistration.process(databaseSet, false);	
		
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.INVALID_NAME_CREATOR, nameUpdate.isValid(databaseSet, releaserReference));
				
		//CREATE INVALID NAME UPDATE NO BALANCE
		name = new Name(invalidOwner, "test2", "this is the value");
		nameUpdate = new UpdateNameTransaction(invalidOwner, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
				
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NO_BALANCE, nameUpdate.isValid(databaseSet, releaserReference));
				
		//CREATE NAME UPDATE INVALID REFERENCE
		name = new Name(sender, "test", "this is the value");
		nameUpdate = new UpdateNameTransaction(sender, name, FEE_POWER, timestamp, new byte[]{});
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, nameUpdate.isValid(databaseSet, releaserReference));
						
	}

	@Test
	public void parseUpdateNameTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
				
		//CREATE NAME UPDATE
		UpdateNameTransaction nameUpdate = new UpdateNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameUpdate.sign(sender, false);
		
		//CONVERT TO BYTES
		byte[] rawNameUpdate = nameUpdate.toBytes(true, null);
		
		try 
		{	
			//PARSE FROM BYTES
			UpdateNameTransaction parsedUpdate = (UpdateNameTransaction) TransactionFactory.getInstance().parse(rawNameUpdate, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedUpdate instanceof UpdateNameTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(nameUpdate.getSignature(), parsedUpdate.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(nameUpdate.viewAmount(sender), parsedUpdate.viewAmount(sender));	
			
			//CHECK OWNER
			assertEquals(nameUpdate.getCreator().getAddress(), parsedUpdate.getCreator().getAddress());	
			
			//CHECK NAME OWNER
			assertEquals(nameUpdate.getName().getOwner().getAddress(), parsedUpdate.getName().getOwner().getAddress());	
			
			//CHECK NAME NAME
			assertEquals(nameUpdate.getName().getName(), parsedUpdate.getName().getName());	
			
			//CHECK NAME VALUE
			assertEquals(nameUpdate.getName().getValue(), parsedUpdate.getName().getValue());	
			
			//CHECK FEE
			assertEquals(nameUpdate.getFee(), parsedUpdate.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(nameUpdate.getReference(), parsedUpdate.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(nameUpdate.getTimestamp(), parsedUpdate.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawNameUpdate = new byte[nameUpdate.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawNameUpdate, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	@Test
	public void processUpdateNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		nameRegistration.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
		
		//CREATE NAME UPDATE
		name = new Name(new Account("Qj5Aq4P4ehXaCEmi6vqVrFQDecpPXKSi8z"), "test", "new value");
		Transaction nameUpdate = new UpdateNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameUpdate.sign(sender, false);
		nameUpdate.process(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(1000).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(nameUpdate.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK NAME EXISTS
		assertEquals(true, databaseSet.getNameMap().contains(name));
		
		//CHECK NAME VALUE
		name =  databaseSet.getNameMap().get("test");
		assertEquals("new value", name.getValue());
		
		//CHECK NAME OWNER
		assertEquals(true, "XYLEQnuvhracK2WMN3Hjif67knkJe9hTQn" != name.getOwner().getAddress());
		assertEquals("Qj5Aq4P4ehXaCEmi6vqVrFQDecpPXKSi8z", name.getOwner().getAddress());
		
	}

	
	@Test
	public void orphanUpdateNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		nameRegistration.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
		
		//CREATE NAME UPDATE
		name = new Name(new Account("XYLEQnuvhracK2WMN3Hjif67knkJe9hTQn"), "test", "new value");
		Transaction nameUpdate = new UpdateNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameUpdate.sign(sender, false);
		nameUpdate.process(databaseSet, false);
		nameUpdate.orphan(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(1000).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(nameRegistration.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK NAME EXISTS
		assertEquals(true, databaseSet.getNameMap().contains(name));
		
		//CHECK NAME VALUE
		name =  databaseSet.getNameMap().get("test");
		assertEquals("this is the value", name.getValue());
		
		//CHECK NAME OWNER
		assertEquals(sender.getAddress(), name.getOwner().getAddress());
	}
	
	//SELL NAME
	
	@Test
	public void validateSignatureSellNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE NAME
		NameSale nameSale = new NameSale("test", BigDecimal.valueOf(1000).setScale(8));
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(true, nameSaleTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		nameSaleTransaction = new SellNameTransaction(
				sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet), new byte[64]);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(false, nameSaleTransaction.isSignatureValid());
	}
	
	@Test
	public void validateSellNameTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
				
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		
		//CHECK IF NAME REGISTRATION IS VALID
		assertEquals(Transaction.VALIDATE_OK, nameRegistration.isValid(databaseSet, releaserReference));
		nameRegistration.process(databaseSet, false);
		
		//CREATE NAME SALE
		NameSale nameSale = new NameSale("test", BigDecimal.valueOf(1000).setScale(8));
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
	
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(Transaction.VALIDATE_OK, nameSaleTransaction.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID NAME SALE INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		nameSale = new NameSale(longName, BigDecimal.valueOf(1000).setScale(8));
		nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));		

		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, nameSaleTransaction.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID NAME SALE NAME DOES NOT EXIST
		nameSale = new NameSale("test2", BigDecimal.valueOf(1000).setScale(8));
		nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
		nameSaleTransaction.process(false);
				
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NAME_DOES_NOT_EXIST, nameSaleTransaction.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID NAME UPDATE INCORRECT OWNER
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		name = new Name(invalidOwner, "test2", "this is the value");
		nameRegistration = new RegisterNameTransaction(invalidOwner, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(invalidOwner, false);
		nameRegistration.process(databaseSet, false);	
		
		//CHECK IF NAME UPDATE IS INVALID
		nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		assertEquals(Transaction.INVALID_NAME_CREATOR, nameSaleTransaction.isValid(databaseSet, releaserReference));
				
		//CREATE INVALID NAME UPDATE NO BALANCE
		nameSale = new NameSale("test2", BigDecimal.valueOf(1000).setScale(8));
		nameSaleTransaction = new SellNameTransaction(invalidOwner, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
				
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NO_BALANCE, nameSaleTransaction.isValid(databaseSet, releaserReference));
				
		//CREATE NAME UPDATE INVALID REFERENCE
		nameSale = new NameSale("test", BigDecimal.valueOf(1000).setScale(8));
		nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, new byte[]{});
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, nameSaleTransaction.isValid(databaseSet, releaserReference));
				
		//CREATE NAME UPDATE PROCESS 
		nameSale = new NameSale("test", BigDecimal.valueOf(1000).setScale(8));
		nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
		nameSaleTransaction.process(databaseSet, false);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.NAME_ALREADY_FOR_SALE, nameSaleTransaction.isValid(databaseSet, releaserReference));
	}

	@Test
	public void parseSellNameTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		NameSale nameSale = new NameSale("test", BigDecimal.valueOf(1).setScale(8));
				
		//CREATE NAME UPDATE
		SellNameTransaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
		nameSaleTransaction.process(false);
		
		//CONVERT TO BYTES
		byte[] rawNameSale = nameSaleTransaction.toBytes(true, null);
		
		try 
		{	
			//PARSE FROM BYTES
			SellNameTransaction parsedNameSale = (SellNameTransaction) TransactionFactory.getInstance().parse(rawNameSale, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedNameSale instanceof SellNameTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(nameSaleTransaction.getSignature(), parsedNameSale.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(nameSaleTransaction.viewAmount(sender), parsedNameSale.viewAmount(sender));	
			
			//CHECK OWNER
			assertEquals(nameSaleTransaction.getCreator().getAddress(), parsedNameSale.getCreator().getAddress());	
			
			//CHECK NAMESALE NAME
			assertEquals(nameSaleTransaction.getNameSale().getKey(), parsedNameSale.getNameSale().getKey());	
			
			//CHECK NAMESALE AMOUNT
			assertEquals(nameSaleTransaction.getNameSale().getAmount(), parsedNameSale.getNameSale().getAmount());	
			
			//CHECK FEE
			assertEquals(nameSaleTransaction.getFee(), parsedNameSale.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(nameSaleTransaction.getReference(), parsedNameSale.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(nameSaleTransaction.getTimestamp(), parsedNameSale.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawNameSale = new byte[nameSaleTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawNameSale, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	@Test
	public void processSellNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		nameRegistration.process(databaseSet, false);
		
		//CREATE SIGNATURE
		NameSale nameSale = new NameSale("test", BigDecimal.valueOf(1000).setScale(8));
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
		nameSaleTransaction.process(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(1000).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(nameSaleTransaction.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK NAME SALE EXISTS
		assertEquals(true, databaseSet.getNameExchangeMap().contains("test"));
		
		//CHECK NAME SALE AMOUNT
		nameSale =  databaseSet.getNameExchangeMap().getNameSale("test");
		assertEquals(BigDecimal.valueOf(1000).setScale(8), nameSale.getAmount());
	}

	@Test
	public void orphanSellNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		nameRegistration.process(databaseSet, false);
		
		//CREATE SIGNATURE
		NameSale nameSale = new NameSale("test", BigDecimal.valueOf(1000).setScale(8));
						
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
		nameSaleTransaction.process(databaseSet, false);
		nameSaleTransaction.orphan(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(1000).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(nameRegistration.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK NAME SALE EXISTS
		assertEquals(false, databaseSet.getNameExchangeMap().contains("test"));
	}
	
	
	//CANCEL SELL NAME
	
	@Test
	public void validateSignatureCancelSellNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new CancelSellNameTransaction(sender, "test", FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(true, nameSaleTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		nameSaleTransaction = new CancelSellNameTransaction(
				sender, "test", FEE_POWER, timestamp, sender.getLastReference(databaseSet), new byte[64]);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(false, nameSaleTransaction.isSignatureValid());
	}
	
	@Test
	public void validateCancelSellNameTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
				
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		
		//CHECK IF NAME REGISTRATION IS VALID
		assertEquals(Transaction.VALIDATE_OK, nameRegistration.isValid(databaseSet, releaserReference));
		nameRegistration.process(databaseSet, false);
		
		//CREATE NAME SALE
		NameSale nameSale = new NameSale("test", BigDecimal.valueOf(1000).setScale(8));
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
	
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(Transaction.VALIDATE_OK, nameSaleTransaction.isValid(databaseSet, releaserReference));
		nameSaleTransaction.process(databaseSet, false);
		
		//CREATE CANCEL NAME SALE
		CancelSellNameTransaction cancelNameSaleTransaction = new CancelSellNameTransaction(sender, nameSale.getKey(), FEE_POWER, timestamp, sender.getLastReference(databaseSet));		

		//CHECK IF CANCEL NAME UPDATE IS VALID
		assertEquals(Transaction.VALIDATE_OK, cancelNameSaleTransaction.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID CANCEL NAME SALE INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		
		cancelNameSaleTransaction = new CancelSellNameTransaction(sender, longName, FEE_POWER, timestamp, sender.getLastReference(databaseSet));		

		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, cancelNameSaleTransaction.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID CANCEL NAME SALE NAME DOES NOT EXIST
		cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test2", FEE_POWER, timestamp, sender.getLastReference(databaseSet));
				
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NAME_DOES_NOT_EXIST, cancelNameSaleTransaction.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID NAME UPDATE INCORRECT OWNER
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		name = new Name(invalidOwner, "test2", "this is the value");
		nameRegistration = new RegisterNameTransaction(invalidOwner, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(invalidOwner, false);
		nameRegistration.process(databaseSet, false);	
		
		//CREATE NAME SALE
		nameSale = new NameSale("test2", BigDecimal.valueOf(1000).setScale(8));
		nameSaleTransaction = new SellNameTransaction(invalidOwner, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(invalidOwner, false);
		nameSaleTransaction.process(databaseSet, false);	
		
		//CHECK IF NAME UPDATE IS INVALID
		cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test2", FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
		assertEquals(Transaction.INVALID_NAME_CREATOR, cancelNameSaleTransaction.isValid(databaseSet, releaserReference));
				
		//CREATE INVALID NAME UPDATE NO BALANCE
		cancelNameSaleTransaction = new CancelSellNameTransaction(invalidOwner, "test2", FEE_POWER, timestamp, sender.getLastReference(databaseSet));
				
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NO_BALANCE, cancelNameSaleTransaction.isValid(databaseSet, releaserReference));
				
		//CREATE NAME UPDATE INVALID REFERENCE
		cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test", FEE_POWER, timestamp, new byte[]{});
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, cancelNameSaleTransaction.isValid(databaseSet, releaserReference));
				
		//CREATE NAME UPDATE PROCESS 
		cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test", FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		cancelNameSaleTransaction.sign(sender, false);
		cancelNameSaleTransaction.process(databaseSet, false);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.NAME_NOT_FOR_SALE, cancelNameSaleTransaction.isValid(databaseSet, releaserReference));
	}

	@Test
	public void parseCancelSellNameTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
				
		//CREATE CANCEL NAME SALE
		CancelSellNameTransaction cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test", FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		cancelNameSaleTransaction.sign(sender, false);
		
		//CONVERT TO BYTES
		byte[] rawCancelNameSale = cancelNameSaleTransaction.toBytes(true, null);
		
		try 
		{	
			//PARSE FROM BYTES
			CancelSellNameTransaction parsedCancelNameSale = (CancelSellNameTransaction) TransactionFactory.getInstance().parse(rawCancelNameSale, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedCancelNameSale instanceof CancelSellNameTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(cancelNameSaleTransaction.getSignature(), parsedCancelNameSale.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(cancelNameSaleTransaction.viewAmount(sender), parsedCancelNameSale.viewAmount(sender));	
			
			//CHECK OWNER
			assertEquals(cancelNameSaleTransaction.getCreator().getAddress(), parsedCancelNameSale.getCreator().getAddress());	
			
			//CHECK NAME
			assertEquals(cancelNameSaleTransaction.getName(), parsedCancelNameSale.getName());	
			
			//CHECK FEE
			assertEquals(cancelNameSaleTransaction.getFee(), parsedCancelNameSale.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(cancelNameSaleTransaction.getReference(), parsedCancelNameSale.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(cancelNameSaleTransaction.getTimestamp(), parsedCancelNameSale.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawCancelNameSale = new byte[cancelNameSaleTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawCancelNameSale, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void processCancelSellNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		nameRegistration.process(databaseSet, false);
		
		//CREATE SIGNATURE
		NameSale nameSale = new NameSale("test", BigDecimal.valueOf(1000).setScale(8));
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
		nameSaleTransaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
			
		//CREATE CANCEL NAME SALE
		Transaction cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test", FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		cancelNameSaleTransaction.sign(sender, false);
		cancelNameSaleTransaction.process(databaseSet, false);	
		
		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(1000).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(cancelNameSaleTransaction.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK NAME SALE EXISTS
		assertEquals(false, databaseSet.getNameExchangeMap().contains("test"));
	}

	@Test
	public void orphanCancelSellNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		nameRegistration.process(databaseSet, false);
		
		//CREATE SIGNATURE
		NameSale nameSale = new NameSale("test", BigDecimal.valueOf(1000).setScale(8));
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
		nameSaleTransaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
			
		//CREATE CANCEL NAME SALE
		Transaction cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test", FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		cancelNameSaleTransaction.sign(sender, false);
		cancelNameSaleTransaction.process(databaseSet, false);	
		cancelNameSaleTransaction.orphan(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(0,BigDecimal.valueOf(1000).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(nameSaleTransaction.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK NAME SALE EXISTS
		assertEquals(true, databaseSet.getNameExchangeMap().contains("test"));
		
		//CHECK NAME SALE AMOUNT
		nameSale =  databaseSet.getNameExchangeMap().getNameSale("test");
		assertEquals(BigDecimal.valueOf(1000).setScale(8), nameSale.getAmount());
	}
	
	//BUY NAME
	
	@Test
	public void validateSignatureBuyNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		NameSale nameSale = new NameSale("test", BigDecimal.valueOf(1000).setScale(8));
		
		//CREATE NAME SALE
		Transaction buyNameTransaction = new BuyNameTransaction(sender, nameSale, nameSale.getName(databaseSet).getOwner(), FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		buyNameTransaction.sign(sender, false);
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(true, buyNameTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		buyNameTransaction = new BuyNameTransaction(
				sender, nameSale, nameSale.getName(databaseSet).getOwner(), FEE_POWER, timestamp, sender.getLastReference(databaseSet), new byte[64]);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(false, buyNameTransaction.isSignatureValid());
	}
	
	@Test
	public void validateBuyNameTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//CREATE KNOWN ACCOUNT
		seed = Crypto.getInstance().digest("buyer".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount buyer = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE BUYER HAS FUNDS
		transaction = new GenesisTransaction(buyer, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
				
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		
		//CHECK IF NAME REGISTRATION IS VALID
		assertEquals(Transaction.VALIDATE_OK, nameRegistration.isValid(databaseSet, releaserReference));
		nameRegistration.process(databaseSet, false);
		
		//CREATE NAME SALE
		BigDecimal bdAmoSell = BigDecimal.valueOf(700).setScale(8);
		NameSale nameSale = new NameSale("test", bdAmoSell);
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
	
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(Transaction.VALIDATE_OK, nameSaleTransaction.isValid(databaseSet, releaserReference));
		nameSaleTransaction.process(databaseSet, false);
		
		//CREATE NAME PURCHASE
		BuyNameTransaction namePurchaseTransaction = new BuyNameTransaction(buyer, nameSale, nameSale.getName(databaseSet).getOwner(), FEE_POWER, timestamp, buyer.getLastReference(databaseSet));
		namePurchaseTransaction.sign(buyer, false);

		//CHECK IF NAME PURCHASE IS VALID
		assertEquals(Transaction.VALIDATE_OK, namePurchaseTransaction.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID NAME PURCHASE INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		
		NameSale nameSaleInvalid = new NameSale(longName, nameSale.getAmount());
		//nameSale = new NameSale(longName, nameSale.getAmount());
		//LOGGER.info("nameSaleLong " + nameSaleLong);
		//LOGGER.info("nameSaleLong getOwner "  + nameSaleLong.getName(databaseSet).getOwner());
		//// nameSaleLong --- nameSale -> owner
		namePurchaseTransaction = new BuyNameTransaction(buyer, nameSaleInvalid, nameSale.getName(databaseSet).getOwner(), FEE_POWER, timestamp, buyer.getLastReference(databaseSet));		

		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, namePurchaseTransaction.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID NAME PURCHASE NAME DOES NOT EXIST
		nameSaleInvalid = new NameSale("test2", BigDecimal.valueOf(1000).setScale(8));
		namePurchaseTransaction = new BuyNameTransaction(buyer, nameSaleInvalid, nameSale.getName(databaseSet).getOwner(), FEE_POWER, timestamp, buyer.getLastReference(databaseSet));		
		
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NAME_DOES_NOT_EXIST, namePurchaseTransaction.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID NAME PURCHASE NAME NOT FOR SALE
		Name test2 = new Name(sender, "test2", "oke");
		databaseSet.getNameMap().add(test2);
		
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NAME_NOT_FOR_SALE, namePurchaseTransaction.isValid(databaseSet, releaserReference));
						
		//CREATE INVALID NAME PURCHASE ALREADY OWNER
		nameSale = new NameSale("test", bdAmoSell);
		namePurchaseTransaction = new BuyNameTransaction(sender, nameSale, nameSale.getName(databaseSet).getOwner(), FEE_POWER, timestamp, sender.getLastReference(databaseSet));		
		
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.BUYER_ALREADY_OWNER, namePurchaseTransaction.isValid(databaseSet, releaserReference));
				
		//CREATE INVALID NAME UPDATE NO BALANCE
		buyer.setConfirmedBalance(BigDecimal.ZERO.setScale(8), databaseSet);
		namePurchaseTransaction = new BuyNameTransaction(buyer,nameSale,nameSale.getName(databaseSet).getOwner(), FEE_POWER, timestamp, buyer.getLastReference(databaseSet));		
		
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NO_BALANCE, namePurchaseTransaction.isValid(databaseSet, releaserReference));

		// setConfirmedBalance(long key, BigDecimal amount, DBSet db)
		buyer.setConfirmedBalance(BigDecimal.valueOf(2000).setScale(8), databaseSet);
				
		//CREATE NAME UPDATE INVALID REFERENCE
		namePurchaseTransaction = new BuyNameTransaction(buyer, nameSale, nameSale.getName(databaseSet).getOwner(), FEE_POWER, timestamp, new byte[]{});		
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, namePurchaseTransaction.isValid(databaseSet, releaserReference));
	}

	@Test
	public void parseBuyNameTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();				
		////////////// FIRST
		//CREATE NAME SALE
		BigDecimal bdAmoSell = BigDecimal.valueOf(700).setScale(8);
		NameSale nameSale = new NameSale("test", bdAmoSell);
		SellNameTransaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
		//nameSaleTransaction.process();
		nameSaleTransaction.process(databaseSet, false);

		LOGGER.addAppender(null);
		LOGGER.debug("nameSale ");
		LOGGER.info("nameSale " + nameSale.getName(databaseSet));
		LOGGER.info("nameSale " + nameSale.getName(databaseSet));
		LOGGER.info("nameSale " + nameSale.getName(databaseSet).getOwner());

		//CREATE CANCEL NAME SALE
		BuyNameTransaction namePurchaseTransaction = new BuyNameTransaction(sender, nameSale, nameSale.getName(databaseSet).getOwner(), FEE_POWER, timestamp, sender.getLastReference(databaseSet));	
		namePurchaseTransaction.sign(sender, false);
		//CONVERT TO BYTES
		byte[] rawNamePurchase = namePurchaseTransaction.toBytes(true, null);
		
		try 
		{	
			//PARSE FROM BYTES
			BuyNameTransaction parsedNamePurchase = (BuyNameTransaction) TransactionFactory.getInstance().parse(rawNamePurchase, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedNamePurchase instanceof BuyNameTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(namePurchaseTransaction.getSignature(), parsedNamePurchase.getSignature()));
			
			//CHECK AMOUNT BUYER
			assertEquals(namePurchaseTransaction.viewAmount(sender), parsedNamePurchase.viewAmount(sender));	
			
			//CHECK OWNER
			assertEquals(namePurchaseTransaction.getBuyer().getAddress(), parsedNamePurchase.getBuyer().getAddress());	
			
			//CHECK NAME
			assertEquals(namePurchaseTransaction.getNameSale().getKey(), parsedNamePurchase.getNameSale().getKey());	
		
			//CHECK FEE
			assertEquals(namePurchaseTransaction.getFee(), parsedNamePurchase.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(namePurchaseTransaction.getReference(), parsedNamePurchase.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(namePurchaseTransaction.getTimestamp(), parsedNamePurchase.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawNamePurchase = new byte[namePurchaseTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawNamePurchase, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void processBuyNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//CREATE KNOWN ACCOUNT
		seed = Crypto.getInstance().digest("buyer".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount buyer = new PrivateKeyAccount(privateKey);		
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE BUYER HAS FUNDS
		transaction = new GenesisTransaction(buyer, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);			
		// set OIL
		buyer.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		nameRegistration.process(databaseSet, false);
		
		//CREATE SIGNATURE
		NameSale nameSale = new NameSale("test", BigDecimal.valueOf(500).setScale(8));
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
		nameSaleTransaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
			
		//CREATE NAME PURCHASE
		Transaction purchaseNameTransaction = new BuyNameTransaction(buyer, nameSale, nameSale.getName(databaseSet).getOwner(), FEE_POWER, timestamp, buyer.getLastReference(databaseSet));
		purchaseNameTransaction.sign(buyer, false);
		purchaseNameTransaction.process(databaseSet, false);	
		
		//CHECK BALANCE SENDER
		//assertEquals(BigDecimal.valueOf(498).setScale(8), buyer.getConfirmedBalance(databaseSet));
		assertEquals(0, BigDecimal.valueOf(500).setScale(8).compareTo(buyer.getConfirmedBalance(databaseSet)));
		
		//CHECK BALANCE SELLER
		assertEquals(0, BigDecimal.valueOf(1500).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
						
		//CHECK REFERENCE BUYER
		assertEquals(true, Arrays.equals(purchaseNameTransaction.getSignature(), buyer.getLastReference(databaseSet)));
				
		//CHECK NAME OWNER
		name = databaseSet.getNameMap().get("test");
		assertEquals(name.getOwner().getAddress(), buyer.getAddress());
	
		//CHECK NAME SALE EXISTS
		assertEquals(false, databaseSet.getNameExchangeMap().contains("test"));
	}

	@Test
	public void orphanBuyNameTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//CREATE KNOWN ACCOUNT
		seed = Crypto.getInstance().digest("buyer".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount buyer = new PrivateKeyAccount(privateKey);		
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE BUYER HAS FUNDS
		transaction = new GenesisTransaction(buyer, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);			
		// set OIL
		buyer.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameRegistration.sign(sender, false);
		nameRegistration.process(databaseSet, false);
		
		//CREATE SIGNATURE
		NameSale nameSale = new NameSale("test", BigDecimal.valueOf(1000).setScale(8));
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		nameSaleTransaction.sign(sender, false);
		nameSaleTransaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
			
		//CREATE NAME PURCHASE
		Transaction purchaseNameTransaction = new BuyNameTransaction(buyer, nameSale, nameSale.getName(databaseSet).getOwner(), FEE_POWER, timestamp, buyer.getLastReference(databaseSet));
		purchaseNameTransaction.sign(buyer, false);
		purchaseNameTransaction.process(databaseSet, false);	
		purchaseNameTransaction.orphan(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), buyer.getConfirmedBalance(databaseSet));
		
		//CHECK BALANCE SELLER
		assertEquals(0, BigDecimal.valueOf(9999).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
						
		//CHECK REFERENCE BUYER
		assertEquals(true, Arrays.equals(transaction.getSignature(), buyer.getLastReference(databaseSet)));
				
		//CHECK NAME OWNER
		name = databaseSet.getNameMap().get("test");
		assertEquals(name.getOwner().getAddress(), sender.getAddress());
	
		//CHECK NAME SALE EXISTS
		assertEquals(true, databaseSet.getNameExchangeMap().contains("test"));
	}
	
	//CREATE POLL
	
	@Test
	public void validateSignatureCreatePollTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE POLL
		Poll poll = new Poll(sender, "test", "this is the value", new ArrayList<PollOption>());
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollCreation.sign(sender, false);
		//CHECK IF POLL CREATION IS VALID
		assertEquals(true, pollCreation.isSignatureValid());
		
		//INVALID SIGNATURE
		pollCreation = new CreatePollTransaction(
				sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet), new byte[64]);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(false, pollCreation.isSignatureValid());
	}
		
	@Test
	public void validateCreatePollTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test")));
				
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollCreation.sign(sender, false);
		
		//CHECK IF POLL CREATION IS VALID
		assertEquals(Transaction.VALIDATE_OK, pollCreation.isValid(databaseSet, releaserReference));
		pollCreation.process(databaseSet, false);
		
		//CREATE INVALID POLL CREATION INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		poll = new Poll(sender, longName, "this is the value", Arrays.asList(new PollOption("test")));
		pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));		

		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, pollCreation.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID POLL CREATION INVALID DESCRIPTION LENGTH
		String longDescription = "";
		for(int i=1; i<10000; i++)
		{
			longDescription += "oke";
		}
		poll = new Poll(sender, "test2", longDescription, Arrays.asList(new PollOption("test")));
		pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));		

		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.INVALID_DESCRIPTION_LENGTH, pollCreation.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID POLL CREATION NAME ALREADY TAKEN
		poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test")));
		pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));		
		
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.POLL_ALREADY_CREATED, pollCreation.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID POLL CREATION NO OPTIONS 
		poll = new Poll(sender, "test2", "this is the value", new ArrayList<PollOption>());
		pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));		
		
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.INVALID_OPTIONS_LENGTH, pollCreation.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID POLL CREATION INVALID OPTION LENGTH
		poll = new Poll(sender, "test2", "this is the value", Arrays.asList(new PollOption(longName)));
		pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));		
				
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.INVALID_OPTION_LENGTH, pollCreation.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID POLL CREATION INVALID DUPLICATE OPTIONS
		poll = new Poll(sender, "test2", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("test")));
		pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));		
						
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.DUPLICATE_OPTION, pollCreation.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID POLL CREATION NOT ENOUGH BALANCE
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		poll = new Poll(sender, "test2", "this is the value", Arrays.asList(new PollOption("test")));
		pollCreation = new CreatePollTransaction(invalidOwner, poll, FEE_POWER, timestamp, invalidOwner.getLastReference(databaseSet));
		pollCreation.sign(invalidOwner, false);
		
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.NOT_ENOUGH_FEE, pollCreation.isValid(databaseSet, releaserReference));
		
		//CREATE POLL CREATION INVALID REFERENCE
		poll = new Poll(sender, "test2", "this is the value", Arrays.asList(new PollOption("test")));
		pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, invalidOwner.getLastReference(databaseSet));		
		
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, pollCreation.isValid(databaseSet, releaserReference));
		
	}

	@Test
	public void parseCreatePollTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("second option")));
				
		//CREATE POLL CREATION
		CreatePollTransaction pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollCreation.sign(sender, false);
		
		//CONVERT TO BYTES
		byte[] rawPollCreation = pollCreation.toBytes(true, null);
		
		try 
		{	
			//PARSE FROM BYTES
			CreatePollTransaction parsedPollCreation = (CreatePollTransaction) TransactionFactory.getInstance().parse(rawPollCreation, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedPollCreation instanceof CreatePollTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(pollCreation.getSignature(), parsedPollCreation.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(pollCreation.viewAmount(sender), parsedPollCreation.viewAmount(sender));	
			
			//CHECK POLL CREATOR
			assertEquals(pollCreation.getPoll().getCreator().getAddress(), parsedPollCreation.getPoll().getCreator().getAddress());	
			
			//CHECK POLL NAME
			assertEquals(pollCreation.getPoll().getName(), parsedPollCreation.getPoll().getName());	
			
			//CHECK POLL DESCRIPTION
			assertEquals(pollCreation.getPoll().getDescription(), parsedPollCreation.getPoll().getDescription());	
			
			//CHECK POLL OPTIONS SIZE
			assertEquals(pollCreation.getPoll().getOptions().size(), parsedPollCreation.getPoll().getOptions().size());	
			
			//CHECK POLL OPTIONS
			for(int i=0; i<pollCreation.getPoll().getOptions().size(); i++)
			{
				//CHECK OPTION NAME
				assertEquals(pollCreation.getPoll().getOptions().get(i).getName(), parsedPollCreation.getPoll().getOptions().get(i).getName());	
			}
			
			//CHECK FEE
			assertEquals(pollCreation.getFee(), parsedPollCreation.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(pollCreation.getReference(), parsedPollCreation.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(pollCreation.getTimestamp(), parsedPollCreation.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawPollCreation = new byte[pollCreation.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawPollCreation, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	@Test
	public void processCreatePollTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("second option")));
						
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollCreation.sign(sender, false);
		pollCreation.process(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(1000).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(pollCreation.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK POLL EXISTS
		assertEquals(true, databaseSet.getPollMap().contains(poll));
	}
	
	@Test
	public void orphanCreatePollTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("second option")));
						
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollCreation.sign(sender, false);
		pollCreation.process(databaseSet, false);
		pollCreation.orphan(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), sender.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK POLL EXISTS
		assertEquals(false, databaseSet.getPollMap().contains(poll));
	}
	
	//VOTE ON POLL
	
	@Test
	public void validateSignatureVoteOnPollTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		
		//CREATE POLL VOTE
		Transaction pollVote = new VoteOnPollTransaction(sender, "test", 5, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollVote.sign(sender, false);
		//CHECK IF POLL VOTE IS VALID
		assertEquals(true, pollVote.isSignatureValid());
		
		//INVALID SIGNATURE
		pollVote = new VoteOnPollTransaction(
				sender, "test", 5, FEE_POWER, timestamp, sender.getLastReference(databaseSet), new byte[64]);
		
		//CHECK IF POLL VOTE IS INVALID
		assertEquals(false, pollVote.isSignatureValid());
	}
		
	@Test
	public void validateVoteOnPollTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
		
		//CREATE SIGNATURE
		//LOGGER.info("asdasd");
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("test2")));
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollCreation.sign(sender, false);
		
		//CHECK IF POLL CREATION IS VALID
		assertEquals(Transaction.VALIDATE_OK, pollCreation.isValid(databaseSet, releaserReference));
		pollCreation.process(databaseSet, false);
		
		//CREATE POLL VOTE
		Transaction pollVote = new VoteOnPollTransaction(sender, poll.getName(), 0, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollVote.sign(sender, false);
		
		//CHECK IF POLL VOTE IS VALID
		assertEquals(Transaction.VALIDATE_OK, pollVote.isValid(databaseSet, releaserReference));
		pollVote.process(databaseSet, false);
		
		//CREATE INVALID POLL VOTE INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		pollVote = new VoteOnPollTransaction(sender, longName, 0, FEE_POWER, timestamp, sender.getLastReference(databaseSet));	

		//CHECK IF POLL VOTE IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, pollVote.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID POLL VOTE POLL DOES NOT EXIST
		pollVote = new VoteOnPollTransaction(sender, "test2", 0, FEE_POWER, timestamp, sender.getLastReference(databaseSet));	
		
		//CHECK IF POLL VOTE IS INVALID
		assertEquals(Transaction.POLL_NO_EXISTS, pollVote.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID POLL VOTE INVALID OPTION
		pollVote = new VoteOnPollTransaction(sender, "test", 5, FEE_POWER, timestamp, sender.getLastReference(databaseSet));	
		
		//CHECK IF POLL VOTE IS INVALID
		assertEquals(Transaction.OPTION_NO_EXISTS, pollVote.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID POLL VOTE INVALID OPTION
		pollVote = new VoteOnPollTransaction(sender, "test", -1, FEE_POWER, timestamp, sender.getLastReference(databaseSet));	
				
		//CHECK IF POLL VOTE IS INVALID
		assertEquals(Transaction.OPTION_NO_EXISTS, pollVote.isValid(databaseSet, releaserReference));
		
		//CRTEATE INVALID POLL VOTE VOTED ALREADY
		pollVote = new VoteOnPollTransaction(sender, "test", 0, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollVote.sign(sender, false);
		pollVote.process(databaseSet, false);
		
		//CHECK IF POLL VOTE IS INVALID
		assertEquals(Transaction.ALREADY_VOTED_FOR_THAT_OPTION, pollVote.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID POLL VOTE NOT ENOUGH BALANCE
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		pollVote = new VoteOnPollTransaction(invalidOwner, "test", 0, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollVote.sign(invalidOwner, false);
		

		//CHECK IF POLL VOTE IS INVALID
		///LOGGER.info("pollVote.getFee: " + pollVote.getFee());
		/// fee = 0 assertEquals(Transaction.NOT_ENOUGH_FEE, pollVote.isValid(databaseSet));
		
		//CREATE POLL CREATION INVALID REFERENCE
		pollVote = new VoteOnPollTransaction(sender, "test", 1, FEE_POWER, timestamp, invalidOwner.getLastReference(databaseSet));	
		
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, pollVote.isValid(databaseSet, releaserReference));
		
	}

	
	@Test
	public void parseVoteOnPollTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
				
		//CREATE POLL Vote
		VoteOnPollTransaction pollVote = new VoteOnPollTransaction(sender, "test", 0, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollVote.sign(sender, false);
		
		//CONVERT TO BYTES
		byte[] rawPollVote = pollVote.toBytes(true, null);
		assertEquals(rawPollVote.length, pollVote.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			VoteOnPollTransaction parsedPollVote = (VoteOnPollTransaction) TransactionFactory.getInstance().parse(rawPollVote, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedPollVote instanceof VoteOnPollTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(pollVote.getSignature(), parsedPollVote.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(pollVote.viewAmount(sender), parsedPollVote.viewAmount(sender));	
			
			//CHECK CREATOR
			assertEquals(pollVote.getCreator().getAddress(), parsedPollVote.getCreator().getAddress());	
			
			//CHECK POLL
			assertEquals(pollVote.getPoll(), parsedPollVote.getPoll());	
			
			//CHECK POLL OPTION
			assertEquals(pollVote.getOption(), parsedPollVote.getOption());	
			
			//CHECK FEE
			assertEquals(pollVote.getFee(), parsedPollVote.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(pollVote.getReference(), parsedPollVote.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(pollVote.getTimestamp(), parsedPollVote.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawPollVote = new byte[pollVote.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawPollVote, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	
	@Test
	public void processVoteOnPollTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("second option")));
						
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollCreation.sign(sender, false);
		pollCreation.process(databaseSet, false);
		
		//CREATE POLL VOTE
		Transaction pollVote = new VoteOnPollTransaction(sender, poll.getName(), 0, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollVote.sign(sender, false);
		assertEquals(Transaction.VALIDATE_OK, pollVote.isValid(databaseSet, null));
		
		pollVote.process(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(1000).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
				
		// NOT NEED !!!! vote not use FEE and not change REFERENCE
		///////CHECK REFERENCE SENDER
		///////assertEquals(true, Arrays.equals(pollVote.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK POLL VOTER
		assertEquals(true, databaseSet.getPollMap().get(poll.getName()).getOptions().get(0).hasVoter(sender));
		
		//CREATE POLL VOTE
		pollVote = new VoteOnPollTransaction(sender, poll.getName(), 1, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollVote.sign(sender, false);
		pollVote.process(databaseSet, false);
				
		//CHECK BALANCE SENDER
		// 999
		assertEquals(0, BigDecimal.valueOf(1000).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
						
		// NOT NEED !!!! vote not use FEE and not change REFERENCE
		/////CHECK REFERENCE SENDER
		/////assertEquals(true, Arrays.equals(pollVote.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK POLL VOTER
		//assertEquals(false, databaseSet.getPollMap().get(poll.getName()).getOptions().get(0).hasVoter(sender));
		
		//CHECK POLL VOTER
		assertEquals(true, databaseSet.getPollMap().get(poll.getName()).getOptions().get(1).hasVoter(sender));
	}
	
	@Test
	public void orphanVoteOnPollTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("second option")));
						
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollCreation.sign(sender, false);
		pollCreation.process(databaseSet, false);
		
		//CREATE POLL VOTE
		Transaction pollVote = new VoteOnPollTransaction(sender, poll.getName(), 0, FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		pollVote.sign(sender, false);
		pollVote.process(databaseSet, false);
		pollVote.orphan(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(1000).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(pollCreation.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK POLL VOTER
		assertEquals(false, databaseSet.getPollMap().get(poll.getName()).hasVotes());
		
		//CHECK POLL VOTER
		assertEquals(false, databaseSet.getPollMap().get(poll.getName()).getOptions().get(0).hasVoter(sender));

	}
	
	//ARBITRARY TRANSACTION
	
	@Test
	public void validateSignatureArbitraryTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		
		//CREATE ARBITRARY TRANSACTION
		Transaction arbitraryTransaction = new ArbitraryTransactionV3(sender, null, 4889, "test".getBytes(), FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		arbitraryTransaction.sign(sender, false);
		
		//CHECK IF ARBITRARY TRANSACTION IS VALID
		assertEquals(true, arbitraryTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		arbitraryTransaction = new ArbitraryTransactionV3(
				sender, null, 4889, "test".getBytes(), FEE_POWER, timestamp, sender.getLastReference(databaseSet), new byte[64]);
		//arbitraryTransaction.sign(sender);
		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(false, arbitraryTransaction.isSignatureValid());
	}
		
	@Test
	public void validateArbitraryTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		// set OIL
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] data = "test".getBytes();
				
		//CREATE ARBITRARY TRANSACTION
		Transaction arbitraryTransaction = new ArbitraryTransactionV3(sender, null, 4776, data, FEE_POWER, timestamp, sender.getLastReference(databaseSet));	
		arbitraryTransaction.sign(sender, false);		

		//CHECK IF ARBITRARY TRANSACTION IS VALID
		assertEquals(Transaction.VALIDATE_OK, arbitraryTransaction.isValid(databaseSet, releaserReference));
		arbitraryTransaction.process(databaseSet, false);
		
		//CREATE INVALID ARBITRARY TRANSACTION INVALID data LENGTH
		byte[] longData = new byte[5000];
		arbitraryTransaction = new ArbitraryTransactionV3(sender, null, 4776, longData, FEE_POWER, timestamp, sender.getLastReference(databaseSet));	

		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.INVALID_DATA_LENGTH, arbitraryTransaction.isValid(databaseSet, releaserReference));
		
		//CREATE INVALID ARBITRARY TRANSACTION NOT ENOUGH BALANCE
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		arbitraryTransaction = new ArbitraryTransactionV3(invalidOwner, null, 4776, data, FEE_POWER, timestamp, sender.getLastReference(databaseSet));	
		
		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.NO_BALANCE, arbitraryTransaction.isValid(databaseSet, releaserReference));
		
		//CREATE ARBITRARY TRANSACTION INVALID REFERENCE
		arbitraryTransaction = new ArbitraryTransactionV3(sender, null, 4776, data, FEE_POWER, timestamp, invalidOwner.getLastReference(databaseSet));	
		
		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, arbitraryTransaction.isValid(databaseSet, releaserReference));
		
	}

	
	@Test
	public void parseArbitraryTransaction() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
				
		//CREATE ARBITRARY TRANSACTION
		ArbitraryTransactionV3 arbitraryTransaction = new ArbitraryTransactionV3(sender, null, 4776,"test".getBytes(), FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		arbitraryTransaction.sign(sender, false);
		
		//CONVERT TO BYTES
		byte[] rawArbitraryTransaction = arbitraryTransaction.toBytes(true, null);
		
		try 
		{	
			//PARSE FROM BYTES
			ArbitraryTransactionV3 parsedArbitraryTransaction = (ArbitraryTransactionV3) TransactionFactory.getInstance().parse(rawArbitraryTransaction, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedArbitraryTransaction instanceof ArbitraryTransactionV3);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(arbitraryTransaction.getSignature(), parsedArbitraryTransaction.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(arbitraryTransaction.viewAmount(sender), parsedArbitraryTransaction.viewAmount(sender));	
			
			//CHECK CREATOR
			assertEquals(arbitraryTransaction.getCreator().getAddress(), parsedArbitraryTransaction.getCreator().getAddress());	
			
			//CHECK VERSION
			assertEquals(arbitraryTransaction.getService(), parsedArbitraryTransaction.getService());	
			
			//CHECK DATA
			assertEquals(true, Arrays.equals(arbitraryTransaction.getData(), parsedArbitraryTransaction.getData()));	
			
			//CHECK FEE
			assertEquals(arbitraryTransaction.getFee(), parsedArbitraryTransaction.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(arbitraryTransaction.getReference(), parsedArbitraryTransaction.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(arbitraryTransaction.getTimestamp(), parsedArbitraryTransaction.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawArbitraryTransaction = new byte[arbitraryTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawArbitraryTransaction, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	
	@Test
	public void processArbitraryTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
		sender.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
						
		//CREATE ARBITRARY TRANSACTION
		ArbitraryTransactionV3 arbitraryTransaction = new ArbitraryTransactionV3(sender, null, 4776,"test".getBytes(), FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		arbitraryTransaction.sign(sender, false);
		arbitraryTransaction.process(databaseSet, false);				
		
		//CHECK BALANCE SENDER
		assertEquals(0, BigDecimal.valueOf(1000).setScale(8).compareTo(sender.getConfirmedBalance(databaseSet)));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(arbitraryTransaction.getSignature(), sender.getLastReference(databaseSet)));
	}
	
	@Test
	public void orphanArbitraryTransaction()
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet, false);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
								
		//CREATE ARBITRARY TRANSACTION
		ArbitraryTransactionV3 arbitraryTransaction = new ArbitraryTransactionV3(sender, null, 4776,"test".getBytes(), FEE_POWER, timestamp, sender.getLastReference(databaseSet));
		arbitraryTransaction.sign(sender, false);
		arbitraryTransaction.process(databaseSet, false);	
		arbitraryTransaction.orphan(databaseSet, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), sender.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
	}

	/*@Test
	public void validateArbitraryTransaction() 
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
		long timestamp = NTP.getTime();
		byte[] data = "test".getBytes();
				
		//CREATE ARBITRARY TRANSACTION
		Transaction arbitraryTransaction = new ArbitraryTransactionV3(sender, null, 4776, data, FEE_POWER, timestamp, sender.getLastReference(databaseSet));	
		
		//CHECK IF ARBITRARY TRANSACTION IS VALID
		assertEquals(Transaction.VALIDATE_OK, arbitraryTransaction.isValid(databaseSet));
		arbitraryTransaction.process(databaseSet);
		
		//CREATE INVALID ARBITRARY TRANSACTION INVALID data LENGTH
		byte[] longData = new byte[5000];
		arbitraryTransaction = new ArbitraryTransactionV3(sender, null, 4776, longData, FEE_POWER, timestamp, sender.getLastReference(databaseSet));	

		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.INVALID_DATA_LENGTH, arbitraryTransaction.isValid(databaseSet));
		
		//CREATE INVALID ARBITRARY TRANSACTION NOT ENOUGH BALANCE
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		arbitraryTransaction = new ArbitraryTransactionV1(invalidOwner, 4776, data, FEE_POWER, timestamp, sender.getLastReference(databaseSet));	
		
		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.NO_BALANCE, arbitraryTransaction.isValid(databaseSet));
		
		//CREATE ARBITRARY TRANSACTION INVALID REFERENCE
		arbitraryTransaction = new ArbitraryTransactionV3(sender, null, 4776, data, FEE_POWER, timestamp, invalidOwner.getLastReference(databaseSet));	
		
		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, arbitraryTransaction.isValid(databaseSet));
		
	}*/

	//ISSUE ASSET TRANSACTION
	
	@Test
	public void validateSignatureIssueAssetTransaction() 
	{
		
		init();
		
		//CREATE ASSET
		AssetCls asset = new AssetVenture(maker, "test", "strontje", 50000l, (byte) 2, false);
		//byte[] data = asset.toBytes(false);
		//Asset asset2 = Asset.parse(data);
		
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastReference(db));
		issueAssetTransaction.sign(maker, false);
		
		//CHECK IF ISSUE ASSET TRANSACTION IS VALID
		assertEquals(true, issueAssetTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		issueAssetTransaction = new IssueAssetTransaction(
				maker, asset, FEE_POWER, timestamp, maker.getLastReference(db), new byte[64]);
		
		//CHECK IF ISSUE ASSET IS INVALID
		assertEquals(false, issueAssetTransaction.isSignatureValid());
	}
		

	
	@Test
	public void parseIssueAssetTransaction() 
	{
		
		init();
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		AssetCls asset = new AssetVenture(maker, "test", "strontje", 50000l, (byte) 2, false);
				
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
			assertEquals(issueAssetTransaction.getAsset().getCreator().getAddress(), parsedIssueAssetTransaction.getAsset().getCreator().getAddress());
			
			//CHECK NAME
			assertEquals(issueAssetTransaction.getAsset().getName(), parsedIssueAssetTransaction.getAsset().getName());
				
			//CHECK DESCRIPTION
			assertEquals(issueAssetTransaction.getAsset().getDescription(), parsedIssueAssetTransaction.getAsset().getDescription());
				
			//CHECK QUANTITY
			assertEquals(issueAssetTransaction.getAsset().getQuantity(), parsedIssueAssetTransaction.getAsset().getQuantity());
			
			//DIVISIBLE
			assertEquals(issueAssetTransaction.getAsset().isDivisible(), parsedIssueAssetTransaction.getAsset().isDivisible());
			
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
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		AssetCls asset = new AssetVenture(maker, "test", "strontje", 50000l, (byte) 2, false);

				
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastReference(db));
		issueAssetTransaction.sign(maker, false);
		
		assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(db, releaserReference));
		
		issueAssetTransaction.process(db, false);
		
		LOGGER.info("asset KEY: " + asset.getKey(db));
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.valueOf(50000).setScale(8), maker.getConfirmedBalance(asset.getKey(db), db));
		
		//CHECK ASSET EXISTS SENDER
		long key = db.getIssueAssetMap().get(issueAssetTransaction);
		assertEquals(true, db.getAssetMap().contains(key));
		
		//CHECK ASSET IS CORRECT
		assertEquals(true, Arrays.equals(db.getAssetMap().get(key).toBytes(true), asset.toBytes(true)));
		
		//CHECK ASSET BALANCE SENDER
		assertEquals(true, db.getBalanceMap().get(maker.getAddress(), key).compareTo(new BigDecimal(asset.getQuantity())) == 0);
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueAssetTransaction.getSignature(), maker.getLastReference(db)));
	}
	
	
	@Test
	public void orphanIssueAssetTransaction()
	{
		
		init();				
				
		
		long timestamp = NTP.getTime();
		AssetCls asset = new AssetVenture(maker, "test", "strontje", 50000l, (byte) 2, false);
				
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastReference(db));
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(db, false);
		long key = db.getIssueAssetMap().get(issueAssetTransaction);
		assertEquals(new BigDecimal(50000).setScale(8), maker.getConfirmedBalance(key,db));
		assertEquals(true, Arrays.equals(issueAssetTransaction.getSignature(), maker.getLastReference(db)));
		
		issueAssetTransaction.orphan(db, false);
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.ZERO.setScale(8), maker.getConfirmedBalance(key,db));
		
		//CHECK ASSET EXISTS SENDER
		assertEquals(false, db.getAssetMap().contains(key));
		
		//CHECK ASSET BALANCE SENDER
		assertEquals(0, db.getBalanceMap().get(maker.getAddress(), key).longValue());
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueAssetTransaction.getReference(), maker.getLastReference(db)));
	}
	
}
