package database;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.Atomic;
import org.mapdb.DB;

import core.item.ItemCls;
import core.item.statuses.StatusCls;
import utils.ObserverMessage;
import database.DBSet;
import database.serializer.ItemSerializer;
//import database.serializer.StatusSerializer;

public class ItemStatusMap extends Item_Map 
{
	
	static final String NAME = "item_statuses";
	static final int TYPE = ItemCls.STATUS_TYPE;

	public ItemStatusMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database,
				//TYPE,
				NAME,
				ObserverMessage.ADD_STATUS_TYPE,
				ObserverMessage.REMOVE_STATUS_TYPE,
				ObserverMessage.LIST_STATUS_TYPE
				);
		}

	public ItemStatusMap(ItemStatusMap parent) 
	{
		super(parent);	
	}
	
	// type+name not initialized yet! - it call as Super in New
	protected Map<Long, ItemCls> getMap(DB database) 
	{
		
		//OPEN MAP
		return database.createTreeMap(NAME)
				.valueSerializer(new ItemSerializer(TYPE))
				//.valueSerializer(new StatusSerializer())
				.makeOrGet();
	}

	/*
	protected long getKey()
	{
		return this.key;
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Long, StatusCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("item_statuses")
				.valueSerializer(new StatusSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<Long, StatusCls> getMemoryMap() 
	{
		return new HashMap<Long, StatusCls>();
	}

	@Override
	protected StatusCls getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public long add(StatusCls status)
	{
		//INCREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.incrementAndGet();
		}
		
		//INCREMENT KEY
		this.key++;
		
		//INSERT WITH NEW KEY
		this.set(this.key, status);
		
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
