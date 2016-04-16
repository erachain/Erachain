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

//import com.google.common.primitives.Longs;

import database.DBSet;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.item.persons.PersonCls;
import qora.item.persons.PersonHuman;
//import qora.item.persons.PersonUnique;
import qora.block.GenesisBlock;
import qora.crypto.Crypto;
import qora.transaction.CancelOrderTransaction;
import qora.transaction.CreateOrderTransaction;
//import qora.transaction.GenesisTransaction;
import qora.transaction.IssuePersonRecord;
import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;
import qora.transaction.PersonalizeRecord;
import qora.wallet.Wallet;

public class TestRecPerson {

	static Logger LOGGER = Logger.getLogger(TestRecPerson.class.getName());

	byte[] releaserReference = null;

	BigDecimal BG_ZERO = BigDecimal.ZERO.setScale(8);
	long OIL_KEY = Transaction.OIL_KEY;
	long VOTE_KEY = Transaction.VOTE_KEY;
	byte FEE_POWER = (byte)1;
	byte[] personReference = new byte[64];
	long timestamp = NTP.getTime();
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);

	//GENERATE ACCOUNT SEED
	int nonce = 1;
	//byte[] accountSeed;
	//qora.wallet.Wallet.generateAccountSeed(byte[], int)
	byte[] accountSeed1 = Wallet.generateAccountSeed(seed, nonce++);
    PrivateKeyAccount userAccount1 = new PrivateKeyAccount(accountSeed1);
    byte[] accountSeed2 = Wallet.generateAccountSeed(seed, nonce++);
    PrivateKeyAccount userAccount2 = new PrivateKeyAccount(accountSeed2);
    byte[] accountSeed3 = Wallet.generateAccountSeed(seed, nonce++);
    PrivateKeyAccount userAccount3 = new PrivateKeyAccount(accountSeed3);

	
	PersonCls person;
	long key = -1;
	IssuePersonRecord issuePersonTransaction;
	PersonalizeRecord personalizeRecord;

	// INIT PERSONS
	private void init() {
		
		db = DBSet.createEmptyDatabaseSet();
		gb = new GenesisBlock();
		gb.process(db);
		
		// OIL FUND
		maker.setLastReference(gb.getGeneratorSignature(), db);
		maker.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), db);
		
		byte gender = 1;
		person = new PersonHuman(maker, "Ermolaev Dmitrii Sergeevich", timestamp - 12345678,
				gender, "Slav", (float)128.12345, (float)33.7777,
				"white", "green", "шанет", 188, "изобретатель, мыслитель, создатель идей");

		//CREATE ISSUE PERSON TRANSACTION
		issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp, maker.getLastReference(db));

	}
	
	public void initPersonalize() {

		issuePersonTransaction.sign(maker, false);
		assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(db, releaserReference));
		
		issuePersonTransaction.process(db, false);
		key = person.getKey(db);
		
		int duration = 356;
		//CREATE PERSONALIZE REcORD
		personalizeRecord = new PersonalizeRecord(maker, FEE_POWER, key,
				userAccount1, userAccount2, userAccount3, duration,
				timestamp, maker.getLastReference(db));

	}
	
	//ISSUE PERSON TRANSACTION
	
	@Test
	public void validateSignatureIssuePersonRecord() 
	{
		
		init();
						
		issuePersonTransaction.sign(maker, false);

		//CHECK IF ISSUE PERSON TRANSACTION IS VALID
		assertEquals(true, issuePersonTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp, maker.getLastReference(db), new byte[64]);
		
		//CHECK IF ISSUE PERSON IS INVALID
		assertEquals(false, issuePersonTransaction.isSignatureValid());
	}
		

	
	@Test
	public void parseIssuePersonRecord() 
	{
		
		init();
		
		LOGGER.info("person: " + person.getType()[0] + ", " + person.getType()[1]);
		byte [] raw = person.toBytes(false);
		assertEquals(raw.length, person.getDataLength(false));
		person.setReference(new byte[64]);
		raw = person.toBytes(true);
		assertEquals(raw.length, person.getDataLength(true));
				
		issuePersonTransaction.sign(maker, false);
		issuePersonTransaction.process(db, false);
		
		//CONVERT TO BYTES
		byte[] rawIssuePersonRecord = issuePersonTransaction.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawIssuePersonRecord.length, issuePersonTransaction.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			IssuePersonRecord parsedIssuePersonRecord = (IssuePersonRecord) TransactionFactory.getInstance().parse(rawIssuePersonRecord, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedIssuePersonRecord instanceof IssuePersonRecord);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(issuePersonTransaction.getSignature(), parsedIssuePersonRecord.getSignature()));
			
			//CHECK ISSUER
			assertEquals(issuePersonTransaction.getCreator().getAddress(), parsedIssuePersonRecord.getCreator().getAddress());
			
			PersonHuman parsedPerson = (PersonHuman)parsedIssuePersonRecord.getItem();

			//CHECK OWNER
			assertEquals(person.getCreator().getAddress(), parsedPerson.getCreator().getAddress());
			
			//CHECK NAME
			assertEquals(person.getName(), parsedPerson.getName());
				
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(issuePersonTransaction.getReference(), parsedIssuePersonRecord.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(issuePersonTransaction.getTimestamp(), parsedIssuePersonRecord.getTimestamp());				

			//CHECK DESCRIPTION
			assertEquals(person.getDescription(), parsedPerson.getDescription());
							
			assertEquals(person.getItemType(), parsedPerson.getType());
			assertEquals(person.getBirthday(), parsedPerson.getBirthday());			
			assertEquals(person.getGender(), parsedPerson.getBirthday());
			assertEquals(person.getRace(), parsedPerson.getRace());
			assertEquals(true, person.getBirthLatitude() == parsedPerson.getBirthLatitude());
			assertEquals(true, person.getBirthLongitude() == parsedPerson.getBirthLongitude());
			assertEquals(person.getSkinColor(), parsedPerson.getSkinColor());
			assertEquals(person.getEyeColor(), parsedPerson.getEyeColor());
			assertEquals(person.getHairСolor(), parsedPerson.getHairСolor());
			assertEquals(person.getHeight(), parsedPerson.getHeight());

		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawIssuePersonRecord = new byte[issuePersonTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawIssuePersonRecord, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	
	@Test
	public void processIssuePersonRecord()
	{
		
		init();				
		
		issuePersonTransaction.sign(maker, false);
		
		assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(db, releaserReference));
		
		issuePersonTransaction.process(db, false);
		
		LOGGER.info("person KEY: " + person.getKey(db));
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.valueOf(1).setScale(8), maker.getConfirmedBalance(person.getKey(db), db));
		
		//CHECK PERSON EXISTS SENDER
		long key = db.getIssuePersonMap().get(issuePersonTransaction);
		assertEquals(true, db.getPersonMap().contains(key));
		
		//CHECK PERSON IS CORRECT
		assertEquals(true, Arrays.equals(db.getPersonMap().get(key).toBytes(true), person.toBytes(true)));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issuePersonTransaction.getSignature(), maker.getLastReference(db)));
	}
	
	
	@Test
	public void orphanIssuePersonRecord()
	{
		
		init();				
				
		issuePersonTransaction.sign(maker, false);
		issuePersonTransaction.process(db, false);
		key = db.getIssuePersonMap().get(issuePersonTransaction);
		
		assertEquals(new BigDecimal(1).setScale(8), maker.getConfirmedBalance(key,db));
		assertEquals(true, Arrays.equals(issuePersonTransaction.getSignature(), maker.getLastReference(db)));
		
		issuePersonTransaction.orphan(db, false);
		
		//CHECK BALANCE ISSUER
		assertEquals(BG_ZERO, maker.getConfirmedBalance(key,db));
		
		//CHECK PERSON EXISTS SENDER
		assertEquals(false, db.getPersonMap().contains(key));
		
		//CHECK PERSON BALANCE SENDER
		assertEquals(0, db.getBalanceMap().get(maker.getAddress(), key).longValue());
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issuePersonTransaction.getReference(), maker.getLastReference(db)));
	}
	

	//TRANSFER PERSON
	
	@Test
	public void validateSignaturePersonalizeRecord() 
	{
		
		init();
		
		initPersonalize();
		
		// SIGN
		personalizeRecord.signUserAccounts(userAccount1, userAccount2, userAccount3);
		personalizeRecord.sign(maker, false);
		
		//CHECK IF PERSONALIZE RECORD SIGNATURE IS VALID
		assertEquals(true, personalizeRecord.isSignatureValid());
		
		//INVALID SIGNATURE
		personalizeRecord.setTimestamp(personalizeRecord.getTimestamp() + 1);
		
		//CHECK IF PERSONALIZE RECORD SIGNATURE IS INVALID
		assertEquals(false, personalizeRecord.isSignatureValid());
	}
	
	@Test
	public void validatePersonalizeRecord() 
	{	
		
		init();
						
		initPersonalize();

		issuePersonTransaction.sign(maker, false);
		assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(db, releaserReference));
		
		issuePersonTransaction.process(db, false);
		key = person.getKey(db);
		
		int duration = 356;
		//CREATE PERSONALIZE REcORD
		PersonalizeRecord personalizeRecord = new PersonalizeRecord(maker, FEE_POWER, key,
				userAccount1, userAccount2, userAccount3, duration,
				timestamp, maker.getLastReference(db));

		personalizeRecord.signUserAccounts(userAccount1, userAccount2, userAccount3);
		personalizeRecord.sign(maker, false);

		//CHECK IF PERSONALIZE RECORD IS VALID
		assertEquals(Transaction.VALIDATE_OK, personalizeRecord.isValid(db, releaserReference));
		
		personalizeRecord.process(db, false);
								
		//CREATE INVALID PERSONALIZE RECORD NOT ENOUGH PERSON BALANCE
		//CREATE PERSONALIZE REcORD
		PersonalizeRecord personalizeRecord_0 = new PersonalizeRecord(userAccount1, FEE_POWER, key,
				userAccount1, userAccount2, userAccount3, duration,
				timestamp, maker.getLastReference(db));

		personalizeRecord_0.sign(maker, false);
		personalizeRecord_0.process(db, false);
		
		//CHECK IF PERSONALIZE RECORD IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, personalizeRecord_0.isValid(db, releaserReference));	

	}
	
	@Test
	public void parsePersonalizeRecord() 
	{

		init();
		
		initPersonalize();

		// SIGN
		personalizeRecord.signUserAccounts(userAccount1, userAccount2, userAccount3);
		personalizeRecord.sign(maker, false);

		//CONVERT TO BYTES
		byte[] rawPersonTransfer = personalizeRecord.toBytes(true, null);
		
		//CHECK DATALENGTH
		assertEquals(rawPersonTransfer.length, personalizeRecord.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			PersonalizeRecord parsedPersonTransfer = (PersonalizeRecord) TransactionFactory.getInstance().parse(rawPersonTransfer, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedPersonTransfer instanceof PersonalizeRecord);
			
			//CHECK TYPEBYTES
			assertEquals(true, Arrays.equals(personalizeRecord.getTypeBytes(), parsedPersonTransfer.getTypeBytes()));				

			//CHECK TIMESTAMP
			assertEquals(personalizeRecord.getTimestamp(), parsedPersonTransfer.getTimestamp());				

			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(personalizeRecord.getReference(), parsedPersonTransfer.getReference()));	

			//CHECK CREATOR
			assertEquals(personalizeRecord.getCreator().getAddress(), parsedPersonTransfer.getCreator().getAddress());				

			//CHECK FEE POWER
			assertEquals(personalizeRecord.getFee(), parsedPersonTransfer.getFee());	

			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(personalizeRecord.getSignature(), parsedPersonTransfer.getSignature()));
			
			//CHECK KEY
			assertEquals(personalizeRecord.getKey(), parsedPersonTransfer.getKey());	
			
			//CHECK AMOUNT
			assertEquals(personalizeRecord.viewAmount(maker), parsedPersonTransfer.viewAmount(maker));	
			
			//CHECK USER SIGNATURES
			assertEquals(true, Arrays.equals(personalizeRecord.getUserSignature1(), parsedPersonTransfer.getUserSignature1()));
			assertEquals(true, Arrays.equals(personalizeRecord.getUserSignature2(), parsedPersonTransfer.getUserSignature2()));
			assertEquals(true, Arrays.equals(personalizeRecord.getUserSignature3(), parsedPersonTransfer.getUserSignature3()));

			
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawPersonTransfer = new byte[personalizeRecord.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawPersonTransfer, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void process_orphan_PersonalizeRecord()
	{

		init();
		
		initPersonalize();
		personalizeRecord.sign(maker, false);
		
		BigDecimal oil_amount_diff = PersonalizeRecord.OIL_AMOUNT;
		BigDecimal vote_amount_diff = PersonalizeRecord.VOTE_AMOUNT;
		
		BigDecimal erm_amount = maker.getConfirmedBalance(db);
		BigDecimal oil_amount = maker.getConfirmedBalance(OIL_KEY, db);
		BigDecimal vote_amount = maker.getConfirmedBalance(VOTE_KEY, db);
		
		BigDecimal erm_amount_user = userAccount1.getConfirmedBalance(db);
		BigDecimal oil_amount_user = userAccount1.getConfirmedBalance(OIL_KEY, db);
		BigDecimal vote_amount_user = userAccount1.getConfirmedBalance(VOTE_KEY, db);
		
		personalizeRecord.process(db, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BG_ZERO, erm_amount);
		assertEquals(oil_amount.subtract(oil_amount_diff), oil_amount);
		assertEquals(vote_amount.subtract(vote_amount_diff), vote_amount);
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BG_ZERO, userAccount1.getConfirmedBalance(db));
		assertEquals(PersonalizeRecord.OIL_AMOUNT, userAccount1.getConfirmedBalance(OIL_KEY, db));
		assertEquals(PersonalizeRecord.VOTE_AMOUNT, userAccount1.getConfirmedBalance(VOTE_KEY, db));
		assertEquals(BG_ZERO, userAccount2.getConfirmedBalance(db));
		assertEquals(BG_ZERO, userAccount2.getConfirmedBalance(OIL_KEY, db));
		assertEquals(BG_ZERO, userAccount2.getConfirmedBalance(VOTE_KEY, db));
		assertEquals(BG_ZERO, userAccount3.getConfirmedBalance(db));
		assertEquals(BG_ZERO, userAccount3.getConfirmedBalance(OIL_KEY, db));
		assertEquals(BG_ZERO, userAccount3.getConfirmedBalance(VOTE_KEY, db));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(personalizeRecord.getSignature(), maker.getLastReference(db)));
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(personalizeRecord.getSignature(), userAccount1.getLastReference(db)));
		assertEquals(false, Arrays.equals(null, userAccount2.getLastReference(db)));
		assertEquals(false, Arrays.equals(null, userAccount3.getLastReference(db)));
		
		// ORPHAN
		personalizeRecord.orphan(db, false);

		//CHECK BALANCE SENDER
		assertEquals(erm_amount, maker.getConfirmedBalance(db));
		assertEquals(oil_amount, maker.getConfirmedBalance(OIL_KEY, db));
		assertEquals(vote_amount, maker.getConfirmedBalance(VOTE_KEY, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(erm_amount_user, userAccount1.getConfirmedBalance(db));
		assertEquals(oil_amount_user, userAccount1.getConfirmedBalance(OIL_KEY, db));
		assertEquals(vote_amount_user, userAccount1.getConfirmedBalance(VOTE_KEY, db));
		assertEquals(BG_ZERO, userAccount2.getConfirmedBalance(db));
		assertEquals(BG_ZERO, userAccount2.getConfirmedBalance(OIL_KEY, db));
		assertEquals(BG_ZERO, userAccount2.getConfirmedBalance(VOTE_KEY, db));
		assertEquals(BG_ZERO, userAccount3.getConfirmedBalance(db));
		assertEquals(BG_ZERO, userAccount3.getConfirmedBalance(OIL_KEY, db));
		assertEquals(BG_ZERO, userAccount3.getConfirmedBalance(VOTE_KEY, db));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(personalizeRecord.getSignature(), maker.getLastReference(db)));
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(personalizeRecord.getSignature(), userAccount1.getLastReference(db)));
		assertEquals(false, Arrays.equals(null, userAccount2.getLastReference(db)));
		assertEquals(false, Arrays.equals(null, userAccount3.getLastReference(db)));
	}
	
}
