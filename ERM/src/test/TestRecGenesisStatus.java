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

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.statuses.StatusCls;
import core.transaction.GenesisIssueStatusTransaction;
import core.transaction.GenesisTransaction;
import core.transaction.GenesisTransferStatusTransaction;
//import core.transaction.IssueStatusTransaction;
//import core.transaction.R_SignNote;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import database.DBSet;;

public class TestRecGenesisStatus {

	static Logger LOGGER = Logger.getLogger(TestRecGenesisStatus.class.getName());

	byte[] releaserReference = null;

	long FEE_KEY = Transaction.FEE_KEY;
	byte FEE_POWER = (byte)1;
	byte[] statusReference = new byte[64];
	long timestamp = NTP.getTime();
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
	StatusCls status;
	long key = -1l;
	GenesisIssueStatusTransaction genesisIssueStatusTransaction;
	
	private void initIssue(boolean toProcess) {
	
		//CREATE EMPTY MEMORY DATABASE
		db = DBSet.createEmptyDatabaseSet();
		
		//CREATE STATUS
		status = GenesisBlock.makeStatus(0);
		//byte[] rawStatus = status.toBytes(true); // reference is new byte[64]
		//assertEquals(rawStatus.length, status.getDataLength());
				
		//CREATE ISSUE STATUS TRANSACTION
		genesisIssueStatusTransaction = new GenesisIssueStatusTransaction(maker, status, timestamp);
		if (toProcess)
		{ 
			genesisIssueStatusTransaction.process(db, false);
			key = status.getKey(db);
		}
		
	}
	
	//GENESIS
	
	// GENESIS ISSUE
	@Test
	public void validateGenesisIssueStatusTransaction() 
	{
		
		initIssue(false);
		
		//genesisIssueStatusTransaction.sign(creator);
		//CHECK IF ISSUE STATUS TRANSACTION IS VALID
		assertEquals(true, genesisIssueStatusTransaction.isSignatureValid());
		assertEquals(Transaction.VALIDATE_OK, genesisIssueStatusTransaction.isValid(db, releaserReference));
				
		//CONVERT TO BYTES
		//LOGGER.info("CREATOR: " + genesisIssueStatusTransaction.getCreator().getPublicKey());
		byte[] rawGenesisIssueStatusTransaction = genesisIssueStatusTransaction.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawGenesisIssueStatusTransaction.length, genesisIssueStatusTransaction.getDataLength(false));
		//LOGGER.info("rawGenesisIssueStatusTransaction.length") + ": + rawGenesisIssueStatusTransaction.length);
		
