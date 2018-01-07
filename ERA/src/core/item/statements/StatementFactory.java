package core.item.statements;

public class StatementFactory {

	private static StatementFactory instance;
	
	public static StatementFactory getInstance()
	{
		if(instance == null)
		{
			instance = new StatementFactory();
		}
		
		return instance;
	}
	
	private StatementFactory()
	{
		
	}
	
	public StatementCls parse(byte[] data, boolean includeReference) throws Exception
	{
		//READ TYPE
		int type = data[0];
				
		switch(type)
		{
		case StatementCls.NOTE:
			
			//PARSE SIMPLE STATUS
			return Note.parse(data, includeReference);
						
		//case StatementCls.TITLE:
				
			//
			//return Status.parse(data, includeReference);
		}

		throw new Exception("Invalid Statement type: " + type);
	}
	
}
