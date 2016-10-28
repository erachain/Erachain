package test.records;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
//import java.math.BigInteger;
//import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import java.util.List;
 import org.apache.log4j.Logger;

import ntp.NTP;


import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.notes.Note;
import core.item.notes.NoteCls;
import core.transaction.IssueNoteRecord;
import core.transaction.R_SignNote;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;

import utils.Corekeys;
import webserver.WebResource;

//import com.google.common.primitives.Longs;

import database.DBSet;
import database.ItemNoteMap;

public class TestRecNote {

	static Logger LOGGER = Logger.getLogger(TestRecNote.class.getName());

	Long releaserReference = null;

	boolean asPack = false;
	long FEE_KEY = AssetCls.FEE_KEY;
	long VOTE_KEY = AssetCls.ERMO_KEY;
	byte FEE_POWER = (byte)1;
	byte[] noteReference = new byte[64];
	long timestamp = NTP.getTime();
	
	private byte[] icon = new byte[]{1,3,4,5,6,9}; // default value
	private byte[] image = new byte[]{4,11,32,23,45,122,11,-45}; // default value

	byte[] data = "test123!".getBytes();
	byte[] isText = new byte[] { 1 };
	byte[] encrypted = new byte[] { 0 };

	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
	NoteCls note;
	long noteKey = -1;
	IssueNoteRecord issueNoteRecord;
	R_SignNote signNoteRecord;

	
	ItemNoteMap noteMap;

