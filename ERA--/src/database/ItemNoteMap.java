package database;

import java.util.Map;

//import java.util.HashMap;
//import java.util.Map;

//import org.mapdb.Atomic;
import org.mapdb.DB;

import core.item.ItemCls;
import utils.ObserverMessage;
import database.DBSet;
import database.serializer.ItemSerializer;
//import database.serializer.ItemSerializer;

public class ItemNoteMap extends Item_Map 
{
	static final String NAME = "item_notes";
	static final int TYPE = ItemCls.NOTE_TYPE;
	
	public ItemNoteMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database,
				//TYPE,
				"item_notes",
				ObserverMessage.ADD_NOTE_TYPE,
				ObserverMessage.REMOVE_NOTE_TYPE,
				ObserverMessage.LIST_NOTE_TYPE
				);		
	}

	public ItemNoteMap(ItemNoteMap parent) 
	{
		super(parent);
	}

	// type+name not initialized yet! - it call as Super in New
	protected Map<Long, ItemCls> getMap(DB database) 
	{
		
		//OPEN MAP
		return database.createTreeMap(NAME)
				.valueSerializer(new ItemSerializer(TYPE))
				//.valueSerializer(new NoteSerializer())
				.makeOrGet();
	}

}
