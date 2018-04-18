package test.records;

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
import org.mapdb.Fun.Tuple4;

import ntp.NTP;

import org.junit.Test;

import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.ItemFactory;
import core.item.persons.PersonCls;
import core.item.persons.PersonHuman;
import core.item.statuses.StatusCls;
import core.transaction.GenesisIssuePersonRecord;
import core.transaction.GenesisCertifyPersonRecord;
import core.transaction.IssuePersonRecord;
import core.transaction.R_SertifyPubKeys;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import core.wallet.Wallet;
import datachain.AddressPersonMap;
import datachain.DCSet;
import datachain.KKPersonStatusMap;
import datachain.PersonAddressMap;

public class TestRecPerson {

	static Logger LOGGER = Logger.getLogger(TestRecPerson.class.getName());

	Long releaserReference = null;

	BigDecimal BG_ZERO = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
	long ERM_KEY = Transaction.RIGHTS_KEY;
	long FEE_KEY = Transaction.FEE_KEY;
	//long ALIVE_KEY = StatusCls.ALIVE_KEY;
	byte FEE_POWER = (byte)1;
	byte[] personReference = new byte[64];
	long timestamp = NTP.getTime();
	
	private byte[] icon = new byte[]{1,3,4,5,6,9}; // default value
	private byte[] image = new byte[2000]; // default value
	private byte[] ownerSignature = new byte[Crypto.SIGNATURE_LENGTH];

	//CREATE EMPTY MEMORY DATABASE
	private DCSet db;
	private GenesisBlock gb;
	Long last_ref;
	
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

    List<PrivateKeyAccount> sertifiedPrivateKeys = new ArrayList<PrivateKeyAccount>();
    List<PublicKeyAccount> sertifiedPublicKeys = new ArrayList<PublicKeyAccount>();
    	
	PersonCls personGeneral;
	PersonCls person;
	long genesisPersonKey = -1;
	long personKey = -1;
	IssuePersonRecord issuePersonTransaction;
	R_SertifyPubKeys r_SertifyPubKeys;

	KKPersonStatusMap dbPS;
	PersonAddressMap dbPA;
	AddressPersonMap dbAP;

	//int version = 0; // without signs of person
	int version = 1; // with signs of person
	
