package qora.assets;

import java.util.Arrays;
import java.util.logging.Logger;

import com.google.common.primitives.Ints;
//import com.google.common.primitives.Longs;

import qora.assets.Asset;

public class AssetFactory {

	private static AssetFactory instance;
	
	public static AssetFactory getInstance()
	{
		if(instance == null)
		{
			instance = new AssetFactory();
		}
		
		return instance;
	}
	
	private AssetFactory()
	{
		
	}
	
	public Asset parse(byte[] data, boolean includeReference) throws Exception
	{
		//READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, Asset.TYPE_LENGTH);
		int type = Ints.fromByteArray(typeBytes);
				
		switch(type)
		{
		case Asset.STATEMENT:
			
			//PARSE PAYMENT TRANSACTION
			return Statement.parse(Arrays.copyOfRange(data, 4, data.length), includeReference);
		
		case Asset.NAME:
			
			//PARSE REGISTER NAME TRANSACTION
			//return RegisterNameTransaction.Parse(Arrays.copyOfRange(data, 4, data.length), includeReference);
			
		case Asset.VENTURE:
			
			//PARSE UPDATE NAME TRANSACTION
			return Venture.parse(Arrays.copyOfRange(data, 4, data.length), includeReference);
						
		}

		throw new Exception("Invalid asset type: " + type);
	}
	
}
