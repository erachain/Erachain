package database;

import java.util.Map;

//import java.util.HashMap;
//import java.util.Map;

//import org.mapdb.Atomic;
import org.mapdb.DB;

import utils.ObserverMessage;
import database.DBSet;
import qora.item.ItemCls;
import database.serializer.NoteSerializer;
//import database.serializer.ItemSerializer;
//import qora.item.notes.NoteCls;

public class ItemNoteMap extends Item_Map 
{
	static final String NAME = "item_notes";
	static final int TYPE = ItemCls.NOTE_TYPE;
	
	public ItemNoteMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database,
				TYPE, "item_notes",
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
				//.valueSerializer(new ItemSerializer(TYPE))
				.valueSerializer(new NoteSerializer())
				.makeOrGet();
	}

	/*
	protected long getKey()
	{
		return this.key;
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Long, ItemCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("item_notes")
				.valueSerializer(new NoteSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<Long, ItemCls> getMemoryMap() 
	{
		return new HashMap<Long, ItemCls>();
	}

	@Override
	protected NoteCls getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public long add(NoteCls note)
	{
		//INCREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.incrementAndGet();
		}
		
		//INCREMENT KEY
		this.key++;
		
		//INSERT WITH NEW KEY
		this.set(this.key, note);
		
		//RETURN KEY
		return this.key;
	}
	
	
	public void delete(long key)
	{
		super.delete(key);
		
		//DECREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.decrementAndGet();
		}
		
		//DECREMENT KEY
		 this.key = key - 1;
	}
	*/
}
