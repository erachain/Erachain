package database;

import org.mapdb.DB;

import utils.ObserverMessage;
import database.DBSet;

public class KKPersonUnionMap extends KK_Map
{
	
	public KKPersonUnionMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database, "person_union",
				ObserverMessage.ADD_PERSON_UNION_TYPE, ObserverMessage.REMOVE_PERSON_UNION_TYPE);
		
	}

	public KKPersonUnionMap(KKPersonUnionMap parent) 
	{
		super(parent);
	}
}
