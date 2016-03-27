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
		int type = data[0];
				
		switch(type)
		{
		case Asset.STATEMENT:
			
			//PARSE PAYMENT TRANSACTION
			return Statement.parse(data, includeReference);
		
		case Asset.NAME:
			
			//PARSE REGISTER NAME TRANSACTION
			//return RegisterNameTransaction.Parse(data, includeReference);
			
		case Asset.VENTURE:
			
			//PARSE UPDATE NAME TRANSACTION
			return Venture.parse(data, includeReference);
						
		}

		throw new Exception("Invalid asset type: " + type);
	}
	
}
