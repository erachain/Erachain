package datachain;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.Atomic;
import org.mapdb.DB;

import core.item.ItemCls;
import core.item.assets.AssetCls;
import database.serializer.ItemSerializer;
import datachain.DCSet;
import datachain.Item_Map;
//import database.serializer.AssetSerializer;
import utils.ObserverMessage;

// income, balance, 
public class PersonAssetsBalance extends _BalanceMap 
{
	static final String NAME = "person";
	
	public PersonAssetsBalance(DCSet databaseSet, DB database)
	{
		super(databaseSet, database,
				NAME,
				ObserverMessage.RESET_BALANCE_TYPE,
				ObserverMessage.ADD_BALANCE_TYPE,
				ObserverMessage.REMOVE_BALANCE_TYPE,
				ObserverMessage.LIST_BALANCE_TYPE
				);
	}

	public PersonAssetsBalance(PersonAssetsBalance parent) 
	{
		super(parent);
	}

}
