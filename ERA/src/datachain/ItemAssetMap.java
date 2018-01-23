package datachain;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.Atomic;
import org.mapdb.DB;

import core.item.ItemCls;
import core.item.assets.AssetCls;
import database.serializer.ItemSerializer;
import datachain.DCSet;
import datachain.Item_Map;
//import database.serializer.AssetSerializer;
import utils.ObserverMessage;

public class ItemAssetMap extends Item_Map 
{
	//private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	//private Atomic.Long atomicKey;
	//private long key;
	static final String NAME = "item_assets";
	static final int TYPE = ItemCls.ASSET_TYPE;
	
	public ItemAssetMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database,
				//TYPE,
				NAME,
				ObserverMessage.RESET_ASSET_TYPE,
				ObserverMessage.ADD_ASSET_TYPE,
				ObserverMessage.REMOVE_ASSET_TYPE,
				ObserverMessage.LIST_ASSET_TYPE
				);
	}

	public ItemAssetMap(ItemAssetMap parent) 
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

}
