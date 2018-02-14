package test.records;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
 import org.apache.log4j.Logger;

import ntp.NTP;
import settings.Settings;

import org.junit.Test;
import org.mapdb.Fun.Tuple2;

import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.BlockGenerator;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.assets.AssetUnique;
import core.item.assets.AssetVenture;
import core.transaction.CancelOrderTransaction;
import core.transaction.CreateOrderTransaction;
import core.transaction.R_Vouch;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import datachain.DCSet;
import core.transaction.R_Send;
import core.transaction.R_Send;

public class TestRec_Vouch {

	static Logger LOGGER = Logger.getLogger(TestRec_Vouch.class.getName());

	Long releaserReference = null;

	long ERM_KEY = AssetCls.ERA_KEY;
	long FEE_KEY = AssetCls.FEE_KEY;
	byte FEE_POWER = (byte)0;
	byte[] assetReference = new byte[64];
	long timestamp = NTP.getTime();
	
	int height = 1;
	int seq = 3;
	//CREATE EMPTY MEMORY DATABASE
	private DCSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("tes213sdffsdft".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
	
	// INIT ASSETS
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
		maker.changeBalance(db, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(8), false);
		maker.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1).setScale(8), false);
		
	}
	
	
	//ISSUE ASSET TRANSACTION
	
	@Test
	public void validateSignature_R_Vouch() 
	{
		
		init();
						
		//CREATE VOUCH RECORD
		Transaction vouchRecord = new R_Vouch(maker, FEE_POWER,  height, seq, timestamp, maker.getLastTimestamp(db));
		vouchRecord.sign(maker, false);
		
		//CHECK IF TRANSACTION IS VALID
		assertEquals(true, vouchRecord.isSignatureValid(db));
		
		//INVALID SIGNATURE
		vouchRecord = new R_Vouch(maker, FEE_POWER, height, seq, timestamp, maker.getLastTimestamp(db), new byte[64]);
		
		//CHECK IF VOUCH IS INVALID
		assertEquals(false, vouchRecord.isSignatureValid(db));
	}
		
	@Test
	public void validate_R_Vouch() 
	{
		
		init();
						
		//CREATE VOUCH RECORD
		Transaction vouchRecord = new R_Vouch(maker, FEE_POWER,  height, seq, timestamp, maker.getLastTimestamp(db));		
		assertEquals(Transaction.VALIDATE_OK, vouchRecord.isValid(db, releaserReference));
		
		vouchRecord = new R_Vouch(maker, FEE_POWER, -1, seq, timestamp, maker.getLastTimestamp(db), new byte[64]);		
		assertEquals(Transaction.INVALID_BLOCK_HEIGHT, vouchRecord.isValid(db, releaserReference));

		// SET <2 in isValid()
		vouchRecord = new R_Vouch(maker, FEE_POWER, 1, -1, timestamp, maker.getLastTimestamp(db), new byte[64]);		
		assertEquals(Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR, vouchRecord.isValid(db, releaserReference));

		vouchRecord = new R_Vouch(maker, FEE_POWER, 99, 1, timestamp, maker.getLastTimestamp(db), new byte[64]);		
		assertEquals(Transaction.INVALID_BLOCK_HEIGHT, vouchRecord.isValid(db, releaserReference));

		vouchRecord = new R_Vouch(maker, FEE_POWER, 1, 88, timestamp, maker.getLastTimestamp(db), new byte[64]);		
		assertEquals(Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR, vouchRecord.isValid(db, releaserReference));
}

	
	@Test
	public void parseR_Vouch() 
	{
		
		init();
						
		//CREATE ISSUE ASSET TRANSACTION
		R_Vouch vouchRecord = new R_Vouch(maker,  FEE_POWER, height, seq, timestamp, maker.getLastTimestamp(db));
		vouchRecord.sign(maker, false);
		
		//CONVERT TO BYTES
		byte[] rawR_Vouch = vouchRecord.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawR_Vouch.length, vouchRecord.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			R_Vouch parsedR_Vouch = (R_Vouch) TransactionFactory.getInstance().parse(rawR_Vouch, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedR_Vouch instanceof R_Vouch);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(vouchRecord.getSignature(), parsedR_Vouch.getSignature()));
			
			//CHECK ISSUER
			assertEquals(vouchRecord.getCreator().getAddress(), parsedR_Vouch.getCreator().getAddress());
			
			//CHECK HEIGHT
			assertEquals(vouchRecord.getVouchHeight(), parsedR_Vouch.getVouchHeight());
			
			//CHECK SEQno
			assertEquals(vouchRecord.getVouchSeq(), parsedR_Vouch.getVouchSeq());
							
			//CHECK FEE
			assertEquals(vouchRecord.getFee(), parsedR_Vouch.getFee());	
			
			//CHECK REFERENCE
			//assertEquals((long)vouchRecord.getReference(), (long)parsedR_Vouch.getReference());	
			
			//CHECK TIMESTAMP
			assertEquals(vouchRecord.getTimestamp(), parsedR_Vouch.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawR_Vouch = new byte[vouchRecord.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawR_Vouch, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	
	@Test
	public void processR_Vouch()
	{
		
		init();				
		
		R_Vouch vouchRecord = new R_Vouch(maker,  FEE_POWER, height, seq, timestamp, maker.getLastTimestamp(db));
		//vouchRecord.sign(maker, false);
		
		//assertEquals(Transaction.VALIDATE_OK, vouchRecord.isValid(db, releaserReference));
		
		/*
		Block block = new Block(1, gb.getReference(), gb.getTimestamp() + 1000,
				BlockGenerator.getNextBlockGeneratingBalance(db, gb), maker,
				//BlockGenerator.calculateSignature(db, gb, maker)
				new byte[]{}
				);
		
		block.addTransaction(vouchRecord);
		block.process(db);
		*/
		vouchRecord.process(db, gb, false);

		Tuple2<Integer, Integer> ggg = new Tuple2<Integer, Integer>(height, seq);

		Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> value = db.getVouchRecordMap().get(height, seq);
		/*
		assertEquals(new Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>(
				new BigDecimal(1000).setScale(8),
				new ArrayList<Tuple2<Integer, Integer>>(
						//new Tuple2<Integer, Integer>(height, seq)
						//ggg
						)), value);
			
		*/
		
		vouchRecord.orphan(db, false);

		value = db.getVouchRecordMap().get(height, seq);
		assertEquals(value, new Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>(
				BigDecimal.ZERO.setScale(8),
				new ArrayList<Tuple2<Integer, Integer>>(
						)));
				
		//CHECK REFERENCE SENDER
		//assertEquals(vouchRecord.getReference(), maker.getLastReference(db));
	}
	
}
