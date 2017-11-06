package datachain;

import org.mapdb.DB;

import datachain.DCSet;
import utils.ObserverMessage;

// Union parent has unions
// TODO - insert in DBSet
public class KKUnionUnionMap extends KK_Map
{
	
	public KKUnionUnionMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database, "union_union",
				ObserverMessage.RESET_UNION_UNION_TYPE,
				ObserverMessage.ADD_UNION_UNION_TYPE,
				ObserverMessage.REMOVE_UNION_UNION_TYPE,
				ObserverMessage.LIST_UNION_UNION_TYPE
				);
		}

	public KKUnionUnionMap(KKUnionUnionMap parent) 
	{
		super(parent);
	}
	
}
