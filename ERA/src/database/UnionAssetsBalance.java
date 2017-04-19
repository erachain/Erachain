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

// income, balance, 
public class UnionAssetsBalance extends _BalanceMap 
{
	static final String NAME = "union";
	
	public UnionAssetsBalance(DBSet databaseSet, DB database)
	{
		super(databaseSet, database,
				NAME,
				ObserverMessage.ADD_BALANCE_TYPE,
				ObserverMessage.REMOVE_BALANCE_TYPE
				);
	}

	public UnionAssetsBalance(UnionAssetsBalance parent) 
	{
		super(parent);
	}

	/* ????
	// type+name not initialized yet! - it call as Super in New
	protected Map<Long, ItemCls> getMap(DB database) 
	{
		
		//OPEN MAP
		return database.createTreeMap(NAME)
				.valueSerializer(new ItemSerializer(TYPE))
				//.valueSerializer(new AssetSerializer())
				.makeOrGet();
	}
	*/

}
