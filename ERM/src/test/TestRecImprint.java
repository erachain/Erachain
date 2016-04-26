package test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
//import java.math.BigInteger;
//import java.util.ArrayList;
import java.util.Arrays;
//import java.util.List;
 import org.apache.log4j.Logger;

import ntp.NTP;

import org.junit.Test;

import com.google.common.primitives.Bytes;

import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
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
	long FEE_KEY = AssetCls.DILE_KEY;
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
	
	String name_total = "123890TYRH76576567567tytryrtyr61fdhgdfdskdfhuiweyriusdfyf8s7fssudfgdytrttygd";
	byte[] digest;

	Imprint imprint;

	// INIT IMPRINTS
	private void init() {

		name_total = Imprint.hashNameToBase58(name_total);
		digest = Base58.decode(name_total);

		db = DBSet.createEmptyDatabaseSet();
		gb = new GenesisBlock();
		gb.process(db);
		
		// OIL FUND
		maker.setLastReference(gb.getGeneratorSignature(), db);
		maker.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);

		imprint = new Imprint(maker, name_total, "");

	}
	
	
	//ISSUE IMPRINT TRANSACTION
	
	@Test
	public void validateSignatureIssueImprintTransaction() 
	{
		
		init();
		
		byte[] reference = imprint.getCuttedReference();
		assertEquals(true, Arrays.equals(digest, reference));
		assertEquals(name_total, Base58.encode(reference));
				
		//CREATE ISSUE IMPRINT TRANSACTION
		IssueImprintRecord issueImprintTransaction = new IssueImprintRecord(maker, imprint, FEE_POWER, timestamp, maker.getLastReference(db));
		issueImprintTransaction.sign(maker, false);
		
		//CHECK IF ISSUE IMPRINT TRANSACTION IS VALID
		assertEquals(true, issueImprintTransaction.isSignatureValid());

		// CHECK REFERENCE OF ITEM NOT CHANGED
		Imprint impr_1 = (Imprint) issueImprintTransaction.getItem();
		assertEquals(name_total, Base58.encode(impr_1.getCuttedReference()));
		
		//INVALID SIGNATURE
		issueImprintTransaction = new IssueImprintRecord(maker, imprint, FEE_POWER, timestamp, maker.getLastReference(db), new byte[64]);
		
		//CHECK IF ISSUE IMPRINT IS INVALID
		assertEquals(false, issueImprintTransaction.isSignatureValid());
	}
		

	
	@Test
	public void parseIssueImprintTransaction() 
	{
		
		init();
		
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
						
		//CREATE ISSUE IMPRINT TRANSACTION
		IssueImprintRecord issueImprintRecord = new IssueImprintRecord(maker, imprint, FEE_POWER, timestamp, maker.getLastReference(db));
		assertEquals(issueImprintRecord.getItem().getName(), Base58.encode(imprint.getCuttedReference()));
		issueImprintRecord.sign(maker, false);
		
		assertEquals(Transaction.VALIDATE_OK, issueImprintRecord.isValid(db, releaserReference));
		
		issueImprintRecord.process(db, false);
		
		LOGGER.info("imprint KEY: " + imprint.getKey(db));
				
		//CHECK IMPRINT EXISTS SENDER
		///////// NOT FONT THROUGHT db.get(issueImprintRecord)
		//long key = db.getIssueImprintMap().get(issueImprintRecord);
		long key = issueImprintRecord.getItem().getKey();
		assertEquals(true, db.getItemImprintMap().contains(key));
		
		ImprintCls imprint_2 = new Imprint(maker, Imprint.hashNameToBase58("test132_2"), "e");				
		IssueImprintRecord issueImprintTransaction_2 = new IssueImprintRecord(maker, imprint_2, FEE_POWER, timestamp+10, maker.getLastReference(db));
		issueImprintTransaction_2.sign(maker, false);
		issueImprintTransaction_2.process(db, false);
		LOGGER.info("imprint_2 KEY: " + imprint_2.getKey(db));
		issueImprintTransaction_2.orphan(db, false);
		ItemImprintMap imprintMap = db.getItemImprintMap();
		int mapSize = imprintMap.size();
		assertEquals(0, mapSize - 1);
		
		//CHECK IMPRINT IS CORRECT
		assertEquals(true, Arrays.equals(db.getItemImprintMap().get(key).toBytes(true), imprint.toBytes(true)));
					
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueImprintRecord.getSignature(), maker.getLastReference(db)));
	}
	
	
	@Test
	public void orphanIssueImprintTransaction()
	{
		
		init();				
				
		//CREATE ISSUE IMPRINT TRANSACTION
		IssueImprintRecord issueImprintRecord = new IssueImprintRecord(maker, imprint, FEE_POWER, timestamp, maker.getLastReference(db));
		issueImprintRecord.sign(maker, false);
		issueImprintRecord.process(db, false);
		long key = db.getIssueImprintMap().get(issueImprintRecord);
		assertEquals(true, Arrays.equals(issueImprintRecord.getSignature(), maker.getLastReference(db)));
		
		issueImprintRecord.orphan(db, false);
				
		//CHECK IMPRINT EXISTS SENDER
		assertEquals(false, db.getItemImprintMap().contains(key));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueImprintRecord.getReference(), maker.getLastReference(db)));
	}
	
	// TODO - in statement - valid on key = 999
}
