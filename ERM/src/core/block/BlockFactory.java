package core.block;

import core.account.PublicKeyAccount;

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
		//PARSE BLOCK
		return Block.parse(data, forDB);
	}

	// not signed and not getGeneratingBalance
	public Block create(int version, byte[] reference, PublicKeyAccount generator, byte[] unconfirmedTransactionsHash, byte[] atBytes) 
	{		
		return new Block(version, reference, generator, unconfirmedTransactionsHash, atBytes);
	}
	
}
