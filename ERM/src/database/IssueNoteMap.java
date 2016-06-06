package database;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import core.transaction.Transaction;
import database.DBSet;
import database.Issue_ItemMap;

public class IssueNoteMap extends Issue_ItemMap 
{
	
	public IssueNoteMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database, "note");
	}

	public IssueNoteMap(IssueNoteMap parent) 
	{
		super(parent);
	}
}
