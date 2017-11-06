package datachain;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import core.transaction.Transaction;
import datachain.DCSet;

public class Issue_ItemMap extends DCMap<byte[], Long> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	protected String name;

	public Issue_ItemMap(DCSet databaseSet, DB database, String name)
	{
		super(databaseSet, database);
		this.name = name;

	}

	public Issue_ItemMap(Issue_ItemMap parent) 
	{
		super(parent, null);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], Long> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap(this.name + "_OrphanData")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}

	@Override
	protected Map<byte[], Long> getMemoryMap() 
	{
		return new TreeMap<byte[], Long>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected Long getDefaultValue() 
	{
		return 0l;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public Long get(Transaction transaction)
	{
		return this.get(transaction.getSignature());
	}
	
	public void set(Transaction transaction, Long key)
	{
		this.set(transaction.getSignature(), key);
	}
	
	public void delete(Transaction transaction)
	{
		this.delete(transaction.getSignature());
	}
}
