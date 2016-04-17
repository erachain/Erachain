package qora.item;

// import org.apache.log4j.Logger;

//import com.google.common.primitives.Ints;
//import com.google.common.primitives.Longs;

import qora.item.ItemCls;
import qora.item.assets.AssetFactory;
import qora.item.notes.NoteFactory;
import qora.item.persons.PersonFactory;
import qora.item.statuses.StatusFactory;
import qora.item.unions.UnionFactory;

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
		case ItemCls.NOTE_TYPE:
			return NoteFactory.getInstance().parse(data, includeReference);
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
