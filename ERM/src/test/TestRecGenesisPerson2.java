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
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import database.DBSet;
import database.AddressPersonMap;
import database.PersonAddressMap;
import database.PersonStatusMap;

public class TestRecGenesisPerson2 {

	static Logger LOGGER = Logger.getLogger(TestRecGenesisPerson2.class.getName());

	byte[] releaserReference = null;

	long FEE_KEY = Transaction.FEE_KEY;
	byte FEE_POWER = (byte)1;
	byte[] packedReference = new byte[64];
	
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
		genesisIssuePersonTransaction = new GenesisIssuePersonRecord(person, maker);
		if (toProcess)
		{ 
			genesisIssuePersonTransaction.process(db, false);
			keyPerson = person.getKey();
		}
		
	}
		
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
		
		//CREATE INVALID PERSON TRANSFER INVALID RECIPIENT ADDRESS
		genesisIssuePersonTransaction = new GenesisIssuePersonRecord(person, new Account("test"));	
		//CHECK IF PERSON TRANSFER IS INVALID
		assertEquals(Transaction.INVALID_ADDRESS, genesisIssuePersonTransaction.isValid(db, releaserReference));

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
							
			assertEquals(genesisIssuePersonTransaction.getItem().getKey(), parsedGenesisIssuePersonRecord.getItem().getKey());	

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
	public void process_orphan_GenesisIssuePersonRecord()
	{
		

		initIssue(false);		
		LOGGER.info("person KEY: " + keyPerson);

		String address = genesisIssuePersonTransaction.getRecipient().getAddress();

		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(genesisIssuePersonTransaction.getSignature(), maker.getLastReference(db)));
		// ADDRESS -> PERSON
		assertEquals( null, dbAP.getItem(address));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA.getItem(keyPerson, address));
		// PERSON STATUS ALIVE
		assertEquals( null, dbPS.getItem(keyPerson)); // , StatusCls.ALIVE_KEY

		genesisIssuePersonTransaction.process(db, false);
		keyPerson = person.getKey();

		//CHECK PERSON EXISTS SENDER
		assertEquals(true, db.getItemPersonMap().contains(keyPerson));
		assertEquals(genesisIssuePersonTransaction.getItem().getKey(), keyPerson);
		assertEquals(genesisIssuePersonTransaction.getItem().getName(), person.getName());
		
		//CHECK PERSON IS CORRECT
		assertEquals(true, Arrays.equals(db.getItemPersonMap().get(keyPerson).toBytes(true), person.toBytes(true)));

		//CHECK REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(genesisIssuePersonTransaction.getSignature(), maker.getLastReference(db)));
		
		// .a - personKey, .b - end_date, .c - block height, .d - reference
		assertEquals( (long)keyPerson, (long)dbAP.getItem(address).a);
		assertEquals( Integer.MAX_VALUE, (int)dbAP.getItem(address).b);
		assertEquals( 0, (int)dbAP.getItem(address).c);
		assertEquals( true, Arrays.equals(dbAP.getItem(address).d, genesisIssuePersonTransaction.getSignature()));
		// PERSON -> ADDRESS
		assertEquals( Integer.MAX_VALUE, (int)dbPA.getItem(keyPerson, address).a);
		assertEquals( 0, (int)dbPA.getItem(keyPerson, address).b);
		assertEquals( true, Arrays.equals(dbPA.getItem(keyPerson, address).c, genesisIssuePersonTransaction.getSignature()));
		// PERSON STATUS ALIVE
		assertEquals( Integer.MAX_VALUE, (int)dbPS.getItem(keyPerson).a);
		assertEquals( 0, (int)dbPS.getItem(keyPerson).b);
		assertEquals( true, Arrays.equals(dbPS.getItem(keyPerson).c, genesisIssuePersonTransaction.getSignature()));

		assertEquals(true, genesisIssuePersonTransaction.getRecipient().isPerson(db));
		assertEquals(true, maker.isPerson(db));

		/////////////////
		///// ORPHAN ////
		genesisIssuePersonTransaction.orphan(db, false);
		
		assertEquals(false, db.getItemPersonMap().contains(keyPerson));

		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(genesisIssuePersonTransaction.getSignature(), maker.getLastReference(db)));
	
		// ADDRESS -> PERSON
		assertEquals( null, dbAP.getItem(address));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA.getItem(keyPerson, address));
		// PERSON STATUS ALIVE
		assertEquals( null, dbPS.getItem(keyPerson)); // , StatusCls.ALIVE_KEY
		
		assertEquals(false, genesisIssuePersonTransaction.getRecipient().isPerson(db));
		assertEquals(false, maker.isPerson(db));

	}
}
