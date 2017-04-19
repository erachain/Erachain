package core.item.statuses;

import core.item.statuses.StatusCls;

public class StatusFactory {

	private static StatusFactory instance;
	
	public static StatusFactory getInstance()
	{
		if(instance == null)
		{
			instance = new StatusFactory();
		}
		
		return instance;
	}
	
	private StatusFactory()
	{
		
	}
	
	public StatusCls parse(byte[] data, boolean includeReference) throws Exception
	{
		//READ TYPE
		int type = data[0];
				
		switch(type)
		{
		case StatusCls.STATUS:
			
			//PARSE SIMPLE STATUS
			return Status.parse(data, includeReference);
						
		case StatusCls.TITLE:
				
			//
			//return Status.parse(data, includeReference);
		}

		throw new Exception("Invalid Status type: " + type);
	}
	
}
