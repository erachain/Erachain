package database;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.Atomic;
import org.mapdb.DB;

import core.item.ItemCls;
import core.item.assets.AssetCls;
import database.DBSet;
import database.Item_Map;
import database.serializer.ItemSerializer;
//import database.serializer.AssetSerializer;
import utils.ObserverMessage;

public class ItemAssetMap extends Item_Map 
{
	//private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	//private Atomic.Long atomicKey;
	//private long key;
	static final String NAME = "item_assets";
	static final int TYPE = ItemCls.ASSET_TYPE;
	
	public ItemAssetMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database,
				TYPE, NAME,
				ObserverMessage.ADD_ASSET_TYPE,
				ObserverMessage.REMOVE_ASSET_TYPE,
				ObserverMessage.LIST_ASSET_TYPE
				);
	}

	public ItemAssetMap(ItemAssetMap parent) 
	{
		super(parent);
		
		this.key = this.getKey();
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
	protected Map<Long, AssetCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("assets")
				.valueSerializer(new AssetSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<Long, AssetCls> getMemoryMap() 
	{
		return new HashMap<Long, AssetCls>();
	}

	@Override
	protected AssetCls getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public long add(AssetCls asset)
	{
		//INCREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.incrementAndGet();
		}
		
		//INCREMENT KEY
		this.key++;
		
		//INSERT WITH NEW KEY
		this.set(this.key, asset);
		
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
