package test.blocks;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import ntp.NTP;

import org.junit.Test;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import controller.Controller;
import core.BlockGenerator;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.BlockChain;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.transaction.GenesisTransferAssetTransaction;
//import core.transaction.GenesisTransaction;
import core.transaction.R_Send;
import core.transaction.Transaction;
import core.wallet.Wallet;
import database.DBSet;

public class GeneratorTests {

	long ERM_KEY = Transaction.RIGHTS_KEY;
	long FEE_KEY = Transaction.FEE_KEY;
	byte FEE_POWER = (byte)0;
	byte[] assetReference = new byte[Crypto.SIGNATURE_LENGTH];
	long timestamp = NTP.getTime();
	Controller cntrlr = Controller.getInstance();

	List<Transaction> transactions =  new ArrayList<Transaction>();
	byte[] transactionsHash =  new byte[Crypto.HASH_LENGTH];

	//CREATE EMPTY MEMORY DATABASE
	DBSet dbSet = DBSet.createEmptyDatabaseSet();

	@Test
	public void generateNewBlock() 
	{
		
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		Controller.getInstance().initBlockChain(dbSet);
		genesisBlock.process(dbSet);
		
		
		//CREATE KNOWN ACCOUNT
		int nonce = 1;
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
	    PrivateKeyAccount generator1 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
	    PrivateKeyAccount generator2 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
	    PrivateKeyAccount generator3 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
	    PrivateKeyAccount generator4 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
				
	    int height = 1;
	    int seq = 1;
	    PrivateKeyAccount generator;
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		// AND LAST REFERENCE
		// AND WIN_DATA
		Transaction transaction;
		transaction = new GenesisTransferAssetTransaction(generator1, ERM_KEY, BigDecimal.valueOf(1000000).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);

		transaction = new GenesisTransferAssetTransaction(generator2, ERM_KEY, BigDecimal.valueOf(10000000).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);

		transaction = new GenesisTransferAssetTransaction(generator3, ERM_KEY, BigDecimal.valueOf(300000).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);

		transaction = new GenesisTransferAssetTransaction(generator4, ERM_KEY, BigDecimal.valueOf(3000000).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);
		
		transaction = new GenesisTransferAssetTransaction(generator1, FEE_KEY, BigDecimal.valueOf(10).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);
		
		assertEquals(1000000, generator1.getBalanceUSE(ERM_KEY, dbSet).longValue());
		
		//GENERATE 2000 NEXT BLOCKS
		Block lastBlock = genesisBlock;
		for(int i=0; i<200; i++)
		{	
			
			if ( NTP.getTime() - lastBlock.getTimestamp(dbSet) < Block.GENERATING_MIN_BLOCK_TIME) {
				break;
			}
			
			if (i < 10)
				generator = generator1;
			else if (i < 30)
				generator = generator2;
			else if (i < 40)
				generator = generator3;
			else if (i < 100)
				generator = generator1;
			else
				generator = generator4;
			
			//GENERATE NEXT BLOCK
			Block newBlock = BlockGenerator.generateNextBlock(dbSet, generator, lastBlock, transactionsHash);
			
			//ADD TRANSACTION SIGNATURE
			//byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
			newBlock.makeTransactionsHash();
			newBlock.sign(generator);
			
			//CHECK IF BLOCK SIGNATURE IS VALID
			assertEquals(true, newBlock.isSignatureValid());
			
			//CHECK IF BLOCK IS VALID
			if (!newBlock.isValid(dbSet))
				assertEquals(true, newBlock.isValid(dbSet));
			
			height = Controller.getInstance().getMyHeight();
			assertEquals(height, i + 1);

			/*
			if (i == 0) {
				assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 1) {
				assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 2) {
				assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(15716, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 9) {
				assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(23803, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 10) {
				assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(25024, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 11) {
				assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 12) {
				assertEquals(1571, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 29) {
				assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
				assertEquals(2018, generator3.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 30) {
				assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
				assertEquals(2124, generator3.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 31) {
				assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
				assertEquals(439, generator3.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 39) {
				assertEquals(439, generator3.calcWinValueTargeted(dbSet, height));
				assertEquals(6729, generator1.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 40) {
				assertEquals(439, generator3.calcWinValueTargeted(dbSet, height));
				assertEquals(7080, generator1.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 41) {
				assertEquals(439, generator3.calcWinValueTargeted(dbSet, height));
				assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 42) {
				assertEquals(439, generator3.calcWinValueTargeted(dbSet, height));
				assertEquals(439, generator1.calcWinValueTargeted(dbSet, height));
			}
			*/

			
			//PROCESS NEW BLOCK
			newBlock.process(dbSet);
			
			height = newBlock.getHeight(dbSet);
			assertEquals(height, i + 2);
			
			if (i == 0) {
				//assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(1024, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 1) {
				//assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(1464, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(15716, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 2) {
				//assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(1464, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(16784, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 9) {
				//assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(1464, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(25024, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 10) {
				//assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(25024, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 11) {
				//assertEquals(1571, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(14648, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 12) {
				//assertEquals(1678, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(14648, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 29) {
				//assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
				assertEquals(14648, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(2124, generator3.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 30) {
				//assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
				assertEquals(2124, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(439, generator3.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 31) {
				//assertEquals(15716, generator2.calcWinValueTargeted(dbSet, height));
				assertEquals(439, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(439, generator3.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 39) {
				//assertEquals(439, generator3.calcWinValueTargeted(dbSet, height));
				assertEquals(439, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(7080, generator1.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 40) {
				//assertEquals(439, generator3.calcWinValueTargeted(dbSet, height));
				assertEquals(7080, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 41) {
				//assertEquals(471, generator3.calcWinValueTargeted(dbSet, height));
				assertEquals(1464, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(1464, generator1.calcWinValueTargeted(dbSet, height));
			}
			
			//LAST BLOCK IS NEW BLOCK
			lastBlock = newBlock;
		}
	}

	@Test
	public void fullWeight() 
	{
		
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		cntrlr.initBlockChain(dbSet);
		// already processed genesisBlock.process(dbSet);
		
		
		//CREATE KNOWN ACCOUNT
		int nonce = 1;
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
	    PrivateKeyAccount generator0 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
	    PrivateKeyAccount generator1 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
	    PrivateKeyAccount generator2 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
	    PrivateKeyAccount generator3 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
	    PrivateKeyAccount generator4 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
	    PrivateKeyAccount generator5 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
	    PrivateKeyAccount generator6 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
	    PrivateKeyAccount generator7 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
	    PrivateKeyAccount generator8 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
	    PrivateKeyAccount generator9 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
				
	    int height = 1;
	    int seq = 1;
	    PrivateKeyAccount generator;
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		// AND LAST REFERENCE
		// AND WIN_DATA
		Transaction transaction;
		transaction = new GenesisTransferAssetTransaction(generator0, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL / 10).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);

		transaction = new GenesisTransferAssetTransaction(generator1, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL / 10).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);

		transaction = new GenesisTransferAssetTransaction(generator2, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL / 20).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);

		transaction = new GenesisTransferAssetTransaction(generator3, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.045).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);
		
		transaction = new GenesisTransferAssetTransaction(generator4, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.045).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);

		transaction = new GenesisTransferAssetTransaction(generator5, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.03).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);

		transaction = new GenesisTransferAssetTransaction(generator6, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.02).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);

		transaction = new GenesisTransferAssetTransaction(generator7, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.02).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);

		transaction = new GenesisTransferAssetTransaction(generator8, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.03).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);

		transaction = new GenesisTransferAssetTransaction(generator9, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.033).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);

		///////////////
		transaction = new GenesisTransferAssetTransaction(generator1, FEE_KEY, BigDecimal.valueOf(10).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		dbSet.getTransactionRef_BlockRef_Map().set(transaction.getSignature(), genesisBlock.getSignature());
		dbSet.getTransactionFinalMap().add( height, seq++, transaction);
		
		assertEquals(1000000, generator1.getBalanceUSE(ERM_KEY, dbSet).longValue());
		
		TreeMap<Integer, Tuple2<Integer, Long>> buffer = new TreeMap();

		// break on NUM
		int i_break = 3;

		//GENERATE 2000 NEXT BLOCKS
		Block lastBlock = genesisBlock;
		for(int i=2; i<120; i++)
		{	
			
			if ( NTP.getTime() - lastBlock.getTimestamp(dbSet) < Block.GENERATING_MIN_BLOCK_TIME) {
				break;
			}
			
			if (i % 10 == 0)
				generator = generator0;
			else if (i % 10 == 1)
				generator = generator1;
			else if (i % 10 == 2)
				generator = generator2;
			else if (i % 10 == 3)
				generator = generator3;
			else if (i % 10 == 4)
				generator = generator4;
			else if (i % 10 == 5)
				generator = generator5;
			else if (i % 10 == 6)
				generator = generator6;
			else if (i % 10 == 7)
				generator = generator7;
			else if (i % 10 == 8)
				generator = generator8;
			else if (i % 10 == 9)
				generator = generator9;
			else
				generator = generator0;
			
			//GENERATE NEXT BLOCK
			Block newBlock = BlockGenerator.generateNextBlock(dbSet, generator, lastBlock, transactionsHash);
			
			//ADD TRANSACTION SIGNATURE
			byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
			newBlock.makeTransactionsHash();
			newBlock.sign(generator);
			
			//CHECK IF BLOCK SIGNATURE IS VALID
			assertEquals(true, newBlock.isSignatureValid());
			
			long weight_old = newBlock.calcWinValueTargeted(dbSet);
			//CHECK IF BLOCK IS VALID
			if (!newBlock.isValid(dbSet))
				assertEquals(false, newBlock.isValid(dbSet));

			Tuple2<Integer, Long> hWeight_old = cntrlr.getBlockChain().getHWeight(dbSet, false);

			if (i == 2) {
				// for debug breakpoint
				i_break++;
			}
			
			//PROCESS NEW BLOCK
			newBlock.process(dbSet);
			
			long weight = newBlock.calcWinValueTargeted(dbSet);
			if (weight != weight_old) {
				i_break++;
			}
			assertEquals(weight, weight_old);
			
			height = newBlock.getHeight(dbSet);

			Tuple2<Integer, Long> hWeight = cntrlr.getBlockChain().getHWeight(dbSet, false);

			assertEquals((long)hWeight_old.b + weight_old, (long)hWeight.b);
			
			assertEquals((int)hWeight.a, (int)hWeight_old.a + 1);
			assertEquals((int)hWeight.a, height);

			buffer.put(height, hWeight);

			if (i == 2) {
				assertEquals(8192, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(14648, generator2.calcWinValueTargetedTargeted(dbSet, height));
				assertEquals((long)hWeight.b, 9192); // GENESIS + new first block
			}
			else if (i == 3) {
				assertEquals(7369, newBlock.calcWinValueTargeted(dbSet));
				assertEquals((int)hWeight.a, height);
				assertEquals((long)hWeight.b, 9192 + 7369);
			}
			else if (i == 4) {
				assertEquals(15522, newBlock.calcWinValueTargeted(dbSet));
				assertEquals((long)hWeight.b, 9192 + 7369 + 15522);
			}
			else if (i == 11) {
				assertEquals(18873, newBlock.calcWinValueTargeted(dbSet));
				assertEquals((long)hWeight.b, 130617);
				//assertEquals(25024, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 12) {
				assertEquals(15963, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(26245, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 13) {
				//assertEquals(1678, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(13226, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 14) {
				//assertEquals(1785, generator1.calcWinValueTargeted(dbSet, height));
				assertEquals(12581, newBlock.calcWinValueTargeted(dbSet));
				//assertEquals(14648, generator2.calcWinValueTargeted(dbSet, height));
			}
			else if (i == 30) {
				//assertEquals(21240, generator3.calcWinValueTargeted(dbSet, height));
				assertEquals(13911, newBlock.calcWinValueTargeted(dbSet));
			}
			else if (i == 31) {
				//assertEquals(22338, generator3.calcWinValueTargeted(dbSet, height));
				assertEquals(13578, newBlock.calcWinValueTargeted(dbSet));
			}
			else if (i == 32) {
				//assertEquals(4394, generator3.calcWinValueTargeted(dbSet, height));
				assertEquals(10218, newBlock.calcWinValueTargeted(dbSet));
			}
			else if (i == 13) {
			}
			
			//LAST BLOCK IS NEW BLOCK
			lastBlock = newBlock;
		}
		
		////////////////////////////
		// P
		for(int i_height=height; i_height>32; i_height--)
		{	

			if (i_height == 102) {
				i_break++;
			}

			
			//GENERATE NEXT BLOCK
			byte[] signature = dbSet.getBlockHeightsMap().get((long)i_height);
			Block block = dbSet.getBlockMap().get(signature);
			if (block == null) {
				i_break++;
			}

			Tuple2<Integer, Long> hWeight_old = cntrlr.getBlockChain().getHWeight(dbSet, false);
			long weight = block.calcWinValueTargeted(dbSet);

			//PRPHAN BLOCK
			block.orphan(dbSet);


			int height_bad = block.getHeight(dbSet);
			assertEquals(-1, height_bad);
			
			Block parent = block.getParent(dbSet);
			int parentHeight = parent.getHeight(dbSet);
			
			Tuple2<Integer, Long> hWeight = cntrlr.getBlockChain().getHWeight(dbSet, false);

			assertEquals((int)hWeight.a, parentHeight);

			if (hWeight_old.b - weight != (long)hWeight.b) {
				int ii = 2;
			}
			assertEquals((long)hWeight_old.b - weight, (long)hWeight.b);

			assertEquals( buffer.get(parentHeight).b, hWeight.b);
			
			if (i_height == 0) {
				assertEquals(1464, block.calcWinValueTargeted(dbSet));
				//assertEquals(14648, generator2.calcWinValueTargeted(dbSet, i_height));
				assertEquals((long)hWeight.b, 1464);
			}
			else if (i_height == 1) {
				assertEquals(1464, block.calcWinValueTargeted(dbSet));
				//assertEquals(15716, generator2.calcWinValueTargeted(dbSet, i_height));
				assertEquals((int)hWeight.a, i_height);
				assertEquals((long)hWeight.b, 1464 + 1464);
			}
			else if (i_height == 2) {
				assertEquals(1464, block.calcWinValueTargeted(dbSet));
				assertEquals((long)hWeight.b, (i_height-1) * 1464);
			}
			else if (i_height == 9) {
			}
			else if (i_height == 10) {
			}
			else if (i_height == 11) {
			}
			
			//LAST BLOCK IS NEW BLOCK
			//lastBlock = block;
		}
		
		height = i_break++;

	}

	@Test
	public void addTransactions()
	{
		// TEN
		
				
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(dbSet);
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
						
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(100000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.setLastReference(genesisBlock.getTimestamp(dbSet), dbSet);
		generator.changeBalance(dbSet, false, ERM_KEY, BigDecimal.valueOf(10000).setScale(8));
		generator.changeBalance(dbSet, false, FEE_KEY, BigDecimal.valueOf(10000).setScale(8));

		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator(false);
		Block newBlock = BlockGenerator.generateNextBlock(dbSet, generator, genesisBlock, transactionsHash);

		// get timestamp for block
		long timestamp = newBlock.getTimestamp(dbSet) - Block.GENERATING_MIN_BLOCK_TIME / 2;

		//ADD 10 UNCONFIRMED VALID TRANSACTIONS	
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		DBSet snapshot = dbSet.fork();
		for(int i=0; i<10; i++)
		{
				
			//CREATE VALID PAYMENT
			Transaction payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(0.01).setScale(8), timestamp++, generator.getLastReference(snapshot));
			payment.sign(generator, false);
		
			//PROCESS IN DB
			payment.process(snapshot, genesisBlock, false);
			
			//ADD TO UNCONFIRMED TRANSACTIONS
			blockGenerator.addUnconfirmedTransaction(dbSet, payment, false);
						
		}
		
		transactions = BlockGenerator.getUnconfirmedTransactions(dbSet, newBlock.getTimestamp(dbSet) );
		// CALCULATE HASH for that transactions
		byte[] transactionsHash = Block.makeTransactionsHash(generator.getPrivateKey(), transactions, null);

		//ADD UNCONFIRMED TRANSACTIONS TO BLOCK
		newBlock = BlockGenerator.generateNextBlock(dbSet, generator, genesisBlock, transactionsHash);
		newBlock.setTransactions(transactions);
		
		//CHECK IF BLOCK IS VALID
		assertEquals(true, newBlock.isValid(dbSet));

		newBlock.sign(generator);
		//CHECK IF BLOCK IS VALID
		assertEquals(true, newBlock.isSignatureValid());
}
	
	@Test
	public void addManyTransactions()
	{
				
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(dbSet);
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
						
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(100000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.setLastReference(genesisBlock.getTimestamp(dbSet), dbSet);
		generator.changeBalance(dbSet, false, ERM_KEY, BigDecimal.valueOf(10000).setScale(8));
		generator.changeBalance(dbSet, false, FEE_KEY, BigDecimal.valueOf(100000).setScale(8));

				
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator(false);
		Block newBlock = BlockGenerator.generateNextBlock(dbSet, generator, genesisBlock, transactionsHash);
		
		// get timestamp for block
		long timestampStart = newBlock.getTimestamp(dbSet) - Block.GENERATING_MIN_BLOCK_TIME / 2;
		long timestamp = timestampStart;

		//ADD 10000 UNCONFIRMED VALID TRANSACTIONS	
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		DBSet snapshot = dbSet.fork();
		int max_count = 2000;
		for(int i=0; i<max_count; i++)
		{
				
			//CREATE VALID PAYMENT
			Transaction payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(0.001).setScale(8),
					 "sss", new byte[3000], new byte[]{1}, new byte[]{0},
					 timestamp++, generator.getLastReference(snapshot));
			payment.sign(generator, false);
			assertEquals(payment.isValid(snapshot, null), Transaction.VALIDATE_OK);
		
			//PROCESS IN DB
			payment.process(snapshot, genesisBlock, false);
			
			//ADD TO UNCONFIRMED TRANSACTIONS
			blockGenerator.addUnconfirmedTransaction(dbSet, payment, false);
		}
		
		//ADD UNCONFIRMED TRANSACTIONS TO BLOCK
		transactions = BlockGenerator.getUnconfirmedTransactions(dbSet, newBlock.getTimestamp(dbSet) );

		//CHECK THAT NOT ALL TRANSACTIONS WERE ADDED TO BLOCK
		assertEquals(true, max_count > transactions.size());
		
		// CALCULATE HASH for that transactions
		byte[] transactionsHash = Block.makeTransactionsHash(generator.getPrivateKey(), transactions, null);

		//ADD UNCONFIRMED TRANSACTIONS TO BLOCK
		newBlock = BlockGenerator.generateNextBlock(dbSet, generator, genesisBlock, transactionsHash);
		newBlock.setTransactions(transactions);
		
		//CHECK THAT NOT ALL TRANSACTIONS WERE ADDED TO BLOCK
		assertEquals(transactions.size(), newBlock.getTransactionCount());
		
		//CHECK IF BLOCK IS VALID
		assertEquals(true, newBlock.isValid(dbSet));
	}

	@Test
	public void winValues()
	{
		// as win values updated on block process
				
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(dbSet);
				
		int nonce = 1;
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
	    PrivateKeyAccount userAccount1 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
	    PrivateKeyAccount userAccount2 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
						
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		Transaction transaction = new GenesisTransferAssetTransaction(userAccount1, ERM_KEY, BigDecimal.valueOf(100000).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		transaction = new GenesisTransferAssetTransaction(userAccount1, FEE_KEY, BigDecimal.valueOf(1).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		transaction = new GenesisTransferAssetTransaction(userAccount2, ERM_KEY, BigDecimal.valueOf(10000).setScale(8));
		transaction.process(dbSet, genesisBlock, false);
		transaction = new GenesisTransferAssetTransaction(userAccount2, FEE_KEY, BigDecimal.valueOf(1).setScale(8));
		transaction.process(dbSet, genesisBlock, false);

				
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator(false);
		Block newBlock = BlockGenerator.generateNextBlock(dbSet, userAccount1, genesisBlock, transactionsHash);
		
		// get timestamp for block
		long timestampStart = newBlock.getTimestamp(dbSet) - Block.GENERATING_MIN_BLOCK_TIME / 2;
		long timestamp = timestampStart;

		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		Transaction payment = new R_Send(userAccount1, FEE_POWER, recipient, ERM_KEY, BigDecimal.valueOf(2000).setScale(8),
				 timestamp++, userAccount1.getLastReference(dbSet));
		payment.sign(userAccount1, false);
		assertEquals(payment.isValid(dbSet, null), Transaction.VALIDATE_OK);
			
		//ADD TO UNCONFIRMED TRANSACTIONS
		blockGenerator.addUnconfirmedTransaction(dbSet, payment, false);
		
		//ADD UNCONFIRMED TRANSACTIONS TO BLOCK
		transactions = BlockGenerator.getUnconfirmedTransactions(dbSet, newBlock.getTimestamp(dbSet) );
		
		// CALCULATE HASH for that transactions
		byte[] transactionsHash = Block.makeTransactionsHash(userAccount1.getPrivateKey(), transactions, null);

		//ADD UNCONFIRMED TRANSACTIONS TO BLOCK
		newBlock = BlockGenerator.generateNextBlock(dbSet, userAccount1, genesisBlock, transactionsHash);
		newBlock.setTransactions(transactions);
		newBlock.process(dbSet);
		
		int height = newBlock.getHeight(dbSet);
		//CHECK THAT NOT ALL TRANSACTIONS WERE ADDED TO BLOCK
		Integer forgingData = userAccount1.getForgingData(dbSet, height);
		assertEquals((int)forgingData, 2);

		forgingData = recipient.getForgingData(dbSet, height);
		assertEquals((int)forgingData, 2);

	}
	

}
