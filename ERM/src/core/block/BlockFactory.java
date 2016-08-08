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
	
	public Block parse(byte[] data) throws Exception
	{
		//PARSE BLOCK
		return Block.parse(data);
	}

	// not signed
	public Block create(int version, byte[] reference, long timestamp, PublicKeyAccount generator, byte[] unconfirmedTransactionsHash, byte[] atBytes) 
	{		
		return new Block(version, reference, timestamp, generator, unconfirmedTransactionsHash, atBytes);
	}
	
}
