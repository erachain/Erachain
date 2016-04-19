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
import core.item.notes.Note;
import core.item.notes.NoteCls;
import core.transaction.IssueNoteRecord;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;

//import com.google.common.primitives.Longs;

import database.DBSet;
import database.ItemNoteMap;

public class TestRecNote {

	static Logger LOGGER = Logger.getLogger(TestRecNote.class.getName());

	byte[] releaserReference = null;

	boolean asPack = false;
	long FEE_KEY = Transaction.DIL_KEY;
	byte FEE_POWER = (byte)1;
	byte[] noteReference = new byte[64];
	long timestamp = NTP.getTime();
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
	

	// INIT NOTES
	private void init() {
		
		db = DBSet.createEmptyDatabaseSet();
		gb = new GenesisBlock();
		gb.process(db);
		
		// OIL FUND
		maker.setLastReference(gb.getGeneratorSignature(), db);
		maker.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);

	}
	
	
	//ISSUE NOTE TRANSACTION
	
	@Test
	public void validateSignatureIssueNoteTransaction() 
	{
		
		init();
		
		//CREATE NOTE
		Note note = new Note(maker, "test", "strontje");
				
		//CREATE ISSUE NOTE TRANSACTION
		Transaction issueNoteTransaction = new IssueNoteRecord(maker, note, FEE_POWER, timestamp, maker.getLastReference(db));
		issueNoteTransaction.sign(maker, false);
		
		//CHECK IF ISSUE NOTE TRANSACTION IS VALID
		assertEquals(true, issueNoteTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		issueNoteTransaction = new IssueNoteRecord(maker, note, FEE_POWER, timestamp, maker.getLastReference(db), new byte[64]);
		
		//CHECK IF ISSUE NOTE IS INVALID
		assertEquals(false, issueNoteTransaction.isSignatureValid());
	}
		

	
	@Test
	public void parseIssueNoteTransaction() 
	{
		
		init();
		
		NoteCls note = new Note(maker, "test132", "12345678910strontje");
		byte[] raw = note.toBytes(false);
		assertEquals(raw.length, note.getDataLength(false));
				
		//CREATE ISSUE NOTE TRANSACTION
		IssueNoteRecord issueNoteRecord = new IssueNoteRecord(maker, note, FEE_POWER, timestamp, maker.getLastReference(db));
		issueNoteRecord.sign(maker, false);
		issueNoteRecord.process(db, false);
		
		//CONVERT TO BYTES
		byte[] rawIssueNoteTransaction = issueNoteRecord.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawIssueNoteTransaction.length, issueNoteRecord.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			IssueNoteRecord parsedIssueNoteTransaction = (IssueNoteRecord) TransactionFactory.getInstance().parse(rawIssueNoteTransaction, releaserReference);
			LOGGER.info("parsedIssueNoteTransaction: " + parsedIssueNoteTransaction);

			//CHECK INSTANCE
			assertEquals(true, parsedIssueNoteTransaction instanceof IssueNoteRecord);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(issueNoteRecord.getSignature(), parsedIssueNoteTransaction.getSignature()));
			
			//CHECK ISSUER
			assertEquals(issueNoteRecord.getCreator().getAddress(), parsedIssueNoteTransaction.getCreator().getAddress());
			
			//CHECK OWNER
			assertEquals(issueNoteRecord.getItem().getCreator().getAddress(), parsedIssueNoteTransaction.getItem().getCreator().getAddress());
			
			//CHECK NAME
			assertEquals(issueNoteRecord.getItem().getName(), parsedIssueNoteTransaction.getItem().getName());
				
			//CHECK DESCRIPTION
			assertEquals(issueNoteRecord.getItem().getDescription(), parsedIssueNoteTransaction.getItem().getDescription());
							
			//CHECK FEE
			assertEquals(issueNoteRecord.getFee(), parsedIssueNoteTransaction.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(issueNoteRecord.getReference(), parsedIssueNoteTransaction.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(issueNoteRecord.getTimestamp(), parsedIssueNoteTransaction.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}
		
	}

	
	@Test
	public void processIssueNoteTransaction()
	{
		
		init();				
		
		Note note = new Note(maker, "test", "strontje");
				
		//CREATE ISSUE NOTE TRANSACTION
		IssueNoteRecord issueNoteRecord = new IssueNoteRecord(maker, note, FEE_POWER, timestamp, maker.getLastReference(db));
		
		assertEquals(Transaction.VALIDATE_OK, issueNoteRecord.isValid(db, releaserReference));
		
		issueNoteRecord.sign(maker, false);
		issueNoteRecord.process(db, false);
		
		LOGGER.info("note KEY: " + note.getKey(db));
				
		//CHECK NOTE EXISTS SENDER
		long key = db.getIssueNoteMap().get(issueNoteRecord);
		assertEquals(true, db.getNoteMap().contains(key));
		
		NoteCls note_2 = new Note(maker, "test132_2", "2_12345678910strontje");				
		IssueNoteRecord issueNoteTransaction_2 = new IssueNoteRecord(maker, note_2, FEE_POWER, timestamp+10, maker.getLastReference(db));
		issueNoteTransaction_2.sign(maker, false);
		issueNoteTransaction_2.process(db, false);
		LOGGER.info("note_2 KEY: " + note_2.getKey(db));
		issueNoteTransaction_2.orphan(db, false);
		ItemNoteMap noteMap = db.getNoteMap();
		int mapSize = noteMap.size();
		assertEquals(0, mapSize - 4);
		
		//CHECK NOTE IS CORRECT
		assertEquals(true, Arrays.equals(db.getNoteMap().get(key).toBytes(true), note.toBytes(true)));
					
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueNoteRecord.getSignature(), maker.getLastReference(db)));
	}
	
	
	@Test
	public void orphanIssueNoteTransaction()
	{
		
		init();				
				
		Note note = new Note(maker, "test", "strontje");
				
		//CREATE ISSUE NOTE TRANSACTION
		IssueNoteRecord issueNoteRecord = new IssueNoteRecord(maker, note, FEE_POWER, timestamp, maker.getLastReference(db));
		issueNoteRecord.sign(maker, false);
		issueNoteRecord.process(db, false);
		long key = db.getIssueNoteMap().get(issueNoteRecord);
		assertEquals(true, Arrays.equals(issueNoteRecord.getSignature(), maker.getLastReference(db)));
		
		issueNoteRecord.orphan(db, false);
				
		//CHECK NOTE EXISTS SENDER
		assertEquals(false, db.getNoteMap().contains(key));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueNoteRecord.getReference(), maker.getLastReference(db)));
	}
	
	// TODO - in statement - valid on key = 999
}