	// INIT PERSONS
	private void init() {
		
		db = DCSet.createEmptyDatabaseSet();
		dbPA = db.getPersonAddressMap();
		dbAP = db.getAddressPersonMap();
		dbPS = db.getPersonStatusMap();

		gb = new GenesisBlock();
		try {
			gb.process(db);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		last_ref = gb.getTimestamp(db);
		
		// GET RIGHTS TO CERTIFIER
		byte gender = 1;
		long birthDay = timestamp - 12345678;
		personGeneral = new PersonHuman(certifier, "Ermolaev Dmitrii Sergeevich as sertifier", birthDay, birthDay - 1,
				gender, "Slav", (float)28.12345, (float)133.7777,
				"white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей", ownerSignature);
		//personGeneral.setKey(genesisPersonKey);
				
		GenesisIssuePersonRecord genesis_issue_person = new GenesisIssuePersonRecord(personGeneral);
		genesis_issue_person.process(gb, false);
		//genesisPersonKey = db.getIssuePersonMap().size();
		genesisPersonKey = genesis_issue_person.getAssetKey(db); 

		GenesisCertifyPersonRecord genesis_certify = new GenesisCertifyPersonRecord(certifier, genesisPersonKey);
		genesis_certify.process(gb, false);
		
		certifier.setLastTimestamp(last_ref, db);
		certifier.changeBalance(db, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		certifier.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		
		person = new PersonHuman(certifier, "Ermolaev Dmitrii Sergeevich", birthDay, birthDay - 2,
				gender, "Slav", (float)28.12345, (float)133.7777,
				"white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей", ownerSignature);

		//person.setKey(genesisPersonKey + 1);
		//CREATE ISSUE PERSON TRANSACTION
		issuePersonTransaction = new IssuePersonRecord(certifier, person, FEE_POWER, timestamp, certifier.getLastTimestamp(db));

		sertifiedPrivateKeys.add(userAccount1);
		sertifiedPrivateKeys.add(userAccount2);
		sertifiedPrivateKeys.add(userAccount3);
		
	    sertifiedPublicKeys.add( new PublicKeyAccount(userAccount1.getPublicKey()));
	    sertifiedPublicKeys.add( new PublicKeyAccount(userAccount2.getPublicKey()));
	    sertifiedPublicKeys.add( new PublicKeyAccount(userAccount3.getPublicKey()));

	}
	
	public void initPersonalize() {


		//assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(db, releaserReference));
		assertEquals(Transaction.INVALID_IMAGE_LENGTH, issuePersonTransaction.isValid(releaserReference));

		issuePersonTransaction.sign(certifier, false);
		
		issuePersonTransaction.process(gb, false);
		personKey = person.getKey(db);

		// issue 1 genesis person in init() here
		//assertEquals( genesisPersonKey + 1, personKey);
		//assertEquals( null, dbPS.getItem(personKey, ALIVE_KEY));
		
		//CREATE PERSONALIZE REcORD
		timestamp += 100;
		r_SertifyPubKeys = new R_SertifyPubKeys(version, certifier, FEE_POWER, personKey,
				sertifiedPublicKeys,
				timestamp, certifier.getLastTimestamp(db));

	}
	
	//ISSUE PERSON TRANSACTION
	
	@Test
	public void validateSignatureIssuePersonRecord() 
	{
		
		init();
						
		issuePersonTransaction.sign(certifier, false);

		//CHECK IF ISSUE PERSON TRANSACTION IS VALID
		assertEquals(true, issuePersonTransaction.isSignatureValid(db));
		
		//INVALID SIGNATURE
		issuePersonTransaction = new IssuePersonRecord(certifier, person, FEE_POWER, timestamp, certifier.getLastTimestamp(db), new byte[64]);		
		//CHECK IF ISSUE PERSON IS INVALID
		assertEquals(false, issuePersonTransaction.isSignatureValid(db));

	}
		
	
	@Test
	public void validateIssuePersonRecord() 
	{

		init();
		
		//issuePersonTransaction.sign(certifier, false);

		//CHECK IF ISSUE PERSON IS VALID
		assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(releaserReference));

		//CREATE INVALID ISSUE PERSON - INVALID PERSONALIZE
		issuePersonTransaction = new IssuePersonRecord(userAccount1, person, FEE_POWER, timestamp, userAccount1.getLastTimestamp(db), new byte[64]);		
		assertEquals(Transaction.NOT_ENOUGH_FEE, issuePersonTransaction.isValid(releaserReference));
		// ADD FEE
		userAccount1.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		assertEquals(Transaction.CREATOR_NOT_PERSONALIZED, issuePersonTransaction.isValid(releaserReference));

	}

	
	@Test
	public void parseIssuePersonRecord() 
	{
		
		init();
		
		LOGGER.info("person: " + person.getType()[0] + ", " + person.getType()[1]);

		// PARSE PERSON
		
		byte [] rawPerson = person.toBytes(false, false);
		assertEquals(rawPerson.length, person.getDataLength(false));
		person.setReference(new byte[64]);
		rawPerson = person.toBytes(true, false);
		assertEquals(rawPerson.length, person.getDataLength(true));
		
		rawPerson = person.toBytes(false, false);
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
		assertEquals(person.getOwner().getAddress(), parsedPerson.getOwner().getAddress());
		assertEquals(person.getName(), parsedPerson.getName());
		assertEquals(person.getDescription(), parsedPerson.getDescription());
		assertEquals(person.getItemTypeStr(), parsedPerson.getItemTypeStr());
		assertEquals(person.getBirthday(), parsedPerson.getBirthday());	
		assertEquals(person.getDeathday(), parsedPerson.getDeathday());	
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
		//issuePersonTransaction.process(db, false);
		
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
		assertEquals(person.getOwner().getAddress(), parsedPerson.getOwner().getAddress());
		
		//CHECK NAME
		assertEquals(person.getName(), parsedPerson.getName());
			
		//CHECK REFERENCE
		//assertEquals(issuePersonTransaction.getReference(), parsedIssuePersonRecord.getReference());	
		
		//CHECK TIMESTAMP
		assertEquals(issuePersonTransaction.getTimestamp(), parsedIssuePersonRecord.getTimestamp());				

		//CHECK DESCRIPTION
		assertEquals(person.getDescription(), parsedPerson.getDescription());
						
		assertEquals(person.getItemTypeStr(), parsedPerson.getItemTypeStr());
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
		
		//assertEquals(Transaction.VALIDATE_OK, issuePersonTransaction.isValid(db, releaserReference));
		assertEquals(Transaction.INVALID_IMAGE_LENGTH, issuePersonTransaction.isValid(releaserReference));

		issuePersonTransaction.sign(certifier, false);
		
		issuePersonTransaction.process(gb, false);
		
		LOGGER.info("person KEY: " + person.getKey(db));
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), certifier.getBalanceUSE(ERM_KEY, db));
		assertEquals(BigDecimal.valueOf(1).subtract(issuePersonTransaction.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), certifier.getBalanceUSE(FEE_KEY, db));
		
		//CHECK PERSON EXISTS DB AS CONFIRMED:  key > -1
		long key = db.getIssuePersonMap().get(issuePersonTransaction);
		assertEquals(true, key >= 0);
		assertEquals(true, db.getItemPersonMap().contains(key));
		