		try 
		{	
			//PARSE FROM BYTES
			GenesisIssueStatusTransaction parsedGenesisIssueStatusTransaction = (GenesisIssueStatusTransaction) TransactionFactory.getInstance().parse(rawGenesisIssueStatusTransaction, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedGenesisIssueStatusTransaction instanceof GenesisIssueStatusTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(genesisIssueStatusTransaction.getSignature(), parsedGenesisIssueStatusTransaction.getSignature()));
			
			//CHECK ISSUER
			assertEquals(genesisIssueStatusTransaction.getCreator().getAddress(), parsedGenesisIssueStatusTransaction.getCreator().getAddress());
						
			//CHECK NAME
			assertEquals(genesisIssueStatusTransaction.getStatus().getName(), parsedGenesisIssueStatusTransaction.getStatus().getName());
				
			//CHECK DESCRIPTION
			assertEquals(genesisIssueStatusTransaction.getStatus().getDescription(), parsedGenesisIssueStatusTransaction.getStatus().getDescription());
							
			//CHECK FEE
			assertEquals(genesisIssueStatusTransaction.getFeePow(), parsedGenesisIssueStatusTransaction.getFeePow());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(genesisIssueStatusTransaction.getReference(), parsedGenesisIssueStatusTransaction.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(genesisIssueStatusTransaction.getTimestamp(), parsedGenesisIssueStatusTransaction.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction." + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawGenesisIssueStatusTransaction = new byte[genesisIssueStatusTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawGenesisIssueStatusTransaction, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	
	@Test
	public void parseGenesisIssueStatusTransaction() 
	{
		
		initIssue(false);
		
		//CONVERT TO BYTES
		byte[] rawGenesisIssueStatusTransaction = genesisIssueStatusTransaction.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawGenesisIssueStatusTransaction.length, genesisIssueStatusTransaction.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			GenesisIssueStatusTransaction parsedGenesisIssueStatusTransaction = (GenesisIssueStatusTransaction) TransactionFactory.getInstance().parse(rawGenesisIssueStatusTransaction, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedGenesisIssueStatusTransaction instanceof GenesisIssueStatusTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(genesisIssueStatusTransaction.getSignature(), parsedGenesisIssueStatusTransaction.getSignature()));
			
			//CHECK ISSUER
			assertEquals(genesisIssueStatusTransaction.getCreator().getAddress(), parsedGenesisIssueStatusTransaction.getCreator().getAddress());
			
			//CHECK OWNER
			assertEquals(genesisIssueStatusTransaction.getStatus().getCreator().getAddress(), parsedGenesisIssueStatusTransaction.getStatus().getCreator().getAddress());
			
			//CHECK NAME
			assertEquals(genesisIssueStatusTransaction.getStatus().getName(), parsedGenesisIssueStatusTransaction.getStatus().getName());
				
			//CHECK DESCRIPTION
			assertEquals(genesisIssueStatusTransaction.getStatus().getDescription(), parsedGenesisIssueStatusTransaction.getStatus().getDescription());
							
			//CHECK FEE
			assertEquals(genesisIssueStatusTransaction.getFeePow(), parsedGenesisIssueStatusTransaction.getFeePow());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(genesisIssueStatusTransaction.getReference(), parsedGenesisIssueStatusTransaction.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(genesisIssueStatusTransaction.getTimestamp(), parsedGenesisIssueStatusTransaction.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction." + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawGenesisIssueStatusTransaction = new byte[genesisIssueStatusTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawGenesisIssueStatusTransaction, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	
	@Test
	public void processGenesisIssueStatusTransaction()
	{
		
		initIssue(true);		
		LOGGER.info("status KEY: " + key);
				
		//CHECK STATUS EXISTS SENDER
		long key = db.getIssueStatusMap().get(genesisIssueStatusTransaction);
		assertEquals(true, db.getItemStatusMap().contains(key));
		
		//CHECK STATUS IS CORRECT
		assertEquals(true, Arrays.equals(db.getItemStatusMap().get(key).toBytes(true), status.toBytes(true)));
		
		//CHECK STATUS BALANCE SENDER
		assertEquals(0, db.getStatusTimeMap().get(maker.getAddress(), key).get(0).longValue());
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(genesisIssueStatusTransaction.getSignature(), maker.getLastReference(db)));
	}
	
	
	@Test
	public void orphanIssueStatusTransaction()
	{
		
		initIssue(true);

		assertEquals(true, Arrays.equals(genesisIssueStatusTransaction.getSignature(), maker.getLastReference(db)));
		
		genesisIssueStatusTransaction.orphan(db, false);
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.ZERO.setScale(8), maker.getConfirmedBalance(key,db));
		
		//CHECK STATUS EXISTS SENDER
		assertEquals(false, db.getItemStatusMap().contains(key));
		
		//CHECK STATUS BALANCE SENDER
		assertEquals(0, db.getStatusTimeMap().get(maker.getAddress(), key).get(0).longValue());
				
		//CHECK REFERENCE SENDER
		// it for not genesis - assertEquals(true, Arrays.equals(genesisIssueStatusTransaction.getReference(), maker.getLastReference(db)));
		assertEquals(true, Arrays.equals(new byte[0], maker.getLastReference(db)));

	}

	//GENESIS TRANSFER STATUS
	
	@Test
	public void validateSignatureGenesisTransferStatusTransaction() 
	{
		
		initIssue(false);
		
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
		long timestamp = NTP.getTime();
		
		//CREATE STATUS TRANSFER
		Transaction statusTransfer = new GenesisTransferStatusTransaction(maker, recipient, key, timestamp);
		//statusTransfer.sign(sender);
		
		//CHECK IF STATUS TRANSFER SIGNATURE IS VALID
		assertEquals(true, statusTransfer.isSignatureValid());		
	}
	
	@Test
	public void validateGenesisTransferStatusTransaction() 
	{
		
		initIssue(true);
		
		//CREATE SIGNATURE
		Account recipient = new Account("QgcphUTiVHHfHg8e1LVgg5jujVES7ZDUTr");

		//CREATE VALID STATUS TRANSFER
		Transaction statusTransfer = new GenesisTransferStatusTransaction(maker, recipient, key, timestamp);

		//CHECK IF STATUS TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, statusTransfer.isValid(db, releaserReference));

		statusTransfer.process(db, false);

		//CREATE VALID STATUS TRANSFER
		maker.setConfirmedBalance(1, BigDecimal.valueOf(100).setScale(8), db);
		statusTransfer = new GenesisTransferStatusTransaction(maker, recipient, key, timestamp);

		//CHECK IF STATUS TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, statusTransfer.isValid(db, releaserReference));			
		
		//CREATE INVALID STATUS TRANSFER INVALID RECIPIENT ADDRESS
		statusTransfer = new GenesisTransferStatusTransaction(maker, new Account("test"), key, timestamp);
	
		//CHECK IF STATUS TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, statusTransfer.isValid(db, releaserReference));
				
		//CHECK IF STATUS TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, statusTransfer.isValid(db, releaserReference));	
		
		//CREATE INVALID STATUS TRANSFER NOT ENOUGH STATUS BALANCE
		statusTransfer = new GenesisTransferStatusTransaction(maker, recipient, key, timestamp);
		
		//CHECK IF STATUS TRANSFER IS INVALID
		// nor need for genesis - assertNotEquals(Transaction.VALIDATE_OK, statusTransfer.isValid(db));
	}
	
	@Test
	public void parseGenesisTransferStatusTransaction() 
	{

		initIssue(true);		
		
		//CREATE SIGNATURE
		Account recipient = new Account("QgcphUTiVHHfHg8e1LVgg5jujVES7ZDUTr");

		//CREATE VALID STATUS TRANSFER
		GenesisTransferStatusTransaction genesisTransferStatus = new GenesisTransferStatusTransaction(maker, recipient, key, timestamp);
		//genesisTransferStatus.sign(maker);
		//genesisTransferStatus.process(db);

		//CONVERT TO BYTES
		byte[] rawGenesisTransferStatus = genesisTransferStatus.toBytes(true, null);
		
		//CHECK DATALENGTH
		assertEquals(rawGenesisTransferStatus.length, genesisTransferStatus.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			GenesisTransferStatusTransaction parsedStatusTransfer = (GenesisTransferStatusTransaction) TransactionFactory.getInstance().parse(rawGenesisTransferStatus, releaserReference);
			LOGGER.info(" 1: " + parsedStatusTransfer.getKey() );

			//CHECK INSTANCE
			assertEquals(true, parsedStatusTransfer instanceof GenesisTransferStatusTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(genesisTransferStatus.getSignature(), parsedStatusTransfer.getSignature()));
			
			//CHECK KEY
			assertEquals(genesisTransferStatus.getKey(), parsedStatusTransfer.getKey());	
			
			//CHECK AMOUNT SENDER
			assertEquals(genesisTransferStatus.viewAmount(maker), parsedStatusTransfer.viewAmount(maker));	
			
			//CHECK AMOUNT RECIPIENT
			assertEquals(genesisTransferStatus.viewAmount(recipient), parsedStatusTransfer.viewAmount(recipient));	
			
			//CHECK FEE
			assertEquals(genesisTransferStatus.getFeePow(), parsedStatusTransfer.getFeePow());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(genesisTransferStatus.getReference(), parsedStatusTransfer.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(genesisTransferStatus.getTimestamp(), parsedStatusTransfer.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawGenesisTransferStatus = new byte[genesisTransferStatus.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawGenesisTransferStatus, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void processGenesisTransferStatusTransaction()
	{

		initIssue(false);		
		BigDecimal maker_balance = maker.getConfirmedBalance(FEE_KEY, db);
		genesisIssueStatusTransaction.process(false);
		
		//CHECK BALANCE SENDER
		assertEquals(maker_balance, maker.getConfirmedBalance(key, db));
			
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
			
		//CREATE STATUS TRANSFER
		Transaction statusTransfer = new GenesisTransferStatusTransaction(maker, recipient, key, timestamp);
		// statusTransfer.sign(sender); // not  NEED
		statusTransfer.process(db, false);
		
		//CHECK BALANCE SENDER
		assertEquals(maker_balance, maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(maker_balance, recipient.getConfirmedBalance(key, db));
		
		/* not NEED
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(statusTransfer.getSignature(), sender.getLastReference(databaseSet)));
		*/
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(statusTransfer.getSignature(), recipient.getLastReference(db)));
	}
	
	@Test
	public void orphanGenesisTransferStatusTransaction()
	{
		
		initIssue(false);		
		BigDecimal maker_balance = maker.getConfirmedBalance(FEE_KEY, db);
					
		//CREATE SIGNATURE
		Account recipient = new Account("QUGKmr4JJjJRoHo9wNYKZa1Lvem7FHRXfU");
			
		//CREATE STATUS TRANSFER
		Transaction statusTransfer = new GenesisTransferStatusTransaction(maker, recipient, key, timestamp);
		// statusTransfer.sign(sender); not NEED
		statusTransfer.process(db, false);
		statusTransfer.orphan(db, false);
		
		//CHECK BALANCE SENDER
		assertEquals(maker_balance, maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(8), recipient.getConfirmedBalance(key, db));
		
		/* not NEED
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
		*/
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(statusTransfer.getSignature(), recipient.getLastReference(db)));
	}
	
}
