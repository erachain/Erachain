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
import core.transaction.R_Hashes;
import core.transaction.R_SignNote;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;

import utils.Corekeys;
import webserver.WebResource;

//import com.google.common.primitives.Longs;

import database.DBSet;
import database.ItemNoteMap;

public class TestRecHash {

	Long releaserReference = null;

	boolean asPack = false;
	long FEE_KEY = AssetCls.FEE_KEY;
	long VOTE_KEY = AssetCls.ERMO_KEY;
	byte FEE_POWER = (byte)1;
	long timestamp = NTP.getTime();
	
	byte[] url = "http://sdsdf.com/dfgr/1".getBytes();
	byte[] data = "test123!".getBytes();

	byte[][] hashes = new byte[12][32];

	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
	R_Hashes hashesRecord;

	// INIT
	private void init() {
		
		db = DBSet.createEmptyDatabaseSet();
		
		gb = new GenesisBlock();
		gb.process(db);
		
		// FEE FUND
		maker.setLastReference(gb.getTimestamp(db), db);
		maker.setBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);

	}
	
	//
	
	@Test
	public void validateSignature() 
	{
		
		init();
		
		hashesRecord = new R_Hashes(maker, FEE_POWER, url, data, hashes, timestamp+10, maker.getLastReference(db));
		hashesRecord.sign(maker, asPack);
		
		//CHECK IF ISSUE NOTE TRANSACTION IS VALID
		assertEquals(true, hashesRecord.isSignatureValid());
		
		//INVALID SIGNATURE
		hashesRecord = new R_Hashes(maker, FEE_POWER, url, data, hashes, timestamp+10, maker.getLastReference(db), new byte[64]);
		
		//CHECK IF ISSUE NOTE IS INVALID
		assertEquals(false, hashesRecord.isSignatureValid());
	}
		

	
	@Test
	public void parse() 
	{
		
		init();
		
		
		hashesRecord = new R_Hashes(maker, FEE_POWER, url, data, hashes, timestamp+10, maker.getLastReference(db));
		hashesRecord.sign(maker, asPack);
		
		//CONVERT TO BYTES
		byte[] rawHashesRecord = hashesRecord.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawHashesRecord.length, hashesRecord.getDataLength(false));
		
		R_Hashes parsed = null;
		try 
		{	
			//PARSE FROM BYTES
			parsed = (R_Hashes) TransactionFactory.getInstance().parse(rawHashesRecord, releaserReference);
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}

		//CHECK INSTANCE
		assertEquals(true, parsed instanceof R_Hashes);
		
		//CHECK SIGNATURE
		assertEquals(true, Arrays.equals(hashesRecord.getSignature(), parsed.getSignature()));
		
		//CHECK ISSUER
		assertEquals(hashesRecord.getCreator().getAddress(), parsed.getCreator().getAddress());
					
		//CHECK NAME
		assertEquals(true, Arrays.equals(hashesRecord.getData(), parsed.getData()));
										
		//CHECK FEE
		assertEquals(hashesRecord.getFee(), parsed.getFee());	
		
		//CHECK REFERENCE
		assertEquals(hashesRecord.getReference(), parsed.getReference());	
		
		//CHECK TIMESTAMP
		assertEquals(hashesRecord.getTimestamp(), parsed.getTimestamp());				

		//CHECK HASHES
		assertEquals(true, Arrays.equals(hashesRecord.getHashes(), parsed.getHashes()));

	}

	
	@Test
	public void process()
	{
		
		init();
		
		
		hashesRecord = new R_Hashes(maker, FEE_POWER, url, data, hashes, timestamp+10, maker.getLastReference(db));
		
		assertEquals(Transaction.VALIDATE_OK, hashesRecord.isValid(db, releaserReference));
		
		hashesRecord.sign(maker, false);
		hashesRecord.process(db, gb, false);
							
		//CHECK REFERENCE SENDER
		assertEquals(hashesRecord.getTimestamp(), maker.getLastReference(db));	
			
		///// ORPHAN
		hashesRecord.orphan(db, false);
										
		//CHECK REFERENCE SENDER
		assertEquals(hashesRecord.getReference(), maker.getLastReference(db));
	}


}
