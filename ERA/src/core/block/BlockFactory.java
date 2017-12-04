package core.block;

import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Ints;

import core.account.PublicKeyAccount;
import core.transaction.Transaction;

public class BlockFactory {

	private static BlockFactory instance;
	
	public static BlockFactory getInstance()
	{
		if(instance == null)
		{
			instance = new BlockFactory();
		}
		
		return instance;
	}
	
	private BlockFactory()
	{
		
	}
	
	public Block parse(byte[] data, boolean forDB) throws Exception
	{
		//READ VERSION
		byte[] versionBytes = Arrays.copyOfRange(data, 0, Block.VERSION_LENGTH);
		
		
		int version = Ints.fromByteArray(versionBytes);

		if (version == 0) {
			//PARSE GENESIS BLOCK
			return GenesisBlock.parse(data, forDB);
		} else {
			//PARSE BLOCK
			return Block.parse(data, forDB);
		}
	}

	// not signed and not getGeneratingBalance
	public Block create(int version, byte[] reference, PublicKeyAccount generator, byte[] unconfirmedTransactionsHash, byte[] atBytes) 
	{		
		return new Block(version, reference, generator, unconfirmedTransactionsHash, atBytes);
	}
	
	// not signed and not getGeneratingBalance
	public Block create(int version, byte[] reference, PublicKeyAccount generator, List<Transaction> trans, byte[] atBytes) 
	{		
		return new Block(version, reference, generator, trans, atBytes);
	}
}
