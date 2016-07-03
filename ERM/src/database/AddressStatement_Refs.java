package database;

import java.util.Map;

//import java.util.HashMap;
//import java.util.Map;

//import org.mapdb.Atomic;
import org.mapdb.DB;

import core.item.ItemCls;
import utils.ObserverMessage;
import database.DBSet;
import database.serializer.ItemSerializer;
//import database.serializer.ItemSerializer;

public class AddressStatement_Refs extends AddressItem_Refs 
{
	static final String NAME = "address_statement";
	
	public AddressStatement_Refs(DBSet databaseSet, DB database)
	{
		super(databaseSet, database,
				NAME,
				ObserverMessage.ADD_STATEMENT_TYPE,
				ObserverMessage.REMOVE_STATEMENT_TYPE,
				ObserverMessage.LIST_STATEMENT_TYPE
				);		
	}

	public AddressStatement_Refs(AddressStatement_Refs parent) 
	{
		super(parent);
	}

}
