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

import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.statuses.Status;
import core.item.statuses.StatusCls;
import core.transaction.IssueStatusRecord;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;

import utils.Corekeys;

//import com.google.common.primitives.Longs;

import database.DBSet;
import database.ItemStatusMap;

public class TestRecStatus {

	static Logger LOGGER = Logger.getLogger(TestRecStatus.class.getName());

	byte[] releaserReference = null;

	boolean asPack = false;
	long FEE_KEY = AssetCls.DILE_KEY;
	long VOTE_KEY = AssetCls.ERMO_KEY;
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
	

	// INIT STATUSS
	private void init() {
		
		db = DBSet.createEmptyDatabaseSet();
		gb = new GenesisBlock();
		gb.process(db);
		
		// OIL FUND
		maker.setLastReference(gb.getGeneratorSignature(), db);
		maker.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);
		maker.setConfirmedBalance(VOTE_KEY, BigDecimal.valueOf(10).setScale(8), db);

	}
	
	@Test
	public void testAddreessVersion() 
	{
		int vers = Corekeys.findAddressVersion("E");
		assertEquals(-1111, vers);
	}
	
	//ISSUE STATUS TRANSACTION
	
	@Test
	public void validateSignatureIssueStatusTransaction() 
	{
		
		init();
		
		//CREATE STATUS
		Status status = new Status(maker, "test", "strontje");
				
		//CREATE ISSUE STATUS TRANSACTION
		Transaction issueStatusTransaction = new IssueStatusRecord(maker, status, FEE_POWER, timestamp, maker.getLastReference(db));
		issueStatusTransaction.sign(maker, false);
		
		//CHECK IF ISSUE STATUS TRANSACTION IS VALID
		assertEquals(true, issueStatusTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		issueStatusTransaction = new IssueStatusRecord(maker, status, FEE_POWER, timestamp, maker.getLastReference(db), new byte[64]);
		
		//CHECK IF ISSUE STATUS IS INVALID
		assertEquals(false, issueStatusTransaction.isSignatureValid());
	}
		

	
	@Test
	public void parseIssueStatusTransaction() 
	{
		
		init();
		
		StatusCls status = new Status(maker, "test132", "12345678910strontje");
		byte[] raw = status.toBytes(false);
		assertEquals(raw.length, status.getDataLength(false));
				
		//CREATE ISSUE STATUS TRANSACTION
		IssueStatusRecord issueStatusRecord = new IssueStatusRecord(maker, status, FEE_POWER, timestamp, maker.getLastReference(db));
		issueStatusRecord.sign(maker, false);
		issueStatusRecord.process(db, false);
		
		//CONVERT TO BYTES
		byte[] rawIssueStatusTransaction = issueStatusRecord.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawIssueStatusTransaction.length, issueStatusRecord.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			IssueStatusRecord parsedIssueStatusTransaction = (IssueStatusRecord) TransactionFactory.getInstance().parse(rawIssueStatusTransaction, releaserReference);
			LOGGER.info("parsedIssueStatusTransaction: " + parsedIssueStatusTransaction);

			//CHECK INSTANCE
			assertEquals(true, parsedIssueStatusTransaction instanceof IssueStatusRecord);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(issueStatusRecord.getSignature(), parsedIssueStatusTransaction.getSignature()));
			
			//CHECK ISSUER
			assertEquals(issueStatusRecord.getCreator().getAddress(), parsedIssueStatusTransaction.getCreator().getAddress());
			
			//CHECK OWNER
			assertEquals(issueStatusRecord.getItem().getCreator().getAddress(), parsedIssueStatusTransaction.getItem().getCreator().getAddress());
			
			//CHECK NAME
			assertEquals(issueStatusRecord.getItem().getName(), parsedIssueStatusTransaction.getItem().getName());
				
			//CHECK DESCRIPTION
			assertEquals(issueStatusRecord.getItem().getDescription(), parsedIssueStatusTransaction.getItem().getDescription());
							
			//CHECK FEE
			assertEquals(issueStatusRecord.getFee(), parsedIssueStatusTransaction.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(issueStatusRecord.getReference(), parsedIssueStatusTransaction.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(issueStatusRecord.getTimestamp(), parsedIssueStatusTransaction.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}
		
	}

	
	@Test
	public void processIssueStatusTransaction()
	{
		
		init();				
		
		Status status = new Status(maker, "test", "strontje");
				
		//CREATE ISSUE STATUS TRANSACTION
		IssueStatusRecord issueStatusRecord = new IssueStatusRecord(maker, status, FEE_POWER, timestamp, maker.getLastReference(db));
		
		assertEquals(Transaction.VALIDATE_OK, issueStatusRecord.isValid(db, releaserReference));
		
		issueStatusRecord.sign(maker, false);
		issueStatusRecord.process(db, false);
		
		LOGGER.info("status KEY: " + status.getKey());
				
		//CHECK STATUS EXISTS SENDER
		long key = db.getIssueStatusMap().get(issueStatusRecord);
		assertEquals(true, db.getItemStatusMap().contains(key));
		
		StatusCls status_2 = new Status(maker, "test132_2", "2_12345678910strontje");				
		IssueStatusRecord issueStatusTransaction_2 = new IssueStatusRecord(maker, status_2, FEE_POWER, timestamp+10, maker.getLastReference(db));
		issueStatusTransaction_2.sign(maker, false);
		issueStatusTransaction_2.process(db, false);
		LOGGER.info("status_2 KEY: " + status_2.getKey());
		issueStatusTransaction_2.orphan(db, false);
		ItemStatusMap statusMap = db.getItemStatusMap();
		int mapSize = statusMap.size();
		assertEquals(0, mapSize - 4);
		
		//CHECK STATUS IS CORRECT
		assertEquals(true, Arrays.equals(db.getItemStatusMap().get(key).toBytes(true), status.toBytes(true)));
					
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueStatusRecord.getSignature(), maker.getLastReference(db)));
	}
	
	
	@Test
	public void orphanIssueStatusTransaction()
	{
		
		init();				
				
		Status status = new Status(maker, "test", "strontje");
				
		//CREATE ISSUE STATUS TRANSACTION
		IssueStatusRecord issueStatusRecord = new IssueStatusRecord(maker, status, FEE_POWER, timestamp, maker.getLastReference(db));
		issueStatusRecord.sign(maker, false);
		issueStatusRecord.process(db, false);
		long key = db.getIssueStatusMap().get(issueStatusRecord);
		assertEquals(true, Arrays.equals(issueStatusRecord.getSignature(), maker.getLastReference(db)));
		
		issueStatusRecord.orphan(db, false);
				
		//CHECK STATUS EXISTS SENDER
		assertEquals(false, db.getItemStatusMap().contains(key));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueStatusRecord.getReference(), maker.getLastReference(db)));
	}
	
	// TODO - in statement - valid on key = 999
}
