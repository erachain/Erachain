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

//import com.google.common.primitives.Longs;

import database.DBSet;
import database.ItemNoteMap;
import qora.account.PrivateKeyAccount;
import qora.item.notes.NoteCls;
import qora.item.notes.Note;
import qora.block.GenesisBlock;
import qora.crypto.Crypto;
//import qora.transaction.GenesisTransaction;
import qora.transaction.IssueNoteRecord;
import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;

public class TrNoteTestAsPack {

	static Logger LOGGER = Logger.getLogger(TrNoteTestAsPack.class.getName());

	byte[] releaserReference = null;

	boolean asPack = true;
	boolean includeReference = false;
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
		Transaction issueNoteTransaction = new IssueNoteRecord(maker, note);
		issueNoteTransaction.sign(maker, asPack);
		
		//CHECK IF ISSUE NOTE TRANSACTION IS VALID
		assertEquals(true, issueNoteTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		issueNoteTransaction = new IssueNoteRecord(maker, note, new byte[64]);
		
		//CHECK IF ISSUE NOTE IS INVALID
		assertEquals(false, issueNoteTransaction.isSignatureValid());
	}
		

	
	@Test
	public void parseIssueNoteTransaction() 
	{
		
		init();
		
		NoteCls note = new Note(maker, "test132", "12345678910strontje");
		byte[] raw = note.toBytes(includeReference);
		assertEquals(raw.length, note.getDataLength(includeReference));
				
		//CREATE ISSUE NOTE TRANSACTION
		IssueNoteRecord issueNoteRecord = new IssueNoteRecord(maker, note);
		issueNoteRecord.sign(maker, asPack);
		issueNoteRecord.process(db, asPack);
		
		//CONVERT TO BYTES
		byte[] rawIssueNoteTransaction = issueNoteRecord.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawIssueNoteTransaction.length, issueNoteRecord.getDataLength(asPack));
		
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
		IssueNoteRecord issueNoteRecord = new IssueNoteRecord(maker, note);
		issueNoteRecord.sign(maker, asPack);
		
		assertEquals(Transaction.VALIDATE_OK, issueNoteRecord.isValid(db, releaserReference));
		byte[] makerReference = maker.getLastReference(db);
		issueNoteRecord.process(db, asPack);
		
		LOGGER.info("note KEY: " + note.getKey(db));
				
		//CHECK NOTE EXISTS SENDER
		long key = db.getIssueNoteMap().get(issueNoteRecord);
		assertEquals(true, db.getNoteMap().contains(key));
		
		NoteCls note_2 = new Note(maker, "test132_2", "2_12345678910strontje");				
		IssueNoteRecord issueNoteTransaction_2 = new IssueNoteRecord(maker, note_2);
		issueNoteTransaction_2.sign(maker, asPack);
		issueNoteTransaction_2.process(db, asPack);
		LOGGER.info("note_2 KEY: " + note_2.getKey(db));
		issueNoteTransaction_2.orphan(db, asPack);
		ItemNoteMap noteMap = db.getNoteMap();
		int mapSize = noteMap.size();
		assertEquals(0, mapSize - 4);
		
		//CHECK NOTE IS CORRECT
		assertEquals(true, Arrays.equals(db.getNoteMap().get(key).toBytes(includeReference), note.toBytes(includeReference)));
					
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(makerReference, maker.getLastReference(db)));
	}
	
	
	@Test
	public void orphanIssueNoteTransaction()
	{
		
		init();				
				
		Note note = new Note(maker, "test", "strontje");
		byte[] makerReference = maker.getLastReference(db);
				
		//CREATE ISSUE NOTE TRANSACTION
		IssueNoteRecord issueNoteRecord = new IssueNoteRecord(maker, note);
		issueNoteRecord.sign(maker, asPack);
		issueNoteRecord.process(db, asPack);
		long key = db.getIssueNoteMap().get(issueNoteRecord);
		assertEquals(true, Arrays.equals(makerReference, maker.getLastReference(db)));
		
		issueNoteRecord.orphan(db, asPack);
				
		//CHECK NOTE EXISTS SENDER
		assertEquals(false, db.getNoteMap().contains(key));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(makerReference, maker.getLastReference(db)));
	}
	
	
}