		//CHECK PERSON IS CORRECT
		assertEquals(true, Arrays.equals(db.getItemPersonMap().get(key).toBytes(true, false), person.toBytes(true, false)));
						
		//CHECK REFERENCE SENDER
		assertEquals(issuePersonTransaction.getTimestamp(), certifier.getLastTimestamp(db));

		//////// ORPHAN /////////
		issuePersonTransaction.orphan(false);
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), certifier.getBalanceUSE(ERM_KEY, db));
		assertEquals(BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), certifier.getBalanceUSE(FEE_KEY, db));
		
		//CHECK PERSON EXISTS ISSUER
		assertEquals(false, db.getItemPersonMap().contains(personKey));
						
		//CHECK REFERENCE ISSUER
		//assertEquals(issuePersonTransaction.getReference(), certifier.getLastReference(db));
	}
	

	///////////////////////////////////////
	// PERSONONALIZE RECORD
	///////////////////////////////////////
	
	@Test
	public void validatePersonalizeRecord() 
	{	
		
		init();
						
		initPersonalize();

		assertEquals(Transaction.VALIDATE_OK, r_SertifyPubKeys.isValid(releaserReference));
		
		//r_SertifyPerson.sign(maker, false);
		//r_SertifyPerson.process(db, false);
								
		//CREATE INVALID PERSONALIZE RECORD NOT ENOUGH ERM BALANCE
		R_SertifyPubKeys personalizeRecord_0 = new R_SertifyPubKeys(0, userAccount1, FEE_POWER, personKey,
				sertifiedPublicKeys,
				356, timestamp, userAccount1.getLastTimestamp(db));
		assertEquals(Transaction.CREATOR_NOT_PERSONALIZED, personalizeRecord_0.isValid(releaserReference));	

		//CREATE INVALID PERSONALIZE RECORD KEY NOT EXIST
		personalizeRecord_0 = new R_SertifyPubKeys(0, certifier, FEE_POWER, personKey + 10,
				sertifiedPublicKeys,
				356, timestamp, certifier.getLastTimestamp(db));
		assertEquals(Transaction.ITEM_PERSON_NOT_EXIST, personalizeRecord_0.isValid(releaserReference));	

		//CREATE INVALID ISSUE PERSON FOR INVALID PERSONALIZE
		personalizeRecord_0 = new R_SertifyPubKeys(0, userAccount1, FEE_POWER, personKey,
				sertifiedPublicKeys,
				356, timestamp, userAccount1.getLastTimestamp(db));
		//CREATE INVALID ISSUE PERSON - NOT FEE
		personalizeRecord_0.setDC(db, false);
		assertEquals(Transaction.NOT_ENOUGH_FEE, personalizeRecord_0.isValid(releaserReference));
		// ADD FEE
		userAccount1.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		//assertEquals(Transaction.NOT_ENOUGH_RIGHTS, personalizeRecord_0.isValid(db, releaserReference));
		assertEquals(Transaction.CREATOR_NOT_PERSONALIZED, personalizeRecord_0.isValid(releaserReference));
		// ADD RIGHTS
		userAccount1.changeBalance(db, false, ERM_KEY, BigDecimal.valueOf(10000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		assertEquals(Transaction.CREATOR_NOT_PERSONALIZED, personalizeRecord_0.isValid(releaserReference));

	    List<PublicKeyAccount> sertifiedPublicKeys011 = new ArrayList<PublicKeyAccount>();
	    sertifiedPublicKeys011.add( new PublicKeyAccount(new byte[60]));
	    sertifiedPublicKeys011.add( new PublicKeyAccount(userAccount2.getPublicKey()));
	    sertifiedPublicKeys011.add( new PublicKeyAccount(userAccount3.getPublicKey()));
		personalizeRecord_0 = new R_SertifyPubKeys(0, certifier, FEE_POWER, personKey,
				sertifiedPublicKeys011,
				356, timestamp, certifier.getLastTimestamp(db));
		assertEquals(Transaction.INVALID_PUBLIC_KEY, personalizeRecord_0.isValid(releaserReference));

	}
	
	@Test
	public void validateSignaturePersonalizeRecord() 
	{
		
		init();
		
		// SIGN only by certifier
		version = 0;
		initPersonalize();
		
		r_SertifyPubKeys.sign(certifier, false);
		// TRUE
		assertEquals(true, r_SertifyPubKeys.isSignatureValid(db));

		version = 1;
		r_SertifyPubKeys = new R_SertifyPubKeys(version, certifier, FEE_POWER, personKey,
				sertifiedPublicKeys,
				timestamp, certifier.getLastTimestamp(db));
		
		r_SertifyPubKeys.sign(certifier, false);
		// + sign by user
		r_SertifyPubKeys.signUserAccounts(sertifiedPrivateKeys);
		// true !
		//CHECK IF PERSONALIZE RECORD SIGNATURE IS VALID
		assertEquals(true, r_SertifyPubKeys.isSignatureValid(db));
		
		//INVALID SIGNATURE
		r_SertifyPubKeys.setTimestamp(r_SertifyPubKeys.getTimestamp() + 1);
		
		//CHECK IF PERSONALIZE RECORD SIGNATURE IS INVALID
		assertEquals(false, r_SertifyPubKeys.isSignatureValid(db));

		// BACK TO VALID
		r_SertifyPubKeys.setTimestamp(r_SertifyPubKeys.getTimestamp() - 1);
		assertEquals(true, r_SertifyPubKeys.isSignatureValid(db));

		r_SertifyPubKeys.sign(null, false);
		//CHECK IF PERSONALIZE RECORD SIGNATURE IS INVALID
		assertEquals(false, r_SertifyPubKeys.isSignatureValid(db));

		// BACK TO VALID
		r_SertifyPubKeys.sign(certifier, false);
		assertEquals(true, r_SertifyPubKeys.isSignatureValid(db));

	}
	
	@Test
	public void parsePersonalizeRecord() 
	{

		init();
		
		version = 1;
		initPersonalize();

		// SIGN
		r_SertifyPubKeys.signUserAccounts(sertifiedPrivateKeys);
		r_SertifyPubKeys.sign(certifier, false);

		//CONVERT TO BYTES
		byte[] rawPersonTransfer = r_SertifyPubKeys.toBytes(true, releaserReference);
		
		//CHECK DATALENGTH
		assertEquals(rawPersonTransfer.length, r_SertifyPubKeys.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			R_SertifyPubKeys parsedPersonTransfer = (R_SertifyPubKeys) TransactionFactory.getInstance().parse(rawPersonTransfer, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedPersonTransfer instanceof R_SertifyPubKeys);
			
			//CHECK TYPEBYTES
			assertEquals(true, Arrays.equals(r_SertifyPubKeys.getTypeBytes(), parsedPersonTransfer.getTypeBytes()));				

			//CHECK TIMESTAMP
			assertEquals(r_SertifyPubKeys.getTimestamp(), parsedPersonTransfer.getTimestamp());				

			//CHECK REFERENCE
			//assertEquals(r_SertifyPubKeys.getReference(), parsedPersonTransfer.getReference());	

			//CHECK CREATOR
			assertEquals(r_SertifyPubKeys.getCreator().getAddress(), parsedPersonTransfer.getCreator().getAddress());				

			//CHECK FEE POWER
			assertEquals(r_SertifyPubKeys.getFee(), parsedPersonTransfer.getFee());	

			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(r_SertifyPubKeys.getSignature(), parsedPersonTransfer.getSignature()));
			
			//CHECK KEY
			assertEquals(r_SertifyPubKeys.getKey(), parsedPersonTransfer.getKey());	
			
			//CHECK AMOUNT
			assertEquals(r_SertifyPubKeys.getAmount(certifier), parsedPersonTransfer.getAmount(certifier));
						
			//CHECK USER SIGNATURES
			assertEquals(true, Arrays.equals(r_SertifyPubKeys.getSertifiedPublicKeys().get(2).getPublicKey(),
									parsedPersonTransfer.getSertifiedPublicKeys().get(2).getPublicKey()));
			
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction." + e);
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawPersonTransfer = new byte[r_SertifyPubKeys.getDataLength(false)];
		
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

		//assertEquals( null, dbPS.getItem(personKey, ALIVE_KEY));

		assertEquals(false, userAccount1.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(false, userAccount2.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(false, userAccount3.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		
		initPersonalize();
		
		// .a - personKey, .b - end_date, .c - block height, .d - reference
		// PERSON STATUS ALIVE
		// exist assertEquals( null, dbPS.getItem(personKey));
		// exist assertEquals( new TreeMap<String, Stack<Tuple3<Integer, Integer, byte[]>>>(), dbPA.getItems(personKey));

		// .a - personKey, .b - end_date, .c - block height, .d - reference
		// PERSON STATUS ALIVE
		//assertEquals( genesisPersonKey + 1, personKey);
		//assertEquals( null, dbPS.getItem(personKey, ALIVE_KEY));

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
		
		BigDecimal oil_amount_diff = BigDecimal.valueOf(BlockChain.GIFTED_COMPU_AMOUNT, BlockChain.FEE_SCALE);
		
		BigDecimal erm_amount = certifier.getBalanceUSE(ERM_KEY,db);
		BigDecimal oil_amount = certifier.getBalanceUSE(FEE_KEY, db);
		
		BigDecimal erm_amount_user = userAccount1.getBalanceUSE(ERM_KEY, db);
		BigDecimal oil_amount_user = userAccount1.getBalanceUSE(FEE_KEY, db);
		
		//// PROCESS /////
		r_SertifyPubKeys.signUserAccounts(sertifiedPrivateKeys);
		r_SertifyPubKeys.sign(certifier, false);
		r_SertifyPubKeys.process(gb, false);
		int transactionIndex = gb.getTransactionSeq(r_SertifyPubKeys.getSignature());
		
		//CHECK BALANCE SENDER
		assertEquals(erm_amount, certifier.getBalanceUSE(ERM_KEY, db));
		// CHECK FEE BALANCE - FEE - GIFT
		assertEquals(oil_amount.subtract(oil_amount_diff).subtract(r_SertifyPubKeys.getFee()),
				certifier.getBalanceUSE(FEE_KEY, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BG_ZERO, userAccount1.getBalanceUSE(ERM_KEY, db));
		assertEquals(BlockChain.GIFTED_COMPU_AMOUNT, userAccount1.getBalanceUSE(FEE_KEY, db));
		assertEquals(BG_ZERO, userAccount2.getBalanceUSE(ERM_KEY, db));
		assertEquals(BG_ZERO, userAccount2.getBalanceUSE(FEE_KEY, db));
		assertEquals(BG_ZERO, userAccount3.getBalanceUSE(ERM_KEY, db));
		assertEquals(BG_ZERO, userAccount3.getBalanceUSE(FEE_KEY, db));
		
		//CHECK REFERENCE SENDER
		assertEquals(r_SertifyPubKeys.getTimestamp(), certifier.getLastTimestamp(db));
		
		//CHECK REFERENCE RECIPIENT
		// TRUE - new reference for first send FEE
		assertEquals(r_SertifyPubKeys.getTimestamp(), userAccount1.getLastTimestamp(db));
		// byte[0]
		assertEquals(null, userAccount2.getLastTimestamp(db));
		assertEquals(null, userAccount3.getLastTimestamp(db));

		////////// TO DATE ////////
		// .a - personKey, .b - end_date, .c - block height, .d - reference
		int to_date = R_SertifyPubKeys.DEFAULT_DURATION + (int)(r_SertifyPubKeys.getTimestamp() / 86400000.0);

		// PERSON STATUS ALIVE - beg_date = person birthDay
		//assertEquals( (long)person.getBirthday(), (long)dbPS.getItem(personKey, ALIVE_KEY).a);
		// PERSON STATUS ALIVE - to_date = 0 - permanent alive
		//assertEquals( (long)Long.MAX_VALUE, (long)dbPS.getItem(personKey, ALIVE_KEY).b);
		//assertEquals( true, Arrays.equals(dbPS.getItem(personKey, ALIVE_KEY).c, r_SertifyPubKeys.getSignature()));
		//assertEquals( (int)dbPS.getItem(personKey, ALIVE_KEY).d, transactionIndex);

		// ADDRESSES
		assertEquals( (long)personKey, (long)dbAP.getItem(userAddress1).a);
		assertEquals( to_date, (int)dbAP.getItem(userAddress1).b);
		assertEquals( 1, (int)dbAP.getItem(userAddress1).c);
//		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress1).d, r_SertifyPubKeys.getSignature()));
		// PERSON -> ADDRESS
		assertEquals( to_date, (int)dbPA.getItem(personKey, userAddress1).a);
		assertEquals( 1, (int)dbPA.getItem(personKey, userAddress1).b);
//		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress1).c, r_SertifyPubKeys.getSignature()));

		assertEquals( (long)personKey, (long)dbAP.getItem(userAddress2).a);
		assertEquals( to_date, (int)dbAP.getItem(userAddress2).b);
		assertEquals( 1, (int)dbAP.getItem(userAddress2).c);
//		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress2).d, r_SertifyPubKeys.getSignature()));
		// PERSON -> ADDRESS
		assertEquals( to_date, (int)dbPA.getItem(personKey, userAddress2).a);
		assertEquals( 1, (int)dbPA.getItem(personKey, userAddress2).b);
//		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress2).c, r_SertifyPubKeys.getSignature()));

		assertEquals( (long)personKey, (long)dbAP.getItem(userAddress3).a);
		assertEquals( to_date, (int)dbAP.getItem(userAddress3).b);
		assertEquals( 1, (int)dbAP.getItem(userAddress3).c);
//		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress3).d, r_SertifyPubKeys.getSignature()));
		// PERSON -> ADDRESS
		assertEquals( to_date, (int)dbPA.getItem(personKey, userAddress3).a);
		assertEquals( 1, (int)dbPA.getItem(personKey, userAddress3).b);
//		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress3).c, r_SertifyPubKeys.getSignature()));

		assertEquals(true, userAccount1.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(true, userAccount2.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(true, userAccount3.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		
		////////// ORPHAN //////////////////
		r_SertifyPubKeys.orphan(false);

		//CHECK BALANCE SENDER
		assertEquals(erm_amount, certifier.getBalanceUSE(ERM_KEY, db));
		assertEquals(oil_amount, certifier.getBalanceUSE(FEE_KEY, db));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(erm_amount_user, userAccount1.getBalanceUSE(ERM_KEY, db));
		assertEquals(oil_amount_user, userAccount1.getBalanceUSE(FEE_KEY, db));
		assertEquals(BG_ZERO, userAccount2.getBalanceUSE(ERM_KEY, db));
		assertEquals(BG_ZERO, userAccount2.getBalanceUSE(FEE_KEY, db));
		assertEquals(BG_ZERO, userAccount3.getBalanceUSE(ERM_KEY, db));
		assertEquals(BG_ZERO, userAccount3.getBalanceUSE(FEE_KEY, db));
		
		//CHECK REFERENCE SENDER
		//assertEquals(r_SertifyPubKeys.getReference(), certifier.getLastReference(db));
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(null, userAccount1.getLastTimestamp(db));
		assertEquals(null, userAccount2.getLastTimestamp(db));
		assertEquals(null, userAccount3.getLastTimestamp(db));
		
		// .a - personKey, .b - end_date, .c - block height, .d - reference
		// PERSON STATUS ALIVE - must not be modified!
		//assertEquals( (long)person.getBirthday(), (long)dbPS.getItem(personKey, ALIVE_KEY).a);

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

		assertEquals(false, userAccount1.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(false, userAccount2.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(false, userAccount3.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));

		/////////////////////////////////////////////// TEST DURATIONS
		// TRY DURATIONS
		int end_date = 222;
		r_SertifyPubKeys = new R_SertifyPubKeys(0, certifier, FEE_POWER, personKey,
				sertifiedPublicKeys,
				end_date, timestamp, certifier.getLastTimestamp(db));
		r_SertifyPubKeys.signUserAccounts(sertifiedPrivateKeys);
		r_SertifyPubKeys.sign(certifier, false);
		r_SertifyPubKeys.process(gb, false);

		int abs_end_date = end_date + (int)(r_SertifyPubKeys.getTimestamp() / 86400000.0);
		
		// PERSON STATUS ALIVE - date_begin
		//assertEquals( (long)person.getBirthday(), (long)dbPS.getItem(personKey, ALIVE_KEY).a);

		assertEquals(abs_end_date, (int)userAccount1.getPersonDuration(db).b);
		assertEquals(true, userAccount2.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));

		// TEST LIST and STACK
		int end_date2 = -12;
		r_SertifyPubKeys = new R_SertifyPubKeys(0, certifier, FEE_POWER, personKey,
				sertifiedPublicKeys,
				end_date2, timestamp, certifier.getLastTimestamp(db));
		r_SertifyPubKeys.signUserAccounts(sertifiedPrivateKeys);
		r_SertifyPubKeys.sign(certifier, false);
		r_SertifyPubKeys.process(gb, false);

		int abs_end_date2 = end_date2 + (int)(r_SertifyPubKeys.getTimestamp() / 86400000.0);

		assertEquals(abs_end_date2, (int)userAccount2.getPersonDuration(db).b);
		assertEquals(false, userAccount2.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));

		r_SertifyPubKeys.orphan(false);

		assertEquals(abs_end_date, (int)userAccount2.getPersonDuration(db).b);
		assertEquals(true, userAccount2.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		
	}
	
	@Test
	public void fork_process_orphan_PersonalizeRecord()
	{
		
		// TODO !!!
		// need .process in DB then .process in FORK - then test DB values!!!
		init();

		//assertEquals( null, dbPS.getItem(personKey, ALIVE_KEY));

		assertEquals(false, userAccount1.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(false, userAccount2.isPerson(db, db.getBlockMap().size()));
		assertEquals(false, userAccount3.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		
		initPersonalize();
		
		// .a - personKey, .b - end_date, .c - block height, .d - reference
		// PERSON STATUS ALIVE
		// exist assertEquals( null, dbPS.getItem(personKey));
		// exist assertEquals( new TreeMap<String, Stack<Tuple3<Integer, Integer, byte[]>>>(), dbPA.getItems(personKey));

		// .a - personKey, .b - end_date, .c - block height, .d - reference
		// PERSON STATUS ALIVE
		assertEquals(2, personKey);
		//assertEquals( null, dbPS.getItem(personKey, ALIVE_KEY));

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
		
		BigDecimal oil_amount_diff = BigDecimal.valueOf(BlockChain.GIFTED_COMPU_AMOUNT, BlockChain.FEE_SCALE);
		
		BigDecimal erm_amount = certifier.getBalanceUSE(ERM_KEY,db);
		BigDecimal oil_amount = certifier.getBalanceUSE(FEE_KEY, db);
		
		BigDecimal erm_amount_user = userAccount1.getBalanceUSE(ERM_KEY, db);
		BigDecimal oil_amount_user = userAccount1.getBalanceUSE(FEE_KEY, db);
		
		last_ref = certifier.getLastTimestamp(db);
		
		//// PROCESS /////
		r_SertifyPubKeys.signUserAccounts(sertifiedPrivateKeys);
		r_SertifyPubKeys.sign(certifier, false);
		
		DCSet fork = db.fork();

		PersonAddressMap dbPA_fork = fork.getPersonAddressMap();
		AddressPersonMap dbAP_fork = fork.getAddressPersonMap();
		KKPersonStatusMap dbPS_fork = fork.getPersonStatusMap();

		
		r_SertifyPubKeys.process(gb, false);
		int transactionIndex = gb.getTransactionSeq(r_SertifyPubKeys.getSignature());
		
		//assertEquals( null, dbPS.getItem(personKey, ALIVE_KEY));

		//CHECK BALANCE SENDER
		assertEquals(erm_amount, certifier.getBalanceUSE(ERM_KEY, db));
		// CHECK FEE BALANCE - FEE - GIFT
		assertEquals(oil_amount,
				certifier.getBalanceUSE(FEE_KEY, db));

		// INN FORK
		//CHECK BALANCE SENDER
		assertEquals(erm_amount, certifier.getBalanceUSE(ERM_KEY, fork));
		// CHECK FEE BALANCE - FEE - GIFT
		assertEquals(oil_amount.subtract(oil_amount_diff).subtract(r_SertifyPubKeys.getFee()),
				certifier.getBalanceUSE(FEE_KEY, fork));

		//CHECK BALANCE RECIPIENT
		assertEquals(BG_ZERO, userAccount1.getBalanceUSE(ERM_KEY, db));
		// in FORK
		//CHECK BALANCE RECIPIENT
		assertEquals(BlockChain.GIFTED_COMPU_AMOUNT, userAccount1.getBalanceUSE(FEE_KEY, fork));

		//CHECK REFERENCE SENDER
		assertEquals(last_ref, certifier.getLastTimestamp(db));
		assertEquals(r_SertifyPubKeys.getTimestamp(), certifier.getLastTimestamp(fork));
		
		//CHECK REFERENCE RECIPIENT
		// TRUE - new reference for first send FEE
		assertEquals(null, userAccount1.getLastTimestamp(db));
		assertEquals(r_SertifyPubKeys.getTimestamp(), userAccount1.getLastTimestamp(fork));

		////////// TO DATE ////////
		// .a - personKey, .b - end_date, .c - block height, .d - reference
		int to_date = R_SertifyPubKeys.DEFAULT_DURATION + (int)(r_SertifyPubKeys.getTimestamp() / 86400000.0);

		// PERSON STATUS ALIVE - beg_date = person birthDay
		//assertEquals( null, dbPS.getItem(personKey, ALIVE_KEY));
		//assertEquals( (long)person.getBirthday(), (long)dbPS_fork.getItem(personKey, ALIVE_KEY).a);
		// PERSON STATUS ALIVE - to_date = 0 - permanent alive
		//assertEquals( (long)Long.MAX_VALUE, (long)dbPS_fork.getItem(personKey, ALIVE_KEY).b);
		//assertEquals( true, Arrays.equals(dbPS.getItem(personKey, ALIVE_KEY).c, r_SertifyPubKeys.getSignature()));
		//assertEquals( (int)dbPS_fork.getItem(personKey, ALIVE_KEY).d, transactionIndex);

		// ADDRESSES
		assertEquals( null, dbAP.getItem(userAddress1));
		Tuple4<Long, Integer, Integer, Integer> item_AP = dbAP_fork.getItem(userAddress1);
		assertEquals( (long)personKey, (long)item_AP.a);
		assertEquals( to_date, (int)item_AP.b);
		assertEquals( 1, (int)item_AP.c);
//		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress1).d, r_SertifyPubKeys.getSignature()));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA.getItem(personKey, userAddress1));
		assertEquals( to_date, (int)dbPA_fork.getItem(personKey, userAddress1).a);
		assertEquals( 1, (int)dbPA_fork.getItem(personKey, userAddress1).b);
//		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress1).c, r_SertifyPubKeys.getSignature()));

		assertEquals( null, dbAP.getItem(userAddress2));
		assertEquals( (long)personKey, (long)dbAP_fork.getItem(userAddress2).a);
		assertEquals( to_date, (int)dbAP_fork.getItem(userAddress2).b);
		//assertEquals( 1, (int)dbAP_fork.getItem(userAddress2).c);
//		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress2).d, r_SertifyPubKeys.getSignature()));

		// PERSON -> ADDRESS
		assertEquals( null, dbPA.getItem(personKey, userAddress2));
		assertEquals( to_date, (int)dbPA_fork.getItem(personKey, userAddress2).a);
		//assertEquals( 1, (int)dbPA_fork.getItem(personKey, userAddress2).b);
//		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress2).c, r_SertifyPubKeys.getSignature()));

		assertEquals( null, dbAP.getItem(userAddress3));
		assertEquals( (long)personKey, (long)dbAP_fork.getItem(userAddress3).a);
		assertEquals( to_date, (int)dbAP_fork.getItem(userAddress3).b);
		//assertEquals( 1, (int)dbAP_fork.getItem(userAddress3).c);
//		assertEquals( true, Arrays.equals(dbAP.getItem(userAddress3).d, r_SertifyPubKeys.getSignature()));

		// PERSON -> ADDRESS
		assertEquals( null, dbPA.getItem(personKey, userAddress3));
		assertEquals( to_date, (int)dbPA_fork.getItem(personKey, userAddress3).a);
		//assertEquals( 1, (int)dbPA_fork.getItem(personKey, userAddress3).b);
//		assertEquals( true, Arrays.equals(dbPA.getItem(personKey, userAddress3).c, r_SertifyPubKeys.getSignature()));

		assertEquals(false, userAccount1.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(null, userAccount1.getPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(false, userAccount2.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(false, userAccount3.isPerson(db, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));

		assertEquals(true, userAccount1.isPerson(fork, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertNotEquals(null, userAccount1.getPerson(fork, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(true, userAccount2.isPerson(fork, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(true, userAccount3.isPerson(fork, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));

		
		////////// ORPHAN //////////////////
		r_SertifyPubKeys.orphan(false);

		//CHECK BALANCE SENDER
		assertEquals(erm_amount, certifier.getBalanceUSE(ERM_KEY, fork));
		assertEquals(oil_amount, certifier.getBalanceUSE(FEE_KEY, fork));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(erm_amount_user, userAccount1.getBalanceUSE(ERM_KEY, fork));
		assertEquals(oil_amount_user, userAccount1.getBalanceUSE(FEE_KEY, fork));
		assertEquals(BG_ZERO, userAccount2.getBalanceUSE(ERM_KEY, fork));
		assertEquals(BG_ZERO, userAccount2.getBalanceUSE(FEE_KEY, fork));
		assertEquals(BG_ZERO, userAccount3.getBalanceUSE(ERM_KEY, fork));
		assertEquals(BG_ZERO, userAccount3.getBalanceUSE(FEE_KEY, fork));
		
		//CHECK REFERENCE SENDER
		//assertEquals(r_SertifyPubKeys.getReference(), certifier.getLastReference(fork));
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(null, userAccount1.getLastTimestamp(fork));
		assertEquals(null, userAccount2.getLastTimestamp(fork));
		assertEquals(null, userAccount3.getLastTimestamp(fork));
		
		// .a - personKey, .b - end_date, .c - block height, .d - reference
		// PERSON STATUS ALIVE - must not be modified!
		//assertEquals( (long)person.getBirthday(), (long)dbPS_fork.getItem(personKey, ALIVE_KEY).a);

		// ADDRESSES
		assertEquals( null, dbAP_fork.getItem(userAddress1));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA_fork.getItem(personKey, userAddress1));

		assertEquals( null, dbAP_fork.getItem(userAddress2));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA_fork.getItem(personKey, userAddress2));

		assertEquals( null, dbAP_fork.getItem(userAddress3));
		// PERSON -> ADDRESS
		assertEquals( null, dbPA_fork.getItem(personKey, userAddress3));

		assertEquals(false, userAccount1.isPerson(fork, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(false, userAccount2.isPerson(fork, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
		assertEquals(false, userAccount3.isPerson(fork, db.getBlockSignsMap().getHeight(db.getBlockMap().getLastBlockSignature())));
	}

	@Test
	public void validatePersonHumanRecord() 
	{	
		
		init();
				
		/*
		PersonCls person = new PersonHuman((PublicKeyAccount)certifier, "person 123456789", 1111, 3333,
				"", "", 22, 33,
				"", "", "", 170, icon, image, "", ownerSignature);
				
		GenesisIssuePersonRecord genesis_issue_person = new GenesisIssuePersonRecord(personGeneral);
		genesis_issue_person.process(db, gb, false);
		//genesisPersonKey = db.getIssuePersonMap().size();
		genesisPersonKey = genesis_issue_person.getAssetKey(db);
		*/ 

	}

}
