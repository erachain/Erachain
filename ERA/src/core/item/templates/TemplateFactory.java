package core.item.templates;

import core.item.templates.TemplateCls;

public class TemplateFactory {

	private static TemplateFactory instance;
	
	public static TemplateFactory getInstance()
	{
		if(instance == null)
		{
			instance = new TemplateFactory();
		}
		
		return instance;
	}
	
	private TemplateFactory()
	{
		
	}
	
	public TemplateCls parse(byte[] data, boolean includeReference) throws Exception
	{
		//READ TYPE
		int type = data[0];
				
		switch(type)
		{
		case TemplateCls.PLATE:
			
			//PARSE SIMPLE PLATE
			return Template.parse(data, includeReference);
						
		case TemplateCls.SAMPLE:

			//
			//return Template.parse(data, includeReference);

		case TemplateCls.PAPER:
				
			//
			//return Template.parse(data, includeReference);
		}

		throw new Exception("Invalid Template type: " + type);
	}
	
}
