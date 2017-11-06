package datachain;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import core.transaction.Transaction;
import datachain.DCSet;
import datachain.Issue_ItemMap;

public class IssueImprintMap extends Issue_ItemMap 
{
	
	public IssueImprintMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database, "imprint");
	}

	public IssueImprintMap(IssueImprintMap parent) 
	{
		super(parent);
	}
}
