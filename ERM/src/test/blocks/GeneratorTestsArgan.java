package test.blocks;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import core.BlockGenerator;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.transaction.R_Send;
import core.transaction.Transaction;
import database.DBSet;
//import ntp.NTP;
import ntp.NTP;

public class GeneratorTestsArgan {

	long ERMO_KEY = 1l;
	byte FEE_POWER = (byte)0;
	long timestamp = NTP.getTime();

	
	@Test
	public void addManyTransactionsWithDifferentFees()
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
						
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(10000000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.setLastReference(genesisBlock.getTimestamp(), databaseSet);
		generator.setConfirmedBalance(ERMO_KEY, BigDecimal.valueOf(10000000).setScale(8), databaseSet);
				
		//GENERATE NEXT BLOCK
		BigDecimal genBal = generator.getGeneratingBalance(databaseSet);
		BlockGenerator blockGenerator = new BlockGenerator(false);
		
		//ADD 10 UNCONFIRMED VALID TRANSACTIONS	
		Account recipient = new Account("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5");

		DBSet snapshot = databaseSet.fork();
		for(int i=0; i<1000; i++)
		{
			timestamp = timestamp + i - 10000;
						 				
			//CREATE VALID PAYMENT
			Transaction payment = new R_Send(generator, FEE_POWER, recipient, ERMO_KEY, BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(snapshot));
			payment.sign(generator, false);
		
			//PROCESS IN DB
			payment.process(snapshot, false);
			
			blockGenerator.addUnconfirmedTransaction(databaseSet, payment, false);
		}
		
		timestamp += 100;
		
		List<Transaction> unconfirmedTransactions = BlockGenerator.getUnconfirmedTransactions(databaseSet, timestamp);
		byte[] unconfirmedTransactionsHash = Block.makeTransactionsHash(unconfirmedTransactions);
		Block newBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock, unconfirmedTransactionsHash);
		//SET UNCONFIRMED TRANSACTIONS TO BLOCK
		newBlock.setTransactions(unconfirmedTransactions);
		newBlock.sign(generator);

		
		//CHECK THAT NOT ALL TRANSACTIONS WERE ADDED TO BLOCK
		assertEquals(1000, newBlock.getTransactionCount());
		
		//CHECK IF BLOCK IS VALID
		assertEquals(true, newBlock.isValid(databaseSet));
	}
	//TODO CALCULATETRANSACTIONSIGNATURE
}
