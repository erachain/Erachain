package database;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import core.transaction.Transaction;
import database.DBSet;

// found by hash -> record signature
public class HashesMap extends DBMap<byte[], byte[]> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public HashesMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public HashesMap(HashesMap parent) 
	{
		super(parent, null);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], byte[]> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("hashes_keys")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}

	@Override
	protected Map<byte[], byte[]> getMemoryMap() 
	{
		return new TreeMap<byte[], byte[]>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected byte[] getDefaultValue() 
	{
		return null;
	}

	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
		
}
