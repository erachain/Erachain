package database;

import org.mapdb.DB;
import utils.ObserverMessage;
import database.DBSet;

public class KKUnionStatusMap extends KK_Map
{
	
	public KKUnionStatusMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database, "union_status",
				ObserverMessage.ADD_UNION_STATUS_TYPE, ObserverMessage.REMOVE_UNION_STATUS_TYPE);
		}

	public KKUnionStatusMap(KKUnionStatusMap parent) 
	{
		super(parent);
	}
	
}
