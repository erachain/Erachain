package database;

import org.mapdb.DB;
import utils.ObserverMessage;
import database.DBSet;

public class KKStatusUnionMap extends KK_Map
{
	public KKStatusUnionMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database, "status_union",
				ObserverMessage.ADD_STATUS_UNION_TYPE, ObserverMessage.REMOVE_STATUS_UNION_TYPE);
	}

	public KKStatusUnionMap(KKStatusUnionMap parent) 
	{
		super(parent);
	}
	
}
