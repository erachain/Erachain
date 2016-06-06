package database;


import org.mapdb.DB;
import utils.ObserverMessage;
import database.DBSet;

public class KKPersonStatusMap extends KK_Map
{
		
	public KKPersonStatusMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database, "person_status",
				ObserverMessage.ADD_PERSON_STATUS_TYPE, ObserverMessage.REMOVE_PERSON_STATUS_TYPE);
	}

	public KKPersonStatusMap(KKPersonStatusMap parent) 
	{
		super(parent);
	}
	
}
