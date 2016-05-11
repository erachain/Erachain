package test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.util.ArrayList;
import java.util.Arrays;
//import java.util.List;
 import org.apache.log4j.Logger;

import ntp.NTP;

import org.junit.Test;
import org.mapdb.Fun.Tuple3;

import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.item.persons.PersonHuman;
import core.item.statuses.Status;
import core.item.statuses.StatusCls;
import core.transaction.IssuePersonRecord;
import core.transaction.R_SetStatusToItem;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;

import utils.Corekeys;

//import com.google.common.primitives.Longs;

import database.DBSet;
import database.ItemStatusMap;

public class TestRecSetStatusToItem {

	static Logger LOGGER = Logger.getLogger(TestRecSetStatusToItem.class.getName());

	byte[] releaserReference = null;

	boolean asPack = false;
	long ERM_KEY = AssetCls.ERMO_KEY;
	long FEE_KEY = AssetCls.FEE_KEY;
	byte FEE_POWER = (byte)0;
	byte[] statusReference = new byte[64];
	long timestamp = NTP.getTime();
	long status_key = StatusCls.ALIVE_KEY;
	int to_date = 0;
	long item_key;
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
	int mapSize;

	PersonCls personGeneral;
	PersonCls person;
	ItemCls item;
	IssuePersonRecord issuePersonTransaction;
	R_SetStatusToItem setStatusTransaction;

	// INIT STATUSS
	private void init() {
		
		db = DBSet.createEmptyDatabaseSet();
		gb = new GenesisBlock();
		gb.process(db);
		
		// FEE FUND
		maker.setLastReference(gb.getGeneratorSignature(), db);
		maker.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(10000).setScale(8), db);
		maker.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);
		//statusMap = db.getItemStatusMap();
		//mapSize = statusMap.size();

		person = new PersonHuman(maker, "Ermolaev1 Dmitrii Sergeevich", timestamp - 12345678,
				(byte)1, "Slav", (float)128.12345, (float)33.7777,
				"white", "green", "шанет", 188, "изобретатель, мыслитель, создатель идей");

		//CREATE ISSUE PERSON TRANSACTION
		issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp, maker.getLastReference(db));
		issuePersonTransaction.process(db, false);
		item = issuePersonTransaction.getItem();
		item_key = item.getKey();

		setStatusTransaction = new R_SetStatusToItem(maker, FEE_POWER, status_key, item, to_date, timestamp, maker.getLastReference(db));

	}
	
	//SET STATUS TRANSACTION
	
	@Test
	public void validateSignatureSetStatusTransaction() 
	{
		
		init();
		
				
		//CREATE SET STATUS TRANSACTION
		setStatusTransaction.sign(maker, false);
		
		//CHECK IF ISSUE STATUS TRANSACTION IS VALID
		assertEquals(true, setStatusTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		setStatusTransaction = new R_SetStatusToItem(maker, FEE_POWER, status_key, item, to_date, timestamp, maker.getLastReference(db), new byte[64]);
		
		//CHECK IF ISSUE STATUS IS INVALID
		assertEquals(false, setStatusTransaction.isSignatureValid());
	}
		

	
	@Test
	public void parseSetStatusTransaction() 
	{
		
		init();
		
		setStatusTransaction.sign(maker, false);
		
		//CONVERT TO BYTES
		byte[] rawIssueStatusTransaction = setStatusTransaction.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawIssueStatusTransaction.length, setStatusTransaction.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			R_SetStatusToItem parsedSetStatusTransaction = (R_SetStatusToItem) TransactionFactory.getInstance().parse(rawIssueStatusTransaction, releaserReference);
			LOGGER.info("parsedSetStatusTransaction: " + parsedSetStatusTransaction);

			//CHECK INSTANCE
			assertEquals(true, parsedSetStatusTransaction instanceof R_SetStatusToItem);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(setStatusTransaction.getSignature(), parsedSetStatusTransaction.getSignature()));
			
			//CHECK STATUS KEY
			assertEquals(setStatusTransaction.getKey(), parsedSetStatusTransaction.getKey());

			//CHECK TO DATE
			assertEquals(setStatusTransaction.getEndDate(), parsedSetStatusTransaction.getEndDate());

			//CHECK ISSUER
			assertEquals(setStatusTransaction.getCreator().getAddress(), parsedSetStatusTransaction.getCreator().getAddress());
			
			//// RESET item to FORK DB
			parsedSetStatusTransaction.resetItemToDB(db);

			//CHECK NAME
			assertEquals(setStatusTransaction.getItem().getName(), parsedSetStatusTransaction.getItem().getName());
				
			//CHECK OWNER
			assertEquals(setStatusTransaction.getItem().getCreator().getAddress(), parsedSetStatusTransaction.getItem().getCreator().getAddress());
			
			//CHECK DESCRIPTION
			assertEquals(setStatusTransaction.getItem().getDescription(), parsedSetStatusTransaction.getItem().getDescription());
							
			//CHECK FEE
			assertEquals(setStatusTransaction.getFee(), parsedSetStatusTransaction.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(setStatusTransaction.getReference(), parsedSetStatusTransaction.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(setStatusTransaction.getTimestamp(), parsedSetStatusTransaction.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}
		
	}

	
	@Test
	public void process_orphanSetStatusTransaction()
	{
		
		init();				
		
		assertEquals(Transaction.ACCOUNT_NOT_PERSONALIZED, setStatusTransaction.isValid(db, releaserReference));
		assertEquals(db.getPersonStatusMap().get(item.getKey()).size(),	0);

		Tuple3<Integer, Integer, byte[]> statusDuration = db.getPersonStatusMap().getItem(item_key, status_key);
		// TEST TIME and EXPIRE TIME for ALIVE person
		assertEquals( null, statusDuration);

		
		setStatusTransaction.sign(maker, false);
		setStatusTransaction.process(db, false);
				
		statusDuration = db.getPersonStatusMap().getItem(item_key, status_key);
		// TEST TIME and EXPIRE TIME for ALIVE person
		int days = statusDuration.a;
		//days *= (long)86400;
		assertEquals(days,	0);
		
		to_date = 1234;
		R_SetStatusToItem setStatusTransaction_2 = new R_SetStatusToItem(maker, FEE_POWER, status_key, item, to_date, timestamp+10, maker.getLastReference(db));
		setStatusTransaction_2.sign(maker, false);
		setStatusTransaction_2.process(db, false);

		statusDuration = db.getPersonStatusMap().getItem(item_key, status_key);
		days = statusDuration.a;
		assertEquals(days,	1234);
		
		
		////// ORPHAN 2 ///////
		setStatusTransaction_2.orphan(db, false);
		
		statusDuration = db.getPersonStatusMap().getItem(item_key, status_key);
		days = statusDuration.a;
		assertEquals(days,	0);

		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(setStatusTransaction.getSignature(), maker.getLastReference(db)));

		////// ORPHAN ///////
		setStatusTransaction.orphan(db, false);
										
		statusDuration = db.getPersonStatusMap().getItem(item_key, status_key);
		assertEquals(statusDuration, null);

		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(setStatusTransaction.getReference(), maker.getLastReference(db)));
	}
	
	// TODO - in statement - valid on key = 999
}
