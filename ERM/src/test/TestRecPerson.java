package test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import ntp.NTP;

import org.junit.Test;

import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.ItemFactory;
import core.item.persons.PersonCls;
import core.item.persons.PersonHuman;
import core.item.statuses.StatusCls;
import core.transaction.GenesisCertifyPersonRecord;
import core.transaction.IssuePersonRecord;
import core.transaction.R_SertifyPerson;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import core.wallet.Wallet;
import database.AddressPersonMap;

//import com.google.common.primitives.Longs;

import database.DBSet;
import database.PersonAddressMap;
import database.PersonStatusMap;

public class TestRecPerson {

	static Logger LOGGER = Logger.getLogger(TestRecPerson.class.getName());

	byte[] releaserReference = null;

	BigDecimal BG_ZERO = BigDecimal.ZERO.setScale(8);
	long ERM_KEY = Transaction.RIGHTS_KEY;
	long FEE_KEY = Transaction.FEE_KEY;
	long ALIVE_KEY = StatusCls.ALIVE_KEY;
	byte FEE_POWER = (byte)1;
	byte[] personReference = new byte[64];
	long timestamp = NTP.getTime();
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount certifier = new PrivateKeyAccount(privateKey);

	//GENERATE ACCOUNT SEED
	int nonce = 1;
	//byte[] accountSeed;
	//core.wallet.Wallet.generateAccountSeed(byte[], int)
	byte[] accountSeed1 = Wallet.generateAccountSeed(seed, nonce++);
    PrivateKeyAccount userAccount1 = new PrivateKeyAccount(accountSeed1);
    String userAddress1 = userAccount1.getAddress();
    byte[] accountSeed2 = Wallet.generateAccountSeed(seed, nonce++);
    PrivateKeyAccount userAccount2 = new PrivateKeyAccount(accountSeed2);
    String userAddress2 = userAccount2.getAddress();
    byte[] accountSeed3 = Wallet.generateAccountSeed(seed, nonce++);
    PrivateKeyAccount userAccount3 = new PrivateKeyAccount(accountSeed3);
    String userAddress3 = userAccount3.getAddress();

	
	PersonCls person;
	long personKey = -1;
	IssuePersonRecord issuePersonTransaction;
	R_SertifyPerson r_SertifyPerson;

	PersonStatusMap dbPS;
	PersonAddressMap dbPA;
	AddressPersonMap dbAP;

