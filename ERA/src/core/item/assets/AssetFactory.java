package core.item.assets;

import core.item.assets.AssetCls;

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
	
	public AssetCls parse(byte[] data, boolean includeReference) throws Exception
	{
		//READ TYPE
		int type = data[0];
				
		switch(type)
		{
		case AssetCls.VENTURE:
			
			//PARSE UPDATE NAME TRANSACTION
			return AssetVenture.parse(data, includeReference);

		case AssetCls.UNIQUE:
			
			//PARSE PAYMENT TRANSACTION
			return AssetUnique.parse(data, includeReference);
		
		case AssetCls.NAME:
			
			//PARSE REGISTER NAME TRANSACTION
			//return RegisterNameTransaction.Parse(data, includeReference);
									
		}

		throw new Exception("Invalid asset type: " + type);
	}
	
}
