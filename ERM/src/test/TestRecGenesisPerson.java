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
import core.item.persons.PersonCls;
import core.item.persons.PersonHuman;
import core.item.statuses.StatusCls;
import core.transaction.GenesisIssuePersonRecord;
import core.transaction.GenesisTransaction;
import core.transaction.GenesisCertifyPersonRecord;
//import core.transaction.IssuePersonTransaction;
//import core.transaction.R_SignNote;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import database.DBSet;
import database.AddressPersonMap;
import database.PersonAddressMap;
import database.PersonStatusMap;

public class TestRecGenesisPerson {

	static Logger LOGGER = Logger.getLogger(TestRecGenesisPerson.class.getName());

	byte[] releaserReference = null;

	long FEE_KEY = Transaction.FEE_KEY;
	byte FEE_POWER = (byte)1;
	byte[] packedReference = new byte[64];
	long timestamp = NTP.getTime();
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
	PersonCls person;
	long keyPerson = -1l;
	GenesisIssuePersonRecord genesisIssuePersonTransaction;

	PersonStatusMap dbPS;
	PersonAddressMap dbPA;
	AddressPersonMap dbAP;
	
	private void initIssue(boolean toProcess) {
	
		//CREATE EMPTY MEMORY DATABASE
		db = DBSet.createEmptyDatabaseSet();
		dbPA = db.getPersonAddressMap();
		dbAP = db.getAddressPersonMap();
		dbPS = db.getPersonStatusMap();
		
		//CREATE PERSON
		//person = GenesisBlock.makePerson(0);
		person = new PersonHuman(maker, "ERMOLAEV DMITRII SERGEEVICH", -106185600,
				(byte)1, "Slav", (float)1.1, (float)1.1,
				"white", "gray", "dark", (int) 188, "icreator");
		//byte[] rawPerson = person.toBytes(true); // reference is new byte[64]
		//assertEquals(rawPerson.length, person.getDataLength());
				
		//CREATE ISSUE PERSON TRANSACTION
		genesisIssuePersonTransaction = new GenesisIssuePersonRecord(person, timestamp);
		if (toProcess)
		{ 
			genesisIssuePersonTransaction.process(db, false);
			keyPerson = person.getKey(db);
		}
		
	}
	
	//GENESIS
	
	// GENESIS ISSUE
	@Test
	public void validateGenesisIssuePersonRecord() 
	{
		
		initIssue(false);
		
		//genesisIssuePersonTransaction.sign(creator);
		//CHECK IF ISSUE PERSON TRANSACTION IS VALID
		assertEquals(true, genesisIssuePersonTransaction.isSignatureValid());
		assertEquals(Transaction.VALIDATE_OK, genesisIssuePersonTransaction.isValid(db, releaserReference));
				
		//CONVERT TO BYTES
		//LOGGER.info("CREATOR: " + genesisIssuePersonTransaction.getCreator().getPublicKey());
		byte[] rawGenesisIssuePersonRecord = genesisIssuePersonTransaction.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawGenesisIssuePersonRecord.length, genesisIssuePersonTransaction.getDataLength(false));
		//LOGGER.info("rawGenesisIssuePersonRecord.length") + ": + rawGenesisIssuePersonRecord.length);
		
		try 
		{	
			//PARSE FROM BYTES
			GenesisIssuePersonRecord parsedGenesisIssuePersonRecord = (GenesisIssuePersonRecord) TransactionFactory.getInstance().parse(rawGenesisIssuePersonRecord, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedGenesisIssuePersonRecord instanceof GenesisIssuePersonRecord);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(genesisIssuePersonTransaction.getSignature(), parsedGenesisIssuePersonRecord.getSignature()));
									
			//CHECK NAME
			assertEquals(genesisIssuePersonTransaction.getItem().getName(), parsedGenesisIssuePersonRecord.getItem().getName());
				
			//CHECK DESCRIPTION
			assertEquals(genesisIssuePersonTransaction.getItem().getDescription(), parsedGenesisIssuePersonRecord.getItem().getDescription());
							
