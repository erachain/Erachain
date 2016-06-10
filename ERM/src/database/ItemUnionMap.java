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

public class ItemUnionMap extends Item_Map
{
	
	static final String NAME = "item_unions";
	static final int TYPE = ItemCls.UNION_TYPE;

	public ItemUnionMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database,
				TYPE, NAME,
				ObserverMessage.ADD_UNION_TYPE,
				ObserverMessage.REMOVE_UNION_TYPE,
				ObserverMessage.LIST_UNION_TYPE
				);
		
	}

	public ItemUnionMap(ItemUnionMap parent) 
	{
		super(parent);
	}

	// type+name not initialized yet! - it call as Super in New
	protected Map<Long, ItemCls> getMap(DB database) 
	{
		
		//OPEN MAP
		return database.createTreeMap(NAME)
				.valueSerializer(new ItemSerializer(TYPE))
				//.valueSerializer(new AssetSerializer())
				.makeOrGet();
	}

	/*
	protected long getKey()
	{
		return this.key;
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Long, UnionCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("item_unions")
				.valueSerializer(new UnionSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<Long, UnionCls> getMemoryMap() 
	{
		return new HashMap<Long, UnionCls>();
	}

	@Override
	protected UnionCls getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public long add(UnionCls union)
	{
		//INCREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.incrementAndGet();
		}
		
		//INCREMENT KEY
		this.key++;
		
		//INSERT WITH NEW KEY
		this.set(this.key, union);
		
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
