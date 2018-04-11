package core.item;

import core.item.ItemCls;
import core.item.assets.AssetFactory;
import core.item.imprints.Imprint;
import core.item.persons.PersonFactory;
import core.item.statuses.StatusFactory;
import core.item.templates.TemplateFactory;
import core.item.unions.UnionFactory;

public class ItemFactory {

	private static ItemFactory instance;
	
	public static ItemFactory getInstance()
	{
		if(instance == null)
		{
			instance = new ItemFactory();
		}
		
		return instance;
	}
	
	private ItemFactory()
	{
		
	}
	
	public ItemCls parse(int type, byte[] data, boolean includeReference) throws Exception
	{

		switch(type)
		{
		case ItemCls.ASSET_TYPE:
			return AssetFactory.getInstance().parse(data, includeReference);
		case ItemCls.IMPRINT_TYPE:
			return Imprint.parse(data, includeReference);
		case ItemCls.TEMPLATE_TYPE:
			return TemplateFactory.getInstance().parse(data, includeReference);
		case ItemCls.PERSON_TYPE:
			return PersonFactory.getInstance().parse(data, includeReference);
		case ItemCls.STATUS_TYPE:
			return StatusFactory.getInstance().parse(data, includeReference);
		case ItemCls.UNION_TYPE:
			return UnionFactory.getInstance().parse(data, includeReference);
		}

		throw new Exception("Invalid ITEM type: " + type);
	}
	
}
