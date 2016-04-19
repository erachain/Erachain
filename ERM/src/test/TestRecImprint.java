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
import core.item.imprints.Imprint;
import core.item.imprints.ImprintCls;
import core.transaction.IssueImprintRecord;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;

//import com.google.common.primitives.Longs;

import database.DBSet;
import database.ItemImprintMap;

public class TestRecImprint {

	static Logger LOGGER = Logger.getLogger(TestRecImprint.class.getName());

	byte[] releaserReference = null;

	boolean asPack = false;
	long FEE_KEY = Transaction.LAEV_KEY;
	byte FEE_POWER = (byte)1;
	byte[] imprintReference = new byte[64];
	long timestamp = NTP.getTime();
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
	

	// INIT IMPRINTS
	private void init() {
		
		db = DBSet.createEmptyDatabaseSet();
		gb = new GenesisBlock();
		gb.process(db);
		
		// OIL FUND
		maker.setLastReference(gb.getGeneratorSignature(), db);
		maker.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);

	}
	
	
	//ISSUE IMPRINT TRANSACTION
	
	@Test
	public void validateSignatureIssueImprintTransaction() 
	{
		
		init();
		
		//CREATE IMPRINT
		Imprint imprint = new Imprint(maker, "test12345678", "strontje");
				
		//CREATE ISSUE IMPRINT TRANSACTION
		Transaction issueImprintTransaction = new IssueImprintRecord(maker, imprint, FEE_POWER, timestamp, maker.getLastReference(db));
		issueImprintTransaction.sign(maker, false);
		
		//CHECK IF ISSUE IMPRINT TRANSACTION IS VALID
		assertEquals(true, issueImprintTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		issueImprintTransaction = new IssueImprintRecord(maker, imprint, FEE_POWER, timestamp, maker.getLastReference(db), new byte[64]);
		
		//CHECK IF ISSUE IMPRINT IS INVALID
		assertEquals(false, issueImprintTransaction.isSignatureValid());
	}
		

	
	@Test
	public void parseIssueImprintTransaction() 
	{
		
		init();
		
		ImprintCls imprint = new Imprint(maker, "tesdfwerwrest132", "12345678910strontje");
		byte[] raw = imprint.toBytes(false);
		assertEquals(raw.length, imprint.getDataLength(false));
				
		//CREATE ISSUE IMPRINT TRANSACTION
		IssueImprintRecord issueImprintRecord = new IssueImprintRecord(maker, imprint, FEE_POWER, timestamp, maker.getLastReference(db));
		issueImprintRecord.sign(maker, false);
		issueImprintRecord.process(db, false);
		
		//CONVERT TO BYTES
		byte[] rawIssueImprintTransaction = issueImprintRecord.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawIssueImprintTransaction.length, issueImprintRecord.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			IssueImprintRecord parsedIssueImprintTransaction = (IssueImprintRecord) TransactionFactory.getInstance().parse(rawIssueImprintTransaction, releaserReference);
			LOGGER.info("parsedIssueImprintTransaction: " + parsedIssueImprintTransaction);

			//CHECK INSTANCE
			assertEquals(true, parsedIssueImprintTransaction instanceof IssueImprintRecord);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(issueImprintRecord.getSignature(), parsedIssueImprintTransaction.getSignature()));
			
			//CHECK ISSUER
			assertEquals(issueImprintRecord.getCreator().getAddress(), parsedIssueImprintTransaction.getCreator().getAddress());
			
			//CHECK OWNER
			assertEquals(issueImprintRecord.getItem().getCreator().getAddress(), parsedIssueImprintTransaction.getItem().getCreator().getAddress());
			
			//CHECK NAME
			assertEquals(issueImprintRecord.getItem().getName(), parsedIssueImprintTransaction.getItem().getName());
				
			//CHECK DESCRIPTION
			assertEquals(issueImprintRecord.getItem().getDescription(), parsedIssueImprintTransaction.getItem().getDescription());
							
			//CHECK FEE
			assertEquals(issueImprintRecord.getFee(), parsedIssueImprintTransaction.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(issueImprintRecord.getReference(), parsedIssueImprintTransaction.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(issueImprintRecord.getTimestamp(), parsedIssueImprintTransaction.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}
		
	}

	
	@Test
	public void processIssueImprintTransaction()
	{
		
		init();				
		
		Imprint imprint = new Imprint(maker, "test", "strontje");
				
		//CREATE ISSUE IMPRINT TRANSACTION
		IssueImprintRecord issueImprintRecord = new IssueImprintRecord(maker, imprint, FEE_POWER, timestamp, maker.getLastReference(db));
		issueImprintRecord.sign(maker, false);
		
		assertEquals(Transaction.VALIDATE_OK, issueImprintRecord.isValid(db, releaserReference));
		
		issueImprintRecord.process(db, false);
		
		LOGGER.info("imprint KEY: " + imprint.getKey(db));
				
		//CHECK IMPRINT EXISTS SENDER
		long key = db.getIssueImprintMap().get(issueImprintRecord);
		assertEquals(true, db.getImprintMap().contains(key));
		
		ImprintCls imprint_2 = new Imprint(maker, "test132_2", "2_12345678910strontje");				
		IssueImprintRecord issueImprintTransaction_2 = new IssueImprintRecord(maker, imprint_2, FEE_POWER, timestamp+10, maker.getLastReference(db));
		issueImprintTransaction_2.sign(maker, false);
		issueImprintTransaction_2.process(db, false);
		LOGGER.info("imprint_2 KEY: " + imprint_2.getKey(db));
		issueImprintTransaction_2.orphan(db, false);
		ItemImprintMap imprintMap = db.getImprintMap();
		int mapSize = imprintMap.size();
		assertEquals(0, mapSize - 4);
		
		//CHECK IMPRINT IS CORRECT
		assertEquals(true, Arrays.equals(db.getImprintMap().get(key).toBytes(true), imprint.toBytes(true)));
					
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueImprintRecord.getSignature(), maker.getLastReference(db)));
	}
	
	
	@Test
	public void orphanIssueImprintTransaction()
	{
		
		init();				
				
		Imprint imprint = new Imprint(maker, "test", "strontje");
				
		//CREATE ISSUE IMPRINT TRANSACTION
		IssueImprintRecord issueImprintRecord = new IssueImprintRecord(maker, imprint, FEE_POWER, timestamp, maker.getLastReference(db));
		issueImprintRecord.sign(maker, false);
		issueImprintRecord.process(db, false);
		long key = db.getIssueImprintMap().get(issueImprintRecord);
		assertEquals(true, Arrays.equals(issueImprintRecord.getSignature(), maker.getLastReference(db)));
		
		issueImprintRecord.orphan(db, false);
				
		//CHECK IMPRINT EXISTS SENDER
		assertEquals(false, db.getImprintMap().contains(key));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueImprintRecord.getReference(), maker.getLastReference(db)));
	}
	
	// TODO - in statement - valid on key = 999
}
