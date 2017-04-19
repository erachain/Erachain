package database;

import org.mapdb.DB;
import utils.ObserverMessage;
import database.DBSet;

// Union parent has unions
// TODO - insert in DBSet
public class KKUnionUnionMap extends KK_Map
{
	
	public KKUnionUnionMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database, "union_union",
				ObserverMessage.ADD_UNION_UNION_TYPE, ObserverMessage.REMOVE_UNION_UNION_TYPE);
		}

	public KKUnionUnionMap(KKUnionUnionMap parent) 
	{
		super(parent);
	}
	
}