	// INIT PERSONS
	private void init() {
		
		db = DBSet.createEmptyDatabaseSet();
		dbPA = db.getPersonAddressMap();
		dbAP = db.getAddressPersonMap();
		dbPS = db.getPersonStatusMap();

		gb = new GenesisBlock();
		gb.process(db);
		
		// GET RIGHTS TO CERTIFIER
		GenesisCertifyPersonRecord genesis_certify = new GenesisCertifyPersonRecord(certifier, 0L);
		genesis_certify.process(db, false);
		
		certifier.setLastReference(gb.getGeneratorSignature(), db);
		certifier.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(1000).setScale(8), db);
		certifier.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);
		
		byte gender = 1;
		person = new PersonHuman(certifier, "Ermolaev Dmitrii Sergeevich", timestamp - 12345678,
				gender, "Slav", (float)128.12345, (float)33.7777,
				"white", "green", "шанет", 188, "изобретатель, мыслитель, создатель идей");

		//CREATE ISSUE PERSON TRANSACTION
		issuePersonTransaction = new IssuePersonRecord(certifier, person, FEE_POWER, timestamp, certifier.getLastReference(db));

	}
	
	public void initPersonalize() {


		assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(db, releaserReference));

		issuePersonTransaction.sign(certifier, false);
		
		issuePersonTransaction.process(db, false);
		personKey = person.getKey();
		
		//CREATE PERSONALIZE REcORD
		r_SertifyPerson = new R_SertifyPerson(certifier, FEE_POWER, personKey,
				userAccount1, userAccount2, userAccount3,
				timestamp, certifier.getLastReference(db));

	}
	
	//ISSUE PERSON TRANSACTION
	
	@Test
	public void validateSignatureIssuePersonRecord() 
	{
		
		init();
						
		issuePersonTransaction.sign(certifier, false);

		//CHECK IF ISSUE PERSON TRANSACTION IS VALID
		assertEquals(true, issuePersonTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		issuePersonTransaction = new IssuePersonRecord(certifier, person, FEE_POWER, timestamp, certifier.getLastReference(db), new byte[64]);		
		//CHECK IF ISSUE PERSON IS INVALID
		assertEquals(false, issuePersonTransaction.isSignatureValid());
		assertEquals(false, issuePersonTransaction.isSignatureValid());


	}
		
	
	@Test
	public void validateIssuePersonRecord() 
	{

		init();
		
		issuePersonTransaction.sign(certifier, false);

		//CHECK IF ISSUE PERSON IS VALID
		assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(db, releaserReference));

		//CREATE INVALID ISSUE PERSON - INVALID PERSONALIZE
		issuePersonTransaction = new IssuePersonRecord(userAccount1, person, FEE_POWER, timestamp, userAccount1.getLastReference(db), new byte[64]);		
		assertEquals(Transaction.NOT_ENOUGH_FEE, issuePersonTransaction.isValid(db, releaserReference));
		// ADD FEE
		userAccount1.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);
		assertEquals(Transaction.ACCOUNT_NOT_PERSONALIZED, issuePersonTransaction.isValid(db, releaserReference));

	}

	
	@Test
	public void parseIssuePersonRecord() 
	{
		
		init();
		
		LOGGER.info("person: " + person.getType()[0] + ", " + person.getType()[1]);

		// PARSE PERSON
		
		byte [] rawPerson = person.toBytes(false);
		assertEquals(rawPerson.length, person.getDataLength(false));
		person.setReference(new byte[64]);
		rawPerson = person.toBytes(true);
		assertEquals(rawPerson.length, person.getDataLength(true));
		
		rawPerson = person.toBytes(false);
		PersonCls parsedPerson = null;
		try 
		{	
			//PARSE FROM BYTES
			parsedPerson = (PersonCls) ItemFactory.getInstance()
					.parse(ItemCls.PERSON_TYPE, rawPerson, false);
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.  : " + e);
		}
		assertEquals(rawPerson.length, person.getDataLength(false));
		assertEquals(parsedPerson.getHeight(), person.getHeight());
		assertEquals(person.getCreator().getAddress(), parsedPerson.getCreator().getAddress());
		assertEquals(person.getName(), parsedPerson.getName());
		assertEquals(person.getDescription(), parsedPerson.getDescription());
		assertEquals(person.getItemType(), parsedPerson.getItemType());
		assertEquals(person.getBirthday(), parsedPerson.getBirthday());			
		assertEquals(person.getGender(), parsedPerson.getGender());
		assertEquals(person.getRace(), parsedPerson.getRace());
		assertEquals(true, person.getBirthLatitude() == parsedPerson.getBirthLatitude());
		assertEquals(true, person.getBirthLongitude() == parsedPerson.getBirthLongitude());
		assertEquals(person.getSkinColor(), parsedPerson.getSkinColor());
		assertEquals(person.getEyeColor(), parsedPerson.getEyeColor());
		assertEquals(person.getHairСolor(), parsedPerson.getHairСolor());
		assertEquals(person.getHeight(), parsedPerson.getHeight());

		
		// PARSE ISSEU PERSON RECORD
		issuePersonTransaction.sign(certifier, false);
		issuePersonTransaction.process(db, false);
		
		//CONVERT TO BYTES
		byte[] rawIssuePersonRecord = issuePersonTransaction.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawIssuePersonRecord.length, issuePersonTransaction.getDataLength(false));
		
		IssuePersonRecord parsedIssuePersonRecord = null;
		try 
		{	
			//PARSE FROM BYTES
			parsedIssuePersonRecord = (IssuePersonRecord) TransactionFactory.getInstance().parse(rawIssuePersonRecord, releaserReference);			

		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.  : " + e);
		}
		
		//CHECK INSTANCE
		assertEquals(true, parsedIssuePersonRecord instanceof IssuePersonRecord);
		
		//CHECK SIGNATURE
		assertEquals(true, Arrays.equals(issuePersonTransaction.getSignature(), parsedIssuePersonRecord.getSignature()));
		
		//CHECK ISSUER
		assertEquals(issuePersonTransaction.getCreator().getAddress(), parsedIssuePersonRecord.getCreator().getAddress());
		
		parsedPerson = (PersonHuman)parsedIssuePersonRecord.getItem();

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
						
		assertEquals(person.getItemType(), parsedPerson.getItemType());
		assertEquals(person.getBirthday(), parsedPerson.getBirthday());			
		assertEquals(person.getGender(), parsedPerson.getGender());
		assertEquals(person.getRace(), parsedPerson.getRace());
		assertEquals(true, person.getBirthLatitude() == parsedPerson.getBirthLatitude());
		assertEquals(true, person.getBirthLongitude() == parsedPerson.getBirthLongitude());
		assertEquals(person.getSkinColor(), parsedPerson.getSkinColor());
		assertEquals(person.getEyeColor(), parsedPerson.getEyeColor());
		assertEquals(person.getHairСolor(), parsedPerson.getHairСolor());
		assertEquals(person.getHeight(), parsedPerson.getHeight());

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
		
		assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(db, releaserReference));

		issuePersonTransaction.sign(certifier, false);
		
		issuePersonTransaction.process(db, false);
		
		LOGGER.info("person KEY: " + person.getKey());
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), certifier.getConfirmedBalance(ERM_KEY, db));
		assertEquals(BigDecimal.valueOf(1).subtract(issuePersonTransaction.getFee()).setScale(8), certifier.getConfirmedBalance(FEE_KEY, db));
		
		//CHECK PERSON EXISTS DB AS CONFIRMED:  key > -1
		long key = db.getIssuePersonMap().get(issuePersonTransaction);
		assertEquals(true, key >= 0);
		assertEquals(true, db.getPersonMap().contains(key));
		
		//CHECK PERSON IS CORRECT
		assertEquals(true, Arrays.equals(db.getPersonMap().get(key).toBytes(true), person.toBytes(true)));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issuePersonTransaction.getSignature(), certifier.getLastReference(db)));

		//////// ORPHAN /////////
		issuePersonTransaction.orphan(db, false);
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), certifier.getConfirmedBalance(ERM_KEY, db));
		assertEquals(BigDecimal.valueOf(1).setScale(8), certifier.getConfirmedBalance(FEE_KEY, db));
		
		//CHECK PERSON EXISTS ISSUER
		assertEquals(false, db.getPersonMap().contains(personKey));
						
		//CHECK REFERENCE ISSUER
		assertEquals(true, Arrays.equals(issuePersonTransaction.getReference(), certifier.getLastReference(db)));
	}
	

	///////////////////////////////////////
	// PERSONONALIZE RECORD
	///////////////////////////////////////
	
	@Test
	public void validatePersonalizeRecord() 
	{	
		
		init();
						
		initPersonalize();

		assertEquals(Transaction.VALIDATE_OK, r_SertifyPerson.isValid(db, releaserReference));
		
		//r_SertifyPerson.sign(maker, false);
		//r_SertifyPerson.process(db, false);
								
		//CREATE INVALID PERSONALIZE RECORD NOT ENOUGH ERM BALANCE
		R_SertifyPerson personalizeRecord_0 = new R_SertifyPerson(0, userAccount1, FEE_POWER, personKey,
				userAccount1, userAccount2, userAccount3,
				356, timestamp, userAccount1.getLastReference(db));
		assertEquals(Transaction.NOT_ENOUGH_RIGHTS, personalizeRecord_0.isValid(db, releaserReference));	

		//CREATE INVALID PERSONALIZE RECORD KEY NOT EXIST
		personalizeRecord_0 = new R_SertifyPerson(0, certifier, FEE_POWER, personKey + 10,
				userAccount1, userAccount2, userAccount3,
				356, timestamp, certifier.getLastReference(db));
		assertEquals(Transaction.ITEM_PERSON_NOT_EXIST, personalizeRecord_0.isValid(db, releaserReference));	

		//CREATE INVALID ISSUE PERSON FOR INVALID PERSONALIZE
		personalizeRecord_0 = new R_SertifyPerson(0, userAccount1, FEE_POWER, personKey,
				userAccount1, userAccount2, userAccount3,
				356, timestamp, userAccount1.getLastReference(db));
		//CREATE INVALID ISSUE PERSON - NOT FEE
		personalizeRecord_0.calcFee();
		assertEquals(Transaction.NOT_ENOUGH_FEE, personalizeRecord_0.isValid(db, releaserReference));
		// ADD FEE
		userAccount1.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);
		assertEquals(Transaction.NOT_ENOUGH_RIGHTS, personalizeRecord_0.isValid(db, releaserReference));
		// ADD RIGHTS
		userAccount1.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(10000).setScale(8), db);
		assertEquals(Transaction.ACCOUNT_NOT_PERSONALIZED, personalizeRecord_0.isValid(db, releaserReference));

		personalizeRecord_0 = new R_SertifyPerson(0, certifier, FEE_POWER, personKey,
				null, userAccount2, userAccount3,
				356, timestamp, certifier.getLastReference(db));
		assertEquals(Transaction.INVALID_ADDRESS, personalizeRecord_0.isValid(db, releaserReference));

		personalizeRecord_0 = new R_SertifyPerson(0, certifier, FEE_POWER, personKey,
				userAccount1, null, userAccount3,
				356, timestamp, certifier.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, personalizeRecord_0.isValid(db, releaserReference));

		personalizeRecord_0 = new R_SertifyPerson(0, certifier, FEE_POWER, personKey,
				userAccount1, userAccount2, null,
				356, timestamp, certifier.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, personalizeRecord_0.isValid(db, releaserReference));

	}
	
	@Test
	public void validateSignaturePersonalizeRecord() 
	{
		
		init();
		
		initPersonalize();
		
		// SIGN
		r_SertifyPerson.signUserAccounts(userAccount1, userAccount2, userAccount3);
		r_SertifyPerson.sign(certifier, false);
		
		//CHECK IF PERSONALIZE RECORD SIGNATURE IS VALID
		assertEquals(true, r_SertifyPerson.isSignatureValid());
		
		//INVALID SIGNATURE
		r_SertifyPerson.setTimestamp(r_SertifyPerson.getTimestamp() + 1);
		
		//CHECK IF PERSONALIZE RECORD SIGNATURE IS INVALID
		assertEquals(false, r_SertifyPerson.isSignatureValid());

		// BACK TO VALID
		r_SertifyPerson.setTimestamp(r_SertifyPerson.getTimestamp() - 1);
		assertEquals(true, r_SertifyPerson.isSignatureValid());

		r_SertifyPerson.sign(null, false);
		//CHECK IF PERSONALIZE RECORD SIGNATURE IS INVALID
		assertEquals(false, r_SertifyPerson.isSignatureValid());

		// BACK TO VALID
		r_SertifyPerson.sign(certifier, false);
		assertEquals(true, r_SertifyPerson.isSignatureValid());
		r_SertifyPerson.signUserAccounts(userAccount1, userAccount2, userAccount2);

		//CHECK IF PERSONALIZE RECORD SIGNATURE IS INVALID
		assertEquals(false, r_SertifyPerson.isSignatureValid());

		// CHECK NULL in USER ADDRESS
		R_SertifyPerson personalizeRecord_0 = new R_SertifyPerson(0, certifier, FEE_POWER, personKey,
				userAccount1, null, userAccount3,
				356, timestamp, certifier.getLastReference(db));
		personalizeRecord_0.signUserAccounts(null, null, null);
		personalizeRecord_0.sign(certifier, false);
		assertEquals(false, personalizeRecord_0.isSignatureValid());
		personalizeRecord_0.signUserAccounts(userAccount1, null, null);
		assertEquals(false, personalizeRecord_0.isSignatureValid());
		personalizeRecord_0.signUserAccounts(userAccount1, null, userAccount3);
		assertEquals(true, personalizeRecord_0.isSignatureValid());

	}
	
	@Test
	public void parsePersonalizeRecord() 
	{

		init();
		
		initPersonalize();

		// SIGN
		r_SertifyPerson.signUserAccounts(userAccount1, userAccount2, userAccount3);
		r_SertifyPerson.sign(certifier, false);

		//CONVERT TO BYTES
		byte[] rawPersonTransfer = r_SertifyPerson.toBytes(true, releaserReference);
		
		//CHECK DATALENGTH
		assertEquals(rawPersonTransfer.length, r_SertifyPerson.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			R_SertifyPerson parsedPersonTransfer = (R_SertifyPerson) TransactionFactory.getInstance().parse(rawPersonTransfer, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedPersonTransfer instanceof R_SertifyPerson);
			
			//CHECK TYPEBYTES
			assertEquals(true, Arrays.equals(r_SertifyPerson.getTypeBytes(), parsedPersonTransfer.getTypeBytes()));				

			//CHECK TIMESTAMP
			assertEquals(r_SertifyPerson.getTimestamp(), parsedPersonTransfer.getTimestamp());				

			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(r_SertifyPerson.getReference(), parsedPersonTransfer.getReference()));	

			//CHECK CREATOR
			assertEquals(r_SertifyPerson.getCreator().getAddress(), parsedPersonTransfer.getCreator().getAddress());				

			//CHECK FEE POWER
			assertEquals(r_SertifyPerson.getFee(), parsedPersonTransfer.getFee());	

			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(r_SertifyPerson.getSignature(), parsedPersonTransfer.getSignature()));
			
			//CHECK KEY
			assertEquals(r_SertifyPerson.getKey(), parsedPersonTransfer.getKey());	
			
			//CHECK AMOUNT
			assertEquals(r_SertifyPerson.getAmount(certifier), parsedPersonTransfer.getAmount(certifier));
						
			//CHECK USER SIGNATURES
			assertEquals(true, Arrays.equals(r_SertifyPerson.getUserSignature1(), parsedPersonTransfer.getUserSignature1()));
			assertEquals(true, Arrays.equals(r_SertifyPerson.getUserSignature2(), parsedPersonTransfer.getUserSignature2()));
			assertEquals(true, Arrays.equals(r_SertifyPerson.getUserSignature3(), parsedPersonTransfer.getUserSignature3()));
			
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction." + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawPersonTransfer = new byte[r_SertifyPerson.getDataLength(false)];
		
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

		assertEquals(false, userAccount1.isPerson(db));
		assertEquals(false, userAccount2.isPerson(db));
		assertEquals(false, userAccount3.isPerson(db));
		
		initPersonalize();
		
		// .a - personKey, .b - end_date, .c - block height, .d - reference
		// PERSON STATUS ALIVE
		// exist assertEquals( null, dbPS.getItem(personKey));
		// exist assertEquals( new TreeMap<String, Stack<Tuple3<Integer, Integer, byte[]>>>(), dbPA.getItems(personKey));

		// ADDRESSES
		assertEquals( null, dbAP.getItem(userAddress1));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA.getItem(personKey, userAddress1));

		assertEquals( null, dbAP.getItem(userAddress2));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA.getItem(personKey, userAddress2));

		assertEquals( null, dbAP.getItem(userAddress3));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA.getItem(personKey, userAddress3));

		int to_day = (int)(NTP.getTime() / 86400);
		int to_date = R_SertifyPerson.DEFAULT_DURATION + to_day;
		assertEquals( to_date, r_SertifyPerson.getDuration());
		
		BigDecimal oil_amount_diff = R_SertifyPerson.GIFTED_FEE_AMOUNT;
		
		BigDecimal erm_amount = certifier.getConfirmedBalance(ERM_KEY,db);
		BigDecimal oil_amount = certifier.getConfirmedBalance(FEE_KEY, db);
		
		BigDecimal erm_amount_user = userAccount1.getConfirmedBalance(ERM_KEY, db);
		BigDecimal oil_amount_user = userAccount1.getConfirmedBalance(FEE_KEY, db);
		
		r_SertifyPerson.signUserAccounts(userAccount1, userAccount2, userAccount3);
		r_SertifyPerson.sign(certifier, false);
		r_SertifyPerson.process(db, false);
		
		//CHECK BALANCE SENDER
		assertEquals(erm_amount, certifier.getConfirmedBalance(ERM_KEY, db));
		// CHECK OIL BALANCE - FEE - GIFT
		assertEquals(oil_amount.subtract(oil_amount_diff).subtract(r_SertifyPerson.getFee()),
				certifier.getConfirmedBalance(FEE_KEY, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BG_ZERO, userAccount1.getConfirmedBalance(ERM_KEY, db));
		assertEquals(R_SertifyPerson.GIFTED_FEE_AMOUNT, userAccount1.getConfirmedBalance(FEE_KEY, db));
		assertEquals(BG_ZERO, userAccount2.getConfirmedBalance(ERM_KEY, db));
		assertEquals(BG_ZERO, userAccount2.getConfirmedBalance(FEE_KEY, db));
		assertEquals(BG_ZERO, userAccount3.getConfirmedBalance(ERM_KEY, db));
		assertEquals(BG_ZERO, userAccount3.getConfirmedBalance(FEE_KEY, db));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(r_SertifyPerson.getSignature(), certifier.getLastReference(db)));
		
		//CHECK REFERENCE RECIPIENT
		// TRUE - new reference for first send OIL
		assertEquals(true, Arrays.equals(r_SertifyPerson.getSignature(), userAccount1.getLastReference(db)));
		// byte[0]
		assertEquals(true, Arrays.equals(new byte[0], userAccount2.getLastReference(db)));
		assertEquals(true, Arrays.equals(new byte[0], userAccount3.getLastReference(db)));
		
		// .a - personKey, .b - end_date, .c - block height, .d - reference
		// PERSON STATUS ALIVE
		assertEquals( to_date, (int)dbPS.getItem(personKey).a);
		assertEquals( -1, (int)dbPS.getItem(personKey).b);
		assertEquals( true, Arrays.equals(dbPS.getItem(personKey).c, r_SertifyPerson.getSignature()));
		// ADDRESSES
		assertEquals( (long)personKey, (long)dbAP.getItem(userAddress1).a);
		assertEquals( to_date, (int)dbAP.getItem(userAddress1).b);
		assertEquals( -1, (int)dbAP.getItem(userAddress1).c);
		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress1).d, r_SertifyPerson.getSignature()));
		// PERSON -> ADDRESS
		assertEquals( to_date, (int)dbPA.getItem(personKey, userAddress1).a);
		assertEquals( -1, (int)dbPA.getItem(personKey, userAddress1).b);
		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress1).c, r_SertifyPerson.getSignature()));

		assertEquals( (long)personKey, (long)dbAP.getItem(userAddress2).a);
		assertEquals( to_date, (int)dbAP.getItem(userAddress2).b);
		assertEquals( -1, (int)dbAP.getItem(userAddress2).c);
		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress2).d, r_SertifyPerson.getSignature()));
		// PERSON -> ADDRESS
		assertEquals( to_date, (int)dbPA.getItem(personKey, userAddress2).a);
		assertEquals( -1, (int)dbPA.getItem(personKey, userAddress2).b);
		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress2).c, r_SertifyPerson.getSignature()));

		assertEquals( (long)personKey, (long)dbAP.getItem(userAddress3).a);
		assertEquals( to_date, (int)dbAP.getItem(userAddress3).b);
		assertEquals( -1, (int)dbAP.getItem(userAddress3).c);
		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress3).d, r_SertifyPerson.getSignature()));
		// PERSON -> ADDRESS
		assertEquals( to_date, (int)dbPA.getItem(personKey, userAddress3).a);
		assertEquals( -1, (int)dbPA.getItem(personKey, userAddress3).b);
		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress3).c, r_SertifyPerson.getSignature()));

		assertEquals(true, userAccount1.isPerson(db));
		assertEquals(true, userAccount2.isPerson(db));
		assertEquals(true, userAccount3.isPerson(db));
		
		////////// ORPHAN //////////////////
		r_SertifyPerson.orphan(db, false);

		//CHECK BALANCE SENDER
		assertEquals(erm_amount, certifier.getConfirmedBalance(ERM_KEY, db));
		assertEquals(oil_amount, certifier.getConfirmedBalance(FEE_KEY, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(erm_amount_user, userAccount1.getConfirmedBalance(ERM_KEY, db));
		assertEquals(oil_amount_user, userAccount1.getConfirmedBalance(FEE_KEY, db));
		assertEquals(BG_ZERO, userAccount2.getConfirmedBalance(ERM_KEY, db));
		assertEquals(BG_ZERO, userAccount2.getConfirmedBalance(FEE_KEY, db));
		assertEquals(BG_ZERO, userAccount3.getConfirmedBalance(ERM_KEY, db));
		assertEquals(BG_ZERO, userAccount3.getConfirmedBalance(FEE_KEY, db));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(r_SertifyPerson.getReference(), certifier.getLastReference(db)));
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(new byte[0], userAccount1.getLastReference(db)));
		assertEquals(true, Arrays.equals(new byte[0], userAccount2.getLastReference(db)));
		assertEquals(true, Arrays.equals(new byte[0], userAccount3.getLastReference(db)));
		
		// .a - personKey, .b - end_date, .c - block height, .d - reference
		// PERSON STATUS ALIVE
		assertEquals( null, dbPS.getItem(personKey));

		// ADDRESSES
		assertEquals( null, dbAP.getItem(userAddress1));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA.getItem(personKey, userAddress1));

		assertEquals( null, dbAP.getItem(userAddress2));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA.getItem(personKey, userAddress2));

		assertEquals( null, dbAP.getItem(userAddress3));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA.getItem(personKey, userAddress3));

		assertEquals(false, userAccount1.isPerson(db));
		assertEquals(false, userAccount2.isPerson(db));
		assertEquals(false, userAccount3.isPerson(db));

		/////////////////////////////////////////////// TEST DURATIONS
		// TRY DURATIONS
		int end_date = to_day + 22;
		r_SertifyPerson = new R_SertifyPerson(0, certifier, FEE_POWER, personKey,
				userAccount1, userAccount2, userAccount3,
				end_date, timestamp, certifier.getLastReference(db));
		r_SertifyPerson.signUserAccounts(userAccount1, userAccount2, userAccount3);
		r_SertifyPerson.sign(certifier, false);
		r_SertifyPerson.process(db, false);

		assertEquals(end_date, (int)userAccount1.getPersonDuration(db).b);
		assertEquals(true, userAccount2.isPerson(db));

		// TEST LIST and STACK
		int end_date2 = to_day - 12;
		r_SertifyPerson = new R_SertifyPerson(0, certifier, FEE_POWER, personKey,
				userAccount1, userAccount2, userAccount3,
				end_date2, timestamp, certifier.getLastReference(db));
		r_SertifyPerson.signUserAccounts(userAccount1, userAccount2, userAccount3);
		r_SertifyPerson.sign(certifier, false);
		r_SertifyPerson.process(db, false);

		assertEquals(end_date2, (int)userAccount2.getPersonDuration(db).b);
		assertEquals(false, userAccount2.isPerson(db));

		r_SertifyPerson.orphan(db, false);

		assertEquals(end_date, (int)userAccount2.getPersonDuration(db).b);
		assertEquals(true, userAccount2.isPerson(db));
	}
	
}
