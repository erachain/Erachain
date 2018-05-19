package core.item.polls;

public class PollFactory {

	private static PollFactory instance;
	
	public static PollFactory getInstance()
	{
		if(instance == null)
		{
			instance = new PollFactory();
		}
		
		return instance;
	}
	
	private PollFactory()
	{
		
	}
	
	public PollCls parse(byte[] data, boolean includeReference) throws Exception
	{
		//READ TYPE
		int type = data[0];
				
		switch(type)
		{
		case PollCls.POLL:
			
			//PARSE SIMPLE PLATE
			return Poll.parse(data, includeReference);
						
		}

		throw new Exception("Invalid Union type: " + type);
	}
	
}
