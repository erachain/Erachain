package database;

import org.mapdb.DB;

import utils.ObserverMessage;
import database.DBSet;

public class KKAssetUnionMap extends KK_Map
{
	public KKAssetUnionMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database, "asset_union",
				ObserverMessage.ADD_ASSET_UNION_TYPE, ObserverMessage.REMOVE_ASSET_UNION_TYPE);
	}

	public KKAssetUnionMap(KKAssetUnionMap parent) 
	{
		super(parent);
	}

}
