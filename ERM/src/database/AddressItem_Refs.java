package database;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import com.google.common.primitives.UnsignedBytes;

import core.crypto.Base58;
import database.DBSet;

public class AddressItem_Refs extends DBMap<Tuple2<byte[], Long>, byte[]> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	protected String name;

	public AddressItem_Refs(DBSet databaseSet, DB database, String name,
			int observeAdd,
			int observeRemove,
			int observeList
			)
	{
		super(databaseSet, database);
		this.name = name;

		this.observableData.put(DBMap.NOTIFY_ADD, observeAdd);
		this.observableData.put(DBMap.NOTIFY_REMOVE, observeRemove);
		this.observableData.put(DBMap.NOTIFY_LIST, observeList);

	}

	public AddressItem_Refs(AddressItem_Refs parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Tuple2<byte[], Long>, byte[]> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap(this.name + "_refs")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<byte[], Long>, byte[]> getMemoryMap() 
	{
		//return new TreeMap<Tuple2<byte[], Long>, byte[]>(UnsignedBytes.lexicographicalComparator());
		return new TreeMap<Tuple2<byte[], Long>, byte[]>();
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
	
	public byte[] get(String address, Long key)
	{
		return this.get(new Tuple2<byte[], Long>(Base58.decode(address), key));
	}
	
	public void set(String address, Long key, byte[] ref)
	{
		this.set(new Tuple2<byte[], Long>(Base58.decode(address), key), ref);
	}
	
	public void delete(String address, Long key)
	{
		this.delete(new Tuple2<byte[], Long>(Base58.decode(address), key));
	}
}
