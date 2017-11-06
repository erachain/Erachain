package datachain;

import org.mapdb.DB;

import datachain.DCSet;
import utils.ObserverMessage;

public class KKStatusUnionMap extends KK_Map
{
	public KKStatusUnionMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database, "status_union",
				ObserverMessage.RESET_STATUS_UNION_TYPE,
				ObserverMessage.ADD_STATUS_UNION_TYPE,
				ObserverMessage.REMOVE_STATUS_UNION_TYPE,
				ObserverMessage.LIST_STATUS_UNION_TYPE
				);
	}

	public KKStatusUnionMap(KKStatusUnionMap parent) 
	{
		super(parent);
	}
	
}
