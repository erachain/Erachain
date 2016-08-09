package test.blocks;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import ntp.NTP;

import org.junit.Test;
import org.mapdb.Fun.Tuple3;

import core.BlockGenerator;
import core.account.Account;
import core.account.PrivateKeyAccount;
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

	List<Transaction> transactions =  new ArrayList<Transaction>();
	byte[] transactionsHash =  new byte[Crypto.HASH_LENGTH];

	//CREATE EMPTY MEMORY DATABASE
	DBSet dbSet = DBSet.createEmptyDatabaseSet();

	@Test
	public void generateNewBlock() 
	{
		
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(dbSet);
		
		
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		// AND LAST REFERENCE
		// AND WIN_DATA
		Transaction transaction = new GenesisTransferAssetTransaction(generator, ERM_KEY, BigDecimal.valueOf(1000).setScale(8));
		transaction.process(dbSet, false);
		transaction = new GenesisTransferAssetTransaction(generator, FEE_KEY, BigDecimal.valueOf(10).setScale(8));
		transaction.process(dbSet, false);
		
		//GENERATE 2000 NEXT BLOCKS
		Block lastBlock = genesisBlock;
		for(int i=0; i<2000; i++)
		{	
			
			if ( NTP.getTime() - lastBlock.getTimestamp(dbSet) < Block.GENERATING_MIN_BLOCK_TIME) {
				break;
			}
			//GENERATE NEXT BLOCK
			Block newBlock = BlockGenerator.generateNextBlock(dbSet, generator, lastBlock, transactionsHash);
			
			//ADD TRANSACTION SIGNATURE
			//byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
			newBlock.makeTransactionsHash();
			
			//CHECK IF BLOCK SIGNATURE IS VALID
			assertEquals(true, newBlock.isSignatureValid());
			
			//CHECK IF BLOCK IS VALID
			if (!newBlock.isValid(dbSet))
				assertEquals(true, newBlock.isValid(dbSet));
			
			//PROCESS NEW BLOCK
			newBlock.process(dbSet);
			
			//LAST BLOCK IS NEW BLOCK
			lastBlock = newBlock;
		}
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
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(10000).setScale(8), dbSet);
		generator.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(10000).setScale(8), dbSet);

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
			payment.process(snapshot, false);
			
			//ADD TO UNCONFIRMED TRANSACTIONS
			blockGenerator.addUnconfirmedTransaction(dbSet, payment, false);
						
		}
		
		transactions = BlockGenerator.getUnconfirmedTransactions(dbSet, newBlock.getTimestamp(dbSet) );
		// CALCULATE HASH for that transactions
		byte[] transactionsHash = Block.makeTransactionsHash(transactions);

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
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(10000).setScale(8), dbSet);
		generator.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(100000).setScale(8), dbSet);

				
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
					 new byte[3000], new byte[]{1}, new byte[]{0},
					 timestamp++, generator.getLastReference(snapshot));
			payment.sign(generator, false);
			assertEquals(payment.isValid(snapshot, null), Transaction.VALIDATE_OK);
		
			//PROCESS IN DB
			payment.process(snapshot, false);
			
			//ADD TO UNCONFIRMED TRANSACTIONS
			blockGenerator.addUnconfirmedTransaction(dbSet, payment, false);
		}
		
		//ADD UNCONFIRMED TRANSACTIONS TO BLOCK
		transactions = BlockGenerator.getUnconfirmedTransactions(dbSet, newBlock.getTimestamp(dbSet) );

		//CHECK THAT NOT ALL TRANSACTIONS WERE ADDED TO BLOCK
		assertEquals(true, max_count > transactions.size());
		
		// CALCULATE HASH for that transactions
		byte[] transactionsHash = Block.makeTransactionsHash(transactions);

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
		transaction.process(dbSet, false);
		transaction = new GenesisTransferAssetTransaction(userAccount1, FEE_KEY, BigDecimal.valueOf(1).setScale(8));
		transaction.process(dbSet, false);
		transaction = new GenesisTransferAssetTransaction(userAccount2, ERM_KEY, BigDecimal.valueOf(10000).setScale(8));
		transaction.process(dbSet, false);
		transaction = new GenesisTransferAssetTransaction(userAccount2, FEE_KEY, BigDecimal.valueOf(1).setScale(8));
		transaction.process(dbSet, false);

				
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
		byte[] transactionsHash = Block.makeTransactionsHash(transactions);

		//ADD UNCONFIRMED TRANSACTIONS TO BLOCK
		newBlock = BlockGenerator.generateNextBlock(dbSet, userAccount1, genesisBlock, transactionsHash);
		newBlock.setTransactions(transactions);
		newBlock.process(dbSet);
		
		//CHECK THAT NOT ALL TRANSACTIONS WERE ADDED TO BLOCK
		Tuple3<Integer, Integer, TreeSet<String>> forgingData = userAccount1.getForgingData(dbSet);
		assertEquals((int)forgingData.a, 2);
		assertEquals((int)forgingData.b, 98000);

		forgingData = recipient.getForgingData(dbSet);
		assertEquals((int)forgingData.a, 2);
		assertEquals((int)forgingData.b, 2000);

	}
	

}
