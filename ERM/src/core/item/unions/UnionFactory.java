package qora.item.unions;

// import org.apache.log4j.Logger;

import qora.item.unions.UnionCls;

public class UnionFactory {

	private static UnionFactory instance;
	
	public static UnionFactory getInstance()
	{
		if(instance == null)
		{
			instance = new UnionFactory();
		}
		
		return instance;
	}
	
	private UnionFactory()
	{
		
	}
	
	public UnionCls parse(byte[] data, boolean includeReference) throws Exception
	{
		//READ TYPE
		int type = data[0];
				
		switch(type)
		{
		case UnionCls.UNION:
			
			//PARSE SIMPLE NOTE
			return Union.parse(data, includeReference);
						
		}

		throw new Exception("Invalid Union type: " + type);
	}
	
}