			//CHECK FEE
			assertEquals(genesisIssuePersonTransaction.getFeePow(), parsedGenesisIssuePersonRecord.getFeePow());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(genesisIssuePersonTransaction.getReference(), parsedGenesisIssuePersonRecord.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(genesisIssuePersonTransaction.getTimestamp(), parsedGenesisIssuePersonRecord.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction." + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawGenesisIssuePersonRecord = new byte[genesisIssuePersonTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawGenesisIssuePersonRecord, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	
	@Test
	public void parseGenesisIssuePersonRecord() 
	{
		
		initIssue(false);
		
		//CONVERT TO BYTES
		byte[] rawGenesisIssuePersonRecord = genesisIssuePersonTransaction.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawGenesisIssuePersonRecord.length, genesisIssuePersonTransaction.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			GenesisIssuePersonRecord parsedGenesisIssuePersonRecord = (GenesisIssuePersonRecord) TransactionFactory.getInstance().parse(rawGenesisIssuePersonRecord, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedGenesisIssuePersonRecord instanceof GenesisIssuePersonRecord);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(genesisIssuePersonTransaction.getSignature(), parsedGenesisIssuePersonRecord.getSignature()));
						
			//CHECK OWNER
			assertEquals(genesisIssuePersonTransaction.getItem().getCreator().getAddress(), parsedGenesisIssuePersonRecord.getItem().getCreator().getAddress());
			
			//CHECK NAME
			assertEquals(genesisIssuePersonTransaction.getItem().getName(), parsedGenesisIssuePersonRecord.getItem().getName());
				
			//CHECK DESCRIPTION
			assertEquals(genesisIssuePersonTransaction.getItem().getDescription(), parsedGenesisIssuePersonRecord.getItem().getDescription());
							
			//CHECK FEE
			assertEquals(genesisIssuePersonTransaction.getFeePow(), parsedGenesisIssuePersonRecord.getFeePow());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(genesisIssuePersonTransaction.getReference(), parsedGenesisIssuePersonRecord.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(genesisIssuePersonTransaction.getTimestamp(), parsedGenesisIssuePersonRecord.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction." + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawGenesisIssuePersonRecord = new byte[genesisIssuePersonTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawGenesisIssuePersonRecord, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	
	@Test
	public void processGenesisIssuePersonRecord()
	{
		
		initIssue(true);		
		LOGGER.info("person KEY: " + keyPerson);
				
		//CHECK PERSON EXISTS SENDER
		long key = db.getIssuePersonMap().get(genesisIssuePersonTransaction);
		assertEquals(true, db.getPersonMap().contains(key));
		
		//CHECK PERSON IS CORRECT
		assertEquals(true, Arrays.equals(db.getPersonMap().get(key).toBytes(true), person.toBytes(true)));
						
	}
	
	
	@Test
	public void orphanIssuePersonTransaction()
	{
		
		initIssue(true);
		
		genesisIssuePersonTransaction.orphan(db, false);
				
		//CHECK PERSON EXISTS SENDER
		assertEquals(false, db.getPersonMap().contains(keyPerson));

	}

	//GENESIS TRANSFER PERSON
	
	@Test
	public void validateSignatureGenesisCertifyPersonRecord() 
	{
		
		initIssue(false);
		
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();
		
		//CREATE PERSON TRANSFER
		Transaction personTransfer = new GenesisCertifyPersonRecord(recipient, keyPerson, timestamp);
		//personTransfer.sign(sender);
		
		//CHECK IF PERSON TRANSFER SIGNATURE IS VALID
		assertEquals(true, personTransfer.isSignatureValid());		
	}
	
	@Test
	public void validateGenesisCertifyPersonRecord() 
	{
		
		initIssue(true);
		
		//CREATE SIGNATURE
		Account recipient = new Account("7FUUEjDSo9J4CYon4tsokMCPmfP4YggPnd");

		//CREATE VALID PERSON TRANSFER
		Transaction personTransfer = new GenesisCertifyPersonRecord(recipient, keyPerson, timestamp);

		//CHECK IF PERSON TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, personTransfer.isValid(db, releaserReference));

		personTransfer.process(db, false);

		//CHECK IF PERSON TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, personTransfer.isValid(db, releaserReference));			
		
		//CREATE INVALID PERSON TRANSFER INVALID RECIPIENT ADDRESS
		personTransfer = new GenesisCertifyPersonRecord(new Account("test"), keyPerson, timestamp);	
		//CHECK IF PERSON TRANSFER IS INVALID
		assertEquals(Transaction.INVALID_ADDRESS, personTransfer.isValid(db, releaserReference));
				
		//CREATE INVALID PERSON
		personTransfer = new GenesisCertifyPersonRecord(new Account("7FUUEjDSo9J4CYon4tsokMCPmfP4YggPnd"), 111, timestamp);
		//CHECK IF PERSON TRANSFER IS INVALID
		assertEquals(Transaction.ITEM_PERSON_NOT_EXIST, personTransfer.isValid(db, releaserReference));	
		
	}
	
	@Test
	public void parseGenesisCertifyPersonRecord() 
	{

		initIssue(true);		
		
		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

		//CREATE VALID PERSON TRANSFER
		GenesisCertifyPersonRecord genesisTransferPerson = new GenesisCertifyPersonRecord(recipient, keyPerson, timestamp);

		//CONVERT TO BYTES
		byte[] rawGenesisTransferPerson = genesisTransferPerson.toBytes(true, null);
		
		//CHECK DATALENGTH
		assertEquals(rawGenesisTransferPerson.length, genesisTransferPerson.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			GenesisCertifyPersonRecord parsedPersonTransfer = (GenesisCertifyPersonRecord) TransactionFactory.getInstance().parse(rawGenesisTransferPerson, releaserReference);
			LOGGER.info(" 1: " + parsedPersonTransfer.getKey() );

			//CHECK INSTANCE
			assertEquals(true, parsedPersonTransfer instanceof GenesisCertifyPersonRecord);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(genesisTransferPerson.getSignature(), parsedPersonTransfer.getSignature()));
			
			//CHECK KEY
			assertEquals(genesisTransferPerson.getKey(), parsedPersonTransfer.getKey());	
									
			//CHECK FEE
			assertEquals(genesisTransferPerson.getFeePow(), parsedPersonTransfer.getFeePow());	
						
			//CHECK TIMESTAMP
			assertEquals(genesisTransferPerson.getTimestamp(), parsedPersonTransfer.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawGenesisTransferPerson = new byte[genesisTransferPerson.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawGenesisTransferPerson, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void process_orphan_GenesisCertifyPersonRecord()
	{

		initIssue(true);

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
			
		//CREATE PERSON TRANSFER
		GenesisCertifyPersonRecord personTransfer = new GenesisCertifyPersonRecord(recipient, keyPerson, timestamp);
		String address = personTransfer.getRecipient().getAddress();

		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(personTransfer.getSignature(), recipient.getLastReference(db)));
	
		// ADDRESS -> PERSON
		assertEquals( null, dbAP.getItem(address));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA.getItem(keyPerson, address));
		// PERSON STATUS ALIVE
		assertEquals( null, dbPS.getItem(keyPerson)); // , StatusCls.ALIVE_KEY

		/// PROCESS /////
		personTransfer.process(db, false);

		//CHECK REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(personTransfer.getSignature(), recipient.getLastReference(db)));
		
		// .a - personKey, .b - duration, .c - block height, .d - reference
		assertEquals( (long)keyPerson, (long)dbAP.getItem(address).a);
		assertEquals( Integer.MAX_VALUE, (int)dbAP.getItem(address).b);
		assertEquals( 0, (int)dbAP.getItem(address).c);
		assertEquals( true, Arrays.equals(dbAP.getItem(address).d, personTransfer.getSignature()));
		// PERSON -> ADDRESS
		assertEquals( Integer.MAX_VALUE, (int)dbPA.getItem(keyPerson, address).a);
		assertEquals( 0, (int)dbPA.getItem(keyPerson, address).b);
		assertEquals( true, Arrays.equals(dbPA.getItem(keyPerson, address).c, personTransfer.getSignature()));
		// PERSON STATUS ALIVE
		assertEquals( Integer.MAX_VALUE, (int)dbPS.getItem(keyPerson).a);
		assertEquals( 0, (int)dbPS.getItem(keyPerson).b);
		assertEquals( true, Arrays.equals(dbPS.getItem(keyPerson).c, personTransfer.getSignature()));
	
		personTransfer.orphan(db, false);
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(personTransfer.getSignature(), recipient.getLastReference(db)));
	
		// ADDRESS -> PERSON
		assertEquals( null, dbAP.getItem(address));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA.getItem(keyPerson, address));
		// PERSON STATUS ALIVE
		assertEquals( null, dbPS.getItem(keyPerson)); // , StatusCls.ALIVE_KEY

	}
}
