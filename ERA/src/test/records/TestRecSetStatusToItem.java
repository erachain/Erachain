package test.records;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.nio.charset.Charset;
//import java.math.BigInteger;
//import java.util.ArrayList;
import java.util.Arrays;
//import java.util.List;
 import org.apache.log4j.Logger;

import ntp.NTP;

import org.junit.Test;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;

import controller.Controller;
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
import datachain.DCSet;
import datachain.ItemStatusMap;
import utils.Corekeys;

public class TestRecSetStatusToItem {

	static Logger LOGGER = Logger.getLogger(TestRecSetStatusToItem.class.getName());

	Long releaserReference = null;

	boolean asPack = false;
	long ERM_KEY = AssetCls.ERA_KEY;
	long FEE_KEY = AssetCls.FEE_KEY;
	byte FEE_POWER = (byte)0;
	byte[] statusReference = new byte[64];
	long timestamp = NTP.getTime();
	long status_key = 1l;
	Long to_date = null;
	long personkey;
	
	private byte[] icon = new byte[]{1,3,4,5,6,9}; // default value
	private byte[] image = new byte[]{4,11,32,23,45,122,11,-45}; // default value
	private byte[] ownerSignature = new byte[Crypto.SIGNATURE_LENGTH];

	//CREATE EMPTY MEMORY DATABASE
	private DCSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
	int mapSize;

	PersonCls personGeneral;
	PersonCls person;
	IssuePersonRecord issuePersonTransaction;
	R_SetStatusToItem setStatusTransaction;

	// INIT STATUSS
	private void init() {
		
		db = DCSet.createEmptyDatabaseSet();
		gb = new GenesisBlock();
		try {
			gb.process(db);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// FEE FUND
		maker.setLastTimestamp(gb.getTimestamp(db), db);
		maker.changeBalance(db, false, ERM_KEY, BigDecimal.valueOf(10000).setScale(8), false);
		maker.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1).setScale(8), false);
		//statusMap = db.getItemStatusMap();
		//mapSize = statusMap.size();

		long birthDay =  timestamp - 12345678;
		person = new PersonHuman(maker, "Ermolaev1 Dmitrii Sergeevich", birthDay, birthDay - 1,
				(byte)1, "Slav", (float)128.12345, (float)33.7777,
				"white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей", ownerSignature);

