package test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.util.ArrayList;
import java.util.Arrays;
//import java.util.List;
import java.util.logging.Logger;

import ntp.NTP;

import org.junit.Test;

//import com.google.common.primitives.Longs;

import database.DBSet;
import database.NoteMap;
import qora.account.PrivateKeyAccount;
import qora.notes.NoteCls;
import qora.notes.Note;
import qora.block.GenesisBlock;
import qora.crypto.Crypto;
//import qora.transaction.GenesisTransaction;
import qora.transaction.IssueNoteTransaction;
import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;

public class TrNoteTest {

	long OIL_KEY = 1l;
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
		maker.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), db);

	}
	
	
	//ISSUE NOTE TRANSACTION
	
	@Test
	public void validateSignatureIssueNoteTransaction() 
	{
		
		init();
		
		//CREATE NOTE
		Note note = new Note(maker, "test", "strontje");
				
		//CREATE ISSUE NOTE TRANSACTION
		Transaction issueNoteTransaction = new IssueNoteTransaction(maker, note, FEE_POWER, timestamp, maker.getLastReference(db));
		issueNoteTransaction.sign(maker);
		
		//CHECK IF ISSUE NOTE TRANSACTION IS VALID
		assertEquals(true, issueNoteTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		issueNoteTransaction = new IssueNoteTransaction(maker, note, FEE_POWER, timestamp, maker.getLastReference(db), new byte[64]);
		
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
		IssueNoteTransaction issueNoteTransaction = new IssueNoteTransaction(maker, note, FEE_POWER, timestamp, maker.getLastReference(db));
		issueNoteTransaction.sign(maker);
		issueNoteTransaction.process(db);
		
		//CONVERT TO BYTES
		byte[] rawIssueNoteTransaction = issueNoteTransaction.toBytes(true);
		
		//CHECK DATA LENGTH
		assertEquals(rawIssueNoteTransaction.length, issueNoteTransaction.getDataLength());
		
		try 
		{	
			//PARSE FROM BYTES
			IssueNoteTransaction parsedIssueNoteTransaction = (IssueNoteTransaction) TransactionFactory.getInstance().parse(rawIssueNoteTransaction);
			Logger.getGlobal().info("parsedIssueNoteTransaction: " + parsedIssueNoteTransaction);

			//CHECK INSTANCE
			assertEquals(true, parsedIssueNoteTransaction instanceof IssueNoteTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(issueNoteTransaction.getSignature(), parsedIssueNoteTransaction.getSignature()));
			
			//CHECK ISSUER
			assertEquals(issueNoteTransaction.getCreator().getAddress(), parsedIssueNoteTransaction.getCreator().getAddress());
			
			//CHECK OWNER
			assertEquals(issueNoteTransaction.getNote().getCreator().getAddress(), parsedIssueNoteTransaction.getNote().getCreator().getAddress());
			
			//CHECK NAME
			assertEquals(issueNoteTransaction.getNote().getName(), parsedIssueNoteTransaction.getNote().getName());
				
			//CHECK DESCRIPTION
			assertEquals(issueNoteTransaction.getNote().getDescription(), parsedIssueNoteTransaction.getNote().getDescription());
							
			//CHECK FEE
			assertEquals(issueNoteTransaction.getFee(), parsedIssueNoteTransaction.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(issueNoteTransaction.getReference(), parsedIssueNoteTransaction.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(issueNoteTransaction.getTimestamp(), parsedIssueNoteTransaction.getTimestamp());				
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
		IssueNoteTransaction issueNoteTransaction = new IssueNoteTransaction(maker, note, FEE_POWER, timestamp, maker.getLastReference(db));
		issueNoteTransaction.sign(maker);
		
		assertEquals(Transaction.VALIDATE_OK, issueNoteTransaction.isValid(db));
		
		issueNoteTransaction.process(db);
		
		Logger.getGlobal().info("note KEY: " + note.getKey(db));
				
		//CHECK NOTE EXISTS SENDER
		long key = db.getIssueNoteMap().get(issueNoteTransaction);
		assertEquals(true, db.getNoteMap().contains(key));
		
		NoteCls note_2 = new Note(maker, "test132_2", "2_12345678910strontje");				
		IssueNoteTransaction issueNoteTransaction_2 = new IssueNoteTransaction(maker, note_2, FEE_POWER, timestamp+10, maker.getLastReference(db));
		issueNoteTransaction_2.sign(maker);
		issueNoteTransaction_2.process(db);
		Logger.getGlobal().info("note_2 KEY: " + note_2.getKey(db));
		issueNoteTransaction_2.orphan(db);
		NoteMap noteMap = db.getNoteMap();
		int mapSize = noteMap.size();
		assertEquals(0, mapSize - 1);
		
		//CHECK NOTE IS CORRECT
		assertEquals(true, Arrays.equals(db.getNoteMap().get(key).toBytes(true), note.toBytes(true)));
					
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueNoteTransaction.getSignature(), maker.getLastReference(db)));
	}
	
	
	@Test
	public void orphanIssueNoteTransaction()
	{
		
		init();				
				
		Note note = new Note(maker, "test", "strontje");
				
		//CREATE ISSUE NOTE TRANSACTION
		IssueNoteTransaction issueNoteTransaction = new IssueNoteTransaction(maker, note, FEE_POWER, timestamp, maker.getLastReference(db));
		issueNoteTransaction.sign(maker);
		issueNoteTransaction.process(db);
		long key = db.getIssueNoteMap().get(issueNoteTransaction);
		assertEquals(true, Arrays.equals(issueNoteTransaction.getSignature(), maker.getLastReference(db)));
		
		issueNoteTransaction.orphan(db);
				
		//CHECK NOTE EXISTS SENDER
		assertEquals(false, db.getNoteMap().contains(key));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueNoteTransaction.getReference(), maker.getLastReference(db)));
	}
	
	
}
