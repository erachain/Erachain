package database;


import org.mapdb.DB;
import utils.ObserverMessage;
import database.DBSet;

// Person has Status of Union - person Ermolaev get Director status in Polza union
public class KK_KPersonStatusUnionMap extends KK_K_Map
{
		
	public KK_KPersonStatusUnionMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database, "person_status_union",
				ObserverMessage.ADD_PERSON_STATUS_UNION_TYPE, ObserverMessage.REMOVE_PERSON_STATUS_UNION_TYPE);
	}

	public KK_KPersonStatusUnionMap(KK_KPersonStatusUnionMap parent) 
	{
		super(parent);
	}
	
}
