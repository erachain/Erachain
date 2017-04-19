package database.wallet;

import java.util.Map;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import core.item.ItemCls;
import utils.ObserverMessage;
import database.serializer.ItemSerializer;

public class WItemUnionMap extends WItem_Map
{
	
	static final String NAME = "union";
	static final int TYPE = ItemCls.UNION_TYPE;


	public WItemUnionMap(WalletDatabase walletDatabase, DB database)
	{
		super(walletDatabase, database,
				TYPE, "item_unions",
				ObserverMessage.ADD_UNION_TYPE,
				ObserverMessage.REMOVE_UNION_TYPE,
				ObserverMessage.LIST_UNION_TYPE
				);
	}

	public WItemUnionMap(WItemUnionMap parent) 
	{
		super(parent);
	}
	
	@Override
	// type+name not initialized yet! - it call as Super in New
	protected Map<Tuple2<String, String>, ItemCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap(NAME)
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.valueSerializer(new ItemSerializer(TYPE))
				.counterEnable()
				.makeOrGet();
	}

}
