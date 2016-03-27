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
import qora.assets.Statement;
import qora.assets.Venture;
import qora.block.GenesisBlock;
import qora.crypto.Crypto;

import qora.transaction.GenesisIssueAssetTransaction;
import qora.transaction.IssueAssetTransaction;
import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;
import qora.transaction.PersonalizeRecord;
import qora.transaction.RecStatement;;

public class TrPersonTests {

	long OIL_KEY = 1l;
	BigDecimal bgZERO = BigDecimal.ZERO.setScale(8);
	byte FEE_POWER = (byte)1;
	byte[] assetReference = new byte[64];
	long timestamp = NTP.getTime();
	int duration = 356;
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);

	byte[] seed_2 = Crypto.getInstance().digest("test_2".getBytes());
	byte[] privateKey_2 = Crypto.getInstance().createKeyPair(seed_2).getA();
	PrivateKeyAccount applicant = new PrivateKeyAccount(privateKey_2);
	byte[] personalData = "FIO:Ermolaev Dmitrii, SN: 2345 1234567".getBytes();

	
	// INIT ASSETS
	private void init() {
		
		db = DBSet.createEmptyDatabaseSet();
		gb = new GenesisBlock();
		gb.process(db);
		
		// OIL FUND
		maker.setLastReference(gb.getGeneratorSignature(), db);
		maker.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), db);
		maker.setConfirmedBalance(0l, BigDecimal.valueOf(999000).setScale(8), db);
		maker.setConfirmedBalance(2l, BigDecimal.valueOf(9000).setScale(8), db);

	}
	
	//TRANSFER ASSET
	
	@Test
	public void validateSignaturePersonalizeRecord() 
	{
		
		init();
				
		//CREATE PERSONALIZE RECORD
		PersonalizeRecord personalizeRecord = new PersonalizeRecord(maker, FEE_POWER, applicant, personalData, duration, timestamp, maker.getLastReference(db));
		personalizeRecord.sign(maker);
		personalizeRecord.signApplicant(applicant);
		
		//CHECK IF SIGNATURE IS VALID
		assertEquals(true, personalizeRecord.isSignatureValid());
		
		// VALID SIGNATURE ?
		personalizeRecord = new PersonalizeRecord(maker, FEE_POWER, applicant, personalData, duration,
				timestamp, maker.getLastReference(db),
				personalizeRecord.getSignature(), personalizeRecord.getApplicantSignature());
		
		//CHECK IF REPESONALIZE RECORD SIGNATURE IS INVALID
		assertEquals(true, personalizeRecord.isSignatureValid());

		//INVALID SIGNATURE
		personalizeRecord = new PersonalizeRecord(maker, FEE_POWER, applicant, personalData, duration,
				timestamp+1, maker.getLastReference(db),
				personalizeRecord.getSignature(), personalizeRecord.getApplicantSignature());
		
		//CHECK IF REPESONALIZE RECORD SIGNATURE IS INVALID
		assertEquals(false, personalizeRecord.isSignatureValid());

		//INVALID SIGNATURE
		personalizeRecord = new PersonalizeRecord(maker, FEE_POWER, applicant, personalData, duration, timestamp+1, maker.getLastReference(db), new byte[64], new byte[64]);
		
		//CHECK IF REPESONALIZE RECORD SIGNATURE IS INVALID
		assertEquals(false, personalizeRecord.isSignatureValid());

	}
	
	@Test
	public void validatePersonalizeRecord() 
	{	
		
		init();
						
		//CREATE VALID RECORD
		PersonalizeRecord personalizeRecord = new PersonalizeRecord(maker, FEE_POWER, applicant, personalData, duration, timestamp, maker.getLastReference(db));
		personalizeRecord.sign(maker);
		personalizeRecord.signApplicant(applicant);

		//CHECK IF REPESONALIZE RECORD IS VALID
		assertEquals(Transaction.VALIDATE_OK, personalizeRecord.isValid(db));
		
		personalizeRecord.process(db);
		
		//CREATE VALID RECORD
		personalizeRecord = new PersonalizeRecord(maker, FEE_POWER, applicant, personalData, duration, timestamp, maker.getLastReference(db));

		//CHECK IF REPESONALIZE RECORD IS VALID
		assertEquals(Transaction.VALIDATE_OK, personalizeRecord.isValid(db));			
									
		//CREATE INVALID REPESONALIZE RECORD WRONG REFERENCE
		personalizeRecord = new PersonalizeRecord(maker, FEE_POWER, applicant,  personalData, duration, timestamp, new byte[64]);
						
		//CHECK IF REPESONALIZE RECORD IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, personalizeRecord.isValid(db));	

		//CREATE INVALID REPESONALIZE RECORD NOT ENOUGH ASSET BALANCE
		maker.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(0).setScale(8), db);
		//CHECK IF REPESONALIZE RECORD IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, personalizeRecord.isValid(db));	

		maker.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), db);
		personalizeRecord = new PersonalizeRecord(maker, FEE_POWER, applicant, personalData, duration, timestamp, maker.getLastReference(db));
		personalizeRecord.sign(maker);
		personalizeRecord.signApplicant(applicant);
		personalizeRecord.process(db);
		assertEquals(Transaction.VALIDATE_OK, personalizeRecord.isValid(db));	

		maker.setConfirmedBalance(0l, bgZERO, db);
		//CHECK IF REPESONALIZE RECORD IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, personalizeRecord.isValid(db));	
		maker.setConfirmedBalance(0l, BigDecimal.valueOf(999000).setScale(8), db);


		maker.setConfirmedBalance(2l, bgZERO, db);

		//CHECK IF REPESONALIZE RECORD IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, personalizeRecord.isValid(db));	
						
	}
	
	@Test
	public void parsePersonalizeRecord() 
	{

		init();
		
		//CREATE VALID REPESONALIZE RECORD
		Transaction personalizeRecord = new PersonalizeRecord(maker, FEE_POWER, applicant, personalData, duration, timestamp, maker.getLastReference(db));
		personalizeRecord.sign(maker);
		PersonalizeRecord trPR = (PersonalizeRecord)personalizeRecord;
		trPR.signApplicant(applicant);

		//CONVERT TO BYTES
		byte[] rawPersonalizeRecord = personalizeRecord.toBytes(true);
		
		//CHECK DATALENGTH
		assertEquals(rawPersonalizeRecord.length, personalizeRecord.getDataLength());
		
		try 
		{	
			//PARSE FROM BYTES
			PersonalizeRecord parsedPersonalizeRecord = (PersonalizeRecord) TransactionFactory.getInstance().parse(rawPersonalizeRecord);
			
			//CHECK INSTANCE
			assertEquals(true, parsedPersonalizeRecord instanceof PersonalizeRecord);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(personalizeRecord.getSignature(), parsedPersonalizeRecord.getSignature()));

			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(personalizeRecord.getReference(), parsedPersonalizeRecord.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(personalizeRecord.getTimestamp(), parsedPersonalizeRecord.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawPersonalizeRecord = new byte[personalizeRecord.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawPersonalizeRecord);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void processPersonalizeRecord()
	{

		init();
		
		//CREATE REPESONALIZE RECORD
		Transaction personalizeRecord = new PersonalizeRecord(maker, FEE_POWER, applicant, personalData, duration, timestamp, maker.getLastReference(db));
		personalizeRecord.sign(maker);
		PersonalizeRecord trPR = (PersonalizeRecord)personalizeRecord;
		trPR.signApplicant(applicant);
		personalizeRecord.process(db);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.ZERO.setScale(8), maker.getConfirmedBalance(db));
		//assertEquals(BigDecimal.valueOf(100).setScale(8), maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE APPLICANT
		assertEquals(BigDecimal.ZERO.setScale(8), applicant.getConfirmedBalance(db));
		//assertEquals(BigDecimal.valueOf(100).setScale(8), applicant.getConfirmedBalance(key, db));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(personalizeRecord.getSignature(), maker.getLastReference(db)));
		
		//CHECK REFERENCE APPLICANT
		assertEquals(false, Arrays.equals(personalizeRecord.getSignature(), applicant.getLastReference(db)));
	
		personalizeRecord.orphan(db);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.ZERO.setScale(8), maker.getConfirmedBalance(db));
		//assertEquals(BigDecimal.valueOf(100).setScale(8), maker.getConfirmedBalance(key, db));
				
		//CHECK BALANCE APPLICANT
		assertEquals(BigDecimal.ZERO.setScale(8), applicant.getConfirmedBalance(db));
		//assertEquals(BigDecimal.ZERO.setScale(8), applicant.getConfirmedBalance(key, db));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(personalizeRecord.getReference(), maker.getLastReference(db)));
		
		//CHECK REFERENCE APPLICANT
		assertEquals(false, Arrays.equals(personalizeRecord.getSignature(), applicant.getLastReference(db)));
	}

}
