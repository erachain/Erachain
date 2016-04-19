package database;

import java.util.Map;

//import java.util.HashMap;
//import java.util.Map;

//import org.mapdb.Atomic;
import org.mapdb.DB;

import core.item.ItemCls;
import utils.ObserverMessage;
import database.DBSet;
import database.serializer.PersonSerializer;

public class ItemPersonMap extends Item_Map
{

	static final String NAME = "item_persons";
	static final int TYPE = ItemCls.PERSON_TYPE;

	public ItemPersonMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database,
				TYPE, NAME,
				ObserverMessage.ADD_PERSON_TYPE,
				ObserverMessage.REMOVE_PERSON_TYPE,
				ObserverMessage.LIST_PERSON_TYPE
				);
			}

	public ItemPersonMap(ItemPersonMap parent) 
	{
		super(parent);
	}

	// type+name not initialized yet! - it call as Super in New
	protected Map<Long, ItemCls> getMap(DB database) 
	{
		
		//OPEN MAP
		return database.createTreeMap(NAME)
				//.valueSerializer(new ItemSerializer(TYPE))
				.valueSerializer(new PersonSerializer())
				.makeOrGet();
	}

	/*
	protected long getKey()
	{
		return this.key;
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Long, PersonCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("item_persons")
				.valueSerializer(new PersonSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<Long, PersonCls> getMemoryMap() 
	{
		return new HashMap<Long, PersonCls>();
	}

	@Override
	protected PersonCls getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public long add(PersonCls person)
	{
		//INCREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.incrementAndGet();
		}
		
		//INCREMENT KEY
		this.key++;
		
		//INSERT WITH NEW KEY
		this.set(this.key, person);
		
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
