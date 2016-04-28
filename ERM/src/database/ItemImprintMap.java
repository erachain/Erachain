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

public class ItemImprintMap extends Item_Map 
{
	static final String NAME = "item_imprints";
	static final int TYPE = ItemCls.IMPRINT_TYPE;
	
	public ItemImprintMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database,
				TYPE, "item_imprints",
				ObserverMessage.ADD_IMPRINT_TYPE,
				ObserverMessage.REMOVE_IMPRINT_TYPE,
				ObserverMessage.LIST_IMPRINT_TYPE
				);		
	}

	public ItemImprintMap(ItemImprintMap parent) 
	{
		super(parent);
	}

	// type+name not initialized yet! - it call as Super in New
	protected Map<Long, ItemCls> getMap(DB database) 
	{
		
		//OPEN MAP
		return database.createTreeMap(NAME)
				.valueSerializer(new ItemSerializer(TYPE))
				//.valueSerializer(new ImprintSerializer())
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
		return database.createTreeMap("item_imprints")
				.valueSerializer(new ImprintSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<Long, ItemCls> getMemoryMap() 
	{
		return new HashMap<Long, ItemCls>();
	}

	@Override
	protected ImprintCls getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public long add(ImprintCls imprint)
	{
		//INCREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.incrementAndGet();
		}
		
		//INCREMENT KEY
		this.key++;
		
		//INSERT WITH NEW KEY
		this.set(this.key, imprint);
		
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
