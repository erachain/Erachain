package datachain;

import org.mapdb.DB;

import datachain.DCSet;
import utils.ObserverMessage;

public class KKUnionStatusMap extends KK_Map
{
	
	public KKUnionStatusMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database, "union_status",
				ObserverMessage.RESET_UNION_STATUS_TYPE,
				ObserverMessage.ADD_UNION_STATUS_TYPE,
				ObserverMessage.REMOVE_UNION_STATUS_TYPE,
				ObserverMessage.LIST_UNION_STATUS_TYPE
				);
		}

	public KKUnionStatusMap(KKUnionStatusMap parent) 
	{
		super(parent);
	}
	
}
