package database;

import org.mapdb.DB;
import utils.ObserverMessage;
import database.DBSet;

public class KKAssetStatusMap extends KK_Map
{
	public KKAssetStatusMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database, "asset_status",
				ObserverMessage.ADD_ASSET_STATUS_TYPE, ObserverMessage.REMOVE_ASSET_STATUS_TYPE);
	}
	
	public KKAssetStatusMap(KKAssetStatusMap parent) 
	{
		super(parent);
	}

	
}
