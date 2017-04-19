package database;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import core.transaction.Transaction;
import database.DBSet;

public class IssueAssetMap extends Issue_ItemMap 
{
	
	public IssueAssetMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database, "asset");
	}

	public IssueAssetMap(IssueAssetMap parent) 
	{
		super(parent);
	}

}