		//CREATE ISSUE PERSON TRANSACTION
		issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp, maker.getLastTimestamp(db));
		issuePersonTransaction.process(db, gb, false);
		person = (PersonCls)issuePersonTransaction.getItem();
		personkey = person.getKey(db);

		timestamp += 100;
		setStatusTransaction = new R_SetStatusToItem(maker, FEE_POWER, status_key,
				person.getItemTypeInt(), person.getKey(db),
				to_date, birthDay + 1000,
				45646533, 987978972,
				"teasdsdst TEST".getBytes(Charset.forName("UTF-8")),
				"teasdskkj kjh kj EST".getBytes(Charset.forName("UTF-8")),
				0l,
				"DESCRIPTION".getBytes(Charset.forName("UTF-8")),
				timestamp, maker.getLastTimestamp(db));
		timestamp += 100;

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
		setStatusTransaction = new R_SetStatusToItem(maker, FEE_POWER, status_key,
				person.getItemTypeInt(), person.getKey(db), to_date, null,
				323234, 2342342, null, "test TEST 11".getBytes(Charset.forName("UTF-8")), 0l, null,
				timestamp, maker.getLastTimestamp(db), new byte[64]);
		
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
		
		R_SetStatusToItem parsedSetStatusTransaction = null;
		try 
		{	
			//PARSE FROM BYTES
			parsedSetStatusTransaction = (R_SetStatusToItem) TransactionFactory.getInstance().parse(rawIssueStatusTransaction, releaserReference);
			LOGGER.info("parsedSetStatusTransaction: " + parsedSetStatusTransaction);

		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}
		
		//CHECK LEN
		assertEquals(parsedSetStatusTransaction.getDataLength(false), setStatusTransaction.getDataLength(false));

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
		
		ItemCls item = ItemCls.getItem(db, setStatusTransaction.getItemType(), setStatusTransaction.getItemKey());
		ItemCls itemParsed = ItemCls.getItem(db, parsedSetStatusTransaction.getItemType(), parsedSetStatusTransaction.getItemKey());
		//CHECK NAME
		assertEquals(item.getName(), itemParsed.getName());
			
		//CHECK OWNER
		assertEquals(item.getOwner().getAddress(), itemParsed.getOwner().getAddress());
		
		//CHECK DESCRIPTION
		assertEquals(item.getDescription(), itemParsed.getDescription());
						
		//CHECK FEE
		assertEquals(setStatusTransaction.getFee(), parsedSetStatusTransaction.getFee());	
		
		//CHECK REFERENCE
		//assertEquals(setStatusTransaction.getReference(), parsedSetStatusTransaction.getReference());	
		
		//CHECK TIMESTAMP
		assertEquals(setStatusTransaction.getTimestamp(), parsedSetStatusTransaction.getTimestamp());				

		assertEquals(setStatusTransaction.getRefParent(), parsedSetStatusTransaction.getRefParent());				

	}

	
	@Test
	public void process_orphanSetStatusTransaction()
	{
		
		init();				
		
		assertEquals(Transaction.CREATOR_NOT_PERSONALIZED, setStatusTransaction.isValid(db, releaserReference));
		assertEquals(db.getPersonStatusMap().get(person.getKey(db)).size(),	0);

		Tuple5<Long, Long, byte[], Integer, Integer> statusDuration = db.getPersonStatusMap().getItem(personkey, status_key);
		// TEST TIME and EXPIRE TIME for ALIVE person
		assertEquals( null, statusDuration);

		
		setStatusTransaction.sign(maker, false);
		setStatusTransaction.process(db, gb, false);
				
		statusDuration = db.getPersonStatusMap().getItem(personkey, status_key);
		// TEST TIME and EXPIRE TIME for ALIVE person
		Long endDate = statusDuration.a;
		//days *= (long)86400;
		assertEquals((long)endDate, (long)Long.MIN_VALUE);
		
		to_date = timestamp + 1234L * 84600000L;
		R_SetStatusToItem setStatusTransaction_2 = new R_SetStatusToItem(maker, FEE_POWER, status_key,
				person.getItemTypeInt(), person.getKey(db), to_date, null,
				234354, 546567,
				"wersdfsdfsdftest TEST".getBytes(Charset.forName("UTF-8")),
				"test TEST".getBytes(Charset.forName("UTF-8")),
				0l,
				"tasasdasdasfsdfsfdsdfest TEST".getBytes(Charset.forName("UTF-8")),
				timestamp+10, maker.getLastTimestamp(db));
		setStatusTransaction_2.sign(maker, false);
		setStatusTransaction_2.process(db, gb, false);

		statusDuration = db.getPersonStatusMap().getItem(personkey, status_key);
		endDate = statusDuration.a;
		assertEquals(endDate,	to_date);
		
		
		////// ORPHAN 2 ///////
		setStatusTransaction_2.orphan(db, false);
		
		statusDuration = db.getPersonStatusMap().getItem(personkey, status_key);
		endDate = statusDuration.a;
		assertEquals((long)endDate, (long)Long.MIN_VALUE);

		//CHECK REFERENCE SENDER
		assertEquals(setStatusTransaction.getTimestamp(), maker.getLastTimestamp(db));

		////// ORPHAN ///////
		setStatusTransaction.orphan(db, false);
										
		statusDuration = db.getPersonStatusMap().getItem(personkey, status_key);
		assertEquals(statusDuration, null);

		//CHECK REFERENCE SENDER
		//assertEquals(setStatusTransaction.getReference(), maker.getLastReference(db));
	}
	
	// TODO - in statement - valid on key = 999
}
