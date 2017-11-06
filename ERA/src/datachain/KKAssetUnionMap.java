package datachain;

import org.mapdb.DB;

import datachain.DCSet;
import utils.ObserverMessage;

public class KKAssetUnionMap extends KK_Map
{
	public KKAssetUnionMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database, "asset_union",
				ObserverMessage.RESET_ASSET_UNION_TYPE,
				ObserverMessage.ADD_ASSET_UNION_TYPE,
				ObserverMessage.REMOVE_ASSET_UNION_TYPE,
				ObserverMessage.LIST_ASSET_UNION_TYPE
				);
	}

	public KKAssetUnionMap(KKAssetUnionMap parent) 
	{
		super(parent);
	}

}
