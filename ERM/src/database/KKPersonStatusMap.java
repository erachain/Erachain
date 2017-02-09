package database;


import java.util.Stack;
import java.util.TreeMap;

import org.mapdb.DB;
import org.mapdb.Fun.Tuple5;

import utils.ObserverMessage;
import database.DBSet;

/*
public class KK_Map extends DBMap<
Long, // item1 Key <-- PERSON
TreeMap<Long, // item2 Key <-- STATUS
	Stack<Tuple5<
		Long, // beg_date
		Long, // end_date

		byte[], // any additional data
		
		Integer, // block.getHeight() -> db.getBlockMap(db.getHeightMap().getBlockByHeight(index))
		Integer // block.getTransaction(transaction.getSignature()) -> block.getTransaction(index)
	>>>>
{
*/

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