	// INIT NOTES
	private void init() {
		
		db = DBSet.createEmptyDatabaseSet();
		noteMap = db.getItemNoteMap();
		
		gb = new GenesisBlock();
		gb.process(db);
		
		// FEE FUND
		maker.setLastReference(gb.getTimestamp(db), db);
		maker.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1).setScale(8));

	}
	private void initNote(boolean process) {
		
		note = new Note(maker, "test132", icon, image, "12345678910strontje");
				
		//CREATE ISSUE NOTE TRANSACTION
		issueNoteRecord = new IssueNoteRecord(maker, note, FEE_POWER, timestamp, maker.getLastReference(db));
		issueNoteRecord.sign(maker, false);
		if (process) {
			issueNoteRecord.process(db, gb, false);
			noteKey = note.getKey(db);
		}
	}
	
	@Test
	public void testAddreessVersion() 
	{
		int vers = Corekeys.findAddressVersion("E");
		assertEquals(-1111, vers);
	}
	
	//ISSUE NOTE TRANSACTION
	
	@Test
	public void validateSignatureIssueNoteTransaction() 
	{
		
		init();
		
		initNote(false);
		
		//CHECK IF ISSUE NOTE TRANSACTION IS VALID
		assertEquals(true, issueNoteRecord.isSignatureValid());
		
		//INVALID SIGNATURE
		issueNoteRecord = new IssueNoteRecord(maker, note, FEE_POWER, timestamp, maker.getLastReference(db), new byte[64]);
		
		//CHECK IF ISSUE NOTE IS INVALID
		assertEquals(false, issueNoteRecord.isSignatureValid());
	}
		

	
	@Test
	public void parseIssueNoteTransaction() 
	{
		
		init();
		
		NoteCls note = new Note(maker, "test132", icon, image, "12345678910strontje");
		byte[] raw = note.toBytes(false);
		assertEquals(raw.length, note.getDataLength(false));
				
		//CREATE ISSUE NOTE TRANSACTION
		IssueNoteRecord issueNoteRecord = new IssueNoteRecord(maker, note, FEE_POWER, timestamp, maker.getLastReference(db));
		issueNoteRecord.sign(maker, false);
		issueNoteRecord.process(db, gb, false);
		
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
			assertEquals(issueNoteRecord.getReference(), parsedIssueNoteTransaction.getReference());	
			
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
		
		Note note = new Note(maker, "test", icon, image, "strontje");
				
		//CREATE ISSUE NOTE TRANSACTION
		IssueNoteRecord issueNoteRecord = new IssueNoteRecord(maker, note, FEE_POWER, timestamp, maker.getLastReference(db));
		
		assertEquals(Transaction.VALIDATE_OK, issueNoteRecord.isValid(db, releaserReference));
		
		issueNoteRecord.sign(maker, false);
		issueNoteRecord.process(db, gb, false);
		int mapSize = noteMap.size();
		
		LOGGER.info("note KEY: " + note.getKey(db));
				
		//CHECK NOTE EXISTS SENDER
		long key = db.getIssueNoteMap().get(issueNoteRecord);
		assertEquals(true, noteMap.contains(key));
		
		NoteCls note_2 = new Note(maker, "test132_2", icon, image, "2_12345678910strontje");				
		IssueNoteRecord issueNoteTransaction_2 = new IssueNoteRecord(maker, note_2, FEE_POWER, timestamp+10, maker.getLastReference(db));
		issueNoteTransaction_2.sign(maker, false);
		issueNoteTransaction_2.process(db, gb, false);
		LOGGER.info("note_2 KEY: " + note_2.getKey(db));
		issueNoteTransaction_2.orphan(db, false);
		assertEquals(mapSize, noteMap.size());
		
		//CHECK NOTE IS CORRECT
		assertEquals(true, Arrays.equals(noteMap.get(key).toBytes(true), note.toBytes(true)));
					
		//CHECK REFERENCE SENDER
		assertEquals(issueNoteRecord.getTimestamp(), maker.getLastReference(db));
	}
	
	
	@Test
	public void orphanIssueNoteTransaction()
	{
		
		init();
				
		Note note = new Note(maker, "test", icon, image, "strontje");
				
		//CREATE ISSUE NOTE TRANSACTION
		IssueNoteRecord issueNoteRecord = new IssueNoteRecord(maker, note, FEE_POWER, timestamp, maker.getLastReference(db));
		issueNoteRecord.sign(maker, false);
		issueNoteRecord.process(db, gb, false);
		long key = db.getIssueNoteMap().get(issueNoteRecord);
		assertEquals(issueNoteRecord.getTimestamp(), maker.getLastReference(db));
		
		issueNoteRecord.orphan(db, false);
				
		//CHECK NOTE EXISTS SENDER
		assertEquals(false, noteMap.contains(key));
						
		//CHECK REFERENCE SENDER
		assertEquals(issueNoteRecord.getReference(), maker.getLastReference(db));
	}
	
	// TODO - in statement - valid on key = 999

	//SIGN NOTE TRANSACTION
	
	@Test
	public void validateSignatureSignNoteTransaction() 
	{
		
		init();
		
		initNote(true);
		
		signNoteRecord = new R_SignNote(maker, FEE_POWER, noteKey, data, isText, encrypted, timestamp+10, maker.getLastReference(db));
		signNoteRecord.sign(maker, asPack);
		
		//CHECK IF ISSUE NOTE TRANSACTION IS VALID
		assertEquals(true, signNoteRecord.isSignatureValid());
		
		//INVALID SIGNATURE
		signNoteRecord = new R_SignNote(maker, FEE_POWER, noteKey, data, isText, encrypted, timestamp+10, maker.getLastReference(db), new byte[64]);
		
		//CHECK IF ISSUE NOTE IS INVALID
		assertEquals(false, signNoteRecord.isSignatureValid());
	}
		

	
	@Test
	public void parseSignNoteTransaction() 
	{
		
		init();
		
		initNote(true);
		
		signNoteRecord = new R_SignNote(maker, FEE_POWER, noteKey, data, isText, encrypted, timestamp+10, maker.getLastReference(db));
		signNoteRecord.sign(maker, asPack);
		
		//CONVERT TO BYTES
		byte[] rawSignNoteRecord = signNoteRecord.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawSignNoteRecord.length, signNoteRecord.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			R_SignNote parsedSignNoteRecord = (R_SignNote) TransactionFactory.getInstance().parse(rawSignNoteRecord, releaserReference);
			LOGGER.info("parsedSignNote: " + parsedSignNoteRecord);

			//CHECK INSTANCE
			assertEquals(true, parsedSignNoteRecord instanceof R_SignNote);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(signNoteRecord.getSignature(), parsedSignNoteRecord.getSignature()));
			
			//CHECK ISSUER
			assertEquals(signNoteRecord.getCreator().getAddress(), parsedSignNoteRecord.getCreator().getAddress());
			
			//CHECK OWNER
			assertEquals(signNoteRecord.getKey(), parsedSignNoteRecord.getKey());
			
			//CHECK NAME
			assertEquals(true, Arrays.equals(signNoteRecord.getData(), parsedSignNoteRecord.getData()));
				
			//CHECK DESCRIPTION
			assertEquals(signNoteRecord.isText(), parsedSignNoteRecord.isText());
							
			//CHECK FEE
			assertEquals(signNoteRecord.getFee(), parsedSignNoteRecord.getFee());	
			
			//CHECK REFERENCE
			assertEquals(signNoteRecord.getReference(), parsedSignNoteRecord.getReference());	
			
			//CHECK TIMESTAMP
			assertEquals(signNoteRecord.getTimestamp(), parsedSignNoteRecord.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}

		
		// NOT DATA
		data = null;
		signNoteRecord = new R_SignNote(maker, FEE_POWER, noteKey, data, isText, encrypted, timestamp+20, maker.getLastReference(db));
		signNoteRecord.sign(maker, asPack);
		
		//CONVERT TO BYTES
		rawSignNoteRecord = signNoteRecord.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawSignNoteRecord.length, signNoteRecord.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			R_SignNote parsedSignNoteRecord = (R_SignNote) TransactionFactory.getInstance().parse(rawSignNoteRecord, releaserReference);
			LOGGER.info("parsedSignNote: " + parsedSignNoteRecord);

			//CHECK INSTANCE
			assertEquals(true, parsedSignNoteRecord instanceof R_SignNote);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(signNoteRecord.getSignature(), parsedSignNoteRecord.getSignature()));
			
			//CHECK ISSUER
			assertEquals(signNoteRecord.getCreator().getAddress(), parsedSignNoteRecord.getCreator().getAddress());
			
			//CHECK OWNER
			assertEquals(signNoteRecord.getKey(), parsedSignNoteRecord.getKey());
			
			//CHECK NAME
			assertEquals(null, parsedSignNoteRecord.getData());
				
			//CHECK DESCRIPTION
			assertEquals(signNoteRecord.isText(), parsedSignNoteRecord.isText());
							
			//CHECK FEE
			assertEquals(signNoteRecord.getFee(), parsedSignNoteRecord.getFee());	
			
			//CHECK REFERENCE
			assertEquals(signNoteRecord.getReference(), parsedSignNoteRecord.getReference());	
			
			//CHECK TIMESTAMP
			assertEquals(signNoteRecord.getTimestamp(), parsedSignNoteRecord.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}

		// NOT KEY
		//data = null;
		noteKey = 0;
		signNoteRecord = new R_SignNote(maker, FEE_POWER, noteKey, data, isText, encrypted, timestamp+20, maker.getLastReference(db));
		signNoteRecord.sign(maker, asPack);
		
		//CONVERT TO BYTES
		rawSignNoteRecord = signNoteRecord.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawSignNoteRecord.length, signNoteRecord.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			R_SignNote parsedSignNoteRecord = (R_SignNote) TransactionFactory.getInstance().parse(rawSignNoteRecord, releaserReference);
			LOGGER.info("parsedSignNote: " + parsedSignNoteRecord);

			//CHECK INSTANCE
			assertEquals(true, parsedSignNoteRecord instanceof R_SignNote);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(signNoteRecord.getSignature(), parsedSignNoteRecord.getSignature()));
			
			//CHECK ISSUER
			assertEquals(signNoteRecord.getCreator().getAddress(), parsedSignNoteRecord.getCreator().getAddress());
			
			//CHECK OWNER
			assertEquals(signNoteRecord.getKey(), parsedSignNoteRecord.getKey());
			
			//CHECK NAME
			assertEquals(null, parsedSignNoteRecord.getData());
				
			//CHECK DESCRIPTION
			assertEquals(signNoteRecord.isText(), parsedSignNoteRecord.isText());
							
			//CHECK FEE
			assertEquals(signNoteRecord.getFee(), parsedSignNoteRecord.getFee());	
			
			//CHECK REFERENCE
			assertEquals(signNoteRecord.getReference(), parsedSignNoteRecord.getReference());	
			
			//CHECK TIMESTAMP
			assertEquals(signNoteRecord.getTimestamp(), parsedSignNoteRecord.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}

		// NOT KEY
		data = null;
		noteKey = 0;
		signNoteRecord = new R_SignNote(maker, FEE_POWER, noteKey, data, isText, encrypted, timestamp+20, maker.getLastReference(db));
		signNoteRecord.sign(maker, asPack);
		
		//CONVERT TO BYTES
		rawSignNoteRecord = signNoteRecord.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawSignNoteRecord.length, signNoteRecord.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			R_SignNote parsedSignNoteRecord = (R_SignNote) TransactionFactory.getInstance().parse(rawSignNoteRecord, releaserReference);
			LOGGER.info("parsedSignNote: " + parsedSignNoteRecord);

			//CHECK INSTANCE
			assertEquals(true, parsedSignNoteRecord instanceof R_SignNote);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(signNoteRecord.getSignature(), parsedSignNoteRecord.getSignature()));
			
			//CHECK ISSUER
			assertEquals(signNoteRecord.getCreator().getAddress(), parsedSignNoteRecord.getCreator().getAddress());
			
			//CHECK OWNER
			assertEquals(signNoteRecord.getKey(), parsedSignNoteRecord.getKey());
			
			//CHECK NAME
			assertEquals(null, parsedSignNoteRecord.getData());
				
			//CHECK DESCRIPTION
			assertEquals(signNoteRecord.isText(), parsedSignNoteRecord.isText());
							
			//CHECK FEE
			assertEquals(signNoteRecord.getFee(), parsedSignNoteRecord.getFee());	
			
			//CHECK REFERENCE
			assertEquals(signNoteRecord.getReference(), parsedSignNoteRecord.getReference());	
			
			//CHECK TIMESTAMP
			assertEquals(signNoteRecord.getTimestamp(), parsedSignNoteRecord.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}

	}

	
	@Test
	public void processSignNoteTransaction()
	{
		
		init();
		
		initNote(true);
		
		signNoteRecord = new R_SignNote(maker, FEE_POWER, noteKey, data, isText, encrypted, timestamp+10, maker.getLastReference(db));
		
		assertEquals(Transaction.VALIDATE_OK, signNoteRecord.isValid(db, releaserReference));
		
		signNoteRecord.sign(maker, false);
		signNoteRecord.process(db, gb, false);
							
		//CHECK REFERENCE SENDER
		assertEquals(signNoteRecord.getTimestamp(), maker.getLastReference(db));	
			
		///// ORPHAN
		signNoteRecord.orphan(db, false);
										
		//CHECK REFERENCE SENDER
		assertEquals(signNoteRecord.getReference(), maker.getLastReference(db));
	}

	private List<String> imagelinks = new ArrayList<String>();

	private void handleVars(String description) {
		Pattern pattern = Pattern.compile(Pattern.quote("{{") + "(.+?)" + Pattern.quote("}}"));
		//Pattern pattern = Pattern.compile("{{(.+)}}");
		Matcher matcher = pattern.matcher(description);
		while (matcher.find()) {
			String url = matcher.group(1);
			imagelinks.add(url);
			//description = description.replace(matcher.group(), getImgHtml(url));
		}
	}
	@Test
	public void regExTest()
	{
		String descr = "AJH {{wer}}, asdj {{we431!12}}";
		handleVars(descr);
		assertEquals(imagelinks, "");
	}


}
