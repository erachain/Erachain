package datachain;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.Atomic;
import org.mapdb.DB;

import core.item.ItemCls;
import core.item.statuses.StatusCls;
import utils.ObserverMessage;
import database.serializer.ItemSerializer;
//import database.serializer.StatusSerializer;
import datachain.DCSet;

public class ItemStatusMap extends Item_Map 
{
	
	static final String NAME = "item_statuses";
	static final int TYPE = ItemCls.STATUS_TYPE;

	public ItemStatusMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database,
				//TYPE,
				NAME,
				ObserverMessage.RESET_STATUS_TYPE,
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

}
